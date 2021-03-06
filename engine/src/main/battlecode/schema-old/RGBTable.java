// automatically generated by the FlatBuffers compiler, do not modify

// package battlecode.schema;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
/**
 * A table of RGB values.
 */
public final class RGBTable extends Table {
  public static RGBTable getRootAsRGBTable(ByteBuffer _bb) { return getRootAsRGBTable(_bb, new RGBTable()); }
  public static RGBTable getRootAsRGBTable(ByteBuffer _bb, RGBTable obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; vtable_start = bb_pos - bb.getInt(bb_pos); vtable_size = bb.getShort(vtable_start); }
  public RGBTable __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int red(int j) { int o = __offset(4); return o != 0 ? bb.getInt(__vector(o) + j * 4) : 0; }
  public int redLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer redAsByteBuffer() { return __vector_as_bytebuffer(4, 4); }
  public ByteBuffer redInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 4); }
  public int green(int j) { int o = __offset(6); return o != 0 ? bb.getInt(__vector(o) + j * 4) : 0; }
  public int greenLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer greenAsByteBuffer() { return __vector_as_bytebuffer(6, 4); }
  public ByteBuffer greenInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 4); }
  public int blue(int j) { int o = __offset(8); return o != 0 ? bb.getInt(__vector(o) + j * 4) : 0; }
  public int blueLength() { int o = __offset(8); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer blueAsByteBuffer() { return __vector_as_bytebuffer(8, 4); }
  public ByteBuffer blueInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 8, 4); }

  public static int createRGBTable(FlatBufferBuilder builder,
      int redOffset,
      int greenOffset,
      int blueOffset) {
    builder.startObject(3);
    RGBTable.addBlue(builder, blueOffset);
    RGBTable.addGreen(builder, greenOffset);
    RGBTable.addRed(builder, redOffset);
    return RGBTable.endRGBTable(builder);
  }

  public static void startRGBTable(FlatBufferBuilder builder) { builder.startObject(3); }
  public static void addRed(FlatBufferBuilder builder, int redOffset) { builder.addOffset(0, redOffset, 0); }
  public static int createRedVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addInt(data[i]); return builder.endVector(); }
  public static void startRedVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addGreen(FlatBufferBuilder builder, int greenOffset) { builder.addOffset(1, greenOffset, 0); }
  public static int createGreenVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addInt(data[i]); return builder.endVector(); }
  public static void startGreenVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addBlue(FlatBufferBuilder builder, int blueOffset) { builder.addOffset(2, blueOffset, 0); }
  public static int createBlueVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addInt(data[i]); return builder.endVector(); }
  public static void startBlueVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endRGBTable(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

