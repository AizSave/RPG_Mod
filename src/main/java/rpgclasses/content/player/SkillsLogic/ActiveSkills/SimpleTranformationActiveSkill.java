package rpgclasses.content.player.SkillsLogic.ActiveSkills;

import necesse.engine.network.packet.PacketMobMount;
import necesse.engine.registries.MobRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.gfx.GameResources;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.RPGResources;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.mobs.mount.SkillTransformationMountMob;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.registry.RPGModifiers;

abstract public class SimpleTranformationActiveSkill extends ActiveSkill {

    public SimpleTranformationActiveSkill(String stringID, String color, int levelMax, int requiredClassLevel) {
        super(stringID, color, levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);
        if (isInUse) {
            removeTransformationOnRun(player);
        } else {
            if (player.buffManager.getModifier(RPGModifiers.TRANSFORMATION_DELAY) > 0F) {
                player.dismount();
                ActiveBuff ab = new ActiveBuff(RPGBuffs.TRANSFORMING, player, (int) (castingDuration() * player.buffManager.getModifier(RPGModifiers.TRANSFORMATION_DELAY)), null);
                ab.getGndData().setInt("transformationType", 0);
                ab.getGndData().setString("transformation", stringID);
                ab.getGndData().setInt("particlesColor", getColorInt());
                player.buffManager.addBuff(ab, true);
            } else {
                transform(player);
            }
            for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                if (equippedActiveSkill.getActiveSkill() != this && equippedActiveSkill.isInUse())
                    equippedActiveSkill.startCooldown(playerData, player.getTime(), activeSkillLevel);
            }
        }
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        if (player.isRiding() && !isInUSe && !(playerData.getInUseActiveSkill() instanceof SimpleTranformationActiveSkill))
            return "canotusemounted";
        return super.canActive(player, playerData, isInUSe);
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        if (isInUse) {
            SoundManager.playSound(GameResources.swoosh, SoundEffect.effect(player).volume(0.35F).pitch(1F));
        } else {
            if (player.buffManager.getModifier(RPGModifiers.TRANSFORMATION_DELAY) <= 0F) {
                SoundManager.playSound(GameResources.swoosh, SoundEffect.effect(player).volume(0.35F).pitch(1F));
            }
            for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                if (equippedActiveSkill.getActiveSkill() != this && equippedActiveSkill.isInUse())
                    equippedActiveSkill.startCooldown(playerData, player.getTime(), activeSkillLevel);
            }
        }
    }

    @Override
    public boolean isInUseSkill() {
        return true;
    }

    public void removeTransformationOnRun(PlayerMob player) {
        Mob mount = player.getMount();
        if (mount != null) mount.remove();
    }

    public int castingDuration() {
        return 3000;
    }

    public void transform(PlayerMob player) {
        player.dismount();

        SkillTransformationMountMob transformationMob = (SkillTransformationMountMob) MobRegistry.getMob(getMobStringID(), player.getLevel());
        transformationMob.applyData(playerClass, this);
        transformationMob.colorInt = getColorInt();
        transformationMob.setPos(player.x, player.y, true);
        transformationMob.dx = player.dx;
        transformationMob.dy = player.dy;
        player.mount(transformationMob, true, player.x, player.y, true);
        player.getLevel().entityManager.mobs.add(transformationMob);
        player.getServer().network.sendToClientsWithEntity(new PacketMobMount(player.getUniqueID(), transformationMob.getUniqueID(), true, player.x, player.y), player);
    }

    @Override
    public void registry() {
        super.registry();
        MobRegistry.registerMob(getMobStringID(), getMobClass(), false);
    }

    @Override
    public void initResources() {
        super.initResources();
        if (initMobTexture())
            RPGResources.mobsTexture.put(getMobStringID(), GameTexture.fromFile("mobs/transformations/" + getMobStringID()));
    }

    public boolean initMobTexture() {
        return false;
    }

    abstract public Class<? extends SkillTransformationMountMob> getMobClass();

    public String getMobStringID() {
        return stringID + "mob";
    }
}
