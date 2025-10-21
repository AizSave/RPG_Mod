package rpgclasses.mobs.summons.damageable.necrotic;

import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.levelevents.NecroticExplosionLevelEvent;
import rpgclasses.mobs.summons.damageable.DamageableFollowingMob;

abstract public class NecroticFollowingMob extends DamageableFollowingMob {
    public NecroticFollowingMob(int health) {
        super(health);
    }

    @Override
    public void remove(float knockbackX, float knockbackY, Attacker attacker, boolean isDeath) {
        if (isServer() && isDeath) {
            Mob followingMob = this.getFollowingMob();
            if (followingMob != null) {
                PlayerMob player = (PlayerMob) followingMob;
                ActiveBuff ab = player.buffManager.getBuff("necroticremainspassivebuff");
                if (ab != null) {
                    int necroticRemainsLevel = ab.getGndData().getInt("skillLevel");
                    if (necroticRemainsLevel > 0) {
                        PlayerData playerData = PlayerDataList.getPlayerData(player);
                        getLevel().entityManager.events.add(new NecroticExplosionLevelEvent(x, y, 150, new GameDamage(0), player, 0.1F * necroticRemainsLevel * playerData.getIntelligence(player)));
                    }
                }
            }
        }
        super.remove(knockbackX, knockbackY, attacker, isDeath);
    }
}
