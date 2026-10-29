package cl.bcs.loaderapp.concrete.portafolio.cuentacustodio;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.portafolio.CuentaCustodio;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente.SalidaSocioNegocio;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CuentaCustodioDbLoader implements DbLoader {
	public String SQL_INGRESAR_CUENTA_CUSTODIO = loadResourceToString("cl/bcs/loaderapp/sql/portafolio/cuentacustodio/CuentaCustodio_Ingresar.sql");
	public String SQL_CONSULTA_PORTAFOLIO = "SELECT \"IdPortafolio\" FROM \"BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio\" CCP INNER JOIN \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" DI ON CCP.\"IdPersona\" = DI.\"IdPersona\" WHERE DI.\"DocumentoIdentidad\" = :DocumentoIdentidadCliente AND DI.\"IdTipoDocumentoIdentidad\" = :TipoDocumentoIdentidad AND CCP.\"CodigoPortafolio\" = :CodigoPortafolio;";
	
	public static class Portafolio{
		public Long IdPortafolio;

		@Override
		public String toString() {
			return String.format("Portafolio [IdPortafolio=%s]", IdPortafolio);
		}
		
	}
	public static class EntidadCuentaCustodio{
		public Long IdCuentaCustodioPortafolio;
		public Long IdPortafolio;
		public String IdBoveda;
		public String Codigo;
		
		public Long IdUsuarioCreador;
		public String IdEstado;
		public String IdEstadoAutorizacion;
		public String IdTipoCuentaCustodia;
		public String IdTipoCustodia;
		public String IdModeloCustodia;
		public long IdPersonaBancoCustodio;
		public long IdPersonaBancoLiquidador;
		public String LiquidaDividendos;
		public String LiquidaCupones;
		public String LiquidaVencimientos;
		public String LiquidaPrepagos;
		@Override
		public String toString() {
			return String.format(
					"EntidadCuentaCustodio [IdCuentaCustodioPortafolio=%s, IdPortafolio=%s, IdBoveda=%s, Codigo=%s, IdUsuarioCreador=%s, IdEstado=%s, IdEstadoAutorizacion=%s, IdTipoCuentaCustodia=%s, IdTipoCustodia=%s, IdModeloCustodia=%s, IdPersonaBancoCustodio=%s, IdPersonaBancoLiquidador=%s, LiquidaDividendos=%s, LiquidaCupones=%s, LiquidaVencimientos=%s, LiquidaPrepagos=%s]",
					IdCuentaCustodioPortafolio, IdPortafolio, IdBoveda, Codigo, IdUsuarioCreador, IdEstado,
					IdEstadoAutorizacion, IdTipoCuentaCustodia, IdTipoCustodia, IdModeloCustodia,
					IdPersonaBancoCustodio, IdPersonaBancoLiquidador, LiquidaDividendos, LiquidaCupones,
					LiquidaVencimientos, LiquidaPrepagos);
		}
		
		
	}
	
	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<String>();
		Long idUsuarioDefecto = in.p_IdUsuario;
        Long idEmpresaDefecto = in.p_IdEmpresa;
		SQL sql = new SQL();
		
		try(Connection conn = jsJB.getConnection()){
			int linea = 1;
			List<CuentaCustodio> cuentascustodioCSV = (List<CuentaCustodio>) (Object) lines;
			for(CuentaCustodio cuentacustodio : cuentascustodioCSV){
				try {
					//Identificar IdCLiente medictae el documento de identidad.
					ConsultarDocumentoIdentidadCliente consDocCli = new ConsultarDocumentoIdentidadCliente();
					SalidaSocioNegocio idsSocioNegocio = consDocCli.logica(conn, cuentacustodio.getDocumentoIdentidadCliente(), cuentacustodio.getTipoDocumentoIdentidad(), "", idEmpresaDefecto);
	
					HashMap<String, Object> params = new HashMap<>();
					params.put("DocumentoIdentidadCliente", cuentacustodio.getDocumentoIdentidadCliente());
					params.put("TipoDocumentoIdentidad", cuentacustodio.getTipoDocumentoIdentidad());
					params.put("CodigoPortafolio", cuentacustodio.getCodigoPortafolio());
					List<Portafolio> portafolios = sql.query(conn, SQL_CONSULTA_PORTAFOLIO, params, Portafolio.class);
					
					//obtener id persona banco custodio
					idsSocioNegocio = consDocCli.logica(conn, cuentacustodio.getDocumentoIdentidadCliente(), cuentacustodio.getTipoDocumentoIdentidad(), "", idEmpresaDefecto);
					
					System.out.println(portafolios);
					//crear cuentacustodio
					EntidadCuentaCustodio cta = new EntidadCuentaCustodio();
					cta.IdCuentaCustodioPortafolio = Utils.getSecuencia(conn, "BSCL.Portafolio.BD.Secuencias::CuentaCustodioPortafolio", (long) 1).get(0);
					cta.IdPortafolio = portafolios.get(0).IdPortafolio;
					cta.IdBoveda = cuentacustodio.getIdBoveda();
					cta.Codigo = cuentacustodio.getCodigo();
					cta.IdUsuarioCreador = idUsuarioDefecto;
					cta.IdEstado = cuentacustodio.getIdEstado();
					cta.IdEstadoAutorizacion = "ING";
					cta.IdTipoCuentaCustodia = cuentacustodio.getIdTipoCuentaCustodia();
					cta.IdTipoCustodia = cuentacustodio.getIdTipoCustodia();
					cta.IdModeloCustodia = cuentacustodio.getIdModeloCustodia();
					
					//por defecto
					cta.IdPersonaBancoCustodio = 0;
					cta.IdPersonaBancoLiquidador = 0;
					cta.LiquidaDividendos = "N";
					cta.LiquidaCupones = "N";
					cta.LiquidaVencimientos = "N";
					cta.LiquidaPrepagos = "N";
	
					int[] resultado = sql.batch(conn, SQL_INGRESAR_CUENTA_CUSTODIO, Arrays.asList(cta), EntidadCuentaCustodio.class);
				
					if(resultado[0]==1){
						salida.add(linea + " - CUENTA CUSTODIO " + cuentacustodio.getDocumentoIdentidadCliente() + " INGRESADA CORRECTAMENTE");
					}else{
						salida.add(linea + " - CUENTA CUSTODIO " + cuentacustodio.getDocumentoIdentidadCliente() + " NO INGRESADA");
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					salida.add(linea + " - CUENTA CUSTODIO " + cuentacustodio.getDocumentoIdentidadCliente() + " NO INGRESADA");
				}
				linea++;
			}
		}catch(Exception e) {
			salida.add("ERROR AL OBTENER CONEXION");
		}
		return salida;
	}

}
