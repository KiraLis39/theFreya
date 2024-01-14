package game.freya.gl.render_engine;

import game.freya.gl.entities.Camera;
import game.freya.gl.entities.Entity;
import game.freya.gl.entities.Light;
import game.freya.gl.models.TexturedModel;
import game.freya.gl.shaders.StaticShader;
import game.freya.gl.shaders.TerrainShader;
import game.freya.gl.terrains.Terrain;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterRenderer {

    public static final float RED = 0.1f;

    public static final float GREEN = 0.4f;

    public static final float BLUE = 0.2f;

    private static final float FOV = 70;

    private static final float NEAR_PLANE = 0.1f;

    private static final float FAR_PLANE = 1000;

    private final StaticShader shader = new StaticShader();

    private final EntityRenderer renderer;

    private final TerrainRenderer terrainRenderer;

    private final TerrainShader terrainShader = new TerrainShader();

    private final NormalMappingRenderer normalMapRenderer;

    private final SkyboxRenderer skyboxRenderer;

    private final Map<TexturedModel, List<Entity>> entities = new HashMap<>();

    private final Map<TexturedModel, List<Entity>> normalMapEntities = new HashMap<>();

    private final List<Terrain> terrains = new ArrayList<>();

    private Matrix4f projectionMatrix;

    public MasterRenderer(Loader loader) {
        enableCulling();
        createProjectionMatrix();
        renderer = new EntityRenderer(shader, projectionMatrix);
        terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
        skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
        normalMapRenderer = new NormalMappingRenderer(projectionMatrix);
    }

    public static void enableCulling() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
    }

    public static void disableCulling() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public void renderScene(
            List<Entity> entities,
            List<Entity> normalEntities,
            List<Terrain> terrains,
            List<Light> lights,
            Camera camera,
            Vector4f clipPlane
    ) {
        for (Terrain terrain : terrains) {
            processTerrain(terrain);
        }
        for (Entity entity : entities) {
            processEntity(entity);
        }
        for (Entity entity : normalEntities) {
            processNormalMapEntity(entity);
        }
        render(lights, camera, clipPlane);
    }

    public void render(List<Light> lights, Camera camera, Vector4f clipPlane) {
        prepare();
        shader.start();
        shader.loadClipPlane(clipPlane);
        shader.loadSkyColor(RED, GREEN, BLUE);
        shader.loadLights(lights);
        shader.loadViewMatrix(camera);
        renderer.render(entities);
        shader.stop();
        normalMapRenderer.render(normalMapEntities, clipPlane, lights, camera);
        terrainShader.start();
        terrainShader.loadClipPlane(clipPlane);
        terrainShader.loadSkyColor(RED, GREEN, BLUE);
        terrainShader.loadLights(lights);
        terrainShader.loadViewMatrix(camera);
        terrainRenderer.render(terrains);
        terrainShader.stop();
        skyboxRenderer.render(camera, RED, GREEN, BLUE);
        terrains.clear();
        entities.clear();
        normalMapEntities.clear();
    }

    public void processTerrain(Terrain terrain) {
        terrains.add(terrain);
    }

    public void processEntity(Entity entity) {
        TexturedModel entityModel = entity.getModel();
        List<Entity> batch = entities.get(entityModel);
        if (batch != null) {
            batch.add(entity);
        } else {
            List<Entity> newBatch = new ArrayList<Entity>();
            newBatch.add(entity);
            entities.put(entityModel, newBatch);
        }
    }

    public void processNormalMapEntity(Entity entity) {
        TexturedModel entityModel = entity.getModel();
        List<Entity> batch = normalMapEntities.get(entityModel);
        if (batch != null) {
            batch.add(entity);
        } else {
            List<Entity> newBatch = new ArrayList<Entity>();
            newBatch.add(entity);
            normalMapEntities.put(entityModel, newBatch);
        }
    }

    public void close() {
        shader.close();
        terrainShader.close();
        normalMapRenderer.close();
    }

    public void prepare() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(RED, GREEN, BLUE, 1);
    }

    private void createProjectionMatrix() {
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
        float x_scale = y_scale / aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;

        projectionMatrix = new Matrix4f();
        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        projectionMatrix.m33 = 0;
    }

}
