package rpgclasses.mobs.ai;

import necesse.entity.mobs.Mob;
import necesse.entity.mobs.ai.behaviourTree.AINodeResult;
import necesse.entity.mobs.ai.behaviourTree.Blackboard;
import necesse.entity.mobs.ai.behaviourTree.leaves.SummonTargetFinderAINode;
import necesse.entity.mobs.ai.behaviourTree.util.TargetFinderDistance;

import java.util.function.Predicate;

public class CustomSummonTargetFinderAINode<T extends Mob> extends SummonTargetFinderAINode<T> {
    public Predicate<Mob> defaultTargets;

    public CustomSummonTargetFinderAINode(Predicate<Mob> defaultTargets, TargetFinderDistance<T> distance, String currentTargetKey) {
        super(distance, currentTargetKey);
        this.defaultTargets = defaultTargets;
    }

    public CustomSummonTargetFinderAINode(Predicate<Mob> defaultTargets, TargetFinderDistance<T> distance) {
        super(distance);
        this.defaultTargets = defaultTargets;
    }

    public CustomSummonTargetFinderAINode(Predicate<Mob> defaultTargets, int searchDistance) {
        super(searchDistance);
        this.defaultTargets = defaultTargets;
    }

    @Override
    public AINodeResult tickNode(T mob, Blackboard<T> blackboard) {
        if (defaultTargets.test(mob)) {
            return super.tickNode(mob, blackboard);
        } else {
            return AINodeResult.FAILURE;
        }

    }
}
