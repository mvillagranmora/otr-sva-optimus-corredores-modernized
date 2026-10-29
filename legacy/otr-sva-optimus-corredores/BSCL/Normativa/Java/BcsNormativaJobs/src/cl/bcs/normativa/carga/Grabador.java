package cl.bcs.normativa.carga;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.icu.math.BigDecimal;

import cl.bcs.normativa.dal.SecuenciadorRegistro;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.utils.SqlBulk;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

/**
 * Grabador de los datos en el destino de la carga.
 *
 * @author fpino@email.com
 *
 */
final class Grabador extends SqlBulk {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Grabador.class.getName());

    /**
     * Arma el comando INSERT de grabado.
     *
     * @param tabla
     *            La tabla.
     * @param esquema
     *            El esquema del documento.
     * @return El comando INSERT de grabado.
     */
    public static String armarInsert(
            final Esquema esquema,
            final SqlTable tabla) {
        final List<String> names = new ArrayList<>();
        final List<String> values = new ArrayList<>();

        for (final Campo campo : esquema.getCampos()) {
            names.add(GENERATOR.getSqlText(campo.getNombre()));
            values.add("?");
        }

        final String sql = String.format("INSERT INTO %s(%s) VALUES(%s)",
                GENERATOR.getSqlText(tabla), StringUtils.join(",", names),
                StringUtils.join(",", values));
        LOGGER.info(sql);
        return sql;
    }

    /**
     * Contexto de carga.
     */
    private final ContextoCarga contexto;

    /**
     * Secuenciador de los registros grabados.
     */
    private final SecuenciadorRegistro secuenciador;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de carga.
     * @param tabla
     *            La tabla.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public Grabador(
            final ContextoCarga contexto,
            final SqlTable tabla) throws SQLException {
        super(contexto.getConnection(), armarInsert(contexto.getEsquema(), tabla),
                contexto.getOpciones().getBatchSize());
        this.contexto = contexto;
        this.secuenciador = new SecuenciadorRegistro(contexto.getConnection(), tabla);
    }

    /**
     * Recibe un registro para ser grabado.
     * <p>
     * Potencialmente, puede grabar un lote de registros, pues el grabado
     * est&aacute; implementado en modo bulk.
     *
     * @param registro
     *            El registro de datos a grabar.
     * @throws Exception 
     */
    public void grabar(
            final Registro registro)
                    throws Exception {
        registro.put(SecuenciadorRegistro.ID_COLUMN_NAME, secuenciador.getSiguiente());
        registro.put("IdDocumento", contexto.getDocumento().getId());
        registro.put("VboUsuario", "N");
        registro.put("Observaciones", "");
        registro.put("FuenteDatos", contexto.getOpciones().getTipoCarga().toString());


        if(contexto.getEsquema().getNombre().equals("DJ1") || contexto.getEsquema().getNombre().equals("DJ999")) {
        	String codigoUsuario = contexto.getUsuario().getCodigo() != null ? contexto.getUsuario().getCodigo() : "";
        	registro.put("UsrUltMod", codigoUsuario);
        	String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(contexto.getConnection(), contexto.getEmpresa().getId(), "PLATAFORMA", "PLA.FechaSistema");
        	registro.put("FecUltMod", fechaSistema);
        	String tipoArchivoDJ1 = contexto.getOpciones().getTipoArchivoDJ1() != null ? contexto.getOpciones().getTipoArchivoDJ1() : "FX";
        	
        	if(contexto.getEsquema().getNombre().equals("DJ1")){
        		registro.put("Estado", "V");
        		registro.put("kTipoArchivo", tipoArchivoDJ1);
        		registro.put("Rectifica","N");
        	}
        	
        	if(contexto.getEsquema().getNombre().equals("DJ999")) {
        		final long folioOperacion = (Long) registro.getPorNombre("IdOperacionSpot") == null ? 0: (Long) registro.getPorNombre("IdOperacionSpot");   
            	if(folioOperacion == 0) {
            		List<Long> secuenciasFolioOperacion = Utils.getSecuencia(contexto.getConnection(),"BSCL.Mesa.BD.Secuencias::SPT.IdOperacionSpot",1L);
            		registro.put("IdOperacionSpot", secuenciasFolioOperacion.get(0));
            	}
        		
        		final long folioPlanilla = (Long) registro.getPorNombre("NumeroOperacion") == null ? 0: (Long) registro.getPorNombre("NumeroOperacion");        		        
            	if(folioPlanilla == 0) {
            		List<Long> secuenciasFolioPlanilla = Utils.getSecuencia(contexto.getConnection(),"BSCL.Mesa.BD.Secuencias::SPT.FolioPlanillaSpot",1L);
            		registro.put("NumeroOperacion", secuenciasFolioPlanilla.get(0));
            	}
            	
            	String codigoInstitucion =  ConfiguracionSistema.getParametroConfiguracionString(contexto.getConnection(), contexto.getEmpresa().getId(), "MESA", "CODIGOCORREDORAOMA");
            	registro.put("CodigoInstitucion", Long.parseLong(codigoInstitucion));
            	registro.put("EstadoGeneracion", "N");
            	registro.put("IdEmpresa", contexto.getDocumento().getIdEmpresa());
            	registro.put("EsArbitraje", 0);
            }
        }

        final PreparedStatement bulkStatement = getBulkStatement();

        int i = 1;
        bulkStatement.clearParameters();
        for (final Campo campo : contexto.getEsquema().getCampos()) {
            final Object value = registro.get(campo.getNombre());
            bulkStatement.setObject(i++, value);
        }

        addBatch();
    }

}
