package rpgclasses.forms.rpgmenu.customactions;

import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.inventory.container.customAction.ContainerCustomAction;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;

public abstract class EquippedActiveSkillsCustomAction extends ContainerCustomAction {
    public EquippedActiveSkillsCustomAction() {
    }

    public void runAndSend(EquippedActiveSkill[] equippedActiveSkills) {
        Packet content = new Packet();
        PacketWriter writer = new PacketWriter(content);
        for (EquippedActiveSkill equippedActiveSkill : equippedActiveSkills) {
            equippedActiveSkill.setupPacket(writer);
        }

        this.runAndSendAction(content);
    }

    public void executePacket(PacketReader reader) {
        EquippedActiveSkill[] equippedActiveSkills = new EquippedActiveSkill[PlayerData.EQUIPPED_SKILLS_MAX];
        for (int i = 0; i < PlayerData.EQUIPPED_SKILLS_MAX; i++) {
            equippedActiveSkills[i] = EquippedActiveSkill.applyPacket(reader);
        }
        this.run(equippedActiveSkills);
    }

    protected abstract void run(EquippedActiveSkill[] equippedActiveSkills);
}
