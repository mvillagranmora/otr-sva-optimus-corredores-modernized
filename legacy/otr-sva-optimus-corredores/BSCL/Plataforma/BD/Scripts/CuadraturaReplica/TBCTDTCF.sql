--TBCTDTCF
--CONTEO DE FACTURAS POR RUT CUENTA
select sebra.rut_cli,optimus.rut_cli,sebra.sec_rut_cli,optimus.sec_rut_cli,c1,c2
from
(
	select RUT_CLI,sec_rut_cli,count(1) as c1
	FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTDTCF" DTCF
	INNER JOIN "BSCL.Replica.BD.Vistas::Sebra.TBCTMSCF" MSCF ON DTCF.FOLIO_COMP_FAC = MSCF.FOLIO_COMP_FAC and dtcf.TIPO_COMP_FAC  = mscf.TIPO_COMP_FAC 
	WHERE FEC_ING_COMP ='2022-05-31'
	group by RUT_CLI,sec_rut_cli
) as optimus
,--LEFT JOIN
(
	select RUT_CLI,sec_rut_cli,count(1) as c2
	FROM "2022_05_31_TBCTDTCF" DTCF
	INNER JOIN "2022_05_31_TBCTMSCF" MSCF ON DTCF.FOLIO_COMP_FAC = MSCF.FOLIO_COMP_FAC and dtcf.TIPO_COMP_FAC  = mscf.TIPO_COMP_FAC 
	WHERE FEC_ING_COMP ='2022-05-31'
	group by RUT_CLI,sec_rut_cli
) AS sebra
WHERE  sebra.sec_rut_cli = optimus.sec_rut_cli
AND sebra.rut_cli = optimus.rut_cli
and c1 != c2
	
--cuadre por nemo no IF 
SELECT  sebra.rut_cli,optimus.rut_cli,sebra.sec_rut_cli,optimus.sec_rut_cli,
sebra.NEMO AS NEMO_sebra ,optimus.NEMO AS NEMO_optimus
,sebra.CANTIDAD AS CANTIDAD_sebra ,optimus.CANTIDAD AS CANTIDAD_optimus
,sebra.MONTO AS MONTO_sebra ,optimus.MONTO AS MONTO_optimus
,sebra.PRECIO AS PRECIO_sebra ,optimus.PRECIO AS PRECIO_optimus
,sebra.TASA AS TASA_sebra ,optimus.TASA AS TASA_optimus
from
(
	SELECT MSCF.RUT_CLI,MSCF.sec_rut_cli,DTCF.NEMO,
	sum(DTCF.CANTIDAD) as CANTIDAD,
	sum(DTCF.MONTO) as MONTO,
	0 as Precio,
	0 as tasa
	--sum(DTCF.PRECIO) as PRECIO,
	--sum(DTCF.TASA) as TASA
	FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTDTCF" DTCF
	INNER JOIN "BSCL.Replica.BD.Vistas::Sebra.TBCTMSCF" MSCF ON DTCF.FOLIO_COMP_FAC = MSCF.FOLIO_COMP_FAC and dtcf.TIPO_COMP_FAC  = mscf.TIPO_COMP_FAC 
	WHERE MSCF.FEC_ING_COMP ='2022-05-31'  and val_resc = 0 and DTCF.tipo_comp_fac != 'NC'
	group by MSCF.RUT_CLI,MSCF.sec_rut_cli,nemo
) as optimus
,
(
	SELECT MSCF.RUT_CLI,MSCF.sec_rut_cli,DTCF.NEMO,
	sum(DTCF.CANTIDAD) as CANTIDAD,
	sum(DTCF.MONTO) as MONTO,
	0 as Precio,
	0 as tasa
	--sum(DTCF.PRECIO) as PRECIO,
	--sum(DTCF.TASA) as TASA
	FROM "2022_05_31_TBCTDTCF" DTCF
	INNER JOIN "2022_05_31_TBCTMSCF" MSCF ON DTCF.FOLIO_COMP_FAC = MSCF.FOLIO_COMP_FAC and dtcf.TIPO_COMP_FAC  = mscf.TIPO_COMP_FAC 
	WHERE MSCF.FEC_ING_COMP ='2022-05-31' and val_resc = 0  and DTCF.tipo_comp_fac != 'ND'
	group by MSCF.RUT_CLI,MSCF.sec_rut_cli,nemo
) AS sebra
WHERE  sebra.rut_cli = optimus.rut_cli
AND sebra.sec_rut_cli = optimus.sec_rut_cli
AND sebra.NEMO = optimus.NEMO
AND (
abs(sebra.CANTIDAD - optimus.CANTIDAD) > 1 or
abs(sebra.PRECIO - optimus.PRECIO) > 1 or
abs(sebra.TASA - optimus.TASA) > 1 or
abs(sebra.MONTO - optimus.MONTO) > 1
)

--FIN CUADRE NO IF


--INICIO CUADRE IF


SELECT  sebra.rut_cli,optimus.rut_cli,sebra.sec_rut_cli,optimus.sec_rut_cli,
sebra.NEMO AS NEMO_sebra ,optimus.NEMO AS NEMO_optimus
,sebra.VAL_RESC AS CANTIDAD_sebra ,optimus.VAL_RESC AS CANTIDAD_optimus
,sebra.MONTO AS MONTO_sebra ,optimus.MONTO AS MONTO_optimus
,sebra.PRECIO AS PRECIO_sebra ,optimus.PRECIO AS PRECIO_optimus
,sebra.TASA AS TASA_sebra ,optimus.TASA AS TASA_optimus
from
(
	SELECT MSCF.RUT_CLI,MSCF.sec_rut_cli,DTCF.NEMO,
	sum(DTCF.VAL_RESC) as VAL_RESC,
	sum(DTCF.MONTO) as MONTO,
	0 as Precio,
	0 as tasa
	--sum(DTCF.PRECIO) as PRECIO,
	--sum(DTCF.TASA) as TASA
	FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTDTCF" DTCF
	INNER JOIN "BSCL.Replica.BD.Vistas::Sebra.TBCTMSCF" MSCF ON DTCF.FOLIO_COMP_FAC = MSCF.FOLIO_COMP_FAC and dtcf.TIPO_COMP_FAC  = mscf.TIPO_COMP_FAC 
	WHERE MSCF.FEC_ING_COMP ='2022-05-31'  and val_resc > 0 and DTCF.tipo_comp_fac != 'NC'
	group by MSCF.RUT_CLI,MSCF.sec_rut_cli,nemo
) as optimus
,
(
	SELECT MSCF.RUT_CLI,MSCF.sec_rut_cli,DTCF.NEMO,
	sum(DTCF.CANTIDAD * DTCF.VAL_RESC) as VAL_RESC,    --EN SEBRA VAN LOS PAPELES EN LA CANTIDAD, EN OPTIMUS VAN LOS CORTES
	sum(DTCF.MONTO) as MONTO,
	0 as Precio,
	0 as tasa
	--sum(DTCF.PRECIO) as PRECIO,
	--sum(DTCF.TASA) as TASA
	FROM "2022_05_31_TBCTDTCF" DTCF
	INNER JOIN "2022_05_31_TBCTMSCF" MSCF ON DTCF.FOLIO_COMP_FAC = MSCF.FOLIO_COMP_FAC and dtcf.TIPO_COMP_FAC  = mscf.TIPO_COMP_FAC 
	WHERE MSCF.FEC_ING_COMP ='2022-05-31' and val_resc > 0  and DTCF.tipo_comp_fac != 'ND'
	group by MSCF.RUT_CLI,MSCF.sec_rut_cli,nemo
) AS sebra
WHERE  sebra.rut_cli = optimus.rut_cli
AND sebra.sec_rut_cli = optimus.sec_rut_cli
AND sebra.NEMO = optimus.NEMO
AND (
abs(sebra.VAL_RESC - optimus.VAL_RESC) > 1 or
abs(sebra.PRECIO - optimus.PRECIO) > 1 or
abs(sebra.TASA - optimus.TASA) > 1 or
abs(sebra.MONTO - optimus.MONTO) > 1
)

--DIFERENCIA DE 9 PESOS POR ASIGNACION ENTRE VARIOS CLIENTES
--FIN CUADRE IF



