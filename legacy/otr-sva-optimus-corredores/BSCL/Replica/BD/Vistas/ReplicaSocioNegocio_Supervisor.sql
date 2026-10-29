SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Supervisor"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Supervisor" AS

        Select WST."IdStack" AS "IDSTACK", 
				  "IdEmpleadoSupervisor"  AS TABLE_KEY,
				  "Action" AS ACTION,
                  "IdEmpleadoSupervisor"  AS "IDEMPLEADOSUPERVISOR", -- BIGINT
                  "FechaCreacion"  AS "FECHACREACION", -- VARCHAR(19)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "CodigoEmpleado"  AS "CODIGOEMPLEADO", -- VARCHAR(40)
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "NombreCompleto"  AS "NOMBRECOMPLETO" -- VARCHAR(302)

        from "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisorAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_Supervisor' AND WST."Key" = "IdEmpleadoSupervisor"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Supervisor" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
