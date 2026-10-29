SET SCHEMA "BSCL";
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTCIIF";

CREATE VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTCIIF" AS
WITH
	v_Saldo_Cartera as (
		SELECT "FolioCartera" AS "FolioCustodia","FechaOperacion","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","IdEmisor",
			   "Nemotecnico","NemoSVS","IdBoveda","IdTipoReajuste","PlazoEnDiasOperacion","EsFungible",
			   "IdClasificacionTipoInstrumento","CodigoIFRS","FechaEmisionInstrumento",
			   "IdTipoComprobante","FolioComprobante","FechaComprobante",
			   "INGCantidad" AS "Cantidad","INGValorRescate" AS "ValorRescate","INGMontoATasaCompraActual" AS "Monto"
		FROM "BSCL.Custodia.BD::CUS.Cartera"
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'IF'
		  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
	),
	v_Saldo_Plazo as (
		SELECT C."FolioCartera" AS "FolioCustodia",C."FechaOperacion",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",
			   S."IdEmisor",S."Nemotecnico",S."NemoSVS",C."IdBoveda",S."IdTipoReajuste",C."PlazoEnDiasOperacion",C."EsFungible",
			   S."IdClasificacionTipoInstrumento",C."CodigoIFRS",S."FechaEmisionInstrumento",
			   C."IdTipoComprobante",C."FolioComprobante",C."FechaComprobante",
			   SUM(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
			   SUM(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC') THEN S."INGValorRescate" ELSE 0 END) AS "ValorRescate_Compra",
			   SUM(CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",
			   SUM(CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN S."INGValorRescate" ELSE 0 END) AS "ValorRescate_Venta",
			   SUM(S."INGMontoATasaSubyacenteActual") AS "Monto"
		FROM bscl."BSCL.Custodia.BD::CUS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
		WHERE S."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'IF'
		  AND C."IdTipoInstrumento" IN ('SIM','PAC')
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
			   SUM(G."INGCantidad") AS "Cantidad",SUM(G."INGValorRescate") AS "ValorRescate",
			   SUM(G."INGMontoATasaGarantiaActual") AS "Monto"
		FROM "BSCL.Custodia.BD::GTA.Garantia" AS G
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS C ON C."IdCartera" = G."IdCarteraOrigen"
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'IF'
		  AND G."IdTipoInstrumento" <> 'FFM'
	    GROUP BY C."FolioCartera",C."FechaOperacion",C."IdCliente",C."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",
			   	 G."IdEmisor",G."Nemotecnico",G."NemoSVS",C."IdBoveda",G."IdTipoReajuste",C."EsFungible",
			   	 G."IdClasificacionTipoInstrumento",C."CodigoIFRS",G."FechaEmisionInstrumento",
			   	 C."IdTipoComprobante",C."FolioComprobante",C."FechaComprobante"
	)
SELECT
	 TO_VARCHAR(SCUST."FechaOperacion", 'YYYY-MM') AS COD_PER
	,SCUST."FolioCustodia" AS SEC_CAR_INVER
	,TRIM(PER."DocumentoIdentidad") AS RUT_CLI
	,PORT."CodigoPortafolio" AS SEC_RUT_CLI
	,SCUST."IdEmisor" AS EMISOR
	,SCUST."IdTipoReajuste" AS TIPO_REAJ
	,'' AS FEC_VCTO
	,SCUST."Monto_Cartera" AS MONTO_VAL_DISP
	,'' AS TIPO_FAM
	,SCUST."Cantidad_Cartera" AS CAN_ACTUAL
	,SCUST."IdBoveda" AS COD_BOVEDA
	, 0 AS CAN_TRAMITE_COM
	,SCUST."Cantidad_Compra_Plazo" AS CAN_COM_PLAZO
	,SCUST."Cantidad_Garantia" AS CAN_GARANTIA
	, 0 AS CAN_ORD_VEN
	, 0 AS CAN_TRAMITE_VEN
	,'' AS COD_RESERVA
	,'' AS FEC_EMI
	,SCUST."FechaOperacion" AS FEC_INVER
	,'' AS FEC_RESERVA
	,'' AS FEC_VCTO_COM
	,SCUST."FolioComprobante" AS FOLIO_COMP_ADJ
	, 0 AS INT_ACUM_GAN_CP
	, 0 AS INT_ACUM_GAN_VP
	, 0 AS INT_DIA_INV_DIS
	, 0 AS LIN_COMP_ADJ
	,SCUST."Monto_Cartera" AS MONTO_ACT_AJUS
	, 0 AS MONTO_INI_AJUS
	, 0 AS MONTO_VAL_DISP_MAS
	, 0 AS MONTO_VAL_DISP_MEN
	,SCUST."Nemotecnico" AS NEMO
	,SCUST."PlazoEnDiasOperacion" AS PLAZO
	, 0 AS QUIEN_RESERVA
	, 0 AS TASA_MED_CIE
	, 0 AS TASA_PROM_COMP
	,'' AS TIPO_CARTERA
	,SUBSTRING(SCUST."IdTipoComprobante",0,2) AS TIPO_COMP_ADJ
	,SCUST."ValorRescate_Cartera" AS VAL_RESC
	,'' AS VTO_COMP_ADJ
	,SCUST."NemoSVS" AS NEMO_SVS
	,TRIM(PEREMI."DocumentoIdentidad") AS RUT_EMISOR
	, 0 AS SEC_CAR_HISTO
	, 0 AS SEC_CAR_ORIGEN
	,SCUST."Cantidad_Compra_Plazo" AS VAL_RESC_PLAZO
	,SCUST."EsFungible" AS IND_FUNGIBILIDAD
	, 0 AS TASA_EMISION
	, 0 AS SPREAD_TASA_EMISION
	,'' AS MODALIDAD_PAGO
	,'' AS PERIODICIDAD_PAGO
	,SCUST."FechaEmisionInstrumento" AS FECHA_EMISION
	,'' AS ORIGEN_RECURSOS
	,SCUST."IdClasificacionTipoInstrumento" AS CLASIF_CARTERA
	,SCUST."CodigoIFRS" AS CODCATIFRSID
	, 0 AS PARIDAD_MORIGEN
	, 0 AS CAPTACION_MORIGEN
	,'' AS MON_MORIGEN
	, 0 AS TASA_MED_CIE_ANT
	, 0 AS MTO_VAL_DISP_ANT
	, 0 AS CAPT_MORIGEN_MCDO
	, 0 AS VAL_RESC_DIF_ORIG
	, 0 AS MTO_VAL_TASA_MCDO
	, 0 AS MTO_VAL_TASA_COMP
FROM (
	SELECT 	P."FolioCustodia",
			COALESCE(C."FechaOperacion",Z."FechaOperacion",G."FechaOperacion") AS "FechaOperacion",
			COALESCE(C."IdCliente",Z."IdCliente",G."IdCliente") AS "IdCliente",
			COALESCE(C."IdPortafolio",Z."IdPortafolio",G."IdPortafolio") AS "IdPortafolio",
			COALESCE(C."IdTipoInstrumento",Z."IdTipoInstrumento",G."IdTipoInstrumento") AS "IdTipoInstrumento",
			COALESCE(C."IdInstrumento",Z."IdInstrumento",G."IdInstrumento") AS "IdInstrumento",
			COALESCE(C."IdEmisor",Z."IdEmisor",G."IdEmisor") AS "IdEmisor",
			COALESCE(C."Nemotecnico",Z."Nemotecnico",G."Nemotecnico") AS "Nemotecnico",
			COALESCE(C."NemoSVS",Z."NemoSVS",G."NemoSVS") AS "NemoSVS",
			COALESCE(C."IdBoveda",Z."IdBoveda",G."IdBoveda") AS "IdBoveda",
			COALESCE(C."IdTipoReajuste",Z."IdTipoReajuste",G."IdTipoReajuste") AS "IdTipoReajuste",
			COALESCE(C."PlazoEnDiasOperacion",Z."PlazoEnDiasOperacion",G."PlazoEnDiasOperacion") AS "PlazoEnDiasOperacion",
			COALESCE(C."EsFungible",Z."EsFungible",G."EsFungible") AS "EsFungible",
			COALESCE(C."IdClasificacionTipoInstrumento",Z."IdClasificacionTipoInstrumento",G."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
			COALESCE(C."CodigoIFRS",Z."CodigoIFRS",G."CodigoIFRS") AS "CodigoIFRS",
			COALESCE(C."FechaEmisionInstrumento",Z."FechaEmisionInstrumento",G."FechaEmisionInstrumento") AS "FechaEmisionInstrumento",
			COALESCE(C."IdTipoComprobante",Z."IdTipoComprobante",G."IdTipoComprobante") AS "IdTipoComprobante",
			COALESCE(C."FolioComprobante",Z."FolioComprobante",G."FolioComprobante") AS "FolioComprobante",
			COALESCE(C."FechaComprobante",Z."FechaComprobante",G."FechaComprobante") AS "FechaComprobante",
			------------------------------------------------------------------------------------------------------------
			COALESCE(C."Cantidad",0) AS "Cantidad_Cartera",
			COALESCE(C."ValorRescate",0) AS "ValorRescate_Cartera",
			COALESCE(C."Monto",0) AS "Monto_Cartera",
			COALESCE(Z."Cantidad_Compra",0) AS "Cantidad_Compra_Plazo",
			COALESCE(Z."ValorRescate_Compra",0) AS "ValorRescate_Compra_Plazo",
			COALESCE(Z."Cantidad_Venta",0) AS "Cantidad_Venta_Plazo",
			COALESCE(Z."ValorRescate_Venta",0) AS "ValorRescate_Venta_Plazo",
			COALESCE(Z."Monto",0) AS "Monto_Plazo",
			COALESCE(G."Cantidad",0) AS "Cantidad_Garantia",
			COALESCE(C."ValorRescate",0) AS "ValorRescate_Garantia",
			COALESCE(G."Monto",0) AS "Monto_Garantia"
	FROM (
		SELECT DISTINCT "FolioCustodia"
		FROM (
			SELECT "FolioCustodia" FROM v_Saldo_Cartera
			UNION ALL
			SELECT "FolioCustodia" FROM v_Saldo_Plazo
			UNION ALL
			SELECT "FolioCustodia" FROM v_Saldo_Garantia
		)
	) as P
	LEFT JOIN v_Saldo_Cartera as C 
			ON  C."FolioCustodia" = P."FolioCustodia"
	LEFT JOIN v_Saldo_Plazo as Z
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

GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaTBCTCIIF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
