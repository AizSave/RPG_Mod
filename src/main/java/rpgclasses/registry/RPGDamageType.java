package rpgclasses.registry;

import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.modifiers.Modifier;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.gameDamageType.DamageType;
import necesse.inventory.item.DoubleItemStatTip;
import necesse.inventory.item.LocalMessageDoubleItemStatTip;

public class RPGDamageType {
    public static DamageType HOLY = new HolyDamageType();
    public static DamageType THROWING = new ThrowingDamageType();

    public static void registerCore() {
        DamageTypeRegistry.registerDamageType("holy", HOLY);
        DamageTypeRegistry.registerDamageType("throwing", THROWING);
    }

    private static class HolyDamageType extends DamageType {

        public HolyDamageType() {
        }

        public Modifier<Float> getBuffDamageModifier() {
            return RPGModifiers.HOLY_DAMAGE;
        }

        public Modifier<Float> getBuffAttackSpeedModifier(Attacker attacker) {
            return null;
        }

        public Modifier<Float> getBuffCritChanceModifier() {
            return RPGModifiers.HOLY_CRIT_CHANCE;
        }

        public Modifier<Float> getBuffCritDamageModifier() {
            return RPGModifiers.HOLY_CRIT_DAMAGE;
        }

        public GameMessage getStatsText() {
            return new LocalMessage("stats", "holy_damage");
        }

        public DoubleItemStatTip getDamageTip(int damage) {
            return new LocalMessageDoubleItemStatTip("itemtooltip", "holydamagetip", "value", damage, 0);
        }

        public String getSteamStatKey() {
            return "holy_damage_dealt";
        }
    }

    private static class ThrowingDamageType extends DamageType {

        public ThrowingDamageType() {
        }

        public Modifier<Float> getBuffDamageModifier() {
            return RPGModifiers.THROWING_DAMAGE;
        }

        public Modifier<Float> getBuffAttackSpeedModifier(Attacker attacker) {
            return RPGModifiers.THROWING_ATTACK_SPEED;
        }

        public Modifier<Float> getBuffCritChanceModifier() {
            return RPGModifiers.THROWING_CRIT_CHANCE;
        }

        public Modifier<Float> getBuffCritDamageModifier() {
            return RPGModifiers.THROWING_CRIT_DAMAGE;
        }

        public GameMessage getStatsText() {
            return new LocalMessage("stats", "throwing_damage");
        }

        public DoubleItemStatTip getDamageTip(int damage) {
            return new LocalMessageDoubleItemStatTip("itemtooltip", "throwingdamagetip", "value", damage, 0);
        }

        public String getSteamStatKey() {
            return "throwing_damage_dealt";
        }
    }


}
