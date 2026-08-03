package net.zerocontact.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.zerocontact.caliber.CaliberVariantDamageHelper;

public record BallisticToolTipComponent(CaliberVariantDamageHelper.Caliber caliber)
        implements TooltipComponent {}
