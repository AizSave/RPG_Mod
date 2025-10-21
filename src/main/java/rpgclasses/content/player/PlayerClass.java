package rpgclasses.content.player;

import necesse.engine.localization.Localization;
import necesse.engine.modLoader.LoadedMod;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTooltips.ListGameTooltips;
import rpgclasses.content.player.PlayerClasses.Cleric.ClericPlayerClass;
import rpgclasses.content.player.PlayerClasses.Druid.DruidPlayerClass;
import rpgclasses.content.player.PlayerClasses.Necromancer.NecromancerPlayerClass;
import rpgclasses.content.player.PlayerClasses.Ranger.RangerPlayerClass;
import rpgclasses.content.player.PlayerClasses.Warrior.WarriorPlayerClass;
import rpgclasses.content.player.PlayerClasses.Wizard.WizardPlayerClass;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.SkillsList;
import rpgclasses.data.PlayerData;
import rpgclasses.settings.RPGSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerClass {
    public static Map<String, PlayerClass> classes = new HashMap<>();
    public static List<PlayerClass> classesList = new ArrayList<>();

    public static void registerCore() {
        registerClass(new WarriorPlayerClass());
        registerClass(new RangerPlayerClass());
        registerClass(new WizardPlayerClass());
        registerClass(new NecromancerPlayerClass());
        registerClass(new ClericPlayerClass());
        registerClass(new UpcomingPlayerClass("rogue", "#8B0000"));
        registerClass(new UpcomingPlayerClass("warlock", "#B22222"));
        registerClass(new UpcomingPlayerClass("paladin", "#F1C40F"));
        registerClass(new DruidPlayerClass());
    }

    public static void registerClass(PlayerClass playerClass) {
        classes.put(playerClass.stringID, playerClass);
        classesList.add(playerClass);

        if (!(playerClass instanceof UpcomingPlayerClass)) RPGSettings.addClassSetting(playerClass);

        playerClass.activeSkillsList.each(
                activeSkill -> activeSkill.playerClass = playerClass
        );
        playerClass.passivesList.each(
                activeSkill -> activeSkill.playerClass = playerClass
        );
    }

    public final int id;
    public final String stringID;
    public final String color;
    public final SkillsList<ActiveSkill> activeSkillsList;
    public final SkillsList<Passive> passivesList;
    public GameTexture texture;
    public GameTexture textureDisabled;
    public LoadedMod mod;

    public PlayerClass(String stringID, String color) {
        this.id = classes.size();
        this.stringID = stringID;
        this.color = color;
        this.activeSkillsList = initActiveSkillsList();
        this.passivesList = initPassivesList();
        this.mod = LoadedMod.getRunningMod();
    }

    public void initResources() {
        texture = GameTexture.fromFile("classes/" + stringID);
        textureDisabled = GameTexture.fromFile("classes/" + stringID + "_disabled");
    }

    public ListGameTooltips getToolTips() {
        boolean enabled = isEnabled();
        ListGameTooltips tooltips = new ListGameTooltips();
        tooltips.add("§" + color + Localization.translate("classes", stringID) + " §0- " + mod.name);
        if (!enabled) {
            tooltips.add(" ");
            tooltips.add(Localization.translate("ui", "disabledclass"));
        }
        if (enabled) {
            tooltips.add(" ");
            tooltips.add(Localization.translate("ui", "clicktoopen"));
        }
        return tooltips;
    }

    public int getLevel(PlayerData playerData) {
        return playerData.getClassLevel(id);
    }

    public int totalPassivePoints(int level) {
        return level * 2;
    }

    public int totalActiveSkillPoints(int level) {
        return level;
    }

    public boolean isEnabled() {
        return RPGSettings.classEnabled(this);
    }

    protected SkillsList<ActiveSkill> initActiveSkillsList() {
        return new SkillsList<>();
    }

    protected SkillsList<Passive> initPassivesList() {
        return new SkillsList<>();
    }

    public GameTexture getTexture() {
        return isEnabled() ? texture : textureDisabled;
    }
}
