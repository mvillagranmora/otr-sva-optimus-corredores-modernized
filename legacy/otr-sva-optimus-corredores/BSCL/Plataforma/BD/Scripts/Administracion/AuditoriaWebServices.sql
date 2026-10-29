
SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
ORDER BY "Key" DESC


SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
where "Servicio" like '%Custodia/consultarCustodia%'
and "FechaInicio" >= '2024-08-19 00:00:00' and "FechaFin" <= '2024-08-20 00:00:00'

SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
where-- "Servicio" like '%CuentaInversion/consultarMovimientos%'
"FechaInicio" >= '2024-08-19 00:00:00' and "FechaFin" <= '2024-08-20 00:00:00'

SELECT TOP 100 * FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "HTTPCode" = 500
ORDER BY "Key" DESC

SELECT TOP 100 * FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "Servicio" LIKE '%crearAporteSAAPV' AND "FechaInicio" >= '2020-08-24' AND "HTTPCode" = 500
ORDER BY "Key" DESC

SELECT TOP 100 * FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "Servicio" LIKE '%FFMM' AND "FechaInicio" >= '2023-10-31'
ORDER BY "Key" DESC

SELECT TOP 100 * FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "Servicio" LIKE '%crearRescate' AND "FechaInicio" >= '2020-08-24' AND "HTTPCode" = 500
ORDER BY "Key" DESC


SELECT avg(SECONDS_BETWEEN("FechaInicio","FechaFin")) AS S
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" SSS
WHERE  "FechaInicio" >= '2025-05-29 00:00:00' -- AND SECONDS_BETWEEN("FechaInicio","FechaFin") > 5
and "Servicio" like '%consultarCustodia%'

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" SSS
WHERE  "FechaInicio" >= '2025-05-29 00:00:00' -- AND SECONDS_BETWEEN("FechaInicio","FechaFin") > 5
and "Servicio" like '%consultarCustodia%'

SELECT DISTINCT "Servicio" FROM "BSCL.Plataforma.BD::PLA.AuditoriaWebServices"

SELECT seconds_between("FechaInicio","FechaFin"),* FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" WHERE "FechaInicio" >= '2021-07-05 16:40:00' AND "FechaInicio" < '2021-07-05 17:10:00'
RDER BY "Key" asc


SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" where "Servicio" like '%Rescate%' order by "Key"  desc

SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE CONTAINS("Entrada",'10297138-8',EXACT) AND "FechaInicio" >= '2020-02-03 00:00:00' AND "Servicio" = '/api/FondosMutuos/crearRescate'
ORDER BY "Key" DESC


SELECT DISTINCT "Servicio" 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE CONTAINS("Entrada",'10297138-8',EXACT) AND "FechaInicio" >= '2020-02-03 00:00:00'

SELECT DISTINCT "Servicio"  FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"



SELECT "Key","Servicio","Entrada","FechaInicio","FechaFin","HTTPCode" 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE  "FechaInicio" >= '2022-08-24 11:30:00' AND "FechaInicio" <= '2022-08-24 12:30:00'
AND NOT CONTAINS("Entrada",'monitor2',EXACT) AND NOT CONTAINS("Entrada",'monitor2',EXACT)
ORDER BY "Key" DESC

SELECT "Servicio",count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE  "FechaInicio" >= '2022-05-29 00:00:00'
AND NOT CONTAINS("Entrada",'monitor',EXACT) AND NOT CONTAINS("Entrada",'monitor2',EXACT)
GROUP BY "Servicio"

SELECT * FROM  BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
 WHERE "Servicio" = '/api/APV/consultarSAAPV'

SELECT "Key","Servicio","Entrada","FechaInicio","FechaFin","HTTPCode" 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE  "FechaInicio" >= '2022-08-23 23:40:00'
AND NOT CONTAINS("Entrada",'monitor',EXACT)-- AND "Servicio" LIKE '%/api/APV/consultarTraspasos%'
ORDER BY "Key" DESC
 

SELECT TOP 100 * FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "Servicio" LIKE '%Movimientos' AND "FechaInicio" >= '2020-08-24'-- AND "HTTPCode" = 500
ORDER BY "Key" DESC




--PARA SURA
SELECT "Key","Servicio","Entrada","FechaInicio","FechaFin" 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "FechaInicio" >= '2022-02-15 23:00:00' AND "FechaFin" <= '2022-02-16 00:59:00'

SELECT top 100 * FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" WHERE "Servicio" like '%crearAporteSAAPV%'
order BY "Key" DESC

--analisis de llamadas por hora,minuto y servicio
SELECT YEAR("FechaInicio") AS y,MONTH("FechaInicio") AS mm,DAYOFMONTH("FechaInicio") AS d,HOUR("FechaInicio") AS h,minute("FechaInicio") AS min,"HTTPCode","Servicio",count(1) 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
--WHERE "Servicio" in ('/api/FondosMutuos/IngresoOperacionFFMM','/api/Ordenes/ingresarorden') AND 
--contains("Entrada",'OPENWP')
WHERE "FechaInicio" >= '2024-01-01 10:30:00'
GROUP BY YEAR("FechaInicio"),MONTH("FechaInicio"),DAYOFMONTH("FechaInicio"),HOUR("FechaInicio"),minute("FechaInicio"),"Servicio","HTTPCode"
ORDER BY y,mm,d,"Servicio",h,min
--order BY "Key" DESC

--analisis de llamadas por hora,minuto y servicio
SELECT YEAR("FechaInicio") AS y,MONTH("FechaInicio") AS mm,DAYOFMONTH("FechaInicio") AS d,"Servicio",count(1) 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
--WHERE "Servicio" in ('/api/FondosMutuos/IngresoOperacionFFMM','/api/Ordenes/ingresarorden') AND 
--contains("Entrada",'OPENWP')
WHERE "FechaInicio" >= '2024-01-01 10:30:00'
GROUP BY YEAR("FechaInicio"),MONTH("FechaInicio"),DAYOFMONTH("FechaInicio"),"Servicio"
ORDER BY y,mm
--order BY "Key" DESC

SELECT YEAR("FechaInicio") AS y,MONTH("FechaInicio") AS mm,DAYOFMONTH("FechaInicio") AS d,HOUR("FechaInicio") AS h,minute("FechaInicio") AS min,count(1) 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
--WHERE "Servicio" in ('/api/FondosMutuos/IngresoOperacionFFMM','/api/Ordenes/ingresarorden') AND 
--contains("Entrada",'OPENWP')
WHERE "FechaInicio" >= '2022-08-09 10:30:00'
GROUP BY YEAR("FechaInicio"),MONTH("FechaInicio"),DAYOFMONTH("FechaInicio"),HOUR("FechaInicio"),minute("FechaInicio")
ORDER BY y,mm,d,h,min

SELECT "Key","Servicio","Entrada","FechaInicio","FechaFin"
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
--WHERE "Servicio" in ('/api/FondosMutuos/IngresoOperacionFFMM','/api/Ordenes/ingresarorden') AND 
--contains("Entrada",'OPENWP')
WHERE "FechaInicio" >= '2022-08-09 10:30:00'
ORDER BY "Key" DESC


SELECT YEAR("FechaInicio") AS y,MONTH("FechaInicio") AS mm,DAYOFMONTH("FechaInicio") AS d,HOUR("FechaInicio") AS h,"Servicio",count(1) FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
--WHERE "Servicio" in ('/api/FondosMutuos/IngresoOperacionFFMM','/api/Ordenes/ingresarorden') AND 
--contains("Entrada",'OPENWP')
WHERE "FechaInicio" >= '2022-08-09 09:00:00'
GROUP BY YEAR("FechaInicio"),MONTH("FechaInicio"),DAYOFMONTH("FechaInicio"),HOUR("FechaInicio"),"Servicio"
HAVING count(1) > 20
ORDER BY y,mm,d,h

SELECT YEAR("FechaInicio") AS y,MONTH("FechaInicio") AS mm,DAYOFMONTH("FechaInicio") AS d,count(1) FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
--WHERE "Servicio" in ('/api/FondosMutuos/IngresoOperacionFFMM','/api/Ordenes/ingresarorden') AND 
--contains("Entrada",'OPENWP')
WHERE "FechaInicio" >= '2026-03-01 09:00:00'
GROUP BY YEAR("FechaInicio"),MONTH("FechaInicio"),DAYOFMONTH("FechaInicio")
--HAVING count(1) > 20
ORDER BY y,mm,d

SELECT YEAR("FechaInicio") AS y,MONTH("FechaInicio") AS mm,DAYOFMONTH("FechaInicio") AS d,"Servicio" s,count(1) 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
--WHERE "Servicio" in ('/api/FondosMutuos/IngresoOperacionFFMM','/api/Ordenes/ingresarorden') AND 
--contains("Entrada",'OPENWP')
WHERE "FechaInicio" >= '2026-03-19 12:00:00'
GROUP BY YEAR("FechaInicio"),MONTH("FechaInicio"),DAYOFMONTH("FechaInicio"),"Servicio"
--HAVING count(1) > 20
ORDER BY y,mm,s,d

SELECT * 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
WHERE "FechaInicio" >= '2026-03-19 12:00:00'
and "FechaInicio" < '2026-03-19 14:00:00'
and contains("Entrada",'IdUsuario')



SELECT YEAR("FechaInicio") AS y,MONTH("FechaInicio") AS mm,DAYOFMONTH("FechaInicio") AS d,HOUR("FechaInicio") AS h,count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
--WHERE "Servicio" in ('/api/FondosMutuos/IngresoOperacionFFMM','/api/Ordenes/ingresarorden') AND 
--contains("Entrada",'OPENWP')
WHERE "FechaInicio" >= '2024-07-07 09:00:00'
GROUP BY YEAR("FechaInicio"),MONTH("FechaInicio"),DAYOFMONTH("FechaInicio"),HOUR("FechaInicio")
--HAVING count(1) > 10000
ORDER BY y,mm,d,h

delete from
BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
where
YEAR("FechaInicio")  = 2024 and 
MONTH("FechaInicio") = 6 and
DAYOFMONTH("FechaInicio") = 20 AND
HOUR("FechaInicio") = 2

2024	6	19	23	50504
2024	6	20	2	15638

update BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
SET "Salida" = null
WHERE "FechaInicio" >= '2022-06-01' AND "FechaInicio" <= '2022-06-15'


SELECT * 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
WHERE "FechaInicio" > '2022-01-10 11:00:00' AND "HTTPCode" = 500 AND "Servicio" = '/api/FondosMutuos/consultarSolicitudesFFMM'

SELECT * 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
WHERE "FechaInicio" >= '2022-01-01 10:00:00' AND CONTAINs("Entrada",'96549050-7')
ORDER BY "Key" desc

SELECT * 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
WHERE "FechaInicio" > '2021-12-30 11:24:00' AND "FechaInicio" < '2021-12-30 12:00:00' -- AND CONTAINs("Entrada",'96549050-7')
ORDER BY "Key" asc

SELECT * 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
WHERE "FechaInicio" > '2022-01-17 09:00:00' AND "FechaInicio" < '2022-01-22 12:00:00' AND "Servicio" = '/api/FondosMutuos/consultarSolicitudesFFMM'
AND CONTAINs("Entrada",'FechaDesde',EXACT) AND NOT CONTAINs("Entrada",'Folio',EXACT)
ORDER BY "FechaInicio" asc

SELECT * 
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
WHERE "FechaInicio" > '2024-04-04 00:00:00' 
AND "Servicio" LIKE '%crearAporteSAAPV%'


SELECT "IdPortafolio",count(1) AS c FROM "BSCL"."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" GROUP BY "IdPortafolio"
ORDER BY c desc



SELECT "Servicio",count(1) FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" WHERE "Servicio" LIKE '%Cliente%'
GROUP BY "Servicio"
--AND contains("Entrada",'W89834')

order BY "Key" DESC

SELECT "IdUsuario","FechaIngresoRegistro","FechaEmision","TipoContratoOperacion","FolioContratoOperacion",* 
FROM "BSCL"."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" WHERE "IdUsuario" = 999 ORDER BY "IdSolicitudAporteRescate" desc



SELECT * FROM "BSCL"."BSCL.Plataforma.BD::ACC.Usuario"
WHERE "CodigoUsuario" LIKE '%MB%'

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "FechaInicio" >= '2021-12-09 10:00:00' AND "Servicio" LIKE '%consultarSolicitudesFFMM%'
AND NOT CONTAINS("Entrada",'Fecha',EXACT)
AND NOT CONTAINS("Entrada",'FechaDesde',EXACT)
AND NOT CONTAINS("Entrada",'Folio',EXACT)
AND CONTAINS("Entrada",'96549050',EXACT)
ORDER BY "Key" desc


SELECT HOUR("FechaInicio") AS h,minute("FechaInicio") AS m,count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "FechaInicio" >= '2021-09-09 10:00:00'
AND CONTAINS("Entrada",'CANALNUEVAWEB',EXACT)
GROUP BY HOUR("FechaInicio"),minute("FechaInicio")
ORDER BY h desc,m DESC

SELECT HOUR("FechaInicio") AS h,minute("FechaInicio") AS m,count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "FechaInicio" >= '2021-08-31 00:00:00'
AND NOT CONTAINS("Entrada",'monitor',EXACT)
GROUP BY HOUR("FechaInicio"),minute("FechaInicio")
ORDER BY h desc,m DESC


SELECT "Servicio",count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "FechaInicio" >= '2022-10-01 00:00:00' and "FechaFin" <= '2022-11-16 23:59:59'
AND NOT CONTAINS("Entrada",'monitor',EXACT) AND NOT CONTAINS("Entrada",'monitor2',EXACT)
GROUP BY "Servicio"



select * from BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
where "Servicio" =  '/api/CuentaInversion/consultarMovimientos'

SELECT top 100 *
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "Servicio" like '%/api/FondosMutuos/crearAporteSAAPV%' AND CONTAINS("Entrada",'8273872',EXACT)
ORDER BY "Key" desc

SELECT top 100 *
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "Servicio" like '%Orden%'
ORDER BY "Key" desc


SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "FechaInicio" >= '2022-07-21 01:55:00' AND "Servicio" = '/api/FondosMutuos/consultarSolicitudesFFMM'
AND CONTAINS("Entrada",'22300309-5',EXACT)

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "FechaInicio" >= '2022-07-21 00:16:00' AND "Servicio" = '/api/FondosMutuos/consultarCustodiaPortafolioFondoMutuo'
AND CONTAINS("Entrada",'22300309-5',EXACT)

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "FechaInicio" >= '2022-07-21 00:16:00' AND "Servicio" = '/api/Clientes/consultarCustodiaPortafolioFondoMutuo'
AND CONTAINS("Entrada",'22300309-5',EXACT)



SELECT DISTINCT "Servicio"
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "FechaInicio" >= '2021-10-01 10:00:00' AND "Servicio" LIKE '%onsulta%'

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE "FechaInicio" >= '2022-01-13 12:08:00' AND "Servicio" LIKE '%/api/FondosMutuos/consultarSolicitudesFFMM%'
AND (contains("Entrada",'18087394-5') OR contains("Entrada",'13928470-4'))
ORDER BY "Key" desc


--hits por servicio ultimos x meses
SELECT "Servicio",count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
--WHERE  "FechaInicio" >= '2022-01-13 13:00:00'
GROUP BY "Servicio"

SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" WHERE "Servicio" = '/api/Mesa/crearOperacionForward'
--AND contains("Entrada",'76460184-K')
AND "FechaInicio" >= ''
ORDER BY "FechaInicio" DESC

--PARA EXPORTAR
SELECT "Key" ,"Servicio" ,"Entrada" ,"FechaInicio" ,"FechaFin" FROM BSCL."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
WHERE "FechaInicio" >= '2022-10-17 00:00:00'
AND NOT CONTAINS("Entrada",'monitor2',EXACT) AND NOT CONTAINS("Entrada",'monitor2',EXACT)



SELECT * FROM "BSCL.Plataforma.BD::PLA.AuditoriaWebServices" bpbpaws 
where "Servicio"  like '%crearContratoSAAPV%'




select * 
from "BSCL.Plataforma.BD::PLA.AuditoriaServicios"




SELECT subprt.*
FROM "BSCL.Plataforma.BD::INS.Instrumento" inst
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoins on tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" sub on 0 = sub."IdInstrumento"
LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" lib on 1 = lib."IdLibroOrdenes"
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" bol on bol."IdBolsa" = 'BCS'
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  tipoope on 'COMCFI' = tipoope."IdTipoOperacion"
LEFT JOIN "BSCL.Orden.BD::ORD.CorredorCorresponsal" corr on corr."IdCorredorCorresponsal" = 0
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" instr on instr."IdInstrumento" = 266898 and instr."IdTipoReajuste" = ''
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" subprt on 266898 = subprt."IdInstrumento" and subprt."IdTipoInstrumento"  IN ('PRT','PRF') and tran."IdBolsa" = subprt."IdBolsa" 
--LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" liq ON liq."IdCondicionDeLiquidacion" = 'T2'
where inst."Nemotecnico" = 'CFI-LPC1E'


LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  tipooperel on tran."IdTipoOperacion" = tipooperel."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" tipoopeins on tipoopeins."IdTipoOperacion" = tipooperel."IdTipoOperacionRelacionada" 
	and tipoopeins."IdTipoInstrumento" = inst."IdTipoInstrumento"
;

select * from "BSCL.Plataforma.BD::INS.Instrumento" bpbii where "Nemotecnico" = 'CFI-LPC1E'


