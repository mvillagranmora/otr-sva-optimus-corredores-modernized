SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCURFHist"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCURFHist" AS

WITH
	v_Saldo_Cartera as (
		SELECT 
			car."FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",
			CASE WHEN trim("IdBoveda") = '' THEN 'DCV' ELSE trim("IdBoveda") END AS "IdBoveda"  ,
			"INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado",
			"INGCantidad" - COALESCE(resTot."IngCantidad", 0) AS "Cantidad","INGMontoATasaCompraActual" AS "MontoCompra", 
			"INGMontoCompraActualMonedaInstrumento" AS "ValorCompra", 
			"INGPrecioMercadoActual" AS "PrecioMercado","INGPrecioCompra" AS "PrecioCompra",
			"INGTasaMercadoActual" AS "TasaMercado" ,"INGMontoATasaMercadoActual" AS "MontoMercado" , "INGTasaCompra" AS "TasaCompra"
		FROM "BSCL.Custodia.BD::HIS.Cartera" car
		LEFT JOIN (
	        SELECT res."FolioCarteraOrigen","FechaRespaldo"
	            ,SUM("IngCantidad") AS "IngCantidad"
	            ,SUM("IngValorRescate") AS "IngValorRescate"
	            ,SUM("MontoValorizado") AS "IngMontoValorizado"
	        FROM "BSCL.Custodia.BD::HIS.Reserva" AS res 
	        WHERE res."IdEstado"='VIG' AND res."EgrCantidad" = 0
	        GROUP BY "FechaRespaldo",res."FolioCarteraOrigen") AS resTot ON resTot."FolioCarteraOrigen" = car."FolioCartera" and
	        car."FechaRespaldo" = resTot."FechaRespaldo"
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'RF'
		  AND "IdTipoInstrumento" NOT IN ('PAC','PRF','ERF')
	),
	v_Saldo_Cartera_Negativa as (
		SELECT 
			CAR."FechaRespaldo","IdCarteraNegativa" AS "FolioCartera","IdCliente","IdPortafolio",ins."IdTipoInstrumento",car."IdInstrumento",car."Nemotecnico",
			CASE WHEN trim("IdBoveda") = '' THEN 'DCV' ELSE trim("IdBoveda") END AS "IdBoveda"  ,
			"MontoMercadoMonedaInstrumento" AS "MontoMercadoMonedaInstrumento",
			"Cantidad" AS "Cantidad",
			"MontoMercadoMonedaSistema" as "MontoMercado","MontoMercadoMonedaInstrumento" as "ValorMercado",
			"MontoOperacionMonedaInstrumento" AS "ValorCompra", "PrecioMercado" AS "PrecioMercado",
			"TasaMercado" AS "TasaMercado" , "TasaOperacion" AS "TasaCompra"
		FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" car
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on car."IdInstrumento" = ins."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tins on tins."IdTipoInstrumento" = ins."IdTipoInstrumento"
		WHERE car."IdEstado" = 'VIG'
		  AND tins."IdClasificacionTipoInstrumento" = 'RF'
		  AND "FechaOperacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	),
	v_Saldo_Plazo as (
		--CLIENTES
		SELECT 
			C."FechaRespaldo",C."FolioCartera",S."FolioSubyacente",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",
			CASE WHEN trim(c."IdBoveda") = '' THEN 'DCV' ELSE trim(c."IdBoveda") END AS "IdBoveda",
			C."INGMontoMercadoActualMonedaInstrumento",
			CASE WHEN C."IdTipoOperacion" in ('COMPAC') THEN S."INGPrecioSubyacente" ELSE 0 END AS "PrecioCompraPlazo",
			CASE WHEN C."IdTipoOperacion" in ('VENPAC') THEN S."INGPrecioSubyacente" ELSE 0 END AS "PrecioVentaPlazo",
			CASE WHEN C."IdTipoOperacion" in ('COMPAC') THEN S."INGTasaSubyacente" ELSE 0 END AS "TasaCompraPlazo",
			CASE WHEN C."IdTipoOperacion" in ('VENPAC') THEN S."INGTasaSubyacente" ELSE 0 END AS "TasaVentaPlazo",
			CASE WHEN C."IdTipoOperacion" in ('COMPAC') THEN S."INGCantidad" ELSE 0 END AS "CantidadCompra",
			CASE WHEN C."IdTipoOperacion" in ('VENPAC') THEN S."INGCantidad" ELSE 0 END AS "CantidadVenta",
			S."INGMontoATasaSubyacenteActual" AS "Monto"
			,0 AS "CantidadNominalCompra" 
		FROM "BSCL.Custodia.BD::HIS.Subyacente" as S
		LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" and S."FechaRespaldo" = C."FechaRespaldo"
		LEFT JOIN "BSCL.Custodia.BD::HIS.Reserva" RES ON RES."FolioSubyacente"= S."FolioSubyacente" and S."FechaRespaldo" = RES."FechaRespaldo"
		WHERE S."IdEstado" = 'VIG'
		  AND C."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RF'
		  AND C."IdTipoInstrumento" IN ('PAC')
		  AND C."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema') --
		UNION
		--CUENTA PROPIA
		SELECT 
			C."FechaRespaldo",C."FolioCartera",S."FolioSubyacente",C."IdClienteRelacionado",C."IdPortafolioRelacionado",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",
			CASE WHEN trim(c."IdBoveda") = '' THEN 'DCV' ELSE trim(c."IdBoveda") END AS "IdBoveda",
			C."INGMontoMercadoActualMonedaInstrumento",
			CASE WHEN C."IdTipoOperacion" in ('VENPAC') THEN S."INGPrecioSubyacente" ELSE 0 END AS "PrecioCompraPlazo",
			CASE WHEN C."IdTipoOperacion" in ('COMPAC') THEN S."INGPrecioSubyacente" ELSE 0 END AS "PrecioVentaPlazo",
			
			CASE WHEN C."IdTipoOperacion" in ('VENPAC') THEN S."INGTasaSubyacente" ELSE 0 END AS "TasaCompraPlazo",
			CASE WHEN C."IdTipoOperacion" in ('COMPAC') THEN S."INGTasaSubyacente" ELSE 0 END AS "TasaVentaPlazo",
			
			
			CASE WHEN C."IdTipoOperacion" in ('VENPAC') THEN S."INGCantidad" ELSE 0 END AS "CantidadCompra",
			CASE WHEN C."IdTipoOperacion" in ('COMPAC') THEN S."INGCantidad" ELSE 0 END AS "CantidadVenta",
			S."INGMontoATasaSubyacenteActual" AS "Monto"
			,CASE    				
   				WHEN C."IdTipoOperacion" in ('COMPAC') AND RES."FolioCarteraOrigen"= 0 THEN S."INGCantidad" * -1
   				ELSE 0
				 END	AS "CantidadNominalCompra" 
		FROM "BSCL.Custodia.BD::HIS.Subyacente" as S 
		LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" AND S."FechaRespaldo" = C."FechaRespaldo"
		LEFT JOIN "BSCL.Custodia.BD::HIS.Reserva" RES ON RES."FolioSubyacente"= S."FolioSubyacente"  AND S."FechaRespaldo" = RES."FechaRespaldo"
		WHERE S."IdEstado" = 'VIG'
		  AND C."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RF'
		  AND C."IdTipoInstrumento" IN ('PAC')
		  AND C."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	),
	v_Saldo_Garantia as (
		SELECT 
			G."FechaRespaldo",G."FolioCarteraOrigen" AS "FolioCartera",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico","IdBoveda",
			c."INGPrecioMercadoActual" AS "PrecioMercado","PrecioCompraOperacionOrigen" AS "PrecioCompra",G."IdMonedaInstrumento",
			G."INGCantidad" AS "Cantidad",G."INGMontoATasaGarantiaActual" AS "Monto",C."INGTasaCompra" AS "TasaCompra",
			G."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado",G."INGMontoATasaMercadoActual" AS "MontoMercado"
		FROM "BSCL.Custodia.BD::HIS.Garantia" AS G
		LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on G."IdCarteraOrigen" = C."IdCartera" and G."FechaRespaldo" = C."FechaRespaldo"
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'RF'
		  AND G."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	),
	v_Saldo_Transito AS (
		SELECT  t."FechaRespaldo",t."IdTipoOperacion","AccionCustodia","FolioCustodia" AS "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",
				CASE WHEN trim(t."IdBoveda") = '' THEN 'DCV' ELSE trim(t."IdBoveda") END AS "IdBoveda",
				"MontoCompra" AS "MontoCompra",
				"PrecioCompra" AS "PrecioCompra", 
				"IdMonedaSistema" AS "IdMonedaInstrumento",
				CASE 
				WHEN "IdCliente" NOT IN (
						SELECT SOC."IdCliente" AS "IdClienteCtaPropia" 
						FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP 
						INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC on (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio")
						INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR on (SOC."IdCliente" = POR."IdCliente")
						INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER on (SOC."IdPersona" = PER."IdPersona")
						INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" 
						WHERE EMP."IdEmpresa" = 1 GROUP BY SOC."IdCliente"
					)
				THEN 
					CASE 
					WHEN t."IdTipoOperacion" IN ('VENPAC','VENTER') THEN "Cantidad"
					WHEN t."IdTipoOperacion" IN ('COMPAC','COMTER') THEN "Cantidad" * - 1
					END
				ELSE 
					CASE 
					WHEN "AccionCustodia" ='INGRESO' THEN "Cantidad"
					WHEN "AccionCustodia" ='RETIRO' THEN "Cantidad" * - 1
					END
				END AS "Cantidad",
				"MontoCompra" AS "Monto"
 
		FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS t 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o ON (t."IdTipoOperacion" = o."IdTipoOperacion" )
		WHERE  "IdEstado"  = 'PEN' AND "IdClasificacionTipoInstrumento" = 'RF' AND "EsNominal" = 'S'
		AND T."FechaOperacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	)	
SELECT
	"FechaRespaldo",
	 LEFT("FechaRespaldo",7) AS "COD_PER"
	 ,CLI."IdCliente"
	 ,POR."IdPortafolio"
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(IDENT."DocumentoIdentidad"),'0-0') AS "RUT_CLI"
	,"DocumentoIdentidad"
	,"CodigoPortafolio"
	,"Nemotecnico"
	,TO_DATE("FechaRespaldo") AS "FechaCustodia"
	,CAST(POR."CodigoPortafolio" AS VARCHAR(100)) AS "SEC_RUT_CLI"
	,CAST(SCUST."Nemotecnico" AS VARCHAR(40)) AS "NEMO"
	,COALESCE(BOVEDA , SCUST."IdBoveda") AS "COD_BOVEDA"
	,SUM(SCUST."CantidadCompraPlazo") AS "CAN_COM_PLAZO"
	,SUM(SCUST."CantidadVentaPlazo") AS "CAN_VEN_PLAZO"
	,TO_DECIMAL(SUM(SCUST."Cantidad_CarteraDisponible" - "Cantidad_CarteraNegativa" + "CantidadTransito" + "CantidadNominalCompra"),14,2) AS "CAN_CUSTODIA"
	,sum("Cantidad_CarteraNegativa") as CAN_NEGATIVA
	,SUM(SCUST."CantidadGarantia") AS CAN_GARANTIA
	, 0 AS "CAN_LIBRE_01"
	, 0 AS "CAN_LIBRE_02"
	, 0 AS "CAN_LIBRE_03"
	, 0 AS "CAN_TRAMITE_COM"
	, 0 AS "CAN_TRAMITE_VEN"
	, TO_DECIMAL(SUM("ValorCompra_CarteraDisponible" - "ValorCompra_CarteraNegativa"),17,2) AS "MONTO_INI_AJUS"
	,TO_DECIMAL(SUM("MontoMercado_CarteraDisponible"-"MontoMercado_CarteraNegativa" + "MontoMercado_Garantia"),17,2) AS "MONTO_VALORIZ"
	--no esta considerando garantias
	,COALESCE(avg(CASE (SCUST."PrecioMercado_CarteraDisponible" + SCUST."PrecioMercado_Garantia") WHEN 0 THEN NULL ELSE (SCUST."PrecioMercado_CarteraDisponible" + SCUST."PrecioMercado_Garantia") END),0) AS "PRECIO_MED_CIE"
	, CASE WHEN SUM(SCUST."Cantidad_CarteraDisponible" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo") = 0 THEN 0
     	ELSE
	    ROUND(
	      sum( SCUST."PrecioCompra_CarteraDisponible" * SCUST."Cantidad_CarteraDisponible" + 
	         SCUST."PrecioCompra_Garantia" * SCUST."CantidadGarantia" + 
	         SCUST."PrecioCompra_CompraPlazo" * SCUST."CantidadCompraPlazo")
	      /sum(SCUST."Cantidad_CarteraDisponible" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo")
	   ,4,ROUND_HALF_UP)
	    END
	    AS "PRECIO_PROM"
	--no esta considerando garantias 
	,COALESCE(TO_DECIMAL(avg(CASE SCUST."TasaMercado_CarteraDisponible" WHEN 0 THEN NULL ELSE SCUST."TasaMercado_CarteraDisponible" END),10,6),0) AS "TIR_MED_CIE"
	,CASE WHEN SUM(SCUST."Cantidad_CarteraDisponible" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo") =0 THEN 0
	ELSE
	ROUND(
	    sum( SCUST."TasaCompra_CarteraDisponible" * SCUST."Cantidad_CarteraDisponible" + 
	         SCUST."TasaCompra_Garantia" * SCUST."CantidadGarantia" + 
	         SCUST."TasaCompra_CompraPlazo" * SCUST."CantidadCompraPlazo")
	    /sum(SCUST."Cantidad_CarteraDisponible" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo")
	   ,4,ROUND_HALF_UP) END AS "TIR_PROM"
FROM (
	SELECT 	P."FechaRespaldo",
			P."FolioCartera",
			P."IdCliente",
			P."IdPortafolio",
			P."IdInstrumento",
			P."Nemotecnico",
			P."IdBoveda",
			
			COALESCE(C."Cantidad",0) AS "Cantidad_CarteraDisponible",
			COALESCE(C."PrecioCompra",0) AS "PrecioCompra_CarteraDisponible",
			COALESCE(C."PrecioMercado",0) AS "PrecioMercado_CarteraDisponible",
			COALESCE(c."TasaMercado",0) AS "TasaMercado_CarteraDisponible",
			COALESCE(c."TasaCompra",0) AS "TasaCompra_CarteraDisponible",
			COALESCE(c."MontoCompra",0) AS "MontoCompra_CarteraDisponible",
			COALESCE(c."ValorCompra",0) AS "ValorCompra_CarteraDisponible",
			COALESCE(C."ValorMercado",0) as "ValorMercado_CarteraDisponible",
			COALESCE(C."MontoMercado",0) as "MontoMercado_CarteraDisponible",

			COALESCE(T."Cantidad",0) AS "CantidadTransito",
			
			COALESCE(Z."CantidadNominalCompra",0) AS "CantidadNominalCompra",

			COALESCE(Z."CantidadCompra",0) AS "CantidadCompraPlazo",
			COALESCE(Z."PrecioCompraPlazo",0) AS "PrecioCompra_CompraPlazo",
			COALESCE(Z."TasaCompraPlazo",0) AS "TasaCompra_CompraPlazo",
			COALESCE(Z."PrecioVentaPlazo",0) AS "PrecioVenta_VentaPlazo",
			COALESCE(Z."TasaVentaPlazo",0) AS "TasaVenta_VentaPlazo",
			COALESCE(Z."CantidadVenta",0) AS "CantidadVentaPlazo",
			COALESCE(Z."Monto",0) AS "MontoPlazo",

			COALESCE(G."Cantidad",0) AS "CantidadGarantia",
			COALESCE(G."Monto",0) AS "MontoGarantia",
			COALESCE(G."PrecioCompra",0) AS "PrecioCompra_Garantia",
			COALESCE(G."PrecioMercado",0) AS "PrecioMercado_Garantia",
			COALESCE(G."TasaCompra",0) AS "TasaCompra_Garantia",
			COALESCE(G."MontoMercado",0) AS "MontoMercado_Garantia",
			COALESCE(G."ValorMercado",0) AS "ValorMercado_Garantia",

			COALESCE(CNEG."Cantidad",0) as "Cantidad_CarteraNegativa",
			COALESCE(cneg."ValorCompra",0) AS "ValorCompra_CarteraNegativa",
			COALESCE(cneg."MontoMercado",0) AS "MontoMercado_CarteraNegativa",
			COALESCE(cneg."MontoMercadoMonedaInstrumento",0) AS "ValorMercado_CarteraNegativa"
			
	FROM (
		SELECT DISTINCT 
			"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdInstrumento","Nemotecnico","IdBoveda","IdTipoInstrumento"
		FROM (
			SELECT 
				"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdInstrumento","Nemotecnico","IdBoveda","IdTipoInstrumento"
			FROM v_Saldo_Cartera
			UNION
			SELECT 
				"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdInstrumento","Nemotecnico","IdBoveda","IdTipoInstrumento"
			FROM v_Saldo_Cartera_Negativa
			UNION
			SELECT 
				"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdInstrumento","Nemotecnico","IdBoveda","IdTipoInstrumento"
			FROM v_Saldo_Plazo
			UNION
			SELECT 
				"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdInstrumento","Nemotecnico","IdBoveda","IdTipoInstrumento"
			FROM v_Saldo_Garantia
				UNION
			SELECT 
				"FechaRespaldo","FolioCartera","IdCliente","IdPortafolio","IdInstrumento","Nemotecnico","IdBoveda","IdTipoInstrumento"
			FROM v_Saldo_Transito
		)
	) as P
	LEFT JOIN v_Saldo_Cartera as C 
			ON C."IdCliente" = P."IdCliente"
			AND C."FolioCartera" = P."FolioCartera"
			AND C."FechaRespaldo" = P."FechaRespaldo"
			AND C."IdPortafolio" = P."IdPortafolio"
			AND C."IdInstrumento" = P."IdInstrumento"
	LEFT JOIN v_Saldo_Cartera_Negativa as CNEG 
			ON CNEG."IdCliente" = P."IdCliente"
			AND CNEG."FechaRespaldo" = P."FechaRespaldo"
			AND CNEG."FolioCartera" = P."FolioCartera"
			AND CNEG."IdPortafolio" = P."IdPortafolio"
			AND CNEG."IdInstrumento" = P."IdInstrumento"
	LEFT JOIN v_Saldo_Plazo as Z
			ON Z."IdCliente" = P."IdCliente" 
			AND Z."FechaRespaldo" = P."FechaRespaldo"
			AND Z."FolioCartera" = P."FolioCartera"
			AND Z."IdPortafolio" = P."IdPortafolio"
			AND Z."IdInstrumento" = P."IdInstrumento"
	LEFT JOIN v_Saldo_Garantia as G 
			ON G."IdCliente" = P."IdCliente"
			AND G."FechaRespaldo" = P."FechaRespaldo"
			AND G."FolioCartera" = P."FolioCartera"
			AND G."IdPortafolio" = P."IdPortafolio"
			AND G."IdInstrumento" = P."IdInstrumento"
	LEFT JOIN v_Saldo_Transito as T
			ON P."IdCliente" = T."IdCliente"
			AND P."FechaRespaldo" = T."FechaRespaldo"
			AND P."FolioCartera" = T."FolioCartera"
			AND P."IdPortafolio" = T."IdPortafolio"
			AND P."IdInstrumento" = T."IdInstrumento"
) AS SCUST
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdPersona" = IDENT."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON SCUST."IdPortafolio" = por."IdPortafolio"
cross JOIN (SELECT 'TOT' AS BOVEDA  FROM dummy UNION 
				SELECT null AS BOVEDA  FROM dummy)
WHERE SCUST."FechaRespaldo" = '2025-03-31'
GROUP BY CLI."IdCliente", IDENT."DocumentoIdentidad","FechaRespaldo",
		POR."IdPortafolio",POR."CodigoPortafolio",SCUST."Nemotecnico",COALESCE(BOVEDA , SCUST."IdBoveda")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTCURFHist" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
