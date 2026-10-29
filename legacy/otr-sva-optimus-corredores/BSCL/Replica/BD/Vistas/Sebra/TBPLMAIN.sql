SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLMAIN"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLMAIN" AS

	SELECT 
		INS."IdInstrumento",
		INS."IdTipoInstrumento",
	    RPAD(COALESCE (LEFT (COD_NEMO_TRUNK."Nemotecnico" , 16),'') ,16)	 AS "NEMO",
	    COALESCE ( 0 , 0 )  AS "COD_CLASIF_RIESGO", -- DECIMAL  JK > cantidad de decimales del fondo, solo aplica FM
	    COALESCE ( INS."CorteMinimo" , 0 )						AS "CORTE_MINIMO", -- DECIMAL
	    COALESCE (CAST(INS."IdEmisor" AS VARCHAR(10)),'')		AS "EMISOR", -- CHAR(10)
	    CASE 
	    	WHEN COALESCE (INS."IdEstado", '') = 'ACT' THEN 'V' --activo = (v)igente
	    	WHEN COALESCE (INS."IdEstado", '') = 'INA' THEN 'B' --inactivo = (b)loqueado
	    	ELSE 'B' --asumo que no estan activos
	    END 													AS "ESTADO", -- CHAR(1)	
	    
	    COALESCE (CAST(INS."EsDivisible" AS VARCHAR(1)),'')			AS "IND_DIVISIBLE", -- CHAR(1)
	    COALESCE (CAST(INS."EstaInscrito" AS VARCHAR(1)),'')		AS "IND_INSCRITO", -- CHAR(1)
	    
	    CASE WHEN TIN."IdClasificacionTipoInstrumento" IN ('RF', 'IF', 'FM') THEN TIN."IdClasificacionTipoInstrumento"
            WHEN INS."IdTipoInstrumento" IN ('OSA','SIM','ACN') THEN 'AC'
 	           WHEN INS."IdTipoInstrumento" IN ('ETN') THEN 'EN'
 	           WHEN INS."IdTipoInstrumento" IN ('ETX') THEN 'ET'
 	           WHEN INS."IdTipoInstrumento" IN ('MIL') THEN 'ST'
 	           WHEN INS."IdTipoInstrumento" IN ('ACX') THEN 'AO'
	           WHEN INS."IdTipoInstrumento" IN ('CFX') THEN 'CO'
	           WHEN INS."IdTipoInstrumento" IN ('CFM') THEN 'CM'
               WHEN TIN."IdClasificacionTipoInstrumento" = 'SP' THEN 'MO'
				WHEN INS."IdTipoInstrumento" = 'FWD' THEN 'FW'
            	WHEN INS."IdTipoInstrumento" = 'CFI' THEN 'CF'
            	ELSE 'XX'
            END	 AS MERCADO,
	    0  														AS "PJTE_COMIS_OF", -- DECIMAL
	    COALESCE (CAST(INS."RazonSocial" AS VARCHAR(50)),'')	AS "RAZON_SOCIAL", -- CHAR(50)
	    COALESCE (CAST("DocumentoIdentidad" AS VARCHAR(11)),'')	AS "RUT_SOC", -- CHAR(11)
		CASE	WHEN  TIN."IdClasificacionTipoInstrumento" IN ('RV','DV') AND INS."IdMoneda" = 'CLP' THEN 'RV'
				WHEN  TIN."IdClasificacionTipoInstrumento" IN ('RV','DV') AND INS."IdMoneda" = 'USD' THEN 'VD'
				WHEN  TIN."IdClasificacionTipoInstrumento" IN ('RV','DV') AND INS."IdMoneda" = 'EUR' THEN 'VE'
				WHEN  TIN."IdClasificacionTipoInstrumento" IN ('RV','DV') THEN 'RV'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'RF' AND TIN."IdTipoInstrumento" in ('BCP','BCD','BCX','BCU','BTP','BTU','BVL') THEN 'CE'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'RF' AND TIN."IdTipoInstrumento" in ('BEM','BUS','BBC') THEN 'BE'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'RF' AND TIN."IdTipoInstrumento" in ('PRC') THEN 'BT'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'RF' AND TIN."IdTipoInstrumento" in ('LTH') THEN 'LH'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'RF' AND INS."IdMoneda" = 'CLP' THEN 'RF'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'RF' AND INS."IdMoneda" = 'USD' THEN 'FD'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'RF' AND INS."IdMoneda" = 'EUR' THEN 'FE'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'CLP' AND FON."IdMercadoFondoMutuo" = 'RF' THEN 'RF'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'USD' AND FON."IdMercadoFondoMutuo" = 'RF' THEN 'FD'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'EUR' AND FON."IdMercadoFondoMutuo" = 'RF' THEN 'FE'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'EUR' AND FON."IdMercadoFondoMutuo" = 'RV' THEN 'VE'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'USD' AND FON."IdMercadoFondoMutuo" = 'RV' THEN 'VD'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'CLP' AND FON."IdMercadoFondoMutuo" = 'RV' THEN 'RV'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'CLP' AND FON."IdMercadoFondoMutuo" = 'MX' THEN 'MX'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'USD' AND FON."IdMercadoFondoMutuo" = 'MX' THEN 'MD'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'EUR' AND FON."IdMercadoFondoMutuo" = 'MX' THEN 'ME'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'EUR' AND FON."IdMercadoFondoMutuo" = '' THEN 'RV'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'CLP' AND COALESCE(FON."IdMercadoFondoMutuo",'') = '' THEN 'RV'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'USD' AND COALESCE(FON."IdMercadoFondoMutuo",'') = '' THEN 'VD'
				WHEN  TIN."IdClasificacionTipoInstrumento" = 'FM' AND INS."IdMoneda" = 'EUR' AND COALESCE(FON."IdMercadoFondoMutuo",'') = '' THEN 'VE'
				ELSE '' 
		END 													AS "TIPO_FAM",
		CASE 
				WHEN FON."IdEstado" = 'ACT' THEN 3
				ELSE 0 END 										AS "TIR_CONTAB", -- DECIMAL  JK > estado del fondo si esta cerrado o abierto 
	    COALESCE (CAST('' AS VARCHAR(10)),'')					AS "VAL_CLASIF_RIESGO", -- CHAR(10) JK > si existe clasificacion de riesgo del fondo setear ()text libre, de lo contrario blanco
	    CASE 
				WHEN TIN."IdClasificacionTipoInstrumento" = 'FM' THEN COALESCE((SELECT "ValorCuota" 
																				FROM "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS VAL 
																				WHERE VAL."IdSerieFondo" = INS."Nemotecnico"), 0)
				ELSE 0 
		END 													AS "VALOR",
		COALESCE (CAST(FON."Diferido" AS VARCHAR(1)),'')		AS "IND_DIFERIDO", -- CHAR(1) JK > indica si el fondo es diferido
	    COALESCE ( 0 , 0 )  									AS "TASA_CAMBIO", -- DECIMAL > JK valor unitario ¿? ni idea que implica eso, aplica solo FM
	    COALESCE (CAST('' AS VARCHAR(10)),'')  					AS "NEMO_ASOC", -- CHAR(10) > JK aplica para fondos diferidos nemo asociado --JL, Alex me indica que esto ya no se usa
	    COALESCE ( FON."DiasRetencionesRescate" , 0 )  			AS "DIAS_RETEN_SORD", -- DECIMAL  JK > dias retencion rescate
	    CASE
			 	WHEN FON."BloqueoAporte" = 'S' AND FON."BloqueoRescate" = 'S' THEN 3
			 	WHEN FON."BloqueoAporte" = 'S' AND FON."BloqueoRescate" = 'N' THEN 1
			 	WHEN FON."BloqueoAporte" = 'N' AND FON."BloqueoRescate" = 'S' THEN 2
			 	ELSE 0 END AS "BLOQUEO_FONDO",
	    COALESCE (CAST(FON."CodigoSerie" AS VARCHAR(1)),'') 	AS "SERIE_FONDO",		
	    ''  													AS "FONDO_PADRE", -- CHAR(10)
	    ''  													AS "COD_IMP_INV", -- CHAR(3)
	    ''  													AS "COD_IMP_RES", -- CHAR(3)
	    COALESCE ( FON."MontoMinimoInvertir" , 0 )				AS "MTO_MIN_INV", -- DECIMAL JK monto minimo para invertir
	   	''														AS "IND_MTO_INV", -- CHAR(1)
	    0  														AS "CLAS_PLAZO_ENT", -- DECIMAL
	    0  														AS "CLAS_PLAZO_SAL", -- DECIMAL
	    CAST(REPLACE(CAST(COALESCE("HoraCierreAporteBackoffice",'00:00:00') AS VARCHAR(8)),':','') AS DECIMAL(10,3))
	    														AS "HORA_CORTE_APOR", -- DECIMAL JK hora de cierre aportes
	    CAST(REPLACE(CAST(COALESCE("HoraCierreRescateBackoffice",'00:00:00') AS VARCHAR(8)),':','') AS DECIMAL(10,3))
	    														AS "HORA_CORTE_RESC", -- DECIMAL  jk hora cierre pa rescates
	    ''														AS "CLAS_MERCADO", -- CHAR(2)
	   '1900-01-01'  											AS "FEC_INI_BLQAPO", -- CHAR(10)
	   '1900-01-01'  											AS "FEC_FIN_BLQAPO", -- CHAR(10)
	    COALESCE ( FON."MontoMaximo" , 0 )  					AS "LIMITE_INVERSION", -- DECIMAL JK > maximo de inversion
	    '1900-01-01' 											AS "FEC_INI_BLQRES", -- CHAR(10)
	    '1900-01-01'  											AS "FEC_FIN_BLQRES", -- CHAR(10)
	    '1900-01-01'  											AS "FEC_CIE_APO", -- CHAR(26) JK validar con mario san
	    '1900-01-01'  											AS "FEC_CIE_RES", -- CHAR(26)JK validar con mario san
	    COALESCE (CAST(FON."CobraComision" AS VARCHAR(1)),'')	AS "IND_AFE_COMI", -- CHAR(1) -- JK indica si el fondo es afecto a comisiones
	    CASE 	WHEN EXISTS(SELECT 1 --"IdRegimenFondoMutuo", "IdSerieFondo", "IdEstado", "IdEmpresa"
							FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS RFO
							WHERE RFO."IdSerieFondo" = FON."IdSerieFondo" AND RFO."IdRegimenFondoMutuo" = '57BIS' ) THEN 'S'
				ELSE 'N' END 									AS "IND_AFE_57B",
	    'N'														AS "IND_EXCLUYE_REIN", -- CHAR(1) --
	    ''														AS "TIPO_CARTERA",  
	    'N'  													AS "IND_18_TER", -- CHAR(1)
	    'N'  													AS "IND_18_CUATER", -- CHAR(1)
	    'FM' 													AS "TIPO_INVERSION", -- CHAR(2)
	    'N'  													AS "IND_INV_CALIF", -- CHAR(1)
	    COALESCE (CAST(FON."MoneyMarket" AS VARCHAR(1)),'')		AS "IND_MONEY_MARKET", -- CHAR(1) JK indica si es money market
	    COALESCE (CAST(FON."EsFondoNacional" AS VARCHAR(1)),'')	AS "IND_FONDO_NACIONAL", -- CHAR(1) JK indicasi es nacional o extranjero
	    COALESCE ( FON."MontoMaximo" , 0 )						AS "MTO_MAX_INV", -- DECIMAL  JK monto maximo de inversion
	    COALESCE ( 0 , 0 )										AS "SUMA_DIAS_APORTE", -- DECIMAL JK dias adicionales al aporte
	    ''														AS "NEMO_BCS", -- CHAR(10)
	    ''														AS "IND_EXC_TRANSF", -- CHAR(1)
	    COALESCE (CAST('' AS VARCHAR(1)),'')					AS "IND_RES_PRO", -- CHAR(1)  -- tiene rescate programado 
	    0														AS "COD_FAMILIA", -- INT
	    'N'														AS "IND_GTIA", -- CHAR(1)
	    'N'														AS "IND_CFI", -- CHAR(1)
	    ''														AS "CAT_INV_CALIFICADO", -- CHAR(1)
	    ''														AS "IND_CANAL", -- CHAR(3)
	    ''														AS "RUT_FONDO", -- CHAR(11)
		LEFT(INS."ISIN",12)										AS "ISIN",
		'' AS RUT_ADM,
		COALESCE(CAST(INS."Nemotecnico" AS CHAR(16)), '') AS "NEMO_RAIZ",
		COALESCE(CAST(INS."IdCondicionLiquidacion" AS CHAR(5)), '') AS "COND_LIQ",
		CAST(
			CASE 
				WHEN INS."IdTipoInstrumento" = 'OSA' THEN 'S'
				ELSE 'N'
			END AS CHAR(1)
		) AS "IND_OSA",
		CAST(COALESCE(INS."IdPaisEmisor", 0) AS CHAR(5)) AS "PAIS_EMISOR"
		
	FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIN ON TIN."IdTipoInstrumento" = INS."IdTipoInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = EMI."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
	LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" AS FON ON FON."IdInstrumento" = INS."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS_MISMONEMO on INS."Nemotecnico"= INS_MISMONEMO."Nemotecnico" and INS."IdInstrumento" != INS_MISMONEMO."IdInstrumento" AND INS."IdTipoInstrumento" != 'FFM'
	
	LEFT JOIN (
			SELECT "IdInstrumento", CASE WHEN RANK > 1 THEN CONCAT(RPAD("Nemotecnico",15),RANK) ELSE "Nemotecnico" END AS "Nemotecnico" FROM (
				SELECT RANK() OVER (PARTITION BY RPAD("Nemotecnico",15) ORDER BY "Nemotecnico" ASC)  as RANK, "IdInstrumento", RPAD("Nemotecnico",15) AS "Nemotecnico" 
				FROM "BSCL.Plataforma.BD::INS.Instrumento"
			) WHERE RANK < 15
		) AS COD_NEMO_TRUNK ON COD_NEMO_TRUNK."IdInstrumento" = INS."IdInstrumento"
	
	WHERE TIN."IdClasificacionTipoInstrumento" NOT IN ('IF') and TIN."IdTipoInstrumento" not in ('SIM','PRT','EMP','FWD','SPT','UNK','PRF','BR','XER','CER','ZER','ERF','COR')
	AND INS_MISMONEMO."Nemotecnico" IS NULL

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBPLMAIN" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
