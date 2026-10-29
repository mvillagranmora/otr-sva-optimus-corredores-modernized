INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'CuentaInversion_PatrimonioCajaFechaOperacion', 'I', 0,  CURRENT_TIMESTAMP 
FROM bscl."BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_PatrimonioCajaFechaOperacion"
WHERE FechaPatrimonio IN ('2025-11-14');

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'CuentaInversion_PatrimonioCajaFechaLiquidacion', 'I', 0,  CURRENT_TIMESTAMP 
FROM bscl."BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_PatrimonioCajaFechaLiquidacion"
WHERE FechaPatrimonio IN ('2026-05-24');

SELECT "FechaPatrimonio",count(1) 
FROM bscl."BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaOperacionAnalitica"
GROUP BY "FechaPatrimonio"
ORDER BY "FechaPatrimonio" DESC

SELECT "FechaPatrimonio",count(1) 
FROM bscl."BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaLiquidacionAnalitica"
GROUP BY "FechaPatrimonio"
ORDER BY "FechaPatrimonio" DESC


SELECT "Table","Action", max("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-10-01'
AND "Status" = 0
-- AND "Action" = 'D'
GROUP BY "Table","Action"
ORDER BY max("Stamp")

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE "Table" IN ('Custodia_PatrimonioConsolidadoFechaLiquidacion','Custodia_PatrimonioConsolidadoFechaOperacion')


SELECT "FechaPatrimonio" ,COUNT(1) FROM 
"BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaLiquidacionAnalitica" 
WHERE TO_DATE("FechaPatrimonio",'YYYY-MM-DD') >= ADD_DAYS(CURRENT_DATE,- 5)
group by "FechaPatrimonio" 

SELECT ("IdPortafolio" + "IdMatrizAgrupadorAPV" + "MontoPatrimonio") AS HASH,*
FROM BSCL."BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaLiquidacionAnalitica" 
WHERE FLOOR("IdPortafolio"/100) = 2512
ORDER BY "IdCliente","IdPortafolio","IdMoneda"



SELECT *
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaLiquidacionAnalitica"
WHERE "IdClasificacionTipoInstrumento" = 'FM'
AND "FechaPatrimonio" = '2020-10-01'
AND "IdRegimenTributario" = ''


SELECT *
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaOperacionAnalitica"
WHERE "IdClasificacionTipoInstrumento" = 'FM'
AND "FechaPatrimonio" = '2020-10-01'
AND "IdRegimenTributario" = ''



SELECT "FechaRespaldo",count(1) FROM "BSCL"."BSCL.Custodia.BD::HIS.Cartera" GROUP BY "FechaRespaldo"


select * from "BSCL.Contabilidad.BD::ITZ.Movimiento" bcbim order by "IdMovimiento"  asc