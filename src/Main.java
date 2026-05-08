import indexing.Posting;
import pipeline.ArabicProcessor;
import pipeline.ArabicNormalizer;
import indexing.PositionalIndex;
import indexing.KGramIndex;
//import indexing.ProximityQuery;
import query.Document;
import query.ProximityQuery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        String stopWordsPath = "data/stopwords/ar_stopwords.txt";
        ArabicProcessor processor = new ArabicProcessor(stopWordsPath);

        PositionalIndex positionalIndex = new PositionalIndex();
        KGramIndex kGramIndex = new KGramIndex();

        List<Document> documents = Arrays.asList(
                new Document(1, Files.readString(Paths.get("data/arabic/doc1 A.txt"))),
                new Document(2, Files.readString(Paths.get("data/arabic/doc2 A.txt"))),
                new Document(3, Files.readString(Paths.get("data/arabic/doc3 A.txt"))),
                new Document(4, Files.readString(Paths.get("data/arabic/doc4 A.txt"))),
                new Document(5, Files.readString(Paths.get("data/arabic/doc5 A.txt"))),
                new Document(6, Files.readString(Paths.get("data/arabic/doc6 A.txt"))),
                new Document(7, Files.readString(Paths.get("data/arabic/doc7 A.txt"))),
                new Document(8, Files.readString(Paths.get("data/arabic/doc8 A.txt")))
        );

        for (Document doc : documents) {
            indexDocument(processor, doc, positionalIndex, kGramIndex);
        }


//        System.out.println("=== Positional Index ===");
        displayIndex(positionalIndex);

//        System.out.println("\n=== K-Gram Index (2-grams) ===");
        displayKGramIndex(kGramIndex);

        performProximityQuery(positionalIndex, "جامع", "منطق", 1, false);

    }
    private static void performProximityQuery(PositionalIndex index, String term1, String term2,
                                              int k, boolean ordered) {
        List<Posting> postings1 = index.getPostings(term1);
        List<Posting> postings2 = index.getPostings(term2);

        Map<Integer, List<Integer>> posMap1 = new HashMap<>();
        Map<Integer, List<Integer>> posMap2 = new HashMap<>();
        for (Posting p : postings1) posMap1.put(p.getDocId(), p.getPositions());
        for (Posting p : postings2) posMap2.put(p.getDocId(), p.getPositions());

        Set<Integer> commonDocs = new HashSet<>(posMap1.keySet());
        commonDocs.retainAll(posMap2.keySet());

        System.out.printf("Query: \"%s\" and \"%s\" (k=%d, ordered=%b)%n", term1, term2, k, ordered);
        if (commonDocs.isEmpty()) {
            System.out.println("  No documents contain both terms.");
        } else {
            for (int docId : commonDocs) {
                List<Integer> list1 = posMap1.get(docId);
                List<Integer> list2 = posMap2.get(docId);
                ProximityQuery pq = new ProximityQuery(
                        new ArrayList<>(list1), new ArrayList<>(list2), k, ordered);
                if (pq.matches()) {
                    System.out.println("  Doc " + docId + " matches.");
                } else {
                    System.out.println("  Doc " + docId + " does NOT match.");
                }
            }
        }
    }

    private static void indexDocument(ArabicProcessor processor, Document doc,
                                      PositionalIndex posIndex, KGramIndex kGramIndex) {
        var tokens = processor.processWithPositions(doc.getContent());
        int docId = doc.getId();

//        System.out.println("Indexing doc " + docId + ": " + doc.getContent());
//        System.out.println("  Raw tokens: " + tokens.raw);
//        System.out.println("  Processed: " + tokens.processed);

        for (int position = 0; position < tokens.processed.size(); position++) {
            String term = tokens.processed.get(position);
            if (term != null && term.length() >= 2) {
                posIndex.addTerm(term, docId, position);
                kGramIndex.addTerm(term);
//                System.out.println("    -> Added '" + term + "' at position " + position);
            } else if (term == null) {
//                System.out.println("    -> Stop word at position " + position);
            }
        }
        System.out.println();
    }


    private static void displayIndex(PositionalIndex index) {

        Map<String, List<Posting>> idx = getIndexMap(index);
        for (Map.Entry<String, List<Posting>> entry : idx.entrySet()) {
            System.out.print(entry.getKey() + " -> ");
            for (Posting p : entry.getValue()) {
                System.out.print("(doc" + p.getDocId() + ": " + p.getPositions() + ") ");
            }
            System.out.println();
        }
    }


    private static Map<String, List<Posting>> getIndexMap(PositionalIndex posIndex) {
        try {
            var field = posIndex.getClass().getDeclaredField("index");
            field.setAccessible(true);
            return (Map<String, List<Posting>>) field.get(posIndex);
        } catch (Exception e) {
//            System.err.println("Cannot access index map: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    private static void displayKGramIndex(KGramIndex kgIndex) {

        Map<String, Set<String>> map = getKGramMap(kgIndex);
//        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
//            System.out.println(entry.getKey() + " -> " + entry.getValue());
//        }
    }

    private static Map<String, Set<String>> getKGramMap(KGramIndex kgIndex) {
        try {
            var field = kgIndex.getClass().getDeclaredField("kGramMap");
            field.setAccessible(true);
            return (Map<String, Set<String>>) field.get(kgIndex);
        } catch (Exception e) {
            System.err.println("Cannot access kGramMap: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}