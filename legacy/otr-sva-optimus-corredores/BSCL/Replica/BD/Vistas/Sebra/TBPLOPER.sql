SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLOPER"; 

--
--select tipo_operario,lugar_operac,cod_operario,count(1) from bscl."BSCL.Replica.BD.Vistas::Sebra.TBPLOPER" group by tipo_operario,lugar_operac,cod_operario having count(1) > 1
--select * from bscl."BSCL.Replica.BD.Vistas::Sebra.TBPLOPER" where tipo_Operario = 'AG' AND COD_operario = 'MBI '

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLOPER" AS

	SELECT 
    EMP."IdEmpleado",
 	TIPO_OPERARIO,-- char(2) NOT NULL, ******************** 
	LUGAR_OPERAC,-- char(4) NOT NULL,
	COD_EMP_TRUN4."CodigoEmpleado" as COD_OPERARIO,-- char(4) NOT NULL, ********************
	CLAVE_LOGIN,-- decimal(3, 0) NOT NULL,
	LEFT(DOC."DocumentoIdentidad",11) AS RUT_OPERARIO,-- char(11) NOT NULL,  ********************
	COD_GASTO_OPER,-- char(4) NOT NULL,
	CARGO,-- char(20) NOT NULL,
	CASILLA,-- char(15) NOT NULL,
	CENTRO_OPER,-- char(4) NOT NULL,
	CIUDAD,-- char(4) NOT NULL,
	COD_MESA_MON,-- char(4) NOT NULL,
	COD_MESA_PAC,-- char(4) NOT NULL,
	COMUNA,-- char(4) NOT NULL,
	DIRECCION,-- char(40) NOT NULL,
	FAX,-- char(20) NOT NULL,
	IND_CAJA_CHICA,-- char(1) NOT NULL,
	IND_HAB_OPER,-- char(1) NOT NULL,
	999999 as MONTO_MAX_MESA,-- decimal(6, 0) NOT NULL,
	CASE PER."Tipo" 
		WHEN 'N' THEN LEFT(CONCAT(PER."Nombres", CONCAT(' ', CONCAT(PER."ApellidoPaterno", CONCAT(' ', PER."ApellidoMaterno")))),50) 
		WHEN 'J' THEN LEFT(PER."RazonSocial",50)
		ELSE ''
	END as NOMBRE_OPERARIO,-- char(50) NOT NULL,  ********************
	PAIS,-- char(4) NOT NULL,
	SEC_RUT_OPER,-- decimal(2, 0) NOT NULL,
	SUBCENTRO_OPER,-- char(4) NOT NULL,
	TELEFONO,-- char(20) NOT NULL,
	TIPO_COMIS,-- char(2) NOT NULL,
	TIPO_FUNC,-- char(2) NOT NULL,
	TIPO_INCEN,-- char(2) NOT NULL,
	VAL_COMIS,-- decimal(15, 4) NOT NULL,
	VAL_INCEN,-- decimal(15, 4) NOT NULL,
	VER_AGE_01,-- char(4) NOT NULL,
	VER_AGE_02,-- char(4) NOT NULL,
	VER_CLI_TODOS,-- char(1) NOT NULL,
	VER_ORD_TODAS,-- char(1) NOT NULL,
	IND_SOBREGIRO,-- char(1) NOT NULL,
	PASSWORD,-- char(8) NOT NULL,
	IND_EXP_PASS,-- char(1) NOT NULL,
	DIAS_EXP_PASS,-- decimal(3, 0) NOT NULL,
	FEC_EXP_PASS,-- char(10) NOT NULL,
	AGENTE_BCI,-- char(10) NOT NULL,
	IND_SIMUL_EAI,-- char(1) NOT NULL,
	 DIREC_INTERNET,-- char(100) NOT NULL,
	 IND_VEN_UTIL

FROM (
	SELECT
	 --WST."IdStack" AS "IDSTACK",
	'AG' AS TIPO_OPERARIO,-- char(2) NOT NULL, ******************** 
	'' AS LUGAR_OPERAC,-- char(4) NOT NULL,
	'' AS COD_OPERARIO,-- char(4) NOT NULL, ********************
	0 AS CLAVE_LOGIN,-- decimal(3, 0) NOT NULL,
	'' AS RUT_OPERARIO,-- char(11) NOT NULL,  ********************
	'' AS COD_GASTO_OPER,-- char(4) NOT NULL,
	'' AS CARGO,-- char(20) NOT NULL,
	'' AS CASILLA,-- char(15) NOT NULL,
	'' AS CENTRO_OPER,-- char(4) NOT NULL,
	'' AS CIUDAD,-- char(4) NOT NULL,
	'' AS COD_MESA_MON,-- char(4) NOT NULL,
	'' AS COD_MESA_PAC,-- char(4) NOT NULL,
	'' AS COMUNA,-- char(4) NOT NULL,
	'' AS DIRECCION,-- char(40) NOT NULL,
	'' AS FAX,-- char(20) NOT NULL,
	'' AS IND_CAJA_CHICA,-- char(1) NOT NULL,
	'' AS IND_HAB_OPER,-- char(1) NOT NULL,
	0 AS MONTO_MAX_MESA,-- decimal(6, 0) NOT NULL,
	'' AS NOMBRE_OPERARIO,-- char(50) NOT NULL,  ********************
	'' AS PAIS,-- char(4) NOT NULL,
	0 AS SEC_RUT_OPER,-- decimal(2, 0) NOT NULL,
	'' AS SUBCENTRO_OPER,-- char(4) NOT NULL,
	'' AS TELEFONO,-- char(20) NOT NULL,
	'' AS TIPO_COMIS,-- char(2) NOT NULL,
	'' AS TIPO_FUNC,-- char(2) NOT NULL,
	'' AS TIPO_INCEN,-- char(2) NOT NULL,
	0 AS VAL_COMIS,-- decimal(15, 4) NOT NULL,
	0 AS VAL_INCEN,-- decimal(15, 4) NOT NULL,
	'' AS VER_AGE_01,-- char(4) NOT NULL,
	'' AS VER_AGE_02,-- char(4) NOT NULL,
	'' AS VER_CLI_TODOS,-- char(1) NOT NULL,
	'' AS VER_ORD_TODAS,-- char(1) NOT NULL,
	'' AS IND_SOBREGIRO,-- char(1) NOT NULL,
	'' AS PASSWORD,-- char(8) NOT NULL,
	'' AS IND_EXP_PASS,-- char(1) NOT NULL,
	0 AS DIAS_EXP_PASS,-- decimal(3, 0) NOT NULL,
	'' AS FEC_EXP_PASS,-- char(10) NOT NULL,
	'' AS AGENTE_BCI,-- char(10) NOT NULL,
	'' AS IND_SIMUL_EAI,-- char(1) NOT NULL,
	'' AS DIREC_INTERNET,-- char(100) NOT NULL,
	'' AS IND_VEN_UTIL-- char(1) NOT NULL,
	FROM DUMMY
	UNION	
	SELECT
		 --WST."IdStack" AS "IDSTACK",
		'AS' AS TIPO_OPERARIO,-- char(2) NOT NULL, ******************** 
		'' AS LUGAR_OPERAC,-- char(4) NOT NULL,
		'' AS COD_OPERARIO,-- char(4) NOT NULL, ********************
		0 AS CLAVE_LOGIN,-- decimal(3, 0) NOT NULL,
		'' AS RUT_OPERARIO,-- char(11) NOT NULL,  ********************
		'' AS COD_GASTO_OPER,-- char(4) NOT NULL,
		'' AS CARGO,-- char(20) NOT NULL,
		'' AS CASILLA,-- char(15) NOT NULL,
		'' AS CENTRO_OPER,-- char(4) NOT NULL,
		'' AS CIUDAD,-- char(4) NOT NULL,
		'' AS COD_MESA_MON,-- char(4) NOT NULL,
		'' AS COD_MESA_PAC,-- char(4) NOT NULL,
		'' AS COMUNA,-- char(4) NOT NULL,
		'' AS DIRECCION,-- char(40) NOT NULL,
		'' AS FAX,-- char(20) NOT NULL,
		'' AS IND_CAJA_CHICA,-- char(1) NOT NULL,
		'' AS IND_HAB_OPER,-- char(1) NOT NULL,
		0 AS MONTO_MAX_MESA,-- decimal(6, 0) NOT NULL,
		'' AS NOMBRE_OPERARIO,-- char(50) NOT NULL,  ********************
		'' AS PAIS,-- char(4) NOT NULL,
		0 AS SEC_RUT_OPER,-- decimal(2, 0) NOT NULL,
		'' AS SUBCENTRO_OPER,-- char(4) NOT NULL,
		'' AS TELEFONO,-- char(20) NOT NULL,
		'' AS TIPO_COMIS,-- char(2) NOT NULL,
		'' AS TIPO_FUNC,-- char(2) NOT NULL,
		'' AS TIPO_INCEN,-- char(2) NOT NULL,
		0 AS VAL_COMIS,-- decimal(15, 4) NOT NULL,
		0 AS VAL_INCEN,-- decimal(15, 4) NOT NULL,
		'' AS VER_AGE_01,-- char(4) NOT NULL,
		'' AS VER_AGE_02,-- char(4) NOT NULL,
		'' AS VER_CLI_TODOS,-- char(1) NOT NULL,
		'' AS VER_ORD_TODAS,-- char(1) NOT NULL,
		'' AS IND_SOBREGIRO,-- char(1) NOT NULL,
		'' AS PASSWORD,-- char(8) NOT NULL,
		'' AS IND_EXP_PASS,-- char(1) NOT NULL,
		0 AS DIAS_EXP_PASS,-- decimal(3, 0) NOT NULL,
		'' AS FEC_EXP_PASS,-- char(10) NOT NULL,
		'' AS AGENTE_BCI,-- char(10) NOT NULL,
		'' AS IND_SIMUL_EAI,-- char(1) NOT NULL,
		'' AS DIREC_INTERNET,-- char(100) NOT NULL,
		'' AS IND_VEN_UTIL-- char(1) NOT NULL,
		FROM DUMMY
	 	UNION	
     	SELECT
		 --WST."IdStack" AS "IDSTACK",
		'CP' AS TIPO_OPERARIO,-- char(2) NOT NULL, ******************** 
		'' AS LUGAR_OPERAC,-- char(4) NOT NULL,
		'' AS COD_OPERARIO,-- char(4) NOT NULL, ********************
		0 AS CLAVE_LOGIN,-- decimal(3, 0) NOT NULL,
		'' AS RUT_OPERARIO,-- char(11) NOT NULL,  ********************
		'' AS COD_GASTO_OPER,-- char(4) NOT NULL,
		'' AS CARGO,-- char(20) NOT NULL,
		'' AS CASILLA,-- char(15) NOT NULL,
		'' AS CENTRO_OPER,-- char(4) NOT NULL,
		'' AS CIUDAD,-- char(4) NOT NULL,
		'' AS COD_MESA_MON,-- char(4) NOT NULL,
		'' AS COD_MESA_PAC,-- char(4) NOT NULL,
		'' AS COMUNA,-- char(4) NOT NULL,
		'' AS DIRECCION,-- char(40) NOT NULL,
		'' AS FAX,-- char(20) NOT NULL,
		'' AS IND_CAJA_CHICA,-- char(1) NOT NULL,
		'' AS IND_HAB_OPER,-- char(1) NOT NULL,
		0 AS MONTO_MAX_MESA,-- decimal(6, 0) NOT NULL,
		'' AS NOMBRE_OPERARIO,-- char(50) NOT NULL,  ********************
		'' AS PAIS,-- char(4) NOT NULL,
		0 AS SEC_RUT_OPER,-- decimal(2, 0) NOT NULL,
		'' AS SUBCENTRO_OPER,-- char(4) NOT NULL,
		'' AS TELEFONO,-- char(20) NOT NULL,
		'' AS TIPO_COMIS,-- char(2) NOT NULL,
		'' AS TIPO_FUNC,-- char(2) NOT NULL,
		'' AS TIPO_INCEN,-- char(2) NOT NULL,
		0 AS VAL_COMIS,-- decimal(15, 4) NOT NULL,
		0 AS VAL_INCEN,-- decimal(15, 4) NOT NULL,
		'' AS VER_AGE_01,-- char(4) NOT NULL,
		'' AS VER_AGE_02,-- char(4) NOT NULL,
		'' AS VER_CLI_TODOS,-- char(1) NOT NULL,
		'' AS VER_ORD_TODAS,-- char(1) NOT NULL,
		'' AS IND_SOBREGIRO,-- char(1) NOT NULL,
		'' AS PASSWORD,-- char(8) NOT NULL,
		'' AS IND_EXP_PASS,-- char(1) NOT NULL,
		0 AS DIAS_EXP_PASS,-- decimal(3, 0) NOT NULL,
		'' AS FEC_EXP_PASS,-- char(10) NOT NULL,
		'' AS AGENTE_BCI,-- char(10) NOT NULL,
		'' AS IND_SIMUL_EAI,-- char(1) NOT NULL,
		'' AS DIREC_INTERNET,-- char(100) NOT NULL,
		'' AS IND_VEN_UTIL-- char(1) NOT NULL,
		FROM DUMMY
		UNION
		SELECT
		 --WST."IdStack" AS "IDSTACK",
		'OD' AS TIPO_OPERARIO,-- char(2) NOT NULL, ******************** 
		'' AS LUGAR_OPERAC,-- char(4) NOT NULL,
		'' AS COD_OPERARIO,-- char(4) NOT NULL, ********************
		0 AS CLAVE_LOGIN,-- decimal(3, 0) NOT NULL,
		'' AS RUT_OPERARIO,-- char(11) NOT NULL,  ********************
		'' AS COD_GASTO_OPER,-- char(4) NOT NULL,
		'' AS CARGO,-- char(20) NOT NULL,
		'' AS CASILLA,-- char(15) NOT NULL,
		'' AS CENTRO_OPER,-- char(4) NOT NULL,
		'' AS CIUDAD,-- char(4) NOT NULL,
		'' AS COD_MESA_MON,-- char(4) NOT NULL,
		'' AS COD_MESA_PAC,-- char(4) NOT NULL,
		'' AS COMUNA,-- char(4) NOT NULL,
		'' AS DIRECCION,-- char(40) NOT NULL,
		'' AS FAX,-- char(20) NOT NULL,
		'' AS IND_CAJA_CHICA,-- char(1) NOT NULL,
		'' AS IND_HAB_OPER,-- char(1) NOT NULL,
		0 AS MONTO_MAX_MESA,-- decimal(6, 0) NOT NULL,
		'' AS NOMBRE_OPERARIO,-- char(50) NOT NULL,  ********************
		'' AS PAIS,-- char(4) NOT NULL,
		0 AS SEC_RUT_OPER,-- decimal(2, 0) NOT NULL,
		'' AS SUBCENTRO_OPER,-- char(4) NOT NULL,
		'' AS TELEFONO,-- char(20) NOT NULL,
		'' AS TIPO_COMIS,-- char(2) NOT NULL,
		'' AS TIPO_FUNC,-- char(2) NOT NULL,
		'' AS TIPO_INCEN,-- char(2) NOT NULL,
		0 AS VAL_COMIS,-- decimal(15, 4) NOT NULL,
		0 AS VAL_INCEN,-- decimal(15, 4) NOT NULL,
		'' AS VER_AGE_01,-- char(4) NOT NULL,
		'' AS VER_AGE_02,-- char(4) NOT NULL,
		'' AS VER_CLI_TODOS,-- char(1) NOT NULL,
		'' AS VER_ORD_TODAS,-- char(1) NOT NULL,
		'' AS IND_SOBREGIRO,-- char(1) NOT NULL,
		'' AS PASSWORD,-- char(8) NOT NULL,
		'' AS IND_EXP_PASS,-- char(1) NOT NULL,
		0 AS DIAS_EXP_PASS,-- decimal(3, 0) NOT NULL,
		'' AS FEC_EXP_PASS,-- char(10) NOT NULL,
		'' AS AGENTE_BCI,-- char(10) NOT NULL,
		'' AS IND_SIMUL_EAI,-- char(1) NOT NULL,
		'' AS DIREC_INTERNET,-- char(100) NOT NULL,
		'' AS IND_VEN_UTIL-- char(1) NOT NULL,
		FROM DUMMY
		UNION	
		SELECT
			 --WST."IdStack" AS "IDSTACK",
			'OP' AS TIPO_OPERARIO,-- char(2) NOT NULL, ******************** 
			'' AS LUGAR_OPERAC,-- char(4) NOT NULL,
			'' AS COD_OPERARIO,-- char(4) NOT NULL, ********************
			0 AS CLAVE_LOGIN,-- decimal(3, 0) NOT NULL,
			'' AS RUT_OPERARIO,-- char(11) NOT NULL,  ********************
			'' AS COD_GASTO_OPER,-- char(4) NOT NULL,
			'' AS CARGO,-- char(20) NOT NULL,
			'' AS CASILLA,-- char(15) NOT NULL,
			'' AS CENTRO_OPER,-- char(4) NOT NULL,
			'' AS CIUDAD,-- char(4) NOT NULL,
			'' AS COD_MESA_MON,-- char(4) NOT NULL,
			'' AS COD_MESA_PAC,-- char(4) NOT NULL,
			'' AS COMUNA,-- char(4) NOT NULL,
			'' AS DIRECCION,-- char(40) NOT NULL,
			'' AS FAX,-- char(20) NOT NULL,
			'' AS IND_CAJA_CHICA,-- char(1) NOT NULL,
			'' AS IND_HAB_OPER,-- char(1) NOT NULL,
			0 AS MONTO_MAX_MESA,-- decimal(6, 0) NOT NULL,
			'' AS NOMBRE_OPERARIO,-- char(50) NOT NULL,  ********************
			'' AS PAIS,-- char(4) NOT NULL,
			0 AS SEC_RUT_OPER,-- decimal(2, 0) NOT NULL,
			'' AS SUBCENTRO_OPER,-- char(4) NOT NULL,
			'' AS TELEFONO,-- char(20) NOT NULL,
			'' AS TIPO_COMIS,-- char(2) NOT NULL,
			'' AS TIPO_FUNC,-- char(2) NOT NULL,
			'' AS TIPO_INCEN,-- char(2) NOT NULL,
			0 AS VAL_COMIS,-- decimal(15, 4) NOT NULL,
			0 AS VAL_INCEN,-- decimal(15, 4) NOT NULL,
			'' AS VER_AGE_01,-- char(4) NOT NULL,
			'' AS VER_AGE_02,-- char(4) NOT NULL,
			'' AS VER_CLI_TODOS,-- char(1) NOT NULL,
			'' AS VER_ORD_TODAS,-- char(1) NOT NULL,
			'' AS IND_SOBREGIRO,-- char(1) NOT NULL,
			'' AS PASSWORD,-- char(8) NOT NULL,
			'' AS IND_EXP_PASS,-- char(1) NOT NULL,
			0 AS DIAS_EXP_PASS,-- decimal(3, 0) NOT NULL,
			'' AS FEC_EXP_PASS,-- char(10) NOT NULL,
			'' AS AGENTE_BCI,-- char(10) NOT NULL,
			'' AS IND_SIMUL_EAI,-- char(1) NOT NULL,
			'' AS DIREC_INTERNET,-- char(100) NOT NULL,
			'' AS IND_VEN_UTIL-- char(1) NOT NULL,
			FROM DUMMY

) AS DUMMYTABLE  CROSS JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP 
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (EMP."IdPersona" = PER."IdPersona")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad")
	LEFT JOIN (
		SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
			SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
		) WHERE RANK < 10
	) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = EMP."IdEmpleado"
	
  WHERE DOC."IdPersona" IS NOT NULL 	


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBPLOPER" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
