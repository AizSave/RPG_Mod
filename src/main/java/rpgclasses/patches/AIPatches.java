package rpgclasses.patches;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.util.gameAreaSearch.GameAreaStream;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.ai.behaviourTree.leaves.TargetFinderAINode;
import necesse.entity.mobs.ai.behaviourTree.util.TargetFinderDistance;
import net.bytebuddy.asm.Advice;

import java.awt.*;
import java.util.function.Predicate;

public class AIPatches {

    @ModMethodPatch(target = TargetFinderAINode.class, name = "streamPlayersAndHumans", arguments = {Mob.class, Point.class, TargetFinderDistance.class})
    public static class TargetFinderAINode_streamPlayersAndHumans {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        static boolean onEnter() {
            return false;
        }

        @Advice.OnMethodExit
        static <T extends Mob> void onExit(@Advice.Argument(0) T mob, @Advice.Argument(1) Point base, @Advice.Argument(2) TargetFinderDistance<T> distance, @Advice.Return(readOnly = false) GameAreaStream<Mob> targets) {
            targets = distance.streamMobsAndPlayersInRange(base, mob).filter(getMobPredicate(mob));
        }

        public static <T extends Mob> Predicate<Mob> getMobPredicate(T mob) {
            return (m) -> {
                if (m != null && m != mob && !m.removed() && m.isVisible()) {
                    int team = m.getTeam();
                    if (team == -100) {
                        return true;
                    } else {
                        if (m.isHuman && team != -1 || m.isPlayer) {
                            return true;
                        } else if (m.isFollowing() && m.canTakeDamage()) {
                            Mob following = m.getFollowingMob();
                            if (following == null) return false;
                            return following.isHuman && team != -1 || following.isPlayer;
                        } else {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            };
        }

    }

}
