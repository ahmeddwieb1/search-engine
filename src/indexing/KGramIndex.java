package indexing;

import java.util.*;

// break words into pieces to help with spelling correction
public class KGramIndex {
    // gram -> set of full words that contain it
    private Map<String, Set<String>> kGramMap = new HashMap<>();
    private final int K = 2; // bigrams as required by the project

    public void addTerm(String term) {
// Use $ to mark the border
           String word = "$" + term + "$";

        for (int i = 0; i < word.length() - K + 1; i++) {  //$apple$
            String gram = word.substring(i, i + K);        //i=0 $a  , i=1  ap
            // using set avoids duplicates automatically
                // 1. Check if the gram already exists as a key in the map
            if (!kGramMap.containsKey(gram)) {
                // 2. If not found, initialize a new empty HashSet for this gram
                // put to create  a new place on hashset
                kGramMap.put(gram, new HashSet<>());
            }
                // 3. Retrieve the set (existing or newly created) and add the term to it
            kGramMap.get(gram).add(term);               }
                                        }

    // get all words that share the same gram
    public Set<String> getWordsForGram(String gram) {
        //getOrDefault
        //look for 'بخ'.. If you find it get its keywords,
        // if you don't find it come back with an empty HashSet
        return kGramMap.getOrDefault(gram, new HashSet<>());
    }

    // helper to break a query word into grams
    public List<String> getGrams(String term) {
        List<String> grams = new ArrayList<>();
        String word = "$" + term + "$";
        for (int i = 0; i < word.length() - K + 1; i++) {
            grams.add(word.substring(i, i + K));
        }
        return grams;
    }
}