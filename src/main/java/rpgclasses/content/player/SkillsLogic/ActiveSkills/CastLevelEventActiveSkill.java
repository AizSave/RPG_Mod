package rpgclasses.content.player.SkillsLogic.ActiveSkills;

import necesse.engine.network.packet.PacketLevelEvent;
import necesse.engine.registries.LevelEventRegistry;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.mobs.PlayerMob;
import rpgclasses.data.PlayerData;

abstract public class CastLevelEventActiveSkill extends CastActiveSkill {

    public CastLevelEventActiveSkill(String stringID, String color, int levelMax, int requiredClassLevel) {
        super(stringID, color, levelMax, requiredClassLevel);
    }

    @Override
    public void castedRunServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunServer(player, playerData, activeSkillLevel, seed);

        addLevelEvent(player, playerData, activeSkillLevel, seed, false);
    }

    public void addLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        LevelEvent event = getLevelEvent(player, playerData, activeSkillLevel, seed, isInUSe);
        if (event != null) {
            player.getLevel().entityManager.events.add(event);
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
