
SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Tesoreria.CrystalReports.Servicios::CartaInstruccionPagoBcoEstado";

GO
CREATE PROCEDURE
-- 
--  Proposito: Obtiene data para la generación de la carta de transferencias bancarias.
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 11/08/2016
--
"BSCL.Tesoreria.CrystalReports.Servicios::CartaInstruccionPagoBcoEstado" (

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
	BEGIN

		DECLARE v_logo TABLE("LogoCorredora" BLOB);
		DECLARE v_TipoDePago nvarchar(4);
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
			,NTD."DocumentoIdentidad"  					AS "RutSocio"
			,NTD."NumeroCuentaBancariaCorredora"
            ,NT."NombreBanco" 							AS "NombreBancoCorredora2"
			,NTD."NombrePersona"  						AS "NombreSocio"
			,NTD."NumeroCuentaBancariaSocio"
			,BNC."CodigoBanco"							AS "NombreBancoSocio"
			--,BNC2."BAN_EMISOR"							AS "NombreBancoSocio"
            ,NT."IdMoneda" 
			,NTD."Monto"								AS "MontoTransferencia"
			,to_decimal(NTD."IdNominaTransferencia")	AS "NumeroDeNomina"
			,NT."Monto"									AS "MontoTotalTransferencia"
			,:p_MontoSegundaFirma						AS "MontoSegundaFirma"
			,EMP1."NombreCompleto"						AS "NombrePrimeraFirma"
			,EMP1."NombreTipoCargoEmpleado" 			AS "CargoPrimeraFirma"
			,EMP2."NombreCompleto"						AS "NombreSegundaFirma"  -- El reporte discrimina si mostrar o no la segunda firma
			,EMP2."NombreTipoCargoEmpleado" 			AS "CargoSegundaFirma"
			,(CASE 	WHEN NT."IdMedioDePago" = 'TDP' THEN  'DCV'
					WHEN NT."IdMedioDePago" = 'TRA' AND NTD."IdBancoCorredora" = "IdBancoSocio" THEN 'CCT'
					WHEN NT."IdMedioDePago" = 'TRA' AND NTD."IdBancoCorredora" != "IdBancoSocio" THEN 'ITF'
					WHEN NT."IdMedioDePago" = 'TRA' AND NTD."IdBancoSocio" = 14 AND NTD."DocumentoIdentidad" = '97029000-1' THEN 'CSBC'
					WHEN NT."IdMedioDePago" = 'TRA' AND NTD."IdBancoSocio" = 14 THEN 'AVAP'
					ELSE ' ' END)					AS "TipoDePago"
			,' '							AS "RefDetallePago"
			,' '							AS "InformacionRemesa"
			,NTD."CuentaDCVDestino"									AS "CodigoDepDcvComprador"
			,NTD."CuentaDCVOrigen"									AS "CodigoDepDcvVendedor"
			,NTD."ClaveDCV"									AS "ClaveMedioPago"
			
			
			-- Datos de la Corredora

		   	,DGEN."RUT" 								AS "RutCorredora"
		   	,DGEN."Direccion" 							AS "DireccionCorredora"		   
		   	,DGEN."Telefono" 							AS "TelefonoCorredora"
		   	,DGEN."RazonSocial" 						AS "RazonSocial"

		   	
		   	
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS NT
		INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" AS NTD ON (NT."IdNominaTransferencia" = NTD."IdNominaTransferencia")
		INNER JOIN "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS BNC ON (NTD."NombreBancoSocio" = BNC."NombreBanco")
		--INNER JOIN "BSCL.Homologacion.BD.Vistas.Volante::ReplicaBANCO" AS BNC2 ON (NTD."NombreBancoSocio" = BNC2."BAN_NOMBRE")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMP1 ON (EMP1."IdEmpleado" = :p_IdEmpleadoFirma1)
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMP2 ON (EMP2."IdEmpleado" = :p_IdEmpleadoFirma2)		
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS DGEN ON DGEN."IdEmpresa" = NT."IdEmpresa"
		WHERE NT."IdNominaTransferencia" = :p_IdNominaTransferencia;
	
		

		v_count =
			SELECT to_decimal(count("IdNominaTransferencia"),5,0) AS "NumeroTotalTransferencia" 
				FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" 
				WHERE "IdNominaTransferencia"= :p_IdNominaTransferencia;
        -- Para cristal report la salida debe ser por ResulSet y no por variable tipo output
		SELECT		
			 DAT."NombreBancoCorredora2"
			,DAT."NumeroCuentaBancariaCorredora"
			,DAT."IdMoneda"
			,DAT."MontoTotalTransferencia"			
			,DAT."RutSocio"
			,DAT."NombreSocio"
			,DAT."NumeroCuentaBancariaSocio"
			,DAT."NombreBancoSocio"
			,DAT."MontoTransferencia"			
			,DAT."NombrePrimeraFirma"
			,DAT."CargoPrimeraFirma"
            ,DAT."MontoSegundaFirma"
			,DAT."NombreSegundaFirma"
			,DAT."CargoSegundaFirma"
			,DAT."NumeroDeNomina"
			,DAT."RutCorredora"
			,DAT."RazonSocial"
			,DAT."TipoDePago"
			,DAT."RefDetallePago"
			,DAT."InformacionRemesa"
			,DAT."CodigoDepDcvComprador"
			,DAT."CodigoDepDcvVendedor"
			,DAT."ClaveMedioPago"
			,CN."NumeroTotalTransferencia"
			,IMG."LogoCorredora"
			
			
		FROM :v_DataCarta AS DAT
		CROSS JOIN :v_logo AS IMG
		CROSS JOIN :v_count AS CN;


  	END;
    
END;

 
