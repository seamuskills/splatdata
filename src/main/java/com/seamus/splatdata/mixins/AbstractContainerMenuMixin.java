package com.seamus.splatdata.mixins;

import com.seamus.splatdata.menus.MenuContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//not a mouthful totally
@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
    @Inject(method = "doClick", cancellable = true, at = @At("HEAD"))
    public void doClick(int slot, int id, ClickType clickType, Player player, CallbackInfo ci){
        if (player.level.isClientSide){
            return;
        }
        MenuContainer container = null;
        AbstractContainerMenu self = (AbstractContainerMenu)(Object)this;

        if (self instanceof ChestMenu chestMenu && chestMenu.getContainer() instanceof MenuContainer menu){
            container = menu;
        }else if (self instanceof DispenserMenu dispenserMenu && ((DispenserAccessor)dispenserMenu).getDispenser() instanceof MenuContainer menu){
            container = menu;
        }else if (self instanceof HopperMenu hopperMenu && ((HopperAccessor)hopperMenu).getHopper() instanceof MenuContainer menu){
            container = menu;
        }

        if (container == null){
            return;
        }
        if (clickType != ClickType.PICKUP || slot >= container.getContainerSize()){
            ci.cancel(); //nuh uh
        }
    }
}
