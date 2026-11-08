package net.zerocontact.datagen.model;

import net.minecraft.world.item.Item;

public record GenerationRecord<T extends Item >(String id , T item, String tabName) {}
