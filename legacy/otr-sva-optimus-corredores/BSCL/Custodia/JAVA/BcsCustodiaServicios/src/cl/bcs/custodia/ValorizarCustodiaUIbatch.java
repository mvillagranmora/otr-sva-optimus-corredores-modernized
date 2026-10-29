package cl.bcs.custodia;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pau.ValorizarCarteraCupones;
import cl.bcs.plataforma.pau.ValorizarCarteraFuturos;
import cl.bcs.plataforma.pau.ValorizarCarteraPactos;
import cl.bcs.plataforma.pau.ValorizarCarteraPrestEmpres;
import cl.bcs.plataforma.pau.ValorizarCarteraIF;
import cl.bcs.plataforma.pau.ValorizarCarteraOpciones;
import cl.bcs.plataforma.pau.ValorizarCarteraRF;
import cl.bcs.plataforma.pau.ValorizarCarteraRV;
import cl.bcs.plataforma.pau.ValorizarCarteraSimultaneas;
import cl.bcs.plataforma.pau.ValorizarGarantiasRVRFIF;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.fondomutuo.ActualizarCarterasFondosMutuos;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class ValorizarCustodiaUIbatch implements JavaCall{	
	private final Log log = new Log("ValorizarCustodiaUIbatch");

	public static class InValorizarCustodiaUIbatch{
		public Long		p_IdEmpresa;		
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public String	p_FechaSistema;
		public Long		p_IdPais = -1L;
		public String 	p_Simular = "N";
		public Long 	p_IdPortafolio = 0L;
		public Long		p_IdCliente = 0L;
		public String 	p_ValorizaCarteraRV = "N";
		public String 	p_ValorizaCarteraFFMM = "N";
		public String	p_ValorizaCarteraRF = "N";
		public String	p_ValorizaCarteraIF = "N";
		public String	p_ValorizaPrestamosEmprestitos = "N";
		public String	p_ValorizaGarantiasRVRFIF = "N";
		public String	p_ValorizaSimultaneas = "N";
		public String	p_ValorizaCupones = "N";
		public String 	p_ValorizaPactos = "N";
		public String 	p_ValorizaFuturos = "N";
		public String 	p_ValorizaOpciones = "N";
	}
	
	public static class OutValorizarCustodiaUIbatch{
		public List<DetalleEjecucion>	P_OUT_DETALLE_EJEUCION;
		public Long						P_OUT_ERRORESCONTADOR;	
		public OutValorizarCustodiaUIbatch(){
			P_OUT_DETALLE_EJEUCION 		= new ArrayList<DetalleEjecucion>();
		}	
		public static class DetalleEjecucion{
			public String Hora = "";
			public String Accion = "";
			public String Resultado = "";
			public String Detalle = "";
			public DetalleEjecucion(String Accion,String Resultado,String Detalle){
				this.Hora = Utils.getCurrentTimeStamp();
				this.Accion = Accion;
				this.Resultado = Resultado;
				this.Detalle = Detalle;
			}
			public DetalleEjecucion(){
			}
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public OutValorizarCustodiaUIbatch logica(Connection con, InValorizarCustodiaUIbatch in) throws Exception {
		OutValorizarCustodiaUIbatch out = new OutValorizarCustodiaUIbatch();
		out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("Inicio", "", "Inicia el proceso [Portafolio:"+(in.p_IdPortafolio>0?in.p_IdPortafolio:"TODOS")+"]"));
		String FechaSistema	= in.p_FechaSistema;
		if(FechaSistema.equals(""))FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");

		log.write("1) p_ValorizaCarteraRV="+in.p_ValorizaCarteraRV);
		if("S".equalsIgnoreCase(in.p_ValorizaCarteraRV)){
			try{
				ValorizarCarteraRV.ValorizarCarteraRVEntrada entrada = new ValorizarCarteraRV.ValorizarCarteraRVEntrada();
				entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdPortafolio = in.p_IdPortafolio;
				entrada.p_IdCliente = in.p_IdCliente;
				entrada.p_FechaSistema = FechaSistema;
				entrada.p_IdPais = in.p_IdPais;
				entrada.p_SimularOperacion = in.p_Simular;
		        ValorizarCarteraRV val = new ValorizarCarteraRV();	
		        ValorizarCarteraRV.ValorizarCarteraRVSalida res = val.logica(con, entrada);
		        if(res.P_OUT_ERRORESCONTADOR==0){
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraRV", "OK", "Proceso ejecutado exitosamente"));
		        }else{
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraRV", "ERROR", "Proceso ejecutado con errores"));
		        }
		        log.writeAsJson("Salida:",res);
			}catch(Exception e){
				out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraRV", "EXCEPCION", "Error al ejecutar el proceso:"+e.getMessage()));
				log.write("error no controlado: "+e.toString());
				e.printStackTrace();
			}
		}else{
			out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraRV", "OMITIDO", "Proceso omitido"));
		}
		
		log.write("2) p_ValorizaCarteraFFMM="+in.p_ValorizaCarteraFFMM);
		if("S".equalsIgnoreCase(in.p_ValorizaCarteraFFMM)){
			try{
				ActualizarCarterasFondosMutuos.EntradaActualizarCarterasFondosMutuos entrada = new ActualizarCarterasFondosMutuos.EntradaActualizarCarterasFondosMutuos();
				entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdModulo = in.p_IdModulo;
				
				ActualizarCarterasFondosMutuos val = new ActualizarCarterasFondosMutuos();
				ActualizarCarterasFondosMutuos.SalidaActualizarCarterasFondosMutuos res = val.logica(con, entrada);
				
				if(res.P_OUT_ERRORESCONTADOR==0){
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraFFMM", "OK", "Proceso ejecutado exitosamente"));
		        }else{
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraFFMM", "ERROR", "Proceso ejecutado con errores"));
		        }
				
		        log.writeAsJson("Salida:",res);
			}catch(Exception e){
				out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraFFMM", "EXCEPCION", "Error al ejecutar el proceso:"+e.getMessage()));
				log.write("error no controlado: "+e.toString());
				e.printStackTrace();
			}
		}else{
			out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraFFMM", "OMITIDO", "Proceso omitido"));
		}

		log.write("3) p_ValorizaCarteraRF="+in.p_ValorizaCarteraRF);
		if("S".equalsIgnoreCase(in.p_ValorizaCarteraRF)){
			try{
				ValorizarCarteraRF.ValorizarCarteraRFEntrada entrada = new ValorizarCarteraRF.ValorizarCarteraRFEntrada();
		    	entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdPortafolio = in.p_IdPortafolio;
				entrada.p_IdCliente = in.p_IdCliente;
				entrada.p_FechaSistema = FechaSistema;
				entrada.p_IdPais = in.p_IdPais;
				entrada.p_SimularOperacion = in.p_Simular;
		        ValorizarCarteraRF val =new ValorizarCarteraRF();		
		        ValorizarCarteraRF.ValorizarCarteraRFSalida res = val.logica(con, entrada);
		        if(res.P_OUT_ERRORESCONTADOR==0){
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraRF", "OK", "Proceso ejecutado exitosamente"));
		        }else{
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraRF", "ERROR", "Proceso ejecutado con errores"));
		        }
		        log.writeAsJson("Salida:",res);
			}catch(Exception e){
				out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraRF", "EXCEPCION", "Error al ejecutar el proceso:"+e.getMessage()));
				log.write("error no controlado: "+e.toString());
				e.printStackTrace();
			}
		}else{
			out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraRF", "OMITIDO", "Proceso omitido"));
		} 
		
		log.write("3.5) p_ValorizaCarteraIF="+in.p_ValorizaCarteraIF);
		if("S".equalsIgnoreCase(in.p_ValorizaCarteraIF)){
			try{
				ValorizarCarteraIF.ValorizarCarteraIFEntrada entrada = new ValorizarCarteraIF.ValorizarCarteraIFEntrada();
		    	entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdPortafolio = in.p_IdPortafolio;
				entrada.p_IdCliente = in.p_IdCliente;
				entrada.p_FechaSistema = FechaSistema;
				entrada.p_IdPais = in.p_IdPais;
				entrada.p_SimularOperacion = in.p_Simular;
		        ValorizarCarteraIF val =new ValorizarCarteraIF();		
		        ValorizarCarteraIF.ValorizarCarteraIFSalida res = val.logica(con, entrada);
		        if(res.P_OUT_ERRORESCONTADOR==0){
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraIF", "OK", "Proceso ejecutado exitosamente"));
		        }else{
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraIF", "ERROR", "Proceso ejecutado con errores"));
		        }
		        log.writeAsJson("Salida:",res);
			}catch(Exception e){
				out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraIF", "EXCEPCION", "Error al ejecutar el proceso:"+e.getMessage()));
				log.write("me cai:"+e.toString());
				e.printStackTrace();
			}
		}else{
			out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCarteraRFIF", "OMITIDO", "Proceso omitido"));
		} 
		
		log.write("4) p_ValorizaPrestamosEmprestitos="+in.p_ValorizaPrestamosEmprestitos);
		if("S".equalsIgnoreCase(in.p_ValorizaPrestamosEmprestitos)){
			try{
				ValorizarCarteraPrestEmpres.ValorizarCarteraPrestEmpresEntrada entrada = new ValorizarCarteraPrestEmpres.ValorizarCarteraPrestEmpresEntrada();
		    	entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdPortafolio = in.p_IdPortafolio;
				entrada.p_IdCliente = in.p_IdCliente;
				entrada.p_FechaSistema = FechaSistema;
				entrada.p_IdPais = in.p_IdPais;
				entrada.p_SimularOperacion = in.p_Simular;
		        ValorizarCarteraPrestEmpres val =new ValorizarCarteraPrestEmpres();		
		        ValorizarCarteraPrestEmpres.ValorizarCarteraPrestEmpresSalida res = val.logica(con, entrada);
		        if(res.P_OUT_ERRORESCONTADOR==0){
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaPrestamosEmprestitos", "OK", "Proceso ejecutado exitosamente"));
		        }else{
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaPrestamosEmprestitos", "ERROR", "Proceso ejecutado con errores"));
		        }
		        log.writeAsJson("Salida:",res);
			}catch(Exception e){
				out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaPrestamosEmprestitos", "EXCEPCION", "Error al ejecutar el proceso:"+e.getMessage()));
				log.write("error no controlado: "+e.toString());
				e.printStackTrace();
			}
		}else{
			out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaPrestamosEmprestitos", "OMITIDO", "Proceso omitido"));
		}
		
		log.write("5) p_ValorizaGarantiasRVRFIF="+in.p_ValorizaGarantiasRVRFIF);
		if("S".equalsIgnoreCase(in.p_ValorizaGarantiasRVRFIF)){
			try{
				ValorizarGarantiasRVRFIF.ValorizarCarteraRVRFIFEntrada entrada = new ValorizarGarantiasRVRFIF.ValorizarCarteraRVRFIFEntrada();
		    	entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdPortafolio = in.p_IdPortafolio;
				entrada.p_IdCliente = in.p_IdCliente;
				entrada.p_FechaSistema = FechaSistema;
				entrada.p_IdPais = in.p_IdPais;
				entrada.p_SimularOperacion = in.p_Simular;
		        ValorizarGarantiasRVRFIF val =new ValorizarGarantiasRVRFIF();		
		        ValorizarGarantiasRVRFIF.ValorizarCarteraRVRFIFSalida res = val.logica(con, entrada);
		        if(res.P_OUT_ERRORESCONTADOR==0){
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaGarantiasRVRFIF", "OK", "Proceso ejecutado exitosamente"));
		        }else{
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaGarantiasRVRFIF", "ERROR", "Proceso ejecutado con errores"));
		        }
		        log.writeAsJson("Salida:",res);
			}catch(Exception e){
				out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaGarantiasRVRFIF", "EXCEPCION", "Error al ejecutar el proceso:"+e.getMessage()));
				log.write("error no controlado: "+e.toString());
				e.printStackTrace();
			}
		}else{
			out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaGarantiasRVRFIF", "OMITIDO", "Proceso omitido"));
		}
		
		log.write("6) p_ValorizaSimultaneas="+in.p_ValorizaSimultaneas);
		if("S".equalsIgnoreCase(in.p_ValorizaSimultaneas)){
			try{
				ValorizarCarteraSimultaneas.ValorizarCarteraSIMEntrada entrada = new ValorizarCarteraSimultaneas.ValorizarCarteraSIMEntrada();
		    	entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdPortafolio = in.p_IdPortafolio;
				entrada.p_IdCliente = in.p_IdCliente;
				entrada.p_FechaSistema = FechaSistema;
				entrada.p_IdPais = in.p_IdPais;
				entrada.p_SimularOperacion = in.p_Simular;
		        ValorizarCarteraSimultaneas val =new ValorizarCarteraSimultaneas();		
		        ValorizarCarteraSimultaneas.ValorizarCarteraSIMSalida res = val.logica(con, entrada);
		        if(res.P_OUT_ERRORESCONTADOR==0){
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaSimultaneas", "OK", "Proceso ejecutado exitosamente"));
		        }else{
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaSimultaneas", "ERROR", "Proceso ejecutado con errores"));
		        }
		        log.writeAsJson("Salida:",res);
			}catch(Exception e){
				out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaSimultaneas", "EXCEPCION", "Error al ejecutar el proceso:"+e.getMessage()));
				log.write("error no controlado: "+e.toString());
				e.printStackTrace();
			}
		}else{
			out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaSimultaneas", "OMITIDO", "Proceso omitido"));
		}
		
		log.write("7) p_ValorizaCupones="+in.p_ValorizaCupones);
		if("S".equalsIgnoreCase(in.p_ValorizaCupones)){
			try{
				ValorizarCarteraCupones.ValorizarCarteraCuponesEntrada entrada = new ValorizarCarteraCupones.ValorizarCarteraCuponesEntrada();
		    	entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdPortafolio = in.p_IdPortafolio;
				entrada.p_IdCliente = in.p_IdCliente;
				entrada.p_FechaSistema = FechaSistema;
				entrada.p_IdPais = in.p_IdPais;
				entrada.p_SimularOperacion = in.p_Simular;
		        ValorizarCarteraCupones val=new ValorizarCarteraCupones();
		        ValorizarCarteraCupones.ValorizarCarteraCuponesSalida res = val.logica(con, entrada);
		        if(res.P_OUT_ERRORESCONTADOR==0){
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCupones", "OK", "Proceso ejecutado exitosamente"));
		        }else{
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCupones", "ERROR", "Proceso ejecutado con errores"));
		        }
		        log.writeAsJson("Salida:",res);
			}catch(Exception e){
				out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCupones", "EXCEPCION", "Error al ejecutar el proceso:"+e.getMessage()));
				log.write("error no controlado: "+e.toString());
				e.printStackTrace();
			}
		}else{
			out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaCupones", "OMITIDO", "Proceso omitido"));
		}
		
		log.write("8) p_ValorizaPactos="+in.p_ValorizaPactos);
		if("S".equalsIgnoreCase(in.p_ValorizaPactos)){
			try{
				ValorizarCarteraPactos.ValorizarCarteraPactosEntrada entrada = new ValorizarCarteraPactos.ValorizarCarteraPactosEntrada();
		    	entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdPortafolio = in.p_IdPortafolio;
				entrada.p_IdCliente = in.p_IdCliente;
				entrada.p_FechaSistema = FechaSistema;
				entrada.p_IdPais = in.p_IdPais;
				entrada.p_SimularOperacion = in.p_Simular;
		        ValorizarCarteraPactos val =new ValorizarCarteraPactos();		
		        ValorizarCarteraPactos.ValorizarCarteraPactosSalida res = val.logica(con, entrada);
		        if(res.P_OUT_ERRORESCONTADOR==0){
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaPactos", "OK", "Proceso ejecutado exitosamente"));
		        }else{
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaPactos", "ERROR", "Proceso ejecutado con errores"));
		        }
		        log.writeAsJson("Salida:",res);
			}catch(Exception e){
				out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaPactos", "EXCEPCION", "Error al ejecutar el proceso:"+e.getMessage()));
				log.write("error no controlado: "+e.toString());
				e.printStackTrace();
			}
		}else{
			out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaPactos", "OMITIDO", "Proceso omitido"));
		}	
		
		log.write("8) p_ValorizaFuturos="+in.p_ValorizaFuturos);
		if("S".equalsIgnoreCase(in.p_ValorizaFuturos)){
			try{
				ValorizarCarteraFuturos.ValorizarCarteraFuturosEntrada entrada = new ValorizarCarteraFuturos.ValorizarCarteraFuturosEntrada();
		    	entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdPortafolio = in.p_IdPortafolio;
				entrada.p_IdCliente = in.p_IdCliente;
				entrada.p_FechaSistema = FechaSistema;
				entrada.p_IdPais = in.p_IdPais;
				entrada.p_SimularOperacion = in.p_Simular;
				ValorizarCarteraFuturos val =new ValorizarCarteraFuturos();		
				ValorizarCarteraFuturos.ValorizarCarteraFuturosSalida res = val.logica(con, entrada);
		        if(res.P_OUT_ERRORESCONTADOR==0){
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaFuturos", "OK", "Proceso ejecutado exitosamente"));
		        }else{
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaFuturos", "ERROR", "Proceso ejecutado con errores"));
		        }
		        log.writeAsJson("Salida:",res);
			}catch(Exception e){
				out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaFuturos", "EXCEPCION", "Error al ejecutar el proceso:"+e.getMessage()));
				log.write("error no controlado: "+e.toString());
				e.printStackTrace();
			}
		}else{
			out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaFuturos", "OMITIDO", "Proceso omitido"));
		}
		
		log.write("9) p_ValorizaOpciones="+in.p_ValorizaOpciones);
		if("S".equalsIgnoreCase(in.p_ValorizaOpciones)){
			try{
				ValorizarCarteraOpciones.ValorizarCarteraOpcionesEntrada entrada = new ValorizarCarteraOpciones.ValorizarCarteraOpcionesEntrada();
		    	entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdPortafolio = in.p_IdPortafolio;
				entrada.p_IdCliente = in.p_IdCliente;
				entrada.p_FechaSistema = FechaSistema;
				entrada.p_IdPais = in.p_IdPais;
				entrada.p_SimularOperacion = in.p_Simular;
				ValorizarCarteraOpciones val =new ValorizarCarteraOpciones();		
				ValorizarCarteraOpciones.ValorizarCarteraOpcionesSalida res = val.logica(con, entrada);
		        if(res.P_OUT_ERRORESCONTADOR==0){
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaOpciones", "OK", "Proceso ejecutado exitosamente"));
		        }else{
		        	out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaOpciones", "ERROR", "Proceso ejecutado con errores"));
		        }
		        log.writeAsJson("Salida:",res);
			}catch(Exception e){
				out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaOpciones", "EXCEPCION", "Error al ejecutar el proceso:"+e.getMessage()));
				log.write("error no controlado: "+e.toString());
				e.printStackTrace();
			}
		}else{
			out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("p_ValorizaOpciones", "OMITIDO", "Proceso omitido"));
		}
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_DETALLE_EJEUCION.add(new OutValorizarCustodiaUIbatch.DetalleEjecucion("Fin", "", "Finaliza el proceso [Portafolio:"+(in.p_IdPortafolio>0?in.p_IdPortafolio:"TODOS")+"]"));
		log.write("*** Finaliza el procesamiento ***");
		return out;
	};
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		InValorizarCustodiaUIbatch jin=gson.fromJson(in.toString() ,InValorizarCustodiaUIbatch.class);
		OutValorizarCustodiaUIbatch out=logica(conn,jin);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getMBIConnection()) {
			try {
				InValorizarCustodiaUIbatch entrada = new InValorizarCustodiaUIbatch();			
				entrada.p_IdEmpresa = 1L;		
				entrada.p_IdUsuario = 25L;
				entrada.p_IdModulo = "CUS";
				entrada.p_FechaSistema = "2022-10-03";
				entrada.p_IdPais = 44L;
				entrada.p_IdPortafolio = 0L; //512341 Juanjo portafolio 1
				entrada.p_ValorizaCarteraRV = "S";
				entrada.p_ValorizaCarteraFFMM = "N";
				entrada.p_ValorizaCarteraRF = "N";
				entrada.p_ValorizaCarteraIF = "N";
				entrada.p_ValorizaPrestamosEmprestitos = "N";
				entrada.p_ValorizaGarantiasRVRFIF = "N";
				entrada.p_ValorizaSimultaneas = "N";
				entrada.p_ValorizaCupones = "N";
				entrada.p_ValorizaPactos = "N";
			    ValorizarCustodiaUIbatch  kk = new ValorizarCustodiaUIbatch();
			    ValorizarCustodiaUIbatch.OutValorizarCustodiaUIbatch salida= kk.logica(con, entrada);
			    System.out.println("RESULTADO: "+salida.toString());
	    	}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
	};
}
