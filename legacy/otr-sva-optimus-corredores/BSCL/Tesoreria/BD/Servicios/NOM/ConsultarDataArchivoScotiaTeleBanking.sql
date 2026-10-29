SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.NOM::ConsultarDataArchivoScotiaTeleBanking";
GO                                                  
CREATE PROCEDURE
-- 
--  Proposito: Obtiene la data para la generación del archivo de pago para Scotia TeleBanking PERU
--           * No usa reglas de negocio ya que las validaciones se realizan en la solicitud de generación del archivo
--           * Los campos de la data de salida deben tener el mismo nombre que los campos del formato del archivo
--           * El Job que dibuja el arhivo formatea y ajusta los largos de campos de acuerdo al formato del archivo y tipo de registro
--           * El campo "TipoRegistroOptimus" debe ir en todas las tablas de salida y concordar con el formato configurado
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 17/10/2017
--
"BSCL.Tesoreria.BD.Servicios.NOM::ConsultarDataArchivoScotiaTeleBanking" (

     IN p_IdEmpresa 					BIGINT,  
     IN p_IdNominaTransferencia         BIGINT,
     IN p_IdFormatoArchivo              BIGINT	DEFAULT 0,  -- Id del formato asociado (BSCL.Tesoreria.BD::TES.FormatoArchivo)
          
--	 IN p_IdBanco  						BIGINT,
--	 IN p_IdMoneda 						NVARCHAR(3),
--	 IN p_IdMedioDePago 				NVARCHAR(3),

	 -- Datos para generar el archivo (TipoRegistro 1)  
	 OUT p_out_Data_TipoRegistro1       TABLE (
		 									"TipoRegistroOptimus"   	INTEGER,  -- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado
											"Ruc"            			BIGINT,
											"RazonSocial"				NVARCHAR(60),
											"NumeroFactura"				NVARCHAR(14),
											"FechaFactura"				NVARCHAR(8),
											"Monto" 					DECIMAL(26,8), 
											"FormaDePago" 				INTEGER, 
											"CodigoOficina"				NVARCHAR(3),
											"CodigoCuenta"				NVARCHAR(7),
											"SinglePay"					NVARCHAR(1),
											"Email"						NVARCHAR(30),
											"CCI"						NVARCHAR(20),
											"Factoring"					NVARCHAR(1),
											"FechaVencimientoFactura"	NVARCHAR(8),
											"PagosAlExterior"			NVARCHAR(1)
										)
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

		/******** Salida Para Pruebas *********
		-- Datos para prueba 
		p_out_Data_TipoRegistro1  =		
		SELECT
			1 AS "TipoRegistroOptimus", -- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado
			'12969999' "Ruc",  
			'JORGE VILLAGRAN' AS "RazonSocial",
			'22222' AS "NumeroFactura",
			'2017-10-25' AS "FechaFactura", -- el java debe formatear la fecha
			'44444.55' AS "Monto",          -- falta quitar punto en java
			'4' AS "FormaDePago",
			'' AS "CodigoOficina",
			'55555' AS "CodigoCuenta",
			'' AS "SinglePay",
			'JVILLAGRAN@TODO.PE' AS "Email",
			'66666' AS "CCI",
			'' AS "Factoring",
			'' AS "FechaVencimientoFactura",
			'' AS "PagosAlExterior"	
		FROM DUMMY;
		*************************************/
		
		
		-- Data de salida para generar el archivo (tipo registro 1.. este archivo tiene solo un nivel de formato)
		p_out_Data_TipoRegistro1  =		
		SELECT
			1 AS "TipoRegistroOptimus", -- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado

			(CASE 
				WHEN DI."IdTipoDocumentoIdentidadAsociado" = 'RUC' THEN "DocumentoIdentidadAsociado"
				WHEN DI."IdTipoDocumentoIdentidadAsociado" = 'DNI' THEN '10' || "DocumentoIdentidadAsociado" -- para dni se antepone 10
			END) AS "Ruc",
			
			NTD."NombrePersona" AS "RazonSocial",
			NTD."IdComprobante" AS "NumeroFactura",
			NTD."FechaLiquidacionNT" AS "FechaFactura",
			"Monto" AS "Monto",
			(CASE
				WHEN (NTD."IdBancoSocio" = NTD."IdBancoCorredora"  AND  NTD."IdTipoCuentaBancariaSocio" = 'CUCO') THEN 2  -- 2: Para abono en cuenta corriente ScotiaBanck
				WHEN (NTD."IdBancoSocio" = NTD."IdBancoCorredora"  AND  NTD."IdTipoCuentaBancariaSocio" = 'CUVI') THEN 2  -- 2: Para abono en cuenta corriente ScotiaBanck ? 
				WHEN (NTD."IdBancoSocio" = NTD."IdBancoCorredora"  AND  NTD."IdTipoCuentaBancariaSocio" = 'CUAH') THEN 3  -- 3: Para abono en cuenta ahorros ScotiaBanck
				WHEN (NTD."IdBancoSocio" <> NTD."IdBancoCorredora") THEN 4 -- 4: Para abono en cuenta en otro banco
			END) AS "FormaDePago",
			'' AS "CodigoOficina", -- FALTA  implementar en mantenedor de cuenta bancaria cliente
			NTD."NumeroCuentaBancariaCorredora" AS "CodigoCuenta",
			'' AS "SinglePay",
			NTD."DireccionElectronica" AS "Email",
			'' AS "CCI", -- FALTA implementar en mantenedor de cuenta bancaria cliente
			'' AS "Factoring",
			'' AS "FechaVencimientoFactura",
			'' AS "PagosAlExterior"	
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" AS NTD
		
		-- PARA pruebas chile
		--LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON (DI."IdTipoDocumentoIdentidad" = 'R' AND DI."IdPersona" = NTD."IdPersona")
		-- PARA PERU
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON (DI."IdTipoDocumentoIdentidad" = 'IUP' AND DI."IdPersona" = NTD."IdPersona")
		
		WHERE NTD."IdNominaTransferencia" = :p_IdNominaTransferencia
		AND NTD."EsTransferencia" = 'S';
		
  	END;    
END;

