package net.zerocontact.api;

import net.zerocontact.caliber.LuaHookContext;
import org.luaj.vm2.LuaTable;

@FunctionalInterface
public interface ZCLuaApiHelper {
    void install(LuaTable api, LuaHookContext context);
}
