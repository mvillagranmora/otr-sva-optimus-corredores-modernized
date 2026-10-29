	INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT cast("IdFondo" AS varchar(200)),'FondoMutuo_Fondo', 'I', 0,  CURRENT_TIMESTAMP  
	FROM "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/Fondo";
	

SELECT "IdFondo" AS GRUPO,COUNT(1)*1.0 as C,1.0*SUM(length("NombreFondo")) AS HASH 
FROM _SYS_BIC."BSCL.FondoMutuo.BD.Analytics/Fondo" GROUP BY "IdFondo" 

SELECT * from
_SYS_BIC."BSCL.FondoMutuo.BD.Analytics/Fondo" 


SELECT "Table","Action", min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-09-01' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-27' AND
"Status" = 0-- AND "Action" = 'D'
AND "Table" = 'FondoMutuo_Fondo'