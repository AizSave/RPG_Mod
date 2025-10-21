package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import aphorea.utils.area.AphAreaList;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGArea;

public class DivineBlessing extends ActiveSkill {

    public DivineBlessing(int levelMax, int requiredClassLevel) {
        super("divineblessing", "#00ff00", levelMax, requiredClassLevel);
    }

    @Override
    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.run(player, playerData, activeSkillLevel, seed, isInUse);

        AphAreaList areaList = new AphAreaList(
                new RPGArea(300, getColor())
                        .setHealingArea((int) (20 + 3 * playerData.getGrace(player) * activeSkillLevel))
        ).setOnlyVision(false);
        areaList.execute(player, false);
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.cling, SoundEffect.effect(player.x, player.y).volume(1F).pitch(2F));
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 20 + activeSkillLevel * 4;
    }

    @Override
    public int getBaseCooldown() {
        return 12000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"manausage"};
    }
}
