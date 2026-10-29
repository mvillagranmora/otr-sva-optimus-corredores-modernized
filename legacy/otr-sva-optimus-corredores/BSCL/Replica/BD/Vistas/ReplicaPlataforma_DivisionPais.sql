SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_DivisionPais"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_DivisionPais" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdDivisionPais"  AS TABLE_KEY,
        		  "Action" AS ACTION,
        		  "IdDivisionPais" AS "IDDIVISIONPAIS", -- BIGINT
				  "IdPais" AS "IDPAIS", -- BIGINT
				  "NombreDivisionPais" AS "NOMBREDIVISIONPAIS", -- VARCHAR(100)
				  "IdClasificacion2" AS "IDCLASIFICACION2", -- BIGINT
				  "IdClasificacion1" AS "IDCLASIFICACION1", -- BIGINT
				  "IdCiudad" AS "IDCIUDAD" -- BIGINT

        from "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPaisAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_DivisionPais' AND WST."Key" = "IdDivisionPais"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_DivisionPais" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
