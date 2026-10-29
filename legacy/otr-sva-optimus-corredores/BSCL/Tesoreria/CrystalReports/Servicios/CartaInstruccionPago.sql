
SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Tesoreria.CrystalReports.Servicios::CartaInstruccionPago";

GO
CREATE PROCEDURE
-- 
--  Proposito: Obtiene data para la generación de la carta de transferencias bancarias.
--  Fecha Creacion: 06/2024
--
"BSCL.Tesoreria.CrystalReports.Servicios::CartaInstruccionPago" (

     IN p_IdEmpresa 					BIGINT,  
     IN p_IdNominaTransferencia         BIGINT,
     IN p_IdEmpleadoFirma1				BIGINT,
     IN p_IdEmpleadoFirma2				BIGINT,
     IN p_MontoSegundaFirma				DECIMAL(26,8)
 )
 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
     	 
AS
  BEGIN
	
	--------------------
	--LOGICA
	--------------------
	BEGIN

		DECLARE v_logo TABLE("LogoCorredora" BLOB);
	
		--Se recupera la ultima versión del logo cargado en el sistema
		v_logo = 
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' 
		AND "IdEmpresa" = p_IdEmpresa 
		ORDER BY "Version" DESC;


		v_DataCarta = 
		SELECT 
		     NT."IdCuentaBanco"
			,NTD."DocumentoIdentidad"  AS "RutSocio"
			,NTD."NumeroCuentaBancariaCorredora"
            ,NT."NombreBanco" AS "NombreBancoCorredora"
			,NTD."NombrePersona"  AS "NombreSocio"
			,NTD."NumeroCuentaBancariaSocio"
			,NTD."NombreBancoSocio"
            ,NT."IdMoneda" 
			,NTD."Monto"					AS "MontoTransferencia"
			,NT."Monto"						AS "MontoTotalTransferencia"
			,:p_MontoSegundaFirma			AS "MontoSegundaFirma"
			,EMP1."NombreCompleto"			AS "NombrePrimeraFirma"
			,EMP1."NombreTipoCargoEmpleado" AS "CargoPrimeraFirma"
			,EMP2."NombreCompleto"			AS "NombreSegundaFirma"  -- El reporte discrimina si mostrar o no la segunda firma
			,EMP2."NombreTipoCargoEmpleado" AS "CargoSegundaFirma"
			
			-- Datos de la Corredora
			,DGEN."RazonSocial" AS "RazonSocialCorredora"
		   	,DGEN."RUT" AS "RutCorredora"
		   	,DGEN."Direccion" AS "DireccionCorredora"		   
		   	,DGEN."Telefono" AS "TelefonoCorredora"
		   	
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS NT
		INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" AS NTD ON (NT."IdNominaTransferencia" = NTD."IdNominaTransferencia")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMP1 ON (EMP1."IdEmpleado" = :p_IdEmpleadoFirma1)
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMP2 ON (EMP2."IdEmpleado" = :p_IdEmpleadoFirma2)		
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS DGEN ON DGEN."IdEmpresa" = NT."IdEmpresa"
		WHERE NT."IdNominaTransferencia" = :p_IdNominaTransferencia;


        -- Para cristal report la salida debe ser por ResulSet y no por variable tipo output
		SELECT		
			 DAT."NombreBancoCorredora"
			,DAT."NumeroCuentaBancariaCorredora"
			,DAT."IdMoneda"
			,DAT."MontoTotalTransferencia"			
			,DAT."RutSocio"
			,DAT."NombreSocio"
			,DAT."NumeroCuentaBancariaSocio"
			,DAT."NombreBancoSocio"
			,DAT."MontoTransferencia"			
			,DAT."RazonSocialCorredora"
			,DAT."NombrePrimeraFirma"
			,DAT."CargoPrimeraFirma"
            ,DAT."MontoSegundaFirma"
			,DAT."NombreSegundaFirma"
			,DAT."CargoSegundaFirma"
			,IMG."LogoCorredora"
		FROM :v_DataCarta AS DAT
		CROSS JOIN :v_logo AS IMG;


  	END;
    
END;

  
