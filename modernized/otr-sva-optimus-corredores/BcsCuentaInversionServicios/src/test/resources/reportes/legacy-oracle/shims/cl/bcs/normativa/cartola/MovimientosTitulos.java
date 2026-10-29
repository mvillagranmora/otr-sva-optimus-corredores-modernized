package cl.bcs.normativa.cartola;

import java.sql.Connection;

import cl.bcs.oracle.Colaboradores;

/**
 * ORACLE SHIM of legacy cl.bcs.normativa.cartola.MovimientosTitulos (BcsNormativaServicios; it reads custody / cash movement tables and many
 * platform views, outside this suite). procesar() records the request under llamadasColaboradores and answers with the list configured in
 * case.json colaboradores.movimientosTitulos: a JSON array of flat objects whose numbers become BigDecimal and whose texts stay Strings (the legacy
 * casts the values: MontoCompraMonedaInstrumento, Abono, ... are BigDecimal; FechaOperacion is a "dd-MM-yyyy" text). securities movements (P_OUT_SALIDA["movimientosTitulos"]).
 * Absent configuration = an empty list.
 */
public class MovimientosTitulos implements BcsCartola {

	@Override
	public SalidaCartola procesar(Connection con, EntradaCartola entrada) throws Exception {
		Colaboradores.registrar("MovimientosTitulos", entrada);
		SalidaCartola s = new SalidaCartola();
		s.P_OUT_SALIDA.put("movimientosTitulos", Colaboradores.listaMapas("movimientosTitulos"));
		return s;
	}
}
