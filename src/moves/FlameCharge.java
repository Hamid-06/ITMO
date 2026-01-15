package moves;

import ru.ifmo.se.pokemon.*;

public class FlameCharge extends PhysicalMove {
    public FlameCharge() {
        super(Type.FIRE, 50, 100);
    }

    @Override
    protected void applySelfEffects(Pokemon p) {
        p.setMod(Stat.SPEED, 1);
    }

    @Override
    public String describe() {
        return "использует Flame Charge";
    }
}
