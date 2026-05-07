import ranking.TFIDFCalculator;
import ranking.CosineSimilarity;
import ranking.SpellingCorrector;

import java.util.*;

public class Main {

    static Map<Integer, String> documents =
            new HashMap<>();

    static Map<Integer, Map<String, Integer>> tf =
            new HashMap<>();

    static Map<String, Integer> df =
            new HashMap<>();

    static Set<String> dictionary =
            new HashSet<>();

    static Map<Integer, Map<String, Double>> tfidfIndex =
            new HashMap<>();

    public static List<String> tokenize(String text) {

        return Arrays.asList(
                text.toLowerCase().split("\\s+")
        );
    }

    public static void buildIndex() {

        for (int docId : documents.keySet()) {

            List<String> tokens =
                    tokenize(documents.get(docId));

            Map<String, Integer> counts =
                    new HashMap<>();

            for (String token : tokens) {

                counts.put(
                        token,
                        counts.getOrDefault(token, 0) + 1
                );
            }

            tf.put(docId, counts);

            for (String term : counts.keySet()) {

                df.put(
                        term,
                        df.getOrDefault(term, 0) + 1
                );

                dictionary.add(term);
            }
        }
    }

    public static void evaluate(
            String query,
            List<Integer> relevantDocs
    ) {

        String fixedQuery =
                SpellingCorrector.correctQuery(
                        query,
                        dictionary
                );

        List<CosineSimilarity.Result> results =
                CosineSimilarity.rankDocuments(
                        fixedQuery,
                        tfidfIndex,
                        df,
                        documents.size()
                );

        List<Integer> retrieved =
                new ArrayList<>();

        for (CosineSimilarity.Result r : results) {

            if (r.score > 0) {
                retrieved.add(r.docId);
            }
        }

        int truePositives = 0;

        for (int doc : retrieved) {

            if (relevantDocs.contains(doc)) {
                truePositives++;
            }
        }

        double precision =
                retrieved.isEmpty()
                        ? 0
                        : (double) truePositives
                        / retrieved.size();

        double recall =
                relevantDocs.isEmpty()
                        ? 0
                        : (double) truePositives
                        / relevantDocs.size();

        System.out.println(
                "\n=============================="
        );

        System.out.println(
                "Original Query: " + query
        );

        System.out.println(
                "Did You Mean: " + fixedQuery
        );

        System.out.println("\nResults:");

        for (CosineSimilarity.Result r : results) {

            System.out.printf(
                    "Doc %d -> %.4f\n",
                    r.docId,
                    r.score
            );
        }

        System.out.printf(
                "\nPrecision: %.2f\n",
                precision
        );

        System.out.printf(
                "Recall: %.2f\n",
                recall
        );
    }

    public static void main(String[] args) {

        documents.put(
                1,
                "information retrieval is the process of searching data"
        );

        documents.put(
                2,
                "retrieval models use tf idf and cosine similarity"
        );

        documents.put(
                3,
                "machine learning improves search ranking"
        );

        documents.put(
                4,
                "deep learning is used in modern search engines"
        );

        documents.put(
                5,
                "data science and machine learning are related fields"
        );

        documents.put(
                6,
                "مرحبا بك في عالم البحث واسترجاع المعلومات"
        );

        documents.put(
                7,
                "تعلم الآلة يساعد في تحسين نتائج البحث"
        );

        documents.put(
                8,
                "محركات البحث تستخدم خوارزميات معقدة"
        );

        buildIndex();

        tfidfIndex =
                TFIDFCalculator.computeTFIDF(
                        tf,
                        df,
                        documents.size()
                );

        evaluate(
                "information retrieval",
                Arrays.asList(1, 2)
        );

        evaluate(
                "machine learning",
                Arrays.asList(3, 4, 5)
        );

        evaluate(
                "retrival modles",
                Arrays.asList(2)
        );

        evaluate(
                "البحث",
                Arrays.asList(6, 8)
        );
    }
}
