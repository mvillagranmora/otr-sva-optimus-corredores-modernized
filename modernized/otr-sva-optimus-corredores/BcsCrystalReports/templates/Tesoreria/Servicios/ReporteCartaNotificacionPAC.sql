SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Tesoreria.CrystalReports.Servicios::ReporteCartaNotificacionPAC";
-- 
--  Proposito: Obtiene datos de un Mandato PAC y genera Carta Notificacion PAC
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 24/01/2019
--
CREATE PROCEDURE "BSCL.Tesoreria.CrystalReports.Servicios::ReporteCartaNotificacionPAC" (
     IN p_IdEmpresa				BIGINT,
     IN p_IdMandatoPAC			BIGINT,
     IN p_IdPersonaCorredora	BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--Variables
	--------------------
	DECLARE v_Salida TABLE (	"FechaImpresion"				NVARCHAR(10),
								"RazonSocialCorredora"			NVARCHAR(200),
								"DocumentoIdentidadCorredora"	NVARCHAR(40),
								"EntregaMandatosServicio"		NVARCHAR(100),
								"BancoConcentrador"				NVARCHAR(100),
								"BancoDebitador"				NVARCHAR(100),
								"IdMandatoPAC"					BIGINT,
								"DocumentoIdentidadPersona"		NVARCHAR(40),
								"NombrePersona"					NVARCHAR(200),
								"FolioPacMandatoAgrupador"		NVARCHAR(50),
								"NumeroCuentaBancaria"			NVARCHAR(100),
								"MensajeError"					NVARCHAR(100));
	
	DECLARE v_count			BIGINT;
	
	DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
	DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
	DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
	DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
	DECLARE v_FechaHoraUTC   		NVARCHAR(40);
	DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
	DECLARE v_FechaEntrada			NVARCHAR(10);
	DECLARE v_FechaSistema			NVARCHAR(10);
	
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
	
	v_Salida =
		SELECT
			 TO_VARCHAR(:v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')			AS "FechaImpresion"
			,COALESCE(PERCOR."NombreCompleto",'')						AS "RazonSocialCorredora"
			,CASE WHEN COALESCE(PERCOR."DocumentoIdentidad",'') <> '' AND PERCOR."TipoDocumentoIdentidad" = 'R' THEN 
					"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(PERCOR."DocumentoIdentidad",'-'),'i',0) || '-'||SUBSTR_AFTER(PERCOR."DocumentoIdentidad",'-')
				  ELSE COALESCE(PERCOR."DocumentoIdentidad",'-') END	AS "DocumentoIdentidadCorredora"
			,'PAC Multibanco'											AS "EntregaMandatosServicio"
			,CONTES."Valor"												AS "BancoConcentrador"
			,PAC."NombreBanco"											AS "BancoDebitador"
			,PAC."IdMandatoPAC"
			,COALESCE(PAC."DocumentoIdentidad",'-')						AS "DocumentoIdentidadPersona"
			,COALESCE(PAC."NombreCompleto",'-')							AS "NombrePersona"
			,COALESCE(PAC."FolioPacMandatoAgrupador",'-')				AS "FolioPacMandatoAgrupador"
			,COALESCE(PAC."NumeroCuentaBancaria",'-')					AS "NumeroCuentaBancaria"
			,'' AS "MensajeError"
		FROM "BSCL.Tesoreria.BD.Vistas::PAC.ConsultarMandatoPAC" PAC
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERCOR
			ON (PERCOR."IdPersona" = :p_IdPersonaCorredora)
		LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria" CONTES
			ON (PAC."IdEmpresa" = CONTES."IdEmpresa" AND
				CONTES."CodigoParametro" = 'BANCOCONCENTRADORCARTANOTIFICACIONPAC')
		WHERE PAC."IdMandatoPAC" = :p_IdMandatoPAC
		  AND PAC."IdEmpresa" = :p_IdEmpresa;
	
	SELECT COUNT(1) INTO v_count FROM :v_Salida;
	IF (:v_count = 0) THEN
		v_Salida =
			SELECT
				 TO_VARCHAR(:v_FechaHoraSistemaLOCAL,'DD-MM-YYYY') AS "FechaImpresion"
				,'' AS "RazonSocialCorredora"
				,'' AS "DocumentoIdentidadCorredora"
				,'' AS "EntregaMandatosServicio"
				,'' AS "BancoConcentrador"
				,'' AS "BancoDebitador"
				,0  AS "IdMandatoPAC"
				,'' AS "DocumentoIdentidadPersona"
				,'' AS "NombrePersona"
				,'' AS "FolioPacMandatoAgrupador"
				,'' AS "NumeroCuentaBancaria"
				,'ERROR : NO SE ENCONTRARON DATOS' AS "MensajeError"
			FROM DUMMY;
	END IF;
	
	--Salida del SP
	SELECT * FROM :v_Salida;
		
END;
