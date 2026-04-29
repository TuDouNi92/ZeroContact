package net.zerocontact.datagen;

import net.minecraft.world.item.Item;
import net.zerocontact.api.AssetHelper;

public record GenerationRecord<T extends Item & AssetHelper.GeneratableItem>(String id , T item) {}
