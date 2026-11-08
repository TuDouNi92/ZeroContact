package net.zerocontact.client.interaction;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.item.plate.BasePlate;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.c2s.EquipPlatePacket;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.Optional;
import java.util.function.Supplier;


// Use a unique subscriber class name: Forge's generated wrappers use the simple class name.
@Mod.EventBusSubscriber(modid = ZeroContact.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlateInteractionManager {

    private static boolean installing = false;
    private static int tick = 0;
    private static final int LENGTH = 30;
    private static ItemStack snapshot = ItemStack.EMPTY;
    private static final Supplier<Optional<LocalPlayer>> player = () -> Optional.ofNullable(Minecraft.getInstance().player);

    public static void install(BasePlate plate) {
        if (installing) return;
        ItemStack checkStack = player.get().map(LivingEntity::getMainHandItem).orElse(ItemStack.EMPTY);
        player.get().ifPresent(player -> {
            BasePlate.resolveSlot(player, (itemHandler, front, back) -> {
                ItemStack frontPlate = front.map(s -> s.getStacks().getStackInSlot(0)).orElse(ItemStack.EMPTY);
                ItemStack backPlate = back.map(s -> s.getStacks().getStackInSlot(0)).orElse(ItemStack.EMPTY);
                if (frontPlate.isEmpty() || backPlate.isEmpty()) {
                    snapshot = checkStack.copy();
                    tick = 0;
                    plate.triggerAnim(player, GeoItem.getId(snapshot),"controller","install");
                    installing = true;
                }
            });
        });
    }

    public static void clear() {
        installing = false;
        tick = 0;
        snapshot = ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (installing) {
            ItemStack checkStack = player.get().map(LivingEntity::getMainHandItem).orElse(ItemStack.EMPTY);
            if (checkStack.isEmpty() || !ItemStack.isSameItemSameTags(checkStack, snapshot)) {
                clear();
                return;
            }
            if (tick < LENGTH) {
                tick++;
            } else {
                ItemStack completedStack = snapshot;
                clear();
                ModMessages.sendToServer(new EquipPlatePacket(completedStack));
            }
        }
    }
}
