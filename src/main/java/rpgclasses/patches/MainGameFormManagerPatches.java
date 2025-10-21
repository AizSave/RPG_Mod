package rpgclasses.patches;

import necesse.engine.GlobalData;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.client.Client;
import necesse.engine.state.MainGame;
import necesse.engine.window.GameWindow;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.MainGameFormManager;
import necesse.gfx.forms.components.FormExpressionWheel;
import net.bytebuddy.asm.Advice;
import org.jetbrains.annotations.NotNull;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.ui.CustomUIManager;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MainGameFormManagerPatches {

    @ModMethodPatch(target = MainGameFormManager.class, name = "tickExpressionWheel", arguments = {boolean.class, Consumer.class})
    public static class tickExpressionWheel {

        @Advice.OnMethodEnter
        static boolean onEnter(@Advice.This MainGameFormManager This, @Advice.Argument(0) boolean shouldBeActive) {
            if (shouldBeActive) {
                try {
                    Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
                    PlayerMob player = client.getPlayer();
                    PlayerData playerData = PlayerDataList.getPlayerData(player);

                    if (playerData.equippedActiveSkills != null) {
                        Field displayName = FormExpressionWheel.Expression.class.getDeclaredField("displayName");
                        displayName.setAccessible(true);

                        Field drawIcon = FormExpressionWheel.Expression.class.getDeclaredField("drawIcon");
                        drawIcon.setAccessible(true);

                        Field drawOptionsModifier = FormExpressionWheel.Expression.class.getDeclaredField("drawOptionsModifier");
                        drawOptionsModifier.setAccessible(true);

                        FormExpressionWheel.Expression[] expressions = new FormExpressionWheel.Expression[]{FormExpressionWheel.Expression.SAD, FormExpressionWheel.Expression.SURPRISED, FormExpressionWheel.Expression.ANGRY, FormExpressionWheel.Expression.BORED};
                        for (int i = 0; i < 4; i++) {
                            FormExpressionWheel.Expression expression = expressions[i];
                            playerData.equippedActiveSkills[i].modifyForm(expressions[i], drawIcon, displayName);
                            drawOptionsModifier.set(expression, new MyDrawOptionsModifier());
                        }
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return false;
        }

        public static class MyDrawOptionsModifier implements BiConsumer<Float, HumanDrawOptions> {
            @Override
            public void accept(Float aFloat, HumanDrawOptions humanDrawOptions) {
                // empty
            }

            @NotNull
            @Override
            public BiConsumer<Float, HumanDrawOptions> andThen(@NotNull BiConsumer<? super Float, ? super HumanDrawOptions> after) {
                return BiConsumer.super.andThen(after);
            }
        }

    }

    @ModMethodPatch(target = MainGameFormManager.class, name = "setup", arguments = {})
    public static class setup {
        @Advice.OnMethodExit
        static void onExit(@Advice.This MainGameFormManager mainGameFormManager) {
            CustomUIManager.setupAll(mainGameFormManager);
        }
    }

    @ModMethodPatch(target = MainGameFormManager.class, name = "frameTick", arguments = {TickManager.class})
    public static class frameTick {
        @Advice.OnMethodExit
        static void onExit(@Advice.This MainGameFormManager mainGameFormManager) {
            for (CustomUIManager<? extends Form> formManager : CustomUIManager.formManagers) {
                formManager.frameTick(mainGameFormManager);
            }
        }
    }

    @ModMethodPatch(target = MainGameFormManager.class, name = "onWindowResized", arguments = {GameWindow.class})
    public static class onWindowResized {
        @Advice.OnMethodExit
        static void onExit(@Advice.This MainGameFormManager mainGameFormManager) {
            for (CustomUIManager<? extends Form> formManager : CustomUIManager.formManagers) {
                if (formManager.mainForm != null) formManager.updatePosition(mainGameFormManager);
            }
        }
    }

}
