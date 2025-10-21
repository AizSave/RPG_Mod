package rpgclasses.registry;

import necesse.engine.modifiers.Modifier;
import necesse.engine.modifiers.ModifierLimiter;
import necesse.entity.mobs.buffs.BuffModifiers;

public class RPGModifiers {
    public static final Modifier<Integer> ENDURANCE_ATTR_FLAT;
    public static final Modifier<Integer> SPEED_ATTR_FLAT;
    public static final Modifier<Integer> STRENGTH_ATTR_FLAT;
    public static final Modifier<Integer> INTELLIGENCE_ATTR_FLAT;
    public static final Modifier<Integer> GRACE_ATTR_FLAT;

    public static final Modifier<Float> DODGE_CHANCE;
    public static final Modifier<Float> RANGED_WEAPONS_ZOOM;
    public static final Modifier<Float> RANGED_WEAPONS_RANGE;
    public static final Modifier<Float> MOB_DETECTION_RANGE;
    public static final Modifier<Float> FOCUS_DAMAGE;
    public static final Modifier<Float> FOCUS_CHANCE;
    public static final Modifier<Float> IGNITE_DAMAGE;
    public static final Modifier<Float> TRANSFORMATION_DELAY;
    public static final Modifier<Float> CAST_TIME;

    public static final Modifier<Float> HOLY_DAMAGE;
    public static final Modifier<Float> HOLY_CRIT_CHANCE;
    public static final Modifier<Float> HOLY_CRIT_DAMAGE;

    public static final Modifier<Float> THROWING_DAMAGE;
    public static final Modifier<Float> THROWING_ATTACK_SPEED;
    public static final Modifier<Float> THROWING_CRIT_CHANCE;
    public static final Modifier<Float> THROWING_CRIT_DAMAGE;

    public static final Modifier<Boolean> NO_SKILLS;

    static {
        ENDURANCE_ATTR_FLAT = new Modifier<>(BuffModifiers.LIST, "enduranceattrflat", 0, 0, Modifier.INT_ADD_APPEND, Modifier.NORMAL_FLAT_INT_PARSER("enduranceattrflat"), ModifierLimiter.NORMAL_FLAT_INT_LIMITER("enduranceattrflat"));
        SPEED_ATTR_FLAT = new Modifier<>(BuffModifiers.LIST, "speedattrflat", 0, 0, Modifier.INT_ADD_APPEND, Modifier.NORMAL_FLAT_INT_PARSER("speedattrflat"), ModifierLimiter.NORMAL_FLAT_INT_LIMITER("speedattrflat"));
        STRENGTH_ATTR_FLAT = new Modifier<>(BuffModifiers.LIST, "strengthattrflat", 0, 0, Modifier.INT_ADD_APPEND, Modifier.NORMAL_FLAT_INT_PARSER("strengthattrflat"), ModifierLimiter.NORMAL_FLAT_INT_LIMITER("strengthattrflat"));
        INTELLIGENCE_ATTR_FLAT = new Modifier<>(BuffModifiers.LIST, "intelligenceattrflat", 0, 0, Modifier.INT_ADD_APPEND, Modifier.NORMAL_FLAT_INT_PARSER("intelligenceattrflat"), ModifierLimiter.NORMAL_FLAT_INT_LIMITER("intelligenceattrflat"));
        GRACE_ATTR_FLAT = new Modifier<>(BuffModifiers.LIST, "graceattrflat", 0, 0, Modifier.INT_ADD_APPEND, Modifier.NORMAL_FLAT_INT_PARSER("graceattrflat"), ModifierLimiter.NORMAL_FLAT_INT_LIMITER("graceattrflat"));

        DODGE_CHANCE = new Modifier<>(BuffModifiers.LIST, "dodgechance", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> v < 0.0F ? 0.0F : (v > 0.8F ? 0.8F : v), Modifier.NORMAL_PERC_PARSER("dodgechance"), ModifierLimiter.NORMAL_PERC_LIMITER("dodgechance"));
        RANGED_WEAPONS_ZOOM = new Modifier<>(BuffModifiers.LIST, "rangedweaponszoom", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, Modifier.NORMAL_FLAT_FLOAT_PARSER("rangedweaponszoom"), ModifierLimiter.NORMAL_FLAT_FLOAT_LIMITER("rangedweaponszoom"));
        RANGED_WEAPONS_RANGE = new Modifier<>(BuffModifiers.LIST, "rangedweaponsrange", 1.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> Math.max(0.0F, v), Modifier.NORMAL_PERC_PARSER("rangedweaponsrange"), ModifierLimiter.NORMAL_PERC_LIMITER("rangedweaponsrange"));
        MOB_DETECTION_RANGE = new Modifier<>(BuffModifiers.LIST, "mobdetectionrange", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, Modifier.NORMAL_FLAT_FLOAT_PARSER("mobdetectionrange"), ModifierLimiter.NORMAL_FLAT_FLOAT_LIMITER("mobdetectionrange"));
        FOCUS_DAMAGE = new Modifier<>(BuffModifiers.LIST, "focusdamage", 0.2F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> Math.max(0.0F, v), Modifier.NORMAL_PERC_PARSER("focusdamage"), ModifierLimiter.NORMAL_PERC_LIMITER("focusdamage"));
        FOCUS_CHANCE = new Modifier<>(BuffModifiers.LIST, "focuschance", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> Math.max(0.0F, v), Modifier.NORMAL_PERC_PARSER("focuschance"), ModifierLimiter.NORMAL_PERC_LIMITER("focuschance"));
        IGNITE_DAMAGE = new Modifier<>(BuffModifiers.LIST, "ignitedamage", 0.2F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> Math.max(0.0F, v), Modifier.NORMAL_PERC_PARSER("ignitedamage"), ModifierLimiter.NORMAL_PERC_LIMITER("ignitedamage"));
        TRANSFORMATION_DELAY = new Modifier<>(BuffModifiers.LIST, "transformationdelay", 1.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> Math.max(0.0F, v), Modifier.INVERSE_PERC_PARSER("transformationdelay"), ModifierLimiter.INVERSE_PERC_LIMITER("transformationdelay"));
        CAST_TIME = new Modifier<>(BuffModifiers.LIST, "casttime", 1.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> Math.max(0.0F, v), Modifier.INVERSE_PERC_PARSER("casttime"), ModifierLimiter.INVERSE_PERC_LIMITER("casttime"));

        HOLY_DAMAGE = new Modifier<>(BuffModifiers.LIST, "holydamage", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> v, Modifier.NORMAL_PERC_PARSER("holydamage"), ModifierLimiter.NORMAL_PERC_LIMITER("holydamage"));
        HOLY_CRIT_CHANCE = new Modifier<>(BuffModifiers.LIST, "holycritchance", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> v, Modifier.NORMAL_PERC_PARSER("holycritchance"), ModifierLimiter.NORMAL_PERC_LIMITER("holycritchance"));
        HOLY_CRIT_DAMAGE = new Modifier<>(BuffModifiers.LIST, "holycritdamage", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> v, Modifier.NORMAL_PERC_PARSER("holycritdamage"), ModifierLimiter.NORMAL_PERC_LIMITER("holycritdamage"));

        THROWING_DAMAGE = new Modifier<>(BuffModifiers.LIST, "throwingdamage", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> v, Modifier.NORMAL_PERC_PARSER("throwingdamage"), ModifierLimiter.NORMAL_PERC_LIMITER("throwingdamage"));
        THROWING_ATTACK_SPEED = new Modifier<>(BuffModifiers.LIST, "throwingattackspeed", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> v, Modifier.NORMAL_PERC_PARSER("throwingattackspeed"), ModifierLimiter.NORMAL_PERC_LIMITER("throwingdamage"));
        THROWING_CRIT_CHANCE = new Modifier<>(BuffModifiers.LIST, "throwingcritchance", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> v, Modifier.NORMAL_PERC_PARSER("throwingcritchance"), ModifierLimiter.NORMAL_PERC_LIMITER("throwingcritchance"));
        THROWING_CRIT_DAMAGE = new Modifier<>(BuffModifiers.LIST, "throwingcritdamage", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> v, Modifier.NORMAL_PERC_PARSER("throwingcritdamage"), ModifierLimiter.NORMAL_PERC_LIMITER("throwingcritdamage"));

        NO_SKILLS = new Modifier<>(BuffModifiers.LIST, "noskills", false, false, Modifier.OR_APPEND, Modifier.INVERSE_BOOL_PARSER("noskills"), null);
    }

}
