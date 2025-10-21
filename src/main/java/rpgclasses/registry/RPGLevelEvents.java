package rpgclasses.registry;

import necesse.engine.registries.LevelEventRegistry;
import rpgclasses.levelevents.FireExplosionLevelEvent;
import rpgclasses.levelevents.IceExplosionLevelEvent;
import rpgclasses.levelevents.NecroticExplosionLevelEvent;
import rpgclasses.levelevents.RPGExplosionLevelEvent;

public class RPGLevelEvents {

    public static void registerCore() {
        LevelEventRegistry.registerEvent("rpgexplosionevent", RPGExplosionLevelEvent.class);
        LevelEventRegistry.registerEvent("iceexplosionevent", IceExplosionLevelEvent.class);
        LevelEventRegistry.registerEvent("fireexplosionevent", FireExplosionLevelEvent.class);
        LevelEventRegistry.registerEvent("necroticexplosionevent", NecroticExplosionLevelEvent.class);
    }

}
