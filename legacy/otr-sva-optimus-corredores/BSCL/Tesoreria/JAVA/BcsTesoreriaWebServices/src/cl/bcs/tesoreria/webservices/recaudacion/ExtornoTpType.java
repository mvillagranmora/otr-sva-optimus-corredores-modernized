
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ExtornoTp_Type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * <p>
 * <pre>
 * &lt;simpleType name="ExtornoTp_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="P"/>
 *     &lt;enumeration value="E"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ExtornoTp_Type")
@XmlEnum
public enum ExtornoTpType {

    P,
    E;

    public String value() {
        return name();
    }

    public static ExtornoTpType fromValue(String v) {
        return valueOf(v);
    }

}
