package query;

import java.util.ArrayList;

public class Document {

    private int id;

    private String content;

    public Document(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }


    public static ArrayList<Integer> getPositions(Document document, String term) {

        String content = document.getContent();
        ArrayList<Integer> positions = new ArrayList<>();

        String[] words = content.split(" ");

        for (int i = 0; i < words.length; i++) {

            if (words[i].equals(term)) {

                positions.add(i + 1);

            }

        }

        return positions;
    }


}
