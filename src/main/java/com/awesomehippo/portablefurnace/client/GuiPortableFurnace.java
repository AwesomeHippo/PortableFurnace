package com.awesomehippo.portablefurnace.client;

import com.awesomehippo.portablefurnace.inventory.ContainerPortableFurnace;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPortableFurnace extends GuiContainer {

    private static final ResourceLocation FURNACE_TEXTURE = new ResourceLocation("textures/gui/container/furnace.png");

    private final ContainerPortableFurnace container;

    public GuiPortableFurnace(EntityPlayer player, EnumHand hand) {
        super(new ContainerPortableFurnace(player, hand));
        this.container = (ContainerPortableFurnace) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = I18n.format("container.portablefurnace.portable_furnace");
        this.fontRenderer.drawString(title, this.xSize / 2 - this.fontRenderer.getStringWidth(title) / 2, 6, 0x404040);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(FURNACE_TEXTURE);

        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);

        if (container.getBurnTime() > 0) {
            int burnHeight = getBurnLeftScaled(13);
            this.drawTexturedModalRect(guiLeft + 56, guiTop + 36 + 12 - burnHeight, 176, 12 - burnHeight, 14, burnHeight + 1);
        }

        int cookWidth = getCookProgressScaled(24);
        this.drawTexturedModalRect(guiLeft + 79, guiTop + 34, 176, 14, cookWidth + 1, 16);
    }

    private int getBurnLeftScaled(int pixels) {
        int total = container.getCurrentItemBurnTime();
        if (total <= 0) {
            total = 200;
        }
        return container.getBurnTime() * pixels / total;
    }

    private int getCookProgressScaled(int pixels) {
        int total = container.getCookTimeTotal();
        if (total <= 0) {
            return 0;
        }
        return container.getCookTime() * pixels / total;
    }
}
