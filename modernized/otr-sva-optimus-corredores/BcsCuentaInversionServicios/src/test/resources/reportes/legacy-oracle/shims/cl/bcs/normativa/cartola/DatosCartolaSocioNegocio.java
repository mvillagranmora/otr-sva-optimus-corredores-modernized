package cl.bcs.normativa.cartola;

import java.sql.Connection;

import cl.bcs.oracle.Colaboradores;

/**
 * ORACLE SHIM of legacy cl.bcs.normativa.cartola.DatosCartolaSocioNegocio (BcsNormativaServicios; it reads custody / cash movement tables and many
 * platform views, outside this suite). procesar() records the request under llamadasColaboradores and answers with the list configured in
 * case.json colaboradores.datosSocioNegocio: a JSON array of flat objects whose numbers become BigDecimal and whose texts stay Strings (the legacy
 * casts the values: MontoCompraMonedaInstrumento, Abono, ... are BigDecimal; FechaOperacion is a "dd-MM-yyyy" text). business partner data (P_OUT_SALIDA["datosSocioNegocio"]).
 * Absent configuration = an empty list.
 */
public class DatosCartolaSocioNegocio implements BcsCartola {

	@Override
	public SalidaCartola procesar(Connection con, EntradaCartola entrada) throws Exception {
		Colaboradores.registrar("DatosCartolaSocioNegocio", entrada);
		SalidaCartola s = new SalidaCartola();
		s.P_OUT_SALIDA.put("datosSocioNegocio", Colaboradores.listaMapas("datosSocioNegocio"));
		return s;
	}
}
