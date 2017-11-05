/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package codeanticode.planetarium;

import java.lang.reflect.Method;
import java.nio.IntBuffer;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.opengl.PGL;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;

/**
 * Basic dome renderer.
 * 
 */
public class Dome extends PGraphics3D {
	public final static String RENDERER = "codeanticode.planetarium.Dome";
	public final static String VERSION = "##library.prettyVersion##";

	public final static int NORMAL = 0;
	public final static int GRID = 1;
	
	protected PShader cubeMapQuadShader;
	protected PShape domeQuad;
	protected PGraphics backgroundCanvas;
	protected PGraphics foregroundCanvas;

	protected int resolution;
	protected int offsetX, offsetY;

	protected IntBuffer cubeMapFbo;
	protected IntBuffer cubeMapRbo;
	protected IntBuffer cubeMapTex;

	protected boolean cubeMapInit = false;
	protected int cubeMapSize = 1024;
	protected boolean cubeMapSizeNeedsManualSetting = true;

	protected boolean renderDomeQuad = true;
	protected int currentFace;

	protected boolean requestedRenderDomeChange = false;
	protected boolean requestedRenderDome;
	
	private boolean[] faceDraw = {true, true, true, true, true, false};
	private String[] faceNames = {"X+", "X-", "Y+", "Y-", "Z+", "Z-"};

	protected float domeLeft, domeRight, domeTop, domeBottom;
	protected float domeDX, domeDY, domeDZ;
	protected float domeScale = 1;
	protected float domeAperture = 1f;
	protected float renderGrid = 0f;

	protected Method borderMethod;
	protected Method screenMethod;

	/**
	 * The default constructor.
	 * 
	 */
	public Dome() {
		super();
		welcome();
	}

	public void setParent(PApplet parent) {
		super.setParent(parent);

		Class<?> c = parent.getClass();
		Method method = null;
		try {
			method = c.getMethod("pre", new Class[] {});
		} catch (NoSuchMethodException nsme) {
		}
		if (method != null) {
			parent.registerMethod("pre", parent);
		}

		method = null;
		try {
			method = c.getMethod("post", new Class[] {});
		} catch (NoSuchMethodException nsme) {
		}
		if (method != null) {
			parent.registerMethod("post", parent);
		}

		try {
			borderMethod = parent.getClass().getMethod("border", new Class[] {});
		} catch (Exception e) {
		}

		try {
			screenMethod = parent.getClass().getMethod("screen", new Class[] {});
		} catch (Exception e) {
		}
	}

	public void setSize(int iwidth, int iheight) {
		if (iwidth != iheight) {
			// throw new RuntimeException("Width must be equal to height");
			if (iwidth < iheight) {
				resolution = iwidth;
				offsetX = 0;
				offsetY = iheight / 2 - resolution / 2;
			} else {
				resolution = iheight;
				offsetX = iwidth / 2 - resolution / 2;
				offsetY = 0;
			}
		} else {
			resolution = iwidth;
			offsetX = offsetY = 0;
		}
		domeLeft = 0;
		domeRight = iwidth;
		domeBottom = 0;
		domeTop = iheight;
		super.setSize(iwidth, iheight);
	}

	//////////////////////////////////////////////////////////////

	// All projection methods are disabled

	/*
	 * @Override public void ortho() { showMethodWarning("ortho"); }
	 * 
	 * 
	 * @Override public void ortho(float left, float right, float bottom, float top)
	 * { showMethodWarning("ortho"); }
	 * 
	 * 
	 * @Override public void ortho(float left, float right, float bottom, float top,
	 * float near, float far) { showMethodWarning("ortho"); }
	 * 
	 * 
	 * @Override public void perspective() { showMethodWarning("perspective"); }
	 * 
	 * 
	 * @Override public void perspective(float fov, float aspect, float zNear, float
	 * zFar) { showMethodWarning("perspective"); }
	 * 
	 * 
	 * @Override public void frustum(float left, float right, float bottom, float
	 * top, float znear, float zfar) { showMethodWarning("frustum"); }
	 * 
	 * 
	 * @Override protected void defaultPerspective() { super.perspective(); }
	 */

	//////////////////////////////////////////////////////////////

	// Rendering

	public void beginDraw() {
		super.beginDraw();
		if (requestedRenderDomeChange) {
			// renderDome = requestedRenderDome;
			renderDomeQuad = requestedRenderDome;
			if (renderDomeQuad) {
				background(0xffCCCCCC);
			} else {
				// go back to default camera and perspective
				perspective();
				camera();
				currentFace = PGL.TEXTURE_CUBE_MAP_POSITIVE_Z; // Current face simply defaults to POSITIVE_Z
			}
			requestedRenderDomeChange = false;
		}
		if (renderDomeQuad && 0 < parent.frameCount) {
			if (!cubeMapInit) {
				initDomeQuad();
			}

			beginPGL();

			pgl.activeTexture(PGL.TEXTURE1);
			pgl.enable(PGL.TEXTURE_CUBE_MAP);
			pgl.bindTexture(PGL.TEXTURE_CUBE_MAP, cubeMapTex.get(0));

			// bind fbo
			pgl.bindFramebuffer(PGL.FRAMEBUFFER, cubeMapFbo.get(0));

			pgl.viewport(0, 0, cubeMapSize, cubeMapSize);
			super.perspective(90.0f * DEG_TO_RAD, 1.0f, 1.0f, cameraFar);

			if (getFaceDraw(DomeCamera.POSITIVE_X)) {
				beginFaceDraw(PGL.TEXTURE_CUBE_MAP_POSITIVE_X);
			}
		}
	}

	public void endDraw() {
		// if (renderDome && 0 < parent.frameCount) {
		if (renderDomeQuad && 0 < parent.frameCount) {
			endFaceDraw();

			// Draw the rest of the cubemap faces
			for (int face = PGL.TEXTURE_CUBE_MAP_NEGATIVE_X; face <= PGL.TEXTURE_CUBE_MAP_NEGATIVE_Z; face++) {
				if (getFaceDraw(face - PGL.TEXTURE_CUBE_MAP_POSITIVE_X)) {
					beginFaceDraw(face);
					parent.draw();
					endFaceDraw();
				}
			}

			endPGL();
			// renderDome();
			if (backgroundCanvas != null) {
				super.image(backgroundCanvas, -width/2, -height/2);
			}
			renderDomeQuad();
			if (foregroundCanvas != null) {
				super.image(foregroundCanvas, -width/2, -height/2);
			}
			pgl.disable(PGL.TEXTURE_CUBE_MAP);
			pgl.bindTexture(PGL.TEXTURE_CUBE_MAP, 0);
		}
		super.endDraw();
		clearCanvas(foregroundCanvas);
		clearCanvas(backgroundCanvas);
	}

	private void clearCanvas(PGraphics canvas) {
		if (canvas != null) {
			canvas.beginDraw();
			canvas.background(0, 0, 0, 0);
			canvas.endDraw();
		}
	}

	protected void setOrthoParams(float left, float right, float bottom, float top) {
		domeLeft = left;
		domeRight = right;
		domeBottom = bottom;
		domeTop = top;
	}

	protected void setOrigin(float dx, float dy, float dz) {
		domeDX = dx;
		domeDY = dy;
		domeDZ = dz;
	}

	protected void setScale(float scale) {
		domeScale = scale;
	}

	protected void domeRendering(boolean value) {
		if (renderDomeQuad != value) {
			requestedRenderDomeChange = true;
			requestedRenderDome = value;
		}
	}

	protected void renderGrid(boolean value) {
		renderGrid = value ? 1f : 0f;
		if (cubeMapQuadShader != null) {
			try {
				cubeMapQuadShader.set("renderGrid", renderGrid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected int getCurrentFace() {
		return currentFace;
	}

	protected void imageToForeground(PImage theImage, int x0, int y0, int w, int h) {
		if (foregroundCanvas == null) {
			foregroundCanvas = parent.createGraphics(parent.width, parent.height);
		}
		foregroundCanvas.beginDraw();
		foregroundCanvas.image(theImage, x0, y0, w, h);
		foregroundCanvas.endDraw();
	}

	protected void imageToBackground(PImage theImage, int x0, int y0, int w, int h) {
		if (backgroundCanvas == null) {
			backgroundCanvas = parent.createGraphics(parent.width, parent.height);
		}
		backgroundCanvas.beginDraw();
		backgroundCanvas.image(theImage, x0, y0, w, h);
		backgroundCanvas.endDraw();
	}

	private void initDomeQuad() {
		if (domeQuad == null) {
			domeQuad = createShape();
			domeQuad.beginShape();
			domeQuad.fill(255, 255, 0);
			domeQuad.textureMode(NORMAL);
			domeQuad.noStroke();
			// TODO
			domeQuad.vertex(-width * 0.5f, -height * 0.5f, 0, 0, 0);
			domeQuad.vertex(width * 0.5f, -height * 0.5f, 0, 1, 0);
			domeQuad.vertex(width * 0.5f, height * 0.5f, 0, 1, 1);
			domeQuad.vertex(-width * 0.5f, height * 0.5f, 0, 0, 1);
			domeQuad.endShape();
		}

		if (cubeMapQuadShader == null) {
			cubeMapQuadShader = parent.loadShader("cubeMapQuadFrag.glsl", "cubeMapQuadVert.glsl");
			cubeMapQuadShader.set("cubemap", 1);
			setDomeAperture(domeAperture);
		}

		if (!cubeMapInit) {
			PGL pgl = beginPGL();

			if (cubeMapSizeNeedsManualSetting) {
				cubeMapSize = PApplet.min(nextPowerOfTwo(resolution), maxTextureSize);
				cubeMapSizeNeedsManualSetting = false;
			}
			
			cubeMapTex = IntBuffer.allocate(1);
			pgl.genTextures(1, cubeMapTex);
			pgl.bindTexture(PGL.TEXTURE_CUBE_MAP, cubeMapTex.get(0));
			pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_WRAP_S, PGL.CLAMP_TO_EDGE);
			pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_WRAP_T, PGL.CLAMP_TO_EDGE);
			pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_WRAP_R, PGL.CLAMP_TO_EDGE);
			pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_MIN_FILTER, PGL.LINEAR);
			pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_MAG_FILTER, PGL.LINEAR);
			for (int i = PGL.TEXTURE_CUBE_MAP_POSITIVE_X; i < PGL.TEXTURE_CUBE_MAP_POSITIVE_X + 6; i++) {
				pgl.texImage2D(i, 0, PGL.RGBA8, cubeMapSize, cubeMapSize, 0, PGL.RGBA, PGL.UNSIGNED_BYTE, null);
			}

			// Init fbo, rbo
			cubeMapFbo = IntBuffer.allocate(1);
			cubeMapRbo = IntBuffer.allocate(1);
			pgl.genFramebuffers(1, cubeMapFbo);
			pgl.bindFramebuffer(PGL.FRAMEBUFFER, cubeMapFbo.get(0));
			pgl.framebufferTexture2D(PGL.FRAMEBUFFER, PGL.COLOR_ATTACHMENT0, PGL.TEXTURE_CUBE_MAP_POSITIVE_X,
					cubeMapTex.get(0), 0);

			pgl.genRenderbuffers(1, cubeMapRbo);
			pgl.bindRenderbuffer(PGL.RENDERBUFFER, cubeMapRbo.get(0));
			pgl.renderbufferStorage(PGL.RENDERBUFFER, PGL.DEPTH_COMPONENT24, cubeMapSize, cubeMapSize);

			// Attach depth buffer to FBO
			pgl.framebufferRenderbuffer(PGL.FRAMEBUFFER, PGL.DEPTH_ATTACHMENT, PGL.RENDERBUFFER, cubeMapRbo.get(0));

			endPGL();

			cubeMapInit = true;
		}
	}

	private void beginFaceDraw(int face) {
		currentFace = face;

		resetMatrix();

		if (currentFace == PGL.TEXTURE_CUBE_MAP_POSITIVE_X) {
			camera(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
		} else if (currentFace == PGL.TEXTURE_CUBE_MAP_NEGATIVE_X) {
			camera(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
		} else if (currentFace == PGL.TEXTURE_CUBE_MAP_POSITIVE_Y) {
			camera(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f);
		} else if (currentFace == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Y) {
			camera(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f);
		} else if (currentFace == PGL.TEXTURE_CUBE_MAP_POSITIVE_Z) {
			camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f);
		} else if (currentFace == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Z) {
			camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f);
		}

		scale(-1, 1, -1);
		translate(-width * 0.5f, -height * 0.5f, -cameraZ);

		pgl.framebufferTexture2D(PGL.FRAMEBUFFER, PGL.COLOR_ATTACHMENT0, currentFace, cubeMapTex.get(0), 0);
	}

	private void endFaceDraw() {
		flush(); // Make sure that the geometry in the scene is pushed to the GPU
		noLights(); // Disabling lights to avoid adding many times
		pgl.framebufferTexture2D(PGL.FRAMEBUFFER, PGL.COLOR_ATTACHMENT0, currentFace, 0, 0);
	}

	private void renderDomeQuad() {
		renderBorder();

		// This setting might be better for 2.1.2+:
		camera(0, 0, resolution * 0.5f, 0, 0, 0, 0, 1, 0);
		ortho(-width / 2, width / 2, -height / 2, height / 2);
		// ortho(-width, 0, -height, 0);
		/*
		 * camera(); ortho(domeLeft, domeRight, domeBottom, domeTop);
		 */
		resetMatrix();
		translate(domeDX, domeDY, domeDZ);
		scale(domeScale);
		shader(cubeMapQuadShader);
		shape(domeQuad);
		resetShader();
		renderScreen();
	}

	private void renderBorder() {
		if (borderMethod != null) {
			try {
				borderMethod.invoke(parent, new Object[] {});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void renderScreen() {
		if (screenMethod != null) {
			try {
				screenMethod.invoke(parent, new Object[] {});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static int nextPowerOfTwo(int val) {
		int ret = 1;
		while (ret < val) {
			ret <<= 1;
		}
		return ret;
	}

	// the aperture value is normalized on PI
	protected void setDomeAperture(float theAperture) {
		if (cubeMapQuadShader != null) {
			try {
				cubeMapQuadShader.set("aperture", theAperture);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		domeAperture = theAperture;
	}

	protected boolean getFaceDraw(int theFace) {
		if (theFace >= 0 && theFace <= faceDraw.length) {
			return faceDraw[theFace];
		}
		return false;
	}
	
	protected void setFaceDraw(int theFace, boolean doDraw) {
		if (theFace >= 0 && theFace <= faceDraw.length) {
			faceDraw[theFace] = doDraw;
			System.out.println("Face no. " + theFace + " (" + faceNames[theFace] +") rendering " + doDraw);
		}	
	}
	
	protected void setCubemapSize(int theSize) {
		cubeMapSize = PApplet.min(nextPowerOfTwo(theSize),maxTextureSize);
		
		System.out.println("Setting cubemap size to: " + cubeMapSize);
		cubeMapInit = false;
	}

	private void welcome() {
		System.out.println("##library.name## ##library.prettyVersion## by ##author##");
	}
}
