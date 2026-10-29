SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.INDICADORES_CUENTA"; 


CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.INDICADORES_CUENTA" AS

		SELECT 
				CASE WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN POR."IdCliente"
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS "PER_ID", -- TIPO :int
				POR."CodigoPortafolio"  AS "CUENTA", -- TIPO :int
				0 	AS "FIL_ID", -- TIPO :int
				TIND."IdTipoIndicador"  AS "IND_ID", -- TIPO :int
				CASE
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = '{}' THEN '' 
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") LIKE '{%' THEN
						CASE 
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") LIKE '%"OE":true%' THEN 'E'
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") LIKE '%"OV":true%' THEN 'C'
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") LIKE '%"OD":true%' THEN 'M'
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") LIKE '%"OR":true%' THEN 'J'
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") LIKE '%"AC":true%' THEN '0'                          
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") LIKE '%"OM":true%' THEN 'S'                       
							ELSE ''
						END  
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'TRIMESTRAL' THEN 'T' --PERCA
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'SEMESTRAL' THEN 'S' --PERCA
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'ANUAL' THEN 'A' --PERCA
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'MENSUAL' THEN 'M' --PERCA
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'NO_SOLICITA' THEN 'N' --PERCA
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'MODPREFMENS' THEN 'S' --COGAS
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'MODPREFNORM' THEN 'N' --PERCA
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'CMANU' THEN 'M' --REOSE
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'MAIL' THEN 'S' --REOSE
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'NOENV' THEN 'N' --REOSE
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'MES' THEN 'M' --REOSP
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'DIA' THEN 'D' --REOSP
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'SEM' THEN 'S' --REOSP
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'OV' THEN 'C' --ORSOL
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'OE' THEN 'E' --ORSOL
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'OD' THEN 'M' --ORSOL
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'OM' THEN 'S' --ORSOL
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'OR' THEN 'J' --ORSOL
					WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'AC' THEN '0' --ORSOL

					WHEN "CodigoTipoIndicador" = 'SERIEAFP' THEN
						CASE 
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'FONDOA' THEN 'A' 
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'FONDOB' THEN 'B' 
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'FONDOC' THEN 'C' 
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'FONDOD' THEN 'D' 
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'FONDOE' THEN 'E' 
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'NO' THEN 'N' 
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'NINGUNO' THEN 'I'
							WHEN COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") = 'N/A' THEN 'Z' 
							ELSE 'X'

						END

					ELSE COALESCE(left(COALESCE(INDPROPIO."ValorIndicador",INDMODELO."ValorIndicador",TIND."ValorIndicadorDefecto") ,1) ,'')
				END AS "DIN_ID", -- TIPO :char(1)
				CURRENT_TIMESTAMP  AS "INC_FEC_INICIO", -- TIPO :datetime
				POR."IdPortafolio" AS "IdPortafolio"
			FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON POR."IdCliente" = CLI."IdCliente"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" )
			LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" as TIND ON (TIND."IdTipoEntidad" = 'PORTAFOLIO' OR TIND."CodigoTipoIndicador" IN('CCGF','ANTLEGAL','CONAPV','CONPRESADM','CS','CVC','CCI','DECRESID','FOTOCOPIA','ENTREGACLAVE','CDFYNSA','AIPFYNSA','REOP','REOE','TCLICMFJ','CM'))AND TIND."IdEstado" != 'INA'
			LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS INDMODELO ON INDMODELO."IdTipoIndicador" = TIND."IdTipoIndicador" AND 
				INDMODELO."ModeloCliente" = CLI."IdTipoCliente" AND INDMODELO."IdEntidad" = 0 AND INDMODELO."ModeloCliente" = ''
			LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS INDPROPIO ON TIND."IdTipoEntidad" = 'PORTAFOLIO' and INDPROPIO."IdEntidad" = POR."IdPortafolio" AND INDPROPIO."IdTipoIndicador" = TIND."IdTipoIndicador"
			WHERE POR."CodigoPortafolio" IS NOT NULL AND "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0
			AND DOC."IdDocumentoIdentidad" IS NOT NULL
			AND (TIND."CodigoTipoIndicador" IN ('CCGF','ANTLEGAL','CONAPV','CONPRESADM','CS','CVC','CCI','DECRESID','FOTOCOPIA','CM','TCLICMFJ','FFMM_MMAUT','AFPAGFINSTODFYNSA','SERIEAFP',
			'REOE','REOP','CAR_ENVCDF','FAC_FLFPPR','ENTREGACLAVE','AIPFYNSA','CDFYNSA','ANTLEGAL','TIPINST','FAC_MODFAC','REOP','REOE',
			'ADMCART','GEN_SOBCTA','ORDVNNOMRF','ORDMODALIDAD','FAC_ENVFEM','ORDOCUSTRF','ORDOCUSTRV','CAR_PERIOD','TES_NIE') )
			UNION ALL
			SELECT 
				CASE WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN POR."IdCliente"
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS "PER_ID", -- TIPO :int
				POR."CodigoPortafolio"  AS "CUENTA", -- TIPO :int
				0 	AS "FIL_ID", -- TIPO :int
				COALESCE(HOM."ValorDestino",9001) AS "IND_ID",
				COALESCE(HOM_VAL."ValorOrigen",'') AS "DIN_ID",
				CURRENT_TIMESTAMP  AS "INC_FEC_INICIO", -- TIPO :datetime
				POR."IdPortafolio" AS "IdPortafolio"
			FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON POR."IdCliente" = CLI."IdCliente"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" )
			INNER JOIN "BSCL.Replica.BD::PLA.Homologacion" AS HOM ON HOM."TipoValor" = 'IND_ID' AND HOM."Entidad" = 'INDICADOR' AND HOM."ValorOrigen" ='GEREN'
			LEFT JOIN "BSCL.Replica.BD::PLA.Homologacion" AS HOM_VAL ON HOM_VAL."TipoValor" = 'AreaCliente' AND trim(HOM_VAL."ValorDestino") = trim(POR."AreaCliente") 
			
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.INDICADORES_CUENTA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

