SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Pais"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Pais" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdPais"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdPais"  AS "IDPAIS", -- BIGINT
                  "NombrePais"  AS "NOMBREPAIS", -- VARCHAR(100)
                  "CodigoPais"  AS "CODIGOPAIS" -- VARCHAR(40)

        from "BSCL.Plataforma.BD.Vistas::REG.ConsultarPaisAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Pais' AND WST."Key" = "IdPais"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Pais" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
