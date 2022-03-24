// automatically generated by the FlatBuffers compiler, do not modify

import * as flatbuffers from 'flatbuffers';

import { SpawnedBodyTable } from '../../battlecode/schema/spawned-body-table';
import { Vec } from '../../battlecode/schema/vec';


/**
 * The map a round is played on.
 */
export class GameMap {
  bb: flatbuffers.ByteBuffer|null = null;
  bb_pos = 0;
__init(i:number, bb:flatbuffers.ByteBuffer):GameMap {
  this.bb_pos = i;
  this.bb = bb;
  return this;
}

static getRootAsGameMap(bb:flatbuffers.ByteBuffer, obj?:GameMap):GameMap {
  return (obj || new GameMap()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
}

static getSizePrefixedRootAsGameMap(bb:flatbuffers.ByteBuffer, obj?:GameMap):GameMap {
  bb.setPosition(bb.position() + flatbuffers.SIZE_PREFIX_LENGTH);
  return (obj || new GameMap()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
}

/**
 * The name of a map.
 */
name():string|null
name(optionalEncoding:flatbuffers.Encoding):string|Uint8Array|null
name(optionalEncoding?:any):string|Uint8Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 4);
  return offset ? this.bb!.__string(this.bb_pos + offset, optionalEncoding) : null;
}

/**
 * The bottom corner of the map.
 */
minCorner(obj?:Vec):Vec|null {
  const offset = this.bb!.__offset(this.bb_pos, 6);
  return offset ? (obj || new Vec()).__init(this.bb_pos + offset, this.bb!) : null;
}

/**
 * The top corner of the map.
 */
maxCorner(obj?:Vec):Vec|null {
  const offset = this.bb!.__offset(this.bb_pos, 8);
  return offset ? (obj || new Vec()).__init(this.bb_pos + offset, this.bb!) : null;
}

/**
 * The map symmetry: 0 for rotation, 1 for horizontal, 2 for vertical.
 */
symmetry():number {
  const offset = this.bb!.__offset(this.bb_pos, 10);
  return offset ? this.bb!.readInt32(this.bb_pos + offset) : 0;
}

/**
 * The bodies on the map.
 */
bodies(obj?:SpawnedBodyTable):SpawnedBodyTable|null {
  const offset = this.bb!.__offset(this.bb_pos, 12);
  return offset ? (obj || new SpawnedBodyTable()).__init(this.bb!.__indirect(this.bb_pos + offset), this.bb!) : null;
}

/**
 * The random seed of the map.
 */
randomSeed():number {
  const offset = this.bb!.__offset(this.bb_pos, 14);
  return offset ? this.bb!.readInt32(this.bb_pos + offset) : 0;
}

/**
 * The walls on the map.
 */
walls(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 16);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

wallsLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 16);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

wallsArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 16);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The uranium on the map.
 */
uranium(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 18);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

uraniumLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 18);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

uraniumArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 18);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

/**
 * The spawn locations.
 */
spawnLocation(index: number):number|null {
  const offset = this.bb!.__offset(this.bb_pos, 20);
  return offset ? this.bb!.readInt32(this.bb!.__vector(this.bb_pos + offset) + index * 4) : 0;
}

spawnLocationLength():number {
  const offset = this.bb!.__offset(this.bb_pos, 20);
  return offset ? this.bb!.__vector_len(this.bb_pos + offset) : 0;
}

spawnLocationArray():Int32Array|null {
  const offset = this.bb!.__offset(this.bb_pos, 20);
  return offset ? new Int32Array(this.bb!.bytes().buffer, this.bb!.bytes().byteOffset + this.bb!.__vector(this.bb_pos + offset), this.bb!.__vector_len(this.bb_pos + offset)) : null;
}

static startGameMap(builder:flatbuffers.Builder) {
  builder.startObject(9);
}

static addName(builder:flatbuffers.Builder, nameOffset:flatbuffers.Offset) {
  builder.addFieldOffset(0, nameOffset, 0);
}

static addMinCorner(builder:flatbuffers.Builder, minCornerOffset:flatbuffers.Offset) {
  builder.addFieldStruct(1, minCornerOffset, 0);
}

static addMaxCorner(builder:flatbuffers.Builder, maxCornerOffset:flatbuffers.Offset) {
  builder.addFieldStruct(2, maxCornerOffset, 0);
}

static addSymmetry(builder:flatbuffers.Builder, symmetry:number) {
  builder.addFieldInt32(3, symmetry, 0);
}

static addBodies(builder:flatbuffers.Builder, bodiesOffset:flatbuffers.Offset) {
  builder.addFieldOffset(4, bodiesOffset, 0);
}

static addRandomSeed(builder:flatbuffers.Builder, randomSeed:number) {
  builder.addFieldInt32(5, randomSeed, 0);
}

static addWalls(builder:flatbuffers.Builder, wallsOffset:flatbuffers.Offset) {
  builder.addFieldOffset(6, wallsOffset, 0);
}

static createWallsVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createWallsVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createWallsVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startWallsVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addUranium(builder:flatbuffers.Builder, uraniumOffset:flatbuffers.Offset) {
  builder.addFieldOffset(7, uraniumOffset, 0);
}

static createUraniumVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createUraniumVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createUraniumVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startUraniumVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static addSpawnLocation(builder:flatbuffers.Builder, spawnLocationOffset:flatbuffers.Offset) {
  builder.addFieldOffset(8, spawnLocationOffset, 0);
}

static createSpawnLocationVector(builder:flatbuffers.Builder, data:number[]|Int32Array):flatbuffers.Offset;
/**
 * @deprecated This Uint8Array overload will be removed in the future.
 */
static createSpawnLocationVector(builder:flatbuffers.Builder, data:number[]|Uint8Array):flatbuffers.Offset;
static createSpawnLocationVector(builder:flatbuffers.Builder, data:number[]|Int32Array|Uint8Array):flatbuffers.Offset {
  builder.startVector(4, data.length, 4);
  for (let i = data.length - 1; i >= 0; i--) {
    builder.addInt32(data[i]!);
  }
  return builder.endVector();
}

static startSpawnLocationVector(builder:flatbuffers.Builder, numElems:number) {
  builder.startVector(4, numElems, 4);
}

static endGameMap(builder:flatbuffers.Builder):flatbuffers.Offset {
  const offset = builder.endObject();
  return offset;
}

}
