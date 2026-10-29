package cl.bcs.normativa.cartolas.tools.generator;

public class NameValue {

    private final String name;
    private final String value;

    public NameValue(
            final String name,
            final String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
