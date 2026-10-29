SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.RELACIONES_FUNCIONARIOS"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.RELACIONES_FUNCIONARIOS" AS

		Select 
            COALESCE (CAST(COD_EMPSUP_TRUN4."CodigoEmpleado" AS VARCHAR(4)),'')  AS "EMP_CODIGO", -- VARCHAR(40)
            COALESCE (CAST(COD_EMP_TRUN4."CodigoEmpleado" AS VARCHAR(4)),'')  AS "EMP_CODIGO_REL", -- VARCHAR(40)
            COALESCE ( 0 , 0 )  AS "FIL_ID", -- INT
            COALESCE (CAST('TRFUNSUPER' AS VARCHAR(10)),'')  AS "PAR_REL_FUN", -- VARCHAR(10)
            COALESCE (CASE WHEN EJE."FechaInicioReemplazo" = '0001-01-01' THEN '1900-01-01'
            				ELSE EJE."FechaInicioReemplazo" END , '1900-01-01' )  AS "RFU_DESDE", -- DATETIME
            COALESCE (CASE WHEN EJE."FechaFinReemplazo" = '0001-01-01' THEN '1900-01-01'
            				ELSE EJE."FechaFinReemplazo" END , '1900-01-01' )  AS "RFU_HASTA", -- DATETIME
            COALESCE (CAST(CASE WHEN EJE."IdEstado" = 'ACT' THEN 'S'
            					ELSE 'N' END AS VARCHAR(1)),'')  AS "RFU_VIGENCIA" -- CHAR(1)

        FROM "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE
        INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
        INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMPSUP ON EJE."IdEmpleadoSupervisor" = EMPSUP."IdEmpleado"
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

       	UNION

       	Select 
            COALESCE (CAST(COD_EMPSUP_TRUN4."CodigoEmpleado" AS VARCHAR(4)),'')  AS "EMP_CODIGO", -- VARCHAR(40)
            COALESCE (CAST(COD_EMP_TRUN4."CodigoEmpleado" AS VARCHAR(4)),'')  AS "EMP_CODIGO_REL", -- VARCHAR(40)
            COALESCE ( 0 , 0 )  AS "FIL_ID", -- INT
            COALESCE (CAST('TRFUNREMPL' AS VARCHAR(10)),'')  AS "PAR_REL_FUN", -- VARCHAR(10)
            COALESCE (CASE WHEN EJE."FechaInicioReemplazo" = '0001-01-01' THEN '1900-01-01'
            				ELSE EJE."FechaInicioReemplazo" END , '1900-01-01' )  AS "RFU_DESDE", -- DATETIME
            COALESCE (CASE WHEN EJE."FechaFinReemplazo" = '0001-01-01' THEN '1900-01-01'
            				ELSE EJE."FechaFinReemplazo" END , '1900-01-01' )  AS "RFU_HASTA", -- DATETIME
            COALESCE (CAST(CASE WHEN EJE."IdEstado" = 'ACT' THEN 'S'
            					ELSE 'N' END AS VARCHAR(1)),'')  AS "RFU_VIGENCIA" -- CHAR(1)

        FROM "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE
        INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
        INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMPSUP ON EJE."IdEmpleadoReemplaza" = EMPSUP."IdEmpleado"
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

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.RELACIONES_FUNCIONARIOS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
