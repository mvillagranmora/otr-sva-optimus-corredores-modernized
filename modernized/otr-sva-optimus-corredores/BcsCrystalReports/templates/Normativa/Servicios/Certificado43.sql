SET SCHEMA "BSCL";
--GO 
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Certificado43";
--GO
CREATE PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Certificado43" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
    IN p_Version								NVARCHAR(10) ,
	IN p_Rut           							TABLE ( "Rut" NVARCHAR(40) ),
	
	OUT p_Salida   TABLE (
        "Folio"							                        BIGINT,	
        "IdEmpresa"						                        BIGINT,	
        "Esquema"						                        NVARCHAR(10),
        "Version"						                        NVARCHAR(10),
        "Anio"							                        NVARCHAR(4),
        "Periodo"						                        NVARCHAR(20),
        ---------------------------------------------------------------------
        "IdCliente"					                            BIGINT,
        "IdPortafolio"					                        BIGINT,	  
        "NombreCompletoEmisor"		                            NVARCHAR(200),	  
        "RutEmisor"				                                NVARCHAR(20),	  
        "TipoOperacionCertificado"					            NVARCHAR(14),
        "NombreCompletoCliente"					                NVARCHAR(200),	
        "DocumentoIdentidadCliente"			                    NVARCHAR(200),	
        "RazonSocialEmpresa"			                        NVARCHAR(200),	
        "DocumentoIdentidadEmpresa"				                NVARCHAR(200),
        "DireccionEmpresa"				                        NVARCHAR(200),
        "CiudadFecha"						                    NVARCHAR(5000),
        "GiroActividad"					                        NVARCHAR(500),
        "FechaNumeroCuotas"			                            NVARCHAR(40),	
        "FechaMontoActualizado"		                            NVARCHAR(40),	
        "IdCertificadoCliente"			                        BIGINT,
        "FechaCertificado"		                                NVARCHAR(40),
        "IdOperacion"		                                    BIGINT,
        "RutAdministradora"				                        NVARCHAR(40),
        "IdEmisor"					                            NVARCHAR(40),
        "RutFondo"			                                    NVARCHAR(40),
        "RunFondo"					                            NVARCHAR(40),
        "Fondo"				                                    NVARCHAR(100),
        "TipoFondo"					                            NVARCHAR(100),	
        "RutCliente"                                            NVARCHAR(40),
        "NombreParticipe"                                       NVARCHAR(250),
        "TaxId"                                                 NVARCHAR(250),	
        "CodigoPais"                                            NVARCHAR(100),
        "TipoOperacion"                                         NVARCHAR(100),
        "FechaInversion"										DATE,        
        "FechaInversionText"                                    NVARCHAR(10),
        "MonedaReajuste"                                        NVARCHAR(100),
        "MontoNominalAportes"                                   DECIMAL(26,8),
        "MontoNominalAportesActualizado"                        DECIMAL(26,8),
        "NumeroSerieCuotas"                                     BIGINT,
        "CantidadCuotasParticipe"                               DECIMAL(26,8),
        "ParticipeCumpleRequisitos"                             BIGINT,
        "CuotasCumplenRequisitos"                               BIGINT,
        "ValorCuotaPesos"                                       DECIMAL(26,8),
        "NumCuotasSerieParticipe"                               DECIMAL(26,8),
        "PorcentajeParticipacion31Dic"                          DECIMAL(26,8),
        "ValNominalCuotasRescatadas"                            DECIMAL(26,8),
        "CantidadCuotasRescatadas"                              DECIMAL(26,8),
        "DiferenciaPorRescateOEnajenacion"                      DECIMAL(26,8),
        "DiferenciaPorRescateOEnajenacionActualizado"           DECIMAL(26,8),
        "DifPorRescateOEnajenacionCFIAcogido107"                DECIMAL(26,8),
        "DifPorRescateOEnajenacionCFIAcogido107Actualizado"     DECIMAL(26,8),
        "MAYORVALOR_FM"   									    DECIMAL(26,8),
		"MENORVALOR_FM"   									    DECIMAL(26,8),
		"MAYORVALOR_I"   									    DECIMAL(26,8),	
        "MENORVALOR_I"   									    DECIMAL(26,8),
        "ValNominalInversionaFechaLiquidacion"                  DECIMAL(26,8),
        "CantCuotasCedidasPorLiquidacion"                       DECIMAL(26,8),
        "DiferenciaValorCuotasFechaLiquidacion"                 DECIMAL(26,8),
        "DiferenciaValorCuotasFechaLiquidacionActualizado"      DECIMAL(26,8),
        "TributaSa"                                             NVARCHAR(2),
        "DivRemDistAfectGCConIDPC"                              DECIMAL(26,8),
        "DivRemDistAfectGCConIDPCal31122016"                    DECIMAL(26,8),
        "DivRemDistAfectGCConIDPCVOL"                           DECIMAL(26,8),
        "DivRemDistAfectGCSINDC"                                DECIMAL(26,8),
        "DisRemRentasTribCProvRap_C96"                          DECIMAL(26,8),
        "DisRemRentasTribCORP_C97"                              DECIMAL(26,8),
        "DisRemRentasTribCExDD_C98"                             DECIMAL(26,8),
        "DisRemRentasTribCUtAFFut_C99"                          DECIMAL(26,8),
        "DivRemDistNoRentaFUT"                                  DECIMAL(26,8),
        "DisRemRentasExIGCAfecImptoAdi_C100"                    DECIMAL(26,8),
        "CredImpGloCompNSRSD"                                   DECIMAL(26,8),
        "CredImpGloCompNSRCD"                                   DECIMAL(26,8),
        "CredIGCAcumD01012017RANSRD01012020SDD_C101"            DECIMAL(26,8),
        "CredIGCAcumD01012017RANSRD01012020CDD_C102"            DECIMAL(26,8),
        "CredImpGloCompSRSD"                                    DECIMAL(26,8),
        "CredImpGloCompSRCD"                                    DECIMAL(26,8),
        "CredIGCAcumD01012017RXSRSDD_C103"                      DECIMAL(26,8),
        "CredIGCAcumD01012017RXSRCDD_C104"                      DECIMAL(26,8),
        "CredImpGloCompCTDCIF"                                  DECIMAL(26,8),
        "CredImpGloCompCDD31122016"                             DECIMAL(26,8),
        "CredImpGloCompSDD31122016"                             DECIMAL(26,8),
        "CredIGCAcumH31122016RXSDD_C105"                        DECIMAL(26,8),
        "CredIGCAcumH31122016RXCDD_C106"                        DECIMAL(26,8),
        "CredImpGloCompCTDCIF31122016"                          DECIMAL(26,8),
        "CredImpGloCompCITAExArt21"                             DECIMAL(26,8),
        "DetBenNetoPerRutFdo_C107"                              NVARCHAR(40),
        "DetBenNetoPerPOS_C113"                                 DECIMAL(26,8),
        "DetBenNetoPerNEG_C114"                                 DECIMAL(26,8),
        "DividendosExentosIGCoAdicional"                        DECIMAL(26,8),    
        "DividendosExentosIGCoAdicionalActualizado"             DECIMAL(26,8),
        "DividendosNoConstitutivosDeRenta"                      DECIMAL(26,8),
        "DividendosNoConstitutivosDeRentaActualizado"           DECIMAL(26,8),
        "DivRemDistNoRentaFE"                                   DECIMAL(26,8),
        "DividendosAfectosImptoUnicoTasa10"                     DECIMAL(26,8),
        "DividendosAfectosImptoUnicoTasa10Actualizado"          DECIMAL(26,8),
        "DividendosAfectosImptoUnicoTasa4"                      DECIMAL(26,8),
        "DividendosAfectosImptoUnicoTasa4Actualizado"           DECIMAL(26,8),
        "DistribucionXDiminucionNoImputadaCapital"              DECIMAL(26,8),
        "DevolucionesDeCapital"                                 DECIMAL(26,8),
        "DevolucionesDeCapitalActualizado"                      DECIMAL(26,8),
        "MontoInversionenCuotasCFIAnterior93"                   DECIMAL(26,8),
        "NumeroCertificado"                                     BIGINT,
        "FactorActualizacion"                                   DECIMAL(26,8),
        "FechaOrigenInversion"                                  NVARCHAR(40),
        "SumaNumeroCuotas"                                      DECIMAL(26,8),
        "SumaMontoActualizado"                                  DECIMAL(26,8),
        "Certificado"                                           NVARCHAR(1),
        "EstadoCertificado"                                     NVARCHAR(3),
        "NombreFondo"                                           NVARCHAR(100),
        -----------------------------------------	
        "RazonSocialCorredora"          NVARCHAR(100),
        "DireccionCorredora"            NVARCHAR(300),
        "DocumentoIdentidadCorredora"   NVARCHAR(10),
        "rut_representante"				NVARCHAR(10),
        "nombre_representante"			NVARCHAR(100),
        -------------------------------------------
        "RutCertificado"				NVARCHAR(40),
        
        "LogoCorredora"                 BLOB,
        "FirmaCorredora"                BLOB,
        "RunNemotecnico"                NVARCHAR(21)
	)
	
)

	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
		DECLARE v_Logo  TABLE("LogoCorredora" BLOB);
		DECLARE v_Firma TABLE("FirmaCorredora" BLOB);
		DECLARE v_count BIGINT:=0;DECLARE v_FechaSistema  NVARCHAR(10);
        DECLARE v_DefineFechaCertificado NVARCHAR(10);
	    DECLARE v_FechaCertificado NVARCHAR(10);

		v_Logo =
			SELECT TOP 1 "Binario" AS "LogoCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
		v_Firma =
			SELECT TOP 1 "Binario" AS "FirmaCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'FIRMA_CORREDORA' ORDER BY "Version" DESC;
	
		v_DatosCorredor = 
			SELECT 
			  PER."RazonSocial" 
			 ,EMP."IdEmpresa" 
			 ,CONCAT(DIR."Calle" ,CONCAT(' ',CONCAT(DIR."Numero",CONCAT(' , ',CIU."NombreCiudad")))) AS "Direccion"
			 ,DOC."DocumentoIdentidad" 
			FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
			INNER JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = SOC."IdPersona" 
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIR ON DIR."IdPersona" = PER."IdPersona" AND DIR."Predeterminado" ='S'
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = PER."IdPersona" AND DOC."Predeterminado" ='S'
			INNER JOIN  "BSCL.Plataforma.BD::REG.Ciudad" AS CIU ON CIU."IdCiudad" = DIR."IdCiudad" 
			WHERE EMP."IdEmpresa" = :p_IdEmpresa;

			---zona de filtros
													
		v_cerificados = 	
		SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo"
			 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
			 INNER JOIN :p_Rut AS RUT ON RUT."Rut" = CER."RutCliente"
			 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT43'
			UNION ALL 
			SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo"
					 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
					 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT43'
					 AND EXISTS 
					 ( 
					 
					   SELECT 1 FROM 
						(SELECT COUNT(1) AS "CANT" FROM :p_Rut WHERE LENGTH(LTRIM(RTRIM("Rut")) )>0 ) AS C WHERE C."CANT"=0
			
					  );
										
		v_datos_representante=
            SELECT "DocumentoIdentidadRepresentante" AS "rut_representante", PERSONA."NombreCompleto" AS "nombre_representante" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante" AND EMPRESA."IdEmpresa"=:p_IdEmpresa);
		
			
		--zona especifica ceritificado
		
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

        v_Resultado =
        SELECT
            CERTIFICADO."Folio"
            ,CERTIFICADO."IdEmpresa"				
            ,CERTIFICADO."Esquema"				
            ,CERTIFICADO."Version"				
            ,CERTIFICADO."Anio"					
            ,CERTIFICADO."Periodo"				
            ,CLIENTE."IdCliente"				
            ,0 as "IdPortafolio"
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
            ,TO_VARCHAR(DETALLE."FechaInversion", 'YYYY-MM-DD') AS "FechaInversionText"
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
        FROM :v_cerificados CERTIFICADO
        LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (TRIM(CLIENTE."DocumentoIdentidad") = TRIM(CERTIFICADO."RutCliente"))
        INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" DETALLE	ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")
        INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA ON (EMPRESA."IdEmpresa" = CERTIFICADO."IdEmpresa")
        INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."IdPersona" = EMPRESA."IdPersona")
        LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMISOR ON (EMISOR."IdEmisor" = DETALLE."IdEmisor")
        LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoTributario" FONDO ON (DETALLE."Fondo" = FONDO."IdFondo")
        LEFT OUTER JOIN  "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro" GIRO ON (GIRO."IdGiro" = PERSONA."IdGiroPrincipal")
        WHERE CERTIFICADO."Version" = p_Version
        ORDER BY CERTIFICADO."Folio";

        v_sumas = SELECT SUM(CASE WHEN "FechaOrigenInversion" < "FechaNumeroCuotas" THEN "CantidadCuotasParticipe"  ELSE 0 END) AS "SumaNumeroCuotas", SUM("MontoNominalAportesActualizado") AS "SumaMontoActualizado","IdCertificadoCliente"
                FROM :v_Resultado CERTIFICADO
                GROUP BY "IdCertificadoCliente"; 

        v_DatosDetalle =
        SELECT 		
            RESULTADO."Folio"
            ,RESULTADO."IdEmpresa"				
            ,RESULTADO."Esquema"				
            ,RESULTADO."Version"				
            ,RESULTADO."Anio"					
            ,RESULTADO."Periodo"	
            ----
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
			,CASE WHEN :p_Version <= 2025 THEN
					CASE "TipoFondo" WHEN 'F' THEN  
						CASE WHEN RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado" < 0 THEN 0 ELSE RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado" end
					ELSE 0 END
		    ELSE --version >2025
		            CASE WHEN RESULTADO."DiferenciaPorRescateOEnajenacionActualizado" < 0 THEN 0 ELSE RESULTADO."DiferenciaPorRescateOEnajenacionActualizado" end
		    END AS "MAYORVALOR_FM"
			
		    ,CASE WHEN :p_Version <= 2025 THEN
		            CASE "TipoFondo" WHEN 'F' THEN  
		                CASE WHEN RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado" > 0 THEN 0 ELSE RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado" * -1 END
		            ELSE 0 END
		    ELSE --version >2025
		            CASE WHEN RESULTADO."DiferenciaPorRescateOEnajenacionActualizado" > 0 THEN 0 ELSE RESULTADO."DiferenciaPorRescateOEnajenacionActualizado" * -1 END
		    END AS "MENORVALOR_FM"
		
		    ,CASE WHEN :p_Version <= 2025 THEN
		            CASE "TipoFondo" WHEN 'I' THEN
		                CASE WHEN RESULTADO."DiferenciaPorRescateOEnajenacionActualizado" < 0  THEN 0 ELSE RESULTADO."DiferenciaPorRescateOEnajenacionActualizado"  END
		            ELSE 0 END
		    ELSE --version >2025
		            CASE WHEN RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado" < 0 THEN 0 ELSE RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado"  END
		    END AS "MAYORVALOR_I"
			
		    ,CASE WHEN :p_Version <= 2025 THEN
		            CASE "TipoFondo" WHEN 'I' THEN
		                CASE WHEN RESULTADO."DiferenciaPorRescateOEnajenacionActualizado" > 0 THEN 0 ELSE RESULTADO."DiferenciaPorRescateOEnajenacionActualizado" * -1 END
		            ELSE 0 END
		    ELSE --version >2025
		            CASE WHEN RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado" > 0 THEN 0 ELSE RESULTADO."DifPorRescateOEnajenacionCFIAcogido107Actualizado" * -1 END
		    END AS "MENORVALOR_I"
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
            , SUMA."SumaNumeroCuotas"
            , SUMA."SumaMontoActualizado"
            , COALESCE(CP."Certificacion",'N') AS "Certificado"
            , COALESCE(CP."EstadoCertificado",'N') AS "EstadoCertificado"
            ,COALESCE(rzs."RazonSocial", fm."NombreSerieFondo") AS "NombreFondo"
            ,RESULTADO."RunNemotecnico"
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
						
        -- Zona Comun
		p_Salida = SELECT	 
				 "Detalle"."Folio"
				,"Detalle"."IdEmpresa"
				,"Detalle"."Esquema"
				,"Detalle"."Version"
				,"Detalle"."Anio"
				,"Detalle"."Periodo"
                ---
				,"Detalle"."IdCliente"
				,"Detalle"."IdPortafolio"
				,"Detalle"."NombreCompletoEmisor"
				,"Detalle"."RutEmisor"
				,"Detalle"."TipoOperacionCertificado"
				,"Detalle"."NombreCompletoCliente"
				,"Detalle"."DocumentoIdentidadCliente"
				,"Detalle"."RazonSocialEmpresa"
				,"Detalle"."DocumentoIdentidadEmpresa"
                ,"Detalle"."DireccionEmpresa"
                ,"Detalle"."CiudadFecha"
                ,"Detalle"."GiroActividad"
                ,"Detalle"."FechaNumeroCuotas"
                ,"Detalle"."FechaMontoActualizado"
				,"Detalle"."IdCertificadoCliente"
				,"Detalle"."FechaCertificado"
				,"Detalle"."IdOperacion"
				,"Detalle"."RutAdministradora"
				,"Detalle"."IdEmisor"
				,"Detalle"."RutFondo"
				,"Detalle"."RunFondo"
				,"Detalle"."Fondo"
				,"Detalle"."TipoFondo"
				,"Detalle"."RutCliente"
				,"Detalle"."NombreParticipe"
				,"Detalle"."TaxId"
                ,"Detalle"."CodigoPais"
                ,"Detalle"."TipoOperacion"
                ,TO_DATE ("Detalle"."FechaInversion", 'YYYY-MM-DD') AS "FechaInversion"                
                ,TO_VARCHAR("Detalle"."FechaInversion", 'YYYY-MM-DD') AS "FechaInversionText"
                ,"Detalle"."MonedaReajuste"
                ,"Detalle"."MontoNominalAportes"
                ,"Detalle"."MontoNominalAportesActualizado"
                ,"Detalle"."NumeroSerieCuotas"
                ,"Detalle"."CantidadCuotasParticipe"
                ,"Detalle"."ParticipeCumpleRequisitos"
                ,"Detalle"."CuotasCumplenRequisitos"
                ,"Detalle"."ValorCuotaPesos"
                ,"Detalle"."NumCuotasSerieParticipe"
                ,"Detalle"."PorcentajeParticipacion31Dic"
                ,"Detalle"."ValNominalCuotasRescatadas"
                ,"Detalle"."CantidadCuotasRescatadas"
                ,"Detalle"."DiferenciaPorRescateOEnajenacion"
                ,"Detalle"."DiferenciaPorRescateOEnajenacionActualizado"
                ,"Detalle"."DifPorRescateOEnajenacionCFIAcogido107"
                ,"Detalle"."DifPorRescateOEnajenacionCFIAcogido107Actualizado"
				,"Detalle"."MAYORVALOR_FM"	
				,"Detalle"."MENORVALOR_FM"
				,"Detalle"."MAYORVALOR_I"	
                ,"Detalle"."MENORVALOR_I"
                ,"Detalle"."ValNominalInversionaFechaLiquidacion"
                ,"Detalle"."CantCuotasCedidasPorLiquidacion"
                ,"Detalle"."DiferenciaValorCuotasFechaLiquidacion"
                ,"Detalle"."DiferenciaValorCuotasFechaLiquidacionActualizado"
                ,"Detalle"."TributaSa"
                ,"Detalle"."DivRemDistAfectGCConIDPC"
                ,"Detalle"."DivRemDistAfectGCConIDPCal31122016"
                ,"Detalle"."DivRemDistAfectGCConIDPCVOL"
                ,"Detalle"."DivRemDistAfectGCSINDC"
                ,"Detalle"."DisRemRentasTribCProvRap_C96"
                ,"Detalle"."DisRemRentasTribCORP_C97"
                ,"Detalle"."DisRemRentasTribCExDD_C98"
                ,"Detalle"."DisRemRentasTribCUtAFFut_C99"
                ,"Detalle"."DivRemDistNoRentaFUT" 
                ,"Detalle"."DisRemRentasExIGCAfecImptoAdi_C100"
                ,"Detalle"."CredImpGloCompNSRSD"
                ,"Detalle"."CredImpGloCompNSRCD"
                ,"Detalle"."CredIGCAcumD01012017RANSRD01012020SDD_C101"
                ,"Detalle"."CredIGCAcumD01012017RANSRD01012020CDD_C102"
                ,"Detalle"."CredImpGloCompSRSD"
                ,"Detalle"."CredImpGloCompSRCD"
                ,"Detalle"."CredIGCAcumD01012017RXSRSDD_C103"
                ,"Detalle"."CredIGCAcumD01012017RXSRCDD_C104"
                ,"Detalle"."CredImpGloCompCTDCIF"
                ,"Detalle"."CredImpGloCompCDD31122016"
                ,"Detalle"."CredImpGloCompSDD31122016"
                ,"Detalle"."CredIGCAcumH31122016RXSDD_C105"
                ,"Detalle"."CredIGCAcumH31122016RXCDD_C106"
                ,"Detalle"."CredImpGloCompCTDCIF31122016"
                ,"Detalle"."CredImpGloCompCITAExArt21"
                ,"Detalle"."DetBenNetoPerRutFdo_C107"
                ,"Detalle"."DetBenNetoPerPOS_C113"
                ,"Detalle"."DetBenNetoPerNEG_C114"
                ,"Detalle"."DividendosExentosIGCoAdicional"           
                ,"Detalle"."DividendosExentosIGCoAdicionalActualizado"
                ,"Detalle"."DividendosNoConstitutivosDeRenta"
                ,"Detalle"."DividendosNoConstitutivosDeRentaActualizado"
                ,"Detalle"."DivRemDistNoRentaFE"
                ,"Detalle"."DividendosAfectosImptoUnicoTasa10"
                ,"Detalle"."DividendosAfectosImptoUnicoTasa10Actualizado"
                ,"Detalle"."DividendosAfectosImptoUnicoTasa4"
                ,"Detalle"."DividendosAfectosImptoUnicoTasa4Actualizado"
                ,"Detalle"."DistribucionXDiminucionNoImputadaCapital"
                ,"Detalle"."DevolucionesDeCapital"
                ,"Detalle"."DevolucionesDeCapitalActualizado"
                ,"Detalle"."MontoInversionenCuotasCFIAnterior93"
                ,"Detalle"."NumeroCertificado"
                ,"Detalle"."FactorActualizacion"
                ,"Detalle"."FechaOrigenInversion"
                ,"Detalle"."SumaNumeroCuotas"
                ,"Detalle"."SumaMontoActualizado"
                ,"Detalle"."Certificado"
                ,"Detalle"."EstadoCertificado"
                ,"Detalle"."NombreFondo"
				-------------------------
				,"Corredor"."RazonSocial" AS "RazonSocialCorredora"
				,"Corredor"."Direccion" AS "DireccionCorredora"
				,"Corredor"."DocumentoIdentidad" AS "DocumentoIdentidadCorredora"
				,"Rep"."rut_representante"
				,"Rep"."nombre_representante"
				,"Detalle"."DocumentoIdentidadCliente" AS "RutCertificado"
				,"Logo"."LogoCorredora"
				,"Firma"."FirmaCorredora"
                ,"Detalle"."RunNemotecnico"
					FROM :v_DatosDetalle AS "Detalle"
					LEFT JOIN :v_DatosCorredor AS "Corredor" ON "Corredor"."IdEmpresa"=:p_IdEmpresa
			        LEFT JOIN :v_Logo AS "Logo" ON "Logo"."LogoCorredora" IS NOT NULL
					LEFT JOIN :v_Firma AS "Firma" ON "Firma"."FirmaCorredora" IS NOT NULL
                    LEFT JOIN :v_datos_representante AS "Rep" ON "Rep"."rut_representante" IS NOT NULL
				ORDER BY "Detalle"."Folio";
					 
END;
