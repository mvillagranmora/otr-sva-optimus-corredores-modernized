package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.bat.AgregarPrograma.OutAgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;


class TestAgregarPrograma {
	
	@Test
    void testCrearCliente() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		AgregarPrograma demo = new AgregarPrograma();    		
    		
    		List<Parametro> params = new ArrayList<Parametro>();
    		Parametro param = new Parametro();
    		param.NombreParametro = "IdEmpresa";
    		param.Valor = "1";
			param.ValorBlob = "";
    		params.add(param);
    		Parametro param2 = new Parametro();
    		param2.NombreParametro = "IdUsuario";
    		param2.Valor = "1";
    		param2.ValorBlob = "";
			params.add(param2);
			Parametro param3 = new Parametro();
			param3.NombreParametro = "Tabla";
			param3.Valor = "\"BSCL.Custodia.BD::HIS.Cartera\"";
			param3.ValorBlob = "";
			params.add(param3);
			Parametro param4 = new Parametro();
			param4.NombreParametro = "Filtro";
			param4.Valor = "\"FechaRespaldo\"";
			param4.ValorBlob = "";
			params.add(param4);
			Parametro param5 = new Parametro();
			param5.NombreParametro = "Desde";
			param5.Valor = "2024-07-31";
			param5.ValorBlob = "";
			params.add(param5);
			Parametro param6 = new Parametro();
			param6.NombreParametro = "Hasta";
    		param6.Valor = "2024-07-31";
    		param6.ValorBlob = "";
			params.add(param6);
			Parametro param7 = new Parametro();
			param7.NombreParametro = "SoloLimpia";
			param7.Valor = "N";
			param7.ValorBlob = "";
			params.add(param7);
    		
    		InAgregarPrograma entradaAgregarPrograma = new InAgregarPrograma();  		
    		entradaAgregarPrograma.p_IdEmpresa = 1L;
    		entradaAgregarPrograma.p_IdUsuario = 1L;
    		entradaAgregarPrograma.p_IdModulo = "PLA";
    		entradaAgregarPrograma.p_IdTipoPrograma = "MIGRARDATAHISTORICA";
    		entradaAgregarPrograma.p_Segundos = 0L;
    		entradaAgregarPrograma.p_FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA", "PLA.FechaSistema");
    		entradaAgregarPrograma.p_Parametros = params;
    		entradaAgregarPrograma.p_Comentario = "Data migrada desde optimus a PCBHistorico";
    		
    		OutAgregarPrograma salida = demo.logica(conn, entradaAgregarPrograma);
					
			assertTrue(salida.P_OUT_IDPROGRAMA > 0);
    		conn.rollback();
    	}
    }
}