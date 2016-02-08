package raw;

import simulation.Manager;
import simulation.Map;

/**
 * Created by Florian on 08.02.2016.
 */
public class Tester {
    public static void main(String args[]) {
        try {
            MatrixPosBundle bundle = Algorithm.genMap(Map.readMap("map3.txt").obstacles);
            Manager.printMatrix(bundle.matrix);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
