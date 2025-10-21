package rpgclasses.ui;

import necesse.engine.GlobalData;
import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.Input;
import necesse.engine.input.InputPosition;
import necesse.engine.network.client.Client;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.state.MainGame;
import necesse.engine.util.GameMath;
import necesse.engine.window.WindowManager;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.staticBuffs.StaminaBuff;
import necesse.gfx.Renderer;
import necesse.gfx.forms.Form;
import rpgclasses.mobs.mount.SkillTransformationMountSimpleAbilityMob;
import rpgclasses.mobs.mount.TransformationMountMob;
import rpgclasses.utils.RPGRenderer;

import java.awt.*;

public class CursorCenterUIForm extends Form {

    public CursorCenterUIForm(int width, int height) {
        super("transformationabilitiesui", width, height);
        canBePutOnTopByClick = false;
    }

    @Override
    public void draw(TickManager tickManager, PlayerMob perspective, Rectangle renderBox) {
        if (WindowManager.getWindow().getCursor() == null) return;

        Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
        Mob mount = client.getPlayer().getMount();
        if (mount instanceof TransformationMountMob) {
            int cursorWidth = getCursorWidth();
            int cursorHeight = getCursorHeight();
            Point cursorCenter = getCursorCenter(cursorWidth, cursorHeight);

            int reference = Math.max(cursorWidth, cursorHeight);

            int separation = (int) (reference * 0.6F);

            int thickness = (int) (reference * 0.4F);
            int length = 86;

            float percentToMax;

            TransformationMountMob transformationMount = (TransformationMountMob) mount;
            if (transformationMount.hasClick()) {
                int timeToMax = (int) (transformationMount.nextRunClick - client.getPlayer().getTime());
                if (timeToMax <= 0) {
                    percentToMax = 0;
                } else {
                    percentToMax = (float) timeToMax / transformationMount.clickCooldown();
                    if (percentToMax > 1) percentToMax = 1;
                }

                int size = (int) ((1 - percentToMax) * length);
                RPGRenderer.drawArc(cursorCenter.x, cursorCenter.y, separation + reference, thickness, 180 - length / 2F, length, 20, RPGRenderer.Alignment.START, 1, 0, 1, 0.2F);
                RPGRenderer.drawArc(cursorCenter.x, cursorCenter.y, separation + reference, thickness, 180 - length / 2F, size, 20, RPGRenderer.Alignment.START, 1, 0, 1, 1);
            }

            if (transformationMount.hasSecondaryClick()) {
                int timeToMax = (int) (transformationMount.nextRunSecondaryClick - client.getPlayer().getTime());
                if (timeToMax <= 0) {
                    percentToMax = 0;
                } else {
                    percentToMax = (float) timeToMax / transformationMount.secondaryClickCooldown();
                    if (percentToMax > 1) percentToMax = 1;
                }

                int size = (int) ((1 - percentToMax) * length);
                RPGRenderer.drawArc(cursorCenter.x, cursorCenter.y, separation + reference, thickness, length / 2F, length, 20, RPGRenderer.Alignment.END, 0, 1, 0, 0.2F);
                RPGRenderer.drawArc(cursorCenter.x, cursorCenter.y, separation + reference, thickness, length / 2F, size, 20, RPGRenderer.Alignment.END, 0, 1, 0, 1);
            }

            if (transformationMount.staminaBasedMountAbility() || transformationMount instanceof SkillTransformationMountSimpleAbilityMob) {
                boolean setRed = false;

                if (transformationMount.staminaBasedMountAbility()) {
                    ActiveBuff buff = perspective.buffManager.getBuff(BuffRegistry.STAMINA_BUFF);
                    if (buff != null && buff.getGndData().getBoolean("onCooldown")) setRed = true;

                    float stamina = StaminaBuff.getCurrentStamina(perspective);
                    stamina = GameMath.limit(stamina, 0.0F, 1.0F);
                    percentToMax = 1 - Math.abs(stamina - 1.0F);
                } else {
                    SkillTransformationMountSimpleAbilityMob skillMountSimpleAbilityMob = (SkillTransformationMountSimpleAbilityMob) transformationMount;
                    int timeToMax = (int) (skillMountSimpleAbilityMob.abilityCooldown - client.getPlayer().getTime());
                    if (timeToMax <= 0) {
                        percentToMax = 0;
                    } else {
                        percentToMax = (float) timeToMax / skillMountSimpleAbilityMob.mountAbilityCooldown();
                        if (percentToMax > 1) percentToMax = 1;
                    }
                }

                int size = (int) ((1 - percentToMax) * length);
                RPGRenderer.drawArc(cursorCenter.x, cursorCenter.y, separation + reference, thickness, 90, length, 20, RPGRenderer.Alignment.CENTER, 1, setRed ? 0.4F : 1, setRed ? 0.4F : 1, 0.2F);
                RPGRenderer.drawArc(cursorCenter.x, cursorCenter.y, separation + reference, thickness, 90, size, 20, RPGRenderer.Alignment.CENTER, 1, setRed ? 0.4F : 1, setRed ? 0.4F : 1, 1);
            }

        }
    }

    public Point getCursorCenter(int cursorWidth, int cursorHeight) {
        InputPosition mousePos = Input.mousePos;
        return new Point(mousePos.hudX + cursorWidth / 2, mousePos.hudY + cursorHeight / 2);
    }

    public int getCursorWidth() {
        return (int) (17 * Renderer.getCursorSizeZoom(Settings.cursorSize));
    }

    public int getCursorHeight() {
        return (int) (17 * Renderer.getCursorSizeZoom(Settings.cursorSize));
    }
}
