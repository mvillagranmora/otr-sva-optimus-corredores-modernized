INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast("IdReinversionSerieFondo" AS varchar(200)),'FondoMutuo_ReinversionSerieFondoDestino', 'A', 0,  CURRENT_TIMESTAMP 
FROM "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/ReinversionSerieFondoDestino";


SELECT *
FROM "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/ReinversionSerieFondoDestino"
WHERE "IdReinversionSerieFondo" >= 390390 AND "IdReinversionSerieFondo" <= 390399

SELECT count(1)
FROM "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/ReinversionSerieFondoDestino"


SELECT "Table","Action", min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-20' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

SELECT "Table","Action", "Status",min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
--WHERE "Stamp" >= '2020-08-20'
GROUP BY "Table","Action","Status"

SELECT *
FROM "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/ReinversionSerieFondoDestino" c
WHERE "IdSolicitudRescate" IS null

SELECT MIN("IdSolicitudRescate")|| '' AS MIN,MAX("IdSolicitudRescate")||'' AS MAX,FLOOR("IdSolicitudRescate"/1000) AS GRUPO,COUNT(1)*1.0 as C,1.0*SUM("IdReinversionSerieFondo" + COALESCE("IdSolicitudAporte",0) + coalesce("MontoAporte",0) + COALESCE("MontoRescate",0) + LENGTH("IdSerieFondoDestino")) AS HASH FROM "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/ReinversionSerieFondoDestino" 
WHERE FLOOR("IdSolicitudRescate"/1000) = 0 OR 1 = 1 GROUP BY FLOOR("IdSolicitudRescate"/1000) 




SELECT  Reinversion.*
FROM BSCL."BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" AS Reinversion
LEFT JOIN BSCL."BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON (SerieDeFondo."IdSerieFondo" = Reinversion."IdSerieFondoDestino")
LEFT JOIN BSCL."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS Rescate
    ON (Reinversion."IdSolicitudAporteRescate" = Rescate."IdSolicitudAporteRescate")
LEFT JOIN BSCL."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS Aporte
    ON (Reinversion."IdSolicitudAporteRescate" = Aporte."IdSolicitudAporteRescateRelacionado" and
    Aporte."IdSerieFondo" = Reinversion."IdSerieFondoDestino")
    WHERE Rescate."IdSolicitudAporteRescate" IS NOT NULL
 
    SELECT * FROM 
    BSCL."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" WHERE "IdSolicitudAporteRescate" IN (11376546,11376547,11376548)