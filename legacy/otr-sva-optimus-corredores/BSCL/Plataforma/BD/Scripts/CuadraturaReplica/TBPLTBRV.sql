select IND_SIMUL,FEC_TRANS ,FOLIO_TRANS ,FOLIO_SIMUL,*  
from OPTIMUS_TBPLTBRV ot 
where IND_SIMUL != ''

select FOLIO_TRANS ,FOLIO_SIMUL  from "2022_05_31_TBPLTBRV" brbvst 
where IND_SIMUL = ''

SELECT * FROM "2022_05_31_TBPLTBRV" t WHERE FECHA = '2022-05-31' AND FOLIO_SIMUL = '' and tasa = 0 

SELECT * FROM OPTIMUS_TBPLTBRV t WHERE FECHA = '2022-05-31'  AND FOLIO_SIMUL = ''  and tasa = 0 order by FOLIO_TRANS 

--FOLIO SIMUL NO COINCIDE TAN TODOS MALOS
--NO SE PUEDEN CUADRAR SIM POR AHORA

--la hora del fectrans no coincide -> campos FechaOperacion y HoraSistemaCreacion estan con hora local, debe estar con hora UTC
--no se carga marca de negociacion

--cantidad de palos no simultanea
SELECT *
FROM
(
	select fecha,nemo,ind_liquid,count(1)   AS C
	from OPTIMUS_TBPLTBRV ot 
	where FECHA  = '2022-05-31'
--	where FOLIO_SIMUL = ''  and tasa = 0
	group by fecha,nemo,ind_liquid
) AS SEBRA
left join 
(
	select fecha,nemo,ind_liquid,count(1)   AS C
	from "2022_05_31_TBPLTBRV"
	where FECHA  = '2022-05-31'
	--where FOLIO_SIMUL = ''  and tasa = 0
	group by fecha,nemo,ind_liquid
) AS OPTIMUS
ON SEBRA.FECHA = OPTIMUS.FECHA AND SEBRA.NEMO = OPTIMUS.NEMO AND SEBRA.IND_LIQUID = OPTIMUS.IND_LIQUID 
WHERE (SEBRA.C != OPTIMUS.C)

--cuadre cantidad
SELECT *
FROM
(
	select fecha,sum(cantidad)   AS C
	from OPTIMUS_TBPLTBRV ot 
	where FECHA  = '2022-05-31'
	--where FOLIO_SIMUL = ''  and tasa = 0
	group by fecha
) AS SEBRA
,
(
	select fecha,sum(cantidad)   AS C
	from "2022_05_31_TBPLTBRV"
	where FECHA  = '2022-05-31'
--	where FOLIO_SIMUL = ''  and tasa = 0
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND (SEBRA.C != OPTIMUS.C)

--cuadre monto
SELECT *
FROM
(
	select fecha,sum(monto)   AS C
	from OPTIMUS_TBPLTBRV ot 
	where FECHA  = '2022-05-31'
	--where FOLIO_SIMUL = ''  and tasa = 0
	group by fecha
) AS SEBRA
,
(
	select fecha,sum(monto)   AS C
	from "2022_05_31_TBPLTBRV"
	where FECHA  = '2022-05-31'
	--where FOLIO_SIMUL = ''  and tasa = 0
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND (SEBRA.C != OPTIMUS.C)

--cuadre precio
SELECT *
FROM
(
	select fecha,sum(precio)   AS C
	from OPTIMUS_TBPLTBRV ot 
	where FECHA  = '2022-05-31'
	--where FOLIO_SIMUL = ''  and tasa = 0
	group by fecha
) AS SEBRA
,
(
	select fecha,sum(precio)   AS C
	from "2022_05_31_TBPLTBRV"
	where FECHA  = '2022-05-31'
	--where FOLIO_SIMUL = ''  and tasa = 0
	group by fecha
) AS OPTIMUS
WHERE (SEBRA.FECHA = OPTIMUS.FECHA OR OPTIMUS.FECHA IS NULL) AND (SEBRA.C != OPTIMUS.C)

