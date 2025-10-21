package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.gfx.gameFont.FontManager;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.content.player.PlayerClasses.Wizard.Passives.Stormbound;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.packets.PacketMobResetBuffTime;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.utils.RPGUtils;

public class StormCaller extends Mastery {

    public StormCaller(String stringID, String color) {
        super(stringID, color);
    }

    @Override
    public MasteryBuff masteryBuff() {
        return new MasteryBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                super.init(activeBuff, buffEventSubscriber);
                isVisible = true;
            }

            @Override
            public void serverTick(ActiveBuff activeBuff) {
                super.serverTick(activeBuff);
                int time = activeBuff.getGndData().getInt("time", 0);
                time += 50;

                if (time > 20000) {
                    time = 0;

                    PlayerMob player = (PlayerMob) activeBuff.owner;

                    RPGUtils.getAllTargets(activeBuff.owner, 2000).forEach(
                            target -> {
                                target.isServerHit(new GameDamage(DamageTypeRegistry.MAGIC, getPlayerLevel(player) + getIntelligence(player)), player.x, player.y, 0, player);

                                RPGBuffs.applyStun(target, 1F);

                                player.getServer().network.sendToClientsAtEntireLevel(new Stormbound.LightningPacket(target.getX(), target.getY()), player.getLevel());
                            }
                    );

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
                String text = EquippedActiveSkill.getTimeLeftString(20000 - time);
                int width = FontManager.bit.getWidthCeil(text, durationFontOptions);
                FontManager.bit.drawString((float) (x + 28 - width), (float) (y + 30 - FontManager.bit.getHeightCeil(text, durationFontOptions)), text, durationFontOptions);
            }
        };
    }
}