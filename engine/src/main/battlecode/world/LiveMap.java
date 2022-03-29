package battlecode.world;

import battlecode.common.*;

import java.util.*;

/**
 * The class represents the map in the game world on which
 * objects interact.
 *
 * This class is STATIC and immutable. It reflects the initial
 * condition of the map. All changes to the map are reflected in GameWorld.
 *
 * It is named LiveMap to distinguish it from a battlecode.schema.GameMap,
 * which represents a serialized LiveMap.
 */
public strictfp class LiveMap {

    /**
     * The width and height of the map.
     */
    private final int width, height;

    /**
     * The coordinates of the origin
     */
    private final MapLocation origin;

    /**
     * The symmetry of the map.
     */
    private final MapSymmetry symmetry;

    /**
     * Whether there is a wall or not.
     */
    private boolean[] wallArray;

    /**
     * How much uranium is on each square.
     */
    private final int[] uraniumArray;

    /**
     * The location of the spawn locations of each team.
     */
    private final MapLocation[] spawnLocs;

    /**
     * The team of the spawn locations.
     */
    private final Team[] spawnTeams;


    /**
     * The random seed contained in the map file.
     */
    private final int seed;

    /**
     * The maximum number of rounds in the game.
     */
    private final int rounds;

    /**
     * The name of the map.
     */
    private final String mapName;

    /**
     * The bodies to spawn on the map; MapLocations are in world space -
     * i.e. in game correct MapLocations that need to have the origin
     * subtracted from them to be used to index into the map arrays.
     */
    private final RobotInfo[] initialBodies; //just one robot each

    public LiveMap(int width,
                   int height,
                   MapLocation origin,
                   int seed,
                   int rounds,
                   String mapName,
                   RobotInfo[] initialBodies) {
        this.width = width;
        this.height = height;
        this.origin = origin;
        this.seed = seed;
        this.rounds = rounds;
        this.mapName = mapName;
        this.symmetry = MapSymmetry.ROTATIONAL;
        this.initialBodies = Arrays.copyOf(initialBodies, initialBodies.length);
        this.wallArray = new boolean[width * height];
        Arrays.fill(this.wallArray, false); // default is there is no wall
        this.uraniumArray = new int[width * height];
        this.spawnLocs = new MapLocation[2];
        this.spawnTeams = new Team[2];

        // invariant: bodies is sorted by id
        Arrays.sort(this.initialBodies, (a, b) -> Integer.compare(a.getID(), b.getID()));
    }

    public LiveMap(int width,
                   int height,
                   MapLocation origin,
                   int seed,
                   int rounds,
                   String mapName,
                   MapSymmetry symmetry,
                   RobotInfo[] initialBodies,
                   boolean[] wallArray,
                   int[] uraniumArray,
                   MapLocation[] spawnLocs, 
                   Team[] spawnTeams) {
        this.width = width;
        this.height = height;
        this.origin = origin;
        this.seed = seed;
        this.rounds = rounds;
        this.mapName = mapName;
        this.symmetry = symmetry;
        this.initialBodies = Arrays.copyOf(initialBodies, initialBodies.length);
        this.wallArray = new boolean[wallArray.length];
        for (int i = 0; i < wallArray.length; i++) {
            this.wallArray[i] = wallArray[i];
        }
        this.uraniumArray = new int[uraniumArray.length];
        for (int i = 0; i < uraniumArray.length; i++) {
            this.uraniumArray[i] = uraniumArray[i];
        }
        assert(spawnLocs.length == 2);
        assert(spawnTeams.length == 2);
        this.spawnLocs = new MapLocation[2];
        this.spawnTeams = new Team[2];
        for (int i = 0; i < spawnLocs.length; i++) {
            this.spawnLocs[i] = spawnLocs[i];
            this.spawnTeams[i] = spawnTeams[i];
        }

        // invariant: bodies is sorted by id
        Arrays.sort(this.initialBodies, (a, b) -> Integer.compare(a.getID(), b.getID()));
    }

    /**
     * Creates a deep copy of the input LiveMap, except initial bodies.
     *
     * @param gm the LiveMap to copy.
     */
    public LiveMap(LiveMap gm) {
        this(gm.width, gm.height, gm.origin, gm.seed, gm.rounds, gm.mapName, gm.symmetry,
             gm.initialBodies, gm.wallArray, gm.uraniumArray, gm.spawnLocs, gm.spawnTeams);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LiveMap)) return false;
        return this.equals((LiveMap) o);
    }

    /**
     * Returns whether two GameMaps are equal.
     *
     * @param other the other map to compare to
     * @return whether the two maps are equivalent
     */
    public boolean equals(LiveMap other) {
        if (this.rounds != other.rounds) return false;
        if (this.width != other.width) return false;
        if (this.height != other.height) return false;
        if (this.seed != other.seed) return false;
        if (!this.mapName.equals(other.mapName)) return false;
        if (!this.origin.equals(other.origin)) return false;
        if (!Arrays.equals(this.wallArray, other.wallArray)) return false;
        if (!Arrays.equals(this.uraniumArray, other.uraniumArray)) return false;
        if (!Arrays.equals(this.spawnLocs, other.spawnLocs)) return false;
        if (!Arrays.equals(this.spawnTeams, other.spawnTeams)) return false;
        return Arrays.equals(this.initialBodies, other.initialBodies);
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + origin.hashCode();
        result = 31 * result + seed;
        result = 31 * result + rounds;
        result = 31 * result + mapName.hashCode();
        result = 31 * result + Arrays.hashCode(wallArray);
        result = 31 * result + Arrays.hashCode(uraniumArray);
        result = 31 * result + Arrays.hashCode(spawnLocs);
        result = 31 * result + Arrays.hashCode(spawnTeams);
        result = 31 * result + Arrays.hashCode(initialBodies);
        return result;
    }

    /**
     * Returns the width of this map.
     *
     * @return the width of this map.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the map.
     *
     * @return the height of the map
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the name of the map.
     *
     * @return the name of the map
     */
    public String getMapName() {
        return mapName;
    }

    /**
     * Returns the symmetry of the map.
     *
     * @return the symmetry of the map
     */
    public MapSymmetry getSymmetry() {
        return symmetry;
    }

    /**
     * Determines whether or not the location at the specified
     * coordinates is on the map. The coordinate should be a shifted one
     * (takes into account the origin). Assumes grid format (0 <= x < width).
     *
     * @param x the (shifted) x-coordinate of the location
     * @param y the (shifted) y-coordinate of the location
     * @return true if the given coordinates are on the map,
     *         false if they're not
     */
    private boolean onTheMap(int x, int y) {
        return (x >= origin.x && y >= origin.y && x < origin.x + width && y < origin.y + height);
    }

    /**
     * Determines whether or not the specified location is on the map.
     *
     * @param loc the MapLocation to test
     * @return true if the given location is on the map,
     *         false if it's not
     */
    public boolean onTheMap(MapLocation loc) {
        return onTheMap(loc.x, loc.y);
    }

    /**
     * Determines whether or not the specified circle is completely on the map.
     *
     * @param loc the center of the circle
     * @param radius the radius of the circle
     * @return true if the given circle is on the map,
     *         false if it's not
     */
    public boolean onTheMap(MapLocation loc, int radius) {
        return (onTheMap(loc.translate(-radius, 0)) &&
                onTheMap(loc.translate(radius, 0)) &&
                onTheMap(loc.translate(0, -radius)) &&
                onTheMap(loc.translate(0, radius)));
    }

    /**
     * Get a list of the initial bodies on the map.
     *
     * @return the list of starting bodies on the map.
     *         MUST NOT BE MODIFIED.
     */
    public RobotInfo[] getInitialBodies() {
        return initialBodies;
    }

    /**
     * Gets the maximum number of rounds for this game.
     *
     * @return the maximum number of rounds for this game
     */
    public int getRounds() {
        return rounds;
    }

    /**
     * @return the seed of this map
     */
    public int getSeed() {
        return seed;
    }

    /**
     * Gets the origin (i.e., upper left corner) of the map
     *
     * @return the origin of the map
     */
    public MapLocation getOrigin() {
        return origin;
    }

    /**
     * @return the wall array of the map
     */
    public boolean[] getWallArray() {
        return wallArray;
    }

    /**
     * @return the uranium array of the map
     */
    public int[] getUraniumArray() {
        return uraniumArray;
    }

    /**
     * @return the MapLocation array of spawn locations
     */
    public MapLocation[] getSpawnLocs() {
        return spawnLocs;
    }

    /**
     * @return the MapLocation array of spawn locations
     */
    public Team[] getSpawnTeams() {
        return spawnTeams;
    }

    @Override
    public String toString() {
        if (wallArray.length == 0) {
            return "LiveMap{" +
                    "width=" + width +
                    ", height=" + height +
                    ", origin=" + origin +
                    ", seed=" + seed +
                    ", rounds=" + rounds +
                    ", mapName='" + mapName + '\'' +
                    ", initialBodies=" + Arrays.toString(initialBodies) +
                    ", len=" + Integer.toString(wallArray.length) +
                    "}";
        } else {
            return "LiveMap{" +
                    "width=" + width +
                    ", height=" + height +
                    ", origin=" + origin +
                    ", seed=" + seed +
                    ", rounds=" + rounds +
                    ", mapName='" + mapName + '\'' +
                    ", initialBodies=" + Arrays.toString(initialBodies) +
                    ", wallArray=" +  Arrays.toString(wallArray) +
                    ", uraniumArray=" +  Arrays.toString(uraniumArray) +
                    ", spawnLocs=" +  Arrays.toString(spawnLocs) +
                    ", spawnTeams=" +  Arrays.toString(spawnTeams) +
                    "}"; 
        }
    }
}
