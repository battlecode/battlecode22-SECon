package battlecode.common;

/**
 * Enumerates the possible types of robots. More information about the
 * capabilities of each robot type are available in the game specs.
 *
 * You can check the type of another robot by inspecting {@link RobotInfo#type},
 * or your own type by inspecting {@link RobotController#getType}.
 */
public enum RobotType {

    // Action Cooldown, Movement Cooldown, Health Decay, Bytecode Limit

    /**
     * Robots are general-purpose units. They can mine uranium deposts around
     * the map and explode to attack enemy units. 
     *
     * @battlecode.doc.robottype
     */
    ROBOT (  10, 10, 0.0007, 100000),
    //       AC  MC    HD      BL
    ;

    /**
     * The action cooldown applied to the robot per action.
    */
    public final int actionCooldown;

    /**
     * The movement cooldown applied to the robot per move.
     */
    public final int movementCooldown;

    /**
     * The fraction of health lost per turn due to radioactive decay.
     */
    public final float healthDecay;

    /**
     * Base bytecode limit of this robot.
     */
    public final int bytecodeLimit;

    RobotType(int actionCooldown, int movementCooldown, float healthDecay, int bytecodeLimit) {
        this.actionCooldown                 = actionCooldown;
        this.movementCooldown               = movementCooldown;
        this.healthDecay                    = healthDecay;
        this.bytecodeLimit                  = bytecodeLimit;
    }
}
