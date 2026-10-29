SET SCHEMA "BSCL";
GO

DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosLiquidarComprobantes";
GO
CREATE PROCEDURE
-- 
--  Proposito: Validar 
--  Autor: NRM - BCS
--  Fecha Creacion: 23/11/2015  
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosLiquidarComprobantes"(
	IN p_IdEmpresa                      BIGINT, 
    IN p_TablaComprobante 		        TABLE ("IdInterno"							BIGINT,
    										   "IdComprobanteInformado" 			BIGINT,
    										   "IdTipoMovimientoInformado"			NVARCHAR(6),
    										   "IdComprobante" 						BIGINT,
    										   "IdTipoMovimiento"					NVARCHAR(6),
    										   "MontoComprobante"					DECIMAL(26,8),
    										   "CantidadFormaDePago"				BIGINT,
    										   "MontoTotalFormaDePago"				DECIMAL(26,8),
    										   "IdEstadoComprobante"				NVARCHAR(3),
    										   "AfectaCuentaInversion" 				NVARCHAR(3),
    										   "IdPortafolioTitularPago"			BIGINT,
									    	   "IdConcepto"							NVARCHAR(6),
									    	   "FechaMovimiento"					NVARCHAR(10),
									    	   "IdPersona"							BIGINT,
											   "NombrePersona"						NVARCHAR(300),
											   "TitularPago"						NVARCHAR(1),
											   "IdTipoPersonaTitularPago"			NVARCHAR(3),
											   "IdPersonaTitularPago"				BIGINT,
											   "FechaLiquidacion"					NVARCHAR(10),
											   "IdCaja"								BIGINT),
	
	IN p_TablaComprobanteDetalle		TABLE ("IdInterno"							BIGINT,
    										   "IdComprobanteInformado" 			BIGINT,
    										   "IdComprobanteDetalle"				BIGINT,
    										   "IdTipoMovimiento"					NVARCHAR(6),
											   "IdTipoComprobanteOrigen"			NVARCHAR(6),
											   "SecuenciaRelacionada"				BIGINT,
											   "PendienteDeLiquidar"				BIGINT,
											   "IdConcepto"							NVARCHAR(6),
		   									   "Monto"								DECIMAL(26,8)),
    										   
	IN p_TablaComprobanteFormaDePago	TABLE ("IdInterno"							BIGINT,
											   "IdComprobanteInformado" 			BIGINT,
											   "IdComprobanteFormaDePagoInformado" 	BIGINT,
											   "IdComprobanteFormaDePago" 			BIGINT,
											   "IdCuentaBancariaCorredora"			BIGINT,
											   "IdEstadoFormaDePago"				NVARCHAR(3),
											   "IdEstadoComprobante"				NVARCHAR(3),
											   "TipoMedioDePago"					NVARCHAR(3),
											   "LiquidacionParcialEnviaACTA"		NVARCHAR(40),		
											   "EstadosPosibleTipoMovimientoMedioDePago" NVARCHAR(40),
											   "AfectaSaldoEnEstado"				NVARCHAR(40),
											   "IdMoneda"							NVARCHAR(3),
		   									   "Monto"								DECIMAL(26,8),
		   									   "IdDeposito"							BIGINT,
											   "IdEstadoDepositoDetalle"			NVARCHAR(3),
											   "FechaLiquidacion"					NVARCHAR(10),
											   "DiasRetencion"						BIGINT,
											   "IdMedioDePago"                      NVARCHAR(3),
											   "IdCaja"								BIGINT),

	OUT p_out_Resultado					TABLE ("IdInterno"					BIGINT,   									   
	   								   		   	"Codigo"					NVARCHAR(100),
	   								   		   	"CodigoDato1" 				NVARCHAR(100),
												"ValorDato1" 				NVARCHAR(520),
												"CodigoDato2" 				NVARCHAR(100),
												"ValorDato2" 				NVARCHAR(520),
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

		
	DECLARE v_APV_Habilitado TABLE ("Valor" NVARCHAR(1));
	DECLARE v_APV_CajaRecaudacion TABLE ("IdCaja" BIGINT, "Codigo" NVARCHAR(60));
	
	v_APV_Habilitado 		= SELECT '' AS "Valor" FROM DUMMY WHERE 1=2;
	v_APV_CajaRecaudacion 	= SELECT 0 AS "IdCaja", '' AS "Codigo" FROM DUMMY WHERE 1=2;
	
	-- Consulta si productos AVP está habilitado
	v_APV_Habilitado = SELECT COALESCE("Valor",'N') AS "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'APV.Habilitado';

	-- Obtiene Caja destinada para recaudación APV
	v_APV_CajaRecaudacion =
		SELECT "IdCaja", "Codigo" 
		FROM "BSCL.Tesoreria.BD::TES.Caja" 
		WHERE "Codigo" 		= 'PAC' 
		AND   "IdEstado" 	= 'ABT' 
		AND   "IdEmpresa" 	= :p_IdEmpresa;
	--1) A nivel de comprobante
	v_MatrizValidaciones_V1 = 
	 SELECT  DISTINCT
	 		 "IdInterno"
	 		,"IdComprobanteInformado"
	 		,"IdTipoMovimientoInformado"
	 		,"IdComprobante"
    		,"IdTipoMovimiento"
    		,"MontoComprobante"
    		,"CantidadFormaDePago"
    		,"MontoTotalFormaDePago"
    		,"IdEstadoComprobante"
    		,"AfectaCuentaInversion"
		    ,"IdPortafolioTitularPago"
    	    ,"IdConcepto"
    	    ,"FechaMovimiento"
    	    ,"IdPersona"
		    ,"NombrePersona"
		    ,"TitularPago"
		    ,"IdTipoPersonaTitularPago"
		    ,"IdPersonaTitularPago"
		    ,"FechaLiquidacion"
	 		,1 as "COMPROBANTE_TESORERIA_NO_EXISTE"
	 		,1 as "COMPROBANTE_TESORERIA_BLOQUEADO"
	 		,1 as "COMPROBANTE_TESORERIA_ANULADO"
	 		,1 as "COMPROBANTE_TESORERIA_MODIFICADO"	 		
	 		,1 as "COMPROBANTE_TESORERIA_LIQUIDADO"
	 		,1 as "COMPROBANTE_TESORERIA_SIN_FORMA_PAGO"
	 		,1 as "MONTO_FORMA_PAGO_MENOR_MONTO_COMPROBANTE"
	 		,1 as "MONTO_FORMA_PAGO_MAYOR_MONTO_COMPROBANTE"
	   FROM :p_TablaComprobante as TC;
	 		 
	v_Validaciones_V1 =
	 SELECT  "IdInterno"
	 		,"IdComprobanteInformado"
			,"IdTipoMovimientoInformado"			
			,CASE WHEN "COMPROBANTE_TESORERIA_NO_EXISTE" = 1 AND "IdComprobante" = 0 THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_NO_EXISTE"
			,CASE WHEN "COMPROBANTE_TESORERIA_BLOQUEADO" = 1 AND "IdEstadoComprobante" = 'BLO' THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_BLOQUEADO"		
			,CASE WHEN "COMPROBANTE_TESORERIA_ANULADO" = 1 AND "IdEstadoComprobante" = 'ANU' THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_ANULADO"
			,CASE WHEN "COMPROBANTE_TESORERIA_MODIFICADO" = 1 AND "IdEstadoComprobante" = 'MOD' THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_MODIFICADO"			
			,CASE WHEN "COMPROBANTE_TESORERIA_LIQUIDADO" = 1 AND "IdEstadoComprobante" = 'LIQ' THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_LIQUIDADO"			
			,CASE WHEN "COMPROBANTE_TESORERIA_SIN_FORMA_PAGO"	  = 1 AND "IdEstadoComprobante" IN ('ING','LIP','GIP','GIR') AND "CantidadFormaDePago" = 0 THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_SIN_FORMA_PAGO"
	 		,CASE WHEN "MONTO_FORMA_PAGO_MENOR_MONTO_COMPROBANTE" = 1 AND "IdEstadoComprobante" IN ('ING','LIP','GIP','GIR') AND "MontoTotalFormaDePago" < "MontoComprobante" THEN 'RECH' ELSE 'OK' END AS "MONTO_FORMA_PAGO_MENOR_MONTO_COMPROBANTE"
	 		,CASE WHEN "MONTO_FORMA_PAGO_MAYOR_MONTO_COMPROBANTE" = 1 AND "IdEstadoComprobante" IN ('ING','LIP','GIP','GIR') AND "MontoTotalFormaDePago" > "MontoComprobante" THEN 'RECH' ELSE 'OK' END AS "MONTO_FORMA_PAGO_MAYOR_MONTO_COMPROBANTE" 
	  FROM :v_MatrizValidaciones_V1;
	   
	 -- SELECT 'v_Validaciones_V1', * FROM :v_Validaciones_V1;  -- REVISAR
	  
	
	--2) A nivel de detalle
	v_MatrizValidaciones_V2 = 
	 SELECT  DISTINCT
	 		 "IdInterno"
	 		,"IdComprobanteInformado"
		    ,"IdComprobanteDetalle"
		    ,"IdTipoMovimiento"
		    ,"IdTipoComprobanteOrigen"
		    ,"SecuenciaRelacionada"
		    ,"PendienteDeLiquidar"
		    ,"IdConcepto"
		   	,"Monto"
	 		,1 as "SIN_LIQUIDAR_MOVIMIENTOS_RELACIONADOS"
	   FROM :p_TablaComprobanteDetalle as TC;
		 
	v_Validaciones_V2 =
	 SELECT  "IdInterno"
	 		,"IdComprobanteInformado"
		    ,"IdComprobanteDetalle"
		    ,"IdTipoMovimiento"
		    ,"IdTipoComprobanteOrigen"
		    ,"SecuenciaRelacionada"
		    ,"PendienteDeLiquidar"
			,CASE WHEN "SIN_LIQUIDAR_MOVIMIENTOS_RELACIONADOS" = 1 AND "IdTipoMovimiento" = 'MOVEGR' AND "PendienteDeLiquidar" > 0 THEN 'RECH' ELSE 'OK' END AS "SIN_LIQUIDAR_MOVIMIENTOS_RELACIONADOS"			
	  FROM :v_MatrizValidaciones_V2;
	   
	
	--3) A nivel de forma de pago
	v_MatrizValidaciones_V3 = 
	 SELECT  DISTINCT
			 TC."IdInterno"
			,TC."IdComprobanteInformado"
		    ,TC."IdComprobanteFormaDePagoInformado"
		    ,TC."IdComprobanteFormaDePago"
		    
		    
		    ,COALESCE(CFP."IdCuentaBancariaCorredora",0) AS "IdCuentaBancariaCorredora"
		    
		    ,TC."IdEstadoFormaDePago"
		    ,TC."IdEstadoComprobante"
		    ,TC."TipoMedioDePago"
		    ,TC."LiquidacionParcialEnviaACTA"
		    ,TC."EstadosPosibleTipoMovimientoMedioDePago"
	 		,TC."AfectaSaldoEnEstado"
		    ,TC."IdMoneda"
		    ,TC."Monto"
		    ,TC."IdDeposito"
			,TC."IdEstadoDepositoDetalle"
			,TC."FechaLiquidacion"
			,TC."IdMedioDePago"
			
			-- jvillagran ... cambio 222
			,CFP."NumeroCuentaBancariaCorredora"
			,CFP."IdTipoMovimiento"
			,CFP."IdModalidadDePago"	
			,COALESCE(CFP."IdCuentaBancariaSocio",0) AS "IdCuentaBancariaSocio"
			,CFP."NumeroCuentaBancariaSocio"
		
			,COALESCE(CFP."IdBancoSocio", 0) AS "IdBancoSocio"
	
	 		,1 as "FORMA_PAGO_COMPROBANTE_NO_EXISTE"
	 		,1 as "FORMA_PAGO_COMPROBANTE_ANULADA"
	 		,1 as "FORMA_PAGO_COMPROBANTE_LIQUIDADA"
	 		,1 as "FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO"
	 		,(CASE
	 			WHEN (PARAM."Valor" = 'S' AND COALESCE(CAJREC."IdCaja",-1) = TC."IdCaja") THEN 0 -- Recaudacion APV no valida certificacion ya que es PAC
	 			ELSE 1
	 		END)	AS "FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR"
	 		
	 		,1 as "FORMA_PAGO_COMPROBANTE_SIN_CUENTA_BANCARIA_CORREDORA"
	 		
	 	
	 		
	 		,(CASE
	 			WHEN (PARAM."Valor" = 'S' AND COALESCE(CAJREC."IdCaja",-1) = TC."IdCaja") THEN 0 -- Recaudacion APV no valida depositos para cheques ya que es PAC
	 			ELSE 1
	 		END) AS "FORMA_PAGO_COMPROBANTE_SIN_DEPOSITAR"
	 		
	   FROM :p_TablaComprobanteFormaDePago as TC
	   
	   
	   LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (CFP."IdComprobanteFormaDePago" = TC."IdComprobanteFormaDePago")

	   
	   LEFT JOIN :v_APV_CajaRecaudacion AS CAJREC ON (1=1)
	   LEFT JOIN :v_APV_Habilitado AS PARAM ON  (1=1);   


	 v_Validaciones_V3 =
	 SELECT  "IdInterno"
	        ,"IdComprobanteInformado"
	 		,CASE WHEN "FORMA_PAGO_COMPROBANTE_NO_EXISTE" = 1 AND "IdEstadoComprobante" <> 'ANU' AND (COALESCE("IdComprobanteFormaDePago",0) = 0) THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_COMPROBANTE_NO_EXISTE"
	 		,CASE WHEN "FORMA_PAGO_COMPROBANTE_ANULADA" = 1 AND "IdEstadoFormaDePago" = 'ANU' THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_COMPROBANTE_ANULADA"
	 		,CASE WHEN "FORMA_PAGO_COMPROBANTE_LIQUIDADA" = 1 AND "IdEstadoFormaDePago" = 'LIQ' THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_COMPROBANTE_LIQUIDADA"	 		
			,CASE WHEN "FORMA_PAGO_COMPROBANTE_SIN_DEPOSITAR" = 1 AND "IdEstadoComprobante" NOT IN ('ANU') AND "IdMedioDePago" IN ('CHE','VVI') AND instr("EstadosPosibleTipoMovimientoMedioDePago",'NDP') > 1 AND "IdEstadoFormaDePago" NOT IN ('NDP') THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_COMPROBANTE_SIN_DEPOSITAR"
	 		,CASE WHEN "FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO" = 1 AND "IdEstadoFormaDePago" = 'NDP' AND "IdEstadoDepositoDetalle" <> 'LIQ' THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO"
	 		,CASE WHEN "FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR" = 1 AND "IdEstadoComprobante" <> 'ANU' AND instr("EstadosPosibleTipoMovimientoMedioDePago",'CRT') > 1 AND "IdEstadoFormaDePago" NOT IN ('CRT','EBV','NDP') THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR"

	 		
	 		
	 	
	 		,CASE WHEN "FORMA_PAGO_COMPROBANTE_SIN_CUENTA_BANCARIA_CORREDORA" = 1 AND "IdEstadoComprobante" <> 'ANU' AND ("IdCuentaBancariaCorredora" = 0 OR "NumeroCuentaBancariaCorredora" = '') AND "TipoMedioDePago" <> 'EFE' THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_COMPROBANTE_SIN_CUENTA_BANCARIA_CORREDORA"		
	 		
	 		
	  FROM :v_MatrizValidaciones_V3; 
	 
	  
	--4) Procesamiento de informacion de salida  
	v_Validaciones = 
	 SELECT DISTINCT
	 		 V1."IdInterno"
	 		 
	 		--Comprobante
		   	,V1."COMPROBANTE_TESORERIA_NO_EXISTE"
		   	,V1."COMPROBANTE_TESORERIA_BLOQUEADO"
	 		,V1."COMPROBANTE_TESORERIA_ANULADO"
	 		,V1."COMPROBANTE_TESORERIA_MODIFICADO"	 		
	 		,V1."COMPROBANTE_TESORERIA_LIQUIDADO"
	 		,V1."COMPROBANTE_TESORERIA_SIN_FORMA_PAGO"
	 		,V1."MONTO_FORMA_PAGO_MENOR_MONTO_COMPROBANTE"
	 		,V1."MONTO_FORMA_PAGO_MAYOR_MONTO_COMPROBANTE"
	 		
	 		--Detalle
	 		,V2."SIN_LIQUIDAR_MOVIMIENTOS_RELACIONADOS"
	 		
	 		--Forma de pago
			,V3."FORMA_PAGO_COMPROBANTE_NO_EXISTE"
	 		,V3."FORMA_PAGO_COMPROBANTE_ANULADA"
	 		,V3."FORMA_PAGO_COMPROBANTE_LIQUIDADA"
	 		,V3."FORMA_PAGO_COMPROBANTE_SIN_DEPOSITAR"
	 		,V3."FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO"
	 		,V3."FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR"
	 		,V3."FORMA_PAGO_COMPROBANTE_SIN_CUENTA_BANCARIA_CORREDORA"	
	 	
	 		
	   FROM :v_Validaciones_V1 as V1
	  INNER JOIN :v_Validaciones_V2 as V2 ON V2."IdInterno" = V1."IdInterno"
	  INNER JOIN :v_Validaciones_V3 as V3 ON V3."IdInterno" = V1."IdInterno";
	   
	  
	 p_out_Resultado =   
	 SELECT "IdInterno",
	 		"Codigo",
	 		'IdComprobante' as "CodigoDato1",
			CAST("IdComprobanteInformado" AS NVARCHAR(50)) as "ValorDato1",
			'' as "CodigoDato2",
			'' as "ValorDato2",
			"Resultado"
	   FROM (
			 SELECT "IdInterno","IdComprobanteInformado",
			 		COALESCE(			 			
			 			 "1" ||"COMPROBANTE_TESORERIA_NO_EXISTE"
				 		,"2" ||"COMPROBANTE_TESORERIA_BLOQUEADO"
				 		,"3" ||"COMPROBANTE_TESORERIA_ANULADO"
				 		,"4" ||"COMPROBANTE_TESORERIA_MODIFICADO"	 		
				 		,"5" ||"COMPROBANTE_TESORERIA_LIQUIDADO"
				 		,"6" ||"COMPROBANTE_TESORERIA_SIN_FORMA_PAGO"
				 		,"7" ||"MONTO_FORMA_PAGO_MENOR_MONTO_COMPROBANTE"
				 		,"8" ||"MONTO_FORMA_PAGO_MAYOR_MONTO_COMPROBANTE"	 			
			 		) AS "Resultado",
			 		COALESCE(			 			
			 			 "1" ||'COMPROBANTE_TESORERIA_NO_EXISTE'
				 		,"2" ||'COMPROBANTE_TESORERIA_BLOQUEADO'
				 		,"3" ||'COMPROBANTE_TESORERIA_ANULADO'
				 		,"4" ||'COMPROBANTE_TESORERIA_MODIFICADO'
				 		,"5" ||'COMPROBANTE_TESORERIA_LIQUIDADO'
				 		,"6" ||'COMPROBANTE_TESORERIA_SIN_FORMA_PAGO'
				 		,"7" ||'MONTO_FORMA_PAGO_MENOR_MONTO_COMPROBANTE'
				 		,"8" ||'MONTO_FORMA_PAGO_MAYOR_MONTO_COMPROBANTE'
			 		) AS "Codigo"
			   FROM :v_Validaciones_V1,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL
	UNION ALL
	SELECT "IdInterno",
	 		"Codigo",
	 		'IdComprobante' as "CodigoDato1",
			CAST("IdComprobanteInformado" AS NVARCHAR(50)) as "ValorDato1",
			'' as "CodigoDato2",
			'' as "ValorDato2",
			"Resultado"
	   FROM (
			 SELECT "IdInterno","IdComprobanteInformado",
			 		"SIN_LIQUIDAR_MOVIMIENTOS_RELACIONADOS" AS "Resultado",
			 		'SIN_LIQUIDAR_MOVIMIENTOS_RELACIONADOS' AS "Codigo"
			   FROM :v_Validaciones_V2,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL
	 UNION ALL 
	 SELECT "IdInterno",
	 		"Codigo",
	 		'IdComprobante' as "CodigoDato1",
			CAST("IdComprobanteInformado" AS NVARCHAR(50)) as "ValorDato1",
			'' as "CodigoDato2",
			'' as "ValorDato2",
			"Resultado"
	   FROM (
			 SELECT "IdInterno","IdComprobanteInformado",
			 		COALESCE(			 			
			 			 "1" ||"FORMA_PAGO_COMPROBANTE_NO_EXISTE"
				 		,"2" ||"FORMA_PAGO_COMPROBANTE_ANULADA"
				 		,"3" ||"FORMA_PAGO_COMPROBANTE_LIQUIDADA"
				 		,"4" ||"FORMA_PAGO_COMPROBANTE_SIN_DEPOSITAR"
				 		,"5" ||"FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO"
				 		,"6" ||"FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR"
				 		,"7" ||"FORMA_PAGO_COMPROBANTE_SIN_CUENTA_BANCARIA_CORREDORA"
				 		
				 		
			 		) AS "Resultado",
			 		COALESCE(			 			
			 			 "1" ||'FORMA_PAGO_COMPROBANTE_NO_EXISTE'
				 		,"2" ||'FORMA_PAGO_COMPROBANTE_ANULADA'
				 		,"3" ||'FORMA_PAGO_COMPROBANTE_LIQUIDADA'
				 		,"4" ||'FORMA_PAGO_COMPROBANTE_SIN_DEPOSITAR'
				 		,"5" ||'FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO'
				 		,"6" ||'FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR'
				 		,"7" ||'FORMA_PAGO_COMPROBANTE_SIN_CUENTA_BANCARIA_CORREDORA'
				 	
				 		
			 		) AS "Codigo"
			   FROM :v_Validaciones_V3,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL;
	  
	  -- SELECT 'p_out_Resultado', * FROM :p_out_Resultado;   -- REVISAR
	  
	  p_out_Resultado = SELECT DISTINCT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","Resultado" FROM :p_out_Resultado;

END;
