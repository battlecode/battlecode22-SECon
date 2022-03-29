package examplefuncsplayer;

import battlecode.common.*;
import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        if (rc.getTeam() == Team.A) rng.nextInt(1);

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm a " + rc.getType() + " and I just got created!");

        // You can also use indicators to save debug notes in replays.
        // rc.setIndicatorString("Hello world!");

        int idx = 0;

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!
            System.out.println("Age: " + turnCount);

            System.out.println("index is: " + idx);
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case ROBOT:     runRobot(rc);  break;
                    case CONTROLLER: runController(rc, idx); break;
                }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for a Robot.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runRobot(RobotController rc) throws GameActionException {
        System.out.println("Pls no!");
    }

    static void runController(RobotController rc, int idx) throws GameActionException {

        // System.out.println("I'm an all powerful controller. (I think) " + rc.getType() + " I have " + rc.getRobotCount() + " robot(s) under my control.");
        System.out.println("Amount uranium " + rc.getTeamUraniumAmount(rc.getTeam()));
        if (rc.getTeamUraniumAmount(rc.getTeam()) > 1) {
            int health = rng.nextInt(rc.getTeamUraniumAmount(rc.getTeam()) - 1) + 1;
            if (!rc.isLocationOccupied(rc.getSpawnLoc())) {
                rc.buildRobot(health);
            }
            if (rc.canBuildRobot(health)) {
                rc.buildRobot(health);
                System.out.println("Built new robot of health " + health);
            }
        }
        RobotInfo[] myRobots = rc.senseNearbyRobots(new MapLocation(0, 0), -1, rc.getTeam());
        // System.out.println("I found " + myRobots.length + " robots to control.");

        for (int i = 0; i < myRobots.length; i ++) {
            int robotId = myRobots[idx].getID();
            int rngActionInt = rng.nextInt(100);
            System.out.println("This robot's action is decided by arbitrary number " + rngActionInt);

            if (rngActionInt < 50 && rc.canMine(robotId)) {
                // Let's try to mine
                // System.out.println("Mining, original amount: " + rc.getTeamUraniumAmount(rc.getTeam()));
                rc.mine(robotId);
                // System.out.println("Mined, final amount: " + rc.getTeamUraniumAmount(rc.getTeam()));
            } else if (rngActionInt < 70 && rc.senseNearbyRobots(rc.getLocation(robotId), 1, rc.getTeam() == Team.A ? Team.B : Team.A).length > 0) {
                System.out.println(rc.senseNearbyRobots(rc.getLocation(robotId), 1, rc.getTeam() == Team.A ? Team.B : Team.A)[0]);
                // Let's try to explode
                if (rc.canExplode(robotId)) {
                    System.out.println("Exploding");
                    rc.explode(robotId);
                }
            }  else if (rngActionInt < 100) {
                System.out.println("It's time to move!");
                // Let's try to move
                Direction dir = directions[rng.nextInt(directions.length)];
                // System.out.println(dir);
                if (rc.canMove(robotId, dir)) {
                    rc.move(robotId, dir);
                    System.out.println("Moving, final place: " + rc.getLocation(robotId));
                }
            }
            idx = (idx + 1) % myRobots.length;
        }
        // System.out.println("I'm robot " + rc.getID() + " at " + rc.getLocation());
        // System.out.println("My action is decided by arbitrary number " + rngActionInt);
        // System.out.println("I have " + rc.getTeamUraniumAmount(rc.getTeam()) + " uranium and " + rc.getHealth() + " health");
        // // Try to build, this doesn't add to cooldown
        // if (rc.getTeamUraniumAmount(rc.getTeam()) > 0) {
        //     int health = rng.nextInt(rc.getTeamUraniumAmount(rc.getTeam())) + 1;
        //     if (rc.canBuildRobot(health)) {
        //         rc.buildRobot(health);
        //         System.out.println("Build new robot of health " + health);
        //     }
        // }
        // if (rngActionInt < 50 && rc.canMine()) {
        //     // Let's try to mine
        //     System.out.println("Mining, original amount: " + rc.getTeamUraniumAmount(rc.getTeam()));
        //     rc.mine();
        //     System.out.println("Mined, final amount: " + rc.getTeamUraniumAmount(rc.getTeam()));
        // } else if (rngActionInt < 70 && rc.senseNearbyRobots(1, rc.getTeam() == Team.A ? Team.B : Team.A).length > 0) {
        //     System.out.println(rc.senseNearbyRobots(1, rc.getTeam() == Team.A ? Team.B : Team.A)[0]);
        //     // Let's try to explode
        //     if (rc.canExplode()) {
        //         System.out.println("Exploding");
        //         rc.explode();
        //     }
        // }  else if (rngActionInt < 100) {
        //     System.out.println("It's time to move!");
        //     // Let's try to move
        //     Direction dir = directions[rng.nextInt(directions.length)];
        //     System.out.println(dir);
        //     if (rc.canMove(dir)) {
        //         rc.move(dir);
        //         System.out.println("Moving, final place: " + rc.getLocation());
        //     }
        // }
    }
}
