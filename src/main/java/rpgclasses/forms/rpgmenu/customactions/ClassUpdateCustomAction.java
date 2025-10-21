package rpgclasses.forms.rpgmenu.customactions;

import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.inventory.container.customAction.ContainerCustomAction;
import rpgclasses.content.player.PlayerClass;

public abstract class ClassUpdateCustomAction extends ContainerCustomAction {
    public ClassUpdateCustomAction() {
    }

    public void runAndSend(int classID, int[] passiveLevels, int[] activeSkillLevels) {
        Packet content = new Packet();
        PacketWriter writer = new PacketWriter(content);
        writer.putNextInt(classID);
        writer.putNextInts(passiveLevels);
        writer.putNextInts(activeSkillLevels);

        this.runAndSendAction(content);
    }

    public void executePacket(PacketReader reader) {
        int classID = reader.getNextInt();
        PlayerClass playerClass = PlayerClass.classesList.get(classID);

        int[] passiveLevels = new int[playerClass.passivesList.size()];
        for (int i = 0; i < playerClass.passivesList.size(); i++) {
            passiveLevels[i] = reader.getNextInt();
        }

        int[] activeSkillLevels = new int[playerClass.activeSkillsList.size()];
        for (int i = 0; i < playerClass.activeSkillsList.size(); i++) {
            activeSkillLevels[i] = reader.getNextInt();
        }

        this.run(classID, passiveLevels, activeSkillLevels);
    }

    protected abstract void run(int classID, int[] passiveLevels, int[] activeSkillLevels);
}
