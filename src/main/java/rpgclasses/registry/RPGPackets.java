package rpgclasses.registry;

import necesse.engine.registries.PacketRegistry;
import rpgclasses.buffs.Passive.HolyDamageDealtBuff;
import rpgclasses.forms.rpgmenu.RPGMenuPacket;
import rpgclasses.content.player.PlayerClasses.Wizard.Passives.Stormbound;
import rpgclasses.packets.*;

public class RPGPackets {

    public static void registerCore() {

        PacketRegistry.registerPacket(RPGMenuPacket.class);

        PacketRegistry.registerPacket(ActiveAbilityRunPacket.class);

        PacketRegistry.registerPacket(ShowModExpPacket.class);
        PacketRegistry.registerPacket(ShowDodgePacket.class);

        PacketRegistry.registerPacket(LoadPlayerDataPacket.class);

        PacketRegistry.registerPacket(UpdateClientExpPacket.class);
        PacketRegistry.registerPacket(UpdateClientResetsPacket.class);
        PacketRegistry.registerPacket(UpdateClientAttributesPacket.class);
        PacketRegistry.registerPacket(UpdateClientMasteryPacket.class);
        PacketRegistry.registerPacket(UpdateClientClassesPacket.class);
        PacketRegistry.registerPacket(UpdateClientEquippedActiveSkillsPacket.class);

        PacketRegistry.registerPacket(UpdateClientClassDataPacket.class);

        PacketRegistry.registerPacket(HolyDamageDealtBuff.ModClientHolyDamageDealtPacket.class);

        PacketRegistry.registerPacket(PacketModAllSkillsTime.class);

        // Skills
        PacketRegistry.registerPacket(Stormbound.LightningPacket.class);

        // Buffs
        PacketRegistry.registerPacket(PacketMobResetBuffTime.class);
        PacketRegistry.registerPacket(PacketMobUpdateIgniteBuff.class);


        // Transformations
        PacketRegistry.registerPacket(TransformationAbility1Packet.class);
        PacketRegistry.registerPacket(TransformationAbility2Packet.class);
    }

}
