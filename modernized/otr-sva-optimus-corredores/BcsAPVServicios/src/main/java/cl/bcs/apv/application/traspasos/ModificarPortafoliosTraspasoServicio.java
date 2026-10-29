package cl.bcs.apv.application.traspasos;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.ModificarPortafoliosTraspaso;
import cl.bcs.apv.api.casouso.ValidarContrato;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.modificarportafoliostraspaso.ModificarPortafoliosTraspasoEntrada;
import cl.bcs.apv.api.dto.modificarportafoliostraspaso.ModificarPortafoliosTraspasoResultado;
import cl.bcs.apv.api.dto.modificarportafoliostraspaso.Traspaso;
import cl.bcs.apv.api.dto.validarcontrato.ContratosSAAPV;
import cl.bcs.apv.api.dto.validarcontrato.ModificaContratoSAAPV;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoEntrada;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoResultado;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;
import cl.bcs.apv.domain.port.out.traspasos.ModificarPortafoliosTraspasoRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.ModificarPortafoliosTraspasoRepositorio.ClienteContrato;
import cl.bcs.apv.domain.port.out.traspasos.ModificarPortafoliosTraspasoRepositorio.DetalleAEliminar;
import cl.bcs.apv.domain.port.out.traspasos.ModificarPortafoliosTraspasoRepositorio.DetalleModificado;
import cl.bcs.apv.domain.port.out.traspasos.ModificarPortafoliosTraspasoRepositorio.DetalleNuevo;
import cl.bcs.apv.domain.port.out.traspasos.ModificarPortafoliosTraspasoRepositorio.DetalleVigente;
import cl.bcs.apv.domain.port.out.traspasos.ModificarPortafoliosTraspasoRepositorio.PlanPortafolio;
import cl.bcs.apv.domain.port.out.traspasos.ModificarPortafoliosTraspasoRepositorio.PortafolioCliente;
import cl.bcs.apv.domain.port.out.traspasos.ModificarPortafoliosTraspasoRepositorio.PortafolioCreado;
import cl.bcs.apv.domain.port.out.traspasos.ModificarPortafoliosTraspasoRepositorio.PortafolioDeMatriz;
import cl.bcs.apv.domain.port.out.traspasos.ModificarPortafoliosTraspasoRepositorio.RelacionPortafolioContrato;

/**
 * Use case {@code ModificarPortafoliosTraspaso}: legacy {@code cl.bcs.APV.ModificarPortafolioTraspaso}. The legacy mutates its input rows
 * ({@code IdDetalleTraspaso}, {@code IdPortafolio}, {@code IdPersonaCliente}, {@code IdCliente}, {@code IdPerfilComercial}) while it goes, so the rows are
 * copied into a mutable working class. The portafolio is created by the stored procedure {@code CrearPortafolio} through {@link Colaboradores}.
 * Not a legacy defect but worth knowing: the legacy called {@code ValidaContratoSAAPV.logica} (which never sets the error counter) and tested
 * {@code P_OUT_ERRORESCONTADOR == null}: that is always true, so the result of the validation is always applied.
 */
@Transactional(rollbackFor = Exception.class)
public class ModificarPortafoliosTraspasoServicio implements ModificarPortafoliosTraspaso {

    private static final String SP_CREAR_PORTAFOLIO = "BSCL.Portafolio.BD.Servicios::CrearPortafolio";

    private final DiccionarioMensajes diccionario;
    private final Reloj reloj;
    private final Secuencias secuencias;
    private final Colaboradores colaboradores;
    private final ModificarPortafoliosTraspasoRepositorio repositorio;
    private final ValidarContrato validarContrato;

    public ModificarPortafoliosTraspasoServicio(DiccionarioMensajes diccionario, Reloj reloj, Secuencias secuencias, Colaboradores colaboradores,
            ModificarPortafoliosTraspasoRepositorio repositorio, ValidarContrato validarContrato) {
        this.diccionario = diccionario;
        this.reloj = reloj;
        this.secuencias = secuencias;
        this.colaboradores = colaboradores;
        this.repositorio = repositorio;
        this.validarContrato = validarContrato;
    }

    /** Mutable copy of the legacy {@code Traspaso} input row (the legacy changes its fields in place). */
    private static final class Fila {
        Long IdTraspaso;
        Long IdContratoSAAPV;
        String FechaIngreso;
        Long IdPersonaCliente;
        Long IdCliente;
        String DocumentoIdentidad;
        String IdPerfilComercial;
        Long IdDetalleTraspaso;
        Long IdPortafolio;
        String IdPropositoPortafolio;
        Long IdPlanTipoInversion;
        String NombrePlan;
        Long IdProductoAgrupador;
        Long IdRegimenTributario;
        String RegimenTributario;
        String CodigoAgrupador;
        BigDecimal MontoPeso;
        BigDecimal MontoUF;
        BigDecimal Porcentaje;
        String EsReapertura;

        static Fila de(Traspaso t) {
            if (t == null) {
                return null;
            }
            Fila f = new Fila();
            f.IdTraspaso = t.idTraspaso();
            f.IdContratoSAAPV = t.idContratoSAAPV();
            f.FechaIngreso = t.fechaIngreso();
            f.IdPersonaCliente = t.idPersonaCliente();
            f.IdCliente = t.idCliente();
            f.DocumentoIdentidad = t.documentoIdentidad();
            f.IdPerfilComercial = t.idPerfilComercial();
            f.IdDetalleTraspaso = t.idDetalleTraspaso();
            f.IdPortafolio = t.idPortafolio();
            f.IdPropositoPortafolio = t.idPropositoPortafolio();
            f.IdPlanTipoInversion = t.idPlanTipoInversion();
            f.NombrePlan = t.nombrePlan();
            f.IdProductoAgrupador = t.idProductoAgrupador();
            f.IdRegimenTributario = t.idRegimenTributario();
            f.RegimenTributario = t.regimenTributario();
            f.CodigoAgrupador = t.codigoAgrupador();
            f.MontoPeso = t.montoPeso();
            f.MontoUF = t.montoUF();
            f.Porcentaje = t.porcentaje();
            f.EsReapertura = t.esReapertura();
            return f;
        }
    }

    /** Legacy local state of {@code logica}: the counter and the shared {@code SalidaCliente.P_OUT_REFIDPORTAFOLIO} (a primitive long: it carries over between rows). */
    private static final class Estado {
        final List<Mensaje> mensajes = new ArrayList<>();
        final List<Long> idResultado = new ArrayList<>();
        long errores = 0L;
        long refIdPortafolio = 0L;
    }

    @Override
    public ModificarPortafoliosTraspasoResultado ejecutar(ModificarPortafoliosTraspasoEntrada in) {
        Estado s = new Estado();
        List<Fila> traspasos = new ArrayList<>();
        // the legacy reads p_Traspasos.get(0) first: an empty list ends in IndexOutOfBoundsException before anything else
        for (Traspaso t : in.traspasos()) {
            traspasos.add(Fila.de(t));
        }
        Long idPortafolioCreado = 0L;
        Long idPortafolioMatriz = 0L;
        String fechaPeriodo = new SimpleDateFormat("yyyy-MM").format(Date.from(reloj.ahora()));

        // Detalles traspaso eliminados
        List<DetalleAEliminar> detalleTraspasoElim = repositorio.detallesAEliminar(traspasos.get(0).IdTraspaso, traspasos.get(0).IdContratoSAAPV);

        if (!detalleTraspasoElim.isEmpty() && s.errores == 0) {
            for (DetalleAEliminar detTraspaso : detalleTraspasoElim) {
                boolean eliminarTraspaso = true;
                for (Fila fila : traspasos) {
                    if (fila.IdDetalleTraspaso == null || fila.IdPortafolio == null) {
                        continue;
                    }
                    if (detTraspaso.IdDetalleTraspaso().compareTo(fila.IdDetalleTraspaso) == 0 && detTraspaso.IdPortafolio().compareTo(fila.IdPortafolio) == 0
                            || fila.EsReapertura.compareTo("S") == 0) {
                        eliminarTraspaso = false;
                    }
                }
                if (eliminarTraspaso) {
                    int contTraspaso = 0;
                    for (Fila filaDT : traspasos) {
                        if (filaDT.IdDetalleTraspaso.compareTo(detTraspaso.IdDetalleTraspaso()) == 0) {
                            if (filaDT.IdPlanTipoInversion.compareTo(detTraspaso.IdPlanTipoInversion()) != 0
                                    || filaDT.IdProductoAgrupador.compareTo(detTraspaso.IdProductoAgrupador()) != 0
                                    || filaDT.IdRegimenTributario.compareTo(detTraspaso.IdRegimenTributario()) != 0
                                    || filaDT.MontoPeso.compareTo(detTraspaso.MontoPeso()) != 0
                                    || filaDT.MontoUF.compareTo(detTraspaso.MontoUF()) != 0) {
                                traspasos.get(contTraspaso).IdDetalleTraspaso = 0L;
                                traspasos.get(contTraspaso).IdPortafolio = 0L;
                            }
                        }
                        contTraspaso++;
                    }
                    int[] deleteDetTraspaso = repositorio.eliminarDetalleTraspaso(detTraspaso.IdDetalleTraspaso());
                    if (deleteDetTraspaso != null && deleteDetTraspaso.length > 0 && deleteDetTraspaso[0] == 1) {
                        int[] deletePortafolioContrato = repositorio.eliminarPortafolioContrato(detTraspaso.IdPortafolio(), detTraspaso.IdContratoSAAPV());
                        if (deletePortafolioContrato != null && deletePortafolioContrato.length > 0 && deletePortafolioContrato[0] == 1) {
                            int deletePortafolio = eliminarPortafolioTraspaso(detTraspaso.IdPortafolio(), detTraspaso.IdContratoSAAPV());
                            if (deletePortafolio < 2) {
                                HashMap<String, String> valores = new HashMap<>();
                                String codMsg = "";
                                if (deletePortafolio == 0) {
                                    codMsg = "MOFIFICACION_DETALLE_CONTRATO";
                                    valores.put("CodigoPortafolio", detTraspaso.CodigoPortafolio() != null ? detTraspaso.CodigoPortafolio() : "");
                                    valores.put("FolioContrato", detTraspaso.FolioContratoSAAPV() != null ? detTraspaso.FolioContratoSAAPV() : "");
                                }
                                if (deletePortafolio == 1) {
                                    codMsg = "DESACTIVAR_PORTAFOLIO";
                                    valores.put("CodigoPortafolio", detTraspaso.CodigoPortafolio() != null ? detTraspaso.CodigoPortafolio() : "");
                                }
                                s.mensajes.add(Mensajes.conDiccionario(diccionario, in.idEmpresa(), detTraspaso.IdPortafolio(), Mensajes.MENSAJE_INFORMACION, codMsg, valores));
                            } else {
                                errorModificarDetalleContrato(in, s, detTraspaso);
                            }
                        } else {
                            errorModificarDetalleContrato(in, s, detTraspaso);
                        }
                    } else {
                        errorModificarDetalleContrato(in, s, detTraspaso);
                    }
                }
            }
        }

        if (!traspasos.isEmpty() && s.errores == 0) {
            for (Fila fila : traspasos) {
                // VALIDACION CONTRATOS SAAPV: only the portafolio is validated (IdPortafolio NULL)
                Long idTraspaso = fila.IdTraspaso;
                idPortafolioCreado = 0L;
                boolean portafolioEncontrado = false;

                List<ContratosSAAPV> tablaValidaContrato = new ArrayList<>();
                tablaValidaContrato.add(new ContratosSAAPV(fila.IdPersonaCliente, null, null, null, fila.DocumentoIdentidad, "", null, fila.RegimenTributario,
                        fila.NombrePlan, fila.CodigoAgrupador));
                ValidarContratoEntrada inValida = new ValidarContratoEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "M", tablaValidaContrato,
                        new ArrayList<>(), new ArrayList<>());
                ValidarContratoResultado outValida = validarContrato.ejecutar(inValida);

                // legacy: if (outValidaContrato.P_OUT_ERRORESCONTADOR == null): logica never sets it, so the block always runs
                for (ModificaContratoSAAPV filaValida : outValida.idsContrato()) {
                    if (filaValida.idPersona() != null) {
                        fila.IdPersonaCliente = filaValida.idPersona();
                    }
                    if (filaValida.idCliente() != null) {
                        fila.IdCliente = filaValida.idCliente();
                    }
                    if (filaValida.codigo() != null) {
                        idPortafolioCreado = filaValida.idPortafolio();
                    }
                }

                // Verificacion si el detalle traspaso existe, no es una reapertura y si ha sido modificado
                // LEGACY DEFECT kept (see README APV-B-204): EsReapertura S is never treated as an existing detalle
                if (fila.IdTraspaso > 0 && fila.IdPortafolio > 0 && fila.IdDetalleTraspaso > 0 && fila.IdDetalleTraspaso != null
                        && fila.EsReapertura.compareTo("N") == 0) {
                    List<DetalleVigente> detalleTraspaso = repositorio.detallesDelTraspaso(fila.IdTraspaso, fila.IdPortafolio, fila.IdDetalleTraspaso);

                    if (fila.IdCliente.compareTo(0L) == 0) {
                        List<ClienteContrato> clienteContrato = repositorio.clienteDelContrato(fila.IdContratoSAAPV);
                        fila.IdCliente = clienteContrato.get(0).IdCliente();
                        fila.IdPerfilComercial = clienteContrato.get(0).IdPerfilComercial();
                    }

                    // Cambio de estado a Inactivo para detalles traspasos antiguos
                    int contDetalleTraspaso = 0;
                    for (DetalleVigente dTrasp : detalleTraspaso) {
                        if (dTrasp.IdDetalleTraspaso().compareTo(fila.IdDetalleTraspaso) == 0 && dTrasp.IdEstado().equals("VIG")) {
                            contDetalleTraspaso++;
                        }
                    }
                    if (contDetalleTraspaso >= 2) {
                        repositorio.cambiarEstadoDetalle(fila.IdDetalleTraspaso, "INA");
                        // Ingreso detalle traspaso vigente
                        ingresarDetalleTraspaso(fila, idPortafolioCreado);
                        continue;
                    }

                    if (detalleTraspaso.get(0).IdProductoAgrupador().compareTo(fila.IdProductoAgrupador) != 0
                            || detalleTraspaso.get(0).IdRegimenTributario().compareTo(fila.IdRegimenTributario) != 0) {

                        // Se elimina relacion portafolio-contrato
                        repositorio.eliminarPortafolioContrato(detalleTraspaso.get(0).IdPortafolio(), fila.IdContratoSAAPV);

                        // Se verifica si matrizAPV ingresada tiene portafolio para el cliente
                        List<PortafolioDeMatriz> matrizAgrupadorTAPVPort = matrizAgrupadorTraspasoPortafolio(fila);

                        if (matrizAgrupadorTAPVPort != null && matrizAgrupadorTAPVPort.size() > 0) {
                            // Se asigna a portafolio preexistente con misma matrizAPV
                            boolean resultadoEstado = true;
                            if (matrizAgrupadorTAPVPort.get(0).IdEstado().compareTo("INA") == 0) {
                                resultadoEstado = modificarEstadoPortafolioTraspaso(matrizAgrupadorTAPVPort.get(0).IdPortafolio(), "ACT");
                            }
                            idPortafolioMatriz = matrizAgrupadorTAPVPort.get(0).IdPortafolio();

                            // Se crea nueva relacion entre portafolio encontrado y contrato
                            int[] result = relacionarPortafolioContratoSAAPV(fila, idPortafolioMatriz, fechaPeriodo);

                            if (result.length > 0 && resultadoEstado) {
                                // Se verifica si el portafolio antiguo debe ser eliminado
                                int deletePortafolio = eliminarPortafolioTraspaso(detalleTraspaso.get(0).IdPortafolio(), fila.IdContratoSAAPV);
                                if (deletePortafolio == 0 || deletePortafolio == 1) {
                                    String descripcion = null;
                                    if (deletePortafolio == 0) {
                                        descripcion = "Se ha Modificado el contrato folio N: " + fila.IdContratoSAAPV.toString();
                                    }
                                    if (deletePortafolio == 1) {
                                        descripcion = "Se ha Desactivado el portafolio N: " + detalleTraspaso.get(0).IdPortafolio().toString();
                                    }
                                    s.mensajes.add(Mensajes.de(detalleTraspaso.get(0).IdPortafolio(), "OK", "ELIMINO_PORTAFOLIO_CONTRATO_OK", descripcion));
                                } else {
                                    s.mensajes.add(Mensajes.de(6L, "ERROR", "ERROR_ELIMINO_PORTAFOLIO_CONTRATO",
                                            "Problema al eliminar el Portafolio Contrato N: " + detalleTraspaso.get(0).IdPortafolio().toString()));
                                    s.errores++;
                                }
                            }

                            // Update detalle traspaso
                            int[] modDT = actualizarDetalleTraspaso(fila, idPortafolioMatriz);
                            if (modDT.length > 0) {
                                s.mensajes.add(Mensajes.de(s.refIdPortafolio, "OK", "MODIFICAR_PORTAFOLIO_CONTRATO_OK", "Se ha modificado el portafolio N: "
                                        + matrizAgrupadorTAPVPort.get(0).IdPortafolio().toString() + " y el contrato traspaso N: " + fila.IdContratoSAAPV.toString()));
                                continue;
                            } else {
                                errorAsignarPortafolioContrato(s);
                                continue;
                            }
                        } else {
                            // Caso en que no exista un portafolio con la matriz agrupadorAPV nueva: se crea nuevo portafolio
                            JsonObject resultSP = crearPortafolioAPV(in, fila, traspasos);
                            leerResultadoCrearPortafolio(resultSP, s);

                            // Se crea nueva relacion entre portafolio nuevo y contrato
                            int[] relCon = relacionarPortafolioContratoSAAPV(fila, s.refIdPortafolio, fechaPeriodo);

                            // verificar si el portafolio antiguo tiene otros traspasos asignados
                            int deletePortafolio = eliminarPortafolioTraspaso(detalleTraspaso.get(0).IdPortafolio(), fila.IdContratoSAAPV);

                            if (relCon != null && relCon.length > 0) {
                                // Update detalle traspaso
                                int[] modDT = actualizarDetalleTraspaso(fila, s.refIdPortafolio);

                                if (modDT != null && modDT.length > 0) {
                                    String descripcion = null;
                                    if (deletePortafolio == 0) {
                                        descripcion = "Se ha Modificado el contrato folio N: " + fila.IdContratoSAAPV.toString();
                                    }
                                    if (deletePortafolio == 1) {
                                        descripcion = "Se ha Modificado el contrato folio N: " + fila.IdContratoSAAPV.toString() + " y se ha Desactivado el portafolio N: "
                                                + detalleTraspaso.get(0).IdPortafolio().toString();
                                    }
                                    s.mensajes.add(Mensajes.de(s.refIdPortafolio, "OK", "MODIFICAR_PORTAFOLIO_CONTRATO_OK", descripcion));
                                    continue;
                                } else {
                                    s.mensajes.add(Mensajes.de(6L, "ERROR", "ERROR_MODIFICAR_DETALLE_TRASPASO", "Problema al modificar el portafolio."));
                                    s.errores++;
                                    continue;
                                }
                            } else {
                                errorAsignarPortafolioContrato(s);
                                continue;
                            }
                        }

                    } else {

                        if (detalleTraspaso.get(0).MontoPeso().compareTo(fila.MontoPeso) != 0 || detalleTraspaso.get(0).MontoUF().compareTo(fila.MontoUF) != 0) {
                            // Solo se modifican los montos del traspaso
                            int[] actDT = actualizarDetalleTraspaso(fila, fila.IdPortafolio);
                            if (actDT.length > 0 && actDT != null) {
                                int[] modTC = repositorio.modificarMontoContrato(fila.IdContratoSAAPV, fila.IdPortafolio, fila.IdPortafolio, fila.MontoPeso, fila.MontoUF);
                                if (modTC.length > 0 && modTC != null) {
                                    s.mensajes.add(Mensajes.de(fila.IdPortafolio, "OK", "MODIFICAR_PORTAFOLIO_CONTRATO_OK", "Se ha modificado el portafolio N: "
                                            + fila.IdPortafolio.toString() + " y el contrato traspaso N: " + fila.IdContratoSAAPV.toString()));
                                    continue;
                                } else {
                                    s.mensajes.add(Mensajes.de(6L, "ERROR", "ERROR_MODIFICAR_DETALLE_TRASPASO", "Problema al modificar el portafolio."));
                                    s.errores++;
                                    continue;
                                }
                            }
                        } else {
                            // Detalle Traspaso no ha sido modificado
                            continue;
                        }
                    }

                } else {
                    // Se agrega Detalle Traspaso nuevo o reapertura ingresada
                    // Verificar portafolio preexistente para la matrizAgrupadorAPV a ingresar
                    List<PortafolioDeMatriz> matrizAgrupadorTAPVPort = matrizAgrupadorTraspasoPortafolio(fila);

                    if (matrizAgrupadorTAPVPort != null && matrizAgrupadorTAPVPort.size() > 0) {
                        // Se asigna a portafolio preexistente con misma matrizAPV
                        boolean modEstado = true;
                        if (matrizAgrupadorTAPVPort.get(0).IdEstado().compareTo("INA") == 0) {
                            modEstado = modificarEstadoPortafolioTraspaso(matrizAgrupadorTAPVPort.get(0).IdPortafolio(), "ACT");
                        }

                        int[] result = relacionarPortafolioContratoSAAPV(fila, matrizAgrupadorTAPVPort.get(0).IdPortafolio(), fechaPeriodo);

                        if (result != null && result.length > 0 && result[0] == 1 && modEstado) {
                            portafolioEncontrado = true;
                            s.mensajes.add(Mensajes.de(s.refIdPortafolio, "OK", "ASIGNAR_PORTAFOLIO_CONTRATO_OK", "Se crea relacion entre portafolio y contrato traspaso."));
                        } else {
                            errorAsignarPortafolioContrato(s);
                        }

                    } else {

                        // validacion tipo de plan
                        Boolean valido = validaPlanInversion(fila);
                        if (!valido) {
                            HashMap<String, String> valores = new HashMap<>();
                            valores.put("NombrePlanAhorro", fila.NombrePlan);
                            s.mensajes.add(Mensajes.conDiccionario(diccionario, in.idEmpresa(), fila.IdContratoSAAPV, Mensajes.MENSAJE_ERROR,
                                    "UI_ERROR_PLAN_TIPO_INVERSION", valores));
                            s.errores++;
                            continue;
                        }

                        // Nuevo Portafolio, Crear y vincular.
                        JsonObject resultSP = crearPortafolioAPV(in, fila, traspasos);
                        leerResultadoCrearPortafolio(resultSP, s);

                        if (s.refIdPortafolio > 0) {
                            idPortafolioCreado = s.refIdPortafolio;
                            s.mensajes.add(Mensajes.de(s.refIdPortafolio, "OK", "CREACION_PORTAFOLIO_OK", "Creacion de portafolio exitosa"));
                        }
                        // Se crea relacion entre el Contrato SAAPV y el Portafolio
                        int[] result = relacionarPortafolioContratoSAAPV(fila, idPortafolioCreado, fechaPeriodo);

                        if (result != null && result.length > 0 && result[0] == 1) {
                            s.mensajes.add(Mensajes.de(s.refIdPortafolio, "OK", "CREAR_PORTAFOLIO_CONTRATO_OK", "Se crea relacion entre portafolio y contrato traspaso."));
                        } else {
                            errorAsignarPortafolioContrato(s);
                        }
                    }

                    /* Ingreso Detalle Traspaso */
                    Long idPortafolioIngreso;
                    if (portafolioEncontrado) {
                        idPortafolioIngreso = matrizAgrupadorTAPVPort.get(0).IdPortafolio();
                    } else {
                        idPortafolioIngreso = idPortafolioCreado;
                    }
                    Long idInsertDetTraspaso = ingresarDetalleTraspaso(fila, idPortafolioIngreso);

                    if (idInsertDetTraspaso != null && idInsertDetTraspaso > 0) {
                        s.idResultado.add(idInsertDetTraspaso);
                        s.mensajes.add(Mensajes.de(idPortafolioIngreso, "OK", "CREAR_DETALLE_TRASPASO_OK", "Se crea detalle del traspaso nuevo."));
                    } else {
                        s.mensajes.add(Mensajes.de(6L, "ERROR", "ERROR_CREAR_DETALLE_TRASPASO", "Problema al ingresar el Detalle Traspaso."));
                        s.errores++;
                    }
                    // the legacy then re-reads the created portafolio and the client's portafolios (results unused): not repeated
                }
            }
        }
        // the counter of the answer is the legacy call(): getNumeroErrores(P_OUT_MENSAJES)
        return new ModificarPortafoliosTraspasoResultado(s.mensajes, Mensajes.numeroErrores(s.mensajes), s.idResultado);
    }

    private void errorModificarDetalleContrato(ModificarPortafoliosTraspasoEntrada in, Estado s, DetalleAEliminar detTraspaso) {
        HashMap<String, String> valores = new HashMap<>();
        valores.put("CodigoPortafolio", detTraspaso.CodigoPortafolio() != null ? detTraspaso.CodigoPortafolio() : "");
        valores.put("FolioContrato", detTraspaso.FolioContratoSAAPV() != null ? detTraspaso.FolioContratoSAAPV() : "");
        s.mensajes.add(Mensajes.conDiccionario(diccionario, in.idEmpresa(), detTraspaso.IdPortafolio(), Mensajes.MENSAJE_ERROR,
                "ERROR_AL_MOFIFICAR_DETALLE_CONTRATO", valores));
        s.errores++;
    }

    private static void errorAsignarPortafolioContrato(Estado s) {
        s.mensajes.add(Mensajes.de(6L, "ERROR", "ERROR_ASIGNAR_PORTAFOLIO_CONTRATO", "Problema al asignar relacion entre portafolio y contrato traspaso."));
        s.errores++;
    }

    /** Legacy: {@code ((Long) resultSP.get("p_out_ErroresContador")).equals(0)}: a missing counter is a NullPointerException, a missing detail list too. */
    private static void leerResultadoCrearPortafolio(JsonObject resultSP, Estado s) {
        Long erroresContador = Json.largo(resultSP, "p_out_ErroresContador");
        if (erroresContador.equals(Long.valueOf(0))) {
            s.refIdPortafolio = Json.largo(resultSP, "p_out_RefId");
        } else {
            // SalidaCliente.addError / addAdvertencia only fill a result object the legacy never returns: only the iteration (and its failures) is kept
            List<JsonObject> errores = resultSP.has("p_out_ErroresDetalle") ? Json.lista(resultSP, "p_out_ErroresDetalle") : null;
            for (JsonObject e : errores) {
                Json.texto(e, "Codigo");
            }
            List<JsonObject> advertencias = resultSP.has("p_out_AdvertenciasDetalle") ? Json.lista(resultSP, "p_out_AdvertenciasDetalle") : null;
            for (JsonObject a : advertencias) {
                Json.texto(a, "Codigo");
            }
        }
    }

    private JsonObject crearPortafolioAPV(ModificarPortafoliosTraspasoEntrada in, Fila fila, List<Fila> traspasos) {
        Map<String, Object> paramsIn = new TreeMap<>();
        paramsIn.put("IdEmpresa", in.idEmpresa());
        paramsIn.put("IdUsuario", in.idUsuario());
        paramsIn.put("IdModulo", in.idModulo());
        paramsIn.put("PropositoPortafolio", fila.IdPropositoPortafolio);
        if (fila.IdCliente.compareTo(0L) == 0) {
            List<ClienteContrato> clienteContrato = repositorio.clienteDelContrato(fila.IdContratoSAAPV);
            paramsIn.put("IdCliente", clienteContrato.get(0).IdCliente());
            paramsIn.put("IdPerfilComercial", clienteContrato.get(0).IdPerfilComercial());
        } else {
            paramsIn.put("IdCliente", fila.IdCliente);
            paramsIn.put("IdPerfilComercial", fila.IdPerfilComercial);
        }
        paramsIn.put("CodigoPortafolio", fila.RegimenTributario);
        paramsIn.put("CodigoRegimen", fila.RegimenTributario);
        paramsIn.put("NombrePlan", fila.NombrePlan);
        paramsIn.put("CodigoAgrupador", fila.CodigoAgrupador);

        List<PortafolioCliente> portPrevioSelect = repositorio.portafoliosDelCliente(traspasos.get(0).IdCliente);

        JsonObject resultSP = colaboradores.invocar("StoredProcedure",
                Json.objeto().poner("procedimiento", SP_CREAR_PORTAFOLIO).poner("entrada", paramsIn).construir());
        Long idPortCreado = Json.largo(resultSP, "p_out_RefId");

        List<PortafolioCreado> portNuevoSelect = repositorio.portafolioCreado(idPortCreado);

        List<PortafolioCliente> portPostSelect = repositorio.portafoliosDelCliente(traspasos.get(0).IdCliente);

        for (PortafolioCliente portafolioPrev : portPrevioSelect) {
            for (PortafolioCliente portafolioPost : portPostSelect) {
                if (portafolioPrev.IdPortafolio().compareTo(portafolioPost.IdPortafolio()) == 0) {
                    if (portafolioPrev.IdEstado().compareTo(portafolioPost.IdEstado()) != 0) {
                        modificarEstadoPortafolioTraspaso(portafolioPrev.IdPortafolio(), portafolioPrev.IdEstado());
                    }
                }
            }
        }

        // LEGACY DEFECT kept (see README APV-B-202): a failed procedure leaves no portafolio and get(0) throws IndexOutOfBoundsException
        if (portNuevoSelect.get(0).IdEstadoAutorizacion().compareTo("AUT") != 0 || portNuevoSelect.get(0).IdEstado().compareTo("ACT") != 0) {
            modificarEstadoPortafolioTraspaso(idPortCreado, "ACT");
        }
        return resultSP;
    }

    private boolean modificarEstadoPortafolioTraspaso(Long idPortafolio, String estado) {
        int[] modCon;
        if (estado.compareTo("ACT") == 0) {
            modCon = repositorio.cambiarEstadoPortafolio(idPortafolio, estado, "AUT");
        } else {
            modCon = repositorio.cambiarEstadoPortafolio(idPortafolio, "INA", "AUT");
        }
        return modCon.length > 0 && modCon != null;
    }

    private int eliminarPortafolioTraspaso(Long idPortafolio, Long idContratoSAAPV) {
        Long cantPortafolios = repositorio.contarOtrosContratosDelPortafolio(idPortafolio, idContratoSAAPV);
        if (cantPortafolios == 0) {
            int[] modCon = repositorio.cambiarEstadoPortafolio(idPortafolio, "INA", "AUT");
            if (modCon.length > 0 && modCon != null) {
                return 1;
            } else {
                return 2;
            }
        } else {
            return 0;
        }
    }

    private List<PortafolioDeMatriz> matrizAgrupadorTraspasoPortafolio(Fila fila) {
        // LEGACY DEFECT kept (see README APV-B-203): no matrix for (plan, producto, regimen): get(0) throws IndexOutOfBoundsException
        List<Long> matrizAgrupadorTAPV = repositorio.matricesAgrupador(fila.IdPlanTipoInversion, fila.IdProductoAgrupador, fila.IdRegimenTributario);
        return repositorio.portafoliosDeMatriz(matrizAgrupadorTAPV.get(0), fila.IdCliente);
    }

    private Long ingresarDetalleTraspaso(Fila fila, Long idPortafolio) {
        Long idDetalleTraspaso = secuencias.siguiente("BSCL.APV.BD.Secuencias::TRA.IdDetalleTraspaso");
        DetalleNuevo d = new DetalleNuevo();
        d.IdTraspaso = fila.IdTraspaso;
        d.IdDetalleTraspaso = idDetalleTraspaso;
        d.IdPortafolio = idPortafolio;
        d.FechaIngreso = fila.FechaIngreso;
        d.FechaModificacion = "0001-01-01";
        d.IdPlanTipoInversion = fila.IdPlanTipoInversion;
        d.IdProductoAgrupador = fila.IdProductoAgrupador;
        d.IdRegimenTributario = fila.IdRegimenTributario;
        d.MontoPeso = Double.valueOf(fila.MontoPeso.toString());
        d.MontoUF = Double.valueOf(fila.MontoUF.toString());
        d.Porcentaje = Double.valueOf(fila.Porcentaje.toString());
        d.IdEstado = "VIG";
        d.EsReapertura = fila.EsReapertura;
        int[] resultadoIngreso = repositorio.insertarDetalleTraspaso(d);
        if (resultadoIngreso.length > 0 && resultadoIngreso != null) {
            return idDetalleTraspaso;
        } else {
            return 0L;
        }
    }

    private int[] relacionarPortafolioContratoSAAPV(Fila fila, Long idPortafolio, String fechaPeriodo) {
        String origenAhorro = "";
        if (fila.RegimenTributario.equals("BF")) {
            origenAhorro = "BF";
        } else {
            if (fila.RegimenTributario.substring(0, 3).equals("APV")) {
                origenAhorro = "APV";
            }
            if (fila.RegimenTributario.substring(0, 2).equals("DC")) {
                origenAhorro = "DC";
            }
        }
        RelacionPortafolioContrato inPortCont = new RelacionPortafolioContrato();
        inPortCont.IdPortafolio = idPortafolio;
        inPortCont.IdContratoSAAPV = fila.IdContratoSAAPV;
        inPortCont.OrigenAhorro = origenAhorro;
        inPortCont.MontoPesos = Double.valueOf(fila.MontoPeso.toString());
        inPortCont.MontoUF = Double.valueOf(fila.MontoUF.toString());
        inPortCont.PorcentajeRegimenImponible = Double.valueOf(fila.Porcentaje.toString());
        inPortCont.Pago = "UNI";
        inPortCont.PeriodoInicio = fechaPeriodo;
        inPortCont.PeriodoTermino = fechaPeriodo;
        inPortCont.IdEstado = "VIG";
        return repositorio.relacionarPortafolioContrato(inPortCont);
    }

    private int[] actualizarDetalleTraspaso(Fila fila, Long idPortafolio) {
        DetalleModificado m = new DetalleModificado();
        m.IdDetalleTraspaso = fila.IdDetalleTraspaso;
        m.IdTraspaso = fila.IdTraspaso;
        m.IdPortafolio = idPortafolio;
        m.IdProductoAgrupador = fila.IdProductoAgrupador;
        m.IdRegimenTributario = fila.IdRegimenTributario;
        m.MontoPeso = fila.MontoPeso;
        m.MontoUF = fila.MontoUF;
        m.FechaIngreso = fila.FechaIngreso;
        return repositorio.actualizarDetalleTraspaso(m);
    }

    private Boolean validaPlanInversion(Fila fila) {
        Boolean respuesta = false;
        List<PlanPortafolio> validaTipoPlan = repositorio.planesDelCliente(fila.IdCliente);
        // valida si existen portafolios
        if (!validaTipoPlan.isEmpty()) {
            // valida si existen del mismo tipo de plan de inversion
            for (PlanPortafolio valPlan : validaTipoPlan) {
                Long idPlanTipoInversion = valPlan.IdPlanTipoInversion() != null ? valPlan.IdPlanTipoInversion().longValue() : 0L;
                idPlanTipoInversion = idPlanTipoInversion != null ? idPlanTipoInversion : 0L;
                if (idPlanTipoInversion.compareTo(fila.IdPlanTipoInversion) == 0) {
                    respuesta = true;
                }
            }
        } else {
            respuesta = true;
        }
        return respuesta;
    }
}
