package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


import cl.bcs.custodia.ConsultarCarteraDerivadosEjercicio;
import cl.bcs.custodia.ConsultarCarteraDerivadosEjercicio.Entrada_ConsultarEjercicio;
import cl.bcs.custodia.ConsultarCarteraDerivadosEjercicio.Salida_ConsultarEjercicio;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class CarteraOP implements BcsCartola{
	
	public final Log log = new Log("NOR.CarteraOP");
	
	public static class SalidaCarteraOP{
		public Long 		IdCliente;
		public Long 		IdPortafolio;	
		public String 		Nemotecnico;
		public String 		FechaVencimiento;
		public Long 		Posicion;
		public BigDecimal 	PrecioEjercicio;
		public String 		Moneyness;
		public String		ModalidadLiquidacion;
		public String 		TipoOpcion;
		public String		Estilo;
		public String		ModalidadEjercicio;
		public String 		Fecha;
		public String 		RazonSocial;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private static final Logger logger = LogManager.getLogger(CarteraOP.class);
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		logger.info("INICIO::CarteraOP");
		
		SalidaCartola out = new SalidaCartola();		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		ConsultarCarteraDerivadosEjercicio consultarCarteraDerivadosEjercicio = new ConsultarCarteraDerivadosEjercicio();
		Entrada_ConsultarEjercicio entrada_ConsultarEjercicio = new Entrada_ConsultarEjercicio();
		entrada_ConsultarEjercicio.p_IdEmpresa =  entrada.p_IdEmpresa;
		entrada_ConsultarEjercicio.p_IdUsuario =  entrada.p_IdUsuario;
		entrada_ConsultarEjercicio.p_IdPais = idPaisEmpresa;
		entrada_ConsultarEjercicio.p_IdModulo = entrada.p_IdModulo;
		entrada_ConsultarEjercicio.p_FechaConsulta = entrada.p_FechaHasta;
		entrada_ConsultarEjercicio.p_IdCliente = entrada.p_Datos.get(0).IdCliente.intValue();		
		entrada_ConsultarEjercicio.p_IdPortafolio =entrada.p_Datos.get(0).IdPortafolio;		
		entrada_ConsultarEjercicio.p_TipoDerivado = "OP";
		
		Salida_ConsultarEjercicio salidaOpciones = consultarCarteraDerivadosEjercicio.logica(con, entrada_ConsultarEjercicio);
		

	    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); 

		List<SalidaCarteraOP> salidas = new ArrayList<SalidaCarteraOP>();
		
		if (!salidaOpciones.P_OUT_EJERCICIOS.isEmpty()) {			
			for (int i=0; i < salidaOpciones.P_OUT_EJERCICIOS.size(); i++){
				SalidaCarteraOP salidaOP = new SalidaCarteraOP();
				salidaOP.Moneyness = salidaOpciones.P_OUT_EJERCICIOS.get(i).Moneyness;			    
				salidaOP.FechaVencimiento = dateFormat.format(salidaOpciones.P_OUT_EJERCICIOS.get(i).FechaVencimiento).toString();
				salidaOP.Nemotecnico = salidaOpciones.P_OUT_EJERCICIOS.get(i).Serie;
				salidaOP.PrecioEjercicio = salidaOpciones.P_OUT_EJERCICIOS.get(i).PrecioEjercicio;
				salidaOP.IdCliente = entrada.p_Datos.get(0).IdCliente;
				salidaOP.IdPortafolio =  entrada.p_Datos.get(0).IdPortafolio;
				salidaOP.Posicion	=salidaOpciones.P_OUT_EJERCICIOS.get(i).ContratosEjercibles.longValue();
				salidaOP.ModalidadLiquidacion =salidaOpciones.P_OUT_EJERCICIOS.get(i).ModalidadLiquidacion;
				salidaOP.TipoOpcion = salidaOpciones.P_OUT_EJERCICIOS.get(i).TipoOpcion;
				salidaOP.Estilo = salidaOpciones.P_OUT_EJERCICIOS.get(i).Estilo;
				salidaOP.ModalidadEjercicio = salidaOpciones.P_OUT_EJERCICIOS.get(i).ModalidadEjercicio;
				salidaOP.Fecha =  dateFormat.format(entrada.p_FechaHasta).toString();
				salidaOP.RazonSocial = salidaOpciones.P_OUT_EJERCICIOS.get(i).RazonSocial;				
				salidas.add(salidaOP);
			}
		}

		List<HashMap<String,Object>> resultadoCarteraOP = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("CarteraOP", resultadoCarteraOP);
		
		logger.info("FIN::CarteraOP");
		return out;
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2020-04-01");
		Objin.p_FechaHasta = Fecha.toDate("2020-04-21");
		Dato dato = new Dato();
		dato.IdCliente = 67760L;//123569L;//524891L;
		dato.IdPortafolio = 412766L;//27499L;//1451920L;
		Objin.p_Datos.add(dato);
		CarteraOP  demo= new CarteraOP();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
}
