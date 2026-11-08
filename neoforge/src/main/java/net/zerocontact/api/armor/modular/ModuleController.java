package net.zerocontact.api.armor.modular;

import net.zerocontact.armor.modular.model.*;

import java.util.Optional;

public interface ModuleController {

    Optional<ModuleView> inspect(ModuleContext context);

    ActionResult execute(ModuleContext context, ModuleAction action);

}
