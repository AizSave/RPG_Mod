package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import aphorea.utils.area.AphAreaList;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGArea;

public class DivineBlessing extends ActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.healingParam(3),
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(20);
    }

    public DivineBlessing(int levelMax, int requiredClassLevel) {
        super("divineblessing", "#00ff00", levelMax, requiredClassLevel);
    }

    @Override
    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.run(player, playerData, activeSkillLevel, seed, isInUse);

        AphAreaList areaList = new AphAreaList(
                new RPGArea(300, getColor())
                        .setHealingArea(params[0].valueInt(playerData.getLevel(), activeSkillLevel))
        ).setOnlyVision(false);
        areaList.execute(player, false);
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.cling, SoundEffect.effect(player.x, player.y).volume(1F).pitch(2F));
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 12000;
    }
}
