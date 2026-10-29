SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.NOM::ConsultarDataArchivoBcpTeleCredito";
                                                 
GO                                                  
CREATE PROCEDURE
-- 
--  Proposito: Obtiene la data para la generación del archivo de pago para BCP TeleCredito PERU
--           * No usa reglas de negocio ya que las validaciones se realizan en la solicitud de generación del archivo
--           * Los campos de la data de salida deben tener el mismo nombre que los campos del formato del archivo
--           * El Job que dibuja el arhivo formatea y ajusta los largos de campos de acuerdo al formato del archivo y tipo de registro
--           * El campo "TipoRegistroOptimus" debe ir en todas las tablas de salida y concordar con el formato configurado
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 17/10/2017
--
"BSCL.Tesoreria.BD.Servicios.NOM::ConsultarDataArchivoBcpTeleCredito" (

     IN p_IdEmpresa 					BIGINT,  
     IN p_IdNominaTransferencia         BIGINT,
     IN p_IdFormatoArchivo              BIGINT	DEFAULT 0,  -- Id del formato asociado (BSCL.Tesoreria.BD::TES.FormatoArchivo) 
	 
     
	 -- Datos para generar el archivo (TipoRegistro 1: Cabecera)  
	 OUT p_out_Data_TipoRegistro1       TABLE (
											"TipoRegistroOptimus"   		INTEGER,  	 	-- Este campo debe ir en todas las tablas de salida y su valor concordar con el tipo de registro
											"TipoRegistro"          		NVARCHAR(1), 	-- 1: cabecera
											"CantidadAbonos"        		BIGINT, 		-- Cantidad de Abonos de la planilla 
											"FechaProceso"       			NVARCHAR(8), 	-- Fecha de Proceso (el job formateará la fecha según el formato configurado)
											"TipoCuentaCargo"       		NVARCHAR(1),	-- C: Cuenta Corriente    M:Cuenta Mandante
											"MonedaCuentaCargo"     		NVARCHAR(4),	-- 0001: Nuevos Soles   1001: Dólares 
											"NumeroCuentaCargo"				NVARCHAR(20),   -- Numero de Cuenta del Cargo  
											"MontoTotal" 					DECIMAL(26,8),	-- Monto Total de la planilla
											"ReferenciaPlanilla"    		NVARCHAR(40),   -- Referencia de la planilla de proveedores
											"FlagExoneracionITF" 			NVARCHAR(1),	-- Marca para cheques de generacia. Informar sólo con Forma de pago "Cheque de Gerencia"
											"TotalControl" 					DECIMAL(15,0)   --  Total de control (Checksum)
										),
										
	 -- Datos para generar el archivo (TipoRegistro 2: Detalle)  
	 OUT p_out_Data_TipoRegistro2       TABLE (
		 									"TipoRegistroOptimus"   		INTEGER,  		-- Este campo debe ir en todas las tablas de salida y su valor concordar con el tipo de registro
		 									"IdInterno"                     INTEGER,        -- Id interno para relacionar el registro 2(detalle) con registro 3(Documento) 
											"TipoRegistro"                  NVARCHAR(1), 	-- 2: Detalle
											"TipoCuentaAbono"               NVARCHAR(1), 	-- A: Cuenta Ahorro   C:Cuenta Corriente   M:Cuenta Maestra
											"NumeroCuentaAbono"             NVARCHAR(20), 	-- Numero de la cuenta de Abono
											"ModalidadDePago"               INTEGER, 		-- Modalidad de Pago (1: Efectivo     2: Cheque Gerencia)
											"TipoDocumentoProveedor"        INTEGER, 		-- Tipo de documento del proveedor
											"NumeroDocumentoProveedor"      NVARCHAR(12), 	-- Numero de documento del proveedor
											"CorrelativoDocumentoProveedor" NVARCHAR(3), 	-- Correlativo de documento del proveedor
											"NombreProveedor"               NVARCHAR(75), 	-- Nombre del Proveedor
											"ReferenciaBeneficiario"        NVARCHAR(40), 	-- Referencia para el beneficiario proveedores
											"ReferenciaEmpresa"             NVARCHAR(20), 	-- Referencia para la empresa
											"MonedaImporteAbono"            NVARCHAR(4), 	-- 0001: Nuevos Soles   1001: Dólares
											"ImporteAbono"                  DECIMAL(26,8), 	-- Importe a Abonar
											"FlagValidarIDC"                NVARCHAR(1) 	-- Flag Validar IDC
										),
										

	 -- Datos para generar el archivo (TipoRegistro 3: Documento)  
	 OUT p_out_Data_TipoRegistro3       TABLE (
		 									"TipoRegistroOptimus"   		INTEGER,  		-- Este campo debe ir en todas las tablas de salida y su valor concordar con el tipo de registro
		 									"IdInterno"                     INTEGER,        -- Id interno para relacionar el registro 2(detalle) con registro 3(Documento)
											"TipoRegistro"                  NVARCHAR(1), 	-- 3: Documento											
											"TipoDocumentoPagar"            NVARCHAR(1), 	-- F											
											"NumeroDocumentoPagar"          BIGINT, 	    -- Numero de Documento a Pagar
											"ImporteDocumentoPagar"         DECIMAL(26,8) 	-- Importe del documento a Pagar
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

		/******** SALIDA PARA PRUEBAS  *********
		p_out_Data_TipoRegistro1  =		
		SELECT
			1 AS "TipoRegistroOptimus", -- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado
			'1' AS "TipoRegistro",      -- 1: Cabecera
			2 AS "CantidadAbonos", 
			'2017-11-04' AS "FechaProceso", -- (el job formateará la fecha según el formato configurado)
			'C' AS "TipoCuentaCargo",
			'0001' AS "MonedaCuentaCargo", 
			'9999999' AS "NumeroCuentaCargo",  
			8888 AS "MontoTotal",
			'7777' AS "ReferenciaPlanilla",
			'' AS "FlagExoneracionITF",
			6666 AS "TotalControl"		
		FROM DUMMY;
		
											
		p_out_Data_TipoRegistro2  =		
		SELECT
			2 AS "TipoRegistroOptimus",   		  		-- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado
			'2' AS "TipoRegistro",                   	-- 2: Detalle
			'C' AS "TipoCuentaAbono",                	-- A: Cuenta Ahorro   C:Cuenta Corriente   M:Cuenta Maestra
			'1111' AS "NumeroCuentaAbono",             	-- Numero de la cuenta de Abono
			1 AS "ModalidadDePago",               		-- Modalidad de Pago (1: Efectivo     2: Cheque Gerencia)
			2 AS "TipoDocumentoProveedor",        		-- Tipo de documento del proveedor
			'2222' AS "NumeroDocumentoProveedor",      	-- Numero de documento del proveedor
			'333' AS "CorrelativoDocumentoProveedor", 	-- Correlativo de documento del proveedor
			'JORGE VILLAGRAN' AS "NombreProveedor",    	-- Nombre del Proveedor
			'444' AS "ReferenciaBeneficiario",        	-- Referencia para el beneficiario proveedores
			'555' AS "ReferenciaEmpresa",              	-- Referencia para la empresa
			'0001' AS "MonedaImporteAbono",            	-- 0001: Nuevos Soles   1001: Dólares
			7777 AS "ImporteAbono",                  	-- Importe a Abonar
			'' AS "FlagValidarIDC"                		-- Flag Validar IDC				
		FROM DUMMY;
		
		p_out_Data_TipoRegistro3  =		
		SELECT
			3 AS "TipoRegistroOptimus",   		  		-- Este campo debe ir en todas las tablas de salida y su valor concordar con el tipo de registro
			'3' AS "TipoRegistro",                   	-- 3: Documento
			'F' AS "TipoDocumentoPagar",                -- F
			888 AS "NumeroDocumentoPagar",             	-- Numero de Documento a Pagar
			999.99 AS "ImporteDocumentoPagar",          -- Importe del documento a Pagar
		FROM DUMMY;		
		**************************************/

		
		-- Data de salida para generar el archivo (TipoRegistro 1: Cabecera)		
		p_out_Data_TipoRegistro1  =		
		SELECT
			1 AS "TipoRegistroOptimus", -- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado
			'1' AS "TipoRegistro",      -- 1: Cabecera
			CNTD."CountNominaTransferenciaDetalle" AS "CantidadAbonos",
			NT."FechaMovimiento" AS "FechaProceso", -- (el job formateará la fecha según el formato configurado)
			
			(CASE
				WHEN (NT."IdTipoCuentaBanco" = 'CUCO') THEN 'C'
				ELSE ''
			END) AS "TipoCuentaCargo",    -- C: Cuenta Corriente    M:Cuenta Mandante			
 
			(CASE
				WHEN (NT."IdMoneda" = 'PEN') THEN '0001'
				WHEN (NT."IdMoneda" = 'USD') THEN '1001' 
				ELSE ''
			END) AS "MonedaCuentaCargo",   			-- 0001: Nuevos Soles   1001: Dólares
			NT."NumeroCuentaBancaria" AS "NumeroCuentaCargo",  
			NT."Monto" AS "MontoTotal",
			'' AS "ReferenciaPlanilla",
			'' AS "FlagExoneracionITF",
			0  AS "TotalControl" -- ?
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS NT
		LEFT JOIN (
			SELECT "IdNominaTransferencia" , COUNT("IdNominaTransferencia") AS "CountNominaTransferenciaDetalle"
			FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"
			WHERE "IdEstado" <> 'NULL'
			GROUP BY "IdNominaTransferencia"
		) AS CNTD ON (CNTD."IdNominaTransferencia" = NT."IdNominaTransferencia") 
			
		WHERE NT."IdNominaTransferencia" = :p_IdNominaTransferencia
		AND NT."EsTransferencia" = 'S'
		AND NT."IdEstado" <> 'ANU';
		

		-- Data de salida para generar el archivo (TipoRegistro 2: Detalle)
		p_out_Data_TipoRegistro2  =
		SELECT
			2 AS "TipoRegistroOptimus",   		  		-- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado
			NTD."IdComprobante" AS "IdInterno",        	-- Id interno para relacionar el registro 2(detalle) con registro 3(Documento)
			'2' AS "TipoRegistro",                   	-- 2: Detalle
			(CASE
				WHEN (NTD."IdTipoCuentaBancariaSocio" = 'CUCO') THEN 'C'
				WHEN (NTD."IdTipoCuentaBancariaSocio" = 'CUAH') THEN 'A'
				ELSE ''
			END) AS "TipoCuentaAbono",    -- A: Cuenta Ahorro   C:Cuenta Corriente   M:Cuenta Maestra
			NTD."NumeroCuentaBancariaSocio" AS "NumeroCuentaAbono",             	-- Numero de la cuenta de Abono
			(CASE
				WHEN (NTD."IdMedioDePago" = 'TBC') THEN 1
				WHEN (NTD."IdMedioDePago" = 'INT') THEN 1 
				WHEN (NTD."IdMedioDePago" = 'GYT') THEN 1
				WHEN (NTD."IdMedioDePago" = 'CHG') THEN 2
				ELSE 0
			END) AS "ModalidadDePago",   			-- Modalidad de Pago (1: Efectivo     2: Cheque Gerencia)
			(CASE 
				WHEN DI."IdTipoDocumentoIdentidadAsociado" = 'RUC' THEN 6
				WHEN DI."IdTipoDocumentoIdentidadAsociado" = 'DNI' THEN 5
				ELSE 0
			END) AS "TipoDocumentoProveedor",   -- Tipo de documento del proveedor
			(CASE 
				WHEN DI."IdTipoDocumentoIdentidadAsociado" = 'RUC' THEN "DocumentoIdentidadAsociado"
				WHEN DI."IdTipoDocumentoIdentidadAsociado" = 'DNI' THEN '10' || "DocumentoIdentidadAsociado"
			END) AS "NumeroDocumentoProveedor",   -- Numero de documento del proveedor (es el documento identidad)
			'' AS "CorrelativoDocumentoProveedor", 	-- Correlativo de documento del proveedor
			NTD."NombrePersona" AS "NombreProveedor",    	-- Nombre del Proveedor			
			'Referencia Beneficiario ' || "DocumentoIdentidadAsociado" AS "ReferenciaBeneficiario",     -- Referencia para el beneficiario proveedores
			'Ref Emp '  || "DocumentoIdentidadAsociado" AS "ReferenciaEmpresa",              			-- Referencia para la empresa
			(CASE
				WHEN (NTD."IdMoneda" = 'PEN') THEN '0001'
				WHEN (NTD."IdMoneda" = 'USD') THEN '1001' 
				ELSE ''
			END) AS "MonedaImporteAbono",   			-- 0001: Nuevos Soles   1001: Dólares
			"Monto" AS "ImporteAbono",                  -- Importe a Abonar
			'N' AS "FlagValidarIDC"                		-- Flag Validar IDC
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" AS NTD
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON (DI."IdTipoDocumentoIdentidad" = 'IUP' AND DI."IdPersona" = NTD."IdPersona")		
		WHERE NTD."IdNominaTransferencia" = :p_IdNominaTransferencia
		AND NTD."EsTransferencia" = 'S'
		AND NTD."IdEstado" <> 'ANU';
		
		

		p_out_Data_TipoRegistro3  =		
		SELECT
			3 AS "TipoRegistroOptimus",   		  				-- Este campo debe ir en todas las tablas de salida y su valor concordar con el tipo de registro			
			NTD."IdComprobante" AS "IdInterno",        			-- Id interno para relacionar el registro 2(detalle) con registro 3(Documento)
			'3' AS "TipoRegistro",                   			-- 3: Documento
			'F' AS "TipoDocumentoPagar",                		-- F
			NTD."IdComprobante" AS "NumeroDocumentoPagar",  	-- Numero de Documento a Pagar
			NTD."Monto" AS "ImporteDocumentoPagar"          	-- Importe del documento a Pagar
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" AS NTD
		WHERE NTD."IdNominaTransferencia" = :p_IdNominaTransferencia
		AND   NTD."EsTransferencia" = 'S'
		AND   NTD."IdEstado" <> 'ANU';

		
		
  	END;    
END;



