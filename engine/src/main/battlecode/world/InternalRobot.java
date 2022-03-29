package battlecode.world;

import java.lang.Math;
import battlecode.common.*;
import battlecode.schema.Action;

import battlecode.instrumenter.RobotDeathException;

/**
 * The representation of a robot used by the server.
 * Comparable ordering:
 *  - tiebreak by creation time (priority to later creation)
 *  - tiebreak by robot ID (priority to lower ID)
 */
public strictfp class InternalRobot implements Comparable<InternalRobot> {

    private final RobotControllerImpl controller;
    private final GameWorld gameWorld;

    private final int ID;
    private Team team;
    private RobotType type;
    private MapLocation location;
    private float health;

    private long controlBits;
    private int currentBytecodeLimit;
    private int bytecodesUsed;

    private int roundsAlive;
    private int cooldownTurns;

    /**
     * Used to avoid recreating the same RobotInfo object over and over.
     */
    private RobotInfo cachedRobotInfo;

    private String indicatorString;

    /**
     * Create a new internal representation of a robot
     *
     * @param gw the world the robot exists in
     * @param type the type of the robot
     * @param loc the location of the robot
     * @param health the health of the robot
     * @param team the team of the robot
     */
    @SuppressWarnings("unchecked")
    public InternalRobot(GameWorld gw, int id, RobotType type, MapLocation loc, float health, Team team) {
        this.gameWorld = gw;

        this.ID = id;
        this.team = team;
        this.type = type;
        this.location = loc;

        this.health = health;

        this.controlBits = 0;
        this.currentBytecodeLimit = this.type.bytecodeLimit;
        this.bytecodesUsed = 0;

        this.roundsAlive = 0;
        this.cooldownTurns = 0;

        this.indicatorString = "";

        this.controller = new RobotControllerImpl(gameWorld, this);
    }

    // ******************************************
    // ****** GETTER METHODS ********************
    // ******************************************

    public RobotControllerImpl getController() {
        return controller;
    }

    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public int getID() {
        return ID;
    }

    public Team getTeam() {
        return team;
    }

    public RobotType getType() {
        return type;
    }

    public MapLocation getLocation() {
        return location;
    }

    public float getHealth() {
        return health;
    }

    public long getControlBits() {
        return controlBits;
    }

    public int getBytecodesUsed() {
        return bytecodesUsed;
    }

    public int getRoundsAlive() {
        return roundsAlive;
    }

    public int getCooldownTurns() {
        return cooldownTurns;
    }

    public RobotInfo getRobotInfo() {
        if (cachedRobotInfo != null
                && cachedRobotInfo.ID == ID
                && cachedRobotInfo.team == team
                && cachedRobotInfo.type == type
                && cachedRobotInfo.health == health
                && cachedRobotInfo.location.equals(location)) {
            return cachedRobotInfo;
        }

        this.cachedRobotInfo = new RobotInfo(ID, team, type, health, location);
        return this.cachedRobotInfo;
    }

    // **********************************
    // ****** CHECK METHODS *************
    // **********************************

    /**
     * Returns whether the robot can either move or act based on cooldown.
     */
    public boolean isReady() {
        return this.cooldownTurns < GameConstants.COOLDOWN_LIMIT;
    }

    // ******************************************
    // ****** UPDATE METHODS ********************
    // ******************************************

    /**
     * Sets the indicator string of the robot.
     *
     * @param string the new indicator string of the robot
     */
    public void setIndicatorString(String string) {
        this.indicatorString = string;
    }

    /**
     * Sets the location of the robot.
     * 
     * @param loc the new location of the robot
     */
    public void setLocation(MapLocation loc) {
        this.gameWorld.getObjectInfo().moveRobot(this, loc);
        this.location = loc;
    }

    /**
     * Resets the cooldown.
     */
    public void resetCooldownTurns() {
        addCooldownTurns(GameConstants.COOLDOWN_LIMIT);;
    }

    /**
     * Adds to the cooldown.
     */
    public void addCooldownTurns(int numCooldownToAdd) {
        setCooldownTurns(this.cooldownTurns + numCooldownToAdd);
    }

    /**
     * Sets the cooldown given the number of turns.
     * 
     * @param newActionTurns the number of cooldown turns
     */
    public void setCooldownTurns(int newTurns) {
        this.cooldownTurns = newTurns;
    }

    /**
     * Subtracts health from a robot. Input can be positive to add health.
     * 
     * @param healthAmount the amount to damage by
     * @param checkWin whether to end the game if the last robot dies
     */
    public void damageHealth(float healthAmount) {
        float oldHealth = this.health;
        this.health -= healthAmount;
        if (this.health <= 0) {
            this.gameWorld.destroyRobot(this.ID);
        } else if (this.health != oldHealth) {
            this.gameWorld.getMatchMaker().addAction(getID(), Action.CHANGE_HEALTH, (int)(this.health - oldHealth));
        }
    }

    // *********************************
    // ****** ACTION METHODS *********
    // *********************************

    /**
     * Handles logic regarding a robot that steps on another robot.
     * @param the bot present on the square before
     * 
     * @return whether or not the current robot survives
     */
    public boolean collide(MapLocation loc, InternalRobot bot){
        this.gameWorld.removeRobot(loc);
        if (Math.abs(bot.getHealth() - this.getHealth()) <= GameConstants.COLLISION_EQUALITY_THRESHOLD){
            this.gameWorld.destroyRobot(bot.getID());
            this.gameWorld.destroyRobot(this.getID());
            return false;
        }
        else {
            float newHealth = Math.abs(bot.getHealth() - this.getHealth()) + 1;
            if (this.getHealth() < bot.getHealth()){
                this.gameWorld.destroyRobot(this.getID());
                bot.damageHealth(bot.getHealth() - newHealth);
                this.gameWorld.addRobot(loc, bot);
                return false;
            }
            else {
                this.gameWorld.destroyRobot(bot.getID());
                this.damageHealth(this.getHealth() - newHealth);
                this.gameWorld.addRobot(loc, this);
                return true;
            }
        }
    }

    // *********************************
    // ****** GAMEPLAY METHODS *********
    // *********************************

    // should be called at the beginning of every round
    public void processBeginningOfRound() {
        if (this.type == RobotType.ROBOT) {
            this.indicatorString = "";
        }
    }

    public void processBeginningOfTurn() {
        if (this.type == RobotType.ROBOT) {
            this.cooldownTurns = Math.max(0, this.cooldownTurns - GameConstants.COOLDOWNS_PER_TURN);
        } else {
            this.currentBytecodeLimit = this.type.bytecodeLimit;
        }
    }

    public void processEndOfTurn() {
        // bytecode stuff!
        if (this.type == RobotType.CONTROLLER)
            this.gameWorld.getMatchMaker().addBytecodes(this.ID, this.bytecodesUsed);
        // indicator strings!
        if (this.type == RobotType.ROBOT)
            this.gameWorld.getMatchMaker().addIndicatorString(this.ID, this.indicatorString);
    }

    public void processEndOfRound() {
        if (this.type == RobotType.CONTROLLER) {
            return;
        }
        this.damageHealth(this.type.healthDecay * this.getHealth());
        if (this.getHealth() < this.type.healthLimit){
            System.out.println("Time to destroy myself " + this);
            this.gameWorld.destroyRobot(getID());
            return;
        }
        this.roundsAlive++;
    }

    // *********************************
    // ****** BYTECODE METHODS *********
    // *********************************

    // TODO
    public boolean canExecuteCode() {
        return true;
    }

    public void setBytecodesUsed(int numBytecodes) {
        this.bytecodesUsed = numBytecodes;
    }

    public int getBytecodeLimit() {
        return canExecuteCode() ? this.currentBytecodeLimit : 0;
    }

    // *********************************
    // ****** VARIOUS METHODS **********
    // *********************************

    public void disintegrate() {
        throw new RobotDeathException();
    }

    public void die_exception() {
        this.gameWorld.getMatchMaker().addAction(getID(), Action.DIE_EXCEPTION, -1);
        this.gameWorld.destroyRobot(getID());
    }

    // *****************************************
    // ****** MISC. METHODS ********************
    // *****************************************

    @Override
    public boolean equals(Object o) {
        return o != null && (o instanceof InternalRobot)
                && ((InternalRobot) o).getID() == ID;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public String toString() {
        return String.format("%s:%s#%d", getTeam(), getType(), getID());
    }

    @Override
    public int compareTo(InternalRobot o) {
        if (this.roundsAlive != o.roundsAlive)
            return this.roundsAlive - o.roundsAlive;
        return this.ID - o.ID;
    }
}
