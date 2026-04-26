package pipeline;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Loads and exposes a stop-word set from a plain text file.
 *
 * File format: one word per line, UTF-8 encoded, blank lines and
 * lines starting with '#' are ignored.
 *
 * Usage:
 *   StopWords en = new StopWords("data/stopwords/en_stopwords.txt");
 *   StopWords ar = new StopWords("data/stopwords/ar_stopwords.txt");
 *   boolean skip = en.contains("the");   // true
 */
public class StopWords {

    private final Set<String> words;

    /**
     * Load stop-words from the given file path.
     * Throws RuntimeException if the file cannot be read.
     */
    public StopWords(String filePath) {
        Set<String> tmp = new HashSet<>();
        File f = new File(filePath);
        if (!f.exists()) {
            System.err.println("[StopWords] WARNING: file not found: " + filePath
                    + " — stop-word filtering disabled.");
            this.words = Collections.emptySet();
            return;
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                tmp.add(line.toLowerCase());
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot read stop-words file: " + filePath, e);
        }
        this.words = Collections.unmodifiableSet(tmp);
        System.out.println("[StopWords] Loaded " + words.size()
                + " stop-words from " + filePath);
    }

    /** Returns true if this token is a stop-word and should be removed. */
    public boolean contains(String token) {
        return words.contains(token.toLowerCase());
    }

    /** Returns the full set (read-only). */
    public Set<String> getWords() {
        return words;
    }

    public int size() {
        return words.size();
    }
}
