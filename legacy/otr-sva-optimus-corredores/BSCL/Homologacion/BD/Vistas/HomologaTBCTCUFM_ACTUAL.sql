SET SCHEMA "BSCL";
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTCUFM_ACTUAL";

CREATE VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTCUFM_ACTUAL" AS
WITH
	v_Saldo_Cartera as (
		SELECT "IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			   SUM("INGCantidad") AS "Cantidad",SUM("INGMontoATasaCompraActual") AS "Monto",
			   SUM(CASE WHEN "IdTipoOperacion" = 'APORFM' THEN "INGMontoATasaCompraActual" ELSE 0 END) AS "MontoAporte",
			   SUM(CASE WHEN "IdTipoOperacion" = 'RESCFM' THEN "INGMontoATasaCompraActual" ELSE 0 END) AS "MontoRescate"
		FROM "BSCL.Custodia.BD::CUS.Cartera"
		WHERE "IdEstado" = 'VIG'
		  AND "IdTipoInstrumento" = 'FFM'
		  AND "INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD')
		GROUP BY "IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda"
	),
	v_Saldo_Garantia as (
		SELECT G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",C."IdBoveda",
			   SUM(G."INGCantidad") AS "Cantidad",SUM(G."INGMontoATasaGarantiaActual") AS "Monto",
			   0 AS "MontoAporte",0 AS "MontoRescate"
		FROM "BSCL.Custodia.BD::GTA.Garantia" AS G
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS C ON C."IdCartera" = G."IdCarteraOrigen"
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdTipoInstrumento" = 'FFM'
		  AND G."INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD')
		GROUP BY G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",C."IdBoveda"
	)
SELECT
	 TO_VARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY-MM') AS "COD_PER"
	,TRIM(PER."DocumentoIdentidad") AS "RUT_CLI"
	,PORT."CodigoPortafolio" AS "SEC_RUT_CLI"
	,SCUST."Nemotecnico" AS "NEMO"
	,SCUST."IdBoveda" AS "COD_BOVEDA"
	,SCUST."Cantidad_Cartera" AS "CAN_CUSTODIA"
	,SCUST."Cantiddad_Garantia" AS "CAN_GARANTIA"
	,SCUST."MontoAporte" AS "CAN_COM_PLAZO"
	,0 AS "CAN_LIBRE_01"
	,0 AS "CAN_LIBRE_02"
	,0 AS "CAN_LIBRE_03"
	,0 AS "CAN_TRAM_COM"
	,0 AS "CAN_TRAM_VEN"
	,SCUST."MontoRescate" AS "CAN_VEN_PLAZO"
	,0 AS "MONTO_INI_AJUS"
	,(SCUST."Monto_Cartera" + SCUST."Monto_Garantia") AS "MONTO_VALORIZ"
	,0 AS "PRECIO_CIERRE"
	,CASE WHEN (SCUST."Cantidad_Cartera" + SCUST."Cantiddad_Garantia") > 0 THEN
			((SCUST."Monto_Cartera" + SCUST."Monto_Garantia") / 
			  (SCUST."Cantidad_Cartera" + SCUST."Cantiddad_Garantia")
			 )
		  ELSE 0
	  END AS "PRECIO_PROM"
	,0 AS "CAN_TRAM_APORTE"
	,0 AS "CAN_TRAM_RESCATE"
FROM (
	SELECT 	P."IdCliente",
			P."IdPortafolio",
			P."IdTipoInstrumento",
			P."IdInstrumento",
			P."Nemotecnico",
			P."IdBoveda",
			COALESCE(C."Cantidad",0) AS "Cantidad_Cartera",
			COALESCE(C."Monto",0) AS "Monto_Cartera",
			COALESCE(C."MontoAporte",0) AS "MontoAporte",
			COALESCE(C."MontoRescate",0) AS "MontoRescate",
			COALESCE(G."Cantidad",0) AS "Cantiddad_Garantia",
			COALESCE(G."Monto",0) AS "Monto_Garantia"
	FROM (
		SELECT DISTINCT "IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda"
		FROM (
			SELECT "IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda"
			FROM v_Saldo_Cartera
			UNION ALL
			SELECT "IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda"
			FROM v_Saldo_Garantia
		)
	) as P
	LEFT JOIN v_Saldo_Cartera as C 
			ON C."IdCliente" = P."IdCliente" 
			AND C."IdPortafolio" = P."IdPortafolio"
			AND C."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND C."IdInstrumento" = P."IdInstrumento"
			AND C."IdBoveda" = P."IdBoveda"
	LEFT JOIN v_Saldo_Garantia as G 
			ON G."IdCliente" = P."IdCliente" 
			AND G."IdPortafolio" = P."IdPortafolio"
			AND G."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND G."IdInstrumento" = P."IdInstrumento"
			AND G."IdBoveda" = P."IdBoveda"
) AS SCUST
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON PORT."IdPortafolio" = SCUST."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaTBCTCUFM_ACTUAL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
