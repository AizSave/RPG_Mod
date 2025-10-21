package rpgclasses.packets;

import necesse.engine.GameLog;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.network.client.ClientClient;
import necesse.engine.network.packet.PacketDisconnect;
import necesse.engine.network.packet.PacketRequestPlayerData;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.PlayerMob;
import necesse.inventory.item.Item;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

public class ActiveAbilityRunPacket extends Packet {
    public final int slot;
    public final int skillSlot;

    public ActiveAbilityRunPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        this.slot = reader.getNextByteUnsigned();
        this.skillSlot = reader.getNextInt();
    }

    public ActiveAbilityRunPacket(int slot, int skillSlot) {
        this.slot = slot;
        this.skillSlot = skillSlot;

        PacketWriter writer = new PacketWriter(this);
        writer.putNextByteUnsigned(slot);
        writer.putNextInt(skillSlot);
    }

    public void processClient(NetworkPacket packet, Client client) {
        if (client.getLevel() != null) {
            ClientClient target = client.getClient(this.slot);
            if (target != null && target.playerMob.getLevel() != null) {
                PlayerMob player = client.getClient(this.slot).playerMob;
                PlayerData playerData = PlayerDataList.getPlayerData(player);
                EquippedActiveSkill equippedActiveSkill = playerData.equippedActiveSkills[skillSlot];
                if (!equippedActiveSkill.isEmpty()) {
                    int activeSkillLevel = equippedActiveSkill.getActiveSkill().getLevel(playerData);
                    if (activeSkillLevel > 0) {
                        int seed = Item.getRandomAttackSeed(GameRandom.globalRandom);
                        equippedActiveSkill.getActiveSkill().runClient(player, playerData, activeSkillLevel, seed, equippedActiveSkill.isInUse());
                    }
                }
            } else {
                client.network.sendPacket(new PacketRequestPlayerData(this.slot));
            }
        }
    }

    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        if (client.slot == this.slot) {
            if (!client.checkHasRequestedSelf() || client.isDead()) {
                return;
            }
            PlayerMob player = client.playerMob;
            PlayerData playerData = PlayerDataList.getPlayerData(player);
            EquippedActiveSkill equippedActiveSkill = playerData.equippedActiveSkills[skillSlot];

            if (!equippedActiveSkill.isEmpty() && equippedActiveSkill.getActiveSkill().canActive(player, playerData, equippedActiveSkill.isInUse()) == null) {
                int activeSkillLevel = equippedActiveSkill.getActiveSkill().getLevel(playerData);
                if (activeSkillLevel > 0) {
                    int seed = Item.getRandomAttackSeed(GameRandom.globalRandom);
                    equippedActiveSkill.getActiveSkill().runServer(player, playerData, activeSkillLevel, seed, equippedActiveSkill.isInUse());
                    server.network.sendToClientsWithEntityExcept(new ActiveAbilityRunPacket(this.slot, skillSlot), client.playerMob, client);
                }
            }
        } else {
            GameLog.warn.println(client.getName() + " tried to run Active Skill from wrong slot, kicking him for desync");
            server.disconnectClient(client, PacketDisconnect.Code.STATE_DESYNC);
        }

    }
}
