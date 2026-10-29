SELECT
	 "IdComprobante"
	,"IdComprobanteFormaDePago" 
	,"FechaLiquidacion"
	,"NombreConcepto"     
	,"NombreMedioDePago"     
	,"NombreTipoMovimiento"
	,"IdMoneda"
	,"Monto"
	,"IdEstado"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago"
WHERE "FechaLiquidacion" = :FechaLiquidacion