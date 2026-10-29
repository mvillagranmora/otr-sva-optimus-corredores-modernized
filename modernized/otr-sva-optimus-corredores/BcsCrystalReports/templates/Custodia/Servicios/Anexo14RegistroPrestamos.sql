SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::Anexo14RegistroPrestamos";
GO
--
--  Proposito: servicio para consultar Prestamos y Emprestitos para el Anexo 14 (normativa).
--  Autor: CFLORES - BCS
--  Fecha Creacion: 13/06/2017
--

CREATE PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::Anexo14RegistroPrestamos"(
	-- Parametros entrada 
	IN p_IdEmpresa		BIGINT,
	IN p_FechaOperacion	DATE,
	IN p_IdUsuario		BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA   
AS
BEGIN
	--------------------
	--Variables
	--------------------
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	DECLARE v_ExisteDolar		BIGINT := 0;
	DECLARE v_countOperaciones 	BIGINT := 0;
	DECLARE v_MensajeError		NVARCHAR(100) := '';
	DECLARE v_IdPersonaCtaPropia				BIGINT:=0;
	
	DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
	DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
	DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
	DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
	DECLARE v_FechaHoraUTC   		NVARCHAR(40);
	DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
	
	DECLARE v_Movimientos TABLE(
	 			 "Correlativo"						BIGINT
				,"FolioPrestamo" 					BIGINT
				,"FolioCartera" 					BIGINT
				,"IdClientePrestamista" 			BIGINT
				,"IdPortafolioPrestamista" 			BIGINT
				,"DocumentoIdentidadPrestamista" 	NVARCHAR(40)
				,"NombrePrestamista"				NVARCHAR(100)
				,"Cantidad" 						DECIMAL(26,8)
				,"IdInstrumentoSubyacente" 			BIGINT
				,"NemoSubyacente"  					NVARCHAR(100)
				,"NemoSVSSubyacente" 				NVARCHAR(100)
				,"FechaVencimientoEmprestito" 		NVARCHAR(10)
				,"FechaVencimientoEmprestitoFormato"NVARCHAR(10)
				,"IdClientePrestario"  				BIGINT
				,"DocumentoIdentidadPrestario"  	NVARCHAR(40)
				,"NombrePrestario" 					NVARCHAR(100)
				,"FechaConsulta" 					NVARCHAR(10)
				,"FechaConsultaFormato" 			NVARCHAR(10)
				,"Usuario"							NVARCHAR(100)
	  			,"LogoCorredora" 					BLOB
				,"FechaSistemaLOCAL"  				NVARCHAR(10)
				,"HoraSistemaLOCAL"					NVARCHAR(5) 			
				,"MensajeError"	 					NVARCHAR(100)	
			);
	
	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;

		--Valores de la cuenta propia	
	SELECT COALESCE(SOC."IdPersona",0) INTO v_IdPersonaCtaPropia
	FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC 	
		INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
	WHERE EMP."IdEmpresa" = p_IdEmpresa AND EMP."IdEstado"='ACT';
	
	-- Se obtiene la misma FechaHoraSistema que se muestra en la UI
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> p_IdEmpresa,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
	);
	


	v_Movimientos =
	
		--Emprestitos (Prestamistas)
		SELECT ROW_NUMBER()  OVER (ORDER BY "FolioPrestamo","FolioCartera")	AS "Correlativo"
		 ,*
		 FROM 
			(

				SELECT
					 EMP."FolioPrestamo"				 
					,EMP."FolioEmprestito" AS "FolioCartera"
					,EMP."IdCliente" AS "IdClientePrestamista"				
					,EMP."IdPortafolio" AS  "IdPortafolioPrestamista"
					,CLI_EMP."DocumentoIdentidad" AS "DocumentoIdentidadPrestamista"
					,EMP."RazonSocial" AS "NombrePrestamista"
					,EMP."Cantidad"
					,EMP."IdInstrumentoSubyacente"
					,EMP."NemoSubyacente"
					,EMP."NemoSVSSubyacente"
					,EMP."FechaVencimientoEmprestito"
					,TO_VARCHAR(EMP."FechaVencimientoEmprestito",'DD-MM-YYYY') as "FechaVencimientoEmprestitoFormato"
					,EMP."IdClientePrestamo" 			AS "IdClientePrestario"
					,CLI_PRE."DocumentoIdentidad"		AS "DocumentoIdentidadPrestario"
					,CLI_PRE."NombreCompleto" 			AS "NombrePrestario"
					,:p_FechaOperacion				AS "FechaConsulta"
					,TO_VARCHAR(:p_FechaOperacion,'DD-MM-YYYY')	AS "FechaConsultaFormato"
					,COALESCE(USU."CodigoUsuario",'') AS "Usuario"
					,LOGO."LogoCorredora"
					,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
					,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"				
					,'SIN_ERRORES'										AS "MensajeError"
					
				FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" AS EMP
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI_EMP ON (CLI_EMP."IdEmpresa" = EMP."IdEmpresa" AND CLI_EMP."IdCliente" = EMP."IdCliente") -- Datos del Cliente Prestamista			
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI_PRE ON (CLI_PRE."IdEmpresa" = EMP."IdEmpresa" AND CLI_PRE."IdCliente" = EMP."IdClientePrestamo") -- Datos del Cliente Prestamo			
				LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON USU."IdUsuario" = :p_IdUsuario			
				LEFT JOIN :v_logo LOGO ON (LOGO."LogoCorredora" IS NOT NULL)
				WHERE EMP."IdEmpresa" 				= :p_IdEmpresa
				AND   EMP."IdEstado" 				= 'VIG'
				AND   EMP."FechaEmprestito"            <= TO_DATE(:p_FechaOperacion) 
				AND   EMP."FechaVencimientoEmprestito" >= TO_DATE(:p_FechaOperacion)
				ORDER BY  EMP."FolioEmprestito"

		);
	
		
	SELECT COUNT(1) INTO v_countOperaciones
	FROM :v_Movimientos;
	
	IF (v_countOperaciones = 0) THEN
		v_MensajeError := 'NO_EXISTEN_OPERACIONES';
	END IF;
		
	IF ( v_countOperaciones = 0) THEN
		v_Movimientos =
			SELECT
				 0 		AS "Correlativo"
				,0 		AS "FolioPrestamo"
				,0 		AS "FolioCartera"
				,0 		AS "IdClientePrestamista"
				,0 		AS "IdPortafolioPrestamista"
				,'' 	AS "DocumentoIdentidadPrestamista"
				,'' 	AS "NombrePrestamista"
				,0 		AS "Cantidad"
				,0 		AS "IdInstrumentoSubyacente"
				,'' 	AS "NemoSubyacente"
				,'' 	AS "NemoSVSSubyacente"
				,'' 	AS "FechaVencimientoEmprestito"
				,'' 	AS "FechaVencimientoEmprestitoFormato"
				,0 		AS "IdClientePrestario" 
				,'' 	AS "DocumentoIdentidadPrestario"
				,'' 	AS "NombrePrestario"
				,'' 	AS "FechaConsulta"
				,'' 	AS "FechaConsultaFormato"
				,''		AS "Usuario"
	  			,LOGO."LogoCorredora"	AS "LogoCorredora"
				,'' 	AS "FechaSistemaLOCAL"
				,'' 	AS "HoraSistemaLOCAL"				
				,v_MensajeError		AS "MensajeError"				
			FROM DUMMY
			LEFT JOIN :v_logo LOGO
				ON (LOGO."LogoCorredora" IS NOT NULL);
	END IF;
	
	
	--Salida del SP
	SELECT * FROM :v_Movimientos ORDER BY "Correlativo";
	
END;