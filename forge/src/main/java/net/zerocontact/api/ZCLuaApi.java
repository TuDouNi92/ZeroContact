package net.zerocontact.api;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.caliber.LuaHookContext;
import org.luaj.vm2.LuaTable;

import java.util.HashMap;
import java.util.Map;

public final class ZCLuaApi {
    private static final Map<ResourceLocation, ZCLuaApiHelper> HELPERS = new HashMap<>();

    public static void register(ResourceLocation id, ZCLuaApiHelper helper) {
        if (HELPERS.putIfAbsent(id, helper) != null) {
            throw new IllegalStateException("Duplicated Lua helper: " + id);
        }
    }

    public static LuaTable create(LuaHookContext context) {
        LuaTable root = new LuaTable();
        HELPERS.forEach((id, helper) -> {
            LuaTable namespace = new LuaTable();
            helper.install(namespace, context);
            root.set(id.getPath(), namespace);
        });
        return root;
    }
}
