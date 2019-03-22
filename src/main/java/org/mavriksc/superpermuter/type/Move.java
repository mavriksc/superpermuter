package org.mavriksc.superpermuter.type;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Move {
    private RowCol rowCol;
    private int overlap;
}
