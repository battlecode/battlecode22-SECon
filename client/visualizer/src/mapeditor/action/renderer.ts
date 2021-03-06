import * as config from '../../config';
import * as cst from '../../constants';

import {GameWorld, schema} from 'battlecode-playback';
import {AllImages} from '../../imageloader';

import {GameMap, MapUnit} from '../index';

/**
 * Renders the world.
 *
 * Note that all rendering functions draw in world-units,
 */
export default class MapRenderer {
  private conf: config.Config;

  readonly canvas: HTMLCanvasElement;
  readonly ctx: CanvasRenderingContext2D;
  readonly imgs: AllImages;

  // Callbacks for clicking robots and trees on the canvas
  readonly onclickUnit: (id: number) => void;
  readonly onclickBlank: (x, y) => void;
  readonly onMouseover: (x: number, y: number, walls: boolean, uranium: number) => void
  readonly onDrag: (x, y) => void

  // Other useful values
  readonly bgPattern: CanvasPattern;
  private width: number; // in world units
  private height: number; // in world units

  private map: GameMap; //the current map

  constructor(canvas: HTMLCanvasElement, imgs: AllImages, conf: config.Config,
    onclickUnit: (id: number) => void, onclickBlank: (x: number, y: number) => void,
    onMouseover: (x: number, y: number, walls: boolean, uranium: number) => void,
    onDrag: (x: number, y: number) => void) {
    this.canvas = canvas;
    this.conf = conf;
    this.imgs = imgs;
    this.onclickUnit = onclickUnit;
    this.onclickBlank = onclickBlank;
    this.onMouseover = onMouseover;
    this.onDrag = onDrag;

    let ctx = canvas.getContext("2d");
    if (ctx === null) {
      throw new Error("Couldn't load canvas2d context");
    } else {
      this.ctx = ctx;
    }

    //this.bgPattern = <CanvasPattern>this.ctx.createPattern(imgs.tiles[0], 'repeat');
    this.setEventListeners();
  }

  /**
   * Renders the game map.
   */
  render(map: GameMap): void {
    const scale = this.canvas.width / map.width;
    this.width = map.width;
    this.height = map.height;
    this.map = map;

    // setup correct rendering
    this.ctx.restore();
    this.ctx.save();
    this.ctx.scale(scale, scale);

    this.renderBackground(map);
    this.renderBodies(map);
    this.renderResources(map);
    console.log(this.canvas)
    // restore default rendering
  }

  /**
   * Returns the mirrored y coordinate to be consistent with (0, 0) in the
   * bottom-left corner (top-left corner is canvas default).
   * params: y coordinate to flip
   *         height coordinate of the maximum edge
   */
  private flip(y: number, height: number) {
    return height - y;
  }

  /**
   * Draw the background
   */
  private renderBackground(map: GameMap): void {
    for(let i = 0; i < this.width; i++){
      for(let j = 0; j < this.height; j++){
        const walls = map.walls[(map.height-j-1)*this.width + i];
        this.renderTile(i, j, walls);
      }
    }
  }

  private renderTile(i: number, j: number, walls: boolean) {
    this.ctx.save();
    this.ctx.globalAlpha = 1;
    const scale = 20;
    this.ctx.scale(1/scale, 1/scale);
    this.ctx.fillStyle = walls ? "black" : "white";
    this.ctx.fillRect(i * scale, j * scale, scale + 1, scale + 1)
    this.ctx.strokeStyle = 'gray';
    this.ctx.strokeRect(i*scale, j*scale, scale, scale);
    this.ctx.restore();
  }

  private renderResources(map: GameMap) {
    this.ctx.save()
    this.ctx.globalAlpha = 1

    const uraniumImg = this.imgs.resources.uranium

    const scale = 1

    const sigmoid = (x) => {
      return 1 / (1 + Math.exp(-x))
    }

    for (let i = 0; i < this. width; i++) for (let j = 0; j < this.height; j++) {
      const uranium = map.uraniumVals[(map.height-j-1)*this.width + i];

      this.ctx.globalAlpha = 1
      const cx = i*scale, cy = j*scale

      if (uranium > 0) {
        let size = sigmoid(uranium / 50)
        this.ctx.drawImage(uraniumImg, cx + (1 - size) / 2, cy + (1 - size) / 2, scale * size, scale * size)

        this.ctx.strokeStyle = '#59727d'
        this.ctx.lineWidth = 1 / 30
        this.ctx.strokeRect(cx + .05, cy + .05, scale * .9, scale * .9)
      }
    }

    this.ctx.restore()
  }

  /**
   * Draw trees and units on the canvas
   */
  private renderBodies(map: GameMap) {

    this.ctx.fillStyle = "#84bf4b";
    map.originalBodies.forEach((body: MapUnit) => {
      this.renderBody(body);
      // this.drawGoodies(x, y, radius, body.containedBullets, body.containedBody);
    });

    map.symmetricBodies.forEach((body: MapUnit) => {
      this.renderBody(body);
      // this.drawGoodies(x, y, radius, body.containedBullets, body.containedBody);
    });
  }

  private renderBody(body: MapUnit) {
    const x = body.x;
    const y = this.flip(body.y, this.map.height);
    const radius = body.radius;
    let img: HTMLImageElement;

    const teamID = body.teamID || 0;
    img = this.imgs.robots[cst.bodyTypeToString(body.type)][teamID];
    this.drawImage(img, x, y, radius);
  }

  /**
   * Sets the map editor display to contain of the information of the selected
   * tree, or on the selected coordinate if there is no tree.
   */
  private setEventListeners() {

    let hoverPos: {x: number, y: number} | null = null;

    const whilemousedown = () => {
      if (hoverPos !== null) {
        const {x,y} = hoverPos;
        this.onDrag(x, y);
      }
    };

    var interval: number;
    this.canvas.onmousedown = (event: MouseEvent) => {
      const {x,y} = this.getIntegerLocation(event, this.map);
      // Get the ID of the selected unit
      let selectedID;
      this.map.originalBodies.forEach(function(body: MapUnit, id: number) {
        if (x == body.x && y == body.y) {
          selectedID = id;
        }
      });
      this.map.symmetricBodies.forEach(function(body: MapUnit, id: number) {
        if (x == body.x && y == body.y) {
          selectedID = id;
        }
      });

      if (selectedID) {
        this.onclickUnit(selectedID);
      } else {
        this.onclickBlank(x, y);
      }

      interval = window.setInterval(whilemousedown, 50);
    };

    this.canvas.onmouseup = () => {
      clearInterval(interval);
    };

    this.canvas.onmousemove = (event) => {
      const {x,y} = this.getIntegerLocation(event, this.map);
      this.onMouseover(x, y, this.map.walls[(y)*this.width + x], this.map.uraniumVals[y*this.width + x]);
      hoverPos = {x: x, y: y};
    };

    this.canvas.onmouseout = (event) => {
      hoverPos = null;
      clearInterval(interval);
    };
  }

  private getIntegerLocation(event: MouseEvent, map: GameMap) {
    let x = map.width * event.offsetX / this.canvas.offsetWidth;
    let y = this.flip(map.height * event.offsetY / this.canvas.offsetHeight, map.height);
    return {x: Math.floor(x), y: Math.floor(y)};
  }

  /**
   * Draws an image centered at (x, y) with the given radius
   */
  private drawImage(img: HTMLImageElement, x: number, y: number, radius: number) {
    this.ctx['imageSmoothingEnabled'] = false;
    this.ctx.drawImage(img, x, y-radius*2, radius*2, radius*2);
  }
}
