package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.custodia.ConsultarCarteraDerivadosEjercicio;
import cl.bcs.custodia.ConsultarCarteraDerivadosEjercicio.ConsultarEjercicio;
import cl.bcs.custodia.ConsultarCarteraDerivadosEjercicio.Entrada_ConsultarEjercicio;
import cl.bcs.custodia.ConsultarCarteraDerivadosEjercicio.Salida_ConsultarEjercicio;
import cl.bcs.custodia.ConsultarSorteoContratos;
import cl.bcs.custodia.ConsultarSorteoContratos.Contratos;
import cl.bcs.custodia.ConsultarSorteoContratos.EntradaConsultarSorteoContratos;
import cl.bcs.custodia.ConsultarSorteoContratos.SalidaConsultarSorteoContratos;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

public class CarteraSE implements BcsCartola{
	
	public static class SalidaCarteraSE{
		public Long 		IdCliente;
		public Long 		IdPortafolio;
		public String 		Serie;
		public String 		Tipo;
		public BigDecimal 	ContratosEjercidos;		
		public String 		TipoOpcion;
		public String 		Estilo;
		public String 		ModalidadLiquidacion;
		public String 		FechaVencimiento;
		public BigDecimal 	PrecioEjercicio;
		public BigDecimal 	PrecioSubyacente;
		public BigDecimal 	UtilidadPerdida;
		public BigDecimal 	SubTotal;
		public BigDecimal 	Total;		
		public String 		Titulo;
		public String 		SubTitulo;		


		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private static final Logger logger = LogManager.getLogger(CarteraSE.class);
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		logger.info("INICIO::CarteraSE");
		
		SalidaCartola out = new SalidaCartola();	
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		
/////// EJERCICIO DE CONTRATOS DE OPCIONES //////////		
		
		ConsultarCarteraDerivadosEjercicio consultarCarteraDerivadosEjercicio = new ConsultarCarteraDerivadosEjercicio();
		Entrada_ConsultarEjercicio entradaConsultarEjercicio = new Entrada_ConsultarEjercicio();
		
		entradaConsultarEjercicio.p_IdEmpresa 		= entrada.p_IdEmpresa;
		entradaConsultarEjercicio.p_IdUsuario 		= entrada.p_IdUsuario;
		entradaConsultarEjercicio.p_IdModulo 		= entrada.p_IdModulo;
		entradaConsultarEjercicio.p_TipoDerivado    = "OP";
		entradaConsultarEjercicio.p_FechaConsulta	= entrada.p_FechaHasta;

		for(Dato dato : entrada.p_Datos){
			entradaConsultarEjercicio.p_IdCliente 		= dato.IdCliente.intValue();		
			entradaConsultarEjercicio.p_IdPortafolio 	= dato.IdPortafolio;		
		}		

		Salida_ConsultarEjercicio salidaOpciones = consultarCarteraDerivadosEjercicio.logica(con, entradaConsultarEjercicio);
		
		List<SalidaCarteraSE> salidas = new ArrayList<SalidaCarteraSE>();
		
		BigDecimal utilidadperdidaEjercicio = BigDecimal.ZERO;
		
		
		if (!salidaOpciones.P_OUT_EJERCICIOS.isEmpty()) {		
			for(ConsultarEjercicio ejercicio :salidaOpciones.P_OUT_EJERCICIOS) {
				SalidaCarteraSE salidaEjercicioOpcion = new SalidaCarteraSE();
				salidaEjercicioOpcion.Titulo = "EJERCICIO DE CONTRATOS DE OPCIONES";
				salidaEjercicioOpcion.SubTitulo = "EJERCICIO DE OPCIONES CON MODALIDAD DE LIQUIDACION " + ejercicio.ModalidadLiquidacion + " (" + dateFormat.format(ejercicio.FechaVencimiento).toString() + ")";
				salidaEjercicioOpcion.IdCliente = ejercicio.IdCliente;
				salidaEjercicioOpcion.IdPortafolio = ejercicio.IdPortafolio;
				salidaEjercicioOpcion.Serie = ejercicio.Serie;
				salidaEjercicioOpcion.Tipo = "VENTA";//ejercicio.TipoOpcion;
				salidaEjercicioOpcion.ContratosEjercidos = ejercicio.ContratosEjercibles;
				salidaEjercicioOpcion.TipoOpcion =ejercicio.TipoOpcion;
				salidaEjercicioOpcion.Estilo = ejercicio.Estilo;
				salidaEjercicioOpcion.ModalidadLiquidacion = ejercicio.ModalidadLiquidacion;
				salidaEjercicioOpcion.FechaVencimiento = dateFormat.format(ejercicio.FechaVencimiento).toString();
				salidaEjercicioOpcion.PrecioEjercicio = ejercicio.PrecioEjercicio;
				salidaEjercicioOpcion.PrecioSubyacente = ejercicio.PrecioSubyacente;
				salidaEjercicioOpcion.UtilidadPerdida = ejercicio.ContratosEjercibles.multiply(ejercicio.PrecioEjercicio.subtract(ejercicio.PrecioSubyacente));
				
				if (ejercicio.ModalidadLiquidacion.equals("FINANCIERA")) {
					utilidadperdidaEjercicio = utilidadperdidaEjercicio.add(ejercicio.ContratosEjercibles.multiply(ejercicio.PrecioEjercicio.subtract(ejercicio.PrecioSubyacente)));
					salidaEjercicioOpcion.SubTotal = utilidadperdidaEjercicio; 
				}
				salidas.add(salidaEjercicioOpcion);			
				
			}
		}
		
/////// CONTRATO DE OPCIONES SORTEADOS //////////		
		
		ConsultarSorteoContratos consultarSorteoContratos = new ConsultarSorteoContratos();
		EntradaConsultarSorteoContratos entradaConsultarSorteoContratos = new EntradaConsultarSorteoContratos();

		entradaConsultarSorteoContratos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarSorteoContratos.p_IdUsuario = entrada.p_IdUsuario.intValue();
		entradaConsultarSorteoContratos.p_IdModulo = entrada.p_IdModulo;		
		entradaConsultarSorteoContratos.p_FechaConsulta = entrada.p_FechaHasta.toString();

		for(Dato dato : entrada.p_Datos){
			entradaConsultarSorteoContratos.p_IdCliente 	= dato.IdCliente.intValue();		
			entradaConsultarSorteoContratos.p_IdPortafolio 	= dato.IdPortafolio;		
		}
		
		SalidaConsultarSorteoContratos salidaSorteo = consultarSorteoContratos.logica(con, entradaConsultarSorteoContratos);
		
		BigDecimal utilidadperdidaSorteo = BigDecimal.ZERO;
		
		if (!salidaSorteo.P_OUT_CONTRATOS.isEmpty() && salidaSorteo.P_OUT_ERRORESCONTADOR.equals(0L)) {		
			for(Contratos sorteo :salidaSorteo.P_OUT_CONTRATOS) {
				SalidaCarteraSE salidaSorteoOpcion = new SalidaCarteraSE();
				salidaSorteoOpcion.Titulo = "CONTRATO DE OPCIONES SORTEADOS";
				salidaSorteoOpcion.SubTitulo = "CONTRATOS DE OPCIONES CON MODALIDAD DE LIQUIDACION " + sorteo.ModalidadLiquidacion + " (" + dateFormat.format(sorteo.FechaVencimiento).toString() + ")";
				salidaSorteoOpcion.IdCliente = sorteo.IdCliente;
				salidaSorteoOpcion.IdPortafolio = sorteo.IdPortafolio;
				salidaSorteoOpcion.Serie = sorteo.Serie;
				salidaSorteoOpcion.Tipo = sorteo.IdTipoOperacion;
				salidaSorteoOpcion.ContratosEjercidos	= sorteo.CantidadContratosTr;
				salidaSorteoOpcion.TipoOpcion = sorteo.EsPutEsCall;
				salidaSorteoOpcion.Estilo = sorteo.EstiloOpcion;
				salidaSorteoOpcion.ModalidadLiquidacion = sorteo.ModalidadLiquidacion;
				salidaSorteoOpcion.FechaVencimiento = dateFormat.format(sorteo.FechaVencimiento).toString();
				salidaSorteoOpcion.PrecioEjercicio = sorteo.Strike;
				salidaSorteoOpcion.PrecioSubyacente = sorteo.PrecioSubyacente;
				salidaSorteoOpcion.UtilidadPerdida= sorteo.PerdidaGanancia;
	
				if (sorteo.ModalidadLiquidacion.equals("FINANCIERA")) {
					utilidadperdidaSorteo = utilidadperdidaSorteo.add(sorteo.PerdidaGanancia);
					salidaSorteoOpcion.SubTotal = utilidadperdidaSorteo;
				}	
				salidas.add(salidaSorteoOpcion);			
			}
		}
			
		BigDecimal totalUtilidadPerdida = utilidadperdidaSorteo.add(utilidadperdidaEjercicio);
		
		for (int i=0; i < salidas.size(); i++){			
			salidas.get(i).Total = totalUtilidadPerdida;

		}
		
		List<HashMap<String,Object>> resultadoCarteraSE = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("CarteraSE", resultadoCarteraSE);		
		logger.info("FIN::CarteraSE");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		//System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2020-04-01");
		Objin.p_FechaHasta = Fecha.toDate("2020-04-17");
		Dato dato = new Dato();
		dato.IdCliente = 123569L;
		dato.IdPortafolio = 27499L;
		Objin.p_Datos.add(dato);
		CarteraSE  demo= new CarteraSE();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.commit();
			//System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		//System.out.println("== END ==");
	}
}
