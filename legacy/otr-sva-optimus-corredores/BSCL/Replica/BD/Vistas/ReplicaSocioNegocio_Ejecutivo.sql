SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Ejecutivo"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Ejecutivo" AS

        Select WST."IdStack" AS "IDSTACK", 
				  "IdEmpleadoEjecutivo"  AS TABLE_KEY,
				  "Action" AS ACTION,
                  "IdEmpleadoEjecutivo"  AS "IDEMPLEADOEJECUTIVO", -- BIGINT
                  "IdEmpleadoSupervisor"  AS "IDEMPLEADOSUPERVISOR", -- BIGINT
                  "FechaCreacion"  AS "FECHACREACION", -- VARCHAR(19)
                  "IdUsuarioCreacion"  AS "IDUSUARIOCREACION", -- BIGINT
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "IdEmpleadoReemplaza"  AS "IDEMPLEADOREEMPLAZA", -- BIGINT
                  "FechaInicioReemplazo"  AS "FECHAINICIOREEMPLAZO", -- VARCHAR(19)
                  "FechaFinReemplazo"  AS "FECHAFINREEMPLAZO", -- VARCHAR(19)
                  "NombreCompletoSupevisor"  AS "NOMBRECOMPLETOSUPEVISOR", -- VARCHAR(302)
                  "CodigoEmpleadoSupervisor"  AS "CODIGOEMPLEADOSUPERVISOR", -- VARCHAR(40)
                  "IdPersonaEmpleado"  AS "IDPERSONAEMPLEADO", -- BIGINT
                  "CodigoEmpleado"  AS "CODIGOEMPLEADO", -- VARCHAR(40)
                  "NombreCompleto"  AS "NOMBRECOMPLETO", -- VARCHAR(302)
                  "CodigoEmpleadoReemplazante"  AS "CODIGOEMPLEADOREEMPLAZANTE", -- VARCHAR(40)
                  "NombreCompletoReemplazante"  AS "NOMBRECOMPLETOREEMPLAZANTE" -- VARCHAR(302)

        from "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_Ejecutivo' AND WST."Key" = "IdEmpleadoEjecutivo"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Ejecutivo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
