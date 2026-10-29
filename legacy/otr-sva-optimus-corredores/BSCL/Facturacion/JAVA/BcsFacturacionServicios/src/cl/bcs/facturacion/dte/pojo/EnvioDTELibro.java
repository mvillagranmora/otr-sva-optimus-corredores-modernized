
package cl.bcs.facturacion.dte.pojo;

import java.util.List;


public class EnvioDTELibro {

    private String tipo;
    private String periodocontable;
    private String empresa;
    private LibroCv libroCV = null;
    private List<LibroCvDetalle> libroCVDetalle = null;
    private List<LibroCvTotal> libroCVTotal = null;
    
       
    

    /**
     * 
     * @return
     *     The tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * 
     * @param tipo
     *     The tipo
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    
    /**
     * 
     * @return
     *     The periodocontable
     */
    public String getPeriodoContable() {
        return periodocontable;
    }

    /**
     * 
     * @param periodocontable
     *     The periodocontable
     */
    public void setPeriodoContable(String periodocontable) {
        this.periodocontable = periodocontable;
    }
    
    
    /**
     * 
     * @return
     *     The empresa
     */
    public String getEmpresa() {
        return empresa;
    }

    /**
     * 
     * @param empresa
     *     The empresa
     */
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
    
    
    
    /**
     * 
     * @return
     *     The libroCv
     */      
    
    public LibroCv getLibroCv() {
        return libroCV;
    }

    /**
     * 
     * @param libroCv
     *     The libroCv
     */
    public void setLibroCv(LibroCv libroCv) {
        this.libroCV = libroCv;
    }
    
    
    
    /**
     * 
     * @return
     *     The libroCvDetalle
     */      
    
    public List<LibroCvDetalle> getLibroCvDetalle() {
        return libroCVDetalle;
    }

    /**
     * 
     * @param libroCvDetalle
     *     The libroCvDetalle
     */
    public void setLibroCvDetalle(List<LibroCvDetalle> libroCvDetalle) {
        this.libroCVDetalle = libroCvDetalle;
    }

    
    
    /**
     * 
     * @return
     *     The libroCvTotal
     */      
    
    public List<LibroCvTotal> getLibroCVTotal() {
        return libroCVTotal;
    }

    /**
     * 
     * @param detalleDocumentos
     *     The detalleDocumentos
     */
    public void setLibroCVTotal(List<LibroCvTotal> libroCvTotal) {
        this.libroCVTotal = libroCvTotal;
    }
    

}
