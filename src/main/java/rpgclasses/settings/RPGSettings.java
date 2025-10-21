package rpgclasses.settings;

import customsettingslib.components.settings.IntSetting;
import customsettingslib.settings.CustomModSettings;
import customsettingslib.settings.CustomModSettingsGetter;
import necesse.engine.modLoader.ModSettings;
import rpgclasses.content.player.PlayerClass;

public class RPGSettings {
    public static CustomModSettingsGetter settingsGetter;
    public static CustomModSettings modSettings;

    public static ModSettings init() {
        try {
            modSettings = new CustomModSettings()
                    .addParagraph("restart_text")
                    .addParagraph("client_text")
                    .addParagraph("server_text")

                    //////////////////////////////////////////////////////////////////////////
                    .addSpace(12) ////////////////////////////////////////////////////////////
                    //////////////////////////////////////////////////////////////////////////

                    .addTextSeparator("default_section")
                    .addParagraph("default_text")
                    .addBooleanSetting("showClassIcons", true)
                    .addBooleanSetting("twelveSkillSlots", false)

                    //////////////////////////////////////////////////////////////////////////
                    .addSpace(12) ////////////////////////////////////////////////////////////
                    //////////////////////////////////////////////////////////////////////////

                    .addTextSeparator("mechanics_section")

                    .addBooleanSetting("boomerangsDealThrowingDamage", true)

                    //////////////////////////////////////////////////////////////////////////
                    .addSpace(12) ////////////////////////////////////////////////////////////
                    //////////////////////////////////////////////////////////////////////////

                    .addTextSeparator("experience_section")

                    .addIntSetting("experienceMod", 100, 0, 10000, IntSetting.DisplayMode.INPUT)
                    .addParagraph("experienceMod_text", 0, 8)

                    .addIntSetting("firstKillBonus", 500, 0, 10000, IntSetting.DisplayMode.INPUT)
                    .addParagraph("firstKillBonus_text", 0, 8)

                    .addIntSetting("bossKillBonus", 500, 0, 10000, IntSetting.DisplayMode.INPUT)
                    .addParagraph("bossKillBonus_text", 0, 8)

                    .addIntSetting("startingExperience", 300, 0, 10000, IntSetting.DisplayMode.INPUT)
                    .addParagraph("startingExperience_text", 0, 8)

                    .addIntSetting("firstExperienceReq", 300, 0, 10000, IntSetting.DisplayMode.INPUT)
                    .addIntSetting("experienceReqInc", 60, 0, 10000, IntSetting.DisplayMode.INPUT)
                    .addIntSetting("squareExperienceReqInc", 30, 0, 10000, IntSetting.DisplayMode.INPUT)
                    .addIntSetting("cubeExperienceReqInc", 3, 0, 10000, IntSetting.DisplayMode.INPUT)
                    .addParagraph("experience_calc", 2, 0)

                    //////////////////////////////////////////////////////////////////////////
                    .addSpace(12) ////////////////////////////////////////////////////////////
                    //////////////////////////////////////////////////////////////////////////

                    .addTextSeparator("classes_section")
                    .addIntSetting("multiClass", 0, 0, 100, IntSetting.DisplayMode.INPUT)
                    .addParagraph("multiClass_text", 0, 8)

                    .addParagraph("classes_enabled", 0, 0)
            ;

            modSettings.addServerSettings(
                    "boomerangsDealThrowingDamage",

                    "experienceMod", "firstKillBonus", "bossKillBonus", "startingExperience",
                    "firstExperienceReq", "experienceReqInc", "squareExperienceReqInc", "cubeExperienceReqInc",
                    "multiClass"
            );

            RPGSettings.settingsGetter = modSettings.getGetter();
        } catch (NoClassDefFoundError err) {
            throw new RuntimeException(
                    "\n\nMissing dependency: \"Custom Settings Lib\"." +
                            "\nThe mod \"RPG Mod\" requires it to run." +
                            "\n\nPlease subscribe and enable \"Custom Settings Lib\" before launching this mod.\n\n"
            );
        }
        return modSettings;
    }

    public static void addClassSetting(PlayerClass playerClass) {
        RPGSettings.modSettings.addCustomSetting(new ClassEnabledSetting(playerClass.stringID, true));
        RPGSettings.modSettings.addServerSettings(playerClass.stringID + "_class");
    }

    public static float experienceMod() {
        return settingsGetter.getFloat("experienceMod", 2);
    }

    public static float firstKillBonus() {
        return settingsGetter.getFloat("firstKillBonus", 2);
    }

    public static float bossKillBonus() {
        return settingsGetter.getFloat("bossKillBonus", 2);
    }

    public static int startingExperience() {
        return settingsGetter.getInt("startingExperience");
    }

    public static int firstExperienceReq() {
        return settingsGetter.getInt("firstExperienceReq");
    }

    public static int experienceReqInc() {
        return settingsGetter.getInt("experienceReqInc");
    }

    public static int squareExperienceReqInc() {
        return settingsGetter.getInt("squareExperienceReqInc");
    }

    public static int cubeExperienceReqInc() {
        return settingsGetter.getInt("cubeExperienceReqInc");
    }

    public static int multiClass() {
        return settingsGetter.getInt("multiClass");
    }

    public static boolean classEnabled(PlayerClass playerClass) {
        return settingsGetter.getBoolean(playerClass.stringID + "_class");
    }
}
