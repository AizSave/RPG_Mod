package rpgclasses.content.player;

import aphorea.registry.AphModifiers;
import necesse.engine.localization.Localization;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.modifiers.Modifier;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.FloatModifierBuff;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.FloatPercentModifierBuff;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.IntModifierBuff;
import rpgclasses.content.player.SkillsLogic.ModifierBuffs.ModifierBuff;
import rpgclasses.content.player.SkillsLogic.Passives.BasicPassive;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGModifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Attribute extends BasicPassive {
    public static Map<String, Attribute> attributes = new HashMap<>();
    public static List<Attribute> attributesList = new ArrayList<>();

    public static void registerCore() {
        registerAttribute(new Attribute(
                "endurance", "#4682B4",
                RPGModifiers.ENDURANCE_ATTR_FLAT,
                new IntModifierBuff(BuffModifiers.MAX_HEALTH_FLAT, 3),
                new FloatPercentModifierBuff(BuffModifiers.ARMOR, 0.02F),
                new FloatModifierBuff(BuffModifiers.COMBAT_HEALTH_REGEN_FLAT, 0.15F)
        ));
        registerAttribute(new Attribute(
                "speed", "#FFD60A",
                RPGModifiers.SPEED_ATTR_FLAT,
                new String[]{"dodgechance"},
                new FloatPercentModifierBuff(BuffModifiers.SPEED, 0.02F),
                new FloatPercentModifierBuff(RPGModifiers.DODGE_CHANCE, 0.005F)
        ));
        registerAttribute(new Attribute(
                "strength", "#D94F30",
                RPGModifiers.STRENGTH_ATTR_FLAT,
                new FloatPercentModifierBuff(BuffModifiers.MELEE_DAMAGE, 0.01F),
                new FloatPercentModifierBuff(BuffModifiers.RANGED_DAMAGE, 0.01F),
                new IntModifierBuff(BuffModifiers.MAX_HEALTH_FLAT, 2)
        ));
        registerAttribute(new Attribute(
                "intelligence", "#9D4EDD",
                RPGModifiers.INTELLIGENCE_ATTR_FLAT,
                new FloatPercentModifierBuff(BuffModifiers.MAGIC_DAMAGE, 0.01F),
                new FloatPercentModifierBuff(BuffModifiers.SUMMON_DAMAGE, 0.01F),
                new FloatPercentModifierBuff(BuffModifiers.COMBAT_MANA_REGEN, 0.05F)
        ));
        registerAttribute(new Attribute(
                "grace", "#2ECC71",
                RPGModifiers.GRACE_ATTR_FLAT,
                new String[]{"holydamage"},
                new FloatPercentModifierBuff(AphModifiers.MAGIC_HEALING, 0.02F),
                new FloatPercentModifierBuff(BuffModifiers.LIFE_ESSENCE_GAIN, 0.02F),
                new FloatPercentModifierBuff(RPGModifiers.HOLY_DAMAGE, 0.01F)
        ));
    }

    public static void registerAttribute(Attribute attribute) {
        attributes.put(attribute.stringID, attribute);
        attributesList.add(attribute);
    }

    public static Attribute getRandom() {
        return GameRandom.globalRandom.getOneOf(attributesList);
    }

    public Modifier<Integer> ownModifier;
    public String[] extraTooltips;
    public LoadedMod mod;

    public Attribute(String stringID, String color, Modifier<Integer> ownModifier, String[] extraTooltips, ModifierBuff<?>... modifierBuffs) {
        super(stringID, color, 0, 0, modifierBuffs);
        this.id = attributes.size();
        this.ownModifier = ownModifier;
        this.extraTooltips = extraTooltips;
        this.mod = LoadedMod.getRunningMod();
    }

    public Attribute(String stringID, String color, Modifier<Integer> ownModifier, ModifierBuff<?>... modifierBuffs) {
        this(stringID, color, ownModifier, new String[0], modifierBuffs);
    }

    public void initResources() {
        texture = GameTexture.fromFile("attributes/" + stringID);
    }

    public List<String> getToolTipsText() {
        List<String> tooltips = new ArrayList<>();
        tooltips.add("§" + color + Localization.translate("attributes", stringID) + " §0- " + mod.name);
        tooltips.add(" ");
        tooltips.add(Localization.translate("ui", "eachlevel"));
        tooltips.add(" ");
        for (ModifierBuff<?> attributeModifier : attributeModifiers) {
            tooltips.add(attributeModifier.getTooltip());
        }
        return tooltips;
    }

    public float getLevel(PlayerData playerData, PlayerMob player) {
        return playerData.getAttribute(id, player);
    }

}
