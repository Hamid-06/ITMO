package moves;

import ru.ifmo.se.pokemon.*;

public class Overheat extends SpecialMove {
    public Overheat() {
        super(Type.FIRE, 130, 90);
    }

    @Override
    protected void applySelfEffects(Pokemon p) {
        p.setMod(Stat.SPECIAL_ATTACK, -2);
    }

    @Override
    public String describe() {
        return "использует Overheat";
    }
}
