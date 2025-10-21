package rpgclasses.forms.rpgmenu.customactions;

import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.inventory.container.customAction.ContainerCustomAction;

public abstract class IntArrayCustomAction extends ContainerCustomAction {
    public IntArrayCustomAction() {
    }

    public void runAndSend(int[] values) {
        Packet content = new Packet();
        PacketWriter writer = new PacketWriter(content);
        if (arrayLength() == -1) writer.putNextInt(values.length);
        for (int value : values) {
            writer.putNextInt(value);
        }
        this.runAndSendAction(content);
    }

    public void executePacket(PacketReader reader) {
        int size = arrayLength() == -1 ? reader.getNextInt() : arrayLength();
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = reader.getNextInt();
        }
        this.run(values);
    }

    protected abstract void run(int[] var1);

    public int arrayLength() {
        return -1;
    }
}
