package cl.bcs.fondomutuo.consultas.custodiaffmm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** JSON and row types of legacy {@code ConsultarCustodiaPortafolioFFMM} (names and order = legacy). */
public final class ConsultarCustodiaPortafolioFfmmContrato {

    private ConsultarCustodiaPortafolioFfmmContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdCliente = 0L;
        public Long p_IdPortafolio = 0L;
        public Long p_IdProductoAgrupador;
        public String p_IdEmisor;
        public Long p_IdInstrumento = 0L;
        public String p_IdBoveda = "";
        public String p_AgrupaPorBoveda = "N";
        public String p_AgrupaPorRelacion = "N";
        public String p_FechaConsulta = "0001-01-01";
        public String p_TipoFechaConsulta = "LIQ";
        public String p_IdFamiliaFondo = "";
    }

    public static class Salida {
        public List<SalidaCustodia> p_out_tbl = new ArrayList<>();
    }

    public static class SalidaCustodia {
        public String IdClasificacionTipoInstrumento;
        public String NombreClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public String NombreTipoInstrumento;
        public Long IdInstrumento;
        public String Nemotecnico;
        public String IdEmisor;
        public Long IdSocioDeNegocio;
        public Long IdCliente;
        public Long IdPortafolio;
        public String DocumentoIdentidad;
        public String RazonSocial;
        public String CodigoPortafolio;
        public String IdBoveda;
        public String Relacion;
        public String IdRegimenFondoMutuo;
        public String RegimenTributario;
        public String Producto;
        public BigDecimal CantidadCustodia;
        public BigDecimal CantidadGarantia;
        public BigDecimal CantidadPrecioPromedio;
        public BigDecimal MontoPrecioPromedio;
        public BigDecimal INGPrecioPromedioCompra;
        public BigDecimal INGMontoPrecioPromedioCompra;
        public BigDecimal INGMontoPrecioPromedioMonedaInstrumento;
        public BigDecimal TotalPosicion;
        public Long IdMatrizAgrupador;
        public String EsADC;
        public BigDecimal PPPCustodia;
    }

    /** Row of the custody SQL (current and historical). */
    public static class Custodia {
        public String IdClasificacionTipoInstrumento;
        public String NombreClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public String NombreTipoInstrumento;
        public Long IdInstrumento;
        public String Nemotecnico;
        public String IdEmisor;
        public Long IdCliente;
        public Long IdPortafolio;
        public Long IdSocioDeNegocio;
        public String DocumentoIdentidad;
        public String RazonSocial;
        public String CodigoPortafolio;
        public String IdBoveda;
        public String Relacion;
        public BigDecimal Cantidad = BigDecimal.ZERO;
        public BigDecimal INGPrecioPromedioCompra = BigDecimal.ZERO;
        public BigDecimal INGMontoPrecioPromedioCompra = BigDecimal.ZERO;
        public BigDecimal INGMontoPrecioPromedioMonedaInstrumento = BigDecimal.ZERO;
        public BigDecimal ValorCompra = BigDecimal.ZERO;
        public BigDecimal PrecioCompra = BigDecimal.ZERO;
        public BigDecimal MontoCompra = BigDecimal.ZERO;
        public BigDecimal MontoPrecioPromedio = BigDecimal.ZERO;
        public String IdRegimenFondoMutuo;
        public Long IdPersona;
        public String Producto;
        public Long IdMatrizAgrupador;
        public String EsADC;
        public String RegimenTributario = "";
    }

    /** Row of the guarantee SQL (current and historical). */
    public static class Garantia {
        public String IdClasificacionTipoInstrumento;
        public BigDecimal MontoCompraCarteraOrigen;
        public String NombreClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public String NombreTipoInstrumento;
        public Long IdInstrumento;
        public String Nemotecnico;
        public String IdEmisor;
        public Long IdCliente;
        public Long IdPortafolio;
        public Long IdSocioDeNegocio;
        public String DocumentoIdentidad;
        public String RazonSocial;
        public String CodigoPortafolio;
        public String IdBoveda;
        public String Relacion;
        public BigDecimal Cantidad = BigDecimal.ZERO;
        public String IdRegimenFondoMutuo;
        public Long IdPersona;
        public String Producto;
        public Long IdMatrizAgrupador;
        public String EsADC;
    }

    public static class PersonaDestino {
        public Long IdPersonaDestino;
    }

    /** The grouping holder (no quantities). */
    public static class Instrumento {
        public String Id;
        public String IdClasificacionTipoInstrumento;
        public String NombreClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public String NombreTipoInstrumento;
        public Long IdInstrumento;
        public String Nemotecnico;
        public String IdEmisor;
        public Long IdCliente;
        public Long IdPortafolio;
        public Long IdSocioDeNegocio;
        public String DocumentoIdentidad;
        public String RazonSocial;
        public String CodigoPortafolio;
        public String IdBoveda;
        public String Relacion;
        public String IdRegimenFondoMutuo;
        public String Producto;
        public String EsADC;
        public Long IdMatrizAgrupador;
        public String RegimenTributario;
    }
}
