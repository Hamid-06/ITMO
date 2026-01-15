package pokemons;

import ru.ifmo.se.pokemon.*;
import moves.*;

public final class Moltres extends Pokemon {
    public Moltres(String name, int level) {
        super(name, level);
        setType(Type.FIRE, Type.FLYING);
        setStats(90.0, 100.0, 90.0, 125.0, 85.0, 90.0);

        setMove(
                new Roost(),
                new FlameCharge(),
                new Overheat(),
                new AncientPower()
        );
    }
}
