SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.DETALLE_INDICADOR"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.DETALLE_INDICADOR" AS

SELECT * FROM (
	SELECT 
			i.llave,
			i.IND_ID,
			CASE
				WHEN i.DIN_ID = 'TRIMESTRAL' THEN 'T' --PERCA
				WHEN i.DIN_ID = 'SEMESTRAL' THEN 'S' --PERCA
				WHEN i.DIN_ID = 'ANUAL' THEN 'A' --PERCA
				WHEN i.DIN_ID = 'MENSUAL' THEN 'M' --PERCA
				WHEN i.DIN_ID = 'NO_SOLICITA' THEN 'N' --PERCA
				WHEN i.DIN_ID = 'MODPREFMENS' THEN 'S' --COGAS
				WHEN i.DIN_ID = 'MODPREFNORM' THEN 'N' --MOD FACTURACION NORMAL
				WHEN i.DIN_ID = 'MODPREFCONV' THEN 'S' --MOD FACTURACION CONVENIO
				WHEN i.DIN_ID = 'MODPREFCONT' THEN 'M' --MOD FACTURACION CONTRATO
				WHEN i.DIN_ID = 'CMANU' THEN 'M' --REOSE
				WHEN i.DIN_ID = 'MAIL' THEN 'S' --REOSE
				WHEN i.DIN_ID = 'NOENV' THEN 'N' --REOSEEMPLEADO_CORREDORA
				WHEN i.DIN_ID = 'MES' THEN 'M' --REOSP
				WHEN i.DIN_ID = 'DIA' THEN 'D' --REOSP
				WHEN i.DIN_ID = 'SEM' THEN 'S' --REOSP
				WHEN i.DIN_ID = 'OV' THEN 'C' --ORSOL
				WHEN i.DIN_ID = 'OE' THEN 'E' --ORSOL
				WHEN i.DIN_ID = 'OD' THEN 'M' --ORSOL
				WHEN i.DIN_ID = 'OM' THEN 'S' --ORSOL
				WHEN i.DIN_ID = 'OR' THEN 'J' --ORSOL
				WHEN i.DIN_ID = 'AC' THEN '0' --ORSOL

				WHEN i.DIN_ID = 'INVERSIONISTA' THEN 'A'
				WHEN i.DIN_ID = 'INTERPOSITA PERSONA' THEN 'B'
				WHEN i.DIN_ID = 'INSTITUCIONAL' THEN 'C'

				WHEN "CodigoTipoIndicador" = 'SERIEAFP' THEN
				CASE 
					WHEN i.DIN_ID = 'FONDOA' THEN 'A' 
					WHEN i.DIN_ID = 'FONDOB' THEN 'B' 
					WHEN i.DIN_ID = 'FONDOC' THEN 'C' 
					WHEN i.DIN_ID = 'FONDOD' THEN 'D' 
					WHEN i.DIN_ID = 'FONDOE' THEN 'E' 
					WHEN i.DIN_ID = 'NO' THEN 'N' 
					WHEN i.DIN_ID = 'NINGUNO' THEN 'I' 
					WHEN i.DIN_ID = 'N/A' THEN 'Z' 
				END
						
				ELSE COALESCE(left(i.DIN_ID ,1) ,'')
			END AS "DIN_ID", -- TIPO :char(1)
			i.DIN_VALOR,
			i.DIN_REQ_CUMPLE,
			i.DIN_ORDEN,
			CASE
				WHEN "CodigoTipoIndicador"= 'TIPINST' THEN 'AFP'
				WHEN "CodigoTipoIndicador"= 'FAC_MODFAC' THEN 'COGAS'
				WHEN "CodigoTipoIndicador"= 'REOP' THEN 'REOSP'
				WHEN "CodigoTipoIndicador"= 'CCG' THEN 'CCG'
				WHEN "CodigoTipoIndicador"= 'REOE' THEN 'REOSE'
				WHEN "CodigoTipoIndicador"= 'DCRSFAT' THEN 'FATCA'
				WHEN "CodigoTipoIndicador"= 'ANEXO51OMIPNPJ' THEN 'ANX5M'
				WHEN "CodigoTipoIndicador"= 'PEP' THEN 'PPEXP'
				WHEN "CodigoTipoIndicador"= 'GEN_SOBCTA' THEN 'AUTCL'
				WHEN "CodigoTipoIndicador"= 'ORDVNNOMRF' THEN 'AUTNO'
				WHEN "CodigoTipoIndicador"= 'CCF' THEN 'CCGFW'
				WHEN "CodigoTipoIndicador"= 'CCGP' THEN 'COCGP'
				WHEN "CodigoTipoIndicador"= 'CC' THEN 'COCUS'
				WHEN "CodigoTipoIndicador"= 'CCGF' THEN 'COGEF'
				WHEN "CodigoTipoIndicador"= 'CS' THEN 'COSIM'
				WHEN "CodigoTipoIndicador"= 'CVC' THEN 'COVCO'
				WHEN "CodigoTipoIndicador"= 'CCI' THEN 'COWEB'
				WHEN "CodigoTipoIndicador"= 'DERPRIV' THEN 'DEPRI'
				WHEN "CodigoTipoIndicador"= 'DECRESID' THEN 'DEREI'
				WHEN "CodigoTipoIndicador"= 'FAC_ENVFEM' THEN 'ENFAC'
				WHEN "CodigoTipoIndicador"= 'DCRSF' THEN 'FATCA'
				WHEN "CodigoTipoIndicador"= 'FOTOCOPIA' THEN 'FOTCI'
				WHEN "CodigoTipoIndicador"= 'ESHABITUAL' THEN 'HABUS'
				WHEN "CodigoTipoIndicador"= 'INVCALIF' THEN 'INICA'
				WHEN "CodigoTipoIndicador"= 'CM' THEN 'MAMER'
				WHEN "CodigoTipoIndicador"= 'TES_NIE' THEN 'NETAC'
				WHEN "CodigoTipoIndicador"= 'ORDMODALIDAD' THEN 'ORSOL'
				WHEN "CodigoTipoIndicador"= 'CAR_PERIOD' THEN 'PERCA'
				WHEN "CodigoTipoIndicador"= 'PEP' THEN 'PPEXP'
				WHEN "CodigoTipoIndicador"= 'ORDOCUSTRF' THEN 'TICRF'
				WHEN "CodigoTipoIndicador"= 'ORDOCUSTRV' THEN 'TICRV'
				WHEN "CodigoTipoIndicador"= 'CONPRESACC' THEN 'COPAC'
				ELSE LEFT("CodigoTipoIndicador",5)
			END AS "REG_ID"
			FROM
			(
				SELECT 
				rtrim(ltrim("value")) || ':' || cast( "IdTipoIndicador" as varchar) as llave,
				"IdTipoIndicador" AS "IND_ID", "value"  AS "DIN_ID", "label"  AS "DIN_VALOR", "ReqCumple" AS "DIN_REQ_CUMPLE", 0 AS "DIN_ORDEN"
				FROM "BSCL.Plataforma.BD::IND.TipoIndicador" AS i
				INNER JOIN 
					(SELECT  'N' AS "value" , 'No' AS "label", 'N' AS "ReqCumple" FROM dummy
						UNION ALL 
					 SELECT  'S' AS "value" , 'Si' AS "label", 'N' AS "ReqCumple" FROM dummy
					)  ON ("ClaseDato" = 'CHECKBOX') 
				UNION
				SELECT 
			 	CASE WHEN trim("Codigo") = '' THEN CAST("IdDetalleIndicador" AS varchar) ELSE trim("Codigo") END || ':' ||    cast("IdTipoIndicador" as varchar) as llave,
			 	"IdTipoIndicador" AS "IND_ID", CASE WHEN trim("Codigo") = '' THEN CAST("IdDetalleIndicador" AS varchar) ELSE trim("Codigo") END   AS "DIN_ID", 
				LEFT("DetalleIndicador",30)  AS "DIN_VALOR", 'N' AS "DIN_REQ_CUMPLE", 0 AS "DIN_ORDEN"
				FROM "BSCL.Plataforma.BD::IND.DetalleIndicador" i
			) i 
			INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" ti ON i."IND_ID" = ti."IdTipoIndicador"
			WHERE ti."CodigoTipoIndicador" IN
				('ADMCART','AFPAGFINSTODFYNSA','AIPFYNSA','ANEXO5CVMVE','ANTLEGAL','ANEXO51OMIPNPJ','GEN_SOBCTA','ORDVNNOMRF','CAL',
				'CAR_ENVCDF','CCG','CCF','CDFYNSA','CCGP','CC','FAC_MODFAC','CCGF','CONAPV','CONPRESADM','CONPRESACC','CS','CVC',
				'CCI','DERPRIV','DECRESID','FAC_ENVFEM','ENTREGACLAVE','EPFYNSA','FAC_FLFPPR','DCRSFAT','FCFYNSA','FFMM_MMAUT','FOTOCOPIA',
				'ESHABITUAL','INVCALIF','CM','TES_NIE','ORDMODALIDAD','CAR_PERIOD','PEP','REOE','REOP','RFFYNSA','SERIEAFP','TCLICMFJ',
				'TCLICMF','ORDOCUSTRF','ORDOCUSTRV','UAF57','NPA','FATCA')
			UNION 
			SELECT 
			COALESCE(HOM_DIN."ValorDestino", 'N') || ':CATIG' AS llave,
			COALESCE(HOM_IND."ValorDestino",9000) AS "IND_ID",
			COALESCE(HOM_DIN."ValorDestino", 'N') AS "DIN_ID",
			CAT."IdCategoriaInversionista" AS DIN_VALOR,
			'N' AS DIN_REQ_CUMPLE,
			0 AS DIN_ORDEN,
			'CATIG' AS REG_ID			
			FROM "BSCL.Plataforma.BD::COM.CategoriaInversionista" CAT
			LEFT JOIN "BSCL.Replica.BD::PLA.Homologacion" as HOM_IND on HOM_IND."TipoValor" = 'IND_ID' AND HOM_IND."Entidad" = 'INDICADOR' AND HOM_IND."ValorOrigen" = 'CATIG'
			INNER JOIN "BSCL.Replica.BD::PLA.Homologacion" as HOM_DIN on HOM_DIN."TipoValor" = 'DIN_ID' AND HOM_DIN."Entidad" = 'INDICADOR' AND HOM_DIN."ValorOrigen" = CAT."IdCategoriaInversionista"
			UNION 
			SELECT 
			COALESCE(HOM_DIN."ValorOrigen", 'N') || ':GEREN' AS llave,
			COALESCE(HOM_IND."ValorDestino",9001) AS "IND_ID",
			COALESCE(HOM_DIN."ValorOrigen", 'N') AS "DIN_ID",
			COALESCE(HOM_DIN."Descripcion", '') AS DIN_VALOR,
			'N' AS DIN_REQ_CUMPLE,
			0 AS DIN_ORDEN,
			'GEREN' AS REG_ID			
			FROM "BSCL.SocioNegocio.BD::CLI.Area" AREA
			LEFT JOIN "BSCL.Replica.BD::PLA.Homologacion" as HOM_IND on HOM_IND."TipoValor" = 'IND_ID' AND HOM_IND."Entidad" = 'INDICADOR' AND HOM_IND."ValorOrigen" = 'GEREN'
			INNER JOIN "BSCL.Replica.BD::PLA.Homologacion" AS HOM_DIN ON HOM_DIN."TipoValor" = 'AreaCliente' AND trim(HOM_DIN."ValorDestino") = trim(AREA."Codigo")
			UNION
			SELECT
			DET."DIN_ID" || ':CLCPA' AS llave,
			COALESCE(HOM_IND."ValorDestino", 9002) AS "IND_ID",
			DET."DIN_ID" AS "DIN_ID",
			DET."DIN_VALOR" AS "DIN_VALOR",
			'N' AS "DIN_REQ_CUMPLE",
			0 AS "DIN_ORDEN",
			'CLCPA' AS "REG_ID"
			FROM (
				SELECT 'N' AS "DIN_ID", 'No' AS "DIN_VALOR" FROM DUMMY
				UNION ALL
				SELECT 'S' AS "DIN_ID", 'Si' AS "DIN_VALOR" FROM DUMMY
			) DET
			INNER JOIN "BSCL.Replica.BD::PLA.Homologacion" HOM_IND ON HOM_IND."TipoValor" = 'IND_ID' AND HOM_IND."Entidad" = 'INDICADOR' AND HOM_IND."ValorOrigen" = 'CLCPA'
		)

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.DETALLE_INDICADOR" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
