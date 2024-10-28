package com.dooji.timewarp.mixin;

import com.dooji.timewarp.Timewarp;
import com.dooji.timewarp.items.TimewarpAxe;
import com.dooji.timewarp.world.TimewarpArea;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WorldRenderer.class)
public class TimewarpWireframeMixin {
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "render", at = @At("TAIL"))
    public void renderWireframe(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
// TODO: Fix da wireframe :(

//        PlayerEntity player = client.player;
//        if (player == null || client.world == null || !(player.getMainHandStack().getItem() instanceof TimewarpAxe)) return;
//
//        MatrixStack matrices = new MatrixStack();
//
//        Vec3d cameraPos = camera.getPos();
//        double camX = cameraPos.getX();
//        double camY = cameraPos.getY();
//        double camZ = cameraPos.getZ();
//
//        VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
//        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
//
//        List<TimewarpArea> areas = Timewarp.getInstance().getTimewarpAreas();
//        for (TimewarpArea area : areas) {
//            BlockPos corner1 = area.getCorner1();
//            BlockPos corner2 = area.getCorner2();
//
//            int centerX = (corner1.getX() + corner2.getX()) / 2;
//            int centerY = (corner1.getY() + corner2.getY()) / 2;
//            int centerZ = (corner1.getZ() + corner2.getZ()) / 2;
//            BlockPos centerPos = new BlockPos(centerX, centerY, centerZ);
//
//            VoxelShape shape = VoxelShapes.cuboid(
//                    0.0, 0.0, 0.0,
//                    Math.abs(corner2.getX() - corner1.getX()),
//                    Math.abs(corner2.getY() - corner1.getY()),
//                    Math.abs(corner2.getZ() - corner1.getZ())
//            );
//
//            drawCuboidShapeOutline1(matrices, vertexConsumer, shape, (double) centerPos.getX() - camX, (double) centerPos.getY() - camY, (double) centerPos.getZ() - camZ, 0.525F, 0.627F, 1.0F, 0.4F);
//        }
//
//        BlockPos[] selection = TimewarpAxe.getSelection(player);
//        if (selection[0] != null && selection[1] != null) {
//            int centerX = (selection[0].getX() + selection[1].getX()) / 2;
//            int centerY = (selection[0].getY() + selection[1].getY()) / 2;
//            int centerZ = (selection[0].getZ() + selection[1].getZ()) / 2;
//            BlockPos centerPos = new BlockPos(centerX, centerY, centerZ);
//
//            VoxelShape shape2 = VoxelShapes.cuboid(
//                    0.0, 0.0, 0.0,
//                    Math.abs(selection[1].getX() - selection[0].getX()),
//                    Math.abs(selection[1].getY() - selection[0].getY()),
//                    Math.abs(selection[1].getZ() - selection[0].getZ())
//            );
//
//            drawCuboidShapeOutline2(matrices, vertexConsumer, shape2, (double) centerPos.getX() - camX, (double) centerPos.getY() - camY, (double) centerPos.getZ() - camZ, 1.0F, 1.0F, 0.0F, 0.8F);
//        }
    }

    @Unique
    private static void drawCuboidShapeOutline1(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {
        MatrixStack.Entry entry = matrices.peek();
        shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float k = (float)(maxX - minX);
            float l = (float)(maxY - minY);
            float m = (float)(maxZ - minZ);
            float n = MathHelper.sqrt(k * k + l * l + m * m);
            k /= n;
            l /= n;
            m /= n;
            vertexConsumer.vertex(entry, (float)(minX + offsetX), (float)(minY + offsetY), (float)(minZ + offsetZ)).color(red, green, blue, alpha).normal(entry, k, l, m);
            vertexConsumer.vertex(entry, (float)(maxX + offsetX), (float)(maxY + offsetY), (float)(maxZ + offsetZ)).color(red, green, blue, alpha).normal(entry, k, l, m);
        });
    }

    @Unique
    private static void drawCuboidShapeOutline2(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {
        MatrixStack.Entry entry = matrices.peek();
        shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float k = (float)(maxX - minX);
            float l = (float)(maxY - minY);
            float m = (float)(maxZ - minZ);
            float n = MathHelper.sqrt(k * k + l * l + m * m);
            k /= n;
            l /= n;
            m /= n;
            vertexConsumer.vertex(entry, (float)(minX + offsetX), (float)(minY + offsetY), (float)(minZ + offsetZ)).color(red, green, blue, alpha).normal(entry, k, l, m);
            vertexConsumer.vertex(entry, (float)(maxX + offsetX), (float)(maxY + offsetY), (float)(maxZ + offsetZ)).color(red, green, blue, alpha).normal(entry, k, l, m);
        });
    }
}