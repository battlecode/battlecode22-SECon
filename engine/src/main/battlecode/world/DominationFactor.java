package battlecode.world;

/**
 * Determines roughly by how much the winning team won.
 */
public enum DominationFactor {
    /**
     * Win by all enemy archons being destroyed (early end).
     */
    ANNIHILATION,
    /**
     * Win by more uranium net worth (tiebreak 1).
     */
    MORE_URANIUM_NET_WORTH,
    /**
     * Win by more uranium mined total (tiebreak 2).
     */
    MORE_URANIUM_MINED,
    /**
     * Win because blue goes second (tiebreak 3).
     */
    WON_BY_BEING_BLUE,
}
