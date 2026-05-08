package query;

import java.util.ArrayList;

public class QueryProcessor {


    private InvertedIndex index;

    public QueryProcessor(InvertedIndex index) {
        this.index = index;
    }

    public ArrayList<Integer> search(String term){

        ArrayList<Integer> result = index.getPostingList(term); // Ahmed [7 , 8 , 10]

        if (result == null) {
            return new ArrayList<>();
        }

        return result;

    }

    public ArrayList<Integer> search(String[] term){

        ArrayList<Integer> result = index.getPostingList(term[0]);

        if (result == null) {
            return new ArrayList<>();
        }

        for (int i = 1; i < term.length; i++) {

            ArrayList<Integer> next = index.getPostingList(term[i]);

            if (next == null) {
                return new ArrayList<>();
            }
            // mohamed [8,10 , 12] - nour [1 , 8 , 10 ]
            result = intersect(result, next);
        }

        return result; // [8,10]


    }

    private ArrayList<Integer> intersect(ArrayList<Integer> list1, ArrayList<Integer> list2){

        ArrayList<Integer> result = new ArrayList<>();

        for (int x : list1) {
            if (list2.contains(x)) {
                result.add(x);
            }

        }

        return result;


    }

}