package game.freya;

import game.freya.items.LittleChest;
import game.freya.items.Shovel;

public class GameTest {

    public void start() {
        Shovel myShovel = new Shovel();
        myShovel.setName("Моя первая лопата");

        LittleChest chest = new LittleChest();
        System.out.println("Chest was created: " + chest.getName() + ". Content place: " + chest.size());
        chest.put(myShovel);
        System.out.println("Is chest has shovel: " + chest.has(myShovel));

        System.out.println("Shovel can be packed by: " + myShovel.packSize());
        System.out.println("Is broken: " + myShovel.isBroken());
        myShovel.onBreak();
        System.out.println("Is broken: " + myShovel.isBroken());
        myShovel.repair();
        System.out.println("Is broken: " + myShovel.isBroken());

        myShovel.destroy(chest);
        System.out.println("Is chest has shovel: " + chest.has(myShovel));
    }
}
