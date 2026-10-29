SET SCHEMA "BSCL";
GO 
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearPartidaIngresoEgresoDesdeMovimientos";
GO  
CREATE PROCEDURE
-- 
--  Proposito: Valida que los movimientos que van a generar una partida de ingreso o egreso cumplan ciertas condiciones
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 28/03/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearPartidaIngresoEgresoDesdeMovimientos"(
 
	IN p_TipoMovimiento		           NVARCHAR(6),  -- PARING | PAREGR  -- Indica si los movimientos enviados son para generar partida de ingresos o egresos 
    IN p_IngresoEgresoGenerar          TABLE("IdIngresoEgreso" BIGINT),
 
	-- Salidas comunes para todos los movimientos
	OUT p_out_IdConcepto               NVARCHAR(6),    -- Si no es comun le asigna multiconcepto 
	OUT p_out_NombreConcepto	       NVARCHAR(100),
    OUT p_out_IdMoneda 				   NVARCHAR(3),    
    OUT p_out_NombreMoneda 			   NVARCHAR(100),
    OUT p_out_FechaLiquidacion         NVARCHAR(10),   
    OUT p_out_IdCaja                   BIGINT,         -- IdCaja de los mov que tienen caja asignada  
    OUT p_out_Monto 				   DECIMAL(26,8),  -- Monto Total


	-- Salidas para mensajes de errores
    OUT p_out_AdvertenciasContador 		INT,
    OUT p_out_AdvertenciasDetalle  		TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
    OUT p_out_ErroresContador      	    INT,
	OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(520)),
	OUT p_out_resultadoRegla            NVARCHAR(40) --OK   = Cumple todos los requisitos,		
													--ADVE = Advertencia
                                                   	--RECH = Alguna de las reglas detienen la operacion
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_erroroutmsg  			NVARCHAR(520);
	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	DECLARE v_resultadoRegla       	NVARCHAR(40);
	DECLARE v_ErroresContador 		INT; 
	DECLARE v_CountMovimientosConMedioDePagoAsignado INT;
	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA     
	BEGIN
		p_out_resultadoRegla	:= 'RECH';	
		v_hash = SELECT '' AS "Codigo", 0 AS "Valor" FROM DUMMY;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData, :v_hash, :v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	p_out_ErroresDetalle	= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;

	
	BEGIN	
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio			BIGINT;
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
        DECLARE v_CountTablaRequisitos  INTEGER;
		DECLARE v_CountIdCaja           INTEGER;

		DECLARE v_TablaRequisitos     	   TABLE (   "Prioridad" 				INT
													,"Cumple" 					BIGINT
													,"CodigoError" 				NVARCHAR(100)
												    ,"CodigoDato" 				NVARCHAR(100)
												    ,"ValorDato" 				NVARCHAR(520)
												    ,"IdIngresoEgreso" 			BIGINT
												  );			
		

		
			
						
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299  -- No se han encontrado datos
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	  = SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;
		v_TablaRequisitos    	  = SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "IdIngresoEgreso" FROM dummy WHERE (1=2);		

		p_out_AdvertenciasContador		:= 0;
		p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;


		---------------------------------------------------------------
		-- VALIDACIONES BASICAS
		---------------------------------------------------------------
		
		-- Valida que vengan movimientos
		v_TablaRequisitos =
		SELECT 	1 AS "Prioridad",	
		       	CASE 
		       		WHEN COUNT("IdIngresoEgreso") < 1 THEN 0 
		       		ELSE 1 
				END AS "Cumple", 
		       	'SIN_ESPECIFICAR_MOVIMIENTOS_PARA_PROCESAR' AS "CodigoError",
		       	'' AS "CodigoDato",
		       	'' AS "ValorDato"
		       	,0 AS "IdIngresoEgreso"
		FROM :p_IngresoEgresoGenerar
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdIngresoEgreso" FROM :v_TablaRequisitos;
		
		

		-- Validar los folios de movimientos existan y sean válidos
		v_TablaRequisitos = 
		SELECT  1 AS "Prioridad",
				CASE WHEN (COALESCE(MOV."IdIngresoEgreso",0) = 0) or IEG."IdIngresoEgreso" = 0 THEN 0 ELSE 1 END AS "Cumple",
				'MOVIMIENTO_TESORERIA_INVALIDO' AS "CodigoError",
		       	'IdIngresoEgreso' AS "CodigoDato",
		       	CAST(IEG."IdIngresoEgreso" as NVARCHAR(50)) AS "ValorDato",
		       	IEG."IdIngresoEgreso" AS "IdIngresoEgreso"	       	
		FROM :p_IngresoEgresoGenerar IEG		
		LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV ON MOV."IdIngresoEgreso" = IEG."IdIngresoEgreso"
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdIngresoEgreso" FROM :v_TablaRequisitos;


 
		-- Calcula cantidad de errores hasta este punto
		SELECT Count("IdIngresoEgreso")
		INTO v_CountTablaRequisitos
		FROM :v_TablaRequisitos
		WHERE "Cumple" = 0;

	
		SELECT '', '','', '', 0, 0
		INTO p_out_IdConcepto, p_out_IdMoneda, p_out_NombreMoneda, p_out_FechaLiquidacion, p_out_Monto, p_out_IdCaja
		FROM DUMMY;


		-- Si vienen movimientos sin errores
		IF (v_CountTablaRequisitos = 0) THEN 
		
				---------------------------------------------------------------
				-- VALIDACIONES DE NEGOCIO
				---------------------------------------------------------------

				-- Valida que los movimientos existan y que no esten bloqueados, anulados ni emitidos
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
						CASE WHEN COALESCE(MOV."IdIngresoEgreso",0) = 0 THEN 0  -- Si el movimiento no existe
						 	 WHEN (MOV."IdEstado" = 'ANU') THEN 0
							 WHEN (MOV."IdEstado" = 'BLO') THEN 0
							 WHEN (MOV."IdEstado" = 'EMI') THEN 0
							 ELSE  1
						END AS "Cumple",
						CASE WHEN COALESCE(MOV."IdIngresoEgreso",0) = 0 THEN 'MOVIMIENTO_TESORERIA_INVALIDO'
						 	 WHEN (MOV."IdEstado" = 'ANU') THEN 'MOVIMIENTO_TESORERIA_ANULADO'
							 WHEN (MOV."IdEstado" = 'BLO') THEN 'MOVIMIENTO_TESORERIA_BLOQUEADO'
							 WHEN (MOV."IdEstado" = 'EMI') THEN 'MOVIMIENTO_TESORERIA_EMITIDO'
							 ELSE  ''
						END AS "CodigoError",
						'IdIngresoEgreso' AS "CodigoDato",								
				       	CAST(IEG."IdIngresoEgreso" as NVARCHAR(50)) AS "ValorDato"		       	
				       	,IEG."IdIngresoEgreso"
				FROM :p_IngresoEgresoGenerar as IEG
				INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV ON MOV."IdIngresoEgreso" = IEG."IdIngresoEgreso"		
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdIngresoEgreso" FROM :v_TablaRequisitos;


				/************
				-- Valida que los movimientos correspondan a ingresos.
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",		
						CASE WHEN (MOV."IdTipoMovimiento" = 'MOVEGR') THEN 0  -- movimiento es egreso
							 ELSE  1
						END AS "Cumple",				
						CASE WHEN (MOV."IdTipoMovimiento" = 'MOVEGR') THEN 'MOVIMIENTO_TESORERIA_ES_EGRESO'
							 ELSE  ''
						END AS "CodigoError",								
						'IdIngresoEgreso' AS "CodigoDato",								
				       	CAST(MOV."IdIngresoEgreso" as NVARCHAR(50)) AS "ValorDato"		       	
				       	,MOV."IdIngresoEgreso"
				       	
				FROM :p_IngresoEgresoGenerar as IEG		
				INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV ON MOV."IdIngresoEgreso" = IEG."IdIngresoEgreso"		
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdIngresoEgreso" FROM :v_TablaRequisitos;
				***************/		
				
				
				-- Valida para PARING --> sólo mov de ingreso     y   PAREGR --> sólo movimientos de egreso
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
				
						CASE 
							WHEN (MOV."IdTipoMovimiento" = 'MOVEGR' AND :p_TipoMovimiento = 'PARING') THEN 0  -- para partida de ingreso vienen movimientos de egreso
							WHEN (MOV."IdTipoMovimiento" = 'MOVING' AND :p_TipoMovimiento = 'PAREGR') THEN 0  -- para partida de egreso vienen movimientos de ingreso
							ELSE  1
						END AS "Cumple",
						CASE
							WHEN (MOV."IdTipoMovimiento" = 'MOVEGR' AND :p_TipoMovimiento = 'PARING') THEN 'MOVIMIENTO_TESORERIA_ES_EGRESO'
							WHEN (MOV."IdTipoMovimiento" = 'MOVING' AND :p_TipoMovimiento = 'PAREGR') THEN 'MOVIMIENTO_TESORERIA_ES_INGRESO'
							ELSE  ''
						END AS "CodigoError",
						'IdIngresoEgreso' AS "CodigoDato",								
				       	CAST(MOV."IdIngresoEgreso" as NVARCHAR(50)) AS "ValorDato"		       	
				       	,MOV."IdIngresoEgreso"
				       	
				FROM :p_IngresoEgresoGenerar as IEG		
				INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV ON MOV."IdIngresoEgreso" = IEG."IdIngresoEgreso"		
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdIngresoEgreso" FROM :v_TablaRequisitos;				
				
				
				
				
				-- Valida que todos los movimientos tengan la misma fecha de liquidación
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
						CASE WHEN (Count(Distinct MOV."FechaLiquidacion") > 1) THEN 0  -- fecha de liquidación distinta entre movimientos
							 ELSE  1  
						END AS "Cumple",
						'FECHA_LIQUICADION_MOVIMIENTOS_NO_COINCIDE' AS "CodigoError",
						'IdIngresoEgreso' AS "CodigoDato",				
				       	'' AS "ValorDato"		       	
				       	,0 AS "IdIngresoEgreso"		       	
				FROM :p_IngresoEgresoGenerar as IEG
				INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV ON MOV."IdIngresoEgreso" = IEG."IdIngresoEgreso"		
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdIngresoEgreso" FROM :v_TablaRequisitos;
		
		
		
				-- Valida que todos los movimientos tengan la misma moneda
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
						CASE WHEN (Count(Distinct MOV."IdMoneda") > 1) THEN 0  -- moneda distinta entre movimientos
							 ELSE  1  
						END AS "Cumple",
						'MONEDA_MOVIMIENTOS_NO_COINCIDE' AS "CodigoError",
						'IdIngresoEgreso' AS "CodigoDato",				
				       	'' AS "ValorDato"		       	
				       	,0 AS "IdIngresoEgreso"		       	
				FROM :p_IngresoEgresoGenerar as IEG
				INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV ON MOV."IdIngresoEgreso" = IEG."IdIngresoEgreso"		
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdIngresoEgreso" FROM :v_TablaRequisitos;
		

				-- Valida que los movimientos que tiene caja asignada, tengan la misma caja.
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
						CASE WHEN (Count(Distinct MOV."IdCaja") > 1) THEN 0  -- caja distinta entre movimientos
							 ELSE  1  
						END AS "Cumple",
						'CAJA_MOVIMIENTOS_NO_COINCIDE' AS "CodigoError",
						'IdIngresoEgreso' AS "CodigoDato",				
				       	'' AS "ValorDato"		       	
				       	,0 AS "IdIngresoEgreso"		       	
				FROM :p_IngresoEgresoGenerar as IEG
				INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV ON MOV."IdIngresoEgreso" = IEG."IdIngresoEgreso"		
				AND MOV."IdCaja" <> 0  -- Considerar solo movimientos con caja asignada
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdIngresoEgreso" FROM :v_TablaRequisitos;




				-- Valida que todos los movimientos que tienen secuencia de agrupación, tenga la misma secuencia
				v_TablaRequisitos = 
		       	SELECT   2 AS "Prioridad",
						CASE WHEN (Count(Distinct MOV."SecuenciaAgrupacion") > 1) THEN 0  -- secuencia de agrupación distinto entre movimientos
							 ELSE  1
						END AS "Cumple",
						'SECUENCIA_AGRUPACION_MOVIMIENTOS_NO_COINCIDE' AS "CodigoError",
						'IdIngresoEgreso' AS "CodigoDato",				
				       	'' AS "ValorDato"		       	
				       	,0 AS "IdIngresoEgreso"
		        FROM :p_IngresoEgresoGenerar as IEG
				INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV ON MOV."IdIngresoEgreso" = IEG."IdIngresoEgreso"
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdIngresoEgreso" FROM :v_TablaRequisitos;
             

				-----------------------------------------------------------------------------
				-- Valida que todos los movimientos tengan un medio de pago en comun
		        -- No considera los movimientos sin medio de pago asignado
				-----------------------------------------------------------------------------
		
		
				-- Obtiene cantidad de movimientos con forma de pago asignado
				SELECT COUNT("IdIngresoEgreso")
				INTO v_CountMovimientosConMedioDePagoAsignado
				FROM 
				(
						SELECT IEG."IdIngresoEgreso"
						FROM :p_IngresoEgresoGenerar AS IEG
						INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" IE ON (IE."IdIngresoEgreso" = IEG."IdIngresoEgreso")
						INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" IEFP ON (IEFP."IdIngresoEgreso" = IE."IdIngresoEgreso")  -- debe usa INNER para que tome solo movimientos con forma de pago asignada
						WHERE (IEFP."IdEstado" <> 'ANU')
						GROUP BY IEG."IdIngresoEgreso"
				);
		 
				-- Obtiene los medios de pagos asociados a los movimientos
				v_MediosDePagoMovimientos =
						SELECT IEG."IdIngresoEgreso", IEFP."IdMedioDePago"
						FROM :p_IngresoEgresoGenerar AS IEG				 
						INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" IE ON (IE."IdIngresoEgreso" = IEG."IdIngresoEgreso")
						INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" IEFP ON (IEFP."IdIngresoEgreso" = IE."IdIngresoEgreso")  -- debe usa INNER para que tome solo movimientos con forma de pago asignada
						WHERE (IEFP."IdEstado" <> 'ANU')
						GROUP BY IEG."IdIngresoEgreso", IEFP."IdMedioDePago";
		
				-- Obtiene los medios de pago que coincide para todos los movimientos con FP asignado
				v_MediosDePagoParaPartida =
						SELECT "IdMedioDePago"
						FROM :v_MediosDePagoMovimientos
						GROUP BY "IdMedioDePago"
						HAVING COUNT("IdMedioDePago") = :v_CountMovimientosConMedioDePagoAsignado;
		
		
				-- Si no existen medios de pago en común, debe frenar el proceso.
				v_TablaRequisitos =
				SELECT 	2 AS "Prioridad",	
				       	CASE 
				       		WHEN (COUNT("IdMedioDePago") = 0 AND v_CountMovimientosConMedioDePagoAsignado<>0) THEN 0 
				       		ELSE 1 
						END AS "Cumple", 
				       	'MEDIO_DE_PAGO_MOVIMIENTOS_NO_COINCIDE' AS "CodigoError",
						'IdIngresoEgreso' AS "CodigoDato",				
				       	'' AS "ValorDato"		       	
				       	,0 AS "IdIngresoEgreso"	
				FROM :v_MediosDePagoParaPartida				
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdIngresoEgreso" FROM :v_TablaRequisitos;

				
				
				---------------------------------------------------------------
				-- PREPARA DATOS PARA SALIDAS DEL SERVICIO
				
				-- Obtiene Moneda de los movimientos a generar la PI(estos datos deben ser comunes para todos los mov.) 
				SELECT Top 1 MOV."IdMoneda", MAX(MON."Nombre"), MOV."FechaLiquidacion", SUM(MOV."Monto")
				INTO p_out_IdMoneda, p_out_NombreMoneda, p_out_FechaLiquidacion, p_out_Monto
				FROM :p_IngresoEgresoGenerar AS IEG
				INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV ON MOV."IdIngresoEgreso" = IEG."IdIngresoEgreso"				
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON MON."IdMoneda" = MOV."IdMoneda"
				GROUP BY MOV."IdMoneda", MOV."FechaLiquidacion";



				-- Obtiene el concepto de los movimientos(si no es común lo deja como multiconcepto) (para salida del servicio)
				SELECT 
						CASE 
							 WHEN (Count(Distinct "CON"."IdConcepto") > 1) THEN 'MULCPT'
							 ELSE MAX("CON"."IdConcepto")
						END AS "IdConcepto",						
						CASE 
							 WHEN (Count(Distinct "CON"."IdConcepto") > 1) THEN 'MULTIPLE CONCEPTOS'
							 ELSE MAX("CON"."Nombre")
						END AS "NombreConcepto"
						
				INTO p_out_IdConcepto, p_out_NombreConcepto
				FROM :p_IngresoEgresoGenerar AS IEG
				INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV ON MOV."IdIngresoEgreso" = IEG."IdIngresoEgreso"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" AS CON ON (CON."IdConcepto" = MOV."IdConcepto");

				
				-- Obtiene el IdCaja de los movimientos que tienen caja asignada (para salida del servicio)				
				v_IdCaja =
				SELECT Top 1 MOV."IdCaja"
				FROM :p_IngresoEgresoGenerar AS IEG
				INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV ON MOV."IdIngresoEgreso" = IEG."IdIngresoEgreso"				
				WHERE MOV."IdCaja" <> 0  -- Considerar solo movimientos con caja asignada
				GROUP BY MOV."IdCaja";	
					
				SELECT count(*) INTO v_CountIdCaja 	FROM :v_IdCaja;

				IF (v_CountIdCaja > 0)  THEN
					SELECT Top 1 MOV."IdCaja"
					INTO p_out_IdCaja
					FROM :p_IngresoEgresoGenerar AS IEG
					INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV ON MOV."IdIngresoEgreso" = IEG."IdIngresoEgreso"				
					WHERE MOV."IdCaja" <> 0  -- Considerar solo movimientos con caja asignada
					GROUP BY MOV."IdCaja";				
				ELSE
					p_out_IdCaja := 0;
				END IF;

		END IF;   -- IF (v_CountTablaRequisitos = 0)		


 

		---------------------------------------------------------------
		-- ANALISIS DE DATOS
		---------------------------------------------------------------

		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" > 1 THEN 1 END)
	   	INTO v_error_General, v_error_Negocio
	   	FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0 THEN 'RECH'
									 WHEN :v_error_Negocio > 0 THEN 'RECH' -- 'ADVE'
	   								 ELSE 'OK'
	   							END;
   
	    
		-- Asigna códigos de mensajes de errores
	    v_TablaCodigosMensajes =
				SELECT 	"CodigoError" AS "CodigoMensaje"
					   ,"CodigoDato"
					   ,"ValorDato" 
				FROM :v_TablaRequisitos
				WHERE "Cumple" = 0
				ORDER By "Prioridad" DESC;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,p_out_ErroresDetalle);
		 

		p_out_ErroresDetalle =
				SELECT ED."Codigo",
					   CASE WHEN (TR."IdIngresoEgreso" <> 0) THEN (ED."Descripcion" || ' (' || TR."IdIngresoEgreso" || ')')
							ELSE ED."Descripcion"
					   END as "Descripcion"
				FROM :p_out_ErroresDetalle as ED
				INNER JOIN :v_TablaRequisitos as TR ON TR."CodigoError" = ED."Codigo"
				WHERE "Cumple" = 0;
		
		
		SELECT count(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;		
				
    END;
	
END;
