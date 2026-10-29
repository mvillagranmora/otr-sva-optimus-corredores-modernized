package cl.bcs.fondomutuo.consultas.custodia;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** JSON types of legacy {@code ConsultarCustodiaPortafolioFondoMutuo} (names and order = legacy), the custody contract and the rows of its four SQL. */
public final class ConsultarCustodiaPortafolioContrato {

    private ConsultarCustodiaPortafolioContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdCliente = 0L;
        public Long p_IdPortafolio = 0L;
        public Long p_IdInstrumento = 0L;
        public String p_FechaConsulta = "0001-01-01";
        public Long p_IdProductoAgrupador;
        public String p_IdEmisor;
        public String p_IdFamiliaFondo = "";
        public String p_BuscaMLT = "N";
    }

    public static class Salida {
        public List<SalidaCustodia> P_OUT_TBL = new ArrayList<>();
    }

    public static class SalidaCustodia {
        public String IdClasificacionTipoInstrumento;
        public String NombreClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public String NombreTipoInstrumento;
        public Long IdInstrumento;
        public String Nemotecnico;
        public Long IdSocioDeNegocio;
        public Long IdCliente;
        public Long IdPortafolio;
        public String DocumentoIdentidad;
        public String RazonSocial;
        public String CodigoPortafolio;
        public String IdBoveda;
        public String Relacion;
        public BigDecimal CantidadCustodia = BigDecimal.ZERO;
        public BigDecimal Monto = BigDecimal.ZERO;
        public String IdMoneda;
        public BigDecimal ValorCuota = BigDecimal.ZERO;
        public Long DecimalesValorCuota;
        public Long DecimalesCuota;
        public BigDecimal CantidadGarantia = BigDecimal.ZERO;
        public BigDecimal CantidadTransSolicitudesAporte = BigDecimal.ZERO;
        public BigDecimal CantidadTransSolicitudesRescate = BigDecimal.ZERO;
        public BigDecimal MontoTransSolicitudesAporte;
        public BigDecimal MontoTransSolicitudesRescate;
        public BigDecimal INGPrecioPromedioCompra = BigDecimal.ZERO;
        public BigDecimal INGMontoPrecioPromedioCompra = BigDecimal.ZERO;
        public BigDecimal INGMontoPrecioPromedioMonedaInstrumento = BigDecimal.ZERO;
        public BigDecimal PPPCustodia = BigDecimal.ZERO;
        public String IdMonedaPrincipal;
        public String IdMonedaFondo;
        public String Producto;
        public String IdEmisor;
        public String MLT;
        public String IdSerieFondo;
        public Long IdEmpresa;
        public String IdFamiliaFondo;
        public String FondoExtranjero;
        public BigDecimal MontoMonedaSistema;
        public String FechaValorCuota;
        public String IdRegimenFondoMutuo;
        public Long IdMatrizAgrupador;
        public String EsADC;
        public String Fondo;
        public String NombreFondo;
        public BigDecimal ValorCuotaMonedaSistema;
        public String RegimenTributario = "";
        public String CodigoSerie;
        public String DocumentoIdentidadAdministradora;
        public String NombreAdministradora;
        public String RestringeRescate = "N";
        public String NombreSerieFondo;
        public BigDecimal MontoGarantiaMonedaSistema = BigDecimal.ZERO;
        public BigDecimal MontoGarantia = BigDecimal.ZERO;
    }

    /** Entry of the custody calculation (legacy {@code ConsultarCustodiaPortafolioFFMM.Entrada...}); serialised with nulls. */
    public static class CustodiaEntrada {
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

    public static class CustodiaSalida {
        public List<CustodiaFila> p_out_tbl = new ArrayList<>();
    }

    /** Legacy {@code ConsultarCustodiaPortafolioFFMM.SalidaCustodia}. */
    public static class CustodiaFila {
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

    /** Row of {@code ..._SelectSolicitudes.sql} (the 22 columns plus the {@code IdInterno} the class assigns). */
    public static class Solicitud {
        public Long IdInterno;
        public String IdSerieFondo;
        public BigDecimal CantidadAporte;
        public BigDecimal CantidadRescate;
        public BigDecimal CantidadMontoAporte;
        public BigDecimal CantidadMontoRescate;
        public BigDecimal MontoMonedaSistemaAporte;
        public Long IdCliente;
        public Long IdPortafolio;
        public Long IdSocioDeNegocio;
        public String DocumentoIdentidad;
        public String RazonSocial;
        public String CodigoPortafolio;
        public Long IdInstrumento;
        public String IdEmisor;
        public String Producto;
        public String IdRegimenFondoMutuo;
        public Long IdMatrizAgrupador;
        public String EsADC;
        public String CalculoPorMontoCuota;
        public String RegimenTributario;
        public String IdMonedaFondo;
        public String IdTipoMovimientoFondoMutuo;
    }

    public static class SolicitudTotal {
        public Long IdInstrumento;
        public Long IdPortafolio;
        public Long Contador;
    }

    /** Row of {@code ..._SelectSeries.sql}. */
    public static class SerieFondo {
        public String IdSerieFondo;
        public Long IdInstrumento;
        public String IdClasificacionTipoInstrumento;
        public String NombreClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public String NombreTipoInstrumento;
        public String IdMoneda;
        public Long DecimalesValorCuota;
        public Long DecimalesCuota;
        public String IdFondo;
        public Long IdEmpresa;
        public String IdFamiliaFondo;
        public String IdEmisor;
        public String FondoExtranjero;
        public String NombreFondo;
        public String CodigoSerie;
        public String DocumentoIdentidadAdministradora;
        public String RestringeRescate;
        public String NombreSerieFondo;
    }

    /** Row of {@code ..._SelectRegimen.sql}. */
    public static class Regimen {
        public String IdSerieFondo;
        public String AceptaMandatoEnReinversion;
        public String RescateConMandato;
    }
}
