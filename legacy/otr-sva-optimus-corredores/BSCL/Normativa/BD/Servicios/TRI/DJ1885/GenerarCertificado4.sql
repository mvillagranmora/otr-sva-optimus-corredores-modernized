SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1885.GenerarCertificado4";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1885.GenerarCertificado4"(
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
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1885.GenerarCertificado4','INICIO','INICIO');
				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			SELECT COUNT(1) into v_count_folio
			FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			WHERE "Esquema" = 'DJ1885' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_4';
			
			IF v_count_folio = 0 THEN
				INSERT INTO "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
				("Esquema","Version","UltimoFolio","CodigoCertificado","NombreCertificado","CodigoPrograma")
				VALUES('DJ1885',:p_Version,0,'CERTIFICADO_4','Certificado 4','GenerarCertificado4');
			END IF;
			
			SELECT "UltimoFolio" into v_ultimo_folio
			FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			WHERE "Esquema" = 'DJ1885' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_4';
			
			v_datos = SELECT DJ1885."IdCliente"
							 ,DOCUMENTO."IdTipoDocumento"
							 ,DJ1885."IdPortafolio"
							 ,DJ1885."FechaPago"
							 ,DJ1885."SecuenciaEvento"
							 ,DJ1885."MontoPagado"
							 ,DJ1885."DividendosAfectos"
							 ,DJ1885."DividendosExentos"
							 ,DJ1885."DividendosNoRentaNoAcogidos"
							 ,DJ1885."DividendosNoRentaAcogidos"
							 ,DJ1885."DevolucionCapital"
							 ,DJ1885."IncrementoImpuesto"
							 ,DJ1885."CreditosAfectosConDevolucion"
							 ,DJ1885."CreditosAfectosSinDevolucion"
							 ,DJ1885."CreditosExentosConDevolucion"
							 ,DJ1885."CreditosExentosSinDevolucion"
							 ,DJ1885."CreditosImpuestosExternos"
							 ,DJ1885."CreditosImpuestoTasaAdicional"
							 ,DJ1885."RazonSocial"
							 ,DJ1885."RutSociedad"
							 ,TO_CHAR(DJ1885."FechaPago",'YYYY-MM') AS "AgnoMesPago"
							 ,:p_IdEmpresa AS "IdEmpresa"
							 ,:p_Version AS "Version"
					 FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1885.Datos" DJ1885
					 INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" DOCUMENTO ON (DJ1885."IdDocumento" = DOCUMENTO."IdDocumento")
					 LEFT OUTER JOIN :p_Clientes CLIENTES ON (CLIENTES."IdCliente" = DJ1885."IdCliente")
					 WHERE DOCUMENTO."Version" = :p_Version AND (:p_Todos = 'S' OR (:p_Todos = 'N' AND COALESCE(CLIENTES."IdCliente",0) != 0));
			
			v_datos_maestro = 	SELECT 	DJ1885."IdCliente"
									 	,DJ1885."IdTipoDocumento"
									 	,DJ1885."IdPortafolio"
									 	,DJ1885."FechaPago"
									 	,DJ1885."SecuenciaEvento"
									 	,DJ1885."MontoPagado"
									 	,DJ1885."DividendosAfectos"
									 	,DJ1885."DividendosExentos"
									 	,DJ1885."DividendosNoRentaNoAcogidos"
									 	,DJ1885."DividendosNoRentaAcogidos"
									 	,DJ1885."DevolucionCapital"
									 	,DJ1885."IncrementoImpuesto"
									 	,DJ1885."CreditosAfectosConDevolucion"
									 	,DJ1885."CreditosAfectosSinDevolucion"
									 	,DJ1885."CreditosExentosConDevolucion"
									 	,DJ1885."CreditosExentosSinDevolucion"
									 	,DJ1885."CreditosImpuestosExternos"
									 	,DJ1885."CreditosImpuestoTasaAdicional"
									 	,DJ1885."RazonSocial"
									 	,DJ1885."RutSociedad"
									 	,DJ1885."IdEmpresa"
									 	,DJ1885."Version"
									 	,DJ1885."AgnoMesPago"
									 	,FACTOR."Factor"
										,FACTOR."DescMes"
								FROM :v_datos DJ1885
								INNER JOIN "BSCL.Normativa.BD.Vistas::TRI.ConsultarFactorIpc" FACTOR ON (DJ1885."AgnoMesPago" = FACTOR."AgnoMes");
			
			v_datos_actualizados = SELECT 	DJ1885."IdCliente"
									 	,DJ1885."IdTipoDocumento"
									 	,DJ1885."IdPortafolio"
									 	,DJ1885."FechaPago"
									 	,DJ1885."SecuenciaEvento"
									 	,DJ1885."MontoPagado" * "Factor" AS "MontoPagado"
									 	,DJ1885."DividendosAfectos" * "Factor" AS "DividendosAfectos"
									 	,DJ1885."DividendosExentos" * "Factor" AS "DividendosExentos"
									 	,DJ1885."DividendosNoRentaNoAcogidos" * "Factor" AS "DividendosNoRentaNoAcogidos"
									 	,DJ1885."DividendosNoRentaAcogidos" * "Factor" AS "DividendosNoRentaAcogidos"
									 	,DJ1885."DevolucionCapital" * "Factor" AS "DevolucionCapital"
									 	,DJ1885."IncrementoImpuesto" * "Factor" AS "IncrementoImpuesto"
									 	,DJ1885."CreditosAfectosConDevolucion" * "Factor" AS "CreditosAfectosConDevolucion"
									 	,DJ1885."CreditosAfectosSinDevolucion" * "Factor" AS "CreditosAfectosSinDevolucion"
									 	,DJ1885."CreditosExentosConDevolucion" * "Factor" AS "CreditosExentosConDevolucion"
									 	,DJ1885."CreditosExentosSinDevolucion" * "Factor" AS "CreditosExentosSinDevolucion"
									 	,DJ1885."CreditosImpuestosExternos" * "Factor" AS "CreditosImpuestosExternos"
									 	,DJ1885."CreditosImpuestoTasaAdicional" * "Factor" AS "CreditosImpuestoTasaAdicional"
									 	,DJ1885."RazonSocial"
									 	,DJ1885."RutSociedad"
									 	,DJ1885."IdEmpresa"
									 	,DJ1885."Version"
									 	,DJ1885."AgnoMesPago"
								FROM :v_datos_maestro DJ1885;
			
			v_datos_certificado = SELECT DISTINCT "IdCliente","IdPortafolio","IdEmpresa","Version","IdTipoDocumento" FROM :v_datos_actualizados;
			
			v_datos_sin_certificado = SELECT "BSCL.Normativa.BD.Secuencias::TRI.IdCertificadoCliente".NEXTVAL AS "IdCertificadoCliente"
											 ,:v_ultimo_folio + "Numero" AS "Folio"
											 ,"IdCliente"
											 ,"IdPortafolio"
											 ,"IdTipoDocumento"
											 ,"IdEmpresa"
									   		 ,"Version"
									   FROM (SELECT ROW_NUMBER() OVER(ORDER BY DATOS."IdPortafolio") AS "Numero"
											,DATOS."IdCliente"
											,DATOS."IdPortafolio"
											,DATOS."IdTipoDocumento"
											,DATOS."IdEmpresa"
									   		,DATOS."Version"
											FROM :v_datos_certificado DATOS
											WHERE NOT EXISTS (
												SELECT 1 FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO 
												INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1885N4" DETALLE ON (DETALLE."IdCertificadoCliente" = CERTIFICADO."IdCertificadoCliente") 
												WHERE CERTIFICADO."IdCliente" = DATOS."IdCliente"
												AND CERTIFICADO."IdPortafolio" = DATOS."IdPortafolio"
												AND CERTIFICADO."Esquema" = 'DJ1885'
												AND CERTIFICADO."Version" = DATOS."Version"
												AND CERTIFICADO."IdEmpresa" = DATOS."IdEmpresa"
											));
			
			v_detalle_sin_certificado = SELECT  "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1885N4".NEXTVAL AS "IdDetalleCertificadoClienteDJ1885N4"
												,CERTIFICADO."IdCertificadoCliente"
												 ,DATOS."SecuenciaEvento"
												 ,DATOS."FechaPago"
												 ,DATOS."MontoPagado"
												 ,DATOS."DividendosAfectos"
												 ,DATOS."DividendosExentos"
												 ,DATOS."DividendosNoRentaNoAcogidos"
												 ,DATOS."DividendosNoRentaAcogidos"
												 ,DATOS."DevolucionCapital"
												 ,DATOS."IncrementoImpuesto"
												 ,DATOS."CreditosAfectosConDevolucion"
												 ,DATOS."CreditosAfectosSinDevolucion"
												 ,DATOS."CreditosExentosConDevolucion"
												 ,DATOS."CreditosExentosSinDevolucion"
												 ,DATOS."CreditosImpuestosExternos"
												 ,DATOS."CreditosImpuestoTasaAdicional"
												 ,DATOS."RazonSocial"
												 ,DATOS."RutSociedad"
										FROM :v_datos_actualizados DATOS
										INNER JOIN :v_datos_sin_certificado CERTIFICADO ON (DATOS."IdCliente" = CERTIFICADO."IdCliente"
																							AND DATOS."IdPortafolio" = CERTIFICADO."IdPortafolio"
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
										FROM :v_datos_certificado DATOS
										INNER JOIN "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO ON (CERTIFICADO."IdCliente" = DATOS."IdCliente"
																												AND CERTIFICADO."IdPortafolio" = DATOS."IdPortafolio"
																												AND CERTIFICADO."Esquema" = 'DJ1885'
																												AND CERTIFICADO."Version" = DATOS."Version"
																												AND CERTIFICADO."IdEmpresa" = DATOS."IdEmpresa") 
										WHERE EXISTS (
											SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1885N4" DETALLE WHERE CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente"
										);
										
			v_detalle_con_certificado = SELECT 	DETALLE."IdDetalleCertificadoClienteDJ1885N4"
												,CERTIFICADO."IdCertificadoCliente"
												,DATOS."SecuenciaEvento"
												,DATOS."FechaPago"
												,DATOS."MontoPagado"
												,DATOS."DividendosAfectos"
												,DATOS."DividendosExentos"
												,DATOS."DividendosNoRentaNoAcogidos"
												,DATOS."DividendosNoRentaAcogidos"
												,DATOS."DevolucionCapital"
												,DATOS."IncrementoImpuesto"
												,DATOS."CreditosAfectosConDevolucion"
												,DATOS."CreditosAfectosSinDevolucion"
												,DATOS."CreditosExentosConDevolucion"
												,DATOS."CreditosExentosSinDevolucion"
												,DATOS."CreditosImpuestosExternos"
												,DATOS."CreditosImpuestoTasaAdicional"
												,DATOS."RazonSocial"
												,DATOS."RutSociedad"
										FROM :v_datos_actualizados DATOS
										INNER JOIN :v_datos_con_certificado CERTIFICADO  ON (DATOS."IdCliente" = CERTIFICADO."IdCliente"
																							AND DATOS."IdPortafolio" = CERTIFICADO."IdPortafolio"
																							AND DATOS."IdEmpresa" = CERTIFICADO."IdEmpresa"
																							AND DATOS."Version" = CERTIFICADO."Version"
																							AND DATOS."IdTipoDocumento" = CERTIFICADO."IdTipoDocumento"
																							)
										INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1885N4" DETALLE ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente" AND DATOS."SecuenciaEvento" = DETALLE."NumeroDividendo");
										
			v_certificado_sin_detalle = SELECT 	"BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1885N4".NEXTVAL AS "IdDetalleCertificadoClienteDJ1885N4"
												,CERTIFICADO."IdCertificadoCliente"
												,DATOS."SecuenciaEvento"
												,DATOS."FechaPago"
												,DATOS."MontoPagado"
												,DATOS."DividendosAfectos"
												,DATOS."DividendosExentos"
												,DATOS."DividendosNoRentaNoAcogidos"
												,DATOS."DividendosNoRentaAcogidos"
												,DATOS."DevolucionCapital"
												,DATOS."IncrementoImpuesto"
												,DATOS."CreditosAfectosConDevolucion"
												,DATOS."CreditosAfectosSinDevolucion"
												,DATOS."CreditosExentosConDevolucion"
												,DATOS."CreditosExentosSinDevolucion"
												,DATOS."CreditosImpuestosExternos"
												,DATOS."CreditosImpuestoTasaAdicional"
												,DATOS."RazonSocial"
												,DATOS."RutSociedad"
										FROM :v_datos_actualizados DATOS
										INNER JOIN :v_datos_con_certificado CERTIFICADO  ON (DATOS."IdCliente" = CERTIFICADO."IdCliente"
																							AND DATOS."IdPortafolio" = CERTIFICADO."IdPortafolio"
																							AND DATOS."IdEmpresa" = CERTIFICADO."IdEmpresa"
																							AND DATOS."Version" = CERTIFICADO."Version"
																							AND DATOS."IdTipoDocumento" = CERTIFICADO."IdTipoDocumento"
																							)
										WHERE 
										NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1885N4" DETALLE WHERE CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente" AND DATOS."SecuenciaEvento" = DETALLE."NumeroDividendo");
															
				
			
			INSERT INTO "BSCL.Normativa.BD::TRI.CertificadoCliente"
			("IdCertificadoCliente","IdTipoDocumento","IdEmpresa","Esquema","Version","Anio","Periodo","IdCliente","IdPortafolio","Folio")
			SELECT 
			 "IdCertificadoCliente","IdTipoDocumento","IdEmpresa",'DJ1885',"Version","Version","Version","IdCliente","IdPortafolio","Folio"	
			FROM :v_datos_sin_certificado;

			INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1885N4"
			("IdDetalleCertificadoClienteDJ1885N4","IdCertificadoCliente","NumeroDividendo","FechaPago","MontoTotal","MontoAfectoGlobal","MontoExentoGlobal","MontoConstitutivoNoAcogido",		
			"MontoConstitutivoAcogido","DevolucionCapital","IncrementoImpuestoPrimeraCategoria","ImptAfectosConDerecho","ImptAfectosSinDerecho","ImptExentosConDerecho",				
			"ImptExentosSinDerecho","CreditoImpuestoExterno","ImptTasaAdicionales","RazonSocial","RutSociedad")
			SELECT "IdDetalleCertificadoClienteDJ1885N4","IdCertificadoCliente","SecuenciaEvento","FechaPago","MontoPagado","DividendosAfectos","DividendosExentos","DividendosNoRentaNoAcogidos",
			"DividendosNoRentaAcogidos","DevolucionCapital","IncrementoImpuesto","CreditosAfectosConDevolucion","CreditosAfectosSinDevolucion","CreditosExentosConDevolucion",
			"CreditosExentosSinDevolucion","CreditosImpuestosExternos","CreditosImpuestoTasaAdicional",COALESCE("RazonSocial",''),COALESCE("RutSociedad",'')
			FROM :v_detalle_sin_certificado;
			
			INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1885N4"
			("IdDetalleCertificadoClienteDJ1885N4","IdCertificadoCliente","NumeroDividendo","FechaPago","MontoTotal","MontoAfectoGlobal","MontoExentoGlobal","MontoConstitutivoNoAcogido",		
			"MontoConstitutivoAcogido","DevolucionCapital","IncrementoImpuestoPrimeraCategoria","ImptAfectosConDerecho","ImptAfectosSinDerecho","ImptExentosConDerecho",				
			"ImptExentosSinDerecho","CreditoImpuestoExterno","ImptTasaAdicionales","RazonSocial","RutSociedad")
			SELECT "IdDetalleCertificadoClienteDJ1885N4","IdCertificadoCliente","SecuenciaEvento","FechaPago","MontoPagado","DividendosAfectos","DividendosExentos","DividendosNoRentaNoAcogidos",
			"DividendosNoRentaAcogidos","DevolucionCapital","IncrementoImpuesto","CreditosAfectosConDevolucion","CreditosAfectosSinDevolucion","CreditosExentosConDevolucion",
			"CreditosExentosSinDevolucion","CreditosImpuestosExternos","CreditosImpuestoTasaAdicional",COALESCE("RazonSocial",''),COALESCE("RutSociedad",'')
			FROM :v_certificado_sin_detalle;
			
			
			UPDATE DETALLE_CERTIFICADO
			SET "NumeroDividendo"= DETALLE."SecuenciaEvento",
				"FechaPago" = DETALLE."FechaPago",
				"MontoTotal" = DETALLE."MontoPagado",
				"MontoAfectoGlobal" = DETALLE."DividendosAfectos",
				"MontoExentoGlobal" = DETALLE."DividendosExentos",
				"MontoConstitutivoNoAcogido" = DETALLE."DividendosNoRentaNoAcogidos",
				"MontoConstitutivoAcogido" = DETALLE."DividendosNoRentaAcogidos",
				"DevolucionCapital" = DETALLE."DevolucionCapital",
				"IncrementoImpuestoPrimeraCategoria" = DETALLE."IncrementoImpuesto",
				"ImptAfectosConDerecho" = DETALLE."CreditosAfectosConDevolucion",
				"ImptAfectosSinDerecho" = DETALLE."CreditosAfectosSinDevolucion",
				"ImptExentosConDerecho" = DETALLE."CreditosExentosConDevolucion",
				"ImptExentosSinDerecho" = DETALLE."CreditosExentosSinDevolucion",
				"CreditoImpuestoExterno" = DETALLE."CreditosImpuestosExternos",
				"ImptTasaAdicionales" = DETALLE."CreditosImpuestoTasaAdicional",
				"RazonSocial" = COALESCE(DETALLE."RazonSocial",''),
				"RutSociedad" = COALESCE(DETALLE."RutSociedad",'')
			FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1885N4" DETALLE_CERTIFICADO
			INNER JOIN :v_detalle_con_certificado DETALLE ON (DETALLE."IdDetalleCertificadoClienteDJ1885N4" = DETALLE_CERTIFICADO."IdDetalleCertificadoClienteDJ1885N4");
			
			SELECT COUNT(1) into count_certificados
			FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
			INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1885N4" DETALLE ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")  
			WHERE "Esquema" = 'DJ1885' AND "Version" = :p_Version;
			
			IF count_certificados > 0 THEN
				SELECT MAX("Folio") into v_max_folio  
				FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
				INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1885N4" DETALLE ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")  
				WHERE "Esquema" = 'DJ1885' AND "Version" = :p_Version;
			
				UPDATE "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
				SET "UltimoFolio" = v_max_folio
				WHERE "Esquema" = 'DJ1885' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_4';
				
			END IF;
			
			UPDATE "BSCL.Normativa.BD::TRI.ControlProceso" 
			SET "Carga" = 'S', "Validacion" = 'S', 
			    "PreDeclaracion" = 'S', 
			    "Certificacion" = 'S', "EstadoCertificado" = 'GEN'
			WHERE "Esquema"= 'DJ1885' AND "Version" = :p_Version;
			
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
						,'CERTIFICADO_4' AS "IdTipoRegistro"
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
		
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1885.GenerarCertificado4','FIN','FIN');
		
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;	
END;