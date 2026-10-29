SELECT 
		CAST(:v_IdEmpresa AS BIGINT) AS "IdEmpresa",
		CUS."FechaCuadratura",
	    CUS."IdInstrumento",
	    CUS."IdTipoInstrumento",
	    CUS."IdClasificacionTipoInstrumento",
		CUS."Nemotecnico",
		SUM(CAST (CUS."CantidadCustodia" AS DECIMAL(26,8))) 	AS "CantidadCustodia",	--1
		SUM(CAST (CUS."CantidadCompraPlazo" AS DECIMAL(26,8)))	AS "CantidadSimultaneaCompra",	--4
		SUM(CAST (CUS."CantidadVentaPlazo" AS DECIMAL(26,8)))	AS "CantidadSimultaneaVenta",	--3
		
		CAST (0 AS DECIMAL(26,8)) 								AS "CantidadPactoCompra",	--4
		CAST (0 AS DECIMAL(26,8))								AS "CantidadPactoVenta",	--3
		
		CAST (0 AS DECIMAL(26,8))								AS "CantidadPrestamo",
		CAST (0 AS DECIMAL(26,8))								AS "CantidadEmprestito",
		SUM(CAST (CUS."CantidadGarantia" AS DECIMAL(26,8)))		AS "CantidadGarantia",	--2
		SUM(CAST (CUS."TransitoIngreso" AS DECIMAL(26,8)))		AS "CantidadTransitoIngreso",
		SUM(CAST (CUS."TransitoEgreso" AS DECIMAL(26,8)))		AS "CantidadTransitoEgreso",
		SUM(CAST (CUS."CantidadCustodia" AS DECIMAL(26,8)))		AS "CantidadTotal",	-- (1+2+3-4)  
		SUM(CAST (CUS."Valorizacion" AS DECIMAL(26,8)))		    AS "MontoATasaMercadoActual",
		MAX(CUS."Diferencia")									AS "Diferencia",
		''                                     				    AS "ExplicacionDescuadratura",
		
		SUM("CantidadDCVCuentaRelacionado" + "CantidadDisponibleDCVCuentaRelacionado" + "CantidadReventaDCVCuentaRelacionado" + "CantidadRecompraDCVCuentaRelacionado"+ "CantidadValorizadaUFDCVCuentaRelacionado")
			AS "CantidadRelacionado",
		SUM("CantidadDCVCuentaTercero"+"CantidadDisponibleDCVCuentaTercero"+"CantidadBloqueadaDCVCuentaTercero"+"CantidadReventaDCVCuentaTercero"+"CantidadRecompraDCVCuentaTercero"+"CantidadValorizadaUFDCVCuentaTercero")
			AS "CantidadTerceros",						
		SUM("CantidadDCVCuentaPropia"+"CantidadDisponibleDCVCuentaPropia"+"CantidadBloqueadaDCVCuentaPropia"+"CantidadReventaDCVCuentaPropia"+"CantidadRecompraDCVCuentaPropia"+"CantidadValorizadaUFDCVCuentaPropia")
			AS "CantidadCuentaPropia",												
		CAST (0 AS DECIMAL(26,8)) AS "CantidadIntermediario",												
		SUM("CantidadBCS") AS "CantidadBCS",
		SUM("CantidadBEC") AS "CantidadBEC",
		SUM("CantidadBOV") AS "CantidadBOVALPO",
		SUM("CantidadDCVCuentaRelacionado"+"CantidadDCVCuentaPropia"+"CantidadDCVCuentaTercero") AS "CantidadDCV",	
		SUM("CantidadEmisores") AS "CantidadEmisores",																			
		CAST (0 AS DECIMAL(26,8)) AS "CantidadOtros",					
		/*SE AGREGA CANTIDAD GARANTIA POR TIPO*/
		CAST (0 AS DECIMAL(26,8)) AS "CantidadGarantiaBCS",
		CAST (0 AS DECIMAL(26,8)) AS "CantidadGarantiaBEC",
		CAST (0 AS DECIMAL(26,8)) AS "CantidadGarantiaBOVALPO",
		CAST (0 AS DECIMAL(26,8)) AS "CantidadGarantiaDCV",	
		CAST (0 AS DECIMAL(26,8)) AS "CantidadGarantiaEmisores",																			
		CAST (0 AS DECIMAL(26,8)) AS "CantidadGarantiaOtros",
		/**/
		SUM("CantidadDCVCuentaRelacionado") AS "CantidadDCVRelacionado",
		SUM("CantidadDCVCuentaTercero") AS "CantidadDCVTerceros",
		SUM("CantidadDCVCuentaPropia") AS "CantidadDCVCuentaPropia"
		
		
	FROM  "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaCircular1720"	CUS
	WHERE "FechaCuadratura" =:v_FechaOperacion
   GROUP BY  CUS."FechaCuadratura",CUS."IdClasificacionTipoInstrumento",CUS."IdTipoInstrumento", CUS."IdInstrumento",CUS."Nemotecnico";