package rpgclasses.mobs.summons.passive;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.network.NetworkClient;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.*;
import necesse.entity.mobs.ai.behaviourTree.BehaviourTreeAI;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.ai.CustomSummonCollisionChaserAI;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.util.List;

public class RangerWolfMob extends PassiveFollowingMob {
    public static GameTexture texture;

    public RangerWolfMob() {
        super(10);
        this.setSpeed(45.0F);
        this.setFriction(4.0F);
        this.moveAccuracy = 8;

        this.collision = new Rectangle(-10, -7, 20, 14);
        this.hitBox = new Rectangle(-14, -12, 28, 24);
        this.selectBox = new Rectangle(-18, -24, 36, 36);
        this.swimMaskMove = 16;
        this.swimMaskOffset = 0;
        this.swimSinkOffset = -12;

        this.attackCooldown = 500;
    }

    @Override
    public void init() {
        super.init();
        this.ai = new BehaviourTreeAI<>(this, new CustomSummonCollisionChaserAI<RangerWolfMob>(1024, getDamage(), 30, 500, 640, 64, null) {
            @Override
            public Mob getCustomFocus(RangerWolfMob mob, int searchDistance) {
                if (!(getFollowingMob() instanceof PlayerMob)) return null;
                PlayerMob player = (PlayerMob) getFollowingMob();
                return RPGUtils.findBestTarget(player, mob.x, mob.y, 1000, RPGUtils.isMarkedFilter(player));
            }
        });
    }

    @Override
    public void playDeathSound() {
        SoundManager.playSound(GameResources.teleport, SoundEffect.effect(this).volume(0.8F));
    }

    @Override
    public void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
        GameLight light = level.getLightLevel(x / 32, y / 32);
        int drawX = camera.getDrawX(x) - 32;
        int drawY = camera.getDrawY(y) - 36;
        int dir = this.getDir();
        Point sprite = this.getAnimSprite(x, y, dir);
        drawY += this.getBobbing(x, y);
        drawY += this.getLevel().getTile(x / 32, y / 32).getMobSinkingAmount(this);
        final MaskShaderOptions swimMask = this.getSwimMaskShaderOptions(this.inLiquidFloat(x, y));
        final DrawOptions body = texture.initDraw().sprite(sprite.x, sprite.y, 64).addMaskShader(swimMask).light(light).pos(drawX, drawY);
        list.add(new MobDrawable() {
            public void draw(TickManager tickManager) {
                swimMask.use();
                body.draw();
                swimMask.stop();
            }
        });
        TextureDrawOptions shadow = MobRegistry.Textures.snowWolf.shadow.initDraw().sprite(0, sprite.y, 64).light(light).pos(drawX, drawY);
        tileList.add((tm) -> shadow.draw());
    }

    @Override
    public int getRockSpeed() {
        return 7;
    }

    public GameDamage getDamage() {
        NetworkClient client = this.getFollowingClient();
        if (client == null) return new GameDamage(0);
        PlayerData playerData = PlayerDataList.getPlayerData(client.playerMob);
        int skillLevel = getPassiveLevel(playerData);
        return new GameDamage(DamageTypeRegistry.SUMMON, playerData.getLevel() + playerData.getIntelligence(client.playerMob) * skillLevel);
    }
}
