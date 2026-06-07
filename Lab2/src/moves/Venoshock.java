package moves;

import ru.ifmo.se.pokemon.*;

public class Venoshock extends SpecialMove {
    public Venoshock() {
        super(Type.POISON, 65, 100);
    }

    @Override
    protected void applyOppEffects(Pokemon p) {
        Effect.poison(p);
    }

    @Override
    public String describe() {
        return "использует Venoshock";
    }
}
