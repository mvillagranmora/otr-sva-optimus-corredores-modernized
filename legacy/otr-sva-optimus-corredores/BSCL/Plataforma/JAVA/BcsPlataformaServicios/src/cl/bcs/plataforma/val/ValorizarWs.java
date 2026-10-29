package cl.bcs.plataforma.val;


import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.val.TipificarInstrumentos.InstrumentoUpdate;
import cl.bcs.plataforma.val.util.ValoUtils;
import cl.bcs.plataforma.val.util.ValorizarWsBCS;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class ValorizarWs implements JavaCall{	
	private final Log log = new Log("ValorizarWs");
	private static ValorizarWsBCS valobcs = null;
	//QUERIES
	
	//ENTRADA/SALIDA SP
	public static class InValorizarWs{
		public Long idGrupo; // idGrupo>0: Es llamado desde el job debe realizar queries.
		public String rutinaValorizador;
		public List<ValoUtils.TareaValorizacion> instrumentos;
		public InValorizarWs(){
			instrumentos = new ArrayList<ValoUtils.TareaValorizacion>();
			idGrupo=new Long(0);
		};
	}
	public static class OutValorizarWs {
		public Long erroresContador;
		public String descripcion;
		public List<ValoUtils.TareaValorizacion> instrumentos;
		public OutValorizarWs(){
			instrumentos = new ArrayList<ValoUtils.TareaValorizacion>();
		};
	}

	/**********************
	 * métodos básicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InValorizarWs in=gson.fromJson(jin.toString() ,InValorizarWs.class);
		OutValorizarWs out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public OutValorizarWs logica(Connection con, InValorizarWs entrada) throws Exception {
		boolean debug = true;
		OutValorizarWs salida = new OutValorizarWs();
		String msjaux = "";
		log.write("Nueva Valorización, IdGrupo: "+entrada.idGrupo);
		List<ValoUtils.TareaValorizacion> instrumentos = null;
		salida.erroresContador = new Long(0);
		instrumentos = entrada.instrumentos;
		if(instrumentos.size()==0){
			salida.erroresContador++;
			salida.descripcion = "No se han enviado instrumentos";
			return salida;
		}log.write("Datos obtenidos");
		if(entrada.rutinaValorizador.equals("BCS")){
			inicializaValorizarWsBCS();
			//Tipificar
			log.write("Rutina BCS - Revisa si hay instrumentos no tipificados");
			List<ValorizarWsBCS.ValorizarWsBCSObj> instip = new ArrayList<ValorizarWsBCS.ValorizarWsBCSObj>();
			for(ValoUtils.TareaValorizacion item : instrumentos){
				item.IdEstado = "INI";
				if(item.TipoVal==null || item.TipoVal.equals(-1)){
					ValorizarWsBCS.ValorizarWsBCSObj itemnuevo = new ValorizarWsBCS.ValorizarWsBCSObj();
					itemnuevo.nemo = item.Nemo;
					itemnuevo.fechaVal = item.FechaVal;
					boolean itemexiste = false;
					for(ValorizarWsBCS.ValorizarWsBCSObj check : instip){
						if (check.nemo.equals(itemnuevo.nemo)){
							itemexiste = true;
							break;
						}
					} if(!itemexiste) instip.add(itemnuevo);
				}
			}
			if(instip.size()==0){
				log.write("Rutina BCS - No hay instrumentos para tipificar (Todos ya tienen tipo)");
			}else{
				valobcs.obtenerTipoVal(instip);
				log.write("Rutina BCS - Procesa la tipificación realizada desde el WebService");
				for(ValoUtils.TareaValorizacion item : instrumentos){
					for(ValorizarWsBCS.ValorizarWsBCSObj itemtip : instip){
						if(item.Nemo.equals(itemtip.nemo)){
							item.FechaVal = itemtip.fechaVal;
							item.Estado = itemtip.estado;
							item.Glosa = itemtip.glosa;
							item.Hora = itemtip.hora;
							item.TipoVal = itemtip.tipoVal;
							item.WsCallerCodSalida = itemtip.wsCallerCodSalida;
							item.WsCallerDescSalida = itemtip.wsCallerDescSalida;
							if(!itemtip.wsCallerCodSalida.equals("0")) item.IdEstado = "ERR";
						}
					}
				}
				//realizar update
				boolean realizaUpdate = false;
				if(realizaUpdate){
					log.write("Rutina BCS - Se actualizarán en base de datos los instrumentos no tipificadosr");
					SQL sql=new SQL();
					List<InstrumentoUpdate> paramsupdateins = new ArrayList<InstrumentoUpdate>(); 
					for(ValorizarWsBCS.ValorizarWsBCSObj itemtip : instip){
						InstrumentoUpdate item = new InstrumentoUpdate();
						item.Nemotecnico = itemtip.nemo;
						item.TipoValorizacion = itemtip.tipoVal;
						paramsupdateins.add(item);
					}
					String SQL_Update_Instrumentos = "UPDATE \"BSCL.Plataforma.BD::INS.Instrumento\" "
							+ "SET \"TipoValorizacion\" = COALESCE(:TipoValorizacion,-1) WHERE \"Nemotecnico\" = :Nemotecnico";
					sql.batch(con,SQL_Update_Instrumentos, paramsupdateins,InstrumentoUpdate.class);
				}	
			}			
			
			//Valorizar
			log.write("Rutina BCS - Inicia Valorizar");
			List<ValorizarWsBCS.ValorizarWsBCSObj> insval = new ArrayList<ValorizarWsBCS.ValorizarWsBCSObj>();
			for(ValoUtils.TareaValorizacion item : instrumentos){
				if(item.TipoVal==null || item.TipoVal.equals(-1)){
					item.IdEstado ="TER";
					item.Estado = "INSTR_NO_TIPIFICADO";
					item.Glosa = "Instrumento no tipificado";
					item.WsCallerCodSalida = "INSTR_NO_TIPIFICADO";
					item.WsCallerDescSalida = "Instrumento no tipificado";
				} else{
					ValorizarWsBCS.ValorizarWsBCSObj itemval = new ValorizarWsBCS.ValorizarWsBCSObj();
					preparaDatosValorizarBCS(item,itemval);
					insval.add(itemval);
				}
			}
			valobcs.valorizar(insval);
			log.write("Rutina BCS - Procesa la valorización realizada desde el WebService");
			//Procesar Salida
			for(ValorizarWsBCS.ValorizarWsBCSObj itemval : insval){
				for(ValoUtils.TareaValorizacion item : instrumentos){
					if(itemval.idTarea.equals(item.IdTarea)){
						if(!itemval.wsCallerCodSalida.equals("0")) {
							item.WsCallerCodSalida = itemval.wsCallerCodSalida;
							item.WsCallerDescSalida = itemval.wsCallerDescSalida;
							item.IdEstado = "TER";
						}
						else {
							item.TipoVal = itemval.tipoVal;
							item.Nemo = itemval.nemo;
							item.FechaVal = itemval.fechaVal;
							item.FechaEmi = itemval.fechaEmi;
							item.FechaVcto = itemval.fechaVcto;
							item.NumeroDias = itemval.numeroDias;
							item.TasaTir = itemval.tasaTir;
							item.TasaTip = itemval.tasaTip;
							item.TasaInteres = itemval.tasaInteres;
							item.ValRescateP = itemval.valRescateP;
							item.ValRescateR = itemval.valRescateR;
							item.ValCaptacionP = itemval.valCaptacionP;
							item.ValCaptacionR = itemval.valCaptacionR;
							item.GlosaEmisor = itemval.glosaEmisor;
							item.GlosaMensaje = itemval.glosaMensaje;
							item.Cantidad = itemval.cantidad;
							item.UnidadMon = itemval.unidadMon;
							item.Precio = itemval.precio;
							item.MontoPeso = itemval.montoPeso;
							item.MontoUF = itemval.montoUF;
							item.MontoUS = itemval.montoUS;
							item.AnoVcto = itemval.anoVcto;
							item.MesVcto = itemval.mesVcto;
							item.Duration = itemval.duration;
							item.ValorBase = itemval.valorBase;
							item.Convexidad = itemval.convexidad;
							item.ValorPar2 = itemval.valorPar2;
							item.ValorPar = itemval.valorPar;
							item.TipoTasa = itemval.tipoTasa;
							item.CalculoEst = itemval.calculoEst;
							item.APlazo = itemval.aPlazo;
							item.ValorRM = itemval.valorRM;
							item.ValorNom = itemval.valorNom;
							item.TipoMoneda = itemval.tipoMoneda;
							item.CLiq = itemval.cLiq;
							item.Gestion = itemval.gestion;
							item.Estado = itemval.estado;
							item.Glosa = itemval.glosa;
							item.Hora = itemval.hora;
							item.WsCallerCodSalida = itemval.wsCallerCodSalida;
							item.WsCallerDescSalida = itemval.wsCallerDescSalida;
							item.IdEstado = "TER";
						}
						
					}
				}
			}
			salida.instrumentos = instrumentos;
			log.write("Rutina BCS - Fin de la rutina");
		}
		// else if(entrada.rutinaValorizador.equals("OTRA_RUTINA")){}
		else{
			msjaux = "Rutina "+entrada.rutinaValorizador+" no implementada";
			log.write(msjaux);
			salida.erroresContador++;
			salida.descripcion = msjaux;
			return salida;
		}
		if(debug){
			log.writeAsJson("Entrada ValorizarWs",entrada);
			log.writeAsJson("Salida ValorizarWs",salida);
		}
		return salida;
	}
	
	/**********************
	 * métodos de la clase
	 **********************/
	private static void preparaDatosValorizarBCS(ValoUtils.TareaValorizacion origen, ValorizarWsBCS.ValorizarWsBCSObj destino){
		Log log = new Log("ValorizarWs");
		destino.idTarea = origen.IdTarea;
		destino.wsCallerCodSalida = origen.WsCallerCodSalida;
    	destino.wsCallerDescSalida = origen.WsCallerDescSalida;
    	switch (origen.TipoVal.intValue())
    	{
    		case 63: // IF - Pagares no reajustables.
    			destino.tipoVal = origen.TipoVal;
    			destino.nemo = origen.Nemo;
    			destino.fechaVal = origen.FechaVal;
    			destino.fechaVcto = origen.FechaVcto;
    			destino.numeroDias = origen.NumeroDias;
    			destino.tasaInteres = origen.TasaInteres;
    			destino.valRescateP = origen.ValRescateP;
    			destino.valCaptacionP = origen.ValCaptacionP;
    			destino.glosaEmisor = origen.GlosaEmisor;
    			destino.cLiq = origen.CLiq;
    			break;
    		case 62: // IF - Pagares reajustables.
    			destino.tipoVal = origen.TipoVal;
    			destino.nemo = origen.Nemo;
    			destino.fechaVal = origen.FechaVal;
    			destino.glosaMensaje = origen.GlosaMensaje;
    			destino.tipoMoneda = origen.TipoMoneda;
    			destino.fechaVcto = origen.FechaVcto;
    			destino.numeroDias = origen.NumeroDias;
    			destino.tasaInteres = origen.TasaInteres;
    			destino.glosaEmisor = origen.GlosaEmisor;
    			destino.valRescateR = origen.ValRescateR;
    			destino.valCaptacionR = origen.ValCaptacionR;
    			destino.valCaptacionP = origen.ValCaptacionP;
    			destino.cLiq = origen.CLiq;
    			break;
    		case 88: // IF - Tasa flotante.
    			destino.tipoVal = origen.TipoVal;
    			destino.nemo = origen.Nemo;
    			destino.fechaVal = origen.FechaVal;
    			destino.glosaMensaje = origen.GlosaMensaje;
    			destino.fechaEmi = origen.FechaEmi;
    			destino.fechaVcto = origen.FechaVcto;
    			destino.numeroDias = origen.NumeroDias;
    			destino.tasaInteres = origen.TasaInteres;
    			destino.tasaTip = origen.TasaTip;
    			destino.glosaEmisor = origen.GlosaEmisor;
    			destino.cantidad = origen.Cantidad;
    			destino.valCaptacionR = origen.ValCaptacionR;
    			destino.valCaptacionP = origen.ValCaptacionP;
    			destino.valRescateR = origen.ValRescateR;
    			destino.tipoMoneda = origen.TipoMoneda;
    			destino.cLiq = origen.CLiq;
    			break;
    		case 64: // RF - Tasa fija.
    			destino.tipoVal = origen.TipoVal;
    			destino.nemo = origen.Nemo;
    			destino.fechaVal = origen.FechaVal;
    			destino.cantidad = origen.Cantidad;
    			destino.tasaTir = origen.TasaTir;
    			destino.unidadMon = origen.UnidadMon;
    			destino.precio = origen.Precio;
    			destino.montoPeso = origen.MontoPeso;
    			destino.mesVcto = origen.MesVcto;
    			destino.anoVcto = origen.AnoVcto;
    			destino.duration = origen.Duration;
    			destino.valorBase = origen.ValorBase;
    			destino.valorPar = origen.ValorPar;
    			destino.glosaMensaje = origen.GlosaMensaje;
    			destino.convexidad = origen.Convexidad;
    			destino.montoUF = origen.MontoUF;
    			destino.cLiq = origen.CLiq;
    			break;
    		case 65: // RF - Tasa flotante.
    			destino.tipoVal = origen.TipoVal;
    			destino.nemo = origen.Nemo;
    			destino.fechaVal = origen.FechaVal;
    			destino.glosaMensaje = origen.GlosaMensaje;
    			destino.tasaTip = origen.TasaTip;
    			destino.precio = origen.Precio;
    			destino.cantidad = origen.Cantidad;
    			destino.tasaTir = origen.TasaTir;
    			destino.montoPeso = origen.MontoPeso;
    			destino.unidadMon = origen.UnidadMon;
    			destino.tipoTasa = origen.TipoTasa;
    			destino.valorPar = origen.ValorPar;
    			destino.calculoEst = origen.CalculoEst;
    			destino.mesVcto = origen.MesVcto;
    			destino.anoVcto = origen.AnoVcto;
    			destino.duration = origen.Duration;
    			destino.convexidad = origen.Convexidad;
    			destino.montoUF = origen.MontoUF;
    			destino.cLiq = origen.CLiq;
    			break;
    		case 66: // RF - CORA-PRTP.
    			destino.tipoVal = origen.TipoVal;
    			destino.nemo = origen.Nemo;
    			destino.fechaVal = origen.FechaVal;
    			destino.aPlazo = origen.APlazo;
    			destino.valorRM = origen.ValorRM;
    			destino.tasaTir = origen.TasaTir;
    			destino.valorNom = origen.ValorNom;
    			destino.precio = origen.Precio;
    			destino.montoPeso = origen.MontoPeso;
    			destino.valorPar2 = origen.ValorPar2;
    			destino.mesVcto = origen.MesVcto;
    			destino.anoVcto = origen.AnoVcto;
    			destino.duration = origen.Duration;
    			destino.convexidad = origen.Convexidad;
    			destino.montoUF = origen.MontoUF;
    			destino.cLiq = origen.CLiq;
    			break;
    		case 91: // RF - bonos CERO y ZERO.
    			destino.tipoVal = origen.TipoVal;
    			destino.nemo = origen.Nemo;
    			destino.glosaMensaje = origen.GlosaMensaje;
    			destino.fechaVal = origen.FechaVal;
    			destino.cantidad = origen.Cantidad;
    			destino.tasaTir = origen.TasaTir;
    			destino.montoPeso = origen.MontoPeso;
    			destino.montoUF = origen.MontoUF;
    			destino.montoUS = origen.MontoUS;
    			destino.unidadMon = origen.UnidadMon;
    			destino.calculoEst = origen.CalculoEst;
    			destino.mesVcto = origen.MesVcto;
    			destino.anoVcto = origen.AnoVcto;
    			destino.duration = origen.Duration;
    			destino.convexidad = origen.Convexidad;
    			destino.cLiq = origen.CLiq;
    			break;
    		case 79: // RF - BR-CBR.
    			destino.tipoVal = origen.TipoVal;
    			destino.nemo = origen.Nemo;
    			destino.fechaVal = origen.FechaVal;
    			destino.valorNom = origen.ValorNom;
    			destino.precio = origen.Precio;
    			destino.tasaTir = origen.TasaTir;
    			destino.montoUF = origen.MontoUF;
    			destino.montoPeso = origen.MontoPeso;
    			destino.valorPar2 = origen.ValorPar2;
    			destino.mesVcto = origen.MesVcto;
    			destino.anoVcto = origen.AnoVcto;
    			destino.duration = origen.Duration;
    			destino.convexidad = origen.Convexidad;
    			destino.cLiq = origen.CLiq;
    			break;
    		case 80: // RF - otros.
    			destino.tipoVal = origen.TipoVal;
    			destino.nemo = origen.Nemo;
    			destino.fechaVal = origen.FechaVal;
    			destino.valorNom = origen.ValorNom;
    			destino.precio = origen.Precio;
    			destino.tasaTir = origen.TasaTir;
    			destino.montoUF = origen.MontoUF;
    			destino.montoPeso = origen.MontoPeso;
    			destino.valorPar2 = origen.ValorPar2;
    			destino.mesVcto = origen.MesVcto;
    			destino.anoVcto = origen.AnoVcto;
    			destino.duration = origen.Duration;
    			destino.convexidad = origen.Convexidad;
    			destino.cLiq = origen.CLiq;
    			break;
    		default: // no definido.
    			log.write("****** TipoVal " + origen.TipoVal + " no definido *******");
    			destino.wsCallerCodSalida = "TIPO_VAL_NO_DEFINIDO";
    			destino.wsCallerDescSalida = "TipoVal " + origen.TipoVal+" no implementado";
    			break;
    	}
	}
	
	private static void inicializaValorizarWsBCS() throws Exception{ 
		Log log = new Log("TipificarInstrumentos");
		if(valobcs == null){
    		Properties prop = new Properties();
            InputStream input = new FileInputStream("valorizador.properties");
            prop.load(input);
            String Server=prop.getProperty("SERVER");
            String User=prop.getProperty("USER");
            String Pass=prop.getProperty("PASS");
            Boolean Depuracion=("Y".equalsIgnoreCase(prop.getProperty("DEPURATION"))?true:false); //Y|N
            int ConnTimeOut=Integer.valueOf(prop.getProperty("CONNTIMEOUT"));
            int ReadTimeOut=Integer.valueOf(prop.getProperty("READTIMEOUT"));
            input.close();
            log.write("configuracion valorizador, server: "+Server+",user: "+User);
            valobcs=new ValorizarWsBCS(Server,User,Pass,Depuracion,ConnTimeOut,ReadTimeOut);
    	}
    }

}
