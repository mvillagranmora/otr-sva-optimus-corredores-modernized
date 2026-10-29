
-- Obtiene información de los comprobantes a analizar diferencias en cobros

SELECT		
	 COM."IdComprobante" 
	,COM."IdTipoComprobante"	
	,COM."IdConcepto" 
	,COM."NombreConcepto"	
	,COM."IdPortafolio" 
	,COM."NombrePortafolio"	
	,COM."IdCliente"
	,COM."NombreLargoCliente"
	,COM."DocumentoIdentidad"
	,COM."IdTipoOperacion" 
	,COM."NombreTipoOperacion"
	,COM."IdTipoProducto" 
	,COM."NombreTipoProducto" 
	,COM."IdMoneda"                -- Moneda del nemo   
	,COM."IdMonedaComprobante"     -- Moneda local
	,COM."FechaComprobante"
	,COM."FechaLiquidacion"
	,COM."Monto"
	,COM."MontoCobro"
	,COM."IdBolsa"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"AS COM
WHERE COM."IdEmpresa"     = :IdEmpresa
AND   COM."FechaComprobante" BETWEEN :FechaDesde AND :FechaHasta
AND  (COM."IdCliente"     = :IdCliente    OR :IdCliente IS NULL)
AND  (COM."IdPortafolio"  = :IdPortafolio OR :IdPortafolio IS NULL)
AND   COM."MontoCobro" > 0
AND   COM."IdEstadoComprobante" = 'ENV';  -- Enviado a generar el DTE

