SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.DETALLE_ANOTACION"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.DETALLE_ANOTACION" AS

Select 
            COALESCE (ANOT."IdAnotacion"*-1 , 0 )  AS "DAN_ID", -- INT
            COALESCE (CAST(CASE WHEN ANOT."IdMotivoAnotacion" = 'ENVCAR' THEN 'TANOTCARTO'
            					WHEN ANOT."IdMotivoAnotacion" = 'EVTBUR' THEN 'TANOTEBURS'
            					ELSE 'TANOTRECOR' END AS VARCHAR(10)),'')  AS "PAR_TAN_ID", -- VARCHAR(10)
            COALESCE (CAST(EJE."CodigoEmpleado" AS VARCHAR(4)),'0000')  AS "EMP_CODIGO", -- CHAR(4)
            CAST(
				CASE WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END  AS varchar)  AS "PER_ID", -- INT
            '0'  AS "CUENTA", -- VARCHAR(100)
            COALESCE ( 0 , 0 )  AS "FIL_ID", -- INT
            COALESCE (CAST(ANOT."Titulo" AS VARCHAR(50)),'')  AS "DAN_TITULO", -- VARCHAR(50)
            COALESCE (CAST(ANOT."Detalle" AS VARCHAR(200)),'')  AS "DAN_NOTA", -- VARCHAR(200)
            
            to_nvarchar(COALESCE (CASE WHEN ANOT."FechaIngreso" < '1900-01-01' THEN '1900-01-01' ELSE ANOT."FechaIngreso" end   , '1900-01-01' ), 'YYYY-MM-DD HH:MI:SS')  AS "DAN_FEC_INGRESO", -- DATETIME
            to_nvarchar(COALESCE (CASE WHEN ANOT."ValidoDesde" < '1900-01-01' THEN '1900-01-01' ELSE ANOT."ValidoDesde" end   , '1900-01-01' ), 'YYYY-MM-DD HH:MI:SS')  AS "DAN_FEC_INICIO", -- DATETIME
            to_nvarchar(COALESCE (CASE WHEN ANOT."ValidoHasta" < '1900-01-01' THEN '1900-01-01' ELSE ANOT."ValidoHasta" end   , '1900-01-01' ), 'YYYY-MM-DD HH:MI:SS')  AS "DAN_FEC_VENCE", -- DATETIME
            
            COALESCE (CAST(CASE WHEN ANOT."IdAmbitoAnotacion" = 'PUBLIC' THEN 'PUB'
            					WHEN ANOT."IdAmbitoAnotacion" = 'PRIVAD' THEN 'PRI'
            					ELSE '' END AS VARCHAR(3)),'')  AS "DAN_IND_AMBITO", -- CHAR(3)
            COALESCE (CAST(CASE WHEN ANOT."IdRelevanciaAnotacion" = 'ALTA' THEN 'S'
            					WHEN ANOT."IdRelevanciaAnotacion" = 'NORMAL' THEN 'N'
            					ELSE 'N' END AS VARCHAR(3)),'')   AS "DAN_IND_RELEVANCIA", -- CHAR(1)		
            COALESCE (CAST('N' AS VARCHAR(1)),'')  AS "DAN_USO_CARTOLA", -- CHAR(1)
			'C' AS "Tipo"
        FROM "BSCL.SocioNegocio.BD::CLI.Anotacion" ANOT
        INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON ANOT."IdCliente" = CLI."IdCliente"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EJE ON CLI."IdPersonaEjecutivo" = EJE."IdPersona"
       	UNION all
        Select 
            COALESCE (ANOT."IdAnotacion" , 0 )  AS "DAN_ID", -- INT
            COALESCE (CAST(CASE WHEN ANOT."IdMotivoAnotacion" = 'ENVCAR' THEN 'TANOTCARTO'
            					WHEN ANOT."IdMotivoAnotacion" = 'EVTBUR' THEN 'TANOTEBURS'
            					ELSE 'TANOTRECOR' END AS VARCHAR(10)),'')  AS "PAR_TAN_ID", -- VARCHAR(10)
            					
            COALESCE (CAST(COD_EMP_TRUN4."CodigoEmpleado" AS VARCHAR(4)),'0000')  AS "EMP_CODIGO", -- CHAR(4)
            CAST(
				CASE WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END  AS varchar)  AS "PER_ID", -- INT
				
            COALESCE("CodigoPortafolio",'-1')  AS "CUENTA", -- VARCHAR(100)
            COALESCE ( 0 , 0 )  AS "FIL_ID", -- INT
            COALESCE (CAST(ANOT."Titulo" AS VARCHAR(50)),'')  AS "DAN_TITULO", -- VARCHAR(50)
            COALESCE (CAST(ANOT."Detalle" AS VARCHAR(200)),'')  AS "DAN_NOTA", -- VARCHAR(200)
            
            to_nvarchar(COALESCE (CASE WHEN ANOT."FechaIngreso" < '1900-01-01' THEN '1900-01-01' ELSE ANOT."FechaIngreso" end   , '1900-01-01' ), 'YYYY-MM-DD HH:MI:SS')  AS "DAN_FEC_INGRESO", -- DATETIME
            to_nvarchar(COALESCE (CASE WHEN ANOT."ValidoDesde" < '1900-01-01' THEN '1900-01-01' ELSE ANOT."ValidoDesde" end   , '1900-01-01' ), 'YYYY-MM-DD HH:MI:SS')  AS "DAN_FEC_INICIO", -- DATETIME
            to_nvarchar(COALESCE (CASE WHEN ANOT."ValidoHasta" < '1900-01-01' THEN '1900-01-01' ELSE ANOT."ValidoHasta" end   , '1900-01-01' ), 'YYYY-MM-DD HH:MI:SS')  AS "DAN_FEC_VENCE", -- DATETIME
            
            COALESCE (CAST(CASE WHEN ANOT."IdAmbitoAnotacion" = 'PUBLIC' THEN 'PUB'
            					WHEN ANOT."IdAmbitoAnotacion" = 'PRIVAD' THEN 'PRI'
            					ELSE '' END AS VARCHAR(3)),'')  AS "DAN_IND_AMBITO", -- CHAR(3)
            COALESCE (CAST(CASE WHEN ANOT."IdRelevanciaAnotacion" = 'ALTA' THEN 'S'
            					WHEN ANOT."IdRelevanciaAnotacion" = 'NORMAL' THEN 'N'
            					ELSE 'N' END AS VARCHAR(3)),'')   AS "DAN_IND_RELEVANCIA", -- CHAR(1)		
            COALESCE (CAST('N' AS VARCHAR(1)),'')  AS "DAN_USO_CARTOLA", -- CHAR(1)
			'P' AS "Tipo"
        FROM "BSCL.Portafolio.BD::POR.Anotacion" ANOT
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON ANOT."IdPortafolio" = POR."IdPortafolio"
        INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"		
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
		--LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" EJE ON POR."IdEmpleadoEjecutivo" = EJE."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EJE ON POR."IdEmpleadoEjecutivo" = EJE."IdEmpleado"
		LEFT JOIN (
			SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
				SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
			) WHERE RANK < 10
		) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = EJE."IdEmpleado"
		
		WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.DETALLE_ANOTACION" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

