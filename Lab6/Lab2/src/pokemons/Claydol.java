package pokemons;

import moves.*;

public final class Claydol extends Baltoy {
    public Claydol(String name, int level) {
        super(name, level);
        setStats(60.0, 70.0, 105.0, 70.0, 120.0, 75.0);
        addMove(new StoneEdge());
    }
}
