package rpgclasses.content.player.PlayerClasses.Necromancer.ActiveSkills;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobAbilityLevelEvent;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import necesse.level.maps.regionSystem.RegionPosition;
import rpgclasses.buffs.MagicPoisonBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleLevelEventActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.util.Collections;
import java.util.Set;

public class UnholyGround extends SimpleLevelEventActiveSkill {
    public UnholyGround(int levelMax, int requiredClassLevel) {
        super("unholyground", "#6633ff", levelMax, requiredClassLevel);
    }

    @Override
    public LevelEvent getLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        return new UnholyGroundLevelEvent(player, playerData.getLevel() + playerData.getIntelligence(player) * activeSkillLevel - playerData.getGrace(player));
    }

    @Override
    public Class<? extends LevelEvent> getLevelEventClass() {
        return UnholyGroundLevelEvent.class;
    }

    @Override
    public int getBaseCooldown() {
        return 60000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"constrained"};
    }

    public static class UnholyGroundLevelEvent extends MobAbilityLevelEvent implements Attacker {
        private int lifeTime = 0;
        public int nextHit = 0;

        public float poisonDamage;

        public UnholyGroundLevelEvent() {
        }

        public UnholyGroundLevelEvent(Mob owner, float poisonDamage) {
            super(owner, new GameRandom());
            this.poisonDamage = poisonDamage;
        }

        @Override
        public void setupSpawnPacket(PacketWriter writer) {
            super.setupSpawnPacket(writer);
            writer.putNextShortUnsigned(this.lifeTime);
            writer.putNextShortUnsigned(this.nextHit);

            writer.putNextFloat(this.poisonDamage);
        }

        @Override
        public void applySpawnPacket(PacketReader reader) {
            super.applySpawnPacket(reader);
            this.lifeTime = reader.getNextShortUnsigned();
            this.nextHit = reader.getNextShortUnsigned();

            this.poisonDamage = reader.getNextFloat();
        }

        @Override
        public void init() {
            super.init();
            this.hitsObjects = false;
            if (this.isClient()) {
                SoundManager.playSound(GameResources.croneLaugh, SoundEffect.effect(this.owner).volume(1.5F));
            }
        }

        @Override
        public void clientTick() {
            super.clientTick();
            tick();
        }

        @Override
        public void serverTick() {
            super.serverTick();
            tick();
        }

        public static int maxRange = 400;

        public void tick() {
            lifeTime += 50;

            int range = 0;
            if (lifeTime < 2000) {
                range = (int) (maxRange * (lifeTime / 2000F));
            } else if (lifeTime <= 8000) {
                range = maxRange;
            } else if (lifeTime <= 10000) {
                range = (int) (maxRange * (1 - ((lifeTime - 8000) / 2000F)));
            } else {
                this.over();
            }

            if (range > 0) {
                if (this.isServer()) {
                    RPGUtils.getAllTargets(owner, range, null)
                            .forEach(
                                    mob -> {
                                        mob.buffManager.addBuff(new ActiveBuff(RPGBuffs.CONSTRAINED, mob, 100, null), true);
                                        MagicPoisonBuff.apply(owner, mob, poisonDamage, 1000);
                                    }
                            );
                } else if (this.isClient()) {
                    int particleCount = (int) (range * range * 0.002);
                    for (int i = 0; i < particleCount; i++) {
                        float angle = GameRandom.globalRandom.getFloatBetween(0, 360);
                        float radius = (float) Math.sqrt(GameRandom.globalRandom.nextFloat()) * range;

                        float px = owner.x + GameMath.cos(angle) * radius;
                        float py = owner.y + GameMath.sin(angle) * radius;

                        if (RPGUtils.isInVision(owner.getLevel(), px, py, owner)) {
                            this.getLevel().entityManager.addParticle(px, py, Particle.GType.IMPORTANT_COSMETIC)
                                    .color(GameRandom.globalRandom.getOneOf(
                                            new Color(51, 0, 204),
                                            new Color(102, 0, 255),
                                            new Color(102, 51, 255)
                                    ))
                                    .heightMoves(0F, GameRandom.globalRandom.getFloatBetween(16, 24F));
                        }
                    }
                }
            }
        }
    }

}
