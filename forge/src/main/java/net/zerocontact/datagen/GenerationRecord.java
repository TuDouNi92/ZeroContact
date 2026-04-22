package net.zerocontact.datagen;

import net.minecraft.world.item.Item;
import net.zerocontact.api.IItemLoader;

public record GenerationRecord<T extends Item & IItemLoader.GeneratableItem>(String id , T item) {}
