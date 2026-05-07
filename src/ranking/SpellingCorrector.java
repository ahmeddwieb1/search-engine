package ranking;

import java.util.*;

public class SpellingCorrector {

    public static List<String> kgrams(
            String word,
            int k
    ) {

        List<String> grams =
                new ArrayList<>();

        for (int i = 0;
             i <= word.length() - k;
             i++) {

            grams.add(
                    word.substring(i, i + k)
            );
        }

        return grams;
    }

    public static double jaccard(
            List<String> a,
            List<String> b
    ) {

        Set<String> setA =
                new HashSet<>(a);

        Set<String> setB =
                new HashSet<>(b);

        Set<String> intersection =
                new HashSet<>(setA);

        intersection.retainAll(setB);

        Set<String> union =
                new HashSet<>(setA);

        union.addAll(setB);

        if (union.size() == 0)
            return 0;

        return (double)
                intersection.size()
                / union.size();
    }

    public static int levenshtein(
            String a,
            String b
    ) {

        int[][] dp =
                new int[a.length() + 1]
                        [b.length() + 1];

        for (int i = 0;
             i <= a.length();
             i++) {

            dp[i][0] = i;
        }

        for (int j = 0;
             j <= b.length();
             j++) {

            dp[0][j] = j;
        }

        for (int i = 1;
             i <= a.length();
             i++) {

            for (int j = 1;
                 j <= b.length();
                 j++) {

                int cost =
                        a.charAt(i - 1)
                                == b.charAt(j - 1)
                                ? 0 : 1;

                dp[i][j] =
                        Math.min(
                                Math.min(
                                        dp[i - 1][j] + 1,
                                        dp[i][j - 1] + 1
                                ),
                                dp[i - 1][j - 1]
                                        + cost
                        );
            }
        }

        return dp[a.length()][b.length()];
    }

    public static String suggest(
            String word,
            Set<String> dictionary
    ) {

        if (dictionary.contains(word))
            return word;

        List<String> wordGrams =
                kgrams(word, 3);

        List<Candidate> candidates =
                new ArrayList<>();

        for (String term : dictionary) {

            List<String> termGrams =
                    kgrams(term, 3);

            double score =
                    jaccard(
                            wordGrams,
                            termGrams
                    );

            if (score > 0.3) {

                candidates.add(
                        new Candidate(
                                term,
                                score
                        )
                );
            }
        }

        if (candidates.isEmpty())
            return word;

        candidates.sort(
                (a, b) ->
                        Double.compare(
                                b.score,
                                a.score
                        )
        );

        String bestWord =
                candidates.get(0).word;

        int minDistance =
                levenshtein(
                        word,
                        bestWord
                );

        for (Candidate c : candidates) {

            int distance =
                    levenshtein(
                            word,
                            c.word
                    );

            if (distance < minDistance) {

                minDistance = distance;
                bestWord = c.word;
            }
        }

        return bestWord;
    }

    public static String correctQuery(
            String query,
            Set<String> dictionary
    ) {

        String[] tokens =
                query.toLowerCase().split("\\s+");

        StringBuilder corrected =
                new StringBuilder();

        for (String token : tokens) {

            corrected.append(
                    suggest(token, dictionary)
            ).append(" ");
        }

        return corrected.toString().trim();
    }

    public static class Candidate {

        public String word;
        public double score;

        public Candidate(
                String word,
                double score
        ) {

            this.word = word;
            this.score = score;
        }
    }
}