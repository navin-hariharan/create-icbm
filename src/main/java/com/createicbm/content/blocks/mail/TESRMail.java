package com.createicbm.content.blocks.mail;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4f;

/**
 * Writes the sender's name on the parcel's label, and shows "Mail from ..." above it while you look at it
 */
public class TESRMail implements BlockEntityRenderer<TileMail>
{
    /** Top of the label in the model, in pixels */
    private static final float LABEL_TOP = 8.25F;
    /** Room for the name along the label and across it, in pixels */
    private static final float NAME_WIDTH = 10.5F;
    private static final float NAME_HEIGHT = 3.2F;

    private static final int INK = 0xFF2B2E4A;
    private static final int INK_FADED = 0xFF7A6A58;

    private final Font font;

    public TESRMail(BlockEntityRendererProvider.Context context)
    {
        this.font = context.getFont();
    }

    @Override
    public void render(TileMail mail, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay)
    {
        final Component sender = MailDelivery.senderName(mail);
        final Direction facing = mail.getBlockState().hasProperty(BlockMail.FACING) ? mail.getBlockState().getValue(BlockMail.FACING) : Direction.NORTH;

        renderLabel(pose, buffers, light, sender, facing);

        final HitResult hit = Minecraft.getInstance().hitResult;
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK && blockHit.getBlockPos().equals(mail.getBlockPos()))
        {
            renderNameplate(pose, buffers, light, sender);
        }
    }

    /** Flat on the label, reading from the side the parcel faces */
    private void renderLabel(PoseStack pose, MultiBufferSource buffers, int light, Component sender, Direction facing)
    {
        pose.pushPose();
        pose.translate(0.5, LABEL_TOP / 16.0 + 0.002, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(-90));
        // Pixels from here on, y pointing down the label like on a page
        pose.scale(1 / 16F, -1 / 16F, 1 / 16F);

        // "From:" in small letters near the top edge
        final Component from = Component.translatable("info.create_icbm:mail.from");
        drawText(pose, buffers, light, from, -NAME_WIDTH / 2, -2.6F, 0.16F, INK_FADED, false);

        // The name as large as fits
        final float scale = Math.min(NAME_HEIGHT / font.lineHeight, NAME_WIDTH / Math.max(1, font.width(sender)));
        drawText(pose, buffers, light, sender, 0, 0.4F, scale, INK, true);

        pose.popPose();
    }

    private void drawText(PoseStack pose, MultiBufferSource buffers, int light, Component text, float x, float y, float scale, int color, boolean centered)
    {
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, scale);
        final float offsetX = centered ? -font.width(text) / 2F : 0;
        final float offsetY = -font.lineHeight / 2F;
        final Matrix4f matrix = pose.last().pose();
        font.drawInBatch(text, offsetX, offsetY, color, false, matrix, buffers, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        pose.popPose();
    }

    /** Name tag style text over the parcel, turned to the camera */
    private void renderNameplate(PoseStack pose, MultiBufferSource buffers, int light, Component sender)
    {
        final Component text = Component.translatable("info.create_icbm:mail.nameplate", sender);
        pose.pushPose();
        pose.translate(0.5, 0.95, 0.5);
        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        pose.scale(0.025F, -0.025F, 0.025F);
        final Matrix4f matrix = pose.last().pose();
        final float x = -font.width(text) / 2F;
        final int background = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        font.drawInBatch(text, x, 0, 0x20FFFFFF, false, matrix, buffers, Font.DisplayMode.SEE_THROUGH, background, light);
        font.drawInBatch(text, x, 0, 0xFFFFFFFF, false, matrix, buffers, Font.DisplayMode.NORMAL, 0, light);
        pose.popPose();
    }
}
