package battlecode.world;

import battlecode.common.MapLocation;
import battlecode.common.Team;

/**
 * Lets maps be built easily, for testing purposes.
 */
public class TestMapBuilder {
    private MapBuilder mapBuilder;

    public TestMapBuilder(String name, int oX, int oY, int width, int height, int seed) {
        this.mapBuilder = new MapBuilder(name, width, height, oX, oY, seed);
    }

    public TestMapBuilder addSpawnLoc(Team team, MapLocation loc) {
        this.mapBuilder.addSpawnLoc(team, loc);
        return this;
    }
    
    public TestMapBuilder setWall(int x, int y, boolean value) {
        this.mapBuilder.setWall(x, y, value);
        return this;
    }

    public TestMapBuilder setUranium(int x, int y, int value) {
        this.mapBuilder.setUranium(x, y, value);
        return this;
    }

    public LiveMap build() {
        return this.mapBuilder.build();
    }
}
