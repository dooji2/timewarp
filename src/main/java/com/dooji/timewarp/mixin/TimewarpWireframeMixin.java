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
    @Unique
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "render", at = @At("TAIL"))
    public void renderWireframe(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        PlayerEntity player = client.player;
        if (player == null || client.world == null || !(player.getMainHandStack().getItem() instanceof TimewarpAxe)) return;

        MatrixStack matrices = new MatrixStack();

        Vec3d cameraPos = camera.getPos();
        double camX = cameraPos.getX();
        double camY = cameraPos.getY();
        double camZ = cameraPos.getZ();

        VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());

        List<TimewarpArea> areas = Timewarp.getInstance().getTimewarpAreas();
        int renderDistance = client.options.getViewDistance().getValue() * 16;

        for (TimewarpArea area : areas) {
            BlockPos corner1 = area.getCorner1();
            BlockPos corner2 = area.getCorner2();

            double centerX = (corner1.getX() + corner2.getX()) / 2.0 + 0.5;
            double centerY = (corner1.getY() + corner2.getY()) / 2.0 + 0.5;
            double centerZ = (corner1.getZ() + corner2.getZ()) / 2.0 + 0.5;

            double distanceToArea = player.squaredDistanceTo(centerX, centerY, centerZ);
            if (distanceToArea > renderDistance * renderDistance) continue;

            double widthX = Math.abs(corner2.getX() - corner1.getX()) + 1.0;
            double heightY = Math.abs(corner2.getY() - corner1.getY()) + 1.0;
            double depthZ = Math.abs(corner2.getZ() - corner1.getZ()) + 1.0;

            VoxelShape shape = VoxelShapes.cuboid(
                    -widthX / 2, -heightY / 2, -depthZ / 2,
                    widthX / 2, heightY / 2, depthZ / 2
            );

            drawCuboidShapeOutline(matrices, vertexConsumer, shape, centerX - camX, centerY - camY, centerZ - camZ, 0.525F, 0.627F, 1.0F, 0.4F);
        }

        BlockPos[] selection = TimewarpAxe.getSelection(player);
        if (selection[0] != null && selection[1] != null) {
            double centerX = (selection[0].getX() + selection[1].getX()) / 2.0 + 0.5;
            double centerY = (selection[0].getY() + selection[1].getY()) / 2.0 + 0.5;
            double centerZ = (selection[0].getZ() + selection[1].getZ()) / 2.0 + 0.5;

            double widthX = Math.abs(selection[1].getX() - selection[0].getX()) + 1.0;
            double heightY = Math.abs(selection[1].getY() - selection[0].getY()) + 1.0;
            double depthZ = Math.abs(selection[1].getZ() - selection[0].getZ()) + 1.0;

            VoxelShape shape2 = VoxelShapes.cuboid(
                    -widthX / 2, -heightY / 2, -depthZ / 2,
                    widthX / 2,  heightY / 2,  depthZ / 2
            );

            drawCuboidShapeOutline(matrices, vertexConsumer, shape2, centerX - camX, centerY - camY, centerZ - camZ, 1.0F, 1.0F, 0.0F, 0.8F);
        }
    }

    @Unique
    private static void drawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {
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