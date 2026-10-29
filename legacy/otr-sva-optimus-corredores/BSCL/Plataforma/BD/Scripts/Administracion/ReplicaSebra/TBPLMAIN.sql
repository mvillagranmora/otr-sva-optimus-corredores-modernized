

select "FechaOperacion","FolioExternoReferencia",* 
from "BSCL.Replica.BD.Vistas::Sebra.TBCTTRTP" where COD_PER  = '2023-07'


1980696-0 33 andina-b

select * from TBPLMAIN t  

select * from "BSCL.Replica.BD.Vistas::Sebra.TBPLMAIN"

select * from TBCTTRTP t where COD_PER  = '2023-07'

and RUT_CLI = ' 96666010-4'

select * from "BSCL.Replica.BD.Vistas::Sebra.FMCarteraForward" brbvsff 

select PRECIO_PROM ,* 
from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURV" t where COD_PER = '2023-07'
and RUT_CLI = '  6609758-7' and NEMO  = 'VAPORES'

select PRECIO_PROM_COMP ,*
from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRV" t 
--where RUT_CLI = '  5884634-1' and NEMO  = 'CHILE'
where "IdPortafolio" = 8693


select cu.rut_cli,cu.SEC_RUT_CLI ,cu.nemo,cu.CAN_CUSTODIA ,ci.can_custodia,cu.precio_cierre,ci.precio_cierre,
cu.CAN_GARANTIA ,ci.can_garantia,cu.CAN_VEN_PLAZO ,ci.can_ven_plazo
from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURV" as cu
left join (	  
	select sum(can_actual) as can_custodia,sum(CAN_PRESTAMO) as can_prestamo,avg(PRECIO_PROM_calc) as precio_cierre ,
	sum(CAN_VEN_PLAZO) as CAN_VEN_PLAZO ,
	sum(CAN_GARANTIA) as CAN_GARANTIA,
	RUT_CLI ,SEC_RUT_CLI ,NEMO 
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRV" t where COD_PER = '2023-07'
	group by RUT_CLI ,SEC_RUT_CLI ,NEMO 
) as Ci on cu.rut_cli = ci.rut_cli and cu.sec_rut_cli = ci.sec_rut_cli and cu.NEMO  = ci.nemo
where cu.PRECIO_PROM  != ci.precio_cierre




WITH
	v_Saldo_Cartera as (
		SELECT 'CAR' AS "Tipo",
			"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",
			CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(c."IdBoveda") END AS "IdBoveda",
			"INGPrecioMercadoActual" AS "PrecioMercado","INGPrecioCompra" as "PrecioCompra","IdMonedaInstrumento",
			"INGCantidad" AS "Cantidad",0 as "PrecioAPlazo","IdTipoOperacion"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS  c
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'RV'
		  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
	),
	v_Saldo_Cartera_Negativa as (
		SELECT 'NEG' AS "Tipo",
			car."IdCarteraNegativa" AS "FolioCartera",car."IdCliente",car."IdPortafolio",ins."IdTipoInstrumento",car."IdInstrumento",car."Nemotecnico",
			CASE WHEN trim(coalesce(car."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(car."IdBoveda") END AS "IdBoveda"  ,
			car."PrecioMercado",car."PrecioOperacion" as "PrecioCompra",car."IdMonedaInstrumento",
			car."Cantidad" AS "Cantidad", 0 as "PrecioAPlazo","IdTipoOperacion"
		FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" car
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on car."IdInstrumento" = ins."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tins on tins."IdTipoInstrumento" = ins."IdTipoInstrumento"
		WHERE car."IdEstado" = 'VIG'
		  AND tins."IdClasificacionTipoInstrumento" = 'RV'
		  AND tins."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP')
	),
	v_Saldo_Plazo as (
		SELECT 'PLZ' AS "Tipo",
			C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",
			CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(c."IdBoveda") END AS "IdBoveda",
			C."INGPrecioMercadoActual" AS "PrecioMercado", CASE C."IdTipoOperacion" WHEN 'VENSIM' THEN CRET."INGPrecioCompra" ELSE C."INGPrecioCompra" END as "PrecioCompra",C."IdMonedaInstrumento",
			/*(CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
			(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC')  THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",*/
			S."INGCantidad" AS "Cantidad",
			C."INGPrecioAPlazo" as "PrecioAPlazo",C."IdTipoOperacion"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as CRET on S."IdCarteraRetirada" = CRET."IdCartera"
		WHERE S."IdEstado" = 'VIG'
		  AND C."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('SIM')
		  AND C."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	),
	v_Saldo_Garantia as (
		SELECT 'GAR' AS "Tipo",
			G."FolioGarantia" AS "FolioCartera",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",
			CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(C."IdBoveda") END AS "IdBoveda",
			c."INGPrecioMercadoActual" AS "PrecioMercado","PrecioCompraOperacionOrigen" as "PrecioCompra",G."IdMonedaInstrumento",
			G."INGCantidad" AS "Cantidad", 0 as "PrecioAPlazo","IdTipoOperacion"
		FROM "BSCL.Custodia.BD::GTA.Garantia" AS G
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS C ON C."IdCartera" = G."IdCarteraOrigen"
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'RV'
		  AND G."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	),
	v_Saldo_Prestamo AS (
		SELECT 'PRT' AS "Tipo",
			C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",
			CASE WHEN trim(coalesce(c."IdBoveda",'')) = '' THEN 'DCV' ELSE trim(c."IdBoveda") END AS "IdBoveda",
			C."INGPrecioMercadoActual" aS "PrecioMercado",C."PrecioCompra" AS "PrecioCompra",C."IdMonedaInstrumento",
			/*(CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
			(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC')  THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",*/
			S."INGCantidad" AS "Cantidad",C."INGPrecioAPlazo" as "PrecioAPlazo","IdTipoOperacion"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
		WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('PRT','EMP')
		  AND C."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	),
	v_Saldo_Transito AS (
		SELECT 'TRA' AS "Tipo" , "FolioCustodia" AS "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",
				CASE WHEN trim(coalesce(t."IdBoveda",'')) = '' THEN NULL ELSE trim(t."IdBoveda") END AS "IdBoveda",
				0 AS "PrecioMercado","PrecioCompra","IdMonedaInstrumento",
				"Cantidad" AS "Cantidad",0 as "PrecioAPlazo",t."IdTipoOperacion"
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS t 
		JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
		WHERE  "IdEstado"  = 'PEN'   AND "IdClasificacionTipoInstrumento" = 'RV'
	)
SELECT 
	sum( SCUST."PrecioCompra"* ( SCUST."CantidadDisponible" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo" + SCUST."CantidadEmprestito" ) )
	  /sum(SCUST."CantidadDisponible" + SCUST."CantidadGarantia" + SCUST."CantidadCompraPlazo" + SCUST."CantidadEmprestito") AS "PRECIO_PROM"
	,CLI."IdCliente"
	,PER."DocumentoIdentidad"
	,SCUST."IdPortafolio"
	,POR."CodigoPortafolio"
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad").RESULT,'0-0') AS "RUT_CLI"
	,CAST(POR."CodigoPortafolio" AS VARCHAR(100)) AS "SEC_RUT_CLI"
	,fec."Valor" AS "FechaCustodia"
	,LEFT(CASE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") 
						WHEN   'OSA' 			THEN 'AC' 
						WHEN   'SIM' 			THEN 'AC'
						WHEN   'ETN' 			THEN 'EN'
						WHEN   'ETX' 			THEN 'ET'
						ELSE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") END,2) AS "MERCADO"
	,CAST(SCUST."Nemotecnico" AS VARCHAR(40)) AS "NEMO"
	,COALESCE(BOVEDA , SCUST."IdBoveda" ) AS "COD_BOVEDA"
	,0 AS "CAN_LIBRE_01"
	,0 AS "CAN_LIBRE_02"
	,0 AS "CAN_LIBRE_03"
	,0 AS "CAN_TRAM_COM"
	,0 AS "CAN_TRAM_VEN"
	,COALESCE(PRECIO_BCS."Precio", 0) AS "PRECIO_CIERRE"
	,CAST('' AS VARCHAR(50)) AS "COD_MANDATO"
	,CAST('' AS VARCHAR(50)) AS "COD_OBSERVAC" 
	,CAST(SCUST."IdMonedaInstrumento" AS VARCHAR(3)) AS "MON_MORIGEN"
FROM (
	SELECT 	P."FolioCartera",
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
			"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
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
			"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
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
		UNION
		SELECT 
			"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
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
		UNION
		SELECT 
			"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
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
			"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
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
		UNION
		SELECT 
			"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
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
WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio").RESULT != 0
AND PER."DocumentoIdentidad" not like '%.%'							
and "DocumentoIdentidad" = '6609758-7' and "Nemotecnico" = 'VAPORES'
GROUP BY CLI."IdCliente",POR."CodigoPortafolio",PER."DocumentoIdentidad", PRECIO_BCS."Precio",
SCUST."IdPortafolio",fec."Valor",
LEFT(CASE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") 
						WHEN   'OSA' 			THEN 'AC' 
						WHEN   'SIM' 			THEN 'AC'
						WHEN   'ETN' 			THEN 'EN'
						WHEN   'ETX' 			THEN 'ET'
						ELSE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") END,2) , 
						LEFT(fec."Valor",7),SCUST."Nemotecnico",COALESCE(BOVEDA , SCUST."IdBoveda" ) ,
						SCUST."IdMonedaInstrumento", dolar."Valor"
