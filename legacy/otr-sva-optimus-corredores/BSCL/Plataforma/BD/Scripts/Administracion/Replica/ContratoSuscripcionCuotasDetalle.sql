SELECT COUNT(1)
FROM "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/ContratoSuscripcionCuotasDetalle"

SELECT FLOOR("IdFolioCSC" + "IdCSCDetalle" + "IdFondo" + "Monto"),*
FROM "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/ContratoSuscripcionCuotasDetalle"
WHERE "IdFolioCSC" >= 1413800 AND "IdFolioCSC" <= 1413899
ORDER BY "IdCSCDetalle"


SELECT "Table","Action", min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-27' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

UPDATE BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
SET "Stamp"  ='2020-08-27 12:10:00'
WHERE
"Stamp" >= '2020-08-27' AND
"Status" = 0-- AND "Action" = 'D'
AND "Table" IN ('Orden_Orden','Orden_Asignacion')

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-27' AND
"Status" = 0-- AND "Action" = 'D'
AND "Table" IN ('FondoMutuo_ContratoSuscripcionCuotasDetalle')



INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )

SELECT SUM(("IdFolioCSC" + "IdCSCDetalle" + "IdFondo" + "Monto"))
FROM _SYS_BIC."BSCL.FondoMutuo.BD.Analytics/ContratoSuscripcionCuotasDetalle" 
WHERE "IdFolioCSC" >= 1455601 and "IdFolioCSC" <= 1455698 


SELECT MIN("IdFolioCSC")|| '' AS MIN,MAX("IdFolioCSC")||'' AS MAX,FLOOR("IdFolioCSC"/100) AS GRUPO,COUNT(1)*1.0 as C,1.0*SUM(floor("IdFolioCSC" + "IdCSCDetalle" + "IdFondo" + "Monto")) AS HASH FROM _SYS_BIC."BSCL.FondoMutuo.BD.Analytics/ContratoSuscripcionCuotasDetalle" WHERE FLOOR("IdFolioCSC"/100) = 0 OR 1 = 1 GROUP BY FLOOR("IdFolioCSC"/100) 


{14556={"MIN":"1455601","MAX":"1455698","GRUPO":14556,"HASH":1216733789,"C":77.0}}