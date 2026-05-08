package pipeline;

/**
 * Porter Stemmer (1980) — pure Java, zero dependencies.
 *
 * Algorithm source: M.F. Porter, "An algorithm for suffix stripping",
 * Program, 14(3), pp.130-137, 1980.
 *
 * Usage:
 *   Stemmer s = new Stemmer();
 *   String root = s.stem("running");  // → "run"
 */
public class EnglishStemmer {

    private char[] b;   // working buffer
    private int    k;   // index of last character in b
    private int    j;   // general offset in buffer

    public EnglishStemmer() {}

    // ────────────────────────────────────────────────────────────
    // Public API
    // ────────────────────────────────────────────────────────────

    /**
     * Stem a single lowercase word.
     * Returns the original word unchanged if it is shorter than 3 chars.
     */
    public String stem(String word) {
        if (word == null || word.length() < 3) return word;
        b = word.toCharArray();
        k = word.length() - 1;
        step1ab();
        step1c();
        step2();
        step3();
        step4();
        step5();
        return new String(b, 0, k + 1);
    }

    // ────────────────────────────────────────────────────────────
    // Primitive helpers
    // ────────────────────────────────────────────────────────────

    /** Is b[i] a consonant? */
    private boolean cons(int i) {
        switch (b[i]) {
            case 'a': case 'e': case 'i': case 'o': case 'u': return false;
            case 'y': return (i == 0) || !cons(i - 1);
            default:  return true;
        }
    }

    /**
     * m() — the "measure" of the word b[0..j].
     * Counts the number of VC sequences between position 0 and j.
     */
    private int m() {
        int n = 0, i = 0;
        while (true) {
            if (i > j) return n;
            if (!cons(i)) break;
            i++;
        }
        i++;
        while (true) {
            while (true) {
                if (i > j) return n;
                if (cons(i)) break;
                i++;
            }
            i++;
            n++;
            while (true) {
                if (i > j) return n;
                if (!cons(i)) break;
                i++;
            }
            i++;
        }
    }

    /** Does b[0..j] contain a vowel? */
    private boolean vowelInStem() {
        for (int i = 0; i <= j; i++)
            if (!cons(i)) return true;
        return false;
    }

    /** Does b[j-1..j] form a double consonant? */
    private boolean doubleCons(int jj) {
        if (jj < 1) return false;
        if (b[jj] != b[jj - 1]) return false;
        return cons(jj);
    }

    /**
     * cvc(i) — is b[i-2, i-1, i] a consonant-vowel-consonant sequence,
     * AND is b[i] not w, x, or y?
     */
    private boolean cvc(int i) {
        if (i < 2 || !cons(i) || cons(i - 1) || !cons(i - 2)) return false;
        int ch = b[i];
        return ch != 'w' && ch != 'x' && ch != 'y';
    }

    /** Does the word end with the string s? Sets j to the position before s if yes. */
    private boolean endsWith(String s) {
        int l = s.length();
        int o = k - l + 1;
        if (o < 0) return false;
        for (int i = 0; i < l; i++)
            if (b[o + i] != s.charAt(i)) return false;
        j = k - l;
        return true;
    }

    /** Replace the ending starting at j+1 with the string s. */
    private void setTo(String s) {
        int l = s.length();
        int o = j + 1;
        for (int i = 0; i < l; i++) b[o + i] = s.charAt(i);
        k = j + l;
    }

    /** Replace ending with s if m() > 0. */
    private void r(String s) {
        if (m() > 0) setTo(s);
    }

    // ────────────────────────────────────────────────────────────
    // Steps 1a and 1b — plurals and -ed/-ing
    // ────────────────────────────────────────────────────────────

    private void step1ab() {
        // Step 1a
        if (endsWith("sses"))      { k -= 2; }
        else if (endsWith("ies")) { setTo("i"); }
        else if (endsWith("ss"))  { /* no-op */ }
        else if (endsWith("s"))   { k--; }

        // Step 1b
        if      (endsWith("eed")) { if (m() > 0) k--; }
        else if (endsWith("ed") || endsWith("ing")) {
            if (vowelInStem()) {
                k = j;
                if      (endsWith("at")) setTo("ate");
                else if (endsWith("bl")) setTo("ble");
                else if (endsWith("iz")) setTo("ize");
                else if (doubleCons(k)) {
                    k--;
                    int ch = b[k];
                    if (ch == 'l' || ch == 's' || ch == 'z') k++;
                }
                else if (m() == 1 && cvc(k)) setTo("e");
            }
        }
    }

    private void step1c() {
        if (endsWith("y") && vowelInStem()) b[k] = 'i';
    }

    // ────────────────────────────────────────────────────────────
    // Steps 2–4 — suffix replacement
    // ────────────────────────────────────────────────────────────

    private void step2() {
        if (k == 0) return;
        switch (b[k - 1]) {
            case 'a':
                if      (endsWith("ational")) r("ate");
                else if (endsWith("tional"))  r("tion");
                break;
            case 'c':
                if      (endsWith("enci")) r("ence");
                else if (endsWith("anci")) r("ance");
                break;
            case 'e':
                if (endsWith("izer")) r("ize");
                break;
            case 'l':
                if      (endsWith("bli"))   r("ble");
                else if (endsWith("alli"))  r("al");
                else if (endsWith("entli")) r("ent");
                else if (endsWith("eli"))   r("e");
                else if (endsWith("ousli")) r("ous");
                break;
            case 'o':
                if      (endsWith("ization")) r("ize");
                else if (endsWith("ation"))   r("ate");
                else if (endsWith("ator"))    r("ate");
                break;
            case 's':
                if      (endsWith("alism"))   r("al");
                else if (endsWith("iveness")) r("ive");
                else if (endsWith("fulness")) r("ful");
                else if (endsWith("ousness")) r("ous");
                break;
            case 't':
                if      (endsWith("aliti"))  r("al");
                else if (endsWith("iviti"))  r("ive");
                else if (endsWith("biliti")) r("ble");
                break;
            case 'g':
                if (endsWith("logi")) r("log");
                break;
        }
    }

    private void step3() {
        switch (b[k]) {
            case 'e':
                if      (endsWith("icate")) r("ic");
                else if (endsWith("ative")) r("");
                else if (endsWith("alize")) r("al");
                break;
            case 'i':
                if (endsWith("iciti")) r("ic");
                break;
            case 'l':
                if      (endsWith("ical")) r("ic");
                else if (endsWith("ful"))  r("");
                break;
            case 's':
                if (endsWith("ness")) r("");
                break;
        }
    }

    private void step4() {
        if (k == 0) return;
        switch (b[k - 1]) {
            case 'a':
                if (endsWith("al")) break; else return;
            case 'c':
                if (endsWith("ance") || endsWith("ence")) break; else return;
            case 'e':
                if (endsWith("er")) break; else return;
            case 'i':
                if (endsWith("ic")) break; else return;
            case 'l':
                if (endsWith("able") || endsWith("ible")) break; else return;
            case 'n':
                if (endsWith("ant") || endsWith("ement") ||
                        endsWith("ment") || endsWith("ent")) break; else return;
            case 'o':
                if ((endsWith("ion") && j >= 0 &&
                        (b[j] == 's' || b[j] == 't')) || endsWith("ou")) break;
                else return;
            case 's':
                if (endsWith("ism")) break; else return;
            case 't':
                if (endsWith("ate") || endsWith("iti")) break; else return;
            case 'u':
                if (endsWith("ous")) break; else return;
            case 'v':
                if (endsWith("ive")) break; else return;
            case 'z':
                if (endsWith("ize")) break; else return;
            default:
                return;
        }
        if (m() > 1) k = j;
    }

    private void step5() {
        // Step 5a
        j = k;
        if (b[k] == 'e') {
            int a = m();
            if (a > 1 || (a == 1 && !cvc(k - 1))) k--;
        }
        // Step 5b
        if (b[k] == 'l' && doubleCons(k) && m() > 1) k--;
    }

    // ────────────────────────────────────────────────────────────
    // Quick smoke-test (run: javac EnglishStemmer.java && java pipeline.Stemmer)
    // ────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        EnglishStemmer s = new EnglishStemmer();
        String[][] cases = {
                {"caresses",    "caress"},
                {"running",     "run"},
                {"happiness",   "happi"},
                {"generalization", "general"},
                {"electricity", "electr"},
                {"universities","univers"},
                {"ponies",      "poni"},
                {"troubled",    "troubl"},
        };
        int pass = 0;
        for (String[] c : cases) {
            String got = s.stem(c[0]);
            boolean ok = got.equals(c[1]);
            System.out.printf("[%s] stem(%s) = %s  (expected %s)%n",
                    ok ? "PASS" : "FAIL", c[0], got, c[1]);
            if (ok) pass++;
        }
        System.out.printf("%d/%d passed%n", pass, cases.length);
    }
}
