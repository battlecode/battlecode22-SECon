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
    public RobotType getType() {
        return this.robot.getType();
    }

    @Override
    public MapLocation getLocation() {
        return this.robot.getLocation();
    }
 
    @Override
    public int getHealth() {
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
        if (!this.robot.canSenseLocation(loc))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target location not within vision range");
        return this.gameWorld.getGameMap().onTheMap(loc);
    }

    private void assertCanSenseLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        if (!this.robot.canSenseLocation(loc))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target location not within vision range");
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
    public boolean canSenseLocation(MapLocation loc) {
        try {
            assertCanSenseLocation(loc);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public boolean isLocationOccupied(MapLocation loc) throws GameActionException {
        assertCanSenseLocation(loc);
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
        assertCanSenseLocation(loc);
        InternalRobot bot = this.gameWorld.getRobot(loc);
        return bot == null ? null : bot.getRobotInfo();
    }

    @Override
    public boolean canSenseRobot(int id) {
        InternalRobot sensedRobot = getRobotByID(id);
        return sensedRobot == null ? false : canSenseLocation(sensedRobot.getLocation());
    }

    @Override
    public RobotInfo senseRobot(int id) throws GameActionException {
        if (!canSenseRobot(id))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Can't sense given robot; It may be out of vision range or not exist anymore");
        return getRobotByID(id).getRobotInfo();
    }

    @Override
    public RobotInfo[] senseNearbyRobots() {
        return senseNearbyRobots(-1);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int radiusSquared) {
        return senseNearbyRobots(radiusSquared, null);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int radiusSquared, Team team) {
        return senseNearbyRobots(getLocation(), radiusSquared, team);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team) {
        assertNotNull(center);
        int actualRadiusSquared = radiusSquared == -1 ? getType().visionRadiusSquared : Math.min(radiusSquared, getType().visionRadiusSquared);
        InternalRobot[] allSensedRobots = gameWorld.getAllRobotsWithinRadiusSquared(center, actualRadiusSquared);
        List<RobotInfo> validSensedRobots = new ArrayList<>();
        for (InternalRobot sensedRobot : allSensedRobots) {
            // check if this robot
            if (sensedRobot.equals(this.robot))
                continue;
            // check if can sense
            if (!canSenseLocation(sensedRobot.getLocation()))
                continue; 
            // check if right team
            if (team != null && sensedRobot.getTeam() != team)
                continue;
            validSensedRobots.add(sensedRobot.getRobotInfo());
        }
        return validSensedRobots.toArray(new RobotInfo[validSensedRobots.size()]);
    }

    @Override 
    public int senseUranium(MapLocation loc) throws GameActionException {
        assertOnTheMap(loc);
        return this.gameWorld.getUranium(loc);
    }

    @Override 
    public int senseWall(MapLocation loc) throws GameActionException {
        assertOnTheMap(loc);
        return this.gameWorld.getWall(loc);
    }

    @Override
    public MapLocation adjacentLocation(Direction dir) {
        return getLocation().add(dir);
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
        assertIsMovementOrActionReady();
        MapLocation loc = adjacentLocation(dir);
        if (!onTheMap(loc))
            throw new GameActionException(OUT_OF_RANGE,
                    "Can only move to locations on the map; " + loc + " is not on the map.");
        if (isLocationOccupied(loc) && this.gameWorld.getRobot(loc).getTeam() == this.robot.getTeam()){
            throw new GameActionException(CANT_MOVE_THERE,
                 "Cannot move to location " + loc " due to friendly robot occupying it.");
        }
        if (this.gameWorld.getWall(loc)){
            throw new GameActionException(CANT_MOVE_THERE,
                "Cannot move to location " + loc " due to wall occupying it.");
        }
    }

    @Override
    public boolean canMove(Direction dir) {
        try {
            assertCanMove(dir);
            return true;
        } catch (GameActionException e) { return false; }
    }

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    private void assertCanBuildRobot(RobotType type, Direction dir) throws GameActionException {
        assertNotNull(type);
        assertNotNull(dir);
        assertIsActionReady();
        if (!getType().canBuild(type))
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot build robots of type " + type + ".");

        Team team = getTeam();
        if (this.gameWorld.getTeamInfo().getLead(team) < type.buildCostLead)
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "Insufficient amount of lead.");
        if (this.gameWorld.getTeamInfo().getGold(team) < type.buildCostGold)
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "Insufficient amount of gold.");

        MapLocation spawnLoc = adjacentLocation(dir);
        if (!onTheMap(spawnLoc))
            throw new GameActionException(OUT_OF_RANGE,
                    "Can only spawn to locations on the map; " + spawnLoc + " is not on the map.");
        if (isLocationOccupied(spawnLoc))
            throw new GameActionException(CANT_MOVE_THERE,
                    "Cannot spawn to an occupied location; " + spawnLoc + " is occupied.");
    }

    @Override
    public boolean canBuildRobot(RobotType type, Direction dir) {
        try {
            assertCanBuildRobot(type, dir);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void buildRobot(RobotType type, Direction dir) throws GameActionException {
        assertCanBuildRobot(type, dir);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        Team team = getTeam();
        this.gameWorld.getTeamInfo().addLead(team, -type.buildCostLead);
        this.gameWorld.getTeamInfo().addGold(team, -type.buildCostGold);
        int newId = this.gameWorld.spawnRobot(type, adjacentLocation(dir), team);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.SPAWN_UNIT, newId);
    }

    // *****************************
    // **** COMBAT UNIT METHODS **** 
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
        MapLocation currentLocation = 
        for(Direction dir : Direction.cardinalDirections()){
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
    public void mine(MapLocation loc) throws GameActionException {
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
