package rpgclasses.content.player.PlayerClasses.Ranger.Passives;

import necesse.engine.registries.MobRegistry;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.itemAttacker.FollowPosition;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.mobs.summons.passive.PassiveFollowingMob;

public class WolfCompanion extends SimpleBuffPassive {

    public WolfCompanion(int levelMax, int requiredClassLevel) {
        super("wolfcompanion", "#E6D9CC", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new WolfCompanionBuff(this, getBuffStringID());
    }

    public static class WolfCompanionBuff extends PrincipalPassiveBuff {
        public String buffStringID;
        public Passive passive;

        public WolfCompanionBuff(Passive passive, String buffStringID) {
            this.passive = passive;
            this.buffStringID = buffStringID;
        }

        @Override
        public void serverTick(ActiveBuff activeBuff) {
            super.serverTick(activeBuff);
            if (activeBuff.owner.isPlayer) {
                PlayerMob player = (PlayerMob) activeBuff.owner;
                if (player.serverFollowersManager.getFollowerCount(buffStringID) == 0) {
                    PassiveFollowingMob mob = (PassiveFollowingMob) MobRegistry.getMob("rangerwolf", activeBuff.owner.getLevel());
                    player.serverFollowersManager.addFollower(buffStringID, mob, FollowPosition.WALK_CLOSE, buffStringID, 1, 1, null, activeBuff.owner.isServer());
                    mob.setSkill(passive);
                    mob.getLevel().entityManager.addMob(mob, activeBuff.owner.x, activeBuff.owner.y);
                }
            }

        }
    }

}
