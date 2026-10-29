SELECT
	 TRA."FechaOperacion" AS "FechaTransaccion"
	,TRA."Folio" AS "FolioTransaccion"
	,TRA."IdBolsa"	
	,TRA."IdBoveda"	
	,INS."IdMoneda" AS "IdMonedaInstrumento"
	,TRA."IdMoneda" AS "IdMonedaLiquidacionOrden"		
	,TRA."IdTransaccion"AS "IdMovimiento"
	,TRA."IdTipoInstrumento"
	,TRA."IdTransaccion"
	,TRA."Nemotecnico" AS "Instrumento"
	,TRA."IdTipoOperacion"
	,PRO."IdTipoProducto"
	,ORD."IdFormaCustodia"
	,PRO."AccionOrden" AS "AccionOperacion"
	,TRA."FechaSistemaCreacion" AS "FechaCreacionTransaccion"
	,INS2."Nemotecnico" AS "InstrumentoNegociacionTran"

FROM "BSCL.Facturacion.BD::GBL.SimulacionRefacturacion_Transaccion" AS GLB
INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRA ON ( TRA."Folio" = GLB."FolioTransaccion" AND TRA."Nemotecnico" = GLB."Nemotecnico" AND TRA."FechaSistemaCreacion" = GLB."FechaSistemaCreacion")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS2 ON TRA."IdInstrumentoNegociacion" = INS2."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = TRA."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" AS PRO ON PRO."IdTipoOperacion" = TRA."IdTipoOperacion"
LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" AS ASI ON  ASI."IdTransaccion"=TRA."IdTransaccion"
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS ORD ON ORD."IdOrden"= ASI."IdOrden";