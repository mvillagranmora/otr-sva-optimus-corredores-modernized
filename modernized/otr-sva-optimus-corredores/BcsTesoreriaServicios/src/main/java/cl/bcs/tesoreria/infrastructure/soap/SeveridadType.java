
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para Severidad_Type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * <p>
 * <pre>
 * &lt;simpleType name="Severidad_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Informacion"/>
 *     &lt;enumeration value="Advertencia"/>
 *     &lt;enumeration value="Error"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Severidad_Type")
@XmlEnum
public enum SeveridadType {

    @XmlEnumValue("Informacion")
    INFORMACION("Informacion"),
    @XmlEnumValue("Advertencia")
    ADVERTENCIA("Advertencia"),
    @XmlEnumValue("Error")
    ERROR("Error");
    private final String value;

    SeveridadType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SeveridadType fromValue(String v) {
        for (SeveridadType c: SeveridadType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
