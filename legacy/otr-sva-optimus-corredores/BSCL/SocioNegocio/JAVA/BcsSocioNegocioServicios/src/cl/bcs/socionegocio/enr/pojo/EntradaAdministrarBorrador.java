package cl.bcs.socionegocio.enr.pojo;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.plataforma.frm.AsignarCamposFormulario;
import cl.bcs.plataforma.frm.ValidarCamposObligatorios;
import cl.bcs.plataforma.frm.ValidarCamposObligatorios.CampoGrid;
import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.frm.ValidarCamposObligatorios.SalidaValidarCamposObligatorios;
import cl.bcs.utils.BcsPojo;

public class EntradaAdministrarBorrador extends BcsPojo {
	public Long p_IdEmpresa;
	public Long p_IdUsuario;
	public String p_IdModulo;
	public String p_UsaReglasNegocio;
	public String p_CodigoFormulario;
	public String p_IdTipoCanal;
	
	public <T> List<T> asignarCamposPorDefecto(Connection con, List<T> objetos, Class<T> tipo) throws Exception{
		FormularioCanal formularioCanal = new FormularioCanal();
		formularioCanal.IdEmpresa = this.p_IdEmpresa;
		formularioCanal.CodigoFormulario = this.p_CodigoFormulario;
		formularioCanal.IdTipoCanal = this.p_IdTipoCanal;
		
		AsignarCamposFormulario asignarCamposFormulario = new AsignarCamposFormulario();
		objetos = asignarCamposFormulario.asignarValoresDefecto(con, formularioCanal, objetos, tipo);
		return objetos;
	}
	
	public <T> SalidaAdministrarBorrador validarCamposObligatoriosEntrada(Connection con, List<T> objetos, Class<T> tipo, SalidaAdministrarBorrador salidaCrearBorrador) throws Exception {
		
		if("S".equals(this.p_UsaReglasNegocio)) {
			FormularioCanal formularioCanal = new FormularioCanal();
			formularioCanal.IdEmpresa = this.p_IdEmpresa;
			formularioCanal.CodigoFormulario = this.p_CodigoFormulario;
			formularioCanal.IdTipoCanal = this.p_IdTipoCanal;
			
			ValidarCamposObligatorios validarCamposObligatorios = new ValidarCamposObligatorios();
			SalidaValidarCamposObligatorios salidaValidarCamposObligatorios = validarCamposObligatorios.validarObjeto(con, formularioCanal, objetos, tipo);
			
			salidaCrearBorrador.P_OUT_ERRORESCONTADOR += salidaValidarCamposObligatorios.P_OUT_ERRORESCONTADOR;
			salidaCrearBorrador.P_OUT_MENSAJES = salidaValidarCamposObligatorios.P_OUT_MENSAJES;
		}
		
		return salidaCrearBorrador;
	}
	
	public <T> SalidaAdministrarBorrador validarGrillasObligatoriasEntrada(Connection con, List<T> objetos, String grilla, SalidaAdministrarBorrador salidaCrearBorrador) throws Exception {
		
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
			
			salidaCrearBorrador.P_OUT_ERRORESCONTADOR += salidaValidarCamposObligatorios.P_OUT_ERRORESCONTADOR;
			salidaCrearBorrador.P_OUT_MENSAJES.addAll(salidaValidarCamposObligatorios.P_OUT_MENSAJES);
		}
		
		return salidaCrearBorrador;
	}
	
	public <T> SalidaAdministrarBorrador validarCamposObligatoriosEntradaIndicadores(Connection con,List<Indicador> objetos, Class<T> tipo,  SalidaAdministrarBorrador salidaCrearBorrador) throws Exception {
		
		if("S".equals(this.p_UsaReglasNegocio)) {
			Map<String, Indicador> indicadores = new HashMap<>();
			FormularioCanal formularioCanal = new FormularioCanal();
			formularioCanal.IdEmpresa = this.p_IdEmpresa;
			formularioCanal.CodigoFormulario = this.p_CodigoFormulario;
			formularioCanal.IdTipoCanal = this.p_IdTipoCanal;
			
			objetos.parallelStream().forEach(obj -> {
				indicadores.put(obj.CodigoTipoIndicador, obj);
			});
			
			ValidarCamposObligatorios validarCamposObligatorios = new ValidarCamposObligatorios();
			SalidaValidarCamposObligatorios salidaValidarCamposObligatorios = validarCamposObligatorios.validarIndicadoresEnrolamiento(con, formularioCanal, indicadores, tipo);
			
			salidaCrearBorrador.P_OUT_ERRORESCONTADOR += salidaValidarCamposObligatorios.P_OUT_ERRORESCONTADOR;
			salidaCrearBorrador.P_OUT_MENSAJES = salidaValidarCamposObligatorios.P_OUT_MENSAJES;
		}
		
		return salidaCrearBorrador;
	}
}
