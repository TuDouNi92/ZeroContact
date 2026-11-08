package net.zerocontact.item.block;

import net.minecraft.world.level.block.Block;

public class WorkbenchItem extends GeoBlockItem {

    public WorkbenchItem(Block block, Properties properties) {
        super(block, properties, Workbench.texture, Workbench.model, Workbench.animation);
    }
}
