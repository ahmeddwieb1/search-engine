import query.Document;
import query.InvertedIndex;
import query.ProximityQuery;
import query.QueryProcessor;

import java.util.ArrayList;

import static query.Document.getPositions;

public class Main {
    public static void main(String[] args) {



        Document d1 = new Document(1, "apple strawberry strawberry AND strawberry blueberry blueberry blueberry blueberry OR blueberry banana");
        Document d2 = new Document(2, "banana strawberry orange");
        Document d3 = new Document(3, "apple orange");

        InvertedIndex index = new InvertedIndex();

        index.addDocument(d1);
        index.addDocument(d2);
        index.addDocument(d3);

        QueryProcessor qp = new QueryProcessor(index);

        System.out.println(qp.search(new String[]{"apple","orange"}));

        // apple /7 banana


        ArrayList<Integer> posApple = getPositions(d1, "apple");

        ArrayList<Integer> posBanana = getPositions(d1, "banana");



        ProximityQuery pq = new ProximityQuery(posApple, posBanana, 11, false);


        System.out.println(pq.matches());


    }
}