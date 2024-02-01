package com.seamus.splatdata.mixins;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.HopperMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HopperMenu.class)
public interface HopperAccessor {
    @Accessor
    Container getHopper();
}
