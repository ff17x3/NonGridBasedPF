package raw;

import util.PointF;

/**
 * Created by Florian on 08.02.2016.
 */
public class MatrixPosBundle {

    public final float[][] matrix;
    public final PointF[] nodesPos;
    public final byte[] expandDir;

    public MatrixPosBundle(float[][] matrix, PointF[] nodesPos, byte[] expandDir) {
        this.matrix = matrix;
        this.nodesPos = nodesPos;
        this.expandDir = expandDir;
    }


}
