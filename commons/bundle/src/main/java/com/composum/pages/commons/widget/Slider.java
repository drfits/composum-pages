package com.composum.pages.commons.widget;

import com.composum.pages.commons.taglib.PropertyEditHandle;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static com.composum.pages.commons.widget.Util.getDecimalOption;

public class Slider extends PropertyEditHandle<BigDecimal> {

    public static final BigDecimal DEFAULT_MIN = BigDecimal.ZERO;
    public static final BigDecimal DEFAULT_MAX = BigDecimal.valueOf(100L);
    public static final BigDecimal DEFAULT_STEP = BigDecimal.ONE;

    public static final String DEFAULT_VALUE_FORMAT = "#,##0.0";

    public class Options {

        public final BigDecimal min;
        public final BigDecimal max;
        public final BigDecimal step;
        public final BigDecimal def;

        public BigDecimal getMin() {
            return min;
        }

        public BigDecimal getMax() {
            return max;
        }

        public BigDecimal getStep() {
            return step;
        }

        public Options() {
            String[] options = widget.consumeDynamicAttribute("options", "").split(";");
            min = getDecimalOption(options, 0, DEFAULT_MIN);
            max = getDecimalOption(options, 1, DEFAULT_MAX);
            step = getDecimalOption(options, 2, DEFAULT_STEP);
            def = getDecimalOption(options, 3, min);
        }
    }

    private transient Options options;
    private transient String format;

    public Slider() {
        super(BigDecimal.class);
    }

    @Override
    public BigDecimal getValue() {
        BigDecimal value = super.getValue();
        return value != null ? value : getOptions().def;
    }

    public String getText() {
        BigDecimal value = getValue();
        return value != null ? new DecimalFormat(getFormat()).format(value) : "";
    }

    public Options getOptions() {
        if (options == null) {
            options = new Options();
        }
        return options;
    }

    public String getFormat() {
        if (format == null) {
            format = widget.consumeDynamicAttribute("format", DEFAULT_VALUE_FORMAT);
        }
        return format;
    }
}
