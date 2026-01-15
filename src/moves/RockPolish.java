package moves;

import ru.ifmo.se.pokemon.*;

public class RockPolish extends StatusMove {
    public RockPolish() {
        super(Type.ROCK, 0, 100);
    }

    @Override
    protected void applySelfEffects(Pokemon p) {
        p.setMod(Stat.SPEED, 2);
    }

    @Override
    public String describe() {
        return "использует Rock Polish";
    }
}
