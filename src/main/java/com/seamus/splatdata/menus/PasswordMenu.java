package com.seamus.splatdata.menus;

import com.seamus.splatdata.menus.buttons.FunctionButton;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;

public class PasswordMenu extends MenuContainer{
    ExecutableFunction func;
    ArrayList<Integer> password;
    public PasswordMenu(ServerPlayer player, ExecutableFunction function) {
        super(MenuSize.SIZE_6X9, new TextComponent("password"), player);
        func = function;
        password = new ArrayList<>();
        init2(player);
    }

    public void init2(ServerPlayer player) {
        addButton(0, new FunctionButton(new ItemStack(Items.GREEN_WOOL), new TextComponent("Confirm: " + Arrays.toString(password.toArray())), (p) -> {
            func.execute(p, password);
            p.closeContainer();
        }));
        for (int i = 0; i < 10; i++){
            final int n = i + 1;
            ItemStack buttonItem = new ItemStack(Items.ENDER_PEARL);
            buttonItem.setCount(n);
            addButton(1 + (i / 3),  i != 9 ? 3 + (i % 3) : 4, new FunctionButton(buttonItem, new TextComponent(Integer.toString(n)), (p) -> {
                password.add(n);
                init2(player);
            }));
        }
        addButton(5, 8, new FunctionButton(new ItemStack(Items.RED_WOOL), new TextComponent("Back").withStyle(ChatFormatting.RED), (p) -> {
            if (!password.isEmpty()){
                password.remove(password.size()-1);
                init2(player);
            }
        }));
    }

    @Override
    public void init(ServerPlayer player) {
    }

    public interface ExecutableFunction{
        void execute(ServerPlayer player, ArrayList<Integer> password);
    }
}
