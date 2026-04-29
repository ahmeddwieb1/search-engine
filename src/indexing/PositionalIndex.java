package indexing;

import java.util.*;

// store terms with their positions in docs
public class PositionalIndex {
     // KEY  ->     Value
    // term -> list of postings (docId + positions)
    private Map<String, List<Posting>> index;

    public PositionalIndex() {
        // create hashmap
        this.index = new HashMap<>();
    }

    // add term with doc and position
    public void addTerm(String term, int docId, int position) {

        // check if term exists
        // if not, create new list
        if (!index.containsKey(term)) {
            index.put(term, new ArrayList<>());
        }

        // get posting list
        //to add something new: if the word appears again in the same file
        // you must first pull up the old list in order to add the new position to it

        //to search: if the search operator wants to know the locations of the word "مايكل "
        // this method is what brings him the data that is actually stored inside the map
        List<Posting> postings = index.get(term);

        // try to find same doc
        Posting currentPosting = null;
        for (Posting p : postings) {
            // if doc already exists, reuse it
            if (p.getDocId() == docId) {
                currentPosting = p;
                break;
            }
        }

        // if doc not found, create new posting
        if (currentPosting == null) {
            currentPosting = new Posting(docId);
            postings.add(currentPosting);
        }

        // add position of term
        currentPosting.addPosition(position);
    }

    // get postings for term
    public List<Posting> getPostings(String term) {
        return index.getOrDefault(term, new ArrayList<>());
    }

    // check if term exists
    public boolean contains(String term) {
        return index.containsKey(term);
    }
}