package rpgclasses.data;

import necesse.engine.localization.Localization;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormExpressionWheel;
import necesse.gfx.gameFont.FontManager;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.item.Item;
import necesse.level.maps.hudManager.floatText.FloatTextFade;
import necesse.level.maps.hudManager.floatText.UniqueFloatText;
import org.jetbrains.annotations.NotNull;
import rpgclasses.RPGResources;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimplePassiveBuffActiveSkill;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleTranformationActiveSkill;
import rpgclasses.packets.ActiveAbilityRunPacket;
import rpgclasses.registry.RPGModifiers;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.BiConsumer;

public class EquippedActiveSkill {
    protected PlayerClass playerClass;
    protected ActiveSkill activeSkill;
    protected long lastUse;
    protected int cooldown;

    public EquippedActiveSkill() {
        this.playerClass = null;
        this.activeSkill = null;
    }

    public EquippedActiveSkill(int classID, int activeSkillID) {
        if (classID != -1 && activeSkillID != -1) {
            this.playerClass = PlayerClass.classesList.get(classID);
            this.activeSkill = playerClass.activeSkillsList.get(activeSkillID);
        }
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public ActiveSkill getActiveSkill() {
        return activeSkill;
    }

    public void modifyForm(FormExpressionWheel.Expression expression, Field drawIcon, Field displayName) throws IllegalAccessException {
        displayName.set(expression, new LocalMessage("activeskills", activeSkill == null ? "empty" : activeSkill.stringID));
        drawIcon.set(expression, new ActiveSkillDrawOptionsModifier());
    }

    public void saveData(SaveData saveData, int position) {
        String dataKeyPrefix = PlayerData.prefixDataName + "equippedactives" + position + "_";

        saveData.addInt(dataKeyPrefix + "class", playerClass == null ? -1 : playerClass.id);
        saveData.addInt(dataKeyPrefix + "activeSkill", activeSkill == null ? -1 : activeSkill.id);
        saveData.addLong(dataKeyPrefix + "lastUse", lastUse);
        saveData.addInt(dataKeyPrefix + "cooldown", cooldown);
    }

    public static EquippedActiveSkill loadData(PlayerMob player, LoadData loadData, int position) {
        String dataKeyPrefix = PlayerData.equippedActiveSkillsDataName + position + "_";

        int classID = loadData.getInt(dataKeyPrefix + "class", -1, false);
        int activeSkillID = loadData.getInt(dataKeyPrefix + "activeSkill", -1, false);
        long lastUse = loadData.getLong(dataKeyPrefix + "lastUse", 0, false);

        EquippedActiveSkill equippedActiveSkill = new EquippedActiveSkill(classID, activeSkillID);

        if (lastUse == -100 && (equippedActiveSkill.activeSkill instanceof SimplePassiveBuffActiveSkill || equippedActiveSkill.activeSkill instanceof SimpleTranformationActiveSkill)) {
            equippedActiveSkill.lastUse = player.getTime();
        } else {
            equippedActiveSkill.lastUse = lastUse;
        }

        equippedActiveSkill.cooldown = loadData.getInt(dataKeyPrefix + "cooldown", 0);

        return equippedActiveSkill;
    }

    public void setupPacket(PacketWriter writer) {
        writer.putNextInt(playerClass == null ? -1 : playerClass.id);
        writer.putNextInt(activeSkill == null ? -1 : activeSkill.id);
        writer.putNextLong(lastUse);
        writer.putNextInt(cooldown);
    }

    public static EquippedActiveSkill applyPacket(PacketReader reader) {
        int classID = reader.getNextInt();
        int activeSkillID = reader.getNextInt();
        long lastUse = reader.getNextLong();
        int cooldown = reader.getNextInt();

        EquippedActiveSkill equippedActiveSkill = new EquippedActiveSkill(classID, activeSkillID);
        equippedActiveSkill.lastUse = lastUse;
        equippedActiveSkill.cooldown = cooldown;

        return equippedActiveSkill;
    }

    public class ActiveSkillDrawOptionsModifier implements BiConsumer<Point, PlayerMob> {
        @Override
        public void accept(Point pos, PlayerMob player) {
            int size = 34;
            GameTexture gameTexture;
            if (activeSkill == null) {
                gameTexture = RPGResources.emptyActiveSkill_texture;
            } else {
                gameTexture = activeSkill.texture;
            }

            gameTexture.initDraw().pos(pos.x - size / 2, pos.y - size / 2 + 5).draw();

            if (playerClass != null && activeSkill != null) {
                int cooldownLeft = getCooldownLeft(player.getTime());
                if (cooldownLeft > 0 || isInUse()) {
                    String cooldownLeftString = getCooldownLeftString(cooldownLeft);

                    FontOptions options = new FontOptions(Item.tipFontOptions);
                    options.color(new Color(255, 102, 102));
                    int width = FontManager.bit.getWidthCeil(cooldownLeftString, options);
                    FontManager.bit.drawString(pos.x + (float) size / 2 - width, pos.y + (float) size / 2 - 5 + 12, cooldownLeftString, options);
                }
            }
        }

        @NotNull
        @Override
        public BiConsumer<Point, PlayerMob> andThen(@NotNull BiConsumer<? super Point, ? super PlayerMob> after) {
            return BiConsumer.super.andThen(after);
        }
    }

    public boolean isInUse() {
        return lastUse == -100;
    }

    public String getCooldownLeftString(int cooldownLeft) {
        if (isInUse()) {
            return Localization.translate("ui", "inuse");
        } else {
            return getTimeLeftString(cooldownLeft);
        }
    }

    public static String getTimeLeftString(int cooldownLeft) {
        String amountString;
        if (cooldownLeft < 1000) {
            amountString = String.format("%.1fs", (float) cooldownLeft / 1000);
        } else {
            amountString = (cooldownLeft / 1000) + "s";
        }
        return amountString;

    }

    public int getCooldown() {
        return cooldown;
    }

    public int getCooldownLeft(long currentTime) {
        long lastUse = this.lastUse;
        int cooldown = getCooldown();

        if (isInUse()) {
            return 0;
        }

        long cooldownLeftLong = (lastUse == 0 || currentTime == 0) ? 0 : (cooldown - (currentTime - lastUse));
        int cooldownLeft;
        if (cooldownLeftLong > Integer.MAX_VALUE) {
            // Extremely high cooldown - prefer to allow the player to use the active skill instead of punishing them
            cooldownLeft = 0;
        } else if (cooldownLeftLong < Integer.MIN_VALUE) {
            cooldownLeft = Integer.MIN_VALUE;
        } else {
            cooldownLeft = (int) cooldownLeftLong;
        }
        return cooldownLeft;
    }

    public boolean isInCooldown(long currentTime) {
        long lastUse = this.lastUse;
        int cooldown = getCooldown();

        return (cooldown - (currentTime - lastUse)) > 0;
    }

    public boolean isSameSkill(EquippedActiveSkill equippedActiveSkill) {
        return !equippedActiveSkill.isEmpty() && this.isSameSkill(equippedActiveSkill.playerClass, equippedActiveSkill.activeSkill);
    }

    public boolean isSameSkill(ActiveSkill activeSkill) {
        return this.isSameSkill(activeSkill.playerClass, activeSkill);
    }

    public boolean isSameSkill(PlayerClass playerClass, ActiveSkill activeSkill) {
        return !this.isEmpty() && this.playerClass.id == playerClass.id && this.activeSkill.id == activeSkill.id;
    }

    public boolean isSameFamily(EquippedActiveSkill equippedActiveSkill) {
        return !equippedActiveSkill.isEmpty() && this.isSameFamily(equippedActiveSkill.playerClass, equippedActiveSkill.activeSkill);
    }

    public boolean isSameFamily(PlayerClass playerClass, ActiveSkill activeSkill) {
        return !this.isEmpty() && activeSkill.family != null && this.playerClass.id == playerClass.id && Objects.equals(this.activeSkill.family, activeSkill.family);
    }

    public boolean isNotSameSkillButSameFamily(EquippedActiveSkill equippedActiveSkill) {
        return isSameFamily(equippedActiveSkill) && !isSameSkill(equippedActiveSkill);
    }

    public boolean isNotSameSkillButSameFamily(PlayerClass playerClass, ActiveSkill activeSkill) {
        return isSameFamily(playerClass, activeSkill) && !isSameSkill(playerClass, activeSkill);
    }

    public boolean sameClass(PlayerClassData classData) {
        return sameClass(classData.playerClass);
    }

    public boolean sameClass(PlayerClass playerClass) {
        return playerClass.id == this.playerClass.id;
    }

    public boolean isEmpty() {
        return playerClass == null || activeSkill == null;
    }

    public void empty() {
        playerClass = null;
        activeSkill = null;
        lastUse = 0;
        cooldown = 0;
    }

    public void update(PlayerClass playerClass, ActiveSkill activeSkill) {
        empty();
        this.playerClass = playerClass;
        this.activeSkill = activeSkill;
    }

    public void startCooldown(PlayerData playerData, long currentTime, int skillLevel) {
        this.startCooldown(playerData, currentTime, skillLevel, 0);
    }

    public void startCooldown(PlayerData playerData, long currentTime, int skillLevel, int addedCooldown) {
        this.startCustomCooldown(playerData, currentTime, activeSkill.getCooldown(skillLevel) + addedCooldown);
    }

    public void startCustomCooldown(PlayerData playerData, long currentTime, int cooldown) {
        this.lastUse = currentTime;
        float mod = 1;
        if (playerData != null) {
            if (playerData.hasMasterySkill(Mastery.CHRONOMANCER)) mod -= 0.2F;
        }
        this.cooldown = (int) (cooldown * mod);
    }

    public void modCooldown(int mod) {
        this.cooldown = Math.max(0, cooldown + mod);
    }

    public void restartCooldown() {
        this.lastUse = 0;
        this.cooldown = 0;
    }

    public void setInUse() {
        this.lastUse = -100;
        this.cooldown = 0;
    }

    public void setCooldown(EquippedActiveSkill equippedActiveSkill) {
        startCustomCooldown(null, equippedActiveSkill.lastUse, equippedActiveSkill.cooldown);
    }

    public boolean canChange(long currentTime) {
        if (isEmpty()) return true;
        if (isInUse()) return false;
        return !isInCooldown(currentTime);
    }

    public void tryClientRun(PlayerMob player, int skillSlot) {
        if (player.isClient()) {
            String canActiveError = this.canActive(player);

            if (canActiveError != null) {
                if (!canActiveError.isEmpty()) {
                    FloatTextFade text = new UniqueFloatText(player.getX(), player.getY() - 20, Localization.translate("message", canActiveError), (new FontOptions(16)).outline().color(new Color(200, 100, 100)), "activeerror") {
                        public int getAnchorX() {
                            return player.getX();
                        }

                        public int getAnchorY() {
                            return player.getY() - 20;
                        }
                    };
                    player.getLevel().hudManager.addElement(text);
                }
                return;
            }

            int seed = Item.getRandomAttackSeed(GameRandom.globalRandom);

            PlayerData playerData = PlayerDataList.getPlayerData(player);
            int activeSkillLevel = activeSkill.getLevel(playerData);

            activeSkill.runClient(player, playerData, activeSkillLevel, seed, isInUse());
            player.getClient().network.sendPacket(new ActiveAbilityRunPacket(player.getClient().getSlot(), skillSlot));
        }
    }

    public void run(PlayerMob player) {
        int seed = Item.getRandomAttackSeed(GameRandom.globalRandom);

        PlayerData playerData = PlayerDataList.getPlayerData(player);
        int activeSkillLevel = activeSkill.getLevel(playerData);

        if (player.isClient()) activeSkill.runClient(player, playerData, activeSkillLevel, seed, isInUse());
        if (player.isServer()) activeSkill.runServer(player, playerData, activeSkillLevel, seed, isInUse());
    }

    public String canActive(PlayerMob player) {
        if (player.buffManager.getModifier(RPGModifiers.NO_SKILLS)) return "";

        if (this.isEmpty()) return "equipfirst";
        ActiveSkill activeSkill = this.activeSkill;

        PlayerData playerData = PlayerDataList.getPlayerData(player);
        PlayerClassData playerClassData = playerData.getClassesData()[playerClass.id];
        int activeSkillLevel = playerClassData.getActiveSkillLevels()[activeSkill.id];

        if (playerClassData.getLevel(player.isServer()) < 1 || activeSkillLevel < 1) return "noactiveskill";

        boolean isInUseSkill = activeSkill.isInUseSkill();
        if (isInUseSkill) {
            ActiveSkill inUseSkill = playerData.getInUseActiveSkill();
            if (inUseSkill != null && inUseSkill != this.activeSkill && !(this.activeSkill instanceof SimpleTranformationActiveSkill && inUseSkill instanceof SimpleTranformationActiveSkill))
                return "anotheractiveskillinuse";
        }

        boolean isInUse = isInUseSkill && isInUse();

        if (isInUse && !activeSkill.canUseIfIsInUse()) return "activeskillinuse";

        long currentTime = player.getLevel().getTime();

        if (!isInUse) {
            int cooldownLeft = getCooldownLeft(currentTime);
            if (cooldownLeft > 0) return "incooldown";
        }

        return activeSkill.canActive(player, playerData, isInUse);
    }
}
