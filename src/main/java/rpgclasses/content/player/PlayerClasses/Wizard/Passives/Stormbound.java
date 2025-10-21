package rpgclasses.content.player.PlayerClasses.Wizard.Passives;

import aphorea.utils.AphColors;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.trails.LightningTrail;
import necesse.entity.trails.TrailVector;
import necesse.gfx.GameResources;
import necesse.gfx.gameFont.FontManager;
import necesse.level.maps.Level;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.packets.PacketMobResetBuffTime;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.utils.RPGColors;
import rpgclasses.utils.RPGUtils;

public class Stormbound extends SimpleBuffPassive {
    public Stormbound(int levelMax, int requiredClassLevel) {
        super("stormbound", RPGColors.HEX.lighting, levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void serverTick(ActiveBuff activeBuff) {
                super.serverTick(activeBuff);
                int time = activeBuff.getGndData().getInt("time", 0);
                time += 50;

                if (time > (11000 - 500 * getLevel(activeBuff))) {
                    time = 0;

                    Mob target = RPGUtils.getRandomTarget(activeBuff.owner, 500);

                    PlayerMob player = (PlayerMob) activeBuff.owner;

                    if (target != null) {
                        target.isServerHit(new GameDamage(DamageTypeRegistry.MAGIC, 0.5F * getPlayerLevel(player) + 0.5F * getIntelligence(player) * getLevel(activeBuff)), player.x, player.y, 0, player);

                        RPGBuffs.applyStun(target, 1F);

                        player.getServer().network.sendToClientsAtEntireLevel(new LightningPacket(target.getX(), target.getY()), player.getLevel());
                    }

                    player.getServer().network.sendToClientsAtEntireLevel(new PacketMobResetBuffTime(player.getUniqueID(), getBuffStringID()), player.getLevel());
                }

                activeBuff.getGndData().setInt("time", time);
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                super.clientTick(activeBuff);
                int time = activeBuff.getGndData().getInt("time", 0);
                time += 50;
                activeBuff.getGndData().setInt("time", time);
            }

            @Override
            public void drawIcon(int x, int y, ActiveBuff activeBuff) {
                super.drawIcon(x, y, activeBuff);
                int time = activeBuff.getGndData().getInt("time", 0) - 50;
                String text = EquippedActiveSkill.getTimeLeftString((11000 - 500 * getLevel(activeBuff)) - time);
                int width = FontManager.bit.getWidthCeil(text, durationFontOptions);
                FontManager.bit.drawString((float) (x + 28 - width), (float) (y + 30 - FontManager.bit.getHeightCeil(text, durationFontOptions)), text, durationFontOptions);
            }

        };
    }

    public static class LightningPacket extends Packet {
        public final int x;
        public final int y;

        public LightningPacket(byte[] data) {
            super(data);
            PacketReader reader = new PacketReader(this);
            this.x = reader.getNextInt();
            this.y = reader.getNextInt();
        }

        public LightningPacket(int x, int y) {
            this.x = x;
            this.y = y;

            PacketWriter writer = new PacketWriter(this);
            writer.putNextInt(x);
            writer.putNextInt(y);
        }

        public void processClient(NetworkPacket packet, Client client) {
            Level level = client.getLevel();

            SoundManager.playSound(GameResources.electricExplosion, SoundEffect.effect(x, y).volume(1.2F).pitch(0.8F));

            float initialMoveX = GameRandom.globalRandom.getIntBetween(-20, 20);
            float initialMoveY = GameRandom.globalRandom.getIntBetween(-20, 20);

            for (int i = 0; i < 6; i++) {
                float finalMoveX;
                float finalMoveY;
                if (i == 0) {
                    finalMoveX = 0;
                    finalMoveY = 0;
                } else {
                    finalMoveX = GameRandom.globalRandom.getIntBetween(50, 80) * (GameRandom.globalRandom.getChance(0.5F) ? -1 : 1);
                    finalMoveY = GameRandom.globalRandom.getIntBetween(50, 80) * (GameRandom.globalRandom.getChance(0.5F) ? -1 : 1);
                }

                float prevX = x;
                float prevY = y;

                LightningTrail trail = new LightningTrail(new TrailVector(prevX, prevY, 0, 0, i == 0 ? 16 : GameRandom.globalRandom.getFloatBetween(8, 12), 0), level, level.isCave ? AphColors.dark_magic : AphColors.lighting);
                level.entityManager.addTrail(trail);

                for (int j = i == 0 ? 1 : i + 2; j < 6; j++) {
                    float progression = (float) j / 10;
                    float height = 500 * progression;
                    float newX = x + GameRandom.globalRandom.getIntBetween(-5, 5) + finalMoveY * (1 - progression) + initialMoveX * progression;
                    float newY = y + GameRandom.globalRandom.getIntBetween(-5, 5) + finalMoveX * (1 - progression) + initialMoveY * progression;
                    trail.addNewPoint(new TrailVector(newX, newY, newX - prevX, newY - prevY, trail.thickness, height));
                    prevX = newX;
                    prevY = newY;
                }
            }
        }
    }

}
