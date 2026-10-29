SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.FM_INSTRUMENTOS"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.FM_INSTRUMENTOS" AS

Select 
			LEFT(INS."Nemotecnico",16) AS Codigo_Ins,-- char(16)
			LEFT(INS."RazonSocial",60) AS Nombre,-- char(60)
			COALESCE(MON."CodigoNumerico",0) AS Moneda,-- int
			'SFM' AS Codigo_Gem,-- char(10)
			LEFT("IdEmisor",10) AS Codigo_Emi,-- char(10)
			ISIN AS CUSIP,-- char(12)
			CASE INS."IdTipoInstrumento"
				WHEN 'ACN' THEN 1027
				WHEN 'OSA' THEN 1038
				WHEN 'ACX' THEN 1028
				WHEN 'BTU' THEN 998
				WHEN 'BR' THEN 997
				WHEN 'BBC' THEN 998
				WHEN 'MIL' THEN 1028	
				WHEN 'BCD' THEN 991
				WHEN 'BCP' THEN 992
				WHEN 'BCU' THEN 993
				WHEN 'BUS' THEN 999
				WHEN 'BCA' THEN 998
				WHEN 'CFX' THEN 1029
				WHEN 'ETX' THEN 1035
				WHEN 'CFI' THEN 1033
				WHEN 'MON' THEN 1040			
				WHEN 'LTH' THEN 1008
				WHEN 'BEM' THEN 994
				WHEN 'COR' THEN 1002
				ELSE 0 END AS Tipo_Ins,-- int
			
			CASE "IdTipoReajuste" 
				WHEN 'UF'  THEN 998
				WHEN 'CLP'  THEN 951
				WHEN 'USD'  THEN 13
				ELSE 
					CASE ins."IdMoneda" 				
						WHEN 'UF'  THEN 998
						WHEN 'CLP'  THEN 951
						WHEN 'USD'  THEN 13
						ELSE 0 END
				END Tipo_Reaj,-- int
				
			'' AS Clasif_Riesgo,-- char(10)
			"PresenciaBursatilAjustada" AS Presencia,-- decimal(5,2)
			CASE WHEN INS."IdEstado" = 'ACT' THEN 'V' ELSE 'B' END   AS Estado,-- char(1)
			
		   CASE  
			LEFT(COALESCE(CASE WHEN TIN."IdClasificacionTipoInstrumento" = 'RV' THEN   hpr."ValorOrigen"  
				    WHEN  INS."IdTipoInstrumento" = 'ETN' THEN   'EN'
				    WHEN  INS."IdTipoInstrumento" = 'ETX' THEN   'ET'
			    		ELSE TIN."IdClasificacionTipoInstrumento" END,TIN."IdClasificacionTipoInstrumento"),2) 
			    	WHEN 'AC'  THEN 901
			    	WHEN 'CF'  THEN 902
			    	WHEN 'FU'  THEN 903	
			    	WHEN 'OP'  THEN 904	
			    	WHEN 'MO'  THEN 905
			    	WHEN 'RF'  THEN 906
			    	WHEN 'IF'  THEN 907
			    	WHEN 'AI'  THEN 985
			    	WHEN 'BI'  THEN 986
			    	WHEN 'CI'  THEN 1135
			    	ELSE 0 END 
			    		AS Mercado,
 											
			"Precio" AS Precio_Mdo,-- decimal(20,6)
			COALESCE("Tasa",0) AS Tasa_Mdo,-- decimal(7,4)
			"CorteMinimo" AS Corte_Min,-- decimal(14,4)
			160 AS Pais,-- int
			"FechaVencimiento"  AS Fec_Vcto,-- char(10)
			0 AS Sector,-- int
			0 AS Dias_Base,-- smallint
			ISIN,-- char(15)
			0 AS Duration,-- decimal(7,4)
			0 AS Pos_Duration,-- int
			0 AS Pos_Fila,-- int
			160 AS Pais_Tran,-- int
			'' AS Fec_Pre_Pago,-- char(10)
			COALESCE("Tasa",0) AS Tasa_Emi,-- decimal(7,4)
			0 AS bcs_tasa,-- decimal(7,4)
			0 AS bcs_tasa_min,-- decimal(7,4)
			0 AS bcs_tasa_may,-- decimal(7,4)
			0 AS Convexidad,-- decimal(7,4)
			0 AS CodTipoInstrSBIF,-- int
			'' AS ClasifRiesgo1,-- varchar(10)
			0 AS CodAgciaClasif1,-- int
			'' AS ClasifRiesgo2,-- varchar(10)
			0 AS CodAgciaClasif2,-- int
			'' AS ClasifRiesgo3,-- varchar(10)
			0 AS CodAgciaClasif3,-- int
			'' AS SEDOL,-- char(12)
			'' AS COMMON,-- char(12)
			'' AS TICKERBLOOM,-- char(15)
			'S' AS AutorizadoCCR,-- char(1)
			'' AS ClaseTitulo,-- char(5)
			'' AS NumeroEmision,-- char(20)
			'' AS TipoTitulo,-- char(1)
			0 AS Spread,-- numeric(12,4)
			'' AS UnidadExpedicion,-- char(5)
			0 AS MinimoEmision,-- numeric(19,4)
			0 AS MultiploEmision,-- numeric(19,4)
			0 AS MultiploFraccionamiento,-- numeric(19,4)
			0 AS ValorIntrinseco,-- numeric(19,4)
			'' AS PagoDividendos,-- char(1)
			'1900-01-01 00:00:00' AS FechaiNicioExDividendos,-- datetime
			'1900-01-01 00:00:00' AS FechaFinalExDividendo,-- datetime
			'' AS AmortizaCapital,-- char(1)
			0 AS FactorAmortizacion,--numeric(19,4)
			'' AS Capitaliza,-- char(1)
			0 AS FactorCapital,-- numeric(19,4)
			'' AS NombreAval,-- char(50)
			'' AS TipoDocAval,-- char(5)
			'' AS NumeroDocAval,-- char(20)
			'' AS TipoAval,-- char(10)
			'' AS BaseProvision,-- char(20)
			'' AS Restricciones,-- char(50)
			'' AS CalificacionLp,-- char(10)
			'' AS CalificacionCp,-- char(10)
			'' AS EntidadCalificadora,-- char(10)
			'' AS EstadoEspecie,-- char(1)
			'' AS CalificacionAval,-- char(6)
			'' AS IinteresesEspeciales,-- char(3)
			'' AS CalidadInversion,-- char(3)
			'' AS ClasificacionTitulo,-- char(3)
			'' AS RiesgoCrediticio,-- char(3)
			'' AS ClasificacionNormativa,-- char(10)
			'S' AS Ind_Inscrito,-- char(1)
			0 AS Bcs_Precio --decimal(20,6)
		
        FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
      	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIN ON TIN."IdTipoInstrumento" = INS."IdTipoInstrumento"
        LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON MON."IdMoneda" = INS."IdMoneda"
        LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr 
				ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
						AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
						AND hpr."ValorDestino" = INS."IdTipoInstrumento" AND  hpr."ValorOrigen"  NOT IN ('CM' , 'ET', 'EN')  )
		WHERE INS."IdTipoInstrumento" != 'FFM'
			AND INS."Nemotecnico" NOT LIKE '%VENTA CORTA%'
        

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.FM_INSTRUMENTOS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
