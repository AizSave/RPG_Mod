package rpgclasses.content;


import necesse.engine.localization.Localization;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.MobClasses.*;
import rpgclasses.data.MobData;
import rpgclasses.settings.RPGSettings;

import java.util.*;

public class MobClass {
    public static Map<String, MobClass> allClasses = new HashMap<>();
    public static ArrayList<MobClass> allClassesList = new ArrayList<>();

    public static MobClass bossClass;
    public static List<MobClass> basicClasses = new ArrayList<>();
    public static List<MobClass> uncommonClasses = new ArrayList<>();
    public static List<MobClass> rareClasses = new ArrayList<>();
    public static List<MobClass> mythicClasses = new ArrayList<>();

    public static void registerCore() {
        registerBossClass(new MobClass("boss", "0", 0.04F, RPGSettings.bossKillBonus(), BossMobClassBuff.class));
        registerMobClass(basicClasses, new MobClass("warrior", "#993333", 0.02F, WarriorMobClassBuff.class));
        registerMobClass(basicClasses, new MobClass("tank", "#666666", 0.02F, TankMobClassBuff.class));
        registerMobClass(basicClasses, new MobClass("runner", "#339966", 0.02F, RunnerMobClassBuff.class));
        registerMobClass(uncommonClasses, new MobClass("healer", "#00ff00", 0.02F, 1.2F, HealerMobClassBuff.class));
        registerMobClass(uncommonClasses, new MobClass("explosive", "#ff0000", 0.02F, 1.2F, ExplosiveMobClassBuff.class));
        registerMobClass(uncommonClasses, new MobClass("glacial", "#00ffff", 0.02F, 1.2F, GlacialMobClassBuff.class));
        registerMobClass(rareClasses, new MobClass("flash", "#ffff00", 0.02F, 2F, FlashMobClassBuff.class));
        registerMobClass(rareClasses, new MobClass("dark", "#000000", 0.02F, 2F, DarkMobClassBuff.class));
        registerMobClass(mythicClasses, new MobClass("legend", "#ff6600", 0.02F, 5F, LegendMobClassBuff.class));
    }

    public static void registerBossClass(MobClass mobClass) {
        bossClass = mobClass;
        registerMobClass(mobClass);
    }

    public static void registerMobClass(List<MobClass> classList, MobClass mobClass) {
        classList.add(mobClass);
        registerMobClass(mobClass);
    }

    public static void registerMobClass(MobClass mobClass) {
        allClasses.put(mobClass.stringID, mobClass);
        allClassesList.add(mobClass);
        try {
            mobClass.buff = BuffRegistry.registerBuff(mobClass.buffStringID(), mobClass.classBuff.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public static MobClass getRandomClass(GameRandom random) {
        List<MobClass> list;

        int randomN = random.getIntBetween(0, 999);
        if (randomN < 5) {
            list = mythicClasses;
        } else if (randomN < 55) {
            list = rareClasses;
        } else if (randomN < 205) {
            list = uncommonClasses;
        } else {
            list = basicClasses;
        }

        return getRandomClass(random, list);
    }

    public static MobClass getRandomClass(GameRandom random, List<MobClass> list) {
        return list.get(random.getIntBetween(0, list.size() - 1));
    }

    public final int id;
    public final String stringID;
    public final String color;
    public final float healthModPerLevel;
    public final float expMod;
    public final Class<? extends MobClassBuff> classBuff;
    public MobClassBuff buff;

    public MobClass(String stringID, String color, float healthModPerLevel, float expMod, Class<? extends MobClassBuff> classBuff) {
        this.id = allClasses.size();
        this.stringID = stringID;
        this.color = color;
        this.healthModPerLevel = healthModPerLevel;
        this.expMod = expMod;
        this.classBuff = classBuff;
    }

    public MobClass(String stringID, String color, float healthModPerLevel, Class<? extends MobClassBuff> classBuff) {
        this(stringID, color, healthModPerLevel, 1, classBuff);
    }

    public String getName() {
        return Localization.translate("mobclass", stringID);
    }

    public void initBuffs(Mob mob, int classLevel) {
        int maxHealth = (int) (mob.getMaxHealth() * (1F + healthModPerLevel * MobData.levelScaling(classLevel)) * (1 + buff.healthBoost()));
        mob.setMaxHealth(maxHealth);
        mob.setHealthHidden(maxHealth);

        ActiveBuff ab = new ActiveBuff(buff, mob, 3600F, null);
        MobClassBuff.setMobLevel(ab, classLevel);
        mob.buffManager.addBuff(ab, false);
    }

    public String buffStringID() {
        return stringID + "mobclassbuff";
    }

    public boolean is(String stringID) {
        return Objects.equals(this.stringID, stringID);
    }
}
