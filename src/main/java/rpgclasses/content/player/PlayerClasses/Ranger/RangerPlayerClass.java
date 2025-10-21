package rpgclasses.content.player.PlayerClasses.Ranger;

import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.PlayerClasses.Ranger.ActiveSkills.*;
import rpgclasses.content.player.PlayerClasses.Ranger.ActiveSkills.Dash.LongDash;
import rpgclasses.content.player.PlayerClasses.Ranger.ActiveSkills.Dash.ShortDash;
import rpgclasses.content.player.PlayerClasses.Ranger.Passives.PlasmaGrenade;
import rpgclasses.content.player.PlayerClasses.Ranger.Passives.WolfCompanion;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.FloatModifierBuff;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.FloatPercentModifierBuff;
import rpgclasses.content.player.SkillsLogic.Passives.BasicPassive;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.SkillsList;
import rpgclasses.registry.RPGModifiers;

public class RangerPlayerClass extends PlayerClass {
    public RangerPlayerClass() {
        super("ranger", "#2ECC71");
    }

    @Override
    protected SkillsList<ActiveSkill> initActiveSkillsList() {
        SkillsList<ActiveSkill> skillsList = new SkillsList<>();

        String dashFamily = "ranger_dash";

        int requiredLevel = 1;

        skillsList.addSkill(new ShortDash(5, requiredLevel).setFamily(dashFamily));
        skillsList.addSkill(new LongDash(5, requiredLevel).setFamily(dashFamily));

        skillsList.addSkill(new LethalShot(5, requiredLevel));

        skillsList.addSkill(new VemomShot(5, requiredLevel));

        skillsList.addSkill(new ExplosiveShot(5, requiredLevel));

        requiredLevel = 5;

        skillsList.addSkill(new ShotsRampage(5, requiredLevel));

        skillsList.addSkill(new HuntersMark(5, requiredLevel));

        skillsList.addSkill(new BearTrap(5, requiredLevel));

        requiredLevel = 10;

        skillsList.addSkill(new HuntersInstinct(5, requiredLevel));

        skillsList.addSkill(new ArrowStorm(5, requiredLevel));

        return skillsList;
    }

    @Override
    protected SkillsList<Passive> initPassivesList() {
        SkillsList<Passive> skillsList = new SkillsList<>();

        skillsList.addSkill(new BasicPassive(
                "aim", "#ff3300", 10, 1,
                new FloatPercentModifierBuff(BuffModifiers.RANGED_DAMAGE, 0.02F),
                new FloatPercentModifierBuff(BuffModifiers.RANGED_CRIT_CHANCE, 0.02F)
        ));
        skillsList.addSkill(new BasicPassive(
                "criticalshots", "#ff0000", 10, 1,
                new FloatPercentModifierBuff(BuffModifiers.RANGED_CRIT_DAMAGE, 0.1F)
        ));
        skillsList.addSkill(new BasicPassive(
                "agility", "#009900", 10, 1,
                new String[]{"dodgechance"},
                new FloatPercentModifierBuff(RPGModifiers.DODGE_CHANCE, 0.03F),
                new FloatPercentModifierBuff(BuffModifiers.SPEED, 0.02F)
        ));
        skillsList.addSkill(new BasicPassive(
                "energy", "#ffff00", 10, 1,
                new FloatPercentModifierBuff(BuffModifiers.STAMINA_CAPACITY, 0.2F)
        ));

        skillsList.addSkill(new BasicPassive(
                "recovery", "#00ff00", 10, 5,
                new FloatPercentModifierBuff(BuffModifiers.STAMINA_REGEN, 0.1F)
        ));
        skillsList.addSkill(new BasicPassive(
                "eagleeye", "#009900", 10, 5,
                new FloatModifierBuff(RPGModifiers.RANGED_WEAPONS_ZOOM, 50F),
                new FloatPercentModifierBuff(RPGModifiers.RANGED_WEAPONS_RANGE, 0.03F)
        ));
        skillsList.addSkill(new BasicPassive(
                "erradication", "#ff0000", 10, 5,
                new String[]{"marked"},
                new FloatPercentModifierBuff(RPGModifiers.FOCUS_DAMAGE, 0.05F)
        ));
        skillsList.addSkill(new BasicPassive(
                "focus", "#ff0000", 10, 5,
                new String[]{"focuschance", "marked"},
                new FloatPercentModifierBuff(RPGModifiers.FOCUS_CHANCE, 0.01F)
        ));

        skillsList.addSkill(new BasicPassive(
                "predatorsense", "#ffcc00", 10, 10,
                new String[]{"mobdetection"},
                new FloatModifierBuff(RPGModifiers.MOB_DETECTION_RANGE, 300F)
        ));

        skillsList.addSkill(new PlasmaGrenade(10, 15));
        skillsList.addSkill(new WolfCompanion(5, 15));

        return skillsList;
    }
}
