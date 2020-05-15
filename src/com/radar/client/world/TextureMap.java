package com.radar.client.world;

import java.io.File;
import java.io.IOException;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * @author radar
 * Class to load in textures and bind them to render
 */
public class TextureMap {
	Texture t;
	
	private static int textureHeight;
	
	private static int textureRes = 16;
	
	private static float[][] textureCoords;
	
//	public static Texture cubeMapTex;
//	
//	public static int cubeMap;
	
	public TextureMap(GL2 gl) {
//		int[] texFaces = new int[] {GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
//				GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
//				GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z};
//		cubeMapTex = TextureIO.newTexture(GL.GL_TEXTURE_CUBE_MAP);
//		gl.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, cubeMap);
//		try {
//			for (int i = 0; i < 6; i++) {
//				TextureData texData = TextureIO.newTextureData(gl.getGLProfile(), new File("resources/stoneBlock.png"), true, null);
//				
//				cubeMapTex.updateImage(gl, texData, texFaces[i]);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		cubeMapTex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
//		cubeMapTex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
//		
//		cubeMapTex.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
//		cubeMapTex.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
//		
//		gl.glGenerateMipmap(GL2.GL_TEXTURE_2D);
//		
//		cubeMapTex.enable(gl);
//		cubeMapTex.bind(gl);
		try {
			File file = new File("resources/textureMapTransparent.png");
//			File file = new File("resources/largeTextureMap.png");
//			BufferedImage temp = ImageIO.read(file);
//			TextureData temp = TextureIO.newTextureData(GLProfile.get(GLProfile.GL2), file, GL2.GL_RGBA, GL2.GL_RGBA, false, ".png");
			t = TextureIO.newTexture(file, false);
//			t = new Texture(gl, new AWTTextureData(GLProfile.get(GLProfile.GL2), GL2.GL_RGBA, GL2.GL_RGBA, false, temp));
			
			textureHeight = t.getHeight();
			
			t.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
			t.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
			
			t.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
			t.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
			
			t.enable(gl);
			t.bind(gl);
			gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
			
			
		} catch (GLException e) {
			System.out.println("Ran into OpenGL error when loading texture");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Ran into IO error when loading texture");
			e.printStackTrace();
		}
		generateTextureCoords();
	}
	
	public void close(GL2 gl) {
		t.destroy(gl);
		System.out.println("Destroyed Texture Map");
	}
	
	private static void generateTextureCoords() {
		textureCoords = new float [(int) Math.pow(textureHeight/textureRes,2)][3];
		
		for (int x = 0; x < textureHeight/textureRes; x++) {
			for (int y = 0; y < textureHeight/textureRes; y++) {
				textureCoords[x+ ((textureHeight/textureRes)*y)] = new float[] {(x*textureRes)/(float)textureHeight, (y*textureRes)/(float)textureHeight, textureRes/(float)textureHeight};
			}
		}
	}
	
	public static float[] getTexCoords(int texture) {
		return textureCoords[texture - 1];
	}
}