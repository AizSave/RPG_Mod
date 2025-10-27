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
import rpgclasses.RPGResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.CastLevelEventActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.utils.RPGColors;
import rpgclasses.utils.RPGUtils;

public class Zap extends CastLevelEventActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(2),
            SkillParam.damageParam(1),
            SkillParam.staticParam(1)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(10);
    }

    public Zap(int levelMax, int requiredClassLevel) {
        super("zap", RPGColors.HEX.lighting, levelMax, requiredClassLevel);
    }

    @Override
    public int castTime() {
        return 1000;
    }

    @Override
    public LevelEvent getLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        return new ZapLevelEvent(player, player.x, player.y, params[1].value(playerData.getLevel(), activeSkillLevel));
    }

    @Override
    public Class<? extends LevelEvent> getLevelEventClass() {
        return ZapLevelEvent.class;
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 8000;
    }

    public static class ZapLevelEvent extends MobAbilityLevelEvent implements Attacker {
        private int lifeTime = 0;
        public int nextHit = 0;

        public float currentX;
        public float currentY;

        public GameDamage damage;

        public int lastHit = -1;

        public ZapLevelEvent() {
        }

        public ZapLevelEvent(Mob owner, float currentX, float currentY, float damage) {
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
                SoundManager.playSound(RPGResources.SOUNDS.Zap, SoundEffect.effect(this.owner).volume(1F));
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
            if (lifeTime > params[0].value() * 1000) {
                this.over();
            } else if (lifeTime >= nextHit) {
                Mob target = RPGUtils.findBestTarget(getAttackOwner(), currentX, currentY, lastHit == -1 ? 500 : 150, m -> lastHit != m.getUniqueID());

                if (target != null) {
                    lastHit = target.getUniqueID();
                    if (this.isServer()) {
                        target.isServerHit(damage, currentX, currentY, 10, owner);

                        RPGBuffs.applyStun(target, params[2].value());

                        SoundManager.playSound(RPGResources.SOUNDS.Zap, SoundEffect.effect(target).volume(1F));
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
                                        GameRandom.globalRandom.getFloatBetween(8, 12),
                                        14
                                ),
                                this.level, AphColors.lighting
                        );
                        this.level.entityManager.addTrail(trail);

                        float prevX = currentPosX;
                        float prevY = currentPosY;

                        for (int i = 1; i <= divisions; i++) {
                            float t = (float) i / divisions;
                            float baseX = currentPosX + ux * (length * t);
                            float baseY = currentPosY + uy * (length * t);

                            // Random lateral offset for zigzag effect
                            float maxOffset = 20;
                            float offset = GameRandom.globalRandom.getFloatBetween(-maxOffset, maxOffset);

                            // Apply lateral offset
                            float newX = baseX + vx * offset;
                            float newY = baseY + vy * offset;

                            // Ensure the last point exactly matches the target
                            if (i == divisions) {
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
                    currentX = target.x;
                    currentY = target.y;
                    nextHit = lifeTime + 200;
                }
            }
        }
    }

}
