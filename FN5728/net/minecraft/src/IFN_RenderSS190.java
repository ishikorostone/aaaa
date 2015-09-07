// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

// Referenced classes of package net.minecraft.src:
//            Render, EntityArrow, Tessellator, MathHelper, 
//            Entity

public class IFN_RenderSS190 extends Render {

	public void renderSS190(IFN_EntitySS190 entityss190, double d, double d1, double d2, float f, float f1) {
//        loadTexture("/item/arrows.png");
		GL11.glPushMatrix();
		GL11.glTranslatef((float)d, (float)d1, (float)d2);
		GL11.glRotatef((entityss190.prevRotationYaw + (entityss190.rotationYaw - entityss190.prevRotationYaw) * f1) - 90F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(entityss190.prevRotationPitch + (entityss190.rotationPitch - entityss190.prevRotationPitch) * f1, 0.0F, 0.0F, 1.0F);
		Tessellator tessellator = Tessellator.instance;
		int i = 0;
		float f10 = 0.05625F;
		GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glScalef(f10, f10, f10);
		
		GL11.glPushMatrix();
		GL11.glRotatef(45F, 1.0F, 0.0F, 0.0F);
		GL11.glTranslatef(-4.7F, 0.0F, 0.0F);
		GL11.glNormal3f(f10, 0.0F, 0.0F);
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(0.5F, 0.25F, 0.0F, 1.0F);
		tessellator.addVertex(4.5D, -0.5D, 0.0D);
		tessellator.addVertex(4.5D, 0.0D, -0.5D);
		tessellator.addVertex(4.5D, 0.5D, 0.0D);
		tessellator.addVertex(4.5D, 0.0D, 0.5D);
		tessellator.draw();
		GL11.glNormal3f(-f10, 0.0F, 0.0F);
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(0.4F, 0.25F, 0.0F, 1.0F);
		tessellator.addVertex(4.5D, 0.0D, 0.5D);
		tessellator.addVertex(4.5D, 0.5D, 0.0D);
		tessellator.addVertex(4.5D, 0.0D, -0.5D);
		tessellator.addVertex(4.5D, -0.5D, 0.0D);
		tessellator.draw();
		for (int j = 0; j < 4; j++) {
			GL11.glRotatef(90F, 1.0F, 0.0F, 0.0F);
			GL11.glNormal3f(0.0F, 0.0F, f10);
			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_F(0.5F, 0.25F, 0.0F, 1.0F);
			tessellator.addVertex(4.5D, -0.5D, 0.0D);
			tessellator.addVertex(6.5D, -0.5D, 0.0D);
			tessellator.addVertex(6.5D, 0.5D, 0.0D);
			tessellator.addVertex(4.5D, 0.5D, 0.0D);
			tessellator.draw();
		}
		GL11.glPopMatrix();
		
		// 曳光弾
		if (entityss190.isTracer) {
			double dx = entityss190.lastTickPosX - entityss190.posX;
			double dy = entityss190.lastTickPosY - entityss190.posY;
			double dz = entityss190.lastTickPosZ - entityss190.posZ;
			double lleng = MathHelper.sqrt_double(dx * dx + dy * dy +  dz * dz) * 4;
//System.out.println(lleng);			
//			lleng = 20D;
			if (lleng > 0.0F) {
				GL11.glPushMatrix();
				OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glColor4f(1.0F, 0.4F, 0.1F, 0.9F);
				GL11.glRotatef(90F, 1.0F, 0.0F, 0.0F);
				GL11.glNormal3f(0.0F, 0.0F, f10);
				
				tessellator.startDrawing(GL11.GL_TRIANGLE_FAN);
				tessellator.setBrightness(256);
//				tessellator.setColorRGBA_F(1.0F, 0.5F, 0.2F, 1.0F);
				tessellator.addVertex(-lleng, 0D, 00D);
				tessellator.addVertex(0D, 0.5D, 0.5D);
				tessellator.addVertex(0D, 0.5D, -0.5D);
				tessellator.addVertex(0D, -0.5D, -0.5D);
				tessellator.addVertex(0D, -0.5D, 0.5D);
				tessellator.draw();
				
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GL11.glPopMatrix();
			}
		}
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
		GL11.glPopMatrix();
	}

	public void doRender(Entity entity, double d, double d1, double d2, 
			float f, float f1) {
		renderSS190((IFN_EntitySS190)entity, d, d1, d2, f, f1);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity var1) {
		// テクスチャ使いません
		return null;
	}

}
