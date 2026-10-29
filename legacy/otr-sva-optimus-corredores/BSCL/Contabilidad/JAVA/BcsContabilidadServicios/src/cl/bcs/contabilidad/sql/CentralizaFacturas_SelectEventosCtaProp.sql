WITH COMPROBANTES_DETALLE AS(
	SELECT 
				  DetA."IdComprobanteDetalle",
				  DetA."IdComprobante",
				  DetA."IdMonedaLiquidacion",
				  MovCmbte."IdMovimientoComprobante" ,
				  MovCmbte."FolioCartera" ,
				  MovCmbte."FolioTransaccion" ,
				  MovCmbte."MontoLiquidacion",
				  CASE 
				   WHEN DetA."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM', 'RRV', 'RRF') THEN DetA."NemotecnicoSubyacente"
			WHEN DetA."IdTipoInstrumento" IN ('PAG') THEN DetA."NemoSVS"
			ELSE DetA."NemotecnicoInstrumento"
		END AS "NemotecnicoInstrumento"
	FROM
		"BSCL.Facturacion.BD::FAC.ComprobanteDetalle" DetA
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteDetalle" MovCmbteDet ON
		MovCmbteDet."IdComprobanteDetalle" = DetA."IdComprobanteDetalle"
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MovCmbte ON
		MovCmbte."IdMovimientoComprobante" = MovCmbteDet."IdMovimientoComprobante"
		 WHERE DetA."IdClasificacionTipoInstrumento" IN('RV','RF','IF') AND "FechaSistema" = :FechaContable
),
 
CONSULTA_MONTO_VALOR_OPERACION AS(
	SELECT 
		CmbteDet."IdComprobanteDetalle",
		CmbteDet."MontoComprobanteDetalle",
		CmbteDet."MontoLiquidacion",
		CmbteDet."IdMonedaComprobante",
		CmbteDet."IdMonedaLiquidacion"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" Cmbte
	LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CmbteDet
		ON
		(Cmbte."IdComprobante" = CmbteDet."IdComprobante")
	WHERE
		"FechaSistema" = :FechaContable
		AND "IdEstadoDTE" IN ('PEN', 'ENV', 'REC')
		AND "DocumentoIdentidad" = :DocumentoIdentidad
)


SELECT
	TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion",
	CmbteDet."IdComprobanteDetalle" AS "IdMovimiento",
	'MONTO_OPERACION' AS "IdEvento",
	'MONTO OPERACION' AS "Nombre",
	IFNULL(CmbteDet."MontoComprobanteDetalle", 0) AS "MontoEvento",
	CmbteDet."IdMonedaComprobante" AS "CodigoMoneda"
FROM CONSULTA_MONTO_VALOR_OPERACION  CmbteDet  

UNION

SELECT
	TO_BIGINT(:IdTipoCentralizacion),
	CmbteDet."IdComprobanteDetalle" AS "IdMovimiento",
	'VALOR_OPERACION' AS "IdEvento",
	'VALOR OPERACION' AS "Nombre",
	IFNULL(CmbteDet."MontoLiquidacion", 0) AS "MontoEvento",
	CmbteDet."IdMonedaLiquidacion" AS "CodigoMoneda"
FROM CONSULTA_MONTO_VALOR_OPERACION  CmbteDet  

UNION

SELECT -- top 1 
    TO_BIGINT(:IdTipoCentralizacion), 
    CmbteDet."IdComprobanteDetalle"				       		    AS "IdMovimiento",
    'UTILIDAD' 													AS "IdEvento", 
    'UTILIDAD' 													AS "Nombre", 
    SUM(CASE 
	WHEN Cmbte."IdClasificacionTipoInstrumento" IN ('RF') THEN
			IFNULL((SELECT A1."montoVenta" - A2."MontoCompra"
FROM(
SELECT 
"Monto" AS "montoVenta", CmbteDet."NemotecnicoInstrumento",CmbteDet."IdComprobanteDetalle", CmbteDet."IdMovimientoComprobante"
    FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MovCmbteDet
    INNER JOIN COMPROBANTES_DETALLE CmbteDet ON (CmbteDet."IdMovimientoComprobante" = MovCmbteDet."IdMovimientoComprobante")
    ) A1 LEFT JOIN(( SELECT 
           TBCOMPRA."MontoParcial" AS "MontoCompra", TBCOMPRA."IdComprobanteDetalle",TBCOMPRA."NemotecnicoInstrumento",CmbteDet."IdMovimientoComprobante"
        FROM (
                SELECT SUM((CA."INGMontoATasaCompraActual"*CA."EGRCantidad")/CA."INGCantidad" ) AS "MontoParcial", 
                CA."EGRFolioExternoReferencia", CmbteDet."IdComprobanteDetalle", CmbteDet."NemotecnicoInstrumento"
                FROM "BSCL.Custodia.BD::CUS.Cartera" CA
                LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (CA."EGRFolioComprobante" = CmbteDet."IdComprobante" AND "EGRFolioExternoReferencia" = CmbteDet."FolioTransaccion" )
                WHERE CmbteDet."FolioTransaccion" IS NOT NULL AND CmbteDet."FolioCartera" = 0 
                GROUP BY CA."EGRFolioExternoReferencia", CmbteDet."IdComprobanteDetalle", CmbteDet."NemotecnicoInstrumento"
                ) AS TBCOMPRA
        LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (TBCOMPRA."IdComprobanteDetalle" = CmbteDet."IdComprobanteDetalle" AND TBCOMPRA."EGRFolioExternoReferencia" = CmbteDet."FolioTransaccion")
        WHERE CmbteDet."FolioTransaccion" IS NOT NULL AND CmbteDet."FolioCartera" = 0 
       ))
       A2 ON A1."IdMovimientoComprobante"=A2."IdMovimientoComprobante" 
       WHERE A1."IdMovimientoComprobante"=CmbteDet."IdMovimientoComprobante"
        ),0) 
    END )                                         AS "MontoEvento",  
    CmbteDet."IdMonedaLiquidacion" 								AS  "CodigoMoneda"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 			Cmbte
	  LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (Cmbte."IdComprobante" = CmbteDet."IdComprobante")
	WHERE "FechaSistema" = :FechaContable AND "IdEstadoDTE"  IN ('PEN', 'ENV', 'REC') AND "IdEstadoComprobante" <> 'ANU' 
	  AND "DocumentoIdentidad" = :DocumentoIdentidad AND CmbteDet."IdMovimientoComprobante" IS NOT NULL
	  AND Cmbte."IdClasificacionTipoInstrumento" NOT IN ('RV','IF')
GROUP BY CmbteDet."IdComprobanteDetalle" ,CmbteDet."IdMonedaLiquidacion"
HAVING SUM(CASE 
	WHEN Cmbte."IdClasificacionTipoInstrumento" IN ('RF') THEN
			IFNULL((SELECT A1."montoVenta" - A2."MontoCompra"
FROM(
SELECT 
"Monto" AS "montoVenta", CmbteDet."NemotecnicoInstrumento",CmbteDet."IdComprobanteDetalle", CmbteDet."IdMovimientoComprobante"
    FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MovCmbteDet
    INNER JOIN COMPROBANTES_DETALLE CmbteDet ON (CmbteDet."IdMovimientoComprobante" = MovCmbteDet."IdMovimientoComprobante")
    ) A1 LEFT JOIN(( SELECT 
           TBCOMPRA."MontoParcial" AS "MontoCompra", TBCOMPRA."IdComprobanteDetalle",TBCOMPRA."NemotecnicoInstrumento",CmbteDet."IdMovimientoComprobante"
        FROM (
                SELECT SUM((CA."INGMontoATasaCompraActual"*CA."EGRCantidad")/CA."INGCantidad" ) AS "MontoParcial", 
                CA."EGRFolioExternoReferencia", CmbteDet."IdComprobanteDetalle", CmbteDet."NemotecnicoInstrumento"
                FROM "BSCL.Custodia.BD::CUS.Cartera" CA
                LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (CA."EGRFolioComprobante" = CmbteDet."IdComprobante" AND CA."EGRFolioExternoReferencia" = CmbteDet."FolioTransaccion")
                WHERE CmbteDet."FolioTransaccion" IS NOT NULL AND CmbteDet."FolioCartera" = 0
                GROUP BY CA."EGRFolioExternoReferencia", CmbteDet."IdComprobanteDetalle", CmbteDet."NemotecnicoInstrumento"
                ) AS TBCOMPRA
        LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (TBCOMPRA."IdComprobanteDetalle" = CmbteDet."IdComprobanteDetalle" AND  TBCOMPRA."EGRFolioExternoReferencia" = CmbteDet."FolioTransaccion")
        WHERE CmbteDet."FolioTransaccion" IS NOT NULL AND CmbteDet."FolioCartera" = 0
       ) )
       A2 ON A1."IdMovimientoComprobante"=A2."IdMovimientoComprobante" 
       WHERE A1."IdMovimientoComprobante"=CmbteDet."IdMovimientoComprobante"
        ),0)
    END ) >0
	
UNION
SELECT --top 1 
    TO_BIGINT(:IdTipoCentralizacion), 
    CmbteDet."IdComprobanteDetalle" 							AS "IdMovimiento",
    'PERDIDA' 													AS "IdEvento", 
    'PERDIDA' 													AS "Nombre", 
SUM(CASE
	WHEN Cmbte."IdClasificacionTipoInstrumento" IN ('RF') THEN IFNULL((SELECT A1."montoVenta" - A2."MontoCompra"
FROM(
SELECT 
"Monto" AS "montoVenta", CmbteDet."NemotecnicoInstrumento",CmbteDet."IdComprobanteDetalle", CmbteDet."IdMovimientoComprobante"
    FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MovCmbteDet
    INNER JOIN COMPROBANTES_DETALLE CmbteDet ON (CmbteDet."IdMovimientoComprobante" = MovCmbteDet."IdMovimientoComprobante")
    ) A1 LEFT JOIN(( SELECT 
           TBCOMPRA."MontoParcial" AS "MontoCompra", TBCOMPRA."IdComprobanteDetalle",TBCOMPRA."NemotecnicoInstrumento",CmbteDet."IdMovimientoComprobante"
        FROM (
                SELECT SUM((CA."INGMontoATasaCompraActual"*CA."EGRCantidad")/CA."INGCantidad" ) AS "MontoParcial", 
                CA."EGRFolioExternoReferencia", CmbteDet."IdComprobanteDetalle", CmbteDet."NemotecnicoInstrumento"
                FROM "BSCL.Custodia.BD::CUS.Cartera" CA
                LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (CA."EGRFolioComprobante" = CmbteDet."IdComprobante" AND   CA."EGRFolioExternoReferencia" = CmbteDet."FolioTransaccion")
                WHERE CmbteDet."FolioTransaccion" IS NOT NULL AND CmbteDet."FolioCartera" = 0
                GROUP BY CA."EGRFolioExternoReferencia", CmbteDet."IdComprobanteDetalle", CmbteDet."NemotecnicoInstrumento"
                ) AS TBCOMPRA
        LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (TBCOMPRA."IdComprobanteDetalle" = CmbteDet."IdComprobanteDetalle" AND TBCOMPRA."EGRFolioExternoReferencia" = CmbteDet."FolioTransaccion")
        WHERE CmbteDet."FolioTransaccion" IS NOT NULL AND CmbteDet."FolioCartera" = 0
       ) )
       A2 ON A1."IdMovimientoComprobante"=A2."IdMovimientoComprobante" 
       WHERE A1."IdMovimientoComprobante"=CmbteDet."IdMovimientoComprobante"
        ),0) END) AS "MontoEvento", 
    CmbteDet."IdMonedaLiquidacion" 								AS  "CodigoMoneda"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 			Cmbte
	  LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (Cmbte."IdComprobante" = CmbteDet."IdComprobante")
	WHERE "FechaSistema" = :FechaContable AND "IdEstadoDTE"  IN ('PEN', 'ENV', 'REC')
	AND "DocumentoIdentidad" = :DocumentoIdentidad AND CmbteDet."IdMovimientoComprobante" IS NOT NULL 
	AND Cmbte."IdClasificacionTipoInstrumento" NOT IN ('RV','IF')
GROUP BY CmbteDet."IdComprobanteDetalle",CmbteDet."IdMonedaLiquidacion"
	HAVING SUM(CASE
	WHEN Cmbte."IdClasificacionTipoInstrumento" IN ('RF') THEN IFNULL((SELECT A1."montoVenta" - A2."MontoCompra"
FROM(
SELECT 
"Monto" AS "montoVenta", CmbteDet."NemotecnicoInstrumento",CmbteDet."IdComprobanteDetalle", CmbteDet."IdMovimientoComprobante"
    FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MovCmbteDet
    INNER JOIN COMPROBANTES_DETALLE CmbteDet ON (CmbteDet."IdMovimientoComprobante" = MovCmbteDet."IdMovimientoComprobante")
    ) A1 LEFT JOIN(( SELECT 
           TBCOMPRA."MontoParcial" AS "MontoCompra", TBCOMPRA."IdComprobanteDetalle",TBCOMPRA."NemotecnicoInstrumento",CmbteDet."IdMovimientoComprobante"
        FROM (
                SELECT SUM((CA."INGMontoATasaCompraActual"*CA."EGRCantidad")/CA."INGCantidad" ) AS "MontoParcial", 
                CA."EGRFolioExternoReferencia", CmbteDet."IdComprobanteDetalle", CmbteDet."NemotecnicoInstrumento"
                FROM "BSCL.Custodia.BD::CUS.Cartera" CA
                LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (CA."EGRFolioComprobante" = CmbteDet."IdComprobante" AND   CA."EGRFolioExternoReferencia" = CmbteDet."FolioTransaccion")
                WHERE CmbteDet."FolioTransaccion" IS NOT NULL AND CmbteDet."FolioCartera" = 0
                GROUP BY CA."EGRFolioExternoReferencia", CmbteDet."IdComprobanteDetalle", CmbteDet."NemotecnicoInstrumento"
                ) AS TBCOMPRA
        LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (TBCOMPRA."IdComprobanteDetalle" = CmbteDet."IdComprobanteDetalle" AND TBCOMPRA."EGRFolioExternoReferencia" = CmbteDet."FolioTransaccion")
        WHERE CmbteDet."FolioTransaccion" IS NOT NULL AND CmbteDet."FolioCartera" = 0
       ) )
       A2 ON A1."IdMovimientoComprobante"=A2."IdMovimientoComprobante" 
       WHERE A1."IdMovimientoComprobante"=CmbteDet."IdMovimientoComprobante"
        ),0) END) <0
UNION      
		SELECT 
		TO_BIGINT(:IdTipoCentralizacion),"IdMovimiento",
		CASE WHEN utilidadesRV."MontoEvento" >0 THEN 'UTILIDAD' ELSE 'PERDIDA'END  AS "IdEvento",
		CASE WHEN utilidadesRV."MontoEvento" >0 THEN 'UTILIDAD' ELSE 'PERDIDA' END AS "Nombre",
		"MontoEvento",
		"CodigoMoneda"
		FROM (SELECT -- top 1 
		
		CmbteDet."IdComprobanteDetalle"				       		    AS "IdMovimiento", 
		(CASE WHEN Cmbte."IdTipoComprobante" IN ('NCE','NCO','NCR','NDE','NDR','NEE','NEO')  THEN -1 ELSE 1 END) *(SUM(IFNULL(CmbteDet."MontoLiquidacion",0) - IFNULL((SELECT sum("PrecioCompra" *"EGRCantidad") AS "MontoCompra"
		                                      FROM "BSCL.Custodia.BD::CUS.Cartera"
		                                      WHERE ((CmbteDet."FolioTransaccion" != 0 AND "EGRFolioExternoReferencia" = CmbteDet."FolioTransaccion") 
		OR (CmbteDet."FolioTransaccion" = 0 AND "FolioCartera" = CmbteDet."FolioCartera")) AND "EGRFolioComprobante" = CASE WHEN Cmbte."IdTipoComprobante"IN ('NCE','NCO','NCR','NDE','NDR','NEE','NEO') THEN Cmbte."IdComprobanteReferencia" ELSE   Cmbte."IdComprobante" END 
		                                        AND CmbteDet."NemotecnicoInstrumento" = "Nemotecnico"
		                                      GROUP  BY "Nemotecnico"),0)))
		                                          AS "MontoEvento",  
		CmbteDet."IdMonedaLiquidacion" 								AS  "CodigoMoneda"
		FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 			Cmbte
		 LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (Cmbte."IdComprobante" = CmbteDet."IdComprobante")
		WHERE "FechaSistema" = :FechaContable AND "IdEstadoDTE"  IN ('PEN', 'ENV', 'REC') AND "IdEstadoComprobante" <> 'ANU' 
		AND   Cmbte."IdClasificacionTipoInstrumento" IN ('RV')
		AND "DocumentoIdentidad" = :DocumentoIdentidad 
		AND (Cmbte."AccionTipoOperacion"!= 'COMPRA' OR (Cmbte."AccionTipoOperacion"= 'COMPRA' AND Cmbte."IdTipoComprobante"IN ('NCE','NCO','NCR','NDE','NDR','NEE','NEO') ))
		GROUP BY CmbteDet."IdComprobanteDetalle" ,CmbteDet."IdMonedaLiquidacion",CmbteDet."NemotecnicoInstrumento",Cmbte."IdTipoComprobante") utilidadesRV
UNION 
SELECT TO_BIGINT(:IdTipoCentralizacion),
"IdMovimiento",
CASE WHEN utilidadesIF."MontoEvento" >0 THEN 'UTILIDAD' ELSE 'PERDIDA' END AS "IdEvento",
CASE WHEN utilidadesIF."MontoEvento" >0 THEN 'UTILIDAD' ELSE 'PERDIDA' END AS "Nombre",
"MontoEvento",
"CodigoMoneda"
FROM (SELECT -- top 1 
    
    CmbteDet."IdComprobanteDetalle"				       		    AS "IdMovimiento",
    (CASE WHEN Cmbte."IdTipoComprobante" IN ('NCE','NCO','NCR','NDE','NDR','NEE','NEO')  THEN -1 ELSE 1 END) * SUM((IFNULL(CmbteDet."MontoLiquidacion",0) - round( IFNULL((SELECT sum(CASE WHEN "IdCarga" !=0 THEN( ("INGMontoATasaCompraActual" /NULLIF("INGValorRescate" ,0))* "EGRValorRescate")
    																		ELSE (("MontoCompra"/"ValorRescate")*"EGRValorRescate")END)   AS "MontoCompra"
                                                  FROM "BSCL.Custodia.BD::CUS.Cartera"
                                                  WHERE ((CmbteDet."FolioTransaccion" != 0 AND "EGRFolioExternoReferencia" = CmbteDet."FolioTransaccion") 
    OR (CmbteDet."FolioTransaccion" = 0 AND "FolioCartera" = CmbteDet."FolioCartera")) AND "EGRFolioComprobante" = CASE WHEN Cmbte."IdTipoComprobante"IN ('NCE','NCO','NCR','NDE','NDR','NEE','NEO') THEN Cmbte."IdComprobanteReferencia" ELSE   Cmbte."IdComprobante" END
                                                    AND CmbteDet."NemotecnicoInstrumento" = "NemoSVS" AND "IdClasificacionTipoInstrumento" = 'IF' 
                                                  GROUP  BY "NemoSVS"),0),0))
     )                                         AS "MontoEvento",  
    CmbteDet."IdMonedaLiquidacion" 								AS  "CodigoMoneda"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 			Cmbte
	 LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (Cmbte."IdComprobante" = CmbteDet."IdComprobante")
	WHERE "FechaSistema" = :FechaContable AND "IdEstadoDTE"  IN ('PEN', 'ENV', 'REC') AND "IdEstadoComprobante" <> 'ANU' 
	AND   Cmbte."IdClasificacionTipoInstrumento" IN ('IF')
	AND "DocumentoIdentidad" = :DocumentoIdentidad AND( Cmbte."AccionTipoOperacion"!= 'COMPRA' OR (Cmbte."AccionTipoOperacion"= 'COMPRA' AND Cmbte."IdTipoComprobante"IN ('NCE','NCO','NCR','NDE','NDR','NEE','NEO') ))
GROUP BY CmbteDet."IdComprobanteDetalle" ,CmbteDet."IdMonedaLiquidacion",Cmbte."IdTipoComprobante") utilidadesIF
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion), ComprobanteCobro."IdComprobante",Cobro."IdCobro", Cobro."NombreCobro", ABS(IFNULL("MontoLocalCobro",0)), "IdMonedaLocalCobro"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" ComprobanteCobro
	JOIN "BSCL.Plataforma.BD::COM.Cobro" Cobro ON
	Cobro."IdCobro" = ComprobanteCobro."IdCobro"
	WHERE ComprobanteCobro."IdComprobante" IN (SELECT "IdComprobante" FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
											   WHERE "FechaSistema" = :FechaContable AND "IdEstadoDTE"  IN ('PEN', 'ENV', 'REC') AND "DocumentoIdentidad" = :DocumentoIdentidad AND "IdEstadoComprobante" <> 'ANU')
UNION 
SELECT TO_BIGINT(:IdTipoCentralizacion),
"IdMovimiento",
CASE WHEN utilidadesIF."MontoEvento" >0 THEN 'UTILIDAD_NETA_IF' ELSE 'PERDIDA_NETA_IF' END AS "IdEvento",
CASE WHEN utilidadesIF."MontoEvento" >0 THEN 'UTILIDAD NETA IF' ELSE 'PERDIDA NETA IF' END AS "Nombre",
"MontoEvento",
"CodigoMoneda"
FROM (SELECT -- top 1 
    
    CmbteDet."IdComprobanteDetalle"				       		    AS "IdMovimiento",
    (CASE WHEN Cmbte."IdTipoComprobante" IN ('NCE','NCO','NCR','NDE','NDR','NEE','NEO')  THEN -1 ELSE 1 END) * SUM((IFNULL(CmbteDet."MontoLiquidacion",0) - round( IFNULL((SELECT sum( ("INGMontoATasaCompraActual" /NULLIF("INGValorRescate" ,0))* "EGRValorRescate") AS "MontoCompra"
                                                  FROM "BSCL.Custodia.BD::CUS.Cartera"
                                                  WHERE ((CmbteDet."FolioTransaccion" != 0 AND "EGRFolioExternoReferencia" = CmbteDet."FolioTransaccion") 
    OR (CmbteDet."FolioTransaccion" = 0 AND "FolioCartera" = CmbteDet."FolioCartera")) AND "EGRFolioComprobante" = CASE WHEN Cmbte."IdTipoComprobante"IN ('NCE','NCO','NCR','NDE','NDR','NEE','NEO') THEN Cmbte."IdComprobanteReferencia" ELSE   Cmbte."IdComprobante" END
                                                    AND CmbteDet."NemotecnicoInstrumento" = "NemoSVS" AND "IdClasificacionTipoInstrumento" = 'IF' 
                                                  GROUP  BY "NemoSVS"),0),0))
     )                                         AS "MontoEvento",  
    CmbteDet."IdMonedaLiquidacion" 								AS  "CodigoMoneda"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 			Cmbte
	 LEFT JOIN COMPROBANTES_DETALLE CmbteDet ON (Cmbte."IdComprobante" = CmbteDet."IdComprobante")
	WHERE "FechaSistema" = :FechaContable AND "IdEstadoDTE"  IN ('PEN', 'ENV', 'REC') AND "IdEstadoComprobante" <> 'ANU' 
	AND   Cmbte."IdClasificacionTipoInstrumento" IN ('IF')
	AND "DocumentoIdentidad" = :DocumentoIdentidad AND( Cmbte."AccionTipoOperacion"!= 'COMPRA' OR (Cmbte."AccionTipoOperacion"= 'COMPRA' AND Cmbte."IdTipoComprobante"IN ('NCE','NCO','NCR','NDE','NDR','NEE','NEO') ))
GROUP BY CmbteDet."IdComprobanteDetalle" ,CmbteDet."IdMonedaLiquidacion",Cmbte."IdTipoComprobante") utilidadesIF
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion), ComprobanteCobro."IdComprobante",Cobro."IdCobro", Cobro."NombreCobro", ABS(IFNULL("MontoLocalCobro",0)), "IdMonedaLocalCobro"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" ComprobanteCobro
	JOIN "BSCL.Plataforma.BD::COM.Cobro" Cobro ON
	Cobro."IdCobro" = ComprobanteCobro."IdCobro"
	WHERE ComprobanteCobro."IdComprobante" IN (SELECT "IdComprobante" FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
											   WHERE "FechaSistema" = :FechaContable AND "IdEstadoDTE"  IN ('PEN', 'ENV', 'REC') AND "DocumentoIdentidad" = :DocumentoIdentidad AND "IdEstadoComprobante" <> 'ANU')