package cl.bcs.normativa.cartola;

import java.sql.Connection;

import cl.bcs.oracle.Colaboradores;

/**
 * ORACLE SHIM of legacy cl.bcs.normativa.cartola.MovimientosCaja (BcsNormativaServicios; it reads custody / cash movement tables and many
 * platform views, outside this suite). procesar() records the request under llamadasColaboradores and answers with the list configured in
 * case.json colaboradores.movimientosCaja: a JSON array of flat objects whose numbers become BigDecimal and whose texts stay Strings (the legacy
 * casts the values: MontoCompraMonedaInstrumento, Abono, ... are BigDecimal; FechaOperacion is a "dd-MM-yyyy" text). cash movements (P_OUT_SALIDA["movimientosCaja"]).
 * Absent configuration = an empty list.
 */
public class MovimientosCaja implements BcsCartola {

	@Override
	public SalidaCartola procesar(Connection con, EntradaCartola entrada) throws Exception {
		Colaboradores.registrar("MovimientosCaja", entrada);
		SalidaCartola s = new SalidaCartola();
		s.P_OUT_SALIDA.put("movimientosCaja", Colaboradores.listaMapas("movimientosCaja"));
		return s;
	}
}
