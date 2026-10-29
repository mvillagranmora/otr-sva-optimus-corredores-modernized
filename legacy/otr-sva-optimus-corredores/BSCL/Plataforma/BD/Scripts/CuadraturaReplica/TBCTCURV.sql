--COSAS VARIAS
--EN SEBRA LA BOVEDA VIENE COR, EN OPTIMUS SE CAMBIO A DCV

select * from "2022_05_31_TBCTCURV" t where COD_PER  = '2022-05' and NEMO  = 'NUEVAREG'

select * from OPTIMUS_TBCTCURV t where COD_PER  = '2022-05'

--TOTALES
select sum(CAN_CUSTODIA)
from "2022_05_31_TBCTCURV" t where COD_PER  = '2022-05'
group by rut_cli

select sum(CAN_CUSTODIA)
from OPTIMUS_TBCTCURV t where COD_PER  = '2022-05'
group by rut_cli

--REGISTROS QUE SOBRAN EN OPTIMUS
SELECT * FROM 
"2022_05_31_TBCTCURV" SEBRA
LEFT JOIN  "OPTIMUS_TBCTCURV" as optimus on sebra.RUT_CLI  = optimus.RUT_CLI 
and sebra.SEC_RUT_CLI  = optimus.SEC_RUT_CLI 
where sebra.COD_PER  = '2022-05' and optimus.RUT_CLI  is null
and sebra.RUT_CLI  != 'OFICINA'
-- 17626735-6 96549620-3

--registros que faltan en optimus
SELECT * FROM 
"OPTIMUS_TBCTCURV" as optimus 
LEFT JOIN "2022_05_31_TBCTCURV" SEBRA on sebra.RUT_CLI  = optimus.RUT_CLI
and sebra.SEC_RUT_CLI  = optimus.SEC_RUT_CLI 
where sebra.COD_PER  = '2022-05' and optimus.RUT_CLI  is null
and sebra.RUT_CLI  != 'OFICINA'
--OFICINA

--TOTA 11600 RUT,CUENTA VALIDOS

--POR RUT,CUENTA CUADRAMOS CAN CUSTODIA, CAN COM PLAZO
--OFICINA SE IGNORA POR EL LEFT JOIN WHERE
select sebra.rut_cli,sebra.sec_rut_cli,sebra.nemo,sebra.c,optimus.c as custodia,sebra.cplazo,optimus.cplazo
,sebra.gar,optimus.gar,SEBRA.VPLAZO,OPTIMUS.vplazo
from
(
	select RUT_CLI,sec_rut_cli,nemo,sum(CAN_CUSTODIA) as c,sum(t.CAN_COM_PLAZO) as cplazo,
	sum(t.CAN_VEN_PLAZO) as vplazo,
	sum(t.CAN_GARANTIA) as gar,
	sum(t.CAN_TRAM_VEN) as tramven
	from "2022_05_31_TBCTCURV" t where COD_PER  = '2022-05'
	group by rut_cli,sec_rut_cli,nemo
) as sebra
,--LEFT JOIN
(
	select RUT_CLI,sec_rut_cli,nemo,sum(CAN_CUSTODIA) as c,sum(t.CAN_COM_PLAZO)  as cplazo,
	sum(t.CAN_VEN_PLAZO) as vplazo,
	sum(t.CAN_GARANTIA) as gar,
	sum(t.CAN_TRAM_VEN) as tramven
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURV" t where COD_PER  = '2022-05'
	group by rut_cli,sec_rut_cli,nemo
) AS OPTIMUS 
where sebra.rut_cli = optimus.rut_cli and sebra.sec_rut_cli = optimus.sec_rut_cli 
and  sebra.nemo = optimus.nemo and
(sebra.c != optimus.c 
or sebra.cplazo != optimus.cplazo
or sebra.vplazo != optimus.vplazo
or sebra.gar != optimus.gar
or sebra.tramven != optimus.tramven)
 
select RUT_CLI ,nemo,sum(CAN_TRAM_VEN) as c,sum(t.CAN_COM_PLAZO) as plazo
from "2022_05_31_TBCTCURV" t where COD_PER  = '2022-05'
and RUT_CLI = '  5572510-1'
group by rut_cli,nemo
having sum(t.CAN_TRAM_VEN) > 0

select RUT_CLI ,nemo,sum(CAN_TRAM_VEN) as c,sum(t.CAN_COM_PLAZO) as plazo
from "OPTIMUS_TBCTCURV" t where COD_PER  = '2022-05'
and RUT_CLI = '  5572510-1'
group by rut_cli,nemo
having sum(t.CAN_TRAM_VEN) > 0

--FIN CUADRE DE NOMINALES

--INICIO CUADRE DE PRECIOS DE CIERRE
SELECT distinct nemo,PRECIO_CIERRE
FROM "2022_05_31_TBCTCURV"
where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA' and COD_PER  = '2022-05'; --214

SELECT distinct nemo,PRECIO_CIERRE
FROM "OPTIMUS_TBCTCURV"  where COD_PER  = '2022-05'; --214

SELECT *
FROM (SELECT DISTINCT NEMO, PRECIO_CIERRE FROM "2022_05_31_TBCTCURV" WHERE COD_PER  = '2022-05') AS t 
LEFT JOIN (SELECT DISTINCT NEMO, PRECIO_CIERRE FROM OPTIMUS_TBCTCURV) as ot on ot.NEMO  = t.NEMO 
where ot.PRECIO_CIERRE != t.PRECIO_CIERRE 

--PRECIOS OK

--INICIO CUADRE PRECIOS PROMEDIO

SELECT distinct rut_cli,SEC_RUT_CLI ,nemo,PRECIO_PROM
FROM "2022_05_31_TBCTCURV"
where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA' and COD_PER  = '2022-05'; --30002

SELECT  distinct rut_cli,SEC_RUT_CLI ,nemo,PRECIO_PROM
FROM "OPTIMUS_TBCTCURV"; --29937

SELECT *
FROM (SELECT DISTINCT rut_cli,SEC_RUT_CLI ,nemo,PRECIO_PROM FROM "2022_05_31_TBCTCURV" where COD_PER  = '2022-05') AS t 
LEFT JOIN (SELECT DISTINCT rut_cli,SEC_RUT_CLI ,nemo,PRECIO_PROM FROM OPTIMUS_TBCTCURV) as ot 
on ot.RUT_CLI = t.RUT_CLI  and ot.SEC_RUT_CLI = t.SEC_RUT_CLI and ot.NEMO  = t.NEMO 
where abs(ot.PRECIO_PROM - t.PRECIO_PROM) >= 1 --61 casos

--CUANDO ESTA TODO EN GARANTIA NO SABE COMO CALCULAR PRECIO PROMEDIO
-- VER CASO DE LA CORREDORA  96564330-3

-- CARTERAS SIN PRECIO COMPRA
select "Nemotecnico" ,* from "BSCL.Custodia.BD::CUS.Cartera" bcbcc where "IdClasificacionTipoInstrumento"  = 'RV'
and "IdEstado"  = 'VIG' and "INGPrecioCompra"  = 0

-- FIN CUADRE PRECIOS PROMEDIO

--INICIO CUADRE MONTO_INI_AJUS 

select * from 
(
	SELECT rut_cli,SEC_RUT_CLI ,nemo,SUM(MONTO_INI_AJUS) as m
	FROM "2022_05_31_TBCTCURV"
	where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA'
	and cod_boveda = 'TOT' and COD_PER  = '2022-05'
	group by rut_cli,sec_rut_cli,nemo
) as sebra
,
(
	SELECT rut_cli,SEC_RUT_CLI ,nemo,SUM(MONTO_INI_AJUS) as m
	FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCURV" brbvst
	where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA'
	and cod_boveda = 'TOT'
	group by rut_cli,sec_rut_cli,nemo
) as optimus
where sebra.rut_cli = optimus.rut_cli and sebra.sec_rut_cli = optimus.sec_rut_cli 
and  sebra.nemo = optimus.nemo
and abs(sebra.m - optimus.m) >= 40; --4022

--MONTO ENTREGADOS POR REPLICA ESTAN CORRECTOS RESPECTO A OPTIMUS

--INICIO CUADRE MONTO VALORIZ
select * from 
(
	SELECT rut_cli,SEC_RUT_CLI ,nemo,SUM(MONTO_VALORIZ) as m
	FROM "2022_05_31_TBCTCURV"
	where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA'
	and cod_boveda = 'TOT' and COD_PER  = '2022-05'
	group by rut_cli,sec_rut_cli,nemo
) as sebra
,
(
	SELECT rut_cli,SEC_RUT_CLI ,nemo,SUM(MONTO_VALORIZ) as m
	FROM  "BSCL.Replica.BD.Vistas::Sebra.TBCTCURV"
	where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA'
	and cod_boveda = 'TOT'
	group by rut_cli,sec_rut_cli,nemo
) as optimus
where sebra.rut_cli = optimus.rut_cli and sebra.sec_rut_cli = optimus.sec_rut_cli 
and  sebra.nemo = optimus.nemo
and abs(sebra.m - optimus.m) >= 40; --4022

--OK
-- FIN CUADRE MONTO VALORIZ

--EXTRAS
SELECT  distinct rut_cli,SEC_RUT_CLI ,nemo,PRECIO_PROM
FROM "OPTIMUS_TBCTCURV"; --214


select * from "2022_05_31_TBCTCURV" t 
where NEMO = 'AGUAS-A' AND RUT_CLI = ' 96564330-3'
and COD_PER  = '2022-05' and COD_BOVEDA = 'TOT'

select * from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURV" brbvst where NEMO = 'VAPORES'
and RUT_CLI = ' 96564330-3'

select distinct "Nemotecnico" from "BSCL.Plataforma.BD::INS.Instrumento" bpbii where "Nemotecnico"  = 'CENCOSHOPP'

   96564330-3	1	AGUAS-A	264183547.00

   
   
   
   
   
   
   
   
   
   
   
   
WITH
	v_Saldo_Cartera as (
		SELECT 
			"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END AS "IdBoveda",
			"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento",
			SUM("INGCantidad") AS "Cantidad"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS  c
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'RV'
		  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
		---  AND "INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
		GROUP BY 
			"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END ,
			"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
	),
	v_Saldo_Plazo as (
		SELECT 
			C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END AS "IdBoveda",
			C."INGMontoPrecioPromedioMonedaInstrumento",C."INGMontoMercadoActualMonedaInstrumento",C."INGPrecioMercadoActual",CASE C."INGPrecioPromedioCompra" WHEN 0 THEN NULL ELSE C."INGPrecioPromedioCompra" END AS "INGPrecioPromedioCompra",C."IdMonedaInstrumento",
			SUM(CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
			SUM(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC')  THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",
			SUM(S."INGMontoATasaSubyacenteActual") AS "Monto"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
		WHERE S."IdEstado" = 'VIG'
		  AND C."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('SIM','PAC')
		  AND C."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
		GROUP BY 
			C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END ,
			C."INGMontoPrecioPromedioMonedaInstrumento",C."INGMontoMercadoActualMonedaInstrumento",C."INGPrecioMercadoActual",C."INGPrecioPromedioCompra",C."IdMonedaInstrumento"
	),
	v_Saldo_Garantia as (
		SELECT 
			G."FolioCarteraOrigen" AS "FolioCartera",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico","IdBoveda",
			g."PrecioPromedioCompraOperacionOrigen" AS "INGMontoPrecioPromedioMonedaInstrumento","MontoPrecioPromedioCompraOperacionOrigen" AS "INGMontoMercadoActualMonedaInstrumento",
			c."INGPrecioMercadoActual" AS "INGPrecioMercadoActual","PrecioCompraOperacionOrigen" AS "INGPrecioPromedioCompra",G."IdMonedaInstrumento",
			SUM(G."INGCantidad") AS "Cantidad",SUM(G."INGMontoATasaGarantiaActual") AS "Monto"
		FROM "BSCL.Custodia.BD::GTA.Garantia" AS G
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS C ON C."IdCartera" = G."IdCarteraOrigen"
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'RV'
		  AND G."IdTipoInstrumento" <> 'FFM'
		  AND G."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
		GROUP BY 
			"FolioCarteraOrigen",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",C."IdBoveda",
			"PrecioCompraOperacionOrigen","MontoPrecioPromedioCompraOperacionOrigen",c."INGPrecioMercadoActual","PrecioPromedioCompraOperacionOrigen",G."IdMonedaInstrumento"
	),
	v_Saldo_Prestamo AS (
		SELECT 
			C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END AS "IdBoveda",
			C."INGMontoPrecioPromedioMonedaInstrumento",C."INGMontoMercadoActualMonedaInstrumento",C."INGPrecioMercadoActual",C."INGPrecioPromedioCompra",C."IdMonedaInstrumento",
			SUM(CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
			SUM(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC')  THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",
			SUM(S."INGMontoATasaSubyacenteActual") AS "Monto"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
		WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('PRT','EMP')
		  AND C."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
		GROUP BY 
			C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END,
			C."INGMontoPrecioPromedioMonedaInstrumento",C."INGMontoMercadoActualMonedaInstrumento",C."INGPrecioMercadoActual",C."INGPrecioPromedioCompra",C."IdMonedaInstrumento"
	),
	v_Saldo_Transito AS (
		SELECT "FolioCustodia" AS "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",'DCV' AS "IdBoveda",
				sum("MontoCompra") AS "INGMontoPrecioPromedioMonedaInstrumento",sum("MontoCompra") AS  "INGMontoMercadoActualMonedaInstrumento",min("PrecioCompra") AS "INGPrecioMercadoActual", min("PrecioCompra") AS "INGPrecioPromedioCompra", 
				max("IdMonedaInstrumento") AS "IdMonedaInstrumento",
				SUM("Cantidad") * - 1 AS "Cantidad",SUM("MontoCompra") AS "Monto"
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS t  JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o
				ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
		WHERE  "IdEstado"  = 'PEN'   AND "IdClasificacionTipoInstrumento" = 'RV'
		GROUP BY "FolioCustodia","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico"
	)
SELECT 
	LEFT(fec."Valor",7)  AS "COD_PER"
	,CLI."IdCliente"
	,PER."DocumentoIdentidad"
	,SCUST."IdPortafolio"
	,POR."CodigoPortafolio"
	,SUM(SCUST."Cantidad_Cartera") AS "CAN_CUSTODIA" --restar los transitos
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
	,SUM(SCUST."Cantidad_Compra_Plazo") AS "CAN_COM_PLAZO"
	,SUM(SCUST."Cantiddad_Garantia") AS "CAN_GARANTIA"
	,0 AS "CAN_LIBRE_01"
	,0 AS "CAN_LIBRE_02"
	,0 AS "CAN_LIBRE_03"
	,0 AS "CAN_TRAM_COM"
	,0 AS "CAN_TRAM_VEN"
	,SUM(SCUST."Cantidad_Venta_Plazo") AS "CAN_VEN_PLAZO"
	,SUM(SCUST."INGMontoPrecioPromedioMonedaInstrumento") AS "MONTO_INI_AJUS"
	,CASE SCUST."IdMonedaInstrumento"
		WHEN 'USD' THEN SUM(SCUST."Cantidad_Cartera")*COALESCE(PRECIO_BCS."Precio", SCUST."INGPrecioMercadoActual") * dolar."Valor"
		ELSE (SUM(SCUST."Cantidad_Cartera") + SUM(SCUST."Cantiddad_Garantia"))*COALESCE(PRECIO_BCS."Precio", SCUST."INGPrecioMercadoActual") 
		END AS "MONTO_VALORIZ"
	,COALESCE(PRECIO_BCS."Precio", SCUST."INGPrecioMercadoActual") AS "PRECIO_CIERRE"
	,(SCUST."INGPrecioPromedioCompra") AS "PRECIO_PROM"
	,CAST('' AS VARCHAR(50)) AS "COD_MANDATO"
	,CAST('' AS VARCHAR(50)) AS "COD_OBSERVAC" 
	,(SUM(SCUST."Cantidad_Compra_Prestamo") + SUM(SCUST."Cantidad_Venta_Prestamo")) AS "CAN_PRESTAMO"
	,0 AS "CAN_VENTA_CORTA"
	,CAST(SCUST."IdMonedaInstrumento" AS VARCHAR(3)) AS "MON_MORIGEN"
	,SUM(SCUST."INGMontoMercadoActualMonedaInstrumento") AS "VALOR_MERCADO_MORIGE"
FROM (
	SELECT 	P."FolioCartera",
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
			P."IdMonedaInstrumento",
			COALESCE(C."Cantidad",0) + COALESCE(T."Cantidad",0)  AS "Cantidad_Cartera",
			COALESCE(Z."Cantidad_Compra",0)    AS "Cantidad_Compra_Plazo",
			COALESCE(Z."Cantidad_Venta",0) AS "Cantidad_Venta_Plazo",
			COALESCE(Z."Monto",0) AS "Monto_Plazo",
			COALESCE(G."Cantidad",0) AS "Cantiddad_Garantia",
			COALESCE(G."Monto",0) AS "Monto_Garantia",
			COALESCE(E."Cantidad_Compra",0) AS "Cantidad_Compra_Prestamo",
			COALESCE(E."Cantidad_Venta",0) AS "Cantidad_Venta_Prestamo"
	FROM (
		SELECT DISTINCT 
			"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
		FROM (
			SELECT 
				"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
			FROM v_Saldo_Cartera
			UNION ALL
			SELECT 
				"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
			FROM v_Saldo_Plazo
			UNION ALL
			SELECT 
				"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
			FROM v_Saldo_Garantia
			UNION ALL
			SELECT
				"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
			FROM v_Saldo_Prestamo
			UNION ALL
			SELECT 
				"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
			FROM v_Saldo_Transito
		)
	) as P
	LEFT JOIN v_Saldo_Cartera as C 
			ON C."IdCliente" = P."IdCliente"
			AND C."FolioCartera" = P."FolioCartera"
			AND C."IdPortafolio" = P."IdPortafolio"
			AND C."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND C."IdInstrumento" = P."IdInstrumento"
			AND C."IdBoveda" = P."IdBoveda"
			AND C."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND C."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND C."INGPrecioMercadoActual" = P."INGPrecioMercadoActual"
			AND C."IdMonedaInstrumento" = P."IdMonedaInstrumento"
	LEFT JOIN v_Saldo_Plazo as Z
			ON Z."IdCliente" = P."IdCliente"
			AND Z."FolioCartera" = P."FolioCartera"
			AND Z."IdPortafolio" = P."IdPortafolio"
			AND Z."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND Z."IdInstrumento" = P."IdInstrumento"
			AND Z."IdBoveda" = P."IdBoveda"
			AND Z."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND Z."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND Z."INGPrecioMercadoActual" = P."INGPrecioMercadoActual"
			AND Z."IdMonedaInstrumento" = P."IdMonedaInstrumento"
	LEFT JOIN v_Saldo_Garantia as G 
			ON G."IdCliente" = P."IdCliente"
			AND G."FolioCartera" = P."FolioCartera"
			AND G."IdPortafolio" = P."IdPortafolio"
			AND G."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND G."IdInstrumento" = P."IdInstrumento"
			AND G."IdBoveda" = P."IdBoveda"
			AND G."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND G."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND G."INGPrecioMercadoActual" = P."INGPrecioMercadoActual"
			AND G."IdMonedaInstrumento" = P."IdMonedaInstrumento"
	LEFT JOIN v_Saldo_Prestamo as E 
			ON E."IdCliente" = P."IdCliente"
			AND E."FolioCartera" = P."FolioCartera"
			AND E."IdPortafolio" = P."IdPortafolio"
			AND E."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND E."IdInstrumento" = P."IdInstrumento"
			AND E."IdBoveda" = P."IdBoveda"
			AND E."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND E."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND E."INGPrecioMercadoActual" = P."INGPrecioMercadoActual"
			AND E."IdMonedaInstrumento" = P."IdMonedaInstrumento"
	LEFT JOIN v_Saldo_Transito as T
			ON P."IdCliente" = T."IdCliente"
			AND P."FolioCartera" = T."FolioCartera"
			AND P."IdPortafolio" = T."IdPortafolio"
			AND P."IdTipoInstrumento" = T."IdTipoInstrumento"
			AND P."IdInstrumento" = T."IdInstrumento"
			AND P."INGMontoPrecioPromedioMonedaInstrumento" = T."INGMontoPrecioPromedioMonedaInstrumento"
			AND P."INGMontoMercadoActualMonedaInstrumento" = T."INGMontoMercadoActualMonedaInstrumento"
			AND P."INGPrecioMercadoActual" = T."INGPrecioMercadoActual"
			AND P."IdMonedaInstrumento" = T."IdMonedaInstrumento"
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
cross JOIN (SELECT null AS BOVEDA  FROM dummy 
								)
WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio").RESULT != 0
AND PER."DocumentoIdentidad" not like '%.%'	 and SCUST."IdCliente" = 8670 AND SCUST."Nemotecnico" ='VAPORES'
GROUP BY "INGPrecioPromedioCompra",CLI."IdCliente",POR."CodigoPortafolio",PER."DocumentoIdentidad", COALESCE(PRECIO_BCS."Precio", SCUST."INGPrecioMercadoActual"),SCUST."IdPortafolio",fec."Valor",LEFT(CASE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") 
						WHEN   'OSA' 			THEN 'AC' 
						WHEN   'SIM' 			THEN 'AC'
						WHEN   'ETN' 			THEN 'EN'
						WHEN   'ETX' 			THEN 'ET'
						ELSE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") END,2) , LEFT(fec."Valor",7),PER."DocumentoIdentidad",POR."CodigoPortafolio", SCUST."Nemotecnico",COALESCE(BOVEDA , SCUST."IdBoveda" ) ,SCUST."IdMonedaInstrumento", dolar."Valor"

