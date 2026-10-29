/* 

Consultar asignaciones para anular

*/

SELECT "IdAsignacion","IdTransaccion","IdOrden","Instrumento" AS "Nemotecnico","NombreLibroOrdenes","IdBolsa",
		"IdInstrumento","IdClasificacionTipoInstrumento","IdLibroOrdenes","IdTipoInstrumento","AccionOrden","FechaAsignacion",
		"IdTipoCanalIngreso","NombreTipoCanalIngreso", "IdAsignacionMasiva", "IdCliente", "NombreCompleto", "DocumentoIdentidad", 
		"IdPortafolio", "IdEstadoAsignacion", "IdTipoOperacion", "FolioTransaccion", "CantidadAsignada", "PrecioAsignacion" AS "Precio", "MontoAsignado",
		"IdMonedaOperacion", "IdMonedaLiquidacionOrden", "ValorTipoCambio", "CodigoPortafolio",
		"IdCondicionDeLiquidacion", "MarcaAsignacion"
FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion"
WHERE "FechaAsignacion" >= :FechaDesde AND "FechaAsignacion" <= :FechaHasta
AND "IdEstadoAsignacion" = 'NEF' AND "IdTipoCanalDistribucion" = 'MAN';