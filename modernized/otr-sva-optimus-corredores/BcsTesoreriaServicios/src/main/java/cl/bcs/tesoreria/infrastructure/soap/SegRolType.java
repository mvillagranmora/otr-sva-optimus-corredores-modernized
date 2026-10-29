
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SegRol_Type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * <p>
 * <pre>
 * &lt;simpleType name="SegRol_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="UsuarioApp"/>
 *     &lt;enumeration value="UsuarioRq"/>
 *     &lt;enumeration value="UsuarioSupervisor"/>
 *     &lt;enumeration value="UsuarioCard"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SegRol_Type")
@XmlEnum
public enum SegRolType {

    @XmlEnumValue("UsuarioApp")
    USUARIO_APP("UsuarioApp"),
    @XmlEnumValue("UsuarioRq")
    USUARIO_RQ("UsuarioRq"),
    @XmlEnumValue("UsuarioSupervisor")
    USUARIO_SUPERVISOR("UsuarioSupervisor"),
    @XmlEnumValue("UsuarioCard")
    USUARIO_CARD("UsuarioCard");
    private final String value;

    SegRolType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SegRolType fromValue(String v) {
        for (SegRolType c: SegRolType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
