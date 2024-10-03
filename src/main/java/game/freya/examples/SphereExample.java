package game.freya.examples;

import com.jme3.asset.AssetEventListener;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import com.jme3.asset.FilterKey;
import com.jme3.asset.ModelKey;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioKey;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Caps;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.shader.ShaderGenerator;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;

public class SphereExample {

    public SphereExample() {
        AssetManager assetManager = new AssetManager() {
            @Override
            public void addClassLoader(ClassLoader loader) {

            }

            @Override
            public void removeClassLoader(ClassLoader loader) {

            }

            @Override
            public List<ClassLoader> getClassLoaders() {
                return null;
            }

            @Override
            public void registerLoader(Class<? extends AssetLoader> loaderClass, String... extensions) {

            }

            @Override
            public void unregisterLoader(Class<? extends AssetLoader> loaderClass) {

            }

            @Override
            public void registerLocator(String rootPath, Class<? extends AssetLocator> locatorClass) {

            }

            @Override
            public void unregisterLocator(String rootPath, Class<? extends AssetLocator> locatorClass) {

            }

            @Override
            public void addAssetEventListener(AssetEventListener listener) {

            }

            @Override
            public void removeAssetEventListener(AssetEventListener listener) {

            }

            @Override
            public void clearAssetEventListeners() {

            }

            @Override
            public AssetInfo locateAsset(AssetKey<?> key) {
                return null;
            }

            @Override
            public <T> T loadAssetFromStream(AssetKey<T> key, InputStream inputStream) {
                return null;
            }

            @Override
            public <T> T loadAsset(AssetKey<T> key) {
                return null;
            }

            @Override
            public Object loadAsset(String name) {
                return null;
            }

            @Override
            public Texture loadTexture(TextureKey key) {
                return null;
            }

            @Override
            public Texture loadTexture(String name) {
                return null;
            }

            @Override
            public AudioData loadAudio(AudioKey key) {
                return null;
            }

            @Override
            public AudioData loadAudio(String name) {
                return null;
            }

            @Override
            public Spatial loadModel(ModelKey key) {
                return null;
            }

            @Override
            public Spatial loadModel(String name) {
                return null;
            }

            @Override
            public Material loadMaterial(String name) {
                return null;
            }

            @Override
            public BitmapFont loadFont(String name) {
                return null;
            }

            @Override
            public FilterPostProcessor loadFilter(FilterKey key) {
                return null;
            }

            @Override
            public FilterPostProcessor loadFilter(String name) {
                return null;
            }

            @Override
            public void setShaderGenerator(ShaderGenerator generator) {

            }

            @Override
            public ShaderGenerator getShaderGenerator(EnumSet<Caps> caps) {
                return null;
            }

            @Override
            public <T> T getFromCache(AssetKey<T> key) {
                return null;
            }

            @Override
            public <T> void addToCache(AssetKey<T> key, T asset) {

            }

            @Override
            public <T> boolean deleteFromCache(AssetKey<T> key) {
                return false;
            }

            @Override
            public void clearCache() {

            }
        };

        /* A bumpy rock with a shiny light effect. To make bumpy objects you must create a NormalMap. */
        Sphere sphereMesh = new Sphere(32, 32, 2f);
        Geometry sphereGeo = new Geometry("Shiny rock", sphereMesh);
        sphereMesh.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
        TangentBinormalGenerator.generate(sphereMesh);           // for lighting effect
        Material sphereMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        sphereMat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
        sphereMat.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
        sphereMat.setBoolean("UseMaterialColors", true);
        sphereMat.setColor("Diffuse", ColorRGBA.White);
        sphereMat.setColor("Specular", ColorRGBA.White);
        sphereMat.setFloat("Shininess", 64f);  // [0,128]
        sphereGeo.setMaterial(sphereMat);
        //sphereGeo.setMaterial((Material) assetManager.loadMaterial("Materials/MyCustomMaterial.j3m"));
        sphereGeo.setLocalTranslation(0, 2, -2); // Move it a bit
        sphereGeo.rotate(1.6f, 0, 0);          // Rotate it a bit
    }
}
