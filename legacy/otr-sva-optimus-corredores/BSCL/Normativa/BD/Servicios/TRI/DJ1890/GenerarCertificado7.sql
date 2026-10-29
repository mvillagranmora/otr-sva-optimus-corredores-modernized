SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1890.GenerarCertificado7";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1890.GenerarCertificado7"(
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
			DECLARE V_ultimo_folio		BIGINT;
			DECLARE v_max_folio			BIGINT;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1894.GenerarCertificado21','INICIO','INICIO');
				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			SELECT COUNT(1) into v_count_folio
			FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			WHERE "Esquema" = 'DJ1890' AND "Version" = :p_Version;
			
			IF v_count_folio = 0 THEN
				INSERT INTO "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
				("Esquema","Version","UltimoFolio")
				VALUES('DJ1890',:p_Version,0);
			END IF;
			
			SELECT "UltimoFolio" into v_ultimo_folio
			FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			WHERE "Esquema" = 'DJ1890' AND "Version" = :p_Version;
			
			v_datos_cabecera = SELECT DJ1890."IdCliente"
									 ,DJ1890."IdPortafolio"
									 ,TO_CHAR(DJ1890."FechaOperacion",'YYYY-MM') AS "AgnoMesOperacion"
									 ,DOCUMENTO."IdTipoDocumento"
									 ,:p_IdEmpresa AS "IdEmpresa"
									 ,:p_Version AS "Version"
									 ,DJ1890."RutInversionista"									
									 ,DJ1890."InteresesRealesDepositosPositivo"					
									 ,DJ1890."InteresesRealesDepositosNegativo"					
									 ,DJ1890."InteresesRealesMantencionPositivo"					
									 ,DJ1890."InteresesRealesMantencionNegativo"					
									 ,DJ1890."InteresesRealesOperacionesCaptacionPositivo"		
									 ,DJ1890."InteresesRealesOperacionesCaptacionNegativo"		
									 ,DJ1890."InteresesMonedaNacionalExtranjeraNominal"			
									 ,DJ1890."ImpuestoAdicionalMondaNacionalExtranjeraNominal"	
									 ,DJ1890."FechaOperacion"
									 ,DJ1890."InteresesRealesDepositosPositivoActualizado"
									 ,DJ1890."InteresesRealesDepositosNegativoActualizado"
									 ,DJ1890."InteresesRealesMantencionPositivoActualizado"
									 ,DJ1890."InteresesRealesMantencionNegativoActualizado"
									 ,DJ1890."InteresesRealesOperacionesCaptacionPositivoActualizado"
									 ,DJ1890."InteresesRealesOperacionesCaptacionNegativoActualizado"
									 ,DJ1890."FechaVencimiento"
									 ,DJ1890."FolioOperacion"	
									 ,DJ1890."MontoOperacionCaptacion"
									 ,DJ1890."MontoPercibidoFechaVencimiento"	
									 ,DJ1890."InteresesRentasPagadas"	
									 ,DJ1890."InteresesRealesPositivosNegativos"
									 ,'CAB' AS "TipoRegistro"	
									 ,DJ1890."FactorActualizacion"
					 FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1890.Datos" DJ1890
					 INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" DOCUMENTO ON (DJ1890."IdDocumento" = DOCUMENTO."IdDocumento")
					 LEFT OUTER JOIN :p_Clientes CLIENTES ON (CLIENTES."IdCliente" = DJ1890."IdCliente") 
					 WHERE DOCUMENTO."Version" = :p_Version AND (:p_Todos = 'S' OR (:p_Todos = 'N' AND COALESCE(CLIENTES."IdCliente",0) != 0));

			v_datos_mensuales = SELECT "IdCliente"
									   ,"IdPortafolio"
									   ,"AgnoMesOperacion"
									   ,"IdTipoDocumento"
									   ,"IdEmpresa"
									   ,"Version"
									   ,"RutInversionista"									
									   ,SUM("InteresesRealesDepositosPositivo") AS "InteresesRealesDepositosPositivo"				
									   ,SUM("InteresesRealesDepositosNegativo") AS "InteresesRealesDepositosNegativo"					
									   ,SUM("InteresesRealesMantencionPositivo") AS "InteresesRealesMantencionPositivo"					
									   ,SUM("InteresesRealesMantencionNegativo") AS "InteresesRealesMantencionNegativo"					
									   ,SUM("InteresesRealesOperacionesCaptacionPositivo") AS "InteresesRealesOperacionesCaptacionPositivo"		
									   ,SUM("InteresesRealesOperacionesCaptacionNegativo") AS "InteresesRealesOperacionesCaptacionNegativo"		
									   ,SUM("InteresesMonedaNacionalExtranjeraNominal") AS "InteresesMonedaNacionalExtranjeraNominal"			
									   ,SUM("ImpuestoAdicionalMondaNacionalExtranjeraNominal") AS "ImpuestoAdicionalMondaNacionalExtranjeraNominal"	
									   
									   ,SUM("InteresesRealesDepositosPositivoActualizado") AS "InteresesRealesDepositosPositivoActualizado"
									   ,SUM("InteresesRealesDepositosNegativoActualizado") AS "InteresesRealesDepositosNegativoActualizado"
									   ,SUM("InteresesRealesMantencionPositivoActualizado") AS "InteresesRealesMantencionPositivoActualizado"
									   ,SUM("InteresesRealesMantencionNegativoActualizado") AS "InteresesRealesMantencionNegativoActualizado" 
									   ,SUM("InteresesRealesOperacionesCaptacionPositivoActualizado") AS "InteresesRealesOperacionesCaptacionPositivoActualizado"
									   ,SUM("InteresesRealesOperacionesCaptacionNegativoActualizado") AS "InteresesRealesOperacionesCaptacionNegativoActualizado"
									   ,'DET' AS "TipoRegistro" 
									   ,"FactorActualizacion"
								FROM :v_datos_cabecera 
								GROUP BY "IdCliente","IdPortafolio","AgnoMesOperacion","IdTipoDocumento","IdEmpresa"
										,"Version","RutInversionista","TipoRegistro","FactorActualizacion";
								
			select * from :v_datos_mensuales;
			
			v_datos = SELECT DISTINCT "IdCliente","IdPortafolio","RutInversionista","IdEmpresa","Version","IdTipoDocumento" FROM :v_datos_cabecera;

			v_datos_sin_certificado = SELECT "BSCL.Normativa.BD.Secuencias::TRI.IdCertificadoCliente".NEXTVAL AS "IdCertificadoCliente"
											 ,:v_ultimo_folio + "Numero" AS "Folio"
											 ,"IdCliente"
											 ,"IdPortafolio"
											 ,"IdTipoDocumento"
											 ,"IdEmpresa"
									   		 ,"Version"
									   		 ,"RutInversionista"
									  FROM (SELECT ROW_NUMBER() OVER(ORDER BY DATOS."IdPortafolio") AS "Numero"
											,DATOS."IdCliente"
											,DATOS."IdPortafolio"
											,DATOS."IdTipoDocumento"
											,DATOS."IdEmpresa"
									   		,DATOS."Version"
									   		,DATOS."RutInversionista"
											FROM :v_datos DATOS
											WHERE NOT EXISTS (
												SELECT 1 FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO 
												WHERE CERTIFICADO."IdCliente" = DATOS."IdCliente"
												AND CERTIFICADO."IdPortafolio" = DATOS."IdPortafolio"
												AND CERTIFICADO."Esquema" = 'DJ1890'
												AND CERTIFICADO."Version" = DATOS."Version"
												AND CERTIFICADO."IdEmpresa" = DATOS."IdEmpresa"
											));
																		
			v_detalle_cabecera_sin_certificado = SELECT  "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1890".NEXTVAL AS "IdDetalleCertificadoClienteDJ1890"
														,"IdCertificadoCliente"
													    ,DATOS."AgnoMesOperacion"									
													    ,DATOS."InteresesRealesDepositosPositivo"				
													    ,DATOS."InteresesRealesDepositosNegativo"					
													    ,DATOS."InteresesRealesMantencionPositivo"					
													    ,DATOS."InteresesRealesMantencionNegativo"					
													    ,DATOS."InteresesRealesOperacionesCaptacionPositivo"		
													    ,DATOS."InteresesRealesOperacionesCaptacionNegativo"		
													    ,DATOS."InteresesMonedaNacionalExtranjeraNominal"			
													    ,DATOS."ImpuestoAdicionalMondaNacionalExtranjeraNominal"	
													    ,DATOS."FechaOperacion"
													    ,DATOS."InteresesRealesDepositosPositivoActualizado"
													    ,DATOS."InteresesRealesDepositosNegativoActualizado"
													    ,DATOS."InteresesRealesMantencionPositivoActualizado"
													    ,DATOS."InteresesRealesMantencionNegativoActualizado"
													    ,DATOS."InteresesRealesOperacionesCaptacionPositivoActualizado"
													    ,DATOS."InteresesRealesOperacionesCaptacionNegativoActualizado"
													    ,DATOS."FechaVencimiento"
													    ,DATOS."FolioOperacion"	
													    ,DATOS."MontoOperacionCaptacion"
													    ,DATOS."MontoPercibidoFechaVencimiento"	
													    ,DATOS."InteresesRentasPagadas"	
													    ,DATOS."InteresesRealesPositivosNegativos"
													    ,DATOS."TipoRegistro"
													    ,DATOS."RutInversionista"
													    ,DATOS."FactorActualizacion"
												FROM :v_datos_cabecera DATOS
												INNER JOIN :v_datos_sin_certificado CERTIFICADO ON (DATOS."IdCliente" = CERTIFICADO."IdCliente"
																									AND DATOS."IdPortafolio" = CERTIFICADO."IdPortafolio"
																									AND DATOS."RutInversionista" = CERTIFICADO."RutInversionista"
																									AND DATOS."IdEmpresa" = CERTIFICADO."IdEmpresa"
																									AND DATOS."Version" = CERTIFICADO."Version"
																									AND DATOS."IdTipoDocumento" = CERTIFICADO."IdTipoDocumento");
			
			v_detalle_mensuales_sin_certificado = SELECT  "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1890".NEXTVAL AS "IdDetalleCertificadoClienteDJ1890"
														,"IdCertificadoCliente"
													    ,DATOS."AgnoMesOperacion"									
													    ,DATOS."InteresesRealesDepositosPositivo"				
													    ,DATOS."InteresesRealesDepositosNegativo"					
													    ,DATOS."InteresesRealesMantencionPositivo"					
													    ,DATOS."InteresesRealesMantencionNegativo"					
													    ,DATOS."InteresesRealesOperacionesCaptacionPositivo"		
													    ,DATOS."InteresesRealesOperacionesCaptacionNegativo"		
													    ,DATOS."InteresesMonedaNacionalExtranjeraNominal"			
													    ,DATOS."ImpuestoAdicionalMondaNacionalExtranjeraNominal"	
													    ,DATOS."InteresesRealesDepositosPositivoActualizado"
													    ,DATOS."InteresesRealesDepositosNegativoActualizado"
													    ,DATOS."InteresesRealesMantencionPositivoActualizado"
													    ,DATOS."InteresesRealesMantencionNegativoActualizado"
													    ,DATOS."InteresesRealesOperacionesCaptacionPositivoActualizado"
													    ,DATOS."InteresesRealesOperacionesCaptacionNegativoActualizado"
													    ,DATOS."TipoRegistro"
													    ,DATOS."FactorActualizacion"
													    ,DATOS."RutInversionista"
												FROM :v_datos_mensuales DATOS
												INNER JOIN :v_datos_sin_certificado CERTIFICADO ON (DATOS."IdCliente" = CERTIFICADO."IdCliente"
																									AND DATOS."IdPortafolio" = CERTIFICADO."IdPortafolio"
																									AND DATOS."RutInversionista" = CERTIFICADO."RutInversionista"
																									AND DATOS."IdEmpresa" = CERTIFICADO."IdEmpresa"
																									AND DATOS."Version" = CERTIFICADO."Version"
																									AND DATOS."IdTipoDocumento" = CERTIFICADO."IdTipoDocumento"
																									);
			
			v_datos_con_certificado = SELECT CERTIFICADO."IdCertificadoCliente"
													 ,CERTIFICADO."Folio"
													 ,DATOS."IdCliente"
													 ,DATOS."IdPortafolio"
													 ,DATOS."IdTipoDocumento"
													 ,DATOS."IdEmpresa"
											   		 ,DATOS."Version"
													 ,DATOS."RutInversionista"
												FROM :v_datos DATOS
												INNER JOIN "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO ON (CERTIFICADO."IdCliente" = DATOS."IdCliente"
																														AND CERTIFICADO."IdPortafolio" = DATOS."IdPortafolio"
																														AND CERTIFICADO."Esquema" = 'DJ1890'
																														AND CERTIFICADO."Version" = DATOS."Version");
				
			v_detalle_cabecera_con_certificado = SELECT "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1890".NEXTVAL AS "IdDetalleCertificadoClienteDJ1890"
														,CERTIFICADO."IdCertificadoCliente"
														,DATOS."AgnoMesOperacion"									
													    ,DATOS."InteresesRealesDepositosPositivo"				
													    ,DATOS."InteresesRealesDepositosNegativo"					
													    ,DATOS."InteresesRealesMantencionPositivo"					
													    ,DATOS."InteresesRealesMantencionNegativo"					
													    ,DATOS."InteresesRealesOperacionesCaptacionPositivo"		
													    ,DATOS."InteresesRealesOperacionesCaptacionNegativo"		
													    ,DATOS."InteresesMonedaNacionalExtranjeraNominal"			
													    ,DATOS."ImpuestoAdicionalMondaNacionalExtranjeraNominal"	
													    ,DATOS."FechaOperacion"
													    ,DATOS."InteresesRealesDepositosPositivoActualizado"
													    ,DATOS."InteresesRealesDepositosNegativoActualizado"
													    ,DATOS."InteresesRealesMantencionPositivoActualizado"
													    ,DATOS."InteresesRealesMantencionNegativoActualizado"
													    ,DATOS."InteresesRealesOperacionesCaptacionPositivoActualizado"
													    ,DATOS."InteresesRealesOperacionesCaptacionNegativoActualizado"
													    ,DATOS."FechaVencimiento"
													    ,DATOS."FolioOperacion"	
													    ,DATOS."MontoOperacionCaptacion"
													    ,DATOS."MontoPercibidoFechaVencimiento"	
													    ,DATOS."InteresesRentasPagadas"	
													    ,DATOS."InteresesRealesPositivosNegativos"
													    ,DATOS."TipoRegistro"
													    ,DATOS."FactorActualizacion"
													    ,DATOS."RutInversionista"
										FROM :v_datos_cabecera DATOS
										INNER JOIN :v_datos_con_certificado CERTIFICADO  ON (DATOS."IdCliente" = CERTIFICADO."IdCliente"
																							AND DATOS."IdPortafolio" = CERTIFICADO."IdPortafolio"
																							AND DATOS."RutInversionista" = CERTIFICADO."RutInversionista"
																							AND DATOS."IdEmpresa" = CERTIFICADO."IdEmpresa"
																							AND DATOS."Version" = CERTIFICADO."Version"
																							AND DATOS."IdTipoDocumento" = CERTIFICADO."IdTipoDocumento"
																							);
			
			v_detalle_mensuales_con_certificado = SELECT DETALLE."IdDetalleCertificado7DJ1890"
														,CERTIFICADO."IdCertificadoCliente"
														,DATOS."AgnoMesOperacion"									
													    ,DATOS."InteresesRealesDepositosPositivo"				
													    ,DATOS."InteresesRealesDepositosNegativo"					
													    ,DATOS."InteresesRealesMantencionPositivo"					
													    ,DATOS."InteresesRealesMantencionNegativo"					
													    ,DATOS."InteresesRealesOperacionesCaptacionPositivo"		
													    ,DATOS."InteresesRealesOperacionesCaptacionNegativo"		
													    ,DATOS."InteresesMonedaNacionalExtranjeraNominal"			
													    ,DATOS."ImpuestoAdicionalMondaNacionalExtranjeraNominal"
													    ,DATOS."InteresesRealesDepositosPositivoActualizado"
													    ,DATOS."InteresesRealesDepositosNegativoActualizado"
													    ,DATOS."InteresesRealesMantencionPositivoActualizado"
													    ,DATOS."InteresesRealesMantencionNegativoActualizado"
													    ,DATOS."InteresesRealesOperacionesCaptacionPositivoActualizado"
													    ,DATOS."InteresesRealesOperacionesCaptacionNegativoActualizado"
													    ,DATOS."TipoRegistro"
													    ,DATOS."FactorActualizacion"
													    ,DATOS."RutInversionista"
										FROM :v_datos_mensuales DATOS
										INNER JOIN :v_datos_con_certificado CERTIFICADO  ON (DATOS."IdCliente" = CERTIFICADO."IdCliente"
																							AND DATOS."IdPortafolio" = CERTIFICADO."IdPortafolio"
																							AND DATOS."RutInversionista" = CERTIFICADO."RutInversionista"
																							AND DATOS."IdEmpresa" = CERTIFICADO."IdEmpresa"
																							AND DATOS."Version" = CERTIFICADO."Version"
																							AND DATOS."IdTipoDocumento" = CERTIFICADO."IdTipoDocumento"
																							)
										INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890" DETALLE ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente" AND DATOS."RutInversionista" = DETALLE."RutInversionista" AND DETALLE."TipoRegistro" = 'DET' AND DATOS."AgnoMesOperacion" = DETALLE."DescMes");
			
			
			v_certificado_sin_detalle_mensuales = SELECT 	"BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1890".NEXTVAL AS "IdDetalleCertificadoClienteDJ1890"
												,CERTIFICADO."IdCertificadoCliente"
												,DATOS."AgnoMesOperacion"									
											    ,DATOS."InteresesRealesDepositosPositivo"				
											    ,DATOS."InteresesRealesDepositosNegativo"					
											    ,DATOS."InteresesRealesMantencionPositivo"					
											    ,DATOS."InteresesRealesMantencionNegativo"					
											    ,DATOS."InteresesRealesOperacionesCaptacionPositivo"		
											    ,DATOS."InteresesRealesOperacionesCaptacionNegativo"		
											    ,DATOS."InteresesMonedaNacionalExtranjeraNominal"			
											    ,DATOS."ImpuestoAdicionalMondaNacionalExtranjeraNominal"
											    ,DATOS."InteresesRealesDepositosPositivoActualizado"
											    ,DATOS."InteresesRealesDepositosNegativoActualizado"
											    ,DATOS."InteresesRealesMantencionPositivoActualizado"
											    ,DATOS."InteresesRealesMantencionNegativoActualizado"
											    ,DATOS."InteresesRealesOperacionesCaptacionPositivoActualizado"
											    ,DATOS."InteresesRealesOperacionesCaptacionNegativoActualizado"
											    ,DATOS."TipoRegistro"
											    ,DATOS."FactorActualizacion"
											    ,DATOS."RutInversionista"
										FROM :v_datos_mensuales DATOS
										INNER JOIN :v_datos_con_certificado CERTIFICADO  ON (DATOS."IdCliente" = CERTIFICADO."IdCliente"
																							AND DATOS."IdPortafolio" = CERTIFICADO."IdPortafolio"
																							AND DATOS."RutInversionista" = CERTIFICADO."RutInversionista"
																							AND DATOS."IdEmpresa" = CERTIFICADO."IdEmpresa"
																							AND DATOS."Version" = CERTIFICADO."Version"
																							AND DATOS."IdTipoDocumento" = CERTIFICADO."IdTipoDocumento"
																							)
										WHERE EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890" DETALLE WHERE CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente" AND DATOS."RutInversionista" = DETALLE."RutInversionista")
										AND NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890" DETALLE WHERE CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente" AND DATOS."RutInversionista" = DETALLE."RutInversionista" AND DATOS."AgnoMesOperacion" = DETALLE."DescMes");
															
				
									
			INSERT INTO "BSCL.Normativa.BD::TRI.CertificadoCliente"
			("IdCertificadoCliente","IdTipoDocumento","IdEmpresa","Esquema","Version","Anio","Periodo","IdCliente","IdPortafolio","Folio")
			SELECT 
			 "IdCertificadoCliente","IdTipoDocumento","IdEmpresa",'DJ1890',"Version","Version","Version","IdCliente","IdPortafolio","Folio"	
			FROM :v_datos_sin_certificado;
			
			
			INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890"
			("IdDetalleCertificado7DJ1890","IdCertificadoCliente","NumeroDocumento","RutInversionista","FechaOperacion","FechaVencimiento",
			"TipoRegistro","DescMes","MontoOperacionCaptacion","MontoPercibidoFechaVencimiento","InteresesPagados",
			"InteresesRealesPositivosNegativos","InteresesRealesDepositosPositivo","InteresesRealesDepositosNegativo","InteresRealesMantencionPositivo",
			"InteresRealesMantencionNegativo","InteresesRealesOperacionesPositivo","InteresesRealesOperacionesNegativo"
			,"FactorActualizacion","InteresesRealesDepositosActualizadoPositivo","InteresesRealesDepositosActualizadoNegativo",
			"InteresRealesMantencionActualizadoPositivo","InteresRealesMantencionActualizadoNegativo","InteresesRealesOperacionesActualizadoPositivo",
			"InteresesRealesOperacionesActualizadoNegativo")
			SELECT "IdDetalleCertificadoClienteDJ1890","IdCertificadoCliente","FolioOperacion","RutInversionista","FechaOperacion","FechaVencimiento",
			"TipoRegistro","AgnoMesOperacion","MontoOperacionCaptacion","MontoPercibidoFechaVencimiento","InteresesRentasPagadas",
			"InteresesRealesPositivosNegativos","InteresesRealesDepositosPositivo","InteresesRealesDepositosNegativo","InteresesRealesMantencionPositivo",
			"InteresesRealesMantencionNegativo","InteresesRealesOperacionesCaptacionPositivo","InteresesRealesOperacionesCaptacionNegativo",
			"FactorActualizacion","InteresesRealesDepositosPositivoActualizado","InteresesRealesDepositosNegativoActualizado",
			"InteresesRealesMantencionPositivoActualizado","InteresesRealesMantencionNegativoActualizado","InteresesRealesOperacionesCaptacionPositivoActualizado",
			"InteresesRealesOperacionesCaptacionNegativoActualizado"
			FROM :v_detalle_cabecera_sin_certificado;
			
			INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890"
			("IdDetalleCertificado7DJ1890","IdCertificadoCliente","NumeroDocumento","RutInversionista","FechaOperacion","FechaVencimiento",
			"TipoRegistro","DescMes","MontoOperacionCaptacion","MontoPercibidoFechaVencimiento","InteresesPagados",
			"InteresesRealesPositivosNegativos","InteresesRealesDepositosPositivo","InteresesRealesDepositosNegativo","InteresRealesMantencionPositivo",
			"InteresRealesMantencionNegativo","InteresesRealesOperacionesPositivo","InteresesRealesOperacionesNegativo"
			,"FactorActualizacion","InteresesRealesDepositosActualizadoPositivo","InteresesRealesDepositosActualizadoNegativo",
			"InteresRealesMantencionActualizadoPositivo","InteresRealesMantencionActualizadoNegativo","InteresesRealesOperacionesActualizadoPositivo",
			"InteresesRealesOperacionesActualizadoNegativo")
			SELECT "IdDetalleCertificadoClienteDJ1890","IdCertificadoCliente",0,"RutInversionista",TO_DATE('1900-01-01'),TO_DATE('1900-01-01'),
			"TipoRegistro","AgnoMesOperacion",0,0,0,
			0,"InteresesRealesDepositosPositivo","InteresesRealesDepositosNegativo","InteresesRealesMantencionPositivo",
			"InteresesRealesMantencionNegativo","InteresesRealesOperacionesCaptacionPositivo","InteresesRealesOperacionesCaptacionNegativo",
			"FactorActualizacion","InteresesRealesDepositosPositivoActualizado","InteresesRealesDepositosNegativoActualizado",
			"InteresesRealesMantencionPositivoActualizado","InteresesRealesMantencionNegativoActualizado","InteresesRealesOperacionesCaptacionPositivoActualizado",
			"InteresesRealesOperacionesCaptacionNegativoActualizado"
			FROM :v_detalle_mensuales_sin_certificado;
			
			DELETE FROM "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890" DATOS
			WHERE EXISTS (SELECT 1 FROM :v_detalle_cabecera_con_certificado CERTIFICADO WHERE DATOS."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente"
			                                                                            AND DATOS."TipoRegistro" = CERTIFICADO."TipoRegistro");
			
			INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890"
			("IdDetalleCertificado7DJ1890","IdCertificadoCliente","NumeroDocumento","RutInversionista","FechaOperacion","FechaVencimiento",
			"TipoRegistro","DescMes","MontoOperacionCaptacion","MontoPercibidoFechaVencimiento","InteresesPagados",
			"InteresesRealesPositivosNegativos","InteresesRealesDepositosPositivo","InteresesRealesDepositosNegativo","InteresRealesMantencionPositivo",
			"InteresRealesMantencionNegativo","InteresesRealesOperacionesPositivo","InteresesRealesOperacionesNegativo"
			,"FactorActualizacion","InteresesRealesDepositosActualizadoPositivo","InteresesRealesDepositosActualizadoNegativo",
			"InteresRealesMantencionActualizadoPositivo","InteresRealesMantencionActualizadoNegativo","InteresesRealesOperacionesActualizadoPositivo",
			"InteresesRealesOperacionesActualizadoNegativo")
			SELECT "IdDetalleCertificadoClienteDJ1890","IdCertificadoCliente","FolioOperacion","RutInversionista","FechaOperacion","FechaVencimiento",
			"TipoRegistro","AgnoMesOperacion","MontoOperacionCaptacion","MontoPercibidoFechaVencimiento","InteresesRentasPagadas",
			"InteresesRealesPositivosNegativos","InteresesRealesDepositosPositivo","InteresesRealesDepositosNegativo","InteresesRealesMantencionPositivo",
			"InteresesRealesMantencionNegativo","InteresesRealesOperacionesCaptacionPositivo","InteresesRealesOperacionesCaptacionNegativo",
			"FactorActualizacion","InteresesRealesDepositosPositivoActualizado","InteresesRealesDepositosNegativoActualizado",
			"InteresesRealesMantencionPositivoActualizado","InteresesRealesMantencionNegativoActualizado","InteresesRealesOperacionesCaptacionPositivoActualizado",
			"InteresesRealesOperacionesCaptacionNegativoActualizado"
			FROM :v_detalle_cabecera_con_certificado;
			
			INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890"
			("IdDetalleCertificado7DJ1890","IdCertificadoCliente","NumeroDocumento","RutInversionista","FechaOperacion","FechaVencimiento",
			"TipoRegistro","DescMes","MontoOperacionCaptacion","MontoPercibidoFechaVencimiento","InteresesPagados",
			"InteresesRealesPositivosNegativos","InteresesRealesDepositosPositivo","InteresesRealesDepositosNegativo","InteresRealesMantencionPositivo",
			"InteresRealesMantencionNegativo","InteresesRealesOperacionesPositivo","InteresesRealesOperacionesNegativo"
			,"FactorActualizacion","InteresesRealesDepositosActualizadoPositivo","InteresesRealesDepositosActualizadoNegativo",
			"InteresRealesMantencionActualizadoPositivo","InteresRealesMantencionActualizadoNegativo","InteresesRealesOperacionesActualizadoPositivo",
			"InteresesRealesOperacionesActualizadoNegativo")
			SELECT "IdDetalleCertificadoClienteDJ1890","IdCertificadoCliente",0,"RutInversionista",TO_DATE('1900-01-01'),TO_DATE('1900-01-01'),
			"TipoRegistro","AgnoMesOperacion",0,0,0,
			0,"InteresesRealesDepositosPositivo","InteresesRealesDepositosNegativo","InteresesRealesMantencionPositivo",
			"InteresesRealesMantencionNegativo","InteresesRealesOperacionesCaptacionPositivo","InteresesRealesOperacionesCaptacionNegativo",
			"FactorActualizacion","InteresesRealesDepositosPositivoActualizado","InteresesRealesDepositosNegativoActualizado",
			"InteresesRealesMantencionPositivoActualizado","InteresesRealesMantencionNegativoActualizado","InteresesRealesOperacionesCaptacionPositivoActualizado",
			"InteresesRealesOperacionesCaptacionNegativoActualizado"
			FROM :v_certificado_sin_detalle_mensuales;
			
			UPDATE DETALLE_CERTIFICADO
			SET "NumeroDocumento" = 0,
				"RutInversionista" = DETALLE."RutInversionista",
				"FechaOperacion" = TO_DATE('1900-01-01'),
				"FechaVencimiento" = TO_DATE('1900-01-01'),
				"TipoRegistro" = DETALLE."TipoRegistro",
				"DescMes" = DETALLE."AgnoMesOperacion",
				"MontoOperacionCaptacion" = 0,
				"MontoPercibidoFechaVencimiento" = 0,
				"InteresesPagados" = 0,
				"InteresesRealesPositivosNegativos" = 0,
				"InteresesRealesDepositosPositivo" = DETALLE."InteresesRealesDepositosPositivo",
				"InteresesRealesDepositosNegativo" = DETALLE."InteresesRealesDepositosNegativo",
				"InteresRealesMantencionPositivo" = DETALLE."InteresesRealesMantencionPositivo",
				"InteresRealesMantencionNegativo" = DETALLE."InteresesRealesMantencionNegativo",
				"InteresesRealesOperacionesPositivo" = DETALLE."InteresesRealesOperacionesCaptacionPositivo",
				"InteresesRealesOperacionesNegativo" = DETALLE."InteresesRealesOperacionesCaptacionNegativo",
				"FactorActualizacion" = DETALLE."FactorActualizacion",
				"InteresesRealesDepositosActualizadoPositivo" = DETALLE."InteresesRealesDepositosPositivoActualizado",
				"InteresesRealesDepositosActualizadoNegativo" = DETALLE."InteresesRealesDepositosNegativoActualizado",
				"InteresRealesMantencionActualizadoPositivo" = DETALLE."InteresesRealesMantencionPositivoActualizado",
				"InteresRealesMantencionActualizadoNegativo" = DETALLE."InteresesRealesMantencionNegativoActualizado",
				"InteresesRealesOperacionesActualizadoPositivo" = DETALLE."InteresesRealesOperacionesCaptacionPositivoActualizado",
				"InteresesRealesOperacionesActualizadoNegativo" = DETALLE."InteresesRealesOperacionesCaptacionNegativoActualizado"
			FROM "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890" DETALLE_CERTIFICADO
			INNER JOIN :v_detalle_mensuales_con_certificado DETALLE ON (DETALLE."IdDetalleCertificado7DJ1890" = DETALLE_CERTIFICADO."IdDetalleCertificado7DJ1890");
			
			SELECT MAX("Folio") into v_max_folio  FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "Esquema" = 'DJ1890' AND "Version" = :p_Version;
			
			UPDATE "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			SET "UltimoFolio" = v_max_folio
			WHERE "Esquema" = 'DJ1890' AND "Version" = :p_Version;
			
			update "BSCL.Normativa.BD::TRI.ControlProceso" 
			set "Carga" = 'S', "Validacion" = 'S', 
			    "PreDeclaracion" = 'S', 
			    "Certificacion" = 'S', "EstadoCertificado" = 'GEN'  
			WHERE "Esquema"= 'DJ1890' AND "Version" = :p_Version;
			
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
						,'CERTIFICADO_7' AS "IdTipoRegistro"
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
		
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1894.GenerarCertificado21','FIN','FIN');
		
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;	
END;
