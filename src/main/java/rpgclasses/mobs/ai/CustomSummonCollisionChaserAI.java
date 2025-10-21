package rpgclasses.mobs.ai;

import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.ai.behaviourTree.AINode;
import necesse.entity.mobs.ai.behaviourTree.AINodeResult;
import necesse.entity.mobs.ai.behaviourTree.Blackboard;
import necesse.entity.mobs.ai.behaviourTree.composites.SelectorAINode;
import necesse.entity.mobs.ai.behaviourTree.composites.SequenceAINode;
import necesse.entity.mobs.ai.behaviourTree.leaves.CollisionChaserAINode;
import necesse.entity.mobs.ai.behaviourTree.leaves.FollowerBaseSetterAINode;
import necesse.entity.mobs.ai.behaviourTree.leaves.PlayerFollowerAINode;

import java.util.function.Predicate;

public class CustomSummonCollisionChaserAI<T extends Mob> extends SelectorAINode<T> {
    public GameDamage damage;
    public int knockback;

    public CustomSummonCollisionChaserAI(int searchDistance, GameDamage damage, int knockback, int hitCooldown, int teleportDistance, int stoppingDistance, Predicate<Mob> defaultTargets) {
        this.damage = damage;
        this.knockback = knockback;
        SequenceAINode<T> chaserSequence = new SequenceAINode<>();
        chaserSequence.addChild(new FollowerBaseSetterAINode<>());
        chaserSequence.addChild(new SkillSummonFocusTargetSetterAINode<T>(searchDistance) {
            public Mob getCustomFocus(T mob, int searchDistance) {
                return CustomSummonCollisionChaserAI.this.getCustomFocus(mob, searchDistance);
            }
        });

        if (defaultTargets != null)
            chaserSequence.addChild(new CustomSummonTargetFinderAINode<>(defaultTargets, searchDistance));

        CollisionChaserAINode<T> chaser = new CollisionChaserAINode<T>() {
            public boolean attackTarget(T mob, Mob target) {
                return CustomSummonCollisionChaserAI.this.attackTarget(mob, target);
            }
        };
        chaser.hitCooldowns.hitCooldown = hitCooldown;
        chaser.attackMoveCooldown = 0;
        chaserSequence.addChild(chaser);
        this.addChild(chaserSequence);
        this.addChild(new PlayerFollowerAINode<>(teleportDistance, stoppingDistance));
    }

    public boolean attackTarget(T mob, Mob target) {
        return CollisionChaserAINode.simpleAttack(mob, target, this.damage, this.knockback);
    }

    public Mob getCustomFocus(T mob, int searchDistance) {
        return null;
    }

    public static class SkillSummonFocusTargetSetterAINode<T extends Mob> extends AINode<T> {
        public String focusTargetKey;
        public int searchDistance;

        public SkillSummonFocusTargetSetterAINode(String focusTargetKey, int searchDistance) {
            this.focusTargetKey = focusTargetKey;
            this.searchDistance = searchDistance;
        }

        public SkillSummonFocusTargetSetterAINode(int searchDistance) {
            this("currentTarget", searchDistance);
        }

        protected void onRootSet(AINode<T> root, T mob, Blackboard<T> blackboard) {
        }

        public void init(T mob, Blackboard<T> blackboard) {
        }

        public AINodeResult tick(T mob, Blackboard<T> blackboard) {
            Mob customFocus = this.getCustomFocus(mob, searchDistance);
            blackboard.put(this.focusTargetKey, customFocus);

            return AINodeResult.SUCCESS;
        }

        public Mob getCustomFocus(T mob, int searchDistance) {
            return null;
        }
    }
}
