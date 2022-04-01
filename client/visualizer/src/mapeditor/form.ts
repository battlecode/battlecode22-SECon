import {Config} from '../config';
import * as cst from '../constants';
import {AllImages} from '../imageloader';
import {cow_border as cow} from '../cow';

import {schema, flatbuffers} from 'battlecode-playback';

import {MapRenderer, HeaderForm, SymmetryForm, RobotForm, TileForm, UraniumForm, UploadedMap} from './index';
import { throws } from 'assert';

export type MapUnit = {
  x: number,
  y: number,
  type: schema.BodyType,
  radius: 0.5,
  teamID?: number
  // influence: number
};

export type GameMap = {
  name: string,
  width: number,
  height: number,
  originalBodies: Map<number, MapUnit>
  symmetricBodies: Map<number, MapUnit>,
  walls: boolean[],
  uraniumVals: number[],
  symmetry: number,
};

/**
 * Reads and interprets information from the map editor input form
 */
export default class MapEditorForm {

  // The public div
  readonly div: HTMLDivElement;

  // HTML elements
  private readonly images: AllImages;
  private readonly canvas: HTMLCanvasElement;
  private readonly renderer: MapRenderer;

  // Forms and text display
  private readonly headerForm: HeaderForm;
  private readonly symmetryForm: SymmetryForm;
  private readonly robotsForm: RobotForm;
  private readonly tilesForm: TileForm;
  private readonly uraniumForm: UraniumForm;

  private robotsRadio: HTMLInputElement;
  private tilesRadio: HTMLInputElement;
  private uraniumRadio: HTMLInputElement;

  private forms: HTMLDivElement;

  readonly buttonAdd: HTMLButtonElement;
  readonly buttonDelete: HTMLButtonElement;
  readonly buttonReverse: HTMLButtonElement;
  readonly buttonRandomize: HTMLButtonElement;
  readonly buttonInvert: HTMLButtonElement;

  readonly tileInfo: HTMLDivElement;

  // Options
  private readonly conf: Config

  // Map information
  private lastID: number; // To give bodies unique IDs
  private originalBodies: Map<number, MapUnit>;
  private symmetricBodies: Map<number, MapUnit>;
  private walls: boolean[];
  private uraniumVals: number[];

  constructor(conf: Config, imgs: AllImages, canvas: HTMLCanvasElement) {
    // Store the parameters
    this.conf = conf;
    this.images = imgs;
    this.canvas = canvas;

    // fields for placing bodies
    this.lastID = 1;
    this.originalBodies = new Map<number, MapUnit>();
    this.symmetricBodies = new Map<number, MapUnit>();


    // Load HTML elements
    this.div = document.createElement("div");

    // callback functions for getting constants
    const cbWidth = () => {return this.headerForm.getWidth()};
    const cbHeight = () => {return this.headerForm.getHeight()};

    // header (name, width, height)
    this.headerForm = new HeaderForm(() => {
      this.reset();
      this.render();
    });
    this.div.appendChild(this.headerForm.div);

    // symmetry
    this.symmetryForm = new SymmetryForm(() => {this.initWalls(); this.initUranium(); this.render()});
    this.div.appendChild(document.createElement("br"));
    this.div.appendChild(this.symmetryForm.div);
    this.div.appendChild(document.createElement("br"));
    this.div.appendChild(document.createElement("hr"));

    // radio buttons
    this.tilesRadio = document.createElement("input");
    this.robotsRadio = document.createElement("input");
    this.uraniumRadio = document.createElement("input");
    this.div.appendChild(this.createUnitOption());
    this.div.appendChild(document.createElement("br"));

    // robot delete + add/update buttons
    this.forms = document.createElement("div");
    this.robotsForm = new RobotForm(cbWidth, cbHeight); // robot info (type, x, y, ...)
    this.tilesForm = new TileForm(cbWidth, cbHeight);
    this.uraniumForm = new UraniumForm(cbWidth, cbHeight);
    this.buttonDelete = document.createElement("button");
    this.buttonAdd = document.createElement("button");
    this.buttonReverse = document.createElement("button");
    this.buttonRandomize = document.createElement("button");
    this.buttonInvert = document.createElement("button");
    this.div.appendChild(this.forms);

    this.buttonDelete.style.display = "none";
    this.buttonAdd.style.display = "none";
    this.buttonReverse.style.display = "none";
    this.buttonRandomize.style.display = "none";
    this.buttonInvert.style.display = "none";

    // TODO add vertical filler to put form buttons at the bottom
    // validate, remove, reset buttons
    this.div.appendChild(this.createFormButtons());
    this.div.appendChild(document.createElement('hr'));

    this.tileInfo = document.createElement("div");
    this.tileInfo.textContent = "X: | Y: | Walls: | Uranium:";
    this.div.appendChild(this.tileInfo);
    this.div.appendChild(document.createElement('hr'));

    // Renderer settings
    const onclickUnit = (id: number) => {
      if (this.originalBodies.has(id) && this.getActiveForm() == this.robotsForm) {
        // Set the corresponding form appropriately
        let body: MapUnit = this.originalBodies.get(id)!;
        this.robotsRadio.click();
        this.robotsForm.setForm(body.x, body.y, body, id);
      }
    };

    const onclickBlank = (x, y) => {
      this.getActiveForm().setForm(x, y);
    };

    const onMouseover = (x: number, y: number, walls: boolean, uranium: number) => {
      let content: string = "";
      content += 'X: ' + `${x}`.padStart(3);
      content += ' | Y: ' + `${y}`.padStart(3);
      content += ' | Wall: ' + `${walls ? "yes" : "no"}`;
      content += ' | Uranium: ' + `${uranium.toFixed(3)}`;
      this.tileInfo.textContent = content;
    };

    const onDrag = (x, y) => {
      if (this.getActiveForm() === this.tilesForm && this.tilesForm.isValid()) {
        let r: number = this.tilesForm.getBrush();
        let inBrush: (dx, dy) => boolean = () => true;
        switch (this.tilesForm.getStyle()) {
          case "Circle":
            inBrush = (dx, dy) => dx*dx + dy*dy < r*r;
            break;
          case "Square":
            inBrush = (dx, dy) => Math.max(Math.abs(dx), Math.abs(dy)) < r;
            break;
          case "Cow":
            inBrush = (dx,dy) => (Math.abs(dx) < r && Math.abs(dy) < r && cow[Math.floor(20*(1+dx/r))][Math.floor(20*(1-dy/r))]);
        }
        this.setAreaWalls(x, y, this.tilesForm.getWalls() == 1, inBrush);
        this.render();
      } 
    }

    this.renderer = new MapRenderer(canvas, imgs, conf, onclickUnit, onclickBlank, onMouseover, onDrag);

    this.initWalls();
    this.initUranium();

    // Load callbacks and finally render
    this.loadCallbacks();
    this.render();
  }

  private createUnitOption(): HTMLFormElement {
    const div = document.createElement("form");

    // Radio button for changing tile passabilities
    this.tilesRadio.id = "tiles-radio";
    this.tilesRadio.type = "radio";
    this.tilesRadio.name = "edit-option"; // radio buttons with same name are mutually exclusive

    this.tilesRadio.onchange = () => {
      // Change the displayed form
      if (this.tilesRadio.checked) {
        while (this.forms.firstChild) this.forms.removeChild(this.forms.firstChild);
        this.forms.appendChild(this.tilesForm.div);
        this.buttonDelete.style.display = "none";
        this.buttonAdd.style.display = "none";
        this.buttonReverse.style.display = "none";
        this.buttonRandomize.style.display = "";
        this.buttonInvert.style.display = "";
      }
    };
    const tilesLabel = document.createElement("label");
    tilesLabel.setAttribute("for", this.tilesRadio.id);
    tilesLabel.textContent = "Walls";

    // Radio button for placing units
    this.robotsRadio.id = "robots-radio";
    this.robotsRadio.type = "radio";
    this.robotsRadio.name = "edit-option";

    this.robotsRadio.onchange = () => {
      // Change the displayed form
      if (this.robotsRadio.checked) {
        while (this.forms.firstChild) this.forms.removeChild(this.forms.firstChild);
        this.forms.appendChild(this.robotsForm.div);
        this.buttonDelete.style.display = "";
        this.buttonAdd.style.display = "";
        this.buttonReverse.style.display = "";
        this.buttonRandomize.style.display = "none";
        this.buttonInvert.style.display = "none";
      }
    };
    const robotsLabel = document.createElement("label");
    robotsLabel.setAttribute("for", this.robotsRadio.id);
    robotsLabel.textContent = "Robots";

    // Radio button for placing uranium
    this.uraniumRadio.id = "uranium-radio";
    this.uraniumRadio.type = "radio";
    this.uraniumRadio.name = "edit-option";

    this.uraniumRadio.onchange = () => {
      // Change the displayed form
      if (this.uraniumRadio.checked) {
        while (this.forms.firstChild) this.forms.removeChild(this.forms.firstChild);
        this.forms.appendChild(this.uraniumForm.div);
        this.buttonDelete.style.display = "";
        this.buttonAdd.style.display = "";
        this.buttonReverse.style.display = "none";
        this.buttonRandomize.style.display = "none";
        this.buttonInvert.style.display = "none";
      }
    };

    const uraniumLabel = document.createElement("label");
    uraniumLabel.setAttribute("for", this.uraniumRadio.id);
    uraniumLabel.textContent = "Uranium";

    // Add radio buttons HTML element
    div.appendChild(this.tilesRadio);
    div.appendChild(tilesLabel);
    div.appendChild(this.robotsRadio);
    div.appendChild(robotsLabel);
    div.appendChild(this.uraniumRadio);
    div.appendChild(uraniumLabel);
    div.appendChild(document.createElement("br"));

    return div;
  }

  private createFormButtons(): HTMLDivElement {
    // HTML structure
    const buttons = document.createElement("div");
    buttons.appendChild(this.buttonDelete);
    buttons.appendChild(this.buttonAdd);
    buttons.appendChild(this.buttonReverse);
    buttons.appendChild(this.buttonRandomize);
    buttons.appendChild(this.buttonInvert);

    // Delete and Add/Update buttons
    this.buttonDelete.type = "button";
    this.buttonDelete.className = "form-button custom-button";
    this.buttonDelete.appendChild(document.createTextNode("Delete"));
    this.buttonAdd.type = "button";
    this.buttonAdd.className = "form-button custom-button";
    this.buttonAdd.appendChild(document.createTextNode("Add/Update"));
    this.buttonReverse.type = "button";
    this.buttonReverse.className = "form-button custom-button";
    this.buttonReverse.appendChild(document.createTextNode("Switch Team"));
    this.buttonRandomize.type = "button";
    this.buttonRandomize.className = "form-button custom-button";
    this.buttonRandomize.appendChild(document.createTextNode("Randomize Tiles"));
    this.buttonInvert.type = "button";
    this.buttonInvert.className = "form-button custom-button";
    this.buttonInvert.appendChild(document.createTextNode("Invert values"));

    return buttons;
  }

  private loadCallbacks() {
    this.buttonAdd.onclick = () => {
      if (this.getActiveForm() == this.robotsForm) {
        const form: RobotForm = this.robotsForm;
        const id: number = form.getID() || this.lastID;
        const unit: MapUnit | undefined = form.getUnit(id);
        if (unit) {
          // Create a new unit or update an existing unit
          this.setUnit(id, unit);
          form.resetForm();
        }
      } else if (this.getActiveForm() == this.uraniumForm) {
        const form = this.uraniumForm;
        const x = form.getX();
        const y = form.getY();
        const uranium = form.getUranium();
        this.setUranium(x, y, uranium);
      }
    }

    this.buttonDelete.onclick = () => {
      if (this.getActiveForm() == this.robotsForm) {
        const id: number | undefined = this.robotsForm.getID();
        if (id && !isNaN(id)) {
          this.deleteUnit(id);
          this.getActiveForm().resetForm();
        }
      } else if (this.getActiveForm() == this.uraniumForm) {
        const form = this.uraniumForm;
        const x = form.getX();
        const y = form.getY();
        this.setUranium(x, y, 0);
      }
    }

    this.buttonReverse.onclick = () => {
      if (this.getActiveForm() == this.robotsForm) {
        const form: RobotForm = this.robotsForm;
        const id: number = form.getID() || this.lastID - 1;
        const unit: MapUnit = this.originalBodies.get(id)!;
        if (unit) {
          var teamID: number = unit.teamID === undefined? 0 : unit.teamID;
          if(teamID > 0) {
            teamID = 3 - teamID;
          }
          unit.teamID = teamID;
          // Create a new unit or update an existing unit
          this.setUnit(id, unit);
          form.resetForm();
        }
      }
    }

    this.buttonRandomize.onclick = () => {
      if (this.getActiveForm() == this.tilesForm) {
        for(let x: number = 0; x < this.headerForm.getWidth(); x++) {
          for(let y:number = 0; y < this.headerForm.getHeight(); y++) {
            this.setWalls(x, y, Math.random() > .5);
          }
        }
        this.render();
      }
    }

    this.buttonInvert.onclick = () => {
      if (this.getActiveForm() == this.tilesForm) {
        for(let x: number = 0; x < this.headerForm.getWidth(); x++) {
          for(let y: number = 0; y < this.headerForm.getHeight(); y++) {
            this.walls[y*this.headerForm.getWidth() + x] = !this.getWalls(x,y);
          }
        }
        this.render();
      }
    }

    // this.buttonSmoothen.onclick = () => {
    //   if (this.getActiveForm() == this.tiles) {
    //     for(let x: number = 0; x < this.header.getWidth(); x++) {
    //       for(let y: number = 0; y < this.header.getHeight(); y++) {
    //         //let sum = 0, n = 0;
    //         let high = this.getWalls(x, y);
    //         let low = this.getWalls(x, y);
    //         for (let x2 = Math.max(0,x-1); x2 <= Math.min(x+1, this.header.getWidth()-1); x2++) {
    //           for (let y2 = Math.max(0,y-1); y2 <= Math.min(y+1, this.header.getWidth()-1); y2++) {
    //            // if (Math.abs(x-x2) + Math.abs(y-y2) > 1) continue; // bad code
    //            // sum += this.getWalls(x2, y2);
    //             //n++;
    //             high = Math.max(this.getWalls(x2, y2), high);
    //             low = Math.min(this.getWalls(x2, y2), high);
    //           }
    //         } 
    //         this.setWalls(x,y, (high+low)/2);
    //       }
    //     }
    //     this.render();
    //   }
    // }
  }

  /**
   * Given an x, y on the map, returns the maximum radius such that the
   * corresponding unit centered on x, y is cst.DELTA away from any other existing
   * unit. Returns 0 if no such radius exists.
   *
   * If an id is given, does not consider the body with the corresponding id to
   * overlap with the given coordinates.
   */
  // private maxRadius(x: number, y: number, ignoreID?: number): number {
  //   // Min distance to wall
  //   let maxRadius = Math.min(x, y, this.header.getWidth()-x, this.header.getHeight()-y);

  //   // Min distance to tree or body
  //   ignoreID = ignoreID || -1;
  //   this.originalBodies.forEach((body: MapUnit, id: number) => {
  //     if (id != ignoreID) {
  //       maxRadius = Math.min(maxRadius, loc.distance(body.loc) - body.radius);
  //     }
  //   });
  //   this.symmetricBodies.forEach((body: MapUnit, id: number) => {
  //     if (id != ignoreID) {
  //       maxRadius = Math.min(maxRadius, loc.distance(body.loc) - body.radius);
  //     }
  //   });

  //   return Math.max(0, maxRadius - cst.DELTA);
  // }

  /**
   * If a unit with the given ID already exists, updates the existing unit.
   * Otherwise, adds the unit to the internal units and increments lastID.
   * Finally re-renders the canvas.
   */
  private setUnit(id: number, body: MapUnit): void {
    if (!this.originalBodies.has(id)) {
      this.lastID += 1;
    }
    this.originalBodies.set(id, body);
    this.render();
  }

  /**
   * Deletes the tree/archon with the given ID if it exists and re-renders
   * the canvas. Otherwise does nothing.
   */
  private deleteUnit(id: number): void {
    if (this.originalBodies.has(id)) {
      this.originalBodies.delete(id);
      this.render();
    }
  }

  /**
   * Initialize walls array based on map dimensions.
   */
  private initWalls() {
    this.walls = new Array(this.headerForm.getHeight() * this.headerForm.getWidth());
    this.walls.fill(false);
  }

  private getWalls(x: number, y: number) {
    return this.walls[y*this.headerForm.getWidth() + x];
  }

  private setWalls(x: number, y: number, walls: boolean) {
    const {x: translated_x, y: translated_y} = this.symmetryForm.transformLoc(x, y, this.headerForm.getWidth(), this.headerForm.getHeight());
    this.walls[y*this.headerForm.getWidth() + x] = this.walls[translated_y*this.headerForm.getWidth() + translated_x] = walls;
  }

  private setAreaWalls(x0: number, y0: number, pass: boolean, inBrush: (dx, dy) => boolean) {
    const width = this.headerForm.getWidth();
    const height = this.headerForm.getHeight();

   for (let x = 0; x < width; x++) {
     for (let y = 0; y < height; y++) {
       if (inBrush(x-x0, y-y0)) {
          this.setWalls(x, y, pass);
       }
     }
   }
  }

  /**
 * Initialize uranium based on map dimensions.
 */
  private initUranium() {
    this.uraniumVals = new Array(this.headerForm.getHeight() * this.headerForm.getWidth());
    this.uraniumVals.fill(0);
  }

  private setUranium(x: number, y: number, uranium: number) {
    const {x: translated_x, y: translated_y} = this.symmetryForm.transformLoc(x, y, this.headerForm.getWidth(), this.headerForm.getHeight());
    this.uraniumVals[y*this.headerForm.getWidth() + x] = this.uraniumVals[translated_y*this.headerForm.getWidth() + translated_x] = uranium;
    this.render();
  }

  /**
   * @return the active form based on which radio button is selected
   */
  private getActiveForm(): RobotForm | TileForm | UraniumForm {
    return (this.tilesRadio.checked ? this.tilesForm : (this.robotsRadio.checked ? this.robotsForm : this.uraniumForm))
  }

  /**
   * Re-renders the canvas based on the parameters of the map editor.
   */
  render() {
    const width: number = this.headerForm.getWidth();
    const height: number = this.headerForm.getHeight();
    const scale: number = this.conf.upscale / Math.sqrt(width * height); // arbitrary scaling factor
    this.canvas.width = width * scale;
    this.canvas.height = height * scale;
    this.symmetricBodies = this.symmetryForm.getSymmetricBodies(this.originalBodies, width, height);
    this.renderer.render(this.getMap());
  }

  /**
   * Returns a map with the given name, width, height, and bodies.
   */
  getMap(): GameMap {
    return {
      name: this.headerForm.getName(),
      width: this.headerForm.getWidth(),
      height: this.headerForm.getHeight(),
      originalBodies: this.originalBodies,
      symmetricBodies: this.symmetricBodies,
      walls: this.walls,
      uraniumVals: this.uraniumVals,
      symmetry: this.symmetryForm.getSymmetry(),
    };
  }

  // getMapJSON(): string {
  //   // from https://stackoverflow.com/questions/29085197/how-do-you-json-stringify-an-es6-map/56150320
  //   const map = this.getMap();
  //   function replacer(key, value) {
  //     const originalObject = this[key];
  //     if(originalObject instanceof Map) {
  //       return {
  //         dataType: 'Map',
  //         value: Array.from(originalObject.entries()), // or with spread: value: [...originalObject]
  //       };
  //     } else {
  //       return value;
  //     }
  //   }
  //   return JSON.stringify(map, replacer);
  // }

  // setMap(mapJSON) {
  //   // from https://stackoverflow.com/questions/29085197/how-do-you-json-stringify-an-es6-map/56150320
  //   function reviver(key, value) {
  //     if(typeof value === 'object' && value !== null) {
  //       if (value.dataType === 'Map') {
  //         return new Map(value.value);
  //       }
  //     }
  //     return value;
  //   }
  //   const map = JSON.parse(mapJSON, reviver);
  //   this.header.setName(map.name);
  //   this.header.setWidth(map.width);
  //   this.header.setHeight(map.height);

  //   this.originalBodies = map.originalBodies;
  //   this.symmetricBodies = map.symmetricBodies;
  //   this.symmetry.setSymmetry(map.symmetry);
  //   this.walls = map.walls;
  //   this.render();
  // }

  // TODO: types
  setUploadedMap(map: UploadedMap) {

    const symmetryAndBodies = this.symmetryForm.discoverSymmetryAndBodies(map.bodies, map.walls, map.width, map.height);
    console.log(symmetryAndBodies);
    if (symmetryAndBodies === null) return;

    this.reset();
    this.headerForm.setName(map.name);
    this.headerForm.setWidth(map.width);
    this.headerForm.setHeight(map.height);
    this.symmetryForm.setSymmetry(symmetryAndBodies.symmetry);
    this.originalBodies = symmetryAndBodies.originalBodies;
    this.lastID = this.originalBodies.size + 1;
    this.symmetricBodies = this.symmetryForm.getSymmetricBodies(this.originalBodies, map.width, map.height);

    this.walls = map.walls;
    this.uraniumVals = map.uranium;

    this.render();
  }

  reset(): void {
    this.lastID = 1;
    this.originalBodies = new Map<number, MapUnit>();
    this.symmetricBodies = new Map<number, MapUnit>();
    this.initWalls();
    this.initUranium();
    this.render();
  }
}
