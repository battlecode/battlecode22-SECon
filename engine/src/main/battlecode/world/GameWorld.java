package battlecode.world;

import battlecode.common.*;
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

    private int[] walls;
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
    public GameWorld(LiveMap gm, RobotControlProvider cp, GameMaker.MatchMaker matchMaker) {
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

        // Add the robots contained in the LiveMap to this world.
        RobotInfo[] initialBodies = this.gameMap.getInitialBodies();
        for (int i = 0; i < initialBodies.length; i++) {
            RobotInfo robot = initialBodies[i];
            MapLocation newLocation = robot.location.translate(gm.getOrigin().x, gm.getOrigin().y);
            spawnRobot(robot.ID, robot.type, newLocation, robot.team);
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

            int teamIndex = this.getCurrentRound() % 2;
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
                return updateRobot((InternalRobot) body, teamToPlay);
            } else {
                throw new RuntimeException("non-robot body registered as dynamic");
            }
        });
    }

    private boolean updateRobot(InternalRobot robot, Team teamToPlay) {
        if (robot.getTeam() == teamToPlay) {
            robot.processBeginningOfTurn();
            this.controlProvider.runRobot(robot);
            robot.setBytecodesUsed(this.controlProvider.getBytecodesUsed(robot));
            robot.processEndOfTurn();
        }

        // TODO: does below still need to be done, also why is this returning True?
        
        // If the robot terminates but the death signal has not yet
        // been visited:
        if (this.controlProvider.getTerminated(robot) && objectInfo.getRobotByID(robot.getID()) != null)
            destroyRobot(robot.getID());
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

    /**
     * Defensively copied at the level of LiveMap.
     */
    public AnomalyScheduleEntry[] getAnomalySchedule() {
        return this.gameMap.getAnomalySchedule();
    }

    public boolean isRunning() {
        return this.running;
    }

    public int getCurrentRound() {
        return this.currentRound;
    }

    public int getWall(MapLocation loc) {
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

    public InternalRobot[] getAllRobotsWithinRadiusSquared(MapLocation center, int radiusSquared) {
        ArrayList<InternalRobot> returnRobots = new ArrayList<InternalRobot>();
        for (MapLocation newLocation : getAllLocationsWithinRadiusSquared(center, radiusSquared))
            if (getRobot(newLocation) != null)
                returnRobots.add(getRobot(newLocation));
        return returnRobots.toArray(new InternalRobot[returnRobots.size()]);
    }

    public MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) {
        return getAllLocationsWithinRadiusSquaredWithoutMap(
            this.gameMap.getOrigin(),
            this.gameMap.getWidth(),
            this.gameMap.getHeight(),
            center, radiusSquared
        );
    }

    public static MapLocation[] getAllLocationsWithinRadiusSquaredWithoutMap(MapLocation origin,
                                                                            int width, int height,
                                                                            MapLocation center, int radiusSquared) {
        ArrayList<MapLocation> returnLocations = new ArrayList<MapLocation>();
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

    /**
     * @return all of the locations on the grid
     */
    private MapLocation[] getAllLocations() {
        return getAllLocationsWithinRadiusSquared(new MapLocation(0, 0), Integer.MAX_VALUE);
    }

    // *********************************
    // ****** GAMEPLAY *****************
    // *********************************

    public void processBeginningOfRound() {
        // Robots only see a round every 2 rounds, so on even rounds they start their perceived round
        boolean perceivedStartOfRound = (currentRound % 2 == 0);

        // Increment round counter
        currentRound++;

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

    /**
     * @return whether a team has a greater net Uranium value
     */
    public boolean setWinnerIfMoreUraniumValue() {
        int[] totalUraniumValues = new int[2];

        // consider team reserves
        totalUraniumValues[Team.A.ordinal()] += this.teamInfo.getUranium(Team.A);
        totalUraniumValues[Team.B.ordinal()] += this.teamInfo.getUranium(Team.B);
        
        // sum live robots worth
        for (InternalRobot robot : objectInfo.robotsArray())
            totalUraniumValues[robot.getTeam().ordinal()] += robot.getUraniumWorth();
        
        if (totalUraniumValues[0] > totalUraniumValues[1]) {
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

        // Robots only see a round every 2 rounds, so on odd rounds they end their perceived round
        boolean perceivedEndOfRound = this.currentRound % 2 != 0;

        if (perceivedEndOfRound) {
            // Process end of each robot's round
            objectInfo.eachRobot((robot) -> {
                robot.processEndOfRound();
                return true;
            });
        }

        // Add uranium resources to the map
        if (this.currentRound % GameConstants.ADD_URANIUM_EVERY_ROUNDS == GameConstants.ROUND_TO_UPDATE_URANIUM) 
            for (int i = 0; i < this.uranium.length; i++)
                if (this.uranium[i] > 0)
                    this.uranium[i] += GameConstants.ADD_URANIUM;

        this.matchMaker.addTeamInfo(Team.A, this.teamInfo.getRoundUraniumChange(Team.A), this.teamInfo.getRoundUraniumMined(Team.A));
        this.matchMaker.addTeamInfo(Team.B, this.teamInfo.getRoundUraniumChange(Team.B), this.teamInfo.getRoundUraniumMined(Team.B));
        this.teamInfo.processEndOfRound();


        if (perceivedEndOfRound) {
            // Check for end of match
            if (timeLimitReached() && gameStats.getWinner() == null)
                if (!setWinnerIfMoreUraniumValue())
                    if (!setWinnerIfMoreUraniumMined())
                        setWinnerBlue();

            if (gameStats.getWinner() != null)
                running = false;
        }
    }

    // *********************************
    // ****** SPAWNING *****************
    // *********************************

    public int spawnRobot(int ID, RobotType type, Team team) {
        MapLocation spawnLoc = getSpawnLoc(team);
        InternalRobot robot = new InternalRobot(this, ID, type, spawnLoc, team);
        objectInfo.spawnRobot(robot);
        addRobot(spawnLoc, robot);

        controlProvider.robotSpawned(robot);
        matchMaker.addSpawnedRobot(robot);
        return ID;
    }

    public int spawnRobot(int ID, RobotType type, MapLocation loc, Team team) {
        InternalRobot robot = new InternalRobot(this, ID, type, loc, team);
        objectInfo.spawnRobot(robot);
        addRobot(loc, robot);

        controlProvider.robotSpawned(robot);
        matchMaker.addSpawnedRobot(robot);
        return ID;
    }

    public int spawnRobot(RobotType type, Team team) {
        MapLocation spawnLoc = getSpawnLoc(team);
        int ID = idGenerator.nextID();
        return spawnRobot(ID, type, spawnLoc, team);
    }

    // *********************************
    // ****** DESTROYING ***************
    // *********************************

    public void destroyRobot(int id) {
        destroyRobot(id, true);
    }

    public void destroyRobot(int id, boolean checkWin) {
        InternalRobot robot = objectInfo.getRobotByID(id);
        RobotType type = robot.getType();
        Team team = robot.getTeam();
        removeRobot(robot.getLocation());

        controlProvider.robotKilled(robot);
        objectInfo.destroyRobot(id);

        if (checkWin) {
            // TODO: check this, possibly not here, since there is also the other case of running into each other which goes to tiebreaks
            // this happens here because the last robot can explode itself and kill another robot in the process
            if (this.objectInfo.getRobotCount(team) == 0)
                setWinner(team == Team.A ? Team.B : Team.A, DominationFactor.ANNIHILATION);
        }

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
