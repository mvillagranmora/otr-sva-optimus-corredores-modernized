SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Giro"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Giro" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdGiro"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdGiro"  AS "IDGIRO", -- BIGINT
                  "IdPais"  AS "IDPAIS", -- BIGINT
                  "Nombre"  AS "NOMBRE", -- VARCHAR(500)
                  "Codigo"  AS "CODIGO" -- VARCHAR(40)

        from "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiroAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Giro' AND WST."Key" = "IdGiro"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Giro" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
