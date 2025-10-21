package rpgclasses.mobs.summons.passive;

import rpgclasses.data.PlayerClassData;
import rpgclasses.data.PlayerData;
import rpgclasses.mobs.summons.SkillFollowingMob;

abstract public class PassiveFollowingMob extends SkillFollowingMob {
    public PassiveFollowingMob(int health) {
        super(health);
    }

    public int getPassiveLevel() {
        return this.getPassiveLevel(getPlayerData());
    }

    public int getPassiveLevel(PlayerData playerData) {
        PlayerClassData classData = getClassData(playerData);
        if (classData == null) return 0;
        return classData.getPassiveLevels()[skill.id];
    }
}
