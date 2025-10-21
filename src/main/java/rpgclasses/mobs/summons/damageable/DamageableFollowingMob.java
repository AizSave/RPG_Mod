package rpgclasses.mobs.summons.damageable;

import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.Control;
import necesse.engine.localization.Localization;
import necesse.engine.modifiers.ModifierValue;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.util.GameMath;
import necesse.engine.window.GameWindow;
import necesse.engine.window.WindowManager;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.summon.summonFollowingMob.attackingFollowingMob.AttackingFollowingMob;
import necesse.gfx.GameColor;
import necesse.gfx.Renderer;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.ProgressBarDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTooltips.*;
import necesse.gfx.ui.GameInterfaceStyle;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.RPGResources;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract public class DamageableFollowingMob extends AttackingFollowingMob {
    public static String prefixDataName = "rpgmod_summon_";
    public static String initialMaxHealthDataName = prefixDataName + "initialMaxHealth";
    public static String keepDecreaseDataName = prefixDataName + "keepDecrease";

    int initialMaxHealth = 0;
    float keepDecrease = 0;

    public DamageableFollowingMob(int health) {
        super(health);
        this.isStatic = false;
    }

    @Override
    public void applyLoadData(LoadData load) {
        super.applyLoadData(load);
        initialMaxHealth = load.getInt(initialMaxHealthDataName);
        keepDecrease = load.getFloat(keepDecreaseDataName);
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addInt(initialMaxHealthDataName, initialMaxHealth);
        save.addFloat(keepDecreaseDataName, keepDecrease);
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        initialMaxHealth = reader.getNextInt();
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextInt(initialMaxHealth);
    }

    @Override
    public boolean onMouseHover(GameCamera camera, PlayerMob perspective, boolean debug) {
        if (!debug && !this.isVisible()) {
            return false;
        } else {
            ListGameTooltips tips = new ListGameTooltips();
            this.addHoverTooltips(tips, debug);
            if (debug) {
                if (!WindowManager.getWindow().isKeyDown(340)) {
                    this.addDebugTooltips(tips);
                } else {
                    List<FairTypeTooltip> modTips = this.buffManager.getModifierTooltips().stream()
                            .map((mf) -> mf.toTooltip(true))
                            .collect(Collectors.toList());
                    if (modTips.isEmpty()) {
                        tips.add(new StringTooltips(Localization.translate("bufftooltip", "nomodifiers"), GameColor.YELLOW));
                    } else {
                        tips.addAll(modTips);
                    }
                }
            }

            if (this.canInteract(perspective)) {
                Renderer.setCursor(GameWindow.CURSOR.INTERACT);
                if (Settings.showControlTips) {
                    String controlMsg = this.getInteractTip(perspective, debug);
                    if (controlMsg != null) {
                        tips.add(new InputTooltip(Control.MOUSE2, controlMsg, this.inInteractRange(perspective) ? 1.0F : 0.7F));
                    }
                }
            }

            GameTooltipManager.addTooltip(tips, TooltipLocation.INTERACT_FOCUS);
            return true;
        }
    }

    public float getHealthDecreasePerSecond() {
        return 0.02F;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (getHealthDecreasePerSecond() > 0) {
            int maxHealth = getMaxHealthFlat();
            if (maxHealth <= 1) {
                this.remove(0, 0, null, true);
            } else {
                float decrease = initialMaxHealth * getHealthDecreasePerSecond() / 20 + keepDecrease;
                int trueDecrease = (int) decrease;
                float keeping = decrease - trueDecrease;
                if (keeping > 0) keepDecrease = keeping;
                this.setHealth(getHealth() - trueDecrease);
                this.setMaxHealth(maxHealth - trueDecrease);
            }
        }
    }

    @Override
    public void clientTick() {
        super.clientTick();
        if (getHealthDecreasePerSecond() > 0) {
            int maxHealth = getMaxHealthFlat();
            if (maxHealth <= 1) {
                this.remove(0, 0, null, true);
            } else {
                float decrease = initialMaxHealth * getHealthDecreasePerSecond() / 20 + keepDecrease;
                int trueDecrease = (int) decrease;
                float keeping = decrease - trueDecrease;
                if (keeping > 0) keepDecrease = keeping;
                this.setMaxHealth(maxHealth - trueDecrease);
            }
        }
    }

    abstract public int getHealthStat(PlayerMob player, PlayerData playerData);

    abstract public float getDamageStat(PlayerMob player, PlayerData playerData);


    public void updateStats(PlayerMob player, PlayerData playerData) {
        this.updateStats(player, playerData, 1F);
    }

    public void updateStats(PlayerMob player, PlayerData playerData, float mod) {
        this.updateStats(player, playerData, mod, mod);
    }

    public void updateStats(PlayerMob player, PlayerData playerData, float healthMod, float damageMod) {
        this.updateStats((int) (getHealthStat(player, playerData) * healthMod), getDamageStat(player, playerData) * damageMod);
    }

    public void updateStats(int health, float damage) {
        this.setMaxHealth(Math.max(1, health));
        this.setHealthHidden(Math.max(1, health));
        initialMaxHealth = Math.max(1, health);
        this.updateDamage(new GameDamage(DamageTypeRegistry.SUMMON, damage));
    }

    @Override
    public boolean isHealthBarVisible() {
        return super.isHealthBarVisible() && getHealth() < getMaxHealth() - 2;
    }

    public Rectangle getTimeBarBounds(int x, int y, int style) {
        Rectangle selectBox = this.getSelectBox(x, y);
        int width = GameMath.limit(selectBox.width, 32, 64);
        x = selectBox.x + selectBox.width / 2 - width / 2;
        y = selectBox.y - RPGResources.UI_TEXTURES.timebar_texture[style].getHeight() - 1;
        if (this.isHealthBarVisible()) {
            y -= 11;
        }
        return new Rectangle(x, y, width, RPGResources.UI_TEXTURES.timebar_texture[style].getHeight());
    }

    @Override
    public void addStatusBarDrawable(OrderableDrawables list, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        super.addStatusBarDrawable(list, level, x, y, tickManager, camera, perspective);

        if (getHealthDecreasePerSecond() > 0) {
            int style = GameInterfaceStyle.styles.indexOf(Settings.UI);

            Rectangle bounds = this.getTimeBarBounds(x, y, style);

            int maxHealth = this.getMaxHealth();
            float perc = GameMath.limit((maxHealth - 1f) / (initialMaxHealth - 1f), 0f, 1f);
            int drawX = camera.getDrawX(bounds.x);
            int drawY = camera.getDrawY(bounds.y);
            GameLight light = level.getLightLevel((bounds.x + bounds.width / 2) / 32, (bounds.y + 4) / 32);
            float alphaMod = GameMath.lerp(light.getFloatLevel(), 0.2F, 1.0F);
            Color statusColor = getStatusColorOrangePref(perc, 0.75F, 0.7F, 2.2F);
            Color finalFillColor = new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), (int) (240.0F * alphaMod));
            Color finalBackgroundColor = new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), (int) (190.0F * alphaMod));
            DrawOptions options = (new ProgressBarDrawOptions(Settings.UI.healthbar_small_fill, bounds.width)).color(finalBackgroundColor).addBar(RPGResources.UI_TEXTURES.timebar_texture[style], perc).color(finalFillColor).minWidth(4).end().pos(drawX, drawY);
            list.add((tm) -> options.draw());
        }
    }

    public static Color getStatusColorOrangePref(float percent, float saturation, float brightness, float exponent) {
        percent = (float) Math.pow(percent, exponent);
        float hue = 0.1f + (0.04f * percent);
        return Color.getHSBColor(hue, saturation, brightness);
    }

    @Override
    public Stream<ModifierValue<?>> getDefaultModifiers() {
        return Stream.of(
                new ModifierValue<>(BuffModifiers.HEALTH_REGEN_FLAT).max(0F),
                new ModifierValue<>(BuffModifiers.COMBAT_HEALTH_REGEN_FLAT).max(0F),
                new ModifierValue<>(BuffModifiers.HEALTH_REGEN).max(0F),
                new ModifierValue<>(BuffModifiers.COMBAT_HEALTH_REGEN).max(0F)
        );
    }

    @Override
    public void init() {
        super.init();
        Mob followingMob = getFollowingMob();
        if (followingMob instanceof PlayerMob) {
            PlayerData playerData = PlayerDataList.getPlayerData((PlayerMob) followingMob);
            if (playerData.hasMasterySkill(Mastery.IRON_INVOKER)) {
                Mastery.IRON_INVOKER.giveDatalessSecondaryPassiveBuff(this, 3600_000);
            }
        }
    }
}
