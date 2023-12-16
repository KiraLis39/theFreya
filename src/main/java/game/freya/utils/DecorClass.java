package game.freya.utils;


import org.lwjgl.opengl.GLCapabilities;

@SuppressWarnings("unused")
public class DecorClass {
    private final float[] materialSpecularColor = {1.0f, 1.0f, 1.0f, 1.0f};

    GLCapabilities Capabilities;

    float[] fogcolor = {0.40f, 0.40f, 0.40f, 1.00f};

    float[] light0_Color = {0.25f, 0.25f, 0.25f, 1.00f};

    float[] light0_Intensity = {1.00f, 0.00f, 0.00f, 1.00f};

    float[] light0_Diffuse = {1.00f, 0.00f, 0.00f, 1.00f};

    float[] light0_Specular = {1.00f, 1.00f, 1.00f, 1.00f};

    float[] light0_Position = {-1.00f, 0.00f, 1.00f, 0.00f};

    float[] light0_Direction = {0.00f, 0.00f, -1.00f, 0.00f};

    float[] light0_Attenuation = {0.00f, 0.00f, 0.00f};

    Boolean lightningOn = false;

    Boolean alphaTest = true;

    Boolean blendOn = true;

    Boolean Vsync = false;

    Boolean smooth = true;

    Boolean depth = true;

    Boolean colorMaterial = true;

    Boolean doubleBuffered = true;

    Boolean hardwareAccelerated = true;

    Boolean fog = false;

    Boolean hints = true;

//    public GLAutoDrawable tune(GLAutoDrawable drawable) {
//        GL2 gl2 = drawable.getGL().getGL2();
//        gl2.glClearColor(0.0f, 0.0f, 0.0f, 1.00f);
//
//        Capabilities.setDoubleBuffered(doubleBuffered);
//        Capabilities.setHardwareAccelerated(hardwareAccelerated);
//
//        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
//        gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
//
//        if (Vsync) {
//            gl2.setSwapInterval(1);
//        } else {
//            gl2.setSwapInterval(0);
//        }
//
////		gl2.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
////		gl2.glEnable(GL2.GL_POLYGON_OFFSET_LINE);
////		gl2.glEnable(GL2.GL_POLYGON_OFFSET_POINT);
////		gl2.glPolygonOffset(0.0f, 2.0f);
//
//        if (fog) {
//            gl2.glEnable(GL2.GL_FOG);
//            gl2.glFogfv(GL2.GL_FOG_COLOR, fogcolor, 0); // устанавливаем цвет тумана
//            gl2.glFogf(GL2.GL_FOG_DENSITY, 0.50f); //плотность тумана
//        } else {
//            gl2.glDisable(GL2.GL_FOG);
//        }
//
//        drawable = lightSwitcher(drawable);
//
//        if (colorMaterial) {
//            gl2.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
////			gl2.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
////			gl2.glColorMaterial(GL2.GL_BACK, 	GL2.GL_AMBIENT_AND_DIFFUSE);
//
//
//            gl2.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE); // разрешить	режим освещенности для двух граней
//
////			gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecularColor, 0);
////			gl2.glMaterialf(GL2.GL_BACK, GL2.GL_SHININESS, 128);
//
//            gl2.glEnable(GL2.GL_COLOR_MATERIAL);
//        } else {
//            gl2.glDisable(GL2.GL_COLOR_MATERIAL);
//        }
//
//
//        if (smooth) {// интерполяция
//            gl2.glDisable(GL2.GL_FLAT);
//
//            gl2.glEnable(GL2.GL_LINE_SMOOTH);
//            gl2.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
//
//            gl2.glEnable(GL2.GL_POLYGON_SMOOTH);
//            gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
//
//            gl2.glEnable(GL2.GL_SMOOTH);
//            gl2.glShadeModel(GL2.GL_SMOOTH);
//        } else {
//            gl2.glDisable(GL2.GL_SMOOTH);
//            gl2.glDisable(GL2.GL_LINE_SMOOTH);
//            gl2.glDisable(GL2.GL_POLYGON_SMOOTH);
//
//            gl2.glEnable(GL2.GL_FLAT);
//            gl2.glShadeModel(GL2.GL_FLAT);
//        }
//
//        if (depth) {
//            gl2.glClearDepth(1.00);
//
////			gl2.glDepthFunc(GL2.GL_LESS);
//            gl2.glDepthFunc(GL2.GL_LEQUAL);
////			gl2.glDepthFunc(GL2.GL_EQUAL);
////			gl2.glDepthFunc(GL2.GL_NOTEQUAL);
////			gl2.glDepthFunc(GL2.GL_GEQUAL);
////			gl2.glDepthFunc(GL2.GL_GREATER);
////			gl2.glDepthFunc(GL2.GL_ALWAYS);
////			gl2.glDepthFunc(GL2.GL_NEVER);
////			gl2.glDepthRange(0, 1);
//            gl2.glEnable(GL2.GL_DEPTH_TEST); // Буфер глубины или z-буфер используется для удаления невидимых линий и поверхностей.
//        } else {
//            gl2.glDisable(GL2.GL_DEPTH_TEST);
//        }
//
//
//        if (alphaTest) {
////			gl2.glAlphaFunc(GL2.GL_ALWAYS, 0.00f);
////			gl2.glAlphaFunc(GL2.GL_LESS, 0.50f);
////			gl2.glAlphaFunc(GL2.GL_EQUAL, 1.00f);
////			gl2.glAlphaFunc(GL2.GL_LEQUAL, 0.75f);
////			gl2.glAlphaFunc(GL2.GL_GREATER, 0.75f);
////			gl2.glAlphaFunc(GL2.GL_NOTEQUAL, 0.00f);
//            gl2.glAlphaFunc(GL2.GL_GEQUAL, 0.25f);
////			gl2.glAlphaFunc(GL2.GL_NEVER, 0.00f);
//
//            gl2.glEnable(GL2.GL_ALPHA_TEST);
//        } else {
//            gl2.glDisable(GL2.GL_ALPHA_TEST);
//        }
//
//
//        if (blendOn) {
//            gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA); // с сортировкой полигонов от дальнего к ближнему
////			gl2.glBlendFunc(GL_SRC_ALPHA_SATURATE, GL_ONE) // с сортировкой полигонов от ближнего к дальнему
//
//            gl2.glDepthMask(false);
//            gl2.glEnable(GL2.GL_BLEND);
//        } else {
//            gl2.glDepthMask(true);
//            gl2.glDisable(GL2.GL_BLEND);
//        }
//
//
//        if (MainCavas.filter.equals(FilterMode.LINEAR)) {
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
//        } else if (MainCavas.filter.equals(FilterMode.NEAREST)) {
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
//        } else if (MainCavas.filter.equals(FilterMode.MIPMAP)) {
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_NEAREST);
//
//            gl2.glHint(GL2.GL_SAMPLES, 4);
//            gl2.glEnable(GL2.GL_MULTISAMPLE);
//        } else if (MainCavas.filter.equals(FilterMode.OPTIMAL)) {
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
//            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_CUBIC_EXT);
//        }
//
//        if (hints) {
//            //		gl2.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_FASTEST);
//            //		gl2.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
//            gl2.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_DONT_CARE);
//
//
////			gl2.glHint(GL2.GL_FOG_HINT, GL2.GL_FASTEST);
////			gl2.glHint(GL2.GL_FOG_HINT, GL2.GL_NICEST);
//            gl2.glHint(GL2.GL_FOG_HINT, GL2.GL_DONT_CARE);
//
////			gl2.glHint(GL2.GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL2.GL_FASTEST);
////			gl2.glHint(GL2.GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL2.GL_NICEST);
//            gl2.glHint(GL2.GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL2.GL_DONT_CARE);
//
////			gl2.glHint(GL2.GL_GENERATE_MIPMAP_HINT, GL2.GL_FASTEST);
////			gl2.glHint(GL2.GL_GENERATE_MIPMAP_HINT, GL2.GL_NICEST);
//            gl2.glHint(GL2.GL_GENERATE_MIPMAP_HINT, GL2.GL_DONT_CARE);
//
////			gl2.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_FASTEST);
////			gl2.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
//            gl2.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_DONT_CARE);
//
////			gl2.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_FASTEST);
////			gl2.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
//            gl2.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_DONT_CARE);
//
////			gl2.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_FASTEST);
////			gl2.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
//            gl2.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_DONT_CARE);
//
////			gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_FASTEST);
////			gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
//            gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_DONT_CARE);
//
////			gl2.glHint(GL2.GL_TEXTURE_COMPRESSION_HINT, GL2.GL_FASTEST);
////			gl2.glHint(GL2.GL_TEXTURE_COMPRESSION_HINT, GL2.GL_NICEST);
//            gl2.glHint(GL2.GL_TEXTURE_COMPRESSION_HINT, GL2.GL_DONT_CARE);
//        }
//
//        return drawable;
//    }

//    private GLAutoDrawable lightSwitcher(GLAutoDrawable drawable) {
//        GL2 gl2 = drawable.getGL().getGL2();
//
//        if (lightningOn) {
////			gl2.glEnable(GL2.GL_NORMALIZE);
//            gl2.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, light0_Color, 0);
////			gl2.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_TRUE);
//
//            gl2.glEnable(GL2.GL_LIGHTING);
//
//            gl2.glEnable(GL2.GL_LIGHT0);
//
//            gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light0_Position, 0);
//            gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light0_Intensity, 0);
//            gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light0_Diffuse, 0);
//            gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, light0_Specular, 0);
//            gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, light0_Direction, 0);
//            gl2.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_EXPONENT, 64); //range 0-128
//            gl2.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, 90); //range 0-90 and the special value 180
//            //			gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_CONSTANT_ATTENUATION, light0_Attenuation, 0);
//            gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, light0_Attenuation, 0);
//            //			gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_QUADRATIC_ATTENUATION, light0_Attenuation, 0);
//
//
//            //			gl2.glEnable(GL2.GL_LIGHT1);
//
//            //			gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, light1_Intensity, 0);
//            //			gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, light1_Position, 0);
//            //			gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, light1_Color, 0);
//            //			gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, light1_Diffuse, 0);
//            //			gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, light1_Position, 0);
//            //			gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, light1_Specular, 0);
//        } else {
//            gl2.glDisable(GL2.GL_LIGHT1);
//            gl2.glDisable(GL2.GL_LIGHT0);
//            gl2.glDisable(GL2.GL_LIGHTING);
//        }
//
//        return drawable;
//    }
}
