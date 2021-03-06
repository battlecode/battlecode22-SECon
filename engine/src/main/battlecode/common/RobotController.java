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
     * If this number ever reaches one for your team (your Controller alone), you immediately lose.
     *
     * @return the number of robots on that team.
     * @throws GameActionException if not called by a controller
     *
     * @battlecode.doc.costlymethod
     */
    int getRobotCount() throws GameActionException;

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
     * @return the robot's Team
     *
     * @battlecode.doc.costlymethod
     */
    Team getTeam();

    /**
     * Returns a robot's Team.
     *
     * @param id of robot of interest
     * @return a robot's Team
     * @throws GameActionException if not called by a controller
     *  or if called with invalid ID
     *
     * @battlecode.doc.costlymethod
     */
    Team getTeam(int id) throws GameActionException;

    /**
     * Returns this robot's type.
     *
     * @return the robot's type
     *
     * @battlecode.doc.costlymethod
     */
    RobotType getType();

    /**
     * Returns a robot's type (ROBOT).
     *
     * @param id of robot of interest
     * @return the robot's type
     * @throws GameActionException if not called by a controller 
     *  or if called with invalid ID
     * 
     * @battlecode.doc.costlymethod
     */
    RobotType getType(int id) throws GameActionException;

    /**
     * Returns a robot's current location.
     *
     * @param id of robot of interest
     * @return the robot's current location
     * @throws GameActionException if not called by a controller
     *  or if called on with invalid ID
     *  or if ID corresponds to a controller robot
     * 
     * @battlecode.doc.costlymethod
     */
    MapLocation getLocation(int id) throws GameActionException;

    /**
     * Returns a robot's current health.
     *
     * @param id of robot of interest
     * @return the robot's current health
     * @throws GameActionException if not called by a controller
     *  or if called with invalid ID
     *  or if ID corresponds to a controller robot
     * 
     * @battlecode.doc.costlymethod
     */
    float getHealth(int id) throws GameActionException;

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
     * Returns the spawn location for your team.
     *
     * @return spawn location for your team
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation getSpawnLoc();

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
     * Senses the robot at the given location, or null if there is no robot
     * there.
     *
     * @param loc the location to check
     * @return the robot at the given location
     * @throws GameActionException if the location is not on the map
     *    or if not called by controller
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException;

    /**
     * Tests whether the given robot exists
     *
     * @param id the ID of the robot to query
     * @return true if the given robot exists; false otherwise
     * @throws GameActionException if the robot doesn't exist (ID is invalid)
     *    or if not called by a controller
     * 
     * @battlecode.doc.costlymethod
     */
    boolean canSenseRobot(int id) throws GameActionException;

    /**
     * Senses information about a particular robot given its ID.
     *
     * @param id the ID of the robot to query
     * @return a RobotInfo object for the sensed robot
     * @throws GameActionException if the robot doesn't exist (ID is invalid)
     *   or if not called by a controller
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
     * @throws GameActionException if not called by a controller
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseAllRobots() throws GameActionException;

    /**
     * Returns all robots within a certain distance of passed in
     * robot. The objects are returned in no particular order.
     *
     * @param id id of robot that is center used for radius
     * @param radiusSquared return robots within this distance from the center of
     * the passed robot; if -1 is passed, all robots are returned;
     * @return array of RobotInfo objects of all the robots you saw
     * @throws GameActionException if radiusSquared is negative but not -1
     *     or if not called by a controller
     *     or if ID is invalid
     *     or if called on the ID of a controller
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(int id, int radiusSquared) throws GameActionException;

    /**
     * Returns all robots of a given team within a certain
     * distance of passed in robot. The objects are returned in no particular order.
     *
     * @param id id of robot that is center used for radius
     * @param radiusSquared return robots within this distance away from the center of
     * passed in robot; if -1 is passed, all robots are returned;
     * @param team filter game objects by the given team; if null is passed,
     * robots from any team are returned
     * @return array of RobotInfo objects of all the robots you saw
     * @throws GameActionException if radiusSquared is negative but not -1
     *     or if not called by a controller
     *     or if ID is invalid
     *     or if called on the ID of a controller
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(int id, int radiusSquared, Team team) throws GameActionException;

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
     * @throws GameActionException if radiusSquared is negative but not -1
     *     if not called by a controller
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team) throws GameActionException;

    /**
     * Given a location, returns whether a wall is at that location.
     * 
     * @param loc the given location
     * @return whether a wall exists
     * @throws GameActionException if the given location is invalid
     *     if not called by a controller
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
     *     if not called by a controller
     *
     * @battlecode.doc.costlymethod
     */
    int senseUranium(MapLocation loc) throws GameActionException;

    /**
     * Return all locations that contain a nonzero amount of uranium.
     *
     * @return all locations that contain a nonzero amount of uranium
     *     if not called by a controller
     * 
     * @throws GameActionException if not called by a controller
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithUranium() throws GameActionException;

    /**
     * Return all locations that contain a nonzero amount of uranium, within a
     * specified radius of the passed in robot's location.
     * If radiusSquared is -1, all locations are returned.
     *
     * @param id id of robot that gives center for the radius
     * @param radiusSquared the squared radius of all locations to be returned
     * @return all locations that contain a nonzero amount of uranium within the radius
     * @throws GameActionException if the radius is negative but not -1 
     *     or if not called by a controller
     *     or if ID is invalid
     *     or if ID corresponds to a controller robot
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithUranium(int id, int radiusSquared) throws GameActionException;

    /**
     * Return all locations that contain a nonzero amount of uranium, within a
     * specified radius of a center location.
     * If radiusSquared is -1, all locations are returned.
     *
     * @param center the center of the search area
     * @param radiusSquared the squared radius of all locations to be returned
     * @return all locations that contain a nonzero amount of uranium within the radius
     * @throws GameActionException if the radius is negative but not -1 or center is invalid
     *     if not called by a controller
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithUranium(MapLocation center, int radiusSquared) throws GameActionException;

    /**
     * Return all locations that contain at least a certain amount of uranium, within a
     * specified radius of passed in robot's location.
     * If radiusSquared is -1, all locations are returned.

     * @param id id of robot that gives center for the radius
     * @param radiusSquared the squared radius of all locations to be returned
     * @param minUranium the minimum amount of uranium
     * @return all locations that contain at least minUranium uranium within the radius
     * @throws GameActionException if the radius is negative but not -1
     *     or if not called by a controller
     *     or if ID is invalid
     *     or if ID corresponds to a controller robot
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithUranium(int id, int radiusSquared, int minUranium) throws GameActionException;

    /**
     * Return all locations that contain at least a certain amount of uranium, within a
     * specified radius of a center location.
     * If radiusSquared is -1, all locations are returned.
     *
     * @param center the center of the search area
     * @param radiusSquared the squared radius of all locations to be returned
     * @param minUranium the minimum amount of uranium
     * @return all locations that contain at least minUranium uranium within the radius
     * @throws GameActionException if the radius is negative but not -1 or center is invalid
     *     if not called by a controller
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseNearbyLocationsWithUranium(MapLocation center, int radiusSquared, int minUranium) throws GameActionException;

    /**
     * Returns the location adjacent to passed in robot's current location in the given direction.
     *
     * @param id id of the robot of interest
     * @param dir the given direction
     * @return the location adjacent to the passed in robot's location in the given direction
     * @throws GameActionException if not called by a controller
     *    or if ID is invalid
     *    or if ID corresponds to a controller robot
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation adjacentLocation(int id, Direction dir) throws GameActionException;

    /**
     * Returns a list of all locations within the given radiusSquared of a location.
     *
     * Checks that radiusSquared is non-negative.
     *
     * @param center the given location
     * @param radiusSquared return locations within this distance away from center
     * @return list of locations on the map and within radiusSquared of center
     * @throws GameActionException if the radius is negative
     *     if not called by a controller
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) throws GameActionException;

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    /**
     * Tests whether this robot can do one and any of move, act, or mine.
     * 
     * @param id the id of the robot to check
     * @return true if the robot can do one and any of move, act, or mine.
     *     if not called by a controller
     * @throws GameActionException if not called by a controller
     *  or if ID is invalid
     *  or if called on an ID of a robot you don't own
     *  or if ID corresponds to a controller robot
     *
     * @battlecode.doc.costlymethod
     */
    boolean isReady(int id) throws GameActionException;

    /**
     * Returns the number of cooldown turns for the passed in robot.
     * 
     * @param id the id of the robot to check
     * @return the number of cooldown robots for the robot.
     * @throws GameActionException if not called by a controller
     *  or if ID is invalid
     *  or if called on an ID of a robot you don't own
     *  or if ID corresponds to a controller robot
     *
     * @battlecode.doc.costlymethod
     */
    int getCooldownTurns(int id) throws GameActionException;

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    /**
     * Checks whether the robot passed in can move one step in the given direction.
     * Returns false if the target location is not on the map, 
     * if the target location is occupied by an ally, 
     * or if there are cooldown turns remaining.
     *
     * @param id id of robot of interest
     * @param dir the direction to move in
     * @return true if it is possible to call move without an exception
     * @throws GameActionException if not called by a controller
     *  or if ID is invalid
     *  or if called on an ID of a robot you don't own
     *  or if ID corresponds to a controller robot
     * 
     * @battlecode.doc.costlymethod
     */
    boolean canMove(int id, Direction dir) throws GameActionException;

    /**
     * Moves the robot given by `id` one step in the given direction. If there is an enemy robot in the 
     * direction, the two robots enter battle. 
     * 
     * If a battle ensues, both robots will be destroyed if their target health
     * is within 1 of each other. Otherwise, the lower health robot is destroyed 
     * and the winning robot has its health reduced to |x - y| + 1, where x and y 
     * are the two robots' initial health values. 
     *
     * @param id of robot of interest
     * @param dir the direction to move in
     * @throws GameActionException if the robot cannot move one step in this
     * direction, such as cooldown being too high, the target location being
     * off the map, or the target destination being occupied by an allied robot
     *   or if function is not called by controller
     *   or if ID is invalid
     *   or if called on an ID of a robot you don't own
     *   or if ID corresponds to a controller robot
     * 
     * @battlecode.doc.costlymethod
     */
    void move(int id, Direction dir) throws GameActionException;

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    /**
     * Tests whether a robot can be built. Checks that the spawn location is not occupied 
     * by a friendly robot, the health is greater than 0 and that the robot has the amount of uranium it's trying to spend.
     *
     * @param health, the health of the robot to build
     * @return whether it is possible to build a robot of this cost in your spawn location.
     * @throws GameActionException if not called by a controller
     *
     * @battlecode.doc.costlymethod
     */
    boolean canBuildRobot(int health) throws GameActionException;

    /**
     * Builds a robot at the given location. The robot has the specified health specified 
     * by its cost
     *
     * @param health, the health of the robot to build
     * @throws GameActionException if the conditions of canBuildRobot
     *    are not all satisfied, 
     *     or if this is not called by controller
     *
     * @battlecode.doc.costlymethod
     */
    void buildRobot(int health) throws GameActionException;

    // *****************************
    // **** COMBAT UNIT METHODS **** 
    // *****************************

    /**
     * Tests whether the robot given by 'id' can explode.
     * 
     * Checks that no cooldown turns remain.
     * @param id of robot of interest
     * @return whether it is possible to explode
     * @throws GameActionException if not called by a controller
     *  or if ID is invalid
     *  or if called on an ID of a robot you don't own
     *  or if ID corresponds to a controller robot
     *
     * @battlecode.doc.costlymethod
     */
    boolean canExplode(int id) throws GameActionException;

    /** 
     * Explodes the robot given by 'id', dealing damage equal to half of the robot's current health to enemy robots on the 
     * four adjacent squares. The robot is destroyed. 

     * @param id of robot of interest
     * @throws GameActionException if conditions for exploding are not satisfied
     *    or if not called by controller
     *    or if called on an ID of a robot you don't own
     *    or if ID corresponds to a controller robot
     *
     * @battlecode.doc.costlymethod
     */
    void explode(int id) throws GameActionException;

    // ***********************
    // **** MINER METHODS **** 
    // ***********************

    /**
     * Tests whether the robot given by 'id' can mine.
     * 
     * Checks that no cooldown turns remain and there is at least 1 uranium to mine at that square.
     *
     * @param id of robot of interest
     * @return whether it is possible to mine at the current location
     * @throws GameActionException if not called by a controller
     *  or if ID is invalid
     *  or if called on an ID of a robot you don't own
     *  or if ID corresponds to controller robot
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMine(int id) throws GameActionException;

    /** 
     * Has robot given by 'id' mine at its current location.
     *
     * @param id of robot of interest to perform mining
     * @throws GameActionException if conditions for mining are not satisfied
     *    or if ID is invalid
     *    or if not called by controller
     *    or if called on an ID of a robot you don't own
     *    or if ID corresponds to controller robot
     *
     * @battlecode.doc.costlymethod
     */
    void mine(int id) throws GameActionException;

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************
    
    /**
    
     * Kills the robot with the given id
     * @param id id of the robot that you want to destroy
     * @throws GameActionException if not called by a controller
     *   or if ID is invalid
     *   or if called on an ID of a robot you don't own
     *   or if ID corresponds to controller robot
     *
     * @battlecode.doc.costlymethod
     */
    void disintegrate(int id) throws GameActionException;

    /**
    
     * Causes your team to lose the game. It's like typing "gg."
     * @throws GameActionException if not called by a controller
     *
     * @battlecode.doc.costlymethod
     */
    void resign() throws GameActionException;

    // ***********************************
    // ******** DEBUG METHODS ************
    // ***********************************

    /**
     * Sets the indicator string for this robot for debugging purposes. Only the first
     * {@link GameConstants#INDICATOR_STRING_MAX_LENGTH} characters are used.
     *
     * @param id of the robot to assign the indicator string
     * @param string the indicator string this round
     * @throws GameActionException if not called by a controller
     *  or if ID is invalid
     *  or if called on an ID of a robot you don't own
     *  or if ID corresponds to controller robot
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorString(int id, String string) throws GameActionException;

    /**
     * Draw a dot on the game map for debugging purposes.
     *
     * @param id of the robot associated with the drawn dot
     * @param loc the location to draw the dot
     * @param red the red component of the dot's color
     * @param green the green component of the dot's color
     * @param blue the blue component of the dot's color
     * @throws GameActionException if not called by a controller
     *  or if ID is invalid
     *  or if called on an ID of a robot you don't own
     *  or if ID corresponds to controller robot
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorDot(int id, MapLocation loc, int red, int green, int blue) throws GameActionException;

    /**
     * Draw a line on the game map for debugging purposes.
     *
     * @param id of the robot associated with the drawn line
     * @param startLoc the location to draw the line from
     * @param endLoc the location to draw the line to
     * @param red the red component of the line's color
     * @param green the green component of the line's color
     * @param blue the blue component of the line's color
     * @throws GameActionException if not called by a controller
     *  or if ID is invalid
     *  or if called on an ID of a robot you don't own
     *  or if ID corresponds to controller robot
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorLine(int id, MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) throws GameActionException;
}
