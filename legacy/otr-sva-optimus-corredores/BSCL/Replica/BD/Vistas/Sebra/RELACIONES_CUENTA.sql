SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.RELACIONES_CUENTA"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.RELACIONES_CUENTA" AS

	SELECT 
		"IdAutorizado",
		PER_ID,
		CUENTA,
		FIL_ID,
		PER_ID_REL,
		PAR_TRE_ID,
		MIN(REL_FEC_INICIO)  AS REL_FEC_INICIO
		
	FROM (
		SELECT REL."IdRelacionPersona" as "IdAutorizado",
			CAST(
				CASE 
					WHEN instr(POR."DocumentoIdentidad" , '-') = 0 THEN -1 
					ELSE trim(LEFT(POR."DocumentoIdentidad"  , instr(POR."DocumentoIdentidad" , '-')-1)) END AS INT
			) AS "PER_ID", --int] NOT NULL,***
			COALESCE (CAST(POR."CodigoPortafolio" AS VARCHAR(100)),'')  AS "CUENTA", -- VARCHAR(100)
			COALESCE ( 0 , 0 )  AS "FIL_ID", -- INT
			CAST(
				CASE 
					WHEN instr(DOCREL."DocumentoIdentidad" , '-') = 0 THEN -1 
					ELSE trim(LEFT(DOCREL."DocumentoIdentidad"  , instr(DOCREL."DocumentoIdentidad" , '-')-1)) END AS INT
			) AS "PER_ID_REL", -- INT
			CAST(COALESCE( hr."ValorOrigen" ,REL."IdTipoRelacion" )   AS VARCHAR(10)) AS "PAR_TRE_ID",
			CAST(COALESCE ( CASE 
				WHEN REL."FechaInicioRelacion" = '0001-01-01' THEN '1900-01-01' 
				WHEN REL."FechaInicioRelacion" = '' THEN '1900-01-01' 
				ELSE REL."FechaInicioRelacion" END , '1900-01-01' ) AS DATETIME)  AS "REL_FEC_INICIO" -- DATETIME
		FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL
		INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" POR ON POR."IdPersona" = REL."IdPersonaOrigen"
		and por."DocumentoIdentidad" != '15.829.477-K'
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PERREL ON REL."IdPersonaDestino" = PERREL."IdPersona"      
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCREL ON  (DOCREL."IdDocumentoIdentidad" = PERREL."IdDocumentoIdentidad" AND docrel."IdEstado" = 'ACT')
		LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hr 
						ON (hr."TipoValor" = 'TipoRelacion' and hr."ValorDestino" = REL."IdTipoRelacion"  )
		WHERE DOCREL."DocumentoIdentidad" not like '%.%' and POR."DocumentoIdentidad" not like '%.%' 
		AND "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0
		and instr(POR."DocumentoIdentidad" , '-') != 0
		and instr(DOCREL."DocumentoIdentidad" , '-') != 0
		
	) AS tb1
	GROUP BY
		"IdAutorizado",
		PER_ID,
		CUENTA,
		FIL_ID,
		PER_ID_REL,
		PAR_TRE_ID	


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.RELACIONES_CUENTA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
