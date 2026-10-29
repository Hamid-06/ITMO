package pokemons;

import ru.ifmo.se.pokemon.*;
import moves.*;

public class Baltoy extends Pokemon {
    public Baltoy(String name, int level) {
        super(name, level);
        setType(Type.GROUND, Type.PSYCHIC);
        setStats(40.0, 40.0, 55.0, 40.0, 70.0, 55.0);

        setMove(
                new MudSlap(),
                new RockPolish(),
                new Confusion()
        );
    }
}
