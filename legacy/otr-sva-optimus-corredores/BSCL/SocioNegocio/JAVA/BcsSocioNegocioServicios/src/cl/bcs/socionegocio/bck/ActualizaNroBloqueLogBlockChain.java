package cl.bcs.socionegocio.bck;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.Utils;
import cl.bcs.utils.SQL;

public class ActualizaNroBloqueLogBlockChain {

	public final String update_nroblock_blockchain = "UPDATE \"BSCL.Custodia.BD::CUS.Configuracion\" \r\n" + 
													 "SET  \"Valor\"= :p_Valor \r\n" + 
													 "WHERE \"IdEmpresa\" = :p_IdEmpresa \r\n" + 
													 "AND \"CodigoParametro\"	= :p_CodigoParametro";
	
	public final String CONS_CODIGO_EXITOSO			= "BLOCKCHAIN_NRO_BLOCK_DE_LOG_ACTUALIZADO_EXITOSAMENTE";
	public final String CONS_CODIGO_ERROR 			= "BLOCKCHAIN_NO_SE_PUSO_ACTUALIZAR_NRO_BLOCK_DE_LOG";
			
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class ActualizaNroBloqueLogBlockChain_Entrada
	{
		public Long 				p_IdEmpresa;
		public String				p_ValorBlock;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}

	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class ActualizaNroBloqueLogBlockChain_Salida
	{
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public ActualizaNroBloqueLogBlockChain_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
		}
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}

	public static class UpdateNroBloqueBC
	{
		public Long		p_IdEmpresa;
		public String	p_CodigoParametro;
		public String	p_Valor;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}

	public ActualizaNroBloqueLogBlockChain_Salida logica (Connection conn, ActualizaNroBloqueLogBlockChain_Entrada in)  {
		
		UpdateNroBloqueBC objUpdate = new UpdateNroBloqueBC();
		SQL sql = new SQL();
		ActualizaNroBloqueLogBlockChain_Salida out = new ActualizaNroBloqueLogBlockChain_Salida();
		
		System.out.println("Inicio Update Nro de Block Leido desde LOG BlockChain");
		
		objUpdate.p_CodigoParametro = ConstantesBCK.NOMBRE_PARAMETRO_BLOCKCHAIN_SECUENCIA;
		objUpdate.p_IdEmpresa		= in.p_IdEmpresa;
		objUpdate.p_Valor			= in.p_ValorBlock;
		
		int[] rows;
		try {
			rows = sql.batch(conn, update_nroblock_blockchain, Arrays.asList(objUpdate), UpdateNroBloqueBC.class);
			System.out.println(rows.toString());
			out.P_OUT_ERRORESCONTADOR = 0L;
			
			String msg	=	Diccionario.getMensaje(conn,in.p_IdEmpresa,CONS_CODIGO_EXITOSO)
							.msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,CONS_CODIGO_EXITOSO,msg));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			out.P_OUT_ERRORESCONTADOR = 1L;
			e.printStackTrace();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,CONS_CODIGO_ERROR,e.getMessage()));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			out.P_OUT_ERRORESCONTADOR = 1L;
			e.printStackTrace();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,CONS_CODIGO_ERROR,e.getMessage()));
		}
		
		System.out.println("Finaliza Update Nro de Block Leido desde LOG BlockChain");
		
		return out;
	}
	

}
