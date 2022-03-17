package battlecode.world;

import battlecode.common.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Build and validate maps easily.
 */
public class MapBuilder {

    public String name;
    public int width;
    public int height;
    public MapLocation origin;
    public int seed;
    private MapSymmetry symmetry;
    private boolean[] wallArray;
    private int[] uraniumArray;
    private int idCounter;

    private List<RobotInfo> bodies;

    public MapBuilder(String name, int width, int height, int originX, int originY, int seed) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.origin = new MapLocation(originX, originY);
        this.seed = seed;
        this.bodies = new ArrayList<>();

        // default values
        this.symmetry = MapSymmetry.ROTATIONAL;
        this.idCounter = 0;
        this.wallArray = new boolean[width * height];
        Arrays.fill(this.wallArray, false); // default is there is no wall
        this.uraniumArray = new int[width * height];
    }

    // ********************
    // BASIC METHODS
    // ********************

    /**
     * Convert location to index. Critical: must conform with GameWorld.indexToLocation.
     * @param x
     * @param y
     * @return
     */
    private int locationToIndex(int x, int y) {
        return x + y * width;
    }

    public void addRobot(int id, Team team, MapLocation loc) {
        // check if something already exists here, if so shout
        for (RobotInfo r : bodies) {
            if (r.location.equals(loc)) {
                throw new RuntimeException("CANNOT ADD ROBOT TO SAME LOCATION AS OTHER ROBOT");
            }
        }
        bodies.add(new RobotInfo(
                id,
                team,
                RobotType.ROBOT,
                GameConstants.ROBOT_INITIAL_HEALTH,
                loc
        ));
    }

    public void addRobot(int x, int y, Team team) {
        addRobot(
                idCounter++,
                team,
                new MapLocation(x, y)
        );
    }

    public void setWall(int x, int y, boolean value) {
        this.wallArray[locationToIndex(x, y)] = value;
    }

    public void setUranium(int x, int y, int value) {
        this.uraniumArray[locationToIndex(x, y)] = value;
    }

    public void setSymmetry(MapSymmetry symmetry) {
        this.symmetry = symmetry;
    }

    // ********************
    // SYMMETRY METHODS
    // ********************

    public int symmetricY(int y) {
        return symmetricY(y, symmetry);
    }

    public int symmetricX(int x) {
        return symmetricX(x, symmetry);
    }

    public int symmetricY(int y, MapSymmetry symmetry) {
        switch (symmetry) {
            case VERTICAL:
                return y;
            case HORIZONTAL:
            case ROTATIONAL:
            default:
                return height - 1 - y;
        }
    }

    public int symmetricX(int x, MapSymmetry symmetry) {
        switch (symmetry) {
            case HORIZONTAL:
                return x;
            case VERTICAL:
            case ROTATIONAL:
            default:
                return width - 1 - x;
        }
    }

    public MapLocation symmetryLocation(MapLocation p) {
        return new MapLocation(symmetricX(p.x), symmetricY(p.y));
    }

    /**
     * Add team A Robot to (x,y) and team B Robot to symmetric position.
     * @param x x position
     * @param y y position
     */
    public void addSymmetricRobot(int x, int y) {
        addRobot(x, y, Team.A);
        addRobot(symmetricX(x), symmetricY(y), Team.B);
    }

    public void setSymmetricWall(int x, int y, boolean value) {
        setWall(x, y, value);
        setWall(symmetricX(x), symmetricY(y), value);
    }

    public void setSymmetricUranium(int x, int y, int value) {
        setUranium(x, y, value);
        setUranium(symmetricX(x), symmetricY(y), value);
    }

    // ********************
    // BUILDING AND SAVING
    // ********************

    public LiveMap build() {
        return new LiveMap(width, height, origin, seed, GameConstants.GAME_MAX_NUMBER_OF_ROUNDS, name,
                symmetry, bodies.toArray(new RobotInfo[bodies.size()]), wallArray, uraniumArray);
    }

    /**
     * Saves the map to the specified location.
     * @param pathname
     * @throws IOException
     */
    public void saveMap(String pathname) throws IOException {
        // validate
        assertIsValid();
        System.out.println("Saving " + this.name + ".");
        GameMapIO.writeMap(this.build(), new File(pathname));
    }

    /**
     * Throws a RuntimeException if the map is invalid.
     */
    public void assertIsValid() {
        System.out.println("Validating " + name + "...");

        // get robots
        RobotInfo[] robots = new RobotInfo[width * height];
        for (RobotInfo r : bodies) {
            if (robots[locationToIndex(r.location.x, r.location.y)] != null)
                throw new RuntimeException("Two robots on the same square");
            robots[locationToIndex(r.location.x, r.location.y)] = r;
        }

        if (width < GameConstants.MAP_MIN_WIDTH || height < GameConstants.MAP_MIN_HEIGHT || 
            width > GameConstants.MAP_MAX_WIDTH || height > GameConstants.MAP_MAX_HEIGHT)
            throw new RuntimeException("The map size must be between " + GameConstants.MAP_MIN_WIDTH + "x" +
                                       GameConstants.MAP_MIN_HEIGHT + " and " + GameConstants.MAP_MAX_WIDTH + "x" +
                                       GameConstants.MAP_MAX_HEIGHT + ", inclusive");

        // checks just 1 robot on each team
        // only needs to check the robots of Team A, because symmetry is checked
        int numTeamARobots = 0;
        for (RobotInfo r : bodies) {
            if (r.getTeam() == Team.A) {
                numTeamARobots++;
            }
        }
        if (numTeamARobots != GameConstants.NUM_STARTING_ROBOTS) {
            throw new RuntimeException("Map must have " + GameConstants.NUM_STARTING_ROBOTS + " starting robots of each team");
        }

        // assert wall, uranium, and inital robot symmetry
        ArrayList<MapSymmetry> allMapSymmetries = getSymmetry(robots);
        System.out.println("This map has the following symmetries: " + allMapSymmetries);
        if (!allMapSymmetries.contains(this.symmetry)) {
            throw new RuntimeException("Walls, uranium and initial robots must be symmetric");
        }
    }

    public boolean onTheMap(MapLocation loc) {
        return loc.x >= 0 && loc.y >= 0 && loc.x < width && loc.y < height;
    }

    public MapLocation indexToLocation(int idx) {
        return new MapLocation(idx % this.width,
                               idx / this.width);
    }

    /**
     * @return the list of symmetries, empty if map is invalid
     */
    private ArrayList<MapSymmetry> getSymmetry(RobotInfo[] robots) {
        ArrayList<MapSymmetry> possible = new ArrayList<MapSymmetry>();
        possible.add(MapSymmetry.ROTATIONAL);
        possible.add(MapSymmetry.HORIZONTAL);
        possible.add(MapSymmetry.VERTICAL);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                MapLocation current = new MapLocation(x, y);
                int curIdx = locationToIndex(current.x, current.y);
                RobotInfo cri = robots[locationToIndex(current.x, current.y)];
                for (int i = possible.size() - 1; i >= 0; i--) { // iterating backwards so we can remove in the loop
                    MapSymmetry symmetry = possible.get(i);
                    MapLocation symm = new MapLocation(symmetricX(x, symmetry), symmetricY(y, symmetry));
                    int symIdx = locationToIndex(symm.x, symm.y);
                    if (wallArray[curIdx] != wallArray[symIdx])
                        possible.remove(symmetry);
                    else if (uraniumArray[curIdx] != uraniumArray[symIdx])
                        possible.remove(symmetry);
                    else {
                        RobotInfo sri = robots[locationToIndex(symm.x, symm.y)];
                        if (cri != null || sri != null) {
                            if (cri == null || sri == null) {
                                possible.remove(symmetry);
                            } else if (cri.getType() != sri.getType()) {
                                possible.remove(symmetry);
                            } else if (!symmetricTeams(cri.getTeam(), sri.getTeam())) {
                                possible.remove(symmetry);
                            }
                        }
                    }
                }
            }
        }
        return possible;
    }

    private boolean symmetricTeams(Team a, Team b) {
        return a != b;
    }
}
