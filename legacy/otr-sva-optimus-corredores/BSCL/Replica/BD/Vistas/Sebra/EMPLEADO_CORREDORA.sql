SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.EMPLEADO_CORREDORA"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.EMPLEADO_CORREDORA" AS

		Select 
			COD_EMP_TRUN4."CodigoEmpleado" AS EMP_CODIGO
			, 0 AS EMP_COD_FM
			,COD_EMP_TRUN4."CodigoEmpleado"  AS EMP_COD_REF
			, CAST( CASE WHEN INSTR(CE."DocumentoIdentidad" , '-') = 0 THEN -1 ELSE trim(LEFT(CE."DocumentoIdentidad"  , INSTR(CE."DocumentoIdentidad" , '-')-1)) END AS INT) AS PER_ID
			--ejecutivo, administrativo, plataforma
			, 'TENTIAGE' AS PAR_TEN_ID
			, LEFT(COALESCE(CASE WHEN "IdPlantillaEmpleado" = '' THEN NULL ELSE "IdPlantillaEmpleado" END, 'DESCONICD'),10) AS PAR_TPA_ID
			, 'TFUNCPLATA' AS PAR_TIF_ID
			, 'CE01' AS CEN_ID
			, 'SC01' AS SUB_ID
			, CASE WHEN CE."IdEstado" = 'ACT' THEN '07' ELSE '08' END AS EST_ID
			, CASE WHEN CE."FechaIngreso" < '1900-01-01' THEN '1900-01-01' ELSE CE."FechaIngreso" end   AS EMP_FEC_INGRESO
			, CASE WHEN CE."FechaModificacion" < '1900-01-01' THEN '1900-01-01' ELSE CE."FechaModificacion" end    AS EMP_FEC_MODIFICA
			, '' AS EMP_CARGO
			, '1900-01-01 00:00:00.000' AS EMP_FEC_MODIFICA_CARGO
			, 0 AS EMP_CODIGO_SIAM
			, '85' AS USUARIO
			,  CASE WHEN CE."FechaInicioCargo" < '1900-01-01' THEN '1900-01-01' ELSE CE."FechaInicioCargo" end  AS FEC_INICIO_FUNCIONES
			, CASE WHEN  COALESCE(CE."FechaTerminoCargo",'0001-01-01') < '1900-01-01' THEN '1900-01-01' ELSE  CE."FechaTerminoCargo" end   AS FEC_TERMINO_FUNCIONES
			, '1900-01-01 00:00:00.000' AS FEC_ACREDITACION
			, 'N' AS INFORMACION_PRIVILEGIADA
			, 'S' AS EMP_CORREDORA
			, COALESCE (CE."IdEmpleado", 0) AS IDEMPLEADO		
		FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE
		LEFT JOIN (
			SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
				SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
			) WHERE RANK < 10
		) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = CE."IdEmpleado"


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.EMPLEADO_CORREDORA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
