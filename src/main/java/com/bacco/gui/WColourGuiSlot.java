package com.bacco.gui;

import com.bacco.IItemBlockColourSaver;
import com.bacco.MCRGBConfig;
import io.github.cottonmc.cotton.gui.client.LibGui;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;


public class WColourGuiSlot extends WWidget {
    public static final ResourceLocation SLOT_TEXTURE = ResourceLocation.tryBuild(LibGui.MOD_ID, "textures/widget/item_slot.png");
    LocalPlayer player = Minecraft.getInstance().player;
    ItemStack stack;
    ColourGui gui;

    public WColourGuiSlot(ItemStack stack, ColourGui gui) {
        this.stack = stack;
        this.gui = gui;
    }

    @Override
    public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
        context.renderItem(stack, x + 1, y + 1);
        ScreenDrawing.texturedRect(context, x, y, 18, 18, SLOT_TEXTURE, 0, 0, .28125f, .28125f, 0xFFFFFFFF);
    }

    @Override
    public InputResult onClick(int x, int y, int button) {
        // x & y are the coordinates of the mouse when the event was triggered
        // int button is which button was pressed
        String nbt = "";
        if (stack.hasTag()) {
            nbt = stack.getOrCreateTag().toString();

        }
        String command = MCRGBConfig.instance.command;

        command = command.replace("%c", nbt);
        switch (button) {
            case 0:
                if (!((player.hasPermissions(2) && player.isCreative()) || MCRGBConfig.instance.bypassOP))
                    return InputResult.PROCESSED;
                command = command.replace("%p", player.getName().getString());
                command = command.replace("%i", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                command = command.replace("%q", "1");
                player.connection.sendCommand(command);
                break;
            case 1:
                if (stack.getItem() instanceof BlockItem)
                    gui.OpenBlockInfoGui(gui.client, gui.mcrgb_forgeClient, stack);
                break;
            case 2:
                if (!((player.hasPermissions(2) && player.isCreative()) || MCRGBConfig.instance.bypassOP))
                    return InputResult.PROCESSED;
                command = command.replace("%p", player.getName().getString());
                command = command.replace("%i", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                command = command.replace("%q", Integer.toString(stack.getMaxStackSize()));
                player.connection.sendCommand(command);
                break;
        }
        return InputResult.PROCESSED;
    }

    @Override
    public void addTooltip(TooltipBuilder tooltip) {
        tooltip.add(Component.translatable(stack.getDescriptionId()));
        IItemBlockColourSaver item = (IItemBlockColourSaver) stack.getItem();
        for (int i = 0; i < item.getLength(); i++) {
            List<String> strings = item.getSpriteDetails(i).getStrings();
            List<Integer> colours = item.getSpriteDetails(i).getTextColours();
            if (!strings.isEmpty()) {
                for (int j = 0; j < strings.size(); j++) {
                    var text = Component.literal(strings.get(j)).withStyle(ChatFormatting.GRAY);
                    MutableComponent text2 = (MutableComponent) Component.literal("⬛").toFlatList(Style.EMPTY.withColor(colours.get(j))).get(0);
                    if (j > 0) {
                        text2.append(text);
                    } else {
                        text2 = text.withStyle(ChatFormatting.DARK_GRAY);
                    }

                    tooltip.add(text2);
                }
            }
        }
    }
}
