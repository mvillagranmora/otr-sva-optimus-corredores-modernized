SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarCertificado31";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarCertificado31"(
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
			DECLARE count_certificados  BIGINT;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1894.GenerarCertificado31','INICIO','INICIO');
				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			SELECT COUNT(1) into v_count_folio
			FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			WHERE "Esquema" = 'DJ1894' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_31';
			
			IF v_count_folio = 0 THEN
				INSERT INTO "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
				("Esquema","Version","UltimoFolio","CodigoCertificado","NombreCertificado","CodigoPrograma")
				VALUES('DJ1894',:p_Version,0,'CERTIFICADO_31','Certificado 31','GenerarCertificado31');
			END IF;
			
			SELECT "UltimoFolio" into v_ultimo_folio
			FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			WHERE "Esquema" = 'DJ1894' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_31';
			
			v_datos = SELECT DJ1894."IdCliente"
							 ,DJ1894."IdPortafolio"
							 ,FONDO."IdFondo" AS "IdFondo"
							 ,FONDO."IdEmisor" AS "IdEmisor"
							 ,DJ1894."IdFondo" AS "IdFondoOrigen"
							 ,DJ1894."IdEmisor" AS "IdEmisorOrigen"
							 ,DJ1894."CostoUF" AS "MontoLiquidacion"
							 ,ROUND(DJ1894."Spread" * DJ1894."ValorUFFechaInversion",0) AS "MayorValorUf" 
							 ,DJ1894."PorcentajePromedioAnualAcciones"
							 ,0 AS "PorcentajePromedioAnualAccionesProvenienteReinversion"
							 ,DJ1894."FechaCertificacion"
							 ,SOLICITUD_ORIGINAL."FechaCertificacion" AS "FechaInversionOriginal"
							 ,DOCUMENTO."IdTipoDocumento"
							 ,:p_IdEmpresa AS "IdEmpresa"
							 ,:p_Version AS "Version"
					 FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos" DJ1894
					 INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" DOCUMENTO ON (DJ1894."IdDocumento" = DOCUMENTO."IdDocumento")
					 INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario" SOLICITUD ON (SOLICITUD."IdSolicitudAporteRescate" = DJ1894."IdSolicitud")
					 INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE ON (SOLICITUD."IdSerieFondoDestino" = SERIE."IdSerieFondo")
					 INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" FONDO ON (SERIE."IdFondo" = FONDO."IdFondo")
					 LEFT OUTER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CARTERAS ON (CARTERAS."EGRFolioComprobante" = DJ1894."IdSolicitud"
																		AND CARTERAS."IdModulo" = 'FMM' AND CARTERAS."IdTipoProductoTIPOPE" = 'INFM' 
																		AND CARTERAS."IdClasificacionTipoInstrumento" = 'FM'
																		AND CARTERAS."EGRIdConcepto" = 'RESCFM'
																		AND CARTERAS."IdEstado" IN ('LIQ','MOD','RPA'))
					 LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario" SOLICITUD_ORIGINAL ON (CARTERAS."FolioComprobante" = SOLICITUD_ORIGINAL."IdSolicitudAporteRescate")
					 LEFT OUTER JOIN :p_Clientes CLIENTES ON (CLIENTES."IdCliente" = DJ1894."IdCliente") 
					 WHERE DOCUMENTO."Version" = :p_Version AND (:p_Todos = 'S' OR (:p_Todos = 'N' AND COALESCE(CLIENTES."IdCliente",0) != 0))
					 AND DJ1894."TipoOperacion" IN (8,9);
			
					
			v_datos_certificado = SELECT DISTINCT "IdCliente","IdPortafolio","IdFondo","IdEmpresa","Version","IdTipoDocumento" FROM :v_datos;
									
			v_datos_sin_certificado = SELECT "BSCL.Normativa.BD.Secuencias::TRI.IdCertificadoCliente".NEXTVAL AS "IdCertificadoCliente"
											 ,:v_ultimo_folio + "Numero" AS "Folio"
											 ,"IdCliente"
											 ,"IdPortafolio"
											 ,"IdTipoDocumento"
											 ,"IdEmpresa"
									   		 ,"Version"
									   		 ,"IdFondo"
									  FROM (SELECT ROW_NUMBER() OVER(ORDER BY DATOS."IdPortafolio") AS "Numero"
											,DATOS."IdCliente"
											,DATOS."IdPortafolio"
											,DATOS."IdTipoDocumento"
											,DATOS."IdEmpresa"
									   		,DATOS."Version"
									   		,DATOS."IdFondo"
											FROM :v_datos_certificado DATOS
											WHERE NOT EXISTS (
												SELECT 1 FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO 
												INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N31" DETALLE ON (DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente") 
												WHERE CERTIFICADO."IdCliente" = DATOS."IdCliente"
												AND CERTIFICADO."IdPortafolio" = DATOS."IdPortafolio"
												AND CERTIFICADO."Esquema" = 'DJ1894'
												AND CERTIFICADO."Version" = DATOS."Version"
												AND CERTIFICADO."IdEmpresa" = DATOS."IdEmpresa"
												AND DETALLE."IdFondo" = DATOS."IdFondo"
											));
			
			
			
			v_detalle_sin_certificado = SELECT  "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1894N31".NEXTVAL AS "IdDetalleCertificadoClienteDJ1894N31"
												,CERTIFICADO."IdCertificadoCliente"
												,DATOS."IdFondo"
												,DATOS."IdEmisor"
												,DATOS."IdFondoOrigen"
												,DATOS."IdEmisorOrigen"
												,DATOS."MontoLiquidacion"
												,DATOS."MayorValorUf"
												,DATOS."PorcentajePromedioAnualAcciones"
												,DATOS."PorcentajePromedioAnualAccionesProvenienteReinversion"
												,DATOS."FechaCertificacion"
												,DATOS."FechaInversionOriginal"
										FROM :v_datos DATOS
										INNER JOIN :v_datos_sin_certificado CERTIFICADO ON (DATOS."IdCliente" = CERTIFICADO."IdCliente"
																							AND DATOS."IdPortafolio" = CERTIFICADO."IdPortafolio"
																							AND DATOS."IdFondo" = CERTIFICADO."IdFondo"
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
											 ,DATOS."IdFondo"
										FROM :v_datos_certificado DATOS
										INNER JOIN "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO ON (CERTIFICADO."IdCliente" = DATOS."IdCliente"
																												AND CERTIFICADO."IdPortafolio" = DATOS."IdPortafolio"
																												AND CERTIFICADO."Esquema" = 'DJ1894'
																												AND CERTIFICADO."Version" = DATOS."Version"
																												AND CERTIFICADO."IdEmpresa" = DATOS."IdEmpresa") 
										WHERE EXISTS (
											SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N31" DETALLE WHERE DETALLE."IdFondo" = DATOS."IdFondo" AND CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente"
										);
			
			v_detalle_con_certificado = SELECT 	"BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1894N31".NEXTVAL AS "IdDetalleCertificadoClienteDJ1894N31"
												,CERTIFICADO."IdCertificadoCliente"
												,DATOS."IdFondo"
												,DATOS."IdEmisor"
												,DATOS."IdFondoOrigen"
												,DATOS."IdEmisorOrigen"
												,DATOS."MontoLiquidacion"
												,DATOS."MayorValorUf"
												,DATOS."PorcentajePromedioAnualAcciones"
												,DATOS."PorcentajePromedioAnualAccionesProvenienteReinversion"
												,DATOS."FechaCertificacion"
												,DATOS."FechaInversionOriginal"
										FROM :v_datos DATOS
										INNER JOIN :v_datos_con_certificado CERTIFICADO  ON (DATOS."IdCliente" = CERTIFICADO."IdCliente"
																							AND DATOS."IdPortafolio" = CERTIFICADO."IdPortafolio"
																							AND DATOS."IdFondo" = CERTIFICADO."IdFondo"
																							AND DATOS."IdEmpresa" = CERTIFICADO."IdEmpresa"
																							AND DATOS."Version" = CERTIFICADO."Version"
																							AND DATOS."IdTipoDocumento" = CERTIFICADO."IdTipoDocumento"
																							);

																								
			INSERT INTO "BSCL.Normativa.BD::TRI.CertificadoCliente"
			("IdCertificadoCliente","IdTipoDocumento","IdEmpresa","Esquema","Version","Anio","Periodo","IdCliente","IdPortafolio","Folio")
			SELECT 
			 "IdCertificadoCliente","IdTipoDocumento","IdEmpresa",'DJ1894',"Version","Version","Version","IdCliente","IdPortafolio","Folio"	
			FROM :v_datos_sin_certificado;
			
			
			INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N31"
			("IdDetalleCertificadoClienteDJ1894N31","IdCertificadoCliente","IdFondo","IdEmisor","IdFondoOrigen","IdEmisorOrigen","MontoLiquidacion","MayorValorUf","PorcentajePromedioAnualAcciones"
			,"PorcentajePromedioAnualAccionesProvenienteReinversion","FechaCertificacion","FechaInversionOriginal")
			SELECT "IdDetalleCertificadoClienteDJ1894N31","IdCertificadoCliente","IdFondo","IdEmisor","IdFondoOrigen","IdEmisorOrigen","MontoLiquidacion","MayorValorUf","PorcentajePromedioAnualAcciones"
			,"PorcentajePromedioAnualAccionesProvenienteReinversion","FechaCertificacion","FechaInversionOriginal"
			FROM :v_detalle_sin_certificado;
			
			DELETE FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N31" DETALLE WHERE 
			EXISTS(SELECT 1 FROM :v_detalle_con_certificado CERTIFICADO WHERE DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente");
			
			INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N31"
			("IdDetalleCertificadoClienteDJ1894N31","IdCertificadoCliente","IdFondo","IdEmisor","IdFondoOrigen","IdEmisorOrigen","MontoLiquidacion","MayorValorUf","PorcentajePromedioAnualAcciones"
			,"PorcentajePromedioAnualAccionesProvenienteReinversion","FechaCertificacion","FechaInversionOriginal")
			SELECT "IdDetalleCertificadoClienteDJ1894N31","IdCertificadoCliente","IdFondo","IdEmisor","IdFondoOrigen","IdEmisorOrigen","MontoLiquidacion","MayorValorUf","PorcentajePromedioAnualAcciones"
			,"PorcentajePromedioAnualAccionesProvenienteReinversion","FechaCertificacion","FechaInversionOriginal"
			FROM :v_detalle_con_certificado;
			
			
			SELECT COUNT(1) into count_certificados
			FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
			INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N31" DETALLE ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")  
			WHERE "Esquema" = 'DJ1894' AND "Version" = :p_Version;
			
			IF count_certificados > 0 THEN
				SELECT MAX("Folio") into v_max_folio  
				FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
				INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N31" DETALLE ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")  
				WHERE "Esquema" = 'DJ1894' AND "Version" = :p_Version;
			
			
				UPDATE "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
				SET "UltimoFolio" = v_max_folio
				WHERE "Esquema" = 'DJ1894' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_31';
			END IF;
			
			update "BSCL.Normativa.BD::TRI.ControlProceso" 
			set "Carga" = 'S', "Validacion" = 'S', 
			    "PreDeclaracion" = 'S', 
			    "Certificacion" = 'S', "EstadoCertificado" = 'GEN'
			WHERE "Esquema"= 'DJ1894' AND "Version" = :p_Version;
			
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
						,'CERTIFICADO_31' AS "IdTipoRegistro"
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
		
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1894.GenerarCertificado31','FIN','FIN');
		
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;	
END;