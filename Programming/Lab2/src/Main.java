import ru.ifmo.se.pokemon.*;
import pokemons.*;

public class Main {
    public static void main(String[] args) {
        Battle b = new Battle();

        Pokemon p1 = new Moltres("Phoenix", 50);
        Pokemon p2 = new Claydol("Guardian", 40);
        Pokemon p3 = new Nidoqueen("Queen", 45);

        Pokemon p4 = new Baltoy("Spinner", 30);
        Pokemon p5 = new Nidorina("PoisonGirl", 35);
        Pokemon p6 = new NidoranF("Baby", 20);

        b.addAlly(p1);
        b.addAlly(p2);
        b.addAlly(p3);

        b.addFoe(p4);
        b.addFoe(p5);
        b.addFoe(p6);

        b.go();
    }
}