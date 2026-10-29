SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.INDICADOR"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.INDICADOR" AS
		
	SELECT IND_ID, IND_TIPO,IND_AMBITO,IND_NOMBRE,IND_TIP_PER,IND_EXPIRA,IND_DURACION_EXPIRA,
	IND_TIPO_OPERADOR,IND_IND_REQUISITO,REG_ID,IND_TAB_HOMOLOG,IND_CAM_HOMOLOG,
	IND_POS_HOMOLOG,IND_POS_PARAMETRO,IND_FOLIO,IND_FECHA_SUSCRIPCION FROM (
		SELECT 
			"IdTipoIndicador" AS "IND_ID", --[ind_id] [int] NOT NULL,
			CAST('CO' AS VARCHAR(2)) AS "IND_TIPO", --[ind_tipo] [char](2) NOT NULL,
			
			CASE  
				WHEN "IdTipoEntidad" = 'PORTAFOLIO' THEN 'CUE'
				WHEN "CodigoTipoIndicador" in ('PEP','ENTIDMCF','DECRESID','UAF57') THEN 'CLI' --agregado por que en optimus es PER, Sebra CLI
				ELSE left("IdTipoEntidad",3) END  AS "IND_AMBITO", --[ind_ambito] [char](3) NOT NULL,
			
			CAST("NombreTipoIndicador" AS VARCHAR(50)) AS "IND_NOMBRE", --[ind_nombre] [varchar](50) NOT NULL,
			CASE "IdTipoPersona" WHEN 'N' THEN 'NAT' WHEN  'J' THEN 'JUR' ELSE 'AMB' END    AS "IND_TIP_PER", --[ind_tip_per] [char](3) NOT NULL,
			
			CAST('N' AS VARCHAR(1)) AS "IND_EXPIRA", --[ind_expira] [char](1) NOT NULL,
			0 AS "IND_DURACION_EXPIRA", --[ind_duracion_expira] [int] NOT NULL,
			CASE WHEN "ClaseDato" = 'CHECKBOX' THEN 'B' ELSE 'L' END   AS "IND_TIPO_OPERADOR", --[ind_tipo_operador] [char](1) NOT NULL,
			CASE WHEN "IdUso" ='REQ' THEN 'S' ELSE 'N' END   AS "IND_IND_REQUISITO", --[ind_ind_requisito] [char](1) NOT NULL,
			CASE
				WHEN "CodigoTipoIndicador"= 'TIPINST' THEN 'AFP'
				WHEN "CodigoTipoIndicador"= 'FAC_MODFAC' THEN 'COGAS'
				WHEN "CodigoTipoIndicador"= 'REOP' THEN 'REOSP'
				WHEN "CodigoTipoIndicador"= 'CCG' THEN 'CCG'
				WHEN "CodigoTipoIndicador"= 'REOE' THEN 'REOSE'
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
				WHEN "CodigoTipoIndicador" IN ('DCRSFAT', 'DCRSF') THEN COALESCE(HOM."ValorDestino", 'FATCA')
				ELSE LEFT("CodigoTipoIndicador",5) END AS "REG_ID",
			CAST('' AS VARCHAR(8)) AS "IND_TAB_HOMOLOG", --[ind_tab_homolog] [varchar](8) NOT NULL,
			CAST('' AS VARCHAR(20)) AS "IND_CAM_HOMOLOG", --[ind_cam_homolog] [varchar](20) NOT NULL,
			0 AS "IND_POS_HOMOLOG", --[ind_pos_homolog] [int] NOT NULL,
			0 AS "IND_POS_PARAMETRO", --[ind_pos_parametro] [int] NOT NULL,
			CAST('NO' AS VARCHAR(50)) AS "IND_FOLIO", --[ind_folio] [varchar](50) NULL,
			CAST('NO' AS VARCHAR(50)) AS "IND_FECHA_SUSCRIPCION"--[ind_fecha_suscripcion] [varchar](50) NULL,
			FROM "BSCL.Plataforma.BD::IND.TipoIndicador" ti 
			LEFT JOIN "BSCL.Replica.BD::PLA.Homologacion" HOM ON HOM."TipoValor" = 'REG_ID' AND HOM."Entidad" = 'INDICADOR' AND HOM."ValorOrigen" = ti."CodigoTipoIndicador"
			WHERE ti."CodigoTipoIndicador" IN ('ADMCART','AFPAGFINSTODFYNSA','AIPFYNSA','ANEXO5CVMVE','ANTLEGAL','ANEXO51OMIPNPJ','GEN_SOBCTA','ORDVNNOMRF','CAL',
			'CAR_ENVCDF','CCG','CCF','CDFYNSA','CCGP','CC','FAC_MODFAC','CCGF','CONAPV','CONPRESADM','CONPRESACC','CS','CVC',
			'CCI','DERPRIV','DECRESID','FAC_ENVFEM','ENTREGACLAVE','EPFYNSA','FAC_FLFPPR','DCRSFAT','FCFYNSA','FFMM_MMAUT','FOTOCOPIA',
			'ESHABITUAL','INVCALIF','CM','TES_NIE','ORDMODALIDAD','CAR_PERIOD','PEP','REOE','REOP','RFFYNSA','SERIEAFP','TCLICMFJ',
			'TCLICMF','ORDOCUSTRF','ORDOCUSTRV','UAF57','NPA','FATCA')
		)
	UNION
	SELECT 
		COALESCE(
		    HOM."ValorDestino",
		    CASE HOM."ValorOrigen"
		        WHEN 'CATIG' THEN 9000
		        WHEN 'GEREN' THEN 9001
		        WHEN 'CLCPA' THEN 9002
		    END
		) AS IND_ID,
		'CO' AS IND_TIPO,
		CASE "ValorOrigen" 
			WHEN 'CATIG' THEN 'CLI'
			WHEN 'GEREN' THEN 'CUE' 
			WHEN 'CLCPA' THEN 'CLI'
			ELSE 'CLI'
		END AS IND_AMBITO,
		"Descripcion" IND_NOMBRE,
		'AMB' AS IND_TIP_PER,
		'N' AS IND_EXPIRA,
		0 AS IND_DURACION_EXPIRA,
		CASE 
			WHEN HOM."ValorOrigen" = 'CLCPA' THEN 'B'
			ELSE 'L'
		END AS IND_TIPO_OPERADOR,
		'N' AS IND_IND_REQUISITO, 
		"ValorOrigen" AS REG_ID,
		'' AS IND_TAB_HOMOLOG,
		'' AS IND_CAM_HOMOLOG,
		0 AS IND_POS_HOMOLOG,
		0 AS IND_POS_PARAMETRO,
		'NO' AS IND_FOLIO,
		'NO' AS IND_FECHA_SUSCRIPCION
	FROM DUMMY
	LEFT JOIN "BSCL.Replica.BD::PLA.Homologacion" as HOM on HOM."TipoValor" = 'IND_ID' AND HOM."Entidad" = 'INDICADOR'
	WHERE HOM."ValorOrigen" IN ('CATIG','GEREN','CLCPA')
				
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.INDICADOR" TO  BCS_DESARROLLADOR WITH GRANT OPTION;


/* Indicadores duplicados en Optimus
SELECT * FROM "BSCL.Replica.BD.Vistas::Sebra.INDICADOR" WHERE REG_ID IN ('AUTNO', 'TICRF')
SELECT * FROM "BSCL.Homologacion.BD::HOM.Homologacion" WHERE "ValorOrigen" = 'TICRF'
SELECT * FROM "BSCL.Plataforma.BD::IND.TipoIndicador" WHERE "CodigoTipoIndicador" IN ('ORDOCUSTRF','MES_CUSTRF')
SELECT * FROM "BSCL.Homologacion.BD::HOM.Homologacion" WHERE "ValorOrigen" = 'AUTNO'
SELECT * FROM "BSCL.Plataforma.BD::IND.TipoIndicador" WHERE "CodigoTipoIndicador" IN ('ORDVNNOMRF','ORDVNNOMRV')
*/

/*
SELECT hic."ValorOrigen",hic2."ValorOrigen", ti."CodigoTipoIndicador", * FROM "BSCL.Plataforma.BD::IND.TipoIndicador" ti 
LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hic ON ti."CodigoTipoIndicador" = hic."ValorDestino"
LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hic2 ON ti."CodigoTipoIndicador" = hic2."TipoValor"
WHERE ti."CodigoTipoIndicador" IN ('REOE','REOP')

UPDATE ClienteH SET  ClienteH."IdEstado"='BLO',ClienteH."IdTipoBloqueo"='BMUERT',ClienteH."ComentarioBloqueo"='INACTIVO'
FROM  BSCL."BSCL.Homologacion.BD::GES.IndicadoresCliente" IC  
INNER JOIN BSCL."BSCL.Homologacion.BD::GES.Indicador"  I ON (IC."ind_id"=I."ind_id" AND IC."din_id"='S' AND I."reg_id"='FALLE')
INNER JOIN BSCL."BSCL.SocioNegocio.BD::CLI.Cliente" as ClienteH	ON  ClienteH."IdOrigen" = IC."per_id"
WHERE  IC."IdCarga"= :p_IdCarga ;
*/

