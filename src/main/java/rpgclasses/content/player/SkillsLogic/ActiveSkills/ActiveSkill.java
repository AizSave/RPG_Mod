package rpgclasses.content.player.SkillsLogic.ActiveSkills;

import necesse.engine.localization.Localization;
import necesse.engine.network.gameNetworkData.GNDItemMap;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.StaminaBuff;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsLogic.Skill;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

abstract public class ActiveSkill extends Skill {
    public boolean newRow;
    public List<RequiredSkill> requiredSkills = new ArrayList<>();

    public ActiveSkill(String stringID, String color, int levelMax, int requiredClassLevel) {
        super(stringID, color, levelMax, requiredClassLevel);
    }

    public ActiveSkill setFamily(String family) {
        this.family = family;
        return this;
    }

    public ActiveSkill addRequiredSkill(ActiveSkill activeSkill, int activeSkillLevel, PlayerClass playerClass) {
        this.requiredSkills.add(new RequiredSkill(activeSkill, activeSkillLevel, playerClass));
        return this;
    }

    public ActiveSkill addRequiredMaxedSkill(ActiveSkill activeSkill, PlayerClass playerClass) {
        this.requiredSkills.add(RequiredSkill.maxLevel(activeSkill, playerClass));
        return this;
    }

    public ActiveSkill setNewRow(boolean newRow) {
        this.newRow = newRow;
        return this;
    }

    @Override
    public List<String> getToolTipsText() {
        List<String> tooltips = new ArrayList<>();
        tooltips.add("§" + color + Localization.translate("activeskills", stringID));
        tooltips.add(" ");
        tooltips.add(Localization.translate("activeskillsdesc", stringID));

        addInfoTooltips(tooltips);

        tooltips.add(" ");
        float rawCooldown = getBaseCooldown();
        float seconds = rawCooldown / 1000f;
        String formattedCooldown = (seconds == (int) seconds)
                ? Integer.toString((int) seconds)
                : String.format("%.2f", seconds);

        int modCooldown = getCooldownModPerLevel();
        if (modCooldown == 0) {
            tooltips.add(Localization.translate("ui", "activeskillcooldown", "seconds", formattedCooldown));
        } else {
            float modSeconds = Math.abs(modCooldown / 1000f);
            String formattedModCooldown = (modSeconds == (int) modSeconds)
                    ? Integer.toString((int) modSeconds)
                    : String.format("%.2f", modSeconds);

            tooltips.add(Localization.translate(
                    "ui", "activeskillcooldownmod",
                    "seconds", formattedCooldown,
                    "sign", modCooldown > 0 ? "+" : "-",
                    "mod", formattedModCooldown
            ));
        }

        if (requiredClassLevel > 1 || !requiredSkills.isEmpty()) {
            tooltips.add(" ");
            if (requiredClassLevel > 1)
                tooltips.add(Localization.translate("ui", "requiredclasslevel", "level", requiredClassLevel));
            if (!requiredSkills.isEmpty()) {
                for (RequiredSkill requiredSkill : requiredSkills) {
                    requiredSkill.addToolTips(tooltips);
                }
            }
        }


        tooltips.add(" ");
        tooltips.add(Localization.translate("ui", "maxlevel", "level", levelMax));

        return tooltips;
    }

    public void addInfoTooltips(List<String> tooltips) {
    }

    @Override
    public void initResources() {
        texture = GameTexture.fromFile("activeskills/" + stringID);
    }

    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        boolean enoughMana = true;

        if(!isInUSe) {
            float consumedStamina = consumedStamina(player);
            if (consumedStamina > 0) {
                StaminaBuff.useStaminaAndGetValid(player, consumedStamina);
            }

            float manaUsage = manaUsage(player, activeSkillLevel);
            if (manaUsage > 0) {
                if (manaUsage > player.getMana()) enoughMana = false;
                player.useMana(manaUsage, player.isServer() ? player.getServerClient() : null);
            }
        }

        int addedCooldown = enoughMana ? 0 : getCooldown(activeSkillLevel);

        long useTime = player.getTime();
        for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
            if (equippedActiveSkill.getActiveSkill() == this) {
                if (equippedActiveSkill.getActiveSkill().isInUseSkill() && !equippedActiveSkill.isInUse()) {
                    equippedActiveSkill.setInUse();
                } else {
                    equippedActiveSkill.startCooldown(playerData, useTime, activeSkillLevel, addedCooldown);
                }
            }
        }
    }

    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        run(player, playerData, activeSkillLevel, seed, isInUse);
    }

    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        run(player, playerData, activeSkillLevel, seed, isInUse);
    }


    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        if(!isInUSe) {
            float consumedStamina = consumedStamina(player);
            if (consumedStamina > 0 && (getStamina(player) < consumedStamina)) {
                return "notenoughstamina";
            }
        }
        return null;
    }

    abstract public int getBaseCooldown();

    public int getCooldown(int activeSkillLevel) {
        return getBaseCooldown() + activeSkillLevel * getCooldownModPerLevel();
    }

    public int getCooldownModPerLevel() {
        return 0;
    }

    public static class RequiredSkill {
        public final ActiveSkill activeSkill;
        public final int activeSkillLevel;
        public final PlayerClass playerClass;

        public RequiredSkill(ActiveSkill activeSkill, int activeSkillLevel, PlayerClass playerClass) {
            this.activeSkill = activeSkill;
            this.activeSkillLevel = activeSkillLevel;
            this.playerClass = playerClass;
        }

        public static RequiredSkill maxLevel(ActiveSkill activeSkill, PlayerClass playerClass) {
            return new RequiredSkill(activeSkill, activeSkill.levelMax, playerClass);
        }

        public void addToolTips(List<String> tooltips) {
            String skillName = Localization.translate("activeskills", activeSkill.stringID);
            tooltips.add(Localization.translate("ui", "requiredactiveskill", "skill", skillName, "level", activeSkillLevel));
        }
    }

    @Override
    public boolean containsComplexTooltips() {
        return true;
    }

    public float consumedStaminaBase() {
        return 0;
    }

    public float consumedStamina(PlayerMob player) {
        float staminaUSage = player.buffManager.getModifier(BuffModifiers.STAMINA_USAGE);
        return consumedStaminaBase() * staminaUSage;
    }

    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 0;
    }

    public boolean isInUseSkill() {
        return false;
    }

    public boolean canUseIfIsInUse() {
        return true;
    }

    public static float getStamina(PlayerMob player) {
        float capacityMod = player.buffManager.getModifier(BuffModifiers.STAMINA_CAPACITY);

        ActiveBuff activeBuff = player.buffManager.getBuff(BuffRegistry.STAMINA_BUFF);
        if (activeBuff == null) {
            return capacityMod;
        } else {
            GNDItemMap gndData = activeBuff.getGndData();
            if (gndData.getBoolean("onCooldown")) {
                return 0;
            } else {
                return Math.abs(1F - gndData.getFloat("stamina")) * capacityMod;
            }
        }
    }

    public static Point2D.Float getRandomClosePlace(PlayerMob player) {
        float angle = GameRandom.globalRandom.getIntBetween(0, 359);
        int distance = GameRandom.globalRandom.getIntBetween(32, 64);
        return new Point2D.Float(
                player.x + GameMath.cos(angle) * distance,
                player.y + GameMath.sin(angle) * distance
        );
    }

    public int getLevel(PlayerData playerData) {
        return playerData.getClassesData()[playerClass.id].getActiveSkillLevels()[id];
    }
}
