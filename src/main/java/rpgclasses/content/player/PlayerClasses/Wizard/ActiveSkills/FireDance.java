package rpgclasses.content.player.PlayerClasses.Wizard.ActiveSkills;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.itemAttacker.FollowPosition;
import necesse.gfx.GameResources;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.summons.DancingFlameMob;

import java.util.ArrayList;

public class FireDance extends SimpleBuffActiveSkill {

    public FireDance(int levelMax, int requiredClassLevel) {
        super("firedance", "#6633cc", levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);

        for (int i = 0; i < 4; i++) {
            summonDancingFlame(player, playerData, activeSkillLevel, stringID);
        }
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.jingle, SoundEffect.effect(player.x, player.y));
        SoundManager.playSound(GameResources.firespell1, SoundEffect.effect(player.x, player.y));
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new DancingFlameBuff(this, stringID);
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 30 + activeSkillLevel * 6;
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 20000;
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"dancingflame", "manausage"};
    }

    public static class DancingFlameBuff extends ActiveSkillBuff {
        public ActiveSkill skill;
        public String skillStringID;

        public DancingFlameBuff(ActiveSkill skill, String skillStringID) {
            this.skill = skill;
            this.skillStringID = skillStringID;
        }

        @Override
        public void serverTick(ActiveBuff activeBuff) {
            int alreadySummoned = activeBuff.getGndData().getInt("alreadySummoned", 0);
            if (alreadySummoned < 3) {
                int skillTime = activeBuff.getGndData().getInt("skillTime", 50);
                skillTime += 50;
                activeBuff.getGndData().setInt("skillTime", skillTime);

                if (skillTime / 5000 > alreadySummoned) {
                    alreadySummoned++;
                    activeBuff.getGndData().setInt("alreadySummoned", alreadySummoned);

                    PlayerMob player = (PlayerMob) activeBuff.owner;
                    summonDancingFlame(player, PlayerDataList.getPlayerData(player), skill, skillStringID);
                }
            }
        }

        @Override
        public void onRemoved(ActiveBuff activeBuff) {
            ArrayList<Mob> mobs = new ArrayList<>();
            ((PlayerMob) activeBuff.owner).serverFollowersManager.streamFollowers()
                    .filter(m -> m.summonType.equals(skillStringID))
                    .forEach(m -> mobs.add(m.mob));
            for (Mob mob : mobs) {
                mob.remove();
            }
        }
    }

    public static void summonDancingFlame(PlayerMob player, PlayerData playerData, ActiveSkill activeSkill, String skillStringID) {
        summonDancingFlame(player, playerData, playerData.getClassesData()[activeSkill.playerClass.id].getActiveSkillLevels()[activeSkill.id], skillStringID);
    }

    public static void summonDancingFlame(PlayerMob player, PlayerData playerData, int activeSkillLevel, String skillStringID) {
        DancingFlameMob mob = (DancingFlameMob) MobRegistry.getMob("dancingflame", player.getLevel());
        player.serverFollowersManager.addFollower(skillStringID, mob, FollowPosition.FLYING_CIRCLE_FAST, null, 1, 7, null, true);
        mob.updateDamage(new GameDamage(DamageTypeRegistry.MAGIC, playerData.getLevel() * activeSkillLevel + playerData.getIntelligence(player) * activeSkillLevel));
        mob.setPurple();
        mob.getLevel().entityManager.addMob(mob, player.x, player.y);
    }
}
