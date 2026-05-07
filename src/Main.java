import query.Document;
import query.InvertedIndex;
import query.ProximityQuery;
import query.QueryProcessor;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {



        Document d1 = new Document(1, "apple banana");
        Document d2 = new Document(2, "banana orange");
        Document d3 = new Document(3, "apple orange");

        InvertedIndex index = new InvertedIndex();

        index.addDocument(d1);
        index.addDocument(d2);
        index.addDocument(d3);

        QueryProcessor qp = new QueryProcessor(index);


        ArrayList<Integer> posApple = index.getPostingList("apple");

        ArrayList<Integer> posBanana = index.getPostingList("banana");

        ProximityQuery pq = new ProximityQuery(posApple, posBanana, , false);

        System.out.println(pq.matches());


    }
}