SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TELEFONO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TELEFONO" AS

	SELECT
		TEL."IdTelefono" AS "TEL_ID", --[tel_id] [int] NOT NULL,
		CAST(
			CASE 
				WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT) AS "PER_ID",--int] NOT NULL,***	
		
		COALESCE(hr."ValorOrigen" , TEL."IdTipoTelefono" )  AS "PAR_TTE_ID", --[par_tte_id] [varchar](10) NOT NULL,
		
		
		CAST(TEL."Numero" AS VARCHAR(20)) AS "TEL_NUMERO", --[tel_numero] [varchar](20) NOT NULL,
		CAST(COALESCE(TEL."Comentario",'') AS VARCHAR(50)) AS "TEL_OBSERVACION", --[tel_observacion] [varchar](50) NOT NULL,
		CAST(
			CASE 
			WHEN TEL."IdEstado" = 'ACT' THEN 'N'
			WHEN TEL."IdEstado" = 'INA' THEN 'S'
			ELSE 'N' END AS VARCHAR(1)
		) AS "TEL_INVALIDO", --[tel_invalido] [char](1) NOT NULL,
		CASE WHEN TEL."FechaModificacion" < '1900-01-01' THEN '1900-01-01' ELSE TEL."FechaModificacion" END AS "TEL_FEC_MODIFICA", --[tel_fec_modifica] [datetime] NOT NULL,
		CAST('S' AS VARCHAR(1)) AS "TEL_MODIFICABLE", --[tel_modificable] [char](1) NOT NULL,
		CAST('' AS VARCHAR(10)) AS "TEL_COM_ID"--[tel_com_id] [varchar](10) NOT NULL
		
	FROM "BSCL.SocioNegocio.BD::PER.Telefono" AS TEL
	JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER ON PER."IdPersona" = TEL."IdPersona"
	JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
	LEFT JOIN  "BSCL.Homologacion.BD::HOM.Homologacion" hr
						ON (hr."TipoValor" = 'TipoTelefono' and hr."ValorDestino" = tel."IdTipoTelefono" AND hr."ValorOrigen" NOT IN ('TTELFFUNCI', 'TTELEFAX') )	
	where DOC."DocumentoIdentidad" not like '%.%'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TELEFONO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
