import indexing.Posting;
import pipeline.ArabicProcessor;
import indexing.PositionalIndex;
import indexing.KGramIndex;
import pipeline.EnglishProcessor;
import query.Document;
import query.ProximityQuery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Scanner;

public class Main {

    private static PositionalIndex positionalIndex;
    private static KGramIndex kGramIndex;
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
            System.out.println("4. Search (Single Term)");
            System.out.println("5. Search (Multiple Terms - AND)");
            System.out.println("6. Proximity Search");
            System.out.println("7. Display Positional Index");
            System.out.println("8. Spelling Correction (K-Gram)");
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
                    searchSingleTerm(scanner);
                    break;
                case 5:
                    searchMultipleTerms(scanner);
                    break;
                case 6:
                    proximitySearch(scanner);
                    break;
                case 7:
                    displayIndex(positionalIndex);
                    break;
                case 8:
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

        positionalIndex = new PositionalIndex();
        kGramIndex = new KGramIndex();

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
                new Document(8, Files.readString(Paths.get("data/arabic/doc8 A.txt")))
        );

        for (Document doc : ardocuments) {
            indexDocumentAR(ARprocessor, doc, positionalIndex, kGramIndex);
        }

        System.out.println("✓ Arabic documents indexed successfully!");
    }

    private static void indexEnglishDocuments() throws IOException {
        System.out.println("\n--- Indexing English Documents ---");

        List<Document> Endocuments = Arrays.asList(
                new Document(101, Files.readString(Paths.get("data/english/doc1 E.txt"))),
                new Document(102, Files.readString(Paths.get("data/english/doc2 E.txt"))),
                new Document(103, Files.readString(Paths.get("data/english/doc3 E.txt"))),
                new Document(104, Files.readString(Paths.get("data/english/doc4 E.txt"))),
                new Document(105, Files.readString(Paths.get("data/english/doc5 E.txt"))),
                new Document(106, Files.readString(Paths.get("data/english/doc6 E.txt"))),
                new Document(107, Files.readString(Paths.get("data/english/doc7 E.txt"))),
                new Document(108, Files.readString(Paths.get("data/english/doc8 E.txt")))
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
        String term1 = scanner.nextLine().toLowerCase();

        System.out.print("Enter second term: ");
        String term2 = scanner.nextLine().toLowerCase();

        System.out.print("Enter maximum distance (k): ");
        int k = scanner.nextInt();

        System.out.print("Ordered? (true/false): ");
        boolean ordered = scanner.nextBoolean();

        performProximityQuery(positionalIndex, term1, term2, k, ordered);
    }

    private static void spellingCorrection(Scanner scanner) {
        System.out.print("\nEnter misspelled word: ");
        String misspelled = scanner.nextLine().toLowerCase();

        System.out.println("\n--- Spelling Correction for: '" + misspelled + "' ---");

        List<String> misspelledGrams = kGramIndex.getGrams(misspelled);
        System.out.println("Grams: " + misspelledGrams);

        Map<String, Integer> candidateScores = new HashMap<>();
        for (String gram : misspelledGrams) {
            Set<String> words = kGramIndex.getWordsForGram(gram);
            for (String word : words) {
                candidateScores.put(word, candidateScores.getOrDefault(word, 0) + 1);
            }
        }

        if (candidateScores.isEmpty()) {
            System.out.println("No suggestions found.");
        } else {
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(candidateScores.entrySet());
            sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            System.out.println("Top suggestions:");
            for (int i = 0; i < Math.min(5, sorted.size()); i++) {
                System.out.println("  " + (i+1) + ". " + sorted.get(i).getKey() +
                        " (score: " + sorted.get(i).getValue() + ")");
            }
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
                ProximityQuery pq = new ProximityQuery(
                        new ArrayList<>(list1), new ArrayList<>(list2), k, ordered);
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
}