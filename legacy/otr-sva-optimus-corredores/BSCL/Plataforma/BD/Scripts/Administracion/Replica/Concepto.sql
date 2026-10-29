INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'Plataforma_Concepto', 'I', 0,  CURRENT_TIMESTAMP  
FROM "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Concepto"

SELECT * FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Concepto"

SELECT "Table","Action", min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2021-01-11' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"



select * from "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Concepto" brbvrpc 


ALTER SEQUENCE "BSCL.Mesa.BD.Secuencias::SPT.IdOperacionSpot" RESTART WITH 8339721