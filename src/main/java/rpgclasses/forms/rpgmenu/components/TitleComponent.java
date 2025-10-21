package rpgclasses.forms.rpgmenu.components;

import necesse.engine.Settings;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.GameInterfaceStyle;

import java.awt.*;

public class TitleComponent extends FormContentBox {
    int width;
    int height;

    public TitleComponent(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.width = width;
        this.height = height;
    }

    public void updateContent() {
        this.clearComponents();

        int fontSize = 16;

        FormLocalLabel localLabel = this.addComponent(new FormLocalLabel(
                "ui", "menutitle",
                new FontOptions(fontSize), 0, width / 2, (height - fontSize) / 2
        ));
        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);
        if (style == 1) {
            localLabel.setColor(new Color(255, 215, 0));
        } else {
            localLabel.setColor(new Color(0, 51, 102));
        }
    }

}
