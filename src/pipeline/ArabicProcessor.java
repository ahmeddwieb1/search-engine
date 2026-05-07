package pipeline;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Arabic linguistic pre-processing pipeline.
 *
 * Pipeline order (matches project spec):
 *   1. Normalize     — unify Alif/Ya/Ta-Marbuta, strip diacritics & Tatweel
 *   2. Tokenize      — split on non-Arabic characters
 *   3. Stop-word removal
 *   4. ISRI light stemming
 *
 * Shared contract (identical signature to EnglishProcessor):
 *   List<String> process(String rawText)
 *
 * Usage:
 *   ArabicProcessor ap = new ArabicProcessor("data/stopwords/ar_stopwords.txt");
 *   List<String> tokens = ap.process("الجامعات العربية في المنطقة");
 *   // → ["جامع", "عرب", "منطق"]
 */
public class ArabicProcessor {

    // A Unicode Arabic character falls in the block U+0600–U+06FF
    // We keep only actual Arabic letters: U+0621–U+064A (basic block)
    // plus U+0671 (Alef Wasla).  Everything else (digits, Latin, punct) = delimiter.
    private static final String ARABIC_WORD_REGEX = "[^\\u0621-\\u064A\\u0671]+";

    private final ArabicNormalizer normalizer;
    private final StopWords        stopWords;
    private final ArabicStemmer    stemmer;

    /**
     * @param stopWordsPath  path to ar_stopwords.txt  (UTF-8)
     */
    public ArabicProcessor(String stopWordsPath) {
        this.normalizer = new ArabicNormalizer();
        this.stopWords  = new StopWords(stopWordsPath);
        this.stemmer    = new ArabicStemmer();
    }

    // ────────────────────────────────────────────────────────────
    // Main pipeline — called by Member 4 (indexer)
    // ────────────────────────────────────────────────────────────

    /**
     * Process raw Arabic text and return a list of stemmed tokens
     * with stop-words removed, in document order.
     *
     * This is the method Member 4 calls — identical contract to
     * EnglishProcessor.process().
     */
    public List<String> process(String rawText) {
        String       normalized = normalizer.normalize(rawText);
        List<String> tokens     = tokenize(normalized);
        List<String> filtered   = removeStopWords(tokens);
        List<String> stemmed    = stem(filtered);
        return stemmed;
    }

    /**
     * Process raw text and return positional information.
     * null entries in .processed mark positions that were stop-words.
     *
     * Member 4 uses this for building the positional inverted index.
     */
    public PositionalTokens processWithPositions(String rawText) {
        String       normalized = normalizer.normalize(rawText);
        List<String> raw        = tokenize(normalized);
        List<String> processed  = new ArrayList<>(raw.size());

        for (String t : raw) {
            if (stopWords.contains(t)) {
                processed.add(null);                    // stop-word position
            } else {
                processed.add(stemmer.stem(t));
            }
        }
        return new PositionalTokens(raw, processed);
    }

    // ────────────────────────────────────────────────────────────
    // Individual steps (package-visible for unit tests)
    // ────────────────────────────────────────────────────────────

    /**
     * Tokenize: split on non-Arabic characters.
     * Discards tokens shorter than 2 characters.
     */
    List<String> tokenize(String normalizedText) {
        String[] parts = normalizedText.split(ARABIC_WORD_REGEX);
        List<String> tokens = new ArrayList<>(parts.length);
        for (String t : parts) {
            if (t.length() >= 2) tokens.add(t);
        }
        return tokens;
    }

    /**
     * Remove Arabic stop-words.
     * Comparison is done on the normalized form (no diacritics).
     */
    List<String> removeStopWords(List<String> tokens) {
        List<String> result = new ArrayList<>(tokens.size());
        for (String t : tokens) {
            if (!stopWords.contains(t)) result.add(t);
        }
        return result;
    }

    /**
     * Apply ISRI light stemmer to each token.
     */
    List<String> stem(List<String> tokens) {
        List<String> result = new ArrayList<>(tokens.size());
        for (String t : tokens) {
            result.add(stemmer.stem(t));
        }
        return result;
    }

    // ────────────────────────────────────────────────────────────
    // Helper class — mirrors EnglishProcessor.PositionalTokens
    // ────────────────────────────────────────────────────────────

    public static class PositionalTokens {
        /** Normalized tokens (stop-words included, unstemmed). */
        public final List<String> raw;
        /** Stemmed tokens — null at positions of stop-words. */
        public final List<String> processed;

        PositionalTokens(List<String> raw, List<String> processed) {
            this.raw       = raw;
            this.processed = processed;
        }
    }

    // ────────────────────────────────────────────────────────────
    // Quick smoke-test
    // ────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        ArabicProcessor ap = new ArabicProcessor("data/stopwords/ar_stopwords.txt");

//        String[] sentences = {
//                "الجامعات العربية في المنطقة",
//                "يدرس الطلاب المعلومات في المكتبة",
//                "الاقتصاد والسياسة والعلوم الاجتماعية",
//                "على المدارس والمعلمين تطوير المناهج",
//        };
//
//        System.out.println("=== process() ===");
//        for (String s : sentences) {
//            System.out.println("IN : " + s);
//            System.out.println("OUT: " + ap.process(s));
//            System.out.println();
//        }
        try {
            String content = Files.readString(
                    Paths.get("data/arabic/doc1 A.txt")
            );

            System.out.println("IN : " + content);
            System.out.println("OUT: " + ap.process(content));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
