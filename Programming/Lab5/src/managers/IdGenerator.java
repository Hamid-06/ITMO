package managers;

import java.util.HashSet;
import java.util.Set;

public class IdGenerator {

    private final Set <Integer> usedIds = new HashSet<>();

    private int counter = 1;

    public int generateId(){
        while (usedIds.contains(counter)){
            counter ++;
        }
        usedIds.add(counter);
        return counter ++;
    }

    public void markUsed(int id){
        usedIds.add(id);

    }
    public void release(int id){
        usedIds.remove(id);
    }
    public void reset(){
        usedIds.clear();
        counter = 1;
    }


}
