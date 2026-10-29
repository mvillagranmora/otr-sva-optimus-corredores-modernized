WITH CTE_PERSONA_ORIGEN AS (
		SELECT
			Persona."IdPersona"				AS "IdPersona"
			, Persona."IdPersonaConyuge"	AS "IdPersonaConyuge"
			, Persona."IdPersonaEmpleador"	AS "IdPersonaEmpleador"
			, Persona."EstadoCivil"			AS "EstadoCivil"
		FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DocumentoIdentidad
			INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" Persona ON (Persona."IdPersona" = DocumentoIdentidad."IdPersona")
		WHERE DocumentoIdentidad."DocumentoIdentidad" = :DocumentoIdentidad
			AND Persona."Borrado" = 'N'
			AND DocumentoIdentidad."Borrado" = 'N'
),	CTE_RELACIONES_ORIGEN AS (
		SELECT
			ROW_NUMBER() OVER (
				PARTITION BY Relacion."IdTipoRelacion"
				ORDER BY Relacion."IdRelacionPersona"
			)											AS "OrdenRegistro"
			, Persona."IdPersona"						AS "IdPersonaOrigen"
			, Relacion."IdPersonaDestino"				AS "IdPersonaRelacionada"
			, Relacion."IdTipoRelacion"					AS "TipoRelacion"
		FROM CTE_PERSONA_ORIGEN Persona
			 LEFT JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" Relacion ON (Relacion."IdPersonaOrigen" = Persona."IdPersona")
			 LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PersonaRelacion ON (PersonaRelacion."IdPersona" = Relacion."IdPersonaDestino")
		WHERE Relacion."IdEstado" = 'ACT'
		 	AND Relacion."IdTipoRelacion" IN ('APO', 'REP', 'AUTZDO')
			AND PersonaRelacion."Borrado" = 'N'
)
	SELECT
		1								AS "OrdenRegistro"
		, Persona."IdPersona"			AS "IdPersonaOrigen"
		, Persona."IdPersona"			AS "IdPersonaRelacionada"
		, 'SELF'						AS "TipoRelacion"
	FROM CTE_PERSONA_ORIGEN Persona
UNION ALL
	SELECT
		1								AS "OrdenRegistro"
		, Persona."IdPersona"			AS "IdPersonaOrigen"
		, Persona."IdPersonaConyuge"	AS "IdPersonaRelacionada"
		, 'CONYUGESOCIO'				AS "TipoRelacion"
	FROM CTE_PERSONA_ORIGEN Persona
	WHERE Persona."EstadoCivil" IN ('CA', 'CC')
UNION ALL
	SELECT
		1								AS "OrdenRegistro"
		, Persona."IdPersona"			AS "IdPersonaOrigen"
		, Persona."IdPersonaEmpleador"	AS "IdPersonaRelacionada"
		, 'EMPLEADORSOCIO'				AS "TipoRelacion"
	FROM CTE_PERSONA_ORIGEN Persona
UNION ALL
	SELECT
		RelacionOrigen."OrdenRegistro"      		AS "OrdenRegistro"
		, RelacionOrigen."IdPersonaOrigen"			AS "IdPersonaOrigen"
	 	, RelacionOrigen."IdPersonaRelacionada"		AS "IdPersonaRelacionada"
		, RelacionOrigen."TipoRelacion"				AS "TipoRelacion"
	FROM CTE_RELACIONES_ORIGEN RelacionOrigen

UNION ALL
	SELECT
		RelacionOrigen."OrdenRegistro"					AS "OrdenRegistro"
		, PersonaRelacion."IdPersona"					AS "IdPersonaOrigen"
		, PersonaRelacion."IdPersonaConyuge"			AS "IdPersonaRelacionada"
		, 'CONYUGE' || RelacionOrigen."TipoRelacion"	AS "TipoRelacion"
	FROM CTE_RELACIONES_ORIGEN RelacionOrigen
		 LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PersonaRelacion ON (PersonaRelacion."IdPersona" = RelacionOrigen."IdPersonaRelacionada")
	WHERE  PersonaRelacion."EstadoCivil" IN ('CA', 'CC');