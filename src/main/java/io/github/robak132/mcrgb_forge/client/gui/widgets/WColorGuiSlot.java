package io.github.robak132.mcrgb_forge.client.gui.widgets;

import io.github.robak132.libgui_forge.LibGui;
import io.github.robak132.libgui_forge.client.ScreenDrawing;
import io.github.robak132.libgui_forge.widget.TooltipBuilder;
import io.github.robak132.libgui_forge.widget.WWidget;
import io.github.robak132.libgui_forge.widget.data.InputResult;
import io.github.robak132.mcrgb_forge.client.analysis.IItemBlockColorSaver;
import io.github.robak132.mcrgb_forge.config.MCRGBConfig;
import io.github.robak132.mcrgb_forge.client.gui.ColorGui;
import java.util.List;
import java.util.Objects;
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


public class WColorGuiSlot extends WWidget {

    public static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(LibGui.MOD_ID, "textures/widget/item_slot.png");
    ItemStack stack;
    ColorGui parentGui;

    public WColorGuiSlot(ItemStack stack, ColorGui parentGui) {
        this.stack = stack;
        this.parentGui = parentGui;
    }

    @Override
    public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
        context.renderItem(stack, x + 1, y + 1);
        ScreenDrawing.texturedRect(context, x, y, 18, 18, SLOT_TEXTURE, 0, 0, .28125f, .28125f, 0xFFFFFFFF);
    }

    @Override
    public InputResult onClick(int x, int y, int button) {
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);

        // x & y are the coordinates of the mouse when the event was triggered
        // int button is which button was pressed
        String nbt = "";
        if (stack.hasTag()) {
            nbt = stack.getOrCreateTag().toString();

        }
        String command = MCRGBConfig.GIVE_COMMAND.get();

        command = command.replace("%c", nbt);
        switch (button) {
            case 0:
                if (!((player.hasPermissions(2) && player.isCreative()) || MCRGBConfig.BYPASS_OP.get())) {
                    return InputResult.PROCESSED;
                }
                command = command.replace("%p", player.getName().getString());
                command = command.replace("%i", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())).toString());
                command = command.replace("%q", "1");
                player.connection.sendCommand(command);
                break;
            case 1:
                if (stack.getItem() instanceof BlockItem) {
                    parentGui.openBlockInfoGui(stack);
                }
                break;
            case 2:
                if (!((player.hasPermissions(2) && player.isCreative()) || MCRGBConfig.BYPASS_OP.get())) {
                    return InputResult.PROCESSED;
                }
                command = command.replace("%p", player.getName().getString());
                command = command.replace("%i", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())).toString());
                command = command.replace("%q", Integer.toString(stack.getMaxStackSize()));
                player.connection.sendCommand(command);
                break;
        }
        return InputResult.PROCESSED;
    }

    @Override
    public void addTooltip(TooltipBuilder tooltip) {
        tooltip.add(Component.translatable(stack.getDescriptionId()));
        IItemBlockColorSaver item = (IItemBlockColorSaver) stack.getItem();
        for (int i = 0; i < item.mcrgb_forge$getLength(); i++) {
            List<String> strings = item.mcrgb_forge$getSpriteDetails(i).getStrings();
            List<Integer> colors = item.mcrgb_forge$getSpriteDetails(i).getTextColors();
            if (!strings.isEmpty()) {
                for (int j = 0; j < strings.size(); j++) {
                    var text = Component.literal(strings.get(j)).withStyle(ChatFormatting.GRAY);
                    MutableComponent text2 = (MutableComponent) Component.literal("⬛").toFlatList(Style.EMPTY.withColor(colors.get(j))).get(0);
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
