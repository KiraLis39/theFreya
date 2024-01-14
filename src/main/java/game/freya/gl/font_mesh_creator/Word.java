package game.freya.gl.font_mesh_creator;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * During the loading of a text this represents one word in the text.
 *
 * @author Karl
 */
@Slf4j
public class Word {
    @Getter
    private final List<Character> characters = new ArrayList<>();

    private final double fontSize;

    @Getter
    private double width = 0;

    /**
     * Create a new empty word.
     *
     * @param fontSize - the font size of the text which this word is in.
     */
    protected Word(double fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Adds a character to the end of the current word and increases the screen-space width of the word.
     *
     * @param character - the character to be added.
     */
    protected void addCharacter(Character character) {
        if (character == null) {
            log.error("Character can not be NULL: {}", character);
            return;
        }

        characters.add(character);
        width += character.getxAdvance() * fontSize;
    }
}
