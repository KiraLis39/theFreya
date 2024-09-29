package game.freya.enums.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CrosshairType {
    SIMPLE_CROSS("+"),
    V_TYPE("V"),
    O_TYPE("O"),
    T_TYPE("T");

    private final String view;
}
