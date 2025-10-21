package rpgclasses.content.player.SkillsLogic.ActiveSkills;

import necesse.engine.network.packet.PacketLevelEvent;
import necesse.engine.registries.LevelEventRegistry;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.mobs.PlayerMob;
import rpgclasses.data.PlayerData;

abstract public class SimpleLevelEventActiveSkill extends ActiveSkill {
    public SimpleLevelEventActiveSkill(String stringID, String color, int levelMax, int requiredClassLevel) {
        super(stringID, color, levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);
        addLevelEvent(player, playerData, activeSkillLevel, seed, isInUse);
    }

    public void addLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        LevelEvent event = getLevelEvent(player, playerData, activeSkillLevel, seed, isInUSe);
        if (event != null) {
            player.getLevel().entityManager.events.addHidden(event);
            player.getServer().network.sendToClientsWithEntity(new PacketLevelEvent(event), event);
        }
    }

    @Override
    public void registry() {
        super.registry();
        LevelEventRegistry.registerEvent(getLevelEventID(), getLevelEventClass());
    }

    abstract public LevelEvent getLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse);

    abstract public Class<? extends LevelEvent> getLevelEventClass();

    public String getLevelEventID() {
        return stringID + "levelevent";
    }
}
