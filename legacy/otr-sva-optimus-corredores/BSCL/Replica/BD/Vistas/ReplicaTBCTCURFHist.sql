SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCURFHist"; 

-- select count(1) from  "BSCL.Replica.BD.Vistas::ReplicaTBCTCURFHist"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCURFHist" AS
WITH
	

	v_FechasCierre as (
		SELECT TO_VARCHAR("FechaRespaldo", 'YYYY-MM'),   max("FechaRespaldo" ) AS "FechaCierre"
						FROM "BSCL.Custodia.BD::HIS.Cartera" AS  c
								WHERE "IdEstado" = 'VIG'
								  AND "IdClasificacionTipoInstrumento" = 'RF'
								  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
								  GROUP BY TO_VARCHAR("FechaRespaldo", 'YYYY-MM')
	),
	v_Saldo_Cartera as (
		SELECT 
			TO_VARCHAR("FechaRespaldo", 'YYYY-MM') AS "Periodo","FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",CASE WHEN trim("IdBoveda") = '' THEN NULL ELSE trim("IdBoveda") END AS "IdBoveda"  ,
			"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioPromedioCompra",
			SUM("INGCantidad") AS "Cantidad",SUM("INGMontoATasaCompraActual") AS "MontoCompraAjustado", avg("INGPrecioMercadoActual") AS "INGPrecioMercadoActual",
			avg ("INGTasaMercadoActual") AS "INGTasaMercadoActual" , avg("INGTasaCompra") AS "INGTasaCompra",
			 sum("INGMontoATasaMercadoActual") AS "MontoCartera"
		FROM "BSCL.Custodia.BD::HIS.Cartera" c JOIN 
					v_FechasCierre AS t 	ON ( t."FechaCierre" =   c."FechaRespaldo")
					
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'RF'
		  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
		  AND "INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD') --AND  "IdCliente" = 8627 
--		   AND "IdCliente" = 7228 AND "Nemotecnico" = 'BLAPO-F'
		GROUP BY 
			TO_VARCHAR("FechaRespaldo", 'YYYY-MM'),"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioPromedioCompra"
	),
	v_Saldo_Plazo as (
		SELECT 
			TO_VARCHAR(s."FechaRespaldo", 'YYYY-MM')  AS "Periodo",C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END AS "IdBoveda",
			C."INGMontoPrecioPromedioMonedaInstrumento",C."INGMontoMercadoActualMonedaInstrumento",C."INGPrecioPromedioCompra",
			SUM(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
			SUM(CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",
			SUM(S."INGMontoATasaSubyacenteActual") AS "Monto"
		FROM bscl."BSCL.Custodia.BD::HIS.Subyacente" as S 
					INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" AND s."FechaRespaldo" =   c."FechaRespaldo"
					INNER JOIN 	v_FechasCierre AS t 	ON ( t."FechaCierre" =   c."FechaRespaldo")
					
		WHERE S."IdEstado" = 'VIG'
		  AND C."IdEstado" = 'VIG' -- AND s."IdCarteraRetirada" = 1417500  
		  AND S."IdClasificacionTipoInstrumento" = 'RF'
		  AND C."IdTipoInstrumento" IN ('SIM','PAC')
		  AND C."INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
		GROUP BY 
			TO_VARCHAR(s."FechaRespaldo", 'YYYY-MM')  ,C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",C."IdBoveda",
			C."INGMontoPrecioPromedioMonedaInstrumento",C."INGMontoMercadoActualMonedaInstrumento",C."INGPrecioPromedioCompra"
		UNION
		SELECT 
			TO_VARCHAR(s."FechaRespaldo", 'YYYY-MM')  AS "Periodo",C."FolioCartera",C."IdClienteRelacionado",C."IdPortafolioRelacionado",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END AS "IdBoveda",
			C."INGMontoPrecioPromedioMonedaInstrumento",C."INGMontoMercadoActualMonedaInstrumento",C."INGPrecioPromedioCompra",
			SUM(CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
			SUM(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",
			SUM(S."INGMontoATasaSubyacenteActual") AS "Monto"
		FROM bscl."BSCL.Custodia.BD::HIS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" AND  s."FechaRespaldo" =   c."FechaRespaldo"
		INNER JOIN 	v_FechasCierre AS t 	ON ( t."FechaCierre" =   c."FechaRespaldo")
		WHERE S."IdEstado" = 'VIG'
		  AND C."IdEstado" = 'VIG' -- AND s."IdCarteraRetirada" = 1417500  
		  AND S."IdClasificacionTipoInstrumento" = 'RF'
		  AND C."IdTipoInstrumento" IN ('SIM','PAC')
		  AND C."INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
		GROUP BY 
			TO_VARCHAR(s."FechaRespaldo", 'YYYY-MM')  ,C."FolioCartera",C."IdClienteRelacionado",C."IdPortafolioRelacionado",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",C."IdBoveda",
			C."INGMontoPrecioPromedioMonedaInstrumento",C."INGMontoMercadoActualMonedaInstrumento",C."INGPrecioPromedioCompra"
	),
	v_Saldo_Garantia as (
		SELECT 
			TO_VARCHAR("FechaRespaldo", 'YYYY-MM')  AS "Periodo",G."FolioCarteraOrigen" AS "FolioCartera",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",'DCV'  AS "IdBoveda",
			g."PrecioPromedioCompraOperacionOrigen" AS "INGMontoPrecioPromedioMonedaInstrumento","MontoPrecioPromedioCompraOperacionOrigen" AS "INGMontoMercadoActualMonedaInstrumento",
			g."PrecioGarantia" AS "INGPrecioMercadoActual","PrecioPromedioCompraOperacionOrigen" AS "INGPrecioPromedioCompra","IdMonedaSistema" AS "IdMonedaInstrumento",
			SUM(G."INGCantidad") AS "Cantidad",SUM(G."INGMontoATasaGarantiaActual") AS "Monto"
		FROM "BSCL.Custodia.BD::HIS.Garantia" AS G
					INNER JOIN v_FechasCierre t ON  ( t."FechaCierre" =   g."FechaRespaldo")
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'RF'
		  AND G."IdTipoInstrumento" <> 'FFM'
		  AND G."INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
		GROUP BY 
			TO_VARCHAR("FechaRespaldo", 'YYYY-MM'),"FolioCarteraOrigen",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",
			"PrecioPromedioCompraOperacionOrigen","MontoPrecioPromedioCompraOperacionOrigen","PrecioGarantia","PrecioPromedioCompraOperacionOrigen","IdMonedaSistema"
	),
	v_Saldo_Transito AS (
		SELECT TO_VARCHAR(t."FechaRespaldo", 'YYYY-MM')  AS "Periodo","FolioCustodia" AS "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",max(CASE WHEN trim(t."IdBoveda") = '' THEN NULL ELSE trim(t."IdBoveda") END) AS "IdBoveda",
				sum("MontoCompra") AS "INGMontoPrecioPromedioMonedaInstrumento",sum("MontoCompra") AS  "INGMontoMercadoActualMonedaInstrumento",min("PrecioCompra") AS "INGPrecioMercadoActual", min("PrecioCompra") AS "INGPrecioPromedioCompra", 
				max("IdMonedaSistema") AS "IdMonedaInstrumento",
				SUM("Cantidad") * - 1 AS "Cantidad",SUM("MontoCompra") AS "Monto"
 
		FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS t  
				JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o
					ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
				INNER JOIN v_FechasCierre t1 
					ON  ( t1."FechaCierre" =   t."FechaRespaldo")
		WHERE  "IdEstado"  = 'PEN' AND "IdOrigenGarantia" = 'CUS' AND "IdClasificacionTipoInstrumento" = 'RF' 
		GROUP BY TO_VARCHAR(t."FechaRespaldo", 'YYYY-MM'),"FolioCustodia","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico"
	)
SELECT
	--TOP 1
	MAX(WST."IdStack") AS "IDSTACK",
	 SCUST."Periodo"	  || ':' || COALESCE(rtrim(ltrim(TRIM(PER."DocumentoIdentidad"))),'') || ':' ||  rtrim(ltrim(POR."CodigoPortafolio" )) || ':' ||  rtrim(ltrim(SCUST."Nemotecnico")) || ':' ||  COALESCE(BOVEDA , SCUST."IdBoveda",'DCV' )  AS TABLE_KEY,
	 SCUST."Periodo"	 AS "COD_PER"
	 ,CLI."IdCliente"
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') AS "RUT_CLI"
	
	,CAST(POR."CodigoPortafolio" AS VARCHAR(100)) AS "SEC_RUT_CLI"
	,CAST(SCUST."Nemotecnico" AS VARCHAR(40)) AS "NEMO"
	,COALESCE(BOVEDA , SCUST."IdBoveda",'DCV' ) AS "COD_BOVEDA"
	,SUM(SCUST."Cantidad_Compra_Plazo") AS "CAN_COM_PLAZO"
	,SUM(SCUST."Cantidad_Venta_Plazo") AS "CAN_VEN_PLAZO"
	,SUM(SCUST."Cantidad_Cartera") AS "CAN_CUSTODIA"
	,SUM(SCUST."Cantiddad_Garantia") AS "CAN_GARANTIA"
	, 0 AS "CAN_LIBRE_01"
	, 0 AS "CAN_LIBRE_02"
	, 0 AS "CAN_LIBRE_03"
	, 0 AS "CAN_TRAMITE_COM"
	, 0 AS "CAN_TRAMITE_VEN"
	
	,SUM(SCUST."MontoCompraAjustado") AS "MONTO_INI_AJUS"
	,SUM(SCUST."INGMontoMercadoActualMonedaInstrumento") AS "MONTO_VALORIZ"
	,avg(SCUST."INGPrecioMercadoActual") AS "PRECIO_MED_CIE"
	,avg(SCUST."INGPrecioPromedioCompra") AS "PRECIO_PROM"
	,avg(SCUST."INGTasaMercadoActual") AS "TIR_MED_CIE"
	, AVG(CASE WHEN SCUST."INGTasaCompra"  < 1000 THEN SCUST."INGTasaCompra" ELSE 0 END ) AS "TIR_PROM"
FROM (
	SELECT 
			P."Periodo"		, 
			P."FolioCartera",
			P."IdCliente",
			P."IdPortafolio",
			P."IdTipoInstrumento",
			P."IdInstrumento",
			P."Nemotecnico",
			P."IdBoveda",
			P."INGMontoPrecioPromedioMonedaInstrumento",
			P."INGMontoMercadoActualMonedaInstrumento",
			P."INGPrecioPromedioCompra",
			
			COALESCE(C."Cantidad",0) + COALESCE(T."Cantidad",0)  AS "Cantidad_Cartera",
			COALESCE(C."MontoCartera",0) - COALESCE(T."Monto",0)  AS "Monto_Cartera",			
			
			COALESCE(Z."Cantidad_Compra",0) AS "Cantidad_Compra_Plazo",
			COALESCE(Z."Cantidad_Venta",0) AS "Cantidad_Venta_Plazo",
			COALESCE(Z."Monto",0) AS "Monto_Plazo",
			COALESCE(G."Cantidad",0) AS "Cantiddad_Garantia",
			COALESCE(G."Monto",0) AS "Monto_Garantia",
			COALESCE(c."INGPrecioMercadoActual",0) AS "INGPrecioMercadoActual",
			COALESCE(c."INGTasaMercadoActual",0) AS "INGTasaMercadoActual",
			COALESCE(c."INGTasaCompra",0) AS "INGTasaCompra",
			COALESCE(c."MontoCompraAjustado" ,P."INGMontoPrecioPromedioMonedaInstrumento")  AS "MontoCompraAjustado"
	FROM (
		SELECT DISTINCT 
			"Periodo" ,"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioPromedioCompra"
		FROM (
			SELECT 
				"Periodo" ,"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioPromedioCompra"
			FROM v_Saldo_Cartera
			UNION ALL
			SELECT 
				"Periodo" ,"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioPromedioCompra"
			FROM v_Saldo_Plazo
			UNION ALL
			SELECT 
				"Periodo" ,"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioPromedioCompra"
			FROM v_Saldo_Garantia
				UNION ALL
			
			SELECT 
				"Periodo" ,"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioPromedioCompra"
			FROM v_Saldo_Transito
		)
	) as P
	
	LEFT JOIN v_Saldo_Cartera as C 
			ON C."IdCliente" = P."IdCliente"
			AND P."Periodo" = C."Periodo"  
			AND C."FolioCartera" = P."FolioCartera"
			AND C."IdPortafolio" = P."IdPortafolio"
			AND C."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND C."IdInstrumento" = P."IdInstrumento"
		--	AND C."IdBoveda" = P."IdBoveda"
			AND C."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND C."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND C."INGPrecioPromedioCompra" = P."INGPrecioPromedioCompra"
	LEFT JOIN v_Saldo_Plazo as Z
			ON Z."IdCliente" = P."IdCliente" 
			AND P."Periodo" = Z."Periodo"  
			AND Z."FolioCartera" = P."FolioCartera"
			AND Z."IdPortafolio" = P."IdPortafolio"
			AND Z."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND Z."IdInstrumento" = P."IdInstrumento"
		--	AND Z."IdBoveda" = P."IdBoveda"
			AND Z."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND Z."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND Z."INGPrecioPromedioCompra" = P."INGPrecioPromedioCompra"
	LEFT JOIN v_Saldo_Garantia as G 
			ON G."IdCliente" = P."IdCliente"
			AND P."Periodo" = G."Periodo"  
			AND G."FolioCartera" = P."FolioCartera"
			AND G."IdPortafolio" = P."IdPortafolio"
			AND G."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND G."IdInstrumento" = P."IdInstrumento"
		--	AND G."IdBoveda" = P."IdBoveda"
			AND G."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND G."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND G."INGPrecioPromedioCompra" = P."INGPrecioPromedioCompra"
			
			
	LEFT JOIN v_Saldo_Transito as T
			ON P."IdCliente" = T."IdCliente"
			AND P."Periodo" = T."Periodo"
			AND P."FolioCartera" = T."FolioCartera"
			AND P."IdPortafolio" = T."IdPortafolio"
			AND P."IdTipoInstrumento" = T."IdTipoInstrumento"
			AND P."IdInstrumento" = T."IdInstrumento"
			--AND P."IdBoveda" = T."IdBoveda"
			AND P."INGMontoPrecioPromedioMonedaInstrumento" = T."INGMontoPrecioPromedioMonedaInstrumento"
			AND P."INGMontoMercadoActualMonedaInstrumento" = T."INGMontoMercadoActualMonedaInstrumento"
			AND P."INGPrecioPromedioCompra" = T."INGPrecioPromedioCompra"
						
			
) AS SCUST
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdCliente" = CLI."IdCliente"  AND SCUST."IdPortafolio" = por."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')

LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTCURF' AND WST."Key" = SCUST."FolioCartera"

cross JOIN (SELECT 'TOT' AS BOVEDA  FROM dummy UNION 
				SELECT null AS BOVEDA  FROM dummy 
								)
WHERE LEFT(fec."Valor",7) !=  SCUST."Periodo"									
GROUP BY SCUST."Periodo"		, CLI."IdCliente", LEFT(fec."Valor",7),PER."DocumentoIdentidad",
		POR."CodigoPortafolio",SUBSTRING (SCUST."IdTipoInstrumento",0,2),SCUST."Nemotecnico",COALESCE(BOVEDA , SCUST."IdBoveda",'DCV' )

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTCURF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
