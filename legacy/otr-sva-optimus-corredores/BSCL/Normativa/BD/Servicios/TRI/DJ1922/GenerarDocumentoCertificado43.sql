SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1922.GenerarDocumentoCertificado43";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1922.GenerarDocumentoCertificado43"(
	-- ID empresa del login.
    IN p_IdEmpresa 						BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 						BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo						NVARCHAR(3)	 ,
    
    IN p_Version						NVARCHAR(10) ,
    
    IN p_Todos							NVARCHAR(1) 	DEFAULT 'S',
    
    --IN p_IdCliente						BIGINT		,
    
    --IN p_IdPortafolio					BIGINT		,
    
    IN p_RutCliente						NVARCHAR(40)	DEFAULT '',
    IN p_CodigoPortafolio		 		NVARCHAR(100)	DEFAULT '',
    
    IN p_Folio							BIGINT		
    
)
	LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	BEGIN


		
		BEGIN
			DECLARE v_resultadoRegla 	NVARCHAR(40);
			DECLARE v_FechaSistema		NVARCHAR(10);
			DECLARE v_DefineFechaCertificado NVARCHAR(10);
	    	DECLARE v_FechaCertificado NVARCHAR(10);
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1922.GenerarDocumentoCertificado43','INICIO','INICIO');
				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			END IF;
			
			SELECT DOCDJ."DefineFechaCertificado" INTO v_DefineFechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1922' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
                       
       		SELECT SUBSTRING(TO_DATE(DOCDJ."FechaCertificado"),1,10) INTO v_FechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1922' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
        
        	IF v_DefineFechaCertificado = 'S'
        		THEN
            		v_FechaSistema := v_FechaCertificado;
        	ELSE
		    	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
        	END IF;
					
			v_logo =
                SELECT TOP 1
                    "Binario" AS "LogoCorredora"
                FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
                WHERE "NombreImagen" = 'LOGO_CORREDOR'
                  AND "IdEmpresa" = :p_IdEmpresa
                ORDER BY "Version" DESC;
            
            v_firma =
                SELECT TOP 1
                    "Binario" AS "FirmaCorredora"
                FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
                WHERE "NombreImagen" = 'FIRMA_CORREDORA'
                ORDER BY "Version" DESC;
               
            v_datos_corredor=
            SELECT "DocumentoIdentidadRepresentante" AS "rut_representante", PERSONA."NombreCompleto" AS "nombre_representante" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante");

			
			v_Resultado = SELECT  	  CERTIFICADO."Folio"
									 ,CERTIFICADO."IdEmpresa"				
									 ,CERTIFICADO."Esquema"				
									 ,CERTIFICADO."Version"				
									 ,CERTIFICADO."Anio"					
									 ,CERTIFICADO."Periodo"				
									 ,CERTIFICADO."IdCliente"				
									 ,CERTIFICADO."IdPortafolio"
									 ,COALESCE(EMISOR."NombreCompleto", '') AS "NombreCompletoEmisor"
									 ,COALESCE(EMISOR."DocumentoIdentidad", '') AS "RutEmisor"
									 ,CASE DETALLE."TipoOperacion"
										 WHEN '1' THEN 'DISTRIBUCIONES'
										 WHEN '2' THEN 'LIQUIDACIONES'
										 WHEN '3' THEN 'DISMINUCIONES'
										 WHEN '4' THEN 'RESCATES'
										 WHEN '5' THEN 'INTERESES'
										 WHEN '6' THEN 'DEVOLUCIONES'
									 END AS "TipoOperacionCertificado"
									 ,COALESCE(CLIENTE."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente"
									 ,CERTIFICADO."RutCliente" AS "DocumentoIdentidadCliente"
									 ,EMPRESA."RazonSocial" AS "RazonSocialEmpresa"
									 ,EMPRESA."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
									 ,EMPRESA."DireccionFisicaPrincipalCalle" || ' ' || EMPRESA."DireccionFisicaPrincipalNumero" || ',' || EMPRESA."DireccionFisicaPrincipalSector" || ' ' || EMPRESA."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa"
									 ,EMPRESA."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
									 ,COALESCE(GIRO."Nombre",'') AS "GiroActividad"
									 ,TO_DATE('1993-06-04') AS "FechaNumeroCuotas"
									 ,TO_DATE(CERTIFICADO."Anio" || '-12-31') AS "FechaMontoActualizado"
									 ---------------------------------------------------------------------------
									 	,DETALLE."IdDetalleCertificadoClienteDJ1922N43"			
										,DETALLE."IdCertificadoCliente"							
										,DETALLE."FechaCertificado"								
										,DETALLE."IdOperacion"									
										,DETALLE."RutAdministradora"							
										,DETALLE."IdEmisor"										
										,DETALLE."RutFondo"										
										,DETALLE."RunFondo"										
										,DETALLE."Fondo"										
										,DETALLE."TipoFondo"									
										,DETALLE."RutCliente"									
										,DETALLE."NombreParticipe"								
										,DETALLE."TaxId"										
										,DETALLE."CodigoPais"									
										,DETALLE."TipoOperacion"								
										,DETALLE."FechaInversion"								
										,DETALLE."MonedaReajuste"								
										,DETALLE."MontoNominalAportes"							
										,DETALLE."MontoNominalAportesActualizado"				
										,DETALLE."NumeroSerieCuotas"							
										,DETALLE."CantidadCuotasParticipe"						
										,DETALLE."ParticipeCumpleRequisitos"					
										,DETALLE."CuotasCumplenRequisitos"						
										,DETALLE."ValorCuotaPesos"								
										,DETALLE."NumCuotasSerieParticipe"							
										,DETALLE."PorcentajeParticipacion31Dic"						
										,DETALLE."ValNominalCuotasRescatadas"						
										,DETALLE."CantidadCuotasRescatadas"							
										,DETALLE."DiferenciaPorRescateOEnajenacion"					
										,DETALLE."DiferenciaPorRescateOEnajenacionActualizado"		
										,DETALLE."DifPorRescateOEnajenacionCFIAcogido107"			
										,DETALLE."DifPorRescateOEnajenacionCFIAcogido107Actualizado"
										,DETALLE."ValNominalInversionaFechaLiquidacion"				
										,DETALLE."CantCuotasCedidasPorLiquidacion"					
										,DETALLE."DiferenciaValorCuotasFechaLiquidacion"			
										,DETALLE."DiferenciaValorCuotasFechaLiquidacionActualizado"	
										,DETALLE."TributaSa"										
										,DETALLE."DivRemDistAfectGCConIDPC"  						
										,DETALLE."DivRemDistAfectGCConIDPCal31122016"  				
										,DETALLE."DivRemDistAfectGCConIDPCVOL"  					
										,DETALLE."DivRemDistAfectGCSINDC" 							
										,DETALLE."DisRemRentasTribCProvRap_C96"  					
										,DETALLE."DisRemRentasTribCORP_C97"  						
										,DETALLE."DisRemRentasTribCExDD_C98"  						
										,DETALLE."DisRemRentasTribCUtAFFut_C99"  					
										,DETALLE."DivRemDistNoRentaFUT"  							
										,DETALLE."DisRemRentasExIGCAfecImptoAdi_C100"  				
										,DETALLE."CredImpGloCompNSRSD"  							
										,DETALLE."CredImpGloCompNSRCD"  							
										,DETALLE."CredIGCAcumD01012017RANSRD01012020SDD_C101"  		
										,DETALLE."CredIGCAcumD01012017RANSRD01012020CDD_C102"  		
										,DETALLE."CredImpGloCompSRSD"  								
										,DETALLE."CredImpGloCompSRCD"  								
										,DETALLE."CredIGCAcumD01012017RXSRSDD_C103"  				
										,DETALLE."CredIGCAcumD01012017RXSRCDD_C104"  				
										,DETALLE."CredImpGloCompCTDCIF"  							
										,DETALLE."CredImpGloCompCDD31122016"  						
										,DETALLE."CredImpGloCompSDD31122016"  						
										,DETALLE."CredIGCAcumH31122016RXSDD_C105"  					
										,DETALLE."CredIGCAcumH31122016RXCDD_C106"  					
										,DETALLE."CredImpGloCompCTDCIF31122016"  					
										,DETALLE."CredImpGloCompCITAExArt21"  						
										,DETALLE."DetBenNetoPerRutFdo_C107" 						
										,DETALLE."DetBenNetoPerPOS_C113"  							
										,DETALLE."DetBenNetoPerNEG_C114"  							
										,DETALLE."DividendosExentosIGCoAdicional"					
										,DETALLE."DividendosExentosIGCoAdicionalActualizado"		
										,DETALLE."DividendosNoConstitutivosDeRenta"					
										,DETALLE."DividendosNoConstitutivosDeRentaActualizado"		
										,DETALLE."DivRemDistNoRentaFE" 								
										,DETALLE."DividendosAfectosImptoUnicoTasa10"				
										,DETALLE."DividendosAfectosImptoUnicoTasa10Actualizado"		
										,DETALLE."DividendosAfectosImptoUnicoTasa4"					
										,DETALLE."DividendosAfectosImptoUnicoTasa4Actualizado"		
										,DETALLE."DistribucionXDiminucionNoImputadaCapital"			
										,DETALLE."DevolucionesDeCapital"							
										,DETALLE."DevolucionesDeCapitalActualizado"					
										,DETALLE."MontoInversionenCuotasCFIAnterior93"				
										,CERTIFICADO."Folio" AS "NumeroCertificado"								
										,DETALLE."FactorActualizacion"								
										,DETALLE."FechaOrigenInversion"	
										,DETALLE."RunNemotecnico"										
								FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
								LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (TRIM(CLIENTE."DocumentoIdentidad") = TRIM(CERTIFICADO."RutCliente"))
								INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" DETALLE	ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")
								INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA ON (EMPRESA."IdEmpresa" = CERTIFICADO."IdEmpresa")
								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."IdPersona" = EMPRESA."IdPersona")
								LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMISOR ON (EMISOR."IdEmisor" = DETALLE."IdEmisor")
								LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoTributario" FONDO ON (DETALLE."Fondo" = FONDO."IdFondo")
								LEFT OUTER JOIN  "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro" GIRO ON (GIRO."IdGiro" = PERSONA."IdGiroPrincipal")
								WHERE p_Todos = 'S' OR (CERTIFICADO."RutCliente" = p_RutCliente AND CERTIFICADO."CodigoPortafolio" = p_CodigoPortafolio)
								AND CERTIFICADO."Version" = p_Version
								ORDER BY CERTIFICADO."Folio";

			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1922.GenerarDocumentoCertificado43','FIN','FIN');
			
			v_sumas = SELECT SUM(CASE WHEN "FechaOrigenInversion" < "FechaNumeroCuotas" THEN "CantidadCuotasParticipe"  ELSE 0 END) AS "SumaNumeroCuotas", SUM("MontoNominalAportesActualizado") AS "SumaMontoActualizado","IdCertificadoCliente"
							FROM :v_Resultado CERTIFICADO
							GROUP BY "IdCertificadoCliente"; 
			
			v_resultado1=SELECT 		
			
			 				  RESULTADO."Folio"
							 ,RESULTADO."IdEmpresa"				
							 ,RESULTADO."Esquema"				
							 ,RESULTADO."Version"				
							 ,RESULTADO."Anio"					
							 ,RESULTADO."Periodo"				
							 ,RESULTADO."IdCliente"				
							 ,RESULTADO."IdPortafolio"
							 ,RESULTADO."NombreCompletoEmisor"
							 ,RESULTADO."RutEmisor"
							 ,RESULTADO."TipoOperacionCertificado"
							 ,RESULTADO."NombreCompletoCliente"
						     ,RESULTADO."DocumentoIdentidadCliente"
							 ,RESULTADO."RazonSocialEmpresa"
							 ,RESULTADO."DocumentoIdentidadEmpresa"
							 ,RESULTADO."DireccionEmpresa"
							 ,RESULTADO."CiudadFecha"
							 ,RESULTADO."GiroActividad"
							 ,RESULTADO."FechaNumeroCuotas"
							 ,RESULTADO."FechaMontoActualizado"
							-----------------------------------------------------------
							--,RESULTADO."IdRESULTADOCertificadoClienteDJ1922N43"			
							,RESULTADO."IdCertificadoCliente"							
							,RESULTADO."FechaCertificado"								
							,RESULTADO."IdOperacion"									
							,RESULTADO."RutAdministradora"							
							,RESULTADO."IdEmisor"										
							,RESULTADO."RutFondo"										
							,RESULTADO."RunFondo"										
							,RESULTADO."Fondo"										
							,RESULTADO."TipoFondo"									
							,RESULTADO."RutCliente"									
							,RESULTADO."NombreParticipe"								
							,RESULTADO."TaxId"										
							,RESULTADO."CodigoPais"									
							,RESULTADO."TipoOperacion"							
							,RESULTADO."FechaInversion"
							,TO_VARCHAR(RESULTADO."FechaInversion", 'YYYY-MM-DD') AS "FechaInversionText"
							,RESULTADO."MonedaReajuste"								
							,RESULTADO."MontoNominalAportes"							
							,RESULTADO."MontoNominalAportesActualizado"				
							,RESULTADO."NumeroSerieCuotas"							
							,RESULTADO."CantidadCuotasParticipe"						
							,RESULTADO."ParticipeCumpleRequisitos"					
							,RESULTADO."CuotasCumplenRequisitos"						
							,RESULTADO."ValorCuotaPesos"								
							,RESULTADO."NumCuotasSerieParticipe"							
							,RESULTADO."PorcentajeParticipacion31Dic"						
							,RESULTADO."ValNominalCuotasRescatadas"						
							,RESULTADO."CantidadCuotasRescatadas"							
							,RESULTADO."DiferenciaPorRescateOEnajenacion"					
							,RESULTADO."DiferenciaPorRescateOEnajenacionActualizado"		
							,RESULTADO."DifPorRescateOEnajenacionCFIAcogido107"			
							,RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado"
							,CASE WHEN RESULTADO."DiferenciaPorRescateOEnajenacionActualizado" < 0 THEN 0 ELSE RESULTADO."DiferenciaPorRescateOEnajenacionActualizado" end AS "MAYORVALOR_FM"
							,CASE WHEN RESULTADO."DiferenciaPorRescateOEnajenacionActualizado" > 0 THEN 0 ELSE RESULTADO."DiferenciaPorRescateOEnajenacionActualizado" *-1 end AS "MENORVALOR_FM"
							,CASE WHEN RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado" < 0 THEN 0 ELSE RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado" end AS "MAYORVALOR_I"
							,CASE WHEN RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado" > 0 THEN 0 ELSE RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado" *-1 end AS "MENORVALOR_I"
							,RESULTADO."ValNominalInversionaFechaLiquidacion"				
							,RESULTADO."CantCuotasCedidasPorLiquidacion"					
							,RESULTADO."DiferenciaValorCuotasFechaLiquidacion"			
							,RESULTADO."DiferenciaValorCuotasFechaLiquidacionActualizado"	
							,RESULTADO."TributaSa"										
							,RESULTADO."DivRemDistAfectGCConIDPC"  						
							,RESULTADO."DivRemDistAfectGCConIDPCal31122016"  				
							,RESULTADO."DivRemDistAfectGCConIDPCVOL"  					
							,RESULTADO."DivRemDistAfectGCSINDC" 							
							,RESULTADO."DisRemRentasTribCProvRap_C96"  					
							,RESULTADO."DisRemRentasTribCORP_C97"  						
							,RESULTADO."DisRemRentasTribCExDD_C98"  						
							,RESULTADO."DisRemRentasTribCUtAFFut_C99"  					
							,RESULTADO."DivRemDistNoRentaFUT"  							
							,RESULTADO."DisRemRentasExIGCAfecImptoAdi_C100"  				
							,RESULTADO."CredImpGloCompNSRSD"  							
							,RESULTADO."CredImpGloCompNSRCD"  							
							,RESULTADO."CredIGCAcumD01012017RANSRD01012020SDD_C101"  		
							,RESULTADO."CredIGCAcumD01012017RANSRD01012020CDD_C102"  		
							,RESULTADO."CredImpGloCompSRSD"  								
							,RESULTADO."CredImpGloCompSRCD"  								
							,RESULTADO."CredIGCAcumD01012017RXSRSDD_C103"  				
							,RESULTADO."CredIGCAcumD01012017RXSRCDD_C104"  				
							,RESULTADO."CredImpGloCompCTDCIF"  							
							,RESULTADO."CredImpGloCompCDD31122016"  						
							,RESULTADO."CredImpGloCompSDD31122016"  						
							,RESULTADO."CredIGCAcumH31122016RXSDD_C105"  					
							,RESULTADO."CredIGCAcumH31122016RXCDD_C106"  					
							,RESULTADO."CredImpGloCompCTDCIF31122016"  					
							,RESULTADO."CredImpGloCompCITAExArt21"  						
							,RESULTADO."DetBenNetoPerRutFdo_C107" 						
							,RESULTADO."DetBenNetoPerPOS_C113"  							
							,RESULTADO."DetBenNetoPerNEG_C114"  							
							,RESULTADO."DividendosExentosIGCoAdicional"					
							,RESULTADO."DividendosExentosIGCoAdicionalActualizado"		
							,RESULTADO."DividendosNoConstitutivosDeRenta"					
							,RESULTADO."DividendosNoConstitutivosDeRentaActualizado"		
							,RESULTADO."DivRemDistNoRentaFE" 								
							,RESULTADO."DividendosAfectosImptoUnicoTasa10"				
							,RESULTADO."DividendosAfectosImptoUnicoTasa10Actualizado"		
							,RESULTADO."DividendosAfectosImptoUnicoTasa4"					
							,RESULTADO."DividendosAfectosImptoUnicoTasa4Actualizado"		
							,RESULTADO."DistribucionXDiminucionNoImputadaCapital"			
							,RESULTADO."DevolucionesDeCapital"							
							,RESULTADO."DevolucionesDeCapitalActualizado"					
							,RESULTADO."MontoInversionenCuotasCFIAnterior93"				
							,RESULTADO."NumeroCertificado"								
							,(COALESCE(IPCACTR."Valor",1)/COALESCE(IPCINVR."Valor",1)) as "FactorActualizacion"								
							,RESULTADO."FechaOrigenInversion"								
						 	,RESULTADO."RunNemotecnico"
							 -----------------------------------------------------------------
							 , SUMA."SumaNumeroCuotas"
							 , SUMA."SumaMontoActualizado"
							 , COALESCE(CP."Certificacion",'N') AS "Certificado"
							 , COALESCE(CP."EstadoCertificado",'N') AS "EstadoCertificado"
							 , COALESCE(rzs."RazonSocial", fm."NombreSerieFondo") AS "NombreFondo"						 
						FROM :v_Resultado AS RESULTADO
						INNER JOIN :v_sumas SUMA ON (SUMA."IdCertificadoCliente" = RESULTADO."IdCertificadoCliente")
						LEFT  JOIN "BSCL.Normativa.BD::TRI.ControlProceso" CP ON (CP."Esquema" = 'DJ1922' AND CP."Version" = :p_Version)
	                        LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINVR  ON (substring(ADD_MONTHS (TO_DATE (COALESCE(RESULTADO."FechaInversion",'1900-01-01'), 'YYYY-MM-DD'), -1),0,7) = IPCINVR."AgnoMes")  
						AND IPCINVR."IdParametroFinanciero" = 'IPC'
 									LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACTR  ON (SUBSTRING(COALESCE(RESULTADO."FechaInversion",'1900-01-01'),0,4) || '-11' = IPCACTR."AgnoMes")  		
						AND IPCACTR."IdParametroFinanciero" = 'IPC'	
						LEFT OUTER JOIN (
						SELECT "RazonSocial","Nemotecnico"  ,RANK() OVER(PARTITION BY "Nemotecnico" ORDER BY "IdInstrumento" ) AS "Rank"
						FROM   "BSCL.Plataforma.BD::INS.Instrumento"
						) AS rzs   on ( rzs."Rank"=1 AND rzs."Nemotecnico" = RESULTADO."Fondo")
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" fm ON (fm."IdSerieFondo" = RESULTADO."Fondo")						
						ORDER BY "Folio" ASC;
			
			SELECT * FROM :v_Resultado1
            CROSS JOIN :v_logo
            CROSS JOIN :v_firma
            CROSS JOIN :v_datos_corredor
            ORDER BY "Folio" ASC;
           
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1922.GenerarDocumentoCertificado43','FIN','FIN');
			
		END;
	END;								
END;