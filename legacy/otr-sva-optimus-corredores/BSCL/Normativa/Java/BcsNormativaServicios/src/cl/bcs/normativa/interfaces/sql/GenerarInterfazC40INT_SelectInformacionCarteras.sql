 SELECT OPER."IdOperacion", 
	'01' as "Banda",
	OPER."IdTipoOperacion", 
	INS."Nemotecnico" AS "Nemotecnico",
	DET."IdTipoReajuste",
	OPER."FechaOperacion" AS "FechaOperacion", 
	OPER."FechaVencimiento" AS "FechaVencimiento", 
	DET."IngMontoValorizado",
	OPER."PlazoEnDias",
	DET."Tasa",
	DET."TasaMercado",
	DET."IdClasificacionTipoInstrumento",
	OPER."MontoOperacionInicial" as "Capital",
	CAR."IdTipoInstrumento" as "TipoInstrumento",
	CASE  WHEN CAR."IdTipoInstrumento" = 'PRC'  THEN '110301200000'
			  WHEN CAR."IdTipoInstrumento" = 'CER'  THEN '110301200000'
			  WHEN CAR."IdTipoInstrumento" = 'ZER'  THEN '110301200000'
			  WHEN CAR."IdTipoInstrumento" = 'BR'   THEN '110301900000'
			  WHEN CAR."IdTipoInstrumento" = 'BCP'  THEN '110301900000'
			  WHEN CAR."IdTipoInstrumento" = 'BCU'  THEN '110301900000'
			  WHEN CAR."IdTipoInstrumento" = 'BCD'  THEN '110301900000'
			  WHEN CAR."IdTipoInstrumento" = 'BCX'  THEN '110301900000'
			  WHEN CAR."IdTipoInstrumento" = 'PRD'  THEN '110302300000'
			  WHEN CAR."IdTipoInstrumento" = 'PRC'  THEN '110302200000'
			  WHEN CAR."IdTipoInstrumento" = 'PAG'  THEN '110302200000'
	          ELSE '110302900000'
	END 																			 as "Cuenta",
	CASE  WHEN CAR."IdTipoInstrumento" = 'PRC'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'CER'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'ZER'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'BR'   THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'BCP'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'BCU'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'BCD'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'BCX'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'PRD'  THEN '1725'
			  WHEN CAR."IdTipoInstrumento" = 'PRC'  THEN '1725'
			  WHEN CAR."IdTipoInstrumento" = 'PAG'  THEN '1725'
	          ELSE '110302900000'
	END 																			 as "Partida",
	'00' as "CuentaCom",
	COALESCE(CAR."INGMontoInteresMercadoProximo"+ CAR."INGMontoReajusteMercadoProximo",0) as "Readev",
	COALESCE(CAR."INGMontoReajusteCorridoCompraActual",0) as "Reaproy",
	
	CASE 
		WHEN COALESCE(CAR."INGMontoInteresCorridoMercadoActual",0) <0 
			THEN 0
			ELSE COALESCE(CAR."INGMontoInteresCorridoMercadoActual",0) 
	END AS  "Interes",
	
	COALESCE(CAR."INGMontoInteresMercadoProximo",0) AS "IntProy",
	CASE WHEN CAR."IdTipoReajuste" = 'CLP' THEN '999'
				  WHEN CAR."IdTipoReajuste" IN ('USD', 'DO') THEN '997'	-- Dolar Observado
	        	  WHEN CAR."IdTipoReajuste" = 'IV'  THEN '997'		
	        	  WHEN CAR."IdTipoReajuste" = 'UF'  THEN '998'		
	        	  WHEN COALESCE(CAR."IdTipoReajuste",'') IN ('','NR')  THEN '999'                	  
	         	  ELSE '999'
	END 																				AS "Moneda"
	FROM    "BSCL.Mesa.BD::MES.Operacion" AS OPER
    LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS DET ON DET."IdOperacion" = OPER."IdOperacion"
    LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = DET."IdOperacion"
    LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON MES."IdMesa" = CAR."IdMesa"
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON DET."IdInstrumento" = INS."IdInstrumento"
    WHERE OPER."IdEmpresa"						 = :IdEmpresa
	  AND OPER."IdEstado" 						 = 'VIG'
	  AND DET."IdClasificacionTipoInstrumento" IN ('RF','IF')
	  AND OPER."FechaOperacion"					<= :FiltroFecha
	  
 UNION
 
 SELECT '1' AS "IdOperacion", 
	'01' as "Banda",
	'1' as "IdTipoOperacion", 
	CAR."Nemotecnico" AS "Nemotecnico",
	'1' AS "IdTipoReajuste",
	CAR."FechaOperacion" AS "FechaOperacion", 
    CAR."FechaVencimientoOperacion" AS "FechaVencimiento", 
	1 AS "IngMontoValorizado",
	1 AS "PlazoEnDias",
	1 AS "Tasa",
	1 AS "TasaMercado",
	'1' AS "IdClasificacionTipoInstrumento",
	COALESCE(CAR."INGCantidad",0) AS "Capital",
	CAR."IdTipoInstrumento" as "TipoInstrumento",
	CASE  WHEN CAR."IdTipoInstrumento" = 'PRC'  THEN '110301200000'
			  WHEN CAR."IdTipoInstrumento" = 'CER'  THEN '110301200000'
			  WHEN CAR."IdTipoInstrumento" = 'ZER'  THEN '110301200000'
			  WHEN CAR."IdTipoInstrumento" = 'BR'   THEN '110301900000'
			  WHEN CAR."IdTipoInstrumento" = 'BCP'  THEN '110301900000'
			  WHEN CAR."IdTipoInstrumento" = 'BCU'  THEN '110301900000'
			  WHEN CAR."IdTipoInstrumento" = 'BCD'  THEN '110301900000'
			  WHEN CAR."IdTipoInstrumento" = 'BCX'  THEN '110301900000'
			  WHEN CAR."IdTipoInstrumento" = 'PRD'  THEN '110302300000'
			  WHEN CAR."IdTipoInstrumento" = 'PRC'  THEN '110302200000'
			  WHEN CAR."IdTipoInstrumento" = 'PAG'  THEN '110302200000'
	          ELSE '110302900000'
	END 																			 as "Cuenta",
		CASE  WHEN CAR."IdTipoInstrumento" = 'PRC'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'CER'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'ZER'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'BR'   THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'BCP'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'BCU'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'BCD'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'BCX'  THEN '1705'
			  WHEN CAR."IdTipoInstrumento" = 'PRD'  THEN '1725'
			  WHEN CAR."IdTipoInstrumento" = 'PRC'  THEN '1725'
			  WHEN CAR."IdTipoInstrumento" = 'PAG'  THEN '1725'
	          ELSE '110302900000'
	END 																			 as "Partida",
	'00' as "CuentaCom",
	COALESCE(CAR."INGMontoReajusteCorridoCompraActual",0) as "Readev",
	COALESCE(CAR."INGMontoReajusteCorridoCompraActual",0) as "Reaproy",
	CASE 
		WHEN COALESCE(CAR."INGMontoInteresCorridoMercadoActual",0) >0 
		THEN COALESCE(CAR."INGMontoInteresCorridoMercadoActual",0)
		ELSE 0
	 END AS "Interes",
	
	COALESCE(CAR."INGMontoInteresMercadoProximo",0) AS "IntProy",
	CASE WHEN CAR."IdTipoReajuste" = 'CLP' THEN '999'
				  WHEN CAR."IdTipoReajuste" IN ('USD', 'DO') THEN '997'	-- Dolar Observado
	        	  WHEN CAR."IdTipoReajuste" = 'IV'  THEN '997'		
	        	  WHEN CAR."IdTipoReajuste" = 'UF'  THEN '998'		
	        	  WHEN COALESCE(CAR."IdTipoReajuste",'') IN ('','NR')  THEN '999'                	  
	         	  ELSE '999'
	END 																				AS "Moneda"		
	FROM   "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
	WHERE CAR."IdEmpresa"						= :IdEmpresa
	  AND CAR."IdEstado" 						= 'VIG'
	  AND CAR."IdClasificacionTipoInstrumento" 	= 'RV'
	  AND CAR."IdTipoInstrumento" 				= 'SIM'
	  AND CAR."FechaOperacion"					<= :FiltroFecha;
	 