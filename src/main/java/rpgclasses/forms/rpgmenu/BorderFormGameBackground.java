package rpgclasses.forms.rpgmenu;

import necesse.engine.Settings;
import necesse.engine.util.PointSetAbstract;
import necesse.gfx.GameBackground;
import necesse.gfx.drawOptions.texture.SharedTextureDrawOptions;

import java.awt.*;
import java.util.Set;

public class BorderFormGameBackground extends GameBackground {
    int padding;

    public BorderFormGameBackground(int padding) {
        this.padding = padding;
    }

    @Override
    public SharedTextureDrawOptions getOutlineDrawOptions(int x, int y, int width, int height) {
        return Settings.UI.form.getOutlineDrawOptions(x, y, width, height);
    }

    @Override
    public SharedTextureDrawOptions getCenterDrawOptions(int x, int y, int width, int height) {
        return Settings.UI.form.getCenterDrawOptions(x, y, width, height);
    }

    @Override
    public SharedTextureDrawOptions getDrawOptions(int x, int y, int width, int height) {
        return Settings.UI.form.getDrawOptions(x, y, width, height);
    }

    @Override
    public SharedTextureDrawOptions getOutlineEdgeDrawOptions(int x, int y, int width, int height) {
        return Settings.UI.form.getOutlineEdgeDrawOptions(x, y, width, height);
    }

    @Override
    public SharedTextureDrawOptions getCenterEdgeDrawOptions(int x, int y, int width, int height) {
        return Settings.UI.form.getCenterEdgeDrawOptions(x, y, width, height);
    }

    @Override
    public SharedTextureDrawOptions getEdgeDrawOptions(int x, int y, int width, int height) {
        return Settings.UI.form.getEdgeDrawOptions(x, y, width, height);
    }

    @Override
    public SharedTextureDrawOptions getTiledEdgeDrawOptions(int x, int y, int xPadding, int yPadding, PointSetAbstract<?> pointSetAbstract, int tileWidth, int tileHeight) {
        return Settings.UI.form.getTiledDrawOptions(x, y, xPadding, yPadding, pointSetAbstract, tileWidth, tileHeight);
    }

    @Override
    public SharedTextureDrawOptions getTiledDrawOptions(int x, int y, int xPadding, int yPadding, PointSetAbstract<?> pointSetAbstract, int tileWidth, int tileHeight) {
        return Settings.UI.form.getTiledEdgeDrawOptions(x, y, xPadding, yPadding, pointSetAbstract, tileWidth, tileHeight);
    }

    @Override
    public Color getCenterColor() {
        return Settings.UI.form.getCenterColor();
    }

    @Override
    public int getContentPadding() {
        return padding;
    }
}
