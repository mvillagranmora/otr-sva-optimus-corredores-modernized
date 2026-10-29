SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosModificarTransaccion";
GO
CREATE PROCEDURE
-- 
--  Proposito: VALIDACIONES DEL SERVICIO CREAR TRANSACCION
--  Autor: PROJO - BCS
--  Fecha Creacion: 15/01/2016
--     

"BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosModificarTransaccion"(
	
	IN p_IdTransaccion						BIGINT,
	IN p_IdEstado							NVARCHAR(3),
	IN p_Cantidad							DECIMAL(26,8),
	IN p_Monto								DECIMAL(26,8),
	IN p_Precio								DECIMAL(26,8),
	IN p_FechaEmision						NVARCHAR(10),
	IN p_Tasa								DECIMAL(26,8),
	IN p_ValorRescate						DECIMAL(26,8),
	IN p_AccionTipoOperacion				NVARCHAR(40)	DEFAULT NULL,
	IN p_TipoCambioCorresponsal				DECIMAL(26,8)	DEFAULT NULL,
	IN p_IdCorredorCorresponsal				BIGINT			DEFAULT NULL,
	IN p_MontoLiquidacionCorresponsal		DECIMAL(26,8)	DEFAULT NULL,
	IN p_GastosCorresponsal					DECIMAL(26,8)	DEFAULT NULL,
	IN p_IdMonedaLiquidacionCorresponsal	NVARCHAR(3)		DEFAULT NULL,

	OUT p_out_SalidaDetalle		"BSCL.Orden.BD.Tipos::SalidaDetalle",
	OUT p_out_Salida        	"BSCL.Orden.BD.Tipos::SalidaResumen"
	
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
	AS
BEGIN
	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE v_ErroresContador INT;
	
	BEGIN
		DECLARE v_TablaRequisitos TABLE(
		 	"Resultado" 		NVARCHAR(40),
			"Codigo" 			NVARCHAR(40), 
			"CodigoDato1" 		NVARCHAR(100), 
			"ValorDato1" 		NVARCHAR(502),
			"CodigoDato2" 		NVARCHAR(100), 
			"ValorDato2" 		NVARCHAR(502)
		);
		DECLARE v_resultadoRegla 	nvarchar(40);
		DECLARE VECTOR_VALIDACIONES INTEGER ARRAY;
		DECLARE v_Vector			INTEGER;
		DECLARE v_MensajeRegla       NVARCHAR(520);
		
		DECLARE v_IdClasificacionTipoInstrumento 	NVARCHAR(2);
		DECLARE v_countAsignaciones 			 	BIGINT;
		
		DECLARE ESTADO_TRANSACCION									INTEGER	:= 23;
		DECLARE CANTIDAD_MENOR_IGUAL_A_CERO							INTEGER	:= 24;
		DECLARE PRECIO_MENOR_IGUAL_A_CERO							INTEGER	:= 25;
		DECLARE MONTO_MENOR_IGUAL_A_CERO							INTEGER := 26;
		
		DECLARE FECHA_EMISION_INSTRUMENTO_INVALIDA					INTEGER := 28;
		DECLARE VALOR_RESCATE_MENOR_IGUAL_A_CERO					INTEGER	:= 32;
		DECLARE LIBRO_ORDENES_NO_EXISTE								INTEGER := 33;
		DECLARE TRANSACCION_NO_EXISTE								INTEGER := 34;
		DECLARE TRANSACCION_TIENE_ASIGNACIONES						INTEGER := 35;
		DECLARE VALOR_LIQUIDACION_CORRESPONSAL						INTEGER := 36;
		DECLARE v_MontoMonedaExtranjera 						    DECIMAL(26,2);
		DECLARE v_Epsilon				 						    DECIMAL(2,2);
		
		--POR DEFECTO TODAS LAS VALIDACIONES VAN ENCENDIDAS

		VECTOR_VALIDACIONES[:ESTADO_TRANSACCION] 					:= 1;
		VECTOR_VALIDACIONES[:TRANSACCION_NO_EXISTE] 				:= 1;
		VECTOR_VALIDACIONES[:CANTIDAD_MENOR_IGUAL_A_CERO]			:= 1;
		VECTOR_VALIDACIONES[:PRECIO_MENOR_IGUAL_A_CERO]				:= 1;
		VECTOR_VALIDACIONES[:MONTO_MENOR_IGUAL_A_CERO]				:= 1;
		VECTOR_VALIDACIONES[:FECHA_EMISION_INSTRUMENTO_INVALIDA]	:= 1;
		VECTOR_VALIDACIONES[:VALOR_RESCATE_MENOR_IGUAL_A_CERO]		:= 1;
		VECTOR_VALIDACIONES[:LIBRO_ORDENES_NO_EXISTE]				:= 1;
		VECTOR_VALIDACIONES[:TRANSACCION_TIENE_ASIGNACIONES]		:= 1;
		VECTOR_VALIDACIONES[:VALOR_LIQUIDACION_CORRESPONSAL]		:= 1;
		
		
		
		--********************** VALIDACIONES ***********************************
		--valida que la transaccion exista
		v_ref_transaccion = SELECT :p_IdTransaccion as "IdTransaccion",COALESCE("IdTransaccion",0) as "IdTransaccionReal","IdClasificacionTipoInstrumento"
							FROM "BSCL.Orden.BD::ORD.Transaccion" where "IdTransaccion" = :p_IdTransaccion;

		v_Vector := :VECTOR_VALIDACIONES[:TRANSACCION_NO_EXISTE];
		v_TablaRequisitos = 
		SELECT CASE WHEN "IdTransaccionReal" = 0 AND :v_Vector = 1 THEN 'RECHAZO' ELSE 'OK' END as "Resultado",
		       'TRANSACCION_NO_EXISTE' as "Codigo",
		       'IdTransaccion' as "CodigoDato1" ,
		       cast(:p_IdTransaccion as NVARCHAR(502)) as "ValorDato1",
		       '' as "CodigoDato2",
		       '' as "ValorDato2"
		       
		FROM :v_ref_transaccion;
		
		SELECT "IdClasificacionTipoInstrumento" into v_IdClasificacionTipoInstrumento FROM :v_ref_transaccion;
		
		--APAGAR VALIDACIONES
		IF (:v_IdClasificacionTipoInstrumento = 'RV' AND :v_IdClasificacionTipoInstrumento = 'DV') THEN
			VECTOR_VALIDACIONES[:FECHA_EMISION_INSTRUMENTO_INVALIDA] := 0;

		END IF;
		
		---------------------------------------------------------------------  
		v_Vector := :VECTOR_VALIDACIONES[:CANTIDAD_MENOR_IGUAL_A_CERO];
		v_TablaRequisitos = 
		SELECT 	CASE WHEN :p_Cantidad IS NOT NULL AND (:p_Cantidad <= 0 AND :v_Vector = 1) THEN 'RECHAZO' ELSE 'OK' END as "Resultado",
		       	'CANTIDAD_MENOR_IGUAL_A_CERO' as "Codigo",
		       	'IdTransaccion' as "CodigoDato1",
		       	cast(:p_Cantidad as NVARCHAR(502)) as "ValorDato1",
				'' as "CodigoDato2",
				'' as "ValorDato2"
		FROM DUMMY
		UNION ALL SELECT * FROM :v_TablaRequisitos;
		
		---------------------------------------------------------------------  
		v_Vector := :VECTOR_VALIDACIONES[:ESTADO_TRANSACCION];
		v_TablaRequisitos = 
		SELECT 	CASE WHEN :p_IdEstado = 'ANU' THEN 'RECHAZO' ELSE 'OK' END as "Resultado",
		       	'ERROR_MODIFICAR_TRANSACCION_ANULADA' as "Codigo",
		       	'IdTransaccion' as "CodigoDato1",
		       	cast(:p_Cantidad as NVARCHAR(502)) as "ValorDato1",
				'' as "CodigoDato2",
				'' as "ValorDato2"
		FROM DUMMY
		UNION ALL SELECT * FROM :v_TablaRequisitos;
		
		---------------------------------------------------------------------  
		v_Vector := :VECTOR_VALIDACIONES[:PRECIO_MENOR_IGUAL_A_CERO];
		v_TablaRequisitos = 
		SELECT CASE WHEN :p_Precio IS NOT NULL AND (:p_Precio <= 0 AND :v_Vector = 1) THEN 'RECHAZO' ELSE 'OK' END as "Resultado",
		       'PRECIO_MENOR_IGUAL_A_CERO' as "Codigo",
		       'IdTransaccion' as "CodigoDato1",
		       cast(:p_Precio as NVARCHAR(502)) as "ValorDato1",
		       '' as "CodigoDato2",
				'' as "ValorDato2" 
		FROM DUMMY
		UNION ALL SELECT * FROM :v_TablaRequisitos;
		
		---------------------------------------------------------------------  
		v_Vector := :VECTOR_VALIDACIONES[:MONTO_MENOR_IGUAL_A_CERO];
		v_TablaRequisitos = 
		SELECT CASE WHEN :p_Monto IS NOT NULL AND (:p_Monto <= 0 AND :v_Vector = 1) THEN 'RECHAZO' ELSE 'OK' END as "Resultado",
		       'MONTO_MENOR_IGUAL_A_CERO' as "Codigo",
		      'IdTransaccion' as "CodigoDato1",
		        cast(:p_Monto as NVARCHAR(502)) as "ValorDato1",
		        '' as "CodigoDato2",
				'' as "ValorDato2"  
		FROM DUMMY
		UNION ALL SELECT * FROM :v_TablaRequisitos;
		
		
		---------------------------------------------------------------------  
		v_Vector := :VECTOR_VALIDACIONES[:VALOR_RESCATE_MENOR_IGUAL_A_CERO];
		v_TablaRequisitos = 
		SELECT CASE WHEN :p_ValorRescate IS NOT NULL AND (:p_ValorRescate <= 0 AND :v_Vector = 1) THEN 'RECHAZO' ELSE 'OK' END as "Resultado",
		       'VALOR_RESCATE_MENOR_IGUAL_A_CERO' as "Codigo",
		       'IdTransaccion' as "CodigoDato1",
		        cast(:p_ValorRescate as NVARCHAR(502)) as "ValorDato1",
		        '' as "CodigoDato2",
				'' as "ValorDato2"  
		FROM DUMMY
		UNION ALL SELECT * FROM :v_TablaRequisitos;
		
		---------------------------------------------------------------------
		v_TablaRequisitos = 
		SELECT CASE WHEN (:v_countAsignaciones > 0 AND :v_Vector = 1) THEN 'RECHAZO' ELSE 'OK' END as "Resultado",
		       'TRANSACCION_TIENE_ASIGNACIONES' as "Codigo",
		       'IdTransaccion' as "CodigoDato1",
		       cast(:p_IdTransaccion as NVARCHAR(502)) as "ValorDato1",
		       '' as "CodigoDato2",
				'' as "ValorDato2"  	  
		FROM DUMMY
		UNION ALL SELECT * FROM :v_TablaRequisitos;
		--------------------------------------------------------------------
	 
		
		v_Vector := :VECTOR_VALIDACIONES[:VALOR_LIQUIDACION_CORRESPONSAL];
		
		-- Valida que slos datos del corresponsal estén ingresados completamente 	
		v_TablaRequisitos = 
		SELECT 
				CASE
					WHEN :v_Vector = 1 AND (:p_GastosCorresponsal <> 0 AND (:p_MontoLiquidacionCorresponsal = 0 OR :p_TipoCambioCorresponsal = 0 OR :p_IdCorredorCorresponsal = 0 OR :p_IdMonedaLiquidacionCorresponsal = ''))  THEN 'RECHAZO'
					WHEN :v_Vector = 1 AND (:p_MontoLiquidacionCorresponsal <> 0 AND (:p_GastosCorresponsal = 0 OR :p_TipoCambioCorresponsal = 0 OR :p_IdCorredorCorresponsal = 0 OR :p_IdMonedaLiquidacionCorresponsal = ''))  THEN 'RECHAZO'
					WHEN :v_Vector = 1 AND (:p_TipoCambioCorresponsal <> 0 AND (:p_MontoLiquidacionCorresponsal = 0 OR :p_GastosCorresponsal = 0 OR :p_IdCorredorCorresponsal = 0 OR :p_IdMonedaLiquidacionCorresponsal = ''))  THEN 'RECHAZO'
					WHEN :v_Vector = 1 AND (:p_IdMonedaLiquidacionCorresponsal <> '' AND (:p_MontoLiquidacionCorresponsal = 0 OR :p_GastosCorresponsal = 0 OR :p_IdCorredorCorresponsal = 0 OR :p_TipoCambioCorresponsal = 0))  THEN 'RECHAZO'
					WHEN :v_Vector = 1 AND (:p_IdCorredorCorresponsal <> 0 AND (:p_MontoLiquidacionCorresponsal = 0 OR :p_GastosCorresponsal = 0 OR :p_TipoCambioCorresponsal = 0 OR :p_IdMonedaLiquidacionCorresponsal = ''))  THEN 'RECHAZO'
					
					ELSE 'OK' 
				END as "Resultado",
		       'DATOS_CORRESPONSAL_INCOMPLETOS' as "Codigo",
		       'IdTransaccion' as "CodigoDato1" ,
		       cast(:p_IdTransaccion as NVARCHAR(502)) as "ValorDato1",
		       '' as "CodigoDato2",
			   '' as "ValorDato2"  
		FROM DUMMY
		UNION ALL SELECT * FROM :v_TablaRequisitos;		
		
		
		
		IF (:p_TipoCambioCorresponsal <> 0) THEN
			v_MontoMonedaExtranjera = CAST(:p_Monto/:p_TipoCambioCorresponsal AS DECIMAL(26,2));
			v_Epsilon = 0.5;      -- Variación permitida
			
			v_TablaRequisitos = 
			SELECT 
					CASE 
						WHEN :v_Vector = 1 AND :p_AccionTipoOperacion = 'COMPRA' AND (((p_MontoLiquidacionCorresponsal-p_GastosCorresponsal)-(:v_MontoMonedaExtranjera)) > v_Epsilon OR ((p_MontoLiquidacionCorresponsal-p_GastosCorresponsal)-(:v_MontoMonedaExtranjera))  < (v_Epsilon*-1))  THEN 'RECHAZO' 
						WHEN :v_Vector = 1 AND :p_AccionTipoOperacion = 'VENTA'  AND (((p_MontoLiquidacionCorresponsal+p_GastosCorresponsal)-(:v_MontoMonedaExtranjera)) > v_Epsilon OR ((p_MontoLiquidacionCorresponsal+p_GastosCorresponsal)-(:v_MontoMonedaExtranjera))  < (v_Epsilon*-1))  THEN 'RECHAZO'
						ELSE 'OK' 
					END as "Resultado",
			       'VALOR_LIQUIDACION_CORRESPONSAL_ERRONEO' as "Codigo",
			       'MontoLiquidacionOk' as "CodigoDato1",
					
			        CAST((CASE
						WHEN :p_AccionTipoOperacion = 'COMPRA' THEN CAST(:v_MontoMonedaExtranjera+p_GastosCorresponsal AS DECIMAL(26,2)) 
						WHEN :p_AccionTipoOperacion = 'VENTA'  THEN CAST(:v_MontoMonedaExtranjera-p_GastosCorresponsal AS DECIMAL(26,2))
						ELSE 0 
					END)AS NVARCHAR(502)) as "ValorDato1",
					
			        '' as "CodigoDato2",
					'' as "ValorDato2"  
			FROM DUMMY
			UNION ALL SELECT * FROM :v_TablaRequisitos;
		END IF;   --  IF (:p_TipoCambioCorresponsal <> 0) THEN
		
		
		
		
		p_out_SalidaDetalle = SELECT :p_IdTransaccion as "IdInterno", :p_IdTransaccion as "IdOperacion", "Resultado","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2"
								FROM :v_TablaRequisitos;
		
    	p_out_Salida = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:p_out_SalidaDetalle);				
	END;
	
END;
