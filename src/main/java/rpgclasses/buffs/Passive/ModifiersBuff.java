package rpgclasses.buffs.Passive;

import necesse.engine.modifiers.ModifierValue;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Interfaces.DodgeClassBuff;
import rpgclasses.buffs.MarkedBuff;
import rpgclasses.content.player.Attribute;
import rpgclasses.content.player.SkillsLogic.Passives.BasicPassive;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.data.PlayerClassData;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.packets.ShowDodgePacket;
import rpgclasses.registry.RPGModifiers;

import java.util.List;
import java.util.stream.Collectors;

public class ModifiersBuff extends PassiveBuff {
    public ModifiersBuff() {
    }

    public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
        if (activeBuff.owner.isPlayer) {
            PlayerMob player = (PlayerMob) activeBuff.owner;

            new ModifierValue<>(RPGModifiers.THROWING_DAMAGE, (player.buffManager.getModifier(BuffModifiers.MELEE_DAMAGE) + player.buffManager.getModifier(BuffModifiers.RANGED_DAMAGE)) * 0.5F).apply(activeBuff);
            new ModifierValue<>(RPGModifiers.THROWING_ATTACK_SPEED, (player.buffManager.getModifier(BuffModifiers.MELEE_ATTACK_SPEED) + player.buffManager.getModifier(BuffModifiers.RANGED_ATTACK_SPEED)) * 0.5F).apply(activeBuff);
            new ModifierValue<>(RPGModifiers.THROWING_CRIT_CHANCE, (player.buffManager.getModifier(BuffModifiers.MELEE_CRIT_CHANCE) + player.buffManager.getModifier(BuffModifiers.RANGED_CRIT_CHANCE)) * 0.5F).apply(activeBuff);
            new ModifierValue<>(RPGModifiers.THROWING_CRIT_DAMAGE, (player.buffManager.getModifier(BuffModifiers.MELEE_CRIT_CHANCE) + player.buffManager.getModifier(BuffModifiers.RANGED_CRIT_CHANCE)) * 0.5F).apply(activeBuff);

            PlayerData playerData = PlayerDataList.getPlayerData(player);
            if (playerData != null) {
                for (Attribute attribute : Attribute.attributesList) {
                    float attributeLevel = attribute.getLevel(playerData, player);
                    if (attributeLevel > 0) {
                        attribute.applyBuff(activeBuff, attributeLevel);
                    }
                }

                for (PlayerClassData classesDatum : playerData.getClassesData()) {
                    for (int i = 0; i < classesDatum.getPassiveLevels().length; i++) {
                        int passiveLevel = classesDatum.getPassiveLevels()[i];
                        if (passiveLevel > 0) {
                            Passive passive = classesDatum.playerClass.passivesList.get(i);
                            if (passive instanceof BasicPassive) {
                                BasicPassive basicPassive = (BasicPassive) passive;
                                if (!basicPassive.onlyTransformed) basicPassive.applyBuff(activeBuff, passiveLevel);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
        super.onHasAttacked(activeBuff, event);
        if (event.damage > 0 && !event.wasPrevented) {
            if (event.attacker.getAttackOwner() == activeBuff.owner) {
                float focusChance = activeBuff.owner.buffManager.getModifier(RPGModifiers.FOCUS_CHANCE);
                if (focusChance >= 1F || (focusChance > 0 && GameRandom.globalRandom.getChance(focusChance))) {
                    MarkedBuff.markMob(((PlayerMob) activeBuff.owner), event.target, 5000);

                }
            }
        }
    }

    @Override
    public void onBeforeHit(ActiveBuff activeBuff, MobBeforeHitEvent event) {
        super.onBeforeHit(activeBuff, event);
        if (preventDamage(activeBuff, event)) {
            event.prevent();
            event.showDamageTip = false;
            event.playHitSound = false;
        }
    }

    private boolean preventDamage(ActiveBuff activeBuff, MobBeforeHitEvent event) {
        String prevent = this.shouldPreventDamage(activeBuff, event);
        if (prevent != null && activeBuff.owner.isServer()) {
            if (prevent.equals("dodge")) {
                activeBuff.owner.getServer().network.sendToClientsAtEntireLevel(new ShowDodgePacket(activeBuff.owner.getX(), activeBuff.owner.getY()), activeBuff.owner.getLevel());
                final List<ActiveBuff> dodgeClassBuffs = activeBuff.owner.buffManager.getBuffs().values().stream()
                        .filter(ab -> ab.buff instanceof DodgeClassBuff)
                        .collect(Collectors.toList());
                for (ActiveBuff ab : dodgeClassBuffs) {
                    ((DodgeClassBuff) ab.buff).onDodge(ab, event);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public String shouldPreventDamage(ActiveBuff activeBuff, MobBeforeHitEvent event) {
        float reduceChance = Math.min(1.0F, activeBuff.owner.buffManager.getModifier(BuffModifiers.SPEED));
        if (event.attacker.getAttackOwner() != null && GameRandom.globalRandom.getChance(activeBuff.owner.buffManager.getModifier(RPGModifiers.DODGE_CHANCE) * reduceChance)) {
            return "dodge";
        }
        return null;
    }
}