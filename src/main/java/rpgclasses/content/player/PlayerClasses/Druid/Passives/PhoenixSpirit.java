package rpgclasses.content.player.PlayerClasses.Druid.Passives;

import necesse.entity.mobs.MobBeforeDamageOverTimeTakenEvent;
import necesse.entity.mobs.MobBeforeHitCalculatedEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.level.maps.Level;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleTranformationActiveSkill;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.packets.UpdateClientEquippedActiveSkillsPacket;

public class PhoenixSpirit extends SimpleBuffPassive {
    public PhoenixSpirit(int levelMax, int requiredClassLevel) {
        super("phoenixspirit", "#ff3300", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {

            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
                int level = getLevel(activeBuff);
                if (level >= 4) {
                    this.isVisible = false;
                    eventSubscriber.subscribeEvent(MobBeforeDamageOverTimeTakenEvent.class, (event) -> {
                        if (this.runLogic(activeBuff, level, event.getExpectedHealth())) {
                            event.prevent();
                        }
                    });
                }
            }

            @Override
            public void onBeforeHitCalculated(ActiveBuff activeBuff, MobBeforeHitCalculatedEvent event) {
                super.onBeforeHitCalculated(activeBuff, event);
                int level = getLevel(activeBuff);
                if (level >= 4) {
                    if (this.runLogic(activeBuff, level, event.getExpectedHealth())) {
                        event.prevent();
                    }
                }

            }

            private boolean runLogic(ActiveBuff activeBuff, int skillLevel, int expectedHealth) {
                Level level = activeBuff.owner.getLevel();
                if (level.isServer() && expectedHealth <= 0 && activeBuff.owner.isPlayer) {
                    PlayerMob player = (PlayerMob) activeBuff.owner;
                    PlayerData playerData = PlayerDataList.getPlayerData(player);
                    if (playerData.getInUseActiveSkill() instanceof SimpleTranformationActiveSkill) {
                        int maxNumber = skillLevel / 4;
                        int inCooldownNumber = 0;
                        for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                            if (equippedActiveSkill.getActiveSkill() instanceof SimpleTranformationActiveSkill && equippedActiveSkill.getCooldownLeft(player.getTime()) > 60000)
                                inCooldownNumber++;
                        }
                        if (inCooldownNumber < maxNumber) {
                            player.dismount();
                            player.setHealth((int) (player.getMaxHealth() * (0.04F + 0.08F * skillLevel)));
                            playerData.getInUseActiveSkillSlot().startCustomCooldown(playerData, player.getTime(), 3600_000);
                            player.getServer().network.sendToAllClients(new UpdateClientEquippedActiveSkillsPacket(playerData));
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }
}
