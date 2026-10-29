package cl.bcs.tesoreria.javacall.kit.colaborador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.utils.Utils;

/**
 * Contract of the legacy service CrearEventoAutorizacionMasivo as the PAC mandate services use it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract); toString is the legacy JSON dump.
 */
public final class CrearEventoAutorizacionMasivo {

    private CrearEventoAutorizacionMasivo() {
    }

    public static class ServiceEntrada {

        public Long                        P_IDEMPRESA;
        public Long                        P_IDUSUARIO;
        public String                    P_IDMODULO;
        public String                    P_SIMULAROPERACION;
        public String                    P_USAREGLASNEGOCIO;
        public List<ObjTABLAEVENTOS>    P_TABLAEVENTOS;
        public List<ObjTABLADETALLE>     P_DETALLE = null ;
        public ServiceEntrada() {
            this.P_TABLAEVENTOS = new ArrayList<ObjTABLAEVENTOS>();
            this.P_DETALLE      = new ArrayList<ObjTABLADETALLE>(); 
        }

        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    public static class ServiceSalida {

        public List<ObjIDEVENTO>            P_OUT_IDEVENTO;
        public Long                            P_OUT_ADVERTENCIASCONTADOR;
        public List<ObjADVERTENCIASDETALLE>    P_OUT_ADVERTENCIASDETALLE;
        public Long                            P_OUT_ERRORESCONTADOR;
        public List<ObjERRORESDETALLE>        P_OUT_ERRORESDETALLE;
        
        public ServiceSalida() {
            this.P_OUT_ADVERTENCIASCONTADOR = 0L;
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_IDEVENTO = new ArrayList<ObjIDEVENTO>();
            this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<ObjADVERTENCIASDETALLE>();
            this.P_OUT_ERRORESDETALLE = new ArrayList<ObjERRORESDETALLE>();
        }
        
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    public static class ObjTABLAEVENTOS {
        public Long            IdUnion = null;
        public String        IdTipoEvento;
        public String        IdModuloSolicita;
        public Long            IdUsuarioSolicita;
        public String        Fecha;
        public String        IdTipoOperacion;
        public String        IdConcepto;
        public String        NumeroOperacionConcepto;
        public String        FechaOperacionConcepto;
        public Long            IdCliente;
        public Long            IdFuncionario;
        public Long            IdProveedor;
        public String        IdTipoProducto;
        public String        IdClasificacionTipoInstrumento;
        public String        IdTipoInstrumento;
        public Long            IdInstrumento;
        public String        Nemo;
        public Long            Cantidad;
        public BigDecimal    Monto;
        public BigDecimal    Precio;
        public BigDecimal    Tasa;
        public String        IdMoneda;
        public String        Glosa;
        
        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    public static class ObjTABLADETALLE {
        public Long          IdUnion          = null;
        public String      NombreCampo      = "";
        public String      TipoDato         = "";
        public String      Valor            = "";
        public String     Clasificacion    = "";
        
    }
    public static class ObjIDEVENTO {
        public Long        IdEvento;
        public String    NumeroOperacionConcepto;
    }
    public static class ObjADVERTENCIASDETALLE {
        public String    Codigo;
        public String    RequiereAutorizacion;
        public String    Descripcion;
    }
    public static class ObjERRORESDETALLE {
        public String    Codigo;
        public String    Descripcion;
    }



    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static ServiceSalida invocar(Colaboradores colaboradores, ServiceEntrada entrada) {
        JsonObject r = colaboradores.invocar("CrearEventoAutorizacionMasivo", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, ServiceSalida.class);
    }
}
