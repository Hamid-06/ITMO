package pokemons;

import moves.*;

public class Nidorina extends NidoranF {
    public Nidorina(String name, int level) {
        super(name, level);
        setStats(70.0, 62.0, 67.0, 55.0, 55.0, 56.0);
        addMove(new Crunch());
    }
}
