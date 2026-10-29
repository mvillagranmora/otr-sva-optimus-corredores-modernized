select *  
from OPTIMUS_TBPLTBRV ot 
where IND_SIMUL != ''

select *  
from "2022_05_31_TBPLTBRF" brbvst 

SELECT * FROM "2022_05_31_TBPLTBRF" t WHERE FECHA = '2022-05-31' ORDER BY FOLIO_TRANS  

SELECT * FROM OPTIMUS_TBPLTBRf t WHERE FECHA = '2022-05-31'  ORDER BY FOLIO_TRANS  

--FOLIO SIMUL NO COINCIDE TAN TODOS MALOS
--NO SE PUEDEN CUADRAR SIM POR AHORA

--la hora del fectrans no coincide -> campos FechaOperacion y HoraSistemaCreacion estan con hora local, debe estar con hora UTC
--no se carga marca de negociacion

--cantidad de palos no simultanea
SELECT *
FROM
(
	select fecha,count(1)   AS C
	from OPTIMUS_TBPLTBRf ot 
	group by fecha
) AS SEBRA
,
(
	select fecha,count(1)   AS C
	from "2022_05_31_TBPLTBRF"
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND (SEBRA.C != OPTIMUS.C)

--cuadre cantidad
SELECT *
FROM
(
	select fecha,sum(cantidad)   AS C
	from OPTIMUS_TBPLTBRF ot 
	group by fecha
) AS SEBRA
,
(
	select fecha,sum(cantidad)   AS C
	from "2022_05_31_TBPLTBRF"
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND (SEBRA.C != OPTIMUS.C)

--cuadre monto
SELECT *
FROM
(
	select fecha,sum(monto)   AS C
	from OPTIMUS_TBPLTBRF ot 
	group by fecha
) AS SEBRA
,
(
	select fecha,sum(monto)   AS C
	from "2022_05_31_TBPLTBRF"
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND (SEBRA.C != OPTIMUS.C)

--cuadre precio
SELECT *
FROM
(
	select fecha,sum(precio)   AS C
	from OPTIMUS_TBPLTBRF ot 
	group by fecha
) AS SEBRA
,
(
	select fecha,sum(precio)   AS C
	from "2022_05_31_TBPLTBRF"
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND abs(SEBRA.C - OPTIMUS.C) > 10

--cuadre tasa
SELECT *
FROM
(
	select fecha,sum(tir)   AS C
	from OPTIMUS_TBPLTBRF ot 
	group by fecha
) AS SEBRA
,
(
	select fecha,sum(tir)   AS C
	from "2022_05_31_TBPLTBRF"
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND abs(SEBRA.C - OPTIMUS.C) > 10

