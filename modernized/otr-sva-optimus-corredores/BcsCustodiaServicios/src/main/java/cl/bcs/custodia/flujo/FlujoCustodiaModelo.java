package cl.bcs.custodia.flujo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.custodia.flujo.kit.MensajeSalida;

/**
 * Shapes of ConsultarFlujoCustodia: the request, the response and the rows the sources are read into. Public fields, in the legacy order and with the
 * legacy names and types: the JSON of the response (field order, nulls, BigDecimal scale, dates) and the mapping of the SQL columns by label depend on
 * them (CUS-FLU-01, D-CUS-05). The progress bar of the legacy request ({@code p_BarraProgreso}) is not carried: nothing in the in-process call
 * supplies one.
 */
public final class FlujoCustodiaModelo {

    private FlujoCustodiaModelo() {
    }

    /** Request. Lists default to empty; an explicit JSON null stays null (D-CUS-01). */
    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaSaldo;

        public Long p_IdPais;                  // -1: the country of the company
        public Integer p_DiasProyeccion;
        public String p_DiaHabil = "S";
        public String p_ValorizarFlujo;

        public List<Uso> p_Uso = new ArrayList<>();
        public List<FiltroBusqueda> p_FiltrosBusqueda = new ArrayList<>();
        public List<AgrupadoPor> p_EsAgrupadoPor = new ArrayList<>();
    }

    /** Response; {@code P_OUT_ERRORESCONTADOR} is recomputed from the messages by the caller (CUS-FLU-01). */
    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;

        public Long p_out_KeyFlujoCustodia;
        public List<Totalizado> p_out_FlujoTotalizado = new ArrayList<>();
        public List<Detallado> p_out_FlujoDetallado = new ArrayList<>();
    }

    /** A requested use; two uses are equal when their text forms are (the legacy {@code distinct()}). */
    public static class Uso {
        public String UsoTipoOperacion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String BusquedaEspecifica;
        public String IdTipoInstrumentoEspecifico;

        @Override
        public boolean equals(Object o) {
            Uso u1 = (Uso) o;
            return (this.UsoTipoOperacion + "").equals(u1.UsoTipoOperacion + "")
                    && (this.IdCliente + "").equals(u1.IdCliente + "")
                    && (this.IdPortafolio + "").equals(u1.IdPortafolio + "")
                    && (this.BusquedaEspecifica + "").equals(u1.BusquedaEspecifica + "")
                    && (this.IdTipoInstrumentoEspecifico + "").equals(u1.IdTipoInstrumentoEspecifico + "");
        }

        @Override
        public int hashCode() {
            return (UsoTipoOperacion + IdCliente + IdPortafolio + BusquedaEspecifica + IdTipoInstrumentoEspecifico).hashCode();
        }
    }

    /** A requested search filter; equality as {@link Uso}. */
    public static class FiltroBusqueda {
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String IdEmisor;
        public java.sql.Date FechaVencimiento;
        public String IdTipoReajuste;
        public String EsFungible;
        public String IdBoveda;
        public String IdRegimenFondoMutuo;

        @Override
        public boolean equals(Object o) {
            FiltroBusqueda u1 = (FiltroBusqueda) o;
            return (this.IdClasificacionTipoInstrumento + "").equals(u1.IdClasificacionTipoInstrumento + "")
                    && (this.IdTipoInstrumento + "").equals(u1.IdTipoInstrumento + "")
                    && (this.IdInstrumento + "").equals(u1.IdInstrumento + "")
                    && (this.IdEmisor + "").equals(u1.IdEmisor + "")
                    && (this.FechaVencimiento + "").equals(u1.FechaVencimiento + "")
                    && (this.IdTipoReajuste + "").equals(u1.IdTipoReajuste + "")
                    && (this.EsFungible + "").equals(u1.EsFungible + "")
                    && (this.IdBoveda + "").equals(u1.IdBoveda + "")
                    && (this.IdRegimenFondoMutuo + "").equals(u1.IdRegimenFondoMutuo + "");
        }

        @Override
        public int hashCode() {
            return (IdClasificacionTipoInstrumento + IdTipoInstrumento + IdInstrumento + IdEmisor + FechaVencimiento + IdTipoReajuste
                    + EsFungible + IdBoveda + IdRegimenFondoMutuo).hashCode();
        }
    }

    /** One use crossed with one filter (CUS-FLU-08). */
    public static class UsoFiltro {
        public String UsoTipoOperacion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String BusquedaEspecifica;
        public String IdTipoInstrumentoEspecifico;

        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String IdEmisor;
        public java.sql.Date FechaVencimiento;
        public String IdTipoReajuste;
        public String EsFungible;
        public String IdBoveda;
        public String IdRegimenFondoMutuo;
    }

    /** Which dimensions the totalised flow is grouped by ("S" keeps the dimension). */
    public static class AgrupadoPor {
        public String ClasificacionTipoInstrumento;
        public String TipoInstrumento;
        public String Instrumento;
        public String Emisor;
        public String Nemotecnico;
        public String NemoSVS;
        public String FechaEmisionInstrumento;
        public String FechaVencimientoInstrumento;
        public String FechaLiquidacionInstrumento;
        public String TipoReajuste;
        public String EsFungible;
        public String CodigoIFRS;
        public String Boveda;
        public String Bolsa;
        public String TipoOperacion;
        public String Mesa;
        public String FolioRegistro;
        public String ItemOrigen;
        public String IdRegimenFondoMutuo;
        public String GrupoRegimen;
        public String MonedaInstrumento;
    }

    /** A totalised line of the response. */
    public static class Totalizado {
        public Long IdProcesamientoInterno;
        public String UsoTipoOperacion;
        public String BusquedaEspecifica;
        public java.sql.Date FechaSaldo;
        public Long Dias = 0L;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String IdEmisor;
        public String Nemotecnico;
        public String NemoSVS;
        public BigDecimal CorteMinimo;
        public String IdMonedaInstrumento;
        public String IdTipoReajuste;
        public java.sql.Date FechaEmisionInstrumento;
        public java.sql.Date FechaVencimientoInstrumento;
        public java.sql.Date FechaLiquidacionInstrumento;
        public String CodigoIFRS;
        public String EsFungible;
        public String IdBoveda;
        public String IdBolsa;
        public String IdTipoOperacion;
        public String Accion;
        public Long IdMesa;
        public Long FolioRegistro;
        public String ItemOrigen;
        public BigDecimal Cantidad;
        public BigDecimal ValorRescate;
        public BigDecimal PrecioMercado;
        public BigDecimal TasaMercado;
        public BigDecimal MontoAMercadoMonedaSistema;
        public BigDecimal MontoAMercadoMonedaInstrumento;
        public String IdMonedaEmisionInstrumento;
        public String IdRegimenFondoMutuo;
        public String GrupoRegimen;
    }

    /** A detail line of the response (every real row read from the sources). */
    public static class Detallado {
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String TipoValorizacion;
        public String IdEmisor;
        public String Nemotecnico;
        public String NemoSVS;
        public BigDecimal CorteMinimo;
        public String IdMonedaInstrumento;
        public String IdTipoReajuste;
        public java.sql.Date FechaEmisionInstrumento;
        public java.sql.Date FechaVencimientoInstrumento;
        public String CodigoIFRS;
        public String EsFungible;
        public String IdBoveda;
        public String IdBolsa;
        public String IdTipoOperacion;
        public String Accion;
        public String IdTipoProducto;
        public Long IdMesa;
        public java.sql.Date FechaLiquidacion;
        public BigDecimal Cantidad;
        public BigDecimal ValorRescate;
        public BigDecimal PrecioCompra;
        public BigDecimal TasaCompra;
        public BigDecimal MontoATasaCompra;
        public BigDecimal PrecioMercado;
        public BigDecimal TasaMercado;
        public BigDecimal MontoATasaMercado;
        public BigDecimal PrecioVector;
        public BigDecimal MontoVector;
        public Long IdRegistro;
        public Long FolioRegistro;

        public String IdConcepto;
        public Long IdDatosFlujoCustodia;
        public Long IdUsoFlujoCustodia;
        public String UsoTipoOperacion;
        public String BusquedaEspecifica;
        public String ItemCalculo;
        public String ItemOrigen;
        public String FuncionOperador;
        public BigDecimal CantidadProceso;
        public BigDecimal ValorRescateProceso;
        public BigDecimal PrecioCompraProceso;
        public BigDecimal TasaCompraProceso;
        public BigDecimal MontoCompraProceso;
        public BigDecimal PrecioMercadoProceso;
        public BigDecimal TasaMercadoProceso;
        public BigDecimal MontoMercadoProceso;
        public BigDecimal PrecioVectorProceso;
        public BigDecimal MontoVectorProceso;
        public java.sql.Date FechaLiquidacionProceso;
        public String RequiereValorizacion;
        public String ValorizarFlujo;
        public String IdRegimenFondoMutuo;
        public String GrupoRegimen;
        public java.sql.Date FechaOrigenCompra;
        public java.sql.Date FechaOperacion;
        public Long IdContraparte;
        public Long IdCorredorContraparte;
    }

    /** Configuration of a use joined with its items (rows of the two usage queries). */
    public static class ConfiguracionUso {
        public Long IdUsuario;
        public Long IdUsoFlujoCustodia;
        public String UsoTipoOperacion;
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public Long IdCliente;
        public Long IdPortafolio;
        public String BusquedaEspecifica;
        public java.sql.Timestamp FechaIngresoRegistro;

        public String ItemCalculo;
        public String ItemOrigen;
        public String Descripcion;
        public String FuncionOperador;
        public String UsoValorizacion;
    }

    /** A row of {@code CUS.FlujoCustodia_MatrizUso}: the winner of a requested key (field names are the INSERT parameters). */
    public static class MatrizUso {
        public Long IdTrabajo;
        public Long IdCliente;
        public Long IdPortafolio;
        public String UsoTipoOperacion;
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String IdEmisor;
        public java.sql.Date FechaVencimiento;
        public String IdTipoReajuste;
        public String EsFungible;
        public String IdBoveda;
        public String IdTipoInstrumentoEspecifico;
        public Long IdUsoFlujoCustodia;
        public String BusquedaEspecifica;
        public String IdRegimenFondoMutuo;
        public String IdEstado;
        public Long IdEmpresa;

        public String ItemCalculo;
        public String ItemOrigen;

        public String FuncionOperador;

        public int MayorSimilitud;
    }

    /** A row read from a source, then projected (rows of the {@code FlujoCustodia_Select*.sql} files; columns by label). */
    public static class DatosFlujo {
        public Long IdDatosFlujoCustodia;
        public Long KeyFlujoCustodia;
        public Long IdUsoFlujoCustodia;
        public String UsoTipoOperacion;
        public String BusquedaEspecifica;
        public String ItemCalculo;
        public String ItemOrigen;
        public String FuncionOperador = "";
        public java.sql.Timestamp FechaEjecucion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String EsCuentaPropia;
        public Long IdRegistro;
        public Long FolioRegistro;
        public String IdConcepto;
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String TipoValorizacion;
        public String IdEmisor;
        public String Nemotecnico;
        public String NemoSVS;
        public BigDecimal CorteMinimo;
        public String IdMonedaInstrumento;
        public String IdTipoReajuste;
        public java.sql.Date FechaEmisionInstrumento;
        public java.sql.Date FechaVencimientoInstrumento;
        public String CodigoIFRS;
        public String EsFungible;
        public String TipoCarteraCorredor;
        public String IdBoveda;
        public String IdTipoInstrumentoEspecifico;
        public String IdBolsa;
        public String IdTipoOperacion;
        public String Accion;
        public String IdTipoProducto;
        public Long IdMesa;
        public java.sql.Date FechaLiquidacion;
        public BigDecimal Cantidad;
        public BigDecimal ValorRescate;
        public BigDecimal PrecioCompra;
        public BigDecimal TasaCompra;
        public BigDecimal MontoATasaCompra;
        public BigDecimal PrecioMercado;
        public BigDecimal TasaMercado;
        public BigDecimal MontoATasaMercado;
        public BigDecimal PrecioVector;
        public BigDecimal MontoVector;
        public BigDecimal CantidadProceso;
        public BigDecimal ValorRescateProceso;
        public BigDecimal PrecioCompraProceso;
        public BigDecimal TasaCompraProceso;
        public BigDecimal MontoCompraProcesoMonedaSistema;
        public BigDecimal MontoCompraProcesoMonedaInstrumento;

        public BigDecimal PrecioMercadoProceso;
        public BigDecimal TasaMercadoProceso;
        public BigDecimal MontoMercadoProcesoMonedaSistema;
        public BigDecimal MontoMercadoProcesoMonedaInstrumento;

        public java.sql.Date FechaLiquidacionProceso;
        public String RequiereValorizacion;
        public String TipoRegistro = "R";      // R real, P projected, M matrix, D discarded
        public String MotivoEliminacioRegistro = "";
        public String ValorizarFlujo = "N";
        public String VALTipoCartera = "";
        public String VALUsoCriterio = "";
        public String VALIdTipoCriterioTasa = "";
        public BigDecimal VALPrecioCompra = BigDecimal.ZERO;
        public BigDecimal VALTasaCompra = BigDecimal.ZERO;
        public BigDecimal VALMontoATasaCompraMonedaInstrumento = BigDecimal.ZERO;
        public BigDecimal VALMontoATasaCompraMonedaSistema = BigDecimal.ZERO;

        public BigDecimal VALPrecioMercado = BigDecimal.ZERO;
        public BigDecimal VALTasaMercado = BigDecimal.ZERO;
        public BigDecimal VALMontoATasaMercadoMonedaInstrumento = BigDecimal.ZERO;
        public BigDecimal VALMontoATasaMercadoMonedaSistema = BigDecimal.ZERO;

        public BigDecimal MontoCompraMonedaLocalProceso;

        public String IdAccionOperacion;
        public String IdMonedaEmision;

        public String IdRegimenFondoMutuo;
        public String GrupoRegimen;
        public java.sql.Date FechaOrigenCompra;
        public java.sql.Date FechaOperacion;
        public String EsVentaDescubierta;
        public Long IdContraparte;
        public Long IdCorredorContraparte;
    }

    /** A processed line (row x projected date x base row) the totalised flow is built from. */
    public static class DatosProcesados {
        public java.sql.Date FechaSaldo;
        public Long IdCliente;
        public Long IdPortafolio;
        public Long FolioRegistro = 0L;
        public String IdTipoOperacion = "";
        public String Accion = "";
        public String IdMonedaInstrumento = "";
        public String IdMonedaEmision;
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String Nemotecnico = "";
        public String NemoSVS = "";
        public BigDecimal CorteMinimo;
        public String IdEmisor;
        public java.sql.Date FechaEmisionInstrumento;
        public java.sql.Date FechaVencimientoInstrumento;
        public java.sql.Date FechaLiquidacionInstrumento;
        public String IdTipoReajuste;
        public String EsFungible;
        public String IdBoveda;
        public String IdBolsa = "";
        public String CodigoIFRS = "";
        public Long IdMesa = 0L;
        public Long IdUsoFlujoCustodia;
        public String UsoTipoOperacion;
        public String BusquedaEspecifica;
        public String ItemOrigen;
        public String ItemCalculo;
        public BigDecimal CantidadProceso = new BigDecimal(0);
        public BigDecimal ValorRescateProceso = new BigDecimal(0);
        public BigDecimal PrecioCompraProceso = new BigDecimal(0);
        public BigDecimal TasaCompraProceso = new BigDecimal(0);
        public BigDecimal MontoCompraProcesoMonedaSistema = new BigDecimal(0);
        public BigDecimal MontoCompraProcesoMonedaInstrumento = new BigDecimal(0);
        public BigDecimal PrecioMercadoProceso = new BigDecimal(0);
        public BigDecimal TasaMercadoProceso = new BigDecimal(0);
        public BigDecimal MontoMercadoProcesoMonedaSistema = new BigDecimal(0);
        public BigDecimal MontoMercadoProcesoMonedaInstrumento = new BigDecimal(0);
        public BigDecimal PrecioVectorProceso = new BigDecimal(0);
        public BigDecimal MontoVectorProceso = new BigDecimal(0);
        public Long CantidadItem;
        public Long SItemCalculo;
        public String hashSItemCalculo;
        public Long SItemOrigen;
        public String hashSItemOrigen;
        public Long SREG;
        public String hashSREG;
        public BigDecimal MontoCompraMonedaLocalProceso = new BigDecimal(0);

        public String Fecha;
        public String ConsiderarRegistro;

        public String IdRegimenFondoMutuo;
        public String GrupoRegimen;
    }

    /** Row of the own-account portfolios query. */
    public static class PortafolioCuentaPropia {
        public Long IdPortafolio;
    }

    /** Row of the guarantee instrument-type query. */
    public static class TipoInstrumentoParaGarantia {
        public String IdTipoInstrumentoAval;
        public String IdTipoInstrumentoGarantizado;
    }

    /** Row of the portfolio-count query ({@code COUNT(1) AS "Count"}). */
    public static class ConteoCartera {
        public Long Count;
    }
}
