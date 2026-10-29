SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.EMPLEADO_FILIAL"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.EMPLEADO_FILIAL" AS

        Select 
			COALESCE (CAST(COD_EMP_TRUN4."CodigoEmpleado" AS VARCHAR(4)),'')  AS "EMP_CODIGO", -- VARCHAR(4)
			COALESCE ( 0 , 0 )  AS "FIL_ID", -- INT
			COALESCE (CAST(COALESCE(SUC."CodigoSucursal", CAST(EMP."IdSucursal" AS VARCHAR(4))) AS VARCHAR(4)),'')  AS "SUC_ID", -- CHAR(4)
			COALESCE (CAST(COD_EMPSUP_TRUN4."CodigoEmpleado" AS VARCHAR(4)),'')  AS "EMP_COD_SUPER", -- CHAR(4)
			'CORR'  AS "CDI_ID_PAC", -- CHAR(4)
			'MDOL'  AS "CDI_ID_DOL", -- CHAR(4)
			COALESCE (CAST(CASE WHEN EMP."IdEstado" = 'ACT' THEN 'S'
								WHEN EMP."IdEstado" = 'BLO' THEN 'N'
								ELSE '' END AS VARCHAR(1)),'')  AS "EMP_IND_HAB", -- CHAR(1)
			COALESCE (CAST(CASE WHEN EMP."RestringeCarteraCliente" = 'S' THEN 'N'
								ELSE 'S' END AS VARCHAR(1)),'')  AS "EMP_IND_VERCLI", -- CHAR(1)
			COALESCE (CAST(CASE WHEN EMP."RestringeOrdenCliente" = 'S' THEN 'N'
								ELSE 'S' END AS VARCHAR(1)),'')  AS "EMP_IND_VERORD", -- CHAR(1)
			COALESCE (CAST('N' AS VARCHAR(1)),'')  AS "EMP_IND_REEMPLAZO", -- CHAR(1)
			COALESCE (CAST('N' AS VARCHAR(1)),'')  AS "EMP_IND_VERCLIPRO", -- CHAR(1)
			COALESCE (CAST('FSEGMNINGU' AS VARCHAR(10)),'')  AS "PAR_SEG_ID", -- VARCHAR(10)
			COALESCE (EMP."IdEmpleado", 0) AS "IdEmpleado",
			COALESCE (EMPSUP."IdEmpleadoSupervisor",0) AS "IdEmpleadoSupervisor"
		FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMPSUP ON EMP."IdEmpleadoSupervisor" = EMPSUP."IdEmpleado"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUC ON EMP."IdSucursal" = SUC."IdSucursal"
		LEFT JOIN (
			SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
				SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
			) WHERE RANK < 10
		) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = EMP."IdEmpleado"
		LEFT JOIN (
			SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
				SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
			) WHERE RANK < 10
		) AS COD_EMPSUP_TRUN4 ON COD_EMPSUP_TRUN4."IdEmpleado" = EMPSUP."IdEmpleado"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.EMPLEADO_FILIAL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
