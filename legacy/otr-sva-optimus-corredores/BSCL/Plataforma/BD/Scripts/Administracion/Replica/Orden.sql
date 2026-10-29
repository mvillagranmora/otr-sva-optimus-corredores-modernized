INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast("IdOrden" AS varchar(200)),'Orden_Orden', 'A', 0,  CURRENT_TIMESTAMP 
FROM "_SYS_BIC"."BSCL.Orden.BD.Analytics/Orden";

SELECT *
FROM "_SYS_BIC"."BSCL.Orden.BD.Analytics/Orden"
WHERE "IdOrden" >= 42004 AND "IdOrden" <= 42335
ORDER BY "IdOrden"

SELECT "Table","Action", min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-27' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"


SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-27' AND
"Status" = 0-- AND "Action" = 'D'


SELECT * FROM bscl."BSCL.Replica.BD.Vistas::ReplicaTBCPDTPC" brbvrt 

