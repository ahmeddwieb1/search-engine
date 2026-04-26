package pipeline;

/**
 * Arabic text normalizer — pure Java, zero dependencies.
 *
 * Performs (in order):
 *   1. Alif normalization  : آ إ أ ٱ  →  ا
 *   2. Ya  normalization   : ى          →  ي
 *   3. Ta Marbuta          : ة          →  ه
 *   4. Tatweel removal     : ـ          →  (deleted)
 *   5. Diacritics removal  : Fatha, Damma, Kasra, Sukun, Shadda,
 *                            Tanwin forms  →  (deleted)
 *   6. Hamza normalization : ؤ ئ        →  ء
 *   7. Whitespace collapse : multiple spaces → single space
 *
 * Usage:
 *   ArabicNormalizer n = new ArabicNormalizer();
 *   String clean = n.normalize("الجَامِعَاتُ");   // → "الجامعات"
 */
public class ArabicNormalizer {

    // ── Unicode codepoints referenced below ───────────────────
    // Diacritics (Harakat + Tanwin + Shadda + Sukun)
    private static final String DIACRITICS =
            "\u064B"  // FATHATAN
                    + "\u064C"  // DAMMATAN
                    + "\u064D"  // KASRATAN
                    + "\u064E"  // FATHA
                    + "\u064F"  // DAMMA
                    + "\u0650"  // KASRA
                    + "\u0651"  // SHADDA
                    + "\u0652"  // SUKUN
                    + "\u0653"  // MADDAH ABOVE
                    + "\u0654"  // HAMZA ABOVE
                    + "\u0655"  // HAMZA BELOW
                    + "\u0656"  // SUBSCRIPT ALEF
                    + "\u0670"; // SUPERSCRIPT ALEF (Alef Wasla mark)

    /** Normalize a single Arabic string. */
    public String normalize(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder sb = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // 1. Alif forms → bare Alif ا
            if (c == '\u0622' // آ  ALEF WITH MADDA ABOVE
                    || c == '\u0623' // أ  ALEF WITH HAMZA ABOVE
                    || c == '\u0625' // إ  ALEF WITH HAMZA BELOW
                    || c == '\u0671' // ٱ  ALEF WASLA
                    || c == '\u0627' // ا  plain Alif (keep, already correct)
            ) {
                sb.append('\u0627'); // ا
                continue;
            }

            // 2. Ya variants → ي
            if (c == '\u0649') { // ى  ALEF MAQSURA
                sb.append('\u064A'); // ي
                continue;
            }

            // 3. Ta Marbuta → ه
            if (c == '\u0629') { // ة
                sb.append('\u0647'); // ه
                continue;
            }

            // 4. Tatweel → delete
            if (c == '\u0640') continue; // ـ

            // 5. Diacritics → delete
            if (DIACRITICS.indexOf(c) >= 0) continue;

            // 6. Hamza variants → ء
            if (c == '\u0624' // ؤ  WAW WITH HAMZA
                    || c == '\u0626' // ئ  YA WITH HAMZA
            ) {
                sb.append('\u0621'); // ء
                continue;
            }

            sb.append(c);
        }

        // 7. Collapse whitespace
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    // ── Quick test ────────────────────────────────────────────

    public static void main(String[] args) {
        ArabicNormalizer n = new ArabicNormalizer();

        Object[][] cases = {
                // input                      expected output (approx description)
                { "الجَامِعَاتُ",             "الجامعات"   },  // diacritics stripped
                { "إِنَّ",                    "ان"         },  // Hamza-below Alif + shadda
                { "آسِيَا",                   "اسيا"       },  // Madda Alif
                { "مُحَمَّدٌ",               "محمد"       },  // Tanwin + Shadda
                { "الطَّالِبَة",             "الطالبه"    },  // Ta Marbuta → ه
                { "الْعَرَبِيَّة",           "العربيه"    },
                { "يَـــا",                  "يا"         },  // Tatweel
        };

        int pass = 0;
        for (Object[] c : cases) {
            String got = n.normalize((String) c[0]);
            boolean ok = got.equals(c[1]);
            System.out.printf("[%s]  %-25s  →  %-15s  (expected %s)%n",
                    ok ? "PASS" : "FAIL", c[0], got, c[1]);
            if (ok) pass++;
        }
        System.out.printf("%d/%d passed%n", pass, cases.length);
    }
}
