SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.CLIENTE"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.CLIENTE" AS

 
	Select 
        	
            TO_INT(CAST(
				CASE WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END  AS varchar))  AS "PER_ID", -- INT
            COALESCE ( 0 , 0 )  AS "FIL_ID", -- INT
            COALESCE (CAST(CASE
            		WHEN CLI."IdEstado" = 'ACT' THEN '07'
            		WHEN CLI."IdEstado" = 'BLO' THEN '15'
            		ELSE '07'
            	END AS VARCHAR(2)),'')  AS "EST_ID", -- VARCHAR(2)
			COALESCE (CAST(CASE
            		WHEN CLI."IdEstado" = 'ACT' THEN 'AGE'
            		WHEN CLI."IdEstado" = 'BLO' THEN 'INT'
            		ELSE 'AGE'
            	END AS VARCHAR(3)),'')  AS "CCA_CODIGO", -- CHAR(3)
            'NING'  AS "CLI_MOD_OPERA", -- CHAR(4) -- 
            ''  AS "CLI_ALIAS", -- VARCHAR(30) --
            COALESCE (CAST('S' AS VARCHAR(1)),'')  AS "CLI_IND_FICHA", -- CHAR(1)
            TO_INT(COALESCE (CLI."IdCliente" , 0 )) AS "CLI_NUM_FICHA", -- INT
            COALESCE (CAST('N' AS VARCHAR(1)),'')  AS "CLI_INF_PRIV", -- CHAR(1)
            COALESCE (CAST(CASE --SELECT * FROM "BSCL.Homologacion.BD::HOM.Homologacion" WHERE "TipoValor"='TipoRelacionCI1237'
					WHEN i_svs."CodigoValorIndicador" = 'OR' THEN 'TCSVSORELA' --RELACLABOR/TCSVSJMESA
					WHEN i_svs."CodigoValorIndicador" = 'GA' THEN 'TCSVSGAREA'
					WHEN i_svs."CodigoValorIndicador" = 'GG' THEN 'TCSVSGGRAL'
					WHEN i_svs."CodigoValorIndicador" = 'OM' THEN 'TCSVSOMESA'
					ELSE 'DESCONOCID'
				END AS VARCHAR(10)),'DESCONOCID')  AS "PAR_TIP_SVS", -- VARCHAR(10) --tipo relacion svs
			COALESCE (CAST(CASE --SELECT * FROM "BSCL.Homologacion.BD::HOM.Homologacion" WHERE "TipoValor"='TipoRelacionCliente'
					WHEN i_rel."CodigoValorIndicador" = 'ADMINISTRACION' THEN 'RELACADMIN'
					WHEN i_rel."CodigoValorIndicador" = 'LABORAL' THEN 'RELACLABOR'
					WHEN i_rel."CodigoValorIndicador" = 'PROPIEDAD' THEN 'RELACPROPI'
					WHEN i_rel."CodigoValorIndicador" = 'NOVINCULADO' THEN 'RELACNINGU'
					ELSE 'RELACNINGU' 
				END AS VARCHAR(10)), 'RELACNINGU')  AS "PAR_REL_CORRE", -- VARCHAR(10) --relacion ng380
			COALESCE (CAST(CASE --SELECT * FROM "BSCL.Homologacion.BD::HOM.Homologacion" WHERE "TipoValor"='TipoRelacionNCG69'
					WHEN i_ncg."CodigoValorIndicador" = 'ADMINISTRACION' THEN 'TCNCGOPERS'
					WHEN i_ncg."CodigoValorIndicador" = 'GERENTE' THEN 'TCNCGGEREN'
					WHEN i_ncg."CodigoValorIndicador" = 'OTRO' THEN 'TCNCGAPODE'
					WHEN i_ncg."CodigoValorIndicador" = 'DIRECTOR' THEN 'TCNCGDIREC'
					WHEN i_ncg."CodigoValorIndicador" = 'OPERADOR' THEN 'TCNCGOPERA'
					ELSE 'DESCONOCID' 
				END AS VARCHAR(10)), 'DESCONOCID')  AS "PAR_TIP_NCG", -- VARCHAR(10) --indicador ncg
            COALESCE (CAST('N' AS VARCHAR(1)),'')  AS "CLI_EMI_DTE", -- CHAR(1)
            COALESCE (CAST('AMBOS' AS VARCHAR(5)),'')  AS "CLI_DES_DTE", -- CHAR(5)
            COALESCE (CAST(CLI."ComentarioBloqueo" AS VARCHAR(200)),'')  AS "CLI_OBS_BLOQUEO", -- VARCHAR(200)
            COALESCE (CASE WHEN CLI."FechaIngreso" < '1900-01-01' THEN '1900-01-01' ELSE CLI."FechaIngreso" END   , '1900-01-01' )  AS "CLI_FEC_INGRESO", -- DATETIME
            COALESCE (CASE WHEN CLI."FechaModificacion" < '1900-01-01' THEN '1900-01-01' ELSE CLI."FechaModificacion" END    , '1900-01-01' )  AS "CLI_FEC_MODIFICA", -- DATETIME
            CAST(coalesce(COD_EMP_TRUN4."CodigoEmpleado",'') AS VARCHAR(4))  AS "EMP_CODIGO", -- CHAR(4)
            COALESCE ( 0 , 0 )  AS "FOLIOCONDGENERALESRF", -- NUMERIC
            '1900-01-01 00:00:00'  AS "FECHACONDGENERALESRF", -- DATETIME
            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "COD_BANCO", -- CHAR(10)
            COALESCE (CAST('' AS VARCHAR(255)),'')  AS "CLI_OBS_NCG", -- VARCHAR(255)
            COALESCE (CAST(CASE 
				WHEN CLI."IdTipoCliente" = 'INSTITUCIONAL' THEN 'TCLIINS' 
				WHEN CLI."IdTipoCliente" = 'INTERPOSITA' THEN 'TCLIINTER' 
				ELSE 'TCLIINVER' 
			END AS VARCHAR(10)),'')  AS "TIPO_CLIENTE", -- VARCHAR(10) --??
            COALESCE (CAST('N' AS VARCHAR(1)),'')  AS "CLI_SUS_CON_INT", -- CHAR(1)	--ver si existe como indicador ??
            COALESCE (CAST('RELSININGU' AS VARCHAR(10)),'')  AS "PAR_REL_SII", -- VARCHAR(10)	--indicadores ??
            COALESCE (CAST('REDCVNOREL' AS VARCHAR(10)),'')  AS "PAR_REL_DCV", -- VARCHAR(10) --indicadores ??
            COALESCE (CAST('REBCSNOREL' AS VARCHAR(10)),'')  AS "PAR_REL_BCS", -- VARCHAR(10) --indicadores ??
            TO_DECIMAL(COALESCE (CASE i_cop."ValorIndicador" WHEN '' THEN 0 ELSE i_cop."ValorIndicador" END , 0 ))  AS "COPER", -- DECIMAL
            TO_DECIMAL(COALESCE (CASE i_copro."ValorIndicador" WHEN '' THEN 0 ELSE i_copro."ValorIndicador" END , 0))  AS "COPEROBO", -- DECIMAL
			CLI."IdPersona"
        FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"  AND  DOC."IdTipoDocumentoIdentidad" = 'R'
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" EJE ON EJE."IdPersonaEjecutivo" = CLI."IdPersonaEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" i_svs ON i_svs."CodigoTipoIndicador" = 'CI1237' AND i_svs."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" i_rel ON i_rel."CodigoTipoIndicador" = '1064PROP' AND i_rel."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" i_ncg ON i_ncg."CodigoTipoIndicador" = 'NCG69PROP' AND i_ncg."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" i_cop ON i_cop."CodigoTipoIndicador" = 'COPER' AND i_cop."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" i_copro ON i_copro."CodigoTipoIndicador" = 'COPEROBO' AND i_copro."IdCliente" = CLI."IdCliente"
		LEFT JOIN (
			SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
				SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
			) WHERE RANK < 10
		) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = EJE."IdEmpleadoEjecutivo"
		WHERE DOC."DocumentoIdentidad" not like '%.%'
			
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.CLIENTE" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
