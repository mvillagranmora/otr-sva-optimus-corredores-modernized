INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'Custodia_CarteraFM', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_CarteraFM"

SELECT "FechaRespaldo",count(1)
FROM "_SYS_BIC"."BSCL.Custodia.BD.Analytics/CarteraFM"
GROUP BY "FechaRespaldo"
ORDER BY "FechaRespaldo" DESC

SELECT "IdCartera" + "IdInstrumento" + "CantidadInicial" + "PrecioCompraInicial" + "MontoCompraInicial",*
FROM BSCL."BSCL.Custodia.BD.Vistas::CAR.CarteraFMAnalitica"
WHERE "IdCartera" >= 852 AND "IdCartera" <= 856
ORDER BY "IdCartera" DESC

SELECT *
FROM "_SYS_BIC"."BSCL.Custodia.BD.Analytics/CarteraRVHis"
WHERE "FechaIngresoRegistro" <= '2020-07-08' AND "FechaIngresoRegistro" >= '2020-01-01' AND "IdMatrizAgrupador" = 18
ORDER BY "IdCartera"

SELECT "FechaRespaldo",count(1)  FROM BSCL."BSCL.Custodia.BD.Vistas::HIS.CarteraFMHisAnalitica" GROUP BY "FechaRespaldo"
ORDER BY "FechaRespaldo" desc

SELECT *
FROM "_SYS_BIC"."BSCL.Custodia.BD.Analytics/CarteraRVHis"
WHERE "FechaIngresoRegistro" <= '2020-07-08' AND "FechaIngresoRegistro" >= '2020-01-01' AND "IdMatrizAgrupador" = 18 AND "IdCartera" = 5042068
ORDER BY "IdCartera"

SELECT "Table","Action", max("Stamp") AS "Stamp",count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2021-03-01' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"
ORDER BY "Stamp"

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-09-11' AND
"Status" = 0-- AND "Action" = 'D'
ORDER BY "Stamp" desc

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Traspasos" WHERE idstack IS NOT null

SELECT "FechaRespaldo",COUNT(1) 
FROM BSCL."BSCL.Custodia.BD.Vistas::HIS.CarteraFMHisAnalitica"
GROUP BY "FechaRespaldo"
ORDER BY "FechaRespaldo" desc

SELECT *
FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_CarteraFM"
WHERE TABLE_KEY = 5781331

SELECT "IdEstado",*
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "FolioCartera" = 5764250

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE "Status" = 0 AND "Table" = 'Custodia_CarteraFM'


SELECT MIN("IdCartera")|| '' AS MIN,MAX("IdCartera")||'' AS MAX,FLOOR("IdCartera"/100) AS GRUPO,COUNT(1)*1.0 as C,1.0*SUM("IdCartera" + "IdInstrumento" + "CantidadInicial" + "PrecioCompraInicial" + "MontoCompraInicial" + "PrecioMercadoActual") AS HASH FROM BSCL."BSCL.Custodia.BD.Vistas::CAR.CarteraFMAnalitica" 
WHERE FLOOR("IdCartera"/100) = 0 OR 1 = 1 GROUP BY FLOOR("IdCartera"/100) 


DELETE FROM 
