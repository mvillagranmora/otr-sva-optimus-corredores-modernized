INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'Custodia_CarteraFMHis', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_CarteraFMHis" 
WHERE FechaRespaldo = '2025-06-19'

SELECT "FechaRespaldo",count(1)
FROM "BSCL"."BSCL.Custodia.BD.Vistas::HIS.CarteraFMHisAnalitica"
where "FechaRespaldo" >= '2025-06-10'
GROUP BY "FechaRespaldo"
ORDER BY "FechaRespaldo" DESC

SELECT "IdCartera" + "IdInstrumento" + "CantidadInicial" + "PrecioCompraInicial" + "MontoCompraInicial",*
FROM BSCL."BSCL.Custodia.BD.Vistas::HIS.CarteraFMHisAnalitica"
WHERE "IdCartera" >= 852 AND "IdCartera" <= 856
ORDER BY "IdCartera" DESC, "FechaRespaldo" desc

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
"Stamp" >= '2020-09-09'
AND "Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"
ORDER BY "Stamp"

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-31' AND
"Status" = 0-- AND "Action" = 'D'
AND "Table" = 'FondoMutuo_Traspasos'
ORDER BY "Stamp" desc

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Traspasos" WHERE idstack IS NOT null

SELECT "FechaRespaldo",COUNT(1) 
FROM BSCL."BSCL.Custodia.BD.Vistas::HIS.CarteraFMHisAnalitica"
GROUP BY "FechaRespaldo"
ORDER BY "FechaRespaldo" desc

SELECT count(1) FROM (
SELECT DISTINCT *
FROM BSCL."BSCL.Custodia.BD.Vistas::HIS.CarteraFMHisAnalitica"
WHERE "FechaRespaldo" = '2020-08-28')

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE "Stamp" < '2020-08-31'


SELECT "Table","Action", min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-31'
AND "Status" = 0
GROUP BY "Table","Action"

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-31'
AND "Table" = 'Custodia_CustodiaFechaLiquidacion'
AND "Status" = 0

DELETE FROM  BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"


SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.Configuracion"


SELECT * FROM "BSCL"."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" WHERE "IdCargoAbonoMovimiento" > 4298119

4298649


select "IdOperacion" ,"FechaCreacion" ,"FechaEdicion" ,* from "BSCL.Mesa.BD::MES.Operacion" bmbmo where "IdTipoProducto" = 'MERF'
and "IdEstado" != 'ANU'