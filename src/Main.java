import indexing.Posting;
import pipeline.ArabicProcessor;
import indexing.PositionalIndex;
import indexing.KGramIndex;
import pipeline.EnglishProcessor;
import query.Document;
import query.ProximityQuery;
import ranking.CosineSimilarity;
import ranking.IndexStatistics;
import ranking.RankedSearch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Scanner;

public class Main {

    private static PositionalIndex positionalIndex = new PositionalIndex();
    private static KGramIndex kGramIndex = new KGramIndex();
    private static ArabicProcessor ARprocessor;
    private static EnglishProcessor ENprocessor;

    public static void startmenu() throws IOException {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("        INFORMATION RETRIEVAL SYSTEM");
            System.out.println("=".repeat(50));
            System.out.println("1. Index Arabic Documents");
            System.out.println("2. Index English Documents");
            System.out.println("3. Index All Documents (Arabic + English)");
            System.out.println("4. Ranked Search (TF-IDF)");
            System.out.println("5. Proximity Search (/k operator)");
            System.out.println("6. Display Positional Index");
            System.out.println("7. Spelling Correction (K-Gram)");
            System.out.println("0. Exit");
            System.out.println("=".repeat(50));
            System.out.print("Enter Your Choice: ");

            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    indexArabicDocuments();
                    break;
                case 2:
                    indexEnglishDocuments();
                    break;
                case 3:
                    indexAllDocuments();
                    break;
                case 4:
                    rankedSearch(scanner);
                    break;
                case 5:
                    proximitySearch(scanner);
                    break;
                case 6:
                    displayIndex(positionalIndex);
                    break;
                case 7:
                    spellingCorrection(scanner);
                    break;
                case 0:
                    System.out.println("Exiting system. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        } while (choice != 0);

        scanner.close();
    }

    public static void main(String[] args) throws IOException {
        // Initialize processors and indices
        String stopWordsEnPath = "data/stopwords/en_stopwords.txt";
        String stopWordsARPath = "data/stopwords/ar_stopwords.txt";
        ARprocessor = new ArabicProcessor(stopWordsARPath);
        ENprocessor = new EnglishProcessor(stopWordsEnPath);
//        indexAllDocuments();

//        ArrayList<String> stemmedEN = ENprocessor.process();

        // Start the interactive menu
        startmenu();
    }

    // ==================== INDEXING METHODS ====================

    private static void indexArabicDocuments() throws IOException {
        System.out.println("\n--- Indexing Arabic Documents ---");

        List<Document> ardocuments = Arrays.asList(
                new Document(1, Files.readString(Paths.get("data/arabic/doc1 A.txt"))),
                new Document(2, Files.readString(Paths.get("data/arabic/doc2 A.txt"))),
                new Document(3, Files.readString(Paths.get("data/arabic/doc3 A.txt"))),
                new Document(4, Files.readString(Paths.get("data/arabic/doc4 A.txt"))),
                new Document(5, Files.readString(Paths.get("data/arabic/doc5 A.txt"))),
                new Document(6, Files.readString(Paths.get("data/arabic/doc6 A.txt"))),
                new Document(7, Files.readString(Paths.get("data/arabic/doc7 A.txt"))),
                new Document(8, Files.readString(Paths.get("data/arabic/doc8 A.txt"))),
                new Document(9, Files.readString(Paths.get("data/arabic/doc9 A.txt"))),
                new Document(10, Files.readString(Paths.get("data/arabic/doc10 A.txt")))
        );

        for (Document doc : ardocuments) {
            indexDocumentAR(ARprocessor, doc, positionalIndex, kGramIndex);
        }

        System.out.println("✓ Arabic documents indexed successfully!");
    }

    private static void indexEnglishDocuments() throws IOException {
        System.out.println("\n--- Indexing English Documents ---");

        List<Document> Endocuments = Arrays.asList(
                new Document(11, Files.readString(Paths.get("data/english/doc1 E.txt"))),
                new Document(12, Files.readString(Paths.get("data/english/doc2 E.txt"))),
                new Document(13, Files.readString(Paths.get("data/english/doc3 E.txt"))),
                new Document(14, Files.readString(Paths.get("data/english/doc4 E.txt"))),
                new Document(15, Files.readString(Paths.get("data/english/doc5 E.txt"))),
                new Document(16, Files.readString(Paths.get("data/english/doc6 E.txt"))),
                new Document(17, Files.readString(Paths.get("data/english/doc7 E.txt"))),
                new Document(18, Files.readString(Paths.get("data/english/doc8 E.txt"))) ,
                new Document(19, Files.readString(Paths.get("data/english/doc9 E.txt"))),
                new Document(20, Files.readString(Paths.get("data/english/doc10 E.txt")))
        );

        for (Document doc : Endocuments) {
            indexDocumentEN(ENprocessor, doc, positionalIndex, kGramIndex);
        }

        System.out.println("✓ English documents indexed successfully!");
    }

    private static void indexAllDocuments() throws IOException {
        System.out.println("\n--- Indexing All Documents (Arabic + English) ---");
        indexArabicDocuments();
        indexEnglishDocuments();
        System.out.println("✓ All documents indexed successfully!");
    }

    // ==================== SEARCH METHODS ====================

    private static void searchSingleTerm(Scanner scanner) {
        System.out.print("\nEnter search term: ");
        String term = scanner.nextLine().toLowerCase();

        System.out.println("\n--- Searching for: '" + term + "' ---");
        List<Posting> postings = positionalIndex.getPostings(term);

        if (postings.isEmpty()) {
            System.out.println("No documents found containing '" + term + "'");
        } else {
            System.out.println("Found in " + postings.size() + " document(s):");
            for (Posting p : postings) {
                System.out.println("  Doc " + p.getDocId() + " at positions: " + p.getPositions());
            }
        }
    }

    private static void searchMultipleTerms(Scanner scanner) {
        System.out.print("\nEnter search terms (space-separated): ");
        String line = scanner.nextLine();
        String[] terms = line.toLowerCase().split(" ");

        System.out.println("\n--- Searching for: " + Arrays.toString(terms) + " (AND) ---");

        if (terms.length == 0) {
            System.out.println("No terms entered.");
            return;
        }

        List<Posting> result = positionalIndex.getPostings(terms[0]);
        Set<Integer> resultDocs = new HashSet<>();
        for (Posting p : result) resultDocs.add(p.getDocId());

        for (int i = 1; i < terms.length; i++) {
            List<Posting> next = positionalIndex.getPostings(terms[i]);
            Set<Integer> nextDocs = new HashSet<>();
            for (Posting p : next) nextDocs.add(p.getDocId());
            resultDocs.retainAll(nextDocs);
        }

        if (resultDocs.isEmpty()) {
            System.out.println("No documents contain all terms.");
        } else {
            System.out.println("Found in " + resultDocs.size() + " document(s):");
            for (int docId : resultDocs) {
                System.out.println("  Doc " + docId);
            }
        }
    }

    private static void proximitySearch(Scanner scanner) {
        System.out.print("\nEnter first term: ");
        String term1 = scanner.nextLine().trim();

        System.out.print("Enter second term: ");
        String term2 = scanner.nextLine().trim();

        System.out.print("Enter maximum distance (k): ");
        int k = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Ordered? (true/false): ");
        boolean ordered = Boolean.parseBoolean(scanner.nextLine().trim());

        // معالجة الكلمتين بنفس الـ pipeline عشان يتطابقوا مع الـ index
        List<String> tokens1 = isArabic(term1)
                ? ARprocessor.process(term1)
                : ENprocessor.process(term1);

        List<String> tokens2 = isArabic(term2)
                ? ARprocessor.process(term2)
                : ENprocessor.process(term2);

        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            System.out.println("Warning: One or both terms are stop-words or invalid.");
            return;
        }

        String processedTerm1 = tokens1.get(0);
        String processedTerm2 = tokens2.get(0);

        System.out.println("Searching for: '" + processedTerm1 + "' and '" + processedTerm2 + "'");

        performProximityQuery(positionalIndex, processedTerm1, processedTerm2, k, ordered);
    }
    private static void spellingCorrection(Scanner scanner) {
        System.out.print("\nEnter misspelled word or phrase: ");
        String misspelled = scanner.nextLine().trim();

        System.out.println("\n--- Spelling Correction for: '" + misspelled + "' ---");

        // use IndexStatistics dictionary (terms from positional index)
        Set<String> dict = IndexStatistics.getDictionary(positionalIndex);

        if (dict.isEmpty()) {
            System.out.println("Dictionary is empty. Please index documents first.");
            return;
        }

        String corrected = ranking.SpellingCorrector.correctQuery(misspelled, dict);

        System.out.println("Corrected query: " + corrected);

        if (!corrected.equalsIgnoreCase(misspelled)) {
            System.out.println("Did you mean: " + corrected + " ?");
        } else {
            System.out.println("No suggestions. Term(s) appear in dictionary or no close candidates.");
        }
    }

    // ==================== INDEXING HELPERS ====================

    private static void indexDocumentAR(ArabicProcessor processor, Document doc,
                                        PositionalIndex posIndex, KGramIndex kGramIndex) {
        var tokens = processor.processWithPositions(doc.getContent());
        int docId = doc.getId();

        for (int position = 0; position < tokens.processed.size(); position++) {
            String term = tokens.processed.get(position);
            if (term != null && term.length() >= 2) {
                posIndex.addTerm(term, docId, position);
                kGramIndex.addTerm(term);
            }
        }
        System.out.print(".");
    }

    private static void indexDocumentEN(EnglishProcessor processor, Document doc,
                                        PositionalIndex posIndex, KGramIndex kGramIndex) {
        var tokens = processor.processWithPositions(doc.getContent());
        int docId = doc.getId();

        for (int position = 0; position < tokens.processed.size(); position++) {
            String term = tokens.processed.get(position);
            if (term != null && term.length() >= 2) {
                posIndex.addTerm(term, docId, position);
                kGramIndex.addTerm(term);
            }
        }
        System.out.print(".");
    }

    // ==================== DISPLAY METHODS ====================

    private static void displayIndex(PositionalIndex index) {
        System.out.println("\n=== Positional Index ===");
        Map<String, List<Posting>> idx = getIndexMap(index);

        if (idx.isEmpty()) {
            System.out.println("Index is empty. Please index documents first.");
            return;
        }

        int count = 0;
        for (Map.Entry<String, List<Posting>> entry : idx.entrySet()) {
            if (count++ >= 30) {
                System.out.println("... and " + (idx.size() - 30) + " more terms");
                break;
            }
            System.out.print(entry.getKey() + " -> ");
            for (Posting p : entry.getValue()) {
                System.out.print("(doc" + p.getDocId() + ": " + p.getPositions() + ") ");
            }
            System.out.println();
        }
        System.out.println("Total unique terms: " + idx.size());
    }

    private static void displayKGramIndex(KGramIndex kgIndex) {
        System.out.println("\n=== K-Gram Index (2-grams) ===");
        Map<String, Set<String>> map = getKGramMap(kgIndex);

        if (map.isEmpty()) {
            System.out.println("K-Gram index is empty. Please index documents first.");
            return;
        }

        int count = 0;
        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            if (count++ >= 20) {
                System.out.println("... and " + (map.size() - 20) + " more grams");
                break;
            }
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println("Total unique grams: " + map.size());
    }

    // ==================== QUERY HELPERS ====================

    private static void performProximityQuery(PositionalIndex index, String term1, String term2,
                                              int k, boolean ordered) {
        List<Posting> postings1 = index.getPostings(term1);
        List<Posting> postings2 = index.getPostings(term2);

        System.out.printf("\nQuery: \"%s\" and \"%s\" (k=%d, ordered=%b)%n", term1, term2, k, ordered);

        if (postings1.isEmpty()) {
            System.out.println("  Term '" + term1 + "' not found in index.");
            return;
        }
        if (postings2.isEmpty()) {
            System.out.println("  Term '" + term2 + "' not found in index.");
            return;
        }

        Map<Integer, List<Integer>> posMap1 = new HashMap<>();
        Map<Integer, List<Integer>> posMap2 = new HashMap<>();
        for (Posting p : postings1) posMap1.put(p.getDocId(), p.getPositions());
        for (Posting p : postings2) posMap2.put(p.getDocId(), p.getPositions());

        Set<Integer> commonDocs = new HashSet<>(posMap1.keySet());
        commonDocs.retainAll(posMap2.keySet());

        if (commonDocs.isEmpty()) {
            System.out.println("  No documents contain both terms.");
        } else {
            int matches = 0;
            for (int docId : commonDocs) {
                List<Integer> list1 = posMap1.get(docId);
                List<Integer> list2 = posMap2.get(docId);
                ProximityQuery pq = new ProximityQuery(new ArrayList<>(list1), new ArrayList<>(list2), k, ordered);
                if (pq.matches()) {
                    System.out.println("  ✓ Doc " + docId + " matches.");
                    matches++;
                } else {
                    System.out.println("  ✗ Doc " + docId + " does NOT match.");
                }
            }
            System.out.println("\n  Total matching documents: " + matches);
        }
    }

    // ==================== REFLECTION HELPERS ====================

    private static Map<String, List<Posting>> getIndexMap(PositionalIndex posIndex) {
        try {
            var field = posIndex.getClass().getDeclaredField("index");
            field.setAccessible(true);
            return (Map<String, List<Posting>>) field.get(posIndex);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
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
//helper method
    private static boolean isArabic(String text) {
        int arabicCount = 0, letterCount = 0;
        for (char c : text.toCharArray()) {
            if (c >= '\u0600' && c <= '\u06FF') { arabicCount++; letterCount++; }
            else if (Character.isLetter(c)) letterCount++;
        }
        return letterCount > 0 && ((double) arabicCount / letterCount) > 0.5;
    }
    // ==================== RANKED SEARCH ====================

    private static void rankedSearch(Scanner scanner) {
        System.out.print("\nEnter your search query: ");
        String query = scanner.nextLine();

        System.out.print("Choose language (ar/en): ");
        String lang = scanner.nextLine().toLowerCase();

        // اختيار المعالج المناسب
        if (lang.equals("ar")) {
            performRankedSearch(query, "ar");
        } else if (lang.equals("en")) {
            performRankedSearch(query, "en");
        } else {
            System.out.println("Invalid language. Please choose 'ar' or 'en'.");
        }
    }

    private static void performRankedSearch(String query, String language) {
        System.out.println("\n--- Ranked Search (TF-IDF + Cosine Similarity) ---");
        System.out.println("Query: " + query);
        System.out.println("Language: " + (language.equals("ar") ? "Arabic" : "English"));

        // إنشاء كائن RankedSearch (نعيد استخدامه)
        RankedSearch rankedSearch = new RankedSearch(positionalIndex, ARprocessor, ENprocessor);

        long startTime = System.currentTimeMillis();
        List<CosineSimilarity.Result> results = rankedSearch.search(query, language);
        long endTime = System.currentTimeMillis();

        System.out.println("\nSearch completed in " + (endTime - startTime) + " ms");

        if (results.isEmpty()) {
            System.out.println("No results found.");
        } else {
            System.out.println("\nResults (sorted by relevance):");
            System.out.println("=".repeat(50));
            for (int i = 0; i < results.size(); i++) {
                CosineSimilarity.Result r = results.get(i);
                System.out.printf("%d. Document ID: %d  (Score: %.4f)%n",
                        i+1, r.docId, r.score);
            }
            System.out.println("=".repeat(50));
            System.out.println("Total results: " + results.size());
        }
    }
}