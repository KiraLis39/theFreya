package game.freya.gl.textures;

import lombok.Getter;

public class ModelTexture {
    @Getter
    private final int id;

    @Getter
    private int normalMap;

    @Getter
    private float shineDamper = 1;

    @Getter
    private float reflectivity = 0;

    @Getter
    private int numberOfRows = 1;

    @Getter
    private boolean hasTransparency = false;

    @Getter
    private boolean useFakeLighting = false;

    public ModelTexture(int texture) {
        this.id = texture;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public void setNormalMap(int normalMap) {
        this.normalMap = normalMap;
    }

    public void setHasTransparency(boolean hasTransparency) {
        this.hasTransparency = hasTransparency;
    }

    public void setUseFakeLighting(boolean useFakeLighting) {
        this.useFakeLighting = useFakeLighting;
    }

    public void setShineDamper(float shineDamper) {
        this.shineDamper = shineDamper;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
    }
}
