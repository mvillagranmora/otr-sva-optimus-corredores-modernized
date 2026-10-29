SET SCHEMA "BSCL";
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTCIRV";

CREATE VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTCIRV" AS
WITH
	v_Saldo_Cartera as (
		SELECT "FolioCartera" AS "FolioCustodia","FechaOperacion","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","IdEmisor",
			   "Nemotecnico","NemoSVS","IdBoveda","IdTipoReajuste","PlazoEnDiasOperacion","EsFungible",
			   "IdClasificacionTipoInstrumento","CodigoIFRS","FechaEmisionInstrumento",
			   "IdTipoComprobante","FolioComprobante","FechaComprobante",
			   "INGCantidad" AS "Cantidad","INGMontoATasaCompraActual" AS "Monto","INGPrecioMercadoActual" AS "PrecioMercado"
		FROM "BSCL.Custodia.BD::CUS.Cartera"
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'RV'
		  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
	),
	v_Saldo_Plazo as (
		SELECT C."FolioCartera" AS "FolioCustodia",C."FechaOperacion",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",
			   S."IdEmisor",S."Nemotecnico",S."NemoSVS",C."IdBoveda",S."IdTipoReajuste",C."PlazoEnDiasOperacion",C."EsFungible",
			   S."IdClasificacionTipoInstrumento",C."CodigoIFRS",S."FechaEmisionInstrumento",
			   C."IdTipoComprobante",C."FolioComprobante",C."FechaComprobante",
			   SUM(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
			   SUM(CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",
			   SUM(S."INGMontoATasaSubyacenteActual") AS "Monto"
		FROM bscl."BSCL.Custodia.BD::CUS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
		WHERE S."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('SIM','PAC')
		GROUP BY C."FolioCartera",C."FechaOperacion",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",
			   	 S."IdEmisor",S."Nemotecnico",S."NemoSVS",C."IdBoveda",S."IdTipoReajuste",C."PlazoEnDiasOperacion",C."EsFungible",
			   	 S."IdClasificacionTipoInstrumento",C."CodigoIFRS",S."FechaEmisionInstrumento",
			   	 C."IdTipoComprobante",C."FolioComprobante",C."FechaComprobante"
	),
	v_Saldo_Prestamo as (
		SELECT C."FolioCartera" AS "FolioCustodia",C."FechaOperacion",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",
			   S."IdEmisor",S."Nemotecnico",S."NemoSVS",C."IdBoveda",S."IdTipoReajuste",C."PlazoEnDiasOperacion",C."EsFungible",
			   S."IdClasificacionTipoInstrumento",C."CodigoIFRS",S."FechaEmisionInstrumento",
			   C."IdTipoComprobante",C."FolioComprobante",C."FechaComprobante",
			   SUM(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
			   SUM(CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",
			   SUM(S."INGMontoATasaSubyacenteActual") AS "Monto"
		FROM bscl."BSCL.Custodia.BD::CUS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
		WHERE S."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('PRT','EMP')
		GROUP BY C."FolioCartera",C."FechaOperacion",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",
			   	 S."IdEmisor",S."Nemotecnico",S."NemoSVS",C."IdBoveda",S."IdTipoReajuste",C."PlazoEnDiasOperacion",C."EsFungible",
			   	 S."IdClasificacionTipoInstrumento",C."CodigoIFRS",S."FechaEmisionInstrumento",
			   	 C."IdTipoComprobante",C."FolioComprobante",C."FechaComprobante"
	),
	v_Saldo_Garantia as (
		SELECT C."FolioCartera" AS "FolioCustodia",C."FechaOperacion",C."IdCliente",C."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",
			   G."IdEmisor",G."Nemotecnico",G."NemoSVS",C."IdBoveda",G."IdTipoReajuste",0 AS "PlazoEnDiasOperacion",C."EsFungible",
			   G."IdClasificacionTipoInstrumento",C."CodigoIFRS",G."FechaEmisionInstrumento",
			   C."IdTipoComprobante",C."FolioComprobante",C."FechaComprobante",
			   SUM(G."INGCantidad") AS "Cantidad",SUM(G."INGMontoATasaGarantiaActual") AS "Monto"
		FROM "BSCL.Custodia.BD::GTA.Garantia" AS G
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS C ON C."IdCartera" = G."IdCarteraOrigen"
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'RV'
		  AND G."IdTipoInstrumento" <> 'FFM'
	    GROUP BY C."FolioCartera",C."FechaOperacion",C."IdCliente",C."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",
			   	 G."IdEmisor",G."Nemotecnico",G."NemoSVS",C."IdBoveda",G."IdTipoReajuste",C."EsFungible",
			   	 G."IdClasificacionTipoInstrumento",C."CodigoIFRS",G."FechaEmisionInstrumento",
			   	 C."IdTipoComprobante",C."FolioComprobante",C."FechaComprobante"
	)
SELECT
	 TO_VARCHAR(SCUST."FechaOperacion", 'YYYY-MM') AS COD_PER								--char	7
	,SCUST."FolioCustodia" AS SEC_CAR_INVER													--decimal	0
	,TRIM(PER."DocumentoIdentidad") AS RUT_CLI												--char	11
	,PORT."CodigoPortafolio" AS SEC_RUT_CLI													--decimal	0
	,CASE WHEN SCUST."IdTipoInstrumento" IN ('ACN','ACX') THEN 'AC'
		  WHEN SCUST."IdTipoInstrumento" IN ('CFI','CFN') THEN 'CF'
		  ELSE ''
	  END AS MERCADO																		--char	2
	,SCUST."Nemotecnico" AS NEMO															--char	16
	,SCUST."IdBoveda" AS COD_BOVEDA															--char	3
	,SCUST."FechaOperacion" AS FEC_INVER													--char	10
	,SCUST."Cantidad_Cartera" AS CAN_ACTUAL													--decimal	0
	,SCUST."Cantidad_Compra_Plazo" AS CAN_COM_PLAZO											--decimal	0
	,SCUST."Cantidad_Garantia" AS CAN_GARANTIA												--decimal	0
	, 0 AS CAN_ORD_VEN																		--decimal	0
	, 0 AS CAN_TRAM_COM																		--decimal	0
	, 0 AS CAN_TRAM_VEN																		--decimal	0
	,'' AS COD_RUBRO																		--char	4
	,'' AS FEC_VCTO_TP																		--char	10
	,SCUST."FolioComprobante" AS FOLIO_COMP_ADJ												--decimal	0
	, 0 AS INT_ACUM_GAN_CP																	--decimal	0
	, 0 AS INT_ACUM_GAN_VP																	--decimal	0
	, 0 AS LIN_COMP_ADJ																		--decimal	0
	, 0 AS LIN_ORDEN																		--decimal	0
	,SCUST."Monto_Cartera" AS MONTO_ACT_AJUS												--decimal	0
	, 0 AS MONTO_ACUM_SPRE																	--decimal	0
	, 0 AS MONTO_ACUM_VC																	--decimal	0
	, 0 AS MONTO_ACUM_VC_CE																	--decimal	0
	, 0 AS MONTO_ACUM_VC_CP																	--decimal	0
	, 0 AS MONTO_ACUM_VC_VE																	--decimal	0
	, 0 AS MONTO_ACUM_VEN																	--decimal	0
	, 0 AS MONTO_INI_AJUS																	--decimal	0
	,SCUST."PrecioMercado" AS PRECIO_CIERRE													--decimal	0
	, 0 AS PRECIO_PROM_COMP																	--decimal	0
	, 0 AS SEC_ORDEN																		--decimal	0
	,'' AS TIPO_CARTERA																		--char	2
	,SUBSTRING(SCUST."IdTipoComprobante",0,2) AS TIPO_COMP_ADJ								--char	2
	,'' AS VTO_COMP_ADJ																		--char	10
	,'' AS ORIGEN_RECURSOS																	--char	1
	,SCUST."IdClasificacionTipoInstrumento" AS CLASIF_CARTERA								--char	1
	, 0 AS PRECIO_PROM_CALC																	--decimal	0
	,(SCUST."Cantidad_Compra_Prestamo" + SCUST."Cantidad_Venta_Prestamo") AS CAN_PRESTAMO	--decimal	0
	, 0 AS CAN_VENTA_CORTA																	--decimal	0
	,CASE WHEN SCUST."CodigoIFRS" = 'CUS' THEN 1			
		  ELSE 0
	  END AS CODCATIFRSID																	--tinyint	0
	,'' AS MON_MORIGEN																		--char	3
	, 0 AS CAPTACION_MORIGEN																--decimal	0
	, 0 AS TCAMBIO_INICIO																	--decimal	0
	, 0 AS VALOR_INI_MORIGEN																--decimal	0
FROM (
	SELECT 	P."FolioCustodia",
			COALESCE(C."FechaOperacion",Z."FechaOperacion",G."FechaOperacion",E."FechaOperacion") AS "FechaOperacion",
			COALESCE(C."IdCliente",Z."IdCliente",G."IdCliente",E."IdCliente") AS "IdCliente",
			COALESCE(C."IdPortafolio",Z."IdPortafolio",G."IdPortafolio",E."IdPortafolio") AS "IdPortafolio",
			COALESCE(C."IdTipoInstrumento",Z."IdTipoInstrumento",G."IdTipoInstrumento",E."IdTipoInstrumento") AS "IdTipoInstrumento",
			COALESCE(C."IdInstrumento",Z."IdInstrumento",G."IdInstrumento",E."IdInstrumento") AS "IdInstrumento",
			COALESCE(C."IdEmisor",Z."IdEmisor",G."IdEmisor",E."IdEmisor") AS "IdEmisor",
			COALESCE(C."Nemotecnico",Z."Nemotecnico",G."Nemotecnico",E."Nemotecnico") AS "Nemotecnico",
			COALESCE(C."NemoSVS",Z."NemoSVS",G."NemoSVS",E."NemoSVS") AS "NemoSVS",
			COALESCE(C."IdBoveda",Z."IdBoveda",G."IdBoveda",E."IdBoveda") AS "IdBoveda",
			COALESCE(C."IdTipoReajuste",Z."IdTipoReajuste",G."IdTipoReajuste",E."IdTipoReajuste") AS "IdTipoReajuste",
			COALESCE(C."PlazoEnDiasOperacion",Z."PlazoEnDiasOperacion",G."PlazoEnDiasOperacion",E."PlazoEnDiasOperacion") AS "PlazoEnDiasOperacion",
			COALESCE(C."EsFungible",Z."EsFungible",G."EsFungible",E."EsFungible") AS "EsFungible",
			COALESCE(C."IdClasificacionTipoInstrumento",Z."IdClasificacionTipoInstrumento",G."IdClasificacionTipoInstrumento",E."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
			COALESCE(C."CodigoIFRS",Z."CodigoIFRS",G."CodigoIFRS",E."CodigoIFRS") AS "CodigoIFRS",
			COALESCE(C."FechaEmisionInstrumento",Z."FechaEmisionInstrumento",G."FechaEmisionInstrumento",E."FechaEmisionInstrumento") AS "FechaEmisionInstrumento",
			COALESCE(C."IdTipoComprobante",Z."IdTipoComprobante",G."IdTipoComprobante",E."IdTipoComprobante") AS "IdTipoComprobante",
			COALESCE(C."FolioComprobante",Z."FolioComprobante",G."FolioComprobante",E."FolioComprobante") AS "FolioComprobante",
			COALESCE(C."FechaComprobante",Z."FechaComprobante",G."FechaComprobante",E."FechaComprobante") AS "FechaComprobante",
			------------------------------------------------------------------------------------------------------------
			COALESCE(C."Cantidad",0) AS "Cantidad_Cartera",
			COALESCE(C."Monto",0) AS "Monto_Cartera",
			COALESCE(Z."Cantidad_Compra",0) AS "Cantidad_Compra_Plazo",
			COALESCE(Z."Cantidad_Venta",0) AS "Cantidad_Venta_Plazo",
			COALESCE(Z."Monto",0) AS "Monto_Plazo",
			COALESCE(G."Cantidad",0) AS "Cantidad_Garantia",
			COALESCE(G."Monto",0) AS "Monto_Garantia",
			COALESCE(Z."Cantidad_Compra",0) AS "Cantidad_Compra_Prestamo",
			COALESCE(Z."Cantidad_Venta",0) AS "Cantidad_Venta_Prestamo",
			COALESCE(Z."Cantidad_Venta",0) AS "Monto_Prestamo",
			C."PrecioMercado"
	FROM (
		SELECT DISTINCT "FolioCustodia"
		FROM (
			SELECT "FolioCustodia" FROM v_Saldo_Cartera
			UNION ALL
			SELECT "FolioCustodia" FROM v_Saldo_Plazo
			UNION ALL
			SELECT "FolioCustodia" FROM v_Saldo_Prestamo
			UNION ALL
			SELECT "FolioCustodia" FROM v_Saldo_Garantia
		)
	) as P
	LEFT JOIN v_Saldo_Cartera as C 
			ON  C."FolioCustodia" = P."FolioCustodia"
	LEFT JOIN v_Saldo_Plazo as Z
			ON Z."FolioCustodia" = P."FolioCustodia"
	LEFT JOIN v_Saldo_Prestamo as E
			ON Z."FolioCustodia" = P."FolioCustodia"		
	LEFT JOIN v_Saldo_Garantia as G 
			ON G."FolioCustodia" = P."FolioCustodia"
) AS SCUST
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON PORT."IdPortafolio" = SCUST."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
 LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = SCUST."IdEmisor"
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PEREMI ON PEREMI."IdPersona" = EMI."IdPersona"
WITH READ ONLY;


GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaTBCTCIRV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
