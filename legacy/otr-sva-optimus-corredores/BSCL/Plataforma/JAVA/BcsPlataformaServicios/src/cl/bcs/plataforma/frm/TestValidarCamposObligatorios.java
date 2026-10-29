package cl.bcs.plataforma.frm;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import cl.bcs.plataforma.frm.ValidarCamposObligatorios.CampoGrid;
import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.frm.ValidarCamposObligatorios.SalidaValidarCamposObligatorios;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.utils.Fecha;

public class TestValidarCamposObligatorios {
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
			try {
				ValidarCamposObligatorios demo = new ValidarCamposObligatorios();
				FormularioCanal formularioCanal = new FormularioCanal();
				formularioCanal.CodigoFormulario = "INGRESAR_CLIENTE_FULL_NATURAL";
				formularioCanal.IdTipoCanal = "OPT";
				formularioCanal.IdEmpresa = 1L;
				Enrolamiento enrolamiento = new Enrolamiento();
				enrolamiento.Nombres = "sadasdadsa";
				enrolamiento.ApellidoPaterno = "sadasdadsa";
				enrolamiento.ApellidoMaterno = "asdasdas";
				enrolamiento.FechaNacimiento = Fecha.toDate("1986-01-01");
				enrolamiento.IdCiudad = 1L;
				enrolamiento.EstadoCivil = "VI";
				
				List<Enrolamiento> entrolamientos = Arrays.asList(enrolamiento);
				SalidaValidarCamposObligatorios ObjOut = demo.validarObjeto(con, formularioCanal, entrolamientos, Enrolamiento.class);
				System.out.println(ObjOut.toString());
				CampoGrid campoGrid = new CampoGrid();
				campoGrid.Tamano = 3L;
				campoGrid.CodigoCampoBackend = "Direcciones";
				List<CampoGrid> grillas = Arrays.asList(campoGrid);
				
				formularioCanal = new FormularioCanal();
				formularioCanal.CodigoFormulario = "DIRECCIONES_FISICAS_FULL_NATURAL";
				formularioCanal.IdTipoCanal = "AGE";
				formularioCanal.IdEmpresa = 1L;
				
				ObjOut = demo.validarGrilla(con, formularioCanal, grillas);
				
				con.rollback();
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			con.rollback();
    	}
		System.out.println("== END ==");
	}
}
