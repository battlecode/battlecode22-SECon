package battlecode.world;

import battlecode.common.*;
import static battlecode.common.GameActionExceptionType.*;
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
    public int getRobotCount(int id) {
        return this.gameWorld.getObjectInfo().getRobotCount(getTeam(id));
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
    public Team getTeam(int id) {
        return this.getRobotByID(id).getTeam();
    }

    @Override
    public RobotType getType() {
        return this.robot.getType();
    }

    @Override
    public RobotType getType(int id) {
        return this.getRobotByID(id).getType();
    }

    @Override
    public MapLocation getLocation(int id) {
        return this.getRobotByID(id).getLocation();
    }
 
    @Override
    public float getHealth(int id) {
        return this.getRobotByID(id).getHealth();
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
    public boolean onTheMap(MapLocation loc) {
        assertNotNull(loc);
        return this.gameWorld.getGameMap().onTheMap(loc);
    }

    private void assertOnTheMap(MapLocation loc) throws GameActionException {
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
    public RobotInfo[] senseNearbyRobots(int id) {
        try {
            return senseNearbyRobots(id, -1);
        } catch (GameActionException willNeverHappen) {
            throw new RuntimeException("impossible", willNeverHappen);
        }
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int id, int radiusSquared) throws GameActionException {
        return senseNearbyRobots(id, radiusSquared, null);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int id, int radiusSquared, Team team) throws GameActionException {
        return senseNearbyRobots(id, this.getLocation(id), radiusSquared, team);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int id, MapLocation center, int radiusSquared, Team team) throws GameActionException {
        assertNotNull(center);
        int actualRadiusSquared = radiusSquared == -1 ? Integer.MAX_VALUE : radiusSquared;
        if (actualRadiusSquared < 0) throw new GameActionException(CANT_DO_THAT,"Radius squared must be non-negative.");
        InternalRobot[] allSensedRobots = gameWorld.getAllRobotsWithinRadiusSquared(center, actualRadiusSquared);
        List<RobotInfo> validSensedRobots = new ArrayList<>();
        for (InternalRobot sensedRobot : allSensedRobots) {
            // check if this robot
            if (sensedRobot.equals(this.getRobotByID(id)))
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
    public MapLocation[] senseNearbyLocationsWithUranium(int id) {
        try {
            return senseNearbyLocationsWithUranium(this.getLocation(id), -1, 1);
        } catch (GameActionException willNeverHappen) {
            throw new RuntimeException("impossible", willNeverHappen);
        }
    }

    @Override
    public MapLocation[] senseNearbyLocationsWithUranium(int id, int radiusSquared) throws GameActionException {
        return senseNearbyLocationsWithUranium(this.getLocation(id), radiusSquared, 1);
    }

    @Override
    public MapLocation[] senseNearbyLocationsWithUranium(MapLocation center, int radiusSquared) throws GameActionException {
        return senseNearbyLocationsWithUranium(center, radiusSquared, 1);
    }

    @Override
    public MapLocation[] senseNearbyLocationsWithUranium(int id, int radiusSquared, int minUranium) throws GameActionException {
        return senseNearbyLocationsWithUranium(this.getLocation(id), radiusSquared, minUranium);
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
    public MapLocation adjacentLocation(int id, Direction dir) {
        return getLocation(id).add(dir);
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

    private void assertIsReady(int id) throws GameActionException {
        if (!this.getRobotByID(id).isReady())
            throw new GameActionException(IS_NOT_READY,
                    "This robot's cooldown has not expired.");
    }

    @Override
    public boolean isReady(int id) {
        try {
            assertIsReady(id);
            return true;
        } catch (GameActionException e) { return false; }
    }

    public int getCooldownTurns(int id) {
        return this.getRobotByID(id).getCooldownTurns();
    }

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    private void assertCanMove(int id, Direction dir) throws GameActionException {
        assertNotNull(dir);
        assertIsReady(id);
        MapLocation loc = adjacentLocation(id, dir);
        if (!onTheMap(loc))
            throw new GameActionException(OUT_OF_RANGE,
                    "Can only move to locations on the map; " + loc + " is not on the map.");
        if (isLocationOccupied(loc) && this.gameWorld.getRobot(loc).getTeam() == this.getRobotByID(id).getTeam()){
            throw new GameActionException(CANT_MOVE_THERE,
                 "Cannot move to location " + loc +" due to friendly robot occupying it.");
        }
        if (this.senseWall(loc)){
            throw new GameActionException(CANT_MOVE_THERE,
                "Cannot move to location " + loc +" due to wall occupying it.");
        }
    }

    @Override
    public boolean canMove(int id, Direction dir) {
        try {
            assertCanMove(id, dir);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void move(int id, Direction dir) throws GameActionException {
        this.assertCanMove(id, dir);
        MapLocation center = this.adjacentLocation(id, dir);
        InternalRobot prevOccupied = this.gameWorld.getRobot(center);
        this.gameWorld.getMatchMaker().addMoved(id, this.getRobotByID(id).getLocation());

        // process collisions
        boolean winner = false;
        if (prevOccupied != null) {
            System.out.println("Collision!");
            winner = this.getRobotByID(id).collide(prevOccupied);
        }
        
        if (winner) {
            this.gameWorld.moveRobot(this.getLocation(id), center);
            this.getRobotByID(id).setLocation(center);

            this.getRobotByID(id).resetCooldownTurns();
        }
        
    }

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    private void assertCanBuildRobot(int id, int health) throws GameActionException {
        Team team = getTeam(id);
        if (this.gameWorld.getTeamInfo().getUranium(team) < health)
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "Insufficient amount of uranium.");
        MapLocation loc = this.gameWorld.getSpawnLoc(getTeam(id));
        if (this.isLocationOccupied(loc) && this.gameWorld.getRobot(loc).getTeam() == this.getTeam(id)){
            throw new GameActionException(FRIENDLY_ROBOT_PRESENT,
                    "Can't spawn if a friendly robot is on your spawn square.");
        }
    } 

    @Override
    public boolean canBuildRobot(int id, int health) {
        try {
            assertCanBuildRobot(id, health);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public void buildRobot(int id, int health) throws GameActionException {
        assertCanBuildRobot(id, health);
        Team team = getTeam(id);
        this.gameWorld.getTeamInfo().addUranium(team, -health);
        MapLocation loc = this.gameWorld.getSpawnLoc(this.getTeam(id));
        InternalRobot prevOccupied = this.gameWorld.getRobot(loc);
        int newId = this.gameWorld.spawnRobot(this.getRobotByID(id).getType(), team, health);
        this.gameWorld.getMatchMaker().addAction(id, Action.SPAWN_UNIT, newId);

        // process collisions (auto-collision with enemy)
        if (prevOccupied != null){
            this.gameWorld.getRobot(loc).collide(prevOccupied);
        }
    }

    // *****************************
    // ****** EXPLODE METHODS ******
    // *****************************

    private void assertCanExplode(int id) throws GameActionException {
        assertIsReady(id);
    }

    @Override
    public boolean canExplode(int id) {
        try {
            assertCanExplode(id);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void explode(int id) throws GameActionException {
        assertCanExplode(id);
        this.getRobotByID(id).resetCooldownTurns();
        for (Direction dir : Direction.cardinalDirections()){
            MapLocation loc = this.adjacentLocation(id,dir);
            if (!onTheMap(loc)) continue;
            InternalRobot bot = this.gameWorld.getRobot(loc);
            if (bot == null) continue;
            // Don't damage friendly robots.
            if(bot.getTeam() == this.getRobotByID(id).getTeam())
                continue;
            bot.damageHealth(this.getRobotByID(id).getHealth() / 2);
        }
        this.gameWorld.getMatchMaker().addAction(id, Action.EXPLODE, -1);
        this.gameWorld.destroyRobot(id);
    }

    // ***********************
    // **** MINING METHODS *** 
    // ***********************

    private void assertCanMine(int id, MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertOnTheMap(loc);
        assertIsReady(id);
        if (this.gameWorld.getUranium(loc) < 1)
            throw new GameActionException(CANT_DO_THAT, 
                    "Uranium amount must be positive to be mined.");
    }

    @Override
    public boolean canMine(int id) {
        try {
            assertCanMine(id, this.getLocation(id));
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void mine(int id) throws GameActionException {
        MapLocation loc = this.getRobotByID(id).getLocation();
        assertCanMine(id, loc);
        this.getRobotByID(id).resetCooldownTurns();
        this.gameWorld.setUranium(loc, this.gameWorld.getUranium(loc) - 1);
        this.gameWorld.getTeamInfo().addUranium(getTeam(id), 1);
        this.gameWorld.getMatchMaker().addAction(id, Action.MINE_URANIUM, locationToInt(loc));
    }

    // ***********************************
    // ****** COMMUNICATION METHODS ****** 
    // ***********************************

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    public void disintegrate(int id) {
        this.getRobotByID(id).disintegrate();
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
    public void setIndicatorString(int id, String string) {
        if (string.length() > GameConstants.INDICATOR_STRING_MAX_LENGTH) {
            string = string.substring(0, GameConstants.INDICATOR_STRING_MAX_LENGTH);
        }
        this.getRobotByID(id).setIndicatorString(string);
    }

    @Override
    public void setIndicatorDot(int id, MapLocation loc, int red, int green, int blue) {
        assertNotNull(loc);
        this.gameWorld.getMatchMaker().addIndicatorDot(id, loc, red, green, blue);
    }

    @Override
    public void setIndicatorLine(int id, MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) {
        assertNotNull(startLoc);
        assertNotNull(endLoc);
        this.gameWorld.getMatchMaker().addIndicatorLine(id, startLoc, endLoc, red, green, blue);
    }
}
