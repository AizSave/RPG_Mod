package rpgclasses.forms.rpgmenu;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.packet.PacketOpenContainer;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ContainerRegistry;
import rpgclasses.registry.RPGContainers;

public class RPGMenuPacket extends Packet {
    public RPGMenuPacket(byte[] data) {
        super(data);
    }

    public RPGMenuPacket() {
    }

    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        if (client.checkHasRequestedSelf() && !client.isDead()) {
            client.checkSpawned();
            PacketOpenContainer p = new PacketOpenContainer(RPGContainers.MENU_CONTAINER);
            ContainerRegistry.openAndSendContainer(client, p);
        }
    }
}