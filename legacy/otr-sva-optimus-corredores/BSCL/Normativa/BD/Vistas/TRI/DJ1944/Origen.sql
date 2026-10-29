SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1944.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1944.Origen"(
	"IdEmpresa"
    ,"ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"	
   	,"Numero"	
	,"EjecutadoComplementoIPC"
	,"TipoCartera"
	,"RutCliente"
	,"IdCliente"
	,"RutInstitucion"
	,"FechaOperacion"
	,"SalAhorroNetoEjerNEGT1"
	,"SalArrastreEjerSIGT1"
	,"SalAhorroNetoEjerT2"
	,"SalArrastreEjerSigNegT2"
	,"MtoRetiro57BisT2"
	,"SalAhorroNetoEjeNegT3"
	,"SalArrastreEjeSigNegT3"
	,"MtoRetiroAnoT3"
	,"MtoRenRetFFMMPOS"
	,"MtoRenRetFFMMNEG"
	,"MtoRenRetOTROSPOS"
	,"MtoRenRetOTROSNEG"
	,"NumeroCertificado"
	
) AS

---Rescates			 	 
  SELECT	
					sol."IdEmpresa"								AS "IdEmpresa"
					,0 											AS "ID"
					,0 											AS "IdDocumento"
					,'N' 										AS "VboUsuario"
					,'' 										AS "Observaciones"
					,'' 										AS "FuenteDatos"
					,sol."IdSolicitudAporteRescate" 			AS "Numero"
					,''											AS "EjecutadoComplementoIPC"
    				,sol."IdRegimenTributario" 			   		AS "TipoCartera"
			   		,cli."DocumentoIdentidad"					AS "RutCliente"
			 		,cli."IdCliente"							AS "IdCliente"
			 		--,''											AS "RutInstitucion"
			 		,LPAD(INS."DocumentoIdentidad",11,' ')      AS "RutInstitucion"
			 		,sol."FechaValorCuota"						AS "FechaOperacion"
			 		,0 AS "SalAhorroNetoEjerNEGT1"
					,0 AS "SalArrastreEjerSIGT1"
					,CASE WHEN sol."IdRegimenTributario"='57B' THEN ROUND(ROUND(sol."MontoMonedaSistema",0) * (COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1))  * ((12-MONTH(sol."FechaValorCuota"))/12),0) ELSE 0 END AS "SalAhorroNetoEjerT2"
					,CASE WHEN sol."IdRegimenTributario"='57B' THEN ROUND((ROUND(sol."MontoMonedaSistema",0))-((ROUND(sol."MontoMonedaSistema",0))  * (COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)) * ((12-MONTH(sol."FechaValorCuota"))/12)),0) ELSE 0 END AS "SalArrastreEjerSigNegT2"
					,CASE WHEN sol."IdRegimenTributario"='57B' THEN ROUND(sol."MontoMonedaSistema" ,0) ELSE 0 END AS "MtoRetiro57BisT2"
					,CASE WHEN sol."IdRegimenTributario"='57T' THEN ROUND(ROUND(sol."MontoMonedaSistema",0) * (COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1))  * ((12-MONTH(sol."FechaValorCuota"))/12),0) ELSE 0 END AS "SalAhorroNetoEjeNegT3"
					,CASE WHEN sol."IdRegimenTributario"='57T' THEN ROUND((ROUND(sol."MontoMonedaSistema",0))-((ROUND(sol."MontoMonedaSistema",0))  * (COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)) * ((12-MONTH(sol."FechaValorCuota"))/12)),0) ELSE 0 END AS "SalArrastreEjeSigNegT3"
					,CASE WHEN sol."IdRegimenTributario"='57T' THEN ROUND(sol."MontoMonedaSistema",0) ELSE 0 END AS "MtoRetiroAnoT3"
					,COALESCE (ROUND(COALESCE(car."MayorValor",0)   * (IPCACTR."Valor"/IPCINVR."Valor"),0),0) AS  "MtoRenRetFFMMPOS"
					,COALESCE (ROUND(COALESCE(car."MenorValor",0)   * (IPCACTR."Valor"/IPCINVR."Valor"),0),0) * -1 AS  "MtoRenRetFFMMNEG"					
					,0 AS "MtoRenRetOTROSPOS"
					,0 AS "MtoRenRetOTROSNEG"
					,0 AS "NumeroCertificado"
	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol 
	INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" INS ON (INS."IdEmpresa"=sol."IdEmpresa")
					LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS ser_fdo ON (ser_fdo."IdSerieFondo" = sol."IdSerieFondo" )
					LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS fdo ON (fdo."IdFondo" = ser_fdo."IdFondo" )
					LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINVR  ON (substring(ADD_MONTHS (TO_DATE (COALESCE(sol."FechaValorCuota",'1900-01-01'), 'YYYY-MM-DD'), -1),0,7) = IPCINVR."AgnoMes")  
									AND IPCINVR."IdParametroFinanciero" = 'IPC'
	 				LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACTR  ON (SUBSTRING(COALESCE(sol."FechaValorCuota",'1900-01-01'),0,4) || '-11' = IPCACTR."AgnoMes")  		
									AND IPCACTR."IdParametroFinanciero" = 'IPC'	
					LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" uf_mov ON (uf_mov."Fecha"= sol."FechaValorCuota" )  AND uf_mov."IdParametroFinanciero" = 'UF'
									
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli 
					ON cli."IdCliente" = sol."IdCliente" 
					INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" por
					ON por."IdPortafolio"= sol."IdPortafolio"
					LEFT JOIN
					 (
					 
					 		SELECT 
					 		    car."EGRIdOperacion"
								,SUM((CASE
								WHEN COALESCE(ROUND((((((car."EGRMonto" ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" ),0),0) > 0 THEN
								COALESCE(ROUND((((((car."EGRMonto" ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" )),0) --* (IPCACTR."Valor"/IPCINVR."Valor"),0),0)
								ELSE 0
								END))								  AS "MayorValor" 
								,SUM((CASE
								WHEN COALESCE(ROUND((((((car."EGRMonto" ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" ),0),0) < 0 THEN
								COALESCE(ROUND((((((car."EGRMonto" ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" )),0)-- * (IPCACTR."Valor"/IPCINVR."Valor"),0),0)
								ELSE 0
								END))								  AS "MenorValor" 
								FROM "BSCL.Custodia.BD::CUS.Cartera" car
								INNER JOIN  "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" so  ON (so."IdSolicitudAporteRescate"= car."EGRIdOperacion")	
								LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" uf_mov ON (uf_mov."Fecha"= so."FechaValorCuota" ) AND uf_mov."IdParametroFinanciero" = 'UF'
									WHERE 
									car."IdClasificacionTipoInstrumento" = 'FM'
									AND car."IdEstado" IN ('LIQ','MOD','RPA') AND  car."EGRIdOperacion"<>0 AND car."IdRegimenFondoMutuo"='57T' AND car."FechaOperacion" BETWEEN '2015-01-01' AND '2016-12-31'
								GROUP BY 	car."EGRIdOperacion"
									
					 ) car	ON (sol."IdSolicitudAporteRescate"= car."EGRIdOperacion")							
			    	WHERE 	sol."IdTipoMovimientoFondoMutuo" IN  ('RESNORM','RESREINV','RESCDF')
					AND 	sol."IdRegimenTributario" IN ('57B','57T')
					AND 	sol."Estado" = 'CRT'	
					--AND 	sol."FechaValorCuota" BETWEEN '2021-01-01'	 AND '2021-12-31'
					AND (  (sol."IdRegimenTributario" ='57T' AND (ROUND(COALESCE("MayorValor",0),0) <> 0 OR ROUND(COALESCE("MenorValor",0),0) <> 0)) OR   sol."IdRegimenTributario" ='57B'   )


					
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1944.Origen" TO bcs_desarrollador WITH GRANT OPTION;