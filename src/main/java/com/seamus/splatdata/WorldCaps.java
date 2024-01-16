package com.seamus.splatdata;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldCaps implements ICapabilityProvider, INBTSerializable<CompoundTag>
{
    public static Capability<WorldInfo> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private WorldInfo WorldInfo = null;
    private final LazyOptional<WorldInfo> opt = LazyOptional.of(() ->
            WorldInfo == null ? (WorldInfo = new WorldInfo()) : WorldInfo);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        return cap == CAPABILITY ? opt.cast() : LazyOptional.empty();
    }

    public static WorldInfo get(Level level)
    {
        return level.getCapability(CAPABILITY).orElseThrow(IllegalStateException::new);
    }

    @Override
    public CompoundTag serializeNBT() {
        return opt.orElseThrow(IllegalStateException::new).writeNBT(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        opt.orElseThrow(IllegalStateException::new).readNBT(nbt);
    }

    public static boolean hasCapability(LivingEntity entity)
    {
        return CAPABILITY != null && entity.getCapability(CAPABILITY).isPresent();
    }
}

/* implements ICapabilitySerializable<CompoundTag>
{
    @CapabilityInject(WorldInfo.class)
    public static final Capability<WorldInfo> CAPABILITY = null;
    private static final WorldInfo DEFAULT = new WorldInfo(SplatcraftInkColors.undyed.getColor());
    private final LazyOptional<WorldInfo> instance = LazyOptional.of(CAPABILITY::getDefaultInstance);

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(WorldInfo.class);
    }

    public static WorldInfo get(LivingEntity entity) throws NullPointerException
    {
        return entity.getCapability(CAPABILITY).orElse(null);
    }

    public static boolean hasCapability(LivingEntity entity)
    {
        return CAPABILITY != null && entity.getCapability(CAPABILITY).isPresent();
    }

    public static boolean isSquid(LivingEntity entity)
    {
        if (entity instanceof InkSquidEntity)
            return true;

        return hasCapability(entity) && get(entity).isSquid();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return (CompoundTag) CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null, nbt);
    }
} */

