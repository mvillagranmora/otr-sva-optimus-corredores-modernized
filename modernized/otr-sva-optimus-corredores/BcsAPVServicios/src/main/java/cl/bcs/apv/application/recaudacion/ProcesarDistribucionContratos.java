package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * JSON contracts of the collaborators OUTSIDE APV that ProcesarDistribucion calls through the {@code Colaboradores} port. The nested classes are the legacy
 * Entrada/Salida POJOs (field names ARE the JSON contract, defaults included: the goldens pin what the legacy handed to each collaborator).
 */
public final class ProcesarDistribucionContratos {

    private ProcesarDistribucionContratos() {
    }

    // ------------------------------------------------------------------------------------------------ CrearCargoAbono (Cuenta Inversion)

    public static final class CrearCargoAbono {
        private CrearCargoAbono() {
        }

        public static class ObjCargoAbonoAGenerar {
            public Long NumeroCargoAbono;
            public Long IdPortafolio;
            public String IdMoneda;
            public String IdTipoMovimiento;
            public String IdConcepto;
            public String Descripcion;
            public String IdCondicionDeLiquidacion;
            public String FechaCondicionDeLiquidacion;
            public BigDecimal Monto;
            public Long IdEjecutivo;
            public Long IdSucursal;
            public String IdTipoProducto;
            public String IdTipoOperacion;
            public Long IdOperacion;
            public String FechaOperacion;
            public String FechaLiquidacionOperacion;
            public String FechaVencimientoOperacion;
            public String IdMonedaOperacion;
            public BigDecimal MontoOperacion;
            public BigDecimal TipoCambioOperacion;
            public Long IdContraparte;
            public String IdTipoComprobante;
            public Long IdComprobante;
            public String FolioComprobante;
            public Long IdComprobanteDetalle;
            public Long IdComprobanteFormaDePago;
            public String IdTipoMovimientoAsociado;
            public Long IdMovimientoAsociado;
            public String FechaComprobante;
            public Long IdPersonaPagoCobro;
            public Long IdInstrumento;
            public String IdTipoInstrumento;
            public BigDecimal Cantidad;
            public String Nemo;
            public String EsIndicadorBilateral;
            public String EsCargoAbonoPorTraspaso;
            public Long IdPortafolioTraspaso;
            public String GeneroContraparteEnTesoreria;
            public String CertificadoAutomatico;
        }

        public static class ObjCargoAbonoGenerado {
            public Long NumeroCargoAbono;
            public Long IdCargoAbonoMovimiento;
            public String IdTipoMovimiento;
        }

        public static class CrearCargoAbonoEntrada {
            public Long p_IdEmpresa;
            public Long p_IdUsuario;
            public String p_IdModulo;
            public String p_simularOperacion;
            public String p_UsaReglasNegocio;
            public String p_FechaSistema;
            public String p_EsIngresoManual = "N";
            public List<ObjCargoAbonoAGenerar> p_CargoAbonoAGenerar;

            public CrearCargoAbonoEntrada() {
                p_CargoAbonoAGenerar = new ArrayList<ObjCargoAbonoAGenerar>();
            }
        }

        public static class CrearCargoAbonoSalida {
            public List<MensajeLegacy> P_OUT_MENSAJES;
            public Long P_OUT_ERRORESCONTADOR;
            public List<ObjCargoAbonoGenerado> P_OUT_CARGOABONOGENERADO;

            public CrearCargoAbonoSalida() {
                P_OUT_MENSAJES = new ArrayList<MensajeLegacy>();
                P_OUT_CARGOABONOGENERADO = new ArrayList<ObjCargoAbonoGenerado>();
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ CalcularMontoYCobrosOrden / CrearOrdenes

    public static final class CalcularMontoYCobrosOrden {
        private CalcularMontoYCobrosOrden() {
        }

        public static class Orden_Entrada {
            public BigDecimal PrecioOrden;
            public Long IdInterno;
            public BigDecimal CantidadOrden;
            public String EsFungible;
            public String IdBolsa;
            public Long IdCorredorCorresponsal;
            public Long IdInstrumento;
            public Long IdInstrumentoSubyacente;
            public String IdMonedaLiquidacionOrden;
            public Long IdPortafolio;
            public String IdTipoProducto;
            public String IdTipoOperacion;
            public String IdTipoCanalIngreso;
            public String FechaOperacion;
            public BigDecimal MontoOrden;
            public BigDecimal TasaSimultanea;
            public BigDecimal TasaRFIF;
            public BigDecimal TasaVentaCorta;
            public BigDecimal TipoCambio;
            public BigDecimal ValorRescateOrden;
            public Long PlazoSimultanea;
            public Long PlazoVentaCorta;
            public Long PlazoIF;
            public String EsAPlazo;
            public BigDecimal MontoTotal;
            public String EsPorMonto;
            public BigDecimal TasaReporte;
            public BigDecimal TasaReportado;
            public Long PlazoReporte;
            public Long BaseTasa;
            public String IdEmisor;
        }

        public static class OrdenSalida {
            public Long IdInterno;
            public BigDecimal CantidadOperacion;
        }

        public static class CalcularMontoYCobrosOrden_Entrada {
            public Long p_IdEmpresa;
            public Long p_IdUsuario;
            public String p_IdModulo;
            public List<Orden_Entrada> p_Ordenes = new ArrayList<>();
        }

        public static class CalcularMontoYCobrosOrden_Salida {
            public List<OrdenSalida> p_out_Ordenes = new ArrayList<>();
        }
    }

    public static final class CrearOrdenes {
        private CrearOrdenes() {
        }

        public static class ObjOrdenesEntrada {
            public String NemotecnicoSVS = "";
            public Long IdInterno;
            public String FechaRecepcion, FechaVigencia, FechaEmision, FechaOperacion, IdCondicionDeLiquidacion, IdTipoOperacion, IdTipoVigencia, Glosa;
            public BigDecimal MontoNeto, Cantidad, ValorRescate, PrecioLimite, TipoCambio, TasaVentaCorta, TasaSimultanea, TasaRFIF;
            public String EsPorMonto, IdBoveda, IdFormaCustodia, EsFacturaInmediata, IdTipoPrecio, IdTipoCanalIngreso, IdTipoReajuste, IdEmisor, IdBolsa, EspecificaBolsa;
            public String IdMonedaLiquidacionOrden, IdCondicionDeLiquidacionPlazo, EsVentaCorta, EsOD, EsFungible, IdTipoLibroOrden, IdTipoCanalDistribucion, EsOrdenEspecial;
            public String PagoCobroCuentaInversion, TipoInteres, IdMecanismoCaptacion;
            public Long IdPorcentajeVisibilidad, IdPortafolio, IdInstrumento, IdInstrumentoSubyacente, PlazoVentaCorta, PlazoSimultanea, PlazoIF, IdLibroOrdenes;
            public Long IdCuentaCustodioPortafolio, IdCorredorCorresponsal, IdPersonaAutorizadoDarOrdenes, IdPreferenciaPagoPortafolio, BaseTasa;
        }

        public static class ObjRESULTADO {
            public Long IdInterno;
            public Long IdOrden;
            public String DocumentoIdentidad;
            public String CodigoPortafolio;
            public Timestamp FechaOperacionOrden;
            public BigDecimal Cantidad;
        }

        public static class CrearOrdenesEntrada {
            public Long p_IdEmpresa;
            public Long p_IdUsuario;
            public String p_IdModulo;
            public String p_simularOperacion;
            public String p_UsaReglasNegocio;
            public List<ObjOrdenesEntrada> p_Ordenes = new ArrayList<>();
        }

        public static class CrearOrdenesSalida {
            public List<MensajeLegacy> P_OUT_MENSAJES = new ArrayList<>();
            public Long P_OUT_ERRORESCONTADOR = 0L;
            public List<ObjRESULTADO> P_OUT_RESULTADO = new ArrayList<>();
        }
    }

    // ------------------------------------------------------------------------------------------------ ConsultarInstrumentoPrecioBolsa (Plataforma)

    public static final class ConsultarInstrumentoPrecioBolsa {
        private ConsultarInstrumentoPrecioBolsa() {
        }

        public static class ConsultarInstrumentoPrecioBolsa_Entrada {
            public Long p_IdEmpresa;
            public Long p_IdUsuario;
            public String p_SoloInscritos;
            public List<Instrumento> p_Instrumentos;
            public String p_IncluyeNemoNegociacion;

            public ConsultarInstrumentoPrecioBolsa_Entrada() {
                p_Instrumentos = new ArrayList<>();
                this.p_SoloInscritos = "N";
            }
        }

        public static class Instrumento {
            public Long IdInterno;
            public Long IdEmpresa;
            public Long IdInstrumento;
            public String IdBolsa;
            public String Fecha;
            public String IdMoneda;
            public String IdClasificacionTipoInstrumento;
            public String IdTipoInstrumento;
            public String IdCondicionDeLiquidacion;
            public String Nemotecnico;
        }

        public static class DatosInstrumentosPrecioBolsa_Salida {
            public Long IdInterno;
            public Long IdInstrumento;
            public String IdBolsa;
            public String FechaConsulta;
            public String Fecha;
            public BigDecimal VolumenTransado;
            public BigDecimal MontoTransado;
            public BigDecimal PrecioBase;
            public BigDecimal PrecioCierre;
            public BigDecimal PuntaCompra;
            public BigDecimal PuntaVenta;
            public BigDecimal Variacion;
            public String IdMoneda;
            public BigDecimal PrecioApertura;
            public BigDecimal PrecioMayor;
            public BigDecimal PrecioMenor;
            public BigDecimal PrecioMedio;
            public BigDecimal PrecioMercado;
            public BigDecimal TasaMercado;
            public BigDecimal CantidadPuntaCompra;
            public BigDecimal CantidadPuntaVenta;
        }

        public static class ConsultarInstrumentoPrecioBolsa_Salida {
            public List<MensajeLegacy> P_OUT_MENSAJES;
            public Long P_OUT_ERRORESCONTADOR;
            public List<DatosInstrumentosPrecioBolsa_Salida> P_OUT_INSTRUMENTO;

            public ConsultarInstrumentoPrecioBolsa_Salida() {
                P_OUT_MENSAJES = new ArrayList<>();
                P_OUT_INSTRUMENTO = new ArrayList<>();
                P_OUT_ERRORESCONTADOR = 0L;
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ Fondo Mutuo

    public static final class CalcularMontoNumeroCuota {
        private CalcularMontoNumeroCuota() {
        }

        public static class EntradaCalcularMontoNumeroCuota {
            public Long p_IdEmpresa;
            public List<Serie> p_Series;

            public EntradaCalcularMontoNumeroCuota() {
                this.p_Series = new ArrayList<Serie>();
            }
        }

        public static class Serie {
            public Long IdInterno;
            public String Fecha;
            public String IdSerieFondo;
            public String CalculoPorMontoCuota;
            public BigDecimal Monto;
            public BigDecimal NumeroCuotas;
            public String IdMonedaFondo;
            public String IdMonedaInversion;
            public String TipoSolicitud;
            public String TipoFactorAjuste;
            public Long IdEmpresa;
            public BigDecimal Porcentaje;
            public String IdTipoMovimientoFondoMutuo;

            public Serie() {
                this.IdTipoMovimientoFondoMutuo = "";
            }
        }

        public static class Resultado {
            public Long IdInterno;
            public String TipoSolicitud;
            public BigDecimal Monto;
            public BigDecimal NumeroCuotas;
            public BigDecimal ValorCuota;
            public BigDecimal MontoDestino;
            public BigDecimal ValorParidad;
            public BigDecimal ValorFactorAjuste;
            public BigDecimal CostoEnFactorAjuste;
            public String IdMonedaSistema;
            public BigDecimal MontoMonedaSistema;
            public String Resultado;
            public String CodigoMensaje;
            public BigDecimal Porcentaje;
            public Long DecimalesValorCuota;
            public Long DecimalesNumeroCuotas;
            public String FechaValorCuota;
        }

        public static class SalidaCalcularMontoNumeroCuota {
            public List<Resultado> P_OUT_RESULTADO;

            public SalidaCalcularMontoNumeroCuota() {
                this.P_OUT_RESULTADO = new ArrayList<Resultado>();
            }
        }
    }

    public static final class CrearSolicitudAporteRescateNormal {
        private CrearSolicitudAporteRescateNormal() {
        }

        public static class EntradaCrearSolicitudAporteRescateNormal {
            public Long p_IdEmpresa;
            public Long p_IdUsuario;
            public String p_IdModulo;
            public String p_SimularOperacion;
            public String p_UsaReglasNegocio;
            public String p_IdTipoCanalIngreso;
            public Long p_NumeroDividendo;
            public List<InSolicitudesAporteRescate> p_SolicitudesAporteRescate;
            public List<InFormaPagoSpot> p_FormaPagoSpot;
            public String p_IdOrigenProceso;
            public String p_PermiteOperarProspectos;

            public EntradaCrearSolicitudAporteRescateNormal() {
                p_IdTipoCanalIngreso = "OPT";
                p_SolicitudesAporteRescate = new ArrayList<InSolicitudesAporteRescate>();
                p_FormaPagoSpot = new ArrayList<InFormaPagoSpot>();
                p_NumeroDividendo = 0L;
                p_IdOrigenProceso = "OPT";
                p_PermiteOperarProspectos = "N";
            }
        }

        public static class InSolicitudesAporteRescate {
            public Long IdInterno;
            public Long IdCliente;
            public Long IdPortafolio;
            public String TipoSolicitud;
            public String IdSerieFondo;
            public String CalculoPorMontoCuota;
            public String EsPorMontoLiquido;
            public BigDecimal Monto;
            public BigDecimal NumeroCuotas;
            public String IdMoneda;
            public String IdMedioDePago;
            public Long IdPreferenciaPagoPortafolio;
            public String Visado;
            public String SolicitudBatch;
            public String OrigenOperacion;
            public String FechaOperacion;
            public String IdRegimenTributario;
            public String Glosa;
            public String IndicadorMLT;
            public String IdSerieFondoDestino;
            public String IdEmisorDestino;
            public Long IdAutorizadoOrigen;
            public BigDecimal Porcentaje;
            public Long FolioContrato;
            public String IdMonedaPago;
            public BigDecimal MontoPatrimonio;
            public String IngresoAutomatico;
            public String ForzarSpot;
            public String TipoContratoOperacion;
            public String FolioContratoOperacion;
            public String EsTraspaso;
            public String Canal;
            public Long IdSolicitudAporteRescateRelacionado;
            public Date FechaValorCuota;
            public String IgnorarMontoMinimo;
            public String SimulaCambioPortafolioEjecutadoOk;
            public String IngresoDesde;

            public InSolicitudesAporteRescate() {
                this.IdMonedaPago = "";
                this.IdEmisorDestino = "";
                this.MontoPatrimonio = BigDecimal.ZERO;
                this.IngresoAutomatico = "N";
                this.ForzarSpot = "N";
                this.TipoContratoOperacion = "";
                this.FolioContratoOperacion = "";
                this.EsTraspaso = "N";
                this.Canal = "";
                this.IdSolicitudAporteRescateRelacionado = 0L;
                this.IgnorarMontoMinimo = "N";
                this.SimulaCambioPortafolioEjecutadoOk = "N";
                this.IngresoDesde = "";
            }

            public Long IdOrigenRecaCliente = 0L;
        }

        public static class InFormaPagoSpot {
            public Long IdInternoPagoOperacion;
            public Long IdComprobantePagoOperacion;
            public String IdTipoMovimiento;
            public String IdFormaDePago;
            public String IdMoneda;
            public String IdMonedaLiquidacion;
            public Long IdOperacion;
            public String RecibePagaPortafolio;
            public BigDecimal MontoFormaDePago;
            public String AfectarTesoreria;
        }

        public static class ResultadoSolicitud {
            public Long IdInterno;
            public Long IdSolicitudAporteRescate;
            public Long IdPortafolio;
            public String TipoSolicitud;
            public String IdSerieFondo;
            public String NombreSerieFondo;
            public String CodigoSerie;
            public String IdTipoMovimientoFondoMutuo;
            public String IdSerieFondoDestino;
            public String IdEmisorDestino;
            public String IndicadorMLT;
            public String Overnight;
            public String CalculoPorMontoCuota;
            public String FechaAceptacion;
            public String FechaOriginalInversion;
            public String FechaIngresoRegistro;
            public String EsPorMontoLiquido;
            public BigDecimal Monto;
            public BigDecimal NumeroCuotas;
            public BigDecimal ValorCuota;
            public BigDecimal CostoUF;
            public BigDecimal CostoMonedaLocal;
            public BigDecimal ValorUFFechaInversion;
            public BigDecimal ParidadFechaInversion;
            public String IdMoneda;
            public BigDecimal MontoComision;
            public BigDecimal MontoPago;
            public BigDecimal MontoComisionLocal;
            public BigDecimal MontoComisionMonedaSistema;
            public BigDecimal MontoPagoLocal;
            public String IdFondo;
            public String Nemotecnico;
            public String NombreFondo;
            public String NombreCorto;
            public String IdEmisorOrigen;
            public String DocumentoIdentidad;
            public String CodigoPortafolio;
            public String NombreCliente;
            public String FechaLiquidacion;
            public String FechaCertificacion;
            public BigDecimal MontoTransferencia;
            public BigDecimal NumeroCuotaTransferencia;
            public String FechaValorCuota;
            public BigDecimal MontoLiquido;
            public BigDecimal MontoLiquidoLocal;
            public BigDecimal Porcentaje;
            public Long FolioContrato;
            public BigDecimal ParidadFechaCertificacion;
            public String TipoCarteraNormativa;
            public String IdMonedaSistema;
            public BigDecimal MontoMonedaSistema;
            public BigDecimal MontoAporte;
            public String IdEmisorAporte;
            public BigDecimal NumeroCuotasAporte;
            public String IdSerieFondoOrigen;
            public Long IdAgrupador;
            public String Switch;
            public String IdTipoCanalIngreso;
            public BigDecimal ValorPivoteLocalConversionLocal;
            public BigDecimal ValorPivoteSistemaConversionSistema;
            public Long FolioCartera;
        }

        public static class SalidaCrearSolicitudAporteRescateNormal {
            public Long P_OUT_ERRORESCONTADOR;
            public List<MensajeLegacy> P_OUT_MENSAJES;
            public List<ResultadoSolicitud> P_OUT_RESULTADO;

            public SalidaCrearSolicitudAporteRescateNormal() {
                this.P_OUT_ERRORESCONTADOR = 0L;
                this.P_OUT_MENSAJES = new ArrayList<MensajeLegacy>();
                this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ AgregarLog (Plataforma)

    public static final class AgregarLog {
        private AgregarLog() {
        }

        public static class InAgregarLog {
            public Long p_IdEmpresa;
            public Long p_IdUsuario;
            public String p_IdModulo;
            public String p_UsaReglasNegocio;
            public String p_IdEstadoAutorizacion;
            public Long p_IdTrabajo = 0L;
            public List<InAgregarLog.Log> p_Log;
            public List<InAgregarLog.DetalleLog> p_LogDetalle;

            public InAgregarLog() {
                p_Log = new ArrayList<InAgregarLog.Log>();
                p_LogDetalle = new ArrayList<InAgregarLog.DetalleLog>();
            }

            public void agregarDetalleLogBasico(Long idCabeceraLog, Long idTrabajo, String campoRegistro, String valorAntiguo, String valorNuevo, String subRegistro) {
                InAgregarLog.DetalleLog dl2 = new InAgregarLog.DetalleLog();
                dl2.IdCabeceraLog = idCabeceraLog;
                dl2.IdTrabajo = idTrabajo;
                dl2.CampoRegistro = campoRegistro;
                dl2.ValorAntiguo = valorAntiguo;
                dl2.ValorNuevo = valorNuevo;
                dl2.SubRegistro = subRegistro;
                p_LogDetalle.add(dl2);
            }

            public static class Log {
                public Long IdTrabajo;
                public Long IdCabeceraLog;
                public String IdTipoLog;
                public String IdModulo;
                public Long IdUsuario;
                public Long IdEmpresa;
                public String FechaSistemaLog;
                public Long IdUsuarioAtendido;
                public Long IdCliente;
                public String EstadoCliente;
                public String NombreCliente;
                public String IdTipoDocumentoIdentidad;
                public String IdDocumentoIdentidad;
                public Long IdPortafolio;
                public String EstadoPortafolio;
                public String IdTipoRegistro;
                public String IdRegistro;
                public String EstadoRegistro;
                public String IdTipoOperacion;
                public Long Folio;
                public BigDecimal Cantidad;
                public BigDecimal PrecioTasa;
                public BigDecimal Monto;
                public String IdMonedaOrigen;
                public String IdMonedaLiquidacion;
                public String TipoCambio;
                public String Nemo;
                public Long IdProceso;
                public String EstadoProceso;
                public String NombreProceso;
                public Long IdPersona;

                public Log() {
                    this.IdTrabajo = 0L;
                    this.IdCliente = 0L;
                    this.EstadoCliente = "";
                    this.NombreCliente = "";
                    this.IdTipoDocumentoIdentidad = "";
                    this.IdDocumentoIdentidad = "";
                    this.IdPortafolio = 0L;
                    this.EstadoPortafolio = "";
                    this.IdRegistro = "";
                    this.EstadoRegistro = "";
                    this.IdTipoOperacion = "";
                    this.Folio = 0L;
                    this.Cantidad = BigDecimal.ZERO;
                    this.PrecioTasa = BigDecimal.ZERO;
                    this.Monto = BigDecimal.ZERO;
                    this.IdMonedaOrigen = "";
                    this.IdMonedaLiquidacion = "";
                    this.TipoCambio = "";
                    this.Nemo = "";
                    this.IdProceso = 0L;
                    this.EstadoProceso = "";
                    this.NombreProceso = "";
                    this.IdPersona = 0L;
                }
            }

            public static class DetalleLog {
                public Long IdTrabajo;
                public Long IdCabeceraLog;
                public String SubRegistro;
                public String CampoRegistro;
                public String ValorAntiguo;
                public String ValorNuevo;
            }
        }
    }
}
