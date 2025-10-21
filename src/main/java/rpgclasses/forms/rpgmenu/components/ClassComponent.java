package rpgclasses.forms.rpgmenu.components;

import necesse.engine.Settings;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormContentIconButton;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.events.FormEventListener;
import necesse.gfx.forms.events.FormInputEvent;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.RPGResources;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.UpcomingPlayerClass;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class ClassComponent extends FormContentBox {
    public static int width = 104;
    public static int height = 116;

    public final AtomicInteger classLevel;
    public final Color sameLevel;
    public final Color differentLevel;

    public final FormLocalLabel levelText;

    public final PlayerClass playerClass;
    public final int currentClassLevel;

    ClassIconComponent classIconComponent;

    public ClassComponent(int x, int y, PlayerClass playerClass, int currentClassLevel) {
        super(x - width / 2, y - height / 2, width, height);

        this.playerClass = playerClass;
        this.currentClassLevel = currentClassLevel;

        sameLevel = Settings.UI.activeTextColor;

        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);
        if (style == 1) {
            differentLevel = new Color(255, 255, 0);
        } else {
            differentLevel = new Color(102, 102, 0);
        }

        classLevel = new AtomicInteger();
        classLevel.set(currentClassLevel);
        levelText = this.addComponent(new FormLocalLabel(getLevelText(classLevel.get()), new FontOptions(16), 0, width / 2, 96 + 4));
        levelText.setColor(sameLevel);

        classIconComponent = this.addComponent(new ClassIconComponent(playerClass, 0, 0, width, 68));
    }

    public void addOnMod(Predicate<Integer> onMod) {
        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);

        int center = width / 2;
        FormContentIconButton buttonAdd = (FormContentIconButton) this.addComponent(new FormContentIconButton(center + 4, 68 + 4, FormInputSize.SIZE_24, ButtonColor.BASE, RPGResources.UI_TEXTURES.add_icon[style])
                .onClicked(c -> {
                    if (onMod.test(classLevel.get() + 1)) {
                        int newLevel = classLevel.incrementAndGet();
                        levelText.setLocalization(getLevelText(newLevel));
                        levelText.setColor(newLevel == currentClassLevel ? sameLevel : differentLevel);
                    }
                }));

        FormContentIconButton buttonRemove = (FormContentIconButton) this.addComponent(new FormContentIconButton(center - 4 - 24, 68 + 4, FormInputSize.SIZE_24, ButtonColor.BASE, RPGResources.UI_TEXTURES.remove_icon[style])
                .onClicked(c -> {
                    if (onMod.test(classLevel.get() - 1)) {
                        int newLevel = classLevel.decrementAndGet();
                        levelText.setLocalization(getLevelText(newLevel));
                        levelText.setColor(newLevel == currentClassLevel ? sameLevel : differentLevel);
                    }
                }));

        FormContentIconButton buttonAdd10 = (FormContentIconButton) this.addComponent(new FormContentIconButton(center + 4 + 24 + 2, 68 + 4 + 4, FormInputSize.SIZE_16, ButtonColor.BASE, RPGResources.UI_TEXTURES.add10_icon[style])
                .onClicked(c -> {
                    int mod = Math.min(10, 999 - classLevel.get());
                    if (onMod.test(classLevel.get() + mod)) {
                        int newLevel = classLevel.addAndGet(mod);
                        levelText.setLocalization(getLevelText(newLevel));
                        levelText.setColor(newLevel == currentClassLevel ? sameLevel : differentLevel);
                    }
                }));

        FormContentIconButton buttonRemove10 = (FormContentIconButton) this.addComponent(new FormContentIconButton(center - 4 - 24 - 2 - 16, 68 + 4 + 4, FormInputSize.SIZE_16, ButtonColor.BASE, RPGResources.UI_TEXTURES.remove10_icon[style])
                .onClicked(c -> {
                    int mod = Math.min(10, classLevel.get());
                    if (onMod.test(classLevel.get() - mod)) {
                        int newLevel = classLevel.addAndGet(-mod);
                        levelText.setLocalization(getLevelText(newLevel));
                        levelText.setColor(newLevel == currentClassLevel ? sameLevel : differentLevel);
                    }
                }));

        if (playerClass instanceof UpcomingPlayerClass) {
            buttonAdd.setActive(false);
            buttonRemove.setActive(false);
            buttonAdd10.setActive(false);
            buttonRemove10.setActive(false);
        }
    }

    public void onClick(FormEventListener<FormInputEvent<FormButton>> listener) {
        classIconComponent.onClicked(listener);
    }

    public GameMessage getLevelText(int classLevel) {
        return new LocalMessage("ui", "level", "level", classLevel);
    }
}
