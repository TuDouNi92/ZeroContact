package net.zerocontact.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.zerocontact.caliber.CaliberHelper;

public record BallisticToolTipComponent(CaliberHelper.Caliber caliber)
        implements TooltipComponent {}
