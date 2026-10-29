SET SCHEMA "BSCL"; 
GO 
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Anexo4"; 
GO

 
CREATE PROCEDURE

"BSCL.Normativa.CrystalReports.Servicios::Anexo4" (
	-- Parametros entrada comunes en todos los servicios privados
  	IN p_IdEmpresa			BIGINT,

    --VARIABLES DEL PROCESO
	IN	p_FechaOperacionDesde					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_FechaOperacionHasta					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_IdCliente 							BIGINT 			DEFAULT '0'
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	
		--------------------
		--Variables
		--------------------
		DECLARE v_Count	 BIGINT:=0;
		DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
		DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
		DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
		DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
		DECLARE v_FechaHoraUTC   		NVARCHAR(40);
		DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
		DECLARE v_MensajeError		NVARCHAR(100) := '';

		
		
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> p_IdEmpresa,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
		);
		
		v_logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;

		v_datosGenerales = select 
		"RazonSocial" AS "RazonCorredora", 
		"Direccion"   AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;
		
		
	
	
		
		CALL "BSCL.Normativa.BD.Servicios.Anexos::ConsultarAnexoCuatro"(
			p_IdEmpresa=>:p_IdEmpresa,
			p_FechaOperacionDesde=>:p_FechaOperacionDesde,
			p_FechaOperacionHasta=>:p_FechaOperacionHasta,
			p_IdCliente=>:p_IdCliente,
			p_out_TablaAnexo4=>v_Datos_Origen
		);						
									
									
	v_tablaSalida=SELECT * FROM :v_Datos_Origen;								
	
		
	SELECT COUNT(1) INTO v_Count FROM :v_Datos_Origen;

	IF v_Count = 0 THEN
		v_MensajeError := 'NO_EXISTEN_OPERACIONES';
			SELECT 	'' AS "FechaMovimientoFormato",
				0 AS  "CuentaTerceros",
				0 AS  "EspecialesOtros",
				0 AS  "CuentaPropiaAcciones",
				0 AS  "CuentaPropiaPlazo",
				0 AS  "CliOperacionPlazo",
				0 AS  "CliPrivilegiados",
				0 AS  "CliConvenio",
				0 AS  "TotalDeclarado",
				0 AS  "TotalCompraAcciones",
				0 AS  "TotalCompraPlazo" ,
				0 AS  "TotalVentaAcciones",
				0 AS  "TotalVentaPlazo",
				0 AS  "TotalGeneral"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
			,v_MensajeError	AS "MensajeError",*
	    FROM DUMMY OPE
		CROSS JOIN :v_datosGenerales
		CROSS JOIN :v_logo;	
	ELSE
	
		SELECT 	TO_VARCHAR(OPE."FechaMovimiento",'DD/MM/YYYY') AS "FechaMovimientoFormato"
			,*
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
			,v_MensajeError	AS "MensajeError"
	    FROM :v_tablaSalida OPE
		CROSS JOIN :v_datosGenerales
		CROSS JOIN :v_logo;	
	END IF;
		



END;


