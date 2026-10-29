SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1922.GenerarCertificado43";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1922.GenerarCertificado43"(
	-- ID empresa del login.
    IN p_IdEmpresa 	BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 	BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo	NVARCHAR(3)	 ,
    
    IN p_Version	NVARCHAR(10) ,
    
    IN p_Todos		NVARCHAR(1) DEFAULT 'S',
    
    IN p_Clientes	TABLE (
    					"IdCliente"	BIGINT
    				),
    
    OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
	LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
		BEGIN
			select count(1) into p_out_ErroresContador 
			from :v_out_ErroresDetalle;
			select count(1) into p_out_AdvertenciasContador 
			from :v_out_AdvertenciasDetalle;
			
			v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_ErroresDetalle
									UNION ALL
									SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_AdvertenciasDetalle;
									
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
			
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdInterno",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdInterno",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo"; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
			DECLARE v_resultadoRegla 	NVARCHAR(40);
			DECLARE v_FechaSistema		NVARCHAR(10);
			DECLARE v_count_folio		BIGINT;
			DECLARE v_count_cli		    BIGINT=0;
			DECLARE V_ultimo_folio		BIGINT;
			DECLARE v_max_folio			BIGINT;
			DECLARE v_count_certificado BIGINT;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1922.GenerarCertificado43','INICIO','INICIO');
				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;

			/*rut cliente*/
       		SELECT COUNT(1)  INTO v_count_cli  FROM :p_Clientes;      
       	     IF :p_Todos = 'N' AND v_count_cli>0 THEN
       	     	v_cliente = SELECT cl."DocumentoIdentidad" AS "RutCliente", cl."IdCliente"   FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS cl 
						               INNER JOIN :p_Clientes parcli ON (parcli."IdCliente" = cl."IdCliente");
       	     ELSE
       	     
       	    	 v_cliente = 
       	    	 SELECT DISTINCT DAT."RutCliente",max(COALESCE(cl."IdCliente",0)) AS "IdCliente"
       	    	 FROM
       	    	 (
       	    			 SELECT trim(UPPER(dj."RutCliente")) AS "RutCliente"  
       	    	 				FROM "BSCL.Normativa.BD::TRI.DatosDJ1922" AS dj
				                INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS d ON (d."IdDocumento" = dj."IdDocumento")
						        WHERE d."Version" = :p_Version  
						 UNION ALL
						 
						  SELECT trim(UPPER(dj."RutCliente")) AS "RutCliente"  
       	    	 				FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" AS dj
				                --INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS d ON (d."IdDocumento" = dj."IdDocumento")
						        WHERE YEAR(dj."FechaInversion") = :p_Version  
							        
				) DAT  
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS cl ON (trim(DAT."RutCliente") = trim(cl."DocumentoIdentidad"))
				GROUP BY DAT."RutCliente" ;	              
						              						              
       	     END IF; 

			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			SELECT COUNT(1) into v_count_folio
			FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			WHERE "Esquema" = 'DJ1922' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_43';
			
			IF v_count_folio = 0 THEN
				INSERT INTO "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
				("Esquema","Version","UltimoFolio","CodigoCertificado","NombreCertificado","CodigoPrograma")
				VALUES('DJ1922',:p_Version,0,'CERTIFICADO_43','Certificado 43','GenerarCertificado43');
			END IF;
			
			SELECT "UltimoFolio" into v_ultimo_folio
			FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			WHERE "Esquema" = 'DJ1922' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_43';
		
			/*EMISOR EN LA DJ*/
			UPDATE DJ SET DJ."IdEmisor" =INS."IdEmisor"             
			 FROM "BSCL.Normativa.BD::TRI.DatosDJ1922"   AS DJ
            INNER JOIN "BSCL.Normativa.BD::DOC.Documento" DOC ON (DOC."IdDocumento"=DJ."IdDocumento")
            INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins ON (ins."Nemotecnico"=DJ."Fondo")  
             WHERE
             DOC."Version" = :p_Version AND dj."IdEmisor" IS NULL ;  

		
			
			v_datos = SELECT CLIENTES."IdCliente"
							 ,DJ1922."IdPortafolio"
							  ,:v_FechaSistema AS "FechaCertificado"
							  ------------------------------------------
								,DJ1922."IdOperacion"		
								,DJ1922."RutAdministradora"
								,DJ1922."IdEmisor"			
								,DJ1922."RutFondo"			
								,DJ1922."RunFondo"	
								,DJ1922."Fondo"	
								,DJ1922."TipoFondo"	
								,TRIM(UPPER(DJ1922."RutCliente")) AS "RutCliente"
								,DJ1922."NombreParticipe"
								,DJ1922."TaxId"			
								,DJ1922."CodigoPais"		
								,DJ1922."TipoOperacion"	
								,DJ1922."FechaInversion"	
								,DJ1922."MonedaReajuste"	
								,DJ1922."MontoNominalAportes"
								,DJ1922."MontoNominalAportesActualizado"	
								,DJ1922."NumeroSerieCuotas"				
								,DJ1922."CantidadCuotasParticipe"			
								,DJ1922."ParticipeCumpleRequisitos"		
								,DJ1922."CuotasCumplenRequisitos"			
								,DJ1922."ValorCuotaPesos"					
								,DJ1922."NumCuotasSerieParticipe"			
								,DJ1922."PorcentajeParticipacion31Dic"		
								,DJ1922."ValNominalCuotasRescatadas"		
								,DJ1922."CantidadCuotasRescatadas"			
								,DJ1922."DiferenciaPorRescateOEnajenacion"	
								,DJ1922."DiferenciaPorRescateOEnajenacionActualizado"		
								,DJ1922."DifPorRescateOEnajenacionCFIAcogido107"			
								,DJ1922."DifPorRescateOEnajenacionCFIAcogido107Actualizado"
								,DJ1922."ValNominalInversionaFechaLiquidacion"				
								,DJ1922."CantCuotasCedidasPorLiquidacion"					
								,DJ1922."DiferenciaValorCuotasFechaLiquidacion"			
								,DJ1922."DiferenciaValorCuotasFechaLiquidacionActualizado"	
								,DJ1922."TributaSa"										
								,DJ1922."DivRemDistAfectGCConIDPC"  						
								,DJ1922."DivRemDistAfectGCConIDPCal31122016"  				
								,DJ1922."DivRemDistAfectGCConIDPCVOL"  					
								,DJ1922."DivRemDistAfectGCSINDC" 							
								,DJ1922."DisRemRentasTribCProvRap_C96"  				
								,DJ1922."DisRemRentasTribCORP_C97"  					
								,DJ1922."DisRemRentasTribCExDD_C98"  					
								,DJ1922."DisRemRentasTribCUtAFFut_C99"  				
								,DJ1922."DivRemDistNoRentaFUT"  						
								,DJ1922."DisRemRentasExIGCAfecImptoAdi_C100"  			
								,DJ1922."CredImpGloCompNSRSD"  						
								,DJ1922."CredImpGloCompNSRCD"  						
								,DJ1922."CredIGCAcumD01012017RANSRD01012020SDD_C101"  	
								,DJ1922."CredIGCAcumD01012017RANSRD01012020CDD_C102"  	
								,DJ1922."CredImpGloCompSRSD"  							
								,DJ1922."CredImpGloCompSRCD"  							
								,DJ1922."CredIGCAcumD01012017RXSRSDD_C103"  			
								,DJ1922."CredIGCAcumD01012017RXSRCDD_C104"  			
								,DJ1922."CredImpGloCompCTDCIF"  						
								,DJ1922."CredImpGloCompCDD31122016"  					
								,DJ1922."CredImpGloCompSDD31122016"  					
								,DJ1922."CredIGCAcumH31122016RXSDD_C105"  				
								,DJ1922."CredIGCAcumH31122016RXCDD_C106"  				
								,DJ1922."CredImpGloCompCTDCIF31122016"  				
								,DJ1922."CredImpGloCompCITAExArt21"  					
								,DJ1922."DetBenNetoPerRutFdo_C107" 					
								,DJ1922."DetBenNetoPerPOS_C113"  						
								,DJ1922."DetBenNetoPerNEG_C114"  						
								,DJ1922."DividendosExentosIGCoAdicional"				
								,DJ1922."DividendosExentosIGCoAdicionalActualizado"	
								,DJ1922."DividendosNoConstitutivosDeRenta"				
								,DJ1922."DividendosNoConstitutivosDeRentaActualizado"	
								,DJ1922."DivRemDistNoRentaFE" 							
								,DJ1922."DividendosAfectosImptoUnicoTasa10"			
								,DJ1922."DividendosAfectosImptoUnicoTasa10Actualizado"	
								,DJ1922."DividendosAfectosImptoUnicoTasa4"				
								,DJ1922."DividendosAfectosImptoUnicoTasa4Actualizado"	
								,DJ1922."DistribucionXDiminucionNoImputadaCapital"		
								,DJ1922."DevolucionesDeCapital"						
								,DJ1922."DevolucionesDeCapitalActualizado"				
								,DJ1922."MontoInversionenCuotasCFIAnterior93"			
								,DJ1922."FactorActualizacion"							
								,DJ1922."FechaOrigenInversion"
								,'0' AS "CodigoPortafolio"
							 ,DOCUMENTO."IdTipoDocumento"
							 ,:p_IdEmpresa AS "IdEmpresa"
							 ,:p_Version AS "Version"
							 ,DJ1922."RunNemotecnico"
					 FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1922.Datos" DJ1922
					 INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" DOCUMENTO ON (DJ1922."IdDocumento" = DOCUMENTO."IdDocumento")
					 LEFT OUTER JOIN :v_cliente CLIENTES ON (CLIENTES."RutCliente" = TRIM(UPPER(DJ1922."RutCliente"))) 
					WHERE DJ1922."TipoOperacion" IN (1,2,3,4,5,6)
					AND DOCUMENTO."Version" = :p_Version;
					-- AND DOCUMENTO."Version" = :p_Version AND (:p_Todos = 'S' OR (:p_Todos = 'N' AND COALESCE(CLIENTES."IdCliente",0) != 0))
					 
									
			v_datos_certificado = SELECT DISTINCT "RutCliente","CodigoPortafolio","Fondo","IdEmpresa","Version","IdTipoDocumento" FROM :v_datos;
									
			v_datos_sin_certificado = SELECT "BSCL.Normativa.BD.Secuencias::TRI.IdCertificadoCliente".NEXTVAL AS "IdCertificadoCliente"
											 ,:v_ultimo_folio + "Numero" AS "Folio"
											--,"IdCliente","IdPortafolio"
											 ,"RutCliente"
											 ,"CodigoPortafolio"
											 ,"IdTipoDocumento"
											 ,"IdEmpresa"
									   		 ,"Version"
									   		 ,"Fondo"
									  FROM (SELECT ROW_NUMBER() OVER(ORDER BY DATOS."CodigoPortafolio") AS "Numero"
									   		--,DATOS."IdCliente"
									   		--,DATOS."IdPortafolio"
											,DATOS."RutCliente"
											,DATOS."CodigoPortafolio"
											,DATOS."IdTipoDocumento"
											,DATOS."IdEmpresa"
									   		,DATOS."Version"
									   		,DATOS."Fondo"
											FROM :v_datos_certificado DATOS
											WHERE NOT EXISTS (
												SELECT 1 FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO  
												WHERE CERTIFICADO."RutCliente" = DATOS."RutCliente"
												AND CERTIFICADO."CodigoPortafolio" = DATOS."CodigoPortafolio"
												AND CERTIFICADO."Esquema" = 'DJ1922'
												AND CERTIFICADO."Version" = DATOS."Version"
												AND CERTIFICADO."IdEmpresa" = DATOS."IdEmpresa"
												AND CERTIFICADO."Nemo" = DATOS."Fondo"
											));
			
			
			v_detalle_sin_certificado = SELECT   "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1922N43".NEXTVAL AS "IdDetalleCertificadoClienteDJ1922N43"
												 ,"IdCertificadoCliente"
												 ,DATOS."FechaCertificado"
												 ,CERTIFICADO."Folio" AS "NumeroCertificado"	
												 ------------------------------
												 	,DATOS."IdOperacion"		
													,DATOS."RutAdministradora"
													,DATOS."IdEmisor"			
													,DATOS."RutFondo"			
													,DATOS."RunFondo"	
													,DATOS."Fondo"	
													,DATOS."TipoFondo"	
													,DATOS."RutCliente"	
													,DATOS."NombreParticipe"
													,DATOS."TaxId"			
													,DATOS."CodigoPais"		
													,DATOS."TipoOperacion"	
													,DATOS."FechaInversion"	
													,DATOS."MonedaReajuste"	
													,DATOS."MontoNominalAportes"
													,DATOS."MontoNominalAportesActualizado"	
													,DATOS."NumeroSerieCuotas"				
													,DATOS."CantidadCuotasParticipe"			
													,DATOS."ParticipeCumpleRequisitos"		
													,DATOS."CuotasCumplenRequisitos"			
													,DATOS."ValorCuotaPesos"					
													,DATOS."NumCuotasSerieParticipe"			
													,DATOS."PorcentajeParticipacion31Dic"		
													,DATOS."ValNominalCuotasRescatadas"		
													,DATOS."CantidadCuotasRescatadas"			
													,DATOS."DiferenciaPorRescateOEnajenacion"	
													,DATOS."DiferenciaPorRescateOEnajenacionActualizado"		
													,DATOS."DifPorRescateOEnajenacionCFIAcogido107"			
													,DATOS."DifPorRescateOEnajenacionCFIAcogido107Actualizado"
													,DATOS."ValNominalInversionaFechaLiquidacion"				
													,DATOS."CantCuotasCedidasPorLiquidacion"					
													,DATOS."DiferenciaValorCuotasFechaLiquidacion"			
													,DATOS."DiferenciaValorCuotasFechaLiquidacionActualizado"	
													,DATOS."TributaSa"										
													,DATOS."DivRemDistAfectGCConIDPC"  						
													,DATOS."DivRemDistAfectGCConIDPCal31122016"  				
													,DATOS."DivRemDistAfectGCConIDPCVOL"  					
													,DATOS."DivRemDistAfectGCSINDC" 							
													,DATOS."DisRemRentasTribCProvRap_C96"  				
													,DATOS."DisRemRentasTribCORP_C97"  					
													,DATOS."DisRemRentasTribCExDD_C98"  					
													,DATOS."DisRemRentasTribCUtAFFut_C99"  				
													,DATOS."DivRemDistNoRentaFUT"  						
													,DATOS."DisRemRentasExIGCAfecImptoAdi_C100"  			
													,DATOS."CredImpGloCompNSRSD"  						
													,DATOS."CredImpGloCompNSRCD"  						
													,DATOS."CredIGCAcumD01012017RANSRD01012020SDD_C101"  	
													,DATOS."CredIGCAcumD01012017RANSRD01012020CDD_C102"  	
													,DATOS."CredImpGloCompSRSD"  							
													,DATOS."CredImpGloCompSRCD"  							
													,DATOS."CredIGCAcumD01012017RXSRSDD_C103"  			
													,DATOS."CredIGCAcumD01012017RXSRCDD_C104"  			
													,DATOS."CredImpGloCompCTDCIF"  						
													,DATOS."CredImpGloCompCDD31122016"  					
													,DATOS."CredImpGloCompSDD31122016"  					
													,DATOS."CredIGCAcumH31122016RXSDD_C105"  				
													,DATOS."CredIGCAcumH31122016RXCDD_C106"  				
													,DATOS."CredImpGloCompCTDCIF31122016"  				
													,DATOS."CredImpGloCompCITAExArt21"  					
													,DATOS."DetBenNetoPerRutFdo_C107" 					
													,DATOS."DetBenNetoPerPOS_C113"  						
													,DATOS."DetBenNetoPerNEG_C114"  						
													,DATOS."DividendosExentosIGCoAdicional"				
													,DATOS."DividendosExentosIGCoAdicionalActualizado"	
													,DATOS."DividendosNoConstitutivosDeRenta"				
													,DATOS."DividendosNoConstitutivosDeRentaActualizado"	
													,DATOS."DivRemDistNoRentaFE" 							
													,DATOS."DividendosAfectosImptoUnicoTasa10"			
													,DATOS."DividendosAfectosImptoUnicoTasa10Actualizado"	
													,DATOS."DividendosAfectosImptoUnicoTasa4"				
													,DATOS."DividendosAfectosImptoUnicoTasa4Actualizado"	
													,DATOS."DistribucionXDiminucionNoImputadaCapital"		
													,DATOS."DevolucionesDeCapital"						
													,DATOS."DevolucionesDeCapitalActualizado"				
													,DATOS."MontoInversionenCuotasCFIAnterior93"			
													,DATOS."FactorActualizacion"							
													,DATOS."FechaOrigenInversion"
													,DATOS."RunNemotecnico"				
										FROM :v_datos DATOS
										INNER JOIN :v_datos_sin_certificado CERTIFICADO ON (DATOS."RutCliente" = CERTIFICADO."RutCliente"
																							AND DATOS."CodigoPortafolio" = CERTIFICADO."CodigoPortafolio"
																							AND DATOS."Fondo" = CERTIFICADO."Fondo"
																							AND DATOS."IdEmpresa" = CERTIFICADO."IdEmpresa"
																							AND DATOS."Version" = CERTIFICADO."Version"
																							AND DATOS."IdTipoDocumento" = CERTIFICADO."IdTipoDocumento"
																							);

			v_datos_con_certificado = SELECT CERTIFICADO."IdCertificadoCliente"
											 ,CERTIFICADO."Folio"
											 --,DATOS."IdCliente"
									   		 --,DATOS."IdPortafolio"
											 ,DATOS."RutCliente"
											 ,DATOS."CodigoPortafolio"
											 ,DATOS."IdTipoDocumento"
											 ,DATOS."IdEmpresa"
									   		 ,DATOS."Version"
											 ,DATOS."Fondo"
										FROM :v_datos_certificado DATOS
										INNER JOIN "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO ON (CERTIFICADO."RutCliente" = DATOS."RutCliente"
																												AND CERTIFICADO."CodigoPortafolio" = DATOS."CodigoPortafolio"
																												AND CERTIFICADO."Esquema" = 'DJ1922'
																												AND CERTIFICADO."Version" = DATOS."Version"
																												AND CERTIFICADO."IdEmpresa" = DATOS."IdEmpresa"
																												AND CERTIFICADO."IdTipoCertificado" = 'CERT43'
																												AND CERTIFICADO."Nemo" = DATOS."Fondo");
										 
										--WHERE EXISTS ( SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" DETALLE WHERE DETALLE."Fondo" = DATOS."Fondo" AND CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente");

			v_detalle_con_certificado = SELECT 	"BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1922N43".NEXTVAL AS "IdDetalleCertificadoClienteDJ1922N43"
												,CERTIFICADO."IdCertificadoCliente"
												,DATOS."FechaCertificado"
												,CERTIFICADO."Folio" AS "NumeroCertificado"	
												 ------------------------------
												 	,DATOS."IdOperacion"		
													,DATOS."RutAdministradora"
													,DATOS."IdEmisor"			
													,DATOS."RutFondo"			
													,DATOS."RunFondo"	
													,DATOS."Fondo"	
													,DATOS."TipoFondo"	
													,DATOS."RutCliente"	
													,DATOS."NombreParticipe"
													,DATOS."TaxId"			
													,DATOS."CodigoPais"		
													,DATOS."TipoOperacion"	
													,DATOS."FechaInversion"	
													,DATOS."MonedaReajuste"	
													,DATOS."MontoNominalAportes"
													,DATOS."MontoNominalAportesActualizado"	
													,DATOS."NumeroSerieCuotas"				
													,DATOS."CantidadCuotasParticipe"			
													,DATOS."ParticipeCumpleRequisitos"		
													,DATOS."CuotasCumplenRequisitos"			
													,DATOS."ValorCuotaPesos"					
													,DATOS."NumCuotasSerieParticipe"			
													,DATOS."PorcentajeParticipacion31Dic"		
													,DATOS."ValNominalCuotasRescatadas"		
													,DATOS."CantidadCuotasRescatadas"			
													,DATOS."DiferenciaPorRescateOEnajenacion"	
													,DATOS."DiferenciaPorRescateOEnajenacionActualizado"		
													,DATOS."DifPorRescateOEnajenacionCFIAcogido107"			
													,DATOS."DifPorRescateOEnajenacionCFIAcogido107Actualizado"
													,DATOS."ValNominalInversionaFechaLiquidacion"				
													,DATOS."CantCuotasCedidasPorLiquidacion"					
													,DATOS."DiferenciaValorCuotasFechaLiquidacion"			
													,DATOS."DiferenciaValorCuotasFechaLiquidacionActualizado"	
													,DATOS."TributaSa"										
													,DATOS."DivRemDistAfectGCConIDPC"  						
													,DATOS."DivRemDistAfectGCConIDPCal31122016"  				
													,DATOS."DivRemDistAfectGCConIDPCVOL"  					
													,DATOS."DivRemDistAfectGCSINDC" 							
													,DATOS."DisRemRentasTribCProvRap_C96"  				
													,DATOS."DisRemRentasTribCORP_C97"  					
													,DATOS."DisRemRentasTribCExDD_C98"  					
													,DATOS."DisRemRentasTribCUtAFFut_C99"  				
													,DATOS."DivRemDistNoRentaFUT"  						
													,DATOS."DisRemRentasExIGCAfecImptoAdi_C100"  			
													,DATOS."CredImpGloCompNSRSD"  						
													,DATOS."CredImpGloCompNSRCD"  						
													,DATOS."CredIGCAcumD01012017RANSRD01012020SDD_C101"  	
													,DATOS."CredIGCAcumD01012017RANSRD01012020CDD_C102"  	
													,DATOS."CredImpGloCompSRSD"  							
													,DATOS."CredImpGloCompSRCD"  							
													,DATOS."CredIGCAcumD01012017RXSRSDD_C103"  			
													,DATOS."CredIGCAcumD01012017RXSRCDD_C104"  			
													,DATOS."CredImpGloCompCTDCIF"  						
													,DATOS."CredImpGloCompCDD31122016"  					
													,DATOS."CredImpGloCompSDD31122016"  					
													,DATOS."CredIGCAcumH31122016RXSDD_C105"  				
													,DATOS."CredIGCAcumH31122016RXCDD_C106"  				
													,DATOS."CredImpGloCompCTDCIF31122016"  				
													,DATOS."CredImpGloCompCITAExArt21"  					
													,DATOS."DetBenNetoPerRutFdo_C107" 					
													,DATOS."DetBenNetoPerPOS_C113"  						
													,DATOS."DetBenNetoPerNEG_C114"  						
													,DATOS."DividendosExentosIGCoAdicional"				
													,DATOS."DividendosExentosIGCoAdicionalActualizado"	
													,DATOS."DividendosNoConstitutivosDeRenta"				
													,DATOS."DividendosNoConstitutivosDeRentaActualizado"	
													,DATOS."DivRemDistNoRentaFE" 							
													,DATOS."DividendosAfectosImptoUnicoTasa10"			
													,DATOS."DividendosAfectosImptoUnicoTasa10Actualizado"	
													,DATOS."DividendosAfectosImptoUnicoTasa4"				
													,DATOS."DividendosAfectosImptoUnicoTasa4Actualizado"	
													,DATOS."DistribucionXDiminucionNoImputadaCapital"		
													,DATOS."DevolucionesDeCapital"						
													,DATOS."DevolucionesDeCapitalActualizado"				
													,DATOS."MontoInversionenCuotasCFIAnterior93"			
													,DATOS."FactorActualizacion"							
													,DATOS."FechaOrigenInversion"	
													,DATOS."RunNemotecnico"						
										FROM :v_datos DATOS
										INNER JOIN :v_datos_con_certificado CERTIFICADO  ON (DATOS."RutCliente" = CERTIFICADO."RutCliente"
																							AND DATOS."CodigoPortafolio" = CERTIFICADO."CodigoPortafolio"
																							AND DATOS."Fondo" = CERTIFICADO."Fondo"
																							AND DATOS."IdEmpresa" = CERTIFICADO."IdEmpresa"
																							AND DATOS."Version" = CERTIFICADO."Version"
																							AND DATOS."IdTipoDocumento" = CERTIFICADO."IdTipoDocumento"
																							);
			
									
			INSERT INTO "BSCL.Normativa.BD::TRI.CertificadoCliente"
			("IdCertificadoCliente","IdTipoDocumento","IdEmpresa","Esquema","Version","Anio","Periodo","IdCliente","IdPortafolio","Folio","IdTipoCertificado","RutCliente","CodigoPortafolio","Nemo")
			SELECT 
			 "IdCertificadoCliente","IdTipoDocumento","IdEmpresa",'DJ1922',"Version","Version","Version",0 AS "IdCliente",0 AS "IdPortafolio","Folio",'CERT43',TRIM(UPPER("RutCliente")),"CodigoPortafolio","Fondo"	
			FROM :v_datos_sin_certificado A
			WHERE NOT EXISTS ( SELECT 1 FROM "BSCL.Normativa.BD::TRI.CertificadoCliente"
								WHERE "Esquema" ='DJ1922' AND  "Version"= A."Version" AND "Anio"=A."Version" AND  "Periodo"= A."Version"
								AND  "RutCliente" =A."RutCliente" AND  "CodigoPortafolio"= A."CodigoPortafolio" AND "IdTipoCertificado" ='CERT43' AND "Nemo"=A."Fondo"
							 );  

			--DELETE  FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" d 
			--WHERE YEAR(d."FechaInversion") = :p_Version
			--AND d."RutCliente" IN ( SELECT "RutCliente" FROM :v_cliente);
		
			DELETE FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43"
			WHERE "IdCertificadoCliente" IN ( SELECT CER."IdCertificadoCliente"  
											  FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
											  WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT43');
				

			INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43"
			(
				 "IdDetalleCertificadoClienteDJ1922N43"			
				,"IdCertificadoCliente"							
				,"FechaCertificado"		
				,"NumeroCertificado"	
				-------------------------
				,"IdOperacion"									
				,"RutAdministradora"							
				,"IdEmisor"										
				,"RutFondo"										
				,"RunFondo"										
				,"Fondo"										
				,"TipoFondo"									
				,"RutCliente"									
				,"NombreParticipe"								
				,"TaxId"										
				,"CodigoPais"									
				,"TipoOperacion"								
				,"FechaInversion"								
				,"MonedaReajuste"								
				,"MontoNominalAportes"							
				,"MontoNominalAportesActualizado"				
				,"NumeroSerieCuotas"							
				,"CantidadCuotasParticipe"						
				,"ParticipeCumpleRequisitos"					
				,"CuotasCumplenRequisitos"						
				,"ValorCuotaPesos"								
				,"NumCuotasSerieParticipe"							
				,"PorcentajeParticipacion31Dic"						
				,"ValNominalCuotasRescatadas"						
				,"CantidadCuotasRescatadas"							
				,"DiferenciaPorRescateOEnajenacion"					
				,"DiferenciaPorRescateOEnajenacionActualizado"		
				,"DifPorRescateOEnajenacionCFIAcogido107"			
				,"DifPorRescateOEnajenacionCFIAcogido107Actualizado"
				,"ValNominalInversionaFechaLiquidacion"				
				,"CantCuotasCedidasPorLiquidacion"					
				,"DiferenciaValorCuotasFechaLiquidacion"			
				,"DiferenciaValorCuotasFechaLiquidacionActualizado"	
				,"TributaSa"										
				,"DivRemDistAfectGCConIDPC"  						
				,"DivRemDistAfectGCConIDPCal31122016"  				
				,"DivRemDistAfectGCConIDPCVOL"  					
				,"DivRemDistAfectGCSINDC" 							
				,"DisRemRentasTribCProvRap_C96"  					
				,"DisRemRentasTribCORP_C97"  						
				,"DisRemRentasTribCExDD_C98"  						
				,"DisRemRentasTribCUtAFFut_C99"  					
				,"DivRemDistNoRentaFUT"  							
				,"DisRemRentasExIGCAfecImptoAdi_C100"  				
				,"CredImpGloCompNSRSD"  							
				,"CredImpGloCompNSRCD"  							
				,"CredIGCAcumD01012017RANSRD01012020SDD_C101"  		
				,"CredIGCAcumD01012017RANSRD01012020CDD_C102"  		
				,"CredImpGloCompSRSD"  								
				,"CredImpGloCompSRCD"  								
				,"CredIGCAcumD01012017RXSRSDD_C103"  				
				,"CredIGCAcumD01012017RXSRCDD_C104"  				
				,"CredImpGloCompCTDCIF"  							
				,"CredImpGloCompCDD31122016"  						
				,"CredImpGloCompSDD31122016"  						
				,"CredIGCAcumH31122016RXSDD_C105"  					
				,"CredIGCAcumH31122016RXCDD_C106"  					
				,"CredImpGloCompCTDCIF31122016"  					
				,"CredImpGloCompCITAExArt21"  						
				,"DetBenNetoPerRutFdo_C107" 						
				,"DetBenNetoPerPOS_C113"  							
				,"DetBenNetoPerNEG_C114"  							
				,"DividendosExentosIGCoAdicional"					
				,"DividendosExentosIGCoAdicionalActualizado"		
				,"DividendosNoConstitutivosDeRenta"					
				,"DividendosNoConstitutivosDeRentaActualizado"		
				,"DivRemDistNoRentaFE" 								
				,"DividendosAfectosImptoUnicoTasa10"				
				,"DividendosAfectosImptoUnicoTasa10Actualizado"		
				,"DividendosAfectosImptoUnicoTasa4"					
				,"DividendosAfectosImptoUnicoTasa4Actualizado"		
				,"DistribucionXDiminucionNoImputadaCapital"			
				,"DevolucionesDeCapital"							
				,"DevolucionesDeCapitalActualizado"					
				,"MontoInversionenCuotasCFIAnterior93"				
				,"FactorActualizacion"								
				,"FechaOrigenInversion"	
				,"RunNemotecnico"	
			)
			SELECT 
				 "IdDetalleCertificadoClienteDJ1922N43"			
				,"IdCertificadoCliente"							
				,"FechaCertificado"
				,"NumeroCertificado"	
				--------------------------
				,COALESCE("IdOperacion",0)								
				,"RutAdministradora"							
				,"IdEmisor"										
				,"RutFondo"										
				,"RunFondo"										
				,"Fondo"										
				,"TipoFondo"									
				,"RutCliente"									
				,"NombreParticipe"								
				,"TaxId"										
				,"CodigoPais"									
				,"TipoOperacion"								
				,"FechaInversion"								
				,"MonedaReajuste"								
				,"MontoNominalAportes"							
				,"MontoNominalAportesActualizado"				
				,COALESCE("NumeroSerieCuotas",900)							
				,"CantidadCuotasParticipe"						
				,"ParticipeCumpleRequisitos"					
				,"CuotasCumplenRequisitos"						
				,"ValorCuotaPesos"								
				,"NumCuotasSerieParticipe"							
				,"PorcentajeParticipacion31Dic"						
				,"ValNominalCuotasRescatadas"						
				,"CantidadCuotasRescatadas"							
				,"DiferenciaPorRescateOEnajenacion"					
				,"DiferenciaPorRescateOEnajenacionActualizado"		
				,"DifPorRescateOEnajenacionCFIAcogido107"			
				,"DifPorRescateOEnajenacionCFIAcogido107Actualizado"
				,"ValNominalInversionaFechaLiquidacion"				
				,"CantCuotasCedidasPorLiquidacion"					
				,"DiferenciaValorCuotasFechaLiquidacion"			
				,"DiferenciaValorCuotasFechaLiquidacionActualizado"	
				,"TributaSa"										
				,"DivRemDistAfectGCConIDPC"  						
				,"DivRemDistAfectGCConIDPCal31122016"  				
				,"DivRemDistAfectGCConIDPCVOL"  					
				,"DivRemDistAfectGCSINDC" 							
				,"DisRemRentasTribCProvRap_C96"  					
				,"DisRemRentasTribCORP_C97"  						
				,"DisRemRentasTribCExDD_C98"  						
				,"DisRemRentasTribCUtAFFut_C99"  					
				,"DivRemDistNoRentaFUT"  							
				,"DisRemRentasExIGCAfecImptoAdi_C100"  				
				,"CredImpGloCompNSRSD"  							
				,"CredImpGloCompNSRCD"  							
				,"CredIGCAcumD01012017RANSRD01012020SDD_C101"  		
				,"CredIGCAcumD01012017RANSRD01012020CDD_C102"  		
				,"CredImpGloCompSRSD"  								
				,"CredImpGloCompSRCD"  								
				,"CredIGCAcumD01012017RXSRSDD_C103"  				
				,"CredIGCAcumD01012017RXSRCDD_C104"  				
				,"CredImpGloCompCTDCIF"  							
				,"CredImpGloCompCDD31122016"  						
				,"CredImpGloCompSDD31122016"  						
				,"CredIGCAcumH31122016RXSDD_C105"  					
				,"CredIGCAcumH31122016RXCDD_C106"  					
				,"CredImpGloCompCTDCIF31122016"  					
				,"CredImpGloCompCITAExArt21"  						
				,"DetBenNetoPerRutFdo_C107" 						
				,"DetBenNetoPerPOS_C113"  							
				,"DetBenNetoPerNEG_C114"  							
				,"DividendosExentosIGCoAdicional"					
				,"DividendosExentosIGCoAdicionalActualizado"		
				,"DividendosNoConstitutivosDeRenta"					
				,"DividendosNoConstitutivosDeRentaActualizado"		
				,"DivRemDistNoRentaFE" 								
				,"DividendosAfectosImptoUnicoTasa10"				
				,"DividendosAfectosImptoUnicoTasa10Actualizado"		
				,"DividendosAfectosImptoUnicoTasa4"					
				,"DividendosAfectosImptoUnicoTasa4Actualizado"		
				,"DistribucionXDiminucionNoImputadaCapital"			
				,"DevolucionesDeCapital"							
				,"DevolucionesDeCapitalActualizado"					
				,"MontoInversionenCuotasCFIAnterior93"				
				,"FactorActualizacion"								
				,"FechaOrigenInversion"	
				,"RunNemotecnico"	
			FROM 
			(SELECT * FROM :v_detalle_con_certificado UNION ALL SELECT * FROM :v_detalle_sin_certificado);
			
			SELECT COUNT(1) into v_count_certificado
			FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
				INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" DETALLE ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")
				WHERE "Esquema" = 'DJ1922' AND "Version" = :p_Version;
			
			IF(v_count_certificado > 0) THEN
				SELECT MAX("Folio") into v_max_folio  
				FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
				INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" DETALLE ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")
				WHERE "Esquema" = 'DJ1922' AND "Version" = :p_Version;
				
				UPDATE "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
				SET "UltimoFolio" = v_max_folio
				WHERE "Esquema" = 'DJ1922' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_43';
			END IF;
			
			---actualizar Folio DJ 
			
			UPDATE  d SET d."NumeroCertificado"=cer."Folio"
					FROM "BSCL.Normativa.BD::TRI.DatosDJ1922" d
					INNER JOIN "BSCL.Normativa.BD::DOC.Documento" doc ON (DOC."IdDocumento"=d."IdDocumento")
					INNER JOIN
					  (
					  SELECT c."Folio",c."RutCliente",TO_VARCHAR (c."Version") AS "Version",c."IdEmpresa",det."Fondo" 
						FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" c  
						INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" det ON (det."IdCertificadoCliente" = c."IdCertificadoCliente") 
						WHERE  c."IdTipoCertificado"= 'CERT43' 			
						GROUP BY c."Folio",c."RutCliente",c."Version",c."IdEmpresa",det."Fondo" 
					  
					  )AS cer ON(cer."RutCliente" = TRIM(UPPER(d."RutCliente")) AND cer."Version" = doc."Version" AND cer."IdEmpresa"  = doc."IdEmpresa"
								AND cer."Fondo" = d."Fondo")
					WHERE 	doc."Version"=:p_Version;	
				
			----actualiza Folio detalle
			
			 UPDATE FOL SET FOL."NumeroCertificado"=CER."Folio"
				 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
				 INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" FOL ON (CER."IdCertificadoCliente"=FOL."IdCertificadoCliente")
				 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT43' ;	
				
				
				
		
			update "BSCL.Normativa.BD::TRI.ControlProceso" 
			set "Carga" = 'S', "Validacion" = 'S', 
			    "PreDeclaracion" = 'S', 
			    "Certificacion" = 'S', "EstadoCertificado" = 'GEN'
			WHERE "Esquema"= 'DJ1922' AND "Version" = :p_Version;
			
			UPDATE C SET C."IdEstado"='ACT'
			FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" C
			WHERE C."IdTipoCertificado"='CERT43' AND C."Version"=:p_Version
			AND EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" D WHERE D."IdCertificadoCliente"=C."IdCertificadoCliente");


			v_log = SELECT 
						0 AS "IdCabeceraLog"
						,'INGRESO' AS "IdTipoLog"
						,'NOR' AS "IdModulo"
						,:p_IdUsuario AS "IdUsuario"
						,:p_IdEmpresa AS "IdEmpresa"
						,:v_FechaSistema AS "FechaSistemaLog"
						,:p_IdUsuario AS "IdUsuarioAtendido"
						,0 AS "IdPersona"
						,0 AS "IdCliente"
						,'' AS "EstadoCliente"
						,'' AS "NombreCliente"
						,'' AS "IdTipoDocumentoIdentidad"
						,'' AS "IdDocumentoIdentidad"
						,0 AS "IdPortafolio"
						,'' AS "EstadoPortafolio"
						,'CERTIFICADO_43' AS "IdTipoRegistro"
						,:p_Version AS "IdRegistro"
						,'ACT' AS "EstadoRegistro"
						,'' AS "IdTipoOperacion"
						,0 AS "Folio"
						,0 AS "Cantidad"
						,0 AS "PrecioTasa"
						,0 AS "Monto"
						,'' AS "IdMonedaOrigen"
						,'' AS "IdMonedaLiquidacion"
						,'' AS "TipoCambio"
						,'' AS "Nemo"
						,0 AS "IdProceso"
						,'' AS "EstadoProceso"
						,'' AS "NombreProceso"
					FROM DUMMY;
			
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1922.GenerarCertificado43','FIN','FIN');
		
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;	
END;