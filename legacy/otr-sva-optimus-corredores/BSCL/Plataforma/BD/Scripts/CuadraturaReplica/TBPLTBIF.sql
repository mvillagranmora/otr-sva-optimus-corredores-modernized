select *  
from OPTIMUS_TBPLTBIF ot 


select *  
from "2022_05_31_TBPLTBIF" brbvst 

SELECT * FROM "2022_05_31_TBPLTBIF" t WHERE FECHA = '2022-05-31' ORDER BY FOLIO_TRANS  

SELECT * FROM OPTIMUS_TBPLTBIf t WHERE FECHA = '2022-05-31'  ORDER BY FOLIO_TRANS  

--FOLIO SIMUL NO COINCIDE TAN TODOS MALOS
--NO SE PUEDEN CUADRAR SIM POR AHORA

--la hora del fectrans no coincide -> campos FechaOperacion y HoraSistemaCreacion estan con hora local, debe estar con hora UTC
--no se carga marca de negociacion

monto
valresc
plazo
paridad_morigen
captacion_morigen
mon_morigen

--cantidad de palos
SELECT *
FROM
(
	select fecha,count(1)   AS C
	from OPTIMUS_TBPLTBIF ot 
	group by fecha
) AS SEBRA
,
(
	select fecha,count(1)   AS C
	from "2022_05_31_TBPLTBIF"
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND (SEBRA.C != OPTIMUS.C)

--cuadre monto
SELECT *
FROM
(
	select fecha,sum(monto)   AS C
	from OPTIMUS_TBPLTBIF ot 
	group by fecha
) AS SEBRA
,
(
	select fecha,sum(monto)   AS C
	from "2022_05_31_TBPLTBIF"
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND (SEBRA.C != OPTIMUS.C)

--cuadre captacion_morigen
SELECT *
FROM
(
	select fecha,sum(captacion_morigen)   AS C
	from OPTIMUS_TBPLTBIF ot 
	group by fecha
) AS SEBRA
,
(
	select fecha,sum(captacion_morigen)   AS C
	from "2022_05_31_TBPLTBIF"
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND (SEBRA.C != OPTIMUS.C)

--cuadre val_resc
SELECT *
FROM
(
	select fecha,sum(val_resc)   AS C
	from OPTIMUS_TBPLTBIF ot 
	group by fecha
) AS SEBRA
,
(
	select fecha,sum(val_resc)   AS C
	from "2022_05_31_TBPLTBIF"
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND abs(SEBRA.C - OPTIMUS.C) > 10

--cuadre plazo
SELECT *
FROM
(
	select fecha,sum(plazo)   AS C
	from OPTIMUS_TBPLTBIF ot 
	group by fecha
) AS SEBRA
,
(
	select fecha,sum(plazo)   AS C
	from "2022_05_31_TBPLTBIF"
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND abs(SEBRA.C - OPTIMUS.C) > 10

--cuadre paridad_morigen
SELECT *
FROM
(
	select fecha,sum(paridad_morigen)   AS C
	from OPTIMUS_TBPLTBIF ot 
	group by fecha
) AS SEBRA
,
(
	select fecha,sum(paridad_morigen)   AS C
	from "2022_05_31_TBPLTBIF"
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND abs(SEBRA.C - OPTIMUS.C) > 1

