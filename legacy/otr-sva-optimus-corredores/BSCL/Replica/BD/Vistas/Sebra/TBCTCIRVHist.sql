SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRVHist"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRVHist" AS

WITH
	v_Saldo_Cartera as (
		SELECT "FechaRespaldo","FolioCartera" AS "FolioCustodia","FechaOperacion","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento",
			   "Nemotecnico",CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(c."IdBoveda") END AS "IdBoveda",
			   "IdTipoReajuste","EsFungible",
			   "IdClasificacionTipoInstrumento","CodigoIFRS",
			   "IdTipoComprobante","FolioComprobante",
			   "INGCantidad" AS "Cantidad",
			   "INGPrecioCompra" as "PrecioCompra", "IdMonedaInstrumento","INGMontoATasaCompraActual" AS "MontoCompraAjus"
		FROM "BSCL.Custodia.BD::HIS.Cartera" c
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'RV'
		  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
	),
	v_Saldo_Cartera_Negativa as (
		SELECT 
			"FechaRespaldo","IdCarteraNegativa" AS "FolioCustodia","FechaOperacion","IdCliente","IdPortafolio",ins."IdTipoInstrumento",car."IdInstrumento"
			,car."Nemotecnico",CASE WHEN trim(car."IdBoveda") = '' THEN 'DCV' ELSE trim(car."IdBoveda") END AS "IdBoveda",
			tins."IdClasificacionTipoInstrumento",car."CodigoIFRS",
			car."IdTipoComprobante",car."FolioComprobante",'0001-01-01' AS "FechaComprobante",
			car."Cantidad"* -1 AS "Cantidad" ,car."MontoMercadoMonedaInstrumento"*-1 AS "Monto",
			car."PrecioOperacion" as "PrecioCompra",car."IdMonedaInstrumento",car."MontoOperacionMonedaInstrumento"*-1 AS "MontoCompraAjus"
		FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" car
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on car."IdInstrumento" = ins."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tins on tins."IdTipoInstrumento" = ins."IdTipoInstrumento"
		WHERE car."IdEstado" = 'VIG'
		  AND tins."IdClasificacionTipoInstrumento" = 'RV'
		  AND tins."IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
	),
	v_Saldo_Plazo as (
		SELECT S."FechaRespaldo",S."FolioSubyacente" AS "FolioCustodia",C."FechaOperacion",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",
			   S."Nemotecnico",S."NemoSVS",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END AS "IdBoveda",
			   S."IdClasificacionTipoInstrumento",C."CodigoIFRS",
			   C."IdTipoComprobante",C."FolioComprobante",C."FechaComprobante",
			    CASE WHEN C."IdTipoOperacion"  in ('VENSIM') THEN S."INGCantidad" ELSE 0 END AS "Cantidad_CompraPlazo",
				CASE WHEN C."IdTipoOperacion" in ('COMSIM')  THEN S."INGCantidad" ELSE 0 END AS "Cantidad_VentaPlazo",
			   c."INGMontoATasaCompraActual" AS "MontoCompraAjus",
			   CASE C."IdTipoOperacion" WHEN 'VENSIM' THEN CRET."INGPrecioCompra" ELSE C."INGPrecioCompra" END as "PrecioCompra",
			   c."IdMonedaInstrumento",C."FechaVencimientoOperacion"
		FROM "BSCL.Custodia.BD::HIS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" AND S."FechaRespaldo" = C."FechaRespaldo"
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as CRET on S."IdCarteraRetirada" = CRET."IdCartera"  AND S."FechaRespaldo" = CRET."FechaRespaldo"
		WHERE S."IdEstado" = 'VIG'
		 AND C."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('SIM')
		  AND C."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	),
	v_Saldo_Prestamo as (
		SELECT  S."FechaRespaldo",C."FolioCartera" AS "FolioCustodia",C."FechaOperacion",C."IdCliente",C."IdPortafolio",
				S."IdTipoInstrumento",S."IdInstrumento",
			   S."Nemotecnico",S."NemoSVS" AS "NemoSVS",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END AS "IdBoveda",
			   S."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento",C."CodigoIFRS" AS "CodigoIFRS",
			   C."IdTipoComprobante" AS "IdTipoComprobante",C."FolioComprobante" AS "FolioComprobante",
			   CASE WHEN C."IdTipoOperacion" in ('EMPRES') THEN S."INGCantidad" ELSE 0 END AS "Cantidad_Emprestito",
			   CASE WHEN C."IdTipoOperacion" in ('PRESTA') THEN S."INGCantidad" ELSE 0 END AS "Cantidad_Prestamo",
			   c."INGMontoATasaCompraActual" AS "MontoCompraAjus",
			   c."PrecioCompra" AS "PrecioCompra",			   
			   c."IdMonedaInstrumento" AS "IdMonedaInstrumento"
		FROM "BSCL.Custodia.BD::HIS.Subyacente" as S 
		LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" AND S."FechaRespaldo" = C."FechaRespaldo"
		WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('PRT','EMP')
		  AND C."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	),
	v_Saldo_Garantia as (
		SELECT 
			G."FechaRespaldo",G."FolioGarantia" AS "FolioCustodia",G."IdCliente",G."IdPortafolio", G."FechaOperacion",
			G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",
			g."NemoSVS",CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(C."IdBoveda") END AS "IdBoveda",
			"PrecioCompraOperacionOrigen" AS "INGPrecioPromedioCompra",G."IdMonedaInstrumento" AS "IdMonedaInstrumento",g."CodigoIFRS" AS "CodigoIFRS",
			G."INGCantidad" AS "Cantidad",
			 g."IdTipoComprobante" AS "IdTipoComprobante",g."FolioComprobante" AS "FolioComprobante",
			 g."MontoPrecioPromedioCompraOperacionOrigen" AS "MontoPrecioPromedioCompraOperacionOrigen"
		FROM "BSCL.Custodia.BD::HIS.Garantia" AS G
		LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS C ON C."IdCartera" = G."IdCarteraOrigen" AND G."FechaRespaldo" = C."FechaRespaldo"
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'RV'
		  AND G."IdTipoInstrumento" <> 'FFM'
		  AND G."INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
	),
	v_Saldo_Transito AS (
		SELECT "FechaRespaldo",-1*"IdMovimientoCarteraEnTransito" as "FolioCustodia","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",
			"FechaOperacion","CodigoIFRS","IdTipoComprobante","FolioComprobante",
				CASE WHEN trim(coalesce(t."IdBoveda",'')) = '' THEN NULL ELSE trim(t."IdBoveda") END AS "IdBoveda",
				"PrecioCompra",
				"IdMonedaInstrumento" AS "IdMonedaInstrumento",
				CASE "AccionCustodia"
					WHEN 'RETIRO' THEN "Cantidad" * -1
					WHEN 'INGRESO' THEN "Cantidad"
				END AS "Cantidad","MontoCompra" AS "MontoCompraAjus"
		FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS t 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o
				ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND ("AccionCustodia" = 'RETIRO' OR ("AccionCustodia" = 'INGRESO' AND "EsNominal" = 'S')))
		WHERE  "IdEstado"  = 'PEN'  AND "IdClasificacionTipoInstrumento" = 'RV'
	)
SELECT
	"FechaRespaldo"
	, SCUST."IdPortafolio"
	,LEFT(SCUST."FechaRespaldo",7) AS COD_PER								--char	7
	,SCUST."FolioCustodia" AS SEC_CAR_INVER													--decimal	0
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(IDENT."DocumentoIdentidad"),'0-0') AS RUT_CLI
	,POR."CodigoPortafolio" AS SEC_RUT_CLI													--decimal	0
	,CASE 		SCUST."IdTipoInstrumento"
						WHEN 'ACN' 			THEN 'AC'
						WHEN 'CFI' 			THEN 'CF'
						WHEN 'CFX' 			THEN 'CF'
						WHEN 'ACX' 			THEN 'AO'
						WHEN 'MIL' 			THEN 'ST'
						WHEN 'OSA' 			THEN 'AC' 
						WHEN 'ETN' 			THEN 'EN'
						WHEN 'ETX' 			THEN 'ET'
						WHEN 'MON' 			THEN 'MO'
						ELSE '  ' end AS "MERCADO"
	,CAST(SCUST."Nemotecnico" AS VARCHAR(16)) AS NEMO										--char	16 
	,CAST(COALESCE(SCUST."IdBoveda",'DCV') AS VARCHAR(3)) AS COD_BOVEDA										--char	3
	,SCUST."FechaOperacion" AS FEC_INVER													--char	10
	,SCUST."Cantidad_Cartera"  AS CAN_ACTUAL													--decimal	0
	,SCUST."Cantidad_Compra_Plazo" AS CAN_COM_PLAZO											--decimal	0
	,SCUST."Cantidad_Venta_Plazo" AS CAN_VEN_PLAZO											--decimal	0
	,SCUST."Cantidad_Garantia" AS CAN_GARANTIA												--decimal	0
	, 0 AS CAN_ORD_VEN																		--decimal	0
	, 0 AS CAN_TRAM_COM																		--decimal	0
	, 0 AS CAN_TRAM_VEN																		--decimal	0
	,'' AS COD_RUBRO																		--char	4
	,COALESCE("FechaVencimientoOperacion",'0001-01-01') AS FEC_VCTO_TP																		--char	10
	,SCUST."FolioComprobante" AS FOLIO_COMP_ADJ												--decimal	0
	, 0 AS INT_ACUM_GAN_CP																	--decimal	0
	, 0 AS INT_ACUM_GAN_VP																	--decimal	0
	, 0 AS LIN_COMP_ADJ																		--decimal	0
	, 0 AS LIN_ORDEN																		--decimal	0
	, SCUST."Cantidad_CarteraTotal"*PRECIO_BCS."Precio" AS MONTO_ACT_AJUS												--decimal	0
	, 0 AS MONTO_ACUM_SPRE																	--decimal	0
	, 0 AS MONTO_ACUM_VC																	--decimal	0
	, 0 AS MONTO_ACUM_VC_CE																	--decimal	0
	, 0 AS MONTO_ACUM_VC_CP																	--decimal	0
	, 0 AS MONTO_ACUM_VC_VE																	--decimal	0
	, 0 AS MONTO_ACUM_VEN																	--decimal	0
	,"MontoCompraAjus" AS MONTO_INI_AJUS																	--decimal	0
	,PRECIO_BCS."Precio"   AS PRECIO_CIERRE													--decimal	0
	, to_decimal(
	   ROUND(
	       SUM("CantidadProm" * "PrecioCompra") over (partition by IDENT."DocumentoIdentidad",POR."CodigoPortafolio",SCUST."Nemotecnico")	
           /SUM(CASE "CantidadProm" WHEN 0 THEN 1 ELSE "CantidadProm" END) over (partition by IDENT."DocumentoIdentidad",POR."CodigoPortafolio",SCUST."Nemotecnico") 
           ,4,ROUND_HALF_UP)
	    ,15,4)
				 AS PRECIO_PROM_COMP																--decimal	0
	, 0 AS SEC_ORDEN																		--decimal	0
	,'' AS TIPO_CARTERA																		--char	2
	,CASE SCUST."IdTipoComprobante" WHEN 'CUS' THEN 'IT' WHEN 'FAE' THEN 'FD' WHEN 'MES' THEN 'MA' ELSE 'IT' END AS TIPO_COMP_ADJ	--char	2
	,'' AS VTO_COMP_ADJ																		--char	10
	,'' AS ORIGEN_RECURSOS																	--char	1
	,'' AS CLASIF_CARTERA																	--char	1
	, to_decimal(
	   ROUND(
	       SUM("CantidadProm" * "PrecioCompra") over (partition by IDENT."DocumentoIdentidad",POR."CodigoPortafolio",SCUST."Nemotecnico")	
           /SUM(CASE "CantidadProm" WHEN 0 THEN 1 ELSE "CantidadProm" END) over (partition by IDENT."DocumentoIdentidad",POR."CodigoPortafolio",SCUST."Nemotecnico") 
           ,4,ROUND_HALF_UP)
	    ,15,4)
	   AS PRECIO_PROM_CALC																	--decimal	0
	,SCUST."Cantidad_Emprestito" AS CAN_PRESTAMO	--decimal	0
	,SCUST."Cantidad_Prestamo" AS CAN_VENTA_CORTA																	--decimal	0
	,CASE WHEN SCUST."CodigoIFRS" = 'CUS' THEN 1			
		  ELSE 0
	  END AS CODCATIFRSID																	--tinyint	0
	,"IdMonedaInstrumento"  AS MON_MORIGEN																		--char	3
	, 0 AS CAPTACION_MORIGEN																--decimal	0
	, 0 AS TCAMBIO_INICIO																	--decimal	0
	, 0 AS VALOR_INI_MORIGEN																--decimal	0
FROM (
	SELECT 	P."FolioCustodia",
			COALESCE(C."FechaRespaldo",Z."FechaRespaldo",G."FechaRespaldo",E."FechaRespaldo",CNEG."FechaRespaldo",T."FechaRespaldo") AS "FechaRespaldo",
			COALESCE(C."FechaOperacion",Z."FechaOperacion",G."FechaOperacion",E."FechaOperacion",CNEG."FechaOperacion",T."FechaOperacion") AS "FechaOperacion",
			COALESCE(C."IdCliente",Z."IdCliente",G."IdCliente",E."IdCliente",CNEG."IdCliente",T."IdCliente") AS "IdCliente",
			COALESCE(C."IdPortafolio",Z."IdPortafolio",G."IdPortafolio",E."IdPortafolio",CNEG."IdPortafolio",T."IdPortafolio") AS "IdPortafolio",
			COALESCE(C."IdTipoInstrumento",Z."IdTipoInstrumento",G."IdTipoInstrumento",E."IdTipoInstrumento",CNEG."IdTipoInstrumento",T."IdTipoInstrumento") AS "IdTipoInstrumento",
			COALESCE(C."IdInstrumento",Z."IdInstrumento",G."IdInstrumento",E."IdInstrumento",CNEG."IdInstrumento",T."IdInstrumento") AS "IdInstrumento",
			COALESCE(C."Nemotecnico",Z."Nemotecnico",G."Nemotecnico",E."Nemotecnico",CNEG."Nemotecnico",T."Nemotecnico") AS "Nemotecnico",
			COALESCE(C."IdBoveda",Z."IdBoveda",G."IdBoveda",E."IdBoveda",CNEG."IdBoveda",T."IdBoveda") AS "IdBoveda",
			Z."FechaVencimientoOperacion" AS "FechaVencimientoOperacion",
			COALESCE(C."CodigoIFRS",Z."CodigoIFRS",G."CodigoIFRS",E."CodigoIFRS",CNEG."CodigoIFRS",T."CodigoIFRS") AS "CodigoIFRS",
			COALESCE(C."IdTipoComprobante",Z."IdTipoComprobante",G."IdTipoComprobante",E."IdTipoComprobante",CNEG."IdTipoComprobante",T."IdTipoComprobante") AS "IdTipoComprobante",
			COALESCE(C."FolioComprobante",Z."FolioComprobante",G."FolioComprobante",E."FolioComprobante",CNEG."FolioComprobante",T."FolioComprobante") AS "FolioComprobante",
			(COALESCE(C."Cantidad",0) - COALESCE(CNEG."Cantidad",0) + COALESCE(T."Cantidad",0))   AS "Cantidad_Cartera",
			COALESCE(C."Cantidad",0) - COALESCE(CNEG."Cantidad",0) + COALESCE(T."Cantidad",0) + COALESCE(G."Cantidad",0) 
				+ COALESCE(Z."Cantidad_CompraPlazo",0) - COALESCE(Z."Cantidad_VentaPlazo",0) + COALESCE(E."Cantidad_Emprestito",0) - COALESCE(E."Cantidad_Prestamo",0) AS "Cantidad_CarteraTotal",
			COALESCE(Z."Cantidad_CompraPlazo",0) AS "Cantidad_Compra_Plazo",
			COALESCE(Z."Cantidad_VentaPlazo",0) AS "Cantidad_Venta_Plazo",
			COALESCE(G."Cantidad",0) AS "Cantidad_Garantia",
			COALESCE(G."Cantidad",0) + COALESCE(C."Cantidad",0) + COALESCE(Z."Cantidad_CompraPlazo",0) + COALESCE(E."Cantidad_Emprestito",0) AS "CantidadProm",
			COALESCE(E."Cantidad_Emprestito",0) AS "Cantidad_Emprestito",
			COALESCE(E."Cantidad_Prestamo",0) AS "Cantidad_Prestamo",
			COALESCE(c."MontoCompraAjus",Z."MontoCompraAjus",G."MontoPrecioPromedioCompraOperacionOrigen",E."MontoCompraAjus",CNEG."MontoCompraAjus",T."MontoCompraAjus",0) AS "MontoCompraAjus",
			COALESCE(c."PrecioCompra" ,Z."PrecioCompra",G."INGPrecioPromedioCompra",E."PrecioCompra",CNEG."PrecioCompra",T."PrecioCompra", 0) AS "PrecioCompra",
			COALESCE(c."IdMonedaInstrumento",Z."IdMonedaInstrumento",G."IdMonedaInstrumento",E."IdMonedaInstrumento",CNEG."IdMonedaInstrumento",T."IdMonedaInstrumento" , 'CLP') AS "IdMonedaInstrumento" 
	FROM (
		SELECT "FechaRespaldo", "Tipo","FolioCustodia"
		FROM (
			SELECT "FechaRespaldo", 'CAR' AS "Tipo","FolioCustodia" FROM v_Saldo_Cartera
				UNION
			SELECT "FechaRespaldo", 'NEG' AS "Tipo","FolioCustodia" FROM v_Saldo_Cartera_Negativa
				UNION
			SELECT "FechaRespaldo", 'PLZ' AS "Tipo","FolioCustodia" FROM v_Saldo_Plazo
				UNION
			SELECT "FechaRespaldo", 'PRT' AS "Tipo","FolioCustodia" FROM v_Saldo_Prestamo
				UNION
			SELECT "FechaRespaldo", 'GAR' AS "Tipo","FolioCustodia" FROM v_Saldo_Garantia
				UNION
			SELECT "FechaRespaldo", 'TRA' AS "Tipo","FolioCustodia" FROM v_Saldo_Transito
		) WHERE "FechaRespaldo" = '2025-03-31'
	) as P
	LEFT JOIN v_Saldo_Cartera as C ON  C."FolioCustodia" = P."FolioCustodia" AND C."FechaRespaldo" = P."FechaRespaldo"
	LEFT JOIN v_Saldo_Cartera_Negativa as CNEG ON CNEG."FolioCustodia" = P."FolioCustodia" AND CNEG."FechaRespaldo" = P."FechaRespaldo"
	LEFT JOIN v_Saldo_Plazo as Z ON Z."FolioCustodia" = P."FolioCustodia" AND Z."FechaRespaldo" = P."FechaRespaldo"
	LEFT JOIN v_Saldo_Prestamo as E ON E."FolioCustodia" = P."FolioCustodia" AND E."FechaRespaldo" = P."FechaRespaldo"	 	
	LEFT JOIN v_Saldo_Garantia as G ON G."FolioCustodia" = P."FolioCustodia" AND G."FechaRespaldo" = P."FechaRespaldo"
	LEFT JOIN v_Saldo_Transito as T ON T."FolioCustodia" = P."FolioCustodia" AND T."FechaRespaldo" = P."FechaRespaldo"
) AS SCUST
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" AS PRECIO_BCS ON PRECIO_BCS."IdEmpresa" = 1 AND SCUST."IdInstrumento" = PRECIO_BCS."IdInstrumento"
	and PRECIO_BCS."FechaSistema" = fec."Valor"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND SCUST."IdPortafolio" = por."IdPortafolio"
WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0
and "FechaRespaldo" = '2025-03-31'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRVHist" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
