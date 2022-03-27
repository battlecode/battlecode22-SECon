// automatically generated by the FlatBuffers compiler, do not modify

package battlecode.schema;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

/**
 * Sent to start a match.
 */
@SuppressWarnings("unused")
public final class MatchHeader extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_2_0_0(); }
  public static MatchHeader getRootAsMatchHeader(ByteBuffer _bb) { return getRootAsMatchHeader(_bb, new MatchHeader()); }
  public static MatchHeader getRootAsMatchHeader(ByteBuffer _bb, MatchHeader obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public MatchHeader __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  /**
   * The map the match was played on.
   */
  public battlecode.schema.GameMap map() { return map(new battlecode.schema.GameMap()); }
  public battlecode.schema.GameMap map(battlecode.schema.GameMap obj) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  /**
   * The maximum number of rounds in this match.
   */
  public int maxRounds() { int o = __offset(6); return o != 0 ? bb.getInt(o + bb_pos) : 0; }

  public static int createMatchHeader(FlatBufferBuilder builder,
      int mapOffset,
      int maxRounds) {
    builder.startTable(2);
    MatchHeader.addMaxRounds(builder, maxRounds);
    MatchHeader.addMap(builder, mapOffset);
    return MatchHeader.endMatchHeader(builder);
  }

  public static void startMatchHeader(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addMap(FlatBufferBuilder builder, int mapOffset) { builder.addOffset(0, mapOffset, 0); }
  public static void addMaxRounds(FlatBufferBuilder builder, int maxRounds) { builder.addInt(1, maxRounds, 0); }
  public static int endMatchHeader(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public MatchHeader get(int j) { return get(new MatchHeader(), j); }
    public MatchHeader get(MatchHeader obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

