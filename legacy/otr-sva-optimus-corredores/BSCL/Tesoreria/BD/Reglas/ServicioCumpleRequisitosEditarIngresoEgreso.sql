SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEditarIngresoEgreso";
GO 
CREATE PROCEDURE
-- 
--  Proposito: Validar los requisitos para la creacion y modificacion de movimientos de Ingreso - Egreso
--  Autor: NRM - BCS
--  Fecha Creacion: 23/11/2015
--  
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEditarIngresoEgreso"(
	-- Parametros entrada 
	IN p_IdEmpresa              BIGINT,
	IN p_IdModulo				NVARCHAR(3),
	IN p_Accion				    NVARCHAR(25),  -- CREAR | MODIFICAR

	OUT p_out_resultadoRegla    NVARCHAR(40)   	-- OK   = Cumple todos los requisitos,		
												-- ADVE = 	 								  	   
                                                -- RECH = Alguna de lAS reglAS detienen la operacion

	-- TABLAS DE ENTRADA
	-- "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar"
	-- "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_FormaDePago"
	
	-- TABLAS DE SALIDA
	-- "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_MovimientosRechazados"
	-- "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_ErroresDetalle"
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
	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA     
	BEGIN
		p_out_resultadoRegla	:= 'RECH';	
		v_hASh = SELECT 'FolioCartera' AS "Codigo", 0 AS "Valor" FROM DUMMY;		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hASh,:v_erroroutmsg);
								
		INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_ErroresDetalle"
		SELECT 0 AS "NumeroReferencia", :v_ErrorNoData  AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
	END; 
	
	p_out_resultadoRegla	:= 'OK';

	
	BEGIN
		DECLARE v_Obligatorios			NVARCHAR(250);
		DECLARE v_Indicadores			NVARCHAR(250);
		DECLARE v_countTableFormaPago	BIGINT;
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio			BIGINT;
   		DECLARE v_FechaSistema          NVARCHAR(10); 
		DECLARE v_TablaCodigosMensajes	TABLE  ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),
												"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
		DECLARE v_TablaRequisitos		TABLE  ("Prioridad" INT,"Cumple" BIGINT, "Codigo" NVARCHAR(100), 
												"CodigoDato1" NVARCHAR(100), "ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100), 
												"ValorDato2" NVARCHAR(520),"NumeroReferencia" BIGINT);			
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT  0 AS "IdInterno",'' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2" 
									FROM dummy WHERE 1=2;
		v_TablaRequisitos 		= SELECT  0 AS "Prioridad",0 AS "Cumple",'' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",
								   		 '' AS "CodigoDato2",'' AS "ValorDato2",0 as "NumeroReferencia" 
							        FROM dummy WHERE (1=2);
		
		
		
		-- Obtiene Fecha de Sistema
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_FechaSistema);
			
		----------------------------------------------------------------
		-- VALIDACIONES BASICAS
		----------------------------------------------------------------
		
		v_TablaRequisitos =
		SELECT 	 1 AS "Prioridad"
		       	,CASE 
		       		WHEN COUNT("NumeroReferencia") < 1 THEN 0 
		       		ELSE 1 
				 END AS "Cumple"
		       	,'NO_LLEGAN_MOVIMIENTOS_TESORERIA' AS "Codigo"
		       	,'' as "CodigoDato1"
		       	,'' as "ValorDato1"
		       	,'' as "CodigoDato2"
		       	,'' as "ValorDato2" 
		       	,0 as "NumeroReferencia"
		 FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar"
		 UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","NumeroReferencia" FROM :v_TablaRequisitos;
	
	
		v_TablaRequisitos =
		SELECT   1 as "Prioridad"
		       	,CASE 
		       		WHEN LENGTH("IdTipoMovimiento") = 0 THEN 0  
		       		WHEN LENGTH("IdConcepto") = 0 THEN 0  
		       		WHEN LENGTH("IdMoneda") = 0 THEN 0  
		       		WHEN COALESCE("Monto",0) = 0 THEN 0  
		       		WHEN COALESCE("FechaLiquidacion",'0001-01-01') = '0001-01-01' THEN 0
		       		---WHEN (DAYS_BETWEEN (TO_DATE(:v_FechaSistema, 'YYYY-MM-DD'), TO_DATE("FechaLiquidacion", 'YYYY-MM-DD')) < 0) THEN 0 -- Debe permitir editar movimientos vencidos
		       		WHEN COALESCE("IdPersona",0) = 0 THEN 0
		       		WHEN ("IdTipoPersonaTitularPago" = 'CLI' AND COALESCE("IdPortafolioTitularPago",0) = 0)	THEN 0		       		
		       		WHEN LENGTH("Bilateral") = 0 THEN 0  
		       		WHEN LENGTH("GeneraComprobante") = 0 THEN 0  
		       		WHEN LENGTH("AgruparEnComprobante") = 0 THEN 0  
		       		WHEN LENGTH("TitularPago") = 0 THEN 0 
		       		ELSE 1 
				 END as "Cumple"
				,CASE 
					WHEN LENGTH("IdTipoMovimiento") = 0 THEN 'SIN_ESPECIFICAR_TIPO_MOVIMIENTO_TESORERIA'
		       		WHEN LENGTH("IdConcepto") = 0 THEN 'SIN_ESPECIFICAR_CONCEPTO_TESORERIA'
		       		WHEN LENGTH("IdMoneda") = 0 THEN 'SIN_ESPECIFICAR_MONEDA_TESORERIA'
		       		WHEN COALESCE("Monto",0) = 0 THEN 'SIN_ESPECIFICAR_MONTO_TESORERIA'
		       		WHEN COALESCE("FechaLiquidacion",'0001-01-01') = '0001-01-01' THEN 'SIN_ESPECIFICAR_FECHA_LIQUIDACION_TESORERIA'
		       		---WHEN (DAYS_BETWEEN (TO_DATE(:v_FechaSistema, 'YYYY-MM-DD'), TO_DATE("FechaLiquidacion", 'YYYY-MM-DD')) < 0) THEN 'FECHA_LIQUIDACION_MENOR_A_FECHA_SISTEMA'   -- Debe permitir editar movimientos vencidos
		       		WHEN COALESCE("IdPersona",0) = 0 THEN 'SIN_ESPECIFICAR_PERSONA_TESORERIA'
		       		WHEN ("IdTipoPersonaTitularPago" = 'CLI' AND COALESCE("IdPortafolioTitularPago",0) = 0) THEN 'SIN_ESPECIFICAR_PORTAFOLIO_PERSONA_TITULAR_PAGO_TESORERIA'
		       		WHEN LENGTH("Bilateral") = 0 THEN 'SIN_ESPECIFICAR_INDICADOR_BILATERAL_TESORERIA'  
		       		WHEN LENGTH("GeneraComprobante") = 0 THEN 'SIN_ESPECIFICAR_INDICADOR_GENERA_COMPROBANTE_TESORERIA'  
		       		WHEN LENGTH("AgruparEnComprobante") = 0 THEN 'SIN_ESPECIFICAR_INDICADOR_AGRUPA_EN_COMPROBANTE_TESORERIA' 
		       		WHEN LENGTH("TitularPago") = 0 THEN 'SIN_ESPECIFICAR_INDICADOR_TITULAR_PAGO_TESORERIA'
		       		ELSE ''
				 END as "Codigo"
				,'NumeroReferencia' as "CodigoDato1"
				,CAST("NumeroReferencia" as NVARCHAR(520)) as "ValorDato1"
				,'' as "CodigoDato2"
				,'' as "ValorDato2"
				,"NumeroReferencia"
		 FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar"
		 UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","NumeroReferencia" FROM :v_TablaRequisitos;
	

		
		----------------------------------------------------------------
		-- VALIDACIONES DE NEGOCIO
		----------------------------------------------------------------

		IF (p_Accion = 'MODIFICAR') THEN
			-- Validación que movimiento a modificar sean manuales y no estén emitidos
			v_TablaRequisitos =
			SELECT   2 as "Prioridad"
					,CASE 
						WHEN (IE."IdEstado" = 'EMI') THEN 0
						--WHEN (IE."EsIngresoAutomatico" = 'S') THEN 0						
						ELSE  1
					 END AS "Cumple"
					 
					,CASE 
						WHEN (IE."IdEstado" = 'EMI') THEN 'MOVIMIENTO_TESORERIA_EMITIDO'
						--WHEN (IE."EsIngresoAutomatico" = 'S') THEN 'MOVIMIENTO_TESORERIA_INGRESO_AUTOMATICO'
						ELSE  ''
					 END AS "Codigo"					 
					,'IdIngresoEgreso' as "CodigoDato1"					
			       	,CAST(IE."IdIngresoEgreso" AS NVARCHAR(10)) as "ValorDato1" 
					,'' as "CodigoDato2"
			       	,'' as "ValorDato2" 			       	
			       	,IEG."NumeroReferencia"			       	
			FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar" AS IEG
			LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" as IE ON IE."IdIngresoEgreso" = IEG."NumeroReferencia"
			WHERE (IE."IdEstado" = 'EMI' OR IE."EsIngresoAutomatico" = 'S')
			UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","NumeroReferencia" FROM :v_TablaRequisitos;		 		
		END IF;


		v_Persona = 
		SELECT PER."IdPersona",PER."NombreCompleto",PER."IdEstado"
		FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar" AS IEG
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as PER ON PER."IdPersona" IN (IEG."IdPersona",IEG."IdPersonaTitularPago");


		v_PersonaTarjetasBancarias = 
		SELECT TBC."IdTarjetaBancaria",TBC."IdEstado"
		FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_FormaDePago"  AS FP 
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" AS TBC ON TBC."IdTarjetaBancaria" IN (FP."IdTarjetaBancariaCorredora",FP."IdTarjetaBancariaSocio");
		  

		--Validar datos obligatorios
		v_TablaRequisitos =
		SELECT 	 2 as "Prioridad"
				,CASE WHEN COALESCE(TM."IdTipoMovimiento",'') = '' THEN 0
				 	  WHEN COALESCE(CT."IdConcepto",'') = '' THEN 0
				 	  WHEN COALESCE(Mon."IdMoneda",'') = '' THEN 0
				 	  WHEN Mon."IdEstado" = 'INA' THEN 0
				 	  WHEN (IEG."IdCaja" > 0 AND COALESCE(Caj."IdCaja",0) = 0) THEN 0				 	  
				 	  WHEN COALESCE(Per."IdPersona",0) = 0 THEN 0
				 	  WHEN Per."IdEstado" = 'INA' THEN 0
				 	  WHEN COALESCE(PerRel."IdPersona",0) = 0 THEN 0
				 	  WHEN PerRel."IdEstado" = 'INA' THEN 0
				 	  WHEN IEG."Monto" = 0 THEN 0				 	  
					  ELSE  1
				 END AS "Cumple"
				,CASE WHEN COALESCE(TM."IdTipoMovimiento",'') = '' THEN 'SIN_DEFINIR_TIPO_MOVIMIENTO_TESORERIA'
				 	  WHEN COALESCE(CT."IdConcepto",'') = '' THEN 'SIN_DEFINIR_CONCEPTO_TESORERIA'				 	  
				 	  WHEN COALESCE(Mon."IdMoneda",'') = '' THEN 'MONEDA_NO_EXISTE'
				 	  WHEN Mon."IdEstado" = 'INA' THEN 'MONEDA_NO_ACTIVA'
				 	  WHEN (IEG."IdCaja" > 0 AND COALESCE(Caj."IdCaja",0) = 0) THEN 'CAJA_NO_EXISTE'
				 	  WHEN COALESCE(Per."IdPersona",0) = 0 THEN 'PERSONA_NO_EXISTE'
				 	  WHEN Per."IdEstado" = 'INA' THEN 'PERSONA_NO_ACTIVA'
				 	  WHEN COALESCE(PerRel."IdPersona",0) = 0 THEN 'PERSONA_RELACIONADA_NO_EXISTE'
				 	  WHEN PerRel."IdEstado" = 'INA' THEN 'PERSONA_NO_ACTIVA'				 	  
				 	  WHEN IEG."Monto" = 0 THEN 'MONTO_MENOR_IGUAL_A_CERO'				 	  
					  ELSE  ''
				 END AS "Codigo"
				,CASE WHEN COALESCE(TM."IdTipoMovimiento",'') = '' THEN 'IdTipoMovimiento'
				 	  WHEN COALESCE(CT."IdConcepto",'') = '' THEN 'IdConcepto'				 	  
				 	  WHEN COALESCE(Mon."IdMoneda",'') = '' THEN 'IdMoneda'
				 	  WHEN Mon."IdEstado" = 'INA' THEN 'IdMoneda'
				 	  WHEN (IEG."IdCaja" > 0 AND COALESCE(Caj."IdCaja",0) = 0) THEN 'IdCaja'
				 	  WHEN COALESCE(Per."IdPersona",0) = 0 THEN 'IdPersona'
				 	  WHEN Per."IdEstado" = 'INA' THEN 'NombrePersona'
				 	  WHEN COALESCE(PerRel."IdPersona",0) = 0 THEN 'IdPersona'
				 	  WHEN PerRel."IdEstado" = 'INA' THEN 'NombrePersona'				 	  
				 	  WHEN IEG."Monto" = 0 THEN ''				 	  
					  ELSE  ''
				 END AS "CodigoDato1"
		       	,CASE WHEN COALESCE(TM."IdTipoMovimiento",'') = '' THEN IEG."IdTipoMovimiento"
				 	  WHEN COALESCE(CT."IdConcepto",'') = '' THEN IEG."IdConcepto"				 	  
				 	  WHEN COALESCE(Mon."IdMoneda",'') = '' THEN IEG."IdMoneda"
				 	  WHEN Mon."IdEstado" = 'INA' THEN Mon."IdMoneda"
				 	  WHEN (IEG."IdCaja" > 0 AND COALESCE(Caj."IdCaja",0) = 0) THEN CAST(IEG."IdCaja" as NVARCHAR(520))
				 	  WHEN COALESCE(Per."IdPersona",0) = 0 THEN CAST(IEG."IdPersona" as NVARCHAR(520))
				 	  WHEN Per."IdEstado" = 'INA' THEN Per."NombreCompleto"
				 	  WHEN COALESCE(PerRel."IdPersona",0) = 0 THEN CAST(IEG."IdPersonaTitularPago" as NVARCHAR(520))
				 	  WHEN PerRel."IdEstado" = 'INA' THEN PerRel."NombreCompleto"				 	  
				 	  WHEN IEG."Monto" = 0 THEN ''				 	  
					  ELSE  ''
				 END AS "ValorDato1" 
				,'' as "CodigoDato2"
		       	,'' as "ValorDato2" 
		       	,IEG."NumeroReferencia"
		  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar"  AS IEG
		  LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" as TM ON TM."IdTipoMovimiento" = IEG."IdTipoMovimiento"
		  LEFT JOIN "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" as CT ON CT."IdConcepto" = IEG."IdConcepto"
		  LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon ON Mon."IdMoneda" = IEG."IdMoneda"
		  LEFT JOIN "BSCL.Tesoreria.BD::TES.Caja" as Caj ON Caj."IdCaja" = IEG."IdCaja"
		  LEFT JOIN :v_Persona as Per ON Per."IdPersona" = IEG."IdPersona"
		  LEFT JOIN :v_Persona as PerRel ON PerRel."IdPersona" = IEG."IdPersonaTitularPago"
		 UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","NumeroReferencia" FROM :v_TablaRequisitos;	
		
		
		--Validación de Secuencia Agrupación(parámetro opcional)
		v_TablaRequisitos =
		SELECT   2 as "Prioridad"
				,CASE WHEN COUNT(IE."SecuenciaAgrupacion") = 0 THEN 0
					  ELSE  1
				 END AS "Cumple"
				,CASE WHEN COUNT(IE."SecuenciaAgrupacion") = 0 THEN 'SECUENCIA_AGRUPACION_UTILIZADA_TESORERIA'
					  ELSE  ''
				 END AS "Codigo"
				,'IdSecuencia' as "CodigoDato1"
		       	,MAX(IE."SecuenciaAgrupacion") as "ValorDato1" 
				,'' as "CodigoDato2"
		       	,'' as "ValorDato2" 
		       	,IEG."NumeroReferencia"
		  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar" AS IEG
		  LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" as IE ON IE."SecuenciaAgrupacion" = IEG."SecuenciaAgrupacion"
		 WHERE IE."IdPersona" = IEG."IdPersona"
		   AND IE."IdEstado" = 'EMI'
		 GROUP BY IEG."NumeroReferencia"
		 UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","NumeroReferencia" FROM :v_TablaRequisitos;
		
		
		--validar los parametros referenciales
		v_TablaRequisitos =
		SELECT   2 as "Prioridad"
				,CASE WHEN (LENGTH(IEG."IdTipoProducto") > 0 AND COALESCE(TPr."IdTipoProducto",'') = '') THEN 0
					  WHEN (LENGTH(IEG."IdTipoOperacion") > 0 AND COALESCE(TOpe."IdTipoOperacion",'') = '') THEN 0
					  WHEN (LENGTH(IEG."IdCondicionLiquidacion") > 0 AND COALESCE(Cond."IdCondicionDeLiquidacion",'') = '') THEN 0
					  ELSE  1
				 END AS "Cumple"
				,CASE WHEN (LENGTH(IEG."IdTipoProducto") > 0 AND COALESCE(TPr."IdTipoProducto",'') = '') THEN 'TIPO_PRODUCTO_NO_EXISTE'
					  WHEN (LENGTH(IEG."IdTipoOperacion") > 0 AND COALESCE(TOpe."IdTipoOperacion",'') = '') THEN 'TIPO_OPERACION_NO_EXISTE'
					  WHEN (LENGTH(IEG."IdCondicionLiquidacion") > 0 AND COALESCE(Cond."IdCondicionDeLiquidacion",'') = '') THEN 'CONDICION_DE_LIQUIDACION_NO_EXISTE'
					  ELSE  ''
				 END AS "Codigo"
				,CASE WHEN (LENGTH(IEG."IdTipoProducto") > 0 AND COALESCE(TPr."IdTipoProducto",'') = '') THEN 'IdTipoProducto'
					  WHEN (LENGTH(IEG."IdTipoOperacion") > 0 AND COALESCE(TOpe."IdTipoOperacion",'') = '') THEN 'IdTipoOperacion'
					  WHEN (LENGTH(IEG."IdCondicionLiquidacion") > 0 AND COALESCE(Cond."IdCondicionDeLiquidacion",'') = '') THEN 'IdCondicionDeLiquidacion'
					  ELSE  ''
				 END AS "CodigoDato1"
		       	,CASE WHEN (LENGTH(IEG."IdTipoProducto") > 0 AND COALESCE(TPr."IdTipoProducto",'') = '') THEN CAST(IEG."IdTipoProducto" as NVARCHAR(520))
					  WHEN (LENGTH(IEG."IdTipoOperacion") > 0 AND COALESCE(TOpe."IdTipoOperacion",'') = '') THEN CAST(IEG."IdTipoOperacion" as NVARCHAR(520))
					  WHEN (LENGTH(IEG."IdCondicionLiquidacion") > 0 AND COALESCE(Cond."IdCondicionDeLiquidacion",'') = '') THEN IEG."IdCondicionLiquidacion"
					  ELSE  ''
				 END AS "ValorDato1" 
				,'' as "CodigoDato2"
		       	,'' as "ValorDato2" 
		       	,IEG."NumeroReferencia"
		 FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar" AS IEG
		  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS TPr ON TPr."IdTipoProducto" = IEG."IdTipoProducto"
		  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TOpe ON TOpe."IdTipoOperacion" = IEG."IdTipoOperacion"
		  LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as Cond ON Cond."IdCondicionDeLiquidacion" = IEG."IdCondicionLiquidacion"
		 UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","NumeroReferencia" FROM :v_TablaRequisitos;
	
		
		--validar Formas de Pago
		v_TablaRequisitos =
		SELECT   2 as "Prioridad"
				,CASE WHEN COALESCE(MeP."IdMedioDePago",'') = '' THEN 0
			 		  --WHEN COALESCE(MoP."IdModalidadDePago",'') = '' THEN 0
				      WHEN (COALESCE(FP."IdMedioDePago",'') <> 'CIN' AND COALESCE(MoP."IdModalidadDePago",'') = '') THEN 0
					  WHEN COALESCE(Mon."IdMoneda",'') = '' THEN 0
				 	  WHEN Mon."IdEstado" = 'INA' THEN 0
				 	  WHEN FP."Monto" = 0 THEN 0
				 	  WHEN FP."IdCuentaBancariaCorredora" > 0 AND COALESCE(CBC."IdCuentaBanco",0) = 0 THEN 0
				 	  WHEN FP."IdTarjetaBancariaCorredora" > 0 AND COALESCE(TBC."IdTarjetaBancaria",0) = 0 THEN 0
				 	  WHEN FP."IdCuentaBancariaSocio" > 0 AND COALESCE(CBS."IdCuentaBancaria",0) = 0 THEN 0
				 	  WHEN FP."IdTarjetaBancariaSocio" > 0 AND COALESCE(TBS."IdTarjetaBancaria",0) = 0 THEN 0
				 	  --WHEN COALESCE(CAN."CountMedioDePago",0) > 1 AND COALESCE(MeP."TipoMedioDePago",'') = 'TRA'  THEN 0
					  ELSE  1
				 END AS "Cumple"
				,CASE WHEN COALESCE(MeP."IdMedioDePago",'') = '' THEN 'MEDIO_DE_PAGO_INVALIDO'
					  --WHEN COALESCE(MoP."IdModalidadDePago",'') = '' THEN 'MODALIDAD_DE_PAGO_INVALIDO'
					  WHEN (COALESCE(FP."IdMedioDePago",'') <> 'CIN' AND COALESCE(MoP."IdModalidadDePago",'') = '') THEN ''
					  WHEN COALESCE(Mon."IdMoneda",'') = '' THEN 'MONEDA_NO_EXISTE'
				 	  WHEN Mon."IdEstado" = 'INA' THEN 'MONEDA_NO_ACTIVA'
				 	  WHEN FP."Monto" = 0 THEN 'MONTO_MENOR_IGUAL_A_CERO'
				 	  WHEN FP."IdCuentaBancariaCorredora" > 0 AND COALESCE(CBC."IdCuentaBanco",0) = 0 THEN 'CUENTA_BANCARIA_CORREDORA_INVALIDA'
				 	  WHEN FP."IdTarjetaBancariaCorredora" > 0 AND COALESCE(TBC."IdTarjetaBancaria",0) = 0 THEN 'TARJETA_BANCARIA_CORREDORA_INVALIDA'
				 	  WHEN FP."IdCuentaBancariaSocio" > 0 AND COALESCE(CBS."IdCuentaBancaria",0) = 0 THEN 'CUENTA_BANCARIA_SOCIO_INVALIDA'
				 	  WHEN FP."IdTarjetaBancariaSocio" > 0 AND COALESCE(TBS."IdTarjetaBancaria",0) = 0 THEN 'TARJETA_BANCARIA_SOCIO_INVALIDA'
				 	  --WHEN COALESCE(CAN."CountMedioDePago",0) > 1 AND COALESCE(MeP."TipoMedioDePago",'') = 'TRA'  THEN 'TRANSFERENCIA_SOLO_ACEPTA_UN_MEDIO_DE_PAGO'
					  ELSE  ''
				 END AS "Codigo"
				,CASE WHEN COALESCE(MeP."IdMedioDePago",'') = '' THEN 'IdMedioDePago'
					  --WHEN COALESCE(MoP."IdModalidadDePago",'') = '' THEN 'MODALIDAD_DE_PAGO_INVALIDO'
					  WHEN (COALESCE(FP."IdMedioDePago",'') <> 'CIN' AND COALESCE(MoP."IdModalidadDePago",'') = '') THEN 'IdModalidadDePago'
					  WHEN COALESCE(Mon."IdMoneda",'') = '' THEN 'IdMoneda'
				 	  WHEN Mon."IdEstado" = 'INA' THEN 'IdMoneda'
				 	  WHEN FP."Monto" = 0 THEN ''
				 	  WHEN FP."IdCuentaBancariaCorredora" > 0 AND COALESCE(CBC."IdCuentaBanco",0) = 0 THEN 'IdCuentaBanco'
				 	  WHEN FP."IdTarjetaBancariaCorredora" > 0 AND COALESCE(TBC."IdTarjetaBancaria",0) = 0 THEN 'IdTarjetaBancaria'
				 	  WHEN FP."IdCuentaBancariaSocio" > 0 AND COALESCE(CBS."IdCuentaBancaria",0) = 0 THEN 'IdCuentaBancaria'
				 	  WHEN FP."IdTarjetaBancariaSocio" > 0 AND COALESCE(TBS."IdTarjetaBancaria",0) = 0 THEN 'IdTarjetaBancaria'
				 	  --WHEN COALESCE(CAN."CountMedioDePago",0) > 1 AND COALESCE(MeP."TipoMedioDePago",'') = 'TRA'  THEN 'IdMedioDePago'
					  ELSE  ''
				 END AS "CodigoDato1"
		       	,CASE WHEN COALESCE(MeP."IdMedioDePago",'') = '' THEN FP."IdMedioDePago"
					  --WHEN COALESCE(MoP."IdModalidadDePago",'') = '' THEN ''
					  WHEN (COALESCE(FP."IdMedioDePago",'') <> 'CIN' AND COALESCE(MoP."IdModalidadDePago",'') = '') THEN FP."IdModalidadDePago"
					  WHEN COALESCE(Mon."IdMoneda",'') = '' THEN FP."IdMoneda"
				 	  WHEN Mon."IdEstado" = 'INA' THEN Mon."IdMoneda"
				 	  WHEN FP."Monto" = 0 THEN ''
				 	  WHEN FP."IdCuentaBancariaCorredora" > 0 AND COALESCE(CBC."IdCuentaBanco",0) = 0 THEN CAST(FP."IdCuentaBancariaCorredora" as NVARCHAR(520))
				 	  WHEN FP."IdTarjetaBancariaCorredora" > 0 AND COALESCE(TBC."IdTarjetaBancaria",0) = 0 THEN CAST(FP."IdTarjetaBancariaCorredora"  as NVARCHAR(520))
				 	  WHEN FP."IdCuentaBancariaSocio" > 0 AND COALESCE(CBS."IdCuentaBancaria",0) = 0 THEN CAST(FP."IdCuentaBancariaSocio" as NVARCHAR(520))
				 	  WHEN FP."IdTarjetaBancariaSocio" > 0 AND COALESCE(TBS."IdTarjetaBancaria",0) = 0 THEN CAST(FP."IdTarjetaBancariaSocio" as NVARCHAR(520))
				 	  --WHEN COALESCE(CAN."CountMedioDePago",0) > 1 AND COALESCE(MeP."TipoMedioDePago",'') = 'TRA'  THEN FP."IdMedioDePago"
					  ELSE  ''
				 END AS "ValorDato1" 
				,'' as "CodigoDato2"
		       	,'' as "ValorDato2" 
		       	,FP."NumeroReferencia"
		  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_FormaDePago" AS FP
		  LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MeP ON MeP."IdMedioDePago" = FP."IdMedioDePago"
		  LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS MoP ON MoP."IdModalidadDePago" = FP."IdModalidadDePago"
		  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon ON Mon."IdMoneda" = FP."IdMoneda"
		  LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CBC ON CBC."IdCuentaBanco" = FP."IdCuentaBancariaCorredora"
		  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS CBS ON CBS."IdCuentaBancaria" = FP."IdCuentaBancariaSocio"
		  LEFT OUTER JOIN :v_PersonaTarjetasBancarias AS TBC ON TBC."IdTarjetaBancaria" = FP."IdTarjetaBancariaCorredora"
		  LEFT OUTER JOIN :v_PersonaTarjetasBancarias AS TBS ON TBS."IdTarjetaBancaria" = FP."IdTarjetaBancariaSocio"
	      LEFT OUTER JOIN (
					        SELECT "NumeroReferencia", COUNT("IdMedioDePago") AS "CountMedioDePago" 
					        FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_FormaDePago"
					        GROUP BY "NumeroReferencia"
		   			      ) AS CAN ON (CAN."NumeroReferencia" = FP."NumeroReferencia")
		 UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","NumeroReferencia" FROM :v_TablaRequisitos;

	

			
			
		-- Valida que la moneda entre el movimiento y sus formas de pago coincida
		v_TablaRequisitos =
		SELECT   2 as "Prioridad"
				,CASE WHEN IEG."IdMoneda" <> MAX(FP."IdMoneda") THEN 0
					  ELSE  1
				 END AS "Cumple"
				,CASE WHEN IEG."IdMoneda" <> MAX(FP."IdMoneda") THEN 'DIFERENTE_MONEDA_ENTRE_MOVIMIENTO_Y_FORMA_PAGO'
					  ELSE  ''
				 END AS "Codigo"
				,'' as "CodigoDato1"
		       	,'' as "ValorDato1" 
				,'' as "CodigoDato2"
		       	,'' as "ValorDato2" 
		       	,IEG."NumeroReferencia"
		  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_FormaDePago" AS FP
		 INNER JOIN "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar" AS IEG ON IEG."NumeroReferencia" = FP."NumeroReferencia"		 
		 GROUP BY IEG."NumeroReferencia",IEG."IdMoneda",IEG."Monto"
		 UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","NumeroReferencia" FROM :v_TablaRequisitos;
		

		-- Valida que el monto del movimiento cuadre con la suma de los montos de sus formas de pago
		v_TablaRequisitos =
		 SELECT  2 as "Prioridad"
				,CASE WHEN (MAX(IEG."Monto") <> SUM(FP."Monto")) THEN 0
					  ELSE  1
				 END AS "Cumple"
				,CASE WHEN (MAX(IEG."Monto") <> SUM(FP."Monto")) THEN 'MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO'
					  ELSE  ''
				 END AS "Codigo"			
				,'Monto' as "CodigoDato1"
		       	,CAST(MAX(IEG."Monto") as NVARCHAR(520)) as "ValorDato1"
		       	,'MontoFP' as "CodigoDato2"
		       	,CAST(SUM(FP."Monto") as NVARCHAR(520)) as "ValorDato2" 
		       	,IEG."NumeroReferencia"			       	
		  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_FormaDePago" AS FP
		  INNER JOIN "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar" AS IEG ON IEG."NumeroReferencia" = FP."NumeroReferencia" AND :p_IdModulo = 'TES'
		  GROUP BY IEG."NumeroReferencia"		 
		  UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","NumeroReferencia" FROM :v_TablaRequisitos;
		

		-- Valida que la moneda del movimiento esté activa en tesoreria		
		v_TablaRequisitos =
		 SELECT  2 as "Prioridad"		
				,CASE WHEN (COALESCE(MT."IdMoneda",'') = '') THEN 0
					  ELSE  1
				 END AS "Cumple"
				,CASE WHEN (COALESCE(MT."IdMoneda",'') = '') THEN 'MONEDA_NO_HABILITADA_PARA_OPERAR_EN_TESORERIA'
					  ELSE  ''
				 END AS "Codigo"
		       	,'IdMoneda' as "CodigoDato1"
		       	,MT."IdMoneda" as "ValorDato1"
		       	,'' as "CodigoDato2"
		       	,'' as "ValorDato2" 
		       	,IEG."NumeroReferencia"		       	
		 FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar" AS IEG		   
		 LEFT JOIN "BSCL.Tesoreria.BD::TES.MonedaTesoreria" AS MT ON (MT."IdMoneda" = IEG."IdMoneda" AND MT."IdEstado" = 'ACT')
		 UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","NumeroReferencia" FROM :v_TablaRequisitos;
				


		----------------------------------------------------------------
		-- ANALISIS DE DATOS PARA SALIDA
		----------------------------------------------------------------

		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" > 1 THEN 1 END)
	   			INTO v_error_General, v_error_Negocio
	   	  FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0 THEN 'RECH'
	   								 WHEN :v_error_Negocio > 0 THEN 'ADVE'
	   								 ELSE 'OK'
	   							END;

		INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_MovimientosRechazados"
	    SELECT DISTINCT "NumeroReferencia"
	    FROM :v_TablaRequisitos
	    WHERE "Prioridad" > 1
	    AND "Cumple" = 0;	   
	       
	    v_TablaCodigosMensajes =
		SELECT  "NumeroReferencia" AS "IdInterno"
			   ,"Codigo"
			   ,"CodigoDato1"
			   ,"ValorDato1"
			   ,"CodigoDato2"
			   ,"ValorDato2" 
		FROM :v_TablaRequisitos
		WHERE "Cumple" = 0
		ORDER BY "Prioridad" DESC;		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalle);


		INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_ErroresDetalle"
		SELECT
			   ED."IdInterno" AS "NumeroReferencia",
			   ED."Codigo",
			   ED."Descripcion"
		FROM :v_out_ErroresDetalle as ED;
		
		
    END;	
END;
