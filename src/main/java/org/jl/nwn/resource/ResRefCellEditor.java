package org.jl.nwn.resource;

import javax.swing.JFormattedTextField;

import org.jl.nwn.Version;
import org.jl.swing.table.FormattedCellEditor;
import org.w3c.dom.Element;

public class ResRefCellEditor extends FormattedCellEditor{
    public static final String ALLOWNULL = "allowNull";

    public ResRefCellEditor( boolean allow2DANull ){
        super(new JFormattedTextField(
                ResRefUtil.instance(Version.getDefaultVersion())
                .getStringFormatter(allow2DANull)));
    }
    public ResRefCellEditor(){
        this(true);
    }

    public ResRefCellEditor( Element e ){
        this( e.getAttribute(ALLOWNULL).length() > 0 ?
            Boolean.parseBoolean(e.getAttribute(ALLOWNULL)) : true );
    }
}
