package rpgclasses.forms.rpgmenu.components;

import necesse.engine.Settings;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.Client;
import necesse.gfx.forms.components.*;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.events.FormEventListener;
import necesse.gfx.forms.events.FormInputEvent;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.RPGResources;
import rpgclasses.content.player.Attribute;
import rpgclasses.data.PlayerData;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;

public class AttributeComponent extends FormContentBox {
    public static int width = 104;
    public static int height = 144;

    public final AtomicInteger attributePoints;
    public final Color samePoints;
    public final Color differentPoints;

    public final FormLocalLabel levelText;
    public final FormLocalLabel pointsText;

    public final int currentAttributePoints;

    public AttributeComponent(Client client, int x, int y, Attribute attribute, int currentAttributePoints) {
        super(x - width / 2, y - height / 2, width, height);

        this.currentAttributePoints = currentAttributePoints;

        samePoints = Settings.UI.activeTextColor;

        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);
        if (style == 1) {
            differentPoints = new Color(255, 255, 0);
        } else {
            differentPoints = new Color(102, 102, 0);
        }

        attributePoints = new AtomicInteger();
        attributePoints.set(currentAttributePoints);

        pointsText = this.addComponent(new FormLocalLabel(getPointsText(attributePoints.get()), new FontOptions(12), 0, width / 2, 88 + 4));
        pointsText.setColor(samePoints);

        levelText = this.addComponent(new FormLocalLabel(getLevelText(attributePoints.get()), new FontOptions(16), 0, width / 2, 88 + 4 + 16 + 4));

        int extraPoints = client.getPlayer().buffManager.getModifier(attribute.ownModifier);
        if (extraPoints > 0) {
            this.addComponent(new FormLabel("(+" + extraPoints + ")", new FontOptions(12), 0, width / 2, 88 + 4 + 16 + 4 + 16 + 4));
        }

        this.addComponent(new AttributeIconComponent(attribute, 0, 0, width, 60));
    }

    public void addOnMod(FormEventListener<FormInputEvent<FormButton>> onMod) {
        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);

        int center = width / 2;
        this.addComponent(new FormContentIconButton(center + 4, 60 + 4, FormInputSize.SIZE_24, ButtonColor.BASE, RPGResources.UI_TEXTURES.add_icon[style])
                .onClicked(c -> {
                    if (attributePoints.get() < 999) {
                        updateTexts(attributePoints.incrementAndGet());
                        onMod.onEvent(c);
                    }
                }));

        this.addComponent(new FormContentIconButton(center - 4 - 24, 60 + 4, FormInputSize.SIZE_24, ButtonColor.BASE, RPGResources.UI_TEXTURES.remove_icon[style])
                .onClicked(c -> {
                    if (attributePoints.get() > 0) {
                        updateTexts(attributePoints.decrementAndGet());
                        onMod.onEvent(c);
                    }
                }));

        this.addComponent(new FormContentIconButton(center + 4 + 24 + 2, 60 + 4 + 4, FormInputSize.SIZE_16, ButtonColor.BASE, RPGResources.UI_TEXTURES.add10_icon[style])
                .onClicked(c -> {
                    if (attributePoints.get() < 999) {
                        int mod = Math.min(10, 999 - attributePoints.get());
                        updateTexts(attributePoints.addAndGet(mod));
                        onMod.onEvent(c);
                    }
                }));

        this.addComponent(new FormContentIconButton(center - 4 - 24 - 2 - 16, 60 + 4 + 4, FormInputSize.SIZE_16, ButtonColor.BASE, RPGResources.UI_TEXTURES.remove10_icon[style])
                .onClicked(c -> {
                    if (attributePoints.get() > 0) {
                        int mod = -Math.min(10, attributePoints.get());
                        updateTexts(attributePoints.addAndGet(mod));
                        onMod.onEvent(c);
                    }
                }));
    }

    public void updateTexts(int usedPoints) {
        levelText.setLocalization(getLevelText(usedPoints));
        pointsText.setLocalization(getPointsText(usedPoints));
        pointsText.setColor(usedPoints == currentAttributePoints ? samePoints : differentPoints);
    }

    public GameMessage getLevelText(int usedPoints) {
        float level = PlayerData.pointsConversion(usedPoints);
        if (level == (int) level) {
            return new LocalMessage("ui", "level", "level", (int) level);
        } else {
            BigDecimal levelBD = new BigDecimal(Float.toString(level));
            levelBD = levelBD.setScale(2, RoundingMode.DOWN);
            return new LocalMessage("ui", "level", "level", levelBD.toString());
        }
    }

    public GameMessage getPointsText(int usedPoints) {
        return new LocalMessage("ui", "points", "points", usedPoints);
    }
}
