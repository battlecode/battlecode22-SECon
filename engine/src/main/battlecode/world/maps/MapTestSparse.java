package battlecode.world.maps;

import java.io.IOException;
import java.util.Random;

import battlecode.world.MapBuilder;

/**
 * Generate a map.
 */
public class MapTestSparse {

    // change this!!!
    public static final String mapName = "maptestsparse";

    // don't change this!!
    public static final String outputDirectory = "engine/src/main/battlecode/world/resources/";

    /**
     * @param args unused
     */
    public static void main(String[] args) {
        try {
            makeSimple();
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println("Generated a map!");
    }

    public static void makeSimple() throws IOException {
        MapBuilder mapBuilder = new MapBuilder(mapName, 32, 32, 0, 0, 30);
        mapBuilder.addSymmetricSpawnLoc(5, 5);
        Random random = new Random(6147);

        for (int i = 0; i < mapBuilder.width / 2; i++) {
            for (int j = 0; j < mapBuilder.height; j++) {
                if (i % 10 == 0 && j % 10 == 0)
                  mapBuilder.setSymmetricUranium(i, j, 100);
            }
        }

        mapBuilder.saveMap(outputDirectory);
    }
}
