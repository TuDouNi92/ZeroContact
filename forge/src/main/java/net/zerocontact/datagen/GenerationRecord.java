package net.zerocontact.datagen;

import net.minecraft.world.item.Item;
import net.zerocontact.api.IAssetManager;

public record GenerationRecord<T extends Item & IAssetManager.GeneratableItem>(String id , T item, String tabName) {}
