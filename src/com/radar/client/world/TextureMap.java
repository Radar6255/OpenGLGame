package com.radar.client.world;

import java.io.File;
import java.io.IOException;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class TextureMap {
	public static int dirtTexture;
	
	public TextureMap(GL2 gl) {
		try {
			File file = new File("resources/textureMap.png");
			Texture t = TextureIO.newTexture(file, true);
			t.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
			t.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
			
			t.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
			t.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			gl.glGenerateMipmap(GL2.GL_TEXTURE_2D);
			t.enable(gl);
			t.bind(gl);
			
			dirtTexture = t.getTextureObject();
		} catch (GLException e) {
			System.out.println("Ran into OpenGL error when loading texture");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Ran into IO error when loading texture");
			e.printStackTrace();
		}
	}
	
	public static float[] getTexCoords(int texture) {
		
		if (texture == 1) {
			return new float[] {0f, 0.5f, 0.5f};
		}else if(texture == 2) {
			return new float[] {0.5f, 0.5f, 0.5f};
		}else {
			return new float[] {0f, 0f, 0.5f};
		}
		
	}
}