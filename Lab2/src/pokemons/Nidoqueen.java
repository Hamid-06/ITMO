package pokemons;

import ru.ifmo.se.pokemon.*;
import moves.*;

public final class Nidoqueen extends Nidorina {
    public Nidoqueen(String name, int level) {
        super(name, level);
        setType(Type.POISON, Type.GROUND);
        setStats(90.0, 92.0, 87.0, 75.0, 85.0, 76.0);
        addMove(new Facade());
    }
}
