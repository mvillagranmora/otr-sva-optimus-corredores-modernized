package cl.bcs.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// No utilizar servicios, solo para pruebas (cl.bcs.utils si se puede)
//import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class ValidarNuevaClave {
	public static String queryParametros = "SELECT \"CodigoParametro\", \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"IdEmpresa\" = :p_IdEmpresa AND \"CodigoParametro\" IN ( 'ACC.numLongitud', 'ACC.numMayusculas', 'ACC.numMinuscula', 'ACC.numCaracteres' )";

	/***********************************************************************************************
	 * CLASES
	 ***********************************************************************************************/
	
	public static class EstadoCriteriosNuevaClave {
		public String criterio;
		public boolean esValido;
		
		public EstadoCriteriosNuevaClave(String criterio, boolean estaValidado) {
			this.criterio = criterio;
			this.esValido = estaValidado;
		}
	}

	public static class EntradaValidarNuevaClave {
		public String p_ClaveNueva;
		public Long p_IdEmpresa;
		boolean p_contieneProhibidas;
		
		public EntradaValidarNuevaClave(String claveNueva, Long idEmpresa) {
			this.p_ClaveNueva = claveNueva;
			this.p_IdEmpresa = idEmpresa;
		}
	}


	public static class SalidaValidarNuevaClave {
		public boolean ClaveValida; 
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_ERRORESDETALLE;
		public List<EstadoCriteriosNuevaClave> P_OUT_ESTADOCRITERIOSNUEVACLAVE;

		public SalidaValidarNuevaClave() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_ERRORESDETALLE = new ArrayList<>();
			P_OUT_ESTADOCRITERIOSNUEVACLAVE = new ArrayList<>();
		}
	}
	
	public static class ParametroSistema {
		public String CodigoParametro; 
		public String Valor;
	}
	
	public static class MensajeSalida {
		public Long id;
		public String tipo;	//ERROR,AUTORIZACION,ADVERTENCIA,INFORMACION
		public String codigo;
		public String descripcion;
		
		public static final String MENSAJE_OK = "OK";
		public static final String MENSAJE_ERROR = "ERROR";
		public static final String MENSAJE_ADVERTENCIA = "ADVERTENCIA";
		public static final String MENSAJE_AUTORIZACION = "AUTORIZACION";
		public static final String MENSAJE_INFORMACION = "INFORMACION";
		public static final Object MENSAJE_RECHAZO = "RECHAZO"; //TODO ELIMINAR DE REQUISITOS OPERATIVOS
		
		public MensajeSalida() {
			
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public MensajeSalida(Long id,String tipo,String codigo,String descripcion) {
			this.id = id;
			this.tipo = tipo;
			this.codigo = codigo;
			this.descripcion = descripcion;
		}
		
		public static long getNumeroErrores(List<MensajeSalida> salida) {
			int c = 0;
			for (MensajeSalida s : salida) {
				if (s.tipo == MENSAJE_ERROR)
					c++;
			}
			return c;
		}
		
	}
	
	// Debido a proximo cambio de modulo de esta pieza, se solicita colocar la query en duro.
	private static String SQL_SELECT_PALABRA_PROHIBIDA = "SELECT\r\n" + 
														 "\"Palabra\" as \"PalabraProhibida\"\r\n" + 
														 "FROM \"BSCL.Plataforma.BD::ACC.PasswordProhibidas\"\r\n" + 
														 "WHERE \"BSCL.Plataforma.BD::ACC.PasswordProhibidas\".\"Palabra\" = :p_ClaveNueva;";
		
	public static SalidaValidarNuevaClave logica(Connection con, EntradaValidarNuevaClave entrada) throws Exception {
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("p_IdEmpresa", 1L);
		System.out.println(queryParametros);
		List<ParametroSistema> parametrosSistema = sql.query(con, queryParametros, params, ParametroSistema.class);
		
		Long Mayusculas = Long.parseLong(getParametroSistema(parametrosSistema, "ACC.numMayusculas"));
		Long Minuscula = Long.parseLong(getParametroSistema(parametrosSistema, "ACC.numMinuscula"));
		Long Caracteres = Long.parseLong(getParametroSistema(parametrosSistema, "ACC.numCaracteres"));
		Long Longitud = Long.parseLong(getParametroSistema(parametrosSistema, "ACC.numLongitud"));
		
		char clave;
		String password = entrada.p_ClaveNueva;
		byte contCaracteres = 0, contLetraMay = 0, contLetraMin = 0;
		//char claveAnterior = ' ';
		boolean ClaveValida = true;
		EstadoCriteriosNuevaClave estado;
		
		SalidaValidarNuevaClave salida = new SalidaValidarNuevaClave();
		Long contadorErrores = 0L;
		String descripcion;
		
		// Validar contra diccionario de palabras prohibidas
    	descripcion = String.format("La clave ingresada no puede contener palabras prohibidas.");
	    estado = new EstadoCriteriosNuevaClave(descripcion, true);
	    
	    // obtener cantidad de palabras prohibidas > 0, validacion mala
		HashMap<String, Object> consultaPalabraProhibida = new HashMap<>();
		consultaPalabraProhibida.put("p_ClaveNueva", entrada.p_ClaveNueva);
		long countPalabrasProhibidasEncontradas = sql.countQuery(con, SQL_SELECT_PALABRA_PROHIBIDA, consultaPalabraProhibida);	
	    
	    if(countPalabrasProhibidasEncontradas > 0) {
			salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", descripcion));
			
			ClaveValida = false;
			contadorErrores = contadorErrores + 1L;
			estado.esValido = false;
	    }
		salida.P_OUT_ESTADOCRITERIOSNUEVACLAVE.add(estado);
		
	    
		// Validar Caracteres y longitud.
		descripcion = String.format("M\u00ednimo %d car\u00e1cteres", Longitud);
		Long longitudClaveentrada = (long) entrada.p_ClaveNueva.length();
		
		estado = new EstadoCriteriosNuevaClave(descripcion, true);
		if (longitudClaveentrada < Longitud) {
			salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", descripcion));
			
			ClaveValida = false;
			contadorErrores = contadorErrores + 1L;
			estado.esValido = false;
		}
		salida.P_OUT_ESTADOCRITERIOSNUEVACLAVE.add(estado);

		
		// Validar Caracteres invalidos.
		descripcion = "Car\u00e1cteres inv\u00e1lidos";
		estado = new EstadoCriteriosNuevaClave("Car\u00e1cteres v\u00e1lidos", true);
		for (byte i = 0; i < longitudClaveentrada; i++) {
			clave = password.charAt(i);
			
			String passValue = String.valueOf(clave);
			if (passValue.matches("[A-Z]")) {
				contLetraMay++;
			} else if (passValue.matches("[a-z]")) {
				contLetraMin++;
			} else if (passValue.matches("\\W")) {
				contCaracteres++;
			} else if (passValue.equals(" ")) {
				salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", descripcion));
				ClaveValida = false;
				contadorErrores = contadorErrores + 1L;
				estado.esValido = false;
			}
			
			//claveAnterior = clave;
		}
		salida.P_OUT_ESTADOCRITERIOSNUEVACLAVE.add(estado);
		

		// Validar cantidad mayusculas.
		descripcion = String.format("Al menos %d may\u00fascula", Mayusculas);
		estado = new EstadoCriteriosNuevaClave(descripcion, true);
		if (contLetraMay < Mayusculas) {

			salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", descripcion));
			ClaveValida= false;
			contadorErrores = contadorErrores + 1L;
			estado.esValido = false;
		}
		salida.P_OUT_ESTADOCRITERIOSNUEVACLAVE.add(estado);
		

		// Validar cantidad minusculas.
		descripcion = String.format("Al menos %d min\u00fascula", Minuscula);
		estado = new EstadoCriteriosNuevaClave(descripcion, true);
		if (contLetraMin < Minuscula){
			
			salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", descripcion));
			ClaveValida= false;
			contadorErrores = contadorErrores + 1L;
			estado.esValido = false;
		}
		salida.P_OUT_ESTADOCRITERIOSNUEVACLAVE.add(estado);

		
		// Validar cantidad caracteres especiales.
		descripcion = String.format("Al menos %d car\u00e1cter especial", Caracteres);
		estado = new EstadoCriteriosNuevaClave(String.format(descripcion, Caracteres), true);
		if (contCaracteres < Caracteres){

			salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", descripcion));
			ClaveValida= false;
			contadorErrores = contadorErrores + 1L;
			estado.esValido = false;
		}
		salida.P_OUT_ESTADOCRITERIOSNUEVACLAVE.add(estado);
		
		salida.P_OUT_ERRORESCONTADOR = contadorErrores;
		salida.ClaveValida = ClaveValida;
		return salida;

	}
	
	public static String getParametroSistema(List<ParametroSistema> parametros, String parametroStr) {
		for (ParametroSistema parametro: parametros) {
			if (parametro.CodigoParametro.equals(parametroStr))
				return parametro.Valor;
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		/*
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			String claveNueva = "Bolsa123+*";
			Long idEmpresa = 1L;
			EntradaValidarNuevaClave entradaValidacion = new EntradaValidarNuevaClave(claveNueva, idEmpresa);

			try {
				SalidaValidarNuevaClave salidaValidacion = ValidarNuevaClave.logica(con, entradaValidacion);
				System.out.println(salidaValidacion);
				
				con.rollback();
				//con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				// con.commit();
			}

			con.close();
			
			
		} catch (Exception e) {
			System.out.println("== F ==");
		}
		System.out.println("== END ==");
		*/
	}

}
