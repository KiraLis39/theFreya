package game.freya.gl.font_mesh_creator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TextMeshCreator {
    protected static final double LINE_HEIGHT = 0.03f;

    protected static final int SPACE_ASCII = 32;

    private final MetaFile metaData;

    protected TextMeshCreator(File metaFile, double aspect) {
        metaData = new MetaFile(metaFile, aspect);
    }

    private static void addVertices(List<Float> vertices, double x, double y, double maxX, double maxY) {
        vertices.add((float) x);
        vertices.add((float) y);
        vertices.add((float) x);
        vertices.add((float) maxY);
        vertices.add((float) maxX);
        vertices.add((float) maxY);
        vertices.add((float) maxX);
        vertices.add((float) maxY);
        vertices.add((float) maxX);
        vertices.add((float) y);
        vertices.add((float) x);
        vertices.add((float) y);
    }

    private static void addTexCoords(List<Float> texCoords, double x, double y, double maxX, double maxY) {
        texCoords.add((float) x);
        texCoords.add((float) y);
        texCoords.add((float) x);
        texCoords.add((float) maxY);
        texCoords.add((float) maxX);
        texCoords.add((float) maxY);
        texCoords.add((float) maxX);
        texCoords.add((float) maxY);
        texCoords.add((float) maxX);
        texCoords.add((float) y);
        texCoords.add((float) x);
        texCoords.add((float) y);
    }

    private static float[] listToArray(List<Float> listOfFloats) {
        float[] array = new float[listOfFloats.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = listOfFloats.get(i);
        }
        return array;
    }

    protected TextMeshData createTextMesh(GUIText text) {
        List<Line> lines = createStructure(text);
        return createQuadVertices(text, lines);
    }

    private List<Line> createStructure(GUIText text) {
        List<Line> lines = new ArrayList<>();
        Line currentLine = new Line(metaData.getSpaceWidth(), text.getSize(), text.getLineLength());
        Word currentWord = new Word(text.getSize());

        char[] chars = text.getText().toCharArray();
        for (char c : chars) {
            if (c == SPACE_ASCII) {
                boolean added = currentLine.attemptToAddWord(currentWord);
                if (!added) {
                    lines.add(currentLine);
                    currentLine = new Line(metaData.getSpaceWidth(), text.getSize(), text.getLineLength());
                    currentLine.attemptToAddWord(currentWord);
                }
                currentWord = new Word(text.getSize());
                continue;
            }
            currentWord.addCharacter(metaData.getCharacter(c));
        }
        completeStructure(lines, currentLine, currentWord, text);
        return lines;
    }

    private void completeStructure(List<Line> lines, Line currentLine, Word currentWord, GUIText text) {
        boolean added = currentLine.attemptToAddWord(currentWord);
        if (!added) {
            lines.add(currentLine);
            currentLine = new Line(metaData.getSpaceWidth(), text.getSize(), text.getLineLength());
            currentLine.attemptToAddWord(currentWord);
        }
        lines.add(currentLine);
    }

    private TextMeshData createQuadVertices(GUIText text, List<Line> lines) {
        text.setNumberOfLines(lines.size());

        double curserX = 0f;
        double curserY = 0f;
        List<Float> vertices = new ArrayList<>();
        List<Float> textureCoords = new ArrayList<>();
        for (Line line : lines) {
            if (text.isCentered()) {
                curserX = (line.getMaxLength() - line.getCurrentLength()) / 2;
            }

            for (Word word : line.getWords()) {
                for (Character letter : word.getCharacters()) {
                    addVerticesForCharacter(curserX, curserY, letter, text.getSize(), vertices);
                    addTexCoords(textureCoords, letter.getxTextureCoord(), letter.getyTextureCoord(),
                            letter.getXMaxTextureCoord(), letter.getYMaxTextureCoord());
                    curserX += letter.getxAdvance() * text.getSize();
                }
                curserX += metaData.getSpaceWidth() * text.getSize();
            }

            curserX = 0;
            curserY += LINE_HEIGHT * text.getSize();
        }

        return new TextMeshData(listToArray(vertices), listToArray(textureCoords));
    }

    private void addVerticesForCharacter(
            double curserX,
            double curserY,
            Character character,
            double fontSize,
            List<Float> vertices
    ) {
        double x = curserX + (character.getxOffset() * fontSize);
        double y = curserY + (character.getyOffset() * fontSize);
        double maxX = x + (character.getSizeX() * fontSize);
        double maxY = y + (character.getSizeY() * fontSize);
        double properX = (2 * x) - 1;
        double properY = (-2 * y) + 1;
        double properMaxX = (2 * maxX) - 1;
        double properMaxY = (-2 * maxY) + 1;

        addVertices(vertices, properX, properY, properMaxX, properMaxY);
    }
}
