/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author rombouts
 */
@ViewScoped
@Named
public class FormEditorModel implements Serializable {
    
     static private final String TEXT_INPUT = "TEXT_INPUT";
    static private final String INTEGER_INPUT = "INTEGER_INPUT";
    static private final String FLOAT_INPUT = "FLOAT_INPUT";
    static private final String MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
    static private final String TEXTAREA_INPUT = "TEXTAREA_INPUT";
    static private final String DATE_INPUT = "DATE_INPUT";

    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
    private String parameter;

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
    private String preCode;

    public String getPreCode() {
        return preCode;
    }

    public void setPreCode(String preCode) {
        this.preCode = preCode;
    }
    private String inputFieldType = TEXT_INPUT;

    public String getInputFieldType() {
        return inputFieldType;
    }

    public void setInputFieldType(String inputFieldType) {
        this.inputFieldType = inputFieldType;
    }

    public void updatePreCode() {
        String required = isFieldRequired() ? " required=\"true\" " : "";
        StringBuilder s = new StringBuilder();
        s.append("<df:outputLabel for=\"");
        s.append(getParameter());
        s.append("\" >");
        s.append(getQuestion());
        if(isFieldRequired()){
            s.append(" (*)");
        }
        s.append("</df:outputLabel>\n");
        if (inputFieldType.equals(TEXT_INPUT)) {
            s.append("<df:inputText ");
            s.append(" value=\"#{p.");
            s.append(getParameter());
            s.append("}\"");
            s.append(required);
            s.append(" id=\"");
            s.append(getParameter());
            s.append("\"");
            s.append("/>\n");
        } else if (inputFieldType.equals(TEXTAREA_INPUT)) {
            s.append("<df:inputTextarea ");
            s.append("rows=\"5\" cols=\"80\" ");
            s.append(" value=\"#{p.");
            s.append(getParameter());
            s.append("}\"");
            s.append(required);
            s.append(" id=\"");
            s.append(getParameter());
            s.append("\"");
            s.append("/>\n");
        } else if (inputFieldType.equals(DATE_INPUT)) {
            s.append("<df:selectInputDate ");
            s.append(" renderAsPopup=\"true\" ");
            s.append(" value=\"#{p.");
            s.append(getParameter());
            s.append("}\"");
            s.append(required);
            s.append(" id=\"");
            s.append(getParameter());
            s.append("\"");
            s.append(">\n");
            s.append("  <f:convertDateTime pattern=\"dd/MM/yyyy\" timeZone=\"GMT\"/>\n"); // SET TO GMT, or otherwise problems when converting.
            s.append("</df:selectInputDate>");
/*        } else if (inputFieldType.equals(FormEditorController.DATETIME_INPUT)) {
            s.append("<df:selectInputDate ");
            s.append(" renderAsPopup=\"true\" ");
            s.append(" value=\"#{p.");
            s.append(getParameter());
            s.append("}\"");
            s.append(required);
            s.append(" id=\"");
            s.append(getParameter());
            s.append("\"");
            s.append(">\n");
            s.append("  <f:convertDateTime pattern=\"dd/MM/yyyy HH:mm\" timeZone=\"#{modelBean.timeZone}\"/>\n");
            s.append("</df:selectInputDate>");
*/            
                   
        } else if (inputFieldType.equals(INTEGER_INPUT)) {
            s.append("<df:inputText ");
            s.append(" value=\"#{p.");
            s.append(getParameter());
            s.append("}\"");
            s.append(required);
            s.append(" id=\"");
            s.append(getParameter());
            s.append("\"");
            s.append(">\n");
//            s.append("  <f:convertNumber for=\"minVal\" maxFractionDigits=\"0\" minFractionDigits=\"0\" locale=\"BE\" groupingUsed=\"true\"/>\n");
            // The converter (unmeant behaviour) deletes everything after the decimal symbol if the wrong (dot instead of comma) decimal symbol is used
            if ((getMinValue() != null) && (getMaxValue() != null)) {
                s.append("  <f:validateLongRange minimum=\"");
                s.append(getMinValue().toPlainString());
                s.append("\" maximum=\"");
                s.append(getMaxValue().toPlainString());
                s.append("\"/>\n");
            }
            s.append("</df:inputText>\n");
        } else if (inputFieldType.equals(FLOAT_INPUT)) {
            s.append("<df:inputText ");
            s.append(" value=\"#{p.");
            s.append(getParameter());
            s.append("}\"");
            s.append(required);
            s.append(" id=\"");
            s.append(getParameter());
            s.append("\"");
            s.append(">\n");
//            s.append("  <f:convertNumber for=\"minVal\" maxFractionDigits=\"2\" minFractionDigits=\"0\" locale=\"BE\" groupingUsed=\"true\"/>");
            
            if ((getMinValue() != null) && (getMaxValue() != null)) {
                s.append("  <f:validateDoubleRange minimum=\"");
                s.append(getMinValue().toPlainString());
                s.append("\" maximum=\"");
                s.append(getMaxValue().toPlainString());
                s.append("\"/>\n");
            }
            s.append("</df:inputText>\n");
        } else if (inputFieldType.equals(MULTIPLE_CHOICE)) {

            String choices[] = multipleChoices.split("\\r?\\n");
            s.append("<df:selectOneRadio ");
            s.append(" layout=\"pageDirection\"");
            s.append(" value=\"#{p.");
            s.append(getParameter());
            s.append("}\"");
            s.append(required);
            s.append(" id=\"");
            s.append(getParameter());
            s.append("\"");
            s.append(">\n");
            for (String choice : choices) {
                if (!choice.isEmpty()) {
                    String valueLabel[] = choice.split(":");
                    s.append("  <f:selectItem");
                    s.append(" itemValue=\"");
                    s.append(valueLabel[0]);
                    s.append("\" ");
                    if (valueLabel.length > 1) {
                        s.append(" itemLabel=\"");
                        s.append(valueLabel[1]);
                        s.append("\" ");
                    } else {
                        s.append(" itemLabel=\"");
                        s.append(valueLabel[0]);
                        s.append("\" ");

                    }
                s.append("/>\n");
                }

            }
            s.append("</df:selectOneRadio>\n");

        }
        s.append("<df:message for=\"");
        s.append(getParameter());
        s.append("\"/>\n");
        setPreCode(s.toString());
    }
    private boolean numericInput = false;

    public boolean isNumericInput() {
        return numericInput;
    }

    public void setNumericInput(boolean numericInput) {
        this.numericInput = numericInput;
    }
    private boolean multipleChoiceInput = false;

    public boolean isMultipleChoiceInput() {
        return multipleChoiceInput;
    }

    public void setMultipleChoiceInput(boolean multipleChoiceInput) {
        this.multipleChoiceInput = multipleChoiceInput;
    }
    private String multipleChoices;

    public String getMultipleChoices() {
        return multipleChoices;
    }

    public void setMultipleChoices(String multipleChoices) {
        this.multipleChoices = multipleChoices;
    }
    private BigDecimal minValue;

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }
    private BigDecimal maxValue;

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }
    
    private boolean fieldRequired;

    public boolean isFieldRequired() {
        return fieldRequired;
    }

    public void setFieldRequired(boolean fieldRequired) {
        this.fieldRequired = fieldRequired;
    }

    
    /**
     * Gets the option items for input field types.
     *
     * @return array of input field type items
     */
    public SelectItem[] getInputFieldTypeItems() {
        SelectItem[] inputFieldTypeItems = {
            new SelectItem(TEXT_INPUT, "Text input"),
            new SelectItem(INTEGER_INPUT, "Integer input"),
            new SelectItem(FLOAT_INPUT, "Float input"),
            new SelectItem(MULTIPLE_CHOICE, "Multiple choice"),
            new SelectItem(TEXTAREA_INPUT, "Text area"),
            new SelectItem(DATE_INPUT, "Date"), //            new SelectItem(DATETIME_INPUT,"Date and time")
        };


        return inputFieldTypeItems;
    }

    void inputFieldTypeChanged(String e) {
       setNumericInput(e.equals(FLOAT_INPUT) || e.equals(INTEGER_INPUT));
       setMultipleChoiceInput(e.equals(MULTIPLE_CHOICE));
    }

}
