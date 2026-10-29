SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCURVHist"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCURVHist" AS

WITH
	v_Saldo_Cartera as (
		SELECT 'CAR' AS "Tipo",
			"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",
			CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(c."IdBoveda") END AS "IdBoveda",
			"INGPrecioMercadoActual" AS "PrecioMercado","INGPrecioCompra" as "PrecioCompra","IdMonedaInstrumento",
			"INGCantidad" AS "Cantidad",0 as "PrecioAPlazo","IdTipoOperacion"
		FROM "BSCL.Custodia.BD::HIS.Cartera" AS  c
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'RV'
		  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
	),
	v_Saldo_Cartera_Negativa as (
		SELECT 'NEG' AS "Tipo",
			car."FechaRespaldo",car."IdCarteraNegativa" AS "FolioCartera",car."IdCliente",car."IdPortafolio",ins."IdTipoInstrumento",car."IdInstrumento",car."Nemotecnico",
			CASE WHEN trim(coalesce(car."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(car."IdBoveda") END AS "IdBoveda"  ,
			car."PrecioMercado",car."PrecioOperacion" as "PrecioCompra",car."IdMonedaInstrumento",
			car."Cantidad" AS "Cantidad", 0 as "PrecioAPlazo","IdTipoOperacion"
		FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" car
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on car."IdInstrumento" = ins."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tins on tins."IdTipoInstrumento" = ins."IdTipoInstrumento"
		WHERE car."IdEstado" = 'VIG'
		  AND tins."IdClasificacionTipoInstrumento" = 'RV'
		  AND tins."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP')
	),
	v_Saldo_Plazo as (
		SELECT 'PLZ' AS "Tipo",
			C."FechaRespaldo",C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",
			CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(c."IdBoveda") END AS "IdBoveda",
			C."INGPrecioMercadoActual" AS "PrecioMercado", CASE C."IdTipoOperacion" WHEN 'VENSIM' THEN CRET."INGPrecioCompra" ELSE C."INGPrecioCompra" END as "PrecioCompra",C."IdMonedaInstrumento",
			S."INGCantidad" AS "Cantidad",
			C."INGPrecioAPlazo" as "PrecioAPlazo",C."IdTipoOperacion"
		FROM "BSCL.Custodia.BD::HIS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" and S."FechaRespaldo" = C."FechaRespaldo"
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as CRET on S."IdCarteraRetirada" = CRET."IdCartera" and S."FechaRespaldo" = CRET."FechaRespaldo"
		WHERE S."IdEstado" = 'VIG'
		  AND C."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('SIM')
		  AND C."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	),
	v_Saldo_Garantia as (
		SELECT 'GAR' AS "Tipo",
			G."FechaRespaldo",G."FolioGarantia" AS "FolioCartera",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",
			CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(C."IdBoveda") END AS "IdBoveda",
			c."INGPrecioMercadoActual" AS "PrecioMercado","PrecioCompraOperacionOrigen" as "PrecioCompra",G."IdMonedaInstrumento",
			G."INGCantidad" AS "Cantidad", 0 as "PrecioAPlazo","IdTipoOperacion"
		FROM "BSCL.Custodia.BD::HIS.Garantia" AS G
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" AS C ON C."IdCartera" = G."IdCarteraOrigen"
			AND G."FechaRespaldo" = C."FechaRespaldo"
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'RV'
		  AND G."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	),
	v_Saldo_Prestamo AS (
		SELECT 'PRT' AS "Tipo",
			C."FechaRespaldo",C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",
			CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(c."IdBoveda") END AS "IdBoveda",
			C."INGPrecioMercadoActual" aS "PrecioMercado",C."PrecioCompra" AS "PrecioCompra",C."IdMonedaInstrumento",
			S."INGCantidad" AS "Cantidad",C."INGPrecioAPlazo" as "PrecioAPlazo","IdTipoOperacion"
		FROM "BSCL.Custodia.BD::HIS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" AND S."FechaRespaldo" = C."FechaRespaldo"
		WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('PRT','EMP')
		  AND C."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	),
	v_Saldo_Transito AS (
		SELECT 'TRA' AS "Tipo" , "FechaRespaldo","FolioCustodia" AS "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",
				CASE WHEN trim(coalesce(t."IdBoveda",'')) = '' THEN NULL ELSE trim(t."IdBoveda") END AS "IdBoveda",
				0 AS "PrecioMercado","PrecioCompra","IdMonedaInstrumento",
				CASE "AccionCustodia"
					WHEN 'RETIRO' THEN "Cantidad" * -1
					WHEN 'INGRESO' THEN "Cantidad"
				END AS "Cantidad",0 as "PrecioAPlazo",t."IdTipoOperacion"
		FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS t 
		JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND ("AccionCustodia" = 'RETIRO' OR ("AccionCustodia" = 'INGRESO' AND "EsNominal" = 'S')))
		WHERE  "IdEstado"  = 'PEN'   AND "IdClasificacionTipoInstrumento" = 'RV'
	)
SELECT 
	LEFT("FechaRespaldo",7)  AS "COD_PER"
	,"FechaRespaldo"
	,CLI."IdCliente"
	,PER."DocumentoIdentidad"
	,SCUST."IdPortafolio"
	,POR."CodigoPortafolio"
	,CAST(SUM(SCUST."CantidadDisponible" + SCUST."CantidadTransito" - SCUST."CantidadNegativa") AS DECIMAL(14,2)) AS "CAN_CUSTODIA" --restar los transitos
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') AS "RUT_CLI"
	,CAST(POR."CodigoPortafolio" AS VARCHAR(100)) AS "SEC_RUT_CLI"
	,"FechaRespaldo" AS "FechaCustodia"
	,LEFT(CASE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") 
						WHEN   'OSA' 			THEN 'AC' 
						WHEN   'SIM' 			THEN 'AC'
						WHEN   'ETN' 			THEN 'EN'
						WHEN   'ETX' 			THEN 'ET'
						ELSE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") END,2) AS "MERCADO"
						
	,RPAD(SCUST."Nemotecnico",16) AS "NEMO"
	,RPAD(COALESCE(BOVEDA , SCUST."IdBoveda" ),3) AS "COD_BOVEDA"
	,CAST(SUM(SCUST."CantidadCompraPlazo") AS DECIMAL(14,2)) AS "CAN_COM_PLAZO"
	,CAST(SUM(SCUST."CantidadGarantia") AS DECIMAL(14,2)) AS "CAN_GARANTIA"
	,0 AS "CAN_LIBRE_01"
	,0 AS "CAN_LIBRE_02"
	,0 AS "CAN_LIBRE_03"
	,0 AS "CAN_TRAM_COM"
	,0 AS "CAN_TRAM_VEN"
	,CAST(SUM(SCUST."CantidadVentaPlazo") AS DECIMAL(14,2)) AS "CAN_VEN_PLAZO"
	,CAST(SUM(SCUST."PrecioCompra"*(SCUST."CantidadDisponible" + SCUST."CantidadTransito" - SCUST."CantidadNegativa" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo" + SCUST."CantidadEmprestito" - SCUST."CantidadPrestamo")) AS DECIMAL(17,2)) AS "MONTO_INI_AJUS"
	,CAST(SUM(SCUST."CantidadDisponible" + SCUST."CantidadTransito" - SCUST."CantidadNegativa" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo" + SCUST."CantidadEmprestito" - SCUST."CantidadPrestamo")*COALESCE(PRECIO_BCS."Precio", 0) AS DECIMAL(17,2)) AS "MONTO_VALORIZ"
	,CAST(COALESCE(PRECIO_BCS."Precio", 0) AS DECIMAL(15,4)) AS "PRECIO_CIERRE"
	,CAST(
	  ROUND(
	    CASE sum(SCUST."CantidadDisponible" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo" + SCUST."CantidadEmprestito") WHEN 0 THEN 0 ELSE
	    sum( SCUST."PrecioCompra"* ( SCUST."CantidadDisponible" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo" + SCUST."CantidadEmprestito" ) )
	    /sum(SCUST."CantidadDisponible" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo" + SCUST."CantidadEmprestito") END 
	   ,4,ROUND_HALF_UP)
	   AS DECIMAL(15,4))
	    AS "PRECIO_PROM"
	,CAST('' AS VARCHAR(50)) AS "COD_MANDATO"
	,CAST('' AS VARCHAR(50)) AS "COD_OBSERVAC" 
	,CAST(SUM(SCUST."CantidadEmprestito")AS DECIMAL(14,2)) AS "CAN_PRESTAMO"
	,CAST(SUM(SCUST."CantidadPrestamo")AS DECIMAL(14,2)) AS "CAN_VENTA_CORTA"
	,CASE SCUST."Nemotecnico"
		WHEN 'CFIBGCOA-E' THEN 'USD'
		WHEN 'CFIBTGGRA'  THEN 'USD'
		WHEN 'CFI-CAPBE'  THEN 'USD'
		WHEN 'CFIEPONR-E'	THEN 'EUR'
		WHEN 'CFIEPONS-E'	THEN 'EUR'
		WHEN 'CFIFYNRFPA'	THEN 'USD'
		WHEN 'CFIFYNSAEA'	THEN 'USD'
		WHEN 'CFIHCIVI-E'	THEN 'USD'
		WHEN 'CFIPIPOC-E'	THEN 'USD'
		WHEN 'CFIROSEB-E'	THEN 'USD'
		WHEN 'CFIFYNSAEB'	THEN 'USD'
		WHEN 'CFIROSEC-E'	THEN 'USD'
		WHEN 'CFISECMA-E'	THEN 'USD'
		WHEN 'CFISECOC-E'	THEN 'USD'
		ELSE CAST(MAX(SCUST."IdMonedaInstrumento") AS VARCHAR(3)) END AS "MON_MORIGEN"
	,SUM(SCUST."CantidadDisponible" + SCUST."CantidadTransito" - SCUST."CantidadNegativa" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo" + SCUST."CantidadEmprestito")*COALESCE(PRECIO_BCS."Precio", 0) AS "VALOR_MERCADO_MORIGE"
	,SUM(SCUST."CantidadNegativa") AS "CAN_NEGATIVA"
FROM (
	SELECT 	"FechaRespaldo",
			P."FolioCartera",
			P."IdCliente",
			P."IdPortafolio",
			P."IdTipoInstrumento",
			P."IdInstrumento",
			P."Nemotecnico",
			P."IdBoveda",
			P."PrecioMercado",
			P."IdMonedaInstrumento",
			COALESCE(P."CantidadDisponible",0) as "CantidadDisponible",
			COALESCE(P."CantidadTransito",0) as "CantidadTransito",
			COALESCE(P."CantidadCompraPlazo",0)    AS "CantidadCompraPlazo",
			COALESCE(P."CantidadVentaPlazo",0) AS "CantidadVentaPlazo",
			COALESCE(P."CantidadGarantia",0) AS "CantidadGarantia",
			COALESCE(P."CantidadPrestamo",0) AS "CantidadPrestamo",
			COALESCE(P."CantidadEmprestito",0) AS "CantidadEmprestito",
			COALESCE(P."CantidadNegativa",0) as "CantidadNegativa",
			"PrecioCompra"
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
		UNION ALL
		SELECT 
			"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"PrecioMercado","PrecioCompra","IdMonedaInstrumento","IdTipoOperacion","PrecioAPlazo",
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
			"PrecioCompra"*"Cantidad" AS "MontoNegativo"
		FROM v_Saldo_Cartera_Negativa
		UNION ALL
		SELECT 
			"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"PrecioMercado","PrecioCompra","IdMonedaInstrumento","IdTipoOperacion","PrecioAPlazo",
			0 AS "CantidadDisponible",
			0 AS "CantidadTransito",
			0 AS "CantidadNegativa",
			CASE "IdTipoOperacion" WHEN 'VENSIM' THEN "Cantidad" ELSE 0 END AS "CantidadCompraPlazo",
			CASE "IdTipoOperacion" WHEN 'COMSIM' THEN "Cantidad" ELSE 0 END AS "CantidadVentaPlazo",
			0 AS "CantidadPrestamo",
			0 AS "CantidadEmprestito",
			0 AS "CantidadGarantia",
			"PrecioAPlazo"*"Cantidad" AS "MontoPlazo",
			0 AS "MontoGarantia",
			0 AS "MontoNegativo"
		FROM v_Saldo_Plazo
		UNION ALL
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
		UNION ALL
		SELECT
			"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"PrecioMercado","PrecioCompra","IdMonedaInstrumento","IdTipoOperacion","PrecioAPlazo",
			0 AS "CantidadDisponible",
			0 AS "CantidadTransito",
			0 AS "CantidadNegativa",
			0 AS "CantidadCompraPlazo",
			0 AS "CantidadVentaPlazo",
			CASE "IdTipoOperacion" WHEN 'PRESTA' THEN "Cantidad" ELSE 0 END AS "CantidadPrestamo",
			CASE "IdTipoOperacion" WHEN 'EMPRES' THEN "Cantidad" ELSE 0 END AS "CantidadEmprestito",
			0 AS "CantidadGarantia",
			0 AS "MontoPlazo",
			0 AS "MontoGarantia",
			0 AS "MontoNegativo"
		FROM v_Saldo_Prestamo
		UNION ALL
		SELECT 
			"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"PrecioMercado","PrecioCompra","IdMonedaInstrumento","IdTipoOperacion","PrecioAPlazo",
			0 AS "CantidadDisponible",
			"Cantidad" AS "CantidadTransito",
			0 AS "CantidadNegativa",
			0 AS "CantidadCompraPlazo",
			0 AS "CantidadVentaPlazo",
			0 AS "CantidadPrestamo",
			0 AS "CantidadEmprestito",
			0 AS "CantidadGarantia",
			0 AS "MontoPlazo",
			0 AS "MontoGarantia",
			0 AS "MontoNegativo"
		FROM v_Saldo_Transito
		) AS P
) AS SCUST
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdCliente" = CLI."IdCliente"  AND SCUST."IdPortafolio" = por."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" AS PRECIO_BCS ON PRECIO_BCS."IdEmpresa" = 1 AND SCUST."IdInstrumento" = PRECIO_BCS."IdInstrumento"
and precio_bcs."FechaSistema" = fec."Valor"
LEFT JOIN (
	select top 1 f."Valor"
	from "BSCL"."BSCL.Plataforma.BD::MER.ParametroFinanciero" f
	left join "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion" conf on conf."CodigoParametro" = 'PLA.FechaSistema'
	where "IdParametroFinanciero" = 'DO' and "Fecha" <= conf."Valor"
	order by f."Fecha" desc
) AS dolar on 1 = 1
LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
                                                                   AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
                                                                   AND hpr."ValorDestino" = SCUST."IdTipoInstrumento" 
                                                                   AND hpr."ValorOrigen" NOT IN ('CM' , 'EN' , 'ET'))
cross JOIN (SELECT 'TOT' AS BOVEDA  FROM dummy UNION 
				SELECT null AS BOVEDA  FROM dummy 
								)
WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0
AND PER."DocumentoIdentidad" not like '%.%'			
AND SCUST."FechaRespaldo" = '2025-03-31'				
GROUP BY CLI."IdCliente",POR."CodigoPortafolio",PER."DocumentoIdentidad", PRECIO_BCS."Precio",
SCUST."IdPortafolio",
LEFT(CASE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") 
						WHEN   'OSA' 			THEN 'AC' 
						WHEN   'SIM' 			THEN 'AC'
						WHEN   'ETN' 			THEN 'EN'
						WHEN   'ETX' 			THEN 'ET'
						ELSE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") END,2) , 
						SCUST."Nemotecnico",COALESCE(BOVEDA , SCUST."IdBoveda" ) ,
						--SCUST."IdMonedaInstrumento", 
						dolar."Valor", 
						"FechaRespaldo"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTCURVHist" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
