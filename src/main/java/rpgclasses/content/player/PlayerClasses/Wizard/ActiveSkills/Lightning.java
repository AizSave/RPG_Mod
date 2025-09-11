package rpgclasses.content.player.PlayerClasses.Wizard.ActiveSkills;

import aphorea.utils.AphColors;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobAbilityLevelEvent;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.particle.Particle;
import necesse.entity.trails.LightningTrail;
import necesse.entity.trails.TrailVector;
import necesse.gfx.GameResources;
import necesse.level.maps.regionSystem.RegionPosition;
import rpgclasses.RPGResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleLevelEventActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.utils.RPGColors;
import rpgclasses.utils.RPGUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Lightning extends SimpleLevelEventActiveSkill {
    public Lightning(int levelMax, int requiredClassLevel) {
        super("lightning", RPGColors.HEX.lighting, levelMax, requiredClassLevel);
    }

    @Override
    public LevelEvent getLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        return new LightningLevelEvent(player, player.x, player.y, 4 * playerData.getLevel() + 4 * playerData.getIntelligence(player) * activeSkillLevel);
    }

    @Override
    public Class<? extends LevelEvent> getLevelEventClass() {
        return LightningLevelEvent.class;
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 30 + activeSkillLevel * 6;
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"manausage"};
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        return RPGUtils.anyTarget(player, 500) ? null : "notarget";
    }

    public static class LightningLevelEvent extends MobAbilityLevelEvent implements Attacker {
        private int lifeTime = 0;
        public int nextHit = 0;

        public float currentX;
        public float currentY;

        public GameDamage damage;

        public Set<Integer> targetedMobs = new HashSet<>();

        public LightningLevelEvent() {
        }

        public LightningLevelEvent(Mob owner, float currentX, float currentY, float damage) {
            super(owner, new GameRandom());
            this.currentX = currentX;
            this.currentY = currentY;
            this.damage = new GameDamage(damage);
        }

        @Override
        public void setupSpawnPacket(PacketWriter writer) {
            super.setupSpawnPacket(writer);
            writer.putNextShortUnsigned(this.lifeTime);
            writer.putNextShortUnsigned(this.nextHit);

            writer.putNextFloat(this.currentX);
            writer.putNextFloat(this.currentY);
            writer.putNextFloat(this.damage.damage);
        }

        @Override
        public void applySpawnPacket(PacketReader reader) {
            super.applySpawnPacket(reader);
            this.lifeTime = reader.getNextShortUnsigned();
            this.nextHit = reader.getNextShortUnsigned();

            this.currentX = reader.getNextFloat();
            this.currentY = reader.getNextFloat();
            this.damage = new GameDamage(reader.getNextFloat());
        }

        @Override
        public void init() {
            super.init();
            this.hitsObjects = false;
            if (this.isClient()) {
                SoundManager.playSound(RPGResources.SOUNDS.Zap, SoundEffect.effect(this.owner).volume(1.5F));
            }
        }

        @Override
        public void clientTick() {
            super.clientTick();
            tick();
            this.getLevel().entityManager.addParticle(currentX + (float) (GameRandom.globalRandom.nextGaussian() * 4.0), currentY + (float) (GameRandom.globalRandom.nextGaussian() * 4.0), Particle.GType.IMPORTANT_COSMETIC).color(AphColors.lighting);
        }

        @Override
        public void serverTick() {
            super.serverTick();
            tick();
        }

        public void tick() {
            lifeTime += 50;
            if (lifeTime > 1000) {
                this.over();
            } else if (lifeTime >= nextHit) {
                Mob target = RPGUtils.findBestTarget(getAttackOwner(), currentX, currentY, 500, m -> !targetedMobs.contains(m.getUniqueID()));

                if (target != null) {
                    targetedMobs.add(target.getUniqueID());
                    if (this.isServer()) {
                        target.isServerHit(damage, currentX, currentY, 50, owner);

                        RPGBuffs.applyStun(target, 2F);

                        SoundManager.playSound(GameResources.electricExplosion, SoundEffect.effect(target).volume(1.5F));
                    }
                    if (this.isClient()) {
                        float currentPosX = currentX;
                        float currentPosY = currentY;

                        float distance = target.getDistance(currentX, currentY);

                        float finalPosX = target.x;
                        float finalPosY = target.y;

                        int divisions = 1 + (int) (distance / 30);

                        float dx = finalPosX - currentPosX;
                        float dy = finalPosY - currentPosY;
                        float length = (float) Math.sqrt(dx * dx + dy * dy);
                        float ux = dx / length;
                        float uy = dy / length;

                        float vx = -uy;
                        float vy = ux;

                        LightningTrail trail = new LightningTrail(
                                new TrailVector(
                                        currentPosX, currentPosY,
                                        0, 0,
                                        GameRandom.globalRandom.getFloatBetween(12, 16),
                                        14
                                ),
                                this.level, AphColors.lighting
                        );
                        this.level.entityManager.addTrail(trail);

                        float prevX = currentPosX;
                        float prevY = currentPosY;

                        for (int i = 0; i < 2; i++) {
                            for (int j = 1; j <= divisions; j++) {
                                float t = (float) j / divisions;
                                float baseX = currentPosX + ux * (length * t);
                                float baseY = currentPosY + uy * (length * t);

                                // Random lateral offset for zigzag effect
                                float maxOffset = 20;
                                float offset = GameRandom.globalRandom.getFloatBetween(-maxOffset, maxOffset);

                                // Apply lateral offset
                                float newX = baseX + vx * offset;
                                float newY = baseY + vy * offset;

                                // Ensure the last point exactly matches the target
                                if (j == divisions) {
                                    newX = finalPosX;
                                    newY = finalPosY;
                                }

                                // Add new segment to the trail
                                trail.addNewPoint(new TrailVector(
                                        newX, newY,
                                        newX - prevX, newY - prevY,
                                        trail.thickness, 14
                                ));

                                // Update previous point
                                prevX = newX;
                                prevY = newY;
                            }
                        }
                    }
                    currentX = target.x;
                    currentY = target.y;
                    nextHit = lifeTime + 200;
                }
            }
        }

        @Override
        public Set<RegionPosition> getRegionPositions() {
            return Collections.singleton(this.getLevel().regionManager.getRegionPosByTile((int) (this.currentX / 32), (int) (this.currentY / 32)));
        }
    }

}
