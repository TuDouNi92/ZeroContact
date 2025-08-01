package net.zerocontact.stamina;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zerocontact.command.CommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerStaminaProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerStamina> PLAYER_STAMINA = CapabilityManager.get(new CapabilityToken<PlayerStamina>() {
    });
    private PlayerStamina playerStamina = null;
    private final LazyOptional<PlayerStamina> optional = LazyOptional.of(this::createPlayerStamina);
    private PlayerStamina createPlayerStamina(){
        if(this.playerStamina !=null)return playerStamina;
        this.playerStamina = new PlayerStamina();
        return playerStamina;
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        if(capability !=PLAYER_STAMINA)return LazyOptional.empty();
        return CommandManager.isEnabledStamina?optional.cast():LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerStamina().setNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerStamina().getNbt(nbt);
    }
}
