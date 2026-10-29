	INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT table_key,'Custodia_GarantiaHis', 'I', 0,  CURRENT_TIMESTAMP  
	FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_GarantiaHis";
	



SELECT "Table","Action", min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2022-10-04 18:00:00' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-27' AND
"Status" = 0-- AND "Action" = 'D'
AND "Table" = 'FondoMutuo_Fondo'

SELECT count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_GarantiaHis" brbvrcgh 