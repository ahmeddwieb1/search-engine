package query;

import indexing.PositionalIndex;
import indexing.Posting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InvertedIndex {

    private PositionalIndex positionalIndex;

    public InvertedIndex(PositionalIndex positionalIndex){
        this.positionalIndex = positionalIndex;
    }

    // keep method for compatibility but delegate to positional index
    public void addDocument(Document document) {
        // This method intentionally left blank — indexing happens via PositionalIndex elsewhere
        // If needed, callers should add terms to PositionalIndex directly.
    }

    // Return posting list as list of doc IDs (no positions)
    public ArrayList<Integer> getPostingList(String term) {
        ArrayList<Integer> result = new ArrayList<>();
        List<Posting> postings = positionalIndex.getPostings(term);
        if (postings == null || postings.isEmpty()) return result;

        Set<Integer> seen = new HashSet<>();
        for (Posting p : postings) {
            if (!seen.contains(p.getDocId())) {
                result.add(p.getDocId());
                seen.add(p.getDocId());
            }
        }
        return result;
    }

}
