package query;

import java.util.ArrayList;
import java.util.HashMap;

public class InvertedIndex {

    HashMap<String, ArrayList<Integer>> index;


    public InvertedIndex(){
        this.index = new HashMap();
    }


    public void addDocument(Document document) {
        int docID = document.getId();
        String content = document.getContent();


        String[] words = content.split(" ");
        for (String word : words) {
            if (!index.containsKey(word)) {
                index.put(word, new ArrayList<>());

            }
            index.get(word).add(docID);

        }

    }


    public ArrayList<Integer> getPostingList(String term) {

        if (!index.containsKey(term)) {
            return new ArrayList<>();
        }

        return index.get(term);

    }




}
