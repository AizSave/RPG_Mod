package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobAbilityLevelEvent;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import necesse.level.maps.regionSystem.RegionPosition;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleLevelEventActiveSkill;
import rpgclasses.data.MobData;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.util.Collections;
import java.util.Set;

public class Sanctuary extends SimpleLevelEventActiveSkill {
    public Sanctuary(int levelMax, int requiredClassLevel) {
        super("sanctuary", "#ffff22", levelMax, requiredClassLevel);
    }

    @Override
    public LevelEvent getLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        return new SanctuaryLevelEvent(player, player.getX(), player.getY(), 0.2F * playerData.getGrace(player) * activeSkillLevel, getBuffStringID());
    }

    @Override
    public Class<? extends LevelEvent> getLevelEventClass() {
        return SanctuaryLevelEvent.class;
    }

    @Override
    public int getBaseCooldown() {
        return 30000;
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 30 + activeSkillLevel * 6;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"manausage"};
    }

    public static class SanctuaryLevelEvent extends MobAbilityLevelEvent implements Attacker {
        private int lifeTime = 0;
        public int nextHit = 0;

        public int targetX;
        public int targetY;
        public float healthRegen;
        public String buffStringID;

        public SanctuaryLevelEvent() {
        }

        public SanctuaryLevelEvent(Mob owner, int targetX, int targetY, float healthRegen, String buffStringID) {
            super(owner, new GameRandom());
            this.targetX = targetX;
            this.targetY = targetY;
            this.healthRegen = healthRegen;
            this.buffStringID = buffStringID;
        }

        @Override
        public void setupSpawnPacket(PacketWriter writer) {
            super.setupSpawnPacket(writer);
            writer.putNextShortUnsigned(this.lifeTime);
            writer.putNextShortUnsigned(this.nextHit);

            writer.putNextInt(this.targetX);
            writer.putNextInt(this.targetY);
            writer.putNextFloat(this.healthRegen);
            writer.putNextString(this.buffStringID);
        }

        @Override
        public void applySpawnPacket(PacketReader reader) {
            super.applySpawnPacket(reader);
            this.lifeTime = reader.getNextShortUnsigned();
            this.nextHit = reader.getNextShortUnsigned();

            this.targetX = reader.getNextInt();
            this.targetY = reader.getNextInt();
            this.healthRegen = reader.getNextFloat();
            this.buffStringID = reader.getNextString();
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
            if (lifeTime < 1000) {
                range = (int) (maxRange * (lifeTime / 1000F));
            } else if (lifeTime <= 7000) {
                range = maxRange;
            } else if (lifeTime <= 8000) {
                range = (int) (maxRange * (1 - ((lifeTime - 7000) / 1000F)));
            } else {
                this.over();
            }

            if (range > 0) {
                if (this.isServer()) {
                    RPGUtils.streamMobsAndPlayers(owner, range)
                            .forEach(
                                    mob -> {
                                        if (mob == owner || mob.isSameTeam(owner)) {
                                            RPGBuffs.purify(mob, true);
                                            ActiveBuff ab = new ActiveBuff(buffStringID, mob, 100, null);
                                            ab.getGndData().setFloat("healthRegen", healthRegen);
                                            mob.buffManager.addBuff(ab, true);
                                        } else if (MobData.isWeakToHoly(mob, owner)) {
                                            mob.buffManager.addBuff(new ActiveBuff(RPGBuffs.CONSTRAINED, mob, 100, null), true);
                                        }
                                    }
                            );
                } else if (this.isClient()) {
                    int particleCount = (int) (range * range * 0.002);
                    for (int i = 0; i < particleCount; i++) {
                        float angle = GameRandom.globalRandom.getFloatBetween(0, 360);
                        float radius = (float) Math.sqrt(GameRandom.globalRandom.nextFloat()) * range;

                        float px = targetX + GameMath.cos(angle) * radius;
                        float py = targetY + GameMath.sin(angle) * radius;

                        this.getLevel().entityManager.addParticle(px, py, Particle.GType.IMPORTANT_COSMETIC)
                                .color(GameRandom.globalRandom.getOneOf(
                                        new Color(255, 255, 0),
                                        new Color(255, 255, 153),
                                        new Color(255, 255, 204)
                                ))
                                .heightMoves(0F, GameRandom.globalRandom.getFloatBetween(16, 24F));

                    }
                }
            }
        }
    }

    @Override
    public void registry() {
        super.registry();
        BuffRegistry.registerBuff(getBuffStringID(), new ActiveSkillBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                activeBuff.setModifier(BuffModifiers.COMBAT_HEALTH_REGEN_FLAT, activeBuff.getGndData().getFloat("healthRegen"));
            }
        });
    }

    public void giveBuff(Mob target, int duration) {
        ActiveBuff ab = getActiveBuff(target, duration);
        target.buffManager.addBuff(ab, target.isServer());
    }

    public ActiveBuff getActiveBuff(Mob target, int duration) {
        return new ActiveBuff(getBuffStringID(), target, duration, null);
    }

    public String getBuffStringID() {
        return stringID + "activeskillbuff";
    }

}
