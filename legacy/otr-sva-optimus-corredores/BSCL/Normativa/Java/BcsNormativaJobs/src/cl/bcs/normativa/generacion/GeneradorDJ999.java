package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.util.Date;
import java.sql.SQLException;

import cl.bcs.normativa.dal.ConfiguracionDAO;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ999;

/**
 * Implementaci&oacute;n del generador de la DJ999.
 *
 */
public final class GeneradorDJ999 extends Generador {

	/**
	 * Registro resumen de la DJ999.
	 */
	private RegistroDJ999 registroResumen;
	private long totalRegistros;
	private String delimitador = "";

	/**
	 * Constructor.
	 *
	 * @param contexto El contexto de generaci&oacute;n.
	 * @throws SQLException          En caso de error SQL.
	 * @throws IOException           En caso de error de I/O.
	 * @throws SerializadorException En caso de error al serializar.
	 */
	public GeneradorDJ999(final ContextoGeneracion contexto) throws SQLException, IOException, SerializadorException {
		super(contexto);
		this.totalRegistros = 0;
		this.registroResumen = new RegistroDJ999();
		this.delimitador = ConfiguracionDAO.buscarPorEmpresaMasCodigo(contexto.getConnection(), contexto.getSchema(),
				contexto.getEmpresa().getId(), "DJ999_pDelimitador");
	}

	@Override
	public void generarEncabezado() throws SerializadorException {
		// No se implementa
	}

	@Override
	public void generarEncabezadoRegistro(final Registro registro, String tipoArchivo, String periodicidad,
			String rutDeclarante, Date fechaHasta) throws SerializadorException {
		// No se implementa
	}

	@Override
	public void generarDetalle(final Registro registro) throws SerializadorException {
		println(generarRegistroTipo2(registro));
	}
	
	 @Override
	    public String generarRegistroTipo0() throws SerializadorException {
		 return "";
	 }


	public String generarRegistroTipo2(final Registro registro) throws SerializadorException {
		final RegistroDJ999 registroDJ999 = new RegistroDJ999(registro);

		String ret = "";
		// OPERACION
		ret += registroDJ999.getEventoReporte();
		
		ret += delimitador + registroDJ999.getCodigoInstitucion();
		
		ret += delimitador + registroDJ999.getNumeroOperacion();
		
		String fechaOperacion =  registroDJ999.getFechaOperacion().toString().equals("0001-01-01") ? "" : registroDJ999.getFechaOperacion().toString();
		ret += delimitador + fechaOperacion.replace("-", "");
		
		ret += delimitador + registroDJ999.getTipoDocumento();
		
		ret += delimitador + registroDJ999.getCodigoComercio();
		
		ret += delimitador + registroDJ999.getIdPais();
		
		ret += delimitador + registroDJ999.getMonedaOperacion();
		
		ret += delimitador + String.format("%.2f", registroDJ999.getMontoOperacion());
		
		ret += delimitador + String.format("%.2f", registroDJ999.getMontoDolar());
		
		ret += delimitador + String.format("%.2f", registroDJ999.getTipoCambio());
		
		ret += delimitador + String.format("%.2f", registroDJ999.getMontoEquiClp());
		
		ret += delimitador + registroDJ999.getAfectoADerivado();
		
		ret += delimitador + registroDJ999.getDocumentoIdentidad().replace("-", "");
		
		ret += delimitador + registroDJ999.getNombreCompleto();
		
		// AUTORIZACION
		ret += delimitador + registroDJ999.getTipoAutorizacion();
		
		String numeroAutorizacion = registroDJ999.getNumeroAutorizacion() == 0 ? "": String.valueOf(registroDJ999.getNumeroAutorizacion());
		ret += delimitador + numeroAutorizacion;
		
		String fechaAutorizacion = registroDJ999.getFechaAutorizacion().toString().equals("0001-01-01") ? "" : registroDJ999.getFechaAutorizacion().toString();
		ret += delimitador + fechaAutorizacion.replace("-", "");;
		
		// CREDITO EXTERNO
		String sectorEconomicoBeneficiario = registroDJ999.getSectorEconomicoBeneficiario() == 0 ? "": String.valueOf(registroDJ999.getSectorEconomicoBeneficiario());
		ret += delimitador + sectorEconomicoBeneficiario;
		
		String numeroInscripcion = registroDJ999.getNumeroInscripcion() == 0 ? "": String.valueOf(registroDJ999.getNumeroInscripcion());
		ret += delimitador + numeroInscripcion;
		
		String fechaDesembolso = registroDJ999.getFechaDesembolso().toString().equals("0001-01-01") ? "" : registroDJ999.getFechaDesembolso().toString();
		ret += delimitador +  fechaDesembolso.replace("-", "");
		
		String sectorEconomicoInversionista = registroDJ999.getSectorEconomicoInversionista() == 0 ? "": String.valueOf(registroDJ999.getSectorEconomicoInversionista());
		ret += delimitador + sectorEconomicoInversionista;
		
		ret += delimitador + registroDJ999.getNombreFinancistaExtranjero();
		
		String fechaVencimientoCapital = registroDJ999.getFechaVencimientoCapital().toString().equals("0001-01-01") ? "" : registroDJ999.getFechaVencimientoCapital().toString();
		ret += delimitador + fechaVencimientoCapital.replace("-", "");
		
		// DERIVADOS
		String nroContDerivado = registroDJ999.getNroContDerivado() == 0 ? "": String.valueOf(registroDJ999.getNroContDerivado());
		ret += delimitador + nroContDerivado;
		
		String fechaSuscDerivado = registroDJ999.getFechaSuscDerivado().toString().equals("0001-01-01") ? "" : registroDJ999.getFechaSuscDerivado().toString();
		ret += delimitador + fechaSuscDerivado.replace("-", "");
		
		String fechaVencDerivado = registroDJ999.getFechaVencDerivado().toString().equals("0001-01-01") ? "" : registroDJ999.getFechaVencDerivado().toString();
		ret += delimitador + fechaVencDerivado.replace("-", "");
		// OPERACION
		ret += delimitador + registroDJ999.getObservacion();

		return ret;
	}

	@Override
	public void generarResumen() throws SerializadorException {
		// No se implementa
	}
}
