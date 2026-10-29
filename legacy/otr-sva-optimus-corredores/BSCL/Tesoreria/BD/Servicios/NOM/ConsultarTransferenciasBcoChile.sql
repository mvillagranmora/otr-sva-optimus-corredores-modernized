SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.NOM::ConsultarTransferenciasBcoChile";
GO

CREATE PROCEDURE
-- 
--  Proposito: Obtiene la data para la generación del archivo de transferencias del banco de chile.
--           * No usa reglas de negocio ya que las validaciones se realizan en la solicitud de generación del archivo
--           * Los campos de la data de salida deben tener el mismo nombre que los campos del formato del archivo
--           * El Job que dibuja el arhivo formatea y ajusta los largos de campos de acuerdo al formato del archivo
--           * El campo "TipoRegistroOptimus" debe ir en todas las tablas de salida y concordar con el formato configurado
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 02/08/2016
--
"BSCL.Tesoreria.BD.Servicios.NOM::ConsultarTransferenciasBcoChile" (

     IN p_IdEmpresa 					BIGINT,  
     IN p_IdNominaTransferencia         BIGINT,
     IN p_IdFormatoArchivo              BIGINT,
     
	 OUT p_out_Transferencias           TABLE(  -- Datos para archivo
	 										 "TipoRegistroOptimus"  INTEGER  	 	-- Este campo debe ir en todas las tablas de salida y su valor concordar con el tipo de registro
											,"TipoOperacion" 		NVARCHAR(3)
											,"RutCliente"  			NVARCHAR(40)  -- Rut de la Corredora
											,"CuentaCargo"  		NVARCHAR(100) -- Numero Cuenta bancaria corredora
											,"RutBeneficiario" 		NVARCHAR(40)  -- Rut del cliente al cual se transferirá
											,"NombreBeneficiario" 	NVARCHAR(100) 
											,"CuentaBeneficiario"  	NVARCHAR(100)
											,"MontoTransferencia"  	INTEGER
											,"RutBancoBeneficiario"	NVARCHAR(40)
											,"AbonoInmediato" 		NVARCHAR(1)
											,"MotivoTransferencia"  NVARCHAR(250) -- Glosa
											,"NotificacionViaEmail" INTEGER       -- (1:Si / 0:No)
											,"AsuntoEmail"  		NVARCHAR(30)
											,"DireccionEMail" 		NVARCHAR(100)
											,"TipoCuenta"  			NVARCHAR(3))


											
 )
 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
     	
AS
  BEGIN
   	
	DECLARE v_RutCorredora        NVARCHAR(50) := '';
	
	--------------------
	--LOGICA
	--------------------
	BEGIN

		-- Obtiene los datos de la corredora
		SELECT CC."DocumentoIdentidad"
		INTO v_RutCorredora
		FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS CSN ON (CSN."IdSocioDeNegocio" = EMP."IdSocioDeNegocio")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON (CC."IdCliente" = CSN."IdCliente")
		WHERE EMP."IdEmpresa" = :p_IdEmpresa;


		
		-- Obtiene configuracion del formato del archivo
		v_FormatoArchivo =
		SELECT "IdFormatoArchivo", "TipoGlosa"
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo"
		WHERE "IdFormatoArchivo" = :p_IdFormatoArchivo;
		
		--SELECT 'v_FormatoArchivo', * FROM :v_FormatoArchivo; -- BORRAR


		


		-- Obtiene la data a incluir en el archivo
		v_DataArchivo = 
		SELECT 
			 (CASE    
				WHEN (COALESCE(NT."IdBancoCorredora",0) = COALESCE(NT."IdBancoSocio",0)) THEN -- TEC = Transferencias a Terceros Banco de Chile
					"BSCL.Tesoreria.BD.Utilitarios::HomologacionCampoArchivo"(FA."IdFormatoArchivo",'TipoOperacion', '1')
				ELSE --TOB = Transferencias a Otros Bancos
					"BSCL.Tesoreria.BD.Utilitarios::HomologacionCampoArchivo"(FA."IdFormatoArchivo",'TipoOperacion', '2')
			 END) AS "TipoOperacion"
		
			,:v_RutCorredora 				 AS "RutCliente"   -- Rut Corredora -> Rut Cliente para el banco
			,"NumeroCuentaBancariaCorredora" AS "CuentaCargo"  -- Numero Cuenta bancaria de la corredora

			,NT."DocumentoIdentidad" 		 AS "RutBeneficiario"    -- Persona a la cual se transferirá
			,NT."NombrePersona" 			 AS "NombreBeneficiario"
			,NT."NumeroCuentaBancariaSocio"  AS "CuentaBeneficiario"			
			,"Monto"
			,NT."RutBancoSocio" 			 AS "RutBancoBeneficiario"			
			,'' AS "AbonoInmediato"
			,(CASE
				WHEN (FA."TipoGlosa" = 0)  THEN ''
				WHEN (FA."TipoGlosa" = 1)  THEN NT."NombreConcepto"
				WHEN (FA."TipoGlosa" = 2)  THEN RTRIM(LTRIM(CAST(NT."IdComprobante" AS NVARCHAR(20)))) || ' ' || NT."NombreConcepto"
			 END) AS "MotivoTransferencia"	-- Glosa		
			,(CASE    
				WHEN (LENGTH(NT."DireccionElectronica") > 0) THEN 1
				ELSE 0
			 END) AS "NotificacionViaEmail" -- (1:Si / 0:No)
			,'' AS "AsuntoEmail"
			,NT."DireccionElectronica" 		AS "DireccionEMail"			 
			,"BSCL.Tesoreria.BD.Utilitarios::HomologacionCampoArchivo"(FA."IdFormatoArchivo",'TipoCuenta', NT."IdTipoCuentaBanco") AS "TipoCuenta"

		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" AS NT
		INNER JOIN :v_FormatoArchivo AS FA ON (1=1)
		WHERE NT."IdNominaTransferencia" = :p_IdNominaTransferencia
		AND NT."EsTransferencia" = 'S'
		AND NT."IdEstado" = 'ING';

		-- SELECT * FROM :v_DataArchivo; -- BORRAR

		
		-- Salida para archivo
		p_out_Transferencias  = 
		SELECT
		     1 AS "TipoRegistroOptimus" -- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado
			,"TipoOperacion"
			,"RutCliente"
			,"CuentaCargo"
			,"RutBeneficiario"
			,"NombreBeneficiario"
			,"CuentaBeneficiario"	
			,TO_INTEGER("Monto") AS "MontoTransferencia"				
			,"RutBancoBeneficiario"
			,"AbonoInmediato"
			,"MotivoTransferencia"
			,"NotificacionViaEmail"
			,"AsuntoEmail"
			,"DireccionEMail"
			,"TipoCuenta"
		FROM :v_DataArchivo;
											 
											 
  	END;
    
END;

  
