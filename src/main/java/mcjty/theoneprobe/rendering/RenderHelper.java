package mcjty.theoneprobe.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.network.ThrowableIdentity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class RenderHelper {

    public static float rot = 0.0f;

    public static void renderEntity(Entity entity, MatrixStack matrixStack, int xPos, int yPos, float scale) {
        matrixStack.push();
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableColorMaterial();
        matrixStack.translate(xPos + 8, yPos + 24, 50F);
        matrixStack.scale(-scale, scale, scale);
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));
        matrixStack.rotate(Vector3f.YP.rotationDegrees(135));
        net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
        matrixStack.rotate(Vector3f.YP.rotationDegrees(-135));
        matrixStack.rotate(Vector3f.YP.rotationDegrees(rot));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(0));

        if (!(entity instanceof PlayerEntity)) {
            entity.rotationPitch = 0.0F;
            entity.prevRotationPitch = 0.0F;
            entity.rotationYaw = 0.0f;
            entity.prevRotationYaw = 0.0f;

            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.prevRenderYawOffset = 0.0f;
                livingEntity.renderYawOffset = 0.0f;

                livingEntity.rotationYawHead = 0.0f;
                livingEntity.prevRotationYawHead = 0.0f;
            }
        }

        matrixStack.translate(0.0F, (float) entity.getYOffset() + (entity instanceof HangingEntity ? 0.5F : 0.0F), 0.0F);

        try {
            IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            Minecraft.getInstance().getRenderManager().renderEntityStatic(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixStack, buffer, 15728880);
            buffer.finish();
        } catch (Exception e) {
            TheOneProbe.logger.error("Error rendering entity!", e);
        }
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();

        RenderSystem.disableRescaleNormal();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableLighting();
        RenderSystem.enableDepthTest();
        RenderSystem.disableColorMaterial();
        Minecraft.getInstance().gameRenderer.getLightTexture().disableLightmap();
        matrixStack.pop();
    }

    public static void drawHorizontalLine(MatrixStack matrixStack, int x1, int y1, int x2, int color) {
        AbstractGui.fill(matrixStack, x1, y1, x2, y1 + 1, color);
    }

    public static void drawVerticalLine(MatrixStack matrixStack, int x1, int y1, int y2, int color) {
        AbstractGui.fill(matrixStack, x1, y1, x1 + 1, y2, color);
    }

    /**
     * Draw a thick beveled box. x2 and y2 are not included.
     */
    public static void drawThickBeveledBox(MatrixStack matrixStack, int x1, int y1, int x2, int y2, int thickness, int topleftcolor, int botrightcolor, int fillcolor) {
        if (fillcolor != -1) {
            AbstractGui.fill(matrixStack, x1 + 1, y1 + 1, x2 - 1, y2 - 1, fillcolor);
        }
        AbstractGui.fill(matrixStack, x1, y1, x2 - 1, y1 + thickness, topleftcolor);
        AbstractGui.fill(matrixStack, x1, y1, x1 + thickness, y2 - 1, topleftcolor);
        AbstractGui.fill(matrixStack, x2 - thickness, y1, x2, y2 - 1, botrightcolor);
        AbstractGui.fill(matrixStack, x1, y2 - thickness, x2, y2, botrightcolor);
    }

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
     */
    public static void drawTexturedModalRect(Matrix4f matrix, int x, int y, int u, int v, int width, int height, int twidth, int theight) {
        float zLevel = 0.01f;
        float f = (1.0f / twidth);
        float f1 = (1.0f / theight);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        buffer.pos(matrix, (x + 0), (y + height), zLevel).tex(((u + 0) * f), ((v + height) * f1)).endVertex();
        buffer.pos(matrix, (x + width), (y + height), zLevel).tex(((u + width) * f), ((v + height) * f1)).endVertex();
        buffer.pos(matrix, (x + width), (y + 0), zLevel).tex(((u + width) * f), ((v + 0) * f1)).endVertex();
        buffer.pos(matrix, (x + 0), (y + 0), zLevel).tex(((u + 0) * f), ((v + 0) * f1)).endVertex();
        tessellator.draw();
    }

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
     */
    public static void drawTexturedModalRect(Matrix4f matrix, int x, int y, int u, int v, int width, int height) {
        float zLevel = 0.01f;
        float f = (1 / 256.0f);
        float f1 = (1 / 256.0f);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        buffer.pos(matrix, (x + 0), (y + height), zLevel).tex(((u + 0) * f), ((v + height) * f1)).endVertex();
        buffer.pos(matrix, (x + width), (y + height), zLevel).tex(((u + width) * f), ((v + height) * f1)).endVertex();
        buffer.pos(matrix, (x + width), (y + 0), zLevel).tex(((u + width) * f), ((v + 0) * f1)).endVertex();
        buffer.pos(matrix, (x + 0), (y + 0), zLevel).tex(((u + 0) * f), ((v + 0) * f1)).endVertex();
        tessellator.draw();
    }

    public static void drawTexturedModalRect(Matrix4f matrix, int x, int y, TextureAtlasSprite sprite, int width, int height) {
        float zLevel = 0.01f;
        float f = (1 / 256.0f);
        float f1 = (1 / 256.0f);

        float u1 = sprite.getMinU();
        float v1 = sprite.getMinV();
        float u2 = sprite.getMaxU();
        float v2 = sprite.getMaxV();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(matrix, (x + 0), (y + height), zLevel).tex(u1, v1).endVertex();
        buffer.pos(matrix, (x + width), (y + height), zLevel).tex(u1, v2).endVertex();
        buffer.pos(matrix, (x + width), (y + 0), zLevel).tex(u2, v2).endVertex();
        buffer.pos(matrix, (x + 0), (y + 0), zLevel).tex(u2, v1).endVertex();
        tessellator.draw();
    }

    public static boolean renderItemStack(Minecraft mc, ItemRenderer itemRender, ItemStack itm, MatrixStack matrixStack, int x, int y, String txt) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0f);

        boolean rc = true;
        if (!itm.isEmpty() && itm.getItem() != null) {
            matrixStack.push();
            matrixStack.translate(0.0F, 0.0F, 32.0F);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableRescaleNormal();
            RenderSystem.enableLighting();
            short short1 = 240;
            short short2 = 240;
            net.minecraft.client.renderer.RenderHelper.setupGui3DDiffuseLighting();
            // @todo 1.15
//            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, short1 / 1.0F, short2 / 1.0F);
            try {
                //Note: As ItemRenderer#renderItemAndEffectIntoGui still is entirely based on GL states instead of matrix stacks
                // we need to massage the matrix stack into it
                RenderSystem.pushMatrix();
                RenderSystem.multMatrix(matrixStack.getLast().getMatrix());
                itemRender.renderItemAndEffectIntoGUI(itm, x, y);
                RenderSystem.popMatrix();
                renderItemStackOverlay(matrixStack, mc.fontRenderer, itm, x, y, txt, txt.length() - 2);
            } catch (Exception e) {
                ThrowableIdentity.registerThrowable(e);
                rc = false; // Report error
            }
            matrixStack.pop();
            RenderSystem.disableRescaleNormal();
            RenderSystem.disableLighting();
        }

        return rc;
    }

    /**
     * Renders the stack size and/or damage bar for the given ItemStack.
     */
    public static void renderItemStackOverlay(MatrixStack matrixStack, FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text,
                                              int scaled) {
        if (!stack.isEmpty()) {
            if (stack.getCount() != 1 || text != null) {
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                if (text == null && stack.getCount() < 1) {
                    s = TextFormatting.RED + String.valueOf(stack.getCount());
                }

                matrixStack.translate(0.0D, 0.0D, (Minecraft.getInstance().getItemRenderer().zLevel + 200.0F));

                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.disableBlend();
                if (scaled >= 2) {
                    matrixStack.push();
                    matrixStack.scale(.5f, .5f, .5f);
                    fr.drawStringWithShadow(matrixStack, s, ((xPosition + 19 - 2) * 2 - 1 - fr.getStringWidth(s)), yPosition * 2 + 24, 16777215);
                    matrixStack.pop();
                } else if (scaled == 1) {
                    matrixStack.push();
                    matrixStack.scale(.75f, .75f, .75f);
                    fr.drawStringWithShadow(matrixStack, s, ((xPosition - 2) * 1.34f + 24 - fr.getStringWidth(s)), yPosition * 1.34f + 14, 16777215);
                    matrixStack.pop();
                } else {
                    fr.drawStringWithShadow(matrixStack, s, (xPosition + 19 - 2 - fr.getStringWidth(s)), (yPosition + 6 + 3), 16777215);
                }
                RenderSystem.enableLighting();
                RenderSystem.enableDepthTest();
                // Fixes opaque cooldown overlay a bit lower
                // TODO: check if enabled blending still screws things up down the line.
                RenderSystem.enableBlend();
            }

            if (stack.getItem().showDurabilityBar(stack)) {
                double health = stack.getItem().getDurabilityForDisplay(stack);
                int j = (int) Math.round(13.0D - health * 13.0D);
                int i = (int) Math.round(255.0D - health * 255.0D);
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder vertexbuffer = tessellator.getBuffer();
                Matrix4f matrix = matrixStack.getLast().getMatrix();
                draw(vertexbuffer, matrix, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                draw(vertexbuffer, matrix, xPosition + 2, yPosition + 13, 12, 1, (255 - i) / 4, 64, 0, 255);
                draw(vertexbuffer, matrix, xPosition + 2, yPosition + 13, j, 1, 255 - i, i, 0, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableAlphaTest();
                RenderSystem.enableTexture();
                RenderSystem.enableLighting();
                RenderSystem.enableDepthTest();
            }

            PlayerEntity PlayerEntitysp = Minecraft.getInstance().player;
            float f = PlayerEntitysp == null ? 0.0F : PlayerEntitysp.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getInstance().getRenderPartialTicks());

            if (f > 0.0F) {
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                Tessellator tessellator1 = Tessellator.getInstance();
                BufferBuilder vertexbuffer1 = tessellator1.getBuffer();
                draw(vertexbuffer1, matrixStack.getLast().getMatrix(), xPosition, yPosition + (int) Math.floor(16.0F * (1.0F - f)), 16, (int) Math.ceil(16.0F * f), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableLighting();
                RenderSystem.enableDepthTest();
            }
        }
    }

    /**
     * Draw with the WorldRenderer
     */
    private static void draw(BufferBuilder renderer, Matrix4f matrix, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(matrix, (x + 0), (y + 0), 0).color(red, green, blue, alpha).endVertex();
        renderer.pos(matrix, (x + 0), (y + height), 0).color(red, green, blue, alpha).endVertex();
        renderer.pos(matrix, (x + width), (y + height), 0).color(red, green, blue, alpha).endVertex();
        renderer.pos(matrix, (x + width), (y + 0), 0).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }


    public static int renderText(Minecraft mc, MatrixStack matrixStack, int x, int y, String txt) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0f);

        matrixStack.push();
        matrixStack.translate(0.0F, 0.0F, 32.0F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableLighting();
        net.minecraft.client.renderer.RenderHelper.setupGui3DDiffuseLighting();

        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        int width = mc.fontRenderer.getStringWidth(txt);
        mc.fontRenderer.drawStringWithShadow(matrixStack, txt, x, y, 16777215);
        RenderSystem.enableLighting();
        RenderSystem.enableDepthTest();
        // Fixes opaque cooldown overlay a bit lower
        // TODO: check if enabled blending still screws things up down the line.
        RenderSystem.enableBlend();


        matrixStack.pop();
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableLighting();

        return width;
    }

    public static class Vector {
        public final float x;
        public final float y;
        public final float z;

        public Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getZ() {
            return z;
        }

        public float norm() {
            return (float) Math.sqrt(x * x + y * y + z * z);
        }

        public Vector normalize() {
            float n = norm();
            return new Vector(x / n, y / n, z / n);
        }
    }

    private static Vector Cross(Vector a, Vector b) {
        float x = a.y * b.z - a.z * b.y;
        float y = a.z * b.x - a.x * b.z;
        float z = a.x * b.y - a.y * b.x;
        return new Vector(x, y, z);
    }

    private static Vector Sub(Vector a, Vector b) {
        return new Vector(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    private static Vector Add(Vector a, Vector b) {
        return new Vector(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    private static Vector Mul(Vector a, float f) {
        return new Vector(a.x * f, a.y * f, a.z * f);
    }


}
