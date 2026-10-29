
WITH ComprobantesConCobrosDiferentes as 
(
	SELECT Comprobante."IdComprobante"
	FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Comprobante
	INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteCobro" AS CmbteCobro ON (CmbteCobro."IdComprobante" = Comprobante."IdComprobante")
	LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS Cobro ON (Cobro."IdCobro" = CmbteCobro."IdCobro") 
	
	-- falta activar variables
	/*
	WHERE Comprobante."IdEmpresa"     = 1 --p_IdEmpresa
	AND   Comprobante."FechaComprobante" BETWEEN '2018-09-28' AND '2018-09-28'  --AND   Comprobante."FechaComprobante" BETWEEN :p_FechaDesde AND :p_FechaHasta
	AND  (Comprobante."IdCliente"     = 249199) --AND  (Comprobante."IdCliente"     = :p_IdCliente    OR :p_IdCliente IS NULL)
	AND  (comprobante."IdPortafolio"  = 153970) -- AND  (comprobante."IdPortafolio"  = :p_IdPortafolio OR :p_IdPortafolio IS NULL)
	AND   CmbteCobro."IdEstado"   = 'VIG'
	AND   CmbteCobro."Porcentaje" <> Cobro."ValorCobro" -- falta activar
	*/
	
	
	WHERE Comprobante."IdEmpresa"     = :IdEmpresa
	AND   Comprobante."FechaComprobante" BETWEEN :FechaDesde AND :FechaHasta
	AND  (Comprobante."IdCliente"     = :IdCliente    OR :IdCliente IS NULL)
	AND  (comprobante."IdPortafolio"  = :IdPortafolio OR :IdPortafolio IS NULL)
	AND   CmbteCobro."IdEstado"       = 'VIG'
	--AND   CmbteCobro."Porcentaje" <> Cobro."ValorCobro" -- ****** FALTA activar
	
	GROUP BY Comprobante."IdComprobante"
)


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
	,COM."IdMoneda"
	,COM."FechaComprobante"
	,COM."FechaLiquidacion"
	,COM."Monto"
	,COM."MontoCobro"

FROM ComprobantesConCobrosDiferentes AS COMDIF
INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" AS COM ON (COM."IdComprobante" = COMDIF."IdComprobante");
