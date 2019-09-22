package org.jl.swing.table;

import javax.swing.CellEditor;
import javax.swing.text.JTextComponent;

/** A {@link CellEditor} that uses a {@link JTextComponent} as editor component. */
public interface TextCellEditor extends CellEditor{
    public JTextComponent getTextComponent();
}
