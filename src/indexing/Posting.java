package indexing;

import java.util.*;

// store doc id with its positions list
public class Posting { //doc1: "data is data"    ,docId = 1  , positions = [1, 3]
    private int docId;
    private List<Integer> positions;

    public Posting(int docId) {
        this.docId = docId;
        this.positions = new ArrayList<>();
    }


    //Each time the word appears:
    //its position is recorded
    public void addPosition(int pos) {
        positions.add(pos);
    }

    // get doc id
    public int getDocId() {
        return docId;
    }

    // get all positions for this doc
    public List<Integer> getPositions() {
        return positions;
    }
}