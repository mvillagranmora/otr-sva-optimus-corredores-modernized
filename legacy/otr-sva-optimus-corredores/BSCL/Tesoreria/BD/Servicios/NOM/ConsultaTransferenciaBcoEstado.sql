SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.NOM::ConsultaTransferenciaBcoEstado"
CREATE PROCEDURE
-- 
--  Proposito: Obtiene data para la generación de la carta de transferencias bancarias.
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 11/08/2016
--
"BSCL.Tesoreria.BD.Servicios.NOM::ConsultaTransferenciaBcoEstado" (

     IN p_IdEmpresa 					BIGINT,  
     IN p_IdNominaTransferencia         BIGINT,
     IN p_idBanco                       BIGINT,
	 IN p_idMoneda                      NVARCHAR(5),
	 IN p_idMedioDePago                 NVARCHAR(5),
	 
	 
	 -- Datos para generar el archivo (TipoRegistro 1: Cabecera)  
	 OUT p_out_Data_TipoRegistro1       TABLE (
											"TipoRegistroOptimus"   		INTEGER,  	 	-- Este campo debe ir en todas las tablas de salida y su valor concordar con el tipo de registro
											"TipoRegistro"          		NVARCHAR(1), 	-- 1: cabecera
											"Relleno"                       NVARCHAR,
											"NumeroNomina"        		    BIGINT, 		
											"RutOrdenate"       			NVARCHAR(12), 	
											"NombreOrdenate"       		    NVARCHAR(70),	
											"NumeroConvenio"     		    NVARCHAR(16),	
											"CuentaCargo"				    NVARCHAR(34),   
											"MonedaNomina" 					NVARCHAR(3),	
											"FechaOperacion"    		    NVARCHAR(10),   
											"NombreUsuario" 			    NVARCHAR(20)	
										),
										
	 -- Datos para generar el archivo (TipoRegistro 2: Detalle)  
	 OUT p_out_Data_TipoRegistro2       TABLE (
		 									"TipoRegistroOptimus"   		INTEGER,  		-- Este campo debe ir en todas las tablas de salida y su valor concordar con el tipo de registro
		 									"IdInterno"                     BIGINT,        -- Id interno para relacionar el registro 2(detalle) con registro 3(Documento) 
											"TipoRegistro"                  NVARCHAR(1), 	-- 2: Detalle
											"TipoDePago"                    NVARCHAR(4), 	
											"RefDetallePago"                NVARCHAR(16), 	
											"RutBeneficiario"               NVARCHAR(40), 		
											"NombreBeneficiario"            NVARCHAR(300), 		
											"MontoRemesa"                   DECIMAL, 	
											"CtaCteBeneficiario"            NVARCHAR(34), 	
											"BancoDelBeneficiario"          NVARCHAR(40), 	
											"InformacionRemesa"             NVARCHAR(70), 	
											"CodigoDepDcvComprador"         NVARCHAR(40), 	
											"CodigoDepDcvVendedor"          NVARCHAR(520), 	
											"ClaveMedioPago"                NVARCHAR(50) 	
										),
										
	 OUT p_out_Data_TipoRegistro3       TABLE (
		 									"TipoRegistroOptimus"   		INTEGER,  		-- Este campo debe ir en todas las tablas de salida y su valor concordar con el tipo de registro
		 									"IdInterno"                     INTEGER,        -- Id interno para relacionar el registro 2(detalle) con registro 3(Documento)
											"TipoRegistro"                  NVARCHAR(1), 	-- 3: Documento											
											"TotalDetalles"                 DECIMAL, 	
											"relleno1"                      NVARCHAR(16),
											"relleno2"                      NVARCHAR(12),
											"relleno3"                      NVARCHAR(70),
											"MontoNomina"                   INTEGER 	
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
		

        -- salida para archivo
    
	p_out_Data_TipoRegistro1 = 
    SELECT 
           1                             AS "TipoRegistroOptimus"
          ,'1'                           AS "TipoRegistro"
          ,' '                           AS "Relleno"
          ,NT."IdNominaTransferencia"    AS "NumeroNomina"
          ,DGEN."RUT"                    AS "RutOrdenate"
          ,DGEN."RazonSocial"            AS "NombreOrdenate"
          ,' '                           AS "NumeroConvenio"
          ,NT."NumeroCuentaBancaria"     AS "CuentaCargo"
          ,NT."IdMoneda"                 AS "MonedaNomina"
          ,NT."FechaLiquidacion"           AS "FechaOperacion" -- Revisar si es la fecha que corresponde
          ,'USU-PROC-FINAN'              AS "NombreUsuario"
    FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS NT
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS DGEN ON (DGEN."IdEmpresa" = NT."IdEmpresa")
	WHERE NT."IdNominaTransferencia" = :p_IdNominaTransferencia;
  
 p_out_Data_TipoRegistro2 =
    SELECT 
          2                               AS "TipoRegistroOptimus"
         ,NTD."IdComprobante"             AS "IdInterno"  
         ,'2'                             AS "TipoRegistro"
         ,(CASE 	WHEN NT."IdMedioDePago" = 'TDP' THEN  'DCV'
					WHEN NT."IdMedioDePago" = 'TRA' AND NTD."IdBancoCorredora" = "IdBancoSocio" THEN 'CCT'
					WHEN NT."IdMedioDePago" = 'TRA' AND NTD."IdBancoCorredora" != "IdBancoSocio" THEN 'ITF'
					WHEN NT."IdMedioDePago" = 'TRA' AND NTD."IdBancoSocio" = 14 AND NTD."DocumentoIdentidad" = '97029000-1' THEN 'CSBC'
					WHEN NT."IdMedioDePago" = 'TRA' AND NTD."IdBancoSocio" = 14 THEN 'AVAP'
					ELSE ' ' END)            AS "TipoDePago"
         ,' '                             AS "RefDetallePago"
         ,NTD."DocumentoIdentidad"        AS "RutBeneficiario"
         ,NTD."NombrePersona"             AS "NombreBeneficiario"
         ,NTD."Monto"                     AS "MontoRemesa"
         ,' '                             AS "CtaCteBeneficiario"
         ,BNC."CodigoBanco"               AS "BancoDelBeneficiario"
         ,' '                             AS "InformacionRemesa"
    	 ,NTD."CuentaDCVDestino"          AS "CodigoDepDcvComprador"
	     ,NTD."CuentaDCVOrigen"			  AS "CodigoDepDcvVendedor"
		 ,NTD."ClaveDCV"				  AS "ClaveMedioPago"
    FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS NT
    INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" AS NTD ON (NT."IdNominaTransferencia" = NTD."IdNominaTransferencia")
    INNER JOIN "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS BNC ON (NTD."NombreBancoSocio" = BNC."NombreBanco")
	WHERE NT."IdNominaTransferencia" = :p_IdNominaTransferencia;

 p_out_Data_TipoRegistro3 =
    SELECT
           3                        AS "TipoRegistroOptimus"   		  							
		  ,NTD."IdComprobante"      AS "IdInterno"        			
		  ,'3'                      AS "TipoRegistro"                   			
          ,to_decimal(count(NTD."IdNominaTransferencia"),5,0)                 AS "TotalDetalles"
          ,' '                      AS "relleno1"
          ,' '                      AS "relleno2"
          ,' '                      AS "relleno3"
          ,NT."Monto"	            AS "MontoNomina"
    FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS NT
    INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" AS NTD ON (NT."IdNominaTransferencia" = NTD."IdNominaTransferencia")
	WHERE NT."IdNominaTransferencia" = :p_IdNominaTransferencia
    GROUP BY NTD."IdNominaTransferencia",NTD."IdComprobante",NT."Monto";
   
 
    END;
END;