package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.network.client.ClientClient;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameFont.FontOptions;
import necesse.level.maps.Level;
import necesse.level.maps.hudManager.floatText.FloatTextFade;
import org.jetbrains.annotations.NotNull;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.awt.*;
import java.text.DecimalFormat;

public class PacketModAllSkillsTime extends Packet {
    public final int slot;
    public final int mod;

    public PacketModAllSkillsTime(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        this.slot = reader.getNextInt();
        this.mod = reader.getNextInt();
    }

    public PacketModAllSkillsTime(int slot, int mod) {
        this.slot = slot;
        this.mod = mod;

        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(slot);
        writer.putNextInt(mod);
    }

    public void processClient(NetworkPacket packet, Client client) {
        ClientClient c = client.getClient(slot);
        if (c != null) {
            PlayerMob player = c.playerMob;
            PlayerData playerData = PlayerDataList.getPlayerData(player);
            for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                if (!equippedActiveSkill.isEmpty()) equippedActiveSkill.modCooldown(mod);
            }

            float seconds = mod / 1000F;

            if (Math.abs(seconds) >= 0.01F) {
                FloatTextFade text = getFloatTextFade(player, seconds);
                Level level = client.getLevel();
                level.hudManager.addElement(text);

            }
        }
    }

    private @NotNull FloatTextFade getFloatTextFade(PlayerMob player, float seconds) {
        int x = (int) player.x;
        int y = (int) player.y;

        DecimalFormat df = new DecimalFormat("+#.##;-#.##");
        String secondsText = df.format(seconds) + "s";

        return new FloatTextFade(x, y - 20, secondsText, (new FontOptions(16)).outline().color(new Color(255, 255, 0))) {
            public int getAnchorX() {
                return x;
            }

            public int getAnchorY() {
                return y;
            }
        };
    }
}