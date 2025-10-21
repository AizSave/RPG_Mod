package rpgclasses.content.player.Mastery;

import necesse.engine.localization.Localization;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.registries.BuffRegistry;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.buffs.Skill.SecondaryMasteryBuff;
import rpgclasses.buffs.Skill.SecondaryPassiveBuff;
import rpgclasses.content.player.Mastery.MasterySkills.*;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.data.PlayerData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class Mastery extends SimpleBuffPassive {
    public static Map<String, Mastery> masterySkills = new HashMap<>();
    public static List<Mastery> masterySkillsList = new ArrayList<>();

    public static Mastery CHRONOMANCER;
    public static Mastery MARKSMAN;
    public static Mastery HUNTER;
    public static Mastery PYROMANCER;
    public static Mastery IRON_INVOKER;
    public static Mastery PLAGUE_BEARER;
    public static Mastery INQUISITOR;

    public static void registerCore() {
        registerMastery(new Bastion("bastion", "#cccc66"));
        registerMastery(new BloodBorn("bloodborn", "#990000"));
        CHRONOMANCER = registerMastery(new Chronomancer("chronomancer", "#00ffff"));
        registerMastery(new Barbarian("barbarian", "#ff6600"));
        registerMastery(new Berserker("berserker", "#ff0000"));
        registerMastery(new Sniper("sniper", "#ff0000"));
        MARKSMAN = registerMastery(new FlatMastery("marksman", "#00ff00"));
        HUNTER = registerMastery(new FlatMastery("hunter", "#00cc00", "constrained"));
        registerMastery(new Trapper("trapper", "#009900"));
        PYROMANCER = registerMastery(new FlatMastery("pyromancer", "#ff3300"));
        registerMastery(new FrostWeaver("frostweaver", "#00ffff"));
        registerMastery(new StormCaller("stormcaller", "#ffff00"));
        IRON_INVOKER = registerMastery(new IronInvoker("ironinvoker", "#666666"));
        PLAGUE_BEARER = registerMastery(new FlatMastery("plaguebearer", "#336633"));
        registerMastery(new DarkEminence("darkeminence", "#663333"));
        INQUISITOR = registerMastery(new FlatMastery("inquisitor", "#ffcc00"));
        registerMastery(new Timekeeper("timekeeper", "#ffff00"));
        registerMastery(new Shapeshifter("shapeshifter", "#ff99ff"));
    }


    public static Mastery registerMastery(Mastery mastery) {
        masterySkills.put(mastery.stringID, mastery);
        masterySkillsList.add(mastery);
        return mastery;
    }

    public LoadedMod mod;

    public Mastery(String stringID, String color) {
        super(stringID, color, 1, 0, Localization.translate("masterydesc", stringID).contains("[["));
        this.id = masterySkillsList.size();
        this.mod = LoadedMod.getRunningMod();
    }

    @Override
    public void initResources() {
        texture = GameTexture.fromFile("mastery/" + stringID);
    }

    @Override
    public List<String> getToolTipsText() {
        List<String> tooltips = new ArrayList<>();
        tooltips.add("§" + color + Localization.translate("mastery", stringID) + " §0- " + mod.name);
        tooltips.add(" ");
        tooltips.add(Localization.translate("masterydesc", stringID));
        return tooltips;
    }

    public boolean hasMastery(PlayerData playerData) {
        return playerData.hasMasterySkill(id);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return masteryBuff();
    }

    @Override
    public SecondaryPassiveBuff getSecondaryBuff() {
        return secondaryMasteryBuff();
    }

    @Override
    public String buffsStringID() {
        return "masterybuff";
    }

    abstract public MasteryBuff masteryBuff();

    public SecondaryMasteryBuff secondaryMasteryBuff() {
        return null;
    }

    public void giveMasteryBuff(Mob target) {
        String buffStringID = getBuffStringID();
        if (BuffRegistry.getBuff(buffStringID) == null) return;

        ActiveBuff ab = new ActiveBuff(BuffRegistry.getBuff(buffStringID), target, 1000, null);
        target.buffManager.addBuff(ab, target.isServer());
    }
}
