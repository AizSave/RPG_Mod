package rpgclasses.content.player.PlayerClasses.Druid;

import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.PlayerClasses.Druid.ActiveSkills.*;
import rpgclasses.content.player.PlayerClasses.Druid.Passives.NaturesFavor;
import rpgclasses.content.player.PlayerClasses.Druid.Passives.PhoenixSpirit;
import rpgclasses.content.player.PlayerClasses.Druid.Passives.PrimalBurst;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.FloatPercentModifierBuff;
import rpgclasses.content.player.SkillsLogic.Passives.BasicPassive;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.SkillsList;
import rpgclasses.registry.RPGModifiers;

public class DruidPlayerClass extends PlayerClass {
    public DruidPlayerClass() {
        super("druid", "#27AE60");
    }

    @Override
    protected SkillsList<ActiveSkill> initActiveSkillsList() {
        SkillsList<ActiveSkill> skillsList = new SkillsList<>();

        int requiredLevel = 1;

        skillsList.addSkill(new WolfTransformation(5, requiredLevel));
        skillsList.addSkill(new QueenBeeTransformation(5, requiredLevel));

        requiredLevel = 5;

        skillsList.addSkill(new BearTransformation(5, requiredLevel));
        skillsList.addSkill(new FoxTransformation(5, requiredLevel));
        skillsList.addSkill(new RatTransformation(5, requiredLevel));

        requiredLevel = 10;

        skillsList.addSkill(new TreantTransformation(5, requiredLevel));


        return skillsList;
    }

    @Override
    protected SkillsList<Passive> initPassivesList() {
        SkillsList<Passive> skillsList = new SkillsList<>();

        skillsList.addSkill(new BasicPassive(
                "quickshift", "#00ff00", 10, 1,
                new FloatPercentModifierBuff(RPGModifiers.TRANSFORMATION_DELAY, -0.1F)
        ));
        skillsList.addSkill(new BasicPassive(
                "feralinstinct", "#ff6600", 10, 1,
                new FloatPercentModifierBuff(BuffModifiers.ALL_DAMAGE, 0.06F)
        ).setOnlyTransformed());
        skillsList.addSkill(new BasicPassive(
                "ironfur", "#666677", 10, 1,
                new FloatPercentModifierBuff(BuffModifiers.INCOMING_DAMAGE_MOD, 0.02F, false)
        ).setOnlyTransformed());
        skillsList.addSkill(new NaturesFavor(10, 1));

        skillsList.addSkill(new BasicPassive(
                "beastpace", "#ffcc00", 10, 5,
                new FloatPercentModifierBuff(BuffModifiers.SPEED, 0.06F)
        ).setOnlyTransformed());
        skillsList.addSkill(new PrimalBurst(10, 5));
        skillsList.addSkill(new BasicPassive(
                "elementalguard", "#ff00ff", 10, 5,
                new FloatPercentModifierBuff(BuffModifiers.FIRE_DAMAGE, -0.08F),
                new FloatPercentModifierBuff(BuffModifiers.FROST_DAMAGE, -0.08F),
                new FloatPercentModifierBuff(BuffModifiers.POISON_DAMAGE, -0.08F)
        ));

        skillsList.addSkill(new PhoenixSpirit(12, 10));

        return skillsList;
    }
}
