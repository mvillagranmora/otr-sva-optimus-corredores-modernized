
 SELECT
 TRA."FechaOperacion" AS "FechaTransaccion"
,TRA."Folio" AS "FolioTransaccion"
,TRA."IdBolsa"
,TRA."IdBoveda"
,INS."IdMoneda" AS "IdMonedaInstrumento"
,INS."IdMoneda" AS "IdMonedaLiquidacionOrden"
,TRA."IdTransaccion"AS "IdMovimiento"
,TRA."IdTipoInstrumento"
,TRA."IdTransaccion"
,TRA."Nemotecnico" AS "Instrumento"
,TRA."IdTipoOperacion"
,PRO."IdTipoProducto"
,ORD."IdFormaCustodia"
FROM "BSCL.Orden.BD::ORD.Transaccion" AS TRA
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"  AS INS ON INS."IdInstrumento" = TRA."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" AS PRO ON PRO."IdTipoOperacion" = TRA."IdTipoOperacion"
LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" AS ASI ON  ASI."IdTransaccion"=TRA."IdTransaccion"
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS ORD ON ORD."IdOrden"= ASI."IdOrden"
WHERE TRA."IdTransaccion" IN (:IdTransacciones);