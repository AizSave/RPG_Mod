package rpgclasses.content.player.PlayerClasses.Cleric;

import aphorea.registry.AphModifiers;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills.*;
import rpgclasses.content.player.PlayerClasses.Cleric.Passives.*;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.FloatModifierBuff;
import rpgclasses.content.player.SkillsLogic.Passives.BasicPassive;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.SkillsList;

public class ClericPlayerClass extends PlayerClass {
    public ClericPlayerClass() {
        super("cleric", "#FFD60A");
    }

    @Override
    protected SkillsList<ActiveSkill> initActiveSkillsList() {
        SkillsList<ActiveSkill> skillsList = new SkillsList<>();

        int requiredLevel = 1;

        skillsList.addSkill(new Smite(5, requiredLevel));
        skillsList.addSkill(new DivineBlessing(5, requiredLevel));
        skillsList.addSkill(new Purify(5, requiredLevel));
        skillsList.addSkill(new LightInfusion(5, requiredLevel));

        requiredLevel = 5;

        skillsList.addSkill(new HolyGuard(5, requiredLevel));
        skillsList.addSkill(new Judgment(5, requiredLevel));
        skillsList.addSkill(new Sanctuary(5, requiredLevel));

        requiredLevel = 10;

        skillsList.addSkill(new DivineIntervention(5, requiredLevel));
        skillsList.addSkill(new Resurrection(5, requiredLevel));
        skillsList.addSkill(new WrathOfLight(5, requiredLevel));

        return skillsList;
    }

    @Override
    protected SkillsList<Passive> initPassivesList() {
        SkillsList<Passive> skillsList = new SkillsList<>();

        skillsList.addSkill(new BasicPassive(
                "healer", "#00ff00", 10, 1,
                new FloatModifierBuff(AphModifiers.MAGIC_HEALING, 0.1F)
        ));
        skillsList.addSkill(new BasicPassive(
                "shaman", "#00ffff", 10, 1,
                new FloatModifierBuff(BuffModifiers.LIFE_ESSENCE_GAIN, 0.1F)
        ));
        skillsList.addSkill(new VitalAura(10, 1));
        skillsList.addSkill(new DivineJudge(10, 1));

        skillsList.addSkill(new EmpoweredHealing(10, 5));
        skillsList.addSkill(new SanctifiedMind(10, 5));
        skillsList.addSkill(new SanctifiedArmor(10, 5));

        skillsList.addSkill(new RadiantExpansion(10, 10));

        return skillsList;
    }
}
