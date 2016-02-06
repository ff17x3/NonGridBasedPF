package ngb;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Florian on 06.02.2016.
 */
public class Map {

    public static final int TYPE_OBSTACLE = 0;

    public final Obstacle[] obstacles;
    public final float mapWidth, mapHeight;

    public Map(Obstacle[] obs, float width, float height) {
        obstacles = obs;
        mapWidth = width;
        mapHeight = height;

    }

    public static Map readMap(String filename) throws Exception {
        try (Scanner reader = new Scanner(new File(filename))) {
            int anzObstacles, i = 0;

            float mapWidth = Integer.parseInt(reader.next());
            float mapHeight = Integer.parseInt(reader.next());
            String anzOStr = reader.next().trim();
            anzObstacles = Integer.parseInt(anzOStr);

            Obstacle[] obstacles = new Obstacle[anzObstacles];

            while (reader.hasNext()) {
                byte type = Byte.parseByte(reader.next());
                switch (type) {
                    case TYPE_OBSTACLE:
                        obstacles[i] = Obstacle.createFromFile(reader.nextLine());
                        i++;
                        break;
                    default:
                        reader.nextLine();
                }
            }

            return new Map(obstacles, mapWidth, mapHeight);

        } catch (FileNotFoundException e) {
            throw new Exception("Invalid map file!",e);
        }
    }
}
