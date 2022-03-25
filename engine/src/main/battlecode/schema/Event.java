// automatically generated by the FlatBuffers compiler, do not modify

package battlecode.schema;

/**
 * Events
 * An Event is a single step that needs to be processed.
 * A saved game simply consists of a long list of Events.
 * Events can be divided by either being sent separately (e.g. as separate
 * websocket messages), or by being wrapped with a GameWrapper.
 * A game consists of a series of matches; a match consists of a series of
 * rounds, and is played on a single map. Each round is a single simulation
 * step.
 */
@SuppressWarnings("unused")
public final class Event {
  private Event() { }
  public static final byte NONE = 0;
  /**
   * There should only be one GameHeader, at the start of the stream.
   */
  public static final byte GameHeader = 1;
  /**
   * There should be one MatchHeader at the start of each match.
   */
  public static final byte MatchHeader = 2;
  /**
   * A single simulation step. A round may be skipped if
   * nothing happens during its time.
   */
  public static final byte Round = 3;
  /**
   * There should be one MatchFooter at the end of each simulation step.
   */
  public static final byte MatchFooter = 4;
  /**
   * There should only be one GameFooter, at the end of the stream.
   */
  public static final byte GameFooter = 5;

  public static final String[] names = { "NONE", "GameHeader", "MatchHeader", "Round", "MatchFooter", "GameFooter", };

  public static String name(int e) { return names[e]; }
}

