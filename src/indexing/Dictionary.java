package indexing;

import java.util.*;

public class Dictionary {
    // holds positional index (docs + positions) || term → docId + position
    private PositionalIndex positionalIndex;

    // used for spelling || grams → words spelling correction
    private KGramIndex kGramIndex;

    public Dictionary() {
        // create objects
        this.positionalIndex = new PositionalIndex();
        this.kGramIndex = new KGramIndex();
    }

    public void addTerm(String term, int docId, int position) {

        // normalize term
        term = term.toLowerCase().trim();

        // check if term already exists before adding to k-gram
        boolean isNewTerm = !positionalIndex.contains(term);

        // send term to positional index
        positionalIndex.addTerm(term, docId, position);
        //doc1: "      data STRUCture        "
        //"data structure"
        //data → (doc1, pos 1)
        //structure → (doc1, pos 2)


        // also send it to k-gram only if new
        if (isNewTerm) {
            kGramIndex.addTerm(term);
        }
    }

    // get posting list for a word (needed for search)
    public List<Posting> getPostings(String term) {
        return positionalIndex.getPostings(term);
    }

    // check if word is in our dictionary
    public boolean contains(String term) {
        return positionalIndex.contains(term);
    }

    // get k-gram object for spelling corrector
    public KGramIndex getKGramIndex() {
        return kGramIndex;
    }

    // get positional index object
    public PositionalIndex getPositionalIndex() {
        return positionalIndex;
    }
}