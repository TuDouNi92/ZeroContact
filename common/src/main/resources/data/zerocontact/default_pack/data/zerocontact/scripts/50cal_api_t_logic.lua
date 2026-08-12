local script = {}
function script.on_hit_ignite(ctx, args)
    zc.entity.ignite("VICTIM", 5)
end
function script.on_fly_tracer(ctx, args)
    local from = ctx.previous_position
    local to = ctx.position

    if from == nil then
        return
    end

    local dx = to.x - from.x
    local dy = to.y - from.y
    local dz = to.z - from.z

    local distance = math.sqrt(dx * dx + dy * dy + dz * dz)
    local spacing = 0.5
    local steps = math.max(1, math.ceil(distance / spacing))

    -- 防止高速弹丸产生过多粒子包
    steps = math.min(steps, 64)

    for i = 0, steps do
        local t = i / steps

        zc.particle.spawn_simple(
            "minecraft:flame",
            from.x + dx * t,
            from.y + dy * t,
            from.z + dz * t,
            1,
            0,
            0,
            0,
            0
        )
    end
end

return script