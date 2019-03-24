package org.mavriksc.superpermuter.type;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Move {
    private RowCol rowCol;
    private int overlap;
    // the number of columns the overlap are in that we have not visited yet.
    //one of my theories is diversity leads to longer optimal chains.
    private int freshComboFactor;
}
