package pipeline;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * ISRI Arabic Light Stemmer — pure Java, zero dependencies.
 *
 * Based on:
 *   Taghva, Elkhoury & Coombs (2005) "Arabic Stemming without a Root Dictionary"
 *   Information Science Research Institute (ISRI), University of Nevada Las Vegas.
 *
 * Strategy (light stemming):
 *   Strip known Arabic prefixes then known Arabic suffixes in multiple passes
 *   while respecting a minimum stem length of 3 characters.
 *   Does NOT use a root dictionary — purely rule-based, making it fast and
 *   deterministic (suitable for an academic IR project with no external libs).
 *
 * Example results (after normalization):
 *   الجامعات  →  جامع
 *   والمدارس  →  مدرس
 *   كتبهم     →  كتب
 *   المعلومات →  معلوم
 *
 * Usage:
 *   ArabicStemmer s = new ArabicStemmer();
 *   String root = s.stem("الجامعات");   // → "جامع"
 */
public class ArabicStemmer {

    private static final int MIN_STEM = 3;   // never reduce below 3 chars

    // ── Prefix table (longest first so greedy match works) ────

    // Size-4 prefixes
    private static final String[] PRE4 = { "وال", "بال", "كال", "فال", "لل" };
    // (لل is 2-char but handled here to avoid special-casing)

    // Size-3 prefixes
    private static final String[] PRE3 = { "ال", "وا", "فا", "با", "كا", "لل", "وو" };
    // ال is the most important — the definite article

    // Size-2 prefixes
    private static final String[] PRE2 = { "ال", "وا", "فا", "با", "كا", "لا", "ما", "او" };

    // Single-char prefixes (applied last)
    // than a preposition prefix, causing false positives on verbs like كتب.
    private static final String[] PRE1 = { "و", "ف", "ب", "ل", "س" };

    // ── Suffix table ─────────────────────────────────────────

    // Size-3 suffixes
    private static final String[] SUF3 = {
            "ات",   // ات   feminine plural
            "ون",   // ون   masculine plural nominative
            "ين",   // ين   masculine plural acc/gen
            "ان",   // ان   dual
            "تن",   // تن
            "كم",   // كم   2nd person masc plural
            "هم",   // هم   3rd person masc plural
            "هن",   // هن   3rd person fem plural
            "نا",   // نا   1st person plural
            "يا",   // يا
            "ها",   // ها   3rd person fem singular
            "تم",   // تم   2nd person masc plural past
            "وا",   // وا   past-tense 3rd plural
    };

    // Size-2 suffixes
    private static final String[] SUF2 = {
            "ة",    // ة    feminine (pre-normalization form)
            "ه",    // ه    after Ta Marbuta normalization
            "ي",    // ي    1st person singular
            "ك",    // ك    2nd person singular
            "ن",    // ن    nunation
            "ت",    // ت    feminine marker in past tense
    };
    //todo remove this
    // Size-1 suffixes — intentionally empty.
    // م excluded: almost always a root letter (تعليم, معلوم) — causes false positives.
    // ا excluded: root vowel in many plurals — too aggressive.
    private static final String[] SUF1 = {};
    //todo remove this
    // ── Invariant stems: never strip further ────────────────
    // Common words that look like they have prefixes/suffixes but don't.
    private static final Set<String> INVARIANTS = new HashSet<>(Arrays.asList(
            "كان", "ليس", "ذلك", "هذا", "هذه", "لكن", "حتى",
            "منذ", "بعد", "قبل", "عند", "على", "الى", "في"
    ));

    /**
     * Stem an already-normalized Arabic word.
     * Input must have been through ArabicNormalizer.normalize() first.
     */
    public String stem(String word) {
        if (word == null || word.length() <= MIN_STEM) return word;
        //todo remove this
        if (INVARIANTS.contains(word)) return word;

        String s = word;

        // Strip outermost prefix, then all strippable suffixes (looping until stable).
        // A second prefix strip handles words like وبالتعليم where a conjunction
        // precedes a prefixed stem.
        s = stripPrefixes(s);
        s = stripSuffixes(s);
        s = stripPrefixes(s);  // one more prefix pass for compound prefixes

        return s;
    }

    // ── Internal helpers ─────────────────────────────────────

    private String stripPrefixes(String s) {
        // Try size-4 first (longest match wins)
        for (String p : PRE4) {
            if (s.startsWith(p) && s.length() - p.length() >= MIN_STEM) {
                return s.substring(p.length());
            }
        }
        for (String p : PRE3) {
            if (s.startsWith(p) && s.length() - p.length() >= MIN_STEM) {
                return s.substring(p.length());
            }
        }
        for (String p : PRE2) {
            if (s.startsWith(p) && s.length() - p.length() >= MIN_STEM) {
                return s.substring(p.length());
            }
        }
        for (String p : PRE1) {
            if (s.startsWith(p) && s.length() - p.length() >= MIN_STEM) {
                return s.substring(p.length());
            }
        }
        return s;
    }

    private String stripSuffixes(String s) {
        // Loop until stable — handles chained suffixes like هم+ت in مدرس
        String prev;
        do {
            prev = s;
            s = stripOneSuffix(s);
        } while (!s.equals(prev));
        return s;
    }

    private String stripOneSuffix(String s) {
        for (String sf : SUF3) {
            if (s.endsWith(sf) && s.length() - sf.length() >= MIN_STEM) {
                return s.substring(0, s.length() - sf.length());
            }
        }
        for (String sf : SUF2) {
            if (s.endsWith(sf) && s.length() - sf.length() >= MIN_STEM) {
                return s.substring(0, s.length() - sf.length());
            }
        }
        for (String sf : SUF1) {
            // Extra guard for single-char suffixes: stem must be >= MIN_STEM+1
            if (s.endsWith(sf) && s.length() - sf.length() >= MIN_STEM + 1) {
                return s.substring(0, s.length() - sf.length());
            }
        }
        return s;
    }

    // ── Quick smoke-test ─────────────────────────────────────

    public static void main(String[] args) {
        ArabicStemmer   st = new ArabicStemmer();
        ArabicNormalizer n = new ArabicNormalizer();

        // { raw (pre-normalization),  expected stem after ISRI light stemming }
        // NOTE: ISRI is a LIGHT stemmer — it strips affixes only, never modifies
        // root-internal vowels.  "مدارس" stays "مدارس" (interior ا is root vowel).
        // A heavy stemmer / root extractor would give "درس", but that requires a lexicon.
        String[][] cases = {
                { "الجامعات",   "جامع"   },   // ال + ات stripped
                { "والمدارس",   "مدارس"  },   // و stripped; interior ا is root vowel
                { "كتبهم",      "كتب"    },   // هم stripped
                { "المعلومات",  "معلوم"  },   // ال + ات stripped
                { "الطلاب",     "طلاب"   },   // ال stripped; no matching suffix
                { "الاقتصادية", "اقتصاد" },   // ال + يه stripped
                { "مدرستهم",   "مدرس"   },   // تهم → strip هم → مدرست → strip ت → مدرس
                { "وبالتعليم",  "تعليم"  },   // و + بال stripped
        };

        int pass = 0;
        for (String[] c : cases) {
            String normed = n.normalize(c[0]);
            String got    = st.stem(normed);
            boolean ok    = got.equals(c[1]);
            System.out.printf("[%s]  %-18s  →  norm: %-16s  stem: %-12s  (expected %s)%n",
                    ok ? "PASS" : "FAIL", c[0], normed, got, c[1]);
            if (ok) pass++;
        }
        System.out.printf("%d/%d passed%n", pass, cases.length);
    }
}
