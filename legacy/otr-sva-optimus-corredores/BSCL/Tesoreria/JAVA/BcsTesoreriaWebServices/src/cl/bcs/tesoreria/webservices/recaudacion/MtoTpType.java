
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para MtoTp_Type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * <p>
 * <pre>
 * &lt;simpleType name="MtoTp_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;maxLength value="20"/>
 *     &lt;enumeration value="MontoTotal"/>
 *     &lt;enumeration value="CargoFijo"/>
 *     &lt;enumeration value="Mora"/>
 *     &lt;enumeration value="MontoMinimo"/>
 *     &lt;enumeration value="MontoMaximo"/>
 *     &lt;enumeration value="ImporteAdicional1"/>
 *     &lt;enumeration value="ImporteAdicional2"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MtoTp_Type")
@XmlEnum
public enum MtoTpType {

    @XmlEnumValue("MontoTotal")
    MONTO_TOTAL("MontoTotal"),
    @XmlEnumValue("CargoFijo")
    CARGO_FIJO("CargoFijo"),
    @XmlEnumValue("Mora")
    MORA("Mora"),
    @XmlEnumValue("MontoMinimo")
    MONTO_MINIMO("MontoMinimo"),
    @XmlEnumValue("MontoMaximo")
    MONTO_MAXIMO("MontoMaximo"),
    @XmlEnumValue("ImporteAdicional1")
    IMPORTE_ADICIONAL_1("ImporteAdicional1"),
    @XmlEnumValue("ImporteAdicional2")
    IMPORTE_ADICIONAL_2("ImporteAdicional2");
    private final String value;

    MtoTpType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MtoTpType fromValue(String v) {
        for (MtoTpType c: MtoTpType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
