SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCUFMHist"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCUFMHist" AS

WITH
	v_Saldo_Cartera as (
		SELECT 'CAR' AS "Tipo",
			"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",
			CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(c."IdBoveda") END AS "IdBoveda",
			"INGPrecioMercadoActual" AS "PrecioMercado","INGPrecioCompra" as "PrecioCompra","IdMonedaInstrumento",
			"INGCantidad" AS "Cantidad",0 as "PrecioAPlazo","IdTipoOperacion"
		FROM "BSCL.Custodia.BD::HIS.Cartera" AS  c
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'FM'
		  
	),
	v_Saldo_Garantia as (
			SELECT 'GAR' AS "Tipo",
			G."FechaRespaldo",G."FolioGarantia" AS "FolioCartera",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",
			CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(C."IdBoveda") END AS "IdBoveda",
			c."INGPrecioMercadoActual" AS "PrecioMercado","PrecioCompraOperacionOrigen" as "PrecioCompra",G."IdMonedaInstrumento",
			G."INGCantidad" AS "Cantidad", 0 as "PrecioAPlazo","IdTipoOperacion"
		FROM "BSCL.Custodia.BD::HIS.Garantia" AS G
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" AS C ON C."IdCartera" = G."IdCarteraOrigen" and G."FechaRespaldo" = C."FechaRespaldo"
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'FM'
		  AND G."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	),
	v_Saldo_Transito AS (
		SELECT "FechaRespaldo","FolioCustodia","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",
				CASE WHEN trim(coalesce(t."IdBoveda",'')) = '' THEN NULL ELSE trim(t."IdBoveda") END AS "IdBoveda",
				"PrecioCompra" AS "PrecioCompra", 0 as "PrecioAPlazo",t."IdTipoOperacion",
				"IdMonedaInstrumento" AS "IdMonedaInstrumento",
				"Cantidad","MontoCompra" AS "Monto"
		FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS t 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o
				ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
		WHERE  "IdEstado"  = 'PEN'  AND "IdClasificacionTipoInstrumento" = 'FM'
	)
SELECT 
	TO_DATE("FechaRespaldo") AS "FechaRespaldo"
	,SCUST."IdInstrumento"
	,PRECIO_BCS."FechaSistema"
	,CLI."IdCliente"
	,PER."DocumentoIdentidad"
	,SCUST."IdPortafolio"
	,POR."CodigoPortafolio"
	,LEFT("FechaRespaldo",7)  AS "COD_PER"
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') AS "RUT_CLI"
	,CAST(POR."CodigoPortafolio" AS VARCHAR(100)) AS "SEC_RUT_CLI"
	,CAST(SCUST."Nemotecnico" AS VARCHAR(40)) AS "NEMO"
	,COALESCE(BOVEDA , SCUST."IdBoveda" ) AS "COD_BOVEDA"
	,0 AS "CAN_COM_PLAZO"
	,SUM(SCUST."CantidadDisponible" - SCUST."CantidadNegativa") AS "CAN_CUSTODIA"	
	,SUM(SCUST."CantidadGarantia") AS "CAN_GARANTIA"
	,0 AS "CAN_LIBRE_01"
	,0 AS "CAN_LIBRE_02"
	,0 AS "CAN_LIBRE_03"
	,0 AS "CAN_TRAM_COM"
	,0 AS "CAN_TRAM_VEN"
	,0 AS "CAN_VEN_PLAZO"
	,CASE 
	    WHEN COALESCE(MAX(conf."Valor"), 'N') = 'S' AND SCUST."IdMonedaInstrumento" = 'USD'
	    THEN
			TO_DECIMAL(ROUND(SUM(SCUST."PrecioCompra"*(SCUST."CantidadDisponible" + SCUST."CantidadGarantia" - SCUST."CantidadNegativa")*dolar."Valor"),0,ROUND_HALF_UP),17,2) 
		ELSE 
			TO_DECIMAL(ROUND(SUM(SCUST."PrecioCompra"*(SCUST."CantidadDisponible" + SCUST."CantidadGarantia" - SCUST."CantidadNegativa")),0,ROUND_HALF_UP),17,2)
	END AS "MONTO_INI_AJUS"
	,CASE SCUST."IdMonedaInstrumento" 
		WHEN 'USD' THEN TO_DECIMAL(ROUND(SUM(SCUST."CantidadDisponible" + SCUST."CantidadGarantia" - SCUST."CantidadNegativa")*COALESCE(PRECIO_BCS."Precio",0) * dolar."Valor",0,ROUND_HALF_UP),17,2)
		ELSE TO_DECIMAL(ROUND(SUM(SCUST."CantidadDisponible" + SCUST."CantidadGarantia" - SCUST."CantidadNegativa")*COALESCE(PRECIO_BCS."Precio",0),0,ROUND_HALF_UP),17,2)
		END AS "MONTO_VALORIZ"
	,COALESCE(PRECIO_BCS."Precio", 0) AS "PRECIO_CIERRE"
	,ROUND(
	    CASE sum(SCUST."CantidadDisponible" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo" + SCUST."CantidadEmprestito") WHEN 0 THEN 0 ELSE
	    sum( SCUST."PrecioCompra"* ( SCUST."CantidadDisponible" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo" + SCUST."CantidadEmprestito" ) )
	    /sum(SCUST."CantidadDisponible" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo" + SCUST."CantidadEmprestito") END 
	   ,4,ROUND_HALF_UP)
	    
	    AS "PRECIO_PROM"
	,0 AS "CAN_TRAM_APORTE"
	,0 AS "CAN_TRAM_RESCATE"
FROM (
		SELECT 
			"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"PrecioMercado","PrecioCompra","IdMonedaInstrumento","IdTipoOperacion","PrecioAPlazo",
			"Cantidad" AS "CantidadDisponible",
			0 AS "CantidadTransito",
			0 AS "CantidadNegativa",
			0 AS "CantidadCompraPlazo",
			0 AS "CantidadVentaPlazo",
			0 AS "CantidadPrestamo",
			0 AS "CantidadEmprestito",
			0 AS "CantidadGarantia",
			0 AS "MontoPlazo",
			0 AS "MontoGarantia",
			0 AS "MontoNegativo"
		FROM v_Saldo_Cartera
		UNION
		SELECT 
			"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"PrecioMercado","PrecioCompra","IdMonedaInstrumento","IdTipoOperacion","PrecioAPlazo",
			0 AS "CantidadDisponible",
			0 AS "CantidadTransito",
			0 AS "CantidadNegativa",
			0 AS "CantidadCompraPlazo",
			0 AS "CantidadVentaPlazo",
			0 AS "CantidadPrestamo",
			0 AS "CantidadEmprestito",
			"Cantidad" AS "CantidadGarantia",
			0 AS "MontoPlazo",
			0 AS "MontoGarantia",
			0 AS "MontoNegativo"
			FROM v_Saldo_Garantia	
			UNION
		SELECT 
			"FechaRespaldo","FolioCustodia" AS "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			0 AS "PrecioMercado","PrecioCompra","IdMonedaInstrumento","IdTipoOperacion","PrecioAPlazo",
			0 AS "CantidadDisponible",
			0 AS "CantidadTransito",
			"Cantidad" AS "CantidadNegativa",
			0 AS "CantidadCompraPlazo",
			0 AS "CantidadVentaPlazo",
			0 AS "CantidadPrestamo",
			0 AS "CantidadEmprestito",
			0 AS "CantidadGarantia",
			0 AS "MontoPlazo",
			0 AS "MontoGarantia",
			"Monto" AS "MontoNegativo"
			FROM v_Saldo_Transito				
) AS SCUST
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS PER ON PER."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdCliente" = CLI."IdCliente"  AND SCUST."IdPortafolio" = por."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
LEFT JOIN "BSCL.Replica.BD::ConfiguracionReplica" AS conf ON (conf."IdEmpresa" = 1 AND conf."CodigoParametro" = 'TBCTCXFM_MontoIniAjus_en_CLP')
LEFT JOIN (
	select top 1 f."Valor"
	from "BSCL.Plataforma.BD::MER.ParametroFinanciero" f
	left join "BSCL.Plataforma.BD::PLA.Configuracion" conf on conf."CodigoParametro" = 'PLA.FechaSistema'
	where "IdParametroFinanciero" = 'DO' and "Fecha" <= conf."Valor"
	order by f."Fecha" desc
) AS dolar on 1 = 1
LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" AS PRECIO_BCS ON 
	PRECIO_BCS."IdEmpresa" = 1 AND SCUST."IdInstrumento" = PRECIO_BCS."IdInstrumento" AND TO_DATE(PRECIO_BCS."FechaSistema") = TO_DATE(SCUST."FechaRespaldo")
cross JOIN (SELECT 'TOT' AS BOVEDA  FROM dummy UNION 
				SELECT null AS BOVEDA  FROM dummy 
								)
WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0
AND PER."DocumentoIdentidad" not like '%.%'
AND SCUST."FechaRespaldo" = '2025-03-31'				
GROUP BY SCUST."FechaRespaldo",CLI."IdCliente",POR."CodigoPortafolio",scust."IdInstrumento",PER."DocumentoIdentidad", PRECIO_BCS."Precio",PRECIO_BCS."FechaSistema",
SCUST."IdPortafolio",fec."Valor", LEFT(fec."Valor",7),PER."DocumentoIdentidad",POR."CodigoPortafolio", SCUST."Nemotecnico",COALESCE(BOVEDA , SCUST."IdBoveda" ) ,SCUST."IdMonedaInstrumento",dolar."Valor"


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTCUFMHist" TO  BCS_DESARROLLADOR WITH GRANT OPTION;