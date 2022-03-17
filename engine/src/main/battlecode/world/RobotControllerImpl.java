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

    @Override
    public boolean assertOnTheMap(MapLocation loc) throws GameActionException{
        if (!this.gameWorld.getGameMap().onTheMap(loc))
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target location is not on the map");
        return this.gameWorld.getGameMap().onTheMap(loc);
    }

    @Override
    public boolean isLocationOccupied(MapLocation loc) throws GameActionException {
        
        return this.gameWorld.getRobot(loc) != null;
    }

  
    // TODO: ADDED FOR SECON

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

    // END TODO

    @Override
    public MapLocation adjacentLocation(Direction dir) {
        return getLocation().add(dir);
    }

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    private void assertCanMove(Direction dir) throws GameActionException {
        assertNotNull(dir);
        assertIsMovementReady();
        MapLocation loc = adjacentLocation(dir);
        if (!onTheMap(loc))
            throw new GameActionException(OUT_OF_RANGE,
                    "Can only move to locations on the map; " + loc + " is not on the map.");
        if (isLocationOccupied(loc))
            throw new GameActionException(CANT_MOVE_THERE,
                    "Cannot move to an occupied location; " + loc + " is occupied.");
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
        assertCanMove(dir);
        MapLocation center = adjacentLocation(dir);
        this.gameWorld.moveRobot(getLocation(), center);
        this.robot.setLocation(center);
        // this has to happen after robot's location changed because rubble
        this.robot.addMovementCooldownTurns(getType().movementCooldown);
        this.gameWorld.getMatchMaker().addMoved(getID(), getLocation());
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

    private void assertCanAttack(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();
        if (!getType().canAttack())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot attack.");
        InternalRobot bot = this.gameWorld.getRobot(loc);
        if (bot == null)
            throw new GameActionException(NO_ROBOT_THERE,
                    "There is no robot to attack at the target location.");
        if (bot.getTeam() == getTeam())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not on the enemy team.");
    }

    @Override
    public boolean canAttack(MapLocation loc) {
        try {
            assertCanAttack(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void attack(MapLocation loc) throws GameActionException {
        assertCanAttack(loc);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        InternalRobot bot = this.gameWorld.getRobot(loc);
        this.robot.attack(bot);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.ATTACK, bot.getID());
    }

    // *****************************
    // ****** REPAIR METHODS *******
    // *****************************

    private void assertCanRepair(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();
        InternalRobot bot = this.gameWorld.getRobot(loc);
        if (bot == null)
            throw new GameActionException(NO_ROBOT_THERE,
                    "There is no robot to repair at the target location.");
        if (!getType().canRepair(bot.getType()))
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot repair robots of type " + bot.getType() + ".");
        if (bot.getTeam() != getTeam())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not on your team so can't be repaired.");
    }

    // ***********************
    // **** MINER METHODS **** 
    // ***********************

    private void assertCanMineLead(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();
        if (!getType().canMine())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot mine.");
        if (this.gameWorld.getLead(loc) < 1)
            throw new GameActionException(CANT_DO_THAT, 
                    "Lead amount must be positive to be mined.");
    }

    @Override
    public boolean canMineLead(MapLocation loc) {
        try {
            assertCanMineLead(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void mineLead(MapLocation loc) throws GameActionException {
        assertCanMineLead(loc);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        this.gameWorld.setLead(loc, this.gameWorld.getLead(loc) - 1);
        this.gameWorld.getTeamInfo().addLead(getTeam(), 1);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.MINE_LEAD, locationToInt(loc));
        this.gameWorld.getMatchMaker().addLeadDrop(loc, -1);
    }

    private void assertCanMineGold(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();
        if (!getType().canMine())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot mine.");
        if (this.gameWorld.getGold(loc) < 1)
            throw new GameActionException(CANT_DO_THAT, 
                    "Gold amount must be positive to be mined.");
    }

    @Override
    public boolean canMineGold(MapLocation loc) {
        try {
            assertCanMineGold(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void mineGold(MapLocation loc) throws GameActionException {
        assertCanMineGold(loc);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        this.gameWorld.setGold(loc, this.gameWorld.getGold(loc) - 1);
        this.gameWorld.getTeamInfo().addGold(getTeam(), 1);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.MINE_GOLD, locationToInt(loc));
        this.gameWorld.getMatchMaker().addGoldDrop(loc, -1);
    }

    // *************************
    // **** MUTATE METHODS **** 
    // *************************

    private void assertCanMutate(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanActLocation(loc);
        assertIsActionReady();
        InternalRobot bot = this.gameWorld.getRobot(loc);
        if (bot == null)
            throw new GameActionException(NO_ROBOT_THERE,
                    "There is no robot to mutate at the target location.");
        if (!getType().canMutate(bot.getType()))
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot mutate robots of type " + bot.getType() + ".");
        if (bot.getTeam() != getTeam())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is not on your team so can't be mutated.");
        if (!bot.canMutate())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is either not in a mutable mode, or already at max level.");
        if (gameWorld.getTeamInfo().getLead(getTeam()) < bot.getLeadMutateCost())
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "You don't have enough lead to mutate this robot.");
        if (gameWorld.getTeamInfo().getGold(getTeam()) < bot.getGoldMutateCost())
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "You don't have enough gold to mutate this robot.");
    }

    @Override
    public boolean canMutate(MapLocation loc) {
        try {
            assertCanMutate(loc);
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void mutate(MapLocation loc) throws GameActionException {
        assertCanMutate(loc);
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        Team team = getTeam();
        InternalRobot bot = this.gameWorld.getRobot(loc);
        int leadNeeded = bot.getLeadMutateCost();
        int goldNeeded = bot.getGoldMutateCost();
        this.gameWorld.getTeamInfo().addLead(team, -leadNeeded);
        this.gameWorld.getTeamInfo().addGold(team, -goldNeeded);
        bot.mutate();
        bot.addActionCooldownTurns(GameConstants.MUTATE_COOLDOWN);
        bot.addMovementCooldownTurns(GameConstants.MUTATE_COOLDOWN);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.MUTATE, bot.getID());
    }

    // ***************************
    // **** TRANSMUTE METHODS **** 
    // ***************************

    @Override
    public int getTransmutationRate() {
        if (getType() != RobotType.LABORATORY) {
            return 0;
        }
        return getTransmutationRate(getLevel());
    }

    @Override
    public int getTransmutationRate(int laboratory_level) {
        final double alchemist_loneliness_k;
        switch (laboratory_level) {
            case 1: alchemist_loneliness_k = GameConstants.ALCHEMIST_LONELINESS_K_L1; break;
            case 2: alchemist_loneliness_k = GameConstants.ALCHEMIST_LONELINESS_K_L2; break;
            case 3: alchemist_loneliness_k = GameConstants.ALCHEMIST_LONELINESS_K_L3; break;
            default: return 0;
        }
        return (int) (GameConstants.ALCHEMIST_LONELINESS_A - GameConstants.ALCHEMIST_LONELINESS_B *
                      Math.exp(-alchemist_loneliness_k * this.robot.getNumVisibleFriendlyRobots(true)));
    }

    private void assertCanTransmute() throws GameActionException {
        assertIsActionReady();
        if (!getType().canTransmute())
            throw new GameActionException(CANT_DO_THAT,
                    "Robot is of type " + getType() + " which cannot transmute lead to gold.");
        if (this.gameWorld.getTeamInfo().getLead(getTeam()) < getTransmutationRate())
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "You don't have enough lead to transmute to gold.");
    }

    @Override
    public boolean canTransmute() {
        try {
            assertCanTransmute();
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void transmute() throws GameActionException {
        assertCanTransmute();
        this.robot.addActionCooldownTurns(getType().actionCooldown);
        Team team = getTeam();
        this.gameWorld.getTeamInfo().addLead(team, -getTransmutationRate());
        this.gameWorld.getTeamInfo().addGold(team, 1);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.TRANSMUTE, -1);
    }

    // ***************************
    // **** TRANSFORM METHODS **** 
    // ***************************

    private void assertCanTransform() throws GameActionException {
        assertIsTransformReady();
    }

    @Override
    public boolean canTransform() {
        try {
            assertCanTransform();
            return true;
        } catch (GameActionException e) { return false; }  
    }

    @Override
    public void transform() throws GameActionException {
        assertCanTransform();
        this.robot.transform();
        if (this.robot.getMode() == RobotMode.TURRET)
            this.robot.addActionCooldownTurns(GameConstants.TRANSFORM_COOLDOWN);
        else
            this.robot.addMovementCooldownTurns(GameConstants.TRANSFORM_COOLDOWN);
        this.gameWorld.getMatchMaker().addAction(getID(), Action.TRANSFORM, -1);
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
    public AnomalyScheduleEntry[] getAnomalySchedule() {
        return this.gameWorld.getAnomalySchedule();
    }

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
