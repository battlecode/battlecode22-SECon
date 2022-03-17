package battlecode.common;

/**
 * GameConstants defines constants that affect gameplay.
 */
@SuppressWarnings("unused")
public class GameConstants {

    /**
     * The current spec version the server compiles with.
     */
    public static final String SPEC_VERSION = "2022.2.1.0";

    // *********************************
    // ****** MAP CONSTANTS ************
    // *********************************

    /** The minimum possible map height. */
    public static final int MAP_MIN_HEIGHT = 20;

    /** The maximum possible map height. */
    public static final int MAP_MAX_HEIGHT = 60;

    /** The minimum possible map width. */
    public static final int MAP_MIN_WIDTH = 20;

    /** The maximum possible map width. */
    public static final int MAP_MAX_WIDTH = 60;

    /** The number of starting robots per team. */
    public static final int NUM_STARTING_ROBOTS = 1;

    // *********************************
    // ****** GAME PARAMETERS **********
    // *********************************

    /** The maximum length of indicator strings that a player can associate with a robot. */
    public static final int INDICATOR_STRING_MAX_LENGTH = 64;

    /** The bytecode penalty that is imposed each time an exception is thrown. */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;

    /** The initial amount of uranium each team starts with. */
    public static final int INITIAL_URANIUM_AMOUNT = 10;

    /** The amount of uranium each team gains per turn. */
    public static final int PASSIVE_URANIUM_INCREASE = 1;

    /** The number of rounds between adding uranium resources to the map. */
    public static final int ADD_URANIUM_EVERY_ROUNDS = 20; //TODO: change possibly

    /** The amount of uranium to add each round that uranium is added. */
    public static final int ADD_URANIUM = 5;
    
    // *********************************
    // ****** COOLDOWNS ****************
    // *********************************

    /** If the amount of cooldown is at least this value, a robot cannot act. */
    public static final int COOLDOWN_LIMIT = 10;

    /** The number of cooldown turns reduced per turn. */
    public static final int COOLDOWNS_PER_TURN = 10;

    // *********************************
    // ****** GAME MECHANICS ***********
    // *********************************

    public static final int ROBOT_INITIAL_HEALTH = 1;

    // *********************************
    // ****** GAMEPLAY PROPERTIES ******
    // *********************************

    /** The default game seed. **/
    public static final int GAME_DEFAULT_SEED = 6370;

    /** The maximum number of rounds in a game.  **/
    public static final int GAME_MAX_NUMBER_OF_ROUNDS = 2000; //TODO: may need to change basically, ctrl-f for all the rounds
}
