
package cl.bcs.custodia.utils.ClientWS.bcomercio.ejb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para wsCreaTokenAgente complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="wsCreaTokenAgente">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="usuario" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="perfil" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="rutCliente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cuenta" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="opcionMenu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wsCreaTokenAgente", propOrder = {
    "usuario",
    "perfil",
    "rutCliente",
    "cuenta",
    "opcionMenu"
})
public class WsCreaTokenAgente {

    protected String usuario;
    protected int perfil;
    protected String rutCliente;
    protected int cuenta;
    protected String opcionMenu;

    /**
     * Obtiene el valor de la propiedad usuario.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * Define el valor de la propiedad usuario.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsuario(String value) {
        this.usuario = value;
    }

    /**
     * Obtiene el valor de la propiedad perfil.
     * 
     */
    public int getPerfil() {
        return perfil;
    }

    /**
     * Define el valor de la propiedad perfil.
     * 
     */
    public void setPerfil(int value) {
        this.perfil = value;
    }

    /**
     * Obtiene el valor de la propiedad rutCliente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRutCliente() {
        return rutCliente;
    }

    /**
     * Define el valor de la propiedad rutCliente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRutCliente(String value) {
        this.rutCliente = value;
    }

    /**
     * Obtiene el valor de la propiedad cuenta.
     * 
     */
    public int getCuenta() {
        return cuenta;
    }

    /**
     * Define el valor de la propiedad cuenta.
     * 
     */
    public void setCuenta(int value) {
        this.cuenta = value;
    }

    /**
     * Obtiene el valor de la propiedad opcionMenu.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpcionMenu() {
        return opcionMenu;
    }

    /**
     * Define el valor de la propiedad opcionMenu.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpcionMenu(String value) {
        this.opcionMenu = value;
    }

}
