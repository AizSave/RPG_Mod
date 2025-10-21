package rpgclasses.forms.rpgmenu.components;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameBackground;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.gfx.gameTooltips.TooltipLocation;
import rpgclasses.content.player.PlayerClass;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClassIconComponent extends FormButton {
    final PlayerClass playerClass;
    final int x;
    final int y;
    final int width;
    final int height;

    public ClassIconComponent(PlayerClass playerClass, int x, int y, int width, int height) {
        this.playerClass = playerClass;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(TickManager tickManager, PlayerMob playerMob, Rectangle rectangle) {
        GameTexture texture = playerClass.getTexture();
        int textureWidth = texture.getWidth();
        int textureHeight = texture.getWidth();
        texture.initDraw().draw(this.x + (this.width - textureWidth) / 2, this.y + (this.height - textureHeight) / 2);
        if (isHovering()) {
            ListGameTooltips tooltips = playerClass.getToolTips();
            GameTooltipManager.addTooltip(tooltips, GameBackground.itemTooltip, TooltipLocation.FORM_FOCUS);
        }
    }

    @Override
    public List<Rectangle> getHitboxes() {
        List<Rectangle> hitBoxes = new ArrayList<>();
        hitBoxes.add(new Rectangle(x, y, width, height));
        return hitBoxes;
    }
}
