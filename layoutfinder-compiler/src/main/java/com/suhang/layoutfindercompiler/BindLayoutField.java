package com.suhang.layoutfindercompiler;

import com.suhang.layoutfinderannotation.BindLayout;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class BindLayoutField {
    private VariableElement mFieldElement;

    public BindLayoutField(Element element) throws IllegalArgumentException {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException(
                String.format("Only fields can be annotated with @%s", BindLayout.class.getSimpleName()));
        }

        mFieldElement = (VariableElement) element;
        mFieldElement.getSimpleName();
    }

    public Name getFieldName() {
        return mFieldElement.getSimpleName();
    }

    public TypeMirror getFieldType() {
        return mFieldElement.asType();
    }
}