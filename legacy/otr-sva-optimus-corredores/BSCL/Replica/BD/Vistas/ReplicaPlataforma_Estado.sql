SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Estado"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Estado" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "Modulo" || "ID"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "ID"  AS "ID", -- BIGINT
                  "Modulo"  AS "MODULO", -- VARCHAR(100)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "Nombre"  AS "NOMBRE", -- VARCHAR(100)
                  "NombreEntidad"  AS "NOMBREENTIDAD" -- VARCHAR(100)

        from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Estado' 
        AND WST."Key" = "Modulo" || "ID"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Estado" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
