SELECT 
		TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
		"IdComprobante"										AS "IdMovimiento",
		concat('L',"IdTipoComprobante")						AS "IdTipoMovimiento",
		"IdEmpresa"         								AS "IdEmpresa",									    
		"IdComprobante"  									AS "Numero", --"FolioComprobante"
		TO_BIGINT(0)          								AS "Linea",
		YEAR("FechaLiquidacion") 		||'-'|| RIGHT('0' || 
		MONTH("FechaLiquidacion"),2) 	||'-'|| RIGHT('0' || 
		DAYOFMONTH("FechaLiquidacion"),2) 					AS "Fecha",
		"DocumentoIdentidad"								AS "DocumentoIdentidad",  
		"IdPortafolio"										AS "IdPortafolio",
		TO_BIGINT(YEAR("FechaLiquidacion"))					AS "Ejercicio",
		RIGHT('0' || 
		MONTH("FechaLiquidacion"),2) ||'-'|| 
		YEAR("FechaLiquidacion") 							AS "Periodo",
		"IdMonedaComprobante"								AS "IdMoneda",
		"MontoComprobante"									AS "Monto",
		"IdEstadoComprobante"								AS "Estado"  
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"
WHERE 	"FechaLiquidacion" 		= :FechaContable 		AND
		"IdTipoComprobante"		NOT IN ('FOC', 'BOE', 'FOE' )	AND 
		"IdEstadoComprobante" 	<> 'ANU'				AND
		"EsVentaCorta" = 'N';
	
	
		