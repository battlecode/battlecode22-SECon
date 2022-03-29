package battlecode.world;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.GameActionExceptionType.*;
import battlecode.instrumenter.profiler.ProfilerCollection;
import battlecode.schema.Action;
import battlecode.server.ErrorReporter;
import battlecode.server.GameMaker;
import battlecode.server.GameState;
import battlecode.world.control.RobotControlProvider;

import java.util.*;

/**
 * The primary implementation of the GameWorld interface for containing and
 * modifying the game map and the objects on it.
 */
public strictfp class GameWorld {
    /**
     * The current round we're running.
     */
    protected int currentRound;

    /**
     * Whether we're running.
     */
    protected boolean running = true;

    protected final IDGenerator idGenerator;
    protected final GameStats gameStats;

    private boolean[] walls;
    private int[] uranium;
    private MapLocation[] spawnLocs;
    private InternalRobot[][] robots;
    private final LiveMap gameMap;
    private final TeamInfo teamInfo;
    private final ObjectInfo objectInfo;

    private Map<Team, ProfilerCollection> profilerCollections;

    private final RobotControlProvider controlProvider;
    private Random rand;
    private final GameMaker.MatchMaker matchMaker;

    @SuppressWarnings("unchecked")
    public GameWorld(LiveMap gm, RobotControlProvider cp, GameMaker.MatchMaker matchMaker) throws GameActionException {
        this.walls = gm.getWallArray();
        this.uranium = gm.getUraniumArray();
        this.spawnLocs = gm.getSpawnLocs();
        this.robots = new InternalRobot[gm.getWidth()][gm.getHeight()]; // if represented in cartesian, should be height-width, but this should allow us to index x-y
        this.currentRound = 0;
        this.idGenerator = new IDGenerator(gm.getSeed());
        this.gameStats = new GameStats();

        this.gameMap = gm;
        this.objectInfo = new ObjectInfo(gm);

        this.profilerCollections = new HashMap<>();

        this.controlProvider = cp;
        this.rand = new Random(this.gameMap.getSeed());
        this.matchMaker = matchMaker;

        controlProvider.matchStarted(this);

        // Add the controller robots (one for each team) to the GameWorld
        for (int i = 0; i < 2; i++) {
            spawnRobot(RobotType.CONTROLLER, Team.values()[i], -1);
        }

        // Add the robots contained in the LiveMap to this world.
        RobotInfo[] initialBodies = this.gameMap.getInitialBodies();
        for (int i = 0; i < initialBodies.length; i++) {
            RobotInfo robot = initialBodies[i];
            spawnRobot(robot.ID, robot.type, robot.team, GameConstants.INITIAL_ROBOT_HEALTH);
        }
        this.teamInfo = new TeamInfo(this);

        // Add initial amounts of resource
        this.teamInfo.addUranium(Team.A, GameConstants.INITIAL_URANIUM_AMOUNT);
        this.teamInfo.addUranium(Team.B, GameConstants.INITIAL_URANIUM_AMOUNT);

        // Write match header at beginning of match
        this.matchMaker.makeMatchHeader(this.gameMap);
    }

    /**
     * Run a single round of the game.
     *
     * @return the state of the game after the round has run
     */
    public synchronized GameState runRound() {
        if (!this.isRunning()) {
            List<ProfilerCollection> profilers = new ArrayList<>(2);
            if (!profilerCollections.isEmpty()) {
                profilers.add(profilerCollections.get(Team.A));
                profilers.add(profilerCollections.get(Team.B));
            }

            // Write match footer if game is done
            matchMaker.makeMatchFooter(gameStats.getWinner(), currentRound, profilers);
            return GameState.DONE;
        }

        try {
            this.processBeginningOfRound();
            this.controlProvider.roundStarted();

            int teamIndex = (this.getCurrentRound() - 1) % 2; // since first round is actually round 1, subtract to make team A go first
            Team teamToPlay = Team.values()[teamIndex];
            // TODO: there may be a safer way to do above because the enum is technically of length 3

            updateDynamicBodies(teamToPlay);

            this.controlProvider.roundEnded();
            this.processEndOfRound();

            if (!this.isRunning()) {
                this.controlProvider.matchEnded();
            }

        } catch (Exception e) {
            ErrorReporter.report(e);
            // TODO throw out file?
            return GameState.DONE;
        }
        // Write out round data
        matchMaker.makeRound(currentRound);
        return GameState.RUNNING;
    }

    private void updateDynamicBodies(Team teamToPlay) {
        objectInfo.eachDynamicBodyByExecOrder((body) -> {
            if (body instanceof InternalRobot) {
                try{
                    return updateRobot((InternalRobot) body, teamToPlay);
                }
                catch (GameActionException e) {
                    throw new RuntimeException("A GameActionException has occured." +
                        "This is likely because a Robot tried to call a Controller function," +
                        " or a Controller tried to control an enemy robot.");
                }

            } else {
                throw new RuntimeException("non-robot body registered as dynamic");
            }
        });
    }

    private boolean updateRobot(InternalRobot robot, Team teamToPlay) throws GameActionException {
        if (robot.getTeam() == teamToPlay) {
            robot.processBeginningOfTurn();
            if (robot.getType() == RobotType.CONTROLLER) {
                this.controlProvider.runRobot(robot);
                robot.setBytecodesUsed(this.controlProvider.getBytecodesUsed(robot));
            }
            robot.processEndOfTurn();
        }

        // TODO: does below still need to be done, also why is this returning True?
        
        // If the robot terminates but the death signal has not yet
        // been visited:
        // if (this.controlProvider.getTerminated(robot) && objectInfo.getRobotByID(robot.getID()) != null)
        //     destroyRobot(robot.getID());
        return true;
    }

    // *********************************
    // ****** BASIC MAP METHODS ********
    // *********************************

    public int getMapSeed() {
        return this.gameMap.getSeed();
    }

    public LiveMap getGameMap() {
        return this.gameMap;
    }

    public TeamInfo getTeamInfo() {
        return this.teamInfo;
    }

    public GameStats getGameStats() {
        return this.gameStats;
    }

    public ObjectInfo getObjectInfo() {
        return this.objectInfo;
    }

    public GameMaker.MatchMaker getMatchMaker() {
        return this.matchMaker;
    }

    public Team getWinner() {
        return this.gameStats.getWinner();
    }

    public boolean isRunning() {
        return this.running;
    }

    public int getCurrentRound() {
        return this.currentRound;
    }

    public boolean getWall(MapLocation loc) {
        return this.walls[locationToIndex(loc)];
    }

    public int getUranium(MapLocation loc) {
        return this.uranium[locationToIndex(loc)];
    }

    public void setUranium(MapLocation loc, int amount) {
        this.uranium[locationToIndex(loc)] = amount;
    }

    public MapLocation getSpawnLoc(Team team) {
        return this.spawnLocs[team.ordinal()];
    }

    /**
     * Helper method that converts a location into an index.
     * 
     * @param loc the MapLocation
     */
    public int locationToIndex(MapLocation loc) {
        return loc.x - this.gameMap.getOrigin().x + (loc.y - this.gameMap.getOrigin().y) * this.gameMap.getWidth();
    }

    /**
     * Helper method that converts an index into a location.
     * 
     * @param idx the index
     */
    public MapLocation indexToLocation(int idx) {
        return new MapLocation(idx % this.gameMap.getWidth() + this.gameMap.getOrigin().x,
                               idx / this.gameMap.getWidth() + this.gameMap.getOrigin().y);
    }

    // ***********************************
    // ****** ROBOT METHODS **************
    // ***********************************

    public InternalRobot getRobotByID(int id){
        return objectInfo.getRobotByID(id);
    }

    public InternalRobot getRobot(MapLocation loc) {
        return this.robots[loc.x - this.gameMap.getOrigin().x][loc.y - this.gameMap.getOrigin().y];
    }

    public void moveRobot(MapLocation start, MapLocation end) {
        addRobot(end, getRobot(start));
        removeRobot(start);
    }

    public void addRobot(MapLocation loc, InternalRobot robot) {
        this.robots[loc.x - this.gameMap.getOrigin().x][loc.y - this.gameMap.getOrigin().y] = robot;
    }

    public void removeRobot(MapLocation loc) {
        this.robots[loc.x - this.gameMap.getOrigin().x][loc.y - this.gameMap.getOrigin().y] = null;
    }

    public InternalRobot[] getAllRobots(){
        ArrayList<InternalRobot> returnRobots = new ArrayList<InternalRobot>();
        for (int i = 0; i < this.robots.length; i++)
            for (int j = 0; j < this.robots[0].length; j++){
                MapLocation newLocation = new MapLocation(i, j);
                if (getRobot(newLocation) != null)
                    returnRobots.add(getRobot(newLocation));
            }
        return returnRobots.toArray(new InternalRobot[returnRobots.size()]);
    }

    public InternalRobot[] getAllRobotsWithinRadiusSquared(MapLocation center, int radiusSquared) {
        ArrayList<InternalRobot> returnRobots = new ArrayList<InternalRobot>();
        for (MapLocation newLocation : getAllLocationsWithinRadiusSquared(center, radiusSquared))
            if (getRobot(newLocation) != null)
                returnRobots.add(getRobot(newLocation));
        return returnRobots.toArray(new InternalRobot[returnRobots.size()]);
    }

    public MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) {

        ArrayList<MapLocation> returnLocations = new ArrayList<MapLocation>();

        MapLocation origin = this.gameMap.getOrigin();
        int width = this.gameMap.getWidth();
        int height = this.gameMap.getHeight();
        
        int ceiledRadius = (int) Math.ceil(Math.sqrt(radiusSquared)) + 1; // add +1 just to be safe
        int minX = Math.max(center.x - ceiledRadius, origin.x);
        int minY = Math.max(center.y - ceiledRadius, origin.y);
        int maxX = Math.min(center.x + ceiledRadius, origin.x + width - 1);
        int maxY = Math.min(center.y + ceiledRadius, origin.y + height - 1);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                MapLocation newLocation = new MapLocation(x, y);
                if (center.isWithinDistanceSquared(newLocation, radiusSquared))
                    returnLocations.add(newLocation);
            }
        }
        return returnLocations.toArray(new MapLocation[returnLocations.size()]);
    }

  
    // *********************************
    // ****** GAMEPLAY *****************
    // *********************************

    public void processBeginningOfRound() {
        // Increment round counter
        currentRound++;

        // Robots only see a round every 2 rounds, so on odd rounds they start their perceived round
        boolean perceivedStartOfRound = (currentRound % 2 != 0);

        if (perceivedStartOfRound) {
            // Process beginning of each robot's round
            objectInfo.eachRobot((robot) -> {
                robot.processBeginningOfRound();
                return true;
            });
        }
    }

    public void setWinner(Team t, DominationFactor d) {
        gameStats.setWinner(t);
        gameStats.setDominationFactor(d);
    }

    private void initiateTieBreakers() {
        if (!setWinnerIfMoreUraniumValue())
            if (!setWinnerIfMoreUraniumMined())
                setWinnerBlue();
    }

    /**
     * @return whether a team wins based on one team no longer having robots
     */
    public boolean setWinnerIfNoMoreRobots() {

        int[] totalRobots = new int[2];
        
        // sum live robots worth
        for (InternalRobot robot : objectInfo.robotsArray()) {
            if (robot.getType() == RobotType.CONTROLLER) {
                continue;
            }
            totalRobots[robot.getTeam().ordinal()] += 1;
        }
        
        if (totalRobots[0] == 0 && totalRobots[1] == 0) {
            initiateTieBreakers();
            return true;
        } else if (totalRobots[0] == 0) {
            setWinner(Team.B, DominationFactor.ANNIHILATION);
            return true;
        } else if (totalRobots[1] == 0) {
            setWinner(Team.A, DominationFactor.ANNIHILATION);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has a greater net Uranium value
     */
    public boolean setWinnerIfMoreUraniumValue() {

        float[] totalUraniumValues = new float[2];

        // consider team reserves
        totalUraniumValues[Team.A.ordinal()] += this.teamInfo.getUranium(Team.A);
        totalUraniumValues[Team.B.ordinal()] += this.teamInfo.getUranium(Team.B);
        
        // sum live robots worth
        for (InternalRobot robot : objectInfo.robotsArray())
            totalUraniumValues[robot.getTeam().ordinal()] += robot.getHealth();
        
        if (Math.abs(totalUraniumValues[0] - totalUraniumValues[1]) < GameConstants.FLOAT_EQUALITY_THRESHOLD) {
            return false;
        } else if (totalUraniumValues[0] > totalUraniumValues[1]) {
            setWinner(Team.A, DominationFactor.MORE_URANIUM_NET_WORTH);
            return true;
        } else if (totalUraniumValues[1] > totalUraniumValues[0]) {
            setWinner(Team.B, DominationFactor.MORE_URANIUM_NET_WORTH);
            return true;
        }
        return false;
    }

    /**
     * @return whether a team has mined more uranium
     */
    public boolean setWinnerIfMoreUraniumMined() {
        int[] uraniumMined = new int[2];

        // consider team reserves
        uraniumMined[Team.A.ordinal()] += this.teamInfo.getUraniumMined(Team.A);
        uraniumMined[Team.B.ordinal()] += this.teamInfo.getUraniumMined(Team.B);
        
        if (uraniumMined[0] > uraniumMined[1]) {
            setWinner(Team.A, DominationFactor.MORE_URANIUM_MINED);
            return true;
        } else if (uraniumMined[1] > uraniumMined[0]) {
            setWinner(Team.B, DominationFactor.MORE_URANIUM_MINED);
            return true;
        }
        return false;
    }

    /**
     * Sets winner to be Team B (Blue team) since they get second turn.
     */
    public void setWinnerBlue() {
        setWinner(Team.B, DominationFactor.WON_BY_BEING_BLUE);
    }

    public boolean timeLimitReached() {
        return currentRound >= this.gameMap.getRounds();
    }

    public void processEndOfRound() {
        // Add uranium resources to the team
        this.teamInfo.addUranium(Team.A, GameConstants.PASSIVE_URANIUM_INCREASE);
        this.teamInfo.addUranium(Team.B, GameConstants.PASSIVE_URANIUM_INCREASE);

        // Robots only see a round every 2 rounds, so on even rounds they end their perceived round
        boolean perceivedEndOfRound = this.currentRound % 2 == 0;

        // Process end of each robot's round
        objectInfo.eachRobot((robot) -> {
            try {
                robot.processEndOfRound();
            } catch (GameActionException e) {
                throw new RuntimeException("A GameActionException has occured." +
                    "This is likely because a Robot tried to call a Controller function," +
                    " or a Controller tried to control an enemy robot.");
            }

            return true;
        });

        // Add uranium resources to the map
        if (this.currentRound % GameConstants.ADD_URANIUM_EVERY_ROUNDS == 0) {
            for (int i = 0; i < this.uranium.length; i++)
                if (this.uranium[i] > 0)
                    this.uranium[i] += GameConstants.ADD_URANIUM;
        }

        this.matchMaker.addTeamInfo(Team.A, this.teamInfo.getRoundUraniumChange(Team.A), this.teamInfo.getRoundUraniumMined(Team.A));
        this.matchMaker.addTeamInfo(Team.B, this.teamInfo.getRoundUraniumChange(Team.B), this.teamInfo.getRoundUraniumMined(Team.B));
        this.teamInfo.processEndOfRound();

        if (perceivedEndOfRound) {
            setWinnerIfNoMoreRobots();
            // Check for end of match
            if (timeLimitReached() && gameStats.getWinner() == null)
                initiateTieBreakers();

            if (gameStats.getWinner() != null)
                running = false;
        }
    }

    // *********************************
    // ****** SPAWNING *****************
    // *********************************

    public int spawnRobot(int ID, RobotType type, Team team, int health) throws GameActionException {
        if (type == RobotType.CONTROLLER) {
            InternalRobot robot = new InternalRobot(this, ID, type, null, health, team);
            objectInfo.spawnController(robot);
            controlProvider.robotSpawned(robot);
            return ID;
        }
        MapLocation spawnLoc = getSpawnLoc(team);
        InternalRobot robot = new InternalRobot(this, ID, type, spawnLoc, health, team);
        objectInfo.spawnRobot(robot);
        addRobot(spawnLoc, robot);
        matchMaker.addSpawnedRobot(robot);
        return ID;
    }

    public int spawnRobot(RobotType type, Team team, int health) throws GameActionException {
        int ID = idGenerator.nextID();
        return spawnRobot(ID, type, team, health);
    }

    // *********************************
    // ****** DESTROYING ***************
    // *********************************

    public void destroyRobot(int id) throws GameActionException {
        InternalRobot robot = objectInfo.getRobotByID(id);
        RobotType type = robot.getType();

        if (type == RobotType.CONTROLLER) {
            objectInfo.destroyController(id);
            controlProvider.robotKilled(robot);
            return;
        }

        removeRobot(robot.getLocation());
        objectInfo.destroyRobot(id);
        matchMaker.addDied(id);
    }

    // *********************************
    // ********* PROFILER **************
    // *********************************

    public void setProfilerCollection(Team team, ProfilerCollection profilerCollection) {
        if (profilerCollections == null) {
            profilerCollections = new HashMap<>();
        }
        profilerCollections.put(team, profilerCollection);
    }
}
