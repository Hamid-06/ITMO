package pokemons;

import ru.ifmo.se.pokemon.*;
import moves.*;

public class NidoranF extends Pokemon {
    public NidoranF(String name, int level) {
        super(name, level);
        setType(Type.POISON);
        setStats(55.0, 47.0, 52.0, 40.0, 40.0, 41.0);

        setMove(
                new Venoshock(),
                new IceBeam()
        );
    }
}
