package rpgclasses.content.player.PlayerClasses.Wizard;

import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.PlayerClasses.Wizard.ActiveSkills.*;
import rpgclasses.content.player.PlayerClasses.Wizard.Passives.FlamingSteps;
import rpgclasses.content.player.PlayerClasses.Wizard.Passives.ManaPower;
import rpgclasses.content.player.PlayerClasses.Wizard.Passives.Shield.ArcaneShield;
import rpgclasses.content.player.PlayerClasses.Wizard.Passives.Shield.FireShield;
import rpgclasses.content.player.PlayerClasses.Wizard.Passives.Shield.IceShield;
import rpgclasses.content.player.PlayerClasses.Wizard.Passives.Stormbound;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.FloatModifierBuff;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.FloatPercentModifierBuff;
import rpgclasses.content.player.SkillsLogic.Passives.BasicPassive;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.SkillsList;
import rpgclasses.registry.RPGModifiers;

public class WizardPlayerClass extends PlayerClass {
    public WizardPlayerClass() {
        super("wizard", "#4F75FF");
    }

    @Override
    protected SkillsList<ActiveSkill> initActiveSkillsList() {
        SkillsList<ActiveSkill> skillsList = new SkillsList<>();

        int requiredLevel = 1;

        skillsList.addSkill(new ArcaneOverload(5, requiredLevel));

        skillsList.addSkill(new ManaRecharge(3, requiredLevel));

        skillsList.addSkill(new FireDance(5, requiredLevel));

        skillsList.addSkill(new Iceball(5, requiredLevel));

        skillsList.addSkill(new Zap(5, requiredLevel));

        requiredLevel = 5;

        skillsList.addSkill(new Teleport(5, requiredLevel));

        skillsList.addSkill(new Inferno(5, requiredLevel));

        skillsList.addSkill(new FireEnchantment(5, requiredLevel));

        skillsList.addSkill(new IceEnchantment(5, requiredLevel));

        requiredLevel = 10;

        skillsList.addSkill(new Fireball(5, requiredLevel));

        skillsList.addSkill(new Lightning(5, requiredLevel));

        return skillsList;
    }

    @Override
    protected SkillsList<Passive> initPassivesList() {
        SkillsList<Passive> skillsList = new SkillsList<>();

        String shieldFamily = "wizard_shield";

        skillsList.addSkill(new BasicPassive(
                "arcanepower", "#6633ff", 10, 1,
                new FloatPercentModifierBuff(BuffModifiers.MAGIC_DAMAGE, 0.1F),
                new FloatPercentModifierBuff(BuffModifiers.SPEED, -0.08F)
        ));
        skillsList.addSkill(new ManaPower(10, 1));
        skillsList.addSkill(new BasicPassive(
                "manaproficiency", "#33ccff", 10, 1,
                new FloatPercentModifierBuff(BuffModifiers.MAX_MANA, 0.05F),
                new FloatModifierBuff(BuffModifiers.COMBAT_MANA_REGEN_FLAT, 0.1F)
        ));
        skillsList.addSkill(new BasicPassive(
                "firepower", "#ff3300", 10, 1,
                new String[]{"ignitedamage"},
                new FloatPercentModifierBuff(RPGModifiers.IGNITE_DAMAGE, 0.2F)
        ));

        skillsList.addSkill(new BasicPassive(
                "quickmagic", "#3366ff", 10, 5,
                new FloatPercentModifierBuff(RPGModifiers.CAST_TIME, -0.08F)
        ));
        skillsList.addSkill(new FlamingSteps(10, 5));
        skillsList.addSkill(new Stormbound(10, 5));

        skillsList.addSkill(new ArcaneShield(10, 10).setFamily(shieldFamily));
        skillsList.addSkill(new FireShield(10, 10).setFamily(shieldFamily));
        skillsList.addSkill(new IceShield(10, 10).setFamily(shieldFamily));


        return skillsList;
    }
}
