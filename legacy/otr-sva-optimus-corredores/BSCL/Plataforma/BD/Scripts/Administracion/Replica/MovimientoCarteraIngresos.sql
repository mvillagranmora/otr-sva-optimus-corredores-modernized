INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT TABLE_KEY,'Custodia_MovimientoCarteraIngresos', 'A', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_MovimientoCarteraIngresos"
WHERE Nemotecnico = 'AAISA'


SELECT "Table","Action", min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2021-12-01' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-27' AND
"Status" = 0-- AND "Action" = 'D'
AND "Table" = 'Custodia_CarteraFM'

SELECT * FROM   BSCL."BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraIngresosAnalitica"
WHERE "Nemotecnico" = 'CFI-PGTPBE'

SELECT MIN("IdCartera")|| '' AS MIN,MAX("IdCartera")||'' AS MAX,FLOOR("IdCartera"/100) AS GRUPO,COUNT(1)*1.0 as C,
1.0*SUM("IdCartera" + "IdInstrumento" + "Cantidad" + "Precio" + "Tasa"  + ROUND("Monto",4)) AS HASH 
FROM BSCL."BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraIngresosAnalitica" 
WHERE (FLOOR("IdCartera"/100) = 44396 OR 0 = 1) GROUP BY FLOOR("IdCartera"/100) 


SELECT ("IdCartera" + "IdInstrumento" + "Cantidad" + "Precio" + "Tasa"  + ROUND("Monto",4)) AS HASH ,c.*
FROM BSCL."BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraIngresosAnalitica" c
WHERE (FLOOR("IdCartera"/100) = 44396 OR 0 = 1) 

SELECT * 
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraIngresosAnalitica" 
WHERE "IdCartera" IN (5786937,
5798805,
5798806,
5798810,
5798626,
5799020)


SELECT ("IdCartera" + "IdInstrumento" + "Cantidad" + "Precio" + "Tasa") AS HASH ,*
FROM BSCL."BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraIngresosAnalitica" 
WHERE FLOOR("IdCartera"/100) = 45899 

SELECT * FROM (
SELECT MIN("IdCartera")|| '' AS MIN,MAX("IdCartera")||'' AS MAX,FLOOR("IdCartera"/100) AS GRUPO,COUNT(1)*1.0 as C,1.0*SUM("IdCartera" + "IdInstrumento" + "Cantidad" + "Precio" + "Tasa") AS HASH FROM BSCL."BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraIngresosAnalitica" 
WHERE FLOOR("IdCartera"/100) = 0 OR 1 = 1 GROUP BY FLOOR("IdCartera"/100) 
) WHERE GRUPO = 45899