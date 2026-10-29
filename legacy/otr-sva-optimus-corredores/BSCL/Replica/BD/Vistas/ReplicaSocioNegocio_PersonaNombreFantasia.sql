SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaNombreFantasia"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaNombreFantasia" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdNombreFantasia"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdNombreFantasia"  AS "IDNOMBREFANTASIA", -- BIGINT
                  "DescripcionNombreFantasia"  AS "DESCRIPCIONNOMBREFANTASIA", -- VARCHAR(200)
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "FechaCreacion"  AS "FECHACREACION", -- VARCHAR(19)
                  "IdEstado"  AS "IDESTADO" -- VARCHAR(3)

        from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreFantasiaAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_PersonaNombreFantasia' AND WST."Key" = "IdNombreFantasia"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaNombreFantasia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
