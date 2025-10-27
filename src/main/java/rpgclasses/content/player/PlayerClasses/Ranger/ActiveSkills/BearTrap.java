package rpgclasses.content.player.PlayerClasses.Ranger.ActiveSkills;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.levelEvent.mobAbilityLevelEvent.HitboxEffectEvent;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.EntityDrawable;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.Level;
import necesse.level.maps.LevelObjectHit;
import necesse.level.maps.light.GameLight;
import rpgclasses.RPGResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleLevelEventActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.utils.RPGColors;

import java.awt.*;
import java.util.List;

public class BearTrap extends SimpleLevelEventActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(20),
            SkillParam.damageParam(5),
            SkillParam.staticParam(5)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public BearTrap(int levelMax, int requiredClassLevel) {
        super("beartrap", RPGColors.HEX.iron, levelMax, requiredClassLevel);
    }

    @Override
    public LevelEvent getLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        return new BearTrapLevelEvent(player, player.x, player.y, params[1].value(playerData.getLevel(), activeSkillLevel));
    }

    @Override
    public Class<? extends LevelEvent> getLevelEventClass() {
        return BearTrapLevelEvent.class;
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 30000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return -3000;
    }

    public static class BearTrapLevelEvent extends HitboxEffectEvent implements Attacker {
        private int lifeTime = 0;
        private int trappedLifeTime = 0;
        private boolean trappedMob = false;
        private boolean ready = false;

        public float targetX;
        public float targetY;
        public GameDamage damage;

        public BearTrapParticle bearTrapParticle;

        public BearTrapLevelEvent() {
        }

        public BearTrapLevelEvent(Mob owner, float targetX, float targetY, float damage) {
            super(owner, new GameRandom());
            this.targetX = targetX;
            this.targetY = targetY;
            this.damage = new GameDamage(damage);
        }

        @Override
        public void setupSpawnPacket(PacketWriter writer) {
            super.setupSpawnPacket(writer);
            writer.putNextShortUnsigned(this.lifeTime);
            writer.putNextShortUnsigned(this.trappedLifeTime);
            writer.putNextBoolean(this.trappedMob);
            writer.putNextBoolean(this.ready);

            writer.putNextFloat(this.targetX);
            writer.putNextFloat(this.targetY);
            writer.putNextFloat(this.damage.damage);
        }

        @Override
        public void applySpawnPacket(PacketReader reader) {
            super.applySpawnPacket(reader);
            this.lifeTime = reader.getNextShortUnsigned();
            this.trappedLifeTime = reader.getNextShortUnsigned();
            this.trappedMob = reader.getNextBoolean();
            this.ready = reader.getNextBoolean();

            this.targetX = reader.getNextFloat();
            this.targetY = reader.getNextFloat();
            this.damage = new GameDamage(reader.getNextFloat());
        }

        @Override
        public void init() {
            super.init();
            this.hitsObjects = false;
            if (this.isClient()) {
                bearTrapParticle = new BearTrapParticle(level, targetX, targetY, 20000);
                level.entityManager.addParticle(bearTrapParticle, Particle.GType.CRITICAL);

                SoundManager.playSound(GameResources.cling, SoundEffect.effect(this.owner).volume(1F));
            }
        }

        @Override
        public void clientTick() {
            super.clientTick();

            if (trappedMob) {
                this.trappedLifeTime += 50;
                if (this.trappedLifeTime >= (int) (params[0].value() * 1000)) {
                    this.over();
                    bearTrapParticle.remove();
                }
            } else {
                this.lifeTime += 50;
                if (this.lifeTime >= 500 && !ready) {
                    ready = true;
                    bearTrapParticle.setOpen(true);
                    SoundManager.playSound(GameResources.cling, SoundEffect.effect(this.owner).volume(1F));
                } else if (this.lifeTime >= 20000) {
                    this.over();
                    bearTrapParticle.remove();
                }
            }
        }

        @Override
        public void serverTick() {
            super.serverTick();

            if (trappedMob) {
                this.trappedLifeTime += 50;
                if (this.trappedLifeTime >= (int) (params[0].value() * 1000)) {
                    this.over();
                }
            } else {
                this.lifeTime += 50;
                if (this.lifeTime >= 500 && !ready) {
                    ready = true;
                } else if (this.lifeTime >= 20000) {
                    this.over();
                }
            }
        }

        @Override
        public Shape getHitBox() {
            if (trappedMob) return new Rectangle();

            int size = 20;
            return new Rectangle((int) (this.targetX - size / 2F), (int) (this.targetY - size / 2F), size, size);
        }

        @Override
        public boolean canHit(Mob mob) {
            return super.canHit(mob);
        }

        @Override
        public void serverHit(Mob target, boolean clientSubmitted) {
            if (!trappedMob && ready) {
                target.isServerHit(damage, target.x - targetX, target.y - targetY, 0, this.owner);
                target.addBuff(new ActiveBuff(RPGBuffs.TRAPPED, target, params[2].value(), this), true);
                trappedMob = true;
                target.setPos(targetX, targetY, true);
            }
        }

        @Override
        public void clientHit(Mob mob) {
            if (!trappedMob && ready) {
                SoundManager.playSound(GameResources.cling, SoundEffect.effect(this.owner).volume(1F));
                trappedMob = true;
                bearTrapParticle.setOpen(false);
                mob.setPos(targetX, targetY, true);
            }
        }

        @Override
        public void hitObject(LevelObjectHit hit) {
        }
    }

    public static class BearTrapParticle extends Particle {
        public static int MOVE_Y = 2;

        public boolean isOpen;

        public BearTrapParticle(Level level, float x, float y, long lifeTime) {
            super(level, x, y + MOVE_Y, lifeTime);
            this.isOpen = false;
        }

        public void setOpen(boolean isOpen) {
            this.isOpen = isOpen;
        }

        @Override
        public void addDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, OrderableDrawables overlayList, Level level, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
            if (!this.removed()) {
                GameTexture texture = isOpen ? RPGResources.PARTICLE_TEXTURES.bearTrapOpen : RPGResources.PARTICLE_TEXTURES.bearTrapClosed;

                GameLight light = level.getLightLevel(this);
                int drawX = camera.getDrawX(this.x) - texture.getWidth() / 2;
                int drawY = camera.getDrawY(this.y) - texture.getHeight() + 11 - MOVE_Y;

                final TextureDrawOptions options = texture.initDraw().light(light).pos(drawX, drawY);
                list.add(new EntityDrawable(this) {
                    public void draw(TickManager tickManager) {
                        options.draw();
                    }
                });
            }
        }
    }

}
