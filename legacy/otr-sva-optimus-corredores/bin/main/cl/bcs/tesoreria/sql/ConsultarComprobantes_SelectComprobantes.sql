SELECT 
	 "IdComprobante"
	,"CuentaDCVOrigen"
	,"CuentaDCVDestino"
	,"IdMoneda"
	,"Monto"
	,"FechaLiquidacion"
	,"NombrePersona"
	,"DocumentoIdentidad"
	,"NombreConcepto"
	,"NombreEstado"
	,"Nemotecnico"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante"
WHERE "IdComprobante" IN (:IdsComprobantes);