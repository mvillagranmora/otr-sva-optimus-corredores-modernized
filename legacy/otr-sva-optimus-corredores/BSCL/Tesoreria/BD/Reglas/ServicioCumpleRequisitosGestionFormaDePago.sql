SET SCHEMA "BSCL";
GO 
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGestionFormaDePago";
GO
CREATE PROCEDURE
-- 
--  Proposito: Validar si movimientos cumplen con los requisitos solicitados
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 12/05/2016
--     	
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGestionFormaDePago"(                                                		
	IN p_TipoEjecucion			NVARCHAR(3), 	-- ING 	: Ingreso de formas de pago
												-- CRT	: Certificacion de formas de pago
												-- EBV	: Envio a bóveda de las formas de pago
												-- EMI	: Forma de pago emitida (se generó el cheque o vale vista)
												-- ANU  : Anulacion de formas de pago

	IN p_FormasDePago			TABLE ("IdInterno"								BIGINT,
									   "IdComprobanteInformado"					BIGINT,
									   "IdComprobante"							BIGINT,
									   "MontoComprobante"						DECIMAL(26,8),
									   "IdEstadoComprobante"					NVARCHAR(3),
									   
   									   "IdComprobanteFormaDePagoInformado"		BIGINT,
   									   "CantidadFormaDePagoInformadas"			BIGINT,
   									   "MontoFormaDePagoInformadas"				DECIMAL(26,8),
   									   "IdComprobanteFormaDePago"				BIGINT,
   									   "IdTipoMovimiento" 						NVARCHAR(6),
   									   "IdEstadoComprobanteFormaDePago"			NVARCHAR(3),
   									   "IdTipoMovimientoInformado" 				NVARCHAR(6), -- IdTipoMovimiento: MOVING, MOVEGR

									   "IdMedioDePagoInformado"					NVARCHAR(3),
									   "IdMedioDePago"							NVARCHAR(3),
									   "TipoMedioDePago"						NVARCHAR(3),
									   "EstadosPosibleTipoMovimientoMedioDePago" NVARCHAR(40),
								 	   "IdModalidadDePago"						NVARCHAR(6),
								 	   "IdMoneda"								NVARCHAR(3),
									   "Monto"									DECIMAL(26,8),											  
									   "IdCuentaBancariaCorredora"				BIGINT,
									   "IdTarjetaBancariaCorredora"				BIGINT,
									   "IdChequera"								BIGINT,
									   "NumeroDocumentoCorredora"				BIGINT,
									   "FechaDocumentoPagoCorredora"			NVARCHAR(40),

									   "IdCuentaBancariaSocio" 					BIGINT,
									   "IdBancoSocio"							BIGINT,
									   "NombreCuentaBancariaSocio"				NVARCHAR(100),
									   "NumeroCuentaBancariaSocio"				NVARCHAR(100),
									   "IdTipoCuentaBancariaSocio"				NVARCHAR(4),
									   "NombreTipoCuentaBancariaSocio"			NVARCHAR(100),
									  
									   "IdTarjetaBancariaSocio"					BIGINT,
									   "IdTipoTarjetaBancariaSocio"				NVARCHAR(40),
									   "NombreTipoTarjetaBancariaSocio"			NVARCHAR(100),
									   "NumeroTarjetaBancariaSocio"				NVARCHAR(100),
									   "NumeroSerieSocio"						NVARCHAR(40),
									   "NumeroDocumentoSocio"					BIGINT,
									   "FechaDocumentoPagoSocio"				NVARCHAR(40),									  
									   "CertificadoAutomatico"					NVARCHAR(1),
									   "IdCaja"									BIGINT),
									   
	IN p_InformacionCheques		TABLE ("IdChequera"					BIGINT,
									   "ChequeDesde"				BIGINT,
									   "ChequeHasta"				BIGINT,
									   "NumeroChequeActual"			BIGINT,
									   "CantidadCheques"			BIGINT),
                                                		
	OUT p_out_Resultado			TABLE ("IdInterno"					BIGINT,
									   "IdComprobanteInformado"		BIGINT,
	   								   "TipoEjecucion" 				NVARCHAR(3),	   									   
	   								   "Codigo"						NVARCHAR(100),
	   								   "Resultado"					NVARCHAR(4))	--OK   = Cumple todos los requisitos,		
																					--ADVE = 
						                                                			--RECH = Alguna de lAS reglAS detienen la operacion
																					--RAUT = Requiere autorizacion
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	
	--**************************************************************************************************
	--********************************* VALIDACIONES Y REGLAS  *****************************************
	--**************************************************************************************************
	v_MatrizValidaciones = 
	SELECT  DISTINCT
			 "IdInterno"
			,:p_TipoEjecucion as "TipoEjecucion"
			,"IdComprobanteInformado"
			,"IdComprobante"
			,"MontoComprobante"
			,"IdEstadoComprobante"
			,"IdComprobanteFormaDePagoInformado"
			,"CantidadFormaDePagoInformadas"
   			,"MontoFormaDePagoInformadas"
   			,"IdComprobanteFormaDePago"
   			,"IdTipoMovimiento"
   			,"IdEstadoComprobanteFormaDePago"
   			,"IdTipoMovimientoInformado"
   			,"IdMedioDePagoInformado"
		  	,"IdMedioDePago"
		  	,"TipoMedioDePago"
		  	,"EstadosPosibleTipoMovimientoMedioDePago"
	 	  	,"IdModalidadDePago"
	 	  	,"IdMoneda"
		  	,"Monto"
		  	,"IdCuentaBancariaCorredora"
		  	,"IdTarjetaBancariaCorredora"
		  	,"IdChequera"
		  	,"NumeroDocumentoCorredora"
		  	,"FechaDocumentoPagoCorredora"
		  	,"IdCuentaBancariaSocio"
		  	,"IdBancoSocio"
		  	,"NombreCuentaBancariaSocio"
		  	,"NumeroCuentaBancariaSocio"
		  	,"IdTipoCuentaBancariaSocio"
		  	,"NombreTipoCuentaBancariaSocio"		 
		  	,"IdTarjetaBancariaSocio"
		  	,"IdTipoTarjetaBancariaSocio"
		  	,"NombreTipoTarjetaBancariaSocio"
		  	,"NumeroTarjetaBancariaSocio"
		  	,"NumeroSerieSocio"
		  	,"NumeroDocumentoSocio"
		  	,"FechaDocumentoPagoSocio"
		  	,"CertificadoAutomatico"
		  	,"IdCaja"
		  	
		  	--1) Validaciones a nivel de comprobante
			,1 as "COMPROBANTE_TESORERIA_NO_EXISTE"
			,1 as "COMPROBANTE_LIQUIDADO_NO_SE_PUEDE_EDITAR"
			,1 as "COMPROBANTE_BLOQUEADO_NO_SE_PUEDE_EDITAR"
			,1 as "COMPROBANTE_GIRADO_NO_SE_PUEDE_EDITAR"
			,CASE WHEN :p_TipoEjecucion = 'ING' THEN 1 ELSE 0 END as "COMPROBANTE_SIN_FORMA_DE_PAGO"
			,CASE WHEN :p_TipoEjecucion = 'ING' THEN 1 ELSE 0 END as "MONTO_MOVIMIENTO_NO_CUADRA"
			,CASE WHEN :p_TipoEjecucion = 'ING' THEN 1 ELSE 0 END as "CAJA_USUARIO_MONEDA_DESHABILITADA"
			
			
			--2) Validaciones a nivel de Forma de Pago								
			,CASE WHEN :p_TipoEjecucion IN ('CRT','ANU') THEN 1 ELSE 0 END as "FORMA_PAGO_CERTIFICADO_NO_SE_PUEDE_EDITAR"
			,CASE WHEN :p_TipoEjecucion IN ('CRT','ANU','EBV') THEN 1 ELSE 0 END as "FORMA_PAGO_NO_EXISTE"
			,CASE WHEN :p_TipoEjecucion IN ('CRT','ANU','EBV') THEN 1 ELSE 0 END as "FORMA_PAGO_LIQUIDADO_NO_SE_PUEDE_EDITAR"
			,CASE WHEN :p_TipoEjecucion IN ('CRT','ANU','EBV') THEN 1 ELSE 0 END as "FORMA_PAGO_GIRADO_NO_SE_PUEDE_EDITAR"
			,CASE WHEN :p_TipoEjecucion IN ('CRT','ANU','EBV') THEN 1 ELSE 0 END as "FORMA_PAGO_ANULADO_NO_SE_PUEDE_EDITAR"
			
			,CASE WHEN :p_TipoEjecucion = 'CRT' THEN 1 ELSE 0 END as "FORMA_PAGO_NO_PERMITE_SER_CERTIFICADA"
			,CASE WHEN :p_TipoEjecucion = 'EMI' THEN 1 ELSE 0 END as "FORMA_PAGO_NO_PERMITE_SER_EMITIDA"
			,CASE WHEN :p_TipoEjecucion = 'EBV' THEN 1 ELSE 0 END as "FORMA_PAGO_SIN_CERTIFICAR"			
			,CASE WHEN :p_TipoEjecucion = 'EMI' AND "IdTipoMovimiento" = 'MOVEGR' AND "TipoMedioDePago" = 'CHE' THEN 1 ELSE 0 END as "CHEQUE_FUE_UTILIZADO"
			,CASE WHEN :p_TipoEjecucion = 'EMI' AND "IdTipoMovimiento" = 'MOVEGR' AND "TipoMedioDePago" = 'CHE' THEN 1 ELSE 0 END as "SIN_INFORMACION_NUMERO_CHEQUE_ACTUAL"
			,CASE WHEN :p_TipoEjecucion = 'CRT' AND "IdTipoMovimiento" = 'MOVING' AND "TipoMedioDePago" = 'CHE' THEN 1 ELSE 0 END as "SIN_INFORMACION_NUMERO_CHEQUE_SOCIO" 
			,CASE WHEN :p_TipoEjecucion = 'CRT' AND "IdTipoMovimiento" = 'MOVING' AND "TipoMedioDePago" = 'CHE' THEN 1 ELSE 0 END as "SIN_FECHA_EMISION_DOCUMENTO_SOCIO" 			
	  FROM :p_FormasDePago as M;
	
	
	v_Validaciones_1 = 
	--1) Validaciones a nivel de comprobante
	SELECT   "IdInterno"
			,"TipoEjecucion"
			,"IdComprobanteInformado"
			,CASE WHEN "COMPROBANTE_TESORERIA_NO_EXISTE" = 1 AND (COALESCE("IdComprobante",0) = 0) THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_NO_EXISTE"
			,CASE WHEN "COMPROBANTE_LIQUIDADO_NO_SE_PUEDE_EDITAR" = 1 AND ("IdEstadoComprobante" = 'LIQ') THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_LIQUIDADO_NO_SE_PUEDE_EDITAR"
			,CASE WHEN "COMPROBANTE_BLOQUEADO_NO_SE_PUEDE_EDITAR" = 1 AND ("IdEstadoComprobante" = 'BLO') THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_BLOQUEADO_NO_SE_PUEDE_EDITAR"
			,CASE WHEN "COMPROBANTE_GIRADO_NO_SE_PUEDE_EDITAR" = 1 AND ("IdEstadoComprobante" = 'GIR') THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_GIRADO_NO_SE_PUEDE_EDITAR"			
			,CASE WHEN "COMPROBANTE_SIN_FORMA_DE_PAGO" = 1 AND (COALESCE("CantidadFormaDePagoInformadas",0) = 0) THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_SIN_FORMA_DE_PAGO"
			,CASE WHEN "MONTO_MOVIMIENTO_NO_CUADRA" = 1 AND (COALESCE("MontoComprobante",0) <> COALESCE("MontoFormaDePagoInformadas",0)) THEN 'RECH' ELSE 'OK' END AS "MONTO_MOVIMIENTO_NO_CUADRA"
			,CASE WHEN "CAJA_USUARIO_MONEDA_DESHABILITADA" = 1 AND ("IdModalidadDePago" = 'MODCAJ' AND "IdCaja" = 0) THEN 'RECH' ELSE 'OK' END AS "CAJA_USUARIO_MONEDA_DESHABILITADA"
	  FROM :v_MatrizValidaciones
	 GROUP BY "IdInterno","TipoEjecucion","IdComprobanteInformado","IdComprobante","IdEstadoComprobante","CantidadFormaDePagoInformadas","MontoFormaDePagoInformadas",
	 		  "MontoComprobante","IdModalidadDePago","IdCaja", "COMPROBANTE_TESORERIA_NO_EXISTE","COMPROBANTE_LIQUIDADO_NO_SE_PUEDE_EDITAR","COMPROBANTE_BLOQUEADO_NO_SE_PUEDE_EDITAR",
	 		  "COMPROBANTE_GIRADO_NO_SE_PUEDE_EDITAR","COMPROBANTE_SIN_FORMA_DE_PAGO","MONTO_MOVIMIENTO_NO_CUADRA","CAJA_USUARIO_MONEDA_DESHABILITADA";
	 		  
	--2) Validaciones a nivel de Forma de Pago
	v_Validaciones_2 =
	SELECT  "IdInterno"
		   ,"TipoEjecucion"
		   ,"IdComprobanteInformado"
		   ,CASE WHEN "FORMA_PAGO_CERTIFICADO_NO_SE_PUEDE_EDITAR" = 1 AND "IdEstadoComprobanteFormaDePago" = 'CRT' THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_CERTIFICADO_NO_SE_PUEDE_EDITAR"		   
		   ,CASE WHEN "FORMA_PAGO_NO_EXISTE" = 1 AND (COALESCE("IdComprobanteFormaDePago",0) = 0) THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_NO_EXISTE"
		   ,CASE WHEN "FORMA_PAGO_LIQUIDADO_NO_SE_PUEDE_EDITAR" = 1 AND "IdEstadoComprobanteFormaDePago" = 'LIQ' THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_LIQUIDADO_NO_SE_PUEDE_EDITAR"
		   ,CASE WHEN "FORMA_PAGO_GIRADO_NO_SE_PUEDE_EDITAR" = 1 AND "IdEstadoComprobanteFormaDePago" = 'GIR' THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_GIRADO_NO_SE_PUEDE_EDITAR"
		   ,CASE WHEN "FORMA_PAGO_ANULADO_NO_SE_PUEDE_EDITAR" = 1 AND "IdEstadoComprobanteFormaDePago" = 'ANU' THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_ANULADO_NO_SE_PUEDE_EDITAR"		   
		   ,CASE WHEN "FORMA_PAGO_NO_PERMITE_SER_CERTIFICADA" = 1 AND instr("EstadosPosibleTipoMovimientoMedioDePago",'CRT') = 0 THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_NO_PERMITE_SER_CERTIFICADA"
		   ,CASE WHEN "FORMA_PAGO_NO_PERMITE_SER_EMITIDA" = 1 AND instr("EstadosPosibleTipoMovimientoMedioDePago",'EMI') = 0 THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_NO_PERMITE_SER_EMITIDA"
		   ,CASE WHEN "FORMA_PAGO_SIN_CERTIFICAR" = 1 AND instr("EstadosPosibleTipoMovimientoMedioDePago",'CRT') > 1 AND "IdEstadoComprobanteFormaDePago" <> 'CRT' THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_SIN_CERTIFICAR"
		   ,CASE WHEN "CHEQUE_FUE_UTILIZADO" = 1 AND CH."ChequeDesde" > 0 AND CH."ChequeHasta" > 0 AND (CH."NumeroChequeActual" = CH."ChequeDesde" OR CH."NumeroChequeActual" <= CH."ChequeHasta") THEN 'RECH' ELSE 'OK' END AS "CHEQUE_FUE_UTILIZADO"
		   ,CASE WHEN "SIN_INFORMACION_NUMERO_CHEQUE_ACTUAL" = 1 AND CH."NumeroChequeActual" = 0 THEN 'RECH' ELSE 'OK' END AS "SIN_INFORMACION_NUMERO_CHEQUE_ACTUAL"		   
		   ,CASE WHEN "SIN_INFORMACION_NUMERO_CHEQUE_SOCIO" = 1 AND COALESCE("NumeroDocumentoSocio",0) = 0 THEN 'RECH' ELSE 'OK' END as "SIN_INFORMACION_NUMERO_CHEQUE_SOCIO"
		   ,CASE WHEN "SIN_FECHA_EMISION_DOCUMENTO_SOCIO" = 1 AND "FechaDocumentoPagoSocio" = '0001-01-01' THEN 'RECH' ELSE 'OK' END as "SIN_FECHA_EMISION_DOCUMENTO_SOCIO"		   
	  FROM :v_MatrizValidaciones as M
	  LEFT JOIN :p_InformacionCheques as CH ON CH."IdChequera" = M."IdChequera";


	p_out_Resultado =
	 SELECT "IdInterno",			
			"IdComprobanteInformado",
			"TipoEjecucion",
			"Codigo",
			"Resultado"
	   FROM (
			 SELECT "IdInterno",					
					"IdComprobanteInformado",
					"TipoEjecucion",
			 		COALESCE(
			 			"1" ||"COMPROBANTE_TESORERIA_NO_EXISTE",
			 			"2" ||"COMPROBANTE_LIQUIDADO_NO_SE_PUEDE_EDITAR",
			 			"3" ||"COMPROBANTE_BLOQUEADO_NO_SE_PUEDE_EDITAR",
			 			"4" ||"COMPROBANTE_GIRADO_NO_SE_PUEDE_EDITAR",
			 			"5" ||"COMPROBANTE_SIN_FORMA_DE_PAGO",
			 			"6" ||"MONTO_MOVIMIENTO_NO_CUADRA",
			 			"7" ||"CAJA_USUARIO_MONEDA_DESHABILITADA"
			 		) AS "Resultado",
			 		COALESCE(
			 			"1" ||'COMPROBANTE_TESORERIA_NO_EXISTE',
			 			"2" ||'COMPROBANTE_LIQUIDADO_NO_SE_PUEDE_EDITAR',
			 			"3" ||'COMPROBANTE_BLOQUEADO_NO_SE_PUEDE_EDITAR',
			 			"4" ||'COMPROBANTE_GIRADO_NO_SE_PUEDE_EDITAR',
			 			"5" ||'COMPROBANTE_SIN_FORMA_DE_PAGO',
			 			"6" ||'MONTO_MOVIMIENTO_NO_CUADRA',
			 			"7" ||'CAJA_USUARIO_MONEDA_DESHABILITADA'
			 		) AS "Codigo"
			   FROM :v_Validaciones_1,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL
	  UNION ALL
	 SELECT "IdInterno",			
			"IdComprobanteInformado",
			"TipoEjecucion",
			"Codigo",
			"Resultado"
	   FROM (
			 SELECT "IdInterno",					
					"IdComprobanteInformado",
					"TipoEjecucion",
			 		COALESCE(
						"1" ||"FORMA_PAGO_CERTIFICADO_NO_SE_PUEDE_EDITAR",
					    "2" ||"FORMA_PAGO_NO_EXISTE",
					    "3" ||"FORMA_PAGO_LIQUIDADO_NO_SE_PUEDE_EDITAR",
					    "4" ||"FORMA_PAGO_GIRADO_NO_SE_PUEDE_EDITAR",
					    "5" ||"FORMA_PAGO_ANULADO_NO_SE_PUEDE_EDITAR",
					    "6" ||"FORMA_PAGO_NO_PERMITE_SER_CERTIFICADA",
					    "7" ||"FORMA_PAGO_NO_PERMITE_SER_EMITIDA",
					    "8" ||"FORMA_PAGO_SIN_CERTIFICAR",
					    "9" ||"CHEQUE_FUE_UTILIZADO",
					    "10" ||"SIN_INFORMACION_NUMERO_CHEQUE_ACTUAL",
					    "11" ||"SIN_INFORMACION_NUMERO_CHEQUE_SOCIO",
					    "12" ||"SIN_FECHA_EMISION_DOCUMENTO_SOCIO"
			 		) AS "Resultado",
			 		COALESCE(
			 			"1" ||'FORMA_PAGO_CERTIFICADO_NO_SE_PUEDE_EDITAR',
					    "2" ||'FORMA_PAGO_NO_EXISTE',
					    "3" ||'FORMA_PAGO_LIQUIDADO_NO_SE_PUEDE_EDITAR',
					    "4" ||'FORMA_PAGO_GIRADO_NO_SE_PUEDE_EDITAR',
					    "5" ||'FORMA_PAGO_ANULADO_NO_SE_PUEDE_EDITAR',
					    "6" ||'FORMA_PAGO_NO_PERMITE_SER_CERTIFICADA',
					    "7" ||'FORMA_PAGO_NO_PERMITE_SER_EMITIDA',
					    "8" ||'FORMA_PAGO_SIN_CERTIFICAR',
					    "9" ||'CHEQUE_FUE_UTILIZADO',
					    "10" ||'SIN_INFORMACION_NUMERO_CHEQUE_ACTUAL',
					    "11" ||'SIN_INFORMACION_NUMERO_CHEQUE_SOCIO',
					    "12" ||'SIN_FECHA_EMISION_DOCUMENTO_SOCIO'
			 		) AS "Codigo"
			   FROM :v_Validaciones_2,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL;

END;
