// automatically generated by the FlatBuffers compiler, do not modify

import * as flatbuffers from 'flatbuffers';

import { Action } from '../../battlecode/schema/action';
import { RGBTable } from '../../battlecode/schema/r-g-b-table';
import { SpawnedBodyTable } from '../../battlecode/schema/spawned-body-table';
import { VecTable } from '../../battlecode/schema/vec-table';


/**
 * A single time-step in a Game.
 * The bulk of the data in the file is stored in tables like this.
 * Note that a struct-of-arrays format is more space efficient than an array-
 * of-structs.
 */
export class Round {
  bb: flatbuffers.ByteBuffer|null = null;
  bb_pos = 0;
__init(i:number, bb:flatbuffers.ByteBuffer):Round {
  this.bb_pos = i;
  this.bb = bb;
  return this;
}

static getRootAsRound(bb:flatbuffers.ByteBuffer, obj?:Round):Round {
  return (obj || new Round()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
}

static getSizePrefixedRootAsRound(bb:flatbuffers.ByteBuffer, obj?:Round):Round {
  bb.setPosition(bb.position() + flatbuffers.SIZE_PREFIX_LENGTH);
  return (obj || new Round()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
}

/**
 * The IDs of teams in the Game.
 */
teamIDs(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 4);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

teamIDsLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 4);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

teamIDsArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 4);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The total amount of lead change of this team, this round
 */
teamUraniumChanges(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 6);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

teamUraniumChangesLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 6);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

teamUraniumChangesArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 6);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The IDs of bodies that moved.
 */
movedIDs(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 8);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

movedIDsLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 8);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

movedIDsArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 8);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The new locations of bodies that have moved.
 */
movedLocs(obj?:VecTable):VecTable|null {
  const offset = this.bb!.__offset(this.bb_pos, 10);
  return offset ? (obj || new VecTable()).__init(this.bb!.__indirect(this.bb_pos + offset), this.bb!) : null;
}

/**
 * New bodies.
 */
spawnedBodies(obj?:SpawnedBodyTable):SpawnedBodyTable|null {
  const offset = this.bb!.__offset(this.bb_pos, 12);
  return offset ? (obj || new SpawnedBodyTable()).__init(this.bb!.__indirect(this.bb_pos + offset), this.bb!) : null;
}

/**
 * The IDs of bodies that died.
 */
diedIDs(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 14);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

diedIDsLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 14);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

diedIDsArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 14);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The IDs of robots that performed actions.
 * IDs may repeat.
 */
actionIDs(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 16);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

actionIDsLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 16);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

actionIDsArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 16);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The actions performed. These actions allow us to track how much soup or dirt a body carries.
 */
actions(index: number):Action|null {
  const offset = this.bb!.__offset(this.bb_pos, 18);
  return offset ? this.bb!.readInt8(this.bb!.__vector(this.bb_pos + offset) + index) : 0;
}

actionsLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 18);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

actionsArray():Int8Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 18);
  return offset ? new Int8Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The 'targets' of the performed actions. Actions without targets may have any value
 */
actionTargets(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 20);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

actionTargetsLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 20);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

actionTargetsArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 20);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The IDs of the robots who changed their indicator strings
 */
indicatorStringIDs(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 22);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

indicatorStringIDsLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 22);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

indicatorStringIDsArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 22);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The messages of the robots who changed their indicator strings
 */
indicatorStrings(index: number):string
indicatorStrings(index: number,optionalEncoding:flatbuffers.Encoding):string|Uint8Array
indicatorStrings(index: number,optionalEncoding?:any):string|Uint8Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 24);
  return offset ? this.bb!.__string(this.bb!.__vector(this.bb_pos + offset) + index * 4, optionalEncoding) : null;
}

indicatorStringsLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 24);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

/**
 * The IDs of bodies that set indicator dots
 */
indicatorDotIDs(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 26);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

indicatorDotIDsLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 26);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

indicatorDotIDsArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 26);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The location of the indicator dots
 */
indicatorDotLocs(obj?:VecTable):VecTable|null {
  const offset = this.bb!.__offset(this.bb_pos, 28);
  return offset ? (obj || new VecTable()).__init(this.bb!.__indirect(this.bb_pos + offset), this.bb!) : null;
}

/**
 * The RGB values of the indicator dots
 */
indicatorDotRGBs(obj?:RGBTable):RGBTable|null {
  const offset = this.bb!.__offset(this.bb_pos, 30);
  return offset ? (obj || new RGBTable()).__init(this.bb!.__indirect(this.bb_pos + offset), this.bb!) : null;
}

/**
 * The IDs of bodies that set indicator lines
 */
indicatorLineIDs(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 32);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

indicatorLineIDsLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 32);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

indicatorLineIDsArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 32);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The start location of the indicator lines
 */
indicatorLineStartLocs(obj?:VecTable):VecTable|null {
  const offset = this.bb!.__offset(this.bb_pos, 34);
  return offset ? (obj || new VecTable()).__init(this.bb!.__indirect(this.bb_pos + offset), this.bb!) : null;
}

/**
 * The end location of the indicator lines
 */
indicatorLineEndLocs(obj?:VecTable):VecTable|null {
  const offset = this.bb!.__offset(this.bb_pos, 36);
  return offset ? (obj || new VecTable()).__init(this.bb!.__indirect(this.bb_pos + offset), this.bb!) : null;
}

/**
 * The RGB values of the indicator lines
 */
indicatorLineRGBs(obj?:RGBTable):RGBTable|null {
  const offset = this.bb!.__offset(this.bb_pos, 38);
  return offset ? (obj || new RGBTable()).__init(this.bb!.__indirect(this.bb_pos + offset), this.bb!) : null;
}

/**
 * The first sent Round in a match should have index 1. (The starting state,
 * created by the MatchHeader, can be thought to have index 0.)
 * It should increase by one for each following round.
 */
roundID():number {
  const offset = this.bb!.__offset(this.bb_pos, 40);
  return offset ? this.bb!.readInt32(this.bb_pos + offset) : 0;
}

/**
 * The IDs of player bodies.
 */
bytecodeIDs(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 42);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

bytecodeIDsLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 42);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

bytecodeIDsArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 42);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The bytecodes used by the player bodies.
 */
bytecodesUsed(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 44);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

bytecodesUsedLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 44);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

bytecodesUsedArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 44);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

static startRound(builder:flatbuffers.Builder) {
  builder.startObject(21);
}

static addTeamIDs(builder:flatbuffers.Builder, teamIDsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(0, teamIDsOffset, 0);
}

static createTeamIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createTeamIDsVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createTeamIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startTeamIDsVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addTeamUraniumChanges(builder:flatbuffers.Builder, teamUraniumChangesOffset:flatbuffers.Offset) {
  builder.addFieldOffset(1, teamUraniumChangesOffset, 0);
}

static createTeamUraniumChangesVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createTeamUraniumChangesVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createTeamUraniumChangesVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startTeamUraniumChangesVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addMovedIDs(builder:flatbuffers.Builder, movedIDsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(2, movedIDsOffset, 0);
}

static createMovedIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createMovedIDsVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createMovedIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startMovedIDsVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addMovedLocs(builder:flatbuffers.Builder, movedLocsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(3, movedLocsOffset, 0);
}

static addSpawnedBodies(builder:flatbuffers.Builder, spawnedBodiesOffset:flatbuffers.Offset) {
  builder.addFieldOffset(4, spawnedBodiesOffset, 0);
}

static addDiedIDs(builder:flatbuffers.Builder, diedIDsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(5, diedIDsOffset, 0);
}

static createDiedIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createDiedIDsVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createDiedIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startDiedIDsVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addActionIDs(builder:flatbuffers.Builder, actionIDsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(6, actionIDsOffset, 0);
}

static createActionIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createActionIDsVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createActionIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startActionIDsVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addActions(builder:flatbuffers.Builder, actionsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(7, actionsOffset, 0);
}

static createActionsVector(builder:flatbuffers.Builder, data:Action[]):flatbuffers.Offset {
  builder.startVector(1, data.length, 1);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt8(data[i]!);
  }
  return builder.endVector();
}

static startActionsVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(1, numElems, 1);
}

static addActionTargets(builder:flatbuffers.Builder, actionTargetsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(8, actionTargetsOffset, 0);
}

static createActionTargetsVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createActionTargetsVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createActionTargetsVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startActionTargetsVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addIndicatorStringIDs(builder:flatbuffers.Builder, indicatorStringIDsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(9, indicatorStringIDsOffset, 0);
}

static createIndicatorStringIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createIndicatorStringIDsVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createIndicatorStringIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startIndicatorStringIDsVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addIndicatorStrings(builder:flatbuffers.Builder, indicatorStringsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(10, indicatorStringsOffset, 0);
}

static createIndicatorStringsVector(builder:flatbuffers.Builder, data:flatbuffers.Offset[]):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addOffset(data[i]!);
  }
  return builder.endVector();
}

static startIndicatorStringsVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addIndicatorDotIDs(builder:flatbuffers.Builder, indicatorDotIDsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(11, indicatorDotIDsOffset, 0);
}

static createIndicatorDotIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createIndicatorDotIDsVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createIndicatorDotIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startIndicatorDotIDsVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addIndicatorDotLocs(builder:flatbuffers.Builder, indicatorDotLocsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(12, indicatorDotLocsOffset, 0);
}

static addIndicatorDotRGBs(builder:flatbuffers.Builder, indicatorDotRGBsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(13, indicatorDotRGBsOffset, 0);
}

static addIndicatorLineIDs(builder:flatbuffers.Builder, indicatorLineIDsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(14, indicatorLineIDsOffset, 0);
}

static createIndicatorLineIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createIndicatorLineIDsVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createIndicatorLineIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startIndicatorLineIDsVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addIndicatorLineStartLocs(builder:flatbuffers.Builder, indicatorLineStartLocsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(15, indicatorLineStartLocsOffset, 0);
}

static addIndicatorLineEndLocs(builder:flatbuffers.Builder, indicatorLineEndLocsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(16, indicatorLineEndLocsOffset, 0);
}

static addIndicatorLineRGBs(builder:flatbuffers.Builder, indicatorLineRGBsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(17, indicatorLineRGBsOffset, 0);
}

static addRoundID(builder:flatbuffers.Builder, roundID:number) {
  builder.addFieldInt32(18, roundID, 0);
}

static addBytecodeIDs(builder:flatbuffers.Builder, bytecodeIDsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(19, bytecodeIDsOffset, 0);
}

static createBytecodeIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createBytecodeIDsVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createBytecodeIDsVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startBytecodeIDsVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addBytecodesUsed(builder:flatbuffers.Builder, bytecodesUsedOffset:flatbuffers.Offset) {
  builder.addFieldOffset(20, bytecodesUsedOffset, 0);
}

static createBytecodesUsedVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createBytecodesUsedVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createBytecodesUsedVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startBytecodesUsedVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static endRound(builder:flatbuffers.Builder):flatbuffers.Offset {
  const offset = builder.endObject();
  return offset;
}

}