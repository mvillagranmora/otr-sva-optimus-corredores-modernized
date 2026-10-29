SET SCHEMA "BSCL";
GO 
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearComprobantes";
GO 
CREATE PROCEDURE
-- 
--  Proposito: Validar los requisitos para la generacion de comprobantes de tesoreria
--  Autor: NRM - BCS
--  Fecha Creacion: 23/11/2015  
-- 
--  Tablas de entradas
--	"BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_IngresoEgresoGenerar" 
--	"BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteGenerar"
--	"BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_FormasDePagoGenerar"
--	"BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_Comprobante"
--	"BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteFormaDePago" 
--	
--	Tablas de salida
--	"BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado"
--
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearComprobantes"(
	IN p_FechaMovimiento						NVARCHAR(10),
	IN p_IdCaja   								BIGINT			DEFAULT 0,    --Caja donde se procesaran los comprobantes
	IN p_GenerarComprobanteDesdeMovimientos		NVARCHAR(1)		DEFAULT 'N',
	IN p_GenerarComprobanteDesdeComprobantes	NVARCHAR(1)		DEFAULT 'N',
	IN p_ValidaComprobanteGenerado				NVARCHAR(1) 	--'N': Valida información de movimientos o comprobantes que generaran un nuevo comprobante
																--'S': Valida la creación del nuevo comprobante.
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	
														  
	DECLARE v_MatrizValidaciones_IngresoEgresoGenerar	TABLE("NumeroReferencia" BIGINT,"IdIngresoEgresoInformado" BIGINT,"TipoComprobante" NVARCHAR(6),"IdIngresoEgreso" BIGINT,
															  "FechaLiquidacion" NVARCHAR(10),"IdEstado" NVARCHAR(3),"CantidadSecuenciaMovimiento" BIGINT,"CantidadSecuenciaComprobante" BIGINT,
															  "MOVIMIENTO_TESORERIA_NO_EXISTE" INT,"MOVIMIENTO_TESORERIA_EMITIDO" INT,"MOVIMIENTO_TESORERIA_BLOQUEADO" INT,"MOVIMIENTO_TESORERIA_ANULADO" INT,
															  "COMPROBANTE_TESORERIA_SECUENCIA_UTILIZADA" INT,"FALTA_INCLUIR_MOVIMIENTOS_SECUENCIA_AGRUPACION_COMPROBANTE" INT);
															  
	DECLARE v_MatrizValidaciones_ComprobanteGenerar		TABLE("NumeroReferencia" BIGINT,"IdComprobanteInformado" BIGINT,"IdComprobante" BIGINT,"TipoComprobante" NVARCHAR(6),
															  "FechaLiquidacion" NVARCHAR(10),"IdEstado" NVARCHAR(3),"COMPROBANTE_TESORERIA_NO_EXISTE" INT,"COMPROBANTE_TESORERIA_LIQUIDADO" INT,
															  "COMPROBANTE_TESORERIA_GIRADO" INT,"COMPROBANTE_TESORERIA_BLOQUEADO" INT,"COMPROBANTE_TESORERIA_ANULADO" INT,
															  "COMPROBANTE_TESORERIA_MODIFICADO" INT,"COMPROBANTE_TESORERIA_VENCIDO" INT);															  
	DECLARE v_MatrizValidaciones_Comprobante			TABLE("NumeroReferencia" BIGINT,"IdComprobante" BIGINT,"TipoComprobante" NVARCHAR(6),"IdTipoMovimiento" NVARCHAR(6),"IdConcepto" NVARCHAR(6),
															  "IdCaja" BIGINT,"IdMoneda" NVARCHAR(3),"Monto" DECIMAL(26,8),"FechaLiquidacion" NVARCHAR(10),"FechaMovimiento" NVARCHAR(10),"IdTipoPersona" NVARCHAR(3),"GeneraComprobanteAutomaticamente" NVARCHAR(1),
															  "IdMonedaFormaDePago" NVARCHAR(3),"CantidadTipoComprobante" BIGINT,"CantidadIdTipoMovimiento" BIGINT,"CantidadConcepto" BIGINT,
															  "CantidadMoneda" BIGINT,"CantidadFechaLiquidacion" BIGINT,"CantidadFormasDePago" BIGINT,"CantidadMonedaFormaDePago" BIGINT, "CantidadConModalidadCaja" BIGINT,
															  "TotalMonto" DECIMAL(26,8),"TotalMontoFormaDePago" DECIMAL(26,8),"COMPROBANTE_CON_MEZCLA_DE_TIPO_COMPROBANTE" INT,
															  "COMPROBANTE_CON_MEZCLA_DE_MONEDAS" INT,"COMPROBANTE_CON_FECHAS_DISTINTAS_LIQUIDACION" INT,"COMPROBANTE_CON_MEZCLA_DE_CONCEPTOS" INT,
															  "COMPROBANTE_CON_MEZCLA_TIPO_MOVIMIENTO" INT,"NO_VIENEN_FORMAS_DE_PAGO" INT,"FORMA_PAGO_CON_MEZCLA_DE_MONEDAS" INT,
															  "DIFERENTE_MONEDA_ENTRE_COMPROBANTE_Y_FORMA_PAGO" INT,"MONTO_FORMA_PAGO_MAYOR_A_COMPROBANTE" INT,"MONTO_FORMA_PAGO_MENOR_A_COMPROBANTE" INT, "COMPROBANTE_SIN_CAJA_ASIGNADA" INT);

											  
	DECLARE v_Validaciones_IngresoEgresoGenerar TABLE("NumeroReferencia" BIGINT,"IdIngresoEgresoInformado" BIGINT,"MOVIMIENTO_TESORERIA_NO_EXISTE" NVARCHAR(4),
													  "MOVIMIENTO_TESORERIA_EMITIDO" NVARCHAR(4),"MOVIMIENTO_TESORERIA_BLOQUEADO" NVARCHAR(4),"MOVIMIENTO_TESORERIA_ANULADO" NVARCHAR(4),
													  "COMPROBANTE_TESORERIA_SECUENCIA_UTILIZADA" NVARCHAR(4),			
													  "FALTA_INCLUIR_MOVIMIENTOS_SECUENCIA_AGRUPACION_COMPROBANTE" NVARCHAR(4));													  
													  
	DECLARE v_Validaciones_comprobanteGenerar	TABLE("NumeroReferencia" BIGINT,"IdComprobanteInformado" BIGINT,"COMPROBANTE_TESORERIA_NO_EXISTE" NVARCHAR(4),
													  "COMPROBANTE_TESORERIA_LIQUIDADO" NVARCHAR(4),"COMPROBANTE_TESORERIA_GIRADO" NVARCHAR(4),"COMPROBANTE_TESORERIA_BLOQUEADO" NVARCHAR(4),
													  "COMPROBANTE_TESORERIA_ANULADO" NVARCHAR(4),"COMPROBANTE_TESORERIA_MODIFICADO" NVARCHAR(4),"COMPROBANTE_TESORERIA_VENCIDO" NVARCHAR(4));													  
	DECLARE v_Validaciones_Comprobante			TABLE("NumeroReferencia" BIGINT,"IdComprobante" BIGINT,"COMPROBANTE_CON_MEZCLA_DE_TIPO_COMPROBANTE" NVARCHAR(4),
													  "COMPROBANTE_CON_MEZCLA_DE_MONEDAS" NVARCHAR(4),"COMPROBANTE_CON_FECHAS_DISTINTAS_LIQUIDACION" NVARCHAR(4),"NO_VIENEN_FORMAS_DE_PAGO" NVARCHAR(4),
													  "FORMA_PAGO_CON_MEZCLA_DE_MONEDAS" NVARCHAR(4),"DIFERENTE_MONEDA_ENTRE_COMPROBANTE_Y_FORMA_PAGO" NVARCHAR(4),
													  "MONTO_FORMA_PAGO_MAYOR_A_COMPROBANTE" NVARCHAR(4),"MONTO_FORMA_PAGO_MENOR_A_COMPROBANTE" NVARCHAR(4),"COMPROBANTE_SIN_CAJA_ASIGNADA" NVARCHAR(4));

	
	----------------------------------------------------												  
	--  ValidaComprobanteGenerado = 'N' 
	----------------------------------------------------
													  
	v_MatrizValidaciones_IngresoEgresoGenerar =
	SELECT   IEG."NumeroReferencia"
			,IEG."IdIngresoEgresoInformado"
    		,IEG."TipoComprobante"
    		,IEG."IdIngresoEgreso"
    		,IEG."FechaLiquidacion"
    		,IEG."IdEstado"
    		,COALESCE(SIE."CantidadSecuenciaMovimiento",0) as "CantidadSecuenciaMovimiento"
    		,COALESCE(SECC."CantidadSecuenciaComprobante",0) as "CantidadSecuenciaComprobante" 		    		
    		,1 as "MOVIMIENTO_TESORERIA_NO_EXISTE"
    		,1 as "MOVIMIENTO_TESORERIA_EMITIDO"
			,1 as "MOVIMIENTO_TESORERIA_BLOQUEADO"
			,1 as "MOVIMIENTO_TESORERIA_ANULADO"
			,1 as "COMPROBANTE_TESORERIA_SECUENCIA_UTILIZADA"
			,1 as "FALTA_INCLUIR_MOVIMIENTOS_SECUENCIA_AGRUPACION_COMPROBANTE"  ---¿Esta bien como se valida?
	  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_IngresoEgresoGenerar" as IEG
	  LEFT JOIN (SELECT "SecuenciaAgrupacion", COUNT(DISTINCT "SecuenciaAgrupacion") as "CantidadSecuenciaMovimiento"
		  	 		   FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" as E
		  	 		  INNER JOIN ( SELECT "IdIngresoEgreso" FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" WHERE "IdEstado" = 'ING'
		  	 		  			   EXCEPT SELECT "IdIngresoEgreso" FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_IngresoEgresoGenerar"
		  	 		  			 ) AS I ON I."IdIngresoEgreso" = E."IdIngresoEgreso"
		              GROUP BY "SecuenciaAgrupacion"
		  			) AS SIE ON SIE."SecuenciaAgrupacion" = IEG."SecuenciaAgrupacion" AND IEG."SecuenciaAgrupacion" <> ''
	  LEFT JOIN (SELECT "SecuenciaAgrupacion", COUNT(DISTINCT "SecuenciaAgrupacion") as "CantidadSecuenciaComprobante"
	  	 		   FROM "BSCL.Tesoreria.BD::TES.Comprobante"
	              GROUP BY "SecuenciaAgrupacion"
	  			) AS SECC ON SECC."SecuenciaAgrupacion" = IEG."SecuenciaAgrupacion" AND IEG."SecuenciaAgrupacion" <>''
	 WHERE :p_GenerarComprobanteDesdeMovimientos = 'S'
	   AND :p_ValidaComprobanteGenerado = 'N';
	   
													  

	v_Validaciones_IngresoEgresoGenerar =
	SELECT  "NumeroReferencia","IdIngresoEgresoInformado"	
			,CASE WHEN "MOVIMIENTO_TESORERIA_NO_EXISTE" = 1 AND COALESCE("IdIngresoEgreso",0) = 0 THEN 'RECH' ELSE 'OK' END AS "MOVIMIENTO_TESORERIA_NO_EXISTE"
			,CASE WHEN "MOVIMIENTO_TESORERIA_EMITIDO" = 1 AND ("IdEstado" = 'EMI') THEN 'RECH' ELSE 'OK' END AS "MOVIMIENTO_TESORERIA_EMITIDO"
			,CASE WHEN "MOVIMIENTO_TESORERIA_BLOQUEADO" = 1 AND ("IdEstado" = 'BLO') THEN 'RECH' ELSE 'OK' END AS "MOVIMIENTO_TESORERIA_BLOQUEADO"
			,CASE WHEN "MOVIMIENTO_TESORERIA_ANULADO" = 1 AND ("IdEstado" = 'ANU') THEN 'RECH' ELSE 'OK' END AS "MOVIMIENTO_TESORERIA_ANULADO"
			,CASE WHEN "COMPROBANTE_TESORERIA_SECUENCIA_UTILIZADA" = 1 AND "CantidadSecuenciaComprobante" > 1 THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_SECUENCIA_UTILIZADA"			
			,CASE WHEN "FALTA_INCLUIR_MOVIMIENTOS_SECUENCIA_AGRUPACION_COMPROBANTE" = 1 AND "CantidadSecuenciaMovimiento" > 1 THEN 'RECH' ELSE 'OK' END AS "FALTA_INCLUIR_MOVIMIENTOS_SECUENCIA_AGRUPACION_COMPROBANTE"			
	  FROM :v_MatrizValidaciones_IngresoEgresoGenerar;



	INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado"
	SELECT RVAL."NumeroReferencia",:p_ValidaComprobanteGenerado,RVAL."Codigo",RVAL."Resultado",'Id' as "CodigoDato1",CAST(MCG."IdIngresoEgresoInformado" as NVARCHAR) as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
	  FROM :v_MatrizValidaciones_IngresoEgresoGenerar as MCG
	 INNER JOIN (SELECT  "NumeroReferencia","IdIngresoEgresoInformado",
		  				COALESCE(			 			
				 			 "1" ||'MOVIMIENTO_TESORERIA_NO_EXISTE'
							,"2" ||'MOVIMIENTO_TESORERIA_EMITIDO'
							,"3" ||'MOVIMIENTO_TESORERIA_BLOQUEADO'
							,"4" ||'MOVIMIENTO_TESORERIA_ANULADO'
							,"5" ||'COMPROBANTE_TESORERIA_SECUENCIA_UTILIZADA'
							,"6" ||'FALTA_INCLUIR_MOVIMIENTOS_SECUENCIA_AGRUPACION_COMPROBANTE'
				 		) AS "Codigo",
				 		COALESCE(			 			
				 			 "1" ||"MOVIMIENTO_TESORERIA_NO_EXISTE"
							,"2" ||"MOVIMIENTO_TESORERIA_EMITIDO"
							,"3" ||"MOVIMIENTO_TESORERIA_BLOQUEADO"
							,"4" ||"MOVIMIENTO_TESORERIA_ANULADO"
							,"5" ||"COMPROBANTE_TESORERIA_SECUENCIA_UTILIZADA"
							,"6" ||"FALTA_INCLUIR_MOVIMIENTOS_SECUENCIA_AGRUPACION_COMPROBANTE"
				 		) AS "Resultado"
			       FROM :v_Validaciones_IngresoEgresoGenerar,"BSCL.Plataforma.BD::PLA.Identidad") as RVAL
			 ON RVAL."NumeroReferencia" = MCG."NumeroReferencia" AND RVAL."IdIngresoEgresoInformado" = MCG."IdIngresoEgresoInformado"			       
	 WHERE RVAL."Resultado" IS NOT NULL;

	
	v_MatrizValidaciones_ComprobanteGenerar =
	SELECT   CG."NumeroReferencia"
			,CG."IdComprobanteInformado"
		  	,CG."IdComprobante"
		  	,CG."TipoComprobante"
		  	,CG."FechaLiquidacion"
		  	,CG."IdEstado"
    		,1 as "COMPROBANTE_TESORERIA_NO_EXISTE"
			,1 as "COMPROBANTE_TESORERIA_LIQUIDADO"
			,1 as "COMPROBANTE_TESORERIA_GIRADO"
			,1 as "COMPROBANTE_TESORERIA_BLOQUEADO"
			,1 as "COMPROBANTE_TESORERIA_ANULADO"
			,1 as "COMPROBANTE_TESORERIA_MODIFICADO"
			,1 as "COMPROBANTE_TESORERIA_VENCIDO"
	  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteGenerar" as CG
	 WHERE :p_GenerarComprobanteDesdeComprobantes = 'S'
	   AND :p_ValidaComprobanteGenerado = 'N';
		
	v_Validaciones_comprobanteGenerar = 
	SELECT  "NumeroReferencia","IdComprobanteInformado"			
			,CASE WHEN "COMPROBANTE_TESORERIA_NO_EXISTE" = 1 AND COALESCE("IdComprobante",0) = 0 THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_NO_EXISTE"
			,CASE WHEN "COMPROBANTE_TESORERIA_LIQUIDADO" = 1 AND ("IdEstado" = 'LIQ') THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_LIQUIDADO"
			,CASE WHEN "COMPROBANTE_TESORERIA_GIRADO" = 1 AND ("IdEstado" = 'GIR') THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_GIRADO"
			,CASE WHEN "COMPROBANTE_TESORERIA_BLOQUEADO" = 1 AND ("IdEstado" = 'BLO') THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_BLOQUEADO"
			,CASE WHEN "COMPROBANTE_TESORERIA_ANULADO" = 1 AND ("IdEstado" = 'ANU') THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_ANULADO"
			,CASE WHEN "COMPROBANTE_TESORERIA_MODIFICADO" = 1 AND ("IdEstado" = 'MOD')  THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_MODIFICADO"
			,CASE WHEN "COMPROBANTE_TESORERIA_VENCIDO" = 1 AND DAYS_BETWEEN (TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD'), TO_DATE("FechaLiquidacion", 'YYYY-MM-DD')) < 0 THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_TESORERIA_VENCIDO"
	  FROM :v_MatrizValidaciones_ComprobanteGenerar;
		
	
	INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado"
	SELECT RVAL."NumeroReferencia",:p_ValidaComprobanteGenerado,RVAL."Codigo",RVAL."Resultado",'Id' as "CodigoDato1",CAST(MCG."IdComprobanteInformado" as NVARCHAR) as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
	  FROM :v_MatrizValidaciones_ComprobanteGenerar as MCG
	 INNER JOIN (SELECT  "NumeroReferencia","IdComprobanteInformado",
		  				COALESCE(			 			
				 			 "1" ||'COMPROBANTE_TESORERIA_NO_EXISTE'
							,"2" ||'COMPROBANTE_TESORERIA_LIQUIDADO'
							,"3" ||'COMPROBANTE_TESORERIA_GIRADO'
							,"4" ||'COMPROBANTE_TESORERIA_BLOQUEADO'
							,"5" ||'COMPROBANTE_TESORERIA_ANULADO'
							,"6" ||'COMPROBANTE_TESORERIA_MODIFICADO'
							,"7" ||'COMPROBANTE_TESORERIA_VENCIDO'
				 		) AS "Codigo",
				 		COALESCE(			 			
				 			 "1" ||"COMPROBANTE_TESORERIA_NO_EXISTE"
							,"2" ||"COMPROBANTE_TESORERIA_LIQUIDADO"
							,"3" ||"COMPROBANTE_TESORERIA_GIRADO"
							,"4" ||"COMPROBANTE_TESORERIA_BLOQUEADO"
							,"5" ||"COMPROBANTE_TESORERIA_ANULADO"
							,"6" ||"COMPROBANTE_TESORERIA_MODIFICADO"
							,"7" ||"COMPROBANTE_TESORERIA_VENCIDO"
				 		) AS "Resultado"
			       FROM :v_Validaciones_comprobanteGenerar,"BSCL.Plataforma.BD::PLA.Identidad") as RVAL
			 ON RVAL."NumeroReferencia" = MCG."NumeroReferencia" AND RVAL."IdComprobanteInformado" = MCG."IdComprobanteInformado"
	 WHERE RVAL."Resultado" IS NOT NULL;

 
	 
 	----------------------------------------------------
	--  ValidaComprobanteGenerado = 'S'
	 ----------------------------------------------------
	v_MatrizValidaciones_Comprobante =
	SELECT   CG."NumeroReferencia"
			,CG."IdComprobante"
		  	,CG."TipoComprobante"
			,CG."IdTipoMovimiento"
			,CG."IdConcepto"
			,CG."IdCaja"
			,CG."IdMoneda"
			,CG."Monto"
			,CG."FechaLiquidacion"
			,CG."FechaMovimiento"
			,CG."IdTipoPersona"
			,COALESCE(CON."GeneraComprobanteAutomaticamente", 'N') AS "GeneraComprobanteAutomaticamente"
			,COALESCE(TFP."IdMonedaFormaDePago",'') as "IdMonedaFormaDePago"
			,COALESCE(TCG."CantidadTipoComprobante",0) as "CantidadTipoComprobante"
	    	,COALESCE(TCG."CantidadIdTipoMovimiento",0) as "CantidadIdTipoMovimiento"
	    	,COALESCE(TCG."CantidadConcepto",0) as "CantidadConcepto"
	    	,COALESCE(TCG."CantidadMoneda",0) as "CantidadMoneda"
	    	,COALESCE(TCG."CantidadFechaLiquidacion",0) as "CantidadFechaLiquidacion"    		
	    	,COALESCE(TFP."CantidadFormasDePago",0) as "CantidadFormasDePago"
	    	,COALESCE(TFP."CantidadMonedaFormaDePago",0) as "CantidadMonedaFormaDePago"
	    	,COALESCE(FPC."CantidadConModalidadCaja",0) as "CantidadConModalidadCaja"
	    	,COALESCE(TCG."TotalMonto",0) as "TotalMonto"    		
	    	,COALESCE(TFP."TotalMontoFormaDePago",0) as "TotalMontoFormaDePago"
			
			
			,1 as "COMPROBANTE_CON_MEZCLA_DE_TIPO_COMPROBANTE"	
	    	,1 as "COMPROBANTE_CON_MEZCLA_DE_MONEDAS"
			,1 as "COMPROBANTE_CON_FECHAS_DISTINTAS_LIQUIDACION"			
			,CASE WHEN CG."TipoComprobante" <> '' THEN 1 ELSE 0 END as "COMPROBANTE_CON_MEZCLA_DE_CONCEPTOS"
			,CASE WHEN CG."TipoComprobante" <> '' THEN 1 ELSE 0 END as "COMPROBANTE_CON_MEZCLA_TIPO_MOVIMIENTO"			
			,1 as "NO_VIENEN_FORMAS_DE_PAGO"
			,1 as "FORMA_PAGO_CON_MEZCLA_DE_MONEDAS"
			,1 as "DIFERENTE_MONEDA_ENTRE_COMPROBANTE_Y_FORMA_PAGO"
			,1 as "MONTO_FORMA_PAGO_MAYOR_A_COMPROBANTE"
			,1 as "MONTO_FORMA_PAGO_MENOR_A_COMPROBANTE"
			,1 AS "COMPROBANTE_SIN_CAJA_ASIGNADA" -- valida si tiene alguna FP con modalidad caja (MODCAJA)
	  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_Comprobante" as CG
	 INNER JOIN (SELECT  "IdComprobante"
		  				,COUNT(DISTINCT "TipoComprobante") as "CantidadTipoComprobante"
				    	,COUNT(DISTINCT "IdTipoMovimiento") as "CantidadIdTipoMovimiento"
				    	,COUNT(DISTINCT "IdConcepto") as "CantidadConcepto" 
				    	,COUNT(DISTINCT "IdMoneda") as "CantidadMoneda"
				    	,CASE WHEN COUNT(DISTINCT "IdMoneda") = 1 THEN MAX("IdMoneda") END as "IdMoneda"
				    	,COUNT(DISTINCT "FechaLiquidacion") as "CantidadFechaLiquidacion"
				    	,SUM("Monto") as "TotalMonto"
				   FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_Comprobante"
		  		  GROUP BY "IdComprobante"
		  		) AS TCG ON TCG."IdComprobante" = CG."IdComprobante"
	  LEFT JOIN (SELECT  "IdComprobante"
		  				,COUNT(DISTINCT "IdComprobante") as "CantidadFormasDePago"
		 				,COUNT(DISTINCT "IdMoneda") as "CantidadMonedaFormaDePago"
		 				,CASE WHEN COUNT(DISTINCT "IdMoneda") = 1 THEN MAX("IdMoneda") ELSE '' END as "IdMonedaFormaDePago"
		  				,SUM("Monto") as "TotalMontoFormaDePago"
				   FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteFormaDePago"
		  		  GROUP BY "IdComprobante"
		 		) AS TFP ON TFP."IdComprobante" = CG."IdComprobante"
	  LEFT JOIN (SELECT  "IdComprobante" 
	  				     ,COUNT(DISTINCT "IdComprobante") as "CantidadConModalidadCaja"
				   FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteFormaDePago"
				   WHERE "IdModalidadDePago" = 'MODCAJ'
		  		  GROUP BY "IdComprobante"
		 		) AS FPC ON FPC."IdComprobante" = CG."IdComprobante"
	LEFT JOIN "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" AS CON ON (CON."IdConcepto" = CG."IdConcepto")
	 WHERE :p_ValidaComprobanteGenerado = 'S';
	
	v_Validaciones_Comprobante =
	SELECT  "NumeroReferencia","IdComprobante"
			,CASE WHEN "COMPROBANTE_CON_MEZCLA_DE_TIPO_COMPROBANTE" = 1 AND "CantidadTipoComprobante" > 1 THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_CON_MEZCLA_DE_TIPO_COMPROBANTE"
			,CASE WHEN "COMPROBANTE_CON_MEZCLA_DE_MONEDAS" = 1 AND "CantidadMoneda" > 1 THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_CON_MEZCLA_DE_MONEDAS"
			,CASE WHEN "COMPROBANTE_CON_FECHAS_DISTINTAS_LIQUIDACION" = 1 AND "CantidadFechaLiquidacion" > 1 THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_CON_FECHAS_DISTINTAS_LIQUIDACION"
			--,CASE WHEN "COMPROBANTE_CON_MEZCLA_DE_CONCEPTOS" = 1 AND "CantidadConcepto" > 1 THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_CON_MEZCLA_DE_CONCEPTOS"
			--,CASE WHEN "COMPROBANTE_CON_MEZCLA_TIPO_MOVIMIENTO" = 1 AND "CantidadIdTipoMovimiento" > 1 THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_CON_MEZCLA_TIPO_MOVIMIENTO"
			,CASE WHEN "NO_VIENEN_FORMAS_DE_PAGO" = 1 AND "CantidadFormasDePago" = 0 AND "IdTipoPersona" = 'CLI' AND "GeneraComprobanteAutomaticamente" = 'N' THEN 'RECH' ELSE 'OK' END AS "NO_VIENEN_FORMAS_DE_PAGO"
			,CASE WHEN "FORMA_PAGO_CON_MEZCLA_DE_MONEDAS" = 1 AND "CantidadFormasDePago" > 0 AND "CantidadMonedaFormaDePago" > 1 THEN 'RECH' ELSE 'OK' END AS "FORMA_PAGO_CON_MEZCLA_DE_MONEDAS"			
			,CASE WHEN "DIFERENTE_MONEDA_ENTRE_COMPROBANTE_Y_FORMA_PAGO" = 1 AND "CantidadFormasDePago" > 0 AND "IdMoneda" <> "IdMonedaFormaDePago" THEN 'RECH' ELSE 'OK' END AS "DIFERENTE_MONEDA_ENTRE_COMPROBANTE_Y_FORMA_PAGO"			
			,CASE WHEN "MONTO_FORMA_PAGO_MAYOR_A_COMPROBANTE" = 1 AND "CantidadFormasDePago" > 0 AND (abs("TotalMonto") - "TotalMontoFormaDePago") < 0 THEN 'RECH' ELSE 'OK' END AS "MONTO_FORMA_PAGO_MAYOR_A_COMPROBANTE"
			,CASE WHEN "MONTO_FORMA_PAGO_MENOR_A_COMPROBANTE" = 1 AND "CantidadFormasDePago" > 0 AND (abs(CAST("TotalMonto" AS decimal(26,2))) - CAST("TotalMontoFormaDePago" AS decimal(26,2))) > 0 THEN 'RECH' ELSE 'OK' END AS "MONTO_FORMA_PAGO_MENOR_A_COMPROBANTE"
			,CASE WHEN "COMPROBANTE_SIN_CAJA_ASIGNADA" = 1 AND "CantidadConModalidadCaja" > 0 AND COALESCE("IdCaja",0) = 0 THEN 'RECH' ELSE 'OK' END AS "COMPROBANTE_SIN_CAJA_ASIGNADA"
			
	  FROM :v_MatrizValidaciones_Comprobante
	 GROUP BY "NumeroReferencia","IdComprobante","CantidadTipoComprobante","CantidadIdTipoMovimiento","CantidadConcepto","CantidadMoneda","CantidadFechaLiquidacion","CantidadFormasDePago","IdTipoPersona",
	 		  "CantidadMonedaFormaDePago","CantidadConModalidadCaja","IdMoneda","IdMonedaFormaDePago","TotalMonto","TotalMontoFormaDePago","IdCaja","COMPROBANTE_CON_MEZCLA_DE_TIPO_COMPROBANTE",
	 		  "COMPROBANTE_CON_MEZCLA_DE_MONEDAS","COMPROBANTE_CON_FECHAS_DISTINTAS_LIQUIDACION","COMPROBANTE_CON_MEZCLA_DE_CONCEPTOS","COMPROBANTE_CON_MEZCLA_TIPO_MOVIMIENTO","NO_VIENEN_FORMAS_DE_PAGO",
	 		  "FORMA_PAGO_CON_MEZCLA_DE_MONEDAS","DIFERENTE_MONEDA_ENTRE_COMPROBANTE_Y_FORMA_PAGO","MONTO_FORMA_PAGO_MAYOR_A_COMPROBANTE","MONTO_FORMA_PAGO_MENOR_A_COMPROBANTE",
	 		  "COMPROBANTE_SIN_CAJA_ASIGNADA", "GeneraComprobanteAutomaticamente";
	 
	
	INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado"
	SELECT RVAL."IdComprobante" as "NumeroReferencia",:p_ValidaComprobanteGenerado,RVAL."Codigo",RVAL."Resultado",'Id' as "CodigoDato1",CAST(MCG."IdComprobante" as NVARCHAR) as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
	  FROM :v_MatrizValidaciones_Comprobante as MCG
	 INNER JOIN (SELECT  "NumeroReferencia","IdComprobante",
		  				COALESCE(
				 			 "1" ||'COMPROBANTE_CON_MEZCLA_DE_TIPO_COMPROBANTE'
							,"2" ||'COMPROBANTE_CON_MEZCLA_DE_MONEDAS'
							,"3" ||'COMPROBANTE_CON_FECHAS_DISTINTAS_LIQUIDACION'
							--,"4" ||'COMPROBANTE_CON_MEZCLA_DE_CONCEPTOS'
							--,"5" ||'COMPROBANTE_CON_MEZCLA_TIPO_MOVIMIENTO'
							,"6" ||'NO_VIENEN_FORMAS_DE_PAGO'
							,"7" ||'FORMA_PAGO_CON_MEZCLA_DE_MONEDAS'							
							,"8" ||'DIFERENTE_MONEDA_ENTRE_COMPROBANTE_Y_FORMA_PAGO'
							,"9" ||'MONTO_FORMA_PAGO_MAYOR_A_COMPROBANTE'
							,"10"||'MONTO_FORMA_PAGO_MENOR_A_COMPROBANTE'
							,"11"||'COMPROBANTE_SIN_CAJA_ASIGNADA'
				 		) AS "Codigo",
				 		COALESCE(			 			
				 			 "1" ||"COMPROBANTE_CON_MEZCLA_DE_TIPO_COMPROBANTE"
							,"2" ||"COMPROBANTE_CON_MEZCLA_DE_MONEDAS"
							,"3" ||"COMPROBANTE_CON_FECHAS_DISTINTAS_LIQUIDACION"
							--,"4" ||"COMPROBANTE_CON_MEZCLA_DE_CONCEPTOS"
							--,"5" ||"COMPROBANTE_CON_MEZCLA_TIPO_MOVIMIENTO"
							,"6" ||"NO_VIENEN_FORMAS_DE_PAGO"
							,"7" ||"FORMA_PAGO_CON_MEZCLA_DE_MONEDAS"
							,"8" ||"DIFERENTE_MONEDA_ENTRE_COMPROBANTE_Y_FORMA_PAGO"
							,"9" ||"MONTO_FORMA_PAGO_MAYOR_A_COMPROBANTE"
							,"10"||"MONTO_FORMA_PAGO_MENOR_A_COMPROBANTE"
							,"11"||"COMPROBANTE_SIN_CAJA_ASIGNADA"
				 		) AS "Resultado"
			       FROM :v_Validaciones_Comprobante,"BSCL.Plataforma.BD::PLA.Identidad") as RVAL
			 ON RVAL."NumeroReferencia" = MCG."NumeroReferencia" AND RVAL."IdComprobante" = MCG."IdComprobante"
	 WHERE RVAL."Resultado" IS NOT NULL;
END;