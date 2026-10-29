SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.INDICADORES_CLIENTE";

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.INDICADORES_CLIENTE" AS

		SELECT 
			CASE 
				WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN  CLI."IdCliente"
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END  AS "PER_ID", --[per_id] [int] NOT NULL,
				
			0 AS "FIL_ID", --[fil_id] [int] NOT NULL,
			TIND."IdTipoIndicador" AS "IND_ID", --[ind_id] [int] NOT NULL,
			CASE WHEN TIND."CodigoTipoIndicador" IN ('TCLICMFJ','TCLICMF') THEN
				CASE
					WHEN COALESCE(INDPROPIO."ValorIndicador", INDPROPIOPER."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'INVERSIONISTA' THEN 'A'
					WHEN COALESCE(INDPROPIO."ValorIndicador", INDPROPIOPER."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'INTERPOSITA PERSONA' THEN 'B'
					WHEN COALESCE(INDPROPIO."ValorIndicador", INDPROPIOPER."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'INSTITUCIONAL' THEN 'C'
					ELSE 'X'
				END
			ELSE
					COALESCE(CAST(left(COALESCE(INDPROPIO."ValorIndicador", INDPROPIOPER."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto"),1) AS VARCHAR) ,'') END AS "DIN_ID", --[din_id] [char](1) NOT NULL,
			CURRENT_TIMESTAMP AS "IND_FEC_INICIO", --[ind_fec_inicio] [datetime] NOT NULL,
			CURRENT_TIMESTAMP AS "IND_FEC_SUSCRIPCION", --[ind_fec_suscripcion] [datetime] NULL,
			CAST('0' AS VARCHAR(250)) AS "IND_FOLIO" --[ind_folio] [varchar](250) NULL,
		FROM "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" )
		LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" as TIND ON (TIND."IdTipoEntidad" = 'CLIENTE' AND TIND."IdEstado" != 'INA') OR (TIND."CodigoTipoIndicador" IN ('PEP','ENTIDMCF','DECRESID','UAF57'))
		LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS INDMODELO ON INDMODELO."IdTipoIndicador" = TIND."IdTipoIndicador" AND 
			INDMODELO."ModeloCliente" = CLI."IdTipoCliente" AND INDMODELO."IdEntidad" = 0
		LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS INDPROPIO ON INDPROPIO."IdTipoEntidad" = 'CLIENTE' AND INDPROPIO."IdEntidad" = CLI."IdCliente" AND INDPROPIO."IdTipoIndicador" = TIND."IdTipoIndicador"
		LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS INDPROPIOPER ON INDPROPIOPER."IdTipoEntidad" = 'PERSONA' AND INDPROPIOPER."IdEntidad" = PER."IdPersona" AND INDPROPIOPER."IdTipoIndicador" = TIND."IdTipoIndicador"
		WHERE (TIND."CodigoTipoIndicador" IN ('ANTLEGAL','CC','CCF','CCG','CCGF','CCGP','CCI','CM','CS','CVC','DERPRIV',
		'ESHABITUAL','FOTOCOPIA','INVCALIF','CDFYNSA','AIPFYNSA','TCLICMFJ','ENTREGACLAVE','DECRESID','CONPRESADM','CONAPV',
		'CAL','FCFYNSA','EPFYNSA','RFFYNSA','ANEXO51OMIPNPJ','TCLICMF','ANEXO5CVMVE','TIPINST','FAC_MODFAC','REOP','REOE',
		'DCRSFAT','PEP','ENTIDMCF','UAF57','NPA','FATCA'))
		AND DOC."IdDocumentoIdentidad" IS NOT NULL
		UNION ALL
		SELECT 
		CASE 
			WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN  CLI."IdCliente"
			ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END  AS "PER_ID",
		0 AS "FIL_ID",
		COALESCE(HOM_IND."ValorDestino",9000) AS "IND_ID",
		COALESCE(HOM_DIN."ValorDestino", 'N') AS "DIN_ID",
		CURRENT_TIMESTAMP AS "IND_FEC_INICIO", 
		CURRENT_TIMESTAMP AS "IND_FEC_SUSCRIPCION",
		CAST('0' AS VARCHAR(250)) AS "IND_FOLIO"
		FROM BSCL."BSCL.SocioNegocio.BD::CLI.Cliente" CLI
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
		LEFT JOIN "BSCL.Replica.BD::PLA.Homologacion" as HOM_IND on HOM_IND."TipoValor" = 'IND_ID' AND HOM_IND."Entidad" = 'INDICADOR' AND HOM_IND."ValorOrigen" = 'CATIG'
		LEFT JOIN "BSCL.Replica.BD::PLA.Homologacion" as HOM_DIN on HOM_DIN."TipoValor" = 'DIN_ID' AND HOM_DIN."Entidad" = 'INDICADOR' AND HOM_DIN."ValorOrigen" = CLI."IdCategoriaInversionista"
		WHERE DOC."IdDocumentoIdentidad" IS NOT NULL
		UNION ALL
		SELECT 
		CASE 
			WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN  PER."IdPersona"
			ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END  AS "PER_ID",
		0 AS "FIL_ID",
		COALESCE(HOM_IND."ValorDestino",9002) AS "IND_ID",
		CASE 
			WHEN CNT."IdPersona" IS NOT NULL THEN 'S'
			ELSE 'N'
		END AS "DIN_ID",
		CURRENT_TIMESTAMP AS "IND_FEC_INICIO", 
		CURRENT_TIMESTAMP AS "IND_FEC_SUSCRIPCION",
		CAST('0' AS VARCHAR(250)) AS "IND_FOLIO"
		FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
		LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" CNT ON CNT."IdPersona" = PER."IdPersona" AND CNT."IdEstado" = 'ACT'
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
		INNER JOIN "BSCL.Replica.BD::PLA.Homologacion" as HOM_IND on HOM_IND."TipoValor" = 'IND_ID' AND HOM_IND."Entidad" = 'INDICADOR' AND HOM_IND."ValorOrigen" = 'CLCPA'
		WHERE DOC."IdDocumentoIdentidad" IS NOT NULL
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.INDICADORES_CLIENTE" TO  BCS_DESARROLLADOR WITH GRANT OPTION;


