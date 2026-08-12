package net.zerocontact.lua;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.ZeroContact;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.ZCLuaApi;
import net.zerocontact.caliber.HookActionExecutor;
import net.zerocontact.caliber.TargetSelector;
import net.zerocontact.datagen.AmmoDataPOJO;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class ZcLuaApiHelpers {
    private ZcLuaApiHelpers() {
    }

    public static void register() {
        registerLog();
        registerEffect();
        registerEntity();
        registerParticle();
    }

    private static void registerLog() {
        ZCLuaApi.register(
                new ResourceLocation(ZeroContact.MOD_ID, "log"),
                (api, context) -> {
                    api.set("info", new OneArgFunction() {
                        @Override
                        public LuaValue call(LuaValue value) {
                            String message = value.checkjstring();

                            // 防止脚本一次写入过长日志
                            if (message.length() > 1_000) {
                                message = message.substring(0, 1_000);
                            }

                            ZeroContactLogger.LOG.info(
                                    "[Lua {}] {}",
                                    context.scriptId(),
                                    message
                            );

                            return LuaValue.NONE;
                        }
                    });
                }
        );
    }

    private static void registerEffect() {
        ZCLuaApi.register(
                new ResourceLocation(ZeroContact.MOD_ID, "effect"),
                (api, context) -> {
                    api.set("apply", new VarArgFunction() {
                        @Override
                        public Varargs invoke(Varargs args) {
                            TargetSelector target = parseTarget(
                                    args.checkjstring(1)
                            );
                            String effectId = args.checkjstring(2);
                            int duration = Mth.clamp(
                                    args.checkint(3),
                                    1,
                                    20 * 60 * 60
                            );
                            int amplifier = Mth.clamp(
                                    args.optint(4, 0),
                                    0,
                                    255
                            );
                            float radius = Mth.clamp(
                                    (float) args.optdouble(5, 0),
                                    0,
                                    32
                            );
                            AmmoDataPOJO.HookActionData action =
                                    new AmmoDataPOJO.HookActionData(
                                            target,
                                            effectId,
                                            duration,
                                            amplifier,
                                            1.0F,
                                            radius
                                    );
                            HookActionExecutor.execute(
                                    action,
                                    context.hookContext()
                            );
                            ZeroContactLogger.LOG.debug("Add effect {} to {}", effectId, target.toString());
                            return LuaValue.NONE;
                        }
                    });
                }
        );
    }

    private static void registerEntity() {
        ZCLuaApi.register(
                new ResourceLocation(ZeroContact.MOD_ID, "entity"),
                (api, context) -> {

                    api.set("position", new OneArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg) {
                            String target = arg.checkjstring();
                            TargetSelector selector = parseTarget(target);
                            return selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .map(lv -> {
                                        LuaTable position = new LuaTable();
                                        position.set(1, LuaValue.valueOf(lv.getX()));
                                        position.set(2, LuaValue.valueOf(lv.getY()));
                                        position.set(3, LuaValue.valueOf(lv.getZ()));
                                        return position;
                                    })
                                    .orElse(LuaValue.tableOf());
                        }
                    });

                    api.set("type", new TwoArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg, LuaValue arg2) {
                            String target = arg.checkjstring();
                            ResourceLocation type = new ResourceLocation(arg2.checkjstring());
                            TargetSelector selector = parseTarget(target);
                            return selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .map(lv -> {
                                        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(type);
                                        return LuaValue.valueOf(lv.getType().equals(entityType));
                                    })
                                    .orElse(LuaValue.FALSE);
                        }
                    });

                    api.set("health", new OneArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg) {
                            String target = arg.checkjstring();
                            TargetSelector selector = parseTarget(target);
                            return selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .map(lv -> (LuaValue) LuaValue.valueOf(lv.getHealth())).orElse(LuaValue.NIL);
                        }
                    });

                    api.set("max_health", new OneArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg) {
                            String target = arg.checkjstring();
                            TargetSelector selector = parseTarget(target);
                            return selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .map(lv -> (LuaValue) LuaValue.valueOf(lv.getMaxHealth())).orElse(LuaValue.NIL);
                        }
                    });

                    api.set("distance_between", new VarArgFunction() {
                        @Override
                        public Varargs invoke() {
                            LivingEntity shooter = context.shooter();
                            LivingEntity victim = context.victim();
                            if (shooter != null && victim != null) {
                                return LuaValue.valueOf(shooter.distanceToSqr(victim));
                            }
                            return LuaValue.NIL;
                        }
                    });

                    api.set("is_on_fire", new OneArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg) {
                            String checkTarget = arg.checkjstring();
                            TargetSelector selector = parseTarget(checkTarget);
                            return selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .map(lv -> (LuaValue) LuaValue.valueOf(lv.isOnFire()))
                                    .orElse(LuaValue.FALSE);
                        }
                    });

                    api.set("count", new TwoArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg, LuaValue arg2) {
                            String checkTarget = arg.checkjstring(1);
                            long radius = arg.checklong(2);
                            TargetSelector selector = parseTarget(checkTarget);
                            return LuaValue.valueOf(selector.resolve(context.hookContext(), radius)
                                    .count());
                        }
                    });

                    api.set("has_effect", new TwoArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg, LuaValue arg2) {
                            String target = arg.checkjstring();
                            String effect = arg2.checkjstring();
                            MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effect));
                            if (mobEffect == null) return LuaValue.FALSE;
                            TargetSelector selector = parseTarget(target);
                            return selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .map(lv -> (LuaValue) LuaValue.valueOf(lv.hasEffect(mobEffect))).orElse(LuaValue.FALSE);
                        }
                    });

                    api.set("remove_effect", new TwoArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg, LuaValue arg2) {
                            String target = arg.checkjstring();
                            String effect = arg2.checkjstring();
                            MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effect));
                            if (mobEffect == null) return LuaValue.FALSE;
                            TargetSelector selector = parseTarget(target);
                            return selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .map(lv -> {
                                        ZeroContactLogger.LOG.debug("Remove effect {} from {}", effect, lv.toString());
                                        return (LuaValue) LuaValue.valueOf(lv.removeEffect(mobEffect));
                                    }).orElse(LuaValue.FALSE);
                        }
                    });

                    api.set("extinguish", new OneArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg) {
                            String target = arg.checkjstring();
                            TargetSelector selector = parseTarget(target);
                            selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .ifPresent(lv -> {
                                        lv.setRemainingFireTicks(0);
                                        ZeroContactLogger.LOG.debug("Extinguished entity {}", lv.toString());
                                    });
                            return LuaValue.NONE;
                        }
                    });

                    api.set("heal", new VarArgFunction() {
                        @Override
                        public Varargs invoke(Varargs varargs) {
                            String target = varargs.checkjstring(1);
                            TargetSelector targetSelector = parseTarget(target);
                            int amount = varargs.optint(2, 1);
                            float radius = (float) varargs.optdouble(3, 1);
                            targetSelector.resolve(context.hookContext(), radius).forEach(
                                    lv -> {
                                        lv.heal(amount);
                                        ZeroContactLogger.LOG.debug("Healed entity {}", lv.toString());
                                    }
                            );
                            return LuaValue.NONE;
                        }
                    });


                    api.set("ignite", new VarArgFunction() {
                        @Override
                        public Varargs invoke(Varargs varargs) {
                            String target = varargs.checkjstring(1);
                            TargetSelector targetSelector = parseTarget(target);
                            int seconds = varargs.optint(2, 1);
                            float radius = (float) varargs.optdouble(3, 1);
                            targetSelector.resolve(context.hookContext(), radius).forEach(
                                    lv -> {
                                        lv.setSecondsOnFire(seconds);
                                        ZeroContactLogger.LOG.debug("Ignited entity {}", lv.toString());
                                    }
                            );
                            return LuaValue.NONE;
                        }
                    });
                }
        );
    }

    private static void registerParticle() {
        ZCLuaApi.register(
                new ResourceLocation(ZeroContact.MOD_ID, "particle"),
                (api, context) ->
                        api.set("spawn_simple", new VarArgFunction() {
                            @Override
                            public Varargs invoke(Varargs varargs) {
                                String particlePath = varargs.checkjstring(1);
                                double x = varargs.checkdouble(2);
                                double y = varargs.checkdouble(3);
                                double z = varargs.checkdouble(4);
                                int count = varargs.checkint(5);
                                double xOffset = varargs.checkdouble(6);
                                double yOffset = varargs.checkdouble(7);
                                double zOffset = varargs.checkdouble(8);
                                double speed = varargs.checkdouble(9);
                                ServerLevel level = context.level();
                                ParticleType<?> particleType = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(particlePath));
                                if (particleType == null) return LuaValue.FALSE;
                                if (particleType instanceof SimpleParticleType simpleParticleType) {
                                    level.sendParticles(simpleParticleType, x, y, z, count, xOffset, yOffset, zOffset, speed);
                                    ZeroContactLogger.LOG.debug("Sent particles: {}; Count = {}, Speed = {} ", particlePath, count, speed);
                                }
                                return LuaValue.TRUE;
                            }
                        })
        );
    }

    private static TargetSelector parseTarget(String value) {
        try {
            return TargetSelector.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new LuaError(
                    "Unknown target '" + value
                            + "', expected SHOOTER, VICTIM or NEARBY"
            );
        }
    }
}
