package ranking;

import java.util.*;

public class CosineSimilarity {

    public static double calculate(
            Map<String, Double> vec1,
            Map<String, Double> vec2
    ) {

        double dot = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (String term : vec1.keySet()) {

            if (vec2.containsKey(term)) {

                dot +=
                        vec1.get(term)
                                * vec2.get(term);
            }
        }

        for (double value : vec1.values()) {
            norm1 += value * value;
        }

        for (double value : vec2.values()) {
            norm2 += value * value;
        }

        if (norm1 == 0 || norm2 == 0)
            return 0;

        return dot /
                (Math.sqrt(norm1)
                        * Math.sqrt(norm2));
    }

    public static List<Result> rankDocuments(
            String query,
            Map<Integer, Map<String, Double>> tfidfIndex,
            Map<String, Integer> df,
            int totalDocs
    ) {

        Map<String, Double> queryVector =
                TFIDFCalculator.buildQueryVector(
                        query,
                        df,
                        totalDocs
                );

        List<Result> results =
                new ArrayList<>();

        for (int docId : tfidfIndex.keySet()) {

            double score =
                    calculate(
                            queryVector,
                            tfidfIndex.get(docId)
                    );

            results.add(
                    new Result(docId, score)
            );
        }

        results.sort(
                (a, b) ->
                        Double.compare(
                                b.score,
                                a.score
                        )
        );

        return results;
    }

    public static class Result {

        public int docId;
        public double score;

        public Result(int docId, double score) {

            this.docId = docId;
            this.score = score;
        }
    }
}