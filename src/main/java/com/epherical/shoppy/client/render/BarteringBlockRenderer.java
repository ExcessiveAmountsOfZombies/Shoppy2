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
import net.minecraft.world.phys.Vec3;

public class BarteringBlockRenderer<T extends BarteringBlockEntity> implements BlockEntityRenderer<T> {

    private final ItemRenderer renderer;

    public BarteringBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.renderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 cameraPos) {
        Direction direction = blockEntity.getBlockState().getValue(AbstractTradingBlock.FACING);
        int k = (int) blockEntity.getBlockPos().asLong();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.55D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
        poseStack.scale(1F, 1F, 1F);
        renderer.renderStatic(blockEntity.getSaleItem(), ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, null, k);
        poseStack.scale(1F, 1F, 1F);
        poseStack.translate(-0.350D, -0.55D, -0.74);
        poseStack.popPose();
    }
}
