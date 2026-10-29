package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CargarContratosMasivo;
import cl.bcs.apv.api.casouso.CargarContratosPAC;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cargarcontratosmasivo.CargarContratosMasivoEntrada;
import cl.bcs.apv.api.dto.cargarcontratosmasivo.CargarContratosMasivoResultado;
import cl.bcs.apv.api.dto.cargarcontratospac.CargaArchivo;
import cl.bcs.apv.api.dto.cargarcontratospac.CargarContratosPACEntrada;
import cl.bcs.apv.api.dto.cargarcontratospac.CargarContratosPACResultado;
import cl.bcs.apv.api.dto.cargarcontratospac.DetalleOrigenTraspaso;
import cl.bcs.apv.api.dto.cargarcontratospac.DistribucionInversion;
import cl.bcs.apv.api.dto.cargarcontratospac.InContratoPAC;
import cl.bcs.apv.api.dto.cargarcontratospac.ParametrosContrato;
import cl.bcs.apv.api.dto.cargarcontratospac.PortafoliosModificados;
import cl.bcs.apv.api.dto.cargarcontratospac.ResultadoCargaMasiva;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code CargarContratosPAC} (family C): loads SAAPV contracts together with their PAC mandates (creating the bank account of the person and
 * entering the mandate in Tesoreria). Legacy {@code cl.bcs.APV.CargaMasivaSAAPVPAC}.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CargarContratosPACServicio implements CargarContratosPAC {

    private static final String SQL_CONSULTAR_CONTRATOSAAPV = SqlLegacy.recurso("CargaMasivaSAAPVPAC_ConsultarContratoSAAPV");
    private static final String SQL_CONSULTAR_CUENTA_BANCARIA = SqlLegacy.recurso("CargaMasivaSAAPVPAC_ConsultarCuentaBancaria");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SqlLegacy sql;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;
    private final CargarContratosMasivo cargarContratosMasivo;

    public CargarContratosPACServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores, CargarContratosMasivo cargarContratosMasivo) {
        this.sql = new SqlLegacy(jdbc);
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
        this.cargarContratosMasivo = cargarContratosMasivo;
    }

    // ---------------------------------------------------------------------------------------------- legacy row classes

    public static class DatosContratoSAAPV {
        public Long IdInterno;
        public Long IdContratoSAAPV;
        public String FolioContratoSAAPV;
        public Long IdPersona;
        public Long IdEmpleador;
        public Long IdCliente;
        public Long IdPortafolio;
        public String FolioPAC;
    }

    public static class DatosCuentaBancaria {
        public Long IdInterno;
        public Long IdPersona;
        public Long IdCliente;
        public Long IdBanco;
        public String CodigoBanco;
        public Long IdCuentaBancaria;
        public String NumeroCuentaBancaria;
        public String FolioPAC;
        public String FolioSAAPV;
    }

    /** Mutable result line (legacy {@code ResultadoCargaMasiva}); the typed record is built at the end. */
    private static final class ResultadoFila {
        Long IdInterno;
        Long IdCargaMasiva;
        String FolioSAAPVPAC;
        Long IdContratoSAAPV;
        String FolioSAAPV;
        Long IdMandatoPAC;
        String FolioPAC;
    }

    // ---- contracts of the collaborators outside APV (legacy POJOs, field names are the JSON contract)

    /** Legacy {@code CrearPersonaCuentaBancaria.InCuentaBancaria}. */
    public static class InCuentaBancaria {
        public Long IdInterno;
        public Long IdBanco;
        public String IdTipoCuentaBancaria;
        public String IdMoneda;
        public String NumeroCuentaBancaria;
        public Long IdPersona;
        public String IdEstado;
        public Long IdPlaza;
        public String CrearPreferenciaPago = "N";
        public Long IdCliente;
        public String EsPac = "N";
        public String Predeterminado = "N";
        public String IdTipoIdentificadorCuenta;
        public String IdentificadorCuenta;
        public Long IdPersonaRelacionada;
        public String CodigoCuentaInterbancario;
        public String CodigoAba;
        public String OtroCodigoBancoExtranjero;
        public String CodigoIban;
        public String OtroCodigoCuentaExtranjero;
    }

    /** Legacy {@code CrearPersonaCuentaBancaria.EntradaCrearPersonaCuentaBancaria}. */
    public static class EntradaCrearPersonaCuentaBancaria {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public List<InCuentaBancaria> p_InCuentaBancaria = new ArrayList<>();
    }

    /** Legacy {@code CrearPersonaCuentaBancaria.OutCuentaBancaria}. */
    public static class OutCuentaBancaria {
        public Long IdInterno;
        public Long IdCuentaBancaria;
        public String IdMoneda;
        public String NumeroCuentaBancaria;
        public Long IdPersona;
        public String IdEstado;
        public String IdEstadoAutorizacion;
        public Long IdUsuarioCreador;
        public String CrearPreferenciaPago;
        public Long IdCliente;
    }

    /** Legacy {@code CrearPersonaCuentaBancaria.SalidaCrearPersonaCuentaBancaria}. */
    public static class SalidaCrearPersonaCuentaBancaria {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeLegacy> P_OUT_MENSAJES = new ArrayList<>();
        public List<Long> P_OUT_REFID = new ArrayList<>();
        public List<OutCuentaBancaria> P_OUT_CUENTABANCARIA = new ArrayList<>();
    }

    /** Legacy {@code IngresarMandatoPAC.InIngresoPAC}. */
    public static class InIngresoPAC {
        public Long IdCliente = 0L;
        public Long IdPortafolio;
        public String FolioCSC;
        public String CuentaCorriente;
        public String FolioSAAPV;
        public Long FolioPACMandatoAgrupador;
        public String Receptor;
        public java.util.Date Fecha;
        public Long NumeroMandato;
        public String Firma;
        public String IdentServicio;
        public String Estado;
        public String EstadoCargo;
        public Long IdEmpresa;
        public Long IdCuentaBancaria;
        public String Canal;
        public String TipoPago;
        public String DocumentoIdentidad;
        public Long DiaDescuento;
        public String EstadoContrato;
    }

    /** Legacy {@code IngresarMandatoPAC.EntradaIngresoMandatoPAC}. */
    public static class EntradaIngresoMandatoPAC {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public List<InIngresoPAC> p_IngresoPAC = new ArrayList<>();
    }

    /** Legacy {@code IngresarMandatoPAC.ResultadoIngresoPAC}. */
    public static class ResultadoIngresoPAC {
        public Long IdMandatoPAC;
    }

    /** Legacy {@code IngresarMandatoPAC.SalidaCrearIngresoPAC}. */
    public static class SalidaCrearIngresoPAC {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeLegacy> P_OUT_MENSAJES = new ArrayList<>();
        public List<ResultadoIngresoPAC> P_OUT_RESULTADO = new ArrayList<>();
    }

    /** Legacy output object. */
    private static final class Salida {
        final List<MensajeLegacy> mensajes = new ArrayList<>();
        Long contador = 0L;
        List<Long> idsCargaMasiva = new ArrayList<>();
        final List<ResultadoFila> resultados = new ArrayList<>();
    }

    @Override
    public CargarContratosPACResultado ejecutar(CargarContratosPACEntrada in) {
        try {
            Salida out = logica(in);
            // legacy call(): the counter is recomputed from the messages
            out.contador = MensajeLegacy.contarErrores(out.mensajes);
            List<ResultadoCargaMasiva> resultados = new ArrayList<>();
            for (ResultadoFila r : out.resultados) {
                resultados.add(new ResultadoCargaMasiva(r.IdInterno, r.IdCargaMasiva, r.FolioSAAPVPAC, r.IdContratoSAAPV, r.FolioSAAPV, r.IdMandatoPAC,
                        r.FolioPAC));
            }
            return new CargarContratosPACResultado(MensajeLegacy.aMensajes(out.mensajes), out.contador, out.idsCargaMasiva, resultados);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    /** Legacy {@code Fecha.toDate}: a 19 character text is {@code yyyy-MM-dd HH:mm:ss}, anything else {@code yyyy-MM-dd}. */
    private static java.sql.Date aFecha(String date) {
        return date.length() == 19 ? java.sql.Date.valueOf(LocalDate.parse(date, FORMATO_FECHA_HORA)) : java.sql.Date.valueOf(LocalDate.parse(date, FORMATO_FECHA));
    }

    private Salida logica(CargarContratosPACEntrada in) throws Exception {
        Salida out = new Salida();
        MensajeLegacy msg;
        out.contador = 0L;

        // the legacy shares these list objects with the called use case; the typed use case gets copies, so the lists are local
        List<CargaArchivo> archivo = new ArrayList<>(in.archivo());
        List<ParametrosContrato> parametrosContratos = new ArrayList<>(in.parametrosContratos());
        List<DistribucionInversion> distribucionInversion = new ArrayList<>(in.distribucionInversion());
        List<PortafoliosModificados> portafoliosModificados = new ArrayList<>(in.portafoliosModificados());
        List<DetalleOrigenTraspaso> detalleOrigenTraspaso = new ArrayList<>(in.detalleOrigenTraspaso());
        String fechaCargaPrimero = archivo.isEmpty() ? null : archivo.get(0).fechaCarga();

        try {
            if (archivo.size() == 0) {
                msg = new MensajeLegacy(1L, MensajeLegacy.ERROR, "CARGA_CONTRATOS_ERROR", "Carga archivo sin datos.");
                out.mensajes.add(msg);
                return out;
            }

            /***** Variables *****/
            String monedaSistema = Plataforma.parametro(configuracion, in.idEmpresa(), "PLATAFORMA", "PLA.MonedaSistema");

            HashMap<String, Object> params = new HashMap<>();
            List<Long> folioContratoSAAPVList = new ArrayList<Long>();
            List<Long> idPersonaList = new ArrayList<Long>();
            List<String> numeroCuentaBancariaList = new ArrayList<String>();
            List<String> codigoBancoList = new ArrayList<String>();

            for (InContratoPAC inContratoPAC : in.inContratoPAC()) {
                if (!folioContratoSAAPVList.contains(inContratoPAC.folioSAAPV())) {
                    folioContratoSAAPVList.add(inContratoPAC.folioSAAPV());
                }
                if (!numeroCuentaBancariaList.contains(inContratoPAC.numeroCuentaBancaria())) {
                    numeroCuentaBancariaList.add(inContratoPAC.numeroCuentaBancaria());
                }
                if (!codigoBancoList.contains(inContratoPAC.codigoBanco())) {
                    codigoBancoList.add(inContratoPAC.codigoBanco());
                }

                // Completa resultado salida
                ResultadoFila resultadoCargaMasiva = new ResultadoFila();
                resultadoCargaMasiva.IdInterno = inContratoPAC.idInterno();
                resultadoCargaMasiva.FolioSAAPVPAC = inContratoPAC.folioSAAPV() + "_" + inContratoPAC.folioPAC();
                resultadoCargaMasiva.FolioSAAPV = inContratoPAC.folioSAAPV().toString();
                resultadoCargaMasiva.FolioPAC = inContratoPAC.folioPAC().toString();
                out.resultados.add(resultadoCargaMasiva);
            }

            /***** Validacion Contratos SAAPV *****/
            params.put("IdEmpresa", in.idEmpresa());
            params.put("FolioContratoSAAPV", folioContratoSAAPVList);
            List<DatosContratoSAAPV> contratoSAAPVExisten = sql.consultar(SQL_CONSULTAR_CONTRATOSAAPV, params, DatosContratoSAAPV.class);

            // Para los contratos SAAPV que existen, setea IdInterno que debe remover
            for (DatosContratoSAAPV contratoExiste : contratoSAAPVExisten) {
                for (ResultadoFila resultadoCargaMasiva : out.resultados) {
                    if (contratoExiste.FolioContratoSAAPV.equals(resultadoCargaMasiva.FolioSAAPV)) {
                        contratoExiste.IdInterno = resultadoCargaMasiva.IdInterno;

                        // LEGACY DEFECT kept (see README APV-C-202): an existing contract is an ADVERTENCIA that also increments the error counter
                        out.contador++;
                        MensajeLegacy msj = new MensajeLegacy();
                        msj.id = Long.parseLong(resultadoCargaMasiva.FolioSAAPV);
                        msj.tipo = MensajeLegacy.ADVERTENCIA;
                        msj.codigo = "FOLIO_CONTRATO_EXISTE";
                        msj.descripcion = "Salida SAAPV: " + "El Folio Contrato SAAPV ya existe.";
                        out.mensajes.add(msj);

                        // Completa resultado salida
                        resultadoCargaMasiva.IdContratoSAAPV = contratoExiste.IdContratoSAAPV;
                    }
                }
            }

            // Continua solo con los contratos que no existen
            for (DatosContratoSAAPV contratoExiste : contratoSAAPVExisten) {
                // LEGACY DEFECT kept (see README APV-C-203): IdInterno is unboxed per element: a null IdInterno throws NullPointerException
                parametrosContratos.removeIf(opeIn -> opeIn.idInterno().longValue() == contratoExiste.IdInterno);
                distribucionInversion.removeIf(opeIn -> opeIn.idInterno().longValue() == contratoExiste.IdInterno);
                portafoliosModificados.removeIf(opeIn -> opeIn.idInterno().longValue() == contratoExiste.IdInterno);
                detalleOrigenTraspaso.removeIf(opeIn -> opeIn.idInterno().longValue() == contratoExiste.IdInterno);
            }
            folioContratoSAAPVList = new ArrayList<Long>();
            for (ParametrosContrato parametrosContrato : parametrosContratos) {
                folioContratoSAAPVList.add(Long.parseLong(parametrosContrato.folioContratoSAAPV()));
            }

            /***** Carga SAAPV *****/
            CargarContratosMasivoResultado cargaSaapvOut = new CargarContratosMasivoResultado(new ArrayList<>(), null, new ArrayList<>());
            List<MensajeLegacy> mensajesCargaSaapv = new ArrayList<>();
            List<DatosContratoSAAPV> contratoSAAPVCreados = new ArrayList<DatosContratoSAAPV>();

            if (parametrosContratos.size() > 0) {
                CargarContratosMasivoEntrada cargaSaapvIn = new CargarContratosMasivoEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(),
                        RecordsCopia.copiar(archivo, cl.bcs.apv.api.dto.cargarcontratosmasivo.CargaArchivo.class),
                        RecordsCopia.copiar(parametrosContratos, cl.bcs.apv.api.dto.cargarcontratosmasivo.ParametrosContrato.class),
                        RecordsCopia.copiar(distribucionInversion, cl.bcs.apv.api.dto.cargarcontratosmasivo.DistribucionInversion.class),
                        RecordsCopia.copiar(portafoliosModificados, cl.bcs.apv.api.dto.cargarcontratosmasivo.PortafoliosModificados.class),
                        RecordsCopia.copiar(detalleOrigenTraspaso, cl.bcs.apv.api.dto.cargarcontratosmasivo.DetalleOrigenTraspaso.class));
                cargaSaapvOut = cargarContratosMasivo.ejecutar(cargaSaapvIn);
                // LEGACY (shared state): the called use case writes the business date into the first file row, which this class reads later
                fechaCargaPrimero = Plataforma.parametro(configuracion, in.idEmpresa(), "PLATAFORMA", "PLA.FechaSistema");
                for (Mensaje m : cargaSaapvOut.mensajes()) {
                    mensajesCargaSaapv.add(MensajeLegacy.de(m));
                }

                out.idsCargaMasiva = new ArrayList<>(cargaSaapvOut.idsCargaMasiva());
                if (MensajeLegacy.contarErrores(mensajesCargaSaapv) > 0) {
                    out.contador = out.contador + MensajeLegacy.contarErrores(mensajesCargaSaapv);
                }

                // Obtiene datos de los Contratos SAAPV creados
                params.put("IdEmpresa", in.idEmpresa());
                params.put("FolioContratoSAAPV", folioContratoSAAPVList);
                contratoSAAPVCreados = sql.consultar(SQL_CONSULTAR_CONTRATOSAAPV, params, DatosContratoSAAPV.class);

                for (DatosContratoSAAPV contratoCreado : contratoSAAPVCreados) {
                    if (!idPersonaList.contains(contratoCreado.IdPersona)) {
                        idPersonaList.add(contratoCreado.IdPersona);
                    }

                    // Completa resultado salida
                    for (ResultadoFila resultadoCargaMasiva : out.resultados) {
                        if (contratoCreado.FolioContratoSAAPV.equals(resultadoCargaMasiva.FolioSAAPV)) {
                            resultadoCargaMasiva.IdContratoSAAPV = contratoCreado.IdContratoSAAPV;

                            contratoCreado.IdInterno = resultadoCargaMasiva.IdInterno;
                            contratoCreado.FolioPAC = resultadoCargaMasiva.FolioPAC;
                        }
                    }

                    // Ajuste de id y descripcion de Mensaje Salida
                    // LEGACY DEFECT kept (see README APV-C-205): the prefix is added once PER CREATED CONTRACT to every message of the load
                    for (MensajeLegacy mensajeSalida : mensajesCargaSaapv) {
                        mensajeSalida.descripcion = "Salida SAAPV: " + mensajeSalida.descripcion;
                        if (contratoCreado.IdContratoSAAPV.longValue() == mensajeSalida.id.longValue()) {
                            mensajeSalida.id = Long.parseLong(contratoCreado.FolioContratoSAAPV);
                        }
                    }
                }

                out.mensajes.addAll(mensajesCargaSaapv);

                // Completa resultado salida
                for (ResultadoFila resultadoCargaMasiva : out.resultados) {
                    resultadoCargaMasiva.IdCargaMasiva = cargaSaapvOut.idsCargaMasiva().get(0);
                }
            }

            /***** Crear Cuenta Bancaria (SocioNegocio) *****/
            List<InCuentaBancaria> inCuentaBancariaList = new ArrayList<InCuentaBancaria>();

            params = new HashMap<>();
            params.put("IdEmpresa", in.idEmpresa());
            params.put("IdPersona", idPersonaList);
            params.put("NumeroCuentaBancaria", numeroCuentaBancariaList);
            params.put("CodigoBanco", codigoBancoList);
            List<DatosCuentaBancaria> datosCuentaBancariaList = sql.consultar(SQL_CONSULTAR_CUENTA_BANCARIA, params, DatosCuentaBancaria.class);

            // Arma las Cuentas Bancarias que debe validar
            List<DatosCuentaBancaria> datosCuentaBancariaValidacion = new ArrayList<DatosCuentaBancaria>();
            for (DatosContratoSAAPV contratoSAAPV : contratoSAAPVCreados) {
                for (InContratoPAC inContratoPAC : in.inContratoPAC()) {
                    if (contratoSAAPV.IdInterno.longValue() == inContratoPAC.idInterno().longValue()) {
                        DatosCuentaBancaria cuentaBancaria = new DatosCuentaBancaria();
                        cuentaBancaria.IdInterno = contratoSAAPV.IdInterno;
                        cuentaBancaria.IdPersona = contratoSAAPV.IdPersona;
                        cuentaBancaria.IdCliente = contratoSAAPV.IdCliente;
                        cuentaBancaria.IdBanco = 0L;
                        cuentaBancaria.CodigoBanco = inContratoPAC.codigoBanco();
                        cuentaBancaria.IdCuentaBancaria = 0L;
                        cuentaBancaria.NumeroCuentaBancaria = inContratoPAC.numeroCuentaBancaria();
                        cuentaBancaria.FolioPAC = contratoSAAPV.FolioPAC;
                        cuentaBancaria.FolioSAAPV = contratoSAAPV.FolioContratoSAAPV;

                        for (DatosCuentaBancaria datosCuentaBancaria : datosCuentaBancariaList) {
                            if (contratoSAAPV.IdPersona.longValue() == datosCuentaBancaria.IdPersona.longValue()
                                    && inContratoPAC.codigoBanco().equals(datosCuentaBancaria.CodigoBanco)) {
                                datosCuentaBancaria.IdCliente = contratoSAAPV.IdCliente;
                                datosCuentaBancaria.IdInterno = contratoSAAPV.IdInterno;
                                datosCuentaBancaria.FolioPAC = contratoSAAPV.FolioPAC;
                                datosCuentaBancaria.FolioSAAPV = contratoSAAPV.FolioContratoSAAPV;

                                cuentaBancaria.IdBanco = datosCuentaBancaria.IdBanco;
                                cuentaBancaria.IdCuentaBancaria = datosCuentaBancaria.IdCuentaBancaria;
                            }
                        }

                        datosCuentaBancariaValidacion.add(cuentaBancaria);
                    }
                }
            }

            // Verifica las Cuentas Bancarias con error y las que debe crear
            for (DatosCuentaBancaria datosCuentaBancaria : datosCuentaBancariaValidacion) {
                // Valida Codigo Banco existe. Si no existe, registro queda marcado como ERROR
                if (datosCuentaBancaria.IdBanco.longValue() == 0) {
                    out.contador++;
                    MensajeLegacy msj = Plataforma.mensaje(diccionario, in.idEmpresa(), Long.parseLong(datosCuentaBancaria.FolioSAAPV), MensajeLegacy.ERROR,
                            "SIN_CONFIGURAR_INFORMACION_BANCO", new HashMap<String, String>());
                    msj.descripcion = "Cuenta Bancaria: " + msj.descripcion;
                    out.mensajes.add(msj);
                    // LEGACY DEFECT kept (see README APV-C-207): the bank-code check cuts the loop with break
                    break;
                }

                // Datos de la Cuenta Bancaria a crear
                if (datosCuentaBancaria.IdCuentaBancaria.longValue() == 0) {
                    InCuentaBancaria inCuentaBancaria = new InCuentaBancaria();
                    inCuentaBancaria.IdInterno = Long.parseLong(datosCuentaBancaria.FolioPAC);
                    inCuentaBancaria.IdBanco = datosCuentaBancaria.IdBanco;
                    inCuentaBancaria.IdTipoCuentaBancaria = "CUCO";
                    inCuentaBancaria.IdMoneda = monedaSistema;
                    inCuentaBancaria.NumeroCuentaBancaria = datosCuentaBancaria.NumeroCuentaBancaria;
                    inCuentaBancaria.IdPersona = datosCuentaBancaria.IdPersona;
                    inCuentaBancaria.IdEstado = "ACT";
                    inCuentaBancaria.IdPlaza = 0L;
                    inCuentaBancaria.CrearPreferenciaPago = "N";
                    inCuentaBancaria.IdCliente = datosCuentaBancaria.IdCliente;
                    inCuentaBancaria.EsPac = "N";
                    inCuentaBancaria.IdTipoIdentificadorCuenta = "";
                    inCuentaBancaria.IdentificadorCuenta = "0";
                    inCuentaBancaria.IdPersonaRelacionada = 0L;
                    inCuentaBancaria.CodigoCuentaInterbancario = "";
                    inCuentaBancaria.CodigoAba = "";
                    inCuentaBancaria.OtroCodigoBancoExtranjero = "";
                    inCuentaBancaria.CodigoIban = "";
                    inCuentaBancaria.OtroCodigoCuentaExtranjero = "";
                    inCuentaBancariaList.add(inCuentaBancaria);
                }
            }

            if (inCuentaBancariaList.size() > 0) {
                EntradaCrearPersonaCuentaBancaria crearCuentaBancariaEntrada = new EntradaCrearPersonaCuentaBancaria();
                crearCuentaBancariaEntrada.p_IdEmpresa = in.idEmpresa();
                crearCuentaBancariaEntrada.p_IdUsuario = in.idUsuario();
                crearCuentaBancariaEntrada.p_IdModulo = "SOC";
                crearCuentaBancariaEntrada.p_UsaReglasNegocio = "S";
                crearCuentaBancariaEntrada.p_InCuentaBancaria = inCuentaBancariaList;
                SalidaCrearPersonaCuentaBancaria crearCuentaBancariaSalida = Colab.de(
                        colaboradores.invocar("CrearPersonaCuentaBancaria", Colab.aJson(crearCuentaBancariaEntrada)), SalidaCrearPersonaCuentaBancaria.class);

                for (DatosCuentaBancaria datosCuentaBancaria : datosCuentaBancariaValidacion) {
                    for (OutCuentaBancaria outCuentaBancaria : crearCuentaBancariaSalida.P_OUT_CUENTABANCARIA) {
                        if (datosCuentaBancaria.FolioPAC.equals(outCuentaBancaria.IdInterno.toString())) {
                            datosCuentaBancaria.IdCuentaBancaria = outCuentaBancaria.IdCuentaBancaria;
                        }
                    }
                }

                for (MensajeLegacy mensaje : crearCuentaBancariaSalida.P_OUT_MENSAJES) {
                    mensaje.descripcion = "Crear Cuenta Bancaria: " + mensaje.descripcion;
                    out.mensajes.add(mensaje);
                }
            }

            // Utils.List2Hash(datosCuentaBancariaValidacion, "FolioSAAPV"): throws when the key is not unique
            HashMap<String, DatosCuentaBancaria> datosCuentaBancariaHash = new HashMap<>();
            for (DatosCuentaBancaria d : datosCuentaBancariaValidacion) {
                if (datosCuentaBancariaHash.containsKey(d.FolioSAAPV)) {
                    throw new Exception("key field is not unique");
                }
                datosCuentaBancariaHash.put(d.FolioSAAPV, d);
            }

            /***** Ingreso Mandato PAC (Tesoreria) *****/
            List<InIngresoPAC> inIngresoPACList = new ArrayList<InIngresoPAC>();
            for (DatosContratoSAAPV contratoSAAPV : contratoSAAPVCreados) {
                for (InContratoPAC inContratoPAC : in.inContratoPAC()) {
                    if (contratoSAAPV.FolioContratoSAAPV.equals(inContratoPAC.folioSAAPV().toString())) {
                        InIngresoPAC inIngresoPAC = new InIngresoPAC();
                        inIngresoPAC.IdCliente = contratoSAAPV.IdCliente;
                        inIngresoPAC.IdPortafolio = contratoSAAPV.IdPortafolio;
                        inIngresoPAC.FolioCSC = "0";
                        inIngresoPAC.CuentaCorriente = inContratoPAC.numeroCuentaBancaria();
                        inIngresoPAC.FolioSAAPV = contratoSAAPV.FolioContratoSAAPV;
                        inIngresoPAC.FolioPACMandatoAgrupador = 1L;
                        inIngresoPAC.Receptor = inContratoPAC.receptor();
                        inIngresoPAC.Fecha = aFecha(fechaCargaPrimero);
                        inIngresoPAC.NumeroMandato = inContratoPAC.numeroMandato();
                        inIngresoPAC.Firma = inContratoPAC.firma();
                        inIngresoPAC.IdentServicio = inContratoPAC.identServicio();
                        inIngresoPAC.IdEmpresa = in.idEmpresa();
                        inIngresoPAC.IdCuentaBancaria = datosCuentaBancariaHash.get(inContratoPAC.folioSAAPV().toString()) == null ? 0L
                                : datosCuentaBancariaHash.get(inContratoPAC.folioSAAPV().toString()).IdCuentaBancaria;
                        inIngresoPAC.Canal = "OPTIMUS";
                        inIngresoPAC.TipoPago = "N";
                        inIngresoPACList.add(inIngresoPAC);
                    }
                }
            }

            if (inIngresoPACList.size() > 0) {
                EntradaIngresoMandatoPAC entradaIngresoMandatoPAC = new EntradaIngresoMandatoPAC();
                entradaIngresoMandatoPAC.p_IdEmpresa = in.idEmpresa();
                entradaIngresoMandatoPAC.p_IdUsuario = in.idUsuario();
                entradaIngresoMandatoPAC.p_IdModulo = "TES";
                entradaIngresoMandatoPAC.p_SimularOperacion = "N";
                entradaIngresoMandatoPAC.p_UsaReglasNegocio = "S";
                entradaIngresoMandatoPAC.p_IngresoPAC = inIngresoPACList;
                SalidaCrearIngresoPAC salidaCrearIngresoPAC = Colab.de(colaboradores.invocar("IngresarMandatoPAC", Colab.aJson(entradaIngresoMandatoPAC)),
                        SalidaCrearIngresoPAC.class);

                // Completa resultado salida
                // LEGACY DEFECT kept (see README APV-C-205): IdMandatoPAC of the LAST mandate result is written to ALL results
                for (ResultadoIngresoPAC resultadoPAC : salidaCrearIngresoPAC.P_OUT_RESULTADO) {
                    for (ResultadoFila resultadoCargaMasiva : out.resultados) {
                        resultadoCargaMasiva.IdMandatoPAC = resultadoPAC.IdMandatoPAC;
                    }
                }

                out.mensajes.addAll(salidaCrearIngresoPAC.P_OUT_MENSAJES);
                if (MensajeLegacy.contarErrores(salidaCrearIngresoPAC.P_OUT_MENSAJES) > 0) {
                    out.contador = out.contador + MensajeLegacy.contarErrores(salidaCrearIngresoPAC.P_OUT_MENSAJES);
                }
            }
        } catch (Exception error) {
            // the messages are lost: the legacy rethrows
            msg = new MensajeLegacy(1L, MensajeLegacy.ERROR, "CARGA_CONTRATOS_ERROR", Sneaky.causa(error).getMessage());
            out.mensajes.add(msg);
            out.contador = (long) out.mensajes.size();
            throw (Exception) Sneaky.causa(error);
        }

        return out;
    }
}
