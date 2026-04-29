package TestIndexing;

import indexing.Dictionary;
import indexing.Posting;
import java.util.*; // عشان Set و List

public class TestIndexing {
    public static void main(String[] args) {
        Dictionary dict = new Dictionary();

        // 1. Adding Arabic and English terms
        dict.addTerm("جامعة", 1, 5);
        dict.addTerm("جامعة", 1, 20);
        dict.addTerm("computer", 2, 3);

        // --- Testing Positional Index ---
        System.out.println("--- Testing Positional Index ---");
        if (dict.contains("جامعة")) {
            List<Posting> postings = dict.getPostings("جامعة");
            for (Posting p : postings) {
                System.out.println("Word 'جامعة' found in Doc: " + p.getDocId());
                System.out.println("Positions: " + p.getPositions()); // Expected: [5, 20]
            }
        }

        // --- Testing K-Gram Index ---
        System.out.println("\n--- Testing K-Gram Index ---");
        // --- Testing K-Gram Index ---
        System.out.println("\n--- Testing K-Gram Index ---");

// 1. Test with Arabic gram from "جامعة"
        Set<String> arabicMatches = dict.getKGramIndex().getWordsForGram("ام");
        System.out.println("Words containing Arabic 'ام': " + arabicMatches);

// 2. Test with English gram from "computer"
        Set<String> englishMatches = dict.getKGramIndex().getWordsForGram("om");
        System.out.println("Words containing English 'om': " + englishMatches);
        // We changed this to Set to be more efficient
        Set<String> matches = dict.getKGramIndex().getWordsForGram("am");
        System.out.println("Words containing 'am': " + matches); // Expected: [جامعة, computer]
    }
}