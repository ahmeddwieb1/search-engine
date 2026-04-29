package pipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * English linguistic pre-processing pipeline.
 *
 * Pipeline order (matches project spec):
 *   1. Lowercase
 *   2. Tokenize  — split on non-alpha characters, keep only [a-z]+ tokens
 *   3. Stop-word removal
 *   4. Porter stemming
 *
 * Shared contract (agreed with the whole team):
 *   List<String> process(String rawText)
 *
 * Usage:
 *   EnglishProcessor ep = new EnglishProcessor("data/stopwords/en_stopwords.txt");
 *   List<String> tokens = ep.process("The running cats are happy!");
 *   // → ["run", "cat", "happi"]
 */
public class EnglishProcessor {

    private final StopWords stopWords;
    private final Stemmer   stemmer;


    public EnglishProcessor(String stopWordsPath) {
        this.stopWords = new StopWords(stopWordsPath);
        this.stemmer   = new Stemmer();
    }

    // ────────────────────────────────────────────────────────────
    // Main pipeline — called by Member 4 (indexer)
    // ────────────────────────────────────────────────────────────

    /**
     * Process raw English text and return a list of stemmed tokens
     * with stop-words removed.  Returns tokens in the original order
     * (position i in the returned list corresponds to position i in
     * the document — used later by the positional index).
     */
    public List<String> process(String rawText) {
        List<String> tokens    = tokenize(rawText);
        List<String> filtered  = removeStopWords(tokens);
        List<String> stemmed   = stem(filtered);
        return stemmed;
    }

    /**
     * Process raw text but return BOTH the original positional tokens
     * (with stop-words kept, unstemmed) and the processed tokens,
     * aligned by index.  Needed by the positional index to record the
     * original character positions.
     *
     * Returns a PositionalTokens object:
     *   .raw      — original token list (lowercase, no stemming, stop-words kept)
     *   .processed — stemmed tokens parallel to raw (null entries where stop-words were)
     */
    public PositionalTokens processWithPositions(String rawText) {
        List<String> raw       = tokenize(rawText);
        List<String> processed = new ArrayList<>(raw.size());
        for (String t : raw) {
            if (stopWords.contains(t)) {
                processed.add(null);          // mark stop-word position
            } else {
                processed.add(stemmer.stem(t));
            }
        }
        return new PositionalTokens(raw, processed);
    }

    // ────────────────────────────────────────────────────────────
    // Individual pipeline steps (package-visible for unit tests)
    // ────────────────────────────────────────────────────────────

    /**
     * Step 1 + 2: lowercase and tokenize.
     * Splits on anything that is not a–z after lowercasing.
     * Single-character tokens are discarded (noise).
     */
    List<String> tokenize(String text) {
        String lower  = text.toLowerCase();
        String[] raw  = lower.split("[^a-z]+");
        List<String> tokens = new ArrayList<>(raw.length);
        for (String t : raw) {
            if (t.length() > 1) tokens.add(t);   // drop single chars and empty strings
        }
        return tokens;
    }

    /**
     * Step 3: remove stop-words.
     */
    List<String> removeStopWords(List<String> tokens) {
        List<String> result = new ArrayList<>(tokens.size());
        for (String t : tokens) {
            if (!stopWords.contains(t)) result.add(t);
        }
        return result;
    }

    /**
     * Step 4: apply Porter stemmer to each token.
     */
    List<String> stem(List<String> tokens) {
        List<String> result = new ArrayList<>(tokens.size());
        for (String t : tokens) {
            result.add(stemmer.stem(t));
        }
        return result;
    }

    // ────────────────────────────────────────────────────────────
    // Helper class returned by processWithPositions()
    // ────────────────────────────────────────────────────────────

    public static class PositionalTokens {
        /** Original lowercase tokens (stop-words included, unstemmed). */
        public final List<String> raw;
        /** Stemmed tokens, null at positions where raw[i] was a stop-word. */
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
        // Adjust path if running from a different working directory
        EnglishProcessor ep = new EnglishProcessor("data/stopwords/en_stopwords.txt");

//        String[] sentences = {
//                "The quick brown foxes are jumping over the lazy dogs",
//                "Information retrieval systems use TF-IDF weighting",
//                "Universities and colleges are educational institutions",
//                "Running, walked, happily generalization",
//        };
//
//        for (String s : sentences) {
//            System.out.println("IN : " + s);
//            System.out.println("OUT: " + ep.process(s));
//            System.out.println();
//        }
        try {
            String content = Files.readString(
                    Paths.get("data/english/doc1 E.txt")
            );

            System.out.println("IN : " + content);
            System.out.println("OUT: " + ep.process(content));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
