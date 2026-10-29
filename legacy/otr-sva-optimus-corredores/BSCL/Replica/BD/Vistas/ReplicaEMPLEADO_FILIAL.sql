SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaEMPLEADO_FILIAL"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaEMPLEADO_FILIAL" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,EMP."EMP_CODIGO"  AS TABLE_KEY,
            "EMP_CODIGO", -- VARCHAR(40)
            "FIL_ID", -- INT
            "SUC_ID", -- CHAR(4)
            "EMP_COD_SUPER", -- CHAR(4)
            "CDI_ID_PAC", -- CHAR(4)
            "CDI_ID_DOL", -- CHAR(4)
            "EMP_IND_HAB", -- CHAR(1)
            "EMP_IND_VERCLI", -- CHAR(1)
            "EMP_IND_VERORD", -- CHAR(1)
            "EMP_IND_REEMPLAZO", -- CHAR(1)
            "EMP_IND_VERCLIPRO", -- CHAR(1)
            "PAR_SEG_ID", -- VARCHAR(10)
			"IdEmpleado" AS IDEMPLEADO,
			"IdEmpleadoSupervisor" AS IDEMPLEADOSUPERVISOR
        FROM "BSCL.Replica.BD.Vistas::Sebra.EMPLEADO_FILIAL" EMP
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'EMPLEADO_FILIAL' AND WST."Key" = EMP.EMP_CODIGO

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaEMPLEADO_FILIAL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
