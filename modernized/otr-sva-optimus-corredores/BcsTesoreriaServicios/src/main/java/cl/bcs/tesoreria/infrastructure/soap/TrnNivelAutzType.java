
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para TrnNivelAutz_Type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * <p>
 * <pre>
 * &lt;simpleType name="TrnNivelAutz_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Normal"/>
 *     &lt;enumeration value="UsuarioRq"/>
 *     &lt;enumeration value="UsuarioSupervisor"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TrnNivelAutz_Type")
@XmlEnum
public enum TrnNivelAutzType {

    @XmlEnumValue("Normal")
    NORMAL("Normal"),
    @XmlEnumValue("UsuarioRq")
    USUARIO_RQ("UsuarioRq"),
    @XmlEnumValue("UsuarioSupervisor")
    USUARIO_SUPERVISOR("UsuarioSupervisor");
    private final String value;

    TrnNivelAutzType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TrnNivelAutzType fromValue(String v) {
        for (TrnNivelAutzType c: TrnNivelAutzType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
