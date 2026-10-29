    INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT CAST(TO_VARCHAR("FECHASISTEMA",'YYYY-MM-DD') || "NOMBRE" AS VARCHAR(200)),'Plataforma_ResumenReplica', 'A', 0,  CURRENT_TIMESTAMP  
FROM "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ResumenReplica";


SELECT "Table","Action", max("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-20' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ResumenReplica"

SELECT * FROM "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WHERE "Table" = 'Plataforma_ResumenReplica'

SELECT *
 from "BSCL.Plataforma.BD::PLA.ResumenReplica"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
        	ON  WST."Table" = 'ReplicaPlataforma_ResumenReplica' AND WST."Key" = TO_VARCHAR("FechaSistema",'YYYY-MM-DD') || "Nombre"
        	
        	
        	
        	
        	DELETE FROM  BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack" WHERE "Table" = 'Plataforma_ResumenReplica'
        	
Plataforma_ResumenReplica