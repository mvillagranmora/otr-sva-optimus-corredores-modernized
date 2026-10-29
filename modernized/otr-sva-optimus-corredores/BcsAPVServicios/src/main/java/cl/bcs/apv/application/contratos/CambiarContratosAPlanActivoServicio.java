package cl.bcs.apv.application.contratos;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.CambiarContratosAPlanActivo;
import cl.bcs.apv.api.casouso.ModificarContratos;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cambiarcontratosaplanactivo.CambiarContratosAPlanActivoEntrada;
import cl.bcs.apv.api.dto.cambiarcontratosaplanactivo.CambiarContratosAPlanActivoResultado;
import cl.bcs.apv.api.dto.cambiarcontratosaplanactivo.ContratoCambioPlanIn;
import cl.bcs.apv.api.dto.modificarcontratos.DetalleOrigenTraspaso;
import cl.bcs.apv.api.dto.modificarcontratos.DistribucionInversion;
import cl.bcs.apv.api.dto.modificarcontratos.ModificarContratosEntrada;
import cl.bcs.apv.api.dto.modificarcontratos.ModificarContratosResultado;
import cl.bcs.apv.api.dto.modificarcontratos.ParametrosContrato;
import cl.bcs.apv.api.dto.modificarcontratos.PortafoliosModificados;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.contratos.CambioPlanActivoRepositorio;
import cl.bcs.apv.domain.port.out.contratos.CambioPlanActivoRepositorio.DatosContratoSAAPV;
import cl.bcs.apv.domain.port.out.contratos.CambioPlanActivoRepositorio.DatosDetalleOrigenTraspaso;
import cl.bcs.apv.domain.port.out.contratos.CambioPlanActivoRepositorio.DatosDistribucionInversion;
import cl.bcs.apv.domain.port.out.contratos.CambioPlanActivoRepositorio.DatosPortafoliosModificados;

/**
 * Use case {@code CambiarContratosAPlanActivo}: legacy {@code cl.bcs.APV.CambiarAPlanActivoContratoSAAPV}. It reads the VIG contracts of plan 1, builds the
 * modification of each one as plan 2 (typed peer {@link ModificarContratos}, the legacy called {@code ModificarContratoSAAPV.logica}), updates the commercial
 * profile of the portfolios through the collaborator {@code ModificarPortafolio} and moves the portfolios to the active-plan matrix.
 */
@Transactional(rollbackFor = Exception.class)
public class CambiarContratosAPlanActivoServicio implements CambiarContratosAPlanActivo {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;
    private final ModificarContratos modificarContratos;
    private final CambioPlanActivoRepositorio repositorio;

    public CambiarContratosAPlanActivoServicio(ConfiguracionSistema configuracion, DiccionarioMensajes diccionario, Colaboradores colaboradores,
            ModificarContratos modificarContratos, CambioPlanActivoRepositorio repositorio) {
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
        this.modificarContratos = modificarContratos;
        this.repositorio = repositorio;
    }

    @Override
    public CambiarContratosAPlanActivoResultado ejecutar(CambiarContratosAPlanActivoEntrada entrada) {
        // legacy call(): the counter is recomputed from the messages after logica, whatever logica left in it
        List<Mensaje> mensajes = logica(entrada);
        return new CambiarContratosAPlanActivoResultado(mensajes, Mensajes.numeroErrores(mensajes));
    }

    private List<Mensaje> logica(CambiarContratosAPlanActivoEntrada in) {
        List<Mensaje> out = new ArrayList<>();
        out.add(Mensajes.conDiccionario(diccionario, in.idEmpresa(), 1L, Mensajes.MENSAJE_ADVERTENCIA, "ACTUALIZAR_MODELOS_COMERCIALES",
                new HashMap<String, String>()));
        // Validacion entrada al servicio
        if (in.contratoCambioPlanIn().size() == 0) {
            out.add(Mensajes.de(0L, Mensajes.MENSAJE_ERROR, "NO_SE_INGRESARON_CONTRATOS", "NO_SE_INGRESARON_CONTRATOS"));
            return out;
        }

        // Filtra IdContratoSAAPV repetidos en la entrada
        List<Long> idsContrato = new ArrayList<>();
        List<Long> lstPortafolios = new ArrayList<>();
        HashMap<String, ContratoCambioPlanIn> contratoHash = new HashMap<>();
        for (ContratoCambioPlanIn contratoIn : in.contratoCambioPlanIn()) {
            lstPortafolios.add(contratoIn.idPortafolio());
            String keyContrato = in.idEmpresa() + "_" + contratoIn.idContratoSAAPV();
            if (!contratoHash.containsKey(keyContrato)) {
                contratoHash.put(keyContrato, contratoIn);
                idsContrato.add(contratoIn.idContratoSAAPV());
            }
        }

        if (idsContrato.size() > 0) {
            // Parametros Contrato SAAPV
            String autorizaGestionContrato = Parametros.texto(configuracion, in.idEmpresa(), Parametros.PLATAFORMA, "APV.AutorizaGestionContrato");

            List<DatosContratoSAAPV> datosContratoSAAPVList = repositorio.consultarParametrosContrato(in.idEmpresa(), 1, idsContrato);
            List<ParametrosContrato> parametrosContratoList = new ArrayList<>();
            for (DatosContratoSAAPV contrato : datosContratoSAAPVList) {
                parametrosContratoList.add(parametrosContrato(contrato, autorizaGestionContrato));
            }

            // Utils.List2Hash(datos, "IdContratoSAAPV"): the legacy refused a repeated key
            HashMap<Long, DatosContratoSAAPV> datosContratoSAAPVHash = new HashMap<>();
            for (DatosContratoSAAPV d : datosContratoSAAPVList) {
                if (datosContratoSAAPVHash.containsKey(d.IdContratoSAAPV())) {
                    throw Sneaky.lanzar(new Exception("key field is not unique"));
                }
                datosContratoSAAPVHash.put(d.IdContratoSAAPV(), d);
            }

            // Distribucion Inversion
            List<DistribucionInversion> distribucionInversionList = new ArrayList<>();
            for (DatosDistribucionInversion d : repositorio.consultarDistribucionInversion(in.idEmpresa(), 1, idsContrato)) {
                distribucionInversionList.add(new DistribucionInversion(d.IdContratoSAAPV(), d.FolioContratoSAAPV(), d.IdInstrumento(), d.Nemotecnico(),
                        d.MontoPesos().doubleValue(), d.Porcentaje().doubleValue(), d.IdEstado()));
            }

            // Portafolios Modificados
            List<DatosPortafoliosModificados> datosPortafoliosModificadosList = repositorio.consultarPortafoliosModificados(in.idEmpresa(), 1, idsContrato);
            List<PortafoliosModificados> portafoliosModificadosList = new ArrayList<>();
            for (DatosPortafoliosModificados d : datosPortafoliosModificadosList) {
                String pago = d.Pago();
                portafoliosModificadosList.add(new PortafoliosModificados(d.CodigoAgrupador(), d.IdMatrizAgrupadorAPV(), d.OrigenAhorro(), d.IdPortafolio(),
                        d.IdPropositoPortafolio(), d.PeriodoInicio(), d.PeriodoTermino(), d.MontoPesos().doubleValue(), d.MontoUF().doubleValue(), pago,
                        "IND".equals(pago) ? "INDEFINIDO" : "TEM".equals(pago) ? "TEMPORAL" : "UNI".equals(pago) ? "UNICO" : "",
                        0d /* Porcentaje: the legacy never copied PorcentajeRegimenImponible (line commented out) */, d.NombreProductoAgrupador(),
                        d.Codigo(), d.IdContratoSAAPV()));
            }

            // Detalle Origen Traspaso
            List<DetalleOrigenTraspaso> detalleOrigenTraspasoList = new ArrayList<>();
            for (DatosDetalleOrigenTraspaso d : repositorio.consultarDetalleOrigenTraspaso(in.idEmpresa(), 1, idsContrato)) {
                detalleOrigenTraspasoList.add(new DetalleOrigenTraspaso(0L, 0L, d.CodigoTipoAhorro(), d.IdEntidadRecaudadora(), d.NombreEntidad(),
                        d.DocumentoEntidad(), d.Fondo(), d.MontoTraspaso().doubleValue(), d.UFTraspaso().doubleValue(), d.PorcentajeTraspaso().doubleValue(),
                        d.IdEstado(), d.IdInstrumento(), d.CodigoRegimenTributario(), d.IdPortafolio(), d.IdContratoSAAPV()));
            }

            // Crea los nuevos Contratos SAAPV con Plan Activo
            ModificarContratosResultado parametrosSalida = modificarContratos.ejecutar(new ModificarContratosEntrada(in.idEmpresa(), in.idUsuario(), "APV", "M",
                    parametrosContratoList, distribucionInversionList, portafoliosModificadosList, detalleOrigenTraspasoList));
            out.addAll(parametrosSalida.mensajes());

            // Modificar Portafolio
            try {
                HashMap<Long, Boolean> portafolioModificadoFiltro = new HashMap<>();
                for (DatosPortafoliosModificados datosPortafoliosModificados : datosPortafoliosModificadosList) {
                    Long idPortafolio = datosPortafoliosModificados.IdPortafolio();

                    // Evita modificar mas de una vez el mismo portafolio
                    if (portafolioModificadoFiltro.containsKey(idPortafolio)) {
                        continue;
                    }
                    portafolioModificadoFiltro.put(idPortafolio, true);

                    DatosContratoSAAPV datosContrato = datosContratoSAAPVHash.get(datosPortafoliosModificados.IdContratoSAAPV());
                    if (datosContrato == null) {
                        continue;
                    }

                    String idPerfilComercialAPV = datosContrato.IdPerfilComercialAPV();
                    // Solo se modifica el portafolio si el contrato posee perfil APV informado
                    if (idPerfilComercialAPV == null || "".equals(idPerfilComercialAPV)) {
                        continue;
                    }

                    JsonObject cambio = Json.objeto().poner("CampoModificado", "IdPerfilComercialBaseOrigen").poner("ValorNuevo", idPerfilComercialAPV).construir();
                    JsonObject salidaModificarPortafolio = colaboradores.invocar("ModificarPortafolio", Json.objeto()
                            .poner("p_IdEmpresa", in.idEmpresa())
                            .poner("p_IdUsuario", in.idUsuario())
                            .poner("p_IdCliente", null)
                            .poner("p_IdModulo", in.idModulo())
                            .poner("p_UsaReglasNegocio", "N")
                            .poner("p_IdPortafolio", idPortafolio)
                            .poner("p_IdCanal", null)
                            .poner("p_RegistroCambio", List.of(cambio))
                            .construir());

                    String folio = datosContrato.FolioContratoSAAPV();

                    List<JsonObject> errores = Json.lista(salidaModificarPortafolio, "P_OUT_ERRORESDETALLE");
                    if (errores != null) {
                        for (JsonObject errorDetalle : errores) {
                            out.add(Mensajes.de(datosPortafoliosModificados.IdContratoSAAPV(), Mensajes.MENSAJE_ERROR, Json.texto(errorDetalle, "Codigo"),
                                    "CONTRATO " + folio + ": Modificar Portafolio: " + Json.texto(errorDetalle, "Descripcion")));
                        }
                    }

                    List<JsonObject> advertencias = Json.lista(salidaModificarPortafolio, "P_OUT_ADVERTENCIASDETALLE");
                    if (advertencias != null) {
                        for (JsonObject advertenciaDetalle : advertencias) {
                            out.add(Mensajes.de(datosPortafoliosModificados.IdContratoSAAPV(), Mensajes.MENSAJE_ADVERTENCIA,
                                    Json.texto(advertenciaDetalle, "Codigo"),
                                    "CONTRATO " + folio + ": Modificar Portafolio: " + Json.texto(advertenciaDetalle, "Descripcion")));
                        }
                    }
                }
            } catch (Exception ex) {
                out.add(Mensajes.de(0L, Mensajes.MENSAJE_ERROR, "ERROR_MODIFICAR_PORTAFOLIO", "Error al modificar portafolios: " + Sneaky.causa(ex).getMessage()));
            }
        }
        // Cambio de plan a portafolios
        if (lstPortafolios.size() > 0) {
            try {
                // LEGACY DEFECT kept (see README APV-A-334): the global table of portfolios is never cleaned and the update moves every portfolio in it,
                // not only the ones of this call
                repositorio.registrarPortafoliosCambioPlan(lstPortafolios);
                int resultadoMod = repositorio.modificarPlanPortafolios(lstPortafolios);
                if (resultadoMod > 0) {
                    out.add(Mensajes.de((long) lstPortafolios.size(), Mensajes.MENSAJE_OK, "MODIFICA_PLAN_PORTAFOLIO_OK",
                            "Cambio de Plan a " + resultadoMod + " Portafolios OK."));
                } else {
                    out.add(Mensajes.de((long) lstPortafolios.size(), Mensajes.MENSAJE_ERROR, "ERROR_MODIFICA_PLAN_PORTAFOLIO",
                            "Problema al modificar el plan de Portafolios."));
                }
            } catch (Exception ex) {
                // legacy: ex.printStackTrace(); the failure is swallowed
            }
        }
        return out;
    }

    private static String fecha(java.sql.Date fecha) {
        return fecha != null ? fecha.toLocalDate().format(FORMATO_FECHA) : "";
    }

    /** Legacy {@code copyPojoParametrosContrato} plus the overrides of {@code logica}. */
    private static ParametrosContrato parametrosContrato(DatosContratoSAAPV c, String autorizaGestionContrato) {
        ParametrosContrato d = ParametrosContrato.porDefecto();
        String idPerfilComercial = c.IdPerfilComercialAPV() == null || "".equals(c.IdPerfilComercialAPV()) ? c.IdPerfilComercial() : c.IdPerfilComercialAPV();
        return new ParametrosContrato(
                c.IdContratoSAAPV(),
                c.IdEmpresa(),
                c.FolioContratoSAAPV(),
                2L,
                c.NombrePlan(),
                fecha(c.FechaSuscripcion()),
                fecha(c.FechaCreacion()),
                fecha(c.FechaAprobacion()),
                fecha(c.FechaNotificacion()),
                fecha(c.FechaRecepcion()),
                c.FolioContratoSAAPV(),
                c.IdPersona(),
                c.IdEmpleador(),
                c.IdCliente(),
                c.IdCategoriaInversionista(),
                idPerfilComercial,
                c.NombreCliente(),
                c.ApellidoPaternoCliente(),
                c.ApellidoMaternoCliente(),
                c.DocumentoIdentidadCliente(),
                fecha(c.FechaNacimientoCliente()),
                c.EstadoCivil(),
                c.IdTipoGenero(),
                c.IdPaisResidenciaCliente(),
                c.IdCiudadCliente(),
                c.IdComunaCliente(),
                c.DireccionCliente(),
                c.NumeroDireccionCliente(),
                c.IdTipoCargoPersona(),
                c.TelefonoCliente(),
                c.TelefonoComercialCliente(),
                c.TelefonoCelularCliente(),
                c.EmailCliente(),
                c.RazonSocialEmpleador(),
                c.IdMedioRecaudacionTrabajador(),
                c.IdEntidadRecaudadora(),
                c.DocumentoIdentidadEmpleador(),
                c.IdPaisEmpleador(),
                c.DireccionComercialEmpleador(),
                c.NumeroDireccionEmpleador(),
                c.IdCiudadEmpleador(),
                c.IdComunaEmpleador(),
                c.TelefonoEmpleador(),
                c.NombreEncargado(),
                c.EmailEncargado(),
                c.TelefonoEncargado(),
                c.CodigoPrevision(),
                c.Pensionado(),
                c.Trabajador(),
                c.FormaDePago(),
                c.IdMedioRecaudacionFormaPago(),
                c.DiaDescuento(),
                c.Institucion(),
                c.ComisionTransferencia().doubleValue(),
                "MOD",
                c.TipoMovimiento(),
                c.RazonRetiro(),
                c.NombreEntidadOrigen(),
                c.RutEntidadOrigen(),
                c.IdPersonaOrigen(),
                c.Descripcion(),
                c.TipoDeAhorro(),
                c.FondoDeOrigen(),
                c.TipoTraspaso(),
                c.MontoTraspaso().doubleValue(),
                c.UFTraspaso().doubleValue(),
                c.PorcentajeTraspaso().doubleValue(),
                "NEV",
                "S".equals(autorizaGestionContrato) ? "PDA" : "",
                c.IdEstado(),
                null,
                c.IdEmpleado(),
                "",
                0L,
                "",
                d.canal(),
                d.esAporteCuentaInversion(),
                c.IdInterno());
    }
}
