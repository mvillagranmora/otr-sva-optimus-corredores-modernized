SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLRECL"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLRECL" AS

	SELECT 
		"RUT_CLI",
		"SEC_RUT_CLI",
		"TIPO_REL_CLI", --  relaciones validas
		/*
			AD	TRELAAPODERADO	S
		AO	TRELAAUTORIZADO PARA DAR ORDENES	S
		RL	TREPRESENTANTE LALEGAL	S
		*/
		
		
		"RUT_PERSONA",
		"SEC_RUT_PERSONA",
		"CARGO_PERSONA", -- 
		'P' AS  "IND_PERSONA_CLI" -- 
	FROM (

	SELECT 		
			"IdStack" AS "IDSTACK", ACTION,
			''  AS "TABLE_KEY",
			COALESCE (CAST("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST(DOCPerCli."DocumentoIdentidad" AS VARCHAR(11))) AS VARCHAR(11)),'') AS "RUT_CLI", -- CHAR(11)
            COALESCE (CAST('0' AS VARCHAR(100)),'')  AS "SEC_RUT_CLI", -- VARCHAR(100)
            COALESCE (CAST( "IdTipoRelacion" AS VARCHAR(2)),'')  AS "TIPO_REL_CLI", -- CHAR(2)
            COALESCE (CAST("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST(DOCRel."DocumentoIdentidad" AS VARCHAR(11))) AS VARCHAR(11)),'')  AS "RUT_PERSONA", -- CHAR(11)
            COALESCE (CAST('0' AS VARCHAR(100)),'')  AS "SEC_RUT_PERSONA", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(20)),'')  AS "CARGO_PERSONA", -- CHAR(20)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_PERSONA_CLI" -- CHAR(1)
		
		FROM (
			--APODERADOS
				SELECT
					WST."IdStack" AS "IdStack","Action" AS ACTION
					,REL."IdPersonaOrigen"
					,REL."IdPersonaDestino"
					, 'AD' AS  "IdTipoRelacion"
				 
				FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL
				LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST
					ON  (WST."Table" = 'TBPLRECL_Relacion_Persona' AND WST."Key" = REL."IdRelacionPersona") 
					WHERE REL."IdTipoRelacion" = 'APO' 
				UNION 

		
			---Persona / Representa Legal 
				SELECT 
					WST."IdStack" AS "IdStack" ,"Action" AS ACTION
					,PER."IdPersona"
					,PER."IdPersonaRepresentante"
					,'RL' AS "IdTipoRelacion"
				 
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
				LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST
					ON  (WST."Table" = 'TBPLRECL_PERSONA_RELACIONES' AND WST."Key" = PER."IdPersona" ) 
			
				WHERE PER."Tipo"='J' AND PER."IdPersonaRepresentante" > 0

				UNION 	
				
			---ORIGEN AUTORIZADO	
				SELECT 
					WST."IdStack" AS "IdStack" ,"Action" AS ACTION
					,SOC."IdPersona"
					,AUT."IdPersona"
					,'AO'
				
				FROM "BSCL.Portafolio.BD.Vistas::ConsultarAutorizadoOrden" 		      AUT
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion"	  REL ON  (CASE WHEN AUT."IdTipoRelacion"='AUTICL' THEN 'ATICLI' ELSE AUT."IdTipoRelacion" END = REL."IdTipoRelacion")	---Segun tipo relacion (agrupador de tipo autorizado)
				INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 		  POR ON  (POR."IdPortafolio"= AUT."IdPortafolio")
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC ON  (POR."IdCliente"= SOC."IdCliente")
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON  (PER."IdPersona"= SOC."IdPersona")
				LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST
					ON  (WST."Table" = 'TBPLRECL_RELACION_AUTORIZADO' AND WST."Key" = AUT."IdAutorizado" ) 

					WHERE PER."Tipo"='N' --AND REL."Administracion"='AUTORIZADO'
					
				GROUP BY "IdStack" , SOC."IdPersona", AUT."IdPersona",	AUT."IdTipoRelacion","Action"
				
			) AS tb1
					INNER JOIN  "BSCL.SocioNegocio.BD::PER.Persona" as PER1 
						ON (PER1."IdPersona" = tb1."IdPersonaOrigen")
	
					INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCPerCli	
						ON  (DOCPerCli."IdDocumentoIdentidad" = PER1."IdDocumentoIdentidad")
						
					INNER JOIN  "BSCL.SocioNegocio.BD::PER.Persona" as PER2
						ON (PER2."IdPersona" = tb1."IdPersonaDestino")
	
					INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCRel	
						ON  (DOCRel."IdDocumentoIdentidad" = PER2."IdDocumentoIdentidad")
						
					LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hr 
						ON (hr."TipoValor" = 'TipoRelacion' and hr."ValorDestino" = tb1."IdTipoRelacion")

		) AS TB2
			WHERE "RUT_CLI" <> '' AND "RUT_PERSONA" <> '' 
			
		GROUP BY RUT_CLI, RUT_PERSONA, TIPO_REL_CLI , SEC_RUT_CLI, SEC_RUT_PERSONA, CARGO_PERSONA, IND_PERSONA_CLI,Action	


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBPLRECL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
