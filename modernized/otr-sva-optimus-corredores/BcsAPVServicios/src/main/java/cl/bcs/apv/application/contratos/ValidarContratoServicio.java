package cl.bcs.apv.application.contratos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.ValidarContrato;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.validarcontrato.ContratosSAAPV;
import cl.bcs.apv.api.dto.validarcontrato.DetalleValidacionContratoSAAPV;
import cl.bcs.apv.api.dto.validarcontrato.ModificaContratoSAAPV;
import cl.bcs.apv.api.dto.validarcontrato.RecaudacionCliente;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoEntrada;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoResultado;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.contratos.ValidarContratoRepositorio;
import cl.bcs.apv.domain.port.out.contratos.ValidarContratoRepositorio.ExisteCliente;
import cl.bcs.apv.domain.port.out.contratos.ValidarContratoRepositorio.ExisteFolioContrato;
import cl.bcs.apv.domain.port.out.contratos.ValidarContratoRepositorio.ExistePersona;
import cl.bcs.apv.domain.port.out.contratos.ValidarContratoRepositorio.ExistePortafolio;
import cl.bcs.apv.domain.port.out.contratos.ValidarContratoRepositorio.RecaudacionValidada;
import cl.bcs.apv.domain.port.out.contratos.ValidarContratoRepositorio.TraspasoReapertura;

/**
 * Use case {@code ValidarContrato}: legacy {@code cl.bcs.APV.ValidaContratoSAAPV} (resolves the ids of person, client, employer and portfolio of each
 * contract, writes them with option M, and validates the recaudacion of each client against its contract).
 */
@Transactional(rollbackFor = Exception.class)
public class ValidarContratoServicio implements ValidarContrato {

    private final ConfiguracionSistema configuracion;
    private final ValidarContratoRepositorio repositorio;

    public ValidarContratoServicio(ConfiguracionSistema configuracion, ValidarContratoRepositorio repositorio) {
        this.configuracion = configuracion;
        this.repositorio = repositorio;
    }

    @Override
    public ValidarContratoResultado ejecutar(ValidarContratoEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        List<ModificaContratoSAAPV> idsContrato = new ArrayList<>();
        List<DetalleValidacionContratoSAAPV> idsRecaudacion = new ArrayList<>();

        String fechaSistema = Parametros.fechaSistema(configuracion, in.idEmpresa());
        String periodoSistema = fechaSistema.substring(0, 7);
        // the legacy reads it (and fails when it is not configured) although the value is not used afterwards
        Parametros.texto(configuracion, in.idEmpresa(), Parametros.PLATAFORMA, "APV.AutorizaGestionContrato");

        if (in.contratosSAAPV().size() > 0) {
            for (ContratosSAAPV fila : in.contratosSAAPV()) {
                // legacy: Ids.FolioContratoSAAPV = fila.FolioContratoSAAPV (a null element throws NullPointerException here)
                String idsFolio = fila.folioContratoSAAPV();
                Long idsPersona = null;
                Long idsEmpleador = null;
                Long idsCliente = null;
                Long idsPortafolio = null;
                String idsCodigo = null;
                String strDescripcion = "";

                if (fila.idPersona() == null) {
                    List<ExistePersona> personas = repositorio.personasPorDocumento(fila.documentoIdentidad(), "N");
                    if (!personas.isEmpty()) {
                        idsPersona = personas.get(0).IdPersona;
                    }
                } else {
                    idsPersona = fila.idPersona();
                }

                if (fila.idEmpleador() == null && !("").equals(fila.documentoIdentidadEmpleador())) {
                    List<ExistePersona> personas = repositorio.personasPorDocumento(fila.documentoIdentidadEmpleador(), "J");
                    if (!personas.isEmpty()) {
                        idsEmpleador = personas.get(0).IdPersona;
                    }
                }

                if (fila.idCliente() == null) {
                    List<ExisteCliente> clientes = repositorio.clientesPorPersona(idsPersona);
                    if (!clientes.isEmpty()) {
                        idsCliente = clientes.get(0).IdCliente;
                    }
                }

                if (fila.idPortafolio() == null) {
                    List<ExistePortafolio> portafolios = repositorio.portafoliosApv(idsPersona, fila.codigo(), fila.nombrePlan(), fila.codigoAgrupador());
                    if (!portafolios.isEmpty() && portafolios.size() == 1) {
                        idsPortafolio = portafolios.get(0).IdPortafolio;
                        idsCodigo = fila.codigo();
                    }
                    if (portafolios.size() > 1) {
                        strDescripcion = "Existe mas de un Portafolio con proposito APV.";
                    }
                }

                // folio check of an Ingreso or Reapertura
                if (in.opcion().equals("I") || in.opcion().equals("R")) {
                    // LEGACY DEFECT kept (see README APV-A-220): `!= null || !equals("")` evaluates equals on a null folio (NullPointerException); with a folio the condition is always true
                    if (fila.folioContratoSAAPV() != null || !fila.folioContratoSAAPV().equals("")) {
                        List<ExisteFolioContrato> existentes = repositorio.foliosExistentes(fila.folioContratoSAAPV());
                        idsFolio = existentes.isEmpty() ? "" : fila.folioContratoSAAPV();
                    } else {
                        idsFolio = "";
                    }
                }

                idsContrato.add(new ModificaContratoSAAPV(idsPersona, idsCliente, idsPortafolio, idsEmpleador, idsFolio, strDescripcion, idsCodigo));

                if (in.opcion().compareTo("M") == 0) {
                    // LEGACY DEFECT kept (see README APV-A-218): the batch re-applies the whole accumulated list at every row
                    repositorio.modificarIdsContratos(idsContrato);
                }
            }
        }

        List<Long> idsRecaudacionCliente = new ArrayList<>();
        if (in.recaudacionCliente() != null) {
            for (RecaudacionCliente fila : in.recaudacionCliente()) {
                idsRecaudacionCliente.add(fila.idRecaudacionCliente());
            }
        }
        List<RecaudacionValidada> totalRecaudacionValidada = repositorio.recaudacionValidada(idsRecaudacionCliente, periodoSistema);

        if (in.recaudacionCliente() != null && in.recaudacionCliente().size() > 0) {
            if (!in.opcion().equals("RECTRA")) {
                String valorValEMPLEADOR = Parametros.texto(configuracion, in.idEmpresa(), Parametros.APV, "REC.ValidacionSAAPV_Empleador");
                String valorValPERIODO_TERMINO = Parametros.texto(configuracion, in.idEmpresa(), Parametros.APV, "REC.ValidacionSAAPV_PeriodoTermino");
                // read for parity with the legacy (it fails when it is not configured); the amount validation is done in the SQL
                Parametros.texto(configuracion, in.idEmpresa(), Parametros.APV, "REC.ValidacionSAAPV_Monto");

                for (RecaudacionCliente fila : in.recaudacionCliente()) {
                    // LEGACY DEFECT kept (see README APV-A-243): the debug message is concatenated eagerly, a null IdRecaudacion throws NullPointerException
                    fila.idRecaudacion().toString();
                    List<RecaudacionValidada> validadas = new ArrayList<>();
                    for (RecaudacionValidada filaVal : totalRecaudacionValidada) {
                        if (filaVal.IdRecaudacionCliente == fila.idRecaudacionCliente()) {
                            validadas.add(filaVal);
                        }
                    }

                    String mensajeValidacion = "";
                    if (validadas.size() == 0) {
                        if (fila.idRecaudacionCliente() == 0) {
                            mensajeValidacion = mensajeValidacion + "No tiene detalle cliente asociado.\n";
                        } else {
                            mensajeValidacion = mensajeValidacion + "No tiene contratos vigentes.\n";
                        }
                    }

                    for (int i = validadas.size() - 1; i >= 0; i--) {
                        if (validadas.get(i).Asignado.compareTo("OK") == 0) {
                            RecaudacionValidada actual = validadas.get(i);
                            validadas.clear();
                            validadas.add(actual);
                            break;
                        }
                    }

                    if (validadas.size() >= 2) {
                        int contadorOK = 0;
                        for (int x = validadas.size() - 1; x >= 0; x--) {
                            // LEGACY DEFECT kept (see README APV-A-235): PeriodoTermino holds a period text, it is compared with "NOK"
                            if (validadas.get(x).PeriodoTermino.compareTo("NOK") == 0 && valorValPERIODO_TERMINO.compareTo("S") == 0) {
                                validadas.remove(x);
                            } else if (validadas.get(x).IdEstadoCliente.compareTo("NOK") == 0) {
                                validadas.remove(x);
                            } else if (validadas.get(x).IdEstadoPortafolio.compareTo("NOK") == 0) {
                                validadas.remove(x);
                            } else if (validadas.get(x).Validacion_FondoRestringido.compareTo("NOK") == 0) {
                                validadas.remove(x);
                            } else {
                                contadorOK = contadorOK + 1;
                            }
                        }
                        if (contadorOK == 0) {
                            mensajeValidacion = mensajeValidacion + "Debe seleccionar un contrato\n";
                        }
                    }

                    long detIdPortafolio = 0L;
                    long detIdContrato = 0L;
                    String detIdRegimen = null;
                    String detPago = null;
                    if (validadas.size() == 1) {
                        RecaudacionValidada unica = validadas.get(0);
                        if (unica.IdContratoSAAPV == 0) {
                            mensajeValidacion = mensajeValidacion + "No existen contratos vigentes.\n";
                        } else {
                            if (unica.Validacion_Empleador.compareTo("NOK") == 0 && valorValEMPLEADOR.compareTo("N") == 0) {
                                mensajeValidacion = mensajeValidacion + "Segun lo definido en el contrato, la recaudacion no es por empleador.\n";
                            }
                            // LEGACY DEFECT kept (see README APV-A-235): the period text is compared with "NOK" (never equal), so no 'vencido' message
                            if (unica.PeriodoTermino.compareTo("NOK") == 0 && valorValPERIODO_TERMINO.compareTo("S") == 0) {
                                mensajeValidacion = mensajeValidacion + "El contrato se encuentra vencido.\n";
                            }
                            if (unica.IdEstadoCliente.compareTo("ACT") != 0) {
                                mensajeValidacion = mensajeValidacion + "Cliente bloqueado.\n";
                            }
                            if (unica.IdEstadoPortafolio.compareTo("ACT") != 0) {
                                mensajeValidacion = mensajeValidacion + "Portafolio bloqueado.\n";
                            }
                            if (unica.Validacion_FondoRestringido.equalsIgnoreCase("NOK")) {
                                mensajeValidacion = mensajeValidacion + "Fondo restringido para aportes. \n";
                            }
                        }
                        detIdPortafolio = unica.IdPortafolio;
                        detIdContrato = unica.IdContratoSAAPV;
                        detIdRegimen = unica.RegimenTributario;
                        detPago = unica.Pago;
                    }

                    String validacionOK = mensajeValidacion.compareTo("") == 0 ? "OK" : "NOK";
                    // a null IdRecaudacionEmpleador unboxes into the legacy long field: NullPointerException
                    idsRecaudacion.add(new DetalleValidacionContratoSAAPV(fila.idRecaudacion(), fila.idRecaudacionEmpleador(),
                            fila.idRecaudacionCliente(), fila.idEstadoRecaudacionCliente(), detIdContrato, detIdPortafolio, detIdRegimen,
                            validacionOK, fila.monto(), mensajeValidacion, detPago));
                }
            } else if (in.opcion().equals("RECTRA")) {
                Long idPortafolioRepetido = 0L;

                List<TraspasoReapertura> detallesTraspasoReapertura = new ArrayList<>();
                for (Long filaIdDT : in.idsDetalleTraspasoReapertura()) {
                    List<TraspasoReapertura> detalleTraspaso = repositorio.detalleTraspaso(filaIdDT);
                    detallesTraspasoReapertura.add(detalleTraspaso.get(0));
                }

                int contDetalleReapertura = 0;
                for (RecaudacionCliente fila : in.recaudacionCliente()) {
                    if (!totalRecaudacionValidada.isEmpty() && totalRecaudacionValidada.get(0).IdEstadoContrato.compareTo("COM") == 0
                            && !in.idsDetalleTraspasoReapertura().isEmpty()) {
                        TraspasoReapertura t = detallesTraspasoReapertura.get(contDetalleReapertura);
                        idPortafolioRepetido = t.IdPortafolio;
                        idsRecaudacion.add(new DetalleValidacionContratoSAAPV(fila.idRecaudacion(), 0L, fila.idRecaudacionCliente(),
                                fila.idEstadoRecaudacionCliente(), t.IdContratoSAAPV, t.IdPortafolio, t.Codigo, "OK", fila.monto(), "", null));
                        contDetalleReapertura++;
                    } else {
                        for (RecaudacionValidada filaVal : totalRecaudacionValidada) {
                            if (filaVal.IdRecaudacionCliente == fila.idRecaudacionCliente() && filaVal.Validacion_Monto.equals("OK")
                                    && filaVal.IdPortafolio != idPortafolioRepetido) {
                                idPortafolioRepetido = filaVal.IdPortafolio;
                                idsRecaudacion.add(new DetalleValidacionContratoSAAPV(fila.idRecaudacion(), 0L, fila.idRecaudacionCliente(),
                                        fila.idEstadoRecaudacionCliente(), filaVal.IdContratoSAAPV, filaVal.IdPortafolio, filaVal.RegimenTributario, "OK",
                                        fila.monto(), "", null));
                            }
                        }
                    }
                }
            }
        }

        // the legacy call() sets P_OUT_ERRORESCONTADOR = getNumeroErrores(P_OUT_MENSAJES) after logica
        return new ValidarContratoResultado(mensajes, Mensajes.numeroErrores(mensajes), idsContrato, idsRecaudacion);
    }
}
