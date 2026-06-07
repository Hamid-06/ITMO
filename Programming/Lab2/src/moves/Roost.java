package moves;

import ru.ifmo.se.pokemon.*;

public class Roost extends StatusMove {
    public Roost() {
        super(Type.FLYING, 0, 100);
    }

    @Override
    protected void applySelfEffects(Pokemon p) {
        Effect e = new Effect().turns(1);
        p.addEffect(e);
        p.setMod(Stat.HP, (int)(p.getStat(Stat.HP) * 0.5));
    }

    @Override
    public String describe() {
        return "использует Roost";
    }
}
