package battlecode.world;

import battlecode.common.*;
import static battlecode.common.GameActionExceptionType.*;
import battlecode.instrumenter.RobotDeathException;
import battlecode.schema.Action;

import java.util.*;


/**
 * The actual implementation of RobotController. Its methods *must* be called
 * from a player thread.
 *
 * It is theoretically possible to have multiple for a single InternalRobot, but
 * that may cause problems in practice, and anyway why would you want to?
 *
 * All overriden methods should assertNotNull() all of their (Object) arguments,
 * if those objects are not explicitly stated to be nullable.
 */
public final strictfp class RobotControllerImpl implements RobotController {

    /**
     * The world the robot controlled by this controller inhabits.
     */
    private final GameWorld gameWorld;

    /**
     * The robot this controller controls.
     */
    private final InternalRobot robot;

    /**
     * An rng based on the world seed.
     */
    private static Random random;

    /**
     * Create a new RobotControllerImpl
     *
     * @param gameWorld the relevant world
     * @param robot the relevant robot
     */
    public RobotControllerImpl(GameWorld gameWorld, InternalRobot robot) {
        this.gameWorld = gameWorld;
        this.robot = robot;

        this.random = new Random(gameWorld.getMapSeed());
    }

    // *********************************
    // ******** INTERNAL METHODS *******
    // *********************************

    /**
     * Throw a null pointer exception if an object is null.
     *
     * @param o the object to test
     */
    private static void assertNotNull(Object o) {
        if (o == null) {
            throw new NullPointerException("Argument has an invalid null value");
        }
    }

    @Override
    public int hashCode() {
        return getID();
    }

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************

    @Override
    public int getRoundNum() {
        return this.gameWorld.getCurrentRound();
    }

    @Override
    public int getMapWidth() {
        return this.gameWorld.getGameMap().getWidth();
    }

    @Override
    public int getMapHeight() {
        return this.gameWorld.getGameMap().getHeight();
    }

    @Override
    public int getRobotCount() {
        return this.gameWorld.getObjectInfo().getRobotCount(getTeam());
    }

    @Override
    public int getTeamUraniumAmount(Team team) {
        return this.gameWorld.getTeamInfo().getUranium(team);
    }

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

    @Override
    public int getID() {
        return this.robot.getID();
    }

    @Override
    public Team getTeam() {
        return this.robot.getTeam();
    }

    @Override
    public MapLocation getLocation() {
        return this.robot.getLocation();
    }
 
    @Override
    public float getHealth() {
        return this.robot.getHealth();
    }

    private InternalRobot getRobotByID(int id) {
        if (!this.gameWorld.getObjectInfo().existsRobot(id))
            return null;
        return this.gameWorld.getObjectInfo().getRobotByID(id);
    }

    private int locationToInt(MapLocation loc) {
        return loc.x + loc.y * this.gameWorld.getGameMap().getWidth();
    }

    // ***********************************
    // ****** GENERAL VISION METHODS *****
    // ***********************************

    @Override
    public boolean onTheMap(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        return this.gameWorld.getGameMap().onTheMap(loc);
    }

    private void assertOnTheMap(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        if (!this.gameWorld.getGameMap().onTheMap(loc))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target location is not on the map");
    }

    private void assertCanActLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        if (!this.gameWorld.getGameMap().onTheMap(loc))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target location is not on the map");
    }

    @Override
    public boolean isLocationOccupied(MapLocation loc) throws GameActionException {
        assertOnTheMap(loc);
        return this.gameWorld.getRobot(loc) != null;
    }

    @Override
    public boolean canSenseRobotAtLocation(MapLocation loc) {
        try {
            return isLocationOccupied(loc);
        } catch (GameActionException e) { return false; }
    }

    @Override
    public RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException {
        assertOnTheMap(loc);
        InternalRobot bot = this.gameWorld.getRobot(loc);
        return bot == null ? null : bot.getRobotInfo();
    }

    @Override
    public boolean canSenseRobot(int id) {
        InternalRobot sensedRobot = getRobotByID(id);
        return (sensedRobot == null);
    }

    @Override
    public RobotInfo senseRobot(int id) throws GameActionException {
        if (!canSenseRobot(id))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Can't sense given robot; It may not exist anymore");
        return getRobotByID(id).getRobotInfo();
    }

    @Override
    public RobotInfo[] senseNearbyRobots() {
        return senseNearbyRobots(null);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(Team team) {
        InternalRobot[] allSensedRobots = gameWorld.getAllRobots();
        List<RobotInfo> validSensedRobots = new ArrayList<>();
        for (InternalRobot sensedRobot : allSensedRobots) {
            // check if this robot
            if (sensedRobot.equals(this.robot))
                continue;
            // check if right team
            if (team != null && sensedRobot.getTeam() != team)
                continue;
            validSensedRobots.add(sensedRobot.getRobotInfo());
        }
        return validSensedRobots.toArray(new RobotInfo[validSensedRobots.size()]);
    }

    @Override 
    public boolean senseWall(MapLocation loc) throws GameActionException {
        assertOnTheMap(loc);
        return this.gameWorld.getWall(loc);
    }

    @Override 
    public int senseUranium(MapLocation loc) throws GameActionException {
        assertOnTheMap(loc);
        return this.gameWorld.getUranium(loc);
    }

    @Override
    public MapLocation[] senseNearbyLocationsWithUranium() {
        try {
            return senseNearbyLocationsWithUranium(getLocation(), -1, 1);
        } catch (GameActionException willNeverHappen) {
            throw new RuntimeException("impossible", willNeverHappen);
        }
    }

    @Override
    public MapLocation[] senseNearbyLocationsWithUranium(int radiusSquared) throws GameActionException {
        return senseNearbyLocationsWithUranium(getLocation(), radiusSquared, 1);
    }

    @Override
    public MapLocation[] senseNearbyLocationsWithUranium(MapLocation center, int radiusSquared) throws GameActionException {
        return senseNearbyLocationsWithUranium(center, radiusSquared, 1);
    }

    @Override
    public MapLocation[] senseNearbyLocationsWithUranium(int radiusSquared, int minUranium) throws GameActionException {
        return senseNearbyLocationsWithUranium(getLocation(), radiusSquared, minUranium);
    }

    @Override
    public MapLocation[] senseNearbyLocationsWithUranium(MapLocation center, int radiusSquared, int minUranium) throws GameActionException {
        radiusSquared = (radiusSquared == -1) ? Integer.MAX_VALUE : radiusSquared;
        if (radiusSquared < 0)
            throw new GameActionException(CANT_DO_THAT,
                    "Radius squared must be non-negative.");
        ArrayList<MapLocation> locations = new ArrayList<>();
        for (MapLocation loc : this.gameWorld.getAllLocationsWithinRadiusSquared(center, radiusSquared)) {
            if (this.gameWorld.getUranium(loc) >= minUranium && onTheMap(loc)) {
                locations.add(loc);
            }
        }
        MapLocation[] result = new MapLocation[locations.size()];
        return locations.toArray(result);
    }

    @Override
    public MapLocation adjacentLocation(Direction dir) {
        return getLocation().add(dir);
    }

    @Override
    public MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) throws GameActionException {
        assertNotNull(center);
        if (radiusSquared < 0)
            throw new GameActionException(CANT_DO_THAT,
                    "Radius squared must be non-negative.");
        return this.gameWorld.getAllLocationsWithinRadiusSquared(center, radiusSquared);
    }

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    private void assertIsReady() throws GameActionException {
        if (!this.robot.canMoveOrActCooldown())
            throw new GameActionException(IS_NOT_READY,
                    "This robot's cooldown has not expired.");
    }

    @Override
    public boolean isReady() {
        try {
            assertIsReady();
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public int getCooldownTurns() {
        return this.robot.getCooldownTurns();
    }

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    private void assertCanMove(Direction dir) throws GameActionException {
        assertNotNull(dir);
        assertIsReady();
        MapLocation loc = adjacentLocation(dir);
        if (!onTheMap(loc))
            throw new GameActionException(OUT_OF_RANGE,
                    "Can only move to locations on the map; " + loc + " is not on the map.");
        if (isLocationOccupied(loc) && this.gameWorld.getRobot(loc).getTeam() == this.robot.getTeam()){
            throw new GameActionException(CANT_MOVE_THERE,
                 "Cannot move to location " + loc +" due to friendly robot occupying it.");
        }
        if (this.gameWorld.getWall(loc)){
            throw new GameActionException(CANT_MOVE_THERE,
                "Cannot move to location " + loc +" due to wall occupying it.");
        }
    }

    @Override
    public boolean canMove(Direction dir) {
        try {
            assertCanMove(dir);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void move(Direction dir) throws GameActionException {
        this.assertCanMove(dir);
        MapLocation center = thisadjacentLocation(dir);
        this.gameWorld.moveRobot(this.getLocation(), center);
        this.setLocation(center);
        // process collisions
        if (this.isLocationOccupied(center)){
            this.robot.collide(this.gameWorld.getRobot(center));
        }
        // this has to happen after robot's location changed because rubble
        this.robot.resetCooldownTurns();
        this.gameWorld.getMatchMaker().addMoved(this.robot.getID(), this.robot.getLocation());
    }

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    private void assertCanBuildRobot(int cost, MapLocation loc) throws GameActionException {
        Team team = getTeam();
        if (this.gameWorld.getTeamInfo().getUranium(team) < cost)
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "Insufficient amount of uranium.");

        if (!onTheMap(loc))
            throw new GameActionException(OUT_OF_RANGE,
                    "Can only spawn to locations on the map; " + loc + " is not on the map.");
        if (loc.equals(this.gameWorld.getSpawnLoc(this.getTeam())))
            throw new GameActionException(CANT_MOVE_THERE,
                    "Cannot spawn to location " + loc + " that is not your spawning location.");
    }

    @Override
    public boolean canBuildRobot(int cost, MapLocation loc) {
        try {
            assertCanBuildRobot(cost, loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void buildRobot(int cost, MapLocation loc) throws GameActionException {
        assertCanBuildRobot(cost, loc);
        Team team = getTeam();
        this.gameWorld.getTeamInfo().addUranium(team, -cost);
        int newId = this.gameWorld.spawnRobot(type, adjacentLocation(dir), cost, team);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.SPAWN_UNIT, newId);
    }

    // *****************************
    // ****** EXPLODE METHODS ******
    // *****************************

    private void assertCanExplode() throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsMoveOrActionReady();
    }

    @Override
    public boolean canExplode() {
        try {
            assertCanExplode();
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void explode() throws GameActionException {
        assertCanExplode(loc);
        this.robot.resetCooldownTurns();
        for (Direction dir : Direction.cardinalDirections()){
            MapLocation loc = this.robot.getLocation().adjacentLocation(dir);
            InternalRobot bot = this.gameWorld.getRobot(loc);
            this.robot.attack(bot);
            this.gameWorld.getMatchMaker().addAction(getID(), Action.ATTACK, bot.getID());
        }
    }

    // ***********************
    // **** MINING METHODS *** 
    // ***********************

    private void assertCanMine(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsReady();
        if (this.gameWorld.getUranium(loc) < 1)
            throw new GameActionException(CANT_DO_THAT, 
                    "Uranium amount must be positive to be mined.");
    }

    @Override
    public boolean canMine(MapLocation loc) {
        try {
            assertCanMine(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void mine() throws GameActionException {
        loc = this.robot.getLocation();
        assertCanMine(loc);
        this.robot.resetCooldownTurns();
        this.gameWorld.setUranium(loc, this.gameWorld.getUranium(loc) - 1);
        this.gameWorld.getTeamInfo().addUranium(getTeam(), 1);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.MINE_URANIUM, locationToInt(loc));
    }

    // ***********************************
    // ****** COMMUNICATION METHODS ****** 
    // ***********************************

    private void assertValidIndex(int index) throws GameActionException {
        if (index < 0 || index >= GameConstants.SHARED_ARRAY_LENGTH)
            throw new GameActionException(CANT_DO_THAT, "You can't access this index as it is not within the shared array.");
    }

    private void assertValidValue(int value) throws GameActionException {
        if (value < 0 || value > GameConstants.MAX_SHARED_ARRAY_VALUE)
            throw new GameActionException(CANT_DO_THAT, "You can't write this value to the shared array " +
                "as it is not within the range of allowable values: [0, " + GameConstants.MAX_SHARED_ARRAY_VALUE + "].");
    }

    @Override
    public int readSharedArray(int index) throws GameActionException {
        assertValidIndex(index);
        return this.gameWorld.getTeamInfo().readSharedArray(getTeam(), index);
    }

    @Override
    public void writeSharedArray(int index, int value) throws GameActionException {
        assertValidIndex(index);
        assertValidValue(value);
        this.gameWorld.getTeamInfo().writeSharedArray(getTeam(), index, value);
    }

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    @Override
    public void disintegrate() {
        throw new RobotDeathException();
    }

    @Override
    public void resign() {
        Team team = getTeam();
        gameWorld.getObjectInfo().eachRobot((robot) -> {
            if (robot.getTeam() == team) {
                gameWorld.destroyRobot(robot.getID());
            }
            return true;
        });
    }

    // ***********************************
    // ******** DEBUG METHODS ************
    // ***********************************

    @Override
    public void setIndicatorString(String string) {
        if (string.length() > GameConstants.INDICATOR_STRING_MAX_LENGTH) {
            string = string.substring(0, GameConstants.INDICATOR_STRING_MAX_LENGTH);
        }
        this.robot.setIndicatorString(string);
    }

    @Override
    public void setIndicatorDot(MapLocation loc, int red, int green, int blue) {
        assertNotNull(loc);
        this.gameWorld.getMatchMaker().addIndicatorDot(getID(), loc, red, green, blue);
    }

    @Override
    public void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) {
        assertNotNull(startLoc);
        assertNotNull(endLoc);
        this.gameWorld.getMatchMaker().addIndicatorLine(getID(), startLoc, endLoc, red, green, blue);
    }
}
