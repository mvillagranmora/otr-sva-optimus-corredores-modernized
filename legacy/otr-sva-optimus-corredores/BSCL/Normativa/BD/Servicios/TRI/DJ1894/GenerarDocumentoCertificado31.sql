SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarDocumentoCertificado31";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarDocumentoCertificado31"(
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
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1894.GenerarDocumentoCertificado31','INICIO','INICIO');
				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
						
			v_Resultado = SELECT  	  CERTIFICADO."Folio"
									 ,CERTIFICADO."IdEmpresa"				
									 ,CERTIFICADO."Esquema"				
									 ,CERTIFICADO."Version"				
									 ,CERTIFICADO."Anio"					
									 ,CERTIFICADO."Periodo"				
									 ,CERTIFICADO."IdCliente"				
									 ,CERTIFICADO."IdPortafolio"							
									 ,DETALLE."IdFondo"							
									 ,DETALLE."IdEmisor"
									 ,FONDO."CodigoSVS" AS "RunFondo"
									 ,FONDO."NombreFondo"							
									 ,DETALLE."IdFondoOrigen"						
									 ,DETALLE."IdEmisorOrigen"
									 ,FONDO_ORIGEN."CodigoSVS" AS "RunFondoOrigen"
									 ,FONDO_ORIGEN."NombreFondo" AS "NombreFondoOrigen"					
									 ,DETALLE."MontoLiquidacion"						
									 ,DETALLE."MayorValorUf"						
									 ,DETALLE."PorcentajePromedioAnualAcciones"				
									 ,DETALLE."PorcentajePromedioAnualAccionesProvenienteReinversion"				
									 ,DETALLE."FechaCertificacion"
									 ,DETALLE."FechaInversionOriginal"
									 ,CLIENTE."NombreCompleto" AS "NombreCompletoCliente"
									 ,CLIENTE."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
									 ,EMPRESA."RazonSocial" AS "RazonSocialEmpresa"
									 ,EMPRESA."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
									 ,EMPRESA."DireccionFisicaPrincipalCalle" || ' ' || EMPRESA."DireccionFisicaPrincipalNumero" || ',' || EMPRESA."DireccionFisicaPrincipalSector" || ' ' || EMPRESA."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa"
									 ,EMISOR."NombreCompleto" AS "NombreCompletoEmisor"
									 ,EMISOR."DocumentoIdentidad" AS "RutEmisor"
									 ,EMPRESA."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
								FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
								INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N31" DETALLE	ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")
								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (CLIENTE."IdCliente" = CERTIFICADO."IdCliente")
								INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA ON (EMPRESA."IdEmpresa" = CERTIFICADO."IdEmpresa")
								INNER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMISOR ON (EMISOR."IdEmisor" = DETALLE."IdEmisor")
								INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoTributario" FONDO ON (DETALLE."IdFondo" = FONDO."IdFondo")
								INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoTributario" FONDO_ORIGEN ON (DETALLE."IdFondo" = FONDO_ORIGEN."IdFondo")
								WHERE p_Todos = 'S' OR (CERTIFICADO."IdCliente" = p_IdCliente AND CERTIFICADO."IdPortafolio" = p_IdPortafolio AND CERTIFICADO."Folio" = p_Folio)
								AND CERTIFICADO."Version" = p_Version;

			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1894.GenerarDocumentoCertificado31','FIN','FIN');
			
			SELECT 			  "Folio"
							 ,"IdEmpresa"				
							 ,Resultado."Esquema"				
							 ,Resultado."Version"				
							 ,"Anio"					
							 ,"Periodo"				
							 ,"IdCliente"				
							 ,"IdPortafolio"							
							 ,"IdFondo"							
							 ,"IdEmisor"
							 ,"RunFondo"
							 ,"NombreFondo"							
							 ,"IdFondoOrigen"						
							 ,"IdEmisorOrigen"
							 ,"RunFondoOrigen"
							 ,"NombreFondoOrigen"					
							 ,"MontoLiquidacion"						
							 ,"MayorValorUf"						
							 ,"PorcentajePromedioAnualAcciones"				
							 ,"PorcentajePromedioAnualAccionesProvenienteReinversion"				
							 ,"FechaCertificacion"
							 ,"FechaInversionOriginal"
							 ,"NombreCompletoCliente"
							 ,"DocumentoIdentidadCliente"
							 ,"RazonSocialEmpresa"
							 ,"DocumentoIdentidadEmpresa"
							 ,"DireccionEmpresa"
							 ,"NombreCompletoEmisor"
							 ,"RutEmisor"
							 ,"CiudadFecha"
							 ,COALESCE("Certificacion",'NGE') AS "Certificacion"
							 ,COALESCE("EstadoCertificado",'NGE') AS "EstadoCertificado"
						FROM :v_Resultado AS Resultado
						LEFT  JOIN "BSCL.Normativa.BD::TRI.ControlProceso" CP ON (CP."Esquema" = 'DJ1890' AND CP."Version" = :p_Version)
						ORDER BY "Folio" ASC;
			
		
		END;
	END;								
END;
