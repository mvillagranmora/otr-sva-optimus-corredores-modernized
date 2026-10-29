SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ClasificacionDivisionPais"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ClasificacionDivisionPais" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		TO_VARCHAR("IdClasificacionDivisionPais")  AS TABLE_KEY,
        		"Action" AS ACTION,
                "IdClasificacionDivisionPais" AS "IDCLASIFICACIONDIVISIONPAIS",
				"IdPais" AS "IDPAIS",
				"NombreClasificacionDivisionPais" AS "NOMBRECLASIFICACIONDIVISIONPAIS",
				"TipoClasificacionDivisionPais" AS "TIPOCLASIFICACIONDIVISIONPAIS"

        from "BSCL.Plataforma.BD.Vistas::REG.ConsultarClasificacionDivisionPaisAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_ClasificacionDivisionPais' AND WST."Key" = "IdClasificacionDivisionPais"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ClasificacionDivisionPais" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
