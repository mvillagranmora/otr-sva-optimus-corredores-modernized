--COSAS VARIAS
--EN SEBRA LA BOVEDA VIENE COR, EN OPTIMUS SE CAMBIO A DCV

select * from "2022_05_31_TBCTCURF" t where COD_PER  = '2022-05' and COD_BOVEDA  = 'TOT' and RUT_CLI != 'OFICINA'
and nemo = 'BCFSA-J'

select * from OPTIMUS_TBCTCURF t where COD_PER  = '2022-05' and COD_BOVEDA  = 'TOT'

--TOTALES
select sum(CAN_CUSTODIA)
from "2022_05_31_TBCTCURF" t where COD_PER  = '2022-05' and COD_BOVEDA  = 'TOT' and RUT_CLI != 'OFICINA'

select sum(CAN_CUSTODIA)
from OPTIMUS_TBCTCURf t where COD_PER  = '2022-05'  and COD_BOVEDA  = 'TOT'

select distinct nemo from "2022_05_31_TBCTCURF" t where COD_PER  = '2022-05'

select distinct nemo from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURF" brbvst where COD_PER  = '2022-05'

--REGISTROS QUE SOBRAN EN OPTIMUS
SELECT * FROM 
"2022_05_31_TBCTCURF" SEBRA
LEFT JOIN  "OPTIMUS_TBCTCURF" as optimus on sebra.RUT_CLI  = optimus.RUT_CLI 
and sebra.SEC_RUT_CLI  = optimus.SEC_RUT_CLI 
where sebra.COD_PER  = '2022-05' and optimus.RUT_CLI  is null
and sebra.RUT_CLI  != 'OFICINA' and sebra.COD_BOVEDA  = 'TOT'

--ruts que faltan en optimus
SELECT * FROM 
"OPTIMUS_TBCTCURF" as optimus 
LEFT JOIN "2022_05_31_TBCTCURF" SEBRA on sebra.RUT_CLI  = optimus.RUT_CLI
and sebra.SEC_RUT_CLI  = optimus.SEC_RUT_CLI 
where sebra.COD_PER  = '2022-05' and optimus.RUT_CLI  is null
and sebra.RUT_CLI  != 'OFICINA'  and sebra.COD_BOVEDA  = 'TOT'


--POR RUT,CUENTA CUADRAMOS CAN CUSTODIA, CAN COM PLAZO
--OFICINA SE IGNORA POR EL LEFT JOIN WHERE
select sebra.rut_cli,sebra.sec_rut_cli,sebra.nemo,sebra.c,optimus.c as custodia,sebra.cplazo,optimus.cplazo
,sebra.gar,optimus.gar
from
(
	select RUT_CLI,sec_rut_cli,nemo,sum(CAN_CUSTODIA) as c,sum(t.CAN_COM_PLAZO) as cplazo,
	sum(t.CAN_VEN_PLAZO) as vplazo,
	sum(t.CAN_GARANTIA) as gar
	from "2022_05_31_TBCTCURF" t where COD_PER  = '2022-05'
	group by rut_cli,sec_rut_cli,nemo
) as sebra
,--LEFT JOIN
(
	select RUT_CLI,sec_rut_cli,nemo,sum(CAN_CUSTODIA) as c,sum(t.CAN_COM_PLAZO)  as cplazo,
	sum(t.CAN_VEN_PLAZO) as vplazo,
	sum(t.CAN_GARANTIA) as gar
	from "OPTIMUS_TBCTCURF" t where COD_PER  = '2022-05'
	group by rut_cli,sec_rut_cli,nemo
) AS OPTIMUS 
where sebra.rut_cli = optimus.rut_cli and sebra.sec_rut_cli = optimus.sec_rut_cli 
and  sebra.nemo = optimus.nemo and
(sebra.c != optimus.c 
or sebra.cplazo != optimus.cplazo
or sebra.vplazo != optimus.vplazo
or sebra.gar != optimus.gar)

--FIN CUADRE DE NOMINALES

--INICIO CUADRE DE PRECIOS MEDIOS CIERRE
SELECT distinct nemo,PRECIO_MED_CIE 
FROM "2022_05_31_TBCTCURF"
where RUT_CLI != 'OFICINA' and COD_PER  = '2022-05' and COD_BOVEDA  = 'TOT' and NEMO  = 'BSTDE90113'; --214

SELECT distinct nemo,PRECIO_MED_CIE 
FROM "OPTIMUS_TBCTCURF"  where COD_PER  = '2022-05' and COD_BOVEDA  = 'TOT' and NEMO  = 'BSTDE90113'; --214

select * from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURF" brbvst 
where "Nemotecnico"  = 'BSTDE90113' and COD_BOVEDA  = 'TOT'


select * from 
(
	SELECT rut_cli,SEC_RUT_CLI ,nemo,SUM(PRECIO_MED_CIE) as m
	FROM "2022_05_31_TBCTCURF"
	where RUT_CLI != 'OFICINA'
	and cod_boveda = 'TOT' and COD_PER  = '2022-05'
	group by rut_cli,sec_rut_cli,nemo
) as sebra
,
(
	SELECT rut_cli,SEC_RUT_CLI ,nemo,SUM(PRECIO_MED_CIE) as m
	FROM "OPTIMUS_TBCTCURF"
	where RUT_CLI != 'OFICINA'
	and cod_boveda = 'TOT'
	group by rut_cli,sec_rut_cli,nemo
) as optimus
where sebra.rut_cli = optimus.rut_cli and sebra.sec_rut_cli = optimus.sec_rut_cli 
and  sebra.nemo = optimus.nemo
and abs(sebra.m - optimus.m) >= 1; --4022
		
--OK, DESCUADRE ES POR DATA MALA EN SEBRA
--INICIO CUADRE PRECIOS PROMEDIO


--viene todo 0 en sebra

SELECT distinct rut_cli,SEC_RUT_CLI ,nemo,PRECIO_PROM
FROM "2022_05_31_TBCTCURF"
where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA' and COD_PER  = '2022-05'; --30002

SELECT  distinct rut_cli,SEC_RUT_CLI ,nemo,PRECIO_PROM
FROM "OPTIMUS_TBCTCURF"; --29937

SELECT *
FROM (
	SELECT DISTINCT rut_cli,SEC_RUT_CLI ,nemo,PRECIO_PROM 
	FROM "2022_05_31_TBCTCURF" where COD_PER  = '2022-05' and RUT_CLI = 'OFICINA' and nemo = 'BGASC-D') AS t 
LEFT JOIN 
(
	SELECT DISTINCT rut_cli,SEC_RUT_CLI ,nemo,PRECIO_PROM FROM OPTIMUS_TBCTCURF) as ot 
on ot.RUT_CLI = t.RUT_CLI  and ot.SEC_RUT_CLI = t.SEC_RUT_CLI and ot.NEMO  = t.NEMO 
where abs(ot.PRECIO_PROM - t.PRECIO_PROM) >= 1 --61 casos

-- FIN CUADRE PRECIOS PROMEDIO

--INICIO CUADRE MONTO_INI_AJUS 

--viene todo 0 en sebra
SELECT rut_cli,SEC_RUT_CLI ,nemo,MONTO_INI_AJUS 
FROM "2022_05_31_TBCTCURF"
where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA' and COD_PER  = '2022-05'; --30002


select * from 
(
	SELECT rut_cli,SEC_RUT_CLI ,nemo,SUM(MONTO_INI_AJUS) as m
	FROM "2022_05_31_TBCTCURF"
	where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA'
	and cod_boveda = 'TOT' and COD_PER  = '2022-05'
	group by rut_cli,sec_rut_cli,nemo
) as sebra
,
(
	SELECT rut_cli,SEC_RUT_CLI ,nemo,SUM(MONTO_INI_AJUS) as m
	FROM "OPTIMUS_TBCTCURF"
	where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA'
	and cod_boveda = 'TOT'
	group by rut_cli,sec_rut_cli,nemo
) as optimus
where sebra.rut_cli = optimus.rut_cli and sebra.sec_rut_cli = optimus.sec_rut_cli 
and  sebra.nemo = optimus.nemo
and abs(sebra.m - optimus.m) >= 40; --4022

--OK

--INICIO CUADRE MONTO VALORIZ XXXXXXXX

SELECT rut_cli,SEC_RUT_CLI ,nemo,MONTO_VALORIZ  
FROM "2022_05_31_TBCTCURF"
where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA' and COD_PER  = '2022-05'; --30002


select * from 
(
	SELECT rut_cli,SEC_RUT_CLI ,nemo,SUM(MONTO_VALORIZ) as m
	FROM "2022_05_31_TBCTCURF"
	where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA'
	and cod_boveda = 'TOT' and COD_PER  = '2022-05'
	group by rut_cli,sec_rut_cli,nemo
) as sebra
,
(
	SELECT rut_cli,SEC_RUT_CLI ,nemo,SUM(MONTO_VALORIZ) as m
	--FROM "OPTIMUS_TBCTCURF"
	FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCURF" brbvst 
	where NEMO != 'ORO 100*' and RUT_CLI != 'OFICINA'
	and cod_boveda = 'TOT'
	group by rut_cli,sec_rut_cli,nemo
) as optimus
where sebra.rut_cli = optimus.rut_cli and sebra.sec_rut_cli = optimus.sec_rut_cli 
and  sebra.nemo = optimus.nemo
and abs(sebra.m - optimus.m) >= 100; --4022

-- REGISTRO SIN VALORIZAR 97004000-5	5	BBNS-N0712
--DIFERENCIAS MENORES EN MONTO VALORIZ
-- FIN CUADRE MONTO VALORIZ

select * from "2022_05_31_TBCTCURF" t where COD_PER  = '2022-05' and RUT_CLI = ' 60706000-2' and SEC_RUT_CLI = 0 and NEMO =	'BTU0451023'

select * from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURF" t where COD_PER  = '2022-05' and RUT_CLI = ' 60706000-2' and SEC_RUT_CLI = 0 and NEMO =	'BTU0451023'



--EXTRAS
SELECT  distinct rut_cli,SEC_RUT_CLI ,nemo,PRECIO_PROM
FROM "OPTIMUS_TBCTCURV"; --214


select * from "2022_05_31_TBCTCURV" t 
where NEMO = 'AGUAS-A' AND RUT_CLI = ' 96564330-3'
and COD_PER  = '2022-05' and COD_BOVEDA = 'TOT'

select * from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURV" brbvst where NEMO = 'AGUAS-A'
and RUT_CLI = ' 96564330-3'

select distinct "Nemotecnico" from "BSCL.Plataforma.BD::INS.Instrumento" bpbii where "Nemotecnico"  = 'CENCOSHOPP'

   96564330-3	1	AGUAS-A	264183547.00

   