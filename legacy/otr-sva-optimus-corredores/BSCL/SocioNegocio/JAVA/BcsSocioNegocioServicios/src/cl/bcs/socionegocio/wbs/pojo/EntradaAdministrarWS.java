package cl.bcs.socionegocio.wbs.pojo;


import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cl.bcs.plataforma.frm.ValidarCamposObligatorios;
import cl.bcs.plataforma.frm.ValidarCamposObligatorios.CampoGrid;
import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.frm.ValidarCamposObligatorios.SalidaValidarCamposObligatorios;
import cl.bcs.utils.BcsPojo;

public class EntradaAdministrarWS extends BcsPojo {
	public Long p_IdEmpresa;
	public Long p_IdUsuario;
	public String p_IdModulo;
	public String p_UsaReglasNegocio;
	public String p_CodigoFormulario;
	public String p_IdTipoCanal;
	
	public EntradaAdministrarWS() {
		this.p_IdModulo = "SOC";
		this.p_UsaReglasNegocio = "S";
		this.p_IdTipoCanal = "WBS";
		this.p_CodigoFormulario = "";
	}

	public <T> SalidaAdministrarWS validarCamposObligatoriosEntrada(Connection con, List<T> objetos, Class<T> tipo, FormularioCanal formularioCanal ,SalidaAdministrarWS salidaCrearWS) throws Exception {
	    ValidarCamposObligatorios validarCamposObligatorios = new ValidarCamposObligatorios();
	    SalidaValidarCamposObligatorios salidaValidarCamposObligatorios = validarCamposObligatorios.validarObjeto(con, formularioCanal, objetos, tipo);

	    salidaCrearWS.P_OUT_ERRORESCONTADOR = salidaValidarCamposObligatorios.P_OUT_ERRORESCONTADOR;
	    salidaCrearWS.P_OUT_MENSAJES.addAll(salidaValidarCamposObligatorios.P_OUT_MENSAJES);

	    return salidaCrearWS;
	}
	
	public <T> SalidaAdministrarWS validarCamposObligatoriosEntradaIndicadores(Connection con, Map<String, Indicador> indicadores, Class<T> tipo, FormularioCanal formularioCanal ,SalidaAdministrarWS salidaCrearWS) throws Exception {
	    ValidarCamposObligatorios validarCamposObligatorios = new ValidarCamposObligatorios();
	    SalidaValidarCamposObligatorios salidaValidarCamposObligatorios = validarCamposObligatorios.validarIndicadores(con, formularioCanal, indicadores, tipo);

	    salidaCrearWS.P_OUT_ERRORESCONTADOR = salidaValidarCamposObligatorios.P_OUT_ERRORESCONTADOR;
	    salidaCrearWS.P_OUT_MENSAJES.addAll(salidaValidarCamposObligatorios.P_OUT_MENSAJES);

	    return salidaCrearWS;
	}
	
	public <T> SalidaAdministrarWS validarGrillasObligatoriasEntrada(Connection con, List<T> objetos, String grilla, SalidaAdministrarWS salidaCrearWS) throws Exception {
		
		if("S".equals(p_UsaReglasNegocio)) {
			FormularioCanal formularioCanal = new FormularioCanal();
			formularioCanal.IdEmpresa = this.p_IdEmpresa;
			formularioCanal.CodigoFormulario = this.p_CodigoFormulario;
			formularioCanal.IdTipoCanal = this.p_IdTipoCanal;
						
			CampoGrid campoGrid = new CampoGrid();
			campoGrid.CodigoCampoBackend = grilla;
			campoGrid.Tamano = Long.valueOf(objetos.size());
			List<CampoGrid> camposGrid = Arrays.asList(campoGrid);
			ValidarCamposObligatorios validarCamposObligatorios = new ValidarCamposObligatorios();
			SalidaValidarCamposObligatorios salidaValidarCamposObligatorios = validarCamposObligatorios.validarGrilla(con, formularioCanal, camposGrid);
			
			salidaCrearWS.P_OUT_ERRORESCONTADOR += salidaValidarCamposObligatorios.P_OUT_ERRORESCONTADOR;
			salidaCrearWS.P_OUT_MENSAJES.addAll(salidaValidarCamposObligatorios.P_OUT_MENSAJES);
		}
		
		return salidaCrearWS;
	}
}
