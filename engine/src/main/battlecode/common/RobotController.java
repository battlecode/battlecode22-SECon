package battlecode.common;
import java.util.ArrayList;

/**
 * A RobotController allows contestants to make their robot sense and interact
 * with the game world. When a contestant's <code>RobotPlayer</code> is
 * constructed, it is passed an instance of <code>RobotController</code> that
 * controls the newly created robot.
 */
@SuppressWarnings("unused")
public strictfp interface RobotController {

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************

    /**
     * Returns the current round number, where round 1 is the first round of the
     * match.
     *
     * @return the current round number, where round 1 is the first round of the
     * match
     *
     * @battlecode.doc.costlymethod
     */
    int getRoundNum();

    /**
     * Returns the width of the game map. Valid x coordinates range from
     * 0 (inclusive) to the width (exclusive).
     *
     * @return the map width
     *
     * @battlecode.doc.costlymethod
     */
    int getMapWidth();

    /**
     * Returns the height of the game map. Valid y coordinates range from
     * 0 (inclusive) to the height (exclusive).
     *
     * @return the map height
     *
     * @battlecode.doc.costlymethod
     */
    int getMapHeight();

    /**
     * Returns the number of robots on your team.
     * If this number ever reaches zero, you immediately lose.
     *
     * @return the number of robots on your team
     *
     * @battlecode.doc.costlymethod
     */
    int getRobotCount();

    /**
     * Returns the amount of uranium a team has in its reserves.
     *
     * @param team the team being queried.
     * @return the amount of uranium a team has in its reserves.
     *
     * @battlecode.doc.costlymethod
     */
    int getTeamUraniumAmount(Team team);

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

    /**
     * Returns the ID of this robot.
     *
     * @return the ID of this robot
     *
     * @battlecode.doc.costlymethod
     */
    int getID();

    /**
     * Returns this robot's Team.
     *
     * @return this robot's Team
     *
     * @battlecode.doc.costlymethod
     */
    Team getTeam();

    /**
     * Returns this robot's current location.
     *
     * @return this robot's current location
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation getLocation();

    /**
     * Returns this robot's current health.
     *
     * @return this robot's current health
     *
     * @battlecode.doc.costlymethod
     */
    float getHealth();

    // ***********************************
    // ****** GENERAL VISION METHODS *****
    // ***********************************

    /**
     * Checks whether a MapLocation is on the map.
     *
     * @param loc the location to check
     * @return true if the location is on the map; false otherwise
     *
     * @battlecode.doc.costlymethod
     */
    boolean onTheMap(MapLocation loc);

    /**
     * Checks whether a robot is at a given location. Assumes the location is valid.
     *
     * @param loc the location to check
     * @return true if a robot is at the location
     * @throws GameActionException if the location is not on the map
     *
     * @battlecode.doc.costlymethod
     */
    boolean isLocationOccupied(MapLocation loc) throws GameActionException;

    /**
     * Checks whether a robot is at a given location. Assume the location is valid.
     *
     * @param loc the location to check
     * @return true if a robot is at the location, false if there is no robot or the location is not on the map.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseRobotAtLocation(MapLocation loc);

    /**
     * Senses the robot at the given location, or null if there is no robot
     * there.
     *
     * @param loc the location to check
     * @return the robot at the given location
     * @throws GameActionException if the location is not on the map
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException;

    /**
     * Tests whether the given robot exists
     *
     * @param id the ID of the robot to query
     * @return true if the given robot exists; false otherwise
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseRobot(int id);

    /**
     * Senses information about a particular robot given its ID.
     *
     * @param id the ID of the robot to query
     * @return a RobotInfo object for the sensed robot
     * @throws GameActionException if the robot doesn't exist
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo senseRobot(int id) throws GameActionException;

  /**
     * Returns all robots. The objects are returned in no
     * particular order.
     *
     * @return array of RobotInfo objects, which contain information about all
     * the robots
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots();

    /**
     * Returns all robots within a certain distance of this
     * robot. The objects are returned in no particular order.
     *
     * @param radiusSquared return robots wihtin this distance rom the center of
     * this robot; if -1 is passed, all robots are returned;
     * @return array of RobotInfo objects of all the robots you saw
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(int radiusSquared);

    /**
     * Returns all robots of a given team within a certain
     * distance of this robot. The objects are returned in no particular order.
     *
     * @param radiusSquared return robots within this distance away from the center of
     * this robot; if -1 is passed, all robots are returned;
     * @param team filter game objects by the given team; if null is passed,
     * robots from any team are returned
     * @return array of RobotInfo objects of all the robots you saw
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(int radiusSquared, Team team);

    /**
     * Returns all robots of a given team within a certain
     * radius of a specified location. The objects are returned in no particular
     * order.
     *
     * @param center center of the given search radius
     * @param radiusSquared return robots this distance away from the center; 
     * if -1 is passed, all robots are returned;
     * @param team filter game objects by the given team; if null is passed,
     * objects from all teams are returned
     * @return array of RobotInfo objects of the robots you saw
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team);

    /**
     * Given a location, returns whether a wall is at that location.
     * 
     * @param loc the given location
     * @return whether a wall exists
     * @throws GameActionException if the given location is invalid
     *
     * @battlecode.doc.costlymethod
     */
    boolean senseWall(MapLocation loc) throws GameActionException;

    /**
     * Given a location, returns the uranium count of that location.
     * 
     * @param loc the given location
     * @return the amount of uranium at that location
     * @throws GameActionException if the given location is invalid
     *
     * @battlecode.doc.costlymethod
     */
    int senseUranium(MapLocation loc) throws GameActionException;

    /**
     * Return all locations that contain a nonzero amount of uranium.
     *
     * @return all locations that contain a nonzero amount of uranium
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithUranium();

    /**
     * Return all locations that contain a nonzero amount of uranium, within a
     * specified radius of your robot location.
     * If radiusSquared is -1, all locations are returned.
     *
     * @param radiusSquared the squared radius of all locations to be returned
     * @return all locations that contain a nonzero amount of uranium within the radius
     * @throws GameActionException if the radius is negative 
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithUranium(int radiusSquared) throws GameActionException;

    /**
     * Return all locations that contain a nonzero amount of uranium, within a
     * specified radius of a center location.
     * If radiusSquared is -1, all locations are returned.
     *
     * @param center the center of the search area
     * @param radiusSquared the squared radius of all locations to be returned
     * @return all locations that contain a nonzero amount of uranium within the radius
     * @throws GameActionException if the radius is negative
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithUranium(MapLocation center, int radiusSquared) throws GameActionException;

    /**
     * Return all locations that contain at least a certain amount of uranium, within a
     * specified radius of your robot location.
     * If radiusSquared is -1, all locations are returned.
     *
     * @param radiusSquared the squared radius of all locations to be returned
     * @param minLead the minimum amount of uranium
     * @return all locations that contain at least minUranium uranium within the radius
     * @throws GameActionException if the radius is negative
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithUranium(int radiusSquared, int minUranium) throws GameActionException;

    /**
     * Return all locations that contain at least a certain amount of uranium, within a
     * specified radius of a center location.
     * If radiusSquared is -1, all locations are returned.
     *
     * @param center the center of the search area
     * @param radiusSquared the squared radius of all locations to be returned
     * @param minLead the minimum amount of uranium
     * @return all locations that contain at least minUranium uranium within the radius
     * @throws GameActionException if the radius is negative or center is invalid
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithUranium(MapLocation center, int radiusSquared, int minUranium) throws GameActionException;

    /**
     * Returns the location adjacent to current location in the given direction.
     *
     * @param dir the given direction
     * @return the location adjacent to current location in the given direction
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation adjacentLocation(Direction dir);

    /**
     * Returns a list of all locations within the given radiusSquared of a location.
     *
     * Checks that radiusSquared is non-negative.
     *
     * @param center the given location
     * @param radiusSquared return locations within this distance away from center
     * @return list of locations on the map and within radiusSquared of center
     * @throws GameActionException if the radius is negative
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) throws GameActionException;

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    /**
     * Tests whether the robot can do one and any of move, act, or mine.
     * 
     * @return true if the robot can do one and any of move, act, or mine.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isReady();

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    /**
     * Checks whether this robot can move one step in the given direction.
     * Returns false if the robot is not in a mode that can move, if the target
     * location is not on the map, if the target location is occupied by an ally, 
     * or if there are cooldown turns remaining.
     *
     * @param dir the direction to move in
     * @return true if it is possible to call <code>move</code> without an exception
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMove(Direction dir);

    /**
     * Moves one step in the given direction. If there is an enemy robot in the 
     * direction, the two robots enter battle. 
     * 
     * If a battle ensues, both robots will be destroyed if their target health
     * is within 1 of each other. Otherwise, the lower health robot is destroyed 
     * and the winning robot has its health reduced to |x - y| + 1, where x and y 
     * are the two robots' initial health values. 
     *
     * @param dir the direction to move in
     * @throws GameActionException if the robot cannot move one step in this
     * direction, such as cooldown being too high, the target location being
     * off the map, or the target destination being occupied by an allied robot
     *
     * @battlecode.doc.costlymethod
     */
    void move(Direction dir) throws GameActionException;

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    /**
     * Tests whether a robot can be built. Checks that the spawn location is not occupied 
     * by a friendly robot and that the robot has the amount of uranium it's trying to spend.
     *
     * @param health, the health of the robot to build
     * @return whether it is possible to build a robot of this cost in your spawn location.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canBuildRobot(int health);

    /**
     * Builds a robot at the given location. The robot has the specified health specified 
     * by its cost
     *
     * @param health, the health of the robot to build
     * @throws GameActionException if the conditions of canBuildRobot
     * are not all satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void buildRobot(int health) throws GameActionException;

    // *****************************
    // **** COMBAT UNIT METHODS **** 
    // *****************************

    /**
     * Tests whether this robot can explode.
     * 
     * Checks that no cooldown turns remain.
     *
     * @return whether it is possible to explode
     *
     * @battlecode.doc.costlymethod
     */
    boolean canExplode();

    /** 
     * Explode, dealing damage equal to half of the robot's current health to enemy robots on the 
     * four adjacent squares. The robot is destroyed. 
     *
     * @throws GameActionException if conditions for exploding are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void explode() throws GameActionException;

    // ***********************
    // **** MINER METHODS **** 
    // ***********************

    /**
     * Tests whether the robot can mine.
     * 
     * Checks that no cooldown turns remain and there is at least 1 uranium to mine at that square.
     *
     * @return whether it is possible to mine at the current location
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMine();

    /** 
     * Mine at the current location.
     *
     * @throws GameActionException if conditions for mining are not satisfied
     *
     * @battlecode.doc.costlymethod
     */
    void mine() throws GameActionException;

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************
    
    /**
     * Causes your team to lose the game. It's like typing "gg."
     *
     * @battlecode.doc.costlymethod
     */
    void resign();

    // ***********************************
    // ******** DEBUG METHODS ************
    // ***********************************

    /**
     * Sets the indicator string for this robot for debugging purposes. Only the first
     * {@link GameConstants#INDICATOR_STRING_MAX_LENGTH} characters are used.
     *
     * @param string the indicator string this round
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorString(String string);

    /**
     * Draw a dot on the game map for debugging purposes.
     *
     * @param loc the location to draw the dot
     * @param red the red component of the dot's color
     * @param green the green component of the dot's color
     * @param blue the blue component of the dot's color
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorDot(MapLocation loc, int red, int green, int blue);

    /**
     * Draw a line on the game map for debugging purposes.
     *
     * @param startLoc the location to draw the line from
     * @param endLoc the location to draw the line to
     * @param red the red component of the line's color
     * @param green the green component of the line's color
     * @param blue the blue component of the line's color
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue);
}
