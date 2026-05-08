package ranking;

import indexing.PositionalIndex;
import indexing.Posting;
import java.util.*;

public class IndexStatistics {

    /**
     * حساب Term Frequency لكل مستند
     * @param positionIndex الـ PositionalIndex الموجود
     * @return Map<docId, Map<term, frequency>>
     */
    public static Map<Integer, Map<String, Integer>> computeTF(PositionalIndex positionIndex) {
        Map<Integer, Map<String, Integer>> tf = new HashMap<>();

        // الحصول على الـ index الداخلي
        Map<String, List<Posting>> index = positionIndex.getIndex();

        // لكل term في الفهرس
        for (Map.Entry<String, List<Posting>> entry : index.entrySet()) {
            String term = entry.getKey();

            // لكل posting (يمثل مستند)
            for (Posting posting : entry.getValue()) {
                int docId = posting.getDocId();
                int freq = posting.getPositions().size(); // عدد مرات ظهور الكلمة

                // إضافة للمستند
                tf.putIfAbsent(docId, new HashMap<>());
                tf.get(docId).put(term, tf.get(docId).getOrDefault(term, 0) + freq);
            }
        }

        return tf;
    }

    /**
     * حساب Document Frequency لكل term
     * @param positionIndex الـ PositionalIndex الموجود
     * @return Map<term, numberOfDocs>
     */
    public static Map<String, Integer> computeDF(PositionalIndex positionIndex) {
        Map<String, Integer> df = new HashMap<>();

        Map<String, List<Posting>> index = positionIndex.getIndex();

        for (Map.Entry<String, List<Posting>> entry : index.entrySet()) {
            String term = entry.getKey();
            int docCount = entry.getValue().size(); // عدد المستندات المختلفة
            df.put(term, docCount);
        }

        return df;
    }

    /**
     * الحصول على عدد المستندات الكلي في الفهرس
     */
    public static int getTotalDocuments(PositionalIndex positionIndex) {
        Set<Integer> uniqueDocs = new HashSet<>();

        Map<String, List<Posting>> index = positionIndex.getIndex();

        for (List<Posting> postings : index.values()) {
            for (Posting posting : postings) {
                uniqueDocs.add(posting.getDocId());
            }
        }

        return uniqueDocs.size();
    }

    /**
     * الحصول على كل المصطلحات الموجودة في الفهرس (للاستخدام في Spelling Correction)
     */
    public static Set<String> getDictionary(PositionalIndex positionIndex) {
        return new HashSet<>(positionIndex.getIndex().keySet());
    }
}