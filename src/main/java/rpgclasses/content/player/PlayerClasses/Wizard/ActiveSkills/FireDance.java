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
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.CastBuffActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.summons.DancingFlameMob;

import java.util.ArrayList;

public class FireDance extends CastBuffActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(4),
            SkillParam.damageParam(3)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(30);
    }

    public FireDance(int levelMax, int requiredClassLevel) {
        super("firedance", "#6633cc", levelMax, requiredClassLevel);
    }

    @Override
    public int castTime() {
        return 2000;
    }

    @Override
    public void castedRunServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunServer(player, playerData, activeSkillLevel, seed);

        for (int i = 0; i < params[0].valueInt(); i++) {
            summonDancingFlame(player, playerData, activeSkillLevel, stringID);
        }
    }

    @Override
    public void castedRunClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunClient(player, playerData, activeSkillLevel, seed);
        SoundManager.playSound(GameResources.jingle, SoundEffect.effect(player.x, player.y));
        SoundManager.playSound(GameResources.firespell1, SoundEffect.effect(player.x, player.y));
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new DancingFlameBuff(this, stringID);
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 20000;
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 20000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"dancingflame"};
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
        mob.updateDamage(new GameDamage(DamageTypeRegistry.MAGIC, params[1].value(playerData.getLevel(), activeSkillLevel)));
        mob.setPurple();
        mob.getLevel().entityManager.addMob(mob, player.x, player.y);
    }
}
