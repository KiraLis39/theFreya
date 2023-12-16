package game.freya.utils;

public class FpsMeter { // implements GLEventListener {
//    private TextRenderer textRenderer;
//
//    private long t0, t1;
//
//    private int fps, frames;
//
//    @Override
//    public void init(GLAutoDrawable drawable) {
//        textRenderer = new TextRenderer(font);
//    }
//
//    @Override
//    public void dispose(GLAutoDrawable arg0) {
//    }
//
//    @Override
//    public void display(GLAutoDrawable drawable) {
//        frames++;
//        t1 = System.currentTimeMillis();
//        GL2 gl2 = drawable.getGL().getGL2();
//        gl2.glEnable(GL.GL_BLEND);
//        gl2.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
//
//        if (t1 - t0 >= 1000) {
//            fps = frames;
//            t0 = t1;
//            frames = 0;
//        }
//
//        textRenderer.beginRendering(900, 50);
//
//        gl2.glClearColor(0f, 0f, 0f, 1.0f);
//        gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
//        gl2.glLoadIdentity();
//
//        gl2.glBegin(GL2ES3.GL_QUADS);
//
//        gl2.glTexCoord2i(0, 0);
//        gl2.glTexCoord2f(1, 0);
//        gl2.glTexCoord2f(1, 1);
//        gl2.glTexCoord2f(0, 1);
//
//        gl2.glEnd();
//
//        textRenderer.setColor(0.8f, 1f, 0.2f, 1.0f);
//        textRenderer.draw(String.valueOf(fps), 5, 20);
//
//        textRenderer.endRendering();
//    }
//
//    @Override
//    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
//    }
}
