package rpgclasses.patches;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.gameNetworkData.GNDItemMap;
import necesse.engine.util.GameMath;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import necesse.entity.mobs.buffs.staticBuffs.StaminaBuff;
import necesse.gfx.gameFont.FontManager;
import necesse.gfx.gameTexture.GameTexture;
import net.bytebuddy.asm.Advice;

import java.lang.reflect.Field;

public class StaminaBuffPatches {

    @ModMethodPatch(target = StaminaBuff.class, name = "drawIcon", arguments = {int.class, int.class, ActiveBuff.class})
    public static class drawIcon {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        static boolean onEnter(@Advice.This StaminaBuff This, @Advice.Argument(0) int x, @Advice.Argument(1) int y, @Advice.Argument(2) ActiveBuff activeBuff) {
            GNDItemMap gndData = activeBuff.getGndData();
            boolean onCooldown = gndData.getBoolean("onCooldown");

            try {
                GameTexture texture = getTexture(This, onCooldown);
                texture.initDraw().size(32, 32).draw(x, y);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            float stamina = gndData.getFloat("stamina");
            stamina = GameMath.limit(stamina, 0.0F, 1.0F);

            int staminaInt = (int) (Math.abs(stamina - 1.0F) * 100.0F * activeBuff.owner.buffManager.getModifier(BuffModifiers.STAMINA_CAPACITY));
            staminaInt = Math.max(staminaInt, onCooldown ? 0 : 1);

            String text = staminaInt + "%";
            int width = FontManager.bit.getWidthCeil(text, Buff.durationFontOptions);

            FontManager.bit.drawString((float) (x + 16 - width / 2), (float) (y + 30), text, Buff.durationFontOptions);

            return true;
        }

        public static GameTexture getTexture(StaminaBuff This, boolean onCooldown) throws NoSuchFieldException, IllegalAccessException {
            GameTexture texture;
            Field field;
            if (onCooldown) {
                field = StaminaBuff.class.getDeclaredField("cooldownTexture");
            } else {
                field = Buff.class.getDeclaredField("iconTexture");
            }
            field.setAccessible(true);
            texture = (GameTexture) field.get(This);
            return texture;
        }
    }

}
