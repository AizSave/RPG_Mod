package rpgclasses.buffs.Passive;

import aphorea.utils.magichealing.AphMagicHealingBuff;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.registries.BuffRegistry;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobHealthChangeEvent;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.gfx.gameFont.FontManager;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.toolItem.ToolItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rpgclasses.data.MobData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.registry.RPGDamageType;

import java.text.DecimalFormat;

public class HolyDamageDealtBuff extends PassiveBuff implements AphMagicHealingBuff {
    public HolyDamageDealtBuff() {
        isVisible = true;
        isImportant = true;
    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
        isVisible = false;
    }

    @Override
    public void serverTick(ActiveBuff activeBuff) {
        super.serverTick(activeBuff);
        float used = getUsedDamageDealt(activeBuff);
        if (used > 0) {
            setUsedDamageDealt(activeBuff, 0);
            addDamageDealt(activeBuff, -used, true);
        } else {
            float damageDealt = getDamageDealt(activeBuff);
            if (damageDealt >= 0.01F) {
                addDamageDealt(activeBuff, -damageDealt / 100F, true);
            }
        }
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        this.isVisible = getDamageDealt(activeBuff) >= 0.01F;
    }

    @Override
    public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
        super.onHasAttacked(activeBuff, event);
        float holyDamage = event.damage * 0.02F;
        if (holyDamage > 0 && !event.wasPrevented) {
            if (event.damageType == RPGDamageType.HOLY) {
                addDamageDealt(activeBuff, holyDamage, true);
                Mob target = event.target;
                if (MobData.isWeakToHoly(target, activeBuff.owner)) {
                    if (event.damage >= 100) {
                        target.buffManager.addBuff(new ActiveBuff(BuffRegistry.Debuffs.ABLAZE, target, 5F, activeBuff.owner), activeBuff.owner.isServer());
                    } else {
                        target.buffManager.addBuff(new ActiveBuff(BuffRegistry.Debuffs.ON_FIRE, target, 10F, activeBuff.owner), activeBuff.owner.isServer());
                    }
                }
            }
        }
    }

    @Override
    public void onMagicalHealing(ActiveBuff activeBuff, Mob healer, Mob target, int healing, int realHealing, @Nullable ToolItem toolItem, @Nullable InventoryItem item) {
        int extraHeal = (int) (Math.min(target.getMaxHealth() - target.getHealth(), getDamageDealt(activeBuff)));
        if (extraHeal > 0) {
            target.getLevel().entityManager.events.add(new MobHealthChangeEvent(target, extraHeal));
            addUsedDamageDealt(activeBuff, extraHeal);
        }
    }

    @Override
    public void drawIcon(int x, int y, ActiveBuff activeBuff) {
        GameTexture drawIcon = this.getDrawIcon(activeBuff);
        drawIcon.initDraw().size(32, 32).draw(x, y);
        float numberDisplay = getDamageDealt(activeBuff);
        if (numberDisplay > 0) {
            String text = getString(numberDisplay);
            int width = FontManager.bit.getWidthCeil(text, durationFontOptions);
            FontManager.bit.drawString((float) (x + 28 - width), (float) (y + 30 - FontManager.bit.getHeightCeil(text, durationFontOptions)), text, durationFontOptions);
        }
    }

    private static @NotNull String getString(double numberDisplay) {
        String postN = "";
        if (numberDisplay >= 1_000_000) {
            numberDisplay /= 1_000_000;
            postN = "M";
        } else if (numberDisplay >= 1_000) {
            numberDisplay /= 1_000;
            postN = "K";
        }

        DecimalFormat oneDecimal = new DecimalFormat("0.0"); // siempre un decimal

        String text = oneDecimal.format(numberDisplay) + postN;

        if (text.length() > 4) {
            text = "999" + postN;
        }

        return text;
    }


    public static ActiveBuff getActiveBuff(Mob mob) {
        return mob.buffManager.getBuff(RPGBuffs.PASSIVES.HOLY_DAMAGE);
    }

    public static void addDamageDealt(ActiveBuff activeBuff, float amount, boolean sendPacket) {
        if (activeBuff != null) {
            setDamageDealt(activeBuff, getDamageDealt(activeBuff) + amount, sendPacket);
        }
    }

    public static void setDamageDealt(ActiveBuff activeBuff, float amount, boolean sendPacket) {
        if (activeBuff != null) {
            float set = Math.max(0, amount);
            activeBuff.getGndData().setFloat("damageDealt", set);
            if (sendPacket && activeBuff.owner.isPlayer && activeBuff.owner.isServer()) {
                PlayerMob player = (PlayerMob) activeBuff.owner;
                player.getServerClient().sendPacket(new ModClientHolyDamageDealtPacket(set));
            }
        }
    }

    public static float getDamageDealt(ActiveBuff activeBuff) {
        if (activeBuff != null) {
            return activeBuff.getGndData().getFloat("damageDealt", 0);
        }
        return 0;
    }

    public static void addUsedDamageDealt(ActiveBuff activeBuff, float amount) {
        if (activeBuff != null) {
            setUsedDamageDealt(activeBuff, getUsedDamageDealt(activeBuff) + amount);
        }
    }

    public static void setUsedDamageDealt(ActiveBuff activeBuff, float amount) {
        if (activeBuff != null) {
            activeBuff.getGndData().setFloat("usedDamageDealt", Math.max(0, amount));
        }
    }

    public static float getUsedDamageDealt(ActiveBuff activeBuff) {
        if (activeBuff != null) {
            return activeBuff.getGndData().getFloat("usedDamageDealt", 0);
        }
        return 0;
    }

    public static class ModClientHolyDamageDealtPacket extends Packet {
        public final float amount;

        public ModClientHolyDamageDealtPacket(byte[] data) {
            super(data);
            PacketReader reader = new PacketReader(this);
            this.amount = reader.getNextFloat();
        }

        public ModClientHolyDamageDealtPacket(float amount) {
            this.amount = amount;

            PacketWriter writer = new PacketWriter(this);
            writer.putNextFloat(amount);
        }

        public void processClient(NetworkPacket packet, Client client) {
            ActiveBuff ab = getActiveBuff(client.getPlayer());
            setDamageDealt(ab, amount, false);
        }
    }
}
