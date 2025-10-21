package rpgclasses.forms.rpgmenu;

import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.NetworkClient;
import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.PlayerMob;
import necesse.inventory.container.Container;
import rpgclasses.forms.rpgmenu.customactions.ClassUpdateCustomAction;
import rpgclasses.forms.rpgmenu.customactions.EquippedActiveSkillsCustomAction;
import rpgclasses.forms.rpgmenu.customactions.IntArrayCustomAction;
import rpgclasses.forms.rpgmenu.entries.ClassEntry;
import rpgclasses.content.player.Attribute;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerClassData;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.packets.*;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.settings.RPGSettings;

import java.util.*;

public class MenuContainer extends Container {
    public final IntArrayCustomAction updateAttributes;
    public final IntArrayCustomAction updateMasterySkills;
    public final IntArrayCustomAction updateClasses;
    public final ClassUpdateCustomAction updateClass;
    public final EquippedActiveSkillsCustomAction updateEquippedActiveSkills;

    public MenuContainer(final NetworkClient client, int uniqueSeed) {
        super(client, uniqueSeed);

        this.updateAttributes = this.registerAction(
                new IntArrayCustomAction() {
                    @Override
                    protected void run(int[] attributePoints) {
                        if (client.isServer()) {
                            if (client.playerMob.isInCombat()) {
                                client.getServerClient().sendChatMessage(new LocalMessage("message", "noupdatesincombat"));
                                return;
                            }

                            ServerClient serverClient = client.getServerClient();

                            if (Arrays.stream(attributePoints).allMatch(attr -> attr < 1000 && attr >= 0)) {
                                int total = Arrays.stream(attributePoints).sum();
                                PlayerData playerData = PlayerDataList.getPlayerData(serverClient.playerMob);
                                if (total <= playerData.totalAttributePoints()) {
                                    int reduced = 0;
                                    for (int i = 0; i < attributePoints.length; i++) {
                                        int difference = playerData.getAttributePoints(i) - attributePoints[i];
                                        if (difference > 0) reduced += difference;
                                    }
                                    if (reduced > 0) {
                                        if (playerData.getResets() >= reduced) {
                                            playerData.modResetsSendPacket(serverClient, -reduced);
                                        } else {
                                            System.out.println("updateAttributes: Not enough Reset Points");
                                            return;
                                        }
                                    }
                                    playerData.setAttributePointsUsed(attributePoints);
                                    serverClient.getServer().network.sendToAllClients(new UpdateClientAttributesPacket(playerData));
                                    playerData.updateModifiersBuff(serverClient.playerMob);
                                } else {
                                    System.out.println("updateAttributes: Not enough Attribute Points");
                                }
                            } else {
                                System.out.println("updateAttributes: At least one wrong assigned attribute level");
                            }
                        }
                    }

                    @Override
                    public int arrayLength() {
                        return Attribute.attributesList.size();
                    }
                }
        );

        this.updateMasterySkills = this.registerAction(
                new IntArrayCustomAction() {
                    @Override
                    protected void run(int[] enabledMasterySkills) {
                        if (client.isServer()) {
                            if (client.playerMob.isInCombat()) {
                                client.getServerClient().sendChatMessage(new LocalMessage("message", "noupdatesincombat"));
                                return;
                            }

                            ServerClient serverClient = client.getServerClient();

                            int total = enabledMasterySkills.length;
                            PlayerData playerData = PlayerDataList.getPlayerData(serverClient.playerMob);
                            if (total > playerData.totalMasteryPoints()) {
                                System.out.println("updateMasterySkills: Not enough Mastery Points");
                                return;
                            }
                            List<Integer> newMasterySkills = new ArrayList<>(enabledMasterySkills.length);
                            for (int skill : enabledMasterySkills) {
                                newMasterySkills.add(skill);
                            }

                            int reduced = 0;
                            for (Integer masterySkill : playerData.masterySkills) {
                                if (!newMasterySkills.contains(masterySkill)) reduced += 5;
                            }

                            if (reduced > 0) {
                                if (playerData.getResets() >= reduced) {
                                    playerData.modResetsSendPacket(serverClient, -reduced);
                                } else {
                                    System.out.println("updateMasterySkills: Not enough Reset Points");
                                    return;
                                }
                            }
                            playerData.setMasterySkills(newMasterySkills);
                            serverClient.getServer().network.sendToAllClients(new UpdateClientMasteryPacket(playerData));
                            playerData.updateAllBuffs(serverClient.playerMob);

                        }
                    }
                }
        );

        this.updateClasses = this.registerAction(
                new IntArrayCustomAction() {
                    @Override
                    protected void run(int[] classLevels) {
                        if (client.isServer()) {
                            if (client.playerMob.isInCombat()) {
                                client.getServerClient().sendChatMessage(new LocalMessage("message", "noupdatesincombat"));
                                return;
                            }

                            ServerClient serverClient = client.getServerClient();

                            int multiClass = RPGSettings.multiClass();

                            if ((multiClass != 0 && Arrays.stream(classLevels).filter(level -> level > 0).count() > multiClass)) {
                                System.out.println("updateClasses: You cannot have more than " + multiClass + " different classes");
                                return;
                            }

                            if (Arrays.stream(classLevels).anyMatch(pClass -> pClass >= 1000 || pClass < 0)) {
                                System.out.println("updateClasses: At least one wrong assigned class level");
                                return;
                            }

                            int total = Arrays.stream(classLevels).sum();
                            PlayerData playerData = PlayerDataList.getPlayerData(serverClient.playerMob);
                            if (total > playerData.totalClassPoints()) {
                                System.out.println("updateClasses: Not enough Class Points");
                                return;
                            }
                            int reduced = 0;
                            for (int i = 0; i < classLevels.length; i++) {
                                int difference = playerData.getClassLevel(i) - classLevels[i];
                                if (difference > 0) reduced += difference;
                            }

                            if (reduced > 0) {
                                if (playerData.getResets() >= reduced * 2) {
                                    playerData.modResetsSendPacket(serverClient, -reduced * 2);
                                } else {
                                    System.out.println("updateClasses: Not enough Reset Points");
                                    return;
                                }
                            }

                            playerData.setClassLevels(classLevels);

                            for (int classID = 0; classID < classLevels.length; classID++) {
                                boolean someChange = false;

                                int classLevel = classLevels[classID];
                                PlayerClassData classData = playerData.getClassesData()[classID];
                                for (int skillID = 0; skillID < classData.getActiveSkillLevels().length; skillID++) {
                                    int activeSkillLevel = classData.getActiveSkillLevels()[skillID];
                                    if (activeSkillLevel > 0) {
                                        ActiveSkill activeSkill = classData.playerClass.activeSkillsList.get(skillID);
                                        int maxEffectiveLevel = classData.getEffectiveSkillMaxLevel(activeSkill, classLevel, classData.getActiveSkillLevels());
                                        if (maxEffectiveLevel < activeSkillLevel) {
                                            classData.setActiveSkillLevel(skillID, maxEffectiveLevel);
                                            checkActiveSkillRequirements(classData, activeSkill, maxEffectiveLevel);
                                            someChange = true;
                                        }
                                    }
                                }
                                for (int skillID = 0; skillID < classData.getPassiveLevels().length; skillID++) {
                                    int passiveLevel = classData.getPassiveLevels()[skillID];
                                    if (passiveLevel > 0) {
                                        Passive passive = classData.playerClass.passivesList.get(skillID);
                                        int maxEffectiveLevel = classData.getEffectiveSkillMaxLevel(passive, classLevel, classData.getPassiveLevels());
                                        if (maxEffectiveLevel < passiveLevel) {
                                            classData.setPassiveLevel(skillID, maxEffectiveLevel);
                                            someChange = true;
                                        }
                                    }
                                }

                                if (someChange)
                                    serverClient.getServer().network.sendToAllClients(new UpdateClientClassDataPacket(classData));
                            }
                            serverClient.getServer().network.sendToAllClients(new UpdateClientClassesPacket(playerData));

                            playerData.updateAllBuffs(serverClient.playerMob);

                            boolean someEquippedUpdate = false;
                            for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                                if (!equippedActiveSkill.isEmpty()) {
                                    PlayerClassData playerClassData = playerData.getClassesData()[equippedActiveSkill.getPlayerClass().id];
                                    if (!equippedActiveSkill.isInCooldown(client.playerMob.getTime()) && (playerClassData.getLevel(true) < 1 || playerClassData.getActiveSkillLevels()[equippedActiveSkill.getActiveSkill().id] < 1)) {
                                        equippedActiveSkill.empty();
                                        someEquippedUpdate = true;
                                    }
                                }
                            }
                            if (someEquippedUpdate) {
                                serverClient.getServer().network.sendToAllClients(new UpdateClientEquippedActiveSkillsPacket(playerData));
                            }
                        }
                    }

                    private void checkActiveSkillRequirements(PlayerClassData classData, ActiveSkill activeSkill, int newActiveSkillLevel) {
                        for (int thisActiveSkillID = 0; thisActiveSkillID < classData.getActiveSkillLevels().length; thisActiveSkillID++) {
                            int thisActiveSkillLevel = classData.getActiveSkillLevels()[thisActiveSkillID];
                            if (thisActiveSkillLevel > 0) {
                                ActiveSkill thisActiveSkill = classData.playerClass.activeSkillsList.get(thisActiveSkillID);
                                if (!thisActiveSkill.requiredSkills.isEmpty()) {
                                    for (ActiveSkill.RequiredSkill requiredSkill : thisActiveSkill.requiredSkills) {
                                        if (requiredSkill.activeSkill.id == activeSkill.id) {
                                            if (requiredSkill.activeSkillLevel > newActiveSkillLevel) {
                                                classData.setActiveSkillLevel(thisActiveSkillID, 0);
                                                checkActiveSkillRequirements(classData, thisActiveSkill, 0);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public int arrayLength() {
                        return PlayerClass.classesList.size();
                    }
                }
        );

        this.updateClass = this.registerAction(
                new ClassUpdateCustomAction() {
                    @Override
                    protected void run(int classID, int[] passiveLevels, int[] activeSkillLevels) {
                        if (client.isServer()) {
                            if (client.playerMob.isInCombat() && !client.playerMob.buffManager.hasBuff(RPGBuffs.PASSIVES.OVER_LEVEL)) {
                                client.playerMob.getServerClient().sendChatMessage(new LocalMessage("message", "noupdatesincombat"));
                                System.out.println("updateClass: No updates in combat");
                                return;
                            }

                            ServerClient serverClient = client.getServerClient();
                            PlayerData playerData = PlayerDataList.getPlayerData(serverClient.playerMob);
                            PlayerClassData classData = playerData.getClassesData()[classID];

                            String familyInvalid = null;
                            String levelsInvalid = null;

                            Set<String> passiveFamilies = new HashSet<>();
                            for (int i = 0; i < passiveLevels.length; i++) {
                                Passive passive = classData.playerClass.passivesList.get(i);
                                int assignedLevel = passiveLevels[i];

                                if (assignedLevel > 0 && passive.family != null) {
                                    if (passiveFamilies.contains(passive.family)) {
                                        familyInvalid = passive.family + " (" + passive.stringID + ")";
                                        break;
                                    }
                                    passiveFamilies.add(passive.family);
                                }

                                int effectiveMax = classData.getEffectiveSkillMaxLevel(passive, classData.getLevel(true), passiveLevels);

                                if (assignedLevel < 0 || assignedLevel > effectiveMax) {
                                    levelsInvalid = passive.stringID;
                                    break;
                                }
                            }

                            if (familyInvalid != null) {
                                System.out.println("updateClass: Repeated Passive Family: " + familyInvalid);
                                return;
                            }

                            if (levelsInvalid != null) {
                                System.out.println("updateClass: Wrong assigned Passive Level: " + levelsInvalid);
                                return;
                            }

                            String requirementsInvalid = null;

                            for (int i = 0; i < activeSkillLevels.length; i++) {
                                ActiveSkill skill = classData.playerClass.activeSkillsList.get(i);

                                int assignedLevel = activeSkillLevels[i];
                                int effectiveMax = classData.getEffectiveSkillMaxLevel(classData.playerClass.activeSkillsList.get(i), classData.getLevel(true), activeSkillLevels);

                                if (assignedLevel < 0 || assignedLevel > effectiveMax) {
                                    levelsInvalid = skill.stringID;
                                    break;
                                }

                                if (assignedLevel > 0) {
                                    boolean hasRequired = ClassEntry.hasRequired(skill, activeSkillLevels);
                                    if (!hasRequired) {
                                        requirementsInvalid = skill.stringID;
                                        break;
                                    }
                                }
                            }

                            if (requirementsInvalid != null) {
                                System.out.println("updateClass: Not accomplished active skill requirements: " + requirementsInvalid);
                                return;
                            }

                            if (levelsInvalid != null) {
                                System.out.println("updateClass: Wrong assigned Active Skill Level: " + levelsInvalid);
                                return;
                            }

                            int totalPassive = Arrays.stream(passiveLevels).sum();
                            if (totalPassive <= classData.totalPassivePoints(true)) {
                                int totalActiveSkill = Arrays.stream(activeSkillLevels).sum();
                                if (totalActiveSkill <= classData.totalActiveSkillPoints(true)) {
                                    classData.setPassiveLevels(passiveLevels);
                                    classData.setActiveSkillLevels(activeSkillLevels);
                                    serverClient.getServer().network.sendToAllClients(new UpdateClientClassDataPacket(classData));
                                    playerData.updateAllBuffs(serverClient.playerMob);

                                    boolean someEquippedUpdate = false;
                                    for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                                        if (!equippedActiveSkill.isEmpty() && !equippedActiveSkill.isInCooldown(client.playerMob.getTime()) && equippedActiveSkill.sameClass(classData) && classData.getActiveSkillLevels()[equippedActiveSkill.getActiveSkill().id] < 1) {
                                            equippedActiveSkill.empty();
                                            someEquippedUpdate = true;
                                        }
                                    }
                                    if (someEquippedUpdate) {
                                        serverClient.getServer().network.sendToAllClients(new UpdateClientEquippedActiveSkillsPacket(playerData));
                                    }
                                } else {
                                    System.out.println("updateClass: Not enough Active Skill Points");
                                }
                            } else {
                                System.out.println("updateClass: Not enough Passive Points");
                            }
                        }
                    }
                }
        );

        this.updateEquippedActiveSkills = this.registerAction(
                new EquippedActiveSkillsCustomAction() {
                    @Override
                    protected void run(EquippedActiveSkill[] equippedActiveSkills) {
                        if (client.isServer()) {
                            ServerClient serverClient = client.getServerClient();

                            // No continue if player is in combat
                            if (client.playerMob.isInCombat()) {
                                serverClient.sendChatMessage(new LocalMessage("message", "noupdatesincombat"));
                                System.out.println("updateEquippedActiveSkills: No updates in combat");
                                return;
                            }

                            PlayerMob player = client.playerMob;
                            PlayerData playerData = PlayerDataList.getPlayerData(player);

                            // No continue if player does not have some equipped skill
                            boolean allCorrect = true;
                            for (EquippedActiveSkill equippedActiveSkill : equippedActiveSkills) {
                                if (equippedActiveSkill != null && equippedActiveSkill.getPlayerClass() != null && equippedActiveSkill.getActiveSkill() != null) {
                                    PlayerClassData playerClassData = playerData.getClassesData()[equippedActiveSkill.getPlayerClass().id];
                                    if (playerClassData.getLevel(true) < 1 || playerClassData.getActiveSkillLevels()[equippedActiveSkill.getActiveSkill().id] < 1) {
                                        allCorrect = false;
                                        break;
                                    }
                                }
                            }
                            if (!allCorrect) {
                                System.out.println("updateEquippedActiveSkills: Some Active Skill is not unlocked");
                                return;
                            }

                            // No continue if there are duplicated skills
                            boolean duplicated = false;
                            for (int i = 0; i < PlayerData.EQUIPPED_SKILLS_MAX; i++) {
                                for (int j = 0; j < PlayerData.EQUIPPED_SKILLS_MAX; j++) {
                                    if (i != j && equippedActiveSkills[i].isSameSkill(equippedActiveSkills[j]))
                                        duplicated = true;
                                }
                            }

                            if (duplicated) {
                                System.out.println("updateEquippedActiveSkills: Can't equip duplicated Active Skills");
                                return;
                            }

                            // No continue if any changed skill is in cooldown or inUse. Also sets lastUse to server's lastUse
                            boolean changedInCooldown = false;
                            for (int i = 0; i < PlayerData.EQUIPPED_SKILLS_MAX; i++) {
                                EquippedActiveSkill equippedActiveSkill = equippedActiveSkills[i];
                                EquippedActiveSkill serverEquippedActiveSkill = playerData.equippedActiveSkills[i];
                                if (equippedActiveSkill.isEmpty() || serverEquippedActiveSkill.isEmpty()) {
                                    equippedActiveSkill.restartCooldown();
                                }
                                if (!serverEquippedActiveSkill.isEmpty()) {
                                    if (equippedActiveSkill.isSameSkill(serverEquippedActiveSkill)) {
                                        equippedActiveSkill.setCooldown(serverEquippedActiveSkill);
                                    } else {
                                        if (!serverEquippedActiveSkill.canChange(player.getTime())) {
                                            changedInCooldown = true;
                                        } else {
                                            equippedActiveSkill.restartCooldown();
                                        }
                                    }
                                }
                            }

                            if (changedInCooldown) {
                                System.out.println("updateEquippedActiveSkills: Can't change Active Skills in cooldown or in use");
                                return;
                            }

                            // No continue if player have more than one different skill of the same family
                            boolean sameFamily = false;
                            for (int i = 0; i < equippedActiveSkills.length; i++) {
                                EquippedActiveSkill equippedActiveSkill = equippedActiveSkills[i];
                                if (equippedActiveSkill != null && !equippedActiveSkill.isEmpty()) {
                                    for (int j = 0; j < equippedActiveSkills.length; j++) {
                                        if (i != j && equippedActiveSkills[j].isNotSameSkillButSameFamily(equippedActiveSkill)) {
                                            sameFamily = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (sameFamily) {
                                System.out.println("updateEquippedActiveSkills: Can't use different Active Skills in same family");
                                return;
                            }

                            playerData.equippedActiveSkills = equippedActiveSkills;
                            serverClient.getServer().network.sendToAllClients(new UpdateClientEquippedActiveSkillsPacket(playerData));
                        }
                    }
                }
        );

    }
}
