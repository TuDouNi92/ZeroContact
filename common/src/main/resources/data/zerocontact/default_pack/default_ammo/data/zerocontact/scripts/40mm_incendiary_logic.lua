local script = {}
function script.on_hit_glowing(ctx,args)
    zc.log.info("Triggered")
    zc.effect.apply(
        "NEARBY",
        "minecraft:poison",
        args.duration or 2,
        args.amplifier or 1,
        args.radius or 1
    )
    zc.log.info("Applied effect")
end

return script