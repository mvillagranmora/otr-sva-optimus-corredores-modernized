package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.BcsPojo;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;
import java.util.Date;

/**
 * ORACLE FAKE of legacy cl.bcs.cuentaInversion.CrearCargoAbono (cl/bcs/cuentaInversion/CrearCargoAbono.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Used by ProcesarDistribucion.
 */
public class CrearCargoAbono implements JavaCall {

	// legacy cl/bcs/cuentaInversion/CrearCargoAbono.java:72-119 (verbatim)
	public static class ObjCargoAbonoAGenerar implements Cloneable{
		public Long					NumeroCargoAbono;
		public Long					IdPortafolio;
		public String				IdMoneda;
		public String				IdTipoMovimiento;
		public String				IdConcepto;
		public String				Descripcion;
		public String				IdCondicionDeLiquidacion;
		public String				FechaCondicionDeLiquidacion;
		public BigDecimal			Monto; 
		public Long					IdEjecutivo; 
		public Long					IdSucursal; 
		public String				IdTipoProducto; 
		public String				IdTipoOperacion; 
		public Long					IdOperacion; 
		public String				FechaOperacion; 
		public String				FechaLiquidacionOperacion; 
		public String				FechaVencimientoOperacion; 
		public String				IdMonedaOperacion; 
		public BigDecimal			MontoOperacion; 
		public BigDecimal			TipoCambioOperacion; 
		public Long					IdContraparte; 
		public String				IdTipoComprobante; 
		public Long					IdComprobante; 
		public String				FolioComprobante; 
		public Long					IdComprobanteDetalle; 
		public Long					IdComprobanteFormaDePago; 
		public String				IdTipoMovimientoAsociado; 
		public Long					IdMovimientoAsociado; 
		public String				FechaComprobante; 
		public Long					IdPersonaPagoCobro; 
		public Long					IdInstrumento; 
		public String				IdTipoInstrumento;
		public BigDecimal			Cantidad; 
		public String				Nemo; 
		public String				EsIndicadorBilateral; 
		public String				EsCargoAbonoPorTraspaso; 
		public Long					IdPortafolioTraspaso; 
		public String				GeneroContraparteEnTesoreria; 
		public String				CertificadoAutomatico;
		
		public String toString() {
			return Utils.printJson(this, false);
		};

		public Object clone(){ Object obj=null; try{ obj=super.clone(); }catch(CloneNotSupportedException ex){ System.out.println(" no se puede duplicar"); } return obj; }
		
	};

	// legacy cl/bcs/cuentaInversion/CrearCargoAbono.java:273-281 (verbatim)
	public static class ObjCargoAbonoGenerado {
		public Long			NumeroCargoAbono; 
		public Long			IdCargoAbonoMovimiento; 
		public String		IdTipoMovimiento; 		
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	};

	// legacy cl/bcs/cuentaInversion/CrearCargoAbono.java:314-331 (verbatim)
	public static class CrearCargoAbonoEntrada{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_simularOperacion;
		public String						p_UsaReglasNegocio;
		public String						p_FechaSistema;
		public String						p_EsIngresoManual	= "N";
		public List<ObjCargoAbonoAGenerar>	p_CargoAbonoAGenerar;
		
		public CrearCargoAbonoEntrada (){			
			p_CargoAbonoAGenerar	= new ArrayList<ObjCargoAbonoAGenerar>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};

	// legacy cl/bcs/cuentaInversion/CrearCargoAbono.java:333-346 (verbatim)
	public static class CrearCargoAbonoSalida{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<ObjCargoAbonoGenerado>	P_OUT_CARGOABONOGENERADO;

		public CrearCargoAbonoSalida (){
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			P_OUT_CARGOABONOGENERADO	= new ArrayList<ObjCargoAbonoGenerado>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }


	/**
	 * FAKE of CrearCargoAbono.logica (legacy writes the investment-account movements): records its input and answers
	 * "colaboradores.crearCargoAbono": {"mensajes":[{id,tipo,codigo,descripcion}], "idCargoAbonoMovimientoBase": n (every requested NumeroCargoAbono is answered with
	 * base + index), "sinGenerados": true, "errores": n (default: number of ERROR messages), "lanza": "text"}.
	 */
	public CrearCargoAbonoSalida logica(Connection con, CrearCargoAbonoEntrada in) throws Exception {
		Hooks.registrar("CrearCargoAbono", in);
		JsonObject script = Hooks.objeto("crearCargoAbono");
		if (script.has("lanza")) throw new Exception(script.get("lanza").getAsString());
		CrearCargoAbonoSalida out = new CrearCargoAbonoSalida();
		if (script.has("mensajes")) {
			for (com.google.gson.JsonElement e : script.getAsJsonArray("mensajes")) out.P_OUT_MENSAJES.add(Hooks.mensaje(e.getAsJsonObject()));
		}
		long base = script.has("idCargoAbonoMovimientoBase") ? script.get("idCargoAbonoMovimientoBase").getAsLong() : 6000L;
		if (!script.has("sinGenerados")) {
			long i = 0;
			for (ObjCargoAbonoAGenerar g : in.p_CargoAbonoAGenerar) {
				ObjCargoAbonoGenerado o = new ObjCargoAbonoGenerado();
				o.NumeroCargoAbono = g.NumeroCargoAbono;
				o.IdCargoAbonoMovimiento = base + (i++);
				o.IdTipoMovimiento = g.IdTipoMovimiento;
				out.P_OUT_CARGOABONOGENERADO.add(o);
			}
		}
		out.P_OUT_ERRORESCONTADOR = script.has("errores") ? script.get("errores").getAsLong() : MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		return out;
	}

}
