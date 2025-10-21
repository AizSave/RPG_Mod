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
import necesse.entity.mobs.Mob;
import rpgclasses.mobs.mount.TransformationMountMob;

public class TransformationAbility1Packet extends Packet {
    public final int slot;
    public final int x;
    public final int y;

    public TransformationAbility1Packet(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        this.slot = reader.getNextByteUnsigned();
        this.x = reader.getNextInt();
        this.y = reader.getNextInt();
    }

    public TransformationAbility1Packet(int slot, int x, int y) {
        this.slot = slot;
        this.x = x;
        this.y = y;
        PacketWriter writer = new PacketWriter(this);
        writer.putNextByteUnsigned(slot);
        writer.putNextInt(x);
        writer.putNextInt(y);
    }

    public void processClient(NetworkPacket packet, Client client) {
        if (client.getLevel() != null) {
            ClientClient target = client.getClient(this.slot);
            if (target != null && target.isSamePlace(client.getLevel())) {
                Mob mount = target.playerMob.getMount();
                if (mount instanceof TransformationMountMob) {
                    TransformationMountMob transformation = (TransformationMountMob) mount;
                    transformation.clickRunClient(client.getLevel(), x, y, target.playerMob);
                }
            } else {
                client.network.sendPacket(new PacketRequestPlayerData(this.slot));
            }
        }

    }

    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        if (client.slot == this.slot) {
            if (!client.checkHasRequestedSelf() || client.isDead()) return;
            Mob mount = client.playerMob.getMount();
            if (mount instanceof TransformationMountMob) {
                TransformationMountMob transformation = (TransformationMountMob) mount;
                if (transformation.canRunClick(client.playerMob)) {
                    transformation.clickRunServer(client.getLevel(), x, y, client.playerMob);
                    server.network.sendToClientsAtEntireLevelExcept(new TransformationAbility1Packet(this.slot, this.x, this.y), client.getLevel(), client);
                }
            }
        } else {
            GameLog.warn.println(client.getName() + " tried to run transformation ability from wrong slot, kicking him for desync");
            server.disconnectClient(client, PacketDisconnect.Code.STATE_DESYNC);
        }

    }
}
