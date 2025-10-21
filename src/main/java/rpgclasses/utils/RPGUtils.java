package rpgclasses.utils;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.util.GameRandom;
import necesse.engine.util.LevelIdentifier;
import necesse.engine.util.gameAreaSearch.GameAreaStream;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.ai.behaviourTree.leaves.HumanAngerTargetAINode;
import necesse.level.maps.CollisionFilter;
import necesse.level.maps.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rpgclasses.buffs.MarkedBuff;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.summons.damageable.DamageableFollowingMob;
import rpgclasses.mobs.summons.damageable.necrotic.NecroticFollowingMob;

import java.awt.geom.Line2D;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RPGUtils {

    // CUSTOM RUNNABLE INTERFACES

    @FunctionalInterface
    public interface TriRunnable<T, U, V> {
        void run(T t, U u, V v);

        default TriRunnable<T, U, V> andThen(TriRunnable<? super T, ? super U, ? super V> after) {
            if (after == null) throw new NullPointerException();
            return (t, u, v) -> {
                this.run(t, u, v);
                after.run(t, u, v);
            };
        }
    }


    // STREAM MOBS
    public static GameAreaStream<Mob> streamMobs(Mob attacker, int range) {
        return streamMobs(attacker.getLevel(), attacker.x, attacker.y, range);
    }

    public static GameAreaStream<Mob> streamMobs(Level level, float x, float y, int range) {
        return level.entityManager.mobs.streamArea(x, y, range)
                .filter(isInRangeFilter(x, y, range));
    }

    // STREAM PLAYERS
    public static GameAreaStream<PlayerMob> streamPlayers(Mob attacker, int range) {
        return streamPlayers(attacker.getLevel(), attacker.x, attacker.y, range);
    }

    public static GameAreaStream<PlayerMob> streamPlayers(Level level, float x, float y, int range) {
        return level.entityManager.players.streamArea(x, y, range)
                .filter(isInRangeFilter(x, y, range));
    }

    // STREAM DEATH PLAYERS
    public static Stream<ServerClient> streamDeathPlayers(Server server, int maxTime, LevelIdentifier levelIdentifier, Predicate<ServerClient> filter) {
        return server == null ? Stream.empty() : server.streamClients().filter(
                        (c) -> c.playerMob != null && c.isDead() && c.isSamePlace(levelIdentifier)
                )
                .filter(c -> {
                    PlayerData playerData = PlayerDataList.getPlayerData(c.playerMob);
                    return playerData != null && c.playerMob.getTime() - playerData.lastDeath <= maxTime;
                })
                .filter(filter);
    }

    public static Stream<? extends ServerClient> streamDeathPlayers(Level level, int maxTime, Predicate<ServerClient> filter) {
        return streamDeathPlayers(level.getServer(), maxTime, level.getIdentifier(), filter);
    }

    // GET LAST DEATH PLAYER
    public static ServerClient lastDeathPlayer(Level level, int maxTime, Predicate<ServerClient> filter) {
        ServerClient[] bestHolder = new ServerClient[]{null};
        long[] bestTime = {0};

        streamDeathPlayers(level, maxTime, filter)
                .forEach(serverClient -> {
                    PlayerData playerData = PlayerDataList.getPlayerData(serverClient.playerMob);
                    if (playerData != null && playerData.lastDeath > bestTime[0]) {
                        bestTime[0] = playerData.lastDeath;
                        bestHolder[0] = serverClient;
                    }
                });

        return bestHolder[0];
    }

    // STREAM MOBS AND PLAYERS
    public static GameAreaStream<Mob> streamMobsAndPlayers(Mob attacker, int range) {
        return streamMobsAndPlayers(attacker.getLevel(), attacker.x, attacker.y, range);
    }

    public static GameAreaStream<Mob> streamMobsAndPlayers(Level level, float x, float y, int range) {
        return level.entityManager.streamAreaMobsAndPlayers(x, y, range)
                .filter(isInRangeFilter(x, y, range));
    }

    // VALID TARGET
    public static boolean isValidTarget(Mob attacker, Mob target) {
        return isValidTarget(attacker.isPlayer ? ((PlayerMob) attacker).getNetworkClient() : null, attacker, target);
    }

    public static boolean isValidTarget(NetworkClient client, Mob attacker, Mob target) {
        if (!target.canTakeDamage()) return false;
        if (attacker.isPlayer) {
            if (!target.isHostile) {
                if (target.isHuman) {
                    HumanAngerTargetAINode<?> humanAngerHandler = (HumanAngerTargetAINode<?>) target.ai.blackboard.getObject(HumanAngerTargetAINode.class, "humanAngerHandler");
                    if (humanAngerHandler == null || !humanAngerHandler.enemies.contains(attacker)) return false;
                } else {
                    return false;
                }
            }
        } else if(attacker.isHostile) {
            if(!target.isPlayer && !target.isHuman) return false;
        }
        return target.canBeTargeted(attacker, client);
    }

    public static Predicate<Mob> isValidTargetFilter(Mob attacker) {
        NetworkClient client = attacker.isPlayer ? ((PlayerMob) attacker).getNetworkClient() : null;

        return (m) -> isValidTarget(client, attacker, m);
    }

    public static Predicate<Mob> isValidAttackerFilter(Mob target) {
        return (m) -> isValidTarget(m, target);
    }


    // IN RANGE
    public static boolean isInRange(float x, float y, int maxDist, Mob target) {
        return isInRangeSq(x, y, maxDist * maxDist, target);
    }

    public static boolean isInRangeSq(float x, float y, int maxDistSq, Mob target) {
        float dx = target.getX() - x;
        float dy = target.getY() - y;
        return dx * dx + dy * dy <= maxDistSq;
    }


    public static Predicate<Mob> isInRangeFilter(float x, float y, int maxDist) {
        int maxDistanceSq = maxDist * maxDist;
        return m -> isInRangeSq(x, y, maxDistanceSq, m);
    }


    // IN VISION
    public static boolean isInVision(Level level, float x, float y, Mob target) {
        return isInVision(level, x, y, new CollisionFilter().projectileCollision(), target);
    }

    public static boolean isInVision(Level level, float x, float y, CollisionFilter collisionFilter, Mob target) {
        return !level.collides(new Line2D.Float(x, y, target.x, target.y), collisionFilter);
    }

    public static Predicate<Mob> inVisionFilter(Level level, float x, float y) {
        CollisionFilter collisionFilter = new CollisionFilter().projectileCollision();
        return m -> isInVision(level, x, y, collisionFilter, m);
    }


    // MARKED
    public static Predicate<Mob> isMarkedFilter(PlayerMob player) {
        return m -> MarkedBuff.isMarked(player, m);
    }


    // FOLLOWER
    public static boolean isFollower(Mob owner, Mob target) {
        return target.isFollowing() && target.getFollowingMob() == owner;
    }

    public static Predicate<Mob> isFollowerFilter(Mob owner) {
        return m -> isFollower(owner, m);
    }

    // DAMAGEABLE FOLLOWER
    public static boolean isDamageableFollower(Mob owner, Mob target) {
        return isFollower(owner, target) && target instanceof DamageableFollowingMob;
    }

    public static Predicate<Mob> isDamageableFollowerFilter(Mob owner) {
        return m -> isDamageableFollower(owner, m);
    }

    // NECROTIC DAMAGEABLE FOLLOWER
    public static boolean isNecroticFollower(Mob owner, Mob target) {
        return isFollower(owner, target) && target instanceof NecroticFollowingMob;
    }

    public static Predicate<Mob> isNecroticFollowerFilter(Mob owner) {
        return m -> isNecroticFollower(owner, m);
    }

    // FIND BEST TARGET
    public static Mob findBestTarget(@NotNull Mob mob, int distance) {
        return findBestTarget(mob, mob.x, mob.y, distance, null);
    }

    public static Mob findBestTarget(@NotNull Mob mob, int distance, @Nullable Predicate<Mob> filter) {
        return findBestTarget(mob, mob.x, mob.y, distance, filter);
    }

    public static Mob findBestTarget(@NotNull Mob mob, float x, float y, int distance, @Nullable Predicate<Mob> filter) {
        return findBestTarget(mob.getLevel(), mob, x, y, distance, filter);
    }

    public static Mob findBestTarget(@NotNull Level level, @NotNull Mob attacker, float x, float y, int maxDistance, @Nullable Predicate<Mob> filter) {
        Mob[] bestHolder = new Mob[1];
        float[] bestDistSq = {Float.MAX_VALUE};
        int[] bestPriority = {-1};
        boolean attackerIsPlayer = attacker.isPlayer;

        getAllTargets(level, attacker, x, y, maxDistance, filter)
                .forEach(m -> {
                    float dx = m.getX() - x, dy = m.getY() - y;
                    float distSq = dx * dx + dy * dy;
                    int priority = 0;
                    if (attackerIsPlayer) {
                        if (MarkedBuff.isMarked((PlayerMob) attacker, m)) priority += 2;
                        if (m.isHostile) priority++;
                    }
                    if (priority > bestPriority[0] || (priority == bestPriority[0] && distSq < bestDistSq[0])) {
                        bestHolder[0] = m;
                        bestDistSq[0] = distSq;
                        bestPriority[0] = priority;
                    }
                });

        return bestHolder[0];
    }


    // GET RANDOM TARGET
    public static Mob getRandomTarget(@NotNull Mob mob, int distance) {
        return getRandomTarget(mob, distance, null);
    }

    public static Mob getRandomTarget(@NotNull Mob mob, int distance, @Nullable Predicate<Mob> filter) {
        return getRandomTarget(mob, mob.x, mob.y, distance, filter);
    }

    public static Mob getRandomTarget(@NotNull Mob mob, float x, float y, int distance, @Nullable Predicate<Mob> filter) {
        return getRandomTarget(mob.getLevel(), mob, x, y, distance, filter);
    }

    public static Mob getRandomTarget(@NotNull Level level, @NotNull Mob attacker, float x, float y, int maxDistance, @Nullable Predicate<Mob> filter) {

        Mob[] chosenHolder = new Mob[1];
        int[] count = {0};

        getAllTargets(level, attacker, x, y, maxDistance, filter)
                .forEach(m -> {
                    count[0]++;
                    if (GameRandom.globalRandom.getChance(1.0f / count[0])) {
                        chosenHolder[0] = m;
                    }
                });

        return chosenHolder[0];
    }


    // ANY TARGETS
    public static boolean anyTarget(@NotNull Mob mob, int distance) {
        return anyTarget(mob, distance, null);
    }

    public static boolean anyTarget(@NotNull Mob mob, int distance, @Nullable Predicate<Mob> filter) {
        return anyTarget(mob, mob.x, mob.y, distance, filter);
    }

    public static boolean anyTarget(@NotNull Mob mob, float x, float y, int distance, @Nullable Predicate<Mob> filter) {
        return anyTarget(mob.getLevel(), mob, x, y, distance, filter);
    }

    public static boolean anyTarget(@NotNull Level level, @NotNull Mob attacker, float x, float y, int maxDistance, @Nullable Predicate<Mob> filter) {
        NetworkClient client = attacker.isPlayer
                ? ((PlayerMob) attacker).getNetworkClient()
                : null;

        CollisionFilter collisionFilter = new CollisionFilter().projectileCollision();

        boolean hasFilter = filter != null;

        return streamMobsAndPlayers(level, x, y, maxDistance)
                .anyMatch(m -> {
                    if (!isValidTarget(client, attacker, m)) return false;
                    if (!isInVision(level, x, y, collisionFilter, m)) return false;

                    return (!hasFilter || filter.test(m));
                });
    }


    // GET ALL TARGETS
    public static GameAreaStream<Mob> getAllTargets(@NotNull Mob mob, int distance) {
        return getAllTargets(mob, distance, null);
    }

    public static GameAreaStream<Mob> getAllTargets(@NotNull Mob mob, int distance, @Nullable Predicate<Mob> filter) {
        return getAllTargets(mob, mob.x, mob.y, distance, filter);
    }

    public static GameAreaStream<Mob> getAllTargets(@NotNull Mob mob, float x, float y, int distance, @Nullable Predicate<Mob> filter) {
        return getAllTargets(mob.getLevel(), mob, x, y, distance, filter);
    }

    public static GameAreaStream<Mob> getAllTargets(@NotNull Level level, @NotNull Mob attacker, float x, float y, int maxDistance, @Nullable Predicate<Mob> filter) {
        NetworkClient client = attacker.isPlayer
                ? ((PlayerMob) attacker).getNetworkClient()
                : null;

        CollisionFilter collisionFilter = new CollisionFilter().projectileCollision();

        boolean hasFilter = filter != null;

        return streamMobsAndPlayers(level, x, y, maxDistance)
                .filter(m -> {
                    if (!isValidTarget(client, attacker, m)) return false;
                    if (!isInVision(level, x, y, collisionFilter, m)) return false;

                    return (!hasFilter || filter.test(m));
                });
    }

    // ANY DAMAGEABLE FOLLOWER
    public static boolean anyDamageableFollower(@NotNull Mob attacker, int maxDistance) {
        return anyDamageableFollower(attacker.getLevel(), attacker, attacker.x, attacker.y, maxDistance, null);
    }

    public static boolean anyDamageableFollower(@NotNull Mob attacker, int maxDistance, @Nullable Predicate<Mob> filter) {
        return anyDamageableFollower(attacker.getLevel(), attacker, attacker.x, attacker.y, maxDistance, filter);
    }

    public static boolean anyDamageableFollower(@NotNull Level level, @NotNull Mob attacker, float x, float y, int maxDistance, @Nullable Predicate<Mob> filter) {
        boolean hasFilter = filter != null;

        return streamMobs(level, x, y, maxDistance)
                .anyMatch(m -> {
                    if (!isDamageableFollower(attacker, m)) return false;

                    return (!hasFilter || filter.test(m));
                });
    }

    // FIND CLOSEST DAMAGEABLE FOLLOWER
    public static Mob findClosestDamageableFollower(@NotNull Mob attacker, int maxDistance) {
        return findClosestDamageableFollower(attacker.getLevel(), attacker, attacker.x, attacker.y, maxDistance, null);
    }

    public static Mob findClosestDamageableFollower(@NotNull Mob attacker, int maxDistance, @Nullable Predicate<Mob> filter) {
        return findClosestDamageableFollower(attacker.getLevel(), attacker, attacker.x, attacker.y, maxDistance, filter);
    }

    public static Mob findClosestDamageableFollower(@NotNull Level level, @NotNull Mob attacker, float x, float y, int maxDistance, @Nullable Predicate<Mob> filter) {
        Mob[] bestHolder = new Mob[1];
        float[] bestDistSq = {Float.MAX_VALUE};
        int[] bestPriority = {-1};
        boolean attackerIsPlayer = attacker.isPlayer;

        getAllDamageableFollowers(level, attacker, x, y, maxDistance, filter)
                .forEach(m -> {
                    float dx = m.getX() - x, dy = m.getY() - y;
                    float distSq = dx * dx + dy * dy;
                    int priority = 0;
                    if (attackerIsPlayer) {
                        if (MarkedBuff.isMarked((PlayerMob) attacker, m)) priority += 2;
                        if (m.isHostile) priority++;
                    }
                    if (priority > bestPriority[0] || (priority == bestPriority[0] && distSq < bestDistSq[0])) {
                        bestHolder[0] = m;
                        bestDistSq[0] = distSq;
                        bestPriority[0] = priority;
                    }
                });

        return bestHolder[0];
    }

    // GET ALL DAMAGEABLE FOLLOWERS
    public static GameAreaStream<Mob> getAllDamageableFollowers(@NotNull Mob attacker, int maxDistance) {
        return getAllDamageableFollowers(attacker.getLevel(), attacker, attacker.x, attacker.y, maxDistance, null);
    }

    public static GameAreaStream<Mob> getAllDamageableFollowers(@NotNull Mob attacker, int maxDistance, @Nullable Predicate<Mob> filter) {
        return getAllDamageableFollowers(attacker.getLevel(), attacker, attacker.x, attacker.y, maxDistance, filter);
    }

    public static GameAreaStream<Mob> getAllDamageableFollowers(@NotNull Level level, @NotNull Mob attacker, float x, float y, int maxDistance, @Nullable Predicate<Mob> filter) {
        boolean hasFilter = filter != null;

        return streamMobs(level, x, y, maxDistance)
                .filter(m -> {
                    if (!isDamageableFollower(attacker, m)) return false;

                    return (!hasFilter || filter.test(m));
                });
    }


}
