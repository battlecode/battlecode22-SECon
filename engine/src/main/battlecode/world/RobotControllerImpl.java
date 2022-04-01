package battlecode.world;

import battlecode.common.*;
import battlecode.instrumenter.RobotDeathException;
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

    private boolean checkRobotType() {
        return this.robot.getType() == RobotType.ROBOT;
    }

    private boolean checkControllerType() {
        return this.robot.getType() == RobotType.CONTROLLER;
    }

    private void assertValidIDUse(int id) throws GameActionException {
        assert(checkControllerType());
        // trying to control the other team's robot
        if (getRobotByID(id) == null) {
            throw new GameActionException(CANT_DO_THAT,
                "You may not operate on a robot that has been destroyed or does not exist.");
        } else if (getRobotByID(id).getTeam() != getTeam()) {
            throw new GameActionException(CANT_DO_THAT,
                "You may not operate on another team's robot.");
        }
    }

    private RobotControllerImpl getController(int id) {
        return getRobotByID(id).getController();
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
        return this.gameWorld.getObjectInfo().getRobotTypeCount(this.getTeam(), RobotType.ROBOT);
    }

    @Override
    public int getTeamUraniumAmount(Team team) {
        return this.gameWorld.getTeamInfo().getUranium(team);
    }

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

    private MapLocation getLocation() throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only robots may call this function.");
        }
        return this.robot.getLocation();
    }

    @Override
    public int getID() {
        return this.robot.getID();
    }

    @Override
    public Team getTeam() {
        return this.robot.getTeam();
    }

    @Override
    public Team getTeam(int id) throws GameActionException {
        //assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        return this.getRobotByID(id).getTeam();
    }

    @Override
    public RobotType getType() {
        return this.robot.getType();
    }

    @Override
    public RobotType getType(int id) throws GameActionException {
        //assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        return this.getRobotByID(id).getType();
    }

    @Override
    public MapLocation getLocation(int id) throws GameActionException {
        //assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        if (this.getType(id) == RobotType.CONTROLLER) {
            throw new GameActionException(CANT_DO_THAT,
                "Controllers don't have a location.");
        }
        return this.getRobotByID(id).getLocation();
    }
 
    @Override
    public float getHealth(int id) throws GameActionException {
        //assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        if (this.getType(id) == RobotType.CONTROLLER) {
            throw new GameActionException(CANT_DO_THAT,
                "Controllers don't have health.");
        }
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

    public MapLocation getSpawnLoc() {
        return this.gameWorld.getSpawnLoc(this.getTeam());
    }

    @Override
    public boolean isLocationOccupied(MapLocation loc) throws GameActionException {
        assertOnTheMap(loc);
        return this.gameWorld.getRobot(loc) != null;
    }

    @Override
    public RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException {
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        assertOnTheMap(loc);
        InternalRobot bot = this.gameWorld.getRobot(loc);
        return bot == null ? null : bot.getRobotInfo();
    }

    @Override
    public boolean canSenseRobot(int id) throws GameActionException {
        //assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        InternalRobot sensedRobot = getRobotByID(id);
        return (sensedRobot == null);
    }

    @Override
    public RobotInfo senseRobot(int id) throws GameActionException {
        //assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        if (!canSenseRobot(id))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Can't sense given robot; It may not exist anymore");
        return getRobotByID(id).getRobotInfo();
    }

    @Override
    public RobotInfo[] senseAllRobots() throws GameActionException{
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        try {
            return senseNearbyRobots(new MapLocation(0, 0), -1, null);
        } catch (GameActionException willNeverHappen) {
            throw new RuntimeException("impossible", willNeverHappen);
        }
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int id, int radiusSquared) throws GameActionException {
        //assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        if (this.getType(id) == RobotType.CONTROLLER) {
            throw new GameActionException(CANT_DO_THAT,
                "Controllers don't have a location.");
        }
        return senseNearbyRobots(id, radiusSquared, null);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int id, int radiusSquared, Team team) throws GameActionException {
        //assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        if (this.getType(id) == RobotType.CONTROLLER) {
            throw new GameActionException(CANT_DO_THAT,
                "Controllers don't have a location.");
        }
        return senseNearbyRobots(this.getLocation(id), radiusSquared, team);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team) throws GameActionException {
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        assertNotNull(center);
        int actualRadiusSquared = radiusSquared == -1 ? Integer.MAX_VALUE : radiusSquared;
        if (actualRadiusSquared < 0) throw new GameActionException(CANT_DO_THAT,"Radius squared must be non-negative.");
        InternalRobot[] allSensedRobots = gameWorld.getAllRobotsWithinRadiusSquared(center, actualRadiusSquared);
        List<RobotInfo> validSensedRobots = new ArrayList<>();
        for (InternalRobot sensedRobot : allSensedRobots) {
            // check if this robot
            if (sensedRobot.equals(getRobotByID(getID())))
                continue;
            // check if right team
            if (team != null && sensedRobot.getTeam() != team)
                continue;
            // don't return controllers here
            if (sensedRobot.getType() == RobotType.CONTROLLER) {
                continue;
            }
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
    public MapLocation[] senseNearbyLocationsWithUranium() throws GameActionException {
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        try {
            return senseNearbyLocationsWithUranium(new MapLocation(0, 0), -1, 1);
        } catch (GameActionException willNeverHappen) {
            throw new RuntimeException("impossible", willNeverHappen);
        }
    }

    @Override
    public MapLocation[] senseNearbyLocationsWithUranium(int id, int radiusSquared) throws GameActionException {
        //assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        if (this.getType(id) == RobotType.CONTROLLER) {
            throw new GameActionException(CANT_DO_THAT,
                "Controllers don't have location.");
        }
        return senseNearbyLocationsWithUranium(this.getLocation(id), radiusSquared, 1);
    }

    @Override
    public MapLocation[] senseNearbyLocationsWithUranium(MapLocation center, int radiusSquared) throws GameActionException {
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        return senseNearbyLocationsWithUranium(center, radiusSquared, 1);
    }

    @Override
    public MapLocation[] senseNearbyLocationsWithUranium(int id, int radiusSquared, int minUranium) throws GameActionException {
        //assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        if (this.getType(id) == RobotType.CONTROLLER) {
            throw new GameActionException(CANT_DO_THAT,
                "Controllers don't have location.");
        }
        return senseNearbyLocationsWithUranium(this.getLocation(id), radiusSquared, minUranium);
    }

    @Override
    public MapLocation[] senseNearbyLocationsWithUranium(MapLocation center, int radiusSquared, int minUranium) throws GameActionException {
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        assertOnTheMap(center);
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

    private MapLocation adjacentLocation(Direction dir) throws GameActionException {
        return this.getLocation().add(dir);
    }

    @Override
    public MapLocation adjacentLocation(int id, Direction dir) throws GameActionException {
        //assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        if (this.getType(id) == RobotType.CONTROLLER) {
            throw new GameActionException(CANT_DO_THAT,
                "Controllers don't have location.");
        }
        return getLocation(id).add(dir);
    }

    @Override
    public MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) throws GameActionException {
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
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
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Robots may call this function.");
        }
        if (!this.robot.isReady())
            throw new GameActionException(IS_NOT_READY,
                    "This robot's cooldown has not expired.");
    }

    private boolean isReady() throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Robots may call this function.");
        }
        try {
            assertIsReady();
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public boolean isReady(int id) throws GameActionException {
        assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        if (this.getType(id) == RobotType.CONTROLLER) {
            throw new GameActionException(CANT_DO_THAT,
                "Controllers don't have cooldown.");
        }
        return getController(id).isReady();
    }

    private int getCooldownTurns() throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Robots may call this function.");
        }
        return this.robot.getCooldownTurns();
    }

    @Override
    public int getCooldownTurns(int id) throws GameActionException {
        assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        if (this.getType(id) == RobotType.CONTROLLER) {
            throw new GameActionException(CANT_DO_THAT,
                "Controllers don't have cooldown.");
        }
        return getController(id).getCooldownTurns();
    }

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    private void assertCanMove(Direction dir) throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only robots may call this function.");
        }
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
        if (this.senseWall(loc)){
            throw new GameActionException(CANT_MOVE_THERE,
                "Cannot move to location " + loc +" due to wall occupying it.");
        }
    }

    private boolean canMove(Direction dir) throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Robots may call this function.");
        }
        try {
            assertCanMove(dir);
            return true;
        } catch (GameActionException e) { return false; }
    }

    @Override
    public boolean canMove(int id, Direction dir) throws GameActionException {
        assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        if (this.getType(id) == RobotType.CONTROLLER) {
            throw new GameActionException(CANT_DO_THAT,
                "Controllers don't have cooldown.");
        }
        return getController(id).canMove(dir);
    }

    private void move(Direction dir) throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Robots may call this function.");
        }
        this.assertCanMove(dir);
        MapLocation center = this.adjacentLocation(dir);
        InternalRobot prevOccupied = this.gameWorld.getRobot(center);
        this.gameWorld.getMatchMaker().addMoved(this.robot.getID(), center);

        // process collisions
        boolean winner = true;
        if (prevOccupied != null) {
            winner = this.robot.collide(center, prevOccupied);
        }
        
        if (winner) {
            this.gameWorld.moveRobot(this.getLocation(), center);
            this.robot.setLocation(center);
            this.robot.resetCooldownTurns();
        } 
    }

    @Override
    public void move(int id, Direction dir) throws GameActionException {
        assertValidIDUse(id);
        assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        getController(id).move(dir);
    }

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    private void assertCanBuildRobot(int health) throws GameActionException {
        Team team = getTeam();
        if (health < 1) {
            throw new GameActionException(CANT_DO_THAT, "Can't spawn robot with less than 1 health.");
        }
        if (this.gameWorld.getTeamInfo().getUranium(team) < health)
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "Insufficient amount of uranium.");
        MapLocation loc = this.gameWorld.getSpawnLoc(this.getTeam());
        if (this.isLocationOccupied(loc) && this.gameWorld.getRobot(loc).getTeam() == this.getTeam()){
            throw new GameActionException(FRIENDLY_ROBOT_PRESENT,
                    "Can't spawn if a friendly robot is on your spawn square.");
        }
    } 

    @Override
    public boolean canBuildRobot(int health) throws GameActionException {
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        try {
            assertCanBuildRobot(health);
            return true;
        } catch (GameActionException e) { return false; }
    }


    @Override
    public void buildRobot(int health) throws GameActionException {
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only controllers may call this function.");
        }
        assertCanBuildRobot(health);
        Team team = getTeam();
        this.gameWorld.getTeamInfo().addUranium(team, -health);
        MapLocation loc = this.gameWorld.getSpawnLoc(this.getTeam());
        InternalRobot prevOccupied = this.gameWorld.getRobot(loc);
        int newId = this.gameWorld.spawnRobot(RobotType.ROBOT, team, health);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.SPAWN_UNIT, newId);
        InternalRobot spawnedBot = this.gameWorld.getRobotByID(newId);

        // process collisions (auto-collision with enemy)
        if (prevOccupied != null) {
            spawnedBot.collide(loc, prevOccupied);
        }
    }

    // *****************************
    // ****** EXPLODE METHODS ******
    // *****************************

    private void assertCanExplode() throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only robots may call this function.");
        }
        assertIsReady();
    }

    private boolean canExplode() throws GameActionException{
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only robots may call this function.");
        }
        try {
            assertCanExplode();
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public boolean canExplode(int id) throws GameActionException {
        assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Controllers may call this function.");
        }
        return getController(id).canExplode();
    }

    private void explode() throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only robots may call this function.");
        } 
        assertCanExplode();
        this.robot.resetCooldownTurns();
        for (Direction dir : Direction.cardinalDirections()){
            MapLocation loc = this.adjacentLocation(dir);
            if (!onTheMap(loc)) continue;
            InternalRobot bot = this.gameWorld.getRobot(loc);
            if (bot == null) continue;
            // Don't damage friendly robots.
            if(bot.getTeam() == this.robot.getTeam())
                continue;
            bot.damageHealth(this.robot.getHealth() / 2);
        }
        this.gameWorld.getMatchMaker().addAction(getID(), Action.EXPLODE, -1);
        this.gameWorld.destroyRobot(getID());
    }

    @Override
    public void explode(int id) throws GameActionException {
        assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Controllers may call this function.");
        }
        if (this.getType(id) == RobotType.CONTROLLER) {
            throw new GameActionException(CANT_DO_THAT,
                "Controllers can't explode.");
        }
        getController(id).explode();
    }


    // ***********************
    // **** MINING METHODS *** 
    // ***********************

    private void assertCanMine(MapLocation loc) throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only robots may call this function.");
        } 
        assertNotNull(loc);
        assertOnTheMap(loc);
        assertIsReady();
        if (this.gameWorld.getUranium(loc) < 1)
            throw new GameActionException(CANT_DO_THAT, 
                    "Uranium amount must be positive to be mined.");
    }

    private boolean canMine() throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only robots may call this function.");
        } 
        try {
            assertCanMine(this.robot.getLocation());
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public boolean canMine(int id) throws GameActionException{
        assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Controllers may call this function.");
        }
        return getController(id).canMine();
    }

    private void mine() throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only robots may call this function.");
        } 
        MapLocation loc = this.robot.getLocation();
        assertCanMine(loc);
        this.robot.resetCooldownTurns();
        this.gameWorld.setUranium(loc, this.gameWorld.getUranium(loc) - 1);
        this.gameWorld.getTeamInfo().addUranium(getTeam(), 1);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.MINE_URANIUM, locationToInt(loc));
    }

    @Override
    public void mine(int id) throws GameActionException {
        assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Controllers may call this function.");
        }
        getController(id).mine();
    }

    // ***********************************
    // ****** COMMUNICATION METHODS ****** 
    // ***********************************

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    private void disintegrate() throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only robots may call this function.");
        } 
        throw new RobotDeathException();
    }

    @Override
    public void disintegrate(int id) throws GameActionException {
        assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Controllers may call this function.");
        }
        getController(id).disintegrate();
    }

    @Override
    public void resign() throws GameActionException {
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Controllers may call this function.");
        }
        Team team = getTeam();
        gameWorld.getObjectInfo().eachRobot((robot) -> {
            if (robot.getTeam() == team) {
                try {
                    gameWorld.destroyRobot(robot.getID());
                } catch (GameActionException e){
                    throw new RuntimeException("A GameActionException has occured." +
                        "This is likely because a Robot tried to call a Controller function," +
                        " or a Controller tried to control an enemy robot.");

                }

            }
            return true;
        });
    }

    // ***********************************
    // ******** DEBUG METHODS ************
    // ***********************************

    private void setIndicatorString(String string) throws GameActionException{
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only robots may call this function.");
        } 
        if (string.length() > GameConstants.INDICATOR_STRING_MAX_LENGTH) {
            string = string.substring(0, GameConstants.INDICATOR_STRING_MAX_LENGTH);
        }
        this.robot.setIndicatorString(string);
    }

    @Override
    public void setIndicatorString(int id, String string) throws GameActionException {
        assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Controllers may call this function.");
        }
        getController(id).setIndicatorString(string);
    }

    private void setIndicatorDot(MapLocation loc, int red, int green, int blue) throws GameActionException {
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only robots may call this function.");
        } 
        assertNotNull(loc);
        this.gameWorld.getMatchMaker().addIndicatorDot(getID(), loc, red, green, blue);
    }

    @Override
    public void setIndicatorDot(int id, MapLocation loc, int red, int green, int blue) throws GameActionException {
        assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Controllers may call this function.");
        }
        getController(id).setIndicatorDot(loc, red, green, blue);
    }

    private void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) throws GameActionException{
        if (!checkRobotType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only robots may call this function.");
        } 
        assertNotNull(startLoc);
        assertNotNull(endLoc);
        this.gameWorld.getMatchMaker().addIndicatorLine(getID(), startLoc, endLoc, red, green, blue);
    }

    @Override
    public void setIndicatorLine(int id, MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) throws GameActionException {
        assertValidIDUse(id);
        if (!checkControllerType()) {
            throw new GameActionException(CANT_DO_THAT,
                "Only Controllers may call this function.");
        }
        getController(id).setIndicatorLine(startLoc, endLoc, red, green, blue);
    }
}
