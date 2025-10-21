package rpgclasses;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.TileRegistry;
import necesse.level.gameTile.GameTile;
import necesse.level.maps.biomes.Biome;
import rpgclasses.content.MobClass;
import rpgclasses.content.player.Attribute;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.content.player.SkillsLogic.Skill;
import rpgclasses.registry.*;
import rpgclasses.settings.RPGSettings;

@ModEntry
public class RPGMod {

    public static String currentVersion = "v0.6.19";

    static {
        new RPGModifiers();
    }

    public void preInit() {
    }

    public void init() {
        // Mod content
        MobClass.registerCore();

        Attribute.registerCore();

        Mastery.registerCore();

        Mastery.masterySkillsList.forEach(SimpleBuffPassive::registry);

        PlayerClass.registerCore();

        PlayerClass.classesList.forEach(playerClass -> playerClass.passivesList.each(Passive::registry));
        PlayerClass.classesList.forEach(playerClass -> playerClass.activeSkillsList.each(Skill::registry));

        // Containers
        RPGContainers.registerCore();

        // Controls
        RPGControls.registerCore();

        // Packets
        RPGPackets.registerCore();

        // Commands
        RPGCommands.registerCore();

        // Level Events
        RPGLevelEvents.registerCore();

        // Damage Type
        RPGDamageType.registerCore();

        // Items
        RPGItems.registerCore();

        // Buffs
        RPGBuffs.registerCore();

        // Mobs
        RPGMobs.registerCore();

        // Projectiles
        RPGProjectiles.registerCore();

        // Enchantments
        RPGEnchantments.registerCore();

    }

    public void postInit() {

        // Loot Tables
        RPGLootTables.modifyLootTables();

        // Recipes
        RPGRecipes.initRecipes();

        // Other
        for (GameTile tile : TileRegistry.getTiles()) {
            String tileStringID = tile.getStringID();
            if (tileStringID.contains("grass")) RPGTiles.grassTiles.add(tileStringID);
        }

    }

    public void initResources() {
        RPGResources.initResources();
    }

    public ModSettings initSettings() {
        return RPGSettings.init();
    }

}
