package query;

import java.util.ArrayList;

public class ProximityQuery {

    ArrayList<Integer> position1;
    ArrayList<Integer> position2;
    int k;
    boolean ordered;

    public ProximityQuery(ArrayList<Integer> position1, ArrayList<Integer> position2, int k, boolean ordered) {
        this.position1 = position1;
        this.position2 = position2;
        this.k = k;
        this.ordered = ordered;
    }

    // machine /4 learning
    public boolean matches() {

        for (int p1 : position1) { // 1 3 5

            for (int p2 : position2) { // 5 6 1

                if (Math.abs(p1 - p2) <= k && (!ordered || p1 < p2) ) {

                    return true;
                }

            }

        }

        return false;
    }





}