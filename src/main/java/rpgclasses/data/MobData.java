package rpgclasses.data;

import aphorea.biomes.InfectedFieldsBiome;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.registries.MobRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.WormMobBody;
import necesse.entity.mobs.WormMobHead;
import necesse.entity.mobs.hostile.bosses.NightSwarmBatMob;
import necesse.level.maps.IncursionLevel;
import necesse.level.maps.Level;
import necesse.level.maps.biomes.Biome;
import necesse.level.maps.biomes.desert.DesertBiome;
import necesse.level.maps.biomes.dungeon.DungeonLevel;
import necesse.level.maps.biomes.plains.PlainsBiome;
import necesse.level.maps.biomes.snow.SnowBiome;
import necesse.level.maps.biomes.swamp.SwampBiome;
import necesse.level.maps.biomes.temple.TempleLevel;
import necesse.level.maps.incursion.DesertDeepCaveIncursionLevel;
import necesse.level.maps.incursion.SnowDeepCaveIncursionLevel;
import org.jetbrains.annotations.Nullable;
import rpgclasses.content.MobClass;
import rpgclasses.content.player.Mastery.Mastery;

import java.util.*;
import java.util.stream.Collectors;

public class MobData {
    public static String prefixDataName = "rpgmod_";
    public static String levelDataName = prefixDataName + "level";
    public static String classDataName = prefixDataName + "class";

    public static List<String> mobsForcedBossClass = new ArrayList<>();

    public static List<String> undeadMobs = new ArrayList<>();
    public static List<String> demonicMobs = new ArrayList<>();

    static {
        mobsForcedBossClass.addAll(
                Arrays.stream(new String[]{
                        // Evil's Protector
                        "evilsportal",

                        // Reaper
                        "reaperspiritportal",
                        "reaperspirit",

                        // Sunlight Champion
                        "sunlightgauntlet",

                        // The Void
                        "thevoidhorn",
                        "thevoidclaw"
                }).collect(Collectors.toList())
        );


        undeadMobs.addAll(
                Arrays.stream(new String[]{
                        "evilwitch",
                        "evilwitchflask",
                        "evilwitchbow",
                        "evilwitchgreatsword",
                        "zombie",
                        "trapperzombie",
                        "vampire",
                        "zombiearcher",
                        "crawlingzombie",
                        "swampzombie",
                        "enchantedzombie",
                        "enchantedzombiearcher",
                        "enchantedcrawlingzombie",
                        "mummy",
                        "mummymage",
                        "skeleton",
                        "skeletonthrower",
                        "skeletonmage",
                        "skeletonminer",
                        "swampskeleton",
                        "ancientskeleton",
                        "ancientskeletonthrower",
                        "ancientarmoredskeleton",
                        "ancientskeletonmage",
                        "cryptbat",
                        "phantom",
                        "cryptvampire",
                        "bonewalker",
                        "spiritghoul",
                        "vampireraider",
                        "mummyraider",
                        "ancientskeletonraider",
                        "reaper",
                        "reaperspirit",
                        "reaperspiritportal",
                        "incursioncrawlingzombie",

                        // RPG Mod
                        "lichskeletonmob",
                        "necromancerskeleton",
                        "necromancerskeletonwarrior",
                        "necromancerboneslinger",
                        "necromancertomb"
                }).collect(Collectors.toList())
        );

        demonicMobs.addAll(
                Arrays.stream(new String[]{
                        "voidapprentice",
                        "evilsprotector",
                        "evilsportal",
                        "voidwizard",
                        "voidwizardclone",
                        "voidadept",
                        "thecursedcrone",
                        "thevoid",
                        "thevoidhorn",
                        "thevoidclaw",
                        "voidapprenticeraider",

                        // AphoreaMod
                        "fallenwizard"
                }).collect(Collectors.toList())
        );
    }

    public Mob mob;
    public int level;
    public MobClass mobClass;

    public static Map<Integer, MobData> mobsData = new HashMap<>();

    public static MobData getMob(Mob mob) {
        if (mob instanceof WormMobBody) {
            WormMobBody<?, ?> body = (WormMobBody<?, ?>) mob;
            WormMobHead<?, ?> master = body.master.get(mob.getLevel());
            return master == null ? null : getMob(master.getUniqueID());
        } else {
            return getMob(mob.getUniqueID());
        }
    }

    public static MobData getMob(int uniqueID) {
        return mobsData.getOrDefault(uniqueID, null);
    }

    public static boolean isBossClass(Mob mob) {
        return mob.isBoss() || mobsForcedBossClass.contains(mob.getStringID()) || (mob.getLevel() != null && mob.getLevel().entityManager.mobs.stream().anyMatch(m -> m.isBoss() && m.getDistance(mob) <= 1024));
    }

    public static boolean shouldInitMob(Mob mob) {
        return ((mob.isHostile && !mob.isPlayer) || mob.isBoss()) && mob.getClass() != WormMobBody.class && mob.getClass() != NightSwarmBatMob.class;
    }

    public static void initMob(Mob mob) {
        if (shouldInitMob(mob) && getMob(mob) == null) {
            GameRandom random = new GameRandom(mob.getUniqueID());

            Level mapLevel = mob.getLevel();
            MobData mobData = new MobData();
            mobData.mob = mob;

            boolean isBossClass = isBossClass(mob);

            Biome biome = mapLevel.getBiome(mob.getTileX(), mob.getTileY());

            mobData.level = getMobLevel(mapLevel, biome) + (isBossClass ? 5 : random.getIntBetween(1, 5));

            mobData.mobClass = isBossClass ? MobClass.bossClass : MobClass.getRandomClass(random);

            if ((mapLevel instanceof SnowDeepCaveIncursionLevel || biome instanceof SnowBiome) && mobData.mobClass.is("explosive")) {
                mobData.mobClass = MobClass.allClasses.get("glacial");
            } else if ((mapLevel instanceof DesertDeepCaveIncursionLevel || biome instanceof DesertBiome) && mobData.mobClass.is("glacial")) {
                mobData.mobClass = MobClass.allClasses.get("explosive");
            }

            mobsData.put(mob.getUniqueID(), mobData);

            mobData.mobClass.initBuffs(mob, mobData.level);
        }
    }

    private static int getMobLevel(Level mapLevel, Biome biome) {
        if (mapLevel.isIncursionLevel) {
            IncursionLevel incursionLevel = (IncursionLevel) mapLevel;
            return 22 + 4 * incursionLevel.incursionData.getTabletTier();
        } else {
            int dimension = mapLevel.getIslandDimension();

            if (mapLevel instanceof DungeonLevel) {
                return 5;
            } else if (mapLevel instanceof TempleLevel) {
                return 24;
            } else if (dimension != -2) {
                if (biome instanceof SnowBiome) {
                    return 3;
                } else if (biome instanceof PlainsBiome) {
                    return 8;
                } else if (biome instanceof SwampBiome) {
                    return 10;
                } else if (biome instanceof DesertBiome) {
                    return 12;
                } else if (biome instanceof InfectedFieldsBiome) {
                    return 16;
                }
            } else {
                if (biome instanceof PlainsBiome) {
                    return 16;
                } else if (biome instanceof SnowBiome) {
                    return 18;
                } else if (biome instanceof SwampBiome) {
                    return 20;
                } else if (biome instanceof DesertBiome) {
                    return 22;
                } else {
                    return 14;
                }
            }
        }
        return 0;
    }

    public static void loadData(LoadData loadData, Mob mob) {
        if (shouldInitMob(mob) && getMob(mob) == null) {
            MobData mobData = new MobData();
            mobData.mob = mob;

            mobData.level = loadData.getInt(levelDataName, 0);
            int mobClassID = loadData.getInt(classDataName, -1);
            if (mobData.level > 0 && mobClassID != -1) {
                mobData.mobClass = MobClass.allClassesList.get(mobClassID);

                mobsData.put(mob.getUniqueID(), mobData);
            }
        }
    }

    public void saveData(SaveData saveData) {
        saveData.addInt(levelDataName, level);
        saveData.addInt(classDataName, mobClass.id);
    }

    public static void applySpawnPacket(PacketReader reader, Mob mob) {
        if (shouldInitMob(mob)) {

            int level = reader.getNextInt();
            int mobClassID = reader.getNextInt();

            if (getMob(mob) == null) {
                MobData mobData = new MobData();
                mobData.level = level;

                if (mobData.level > 0 && mobClassID != -1 && (isBossClass(mob) == (mobClassID == 0))) {
                    mobData.mobClass = MobClass.allClassesList.get(mobClassID);
                    mobData.mob = mob;
                    mobsData.put(mob.getUniqueID(), mobData);
                }
            }
        }
    }

    public void setupSpawnPacket(PacketWriter writer) {
        writer.putNextInt(level);
        writer.putNextInt(mobClass.id);
    }

    public String realName() {
        return MobRegistry.getLocalization(mob.getID()).translate();
    }

    public int levelScaling() {
        return 4 + level;
    }

    public static int levelScaling(int level) {
        return 10 + level;
    }

    public boolean isUndead() {
        return isUndead(mob);
    }

    public boolean isDemonic() {
        return isDemonic(mob);
    }

    public static boolean isUndead(Mob mob) {
        return MobData.undeadMobs.contains(mob.getStringID());
    }

    public static boolean isDemonic(Mob mob) {
        return isDemonic(mob, null);
    }

    public static boolean isDemonic(Mob mob, @Nullable Mob attacker) {
        if (attacker instanceof PlayerMob) {
            PlayerData playerData = PlayerDataList.getPlayerData((PlayerMob) attacker);
            if (playerData.hasMasterySkill(Mastery.INQUISITOR)) return true;
        }
        return MobData.demonicMobs.contains(mob.getStringID());
    }

    public boolean isWeakToHoly(@Nullable Mob attacker) {
        return isWeakToHoly(mob, attacker);
    }

    public static boolean isWeakToHoly(Mob mob, @Nullable Mob attacker) {
        if (MobData.isUndead(mob) || MobData.isDemonic(mob, attacker)) return true;
        Mob mount = mob.getMount();
        return mount != null && isWeakToHoly(mount, attacker);
    }
}
