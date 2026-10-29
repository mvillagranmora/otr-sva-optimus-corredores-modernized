SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGirarComprobantesEgreso";
go
CREATE PROCEDURE
-- 
--  Proposito: Validar comprabantes de egreso para girar.
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 09/03/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGirarComprobantesEgreso"(
	-- Parametros entrada
    IN p_ComprobantesAGirar	           TABLE("IdComprobante" BIGINT),
    IN p_IdCuentaBancariaCorredora 	   BIGINT,
    IN p_simularOperacion 			   NVARCHAR(1),

   -- Salidas comunes para todos los comprobantes
	OUT p_out_MediosDePagoAGirar       TABLE("IdMedioDePago" NVARCHAR(3), "MontoPorMedioDePago" DECIMAL(26,8)),
	OUT p_out_IdConcepto               NVARCHAR(6),   -- IdConcepto a Girar (común en todos los comprobantes)
	OUT p_out_NombreConcepto	       NVARCHAR(100),
    OUT p_out_IdMoneda 				   NVARCHAR(3),   -- IdMoneda a Girar (común en todos los comprobantes)
    OUT p_out_NombreMoneda 			   NVARCHAR(100),
    OUT p_out_FechaLiquidacion         NVARCHAR(10),   -- Fecha de Liquidación (común para todos)
    OUT p_out_IdCaja                   BIGINT,         -- IdCaja de mos mov que tienen caja asignada  
    OUT p_out_NombreCaja   			   NVARCHAR(100),
	OUT p_out_ComprobantesRech	       TABLE("IdComprobante" BIGINT),
	
	-- Salidas para mensajes de errores
	OUT p_out_ErroresDetalle           TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(520)),
	OUT p_out_resultadoRegla           NVARCHAR(40) --OK   = Cumple todos los requisitos,		
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
	DECLARE v_erroroutmsg  			  NVARCHAR(520);
	DECLARE v_ErrorNoData  			  NVARCHAR(100); 
	DECLARE v_resultadoRegla       	  NVARCHAR(40);
	DECLARE v_ErroresContador 		  INT; 
	DECLARE v_CountComprobantesConMedioDePagoAsignado  INT; 
	DECLARE v_CountIdCaja             INT;	
    DECLARE v_CountTablaRequisitos    INT;
    DECLARE v_CountMediosDePagoAGirar INT;

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
	p_out_ComprobantesRech	= SELECT 0 AS "IdComprobante" FROM dummy WHERE 1=2;
	
	BEGIN	
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio			BIGINT;
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));


		DECLARE v_TablaRequisitos     	   TABLE (   "Prioridad" 				INT
													,"Cumple" 					BIGINT
													,"CodigoError" 				NVARCHAR(100)
												    ,"CodigoDato" 				NVARCHAR(100)
												    ,"ValorDato" 				NVARCHAR(520)
												    ,"IdComprobante" 			BIGINT
												  );			
		

		
			
						
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299  -- No se han encontrado datos
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	  = SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;
		v_TablaRequisitos    	  = SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "IdComprobante" FROM dummy WHERE (1=2);		

		---------------------------------------------------------------
		-- VALIDACIONES BASICAS
		---------------------------------------------------------------
		
		-- Validar que vengan comprobantes de tesoreria a girar
		v_TablaRequisitos =
		SELECT 	1 AS "Prioridad",	
		       	CASE 
		       		WHEN COUNT("IdComprobante") < 1 THEN 0 
		       		ELSE 1 
				END AS "Cumple", 
		       	'SIN_ESPECIFICAR_COMPROBANTES_GIRAR' AS "CodigoError",
		       	'' AS "CodigoDato",
		       	'' AS "ValorDato"
		       	,0 AS "IdComprobante"
		FROM :p_ComprobantesAGirar
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdComprobante" FROM :v_TablaRequisitos;




		-- Validar que la cuenta bancaria de la corredora esté bien configurada
		v_TablaRequisitos = 
		SELECT  1 AS "Prioridad",
				CASE 
					WHEN :p_simularOperacion = 'N' AND (COUNT(CTA."IdCuentaBanco") = 0) THEN 0
					WHEN :p_simularOperacion = 'N' AND (COALESCE(PER."IdPersona",0) = 0) THEN 0 
					ELSE 1 END AS "Cumple",
				CASE 
					WHEN :p_simularOperacion = 'N' AND (COUNT(CTA."IdCuentaBanco") = 0) THEN 'CUENTA_CORRIENTE_NO_EXISTE'
					WHEN :p_simularOperacion = 'N' AND (COALESCE(PER."IdPersona",0) = 0) THEN 'BANCO_CUENTA_CORRIENTE_SIN_CONFIGURACION'
					ELSE '' 
				END AS "CodigoError"
				,'' AS "CodigoDato"
		       	,'' AS "ValorDato"
		       	,0 AS "IdComprobante"
		FROM "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CTA
		INNER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BAN ON (BAN."IdBanco" = CTA."IdBanco")
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = BAN."IdPersona")
		WHERE CTA."IdCuentaBanco" = :p_IdCuentaBancariaCorredora
		GROUP BY CTA."IdCuentaBanco", PER."IdPersona"
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdComprobante" FROM :v_TablaRequisitos;


		

		-- Validar los folios de comprobantes sean válidos
		v_TablaRequisitos = 
		SELECT  1 AS "Prioridad",
				CASE WHEN (COALESCE(COM."IdComprobante",0) = 0) or GIR."IdComprobante" = 0 THEN 0 ELSE 1 END AS "Cumple",
				'COMPROBANTE_TESORERIA_INVALIDO' AS "CodigoError",
				'IdComprobante' AS "CodigoDato",
		       	CAST(GIR."IdComprobante" AS NVARCHAR(50)) AS "ValorDato"
		       	,GIR."IdComprobante" AS "IdComprobante"
		FROM :p_ComprobantesAGirar GIR		
		LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON COM."IdComprobante" = GIR."IdComprobante"				
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdComprobante" FROM :v_TablaRequisitos;


		-- Calcula cantidad de errores hasta este punto
		SELECT Count("IdComprobante")
		INTO v_CountTablaRequisitos
		FROM :v_TablaRequisitos
		WHERE "Cumple" = 0;

	
		SELECT '', '','', '', '0001-01-01', 0, ''
		INTO p_out_IdConcepto, p_out_NombreConcepto ,p_out_IdMoneda, p_out_NombreMoneda, p_out_FechaLiquidacion, p_out_IdCaja, p_out_NombreCaja
		FROM DUMMY;
		
		
		-- Si no hay errores hasta acá
		IF (v_CountTablaRequisitos = 0) THEN 
				
				---------------------------------------------------------------
				-- VALIDACIONES DE NEGOCIO
				---------------------------------------------------------------
				-- Valida que el comprobante exista y que no esten bloqueados, liquidados ni girados.
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
						CASE WHEN COALESCE(COM."IdComprobante",0) = 0 THEN 0  -- Si el comprobante no existe
						 	 WHEN (COM."IdEstado" = 'LIQ') THEN 0
							 WHEN (COM."IdEstado" = 'BLO') THEN 0
							 WHEN (COM."IdEstado" = 'GIR') THEN 0
							 ELSE  1  -- si comprobante está ingresado(ING) ó GIP(girado parcialmente)
						END AS "Cumple",
						
						CASE WHEN COALESCE(COM."IdComprobante",0) = 0 THEN 'COMPROBANTE_TESORERIA_NO_EXISTE'
						 	 WHEN (COM."IdEstado" = 'LIQ') THEN 'COMPROBANTE_TESORERIA_LIQUIDADO'
							 WHEN (COM."IdEstado" = 'BLO') THEN 'COMPROBANTE_TESORERIA_BLOQUEADO'
							 WHEN (COM."IdEstado" = 'GIR') THEN 'COMPROBANTE_TESORERIA_GIRADO'
							 ELSE  ''
						END AS "CodigoError",
						
						'IdComprobante' AS "CodigoDato",								
				       	CAST(GIR."IdComprobante" as NVARCHAR(50)) AS "ValorDato"		       	
				       	,GIR."IdComprobante"
				FROM :p_ComprobantesAGirar as GIR
				LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdComprobante" FROM :v_TablaRequisitos;
				
				
		
				-- Valida que los comprobante correspondan a egresos.
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
						CASE WHEN (COM."IdTipoMovimiento" = 'MOVING') THEN 0  -- comprobante es ingreso
							 ELSE  1  -- comprobante es egreso(MOVEGR)
						END AS "Cumple",
						CASE WHEN (COM."IdTipoMovimiento" = 'MOVING') THEN 'COMPROBANTE_TESORERIA_ES_INGRESO'
							 ELSE  ''
						END AS "CodigoError",				
						'IdComprobante' AS "CodigoDato",								
				       	CAST(COM."IdComprobante" as NVARCHAR(50)) AS "ValorDato"		       	
				       	,COM."IdComprobante"		       	
				FROM :p_ComprobantesAGirar as GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdComprobante" FROM :v_TablaRequisitos;
				
				
				
				-- Valida que todos los comprobantes tengan la misma fecha de liquidación
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
						CASE WHEN (Count(Distinct "FechaLiquidacion") > 1) THEN 0  -- fecha de liquidación distinta entre comprobantes
							 ELSE  1  
						END AS "Cumple",
						'FECHA_LIQUICADION_COMPROBANTES_NO_COINCIDE' AS "CodigoError",
						'IdComprobante' AS "CodigoDato",				
				       	'' AS "ValorDato"		       	
				       	,0 AS "IdComprobante"		       	
				FROM :p_ComprobantesAGirar as GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdComprobante" FROM :v_TablaRequisitos;
		
		
				-- Valida que todos los comprobantes tengan la misma moneda
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
						CASE WHEN (Count(Distinct "IdMoneda") > 1) THEN 0  -- moneda distinta entre comprobantes
							 ELSE  1  
						END AS "Cumple",
						'MONEDA_COMPROBANTES_NO_COINCIDE' AS "CodigoError",
						'IdComprobante' AS "CodigoDato",				
				       	'' AS "ValorDato"		       	
				       	,0 AS "IdComprobante"		       	
				FROM :p_ComprobantesAGirar as GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdComprobante" FROM :v_TablaRequisitos;

		 
				-- Valida que todos los comprobantes tengan forma de pago asignada
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
						CASE WHEN COALESCE(CFP."IdComprobanteFormaDePago",0) = 0 THEN 0
							 ELSE  1  
						END AS "Cumple",
						'COMPROBANTES_SIN_FORMA_DE_PAGO_ASIGNADA' AS "CodigoError",	
						'IdComprobante' AS "CodigoDato",
				       	CAST(COM."IdComprobante" as NVARCHAR(50)) AS "ValorDato"		       	
				       	,COM."IdComprobante"				       	
				FROM :p_ComprobantesAGirar as GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"
				LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP ON (CFP."IdComprobante" = COM."IdComprobante" AND (CFP."IdEstado" <> 'ANU' AND CFP."IdEstado" <> 'LIQ' AND CFP."IdEstado" <> 'GIR'))
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdComprobante" FROM :v_TablaRequisitos;
				
				-- Valida que no se haga una partida de egreso al tipo de comprobante PAREGR
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
						CASE WHEN (COM."TipoComprobante" = 'PAREGR') THEN 0  -- comprobante es partida egreso
							 ELSE  1  
						END AS "Cumple",
						CASE WHEN (COM."TipoComprobante" = 'PAREGR') THEN 'COMPROBANTE_TESORERIA_ES_EGRESO'
							 ELSE  ''
						END AS "CodigoError",				
						'IdComprobante' AS "CodigoDato",								
				       	CAST(COM."IdComprobante" as NVARCHAR(50)) AS "ValorDato"		       	
				       	,COM."IdComprobante"		       	
				FROM :p_ComprobantesAGirar as GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdComprobante" FROM :v_TablaRequisitos;
				
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
						CASE WHEN (Count(Distinct "MueveCuentaInversion") > 1) THEN 0
							 ELSE  1  
						END AS "Cumple",
						'MUEVE_CUENTA_INVERSION_NO_COINCIDE' AS "CodigoError",
						'IdComprobante' AS "CodigoDato",				
				       	'' AS "ValorDato"		       	
				       	,0 AS "IdComprobante"		       	
				FROM :p_ComprobantesAGirar as GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdComprobante" FROM :v_TablaRequisitos;

				-----------------------------------------------------------------------------
				-- Valida que todos los comprobates tengan un medio de pago en comun.
		        -- No considera los camprobantes sin medio de pago asignado..
				-----------------------------------------------------------------------------

				-- Obtiene cantidad de comprobantes con medio de pago asignado
				SELECT COUNT("IdComprobante")
				INTO v_CountComprobantesConMedioDePagoAsignado
				FROM 
				(
						SELECT COM."IdComprobante"
						FROM :p_ComprobantesAGirar AS GIR
						INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" COM ON (COM."IdComprobante" = GIR."IdComprobante")
						INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP ON (CFP."IdComprobante" = COM."IdComprobante")  -- usa INNER para que tome solo comprobantes con forma de pago asignada
						WHERE (CFP."IdEstado" <> 'MOD' AND CFP."IdEstado" <> 'ANU' AND CFP."IdEstado" <> 'GIR' AND CFP."IdEstado" <> 'LIQ')
						GROUP BY COM."IdComprobante"		
				);
				
				-- select 'v_CountComprobantesConMedioDePagoAsignado', v_CountComprobantesConMedioDePagoAsignado from dummy; -- borrar
		 
				-- Obtiene los medios de pagos asociados a los comprobantes
				v_MediosDePagoComprobantes = 
						SELECT
							  COM."IdComprobante"
							 ,CFP."IdMedioDePago"
							 ,SUM(CFP."Monto") AS "MontoPorMedioDePago"
						FROM :p_ComprobantesAGirar AS GIR					 
						INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" COM ON (COM."IdComprobante" = GIR."IdComprobante")
						INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP ON (CFP."IdComprobante" = COM."IdComprobante")  -- toma sólo comprobantes con forma de pago asignada
						WHERE (CFP."IdEstado" <> 'MOD' AND CFP."IdEstado" <> 'ANU' AND CFP."IdEstado" <> 'GIR' AND CFP."IdEstado" <> 'LIQ')
						GROUP BY COM."IdComprobante", CFP."IdMedioDePago";
		
		
				-- select 'v_MediosDePagoComprobantes',*  from :v_MediosDePagoComprobantes; -- borrar
		
				-- Obtiene los medios de pago a girar(aquel que coincide para todos los comprobantes con MP asignado)
				p_out_MediosDePagoAGirar =
						SELECT "IdMedioDePago", SUM("MontoPorMedioDePago") AS "MontoPorMedioDePago"
						FROM :v_MediosDePagoComprobantes
						GROUP BY "IdMedioDePago"
						HAVING COUNT("IdMedioDePago") = :v_CountComprobantesConMedioDePagoAsignado;
						
		
				select 'p_out_MediosDePagoAGirar',*  from :p_out_MediosDePagoAGirar; -- borrar

				SELECT Count("IdMedioDePago")
				INTO v_CountMediosDePagoAGirar
				FROM :p_out_MediosDePagoAGirar;
				
				--select 'v_CountMediosDePagoAGirar',:v_CountMediosDePagoAGirar  from dummy; -- borrar
		
				-- Si no existen medios de pago en común(para comprobantes que tienen MP asignado), debe frenar el proceso.
				v_TablaRequisitos =
				SELECT 	2 AS "Prioridad",	
				       	CASE 
				       		WHEN (COUNT("IdMedioDePago") < 1 and v_CountComprobantesConMedioDePagoAsignado<>0) THEN 0 
				       		ELSE 1 
						END AS "Cumple", 
				       	'MEDIO_DE_PAGO_COMPROBANTES_NO_COINCIDE' AS "CodigoError",
				       	'' AS "CodigoDato",
				       	'' AS "ValorDato"
				       	,0 AS "IdComprobante"
				FROM :p_out_MediosDePagoAGirar
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdComprobante" FROM :v_TablaRequisitos;


				-- Valida que los comprobantes que tiene caja asignada, tengan la misma caja.
				v_TablaRequisitos = 
				SELECT  2 AS "Prioridad",
						CASE WHEN (Count(Distinct COM."IdCaja") > 1) THEN 0  -- caja distinta entre comprobantes
							 ELSE  1  
						END AS "Cumple",
						'CAJA_COMPROBANTES_NO_COINCIDE' AS "CodigoError",						
						'IdComprobante' AS "CodigoDato",				
				       	'' AS "ValorDato"		       	
				       	,0 AS "IdComprobante"
				FROM :p_ComprobantesAGirar as GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"
				AND COM."IdCaja" <> 0  -- Considerar solo comprobantes con caja asignada
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdComprobante" FROM :v_TablaRequisitos;


		
				-- Obtiene la moneda de los comprobantes a girar(es común para todos los comprobantes).
				SELECT Top 1 COM."IdMoneda", MAX(MON."Nombre"), COM."FechaLiquidacion"
				INTO p_out_IdMoneda, p_out_NombreMoneda, p_out_FechaLiquidacion
				FROM :p_ComprobantesAGirar AS GIR
				INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" AS COM ON (COM."IdComprobante" = GIR."IdComprobante")
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON MON."IdMoneda" = COM."IdMoneda"
				GROUP BY COM."IdMoneda", COM."FechaLiquidacion";
		
		

				-- Obtiene el concepto de los comprobantes a girar(si no es común lo deja como multiconcepto)
				SELECT 
						CASE 
							 WHEN (Count(Distinct "COM"."IdConcepto") > 1) THEN 'MULCPT'
							 ELSE MAX("COM"."IdConcepto")
						END AS "IdConcepto",						
						CASE 
							 WHEN (Count(Distinct "COM"."IdConcepto") > 1) THEN 'MULTIPLE CONCEPTOS'
							 ELSE MAX("CON"."Nombre")
						END AS "NombreConcepto"
						
				INTO p_out_IdConcepto, p_out_NombreConcepto
				FROM :p_ComprobantesAGirar AS GIR				
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM  ON (COM."IdComprobante" = GIR."IdComprobante")
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" AS CON ON (CON."IdConcepto" = COM."IdConcepto");

		
				-- Obtiene el IdCaja de los movimientos que tienen caja asignada				
				v_IdCaja =
				SELECT Top 1 COM."IdCaja"
				FROM :p_ComprobantesAGirar AS GIR				
				INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" AS COM ON COM."IdComprobante" = GIR."IdComprobante"								
				WHERE COM."IdCaja" <> 0  -- Considerar comprobantes con caja asignada
				GROUP BY COM."IdCaja";	
					
				SELECT count(*) INTO v_CountIdCaja 	FROM :v_IdCaja;
		
				IF (v_CountIdCaja > 0)  THEN				
					SELECT Top 1 COM."IdCaja", MAX(COM."NombreCaja")
					INTO p_out_IdCaja, p_out_NombreCaja
					FROM :p_ComprobantesAGirar AS GIR				
					INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" AS COM ON COM."IdComprobante" = GIR."IdComprobante"													
					WHERE COM."IdCaja" <> 0  -- Considerar comprobantes con caja asignada
					GROUP BY COM."IdCaja";
				ELSE
					p_out_IdCaja     := 0;
					p_out_NombreCaja := '';
				END IF;

		END IF; -- IF (v_CountTablaRequisitos = 0) THEN 

		---------------------------------------------------------------
		-- ANALISIS DE DATOS
		---------------------------------------------------------------

		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" > 1 THEN 1 END)
	   	INTO v_error_General, v_error_Negocio
	   	FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0 THEN 'RECH'
									 WHEN :v_error_Negocio > 0 THEN 'RECH' -- Todos los comprobantes deben ser válidos
	   								 ELSE 'OK'
	   							END;
	    
	    -- Asigna IdComprobantes Rechazados
	    p_out_ComprobantesRech =
			    SELECT DISTINCT "IdComprobante"
			    FROM :v_TablaRequisitos
			    WHERE "Prioridad" > 1
			    AND "Cumple" = 0;
	    
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
					   CASE WHEN ("Prioridad" > 1 AND TR."IdComprobante" <> 0) THEN (ED."Descripcion" || ' (' || TR."IdComprobante" || ')')
							ELSE ED."Descripcion"
					   END as "Descripcion"
				FROM :p_out_ErroresDetalle as ED
				INNER JOIN :v_TablaRequisitos as TR ON TR."CodigoError" = ED."Codigo"
				WHERE "Cumple" = 0;
				
    END;
	
END;
