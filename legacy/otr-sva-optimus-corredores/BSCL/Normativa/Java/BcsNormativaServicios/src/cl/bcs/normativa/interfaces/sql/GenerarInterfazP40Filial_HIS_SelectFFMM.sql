	SELECT    
  	         TO_VARCHAR(CAR."FechaOperacion", 'YYYYMMDD') 		 	AS "FechaInversion"
	        ,LPAD(COALESCE(EMI."DocumentoIdentidad",''), 10, '0') 	AS "RutAdminsitradorFondo"
	        ,COALESCE(CAR."Nemotecnico",'')							AS "Nemotecnico"
	        ,CAR."IdClasificacionTipoInstrumento"	        
	        ,CAR."IdTipoInstrumento"
	        ,LPAD(COALESCE(F."IdPaisOrigen",0), 3, '0')         	AS "Pais"
	        ,LPAD(COALESCE(F."IdTipoFondo", '0'), 1,'0')            AS "IdTipoFondo"	        
	        ,COALESCE(CAR."INGCostoInversionEnFactorAjuste",0)  	AS "CostoAdquisición"
			,COALESCE(CAR."INGCantidad", 0)							AS "NumeroDeCuotas"
	        ,CASE WHEN CAR."IdMonedaInstrumento" = 'EUR' THEN '142'
	        	  WHEN CAR."IdMonedaInstrumento" IN ('USD', 'DO') THEN '994'	-- Dolar Observado
	        	  WHEN CAR."IdMonedaInstrumento" = 'IV'  THEN '997'
	        	  WHEN CAR."IdMonedaInstrumento" = 'UF'  THEN '998'
	        	  WHEN COALESCE(CAR."IdMonedaInstrumento",'CLP') IN ('CLP','NR')  THEN '999'
	         	  ELSE '880'
	         END 													AS "Moneda"				
			,COALESCE(CAR."INGPrecioCompra",0)						AS "ValorInicialCuota"	        
			,COALESCE(CAR."INGPrecioPromedioCompra",0)				AS "ValorCuota"			
			,COALESCE(CAR."INGMontoATasaMercadoActual",0)			AS "ValorRazonable"
	FROM   "BSCL.Custodia.BD::HIS.Cartera" AS CAR 
    LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SF ON (SF."IdInstrumento" = CAR."IdInstrumento")
    LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS F ON (F."IdFondo" = SF."IdFondo")
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI ON EMI."IdEmisor" = F."IdEmisor"    
	WHERE CAR."IdEmpresa"						= :IdEmpresa
	  AND CAR."IdEstado" 						= 'VIG'
	  AND CAR."IdClasificacionTipoInstrumento" 	= 'FM'
	  AND CAR."IdCliente" 						= :IdCliente
	  AND CAR."FechaRespaldo"  					= :Fecha