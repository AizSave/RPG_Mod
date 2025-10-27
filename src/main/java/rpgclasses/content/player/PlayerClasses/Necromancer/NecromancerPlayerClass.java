package rpgclasses.content.player.PlayerClasses.Necromancer;

import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.PlayerClasses.Necromancer.ActiveSkills.*;
import rpgclasses.content.player.PlayerClasses.Necromancer.Passives.*;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.FloatPercentModifierBuff;
import rpgclasses.content.player.SkillsLogic.Passives.BasicPassive;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.SkillsList;

public class NecromancerPlayerClass extends PlayerClass {
    public NecromancerPlayerClass() {
        super("necromancer", "#6A0DAD");
    }

    @Override
    protected SkillsList<ActiveSkill> initActiveSkillsList() {
        SkillsList<ActiveSkill> skillsList = new SkillsList<>();

        int requiredLevel = 1;

        skillsList.addSkill(new SkeletonHorde(5, requiredLevel));

        skillsList.addSkill(new SkeletonWarrior(5, requiredLevel));

        skillsList.addSkill(new Sacrifice(5, requiredLevel));

        skillsList.addSkill(new SiegeCry(5, requiredLevel));


        requiredLevel = 5;

        skillsList.addSkill(new BoneslingerHorde(5, requiredLevel));

        skillsList.addSkill(new Tomb(5, requiredLevel));

        skillsList.addSkill(new NecroticBloom(5, requiredLevel));

        skillsList.addSkill(new NecroticHero(5, requiredLevel));

        requiredLevel = 10;

        skillsList.addSkill(new UnholyGround(5, requiredLevel));

        skillsList.addSkill(new NecroticBarrage(5, requiredLevel));

        return skillsList;
    }

    @Override
    protected SkillsList<Passive> initPassivesList() {
        SkillsList<Passive> skillsList = new SkillsList<>();

        skillsList.addSkill(new BasicPassive(
                "darkpower", "#666666", 10, 1,
                new FloatPercentModifierBuff(BuffModifiers.SUMMON_DAMAGE, 0.05F),
                new FloatPercentModifierBuff(BuffModifiers.SUMMON_CRIT_CHANCE, 0.02F),
                new FloatPercentModifierBuff(BuffModifiers.SPEED, -0.08F)
        ));
        skillsList.addSkill(new NecroticPower(10, 1));
        skillsList.addSkill(new DarkMagic(10, 1));
        skillsList.addSkill(new NecroticArmy(10, 1));

        skillsList.addSkill(new DarkSummons(10, 5));
        skillsList.addSkill(new LifeLeech(10, 5));
        skillsList.addSkill(new NecroticRemains(10, 5));
        skillsList.addSkill(new EndlessLegion(8, 5));

        skillsList.addSkill(new Lichborn(10, 10));

        return skillsList;
    }
}
