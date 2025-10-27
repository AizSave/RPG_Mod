package rpgclasses.content.player.PlayerClasses.Necromancer.ActiveSkills;

import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.gfx.GameResources;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;

public class SiegeCry extends SimpleBuffActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("50 x <skilllevel>").setDecimals(2, 0),
            SkillParam.staticParam(6)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(5);
    }

    public SiegeCry(int levelMax, int requiredClassLevel) {
        super("siegecry", "#ff6600", levelMax, requiredClassLevel);
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.roar, SoundEffect.effect(player.x, player.y).volume(1F).pitch(1.2F));
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                activeBuff.setModifier(BuffModifiers.SUMMONS_SPEED, params[0].value(getLevel(activeBuff)));
            }
        };
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return (int) (params[1].value() * 1000);
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 20000;
    }
}
