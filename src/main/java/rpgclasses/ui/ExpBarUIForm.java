package rpgclasses.ui;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.Renderer;
import necesse.gfx.forms.Form;

import java.awt.*;

public class ExpBarUIForm extends Form {
    public ExpBarUIForm(String name, int width, int height) {
        super(name, width, height);
    }

    @Override
    public void draw(TickManager tickManager, PlayerMob perspective, Rectangle renderBox) {
        super.draw(tickManager, perspective, renderBox);

        if (CustomUIManager.expBar.vertical) {
            int filled = (int) (CustomUIManager.expBar.barPercent * (getHeight() - 6));

            Renderer.initQuadDraw(getWidth() + 4, 4 + filled)
                    .color(0, 0.6F, 0, 0.6F)
                    .draw(getX() - 2, getY() + getHeight() - filled - 4);

            Renderer.initQuadDraw(getWidth(), filled)
                    .color(0, 255, 0)
                    .draw(getX(), getY() + getHeight() - filled - 3);
        } else {
            float filled = CustomUIManager.expBar.barPercent * (getWidth() - 6);
            Renderer.initQuadDraw((int) (4 + filled), getHeight() + 4).color(0, 0.6F, 0, 0.6F).draw(getX() + 1, getY() - 2);
            Renderer.initQuadDraw((int) filled, getHeight()).color(0, 255, 0).draw(getX() + 3, getY());
        }
    }
}