package rpgclasses.patches;

import necesse.engine.GlobalData;
import necesse.engine.input.Input;
import necesse.engine.input.InputPosition;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.state.MainGame;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.pickup.ItemPickupEntity;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.forms.components.FormExpressionWheel;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.placeableItem.objectItem.ObjectItem;
import necesse.level.maps.Level;
import net.bytebuddy.asm.Advice;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.mount.TransformationMountMob;
import rpgclasses.packets.TransformationAbility1Packet;
import rpgclasses.packets.TransformationAbility2Packet;
import rpgclasses.registry.RPGControls;

import java.util.HashSet;

public class PlayerMobPatches {
    @ModMethodPatch(target = PlayerMob.class, name = "applyLoadData", arguments = {LoadData.class})
    public static class applyLoadData {

        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob This, @Advice.Argument(0) LoadData loadData) {
            PlayerData playerData = PlayerDataList.initPlayerData(This);
            playerData.loadData(This, loadData);
        }

    }

    @ModMethodPatch(target = PlayerMob.class, name = "addSaveData", arguments = {SaveData.class})
    public static class addSaveData {

        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob This, @Advice.Argument(0) SaveData saveData) {
            PlayerData player = PlayerDataList.getPlayerData(This);
            if (player != null) player.saveData(saveData);
        }

    }

    @ModMethodPatch(target = PlayerMob.class, name = "setupSpawnPacket", arguments = {PacketWriter.class})
    public static class setupSpawnPacket {

        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob This, @Advice.Argument(0) PacketWriter writer) {
            PlayerData playerData = PlayerDataList.getPlayerData(This.playerName, This.isServer());
            if (playerData != null) {
                playerData.setupPacket(writer);
            }
        }

    }

    @ModMethodPatch(target = PlayerMob.class, name = "applySpawnPacket", arguments = {PacketReader.class})
    public static class applySpawnPacket {

        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob This, @Advice.Argument(0) PacketReader reader) {
            if (reader.hasNext()) {
                PlayerData playerData = PlayerData.applyPacket(reader);
                PlayerDataList.setPlayerData(playerData.playerName, playerData, This.isServer());
                playerData.updateAllBuffs(This);

            }
        }

    }


    @ModMethodPatch(target = PlayerMob.class, name = "restore", arguments = {})
    public static class restore {

        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob This) {
            PlayerData playerData = PlayerDataList.getPlayerData(This);
            if (playerData != null) {
                playerData.updateAllBuffs(This);
                for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                    equippedActiveSkill.restartCooldown();
                }
            }
        }

    }

    @ModMethodPatch(target = PlayerMob.class, name = "onDeath", arguments = {Attacker.class, HashSet.class})
    public static class onDeath {

        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob This) {
            PlayerData playerData = PlayerDataList.getPlayerData(This);
            if (playerData != null) {
                playerData.lastDeath = This.getTime();
                if (playerData.grabbedObject != null) {
                    Level level = This.getLevel();
                    if (!level.isProtected && playerData.grabbedObject.canPlace(level, This.getTileX(), This.getTileY(), 2, true) == null) {
                        playerData.grabbedObject.placeObject(level, This.getTileX(), This.getTileY(), 2, true);
                    } else {
                        ObjectItem objectItem = playerData.grabbedObject.getObjectItem();
                        if (objectItem != null) {
                            level.entityManager.pickups.add(
                                    new ItemPickupEntity(level, new InventoryItem(objectItem), This.x, This.y, 0, 0)
                            );
                        }
                    }
                }
            }
        }

    }

    @ModMethodPatch(target = PlayerMob.class, name = "startExpression", arguments = {FormExpressionWheel.Expression.class})
    public static class startExpression {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        static boolean onEnter(@Advice.This PlayerMob This, @Advice.Argument(0) FormExpressionWheel.Expression expression) {
            if (This.isClient()) {
                Client client = ((MainGame) GlobalData.getCurrentState()).getClient();

                if (client.getPlayer() == This) {
                    int skillSlot;
                    if (expression == FormExpressionWheel.Expression.SAD) {
                        skillSlot = 0;
                    } else if (expression == FormExpressionWheel.Expression.SURPRISED) {
                        skillSlot = 1;
                    } else if (expression == FormExpressionWheel.Expression.ANGRY) {
                        skillSlot = 2;
                    } else {
                        skillSlot = 3;
                    }

                    PlayerData playerData = PlayerDataList.getPlayerData(This);
                    EquippedActiveSkill equippedActiveSkill = playerData.equippedActiveSkills[skillSlot];
                    equippedActiveSkill.tryClientRun(This, skillSlot);
                }
            }

            return true;
        }
    }


    @ModMethodPatch(target = PlayerMob.class, name = "tickControls", arguments = {MainGame.class, boolean.class, GameCamera.class})
    public static class tickControls {

        @Advice.OnMethodExit
        static void onExit(@Advice.This PlayerMob player, @Advice.Argument(0) MainGame mainGame, @Advice.Argument(1) boolean isGameTick, @Advice.Argument(2) GameCamera camera) {

            if (RPGControls.TRANSFORMATION_ABILITY_1.isDown()) {
                if (player != null) {
                    Mob mount = player.getMount();
                    if (mount instanceof TransformationMountMob) {
                        TransformationMountMob transformation = (TransformationMountMob) mount;
                        if (transformation.canRunClick(player)) {
                            InputPosition inputPosition = Input.mousePos;
                            int mouseLevelX = inputPosition.sceneX + camera.getX();
                            int mouseLevelY = inputPosition.sceneY + camera.getY();

                            transformation.clickRunClient(player.getLevel(), mouseLevelX, mouseLevelY, player);

                            player.getClient().network.sendPacket(new TransformationAbility1Packet(player.getClient().getSlot(), mouseLevelX, mouseLevelY));
                        }
                    }
                }
            }
            if (RPGControls.TRANSFORMATION_ABILITY_2.isDown()) {
                if (player != null) {
                    Mob mount = player.getMount();
                    if (mount instanceof TransformationMountMob) {
                        TransformationMountMob transformation = (TransformationMountMob) mount;
                        if (transformation.canRunSecondaryClick(player)) {
                            InputPosition inputPosition = Input.mousePos;
                            int mouseLevelX = inputPosition.sceneX + camera.getX();
                            int mouseLevelY = inputPosition.sceneY + camera.getY();

                            transformation.secondaryClickRunClient(player.getLevel(), mouseLevelX, mouseLevelY, player);

                            player.getClient().network.sendPacket(new TransformationAbility2Packet(player.getClient().getSlot(), mouseLevelX, mouseLevelY));
                        }
                    }
                }
            }
        }

    }
}