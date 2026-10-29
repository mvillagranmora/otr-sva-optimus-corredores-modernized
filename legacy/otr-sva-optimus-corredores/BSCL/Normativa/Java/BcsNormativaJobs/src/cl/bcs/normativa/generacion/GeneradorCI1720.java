package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroCI1720;

/**
 * Implementacion del generador de la CI1720.
 * 
 * @author cespinosa
 *
 */
public final class GeneradorCI1720 extends Generador {

	private static final int LARGO_REGISTRO = 262;
	
	
	private long totalRegistros;
	/**
	 * Constructor.
	 * 
	 * @param contexto
	 * 				El contexto de generacion.
	 * @throws SQLException
	 * 				En caso de error SQL.
	 * @throws IOException
	 * 				En caso de error I/O.
	 * @throws SerializadorException
	 * 				En caso de error al serializar.
	 */
	public GeneradorCI1720(final ContextoGeneracion contexto)
			throws SQLException, IOException, SerializadorException {
		super(contexto);
		this.totalRegistros = 0;
		
	}
	
	/**
	 * 
	 * @param registroCI1720
	 */
	private void actualizarAcumuladores(final RegistroCI1720 registroCI1720) {
		this.totalRegistros += 1;
	}
	
	@Override
	public void generarDetalle(final Registro registro) throws SerializadorException {
		println(generarRegistroTipo2(registro));
	}
	
	@Override
	public void generarEncabezado() throws SerializadorException {
		println(generarRegistroTipo1());
	}
	
	@Override
	public void generarResumen() throws SerializadorException {
		println(generarRegistroTipo3());
	}
	
	@Override
	public String generarRegistroTipo0() throws SerializadorException {
		//String ret = "";
		return "";
	}
	
	private String fechaActual() {
		final DateFormat df = new SimpleDateFormat("yyyyMMdd", new Locale("es"));
        final String ret = df.format(new Date());
        return ret.toUpperCase(new Locale("es"));
	}
	
	/**
	 * Genera registro tipo 1.
	 * @param 
	 * @return
	 * @throws SerializadorException
	 * 				En caso de error al serializar.
	 */
	public String generarRegistroTipo1() throws SerializadorException {
		// Tipo de Registro.
		String ret = "1";
		
		//Rut Declarante con digito verificador.
		ret += String.format("%10s", Serializador.serializarRut(getContexto().getRutDeclarante())).replace(" ", "0");
		
		//Fecha de presentacion. Nota: Revisar el formato
		ret += String.format("%8s", fechaActual()).substring(0, 8);
		
		//Nombre o Razon Social.
		ret += String.format("%-60s", getContexto().getRazonSocial());
		
		//Fille. Espacios
		ret += Serializador.espacios(183);
		
		if (ret.length() != LARGO_REGISTRO) {
			throw new SerializadorException("Largo invalido registro tipo 1");
		}
		
		return ret;
	}
	
	/**
	 * 
	 * @param registro
	 * @return Registro tipo 2
	 * @throws SerializadorException
	 * 				En caso de error al serializar
	 */
	public String generarRegistroTipo2(final Registro registro)
			throws SerializadorException {
		
		final RegistroCI1720 registroCI1720 = new RegistroCI1720(registro);
		
		actualizarAcumuladores(registroCI1720);
		
		//Tipo de Registro.
		String ret = "2";
		
		//Nemotecnico.
		ret += String.format("%-20s", registroCI1720.getNemotecnico()).substring(0, 20);
		
		//Nombre Emisor.
		ret += String.format("%-30s", registroCI1720.getNombreEmisor()).substring(0, 30);
		
		//Tipo Instrumento.
		ret += String.format("%-10s", registroCI1720.getTipoInstrumento()).substring(0, 10);
		
		//Cantidad Unidades Nominal.
		ret += String.format("%020.04f", registroCI1720.getCantidadUnidadesNominal()).replace(".", "");
		
		//Tipo Unidades.
		ret += String.format("%-15s", registroCI1720.getTipoUnidades()).substring(0, 15);
		
		//Valorizacion.
		ret += String.format("%015d", registroCI1720.getValorizacion());
		
		//Cartera Propia.
		ret += String.format("%020.04f", registroCI1720.getCarteraPropia()).replace(".", "");
		
		//Custodia de Terceros.
		ret += String.format("%020.04f", registroCI1720.getCustodiaTerceros()).replace(".", "");
		
		//Custodia Propia en DCV.
		ret += String.format("%020.04f", registroCI1720.getCustodiaPropiaDCV()).replace(".", "");
		
		//Custodia Terceros DCV.
		ret += String.format("%020.04f", registroCI1720.getCustodiaTercerosDCV()).replace(".", "");
		
		//Custodia en Sociedad Emisora.
		ret += String.format("%020.04f", registroCI1720.getCustodiaSociedadEmisora()).replace(".", "");
		
		//Custodia en el Intermediario.
		ret += String.format("%020.04f", registroCI1720.getCustodiaIntermediario()).replace(".", "");
		
		//Custodia en Bolsas.
		ret += String.format("%020.04f", registroCI1720.getCustodiaBolsas()).replace(".", "");
		
		// Custodia en Otros.
		ret += String.format("%020.04f", registroCI1720.getCustodiaOtros()).replace(".", "");
		
		if(ret.length() != LARGO_REGISTRO) {
			throw new SerializadorException("Largo invalido registro tipo 2");
		}
		
		return ret;
	}
	
	/**
	 * 
	 * @return Registro tipo 3
	 * @throws SerializadorException
	 * 				En caso de error al serializar
	 */
	public String generarRegistroTipo3() throws SerializadorException {
		
		//Tipo de Registro.
		String ret = "3";
		
		//Total de Registros.
		ret += String.format("%010d", totalRegistros);
		
		//Filler. Espacios.
		ret += Serializador.espacios(251);
		
		if (ret.length() != LARGO_REGISTRO) {
			throw new SerializadorException("Largo invalido registro tipo 3");
		}
		
		return ret;
	}
}
