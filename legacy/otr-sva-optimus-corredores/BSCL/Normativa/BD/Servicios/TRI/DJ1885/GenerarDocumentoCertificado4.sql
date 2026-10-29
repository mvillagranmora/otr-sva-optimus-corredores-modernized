SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1885.GenerarDocumentoCertificado4";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1885.GenerarDocumentoCertificado4"(
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
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1885.GenerarDocumentoCertificado4','INICIO','INICIO');
				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			v_Resultado = SELECT   	  CERTIFICADO."Folio"
									 ,CERTIFICADO."IdEmpresa"				
									 ,CERTIFICADO."Esquema"				
									 ,CERTIFICADO."Version"				
									 ,CERTIFICADO."Anio"					
									 ,CERTIFICADO."Periodo"				
									 ,CERTIFICADO."IdCliente"				
									 ,CERTIFICADO."IdPortafolio"							
									 ,DETALLE."NumeroDividendo"							
									 ,DETALLE."FechaPago"
									 ,DETALLE."MontoTotal"
									 ,DETALLE."MontoAfectoGlobal"
									 ,DETALLE."MontoExentoGlobal"
									 ,DETALLE."MontoConstitutivoNoAcogido"
									 ,DETALLE."MontoConstitutivoAcogido"
									 ,DETALLE."DevolucionCapital"
									 ,DETALLE."IncrementoImpuestoPrimeraCategoria"
									 ,DETALLE."ImptAfectosConDerecho"
									 ,DETALLE."ImptAfectosSinDerecho"
									 ,DETALLE."ImptExentosConDerecho"
									 ,DETALLE."ImptExentosSinDerecho"
									 ,DETALLE."CreditoImpuestoExterno"
									 ,DETALLE."ImptTasaAdicionales"
									 ,DETALLE."RazonSocial"
									 ,DETALLE."RutSociedad"
									 ,'S' AS "TransaccionBolsaValores"
									 ,EMPRESA."RazonSocial" AS "RazonSocialEmpresa"
									 ,EMPRESA."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
									 ,EMPRESA."DireccionFisicaPrincipalCalle" || ' ' || EMPRESA."DireccionFisicaPrincipalNumero" || ',' || EMPRESA."DireccionFisicaPrincipalSector" || ' ' || EMPRESA."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa"
									 ,EMPRESA."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
									 ,CLIENTE."NombreCompleto" AS "NombreCompletoCliente"
									 ,CLIENTE."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
								FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" CERTIFICADO
								INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1885N4" DETALLE	ON (CERTIFICADO."IdCertificadoCliente" = DETALLE."IdCertificadoCliente")
								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (CLIENTE."IdCliente" = CERTIFICADO."IdCliente")
								INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA ON (EMPRESA."IdEmpresa" = CERTIFICADO."IdEmpresa")
								WHERE p_Todos = 'S' OR (CERTIFICADO."IdCliente" = p_IdCliente AND CERTIFICADO."IdPortafolio" = p_IdPortafolio AND CERTIFICADO."Folio" = p_Folio)
								AND CERTIFICADO."Version" = p_Version;
					/*			
			SELECT "Folio"
					,"IdEmpresa"
					,"Esquema"
					,"Version"
					,"Anio"
					,"Periodo"
					,"IdCliente"
					,"IdPortafolio"
					,"NumeroDividendo"
					,"FechaPago"
					,"MontoTotal"
					,"MontoAfectoGlobal"
					,"MontoExentoGlobal"
					,"MontoConstitutivoNoAcogido"
					,"MontoConstitutivoAcogido"
					,"DevolucionCapital"
					,"IncrementoImpuestoPrimeraCategoria"
					,"ImptAfectosConDerecho"
					,"ImptAfectosSinDerecho"
					,"ImptExentosConDerecho"
					,"ImptExentosSinDerecho"
					,"CreditoImpuestoExterno"
					,"ImptTasaAdicionales"
					,"RazonSocial"
					,"RutSociedad"
					,"TransaccionBolsaValores"
					,"RazonSocialEmpresa"
					,"DocumentoIdentidadEmpresa"
					,"DireccionEmpresa"
					,"CiudadFecha"
					,"NombreCompletoCliente"
					,"DocumentoIdentidadCliente"
			FROM :v_Resultado
			ORDER BY "Folio" ASC;
			*/
			v_logo = select TOP 1
	        "Binario" AS "LogoCorredora"
	        from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
	        WHERE "NombreImagen" = 'LOGO_CORREDOR' AND "IdEmpresa" = p_IdEmpresa ORDER BY "Version" DESC;
			
		    SELECT *
			FROM :v_Resultado
			CROSS JOIN :v_logo
			ORDER BY "Folio" ASC;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1885.GenerarDocumentoCertificado4','FIN','FIN');
			
		END;
	END;								
END;
