SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Ciudad"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Ciudad" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdCiudad"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "NombrePais"  AS "NOMBREPAIS", -- VARCHAR(100)
                  "NombreCiudad"  AS "NOMBRECIUDAD", -- VARCHAR(100)
                  "NombreClasificacionDivisionPais"  AS "NOMBRECLASIFICACIONDIVISIONPAIS", -- VARCHAR(100)
                  "NombreDivisionPais_1"  AS "NOMBREDIVISIONPAIS_1", -- VARCHAR(100)
                  "NombreDivisionPais"  AS "NOMBREDIVISIONPAIS", -- VARCHAR(100)
                  "CodigoPais"  AS "CODIGOPAIS", -- VARCHAR(40)
                  "TipoClasificacionDivisionPais"  AS "TIPOCLASIFICACIONDIVISIONPAIS", -- VARCHAR(40)
                  "IdCiudad"  AS "IDCIUDAD", -- BIGINT
                  "IdPais"  AS "IDPAIS" -- BIGINT

        from "BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudadAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Ciudad' AND WST."Key" = "IdCiudad"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Ciudad" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
