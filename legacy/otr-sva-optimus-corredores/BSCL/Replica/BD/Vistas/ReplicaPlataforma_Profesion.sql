SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Profesion"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Profesion" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdProfesion"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdProfesion"  AS "IDPROFESION", -- BIGINT
                  "Nombre"  AS "NOMBRE", -- VARCHAR(100)
                  "IdPais"  AS "IDPAIS", -- BIGINT
                  "IdEstado"  AS "IDESTADO" -- VARCHAR(3)

        from "BSCL.Plataforma.BD.Vistas::REG.ConsultarProfesionAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Profesion' AND WST."Key" = "IdProfesion"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Profesion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
