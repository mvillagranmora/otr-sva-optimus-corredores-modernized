SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearDeposito";

CREATE PROCEDURE
-- 
--  Proposito: Validar documentos para depositar
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 29/03/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearDeposito"(
    IN 	p_DocumentosADepositar	TABLE("NumeroDeposito" 							BIGINT,
    								  "SimularOperacion"						NVARCHAR(1),
    								  "IdCuentaBancariaCorredoraInformado" 		BIGINT, 
    								  "IdComprobanteFormaDePagoInformado" 		BIGINT,
    								  "IdCuentaBanco"							BIGINT,
    								  "IdBanco"									BIGINT,
									  "NombreCuentaBancaria"					NVARCHAR(100),
									  "NumeroCuentaBancaria"					NVARCHAR(100),
									  "IdTipoCuentaBancaria"					NVARCHAR(4),
									  "NombreTipoCuentaBancaria"				NVARCHAR(100),
    								  "IdMonedaCuentaBanco"						NVARCHAR(3),
    								  "IdEstadoCuentaBanco"						NVARCHAR(3),
    								  "IdComprobanteFormaDePago"				BIGINT,
									  "IdCuentaBancariaSocio"					BIGINT,
									  "IdBancoSocio"							BIGINT,
									  "NumeroSerieSocio"						NVARCHAR(40),
									  "NumeroDocumentoSocio"					BIGINT,
									  "FechaDocumentoPagoSocio"					NVARCHAR(40),
									  
									  "IdMonedaFormaDePago"						NVARCHAR(3),
									  "MontoFormaDePago"						DECIMAL(26,8),
									  "IdEstadoFormaDePago"						NVARCHAR(3),
									  "IdTipoMovimiento"						NVARCHAR(6),
							 		  "IdMedioDePago"							NVARCHAR(3),
							 		  "TipoMedioDePago"							NVARCHAR(3),
							 		  "EstadosPosibleTipoMovimientoMedioDePago"	NVARCHAR(40),
							 		  "IdPlaza"									BIGINT,
								 	  "PlazoRetencionMismoBanco"				BIGINT,
								 	  "PlazoRetencionOtroBanco"					BIGINT,
								 	  "IdTipoDeposito"							NVARCHAR(6)),
                                    		 
	OUT p_out_Resultado			TABLE("NumeroDeposito"	BIGINT,									   	   									   
	   								  "Codigo"			NVARCHAR(100),
	   								  "Resultado"		NVARCHAR(4))	--OK   = Cumple todos los requisitos,		
																		--ADVE = 
						                                                --RECH = Alguna de lAS reglAS detienen la operacion
																		--RAUT = Requiere autorizacion
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA BSCL
AS
BEGIN

	--1) A nivel de comprobante
	v_MatrizValidaciones = 
	 SELECT  DISTINCT
	 		 TC."NumeroDeposito"
	 		,"SimularOperacion"
	 		,"IdCuentaBancariaCorredoraInformado"
		    ,"IdComprobanteFormaDePagoInformado"
		    ,"IdCuentaBanco"
		    ,"IdBanco"
		    ,"IdMonedaCuentaBanco"
    		,"IdEstadoCuentaBanco"
		  	,"IdComprobanteFormaDePago"
		  	,"IdCuentaBancariaSocio"
		  	,"IdBancoSocio"
		  	,"NumeroSerieSocio"
			,"NumeroDocumentoSocio"
			,"FechaDocumentoPagoSocio"
		  	,"IdMonedaFormaDePago"
		  	,"MontoFormaDePago"
		  	,"IdEstadoFormaDePago"
		  	,"IdTipoMovimiento"
			,"IdMedioDePago"
			,"TipoMedioDePago"
			,"EstadosPosibleTipoMovimientoMedioDePago"
			,"CantidadDistintaMoneda"
			,"CantidadDistintaMedioPago"
	 		,1 as "SIN_ESPECIFICAR_DOCUMENTOS_DEPOSITAR"	 		
	 		,1 as "SIN_ESPECIFICAR_CUENTA_BANCARIA_CORREDORA"
	 		,1 as "CUENTA_BANCARIA_NO_EXISTE"
	 		,1 as "CUENTA_BANCARIA_INACTIVA"	 		
			,CASE WHEN "TipoMedioDePago" NOT IN ('EFE','CHE','VVI') THEN 1 ELSE 0 END as "SIN_ESPECIFICAR_CUENTA_BANCARIA_SOCIO"			
	 		,1 as "FORMA_PAGO_NO_EXISTE"	 		
	 		,1 as "FORMA_PAGO_COMPROBANTE_ANULADA"	 		
	 		,1 as "FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO"
	 		,CASE WHEN "IdTipoMovimiento" = 'MOVING' AND "TipoMedioDePago" <> 'EFE' THEN 1 ELSE 0 END as "FORMA_PAGO_COMPROBANTE_LIQUIDADA"
	 		,CASE WHEN "IdTipoMovimiento" = 'MOVING' AND "TipoMedioDePago" <> 'EFE' THEN 1 ELSE 0 END as "FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR"
	 		,1 as "FORMA_PAGO_NO_CREA_NOMINA_DEPOSITO"
	 		,1 as "DISTINTA_MONEDA_ENTRE_CUENTA_Y_FORMA_PAGO"
	 		,CASE WHEN "TipoMedioDePago" = 'CHE' THEN 1 ELSE 0 END as "SIN_INFORMACION_NUMERO_CHEQUE_SOCIO"
			,CASE WHEN "TipoMedioDePago" = 'CHE' THEN 1 ELSE 0 END as "SIN_FECHA_EMISION_DOCUMENTO_SOCIO"
	 		,1 as "MONEDA_COMPROBANTES_NO_COINCIDE"
	 		--,1 as "MEDIO_DE_PAGOS_NO_COINCIDEN_DEPOSITO"
	   FROM :p_DocumentosADepositar as TC
	   LEFT JOIN (SELECT "NumeroDeposito",COUNT(DISTINCT "IdMonedaFormaDePago") AS "CantidadDistintaMoneda" 
	   				FROM :p_DocumentosADepositar 
	   			   GROUP BY "NumeroDeposito") as CMo ON CMo."NumeroDeposito" = TC."NumeroDeposito"
	   LEFT JOIN (SELECT "NumeroDeposito",COUNT(DISTINCT "TipoMedioDePago") AS "CantidadDistintaMedioPago"
	   				FROM :p_DocumentosADepositar 
	   			   GROUP BY "NumeroDeposito") as CMe ON CMe."NumeroDeposito" = TC."NumeroDeposito";


	v_Validaciones_v1 =
	 SELECT  "NumeroDeposito"
	 		,CASE WHEN "SIN_ESPECIFICAR_CUENTA_BANCARIA_CORREDORA" = 1 AND "SimularOperacion" = 'N' AND COALESCE("IdCuentaBancariaCorredoraInformado",0) = 0 THEN 'RECH' ELSE 'OK' END "SIN_ESPECIFICAR_CUENTA_BANCARIA_CORREDORA"
			,CASE WHEN "CUENTA_BANCARIA_NO_EXISTE" = 1 AND "SimularOperacion" = 'N' AND COALESCE("IdCuentaBanco",0) = 0 THEN 'RECH' ELSE 'OK' END "CUENTA_BANCARIA_NO_EXISTE"
	 		,CASE WHEN "CUENTA_BANCARIA_INACTIVA" = 1 AND "SimularOperacion" = 'N' AND "IdEstadoCuentaBanco" = '' THEN 'RECH' ELSE 'OK' END "CUENTA_BANCARIA_INACTIVA"			
			,CASE WHEN "SIN_ESPECIFICAR_CUENTA_BANCARIA_SOCIO" = 1 AND COALESCE("IdCuentaBancariaSocio",0) = 0 THEN 'RECH' ELSE 'OK' END "SIN_ESPECIFICAR_CUENTA_BANCARIA_SOCIO"			 			 		
	 		,CASE WHEN "SIN_ESPECIFICAR_DOCUMENTOS_DEPOSITAR" = 1 AND COALESCE("IdComprobanteFormaDePagoInformado",0) = 0 THEN 'RECH' ELSE 'OK' END "SIN_ESPECIFICAR_DOCUMENTOS_DEPOSITAR"
	 		,CASE WHEN "FORMA_PAGO_NO_EXISTE" = 1 AND COALESCE("IdComprobanteFormaDePago",0) = 0 THEN 'RECH' ELSE 'OK' END "FORMA_PAGO_NO_EXISTE"
	 		,CASE WHEN "FORMA_PAGO_COMPROBANTE_ANULADA" = 1 AND "IdEstadoFormaDePago" = 'ANU' THEN 'RECH' ELSE 'OK' END "FORMA_PAGO_COMPROBANTE_ANULADA" 
	 		,CASE WHEN "FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO" = 1 AND "IdEstadoFormaDePago" = 'NDP' THEN 'RECH' ELSE 'OK' END "FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO"
	 		,CASE WHEN "FORMA_PAGO_COMPROBANTE_LIQUIDADA" = 1 AND "IdEstadoFormaDePago" = 'LIQ' THEN 'RECH' ELSE 'OK' END "FORMA_PAGO_COMPROBANTE_LIQUIDADA"
	 		,CASE WHEN "FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR" = 1 AND instr("EstadosPosibleTipoMovimientoMedioDePago",'CRT') > 1 AND "IdEstadoFormaDePago" NOT IN ('CRT','EBV','NDP') THEN 'RECH' ELSE 'OK' END "FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR"	 		
	 		,CASE WHEN "FORMA_PAGO_NO_CREA_NOMINA_DEPOSITO" = 1 AND instr("EstadosPosibleTipoMovimientoMedioDePago",'NDP') = 0 THEN 'RECH' ELSE 'OK' END "FORMA_PAGO_NO_CREA_NOMINA_DEPOSITO"
	 		,CASE WHEN "DISTINTA_MONEDA_ENTRE_CUENTA_Y_FORMA_PAGO" = 1 AND "SimularOperacion" = 'N' AND "IdMonedaCuentaBanco" <> "IdMonedaFormaDePago" THEN 'RECH' ELSE 'OK' END "DISTINTA_MONEDA_ENTRE_CUENTA_Y_FORMA_PAGO"	 		
	 		,CASE WHEN "SIN_INFORMACION_NUMERO_CHEQUE_SOCIO" = 1 AND COALESCE("NumeroDocumentoSocio",0) = 0 THEN 'RECH' ELSE 'OK' END as "SIN_INFORMACION_NUMERO_CHEQUE_SOCIO"
			,CASE WHEN "SIN_FECHA_EMISION_DOCUMENTO_SOCIO" = 1 AND "FechaDocumentoPagoSocio" = '0001-01-01' THEN 'RECH' ELSE 'OK'END as "SIN_FECHA_EMISION_DOCUMENTO_SOCIO"
	  FROM :v_MatrizValidaciones;  
	  
	v_Validaciones_v2 =
	 SELECT  "NumeroDeposito"
	 		,CASE WHEN "MONEDA_COMPROBANTES_NO_COINCIDE" = 1 AND "CantidadDistintaMoneda" > 1 THEN 'RECH' ELSE 'OK' END "MONEDA_COMPROBANTES_NO_COINCIDE"
	 		--,CASE WHEN "MEDIO_DE_PAGOS_NO_COINCIDEN_DEPOSITO" = 1 AND "CantidadDistintaMedioPago" > 1 THEN 'RECH' ELSE 'OK' END "MEDIO_DE_PAGOS_NO_COINCIDEN_DEPOSITO" 
	  FROM :v_MatrizValidaciones
	 GROUP BY "NumeroDeposito","MONEDA_COMPROBANTES_NO_COINCIDE","CantidadDistintaMoneda","CantidadDistintaMedioPago";
	
	
	
	p_out_Resultado =   
	 SELECT "NumeroDeposito",
	 		"Codigo",
			CAST("Resultado" as NVARCHAR(4)) as "Resultado"
	   FROM (
			 SELECT "NumeroDeposito",
			 		COALESCE(			 			
			 			 "1" ||"SIN_ESPECIFICAR_CUENTA_BANCARIA_CORREDORA"
				 		,"2" ||"CUENTA_BANCARIA_NO_EXISTE"
				 		,"3" ||"CUENTA_BANCARIA_INACTIVA"				 			 		
				 		,"4" ||"SIN_ESPECIFICAR_CUENTA_BANCARIA_SOCIO"				 		
				 		,"5" ||"SIN_ESPECIFICAR_DOCUMENTOS_DEPOSITAR"
				 		,"6" ||"FORMA_PAGO_NO_EXISTE"
				 		,"7" ||"FORMA_PAGO_COMPROBANTE_ANULADA"
				 		,"8" ||"FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO"
				 		,"9" ||"FORMA_PAGO_COMPROBANTE_LIQUIDADA"				 		
				 		,"10" ||"FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR"
				 		,"11" ||"FORMA_PAGO_NO_CREA_NOMINA_DEPOSITO"
				 		,"12" ||"DISTINTA_MONEDA_ENTRE_CUENTA_Y_FORMA_PAGO"
				 		,"13" ||"SIN_INFORMACION_NUMERO_CHEQUE_SOCIO"
						,"14" ||"SIN_FECHA_EMISION_DOCUMENTO_SOCIO"
			 		) AS "Resultado",
			 		COALESCE(			 			
			 			 "1" ||'SIN_ESPECIFICAR_CUENTA_BANCARIA_CORREDORA'
				 		,"2" ||'CUENTA_BANCARIA_NO_EXISTE'
				 		,"3" ||'CUENTA_BANCARIA_INACTIVA'				 			 		
				 		,"4" ||'SIN_ESPECIFICAR_CUENTA_BANCARIA_SOCIO'				 	
				 		,"5" ||'SIN_ESPECIFICAR_DOCUMENTOS_DEPOSITAR'
				 		,"6" ||'FORMA_PAGO_NO_EXISTE'
				 		,"7" ||'FORMA_PAGO_COMPROBANTE_ANULADA'
				 		,"8" ||'FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO'
				 		,"9" ||'FORMA_PAGO_COMPROBANTE_LIQUIDADA'
				 		,"10" ||'FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR'
				 		,"11" ||'FORMA_PAGO_NO_CREA_NOMINA_DEPOSITO'
				 		,"12" ||'DISTINTA_MONEDA_ENTRE_CUENTA_Y_FORMA_PAGO'
				 		,"13" ||'SIN_INFORMACION_NUMERO_CHEQUE_SOCIO'
						,"14" ||'SIN_FECHA_EMISION_DOCUMENTO_SOCIO'
			 		) AS "Codigo"
			   FROM :v_Validaciones_v1,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL 
	  UNION ALL
	 SELECT "NumeroDeposito",
	 		"Codigo",
			CAST("Resultado" as NVARCHAR(4)) as "Resultado"
	   FROM (
			 SELECT "NumeroDeposito",
			 		--COALESCE(
				 		 "1" ||"MONEDA_COMPROBANTES_NO_COINCIDE"
				 		--,"2" ||"MEDIO_DE_PAGOS_NO_COINCIDEN_DEPOSITO"	 			
			 		--) 
			 		AS "Resultado",
			 		--COALESCE(			 			
				 		 "1" ||'MONEDA_COMPROBANTES_NO_COINCIDE'
				 		--,"2" ||'MEDIO_DE_PAGOS_NO_COINCIDEN_DEPOSITO'
			 		--) 
			 		AS "Codigo"
			   FROM :v_Validaciones_v2,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL
	  ;
END;
