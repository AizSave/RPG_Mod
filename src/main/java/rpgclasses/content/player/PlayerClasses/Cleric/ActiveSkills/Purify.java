package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import aphorea.utils.area.AphAreaList;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.utils.RPGArea;
import rpgclasses.utils.RPGUtils;

public class Purify extends ActiveSkill {

    public Purify(int levelMax, int requiredClassLevel) {
        super("purify", "#ccffcc", levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);
        RPGUtils.streamMobsAndPlayers(player, 200)
                .filter(m -> m == player || m.isSameTeam(player))
                .forEach(m -> RPGBuffs.purify(m, player.isServer()));
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.cling, SoundEffect.effect(player.x, player.y).volume(1F).pitch(2F));

        new AphAreaList(
                new RPGArea(200, getColor())
        ).setOnlyVision(false).executeClient(player.getLevel(), player.x, player.y);
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 10;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"manausage"};
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return -3600;
    }
}
