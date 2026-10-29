package cl.bcs.normativa.cartolas.tools.generator;

import net.sf.jasperreports.engine.JRField;

public class Field {

    public static Field newInstance(
            final JRField jrField) {
        return new Field(jrField);
    }

    public static String toLowerCamelCase(
            final String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        String ret = name.substring(0, 1).toLowerCase();
        if (name.length() > 1) {
            ret += name.substring(1);
        }

        return ret;
    }

    public static String toSqlType(
            final String javaClass) {
        if ("String".equals(javaClass)) {
            return "NVARCHAR(1024)";
        } else if ("Integer".equals(javaClass)) {
            return "BIGINT";
        } else if ("java.math.BigDecimal".equals(javaClass)) {
            return "DECIMAL(18,6)";
        } else if ("java.sql.Timestamp".equals(javaClass)) {
            return "TIMESTAMP";
        } else {
            return javaClass;
        }
    }

    public static String toUpperCamelCase(
            final String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        String ret = name.substring(0, 1).toUpperCase();
        if (name.length() > 1) {
            ret += name.substring(1);
        }

        return ret;
    }

    private String javaClass;
    private String name;

    public Field(
            final JRField jrField) {
        this(jrField.getName(), jrField.getValueClassName());
    }

    public Field(
            final String name,
            final String javaClass) {
        this.name = name;
        this.javaClass = javaClass;
    }

    public String getJavaClass() {
        return javaClass;
    }

    public String getLowerCamelName() {
        return toLowerCamelCase(name);
    }

    public String getName() {
        return name;
    }

    public String getSqlType() {
        return toSqlType(this.javaClass);
    }

    public String getUpperCamelName() {
        return toUpperCamelCase(name);
    }

    public void setJavaClass(
            final String javaClass) {
        this.javaClass = javaClass;
    }

    public void setName(
            final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Field [name=" + name + ", javaClass=" + javaClass + "]";
    }
}
