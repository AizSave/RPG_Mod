package rpgclasses.forms.rpgmenu.components;

import necesse.engine.Settings;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.content.player.Mastery.Mastery;

import java.awt.*;

public class MasteryComponent extends FormContentBox {
    public static int width = 96;
    public static int height = 96;

    public final Color samePoints;
    public final Color differentPoints;

    MasteryIconComponent iconComponent;

    public MasteryComponent(int x, int y, Mastery mastery, boolean hasMastery, Runnable onAdded, Runnable onRemoved) {
        super(x - width / 2, y - height / 2, width, height);

        samePoints = Settings.UI.activeTextColor;

        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);
        if (style == 1) {
            differentPoints = new Color(255, 255, 0);
        } else {
            differentPoints = new Color(102, 102, 0);
        }

        iconComponent = this.addComponent(new MasteryIconComponent(mastery, 0, 0, width, height, hasMastery));
        iconComponent.onClicked(e -> {
            iconComponent.hasMastery = !iconComponent.hasMastery;
            if (iconComponent.hasMastery) {
                onAdded.run();
            } else {
                onRemoved.run();
            }
        });
    }
}
