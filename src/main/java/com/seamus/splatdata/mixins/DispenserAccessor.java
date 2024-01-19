package com.seamus.splatdata.mixins;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.DispenserMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DispenserMenu.class)
public interface DispenserAccessor {
    @Accessor
    public Container getDispenser();
}
