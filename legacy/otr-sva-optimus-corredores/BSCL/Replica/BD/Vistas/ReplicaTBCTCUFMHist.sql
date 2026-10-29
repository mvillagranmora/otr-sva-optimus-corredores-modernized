SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCUFMHist"; 

--SELECT count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCUFMHist"
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCUFMHist" AS
WITH
	v_FechasCierre as (
		SELECT TO_VARCHAR("FechaRespaldo", 'YYYY-MM'),   max("FechaRespaldo" ) AS "FechaCierre"
						FROM "BSCL.Custodia.BD::HIS.Cartera" AS  c
								WHERE "IdEstado" = 'VIG'
								  AND "IdTipoInstrumento" = 'FFM'
								  GROUP BY TO_VARCHAR("FechaRespaldo", 'YYYY-MM')
	),
	v_Saldo_Cartera as (
		SELECT 
			TO_VARCHAR("FechaRespaldo", 'YYYY-MM') AS "Periodo", "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra",
			SUM("INGCantidad") AS "Cantidad",SUM("INGMontoATasaCompraActual") AS "Monto",
			SUM(CASE WHEN "IdTipoOperacion" = 'APORFM' THEN "INGMontoAPlazo" ELSE 0 END) AS "MontoAporte",
			SUM(CASE WHEN "IdTipoOperacion" = 'RESCFM' THEN "INGMontoAPlazo" ELSE 0 END) AS "MontoRescate"
		FROM "BSCL.Custodia.BD::HIS.Cartera" c JOIN 
					v_FechasCierre AS t 	ON ( t."FechaCierre" =   c."FechaRespaldo")
		WHERE "IdEstado" = 'VIG'
		  AND "IdTipoInstrumento" = 'FFM'
		  AND "INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
		GROUP BY 
			TO_VARCHAR("FechaRespaldo", 'YYYY-MM'),"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra"
	),
	v_Saldo_Garantia as (
		SELECT 
			TO_VARCHAR("FechaRespaldo", 'YYYY-MM') AS "Periodo", G."FolioCarteraOrigen" AS "FolioCartera",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",'DCV'  AS "IdBoveda",
			g."PrecioPromedioCompraOperacionOrigen" AS "INGMontoPrecioPromedioMonedaInstrumento","MontoPrecioPromedioCompraOperacionOrigen" AS "INGMontoMercadoActualMonedaInstrumento",
			g."PrecioGarantia" AS "INGPrecioMercadoActual","PrecioPromedioCompraOperacionOrigen" AS "INGPrecioPromedioCompra","IdMonedaSistema" AS "IdMonedaInstrumento",
			SUM(G."INGCantidad") AS "Cantidad",SUM(G."INGMontoATasaGarantiaActual") AS "Monto"
		FROM "BSCL.Custodia.BD::HIS.Garantia" AS G		INNER JOIN v_FechasCierre t ON  ( t."FechaCierre" =   g."FechaRespaldo")
		--INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS C ON C."FolioCartera" = G."FolioCarteraOrigen"
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'FM'
		--  AND G."IdTipoInstrumento" <> 'FFM'
		  AND G."INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
		GROUP BY 
			TO_VARCHAR("FechaRespaldo", 'YYYY-MM'),"FolioCarteraOrigen",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",
			"PrecioPromedioCompraOperacionOrigen","MontoPrecioPromedioCompraOperacionOrigen","PrecioGarantia","PrecioPromedioCompraOperacionOrigen","IdMonedaSistema"			
	)
SELECT
	--TOP 1
	MAX(WST."IdStack") AS "IDSTACK",
	 SCUST."Periodo"	  || ':' || COALESCE(rtrim(ltrim(TRIM(PER."DocumentoIdentidad"))),'') || ':' ||  rtrim(ltrim(POR."CodigoPortafolio" )) || ':' ||  rtrim(ltrim(SCUST."Nemotecnico")) || ':' ||  COALESCE(BOVEDA , SCUST."IdBoveda",'COR' ) AS TABLE_KEY,
	SCUST."Periodo"	 AS "COD_PER"
	
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') AS RUT_CLI
	
	,CAST(POR."CodigoPortafolio" AS VARCHAR(40)) AS "SEC_RUT_CLI"
	,CAST(SCUST."Nemotecnico" AS VARCHAR(40)) AS "NEMO"
	,COALESCE(BOVEDA , SCUST."IdBoveda",'COR' ) AS "COD_BOVEDA" --CAST(SCUST."IdBoveda" AS VARCHAR(3)) AS "COD_BOVEDA"
	,SUM(SCUST."Cantidad_Cartera") AS "CAN_CUSTODIA"
	,SUM(SCUST."Cantiddad_Garantia") AS "CAN_GARANTIA"
	,0 AS "CAN_COM_PLAZO"
	,0 AS "CAN_LIBRE_01"
	,0 AS "CAN_LIBRE_02"
	,0 AS "CAN_LIBRE_03"
	,0 AS "CAN_TRAM_COM"
	,0 AS "CAN_TRAM_VEN"
	,0 AS "CAN_VEN_PLAZO"
	,SUM(SCUST."INGMontoPrecioPromedioMonedaInstrumento") AS "MONTO_INI_AJUS"
	,SUM(SCUST."INGMontoMercadoActualMonedaInstrumento") AS "MONTO_VALORIZ"
	,avg(SCUST."INGPrecioMercadoActual") AS "PRECIO_CIERRE"
	,avg(SCUST."INGPrecioPromedioCompra") AS "PRECIO_PROM"
	,0 AS "CAN_TRAM_APORTE"
	,0 AS "CAN_TRAM_RESCATE"
FROM (
	SELECT 
			P."Periodo"	,
			P."FolioCartera",
			P."IdCliente",
			P."IdPortafolio",
			P."IdTipoInstrumento",
			P."IdInstrumento",
			P."Nemotecnico",
			P."IdBoveda",
			P."INGMontoPrecioPromedioMonedaInstrumento",
			P."INGMontoMercadoActualMonedaInstrumento",
			P."INGPrecioMercadoActual",
			P."INGPrecioPromedioCompra",
			COALESCE(C."Cantidad",0) AS "Cantidad_Cartera",
			COALESCE(C."Monto",0) AS "Monto_Cartera",
			COALESCE(C."MontoAporte",0) AS "MontoAporte",
			COALESCE(C."MontoRescate",0) AS "MontoRescate",
			COALESCE(G."Cantidad",0) AS "Cantiddad_Garantia",
			COALESCE(G."Monto",0) AS "Monto_Garantia"
	FROM (
		SELECT DISTINCT 
			"Periodo" ,"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra"
		FROM (
			SELECT 
				"Periodo" ,"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra"
			FROM v_Saldo_Cartera
			UNION ALL
			SELECT 
				"Periodo" ,"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra"
			FROM v_Saldo_Garantia
		)
	) as P
	LEFT JOIN v_Saldo_Cartera as C 
			ON C."IdCliente" = P."IdCliente"
			AND P."Periodo" = C."Periodo"  
			AND C."FolioCartera" = P."FolioCartera"
			AND C."IdPortafolio" = P."IdPortafolio"
			AND C."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND C."IdInstrumento" = P."IdInstrumento"
			AND C."IdBoveda" = P."IdBoveda"
			AND C."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND C."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND C."INGPrecioMercadoActual" = P."INGPrecioMercadoActual"
			AND C."INGPrecioPromedioCompra" = P."INGPrecioPromedioCompra"
	LEFT JOIN v_Saldo_Garantia as G 
			ON G."IdCliente" = P."IdCliente"
			AND g."Periodo" = p."Periodo"  
			AND G."FolioCartera" = P."FolioCartera"
			AND G."IdPortafolio" = P."IdPortafolio"
			AND G."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND G."IdInstrumento" = P."IdInstrumento"
			AND G."IdBoveda" = P."IdBoveda"
			AND G."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND G."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND G."INGPrecioMercadoActual" = P."INGPrecioMercadoActual"
			AND G."INGPrecioPromedioCompra" = P."INGPrecioPromedioCompra"
) AS SCUST
left JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
left JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')

LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdCliente" = CLI."IdCliente"  AND SCUST."IdPortafolio" = por."IdPortafolio"
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTCUFM' AND WST."Key" = SCUST."FolioCartera"
--WHERE SCUST."IdCliente" = 6082 
cross JOIN (SELECT 'TOT' AS BOVEDA  FROM dummy UNION 
				SELECT null AS BOVEDA  FROM dummy 
								)
WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0								
	AND LEFT(fec."Valor",7) !=  SCUST."Periodo"	
	
GROUP BY SCUST."Periodo"	, PER."DocumentoIdentidad", POR."CodigoPortafolio", SUBSTRING (SCUST."IdTipoInstrumento",0,2), SCUST."Nemotecnico", COALESCE(BOVEDA , SCUST."IdBoveda",'COR' )

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTCUFM" TO  BCS_DESARROLLADOR WITH GRANT OPTION;