package rpgclasses.forms.rpgmenu.components;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.Localization;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.Renderer;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.gfx.gameTooltips.TooltipLocation;
import rpgclasses.forms.rpgmenu.BorderFormGameBackground;
import rpgclasses.content.player.Mastery.Mastery;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MasteryIconComponent extends FormButton {
    final Mastery mastery;
    final int x;
    final int y;
    final int width;
    final int height;
    public boolean hasMastery;

    public MasteryIconComponent(Mastery mastery, int x, int y, int width, int height, boolean hasMastery) {
        this.mastery = mastery;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hasMastery = hasMastery;
    }

    @Override
    public void draw(TickManager tickManager, PlayerMob playerMob, Rectangle rectangle) {
        int textureWidth = mastery.texture.getWidth();
        int textureHeight = mastery.texture.getWidth();
        if (hasMastery) {
            Renderer.drawCircle(x + width / 2, y + height / 2, width / 2, 20, new Color(0, 255, 0, 102), true);
        }
        mastery.texture.initDraw().draw(this.x + (this.width - textureWidth) / 2, this.y + (this.height - textureHeight) / 2);
        if (isHovering()) {
            ListGameTooltips tooltips = mastery.getToolTips();
            GameTooltipManager.addTooltip(tooltips, new BorderFormGameBackground(12), TooltipLocation.FORM_FOCUS);
            for (String extraTooltip : mastery.getExtraTooltips()) {
                GameTooltipManager.addTooltip(new ListGameTooltips(Localization.translate("extraskilldesc", extraTooltip)), new BorderFormGameBackground(12), TooltipLocation.FORM_FOCUS);
            }
        }
    }

    @Override
    public List<Rectangle> getHitboxes() {
        List<Rectangle> hitBoxes = new ArrayList<>();
        hitBoxes.add(new Rectangle(x, y, width, height));
        return hitBoxes;
    }
}
