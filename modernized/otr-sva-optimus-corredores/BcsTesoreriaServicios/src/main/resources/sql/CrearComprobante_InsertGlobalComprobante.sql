-- Inserta en global datos para comprobante
INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante"
(
	"NumeroReferencia","IdComprobante","TipoComprobante","IdTipoMovimiento","IdConcepto","IdCaja","IdMoneda","Monto","FechaLiquidacion",
	"IdTipoPersona","IdPersona","NombrePersona","IdTipoDocumentoIdentidad","DocumentoIdentidad","IdTipoPersonaTitularPago",
	"IdPersonaTitularPago","NombrePersonaTitularPago","IdPortafolioTitularPago","TitularPago","SecuenciaAgrupacion","EsBilateral",
	"ComprobanteAgrupado","ComprobanteNeteado","SeparaPorConcepto","GeneradoDesdeMovimientos","MueveCuentaInversion","IdCondicionDeLiquidacion", "Glosa"
)
SELECT  MI."NumeroReferencia",MI."IdComprobante",MI."TipoComprobante",
		CASE WHEN MI."IdTipoMovimiento" = '' THEN CASE WHEN SUM(MI."Monto") > 0 THEN 'MOVING' ELSE 'MOVEGR' END ELSE MI."IdTipoMovimiento" END as "IdTipoMovimiento",
		CASE WHEN LENGTH(MI."TipoComprobante") > 0 THEN MI."TipoComprobante" 
			 WHEN LENGTH(MI."TipoComprobante") = 0 AND COUNT(Distinct MI."IdConcepto") > 1 THEN 'MULCPT' 
			 ELSE MAX(MI."IdConcepto") 
		 END as "IdConcepto",
        MI."IdCaja",  
        MI."IdMoneda",
        CAST(ABS(SUM(MI."Monto")) AS DECIMAL(26,8)) as "Monto",
        MI."FechaLiquidacion",
        MI."IdTipoPersona",
        MI."IdPersona",
        MI."NombrePersona",
        CAST(MI."IdTipoDocumentoIdentidad"  as NVARCHAR(1)) as "IdTipoDocumentoIdentidad",
        MI."DocumentoIdentidad",
        MI."IdTipoPersonaTitularPago",
        MI."IdPersonaTitularPago",
        MI."NombrePersonaTitularPago",
        MI."IdPortafolioTitularPago",		            
        CASE WHEN LENGTH(MI."TipoComprobante") = 0 THEN 'S' ELSE MI."TitularPago" END AS "TitularPago",
        MI."SecuenciaAgrupacion",
        MI."EsBilateral",
        MI."ComprobanteAgrupado",
		MI."ComprobanteNeteado",
		CASE WHEN COUNT(Distinct MI."IdConcepto") > 1 THEN 'N' ELSE 'S' END as "SeparaPorConcepto",
		'S' AS "GeneradoDesdeMovimientos",
		MI."MueveCuentaInversion",
		MI."IdCondicionDeLiquidacion",
		CASE 
			WHEN COUNT(DISTINCT NULLIF(MI."Glosa", '')) = 1 THEN MAX(NULLIF(MI."Glosa", ''))    
			WHEN COUNT(DISTINCT NULLIF(MI."Glosa", '')) > 1 THEN ''   WHEN COUNT(MI."Glosa") = 1 THEN MAX(MI."Glosa")
			ELSE ''
		END AS "Glosa"
 FROM "BSCL.Tesoreria.BD::GBL.MovimientoDetalleGenerarComprobante" as MI
GROUP BY MI."NumeroReferencia",MI."IdComprobante",MI."TipoComprobante",MI."IdPersona",MI."NombrePersona",MI."IdTipoPersona",
		 MI."IdTipoDocumentoIdentidad",MI."DocumentoIdentidad",MI."TitularPago",MI."IdTipoPersonaTitularPago",MI."IdPersonaTitularPago",
		 MI."NombrePersonaTitularPago",MI."IdPortafolioTitularPago",MI."IdMoneda",MI."IdCaja",MI."IdTipoMovimiento",
		 MI."FechaLiquidacion", MI."SecuenciaAgrupacion",MI."EsBilateral",MI."ComprobanteAgrupado",MI."ComprobanteNeteado",MI."MueveCuentaInversion",MI."IdCondicionDeLiquidacion";

							