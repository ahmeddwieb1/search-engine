package ranking;

import indexing.PositionalIndex;
import pipeline.ArabicProcessor;
import pipeline.EnglishProcessor;
import java.util.*;

public class RankedSearch {

    private PositionalIndex positionalIndex;
    private ArabicProcessor arabicProcessor;
    private EnglishProcessor englishProcessor;

    public RankedSearch(PositionalIndex positionalIndex,
                        ArabicProcessor arabicProcessor,
                        EnglishProcessor englishProcessor) {
        this.positionalIndex = positionalIndex;
        this.arabicProcessor = arabicProcessor;
        this.englishProcessor = englishProcessor;
    }

    /**
     * البحث مع ranking (TF-IDF + Cosine Similarity)
     * @param query استعلام المستخدم (نص خام)
     * @param language "ar" أو "en"
     * @return قائمة بالنتائج مرتبة تنازلياً
     */
    public List<CosineSimilarity.Result> search(String query, String language) {
        // 1. معالجة الاستعلام (نفس pipeline)
        List<String> processedQueryTerms = processQuery(query, language);

        if (processedQueryTerms.isEmpty()) {
            System.out.println("No valid terms in query after processing.");
            return new ArrayList<>();
        }

        System.out.println("Processed query: " + processedQueryTerms);

        // 2. بناء الاستعلام كـ String واحدة (لـ buildQueryVector)
        String processedQueryString = String.join(" ", processedQueryTerms);

        // 3. حساب إحصائيات الفهرس
        Map<Integer, Map<String, Integer>> tf = IndexStatistics.computeTF(positionalIndex);
        Map<String, Integer> df = IndexStatistics.computeDF(positionalIndex);
        int totalDocs = IndexStatistics.getTotalDocuments(positionalIndex);

        if (totalDocs == 0) {
            System.out.println("No documents indexed. Please index documents first.");
            return new ArrayList<>();
        }

        // 4. حساب TF-IDF vectors للمستندات
        Map<Integer, Map<String, Double>> tfidfIndex =
                TFIDFCalculator.computeTFIDF(tf, df, totalDocs);

        // 5. حساب النتائج مرتبة
        List<CosineSimilarity.Result> results =
                CosineSimilarity.rankDocuments(processedQueryString, tfidfIndex, df, totalDocs);

        // 6. إزالة النتائج ذات الدرجة 0
        results.removeIf(r -> r.score == 0);

        return results;
    }

    /**
     * معالجة الاستعلام (نفس pipeline اللي استخدمناه مع المستندات)
     */
    private List<String> processQuery(String query, String language) {
        if (language.equals("ar")) {
            return arabicProcessor.process(query);
        } else {
            return englishProcessor.process(query);
        }
    }
}