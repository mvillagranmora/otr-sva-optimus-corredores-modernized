SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1890.GenerarDocumentoCertificado7";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1890.GenerarDocumentoCertificado7"(
	-- ID empresa del login.
    IN p_IdEmpresa 						BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 						BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo						NVARCHAR(3)	 ,
    
    IN p_Version						NVARCHAR(10) ,
    
    IN p_Todos							NVARCHAR(1) DEFAULT 'S',
    
    IN p_IdCliente						BIGINT		,
    
    IN p_IdPortafolio					BIGINT		,
    
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
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1894.GenerarCertificado21','INICIO','INICIO');
				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			v_meses = SELECT 1 "NUMERO",'ENERO' AS "MES", '01' as "NUMEROMES" FROM DUMMY
					  UNION ALL SELECT 2 "NUMERO",'FEBRERO' AS "MES", '02' as "NUMEROMES" FROM DUMMY
					  UNION ALL SELECT 3 "NUMERO",'MARZO' AS "MES", '03' as "NUMEROMES" FROM DUMMY
					  UNION ALL SELECT 4 "NUMERO",'ABRIL' AS "MES", '04' as "NUMEROMES" FROM DUMMY
					  UNION ALL SELECT 5 "NUMERO",'MAYO' AS "MES", '05' as "NUMEROMES" FROM DUMMY
					  UNION ALL SELECT 6 "NUMERO",'JUNIO' AS "MES", '06' as "NUMEROMES" FROM DUMMY
					  UNION ALL SELECT 7 "NUMERO",'JULIO' AS "MES", '07' as "NUMEROMES" FROM DUMMY
					  UNION ALL SELECT 8 "NUMERO",'AGOSTO' AS "MES", '08' as "NUMEROMES" FROM DUMMY
					  UNION ALL SELECT 9 "NUMERO",'SEPTIEMBRE' AS "MES", '09' as "NUMEROMES" FROM DUMMY
					  UNION ALL SELECT 10 "NUMERO",'OCTUBRE' AS "MES", '10' as "NUMEROMES" FROM DUMMY
					  UNION ALL SELECT 11 "NUMERO",'NOVIEMBRE' AS "MES", '11' as "NUMEROMES" FROM DUMMY
					  UNION ALL SELECT 12 "NUMERO",'DICIEMBRE' AS "MES", '12' as "NUMEROMES" FROM DUMMY;
			
			v_Resultado = SELECT  CERTIFICADO."Folio"
									 ,CERTIFICADO."IdEmpresa"				
									 ,CERTIFICADO."Esquema"				
									 ,CERTIFICADO."Version"				
									 ,CERTIFICADO."Anio"					
									 ,CERTIFICADO."Periodo"				
									 ,CERTIFICADO."IdCliente"				
									 ,CERTIFICADO."IdPortafolio"	
									 ,DETALLE."IdCertificadoCliente"
									 ,DETALLE."NumeroDocumento"
									 ,DETALLE."RutInversionista"
									 ,DETALLE."FechaOperacion"
									 ,DETALLE."FechaVencimiento"
									 ,DETALLE."TipoRegistro"
									 ,DETALLE."DescMes"
									 ,DETALLE."MontoOperacionCaptacion"
									 ,DETALLE."MontoPercibidoFechaVencimiento"
									 ,DETALLE."InteresesPagados"
									 ,0 AS "InteresesRealesPositivosNegativos"
									 ,0 AS "InteresesRealesDepositosPositivo"
									 ,0 AS "InteresesRealesDepositosNegativo"
									 ,0 AS "InteresRealesMantencionPositivo"
									 ,0 AS "InteresRealesMantencionNegativo"
									 ,0 AS "InteresesRealesOperacionesPositivo"
									 ,0 AS "InteresesRealesOperacionesNegativo"
									 ,DETALLE."FactorActualizacion"
									 ,0 AS "InteresesRealesDepositosActualizadoPositivo"
									 ,0 AS "InteresesRealesDepositosActualizadoNegativo"
									 ,0 AS "InteresRealesMantencionActualizadoPositivo"
									 ,0 AS "InteresRealesMantencionActualizadoNegativo"
									 ,0 AS "InteresesRealesOperacionesActualizadoPositivo"
									 ,0 AS "InteresesRealesOperacionesActualizadoNegativo"
									 ,0 AS "Numero"
									 ,CLIENTE."NombreCompleto" AS "NombreCompletoCliente"
									 ,CLIENTE."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
									 ,EMPRESA."RazonSocial" AS "RazonSocialEmpresa"
									 ,EMPRESA."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
									 ,EMPRESA."DireccionFisicaPrincipalCalle" || ' ' || EMPRESA."DireccionFisicaPrincipalNumero" || ',' || EMPRESA."DireccionFisicaPrincipalSector" || ' ' || EMPRESA."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa"
									 ,EMPRESA."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
								FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
								INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890" DETALLE	ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")
								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (CLIENTE."IdCliente" = CERTIFICADO."IdCliente")
								INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA ON (EMPRESA."IdEmpresa" = CERTIFICADO."IdEmpresa")
								WHERE DETALLE."TipoRegistro" = 'CAB'
								AND p_Todos = 'S' OR (CERTIFICADO."IdCliente" = p_IdCliente AND CERTIFICADO."IdPortafolio" = p_IdPortafolio AND CERTIFICADO."Folio" = p_Folio)
								AND CERTIFICADO."Version" = p_Version;
								
			v_Resultado_Mensuales = SELECT  CERTIFICADO."Folio"
									 ,CERTIFICADO."IdEmpresa"				
									 ,CERTIFICADO."Esquema"				
									 ,CERTIFICADO."Version"				
									 ,CERTIFICADO."Anio"					
									 ,CERTIFICADO."Periodo"				
									 ,CERTIFICADO."IdCliente"				
									 ,CERTIFICADO."IdPortafolio"	
									 ,DETALLE."IdCertificadoCliente"
									 ,DETALLE."NumeroDocumento"
									 ,DETALLE."RutInversionista"
									 ,DETALLE."FechaOperacion"
									 ,DETALLE."FechaVencimiento"
									 ,DETALLE."TipoRegistro"
									 ,MESES."MES" AS "DescMes"
									 ,DETALLE."MontoOperacionCaptacion"
									 ,DETALLE."MontoPercibidoFechaVencimiento"
									 ,DETALLE."InteresesPagados"
									 ,DETALLE."InteresesRealesPositivosNegativos"
									 ,DETALLE."InteresesRealesDepositosPositivo"
									 ,DETALLE."InteresesRealesDepositosNegativo"
									 ,DETALLE."InteresRealesMantencionPositivo"
									 ,DETALLE."InteresRealesMantencionNegativo"
									 ,DETALLE."InteresesRealesOperacionesPositivo"
									 ,DETALLE."InteresesRealesOperacionesNegativo"
									 ,DETALLE."FactorActualizacion"
									 ,DETALLE."InteresesRealesDepositosActualizadoPositivo"
									 ,DETALLE."InteresesRealesDepositosActualizadoNegativo"
									 ,DETALLE."InteresRealesMantencionActualizadoPositivo"
									 ,DETALLE."InteresRealesMantencionActualizadoNegativo"
									 ,DETALLE."InteresesRealesOperacionesActualizadoPositivo"
									 ,DETALLE."InteresesRealesOperacionesActualizadoNegativo"
									 ,MESES."NUMERO" AS "Numero"
									 ,CLIENTE."NombreCompleto" AS "NombreCompletoCliente"
									 ,CLIENTE."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
									 ,EMPRESA."RazonSocial" AS "RazonSocialEmpresa"
									 ,EMPRESA."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
									 ,EMPRESA."DireccionFisicaPrincipalCalle" || ' ' || EMPRESA."DireccionFisicaPrincipalNumero" || ',' || EMPRESA."DireccionFisicaPrincipalSector" || ' ' || EMPRESA."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa"
									 ,EMPRESA."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
								FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
								INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificado7DJ1890" DETALLE	ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")
								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (CLIENTE."IdCliente" = CERTIFICADO."IdCliente")
								INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA ON (EMPRESA."IdEmpresa" = CERTIFICADO."IdEmpresa")
								INNER JOIN :v_meses MESES ON (MESES."NUMEROMES" = TO_CHAR(TO_DATE("DescMes",'YYYY-MM'),'MM'))
								WHERE DETALLE."TipoRegistro" = 'DET'
								AND p_Todos = 'S' OR (CERTIFICADO."IdCliente" = p_IdCliente AND CERTIFICADO."IdPortafolio" = p_IdPortafolio AND CERTIFICADO."Folio" = p_Folio)
								AND CERTIFICADO."Version" = p_Version;
			
			v_datos = SELECT DISTINCT CERTIFICADO."Folio"
									 ,CERTIFICADO."IdEmpresa"				
									 ,CERTIFICADO."Esquema"				
									 ,CERTIFICADO."Version"				
									 ,CERTIFICADO."Anio"					
									 ,CERTIFICADO."Periodo"				
									 ,CERTIFICADO."IdCliente"				
									 ,CERTIFICADO."IdPortafolio"							
									 ,CERTIFICADO."RutInversionista"		
									 ,CERTIFICADO."IdCertificadoCliente"
									 ,CERTIFICADO."TipoRegistro"
									 ,CERTIFICADO."NombreCompletoCliente"
									 ,CERTIFICADO."DocumentoIdentidadCliente"
									 ,CERTIFICADO."RazonSocialEmpresa"
									 ,CERTIFICADO."DocumentoIdentidadEmpresa"
									 ,CERTIFICADO."DireccionEmpresa"
									 ,CERTIFICADO."CiudadFecha"
						FROM :v_Resultado_Mensuales AS CERTIFICADO;
			
			v_meses_sin_datos = SELECT CERTIFICADO."Folio"
									 ,CERTIFICADO."IdEmpresa"				
									 ,CERTIFICADO."Esquema"				
									 ,CERTIFICADO."Version"				
									 ,CERTIFICADO."Anio"					
									 ,CERTIFICADO."Periodo"				
									 ,CERTIFICADO."IdCliente"				
									 ,CERTIFICADO."IdPortafolio"							
									 ,CERTIFICADO."RutInversionista"		
									 ,CERTIFICADO."IdCertificadoCliente"
									 ,0 AS "NumeroDocumento"
									 ,TO_DATE('1900-01-01') AS "FechaOperacion"
									 ,TO_DATE('1900-01-01') AS "FechaVencimiento"
									 ,CERTIFICADO."TipoRegistro"
									 ,0 AS "MontoOperacionCaptacion"
									 ,0 AS "MontoPercibidoFechaVencimiento"
									 ,0 AS "InteresesPagados"
									 ,0 AS "InteresesRealesPositivosNegativos"
									 ,0 AS "InteresesRealesDepositosPositivo"
									 ,0 AS "InteresesRealesDepositosNegativo"
									 ,0 AS "InteresRealesMantencionPositivo"
									 ,0 AS "InteresRealesMantencionNegativo"
									 ,0 AS "InteresesRealesOperacionesPositivo"
									 ,0 AS "InteresesRealesOperacionesNegativo"
									 ,0 AS "FactorActualizacion"
									 ,0 AS "InteresesRealesDepositosActualizadoPositivo"
									 ,0 AS "InteresesRealesDepositosActualizadoNegativo"
									 ,0 AS "InteresRealesMantencionActualizadoPositivo"
									 ,0 AS "InteresRealesMantencionActualizadoNegativo"
									 ,0 AS "InteresesRealesOperacionesActualizadoPositivo"
									 ,0 AS "InteresesRealesOperacionesActualizadoNegativo"	
									 ,MES."MES" AS "DescMes"
									 ,MES."NUMERO" AS "Numero"
									 ,CERTIFICADO."NombreCompletoCliente"
									 ,CERTIFICADO."DocumentoIdentidadCliente"
									 ,CERTIFICADO."RazonSocialEmpresa"
									 ,CERTIFICADO."DocumentoIdentidadEmpresa"
									 ,CERTIFICADO."DireccionEmpresa"
									 ,CERTIFICADO."CiudadFecha"
								FROM :v_datos CERTIFICADO,:v_meses MES
								WHERE NOT EXISTS (SELECT 1 FROM :v_Resultado_Mensuales RESULTADO WHERE RESULTADO."Folio" = CERTIFICADO."Folio" AND MES."MES" = RESULTADO."DescMes");				

			v_Salida = SELECT "Folio"
							 ,"IdEmpresa"				
							 ,"Esquema"				
							 ,"Version"				
							 ,"Anio"					
							 ,"Periodo"				
							 ,"IdCliente"				
							 ,"IdPortafolio"							
							 ,"RutInversionista"		
							 ,"IdCertificadoCliente"
							 ,"NumeroDocumento"
							 ,"FechaOperacion"
							 ,"FechaVencimiento"
							 ,"TipoRegistro"
							 ,"MontoOperacionCaptacion"
							 ,"MontoPercibidoFechaVencimiento"
							 ,"InteresesPagados"
							 ,"InteresesRealesPositivosNegativos"
							 ,"InteresesRealesDepositosPositivo"
							 ,"InteresesRealesDepositosNegativo"
							 ,"InteresRealesMantencionPositivo"
							 ,"InteresRealesMantencionNegativo"
							 ,"InteresesRealesOperacionesPositivo"
							 ,"InteresesRealesOperacionesNegativo"
							 ,"FactorActualizacion"
							 ,"InteresesRealesDepositosActualizadoPositivo"
							 ,"InteresesRealesDepositosActualizadoNegativo"
							 ,"InteresRealesMantencionActualizadoPositivo"
							 ,"InteresRealesMantencionActualizadoNegativo"
							 ,"InteresesRealesOperacionesActualizadoPositivo"
							 ,"InteresesRealesOperacionesActualizadoNegativo"		
							 ,"DescMes"
							 ,"Numero"
							 ,"NombreCompletoCliente"
							 ,"DocumentoIdentidadCliente"
							 ,"RazonSocialEmpresa"
							 ,"DocumentoIdentidadEmpresa"
							 ,"DireccionEmpresa"
							 ,"CiudadFecha"
						FROM :v_Resultado
						UNION ALL
						SELECT "Folio"
							 ,"IdEmpresa"				
							 ,"Esquema"				
							 ,"Version"				
							 ,"Anio"					
							 ,"Periodo"				
							 ,"IdCliente"				
							 ,"IdPortafolio"							
							 ,"RutInversionista"		
							 ,"IdCertificadoCliente"
							 ,"NumeroDocumento"
							 ,"FechaOperacion"
							 ,"FechaVencimiento"
							 ,"TipoRegistro"
							 ,"MontoOperacionCaptacion"
							 ,"MontoPercibidoFechaVencimiento"
							 ,"InteresesPagados"
							 ,"InteresesRealesPositivosNegativos"
							 ,"InteresesRealesDepositosPositivo"
							 ,"InteresesRealesDepositosNegativo"
							 ,"InteresRealesMantencionPositivo"
							 ,"InteresRealesMantencionNegativo"
							 ,"InteresesRealesOperacionesPositivo"
							 ,"InteresesRealesOperacionesNegativo"
							 ,"FactorActualizacion"
							 ,"InteresesRealesDepositosActualizadoPositivo"
							 ,"InteresesRealesDepositosActualizadoNegativo"
							 ,"InteresRealesMantencionActualizadoPositivo"
							 ,"InteresRealesMantencionActualizadoNegativo"
							 ,"InteresesRealesOperacionesActualizadoPositivo"
							 ,"InteresesRealesOperacionesActualizadoNegativo"		
							 ,"DescMes"
							 ,"Numero"
							 ,"NombreCompletoCliente"
							 ,"DocumentoIdentidadCliente"
							 ,"RazonSocialEmpresa"
							 ,"DocumentoIdentidadEmpresa"
							 ,"DireccionEmpresa"
							 ,"CiudadFecha"
						FROM :v_Resultado_Mensuales
						UNION ALL
						SELECT "Folio"
							 ,"IdEmpresa"				
							 ,"Esquema"				
							 ,"Version"				
							 ,"Anio"					
							 ,"Periodo"				
							 ,"IdCliente"				
							 ,"IdPortafolio"							
							 ,"RutInversionista"		
							 ,"IdCertificadoCliente"
							 ,"NumeroDocumento"
							 ,"FechaOperacion"
							 ,"FechaVencimiento"
							 ,"TipoRegistro"
							 ,"MontoOperacionCaptacion"
							 ,"MontoPercibidoFechaVencimiento"
							 ,"InteresesPagados"
							 ,"InteresesRealesPositivosNegativos"
							 ,"InteresesRealesDepositosPositivo"
							 ,"InteresesRealesDepositosNegativo"
							 ,"InteresRealesMantencionPositivo"
							 ,"InteresRealesMantencionNegativo"
							 ,"InteresesRealesOperacionesPositivo"
							 ,"InteresesRealesOperacionesNegativo"
							 ,"FactorActualizacion"
							 ,"InteresesRealesDepositosActualizadoPositivo"
							 ,"InteresesRealesDepositosActualizadoNegativo"
							 ,"InteresRealesMantencionActualizadoPositivo"
							 ,"InteresRealesMantencionActualizadoNegativo"
							 ,"InteresesRealesOperacionesActualizadoPositivo"
							 ,"InteresesRealesOperacionesActualizadoNegativo"		
							 ,"DescMes"
							 ,"Numero"
							 ,"NombreCompletoCliente"
							 ,"DocumentoIdentidadCliente"
							 ,"RazonSocialEmpresa"
							 ,"DocumentoIdentidadEmpresa"
							 ,"DireccionEmpresa"
							 ,"CiudadFecha"
						FROM :v_meses_sin_datos;
			
			SELECT "Folio"
				 ,"IdEmpresa"				
				 ,Salida."Esquema"				
				 ,Salida."Version"				
				 ,"Anio"					
				 ,"Periodo"				
				 ,"IdCliente"				
				 ,"IdPortafolio"							
				 ,"RutInversionista"		
				 ,"IdCertificadoCliente"
				 ,"NumeroDocumento"
				 ,"FechaOperacion"
				 ,"FechaVencimiento"
				 ,"TipoRegistro"
				 ,"MontoOperacionCaptacion"
				 ,"MontoPercibidoFechaVencimiento"
				 ,"InteresesPagados"
				 ,"InteresesRealesPositivosNegativos"
				 ,"InteresesRealesDepositosPositivo"
				 ,"InteresesRealesDepositosNegativo"
				 ,"InteresRealesMantencionPositivo"
				 ,"InteresRealesMantencionNegativo"
				 ,"InteresesRealesOperacionesPositivo"
				 ,"InteresesRealesOperacionesNegativo"
				 ,"FactorActualizacion"
				 ,"InteresesRealesDepositosActualizadoPositivo"
				 ,"InteresesRealesDepositosActualizadoNegativo"
				 ,"InteresRealesMantencionActualizadoPositivo"
				 ,"InteresRealesMantencionActualizadoNegativo"
				 ,"InteresesRealesOperacionesActualizadoPositivo"
				 ,"InteresesRealesOperacionesActualizadoNegativo"		
				 ,"DescMes"
				 ,"Numero"
				 ,"NombreCompletoCliente"
				 ,"DocumentoIdentidadCliente"
				 ,"RazonSocialEmpresa"
				 ,"DocumentoIdentidadEmpresa"
				 ,"DireccionEmpresa"
				 ,"CiudadFecha"
				 ,COALESCE("Certificacion",'N') AS "Certificacion"
				 ,COALESCE("EstadoCertificado",'NGE') AS "EstadoCertificado"
				 FROM :v_Salida AS Salida
				 LEFT  JOIN "BSCL.Normativa.BD::TRI.ControlProceso" CP ON (CP."Esquema" = 'DJ1890' AND CP."Version" = :p_Version)
				 ORDER BY "Folio","Numero" ASC;
			
		
		END;
	END;								
END;
