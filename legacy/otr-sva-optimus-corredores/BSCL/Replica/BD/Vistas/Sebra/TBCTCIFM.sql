SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCIFM"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCIFM" AS

WITH
	v_Saldo_Cartera as (
		SELECT 'CAR' AS "Tipo",
			"FolioCartera","IdCliente","IdPortafolio","FechaOperacion","IdTipoInstrumento","IdInstrumento","Nemotecnico",
			CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(c."IdBoveda") END AS "IdBoveda",
			"INGPrecioMercadoActual" AS "PrecioMercado","INGPrecioCompra" as "PrecioCompra","IdMonedaInstrumento",
			"INGCantidad" AS "Cantidad",0 as "PrecioAPlazo","IdTipoOperacion","FolioComprobante"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS  c
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'FM'
	),
	v_Saldo_Garantia as (
		SELECT 'GAR' AS "Tipo",
			G."FolioGarantia" AS "FolioCartera",G."IdCliente",G."IdPortafolio",G."FechaOperacion",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",
			CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(C."IdBoveda") END AS "IdBoveda",
			c."INGPrecioMercadoActual" AS "PrecioMercado","PrecioCompraOperacionOrigen" as "PrecioCompra",G."IdMonedaInstrumento",
			G."INGCantidad" AS "Cantidad", 0 as "PrecioAPlazo","IdTipoOperacion",C."FolioComprobante"
		FROM "BSCL.Custodia.BD::GTA.Garantia" AS G
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS C ON C."IdCartera" = G."IdCarteraOrigen"
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'FM'
		  AND G."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	)
SELECT

	LEFT(fec."Valor",7) AS COD_PER								--char	7
	,SCUST."FolioCartera" AS SEC_CAR_INVER													--decimal	0
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') AS RUT_CLI
	,POR."CodigoPortafolio" AS SEC_RUT_CLI													--decimal	0
	,
	
	CASE SCUST."Nemotecnico"
			WHEN 'CFMTOEEQU A' THEN 'CFMTOEEQUA'
			WHEN 'CFMCGACH D' THEN 'CFMCGACHD'
			WHEN 'CFMCGACH D' THEN 'CFMCGACHD'
			WHEN 'CFMCGDCH B' THEN 'CFMCGDCHB'
			ELSE CAST(SCUST."Nemotecnico" AS VARCHAR(16))
			 END AS NEMO										--char	16 
	,CAST(COALESCE(SCUST."IdBoveda",'DCV') AS VARCHAR(3)) AS COD_BOVEDA										--char	3
	,CAST(SCUST."FechaOperacion" AS CHAR(10)) AS FEC_INVER													--char	10
	,SCUST."CantidadCartera"  AS CAN_ACTUAL													--decimal	0
	,0 AS CAN_COM_PLAZO											--decimal	0
	,SCUST."CantidadGarantia" AS CAN_GARANTIA												--decimal	0
	, 0 AS CAN_ORD_VEN																		--decimal	0
	, 0 AS CAN_TRAM_COM																		--decimal	0
	, 0 AS CAN_TRAM_VEN																		--decimal	0
	,'' AS COD_RUBRO																		--char	4
	,'0001-01-01' AS FEC_VCTO_TP															--char	10
	,SCUST."FolioComprobante" AS FOLIO_COMP_ADJ												--decimal	0
	, 0 AS INT_ACUM_GAN_CP																	--decimal	0
	, 0 AS INT_ACUM_GAN_VP																	--decimal	0
	, 0 AS LIN_COMP_ADJ																		--decimal	0
	, 0 AS LIN_ORDEN																		--decimal	0
	, CASE WHEN "IdMonedaInstrumento" = 'USD' THEN (SCUST."CantidadGarantia" + SCUST."CantidadCartera")*COALESCE(PRECIO_BCS."Precio",0) * dolar."Valor"
	ELSE (SCUST."CantidadGarantia" + SCUST."CantidadCartera")*COALESCE(PRECIO_BCS."Precio",0) END AS MONTO_ACT_AJUS	--decimal	0
	, 0 AS MONTO_ACUM_SPRE																	--decimal	0
	, 0 AS MONTO_ACUM_VC																	--decimal	0
	, 0 AS MONTO_ACUM_VC_CE																	--decimal	0
	, 0 AS MONTO_ACUM_VC_CP																	--decimal	0
	, 0 AS MONTO_ACUM_VC_VE																	--decimal	0
	, 0 AS MONTO_ACUM_VEN																	--decimal	0
	, CASE WHEN "IdMonedaInstrumento" = 'USD' THEN (SCUST."CantidadGarantia" + SCUST."CantidadCartera")*"PrecioCompra" * dolar."Valor"
	ELSE (SCUST."CantidadGarantia" + SCUST."CantidadCartera")*"PrecioCompra" END AS MONTO_INI_AJUS																	--decimal	0
	,SCUST."PrecioMercado"   AS PRECIO_CIERRE													--decimal	0
	, to_decimal(
	   ROUND(
	       SUM((SCUST."CantidadGarantia" + SCUST."CantidadCartera") * "PrecioCompra") over (partition by SCUST."IdCliente",SCUST."IdPortafolio","Nemotecnico")	
           /SUM(SCUST."CantidadGarantia" + SCUST."CantidadCartera") over (partition by SCUST."IdCliente",SCUST."IdPortafolio","Nemotecnico") 
           ,4,ROUND_HALF_UP)
	    ,15,4)
				 AS PRECIO_PROM_COMP																	--decimal	0
	, 0 AS SEC_ORDEN																		--decimal	0
	,'' AS TIPO_CARTERA																		--char	2
	,'FM' AS TIPO_COMP_ADJ								--char	2
	,'' AS VTO_COMP_ADJ																		--char	10
FROM (
	SELECT 
			"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"PrecioMercado","PrecioCompra","IdMonedaInstrumento","IdTipoOperacion","PrecioAPlazo","FechaOperacion","FolioComprobante",
			"Cantidad" AS "CantidadCartera",
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
			"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"PrecioMercado","PrecioCompra","IdMonedaInstrumento","IdTipoOperacion","PrecioAPlazo","FechaOperacion","FolioComprobante",
			0 AS "CantidadCartera",
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

			
) AS SCUST
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND SCUST."IdPortafolio" = por."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" AS PRECIO_BCS ON PRECIO_BCS."IdEmpresa" = 1 AND SCUST."IdInstrumento" = PRECIO_BCS."IdInstrumento"
and precio_bcs."FechaSistema" = fec."Valor"
LEFT JOIN (
	select top 1 f."Valor"
	from "BSCL"."BSCL.Plataforma.BD::MER.ParametroFinanciero" f
	left join "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion" conf on conf."CodigoParametro" = 'PLA.FechaSistema'
	where "IdParametroFinanciero" = 'DO' and "Fecha" <= conf."Valor"
	order by f."Fecha" desc
) AS dolar on 1 = 1
WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTCIFM" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
