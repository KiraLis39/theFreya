package game.freya.gl.font_mesh_creator;

public class Character {
    private final int id;

    private final double xTextureCoord;

    private final double yTextureCoord;

    private final double xMaxTextureCoord;

    private final double yMaxTextureCoord;

    private final double xOffset;

    private final double yOffset;

    private final double sizeX;

    private final double sizeY;

    private final double xAdvance;

    protected Character(int id, double xTextureCoord, double yTextureCoord, double xTexSize, double yTexSize,
                        double xOffset, double yOffset, double sizeX, double sizeY, double xAdvance
    ) {
        this.id = id;
        this.xTextureCoord = xTextureCoord;
        this.yTextureCoord = yTextureCoord;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.xMaxTextureCoord = xTexSize + xTextureCoord;
        this.yMaxTextureCoord = yTexSize + yTextureCoord;
        this.xAdvance = xAdvance;
    }

    protected int getId() {
        return id;
    }

    protected double getxTextureCoord() {
        return xTextureCoord;
    }

    protected double getyTextureCoord() {
        return yTextureCoord;
    }

    protected double getXMaxTextureCoord() {
        return xMaxTextureCoord;
    }

    protected double getYMaxTextureCoord() {
        return yMaxTextureCoord;
    }

    protected double getxOffset() {
        return xOffset;
    }

    protected double getyOffset() {
        return yOffset;
    }

    protected double getSizeX() {
        return sizeX;
    }

    protected double getSizeY() {
        return sizeY;
    }

    protected double getxAdvance() {
        return xAdvance;
    }
}
