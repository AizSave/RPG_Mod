package rpgclasses.content.player.PlayerClasses.Warrior.ActiveSkills;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

public class BattleCry extends SimpleBuffActiveSkill {

    public BattleCry(int levelMax, int requiredClassLevel) {
        super("battlecry", "#ff6600", levelMax, requiredClassLevel);
    }

    @Override
    public void giveBuffOnRun(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
        super.giveBuff(player, player, playerData, activeSkillLevel);

        GameUtils.streamServerClients(player.getLevel()).forEach(targetPlayer -> {
            if (targetPlayer.isSameTeam(player.getTeam()))
                super.giveBuff(player, targetPlayer.playerMob, playerData, activeSkillLevel);
        });

        RPGUtils.streamMobsAndPlayers(player, 200)
                .filter(m -> m == player || m.isSameTeam(player))
                .forEach(
                        target -> super.giveBuff(player, target, playerData, activeSkillLevel)
                );
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.roar, SoundEffect.effect(player.x, player.y).volume(2F).pitch(1F));
        AphAreaList areaList = new AphAreaList(
                new AphArea(200, getColor())
        ).setOnlyVision(false);
        areaList.executeClient(player.getLevel(), player.x, player.y);
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                int level = getLevel(activeBuff);
                activeBuff.setModifier(BuffModifiers.ALL_DAMAGE, level * 0.1F);
                activeBuff.setModifier(BuffModifiers.SPEED, level * 0.1F);
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                Mob owner = activeBuff.owner;
                if (owner.isVisible() && GameRandom.globalRandom.nextInt(2) == 0) {
                    owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(getColor()).height(16.0F);
                }
            }
        };
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 10000;
    }

    @Override
    public int getBaseCooldown() {
        return 30000;
    }
}
