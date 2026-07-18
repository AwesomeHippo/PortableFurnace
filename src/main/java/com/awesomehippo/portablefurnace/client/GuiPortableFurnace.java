package com.awesomehippo.portablefurnace.client;

import com.awesomehippo.portablefurnace.inventory.ContainerPortableFurnace;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiPortableFurnace extends AbstractContainerScreen<ContainerPortableFurnace> {

    private static final ResourceLocation FURNACE_TEXTURE = new ResourceLocation("textures/gui/container/furnace.png");

    public GuiPortableFurnace(ContainerPortableFurnace menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String title = this.title.getString();
        guiGraphics.drawString(this.font, title, this.imageWidth / 2 - this.font.width(title) / 2, 6, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int guiLeft = this.leftPos;
        int guiTop = this.topPos;
        guiGraphics.blit(FURNACE_TEXTURE, guiLeft, guiTop, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.getBurnTime() > 0) {
            int burnHeight = getBurnLeftScaled(13);
            guiGraphics.blit(FURNACE_TEXTURE, guiLeft + 56, guiTop + 36 + 12 - burnHeight, 176, 12 - burnHeight, 14, burnHeight + 1);
        }

        int cookWidth = getCookProgressScaled(24);
        guiGraphics.blit(FURNACE_TEXTURE, guiLeft + 79, guiTop + 34, 176, 14, cookWidth + 1, 16);
    }

    private int getBurnLeftScaled(int pixels) {
        int total = this.menu.getCurrentItemBurnTime();
        if (total <= 0) {
            total = 200;
        }
        return this.menu.getBurnTime() * pixels / total;
    }

    private int getCookProgressScaled(int pixels) {
        int total = this.menu.getCookTimeTotal();
        if (total <= 0) {
            return 0;
        }
        return this.menu.getCookTime() * pixels / total;
    }
}
