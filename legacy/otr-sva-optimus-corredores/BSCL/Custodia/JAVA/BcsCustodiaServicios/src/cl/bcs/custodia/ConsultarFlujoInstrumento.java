package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoEsAgrupadoPor;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoFiltrosBusqueda;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoUso;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class ConsultarFlujoInstrumento implements JavaCall {

	private final Log log = new Log("CUS.ConsultarFlujoInstrumento");
	
	public static class ConsultarFlujoInstrumentoEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public Long							p_IdPais;
		public String						p_FechaSaldo;
		public Integer						p_DiasProyeccion;
		public List<FlujoUso> 				p_Uso;
		public List<FlujoFiltrosBusqueda> 	p_FiltrosBusqueda;
		public List<FlujoEsAgrupadoPor> 	p_EsAgrupadoPor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjSalidaFlujo {
		public String		ItemOrigen;
		public String		IdClasificacionTipoInstrumento;
		public String		Nemotecnico;
		public Long			IdInstrumento;
		public BigDecimal	T0;
		public BigDecimal	T1;
		public BigDecimal	T2;
		public BigDecimal	T3;
		public BigDecimal	T4;
		public BigDecimal	T5;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	};
	
	public static class ConsultarFlujoInstrumentoSalida {
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<ObjSalidaFlujo>					P_OUT_SALIDA;
		
		public ConsultarFlujoInstrumentoSalida (){
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_SALIDA = new ArrayList<ObjSalidaFlujo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	};
	
	class ComparadorFlujo implements Comparator<FlujoCustodia> {
	    @Override
	    public int compare(FlujoCustodia o1, FlujoCustodia o2) {
	    	return o1.FechaSaldo.compareTo(o2.FechaSaldo);
	    }
	}
	
	//TODO: Logica
	public ConsultarFlujoInstrumentoSalida logica(Connection con, ConsultarFlujoInstrumentoEntrada in) throws Exception {		
		ConsultarFlujoInstrumentoSalida out = new ConsultarFlujoInstrumentoSalida();
		try {			
			
			//******************************************************************************************************************
			//												VALIDACIONES INICIALES
			//******************************************************************************************************************			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();			
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			};
		
			if (out.P_OUT_MENSAJES.size() > 0){
				return out;
			};
			
			//******************************************************************************************************************
			//													LOGICA
			//******************************************************************************************************************			
			
			//Llamado a Flujo de Custodia
			ConsultarFlujoCustodia llamadoFlujoCustodia = new ConsultarFlujoCustodia();
			ConsultarFlujoCustodia.FlujoCustodiaEntrada paramInSP = new ConsultarFlujoCustodia.FlujoCustodiaEntrada(); 
			paramInSP.p_IdEmpresa			= in.p_IdEmpresa;
			paramInSP.p_IdUsuario			= in.p_IdUsuario;
			paramInSP.p_IdModulo			= in.p_IdModulo;
			paramInSP.p_FechaSaldo			= in.p_FechaSaldo;
			paramInSP.p_IdPais				= in.p_IdPais;
			paramInSP.p_DiasProyeccion		= in.p_DiasProyeccion;
			paramInSP.p_DiaHabil			= "S";
			paramInSP.p_ValorizarFlujo		= "N";
			paramInSP.p_Uso					= in.p_Uso;
			paramInSP.p_FiltrosBusqueda		= in.p_FiltrosBusqueda;
			paramInSP.p_EsAgrupadoPor		= in.p_EsAgrupadoPor;
			
			ConsultarFlujoCustodia.FlujoCustodiaSalida salidaFlujo = llamadoFlujoCustodia.logica(con, paramInSP);

			//ORDENA EL FLUJO POR FECHA DE SALDO
			Collections.sort(salidaFlujo.p_out_FlujoTotalizado,new ComparadorFlujo());
			List<ObjSalidaFlujo> listaFlujo = new ArrayList<ObjSalidaFlujo>();
			ArrayList<String> instListaFlujo = new ArrayList<>();
			if (salidaFlujo.P_OUT_ERRORESCONTADOR == null || salidaFlujo.P_OUT_ERRORESCONTADOR.equals(0L) ){
				Date vFechaAnterior = new Date(0);
				Long i		= new Long(-1L);
				Long ii		= new Long(-1L);
				Long iii	= new Long(-1L);
				Long iiii	= new Long(-1L);
				Long iiiii	= new Long(-1L);
				Long iiiiii	= new Long(-1L);
				for(ConsultarFlujoCustodia.FlujoCustodia recorreFlujoTotalizado : salidaFlujo.p_out_FlujoTotalizado){
					ObjSalidaFlujo registroFlujo = new ObjSalidaFlujo();
					if(!vFechaAnterior.equals(recorreFlujoTotalizado.FechaSaldo)){
						i++;
						ii++;
						iii++;
						iiii++;
						iiiii++;
						iiiiii++;
					}
					if(i.equals(0L)){
						// LLENAMOS ITEMS Y T0 
						instListaFlujo.add(recorreFlujoTotalizado.ItemOrigen);
						registroFlujo.ItemOrigen 						= recorreFlujoTotalizado.ItemOrigen;
						registroFlujo.IdClasificacionTipoInstrumento 	= recorreFlujoTotalizado.IdClasificacionTipoInstrumento;
						registroFlujo.Nemotecnico						= recorreFlujoTotalizado.Nemotecnico;
						registroFlujo.IdInstrumento						= recorreFlujoTotalizado.IdInstrumento;
	 					switch(recorreFlujoTotalizado.IdClasificacionTipoInstrumento){
							case "IF":
								registroFlujo.T0 = recorreFlujoTotalizado.ValorRescate;
								break;
							default:
								registroFlujo.T0 = recorreFlujoTotalizado.Cantidad;
								break;
						};
						listaFlujo.add(registroFlujo);
					}else{
						// LLENAMOS T1
						if(ii.equals(1L)){
							int posicion = instListaFlujo.indexOf(recorreFlujoTotalizado.ItemOrigen);
		 					switch(recorreFlujoTotalizado.IdClasificacionTipoInstrumento){
								case "IF":
									listaFlujo.get(posicion).T1 = recorreFlujoTotalizado.ValorRescate;
									break;
								default:
									listaFlujo.get(posicion).T1 = recorreFlujoTotalizado.Cantidad;
									break;
							};
						}else{
							// LLENAMOS T2
							if(iii.equals(2L)){
								int posicion = instListaFlujo.indexOf(recorreFlujoTotalizado.ItemOrigen);
			 					switch(recorreFlujoTotalizado.IdClasificacionTipoInstrumento){
									case "IF":
										listaFlujo.get(posicion).T2 = recorreFlujoTotalizado.ValorRescate;
										break;
									default:
										listaFlujo.get(posicion).T2 = recorreFlujoTotalizado.Cantidad;
										break;
								};
							}else{
								// LLENAMOS T3
								if(iiii.equals(3L)){
									int posicion = instListaFlujo.indexOf(recorreFlujoTotalizado.ItemOrigen);
				 					switch(recorreFlujoTotalizado.IdClasificacionTipoInstrumento){
										case "IF":
											listaFlujo.get(posicion).T3 = recorreFlujoTotalizado.ValorRescate;
											break;
										default:
											listaFlujo.get(posicion).T3 = recorreFlujoTotalizado.Cantidad;
											break;
									};
								}else{
									// LLENAMOS T4
									if(iiiii.equals(4L)){
										int posicion = instListaFlujo.indexOf(recorreFlujoTotalizado.ItemOrigen);
					 					switch(recorreFlujoTotalizado.IdClasificacionTipoInstrumento){
											case "IF":
												listaFlujo.get(posicion).T4 = recorreFlujoTotalizado.ValorRescate;
												break;
											default:
												listaFlujo.get(posicion).T4 = recorreFlujoTotalizado.Cantidad;
												break;
										};
									}else{
										// LLENAMOS T5
										if(iiiiii.equals(5L)){
											int posicion = instListaFlujo.indexOf(recorreFlujoTotalizado.ItemOrigen);
						 					switch(recorreFlujoTotalizado.IdClasificacionTipoInstrumento){
												case "IF":
													listaFlujo.get(posicion).T5 = recorreFlujoTotalizado.ValorRescate;
													break;
												default:
													listaFlujo.get(posicion).T5 = recorreFlujoTotalizado.Cantidad;
													break;
											};
										}
									}
								}
							}
						}
					}
					vFechaAnterior = recorreFlujoTotalizado.FechaSaldo;
				}
			}
			
			out.P_OUT_MENSAJES.addAll(salidaFlujo.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			out.P_OUT_SALIDA = listaFlujo;
			
			log.write(out.P_OUT_MENSAJES);
			log.write(listaFlujo);
			
		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			throw e;
		}
		
		// SALIDA
		return out;
	};

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		ConsultarFlujoInstrumentoEntrada jin=gson.fromJson(in.toString() ,ConsultarFlujoInstrumentoEntrada.class);
		ConsultarFlujoInstrumentoSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	// MAIN
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//Nemo con error al ordenar
			//String json = "{\\\"p_IdEmpresa\\\":1,\\\"p_IdUsuario\\\":6096,\\\"p_IdModulo\\\":\\\"CUS\\\",\\\"p_IdPais\\\":-1,\\\"p_FechaSaldo\\\":\\\"2018-07-13\\\",\\\"p_DiasProyeccion\\\":5,\\\"p_Uso\\\":[{\\\"UsoTipoOperacion\\\":\\\"FLJINS\\\",\\\"IdCliente\\\":224158,\\\"IdPortafolio\\\":294452,\\\"BusquedaEspecifica\\\":\\\"FLUJO_INSTRUMENTO\\\",\\\"IdTipoInstrumentoEspecifico\\\":\\\"ACN\\\"}],\\\"p_FiltrosBusqueda\\\":[{\\\"IdClasificacionTipoInstrumento\\\":\\\"RV\\\",\\\"IdTipoInstrumento\\\":\\\"ACN\\\",\\\"IdInstrumento\\\":252930,\\\"IdEmisor\\\":null,\\\"FechaVencimiento\\\":null,\\\"IdTipoReajuste\\\":null,\\\"EsFungible\\\":null,\\\"IdBoveda\\\":null}],\\\"p_EsAgrupadoPor\\\":[{\\\"ClasificacionTipoInstrumento\\\":\\\"S\\\",\\\"TipoInstrumento\\\":\\\"S\\\",\\\"Instrumento\\\":\\\"S\\\",\\\"Emisor\\\":null,\\\"Nemotecnico\\\":null,\\\"NemoSVS\\\":null,\\\"FechaEmisionInstrumento\\\":null,\\\"FechaVencimientoInstrumento\\\":null,\\\"FechaLiquidacionInstrumento\\\":null,\\\"TipoReajuste\\\":null,\\\"EsFungible\\\":null,\\\"CodigoIFRS\\\":null,\\\"Boveda\\\":null,\\\"Bolsa\\\":null,\\\"TipoOperacion\\\":null,\\\"Mesa\\\":null,\\\"FolioRegistro\\\":null,\\\"ItemOrigen\\\":\\\"S\\\"}]}";
			//nemo bueno {"p_IdEmpresa":1,"p_IdUsuario":6096,"p_IdModulo":"CUS","p_IdPais":-1,"p_FechaSaldo":"2018-07-13","p_DiasProyeccion":5,"p_Uso":[{"UsoTipoOperacion":"FLJINS","IdCliente":224158,"IdPortafolio":294452,"BusquedaEspecifica":"FLUJO_INSTRUMENTO","IdTipoInstrumentoEspecifico":"ACN"}],"p_FiltrosBusqueda":[{"IdClasificacionTipoInstrumento":"RV","IdTipoInstrumento":"ACN","IdInstrumento":502,"IdEmisor":null,"FechaVencimiento":null,"IdTipoReajuste":null,"EsFungible":null,"IdBoveda":null}],"p_EsAgrupadoPor":[{"ClasificacionTipoInstrumento":"S","TipoInstrumento":"S","Instrumento":"S","Emisor":null,"Nemotecnico":null,"NemoSVS":null,"FechaEmisionInstrumento":null,"FechaVencimientoInstrumento":null,"FechaLiquidacionInstrumento":null,"TipoReajuste":null,"EsFungible":null,"CodigoIFRS":null,"Boveda":null,"Bolsa":null,"TipoOperacion":null,"Mesa":null,"FolioRegistro":null,"ItemOrigen":"S"}]}
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6274,\"p_IdModulo\":\"CUS\",\"p_IdPais\":-1,\"p_FechaSaldo\":\"2018-07-17\",\"p_DiasProyeccion\":5,\"p_Uso\":[{\"UsoTipoOperacion\":\"FLJINS\",\"IdCliente\":213082,\"IdPortafolio\":280005,\"BusquedaEspecifica\":\"FLUJO_INSTRUMENTO\",\"IdTipoInstrumentoEspecifico\":\"ACN\"}],\"p_FiltrosBusqueda\":[{\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":253,\"IdEmisor\":null,\"FechaVencimiento\":null,\"IdTipoReajuste\":null,\"EsFungible\":null,\"IdBoveda\":null}],\"p_EsAgrupadoPor\":[{\"ClasificacionTipoInstrumento\":\"S\",\"TipoInstrumento\":\"S\",\"Instrumento\":\"S\",\"Emisor\":null,\"Nemotecnico\":null,\"NemoSVS\":null,\"FechaEmisionInstrumento\":null,\"FechaVencimientoInstrumento\":null,\"FechaLiquidacionInstrumento\":null,\"TipoReajuste\":null,\"EsFungible\":null,\"CodigoIFRS\":null,\"Boveda\":null,\"Bolsa\":null,\"TipoOperacion\":null,\"Mesa\":null,\"FolioRegistro\":null,\"ItemOrigen\":\"S\"}]}";
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"CUS\",\"p_IdPais\":-1,\"p_FechaSaldo\":\"2018-10-04\",\"p_DiasProyeccion\":5,\"p_Uso\":[{\"UsoTipoOperacion\":\"FLJINS\",\"IdCliente\":116323,\"IdPortafolio\":20021,\"BusquedaEspecifica\":\"FLUJO_INSTRUMENTO\",\"IdTipoInstrumentoEspecifico\":\"ACN\"}],\"p_FiltrosBusqueda\":[{\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":764,\"IdEmisor\":null,\"FechaVencimiento\":null,\"IdTipoReajuste\":null,\"EsFungible\":null,\"IdBoveda\":\"COR\"}],\"p_EsAgrupadoPor\":[{\"ClasificacionTipoInstrumento\":\"S\",\"TipoInstrumento\":\"S\",\"Instrumento\":\"S\",\"Emisor\":null,\"Nemotecnico\":null,\"NemoSVS\":null,\"FechaEmisionInstrumento\":null,\"FechaVencimientoInstrumento\":null,\"FechaLiquidacionInstrumento\":null,\"TipoReajuste\":null,\"EsFungible\":null,\"CodigoIFRS\":null,\"Boveda\":null,\"Bolsa\":null,\"TipoOperacion\":null,\"Mesa\":null,\"FolioRegistro\":null,\"ItemOrigen\":\"S\"}]}";
			ConsultarFlujoInstrumentoEntrada Objin = gson.fromJson(json, ConsultarFlujoInstrumentoEntrada.class);
			
			ConsultarFlujoInstrumento  kk = new ConsultarFlujoInstrumento();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ConsultarFlujoInstrumentoSalida ObjOut = kk.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
			finally {
				con.close();
			}
 			
		} catch (Exception e) {
			e.printStackTrace();			
		}
		System.out.println("== END ==");
	};
	
};