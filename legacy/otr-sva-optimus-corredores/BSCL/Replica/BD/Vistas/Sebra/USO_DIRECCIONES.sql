SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.USO_DIRECCIONES"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.USO_DIRECCIONES" AS

	SELECT DISTINCT 
			cast("IdPersona" AS varchar(100)) as "IdPersona",
			"PER_ID",
			COALESCE (CUENTA,'-1') AS CUENTA, -- int] NOT NULL, ***
			0 AS fil_id, -- int] NOT NULL,
			dir_id, -- int] NOT NULL,****
			CASE WHEN udi_fec_inicio < '1900-01-01' THEN '1900-01-01' ELSE udi_fec_inicio END   AS udi_fec_inicio, -- datetime] NOT NULL,
			"UDI_PARTICULAR", -- char](1) NOT NULL,
			"UDI_COMERCIAL", -- char](1) NOT NULL,****
			"UDI_LEGAL", -- char](1) NOT NULL,****
			"UDI_CORRESP", -- char](1) NOT NULL,
			"UDI_MAIL_CORR", -- char](1) NOT NULL,****
			"UDI_MAIL_DTE" -- char](1) NOT NULL,
	
	FROM (	SELECT
			PER."IdPersona",
			CAST(
				CASE 
					WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
					ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT) AS "PER_ID",--int] NOT NULL,***
			POR."CodigoPortafolio" AS CUENTA, -- int] NOT NULL, ***
			0 AS fil_id, -- int] NOT NULL,
			DF."IdDireccionFisica" AS dir_id, -- int] NOT NULL,****
			DF."FechaCreacion" AS udi_fec_inicio, -- datetime] NOT NULL,
			'S' AS "UDI_PARTICULAR", -- char](1) NOT NULL,
			'N' AS "UDI_COMERCIAL", -- char](1) NOT NULL,****
			CASE WHEN POR."IdDireccionEnvioCorrespondencia" = DF."IdDireccionFisica" THEN 'S' ELSE 'N' END AS "UDI_LEGAL", -- char](1) NOT NULL,****
			CASE WHEN POR."IdDireccionEnvioCorrespondencia" = DF."IdDireccionFisica" THEN 'S' ELSE 'N' END AS "UDI_CORRESP", -- char](1) NOT NULL,
			'N' AS "UDI_MAIL_CORR", -- char](1) NOT NULL,****
			'N' AS "UDI_MAIL_DTE" -- char](1) NOT NULL,
		from "BSCL.Portafolio.BD::POR.Portafolio" POR
		JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
		JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
		JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" )
		JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF ON PER."IdPersona" = DF."IdPersona" AND DF."CodigoProposito" = 'PART'
				 
		UNION ALL
			
		SELECT
			PER."IdPersona",
			CAST(
				CASE 
					WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
					ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT) AS "PER_ID",--int] NOT NULL,***
		 	POR."CodigoPortafolio" AS CUENTA, -- int] NOT NULL, ***
			0 AS fil_id, -- int] NOT NULL,
			DF."IdDireccionFisica" AS dir_id, -- int] NOT NULL,****
			DF."FechaCreacion" AS udi_fec_inicio, -- datetime] NOT NULL,
			'N' AS "UDI_PARTICULAR", -- char](1) NOT NULL,
			'S' AS "UDI_COMERCIAL", -- char](1) NOT NULL,****
			CASE WHEN POR."IdDireccionEnvioCorrespondencia" = DF."IdDireccionFisica" THEN 'S' ELSE 'N' END AS "UDI_LEGAL", -- char](1) NOT NULL,****
			CASE WHEN POR."IdDireccionEnvioCorrespondencia" = DF."IdDireccionFisica" THEN 'S' ELSE 'N' END AS "UDI_CORRESP", -- char](1) NOT NULL,
			'N' AS "UDI_MAIL_CORR", -- char](1) NOT NULL,****
			'N' AS "UDI_MAIL_DTE" -- char](1) NOT NULL,
		from "BSCL.Portafolio.BD::POR.Portafolio" POR
		JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
		JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
		JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" )
		JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF ON PER."IdPersona" = DF."IdPersona" AND DF."CodigoProposito" = 'COME'
				 
		UNION ALL
		
		SELECT
			PER."IdPersona",
			CAST(
				CASE 
					WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
					ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT) AS "PER_ID",--int] NOT NULL,***
		 	POR."CodigoPortafolio" AS CUENTA, -- int] NOT NULL, ***
			0 AS fil_id, -- int] NOT NULL,
			DF."IdDireccionFisica" AS dir_id, -- int] NOT NULL,****
			DF."FechaCreacion" AS udi_fec_inicio, -- datetime] NOT NULL,
			'N' AS "UDI_PARTICULAR", -- char](1) NOT NULL,
			'N' AS "UDI_COMERCIAL", -- char](1) NOT NULL,****
			CASE WHEN POR."IdDireccionEnvioCorrespondencia" = DF."IdDireccionFisica" THEN 'S' ELSE 'N' END AS "UDI_LEGAL", -- char](1) NOT NULL,****
			CASE WHEN POR."IdDireccionEnvioCorrespondencia" = DF."IdDireccionFisica" THEN 'S' ELSE 'N' END AS "UDI_CORRESP", -- char](1) NOT NULL,
			'N' AS "UDI_MAIL_CORR", -- char](1) NOT NULL,****
			'N' AS "UDI_MAIL_DTE" -- char](1) NOT NULL,
		from "BSCL.Portafolio.BD::POR.Portafolio" POR
		JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
		JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
		JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" )
		JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF ON PER."IdPersona" = DF."IdPersona" AND DF."CodigoProposito" = 'LABO'
				 
		UNION ALL

			
		SELECT
			PER."IdPersona",
			CAST(
				CASE 
					WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
					ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT) AS "PER_ID",--int] NOT NULL,***
		 	POR."CodigoPortafolio" AS CUENTA, -- int] NOT NULL, ***
			0 AS fil_id, -- int] NOT NULL,
			DF."IdDireccionElectronica" *-1 AS dir_id, -- int] NOT NULL,****
			DF."FechaCreacion" AS udi_fec_inicio, -- datetime] NOT NULL,
			'N' AS "UDI_PARTICULAR", -- char](1) NOT NULL,
			'N' AS "UDI_COMERCIAL", -- char](1) NOT NULL,****
			'N' AS "UDI_LEGAL", -- char](1) NOT NULL,****
			'N' AS "UDI_CORRESP", -- char](1) NOT NULL,
			'S' AS "UDI_MAIL_CORR", -- char](1) NOT NULL,****
			'N' AS "UDI_MAIL_DTE" -- char](1) NOT NULL,
		from "BSCL.Portafolio.BD::POR.Portafolio" POR
		JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
		JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
		JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" )
		JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DF ON PER."IdPersona" = DF."IdPersona" AND DF."CodigoProposito" = 'CORR'
					 
		UNION ALL
			
		SELECT
			PER."IdPersona",
			CAST(
				CASE 
					WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
					ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT) AS "PER_ID",--int] NOT NULL,***
		 	POR."CodigoPortafolio" AS CUENTA, -- int] NOT NULL, ***
			0 AS fil_id, -- int] NOT NULL,
			DF."IdDireccionElectronica"  *-1  AS dir_id, -- int] NOT NULL,****
			DF."FechaCreacion" AS udi_fec_inicio, -- datetime] NOT NULL,
			'N' AS "UDI_PARTICULAR", -- char](1) NOT NULL,
			'N' AS "UDI_COMERCIAL", -- char](1) NOT NULL,****
			'N' AS "UDI_LEGAL", -- char](1) NOT NULL,****
			'N' AS "UDI_CORRESP", -- char](1) NOT NULL,
			'N' AS "UDI_MAIL_CORR", -- char](1) NOT NULL,****
			'S' AS "UDI_MAIL_DTE" -- char](1) NOT NULL,
		from "BSCL.Portafolio.BD::POR.Portafolio" POR
		JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
		JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
		JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" )
		JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DF ON PER."IdPersona" = DF."IdPersona" 
		JOIN "BSCL.Plataforma.BD::IND.Indicador" ON (
									"IdTipoEntidad" = 'PORTAFOLIO' AND "IdEntidad" = "IdPortafolio"
									AND  "IdTipoIndicador"  = 25 AND "ValorIndicador" IS NOT NULL 
									AND "ValorIndicador" =  DF."IdDireccionElectronica")
			 
				
			UNION ALL
		SELECT
			PER."IdPersona",
			CAST(
				CASE 
					WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
					ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT) AS "PER_ID",--int] NOT NULL,***
		 	POR."CodigoPortafolio" AS CUENTA, -- int] NOT NULL, ***
			0 AS fil_id, -- int] NOT NULL,
			DF."IdDireccionElectronica" *-1  AS dir_id, -- int] NOT NULL,****
			DF."FechaCreacion" AS udi_fec_inicio, -- datetime] NOT NULL,
			'N' AS "UDI_PARTICULAR", -- char](1) NOT NULL,
			'N' AS "UDI_COMERCIAL", -- char](1) NOT NULL,****
			'N' AS "UDI_LEGAL", -- char](1) NOT NULL,****
			'N' AS "UDI_CORRESP", -- char](1) NOT NULL,
			'N' AS "UDI_MAIL_CORR", -- char](1) NOT NULL,****
			'S' AS "UDI_MAIL_DTE" -- char](1) NOT NULL,
		from "BSCL.Portafolio.BD::POR.Portafolio" POR
		JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
		JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
		JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" )
		
		JOIN "BSCL.SocioNegocio.BD::PER.GrupoDireccion" AS g  ON (g."IdPersona" = PER."IdPersona" AND g. "IdEstado" = 'ACT'  )
		
		JOIN "BSCL.Plataforma.BD::IND.Indicador" ON (
									"IdTipoEntidad" = 'PORTAFOLIO' AND "IdEntidad" = "IdPortafolio" AND "IdEntidad" != 0
									AND  "IdTipoIndicador"  = 25 AND "ValorIndicador" IS NOT NULL 
									AND "ValorIndicador" = cast(g."IdGrupoDireccion" as nvarchar))
		
		JOIN "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle" AS gd	ON (
									gd."IdGrupoDireccion" = g."IdGrupoDireccion" AND 
									gd."IdEstado" = 'ACT' 
					)
									
		JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DF ON (PER."IdPersona" = DF."IdPersona" 
																	AND DF."IdDireccionElectronica" = gd."IdDireccionElectronica")
									
				 			
									

	) AS tb1
	WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(CUENTA) != 0

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.USO_DIRECCIONES" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
