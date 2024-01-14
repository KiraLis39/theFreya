package game.freya.gl.font_mesh_creator;

import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides functionality for getting the values from a font file.
 *
 * @author Karl
 */
@Slf4j
public class MetaFile {
    private static final int PAD_TOP = 0;

    private static final int PAD_LEFT = 1;

    private static final int PAD_BOTTOM = 2;

    private static final int PAD_RIGHT = 3;

    private static final int DESIRED_PADDING = 3;

    private static final String SPLITTER = " ";

    private static final String NUMBER_SEPARATOR = ",";

    private final Map<Integer, Character> metaData = new HashMap<>();

    private final Map<String, String> values = new HashMap<>();

    private double verticalPerPixelSize;

    private double horizontalPerPixelSize;

    @Getter
    private double spaceWidth;

    private int[] padding;

    private int paddingWidth;

    private int paddingHeight;

    private BufferedReader reader;

    /**
     * Opens a font file in preparation for reading.
     *
     * @param file - the font file.
     */
    protected MetaFile(File file, double aspect) {
        openFile(file);

        loadPaddingData();
        loadLineSizes(aspect);

        int imageWidth = getValueOfVariable("scaleW");
        loadCharacterData(imageWidth);

        close();
    }

    protected Character getCharacter(int ascii) {
        return metaData.get(ascii);
    }

    /**
     * Read in the next line and store the variable values.
     *
     * @return {@code true} if the end of the file hasn't been reached.
     */
    private boolean processNextLine() {
        values.clear();

        String line;
        try {
            line = reader.readLine();
        } catch (IOException e1) {
            log.warn(ExceptionUtils.getFullExceptionMessage(e1));
            return false;
        }
        if (line == null) {
            return false;
        }
        for (String part : line.split(SPLITTER)) {
            String[] valuePairs = part.split("=");
            if (valuePairs.length == 2) {
                values.put(valuePairs[0], valuePairs[1]);
            }
        }
        return true;
    }

    /**
     * Gets the {@code int} value of the variable with a certain name on the
     * current line.
     *
     * @param variable - the name of the variable.
     * @return The value of the variable.
     */
    private int getValueOfVariable(String variable) {
        return Integer.parseInt(values.get(variable));
    }

    /**
     * Gets the array of ints associated with a variable on the current line.
     *
     * @param variable - the name of the variable.
     * @return The int array of values associated with the variable.
     */
    private int[] getValuesOfVariable(String variable) {
        String found = values.get(variable);
        if (found == null) {
            log.error("Variable '{}' as found-string is null!", variable);
            return new int[0];
        }

        String[] numbers = values.get(variable).split(NUMBER_SEPARATOR);
        int[] actualValues = new int[numbers.length];
        for (int i = 0; i < actualValues.length; i++) {
            actualValues[i] = Integer.parseInt(numbers[i]);
        }
        return actualValues;
    }

    /**
     * Closes the font file after finishing reading.
     */
    private void close() {
        try {
            reader.close();
        } catch (Exception e) {
            log.warn("Reader closing exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    /**
     * Opens the font file, ready for reading.
     *
     * @param file - the font file.
     */
    private void openFile(File file) {
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (Exception e) {
            log.error("File read exception: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    /**
     * Loads the data about how much padding is used around each character in
     * the texture atlas.
     */
    private void loadPaddingData() {
        processNextLine();
        this.padding = getValuesOfVariable("padding");
        if (this.padding.length > 0) {
            this.paddingWidth = padding[PAD_LEFT] + padding[PAD_RIGHT];
            this.paddingHeight = padding[PAD_TOP] + padding[PAD_BOTTOM];
        }
    }

    /**
     * Loads information about the line height for this font in pixels, and uses
     * this as a way to find the conversion rate between pixels in the texture
     * atlas and screen-space.
     */
    private void loadLineSizes(double aspect) {
        processNextLine();
        int lineHeightPixels = getValueOfVariable("lineHeight") - paddingHeight;
        verticalPerPixelSize = TextMeshCreator.LINE_HEIGHT / lineHeightPixels;
        horizontalPerPixelSize = verticalPerPixelSize / aspect;
    }

    /**
     * Loads in data about each character and stores the data in the
     * {@link Character} class.
     *
     * @param imageWidth - the width of the texture atlas in pixels.
     */
    private void loadCharacterData(int imageWidth) {
        processNextLine();
        processNextLine();
        while (processNextLine()) {
            Character c = loadCharacter(imageWidth);
            if (c != null) {
                metaData.put(c.getId(), c);
            }
        }
    }

    /**
     * Loads all the data about one character in the texture atlas and converts
     * it all from 'pixels' to 'screen-space' before storing. The effects of
     * padding are also removed from the data.
     *
     * @param imageSize - the size of the texture atlas in pixels.
     * @return The data about the character.
     */
    private Character loadCharacter(int imageSize) {
        int id = getValueOfVariable("id");
        if (id == TextMeshCreator.SPACE_ASCII) {
            this.spaceWidth = (getValueOfVariable("xadvance") - paddingWidth) * horizontalPerPixelSize;
            return null;
        }

        double xTex = ((double) getValueOfVariable("x") + (padding[PAD_LEFT] - DESIRED_PADDING)) / imageSize;
        double yTex = ((double) getValueOfVariable("y") + (padding[PAD_TOP] - DESIRED_PADDING)) / imageSize;
        int width = getValueOfVariable("width") - (paddingWidth - (2 * DESIRED_PADDING));
        int height = getValueOfVariable("height") - ((paddingHeight) - (2 * DESIRED_PADDING));
        double quadWidth = width * horizontalPerPixelSize;
        double quadHeight = height * verticalPerPixelSize;
        double xTexSize = (double) width / imageSize;
        double yTexSize = (double) height / imageSize;
        double xOff = (getValueOfVariable("xoffset") + padding[PAD_LEFT] - DESIRED_PADDING) * horizontalPerPixelSize;
        double yOff = (getValueOfVariable("yoffset") + (padding[PAD_TOP] - DESIRED_PADDING)) * verticalPerPixelSize;
        double xAdvance = (getValueOfVariable("xadvance") - paddingWidth) * horizontalPerPixelSize;

        return new Character(id, xTex, yTex, xTexSize, yTexSize, xOff, yOff, quadWidth, quadHeight, xAdvance);
    }
}
