package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.Team;
import java.util.*;
import static battlecode.common.GameActionExceptionType.*;

/**
 * This class is used to hold information regarding team specific values such as
 * team names.
 */
public class TeamInfo {

    private GameWorld gameWorld;
    private int[] uraniumCounts;
    private int[] uraniumMined;
    private final int[] controllerIDs;

    // for reporting round statistics to client
    private int[] oldUraniumCounts;
    private int[] oldUraniumMined;

    /**
     * Create a new representation of TeamInfo
     *
     * @param gameWorld the gameWorld the teams exist in
     */
    public TeamInfo(GameWorld gameWorld, int[] controllerIDs) {
        this.gameWorld = gameWorld;
        this.uraniumCounts = new int[2];
        this.uraniumMined = new int[2];
        this.oldUraniumCounts = new int[2];
        this.oldUraniumMined = new int[2];
        this.controllerIDs = controllerIDs.clone();
    }
    
    // *********************************
    // ***** GETTER METHODS ************
    // *********************************

    /**
     * Get the amount of uranium.
     *
     * @param team the team to query
     * @return the team's uranium count
     */
    public int getUranium(Team team) {
        return this.uraniumCounts[team.ordinal()];
    }

    /**
     * Get the amount of uranium mined.
     *
     * @param team the team to query
     * @return the amount of uranium the team has mined
     */
    public int getUraniumMined(Team team) {
        return this.uraniumMined[team.ordinal()];
    }

    /**
     * Get the id for the controller.
     *
     * @param team the team to query
     * @return the id of the controller
     */
    public int getControllerID(Team team) {
        return this.controllerIDs[team.ordinal()];
    }

    // *********************************
    // ***** UPDATE METHODS ************
    // *********************************

    /**
     * Add to the amount of uranium. If amount is negative, subtract from uranium instead. 
     * 
     * @param team the team to query
     * @param amount the change in the uranium count
     * @throws IllegalArgumentException if the resulting amount of uranium is negative
     */
    public void addUranium(Team team, int amount) throws IllegalArgumentException {
        if (this.uraniumCounts[team.ordinal()] + amount < 0) {
            throw new IllegalArgumentException("Invalid uranium change");
        }
        this.uraniumCounts[team.ordinal()] += amount;
    }

    /**
     * Add to the amount of uranium mined.
     * 
     * @param team the team to query
     * @param amount the change in the uranium count, must be positive
     * @throws IllegalArgumentException if the amount passed in is negative
     */
    public void addUraniumMined(Team team, int amount) throws IllegalArgumentException {
        if (amount < 0) {
            throw new IllegalArgumentException("Invalid uranium change");
        }
        this.uraniumMined[team.ordinal()] += amount;
    }

    public int getRoundUraniumChange(Team team) {
        return this.uraniumCounts[team.ordinal()] - this.oldUraniumCounts[team.ordinal()];
    }

    public int getRoundUraniumMined(Team team) {
        return this.uraniumMined[team.ordinal()] - this.oldUraniumMined[team.ordinal()];
    }

    public void processEndOfRound() {
        if (this.uraniumMined[0] != this.oldUraniumMined[0] && this.uraniumMined[1] != this.oldUraniumMined[1]) {
            throw new IllegalArgumentException("Both teams can not mine uranium");
        }
        this.oldUraniumCounts[0] = this.uraniumCounts[0];
        this.oldUraniumCounts[1] = this.uraniumCounts[1];
        this.oldUraniumMined[0] = this.uraniumMined[0];
        this.oldUraniumMined[1] = this.uraniumMined[1];
    }
}
