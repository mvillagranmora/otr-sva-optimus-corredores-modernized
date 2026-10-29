SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ999.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ999.Origen" (
     "IdEmpresa"
    ,"IdOperacionSpot"
	,"EventoReporte"							
	,"CodigoInstitucion" 						
	,"NumeroOperacion"					
	,"FechaOperacion" 
	,"TipoDocumento"  				
	,"CodigoComercio" 				
	,"IdPais" 						
	,"MonedaOperacion" 				
	,"MontoOperacion" 				
	,"MontoDolar"  					
	,"TipoCambio" 					
	,"MontoEquiClp" 					
	,"AfectoADerivado"  				
	,"DocumentoIdentidad" 			
    ,"NombreCompleto"  				
	,"TipoAutorizacion"  			
	,"NumeroAutorizacion" 			
	,"FechaAutorizacion" 			
	,"SectorEconomicoBeneficiario" 	
	,"NumeroInscripcion" 			
	,"FechaDesembolso" 				
	,"SectorEconomicoInversionista"
	,"NombreFinancistaExtranjero" 	
	,"FechaVencimientoCapital" 		
	,"NroContDerivado" 												
	,"FechaSuscDerivado" 	
    ,"FechaVencDerivado" 	
	,"Observacion"  
	,"EstadoGeneracion"
	,"FecUltMod"
	,"UsrUltMod"
	,"EsArbitraje"
	,"CodigoValidacion"
) AS
WITH OPERACIONES AS (SELECT	
	 EMP."IdEmpresa"
	,SPT."IdOperacionSpot"
	,CASE WHEN SPT."Estado" = 'VIG' THEN 'N'
	     WHEN SPT."Estado" = 'MOD' THEN 'M'
		 WHEN SPT."Estado" = 'ANU' THEN 'E'
	END AS "EventoReporte"												--Evento 				char(1)        -- 1 Evento de reporte
	,COALESCE(CONFMES."Valor", 0) AS "CodigoInstitucion" 				--CodigoInstitucion		Numeric (3)    -- 2 Codigo institucion
	,SPT."FolioPlanilla" AS "NumeroOperacion" 							--NroOpe 				Numeric (12)   -- 3 numero operacion
	,TO_NVARCHAR(SPT."FechaOperacion",'YYYY-MM-DD') AS "FechaOperacion" --FechaOperacion 		Numeric(8)     -- 4 fecha de operacion
	,1 AS "TipoDocumento"  												--Tipodocumento 		Numeric(1)     -- 5 tipo de documento
	,SPT."CodigoComercio"  												--CodigoOperaciones 	Numeric(5)     -- 6 Codigo de Operacion de Cambios
	,PAIS."CodigoAduana" AS "IdPais" 									--PaisOpe 				Numeric(3)     -- 7 Pais de la operacion
	,COALESCE(MON."CodigoNumerico",'0')	AS "MonedaOperacion" 			--MonedaOp 				Numeric(3)     -- 8 Moneda de la operacion
	,ROUND(SPT."MontoMonedaPrincipal",2) AS "MontoOperacion" 			--MontoOri 				Decimal(20,2)  -- 9 Monto de la Operacion en la Moneda de Origen
	,CASE WHEN SPT."IdMonedaPrincipal"='USD' THEN SPT."MontoMonedaPrincipal"
          WHEN  SPT."IdMonedaSecundaria"='USD'  THEN SPT."MontoMonedaSecundaria"
		  WHEN MON."IdTipoMoneda" = 'FUE' AND SPT."IdMonedaPrincipal" <> 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" * 
		 	(CASE WHEN SPT."ParidadCierre" <> 0 THEN SPT."ParidadCierre" ELSE NULLIF(PAR_MON."ParidadMercado",0) END), 0)
		  WHEN MON."IdTipoMoneda" <> 'FUE' AND SPT."IdMonedaPrincipal" <> 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" / 
		 	(CASE WHEN SPT."ParidadCierre" <> 0 THEN SPT."ParidadCierre" ELSE NULLIF(PAR_MON."ParidadMercado",0) END), 0) 
		  WHEN SPT."IdMonedaPrincipal" = 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" / 
		        (CASE WHEN SPT."TipoCambioCierre" <> 0 THEN SPT."TipoCambioCierre" ELSE NULLIF(PAR_USD."TipoCambioMercado",0) END), 0)
         ELSE 0
	 END AS "MontoDolar"  												--MontoDolar 			Decimal(20,2)   -- 10 Monto Operacion en Dolares
	,Round(SPT."TipoCambioCierre",2) AS "TipoCambio" 					--TipoCambio 			Decimal(10,2)   -- 11 Tipo de Cambio
	,CASE WHEN SPT."IdMonedaSecundaria" = 'CLP' 
		THEN Round(Round(SPT."MontoMonedaPrincipal",2) * Round(COALESCE(SPT."TipoCambioCierre",0),2),2)
	 	ELSE SPT."MontoMonedaSecundaria" * COALESCE(PAR_MON."TipoCambioMercado",0) 
	END AS "MontoEquiClp" 												--MontoEqPeso 			Decimal(20,2)   -- 12 Monto equivalente en Pesos
	,CASE WHEN SPT."IdTipoCanalIngreso" = 'FWD' THEN 1 
	 ELSE
	  	CASE WHEN SPT."AfectoADerivado" = 'S' THEN 1 
		     WHEN SPT."AfectoADerivado" = 'N' THEN 0 
		ELSE 0 END 
	 END AS "AfectoADerivado"  											--AfectoADerivados 		Numeric(1) 	   -- 13 Afecto a Derivados
	,Cliente."DocumentoIdentidad" 										--RutInteresado 		char(10) 	   -- 14 RUT del Interesado	 		
	,Cliente."NombreCompleto"  											--NombreInteresado 		char(100) 	   -- 15 Nombre del Interesado	
	,CASE WHEN SPT."CodigoComercio" IN (21100, 21101, 21485, 21486) THEN 'EE'
		ELSE ''
	END AS "TipoAutorizacion"  											--TipoAutorizacion      char(2)        -- 16 Tipo de Autorizacion
	,0  AS "NumeroAutorizacion" 											--NumAutorizacion       Numeric(6)     -- 17 Numero de la Autorizacion
	,'0001-01-01' AS "FechaAutorizacion" 											--FecAutorizacion       Numeric(8)     -- 18 Fecha de la Autorizacion
	,CASE WHEN COALESCE(CONFMES."Valor", 0) = 125 AND SPT."CodigoComercio" IN (11260, 21260) THEN 81 ELSE 0 END  AS "SectorEconomicoBeneficiario" 								--SectorEcoBene         Numeric(2)     -- 19 Sector Economico Beneficiario
	,0  AS "NumeroInscripcion" 											--NumInscripcion        Numeric(8)     -- 20 Numero de Inscripcion
	,'0001-01-01' AS "FechaDesembolso" 											--FecDesembolso         Numeric(8)     -- 21 Fecha Desembolso
	,CASE WHEN COALESCE(CONFMES."Valor", 0) = 125 AND SPT."CodigoComercio" IN (11260, 21260) THEN 81 ELSE 0 END  AS "SectorEconomicoInversionista" 								--SectorEcoInver        Numeric(2)     -- 22 Sector Economico del Inversionista o del Receptor de un Anticipo de Comprador
	,CASE WHEN COALESCE(CONFMES."Valor", 0) = 125 AND SPT."CodigoComercio" IN (11260, 21260) THEN 'xxxxxx' ELSE '' END AS "NombreFinancistaExtranjero" 								--NomFinancistaExt      char(100)      -- 23 Nombre del financista extranjero
	,'0001-01-01' AS "FechaVencimientoCapital" 									--FecVencimientoCap     Numeric(8)     -- 24 Fecha vencimiento del Capital, Interes o Comision
	,CASE WHEN SPT."IdTipoCanalIngreso" = 'FWD' THEN TO_NVARCHAR(SPT."FolioOrigenFW")
	 ELSE
	  	CASE WHEN SPT."AfectoADerivado" = 'S' THEN TO_NVARCHAR(SPT."FolioOrigenFW") 
		     WHEN SPT."AfectoADerivado" = 'N' THEN 0 
		ELSE 0 END 
	 END AS "NroContDerivado" 																		 	--NumContratoDeri		char(52)       -- 25 Numero del Contrato de Derivado
	,COALESCE(TO_NVARCHAR(FWD."FechaOperacion",'YYYY-MM-DD'),'0001-01-01') AS "FechaSuscDerivado" 			 	--FecSusContratoDer		Numeric(8)     -- 26 Fecha suscripcion del Contrato de Derivado
	,COALESCE(TO_NVARCHAR(FWD."FechaVencimientoOperacion", 'YYYY-MM-DD'), '0001-01-01') AS "FechaVencDerivado" 	--FecVenContratoDer		Numeric(8)     -- 27 Fecha vencimiento del Contrato de Derivado
	,CASE WHEN TRIM(SPT."Observacion") = '' THEN 'Sin observaciones' 
		ELSE SPT."Observacion" 
	 END AS "Observacion"--Observaciones 		char(250)	   -- 28 Observaciones
	,0 AS "EsArbitraje"
	,'' AS "CodigoValidacion"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente 
	ON Cliente."IdCliente" = SPT."IdCliente"
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP
	ON (EMP."IdEmpresa" = SPT."IdEmpresa")	
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS
	ON (EMP."IdPais" = PAIS."IdPais")
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" FWD 
	ON  (SPT."FolioOrigenFW" = FWD."FolioCartera")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON   
	ON (SPT."IdMonedaPrincipal" = MON."IdMoneda")	
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR_MON
	ON (SPT."IdMonedaPrincipal" = PAR_MON."IdMoneda" AND TO_CHAR(SPT."FechaOperacion",'YYYY-MM-DD') = TO_CHAR(PAR_MON."Fecha",'YYYY-MM-DD'))
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR_USD
    ON ('USD' = PAR_USD."IdMoneda" AND TO_CHAR(SPT."FechaOperacion",'YYYY-MM-DD') = TO_CHAR(PAR_USD."Fecha",'YYYY-MM-DD'))
LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONFMES 
    ON CONFMES."IdEmpresa" = SPT."IdEmpresa" AND CONFMES."IdTipoProducto" = 'MEME' AND CONFMES."CodigoParametro" = 'CODIGOCORREDORAOMA'	
WHERE SPT."OperacionFicticia" = 'N'
AND SPT."Estado" ='VIG'

UNION ALL --ARBITRAJES

SELECT
     EMP."IdEmpresa"
    ,SPT."IdOperacionSpot"
	,CASE WHEN SPT."Estado" = 'VIG' THEN 'N'
	     WHEN SPT."Estado" = 'MOD' THEN 'M'
		 WHEN SPT."Estado" = 'ANU' THEN 'E'
	 END AS "EventoReporte"											    --Evento 				char(1)        -- 1 Evento de reporte
	,COALESCE(CONFMES."Valor", 0) AS "CodigoInstitucion"				--CodigoInstitucion		Numeric (3)    -- 2 Codigo institucion
	,0 AS "NumeroOperacion"												--NroOpe 				Numeric (12)   -- 3 numero operacion
	,TO_NVARCHAR(SPT."FechaOperacion",'YYYY-MM-DD') AS "FechaOperacion" --FechaOperacion 		Numeric(8)     -- 4 fecha de operacion
	,1 AS "TipoDocumento" 												--Tipodocumento 		Numeric(1)     -- 5 tipo de documento
	,SPT."CodigoComercio" 												--CodigoOperaciones 	Numeric(5)     -- 6 Codigo de Operacion de Cambios
	,PAIS."CodigoAduana" AS "IdPais"									--PaisOpe 				Numeric(3)     -- 7 Pais de la operacion
	,COALESCE(MON."CodigoNumerico",'0')	AS "MonedaOperacion"			--MonedaOp 				Numeric(3)     -- 8 Moneda de la operacion
	,ROUND(SPT."MontoMonedaSecundaria",2) AS "MontoOperacion"			--MontoOri 				Decimal(20,2)  -- 9 Monto de la Operacion en la Moneda de Origen
	,CASE WHEN  SPT."IdMonedaSecundaria"='USD'  THEN SPT."MontoMonedaSecundaria"
		  WHEN MON."IdTipoMoneda" = 'FUE' AND SPT."IdMonedaSecundaria" <> 'CLP' THEN COALESCE(SPT."MontoMonedaSecundaria" * 
		 	(CASE WHEN SPT."ParidadCierre" <> 0 THEN SPT."ParidadCierre" ELSE NULLIF(PAR_MON."ParidadMercado",0) END), 0)
		  WHEN MON."IdTipoMoneda" <> 'FUE' AND SPT."IdMonedaSecundaria" <> 'CLP' THEN COALESCE(SPT."MontoMonedaSecundaria" / 
		 	(CASE WHEN SPT."ParidadCierre" <> 0 THEN SPT."ParidadCierre" ELSE NULLIF(PAR_MON."ParidadMercado",0) END), 0) 
		  WHEN SPT."IdMonedaSecundaria" = 'CLP' THEN COALESCE(SPT."MontoMonedaSecundaria" / 
		        (CASE WHEN SPT."TipoCambioCierre" <> 0 THEN SPT."TipoCambioCierre" ELSE NULLIF(PAR_USD."TipoCambioMercado",0) END), 0)
         ELSE 0
	 END AS "MontoDolar"  												--MontoDolar 			Decimal(20,2)   -- 10 Monto Operacion en Dolares
	,Round(SPT."TipoCambioCierre",2) AS "TipoCambio"					--TipoCambio 			Decimal(10,2)   -- 11 Tipo de Cambio
	,CASE WHEN SPT."IdMonedaSecundaria" = 'CLP' 
		THEN Round(Round(SPT."MontoMonedaSecundaria",2) * Round(COALESCE(SPT."TipoCambioCierre",0),2),2)
	 	ELSE SPT."MontoMonedaSecundaria" * COALESCE(PAR_MON."TipoCambioMercado",0) 
	END AS "MontoEquiClp"												--MontoEqPeso 			Decimal(20,2)   -- 12 Monto equivalente en Pesos
	,CASE WHEN SPT."IdTipoCanalIngreso" = 'FWD' THEN 1 
	 ELSE
	  	CASE WHEN SPT."AfectoADerivado" = 'S' THEN 1 
		     WHEN SPT."AfectoADerivado" = 'N' THEN 0 
		ELSE 0 END 
	 END AS "AfectoADerivado"  											--AfectoADerivados 		Numeric(1) 	   -- 13 Afecto a Derivados
	,Cliente."DocumentoIdentidad"										--RutInteresado 		char(10) 	   -- 14 RUT del Interesado	
	,Cliente."NombreCompleto"											--NombreInteresado 		char(100) 	   -- 15 Nombre del Interesado	
	,CASE WHEN SPT."CodigoComercio" IN (21100, 21101, 21485, 21486) THEN 'EE'
		ELSE ''
	END AS "TipoAutorizacion"											--TipoAutorizacion      char(2)        -- 16 Tipo de Autorizacion
	,0  AS "NumeroAutorizacion"											--NumAutorizacion       Numeric(6)     -- 17 Numero de la Autorizacion
	,'0001-01-01' AS "FechaAutorizacion"											--FecAutorizacion       Numeric(8)     -- 18 Fecha de la Autorizacion
	,CASE WHEN COALESCE(CONFMES."Valor", 0) = 125 AND SPT."CodigoComercio" IN (11260, 21260) THEN 81 ELSE 0 END  AS "SectorEconomicoBeneficiario"								--SectorEcoBene         Numeric(2)     -- 19 Sector Economico Beneficiario
	,0  AS "NumeroInscripcion"											--NumInscripcion        Numeric(8)     -- 20 Numero de Inscripcion
	,'0001-01-01' AS "FechaDesembolso"											--FecDesembolso         Numeric(8)     -- 21 Fecha Desembolso
	,CASE WHEN COALESCE(CONFMES."Valor", 0) = 125 AND SPT."CodigoComercio" IN (11260, 21260) THEN 81 ELSE 0 END  AS "SectorEconomicoInversionista"								--SectorEcoInver        Numeric(2)     -- 22 Sector Economico del Inversionista o del Receptor de un Anticipo de Comprador
	,CASE WHEN COALESCE(CONFMES."Valor", 0) = 125 AND SPT."CodigoComercio" IN (11260, 21260) THEN 'xxxxxx' ELSE '' END AS "NombreFinancistaExtranjero"									--NomFinancistaExt      char(100)      -- 23 Nombre del financista extranjero
	,'0001-01-01' AS "FechaVencimientoCapital"									--FecVencimientoCap     Numeric(8)     -- 24 Fecha vencimiento del Capital, Interes o Comision
	,CASE WHEN SPT."IdTipoCanalIngreso" = 'FWD' THEN TO_NVARCHAR(SPT."FolioOrigenFW")
	 ELSE
	  	CASE WHEN SPT."AfectoADerivado" = 'S' THEN TO_NVARCHAR(SPT."FolioOrigenFW") 
		     WHEN SPT."AfectoADerivado" = 'N' THEN 0 
		ELSE 0 END 
	 END AS "NroContDerivado"																			--NumContratoDeri		char(52)       -- 25 Numero del Contrato de Derivado
	,COALESCE(TO_NVARCHAR(FWD."FechaOperacion",'YYYY-MM-DD'),'0001-01-01') AS "FechaSuscDerivado"					--FecSusContratoDer		Numeric(8)     -- 26 Fecha suscripcion del Contrato de Derivado
	,COALESCE(TO_NVARCHAR(FWD."FechaVencimientoOperacion", 'YYYY-MM-DD'), '0001-01-01') AS "FechaVencDerivado"	--FecVenContratoDer		Numeric(8)     -- 27 Fecha vencimiento del Contrato de Derivado
	,CASE WHEN TRIM(SPT."Observacion") = '' THEN 'Sin observaciones' 
		ELSE SPT."Observacion" 
	 END AS "Observacion"		--Observaciones 		char(250)	   -- 28 Observaciones
	,1 AS "EsArbitraje"
	,'' AS "CodigoValidacion"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente 
	ON Cliente."IdCliente" = SPT."IdCliente"
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP
	ON (EMP."IdEmpresa" = SPT."IdEmpresa")	
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS
	ON (EMP."IdPais" = PAIS."IdPais")
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS 
	ON SPT."IdInstrumento" = INS."IdInstrumento"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" FWD 
	ON  (SPT."FolioOrigenFW" = FWD."FolioCartera")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON   
	ON (SPT."IdMonedaSecundaria" = MON."IdMoneda")	
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR_MON
	ON (SPT."IdMonedaSecundaria" = PAR_MON."IdMoneda" AND TO_CHAR(SPT."FechaOperacion",'YYYY-MM-DD') = TO_CHAR(PAR_MON."Fecha",'YYYY-MM-DD'))
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR_USD
    ON ('USD' = PAR_USD."IdMoneda" AND TO_CHAR(SPT."FechaOperacion",'YYYY-MM-DD') = TO_CHAR(PAR_USD."Fecha",'YYYY-MM-DD'))
LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONFMES 
    ON CONFMES."IdEmpresa" = SPT."IdEmpresa" AND CONFMES."IdTipoProducto" = 'MEME' AND CONFMES."CodigoParametro" = 'CODIGOCORREDORAOMA'
WHERE SPT."OperacionFicticia" = 'N'
AND SPT."Estado" ='VIG'
AND INS."Nemotecnico" IN ('ARBDIS','ARBINT'))
SELECT	 "IdEmpresa" 
		,"IdOperacionSpot"
		,"EventoReporte"	
		,"CodigoInstitucion" 						
		,"NumeroOperacion"				
		,TO_DATE("FechaOperacion")
		,"TipoDocumento"  				
		,"CodigoComercio" 				
		,TO_BIGINT("IdPais") 						
		,"MonedaOperacion" 				
		,"MontoOperacion" 				
		,ROUND("MontoDolar", 2)  AS "MontoDolar"				
		,"TipoCambio" 					
		,"MontoEquiClp" 					
		,"AfectoADerivado"  				
		,"DocumentoIdentidad" 			
		,"NombreCompleto"  				
		,"TipoAutorizacion"  			
		,"NumeroAutorizacion" 			
		,TO_DATE("FechaAutorizacion")			
		,"SectorEconomicoBeneficiario" 	
		,"NumeroInscripcion" 			
		,TO_DATE("FechaDesembolso")			
		,"SectorEconomicoInversionista"
		,"NombreFinancistaExtranjero" 	
		,TO_DATE("FechaVencimientoCapital") 		
		,"NroContDerivado" 												
		,TO_DATE("FechaSuscDerivado") 	
		,TO_DATE("FechaVencDerivado")	
		,"Observacion"  		
		,'N' AS "EstadoGeneracion"
		,TO_DATE('0001-01-01') AS "FecUltMod"
		,'SISTEMA' AS "UsrUltMod" 
		,"EsArbitraje"
		,"CodigoValidacion"
FROM OPERACIONES 
ORDER BY "IdOperacionSpot"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ999.Origen" TO BCS_DESARROLLADOR WITH GRANT OPTION;