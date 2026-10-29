SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ResumenReplica"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ResumenReplica" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  TO_VARCHAR("UltimaActualizacion",'YYYY-MM-DD HH24:MI:SS') || "Nombre"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "Tipo" As "TIPO",
                  "Nombre" AS "NOMBRE",
                  "Estado" AS "ESTADO",
                  "Detalle" AS "DETALLE",
                  TO_VARCHAR("FechaSistema",'YYYY-MM-DD') AS "FECHASISTEMA",
                  TO_VARCHAR("UltimaActualizacion",'YYYY-MM-DD HH24:MI:SS') AS "ULTIMAACTUALIZACION",
			      TO_VARCHAR("FechaReal",'YYYY-MM-DD') AS "FECHAREAL"

        from "BSCL.Plataforma.BD::PLA.ResumenReplica"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
        	ON  WST."Table" = 'Plataforma_ResumenReplica' AND WST."Key" = TO_VARCHAR("UltimaActualizacion",'YYYY-MM-DD HH24:MI:SS') || "Nombre"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ResumenReplica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;