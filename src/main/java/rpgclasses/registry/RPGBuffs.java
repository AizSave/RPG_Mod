package rpgclasses.registry;

import aphorea.registry.AphBuffs;
import necesse.engine.registries.BuffRegistry;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import rpgclasses.buffs.*;
import rpgclasses.buffs.Passive.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RPGBuffs {

    public static List<String> debuffs = new ArrayList<>();

    static {
        debuffs.addAll(
                Arrays.stream(new String[]{
                        "tremorhappening",
                        "swampspores",

                        "onfire",
                        "ablaze",
                        "brokenarmor",
                        "spiderwebslow",
                        "webpotionslow",
                        "spidervenom",
                        "necroticpoison",
                        "necroticslow",
                        "ivypoison",
                        "widowpoison",
                        "chilled",
                        "freezing",
                        "frostburn",
                        "frostslow",
                        "snowcovered",
                        "snowcoveredslow",
                        "flourcoveredslow",
                        "haunted",
                        "emeraldpoison",
                        "spidercharmpoison",
                        "slimepoison",
                        "sandknifewound",
                        "crystallize",
                        "bloodgrimoiredebuff",
                        "slimegreatbowdebuff",
                        "crushingdarkness",
                        "netted",
                        "dryadhaunted",
                        "dryadpossessed",
                        "spirithaunted",
                        "spiritpossessed",
                        "stunneddamagetakenincreased",
                        "spiritcorrupted",

                        // AphoreaMod
                        "stunbuff",
                        "fallenstunbuff",
                        "stickybuff",
                        "honeyedbuff",
                        "cursedbuff",

                        // RPG Mod
                        "trappedbuff",
                        "markedbuff",
                        "darkcursebuff",
                        "magicpoisonbuff",
                        "ignitedbuff",
                        "constrainedbuff"
                }).collect(Collectors.toList())
        );
    }

    public static Buff TRAPPED;
    public static Buff MARKED;
    public static Buff DARK_CURSE;
    public static Buff MAGIC_POISON;
    public static Buff IGNITED;
    public static Buff CONSTRAINED;
    public static Buff TRANSFORMING;
    public static Buff AGGRESSIVE_BEES;
    public static Buff TARGET_RANGE_TO_100;

    public static class PASSIVES {
        public static ModifiersBuff MODIFIERS;
        public static HolyDamageDealtBuff HOLY_DAMAGE;
        public static OverLevelBuff OVER_LEVEL;
        public static GrabbedObjectBuff GRABBED_OBJECT;
        public static TransformedBuff TRANSFORMED;
    }

    public static void registerCore() {
        BuffRegistry.registerBuff("trappedbuff", TRAPPED = new TrappedBuff());
        BuffRegistry.registerBuff("markedbuff", MARKED = new MarkedBuff());
        BuffRegistry.registerBuff("darkcursebuff", DARK_CURSE = new DarkCurseBuff());
        BuffRegistry.registerBuff("magicpoisonbuff", MAGIC_POISON = new MagicPoisonBuff());
        BuffRegistry.registerBuff("ignitedbuff", IGNITED = new IgnitedBuff());
        BuffRegistry.registerBuff("constrainedbuff", CONSTRAINED = new ConstrainedBuff());
        BuffRegistry.registerBuff("transformingbuff", TRANSFORMING = new TransformingBuff());
        BuffRegistry.registerBuff("aggresivebeesbuff", AGGRESSIVE_BEES = new SimpleBuff());
        BuffRegistry.registerBuff("targetrangeto100", TARGET_RANGE_TO_100 = new TargetRangeTo100Buff());

        // Passive Buffs
        BuffRegistry.registerBuff("modifiersbuff", PASSIVES.MODIFIERS = new ModifiersBuff());
        BuffRegistry.registerBuff("holydamagedealtbuff", PASSIVES.HOLY_DAMAGE = new HolyDamageDealtBuff());
        BuffRegistry.registerBuff("overlevelbuff", PASSIVES.OVER_LEVEL = new OverLevelBuff());
        BuffRegistry.registerBuff("grabbedobjectbuff", PASSIVES.GRABBED_OBJECT = new GrabbedObjectBuff());
        BuffRegistry.registerBuff("transformedbuff", PASSIVES.TRANSFORMED = new TransformedBuff());

    }

    public static void applyStop(Mob target, float duration) {
        applyStop(target, (int) (duration * 1000));
    }

    public static void applyStop(Mob target, int duration) {
        ActiveBuff ab = new ActiveBuff(AphBuffs.STOP, target, duration, null);
        target.buffManager.addBuff(ab, true);
    }

    public static void applyStun(Mob target, float duration) {
        applyStun(target, (int) (duration * 1000));
    }

    public static void applyStun(Mob target, int duration) {
        ActiveBuff ab = new ActiveBuff(AphBuffs.STUN, target, duration, null);
        target.buffManager.addBuff(ab, true);
    }

    public static void purify(Mob target, boolean updatePacket) {
        List<String> buffsToRemove = new ArrayList<>();

        for (ActiveBuff activeBuff : target.buffManager.getBuffs().values()) {
            String buffStringID = activeBuff.buff.getStringID();
            if (debuffs.contains(buffStringID)) {
                buffsToRemove.add(buffStringID);
            }
        }

        for (String id : buffsToRemove) {
            target.buffManager.removeBuff(id, updatePacket);
        }
    }

}