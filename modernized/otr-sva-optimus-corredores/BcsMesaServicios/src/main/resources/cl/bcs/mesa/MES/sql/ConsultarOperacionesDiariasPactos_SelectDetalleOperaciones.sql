
SELECT 
	OPE."IdTipoOperacion" AS "TipoCompromiso"
	,0 AS "IndicadorCobertura" 
	,TO_NVARCHAR(OPE."FechaOperacion",'DD-MM-YYYY') AS "FechaOperacion"
	,CASE WHEN CLI."Tipo"='J' THEN CLI."RazonSocial" ELSE CLI."NombreCompleto" END AS "NombreCliente"
	,CLI."DocumentoIdentidad" AS "RutCliente"
	,IND."ValorIndicador" AS "Relacionado"
	,RES."IdClasificacionTipoInstrumentoDetalle" AS "Mercado"
	,RES."NemotecnicoDetalle" AS "Nemotecnico"
	,RES."IdEmisorDetalle" AS "IdEmisor"
	,RES."IdTipoReajuste"
	,CASE WHEN RES."IdClasificacionTipoInstrumentoDetalle" = 'IF' THEN RES."ValorRescateDetalle" ELSE RES."CantidadDetalle" END AS "Cantidad"	
	,RES."FolioOperacion"
	,TO_NVARCHAR(OPE."FechaVencimiento",'DD-MM-YYYY') AS "FechaVencimientoOperacion"
	,RES."FechaVencimientoInstrumentoDetalle" AS "FechaVencimientoInstrumento"
	,TO_NVARCHAR(RES."FechaVencimientoInstrumentoDetalle",'DD-MM-YYYY') AS "FechaVencimientoInstrumentoCadena"
	,RES."MontoOperacionFinalMoneda" AS "MontoPacto"
	,OPE."IdTipoReajuste" AS "IdTipoReajusteOperacion"
	,OPE."MontoOperacion" 
	,RES."TasaCompraDetalle" AS "Tasa"
	,RES."MontoLiquidacionOperacionDetalle" AS "MontoSubyacente"
	,RES."INGMontoAPlazoDetalle" AS "MontoCompromisoDetalle"
	,CASE WHEN RES."IdTipoReajusteDetalle" <> '' THEN RES."IdTipoReajusteDetalle" ELSE CAR."IdMonedaSistema" END AS "IdMonedaInstrumento"
	,CASE
		WHEN RES."FolioCarteraDetalle" = 0 THEN res."FolioCarteraCustodiaDetalle"
		ELSE RES."FolioCarteraDetalle"
	END AS "FolioCartera"
 	,COALESCE(FLAG."FlagDeterioro",'N') AS "FlagDeterioro" --cambio
    ,COALESCE(FLAG."PorcentajeDeterioro",0 ) AS "PorcentajeDeterioro"
    ,CASE FLAG."FlagDeterioro" WHEN 'S' THEN 
            CASE WHEN Sub."IdClasificacionTipoInstrumento" = 'IF' THEN CAST((COALESCE(FLAG."PorcentajeDeterioro" ,0)/100) * COALESCE(Sub."ValorRescate",0) AS DECIMAL)
                ELSE CAST((COALESCE(FLAG."PorcentajeDeterioro" ,0)/100) * COALESCE(Sub."Cantidad",0) AS DECIMAL)
                END
            ELSE 0 
    END AS "Deterioro"
    ,COALESCE(FLAG."Fase",0) AS "Fase"
	,CASE WHEN Sub."IdMonedaInstrumento" <> '' THEN Sub."IdMonedaInstrumento" ELSE CAR."IdMonedaSistema" END AS "IdMonedaInstrumento" --cambio
FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF" RES
LEFT JOIN  "BSCL.Custodia.BD::CUS.Cartera" CAR ON (CAR."IdOperacion" = RES."IdOperacion")
LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE ON (CAR."FolioComprobante" = OPE."IdOperacion" OR CAR."IdOperacion" = OPE."IdOperacion")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON OPE."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS IND ON (IND."IdCliente" = CLI."IdCliente" AND "IdTipoIndicador" = 56 AND "GrupoIndicador" = 'NORMATIVOS')
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" Sub ON Sub."FolioCarteraRelacionada" = CAR."FolioCartera" AND CAR."IdTipoInstrumento" = 'PAC'
LEFT JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS FLAG ON FLAG."Emisor" = Sub."IdEmisor" AND FLAG."IdEstado" = 'ACT' --cambio
WHERE OPE."IdEstado" <> 'PEN' AND OPE."IdTipoOperacion" IN ('COMPAC', 'VENPAC' )
AND OPE."FolioOperacion" IN (:FolioOperacion);