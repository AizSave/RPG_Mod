package rpgclasses.content.player.PlayerClasses.Warrior;

import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.PlayerClasses.Warrior.ActiveSkills.*;
import rpgclasses.content.player.PlayerClasses.Warrior.ActiveSkills.Ground.GroundDestruction;
import rpgclasses.content.player.PlayerClasses.Warrior.ActiveSkills.Ground.GroundSlam;
import rpgclasses.content.player.PlayerClasses.Warrior.Passives.LastBreath;
import rpgclasses.content.player.PlayerClasses.Warrior.Passives.UnleashingHaste;
import rpgclasses.content.player.PlayerClasses.Warrior.Passives.UnleashingRage;
import rpgclasses.content.player.PlayerClasses.Warrior.Passives.Unyielding;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.FloatModifierBuff;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.FloatPercentModifierBuff;
import rpgclasses.content.player.SkillsLogic.Passives.BasicPassive;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.SkillsList;
import rpgclasses.utils.RPGColors;

public class WarriorPlayerClass extends PlayerClass {
    public WarriorPlayerClass() {
        super("warrior", "#E67E22");
    }

    @Override
    protected SkillsList<ActiveSkill> initActiveSkillsList() {
        SkillsList<ActiveSkill> skillsList = new SkillsList<>();

        String groundFamily = "warrior_ground";

        int requiredLevel = 1;

        skillsList.addSkill(new Fury(5, requiredLevel));

        skillsList.addSkill(new Recovery(5, requiredLevel));

        skillsList.addSkill(new BerserkerCharge(5, requiredLevel));

        skillsList.addSkill(new Provocation(5, requiredLevel));

        requiredLevel = 5;

        ActiveSkill groundS = skillsList.addSkill(new GroundSlam(5, requiredLevel).setFamily(groundFamily));
        skillsList.addSkill(new GroundDestruction(3, requiredLevel).addRequiredSkill(groundS, 2, this).setFamily(groundFamily));

        skillsList.addSkill(new Parry(5, requiredLevel));

        skillsList.addSkill(new BattleCry(5, requiredLevel));


        requiredLevel = 10;

        skillsList.addSkill(new IronGuard(5, requiredLevel));

        skillsList.addSkill(new Intimidation(5, requiredLevel));


        requiredLevel = 15;

        skillsList.addSkill(new ObjectThrowing(5, requiredLevel));


        return skillsList;
    }

    @Override
    protected SkillsList<Passive> initPassivesList() {
        SkillsList<Passive> skillsList = new SkillsList<>();

        skillsList.addSkill(new BasicPassive(
                "regeneration", "#00ff00", 10, 1,
                new FloatModifierBuff(BuffModifiers.COMBAT_HEALTH_REGEN_FLAT, 0.4F)
        ));
        skillsList.addSkill(new BasicPassive(
                "muscles", "#ff6600", 10, 1,
                new FloatPercentModifierBuff(BuffModifiers.MELEE_DAMAGE, 0.02F),
                new FloatPercentModifierBuff(BuffModifiers.ATTACK_SPEED, 0.02F)
        ));
        skillsList.addSkill(new BasicPassive(
                "legs", "#ff6600", 10, 1,
                new FloatPercentModifierBuff(BuffModifiers.STAMINA_CAPACITY, 0.1F),
                new FloatPercentModifierBuff(BuffModifiers.ATTACK_MOVEMENT_MOD, 0.06F, false)
        ));
        skillsList.addSkill(new BasicPassive(
                "force", "#ff6600", 10, 1,
                new FloatPercentModifierBuff(BuffModifiers.KNOCKBACK_OUT, 0.2F)
        ));

        skillsList.addSkill(new BasicPassive(
                "vigor", "#ff0000", 10, 5,
                new FloatPercentModifierBuff(BuffModifiers.MAX_HEALTH, 0.02F)
        ));
        skillsList.addSkill(new BasicPassive(
                "unstoppable", RPGColors.HEX.iron, 5, 5,
                new FloatPercentModifierBuff(BuffModifiers.KNOCKBACK_INCOMING_MOD, 0.2F, false)
        ));

        skillsList.addSkill(new BasicPassive(
                "ironskin", RPGColors.HEX.iron, 5, 10,
                new FloatPercentModifierBuff(BuffModifiers.INCOMING_DAMAGE_MOD, 0.02F, false)
        ));

        skillsList.addSkill(new UnleashingRage(10, 15));
        skillsList.addSkill(new UnleashingHaste(10, 15));

        skillsList.addSkill(new Unyielding(10, 20));
        skillsList.addSkill(new LastBreath(10, 20));

        return skillsList;
    }
}
