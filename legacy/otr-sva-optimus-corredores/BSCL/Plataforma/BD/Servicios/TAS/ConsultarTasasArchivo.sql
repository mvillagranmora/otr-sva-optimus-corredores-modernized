SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.ConsultarTasasArchivo";

-- 
--  Proposito: Cierre y Apertura Mesa RF
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 06/09/2016  
-- 

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.ConsultarTasasArchivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_IdEmpresa		BIGINT,
	
	-- Parametros entrada
	IN p_Fecha			NVARCHAR(10),
	IN p_Fuente			NVARCHAR(40),
	
	-- Parametro salida : Cabecera del archivo XML
	OUT p_out_Cabecera	TABLE(	"SERVICIO"			NVARCHAR(250),	-- Nombre de Fuente
								"NOMBRE"			NVARCHAR(100),	-- Nombre de la Corredora
								"CUENTA"			NVARCHAR(40),	-- Codigo de Fuente
								"RUT"				NVARCHAR(40),	-- Rut de la Corredora
								"FECHA"				NVARCHAR(10),
								"HORA"				NVARCHAR(8),
								"REGISTROS"			BIGINT
								),
	
	-- Parametro salida : Detalle (Tasas) del archivo XML
	OUT p_out_Detalle	TABLE(	"NEMO"			NVARCHAR(40),
								"TASA"			DECIMAL(26,8),
								"TASA-ESTIMADA"	DECIMAL(26,8),
								"PLAZO"			BIGINT)
 )
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL" 
AS

BEGIN
	
	--------------------
	-- Declare Variables
	--------------------
	DECLARE v_FechaSistema		SECONDDATE; -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
	DECLARE v_FecSis			NVARCHAR(10);
	DECLARE v_HorSis			NVARCHAR(8);
	DECLARE v_CountRegistros	BIGINT := 0;
	DECLARE v_FechaDesde		SECONDDATE;
	DECLARE v_FechaHasta		SECONDDATE;
	
	
	--------------------
	--LOGICA
	--------------------

	BEGIN
	
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaSistema);
		--v_FecSis := TO_NVARCHAR(:v_FechaSistema,'DD-MM-YYYY');
		v_FecSis := TO_NVARCHAR(:p_Fecha,'DD-MM-YYYY');
		v_HorSis := TO_NVARCHAR(:v_FechaSistema,'HH24:MI:SS');
		
		v_FechaDesde := TO_SECONDDATE(:p_Fecha||' 00:00:00','YYYY-MM-DD HH24:MI:SS');
		v_FechaHasta := TO_SECONDDATE(:p_Fecha||' 23:59:59','YYYY-MM-DD HH24:MI:SS');
		
		-- Se obtienen datos para el detalle (Tasas)
		v_out_Detalle = 
			SELECT
				TAS."IdPrecioTasa",
				TAS."Nemotecnico",
				TAS."Tasa",
				TAS."TasaEstimada",
				TAS."Plazo"
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" TAS
			WHERE (TAS."FechaSolicitud" BETWEEN v_FechaDesde AND v_FechaHasta)
			  AND TAS."CodigoFuente"= p_Fuente
			  AND TAS."IdEstado"	= 'CRA'
			  AND TAS."RutaArchivo" <> ''
			  AND TAS."NombreArchivo"<>''
			  AND TAS."IdEmpresa"	= p_IdEmpresa
			  AND TAS."Borrado"		= 'N';
		
		
		-- Se obtienen datos para la cabecera
		SELECT COUNT(1) INTO v_CountRegistros FROM :v_out_Detalle;
		
		v_out_Cabecera = 
			SELECT
				FU."Descripcion",	-- Servicio
				FU."Codigo",		-- Cuenta
				EMP."RazonSocial",	-- Nombre  // puede ser EMP."Nombre"
				EMP."DocumentoIdentidad", -- Rut
				v_FecSis AS "FechaSistema",
				v_HorSis AS "HoraSistema"
				,v_CountRegistros AS "Registros"
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
			INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
				ON (FU."IdEmpresa" = EMP."IdEmpresa" AND FU."IdEmpresa" = p_IdEmpresa)
			WHERE FU."Codigo" = p_Fuente;
		
		
		-- ResultSet para el archivo
		p_out_Cabecera = 
			SELECT
				"Descripcion"		AS "SERVICIO",
				"RazonSocial"		AS "NOMBRE",
				"Codigo"		    AS "CUENTA",
				"DocumentoIdentidad"AS "RUT",
				"FechaSistema"		AS "FECHA",
				"HoraSistema"		AS "HORA",
				"Registros"			AS "REGISTROS"
			FROM :v_out_Cabecera;
		
		p_out_Detalle = 
			SELECT 
				"Nemotecnico"	AS "NEMO",
				"Tasa"			AS "TASA",
				"TasaEstimada"	AS "TASA-ESTIMADA",
				"Plazo"			AS "PLAZO"
			FROM :v_out_Detalle;
		
		--SELECT * FROM :p_out_Cabecera; -- para pruebas
		--SELECT * FROM :p_out_Detalle; -- para pruebas
		
		
		-- UPDATE al estado de las tasas solicitadas
		UPDATE TAS
		SET TAS."IdEstado" = 'SOL'
		FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
		INNER JOIN :v_out_Detalle DET ON (TAS."IdPrecioTasa" = DET."IdPrecioTasa");
		
  	END;
    
END;
