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
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.utils.RPGUtils;

public class NecroticBloom extends ActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(10).setDecimals(2, 0),
            new SkillParam("10 x <skilllevel>").setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(5);
    }

    public NecroticBloom(int levelMax, int requiredClassLevel) {
        super("necroticbloom", "#669966", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 60000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return -8000;
    }

    @Override
    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        super.run(player, playerData, activeSkillLevel, seed, isInUSe);

        float mod = params[0].value();
        float modDamage = params[1].value(activeSkillLevel);

        RPGUtils.getAllTargets(player, 300, mob -> mob.buffManager.hasBuff(RPGBuffs.MAGIC_POISON))
                .forEach(
                        mob -> {
                            ActiveBuff ab = mob.buffManager.getBuff(RPGBuffs.MAGIC_POISON);
                            MagicPoisonBuff.setPoisonDamage(ab, (MagicPoisonBuff.getPoisonDamage(ab) / mod) * modDamage);
                            MagicPoisonBuff.updateModifier(ab);
                            ab.setDurationLeft((int) (ab.getDurationLeft() * mod));
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