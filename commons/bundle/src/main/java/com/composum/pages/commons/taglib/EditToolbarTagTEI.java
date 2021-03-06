package com.composum.pages.commons.taglib;

import com.composum.sling.cpnl.AbstractTagTEI;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.VariableInfo;
import java.util.List;

public class EditToolbarTagTEI extends AbstractTagTEI {

    protected void collectVariables(TagData data, List<VariableInfo> variables) {
        variables.add(new VariableInfo(EditToolbarTag.TOOLBAR_VAR, EditToolbarTag.class.getName(), true, VariableInfo.NESTED));
        variables.add(new VariableInfo(EditToolbarTag.TOOLBAR_CSS_VAR, "java.lang.String", true, VariableInfo.NESTED));
    }
}
