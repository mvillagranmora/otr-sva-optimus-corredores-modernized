SET SCHEMA "BSCL"; 
GO 
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::CIR1804"; 
GO

CREATE PROCEDURE

"BSCL.Normativa.CrystalReports.Servicios::CIR1804" (
	-- Parametros entrada comunes en todos los servicios privados
  	IN p_IdEmpresa			BIGINT,
  	IN p_IdUsuario			BIGINT,
  	IN p_IdModulo			NVARCHAR(3),

    --VARIABLES DEL PROCESO
	IN	p_Periodo					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_IdPersonaAGF 				BIGINT 			DEFAULT 0,
	IN	p_IdPersonaFondo			BIGINT 			DEFAULT 0,
	IN  p_IdSerieFondo				NVARCHAR(40)	DEFAULT ''
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
				
	CALL "BSCL.Normativa.BD.Servicios.CIR1804::ConsultarCIR1804"(
		p_IdEmpresa=>:p_IdEmpresa,
		p_IdUsuario=>:p_IdUsuario,
		p_IdModulo=>:p_IdModulo,
		p_Periodo=>:p_Periodo,
		p_IdPersonaAGF=>:p_IdPersonaAGF,
		p_IdPersonaFondo=>:p_IdPersonaFondo,
		p_IdSerieFondo=>:p_IdSerieFondo,
		p_out_TablaCIR1804=>v_Datos_Origen
	);							
									
	v_tablaSalida=SELECT * FROM :v_Datos_Origen;								
		
	SELECT COUNT(1) INTO v_Count FROM :v_Datos_Origen;

	IF v_Count = 0 THEN
		v_MensajeError := 'NO_EXISTEN_OPERACIONES';
			SELECT 	
				 '2018-10'	AS "PeriodoInformado"
				,'RAZON SOCIAL AGF' AS "RazonSocialAGF"
				,'1-9' 	AS "DocumentoIdentidadAGF"	
				,'NombreFondo' 	AS "NombreFondo"
				,'1-9'	AS "DocumentoIdentidadFondo"
				,'CLP' AS "IdMoneda"	
				,1000 AS "SaldoInicial"	
				,'2018-10-01' AS "FechaTransaccion"	
				,'1-9' AS "DocumentoIdentidadDelParticipe"
				,'SA' 	AS "CodigoDelParticipe"
				,1000 	AS "PatrimonioNetoInicial"
				,1 AS "PorcentajeSaldoInicial"
				,1000 	AS "PatrimonioNeto"
				,1 	AS "PorcentajeSaldoIndividual"
				,1 AS "PorcentajeSaldoTotal"
				,'' AS "PatrimonioNeto"
				,''	AS "MontoTransadoEntrada"
				,'' AS "MontoTransadoSalida"
				,'' AS "SaldoIndividual"
				,'' AS "SaldoIndividual_P"
				,'' AS "SaldoTotal"
				,'' AS "SaldoTotal_P"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
				,v_MensajeError	AS "MensajeError",*
	    	FROM DUMMY OPE
			CROSS JOIN :v_datosGenerales
			CROSS JOIN :v_logo;	
	ELSE
	
		SELECT 	"PeriodoInformado" 								AS "PeriodoInformado"
			,"RazonSocialAGF" 									AS "RazonSocialAGF"
			,"DocumentoIdentidadAGF" 							AS "DocumentoIdentidadAGF"	
			,"NombreFondo" 										AS "NombreFondo"
			,"DocumentoIdentidadFondo"		 					AS "DocumentoIdentidadFondo"
			,"IdMoneda" 										AS "IdMoneda"	
			,"FechaTransaccion" 								AS "FechaTransaccion"	
			,"DocumentoIdentidadDelParticipe" 					AS "DocumentoIdentidadDelParticipe"
			,"CodigoDelParticipe" 								AS "CodigoDelParticipe"
			,"PatrimonioNetoInicial" 							AS "PatrimonioNetoInicial"
			,"PorcentajeSaldoInicial" 							AS "PorcentajeSaldoInicial"
			,"PatrimonioNeto" 									AS "PatrimonioNeto"
			,"MontoTransadoEntrada" 							AS "MontoTransadoEntrada"
			,"MontoTransadoSalida" 								AS "MontoTransadoSalida"
			,"SaldoIndividual" 								    AS "SaldoIndividual"
			,"SaldoIndividual_P" 								AS "SaldoIndividual_P"
			,"SaldoTotal" 								        AS "SaldoTotal"
			,"SaldoTotal_P" 								    AS "SaldoTotal_P"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
			,v_MensajeError										AS "MensajeError",*
	    FROM :v_Datos_Origen
		CROSS JOIN :v_datosGenerales
		CROSS JOIN :v_logo;	
	END IF;
		
END;