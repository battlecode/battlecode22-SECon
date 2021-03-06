package battlecode.common;

/**
 * Enumerates the possible types of robots. More information about the
 * capabilities of each robot type are available in the game specs.
 *
 * You can check the type of another robot by inspecting {@link RobotInfo#type},
 * or your own type by inspecting {@link RobotController#getType}.
 */
public enum RobotType {

    // Action Cooldown, Health Decay, Health Limit, Bytecode Limit

    /**
     * Robots are general-purpose units. They can mine uranium deposts around
     * the map and explode to attack enemy units. If they move onto an enemy unit,
     * the two robots initiate a battle. 
     *
     * @battlecode.doc.robottype
     */
    ROBOT       (  10, 0.0014f, 0.1f,      0),
    //           AC    HD     HL           BL
    CONTROLLER  (    0,     0,     0, 100000),
    //           AC    HD     HL           BL
    ;

    /**
     * The action cooldown applied to the robot per action.
    */
    public final int actionCooldown;

    /**
     * The fraction of health lost per turn due to radioactive decay.
     */
    public final float healthDecay;

    /**
     * The minimum health value before the robot disintegrates.
     */
    public final float healthLimit;

    /**
     * Bytecode limit for this robot.
     */
    public final int bytecodeLimit;

    RobotType(int actionCooldown, float healthDecay, float healthLimit, int bytecodeLimit) {
        this.actionCooldown                 = actionCooldown;
        this.healthDecay                    = healthDecay;
        this.healthLimit                    = healthLimit;
        this.bytecodeLimit                  = bytecodeLimit;
    }
}
