package rpgclasses.registry;

import necesse.entity.mobs.Mob;

import java.util.ArrayList;
import java.util.List;

public class RPGTiles {

    public static List<String> grassTiles = new ArrayList<>();

    public static boolean isInGrassTile(Mob mob) {
        return mob.getLevel() != null && RPGTiles.grassTiles.contains(mob.getLevel().getTile(mob.getTileX(), mob.getTileY()).getStringID());
    }

}
