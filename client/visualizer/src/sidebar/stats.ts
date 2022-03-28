import { Config } from '../config';
import * as cst from '../constants';
import { AllImages } from '../imageloader';
import { schema } from 'battlecode-playback';
import Runner from '../runner';
import Chart = require('chart.js');

const hex: Object = {
  1: "#db3627",
  2: "#4f7ee6"
};

type ArchonBar = {
  bar: HTMLDivElement,
  archon: HTMLSpanElement,
  //bid: HTMLSpanElement
};

type BuffDisplay = {
  numBuffs: HTMLSpanElement,
  buff: HTMLSpanElement
}

type IncomeDisplay = {
  uraniumIncome: HTMLSpanElement
}

/**
* Loads game stats: team name, votes, robot count
* We make the distinction between:
*    1) Team names - a global string identifier i.e. "Teh Devs"
*    2) Team IDs - each Battlecode team has a unique numeric team ID i.e. 0
*    3) In-game ID - used to distinguish teams in the current match only;
*       team 1 is red, team 2 is blue
*/
export default class Stats {

  readonly div: HTMLDivElement;
  private readonly images: AllImages;

  private readonly tourIndexJump: HTMLInputElement;

  private teamNameNodes: HTMLSpanElement[] = [];

  // Key is the team ID
  private robotImages: Map<string, Array<HTMLImageElement>> = new Map(); // the robot image elements in the unit statistics display 
  private robotTds: Map<number, Map<string, Map<number, HTMLTableCellElement>>> = new Map();

  private archonBars: ArchonBar[];
  //private maxVotes: number;

  private incomeDisplays: IncomeDisplay[];

  private relativeBars: HTMLDivElement[];
  
  private buffDisplays: BuffDisplay[];
  
  private extraInfo: HTMLDivElement;
  
  private robotConsole: HTMLDivElement;
  
  private runner: Runner; //needed for file uploading in tournament mode
  
  private conf: Config;

  private tourneyUpload: HTMLDivElement;


  private incomeChartUranium: Chart;

  private bytecodeChart: Chart;

  private redHpHisto: Chart;
  private blueHpHisto: Chart;
  private num_lumps = 20;
  //private incomeChartLead: Chart;
  //private incomeChartGold: Chart;

  private ECs: HTMLDivElement;
  
  private teamMapToTurnsIncomeSet: Map<number, Set<number>>;

  // Note: robot types and number of teams are currently fixed regardless of
  // match info. Keep in mind if we ever change these, or implement this less
  // statically.

  readonly robots: schema.BodyType[] = cst.bodyTypeList;

  constructor(conf: Config, images: AllImages, runner: Runner) {
    this.conf = conf;
    this.images = images;

    for (const robot in this.images.robots) {
      let robotImages: Array<HTMLImageElement> = this.images.robots[robot];
      this.robotImages[robot] = robotImages.map((image) => image.cloneNode() as HTMLImageElement);
    }
    
    this.div = document.createElement("div");
    this.tourIndexJump = document.createElement("input");
    this.runner = runner;

    let teamNames: Array<string> = ["?????", "?????"];
    let teamIDs: Array<number> = [1, 2];
    this.initializeGame(teamNames, teamIDs);
  }

  /**
   * Colored banner labeled with the given teamName
   */
  private teamHeaderNode(teamName: string, inGameID: number) {
    let teamHeader: HTMLDivElement = document.createElement("div");
    teamHeader.className += ' teamHeader';

    let teamNameNode = document.createElement('span');
    teamNameNode.innerHTML = teamName;
    teamHeader.style.backgroundColor = hex[inGameID];
    teamHeader.appendChild(teamNameNode);
    this.teamNameNodes[inGameID] = teamNameNode;
    return teamHeader;
  }

  /**
   * Create the table that displays the robot images along with their counts.
   * Uses the teamID to decide which color image to display.
   */
  private robotTable(teamID: number, inGameID: number): HTMLTableElement {
    let table: HTMLTableElement = document.createElement("table");
    table.setAttribute("align", "center");

    // Create the table row with the robot images
    let robotImages: HTMLTableRowElement = document.createElement("tr");
    robotImages.appendChild(document.createElement("td")); // blank header

    // Create the table row with the robot counts
    let robotCounts = {};

    for (let value in this.robotTds[teamID]) {
      robotCounts[value] = document.createElement("tr");
      const title = document.createElement("td");
      if (value === "count") title.innerHTML = "<b>Count</b>";
      if (value === "hp") title.innerHTML = "<b>Σ(HP)</b>";
      robotCounts[value].appendChild(title);
    }

    for (let robot of this.robots) {
      let robotName: string = cst.bodyTypeToString(robot);
      let tdRobot: HTMLTableCellElement = document.createElement("td");
      tdRobot.className = "robotSpriteStats";
      tdRobot.style.height = "45px";
      tdRobot.style.width = "60px";

      const img: HTMLImageElement = this.robotImages[robotName][inGameID];
      img.style.width = "100%";
      img.style.height = "100%";
      // TODO: images

      tdRobot.appendChild(img);
      robotImages.appendChild(tdRobot);

      for (let value in this.robotTds[teamID]) {
        let tdCount: HTMLTableCellElement = this.robotTds[teamID][value][robot];
        robotCounts[value].appendChild(tdCount);
        // TODO: figure out what's going on here
        // if (robot === schema.BodyType.ENLIGHTENMENT_CENTER && value === "count") {
        //   tdCount.style.fontWeight = "bold";
        //   tdCount.style.fontSize = "18px";          
        // }
      }
    }
    table.appendChild(robotImages);
    for (let value in this.robotTds[teamID]) {
      table.appendChild(robotCounts[value]);
    }

    return table;
  }

  private initRelativeBars(teamIDs: Array<number>) {
    //let metalIDs = [0, 1];
    let metalIDs = [0];
    let colors = ["#AA9700", "#696969"];
    const relativeBars: HTMLDivElement[] = [];
    teamIDs.forEach((teamID: number) => metalIDs.forEach((id: number) => {
      const bar = document.createElement("div");
      //bar.setAttribute("align", ((teamID === teamIDs[0]) ? alignment[0] : alignment[1]));
      bar.style.backgroundColor = colors[id];
      bar.style.border = "5px solid " + ((teamID === teamIDs[0]) ? "#C00040" : "#4000C0");
      bar.style.width = `90%`;
      bar.className = "influence-bar";
      bar.innerText = "0%";
      bar.id = teamID.toString();
      relativeBars[2*id + ((teamIDs[0] === teamID)?0:1)] = bar;
    }));
    return relativeBars;
  }

  private getRelativeBarsElement(){
    //let metalIDs = [0, 1];
    let metalIDs = [0];
    const divleft = document.createElement("div");
    divleft.setAttribute("align", "center");
    divleft.id = "relative-bars-left";

    const labelleft = document.createElement('div');
    labelleft.className = "stats-header";
    labelleft.innerText = 'Total Red Uranium';

    const frameleft = document.createElement("div");
    frameleft.style.width = "100%";

    frameleft.appendChild(this.relativeBars[0]);
    //frameleft.appendChild(this.relativeBars[1]);

    divleft.appendChild(labelleft);
    divleft.appendChild(frameleft);
    
    const divright = document.createElement("div");
    divright.setAttribute("align", "center");
    divright.id = "relative-bars-right";

    const labelright = document.createElement('div');
    labelright.className = "stats-header";
    labelright.innerText = 'Total Blue Uranium';

    const frameright = document.createElement("div");
    frameright.style.width = "100%";

    frameright.appendChild(this.relativeBars[1]);
    //frameright.appendChild(this.relativeBars[3]);

    divright.appendChild(labelright);
    divright.appendChild(frameright);
    
    //return [divleft, divright];
    return [divleft, divright];
  }

  private updateRelBars(teamUranium: Array<number>){
    for(var a = 0; a < teamUranium.length; a++){
      this.relativeBars[a].innerHTML = teamUranium[a].toString();
      this.relativeBars[a].style.width = (Math.max(teamUranium[0], teamUranium[1]) === 0 ? 90:(90.0*teamUranium[a]/Math.max(teamUranium[0], teamUranium[1]))).toString() + "%";
    }
  }

  private initIncomeDisplays(teamIDs: Array<number>) {
    const incomeDisplays: IncomeDisplay[] = [];
    teamIDs.forEach((id: number) => {
      const uraniumIncome = document.createElement("span");
      uraniumIncome.style.color = hex[id];
      uraniumIncome.style.fontWeight = "bold";
      uraniumIncome.textContent = "U: 0";
      uraniumIncome.style.padding = "10px";
      incomeDisplays[id] = {uraniumIncome: uraniumIncome};
    });
    return incomeDisplays;
  }


  private getByteDisplaysElement(teamIDs: Array<number>): HTMLElement {
    const table = document.createElement("table");
    table.id = "byte-table";
    table.style.width = "100%";

    const title = document.createElement('td');
    title.colSpan = 4;
    const label = document.createElement('div');
    label.className = "stats-header";
    label.innerText = 'Bytecodes used Per Turn';

    const row = document.createElement("tr");

    title.appendChild(label);
    table.appendChild(title);
    table.appendChild(row);

    return table;
  }

  private getIncomeDisplaysElement(teamIDs: Array<number>): HTMLElement {
    const table = document.createElement("table");
    table.id = "income-table";
    table.style.width = "100%";

    const title = document.createElement('td');
    title.colSpan = 4;
    const label = document.createElement('div');
    label.className = "stats-header";
    label.innerText = 'Uranium Income Per Turn';

    const row = document.createElement("tr");

    const cellUranium = document.createElement("td");
    teamIDs.forEach((id: number) => {
      
      // cell.appendChild(document.createTextNode("1.001"));
      // cell.appendChild(this.buffDisplays[id].numBuffs);
      // cell.appendChild(document.createTextNode(" = "));
      cellUranium.appendChild(this.incomeDisplays[id].uraniumIncome);
      row.appendChild(cellUranium);
    });

    title.appendChild(label);
    table.appendChild(title);
    table.appendChild(row);

    return table;
  }

  private getByteCodeGraph(){
    const canvas = document.createElement("canvas");
    canvas.id = "byteGraph";
    canvas.className = "graph";
    return canvas;
  }

  private getIncomeUraniumGraph() {
    const canvas = document.createElement("canvas");
    //canvas.id = "leadGraph";
    canvas.id = "uraniumGraph";
    canvas.className = "graph";
    return canvas;
  }

  private getHpHistogram(team) {
    const canvas = document.createElement("canvas");
    //canvas.id = "leadGraph";
    canvas.id = team.concat("-hp-histogram");
    canvas.className = "histogram";
    return canvas;
  }

//   private getIncomeGoldGraph() {
//     const canvas = document.createElement("canvas");
//     canvas.id = "goldGraph";
//     canvas.className = "graph";
//     return canvas;
//   }

  private getECDivElement() {
    const div = document.createElement('div');
    const label = document.createElement('div');
    label.className = "stats-header";
    label.innerText = 'Unit Size Histograms';
    div.appendChild(label);
    div.appendChild(this.ECs);
    return div;
  }

  // private drawBuffsGraph(ctx: CanvasRenderingContext2D, upto: number) {
  //   ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
  //   // draw axes
  //   ctx.save();
  //   ctx.strokeStyle = "#000000";
  //   ctx.lineWidth = 0.02;
  //   ctx.moveTo(0, 1);
  //   ctx.lineTo(0, 0);
  //   ctx.stroke();
  //   ctx.moveTo(0, 1);
  //   ctx.lineTo(1, 1);
  //   ctx.stroke();

  //   const xscale = 1 / upto;
  //   const yscale = 1 / cst.buffFactor(upto);

  //   for (let i = 0; i <= upto; i++) {
  //     ctx.moveTo(i * xscale, 1 - cst.buffFactor(i) * yscale);
  //     ctx.lineTo(i * xscale, 1 - cst.buffFactor(i + 1) * yscale);
  //   }
  //   ctx.stroke();

  //   ctx.restore();
  // }

  // private plotBuff(ctx: CanvasRenderingContext2D, upto: number, buff1: number, buff2: number) {
  //   const xscale = 1 / upto;
  //   const yscale = 1 / cst.buffFactor(upto);

  //   ctx.save();

  //   ctx.fillStyle = hex[1];
  //   ctx.font = "0.1px Comic Sans MS";
  //   //  ctx.moveTo(buff1*xscale, cst.buffFactor(buff1)*yscale);
  //   ctx.fillText("R", buff1 * xscale, 1 - cst.buffFactor(buff1) * yscale + 0.08);
  //   // ctx.arc(buff1*xscale, cst.buffFactor(buff1)*yscale, 0.02, 0, 2*Math.PI);
  //   // ctx.fill();

  //   ctx.fillStyle = hex[2];
  //   ctx.fillText("B", buff2 * xscale, 1 - cst.buffFactor(buff2) * yscale - 0.04);

  //   ctx.moveTo(buff2 * xscale, cst.buffFactor(buff2) * yscale - 0.05);
  //   // ctx.arc(buff1*xscale, cst.buffFactor(buff2)*yscale - 0.05, 0.02, 0, 2*Math.PI);
  //   // ctx.fill();
  //   ctx.restore();
  // }

  /**
   * Clear the current stats bar and reinitialize it with the given teams.
   */
  initializeGame(teamNames: Array<string>, teamIDs: Array<number>) {
    // Remove the previous match info
    while (this.div.firstChild) {
      this.div.removeChild(this.div.firstChild);
    }
    this.relativeBars = [];
    //this.maxVotes = 750; //TODO SECON 
    this.teamMapToTurnsIncomeSet = new Map();

    this.div.appendChild(document.createElement("br"));
    if (this.conf.tournamentMode) {
      // FOR TOURNAMENT
      this.tourneyUpload = document.createElement('div');
      
      let uploadButton = this.runner.getUploadButton();
      let tempdiv = document.createElement("div");
      tempdiv.className = "upload-button-div";
      tempdiv.appendChild(uploadButton);
      this.tourneyUpload.appendChild(tempdiv);

      // add text input field
      this.tourIndexJump.type = "text";
      this.tourIndexJump.onkeyup = (e) => { this.tourIndexJumpFun(e) };
      this.tourIndexJump.onchange = (e) => { this.tourIndexJumpFun(e) };
      this.tourneyUpload.appendChild(this.tourIndexJump);

      this.div.appendChild(this.tourneyUpload);
    }

    this.extraInfo = document.createElement('div');
    this.extraInfo.className = "extra-info";
    this.div.appendChild(this.extraInfo);

    // Populate with new info
    // Add a section to the stats bar for each team in the match
    for (var index = 0; index < teamIDs.length; index++) {
      // Collect identifying information
      let teamID = teamIDs[index];
      let teamName = teamNames[index];
      let inGameID = index + 1; // teams start at index 1
      this.robotTds[teamID] = new Map();

      // A div element containing all stats information about this team
      let teamDiv = document.createElement("div");

      // Create td elements for the robot counts and store them in robotTds
      // so we can update these robot counts later; maps robot type to count
      for (let value of ["count", "hp"]) {
        this.robotTds[teamID][value] = new Map<number, HTMLTableCellElement>();
        for (let robot of this.robots) {
          let td: HTMLTableCellElement = document.createElement("td");
          td.innerHTML = "0";
          this.robotTds[teamID][value][robot] = td;
        }
      }

      // Add the team name banner and the robot count table
      teamDiv.appendChild(this.teamHeaderNode(teamName, inGameID));
      teamDiv.appendChild(this.robotTable(teamID, inGameID));

      this.div.appendChild(teamDiv);
    }

    this.div.appendChild(document.createElement("hr"));

    // Add stats table
    /*let archonnums: Array<number> = [1, 2];
    teamIDs.forEach((id: number) => {
      archonnums[id] = (this.robotTds[id]["count"][ARCHON].inner == undefined) ? 0 : this.robotTds[id]["count"][ARCHON].inner;
      console.log(archonnums[id]);
    });*/
    this.relativeBars = this.initRelativeBars(teamIDs);
    const relativeBarsElement = this.getRelativeBarsElement();
    relativeBarsElement.forEach((relBar: HTMLDivElement) => { this.div.appendChild(relBar);});
    
    this.div.appendChild(document.createElement("hr"));

    this.incomeDisplays = this.initIncomeDisplays(teamIDs);
    const incomeElement = this.getIncomeDisplaysElement(teamIDs);

    const byteElement = this.getByteDisplaysElement(teamIDs);
    
    //this.div.appendChild(incomeElement);

    const graphs = document.createElement("div");
    graphs.style.display = 'flex';
    
    const leadWrapper = document.createElement("div");
    leadWrapper.style.width = "50%";
    leadWrapper.style.float = "left";
    leadWrapper.appendChild(incomeElement);

    const canvasElementLead = this.getIncomeUraniumGraph();
    leadWrapper.appendChild(canvasElementLead);

    const byteWrapper = document.createElement("div");
    byteWrapper.style.width = "50%";
    byteWrapper.style.float = "right";

    byteWrapper.appendChild(byteElement);
    
    const canvasBytecodeElement = this.getByteCodeGraph();
    byteWrapper.appendChild(canvasBytecodeElement);
        
    graphs.appendChild(leadWrapper);
    graphs.appendChild(byteWrapper);

    //const goldWrapper = document.createElement("div");
    //goldWrapper.style.width = "50%";
    //goldWrapper.style.float = "right";
    //const canvasElementGold = this.getIncomeGoldGraph();
    //goldWrapper.appendChild(canvasElementGold);    
    //graphs.appendChild(goldWrapper);

    this.div.appendChild(graphs);


    this.incomeChartUranium = new Chart(canvasElementLead, {
      type: 'line',
      data: {
          datasets: [{
            label: 'Red',
            data: [],
            backgroundColor: 'rgba(255, 99, 132, 0)',
            borderColor: 'rgb(131,24,27)',
            pointRadius: 0,
          },
          {
            label: 'Blue',
            data: [],
            backgroundColor: 'rgba(54, 162, 235, 0)',
            borderColor: 'rgb(108, 140, 188)',
            pointRadius: 0,
          }]
      },
      options: {
          aspectRatio: 0.75,
          scales: {
            xAxes: [{
              type: 'linear',
              ticks: {
                beginAtZero: true
            },
              scaleLabel: {
                display: true,
                labelString: "Turn"
              }
            }],
              yAxes: [{
                type: 'linear',
                  ticks: {
                      beginAtZero: true
                  }
              }]
          }
      }
    });

    this.bytecodeChart = new Chart(canvasBytecodeElement, {
      type: 'line',
      data: {
          datasets: [{
            label: 'Red',
            data: [],
            backgroundColor: 'rgba(255, 99, 132, 0)',
            borderColor: 'rgb(131,24,27)',
            pointRadius: 0,
          },
          {
            label: 'Blue',
            data: [],
            backgroundColor: 'rgba(54, 162, 235, 0)',
            borderColor: 'rgb(108, 140, 188)',
            pointRadius: 0,
          }]
      },
      options: {
          aspectRatio: 0.7,
          scales: {
            xAxes: [{
              type: 'linear',
              ticks: {
                beginAtZero: true
            },
              scaleLabel: {
                display: true,
                labelString: "Turn"
              }
            }],
              yAxes: [{
                type: 'linear',
                  ticks: {
                      beginAtZero: true
                  }
              }]
          }
      }
    });
    /*
    this.incomeChartGold = new Chart(canvasElementGold, {
      type: 'line',
      data: {
          datasets: [
          {
            label: 'Red Gold',
            data: [],
            backgroundColor: 'rgba(162, 162, 235, 0)',
            borderColor: 'rgb(205,162,163)',
            pointRadius: 0,
          },
          {
            label: 'Blue Gold',
            data: [],
            backgroundColor: 'rgba(54, 0, 235, 0)',
            borderColor: 'rgb(68, 176, 191)',
            pointRadius: 0,
          }]
      },
      options: {
          aspectRatio: 0.75,
          scales: {
            xAxes: [{
              type: 'linear',
              ticks: {
                beginAtZero: true
            },
              scaleLabel: {
                display: true,
                labelString: "Turn"
              }
            }],
              yAxes: [{
                type: 'linear',
                  ticks: {
                      beginAtZero: true
                  }
              }]
          }
      }
    });
    */
    this.div.appendChild(document.createElement("hr"));

    this.ECs = document.createElement("div");
    //this.ECs.style.height = "100px";
    //this.ECs.style.display = "flex";
    this.div.appendChild(this.getECDivElement());

    

    const redhistoWrapper = document.createElement("div");
    redhistoWrapper.style.width = "50%";
    redhistoWrapper.style.float = "left";

    const canvasElementRedHisto = this.getHpHistogram("red");

    redhistoWrapper.appendChild(canvasElementRedHisto);


    var xValues : Array<number> = [];
    var yValues : Array<number> = [];
    var barColors  : Array<string> = [];
    for(let i = 0; i < this.num_lumps; i++){
      xValues.push(i);
      yValues.push(i);
      barColors.push("red");
    }


    this.redHpHisto =  new Chart(canvasElementRedHisto, {
    type: "bar",
    data: {
      labels: xValues,
      datasets: [{
        backgroundColor: barColors,
        data: yValues
      }]
    },
    options: {
      legend: {display: false},
      title: {
        display: false,
        text: "World Wine Production 2018"
      }
    }
  });

    this.div.appendChild(redhistoWrapper);

//-------------------------------------------
    const bluehistoWrapper = document.createElement("div");
    bluehistoWrapper.style.width = "50%";
    bluehistoWrapper.style.float = "right";

    const canvasElementBlueHisto = this.getHpHistogram("blue");

    bluehistoWrapper.appendChild(canvasElementBlueHisto);


    var xValues : Array<number> = [];
    var yValues : Array<number> = [];
    var barColors  : Array<string> = [];
    for(let i = 0; i < this.num_lumps; i++){
      xValues.push(i);
      yValues.push(i);
      barColors.push("blue");
    }


    this.blueHpHisto =  new Chart(canvasElementBlueHisto, {
    type: "bar",
    data: {
      labels: xValues,
      datasets: [{
        backgroundColor: barColors,
        data: yValues
      }]
    },
    options: {
      legend: {display: false},
      title: {
        display: false,
        text: "World Wine Production 2018"
      }
    }
  });

  this.div.appendChild(bluehistoWrapper);
  this.div.appendChild(document.createElement("br"));


  }

  private getTeamByteCodes(bytecodesUsed, teams, teamNum){
    var total = 0;
    for(let i = 0; i < teams.length; i++){
      if(teams[i] == teamNum){
        total += bytecodesUsed[i];
      }
    }
    return total;
  }

  updateBytecode(bytecodesUsed, team, turn){
    let bytecodesUsedRed = this.getTeamByteCodes(bytecodesUsed, team, 1);
    let bytecodesUsedBlue = this.getTeamByteCodes(bytecodesUsed, team, 2);
    console.log("updating bytecode");
    console.log(bytecodesUsed);
    console.log(bytecodesUsedRed);
    console.log(bytecodesUsedBlue);
    //@ts-ignore
    this.bytecodeChart.data.datasets[0].data?.push({y: bytecodesUsedRed, x: turn});
    //@ts-ignore
    this.bytecodeChart.data.datasets[1].data?.push({y: bytecodesUsedBlue, x: turn});
    this.bytecodeChart.update();

    /*
    data: {
          datasets: [{
            label: 'Red',
            data: [],
            backgroundColor: 'rgba(255, 99, 132, 0)',
            borderColor: 'rgb(131,24,27)',
            pointRadius: 0,
          },
          {
            label: 'Blue',
            data: [],
            backgroundColor: 'rgba(54, 162, 235, 0)',
            borderColor: 'rgb(108, 140, 188)',
            pointRadius: 0,
          }]
    */

  }

  tourIndexJumpFun(e) {
    if (e.keyCode === 13) {
      var h = +this.tourIndexJump.value.trim().toLowerCase();
      this.runner.seekTournament(h - 1);
    }
  }

  /**
   * Change the robot count on the stats bar
   */
  setRobotCount(teamID: number, robotType: schema.BodyType, count: number) {
    let td: HTMLTableCellElement = this.robotTds[teamID]["count"][robotType];
    td.innerHTML = String(count);
  }

  /**
   * Change the robot HP (previously conviction) on the stats bar
   */
  setRobotHP(teamID: number, robotType: schema.BodyType, HP: number, totalHP: number) {
    let td: HTMLTableCellElement = this.robotTds[teamID]["hp"][robotType];
    td.innerHTML = String(HP);

    const robotName: string = cst.bodyTypeToString(robotType);
    let img = this.robotImages[robotName][teamID];

    const size = (55 + 45 * HP / totalHP);
    img.style.width = size + "%";
    img.style.height = size + "%";
  }

  /**
   * Change the robot influence on the stats bar
   */
  /**### setRobotInfluence(teamID: number, robotType: schema.BodyType, influence: number) {
    let td: HTMLTableCellElement = this.robotTds[teamID]["influence"][robotType];
    td.innerHTML = String(influence);
  }*/

  /**
   * Change the votes of the given team
   */
 
 /*
  setVotes(teamID: number, count: number) {
    // TODO: figure out if statbars.get(id) can actually be null??
    const statBar: ArchonBar = this.archonBars[teamID];
    statBar.archon.innerText = String(count);
    this.maxVotes = Math.max(this.maxVotes, count);
    statBar.bar.style.width = `${Math.min(100 * count / this.maxVotes, 100)}%`;

    // TODO add reactions to relative bars
    // TODO get total votes to get ratio
    // this.relBars[teamID].width;

    // TODO winner gets star?
    // if (this.images.star.parentNode === statBar.bar) {
    //   this.images.star.remove();
    // }
  }
  */
  /** setTeamInfluence(teamID: number, influence: number, totalInfluence: number) {
    const relBar: HTMLDivElement = this.relativeBars[teamID];
    relBar.innerText = String(influence);
    if (totalInfluence == 0) relBar.style.width = '50%';
    else relBar.style.width = String(Math.round(influence * 100 / totalInfluence)) + "%";
  }*/

  setIncome(teamID: number, uraniumIncome: number, turn: number) { // incomes
    this.incomeDisplays[teamID].uraniumIncome.textContent = "L: " + String(uraniumIncome.toFixed(2)); // change incomeDisplays later
    if (!this.teamMapToTurnsIncomeSet.has(teamID)) {
      this.teamMapToTurnsIncomeSet.set(teamID, new Set());
    }
    let teamTurnsIncomeSet = this.teamMapToTurnsIncomeSet.get(teamID);
    
     if (!teamTurnsIncomeSet!.has(turn)) {
       //@ts-ignore
       this.incomeChartUranium.data.datasets![teamID - 1].data?.push({y: uraniumIncome, x: turn});
       //@ts-ignore
       this.incomeChartUranium.data.datasets?.forEach((d) => {
         d.data?.sort((a, b) => a.x - b.x);
       });
//       this.incomeChartGold.data.datasets?.forEach((d) => {
//         d.data?.sort((a, b) => a.x - b.x);
//       });
       teamTurnsIncomeSet?.add(turn);
       this.incomeChartUranium.update();
//       this.incomeChartGold.update();
     }
//     // update bars here
      //console.log(teamID, count, "fsdfsdf");
//     //if(robotType === ARCHON) this.updateRelBars(teamID, count);
    }
  
  updateBars(teamUranium: Array<number>){
    this.updateRelBars(teamUranium);
  }

  setWinner(teamID: number, teamNames: Array<string>, teamIDs: Array<number>) {
    const name = teamNames[teamIDs.indexOf(teamID)];
    this.teamNameNodes[teamID].innerHTML  = "<b>" + name + "</b> " +  `<span style="color: yellow">&#x1f31f</span>`;
  }

  /*setBid(teamID: number, bid: number) {
    // TODO: figure out if statbars.get(id) can actually be null??
    const statBar: VoteBar = this.voteBars[teamID];
    statBar.bid.innerText = String(bid);
    // TODO add reactions to relative bars
    // TODO get total votes to get ratio
    // this.relBars[teamID].width;

    // TODO winner gets star?
    // if (this.images.star.parentNode === statBar.bar) {
    //   this.images.star.remove();
    // }
  }*/

  /*
  private cleateHistograms(teamRed, teamBlue){
    const num_lumps = 10;

    const divleft = document.createElement("div");
    divleft.setAttribute("align", "center");
    divleft.id = "hp-histogram-left";

    const labelleft = document.createElement('div');
    labelleft.className = "stats-header";
    labelleft.innerText = teamRed.concat(" hp histogram");

    const frameleft = document.createElement("div");
    frameleft.style.width = "100%";

    var xValues : Array<number> = [];
    var yValues : Array<number> = [];
    var barColors  : Array<string> = [];
    for(let i = 0; i < num_lumps; i++){
      xValues.push(i);
      yValues.push(0);
      barColors.push("red");
    }

    let red_chart = new Chart("red-hp-chart", {
    type: "bar",
    data: {
      labels: xValues,
      datasets: [{
        backgroundColor: barColors,
        data: yValues
      }]
    },
    options: {
      legend: {display: false},
      title: {
        display: false,
        text: "World Wine Production 2018"
      }
    }
  });
 


    frameleft.appendChild(red_chart);
    
    divleft.appendChild(labelleft);
    divleft.appendChild(frameleft);

  }
*/
  updateHistograms(hp, team){
    let red_hps : Array<number> = [];
    let blue_hps : Array<number> = [];
    for(var i = 0; i < hp.length; i++){
      if(hp[i] == 0){
        continue;
      }
      if(team[i] == 1){
        red_hps.push(hp[i]);
      } else{
        blue_hps.push(hp[i]);
      }
    }
    
    //TODO: fetch histograms to update and pass them below
    this.updateTeamHistogram(red_hps, this.redHpHisto);
    this.updateTeamHistogram(blue_hps, this.blueHpHisto);

  }

  private updateTeamHistogram(hps: Array<number>, histogram: Chart){
    let lump_vals : Array<number> = [];
    let lump_labels : Array<number> = [];
    let max_hp = hps.length >= 1? Math.max(...hps) : 1;
    let min_hp = hps.length >= 1? Math.min(...hps) : 1;
    let lump_size = Math.max(Math.ceil((max_hp - min_hp) / this.num_lumps), 1);
    for(let i = 0; i < this.num_lumps; i++){
      lump_labels.push(min_hp + i * lump_size);
      lump_vals.push(0);
    }
    for(let i = 0; i < hps.length; i++){
      let bin_num = Math.floor((hps[i] - min_hp) / lump_size);
      lump_vals[bin_num] += 1;
    }
    //@ts-ignore
    histogram.data.labels =  lump_labels;
    //@ts-ignore
    histogram.data.datasets[0].data = lump_vals;
    histogram.update();
    
  }

  setExtraInfo(info: string) {
    this.extraInfo.innerHTML = info;
  }

  hideTourneyUpload() {
    console.log(this.tourneyUpload);
    this.tourneyUpload.style.display = this.tourneyUpload.style.display === "none" ? "" : "none";
  }

//   resetECs() {
//     while (this.ECs.lastChild) this.ECs.removeChild(this.ECs.lastChild);
//     // console.log(this.ECs);
//     this.ECs.innerHTML = "";
//   }

//   addEC(teamID: number, health: number, body_status: number, level: number/*, img: HTMLImageElement */) {
//     const div = document.createElement("div");
//     let size = 1.0/(1 + Math.exp(-(health/100))) + 0.3;
//     div.style.width = (28*size).toString() + "px";
//     div.style.height = (28*size).toString() + "px";
//     div.style.position = 'releative';
//     div.style.top = '50%';
//     div.style.transform  = `translateY(-${50*size - 35}%)`;
//     const img = /* img */this.images.robots.archon[level * 6 + body_status * 2 + teamID].cloneNode() as HTMLImageElement;
//     img.style.width = `${56 * size}px`;
//     img.style.height = `${56 * size}px`; // update dynamically later
//     // img.style.marginTop = `${28*size}px`;

//     div.appendChild(img);
//     this.ECs.appendChild(div);
//   }
}
