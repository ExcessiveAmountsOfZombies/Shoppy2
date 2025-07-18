package com.epherical.shoppy.client.render;

import com.epherical.shoppy.block.AbstractTradingBlock;
import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;

public class BarteringBlockRenderer<T extends BarteringBlockEntity> implements BlockEntityRenderer<T> {

    private final ItemRenderer renderer;

    public BarteringBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.renderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        Direction direction = blockEntity.getBlockState().getValue(AbstractTradingBlock.FACING);
        int k = (int) blockEntity.getBlockPos().asLong();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.55D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
        poseStack.scale(1F, 1F, 1F);
        renderer.renderStatic(blockEntity.getSaleItem(), ItemDisplayContext.GROUND, i, j, poseStack, multiBufferSource, null, k);
        poseStack.scale(1F, 1F, 1F);
        poseStack.translate(-0.350D, -0.55D, -0.74);
        poseStack.popPose();
    }
}
