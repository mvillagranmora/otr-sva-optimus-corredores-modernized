SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Empleado"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Empleado" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdEmpleado"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpleado"  AS "IDEMPLEADO", -- BIGINT
                  "CodigoEmpleado"  AS "CODIGOEMPLEADO", -- VARCHAR(40)
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdSucursal"  AS "IDSUCURSAL", -- BIGINT
                  "FechaCreacion"  AS "FECHACREACION", -- VARCHAR(19)
                  "FechaModificacion"  AS "FECHAMODIFICACION", -- VARCHAR(19)
                  "FechaIngreso"  AS "FECHAINGRESO", -- VARCHAR(19)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "NombreEstadoEmpleado"  AS "NOMBREESTADOEMPLEADO", -- VARCHAR(100)
                  "FechaInicioCargo"  AS "FECHAINICIOCARGO", -- VARCHAR(10)
                  "FechaTerminoCargo"  AS "FECHATERMINOCARGO", -- VARCHAR(10)
                  "CodigoTipoCargoEmpleado"  AS "CODIGOTIPOCARGOEMPLEADO", -- VARCHAR(6)
                  "NombreTipoCargoEmpleado"  AS "NOMBRETIPOCARGOEMPLEADO" -- VARCHAR(100)

        from "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_Empleado' AND WST."Key" = "IdEmpleado"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Empleado" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
