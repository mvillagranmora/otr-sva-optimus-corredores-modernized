package cl.bcs.apv.application.contratos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.AsociarPerfiles;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.asociarperfiles.AsociarPerfilesAPVIn;
import cl.bcs.apv.api.dto.asociarperfiles.AsociarPerfilesEntrada;
import cl.bcs.apv.api.dto.asociarperfiles.AsociarPerfilesResultado;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.Secuencias;
import cl.bcs.apv.domain.port.out.contratos.PerfilComercialRepositorio;
import cl.bcs.apv.domain.port.out.contratos.PerfilComercialRepositorio.PerfilInsertar;
import cl.bcs.apv.domain.port.out.contratos.PerfilComercialRepositorio.ValidacionFila;

/** Use case {@code AsociarPerfiles} (legacy {@code cl.bcs.APV.AsociarPerfilesAPV}). */
@Transactional(rollbackFor = Exception.class)
public class AsociarPerfilesServicio implements AsociarPerfiles {

    private static final String SECUENCIA = "BSCL.APV.BD.Secuencia::APV.IdPerfilComercialAPV";

    private final PerfilComercialRepositorio repositorio;
    private final Secuencias secuencias;

    public AsociarPerfilesServicio(PerfilComercialRepositorio repositorio, Secuencias secuencias) {
        this.repositorio = repositorio;
        this.secuencias = secuencias;
    }

    @Override
    public AsociarPerfilesResultado ejecutar(AsociarPerfilesEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        logica(in, mensajes);
        // legacy call(): the counter is set after logica
        return new AsociarPerfilesResultado(mensajes, Mensajes.numeroErrores(mensajes));
    }

    private void logica(AsociarPerfilesEntrada in, List<Mensaje> out) {
        if (in.asociarPerfilesAPVIn().size() == 0) {
            out.add(Mensajes.de(0L, Mensajes.MENSAJE_ERROR, "NO_SE_INGRESARON_PARAMETROS", "NO_SE_INGRESARON_PARAMETROS"));
            return;
        }

        HashMap<String, Boolean> perfilFiltro = new HashMap<>();
        HashMap<String, Boolean> categoriaFiltro = new HashMap<>();
        HashMap<String, Boolean> abiertoFiltro = new HashMap<>();
        HashMap<String, Boolean> activoFiltro = new HashMap<>();
        List<String[]> perfiles = new ArrayList<>();
        List<String> categorias = new ArrayList<>();
        List<String> abiertos = new ArrayList<>();
        List<String> activos = new ArrayList<>();

        for (AsociarPerfilesAPVIn perfil : in.asociarPerfilesAPVIn()) {
            if (perfil.idCategoriaInversionista() == null || "".equals(perfil.idCategoriaInversionista())) {
                out.add(Mensajes.de(0L, Mensajes.MENSAJE_ERROR, "CATEGORIA_INVERSIONISTA_VACIO", "CATEGORIA_INVERSIONISTA_VACIO"));
                return;
            }
            if (perfil.idPerfilComercialAbierto() == null || "".equals(perfil.idPerfilComercialAbierto())) {
                out.add(Mensajes.de(0L, Mensajes.MENSAJE_ERROR, "PERFIL_COMERCIAL_ABIERTO_VACIO", "PERFIL_COMERCIAL_ABIERTO_VACIO"));
                return;
            }
            if (perfil.idPerfilComercialActivo() == null || "".equals(perfil.idPerfilComercialActivo())) {
                out.add(Mensajes.de(0L, Mensajes.MENSAJE_ERROR, "PERFIL_COMERCIAL_ACTIVO_VACIO", "PERFIL_COMERCIAL_ACTIVO_VACIO"));
                return;
            }
            String hash = perfil.idCategoriaInversionista() + "_" + perfil.idPerfilComercialAbierto() + "_" + perfil.idPerfilComercialActivo();
            if (!perfilFiltro.containsKey(hash)) {
                perfilFiltro.put(hash, true);
                perfiles.add(new String[] { perfil.idCategoriaInversionista(), perfil.idPerfilComercialAbierto(), perfil.idPerfilComercialActivo() });
            }
            if (!categoriaFiltro.containsKey(perfil.idCategoriaInversionista())) {
                categoriaFiltro.put(perfil.idCategoriaInversionista(), true);
                categorias.add(perfil.idCategoriaInversionista());
            }
            if (!abiertoFiltro.containsKey(perfil.idPerfilComercialAbierto())) {
                abiertoFiltro.put(perfil.idPerfilComercialAbierto(), true);
                abiertos.add(perfil.idPerfilComercialAbierto());
            }
            if (!activoFiltro.containsKey(perfil.idPerfilComercialActivo())) {
                activoFiltro.put(perfil.idPerfilComercialActivo(), true);
                activos.add(perfil.idPerfilComercialActivo());
            }
        }

        List<ValidacionFila> validaciones = repositorio.validaciones(in.idEmpresa(), "VIG", categorias, abiertos, activos);
        if (validaciones.size() > 0) {
            for (ValidacionFila validacion : validaciones) {
                out.add(Mensajes.de(0L, Mensajes.MENSAJE_ERROR, validacion.Codigo(), validacion.Codigo() + " : " + validacion.Descripcion()));
            }
            if (Mensajes.numeroErrores(out) > 0L) {
                return;
            }
        }

        if (perfiles.size() > 0) {
            // the legacy drew all the ids in one call (getSecuencia with cantidad = n)
            List<Long> ids = new ArrayList<>();
            for (int n = 0; n < perfiles.size(); n++) {
                ids.add(secuencias.siguiente(SECUENCIA));
            }
            int i = 0;
            List<PerfilInsertar> filas = new ArrayList<>();
            for (String[] p : perfiles) {
                // LEGACY DEFECT kept (see README APV-A, AsociarPerfilesAPV): i is never incremented, every row gets the first id of the sequence
                filas.add(new PerfilInsertar(ids.get(i), in.idEmpresa(), p[0], p[1], p[2], "VIG"));
            }
            repositorio.insertar(filas);
        }
    }
}
