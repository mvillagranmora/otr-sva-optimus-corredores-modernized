 WITH NCES as (
SELECT "IdComprobanteReferencia" FROM "BSCL.Facturacion.BD::FAC.Comprobante"
WHERE	"IdEmpresa"					= 	:IdEmpresa
AND 	"FechaComprobante"  		=  	:FechaIng
AND 	"IdComprobanteReferencia" 	> 	0
)


SELECT 
	COM."IdComprobante"												AS "NumeroOperacion"
	,'00'															AS "LinDet"
	,CASE WHEN COM."IdTipoOperacion" = 'VENACN' THEN 'FV  '
			WHEN COM."IdTipoOperacion" = 'COMACN' THEN 'FC  '
			ELSE '    ' END  										AS "Producto"  
	,CASE WHEN COM."IdClasificacionTipoInstrumento" = 'SP'
	THEN 'MO'	ELSE COM."IdClasificacionTipoInstrumento" 	END 	AS "Mercado"
	,TO_VARCHAR(COM."FechaComprobante", 'YYYYMMDD') 				AS "FechaInicio"
	,TO_VARCHAR(COM."FechaComprobante",'YYYYMMDD') 					AS "FechaVencimiento"
	,TO_BIGINT(1) 											AS "Plazo"
	,LPAD(CONCAT(SUBSTRING(COM."DocumentoIdentidad",1,LENGTH(COM."DocumentoIdentidad")-2),SUBSTRING(COM."DocumentoIdentidad",LENGTH(COM."DocumentoIdentidad"),1)),10,'0')								AS "RutCliente"
	,'0000000000'													AS "RutEmisor"
	,'999'				 											AS "MonedaEmisor"
	,'000' 															AS "MonedaPacto"
	,0.0 															as "Tasatransferencia"
	,0.0															AS "TasaTransaccion"
	,0.0 															AS "TasaOperacion"
	,'000'															AS "BASTAS"
	,'001' 															AS "Sucursal"
	,0.0															AS "MontoCompraVenta"
	,0.0					 										AS "ValorPresente"
	,0.0	 														AS "Interes"
	,0.0 															AS "Reajuste"
	,0.0 															AS "MontoTransaccion"
	,tblCobrosLiq."TotalComisLiq" + tblCobrosLiq."TotalDereLiq"	+  tblCobrosLiq."TotalGastoLiq"	AS "Comision"
	,0.0	 														AS "UtilidaPerdidaDiferenciaPrecio"
	,0.0		 													AS "PrecioCompraVenta"
	,(DET."Tasa" * DET."Cantidad" )									AS "UtilidadPerdidaOperacionCambio"
	,0.0 															AS "UtilidadPerdidaSimultanea"
	
	FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS COM
	INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS DET ON COM."IdComprobante"= DET."IdComprobante"
  LEFT OUTER JOIN (
	   SELECT  "IdComprobante",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "MontoCobro" ELSE 0 END) AS "TotalComisLiq",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "MontoCobro" ELSE 0 END) AS "TotalDereLiq",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "MontoCobro" ELSE 0 END) AS "TotalGastoLiq",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "MontoCobro" ELSE 0 END) AS "TotalImpto",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' THEN "MontoCobro" ELSE 0 END) AS "TotalReten",
                MAX("IdMonedaCobro") AS "IdMonedaCobro"
         FROM (
				SELECT  CmbteCobro."IdComprobante",
						Cobro."IdCobroClasificacion", 
						CobroClasificacion."CodigoCobroClasificacion",
						IFNULL(SUM(CmbteCobro."MontoCobro"),0) as "MontoCobro",
						CmbteCobro."IdMonedaCobro"
				  FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" CmbteCobro
				  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" As Cobro On Cobro."IdCobro" = CmbteCobro."IdCobro"
				  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion On CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion"
				GROUP BY CmbteCobro."IdComprobante", Cobro."IdCobroClasificacion",CobroClasificacion."CodigoCobroClasificacion", CmbteCobro."IdMonedaCobro"
				ORDER BY Cobro."IdCobroClasificacion"
         )
         GROUP BY "IdComprobante"
   	) As tblCobrosLiq on tblCobrosLiq."IdComprobante" = COM."IdComprobante"
	WHERE COM."IdEmpresa"= :IdEmpresa
	AND (COM."IdTipoComprobante" 	IN ('FAC','FAE') AND COM."IdClasificacionTipoInstrumento" ='RV')
	AND COM."IdTipoInstrumento" 	<> 'SIM'
	AND DET."IdTipoOperacion" 		IN ('VENACN','COMACN')
	AND COM."IdEstadoComprobante" 	IN ('FAC','ENV')
	AND COM."FechaComprobante" 		= :FechaIng
	AND COM."IdPortafolio"			NOT IN (:CtaPropia)
	AND COM."IdComprobanteReferencia" NOT IN (SELECT "IdComprobanteReferencia" FROM NCES)
UNION
SELECT 
	COM."IdComprobante"												AS "NumeroOperacion"
	,'00'															AS "LinDet"
	,CASE WHEN COM."IdTipoOperacion" = 'VENRF' THEN 'FV  '
			WHEN COM."IdTipoOperacion" = 'COMRF' THEN 'FC  '
			ELSE '    ' END  										AS "Producto"  
	,CASE WHEN COM."IdClasificacionTipoInstrumento" = 'SP'
	THEN 'MO'	ELSE COM."IdClasificacionTipoInstrumento" 	END 	AS "Mercado"
	,TO_VARCHAR(COM."FechaComprobante", 'YYYYMMDD') 				AS "FechaInicio"
	,TO_VARCHAR(COM."FechaComprobante",'YYYYMMDD') 					AS "FechaVencimiento"
	,TO_BIGINT(1) 											AS "Plazo"
	,LPAD(CONCAT(SUBSTRING(COM."DocumentoIdentidad",1,LENGTH(COM."DocumentoIdentidad")-2),SUBSTRING(COM."DocumentoIdentidad",LENGTH(COM."DocumentoIdentidad"),1)),10,'0')								AS "RutCliente"
	,'0000000000'													AS "RutEmisor"
	,'999'				 											AS "MonedaEmisor"
	,'000' 															AS "MonedaPacto"
	,0.0 															as "Tasatransferencia"
	,0.0															AS "TasaTransaccion"
	,0.0 															AS "TasaOperacion"
	,'000'															AS "BASTAS"
	,'001' 															AS "Sucursal"
	,0.0															AS "MontoCompraVenta"
	,0.0					 										AS "ValorPresente"
	,0.0	 														AS "Interes"
	,0.0 															AS "Reajuste"
	,COM."MontoComprobante" 										AS "MontoTransaccion"
	,tblCobrosLiq."TotalComisLiq" + tblCobrosLiq."TotalDereLiq"	+  tblCobrosLiq."TotalGastoLiq"	AS "Comision"
	,0.0	 														AS "UtilidaPerdidaDiferenciaPrecio"
	,0.0		 													AS "PrecioCompraVenta"
	,0.0 															AS "UtilidadPerdidaOperacionCambio"
	,0.0 															AS "UtilidadPerdidaSimultanea"
	FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS COM
	INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS DET ON COM."IdComprobante"= DET."IdComprobante"
  LEFT OUTER JOIN (
	   SELECT  "IdComprobante",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "MontoCobro" ELSE 0 END) AS "TotalComisLiq",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "MontoCobro" ELSE 0 END) AS "TotalDereLiq",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "MontoCobro" ELSE 0 END) AS "TotalGastoLiq",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "MontoCobro" ELSE 0 END) AS "TotalImpto",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' THEN "MontoCobro" ELSE 0 END) AS "TotalReten",
                MAX("IdMonedaCobro") AS "IdMonedaCobro"
         FROM (
				SELECT  CmbteCobro."IdComprobante",
						Cobro."IdCobroClasificacion", 
						CobroClasificacion."CodigoCobroClasificacion",
						IFNULL(SUM(CmbteCobro."MontoCobro"),0) as "MontoCobro",
						CmbteCobro."IdMonedaCobro"
				  FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" CmbteCobro
				  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" As Cobro On Cobro."IdCobro" = CmbteCobro."IdCobro"
				  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion On CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion"
				GROUP BY CmbteCobro."IdComprobante", Cobro."IdCobroClasificacion",CobroClasificacion."CodigoCobroClasificacion", CmbteCobro."IdMonedaCobro"
				ORDER BY Cobro."IdCobroClasificacion"
         )
         GROUP BY "IdComprobante"
   	) As tblCobrosLiq on tblCobrosLiq."IdComprobante" = COM."IdComprobante"
	WHERE COM."IdEmpresa"= :IdEmpresa
	AND (COM."IdTipoComprobante" 	IN ('FAC','FAE') AND COM."IdClasificacionTipoInstrumento" = 'RF')
	AND COM."IdTipoInstrumento"		<> 'SIM'
	AND DET."IdTipoOperacion" 		IN ('VENRF','COMRF')
	AND COM."IdEstadoComprobante" 	IN ('FAC','ENV')
	AND COM."FechaComprobante" 		= :FechaIng
	AND COM."IdPortafolio"			NOT IN (:CtaPropia)
	AND COM."IdComprobanteReferencia" NOT IN (SELECT "IdComprobanteReferencia" FROM NCES)
UNION
SELECT 
	COM."IdComprobante"												AS "NumeroOperacion"
	,'00'															AS "LinDet"
	,CASE WHEN COM."IdTipoOperacion" = 'VENIIF' THEN 'FV  '
			WHEN COM."IdTipoOperacion" = 'COMIIF' THEN 'FC  '
			ELSE '    ' END  										AS "Producto"  
	,CASE WHEN COM."IdClasificacionTipoInstrumento" = 'SP'
	THEN 'MO'	ELSE COM."IdClasificacionTipoInstrumento" 	END 	AS "Mercado"
	,TO_VARCHAR(COM."FechaComprobante", 'YYYYMMDD') 				AS "FechaInicio"
	,TO_VARCHAR(COM."FechaComprobante",'YYYYMMDD') 					AS "FechaVencimiento"
	,TO_BIGINT(1)											AS "Plazo"
	,LPAD(CONCAT(SUBSTRING(COM."DocumentoIdentidad",1,LENGTH(COM."DocumentoIdentidad")-2),SUBSTRING(COM."DocumentoIdentidad",LENGTH(COM."DocumentoIdentidad"),1)),10,'0')								AS "RutCliente"
	,'0000000000'													AS "RutEmisor"
	,'999'				 											AS "MonedaEmisor"
	,'000' 															AS "MonedaPacto"
	,0.0 															as "Tasatransferencia"
	,0.0															AS "TasaTransaccion"
	,0.0 															AS "TasaOperacion"
	,'000'															AS "BASTAS"
	,'001' 															AS "Sucursal"
	,0.0															AS "MontoCompraVenta"
	,0.0					 										AS "ValorPresente"
	,0.0	 														AS "Interes"
	,0.0 															AS "Reajuste"
	,COM."MontoComprobante" 										AS "MontoTransaccion"
	,tblCobrosLiq."TotalComisLiq" + tblCobrosLiq."TotalDereLiq"	+  tblCobrosLiq."TotalGastoLiq"	AS "Comision"
	,0.0	 														AS "UtilidaPerdidaDiferenciaPrecio"
	,0.0		 													AS "PrecioCompraVenta"
	,0.0 															AS "UtilidadPerdidaOperacionCambio"
	,0.0 															AS "UtilidadPerdidaSimultanea"
	FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS COM
	INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS DET ON COM."IdComprobante"= DET."IdComprobante"
  LEFT OUTER JOIN (
	   SELECT  "IdComprobante",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "MontoCobro" ELSE 0 END) AS "TotalComisLiq",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "MontoCobro" ELSE 0 END) AS "TotalDereLiq",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "MontoCobro" ELSE 0 END) AS "TotalGastoLiq",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "MontoCobro" ELSE 0 END) AS "TotalImpto",
                MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' THEN "MontoCobro" ELSE 0 END) AS "TotalReten",
                MAX("IdMonedaCobro") AS "IdMonedaCobro"
         FROM (
				SELECT  CmbteCobro."IdComprobante",
						Cobro."IdCobroClasificacion", 
						CobroClasificacion."CodigoCobroClasificacion",
						IFNULL(SUM(CmbteCobro."MontoCobro"),0) as "MontoCobro",
						CmbteCobro."IdMonedaCobro"
				  FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" CmbteCobro
				  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" As Cobro On Cobro."IdCobro" = CmbteCobro."IdCobro"
				  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion On CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion"
				GROUP BY CmbteCobro."IdComprobante", Cobro."IdCobroClasificacion",CobroClasificacion."CodigoCobroClasificacion", CmbteCobro."IdMonedaCobro"
				ORDER BY Cobro."IdCobroClasificacion"
         )
         GROUP BY "IdComprobante"
   	) As tblCobrosLiq on tblCobrosLiq."IdComprobante" = COM."IdComprobante"
	WHERE COM."IdEmpresa"= :IdEmpresa
	AND (COM."IdTipoComprobante" 	IN ('FAC','FAE') AND COM."IdClasificacionTipoInstrumento" = 'IF')
	AND COM."IdTipoInstrumento" 	<> 'SIM'
	AND DET."IdTipoOperacion" 		IN ('VENIIF','COMIIF')
	AND COM."IdEstadoComprobante" 	IN ('FAC','ENV')
	AND COM."FechaComprobante" 		= :FechaIng	
	AND COM."IdPortafolio"			NOT IN (:CtaPropia)
	AND COM."IdComprobanteReferencia" NOT IN (SELECT "IdComprobanteReferencia" FROM NCES)
UNION
	SELECT 
	COM."IdComprobante"												AS "NumeroOperacion"
	,'00'															AS "LinDet"
	,	CASE WHEN CONCEPTO."Nombre" like 'ADMINISTRACION CARTERA%' THEN 'FCAD'
		WHEN CONCEPTO."Nombre" like 'ASESORIAS FINANCIERAS%' THEN	'FCAS'
		ELSE 'FCOP'		END  										AS "Producto"  
	,'  '															AS "Mercado"
	,TO_VARCHAR(COM."FechaComprobante", 'YYYYMMDD') 				AS "FechaInicio"
	,TO_VARCHAR(COM."FechaComprobante",'YYYYMMDD') 					AS "FechaVencimiento"
	,TO_BIGINT(1) 																AS "Plazo"
	,LPAD(CONCAT(SUBSTRING(COM."DocumentoIdentidad",1,LENGTH(COM."DocumentoIdentidad")-2),SUBSTRING(COM."DocumentoIdentidad",LENGTH(COM."DocumentoIdentidad"),1)),10,'0')								AS "RutCliente"
	,'0000000000'													AS "RutEmisor"
	,'999'				 											AS "MonedaEmisor"
	,'000' 															AS "MonedaPacto"
	,0.0 															as "Tasatransferencia"
	,0.0															AS "TasaTransaccion"
	,0.0 															AS "TasaOperacion"
	,'000'															AS "BASTAS"
	,'001' 															AS "Sucursal"
	,0.0															AS "MontoCompraVenta"
	,0.0					 										AS "ValorPresente"
	,0.0	 														AS "Interes"
	,0.0 															AS "Reajuste"
	,0.0 															AS "MontoTransaccion"
	,COM."MontoLiquidacion"											AS "Comision"
	,0.0	 														AS "UtilidaPerdidaDiferenciaPrecio"
	,0.0		 													AS "PrecioCompraVenta"
	,0.0 															AS "UtilidadPerdidaOperacionCambio"
	,0.0 															AS "UtilidadPerdidaSimultanea"
	FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS COM
	INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS DET ON COM."IdComprobante"= DET."IdComprobante"
	INNER JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS CONC ON CONC."IdConcepto" = DET."IdConcepto"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" CONCEPTO ON COM."IdConcepto"  = CONCEPTO."IdConcepto" 
	WHERE COM."IdEmpresa"= :IdEmpresa
	AND COM."IdTipoComprobante" 	IN ('FOC','FOE')
	AND COM."IdEstadoComprobante" 	IN ('FAC','ENV')
	AND CONC."EsOtrosConceptos" 	= 'S'
	AND COM."FechaComprobante" 		= :FechaIng
	AND COM."IdPortafolio"			NOT IN (:CtaPropia )
	AND COM."IdComprobanteReferencia" NOT IN (SELECT "IdComprobanteReferencia" FROM NCES)