package cl.bcs.apv.application.contratos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CargarContratosMasivo;
import cl.bcs.apv.api.casouso.GrabarContratos;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cargarcontratosmasivo.CargaArchivo;
import cl.bcs.apv.api.dto.cargarcontratosmasivo.CargarContratosMasivoEntrada;
import cl.bcs.apv.api.dto.cargarcontratosmasivo.CargarContratosMasivoResultado;
import cl.bcs.apv.api.dto.cargarcontratosmasivo.ParametrosContrato;
import cl.bcs.apv.api.dto.grabarcontratos.GrabarContratosEntrada;
import cl.bcs.apv.api.dto.grabarcontratos.GrabarContratosResultado;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.Secuencias;
import cl.bcs.apv.domain.port.out.contratos.CargaMasivaRepositorio;
import cl.bcs.apv.domain.port.out.contratos.CargaMasivaRepositorio.CargaArchivoFila;

/** Use case {@code CargarContratosMasivo} (legacy {@code cl.bcs.APV.CargaMasivaArchivo}): the file header and the contract recording of the file. */
@Transactional(rollbackFor = Exception.class)
public class CargarContratosMasivoServicio implements CargarContratosMasivo {

    private static final String SECUENCIA = "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva";

    private final CargaMasivaRepositorio repositorio;
    private final ConfiguracionSistema configuracion;
    private final Secuencias secuencias;
    private final GrabarContratos grabarContratos;

    public CargarContratosMasivoServicio(CargaMasivaRepositorio repositorio, ConfiguracionSistema configuracion, Secuencias secuencias,
            GrabarContratos grabarContratos) {
        this.repositorio = repositorio;
        this.configuracion = configuracion;
        this.secuencias = secuencias;
        this.grabarContratos = grabarContratos;
    }

    @Override
    public CargarContratosMasivoResultado ejecutar(CargarContratosMasivoEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        List<Long> idsCargaMasiva = new ArrayList<>();
        String fechaSistema = Parametros.fechaSistema(configuracion, in.idEmpresa());
        try {
            if (in.archivo().size() > 0) {
                // the legacy drew the ids in one call (getSecuencia with cantidad = n) and used the first
                List<Long> idsCargaArchivo = new ArrayList<>();
                for (int n = 0; n < in.archivo().size(); n++) {
                    idsCargaArchivo.add(secuencias.siguiente(SECUENCIA));
                }
                // only the first file row receives the id and the date (the legacy set them on the first element of the input list)
                List<CargaArchivoFila> filas = new ArrayList<>();
                for (int i = 0; i < in.archivo().size(); i++) {
                    CargaArchivo a = in.archivo().get(i);
                    Long id = i == 0 ? idsCargaArchivo.get(0) : a.idCargaMasiva();
                    String fecha = i == 0 ? fechaSistema : a.fechaCarga();
                    filas.add(new CargaArchivoFila(a.idEmpresa(), id, a.nombreCargaMasiva(), a.cantidadRegistros(), a.idUsuario(), a.idTipoCarga(),
                            a.descripcion(), fecha, a.idEstado()));
                }
                int[] res = repositorio.insertarArchivos(filas);
                if (res != null && res.length > 0 && res[0] == 1) {
                    idsCargaMasiva.add(idsCargaArchivo.get(0));
                    if (in.parametrosContratos().size() > 0) {
                        List<cl.bcs.apv.api.dto.grabarcontratos.ParametrosContrato> parametros = new ArrayList<>();
                        for (ParametrosContrato p : in.parametrosContratos()) {
                            parametros.add(RegistroReflejo.convertir(p, cl.bcs.apv.api.dto.grabarcontratos.ParametrosContrato.class,
                                    Map.of("idCargaMasiva", idsCargaArchivo.get(0))));
                        }
                        GrabarContratosEntrada entrada = new GrabarContratosEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "I", parametros,
                                convertirLista(in.distribucionInversion(), cl.bcs.apv.api.dto.grabarcontratos.DistribucionInversion.class),
                                convertirLista(in.portafoliosModificados(), cl.bcs.apv.api.dto.grabarcontratos.PortafoliosModificados.class),
                                convertirLista(in.detalleOrigenTraspaso(), cl.bcs.apv.api.dto.grabarcontratos.DetalleOrigenTraspaso.class));
                        GrabarContratosResultado paramsalida = grabarContratos.ejecutar(entrada);
                        // the legacy copied the messages when logica's counter was null, which it always is (only call() sets it): always copied
                        for (Mensaje mensaje : paramsalida.mensajes()) {
                            mensajes.add(new Mensaje(mensaje.id(), mensaje.tipo(), mensaje.codigo(), mensaje.descripcion()));
                        }
                    } else {
                        mensajes.add(Mensajes.de(1L, "ERROR", "CARGA_CONTRATO_ERROR", "La tabla de contrato viene vacia."));
                    }
                } else {
                    mensajes.add(Mensajes.de(1L, "ERROR", "CARGA_MASIVA_ERROR", "Carga masiva de contratos con problemas de ingreso."));
                }
            }
        } catch (Exception error) {
            mensajes.add(Mensajes.de(1L, "ERROR", "CARGA_CONTRATOS_ERROR", cl.bcs.apv.application.comun.Sneaky.causa(error).getMessage()));
        }
        // legacy call(): the counter is set after logica
        return new CargarContratosMasivoResultado(mensajes, Mensajes.numeroErrores(mensajes), idsCargaMasiva);
    }

    private static <R extends Record> List<R> convertirLista(List<? extends Record> origen, Class<R> destino) {
        List<R> lista = new ArrayList<>();
        for (Record r : origen) {
            lista.add(RegistroReflejo.convertir(r, destino, Map.of()));
        }
        return lista;
    }
}
