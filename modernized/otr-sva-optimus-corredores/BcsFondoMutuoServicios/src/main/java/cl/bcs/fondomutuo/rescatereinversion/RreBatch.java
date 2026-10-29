package cl.bcs.fondomutuo.rescatereinversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.fondomutuo.agf.CrearSolicitudAgfService;
import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.port.out.http.BatchPort;

/**
 * FM-REI-35 (RR:1334-1392): the batch program of fiscal bonus of the reinversion (HTTP write, gate G5). Bug for bug (D-F2-9): it looks only at the FIRST
 * request of {@code solicitudesProcesar}, adds it to the AGF input AFTER AGF ran (no effect), adds its result a SECOND time to the output and, for an APV
 * portfolio, sends one call per FolioContrato with the ids of ALL the requests of {@code solicitudesProcesar} (rejected and grouped ones included).
 */
final class RreBatch {

    private RreBatch() {
    }

    /** Legacy {@code InscribirProcesoBatch.InscribirProcesoBatchEntrada} and its nested classes (field names and order; null keys written). */
    static class BatchEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<TipoPrograma> p_TipoPrograma;
        public List<Parametro> p_Parametro;
    }

    static class TipoPrograma {
        public Long IdInterno;
        public String IdTipoPrograma;
        public Long SegundosDesfase;
        public String Comentario = "";
    }

    static class Parametro {
        public Long IdInterno;
        public String NombreParametro;
        public String Valor;
        public String ValorBlob;

        Parametro(Long idInterno, String nombre, String valor, String blob) {
            this.IdInterno = idInterno;
            this.NombreParametro = nombre;
            this.Valor = valor;
            this.ValorBlob = blob;
        }
    }

    static void inscribir(RreContexto c, RreDatos datos, BatchPort batch, CrearSolicitudAgfService.Entrada entradaAgf) throws Exception {
        HashMap<Long, RreDatos.PortafolioAPV> hportafolioAPV = ListaHash.aHash(datos.portafoliosApv(c.portafolios), p -> p.IdPortafolio);
        List<Parametro> parametros = new ArrayList<>();
        SolicitudAporteRescate primera = c.solicitudesProcesar.get(0);
        if (RreConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(primera.EstadoProceso)) {
            if (RreConstantes.NO.equalsIgnoreCase(primera.SolicitudBatch)) {
                CrearSolicitudAgfService.Solicitudes s = new CrearSolicitudAgfService.Solicitudes();   // AGF already ran: no effect (D-F2-9)
                s.IdSolicitudAporteRescate = primera.IdSolicitudAporteRescate;
                entradaAgf.p_Solicitudes.add(s);
            }
            c.out.P_OUT_RESULTADO.add(c.hSolicitudSalida.get(primera.IdInterno));   // D-F2-9: the first result a second time
            RreDatos.PortafolioAPV papv = hportafolioAPV.get(primera.IdPortafolio);
            if (papv != null) {
                long contador = 0L;
                for (String ids : RreEscritura.idsPorFolio(c.solicitudesProcesar)) {
                    parametros.add(new Parametro(contador, "IdSolicitudAporteRescate", ids, null));
                    contador++;
                }
            }
        }
        for (Parametro param : parametros) {
            BatchEntrada e = new BatchEntrada();
            e.p_Parametro = new ArrayList<>();
            e.p_TipoPrograma = new ArrayList<>();
            TipoPrograma tp = new TipoPrograma();
            tp.IdInterno = 0L;
            tp.IdTipoPrograma = RreConstantes.PROGRAMA_BONIFICACION;
            tp.SegundosDesfase = 0L;
            e.p_TipoPrograma.add(tp);
            e.p_Parametro.add(new Parametro(0L, param.NombreParametro, param.Valor, null));
            e.p_Parametro.add(new Parametro(0L, "IdEmpresa", c.entrada.p_IdEmpresa.toString(), null));
            e.p_Parametro.add(new Parametro(0L, "IdUsuario", c.entrada.p_IdUsuario.toString(), null));
            e.p_IdEmpresa = c.entrada.p_IdEmpresa;
            e.p_IdUsuario = c.entrada.p_IdUsuario;
            e.p_IdModulo = c.entrada.p_IdModulo;
            batch.inscribir(JsonLegacy.conNulos().toJsonTree(e).getAsJsonObject());   // answer not read (D-NOR-12)
        }
    }
}
