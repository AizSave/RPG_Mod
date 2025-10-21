package rpgclasses.content.player.PlayerClasses.Necromancer.ActiveSkills;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.gfx.GameResources;
import rpgclasses.buffs.MagicPoisonBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.utils.RPGUtils;

public class NecroticBloom extends ActiveSkill {

    public NecroticBloom(int levelMax, int requiredClassLevel) {
        super("necroticbloom", "#669966", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown() {
        return 60000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return -8000;
    }

    @Override
    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        super.run(player, playerData, activeSkillLevel, seed, isInUSe);
        RPGUtils.getAllTargets(player, 300, mob -> mob.buffManager.hasBuff(RPGBuffs.MAGIC_POISON))
                .forEach(
                        mob -> {
                            ActiveBuff ab = mob.buffManager.getBuff(RPGBuffs.MAGIC_POISON);
                            MagicPoisonBuff.setPoisonDamage(ab, MagicPoisonBuff.getPoisonDamage(ab) * 10);
                            MagicPoisonBuff.updateModifier(ab);
                            ab.setDurationLeft(ab.getDurationLeft() / 10);
                        }
                );
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.roar, SoundEffect.effect(player.x, player.y).volume(2F).pitch(0.5F));
        AphAreaList areaList = new AphAreaList(
                new AphArea(300, getColor())
        ).setOnlyVision(false);
        areaList.executeClient(player.getLevel(), player.x, player.y);
    }
}