// automatically generated by the FlatBuffers compiler, do not modify

package battlecode.schema;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

/**
 * The map a round is played on.
 */
@SuppressWarnings("unused")
public final class GameMap extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_2_0_0(); }
  public static GameMap getRootAsGameMap(ByteBuffer _bb) { return getRootAsGameMap(_bb, new GameMap()); }
  public static GameMap getRootAsGameMap(ByteBuffer _bb, GameMap obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public GameMap __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  /**
   * The name of a map.
   */
  public String name() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  /**
   * The bottom corner of the map.
   */
  public battlecode.schema.Vec minCorner() { return minCorner(new battlecode.schema.Vec()); }
  public battlecode.schema.Vec minCorner(battlecode.schema.Vec obj) { int o = __offset(6); return o != 0 ? obj.__assign(o + bb_pos, bb) : null; }
  /**
   * The top corner of the map.
   */
  public battlecode.schema.Vec maxCorner() { return maxCorner(new battlecode.schema.Vec()); }
  public battlecode.schema.Vec maxCorner(battlecode.schema.Vec obj) { int o = __offset(8); return o != 0 ? obj.__assign(o + bb_pos, bb) : null; }
  /**
   * The map symmetry: 0 for rotation, 1 for horizontal, 2 for vertical.
   */
  public int symmetry() { int o = __offset(10); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /**
   * The bodies on the map.
   */
  public battlecode.schema.SpawnedBodyTable bodies() { return bodies(new battlecode.schema.SpawnedBodyTable()); }
  public battlecode.schema.SpawnedBodyTable bodies(battlecode.schema.SpawnedBodyTable obj) { int o = __offset(12); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  /**
   * The random seed of the map.
   */
  public int randomSeed() { int o = __offset(14); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  /**
   * The walls on the map.
   */
  public int walls(int j) { int o = __offset(16); return o != 0 ? bb.getInt(__vector(o) + j * 4) : 0; }
  public int wallsLength() { int o = __offset(16); return o != 0 ? __vector_len(o) : 0; }
  public IntVector wallsVector() { return wallsVector(new IntVector()); }
  public IntVector wallsVector(IntVector obj) { int o = __offset(16); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer wallsAsByteBuffer() { return __vector_as_bytebuffer(16, 4); }
  public ByteBuffer wallsInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 16, 4); }
  /**
   * The uranium on the map.
   */
  public int uranium(int j) { int o = __offset(18); return o != 0 ? bb.getInt(__vector(o) + j * 4) : 0; }
  public int uraniumLength() { int o = __offset(18); return o != 0 ? __vector_len(o) : 0; }
  public IntVector uraniumVector() { return uraniumVector(new IntVector()); }
  public IntVector uraniumVector(IntVector obj) { int o = __offset(18); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer uraniumAsByteBuffer() { return __vector_as_bytebuffer(18, 4); }
  public ByteBuffer uraniumInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 18, 4); }
  /**
   * The spawn locations.
   */
  public int spawnLocation(int j) { int o = __offset(20); return o != 0 ? bb.getInt(__vector(o) + j * 4) : 0; }
  public int spawnLocationLength() { int o = __offset(20); return o != 0 ? __vector_len(o) : 0; }
  public IntVector spawnLocationVector() { return spawnLocationVector(new IntVector()); }
  public IntVector spawnLocationVector(IntVector obj) { int o = __offset(20); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer spawnLocationAsByteBuffer() { return __vector_as_bytebuffer(20, 4); }
  public ByteBuffer spawnLocationInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 20, 4); }

  public static void startGameMap(FlatBufferBuilder builder) { builder.startTable(9); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(0, nameOffset, 0); }
  public static void addMinCorner(FlatBufferBuilder builder, int minCornerOffset) { builder.addStruct(1, minCornerOffset, 0); }
  public static void addMaxCorner(FlatBufferBuilder builder, int maxCornerOffset) { builder.addStruct(2, maxCornerOffset, 0); }
  public static void addSymmetry(FlatBufferBuilder builder, int symmetry) { builder.addInt(3, symmetry, 0); }
  public static void addBodies(FlatBufferBuilder builder, int bodiesOffset) { builder.addOffset(4, bodiesOffset, 0); }
  public static void addRandomSeed(FlatBufferBuilder builder, int randomSeed) { builder.addInt(5, randomSeed, 0); }
  public static void addWalls(FlatBufferBuilder builder, int wallsOffset) { builder.addOffset(6, wallsOffset, 0); }
  public static int createWallsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addInt(data[i]); return builder.endVector(); }
  public static void startWallsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addUranium(FlatBufferBuilder builder, int uraniumOffset) { builder.addOffset(7, uraniumOffset, 0); }
  public static int createUraniumVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addInt(data[i]); return builder.endVector(); }
  public static void startUraniumVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addSpawnLocation(FlatBufferBuilder builder, int spawnLocationOffset) { builder.addOffset(8, spawnLocationOffset, 0); }
  public static int createSpawnLocationVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addInt(data[i]); return builder.endVector(); }
  public static void startSpawnLocationVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endGameMap(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public GameMap get(int j) { return get(new GameMap(), j); }
    public GameMap get(GameMap obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

