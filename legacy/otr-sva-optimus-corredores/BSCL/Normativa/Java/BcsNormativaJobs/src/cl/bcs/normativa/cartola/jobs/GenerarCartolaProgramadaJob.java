package cl.bcs.normativa.cartola.jobs;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;

import com.google.gson.Gson;

import cl.bcs.normativa.cartola.GestionarNomina;
import cl.bcs.normativa.cartola.GestionarNomina.GestionarNominaEntrada;
import cl.bcs.normativa.cartola.GestionarNomina.GestionarNominaSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarCartolaProgramadaJob extends BcsJob {
	
	private final Log log = new Log("NOR.GenerarCartolasProgramadas");
	protected static final String UPDATE_NOMINA = "UPDATE \"BSCL.Normativa.BD::CAR.Nomina\" SET \"EstadoProceso\" = 'PEN' WHERE \"IdNomina\" = :IdNomina";
	
	public static class Wrap extends GestionarNomina{
		public Wrap() {
			super();
		}
	}
	
	public static class EntradaGenerarCartolaProgramada{
		public Long IdEmpresa;
		public Long IdUsuario;
		public String IdModulo;
		public String ConfiguracionNomina;
		@Override
		public String toString() {
			return String.format(
					"EntradaGenerarCartolaProgramada [IdEmpresa=%s, IdUsuario=%s, IdModulo=%s, ConfiguracionNomina=%s]",
					IdEmpresa, IdUsuario, IdModulo, ConfiguracionNomina);
		}
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		EntradaGenerarCartolaProgramada in = new EntradaGenerarCartolaProgramada();
		in.IdEmpresa = Long.parseLong(this.getOptionalParam("IdEmpresa").orElse("1"));
		in.IdUsuario = Long.parseLong(this.getOptionalParam("IdUsuario").orElse("1"));
		in.IdModulo = this.getOptionalParam("IdModulo").orElse("CAR");
		in.ConfiguracionNomina = this.getOptionalParam("ConfiguracionNomina").orElse("{}");
		
		return logica(con, in);
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}
	
	public boolean logica(Connection conn, EntradaGenerarCartolaProgramada in) {
		Gson gson = Utils.getGsonBuilder();
		
		
		try {
			SQL sql = new SQL();
			//01 VARIABLES DE SISTEMA
			GestionarNominaEntrada confNomina = gson.fromJson(in.ConfiguracionNomina, GestionarNominaEntrada.class);
			confNomina.p_AccionNomina = "INGRESAR";
			//OBTENER FECHA DEL SISTEMA
			String fechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(conn, in.IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");
			LocalDate fechaActual = LocalDate.parse(fechaSistema, Fecha.YYYYMMDD);
			//CAMBIAR AL INICIO DEL MES ANTERIOR
			LocalDate inicioMes = fechaActual.minusMonths(1).withDayOfMonth(1);
			//OBTENER EL ULTIMO DIA DEL MES
			LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
			
			confNomina.p_FechaDesde = inicioMes.format(Fecha.YYYYMMDD);
			confNomina.p_FechaHasta = finMes.format(Fecha.YYYYMMDD);
			confNomina.p_Periodo = inicioMes.format(Fecha.YYYYMM);
			
			//Ejecutar Nomina
			GestionarNomina nomina = new GestionarNomina();
			GestionarNominaSalida salida = nomina.logica(conn, confNomina);

			System.out.println(salida);
			
		} catch (Exception e) {
			log.write(e);
			return false;
		}

		return true;
	}

	public static void main(String[] args) {
		
		try (Connection conn = PlataformaUtils.getDESAConnection()){
			EntradaGenerarCartolaProgramada in = new EntradaGenerarCartolaProgramada();
			in.IdEmpresa = Long.parseLong("1");
			in.IdUsuario = Long.parseLong("1");
			in.IdModulo = "CAR";
			in.ConfiguracionNomina = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":3,\"p_IdModulo\":\"CAR\",\"p_AccionNomina\":\"PROGRAMADO\",\"p_IdNomina\":0,\"p_TipoProceso\":\"PROGRAMADO\",\"p_Periodo\":null,\"p_FechaDesde\":null,\"p_FechaHasta\":null,\"p_SegundosEsperaProceso\":0,\"p_Comentario\":\"\",\"p_Certificada\":null,\"p_NotaCertificacion\":null,\"p_IncluirPortafoliosConSaldo\":\"S\",\"p_IncluirPortafoliosConCustodia\":\"S\",\"p_IncluirPortafoliosConMovimientoCuentaInversion\":\"S\",\"p_IncluirCategoriaInversionista\":\"N\",\"p_CategoriaInversionista\":\"\",\"p_IncluirTipoPersona\":\"N\",\"p_TipoPersona\":\"\",\"p_IncluirTipoCliente\":\"N\",\"p_TipoCliente\":\"\",\"p_IncluirPortafoliosExcluidos\":\"N\",\"p_ExcluirPortafoliosIncluidosOtraNomina\":\"N\",\"p_ExcluirOperadoresDirectos\":\"N\",\"p_MensajeProceso\":null,\"p_OpcionProceso\":\"NORMATIVO\",\"p_IdCartola\":null,\"p_Clientes\":[],\"p_Habilitado\":\"N\",\"p_DiaEjecucion\":\"1\",\"p_HoraEjecucion\":\"0:00\"}";
			
			GenerarCartolaProgramadaJob job = new GenerarCartolaProgramadaJob();
			job.logica(conn, in);
			conn.rollback();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
