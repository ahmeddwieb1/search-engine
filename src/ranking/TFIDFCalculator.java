package ranking;

import java.util.*;
import java.lang.Math;

public class TFIDFCalculator {

    public static Map<Integer, Map<String, Double>> computeTFIDF(
            Map<Integer, Map<String, Integer>> tf,
            Map<String, Integer> df,
            int totalDocs
    ) {

        Map<Integer, Map<String, Double>> tfidfIndex =
                new HashMap<>();

        for (int docId : tf.keySet()) {

            Map<String, Double> weights =
                    new HashMap<>();

            for (String term : tf.get(docId).keySet()) {

                int freq = tf.get(docId).get(term);

                double tfWeight =
                        1 + Math.log(freq);

                double idf =
                        getIDF(term, df, totalDocs);

                weights.put(term, tfWeight * idf);
            }

            tfidfIndex.put(docId, weights);
        }

        return tfidfIndex;
    }

    public static double getIDF(
            String term,
            Map<String, Integer> df,
            int totalDocs
    ) {

        if (!df.containsKey(term))
            return 0;

        return Math.log10(
                (double)(totalDocs + 1)
                        / (df.get(term) + 1)
        ) + 1;
    }

    public static Map<String, Double> buildQueryVector(
            String query,
            Map<String, Integer> df,
            int totalDocs
    ) {

        Map<String, Integer> counts =
                new HashMap<>();

        String[] tokens =
                query.toLowerCase().split("\\s+");

        for (String token : tokens) {

            counts.put(
                    token,
                    counts.getOrDefault(token, 0) + 1
            );
        }

        Map<String, Double> queryVector =
                new HashMap<>();

        for (String term : counts.keySet()) {

            if (df.containsKey(term)) {

                double tfWeight =
                        1 + Math.log(counts.get(term));

                double idf =
                        getIDF(term, df, totalDocs);

                queryVector.put(
                        term,
                        tfWeight * idf
                );
            }
        }

        return queryVector;
    }
}