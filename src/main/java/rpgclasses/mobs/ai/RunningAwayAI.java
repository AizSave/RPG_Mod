package rpgclasses.mobs.ai;

import aphorea.mobs.ai.AphRunFromMobsAI;
import necesse.entity.mobs.Mob;

import java.util.function.Predicate;

public class RunningAwayAI<T extends Mob> extends AphRunFromMobsAI<T> {
    public RunningAwayAI(int runDistance, Predicate<Mob> runFromMob) {
        super(runDistance, runFromMob);
    }

    @Override
    public boolean alwaysReturnSuccess() {
        return true;
    }
}