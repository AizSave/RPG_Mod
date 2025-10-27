package rpgclasses.content.player.PlayerClasses.Wizard.ActiveSkills;

import necesse.engine.modifiers.ModifierValue;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import rpgclasses.buffs.Skill.PassiveActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimplePassiveBuffActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

public class ArcaneOverload extends SimplePassiveBuffActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("30 x <skilllevel>").setDecimals(2, 0),
            SkillParam.staticParam(50).setDecimals(2, 0)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(10);
    }

    public ArcaneOverload(int levelMax, int requiredClassLevel) {
        super("arcaneoverload", "#6633ff", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 6000;
    }

    @Override
    public boolean consumesManaPerSecond() {
        return true;
    }

    @Override
    public PassiveActiveSkillBuff getBuff() {
        return new ArcaneOverloadBuff(this, getBuffStringID());
    }

    public class ArcaneOverloadBuff extends PassiveActiveSkillBuff {
        public ActiveSkill skill;
        public String buffStringID;

        public ArcaneOverloadBuff(ActiveSkill skill, String buffStringID) {
            this.skill = skill;
            this.buffStringID = buffStringID;
        }

        @Override
        public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
            super.init(activeBuff, buffEventSubscriber);
            int level = getLevel(activeBuff);
            activeBuff.setModifier(BuffModifiers.MAGIC_ATTACK_SPEED, params[0].value(level));
            new ModifierValue<>(BuffModifiers.SPEED).max(params[1].value()).apply(activeBuff);
        }

        @Override
        public void clientTick(ActiveBuff activeBuff) {
            this.tick(activeBuff);
            activeBuff.owner.getLevel().entityManager.addParticle(activeBuff.owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), activeBuff.owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).color(getColor()).givesLight(0.0F, 0.5F).height(16.0F);
        }

        @Override
        public void serverTick(ActiveBuff activeBuff) {
            this.tick(activeBuff);
        }

        public void tick(ActiveBuff activeBuff) {
            PlayerMob player = (PlayerMob) activeBuff.owner;

            int level = getLevel(activeBuff);
            float manaUsage = getManaParam().value(level) / 20F;

            player.useMana(manaUsage, player.isServer() ? player.getServerClient() : null);

            if (player.getMana() < manaUsage) {
                player.buffManager.removeBuff(buffStringID, false);

                PlayerData playerData = PlayerDataList.getPlayerData(player);
                for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                    if (equippedActiveSkill.isSameSkill(skill)) {
                        equippedActiveSkill.startCooldown(player, playerData, player.getTime(), level);
                    }
                }
            }
        }
    }
}
