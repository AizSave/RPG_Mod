package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.engine.registries.MobRegistry;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobHealthChangeEvent;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobBeforeHitCalculatedEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.itemAttacker.FollowPosition;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.summons.damageable.DamageableFollowingMob;

public class DarkEminence extends Mastery {

    public DarkEminence(String stringID, String color) {
        super(stringID, color);
    }

    @Override
    public MasteryBuff masteryBuff() {
        return new MasteryBuff() {
            @Override
            public void onBeforeHitCalculated(ActiveBuff buff, MobBeforeHitCalculatedEvent event) {
                super.onBeforeHitCalculated(buff, event);
                if (buff.owner.isServer() && buff.owner.isPlayer) {
                    Mob attacker = event.attacker.getAttackOwner();
                    if (attacker != null) {
                        PlayerMob player = (PlayerMob) buff.owner;
                        int maxDamage = (int) (player.getMaxHealth() * 0.15F);
                        int excess = event.damage - maxDamage;
                        if (excess > 0) {
                            event.prevent();
                            event.showDamageTip = false;
                            event.playHitSound = false;

                            player.getLevel().entityManager.addLevelEvent((new MobHealthChangeEvent(player, -maxDamage)));

                            DamageableFollowingMob mob = (DamageableFollowingMob) MobRegistry.getMob("necromancerskeletonwarrior", player.getLevel());
                            player.serverFollowersManager.addFollower(stringID, mob, FollowPosition.WALK_CLOSE, null, 1, Integer.MAX_VALUE, null, true);

                            PlayerData playerData = PlayerDataList.getPlayerData(player);

                            mob.updateStats(player, playerData);

                            player.getLevel().entityManager.addMob(mob, player.x, player.y);

                            player.getLevel().entityManager.addLevelEvent((new MobHealthChangeEvent(mob, -excess)));
                        }
                    }
                }
            }
        };
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"necromancerskeletonwarrior"};
    }
}