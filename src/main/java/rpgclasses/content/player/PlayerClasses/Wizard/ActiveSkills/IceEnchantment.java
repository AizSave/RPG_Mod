package rpgclasses.content.player.PlayerClasses.Wizard.ActiveSkills;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.CastBuffActiveSkill;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

public class IceEnchantment extends CastBuffActiveSkill {

    public IceEnchantment(int levelMax, int requiredClassLevel) {
        super("iceenchantment", "#00ffff", levelMax, requiredClassLevel);
    }

    @Override
    public int castTime() {
        return 1000;
    }

    @Override
    public void castedRunServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
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
    public void castedRunClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunClient(player, playerData, activeSkillLevel, seed);

        SoundManager.playSound(GameResources.iceHit, SoundEffect.effect(player.x, player.y).volume(2F).pitch(1F));
        AphAreaList areaList = new AphAreaList(
                new AphArea(200, getColor())
        ).setOnlyVision(false);
        areaList.executeClient(player.getLevel(), player.x, player.y);
    }


    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {
            @Override
            public void clientTick(ActiveBuff activeBuff) {
                Mob owner = activeBuff.owner;
                if (owner.isVisible() && GameRandom.globalRandom.nextInt(2) == 0) {
                    owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(getColor()).height(16.0F);
                }
            }

            @Override
            public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
                super.onHasAttacked(activeBuff, event);
                if (event.damage > 0 && !event.wasPrevented) {
                    ActiveBuff ab = new ActiveBuff(BuffRegistry.Debuffs.FREEZING, event.target, 2F * getLevel(activeBuff), activeBuff.owner);
                    event.target.buffManager.addBuff(ab, activeBuff.owner.isServer());
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

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 20;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"manausage"};
    }

}
