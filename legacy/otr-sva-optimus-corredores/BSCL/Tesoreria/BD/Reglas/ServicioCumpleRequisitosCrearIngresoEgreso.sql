SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearIngresoEgreso";

CREATE PROCEDURE
-- 
--  Proposito: Validar los requisitos para la creacion y modificacion de movimientos de Ingreso - Egreso
--  Autor: NRM - BCS
--  Fecha Creacion: 23/11/2015
--  
-- TABLAS DE ENTRADA
-- "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_IngresoEgreso"
-- "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_FormaDePago"

-- TABLAS DE SALIDA
-- "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Salida_Resultado"
--
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearIngresoEgreso"(
	-- Parametros entrada 
	IN p_IdEmpresa              BIGINT,
	IN p_IdModulo				NVARCHAR(3)
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE v_FechaSistema      	NVARCHAR(10); 
	DECLARE v_FechaDesde			NVARCHAR(10);
	DECLARE v_FechaHasta			NVARCHAR(10);
	DECLARE v_ErroresContador		BIGINT;
	DECLARE v_Validaciones_RV2 		TABLE (
				"NumeroReferencia"   								BIGINT,
				"ERROR_CONSULTA_FLUJO_CAJA" 						NVARCHAR(10),
				"SALDO_CAJA_INSUFICIENTE_AUTORIZACION" 				NVARCHAR(10),
				"CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO_AUTORIZACION" NVARCHAR(10)
			);

	v_Validaciones_RV2 = SELECT 0 AS "NumeroReferencia", '' AS "ERROR_CONSULTA_FLUJO_CAJA", '' AS "SALDO_CAJA_INSUFICIENTE_AUTORIZACION", '' AS "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO_AUTORIZACION" 
						 FROM DUMMY WHERE 1=2;
				

	v_Persona = 
	SELECT PER."IdPersona",PER."NombreCompleto",PER."IdEstado"
	FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_IngresoEgreso" AS IEG
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as PER ON PER."IdPersona" IN (IEG."IdPersona",IEG."IdPersonaTitularPago");


	v_PersonaTarjetasBancarias = 
	SELECT TBC."IdTarjetaBancaria",TBC."IdEstado"
	FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_FormaDePago"  AS FP 
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" AS TBC ON TBC."IdTarjetaBancaria" IN (FP."IdTarjetaBancariaCorredora",FP."IdTarjetaBancariaSocio");

	-- Obtiene Fecha de Sistema
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_FechaSistema);


	----------------------------------------------------------------
	-- REGISTRO
	----------------------------------------------------------------
	v_MatrizValidaciones_v1 = 
	 SELECT IEG."NumeroReferencia"
			,IEG."IdTipoMovimiento"
		   	,IEG."IdTipoPersona"
		   	,IEG."IdPersona"
		   	,IEG."IdMoneda"
		   	,IEG."Monto"
		   	,IEG."FechaLiquidacion"
		   	,IEG."IdConcepto"
		   	,IEG."IdTipoPersonaTitularPago"
		   	,IEG."IdPersonaTitularPago"
		   	,IEG."IdPortafolioTitularPago"
		   	,IEG."IdCaja"
		   	,IEG."SecuenciaAgrupacion"		   	
		   	,IEG."SecuenciaRelacionada"
		   	,IEG."Glosa"
											   	
		   	-- Parámetros Opcionales(Referenciales)  
		   	,IEG."IdEmpleadoDigitador"
		   	,IEG."IdSucursalEmpleadoDigitador"
		   	,IEG."IdTipoProducto"
		    ,IEG."IdTipoOperacion"
		    ,IEG."IdTipoComprobante"
		   	,IEG."IdComprobante"
		   	,IEG."FechaComprobante"
		    ,IEG."FechaLiquidacionComprobante"											    
		    ,IEG."IdOperacion"
		    ,IEG."FechaOperacion"
		    ,IEG."IdCondicionLiquidacion"
		    ,IEG."IdBolsa"
		    ,IEG."IdMonedaOperacion"
		    ,IEG."MontoOperacion"
		    ,IEG."IdTipoReajuste"
		    ,IEG."FechaParidad"
		    ,IEG."ValorParidad"
		    ,IEG."FolioCartera"
		    ,IEG."IdInstrumento"
		    ,IEG."IdTipoInstrumento"
		    ,IEG."IdClasificacionTipoInstrumento"
											  
		    -- Indicarores
		    ,IEG."Bilateral"
		    ,IEG."GeneraComprobante"
		    ,IEG."AgruparEnComprobante"
			,IEG."MueveCuentaInversion"
		    ,IEG."TitularPago"
		    
		    ,TM."IdTipoMovimiento" AS "IdTipoMovimiento_Real"
		    ,CT."IdConcepto" AS "IdConcepto_Real"
		    ,Mon."IdMoneda" AS "IdMoneda_Real"
		    ,Mon."IdEstado" AS "IdEstado_MonedaReal"
		    ,Caj."IdCaja" AS "IdCaja_Real"
		    ,Per."IdPersona" AS "IdPersona_Real"
		    ,Per."NombreCompleto" AS "NombrePersona"
		    ,Per."IdEstado" AS "IdEstado_PersonaReal"
		    ,PerRel."IdPersona" AS "IdPersonaRelacionada_Real"
		    ,PerRel."NombreCompleto" AS "NombrePersonaRelacionada"
		    ,PerRel."IdEstado" AS "IdEstado_RelacionadoReal"
		    ,MT."IdMoneda" AS "IdMoneda_Tesoreria"
		    ,SEC."CountSec"
		    ,TPr."IdTipoProducto" AS "IdTipoProducto_Real"
		    ,TOpe."IdTipoOperacion" AS "IdTipoOperacion_Real"
		    ,Cond."IdCondicionDeLiquidacion" AS "IdCondicionDeLiquidacion_Real"
		    ,ARG."MontoTotalFP"
		    ,ARG."ExisteDiferenciaMoneda"
		    ,COALESCE(IndPor."OperaLineaSobregiro",'N') AS "OperaLineaSobregiro"
		    
		    ,CASE
		    	WHEN COALESCE(IndPor."OperaLineaSobregiro",'') = '' THEN 0  -- si no existe indicador de sobregiro
		    	WHEN IndPor."OperaLineaSobregiro" = 'N'             THEN 0
		    	WHEN IndPor."OperaLineaSobregiro" = 'S'             THEN COALESCE(LIM."MontoSobregiro",0)
		    	ELSE 0
		    END AS "MontoSobregiro"
		    
		    -- Que se valida
			,1 as "SIN_ESPECIFICAR_TIPO_MOVIMIENTO_TESORERIA"
		    ,1 as "SIN_ESPECIFICAR_CONCEPTO_TESORERIA"
		    ,1 as "SIN_ESPECIFICAR_MONEDA_TESORERIA"
		    ,1 as "SIN_ESPECIFICAR_MONTO_TESORERIA"
		    ,1 as "SIN_ESPECIFICAR_FECHA_LIQUIDACION_TESORERIA"
		    ,1 as "SIN_ESPECIFICAR_PERSONA_TESORERIA"
		    ,1 as "SIN_ESPECIFICAR_PORTAFOLIO_PERSONA_TITULAR_PAGO_TESORERIA"
		    ,1 as "SIN_ESPECIFICAR_INDICADOR_BILATERAL_TESORERIA"  
		    ,1 as "SIN_ESPECIFICAR_INDICADOR_GENERA_COMPROBANTE_TESORERIA"  
		    ,1 as "SIN_ESPECIFICAR_INDICADOR_AGRUPA_EN_COMPROBANTE_TESORERIA" 
		    ,1 as "SIN_ESPECIFICAR_INDICADOR_TITULAR_PAGO_TESORERIA"		    
		    ,1 as "SIN_DEFINIR_TIPO_MOVIMIENTO_TESORERIA"
			,1 as "SIN_DEFINIR_CONCEPTO_TESORERIA"
			,1 as "MONEDA_NO_EXISTE"
			,1 as "MONEDA_NO_ACTIVA"
			,1 as "CAJA_NO_EXISTE"
			,1 as "PERSONA_NO_EXISTE"
			,1 as "PERSONA_NO_ACTIVA"
			,1 as "PERSONA_RELACIONADA_NO_EXISTE"
			,1 as "PERSONA_RELACIONADA_NO_ACTIVA"
			,1 as "MONTO_MENOR_IGUAL_A_CERO"		    
		    ,1 as "MONEDA_NO_HABILITADA_PARA_OPERAR_EN_TESORERIA"
		    ,1 as "SECUENCIA_AGRUPACION_UTILIZADA_TESORERIA"		    
		    ,1 as "TIPO_PRODUCTO_NO_EXISTE"
			,1 as "TIPO_OPERACION_NO_EXISTE"
			,1 as "CONDICION_DE_LIQUIDACION_NO_EXISTE"
			,1 as "DIFERENTE_MONEDA_ENTRE_MOVIMIENTO_Y_FORMA_PAGO"
			,1 as "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO_AUTORIZACION"
			,1 as "SALDO_CAJA_INSUFICIENTE_AUTORIZACION"
			,1 as "ERROR_CONSULTA_FLUJO_CAJA"
						
---			,1 as "FECHA_LIQUIDACION_MENOR_A_FECHA_SISTEMA"
			,CASE WHEN :p_IdModulo = 'TES' THEN 1 ELSE 0 END as "MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO"
	 FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_IngresoEgreso" AS IEG
	 LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" as TM ON TM."IdTipoMovimiento" = IEG."IdTipoMovimiento"
	 LEFT JOIN "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" as CT ON CT."IdConcepto" = IEG."IdConcepto"
	 LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon ON Mon."IdMoneda" = IEG."IdMoneda"
	 LEFT JOIN "BSCL.Tesoreria.BD::TES.Caja" as Caj ON Caj."IdCaja" = IEG."IdCaja"	 
	 LEFT JOIN "BSCL.Tesoreria.BD::TES.MonedaTesoreria" AS MT ON (MT."IdMoneda" = IEG."IdMoneda" AND MT."IdEstado" = 'ACT')
	 LEFT JOIN (SELECT "SecuenciaAgrupacion", COUNT("SecuenciaAgrupacion") as "CountSec" FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" GROUP BY "SecuenciaAgrupacion") AS SEC 
	 		ON SEC."SecuenciaAgrupacion" = IEG."SecuenciaAgrupacion"
	 LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS TPr ON TPr."IdTipoProducto" = IEG."IdTipoProducto"
	 LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TOpe ON TOpe."IdTipoOperacion" = IEG."IdTipoOperacion"
	 LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as Cond ON Cond."IdCondicionDeLiquidacion" = IEG."IdCondicionLiquidacion"
	 LEFT JOIN :v_Persona as Per ON Per."IdPersona" = IEG."IdPersona"
	 LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicadoresPortafolioCuentaInversion" AS IndPor 
	  		 ON IndPor."IdPortafolio" = IEG."IdPortafolioTitularPago"
	 LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (POR."IdPortafolio" = IEG."IdPortafolioTitularPago")
	 LEFT JOIN  -- limite para sobregiro cuenta inversión
	 (
	  	 SELECT "IdPlantillaLimite", "IdMoneda", MAX("MontoTotal") AS "MontoSobregiro" 
	  	 FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarDetallePlantillaLimites"
	  	 WHERE "IdTipoLimite" = 'LIMPORTSOBREGIRO' 
		 AND   "IdEstado" = 'ACT'
		 GROUP BY "IdPlantillaLimite", "IdMoneda" 
	 )  AS LIM ON (LIM."IdPlantillaLimite" = POR."IdPerfilLimitesExtra" AND LIM."IdMoneda" = IEG."IdMoneda")
	 LEFT JOIN :v_Persona as PerRel ON PerRel."IdPersona" = IEG."IdPersonaTitularPago"
	 LEFT JOIN (SELECT DISTINCT FP."NumeroReferencia",
					   SUM(FP."Monto") OVER (partition by FP."NumeroReferencia") AS "MontoTotalFP",
					   SUM(CASE WHEN FP."IdMoneda" <> IEG."IdMoneda" THEN 1 ELSE 0 END) OVER (partition by FP."NumeroReferencia") AS "ExisteDiferenciaMoneda"
				  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_FormaDePago" AS FP
				 INNER JOIN "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_IngresoEgreso" AS IEG 
						 ON IEG."NumeroReferencia" = FP."NumeroReferencia") AS ARG
		    ON ARG."NumeroReferencia" = IEG."NumeroReferencia";
	

		
	
	v_Validaciones_RV1 =
	 SELECT  "NumeroReferencia"
			,CASE WHEN "SIN_ESPECIFICAR_TIPO_MOVIMIENTO_TESORERIA" = 1 AND LENGTH("IdTipoMovimiento") = 0 THEN 'RECH' ELSE 'OK' END AS "SIN_ESPECIFICAR_TIPO_MOVIMIENTO_TESORERIA"
		    ,CASE WHEN "SIN_ESPECIFICAR_CONCEPTO_TESORERIA" = 1 AND LENGTH("IdConcepto") = 0  THEN 'RECH' ELSE 'OK' END AS "SIN_ESPECIFICAR_CONCEPTO_TESORERIA"
		    ,CASE WHEN "SIN_ESPECIFICAR_MONEDA_TESORERIA" = 1 AND LENGTH("IdMoneda") = 0 THEN 'RECH' ELSE 'OK' END AS "SIN_ESPECIFICAR_MONEDA_TESORERIA"
		    ,CASE WHEN "SIN_ESPECIFICAR_MONTO_TESORERIA" = 1 AND COALESCE("Monto",0) = 0 THEN 'RECH' ELSE 'OK' END AS "SIN_ESPECIFICAR_MONTO_TESORERIA"
		    ,CASE WHEN "SIN_ESPECIFICAR_FECHA_LIQUIDACION_TESORERIA" = 1 AND COALESCE("FechaLiquidacion",'0001-01-01') = '0001-01-01' THEN 'RECH' ELSE 'OK' END AS "SIN_ESPECIFICAR_FECHA_LIQUIDACION_TESORERIA"
		    ,CASE WHEN "SIN_ESPECIFICAR_PERSONA_TESORERIA" = 1 AND COALESCE("IdPersona",0) = 0 THEN 'RECH' ELSE 'OK' END AS "SIN_ESPECIFICAR_PERSONA_TESORERIA"
		    ,CASE WHEN "SIN_ESPECIFICAR_PORTAFOLIO_PERSONA_TITULAR_PAGO_TESORERIA" = 1 AND ("IdTipoPersonaTitularPago" = 'CLI' AND COALESCE("IdPortafolioTitularPago",0) = 0) THEN 'RECH' ELSE 'OK' END AS "SIN_ESPECIFICAR_PORTAFOLIO_PERSONA_TITULAR_PAGO_TESORERIA"
		    ,CASE WHEN "SIN_ESPECIFICAR_INDICADOR_BILATERAL_TESORERIA" = 1 AND LENGTH("Bilateral") = 0 THEN 'RECH' ELSE 'OK' END AS "SIN_ESPECIFICAR_INDICADOR_BILATERAL_TESORERIA"
		    ,CASE WHEN "SIN_ESPECIFICAR_INDICADOR_GENERA_COMPROBANTE_TESORERIA" = 1 AND LENGTH("GeneraComprobante") = 0 THEN 'RECH' ELSE 'OK' END AS "SIN_ESPECIFICAR_INDICADOR_GENERA_COMPROBANTE_TESORERIA"
		    ,CASE WHEN "SIN_ESPECIFICAR_INDICADOR_AGRUPA_EN_COMPROBANTE_TESORERIA" = 1 AND LENGTH("AgruparEnComprobante") = 0 THEN 'RECH' ELSE 'OK' END AS "SIN_ESPECIFICAR_INDICADOR_AGRUPA_EN_COMPROBANTE_TESORERIA"
		    ,CASE WHEN "SIN_ESPECIFICAR_INDICADOR_TITULAR_PAGO_TESORERIA" = 1 AND LENGTH("TitularPago") = 0 THEN 'RECH' ELSE 'OK' END AS "SIN_ESPECIFICAR_INDICADOR_TITULAR_PAGO_TESORERIA"		    
		    ,CASE WHEN "SIN_DEFINIR_TIPO_MOVIMIENTO_TESORERIA" = 1 AND COALESCE("IdTipoMovimiento_Real",'') = '' THEN 'RECH' ELSE 'OK' END AS "SIN_DEFINIR_TIPO_MOVIMIENTO_TESORERIA"
			,CASE WHEN "SIN_DEFINIR_CONCEPTO_TESORERIA" = 1 AND COALESCE("IdConcepto_Real",'') = '' THEN 'RECH' ELSE 'OK' END AS "SIN_DEFINIR_CONCEPTO_TESORERIA"
			,CASE WHEN "MONEDA_NO_EXISTE" = 1 AND LENGTH("IdMoneda") > 0 AND COALESCE("IdMoneda_Real",'') = '' THEN 'RECH' ELSE 'OK' END AS "MONEDA_NO_EXISTE"
			,CASE WHEN "MONEDA_NO_ACTIVA" = 1 AND LENGTH("IdMoneda") > 0 AND "IdEstado_MonedaReal" = 'INA' THEN 'RECH' ELSE 'OK' END AS "MONEDA_NO_ACTIVA"
			,CASE WHEN "CAJA_NO_EXISTE" = 1 AND ("IdCaja" > 0 AND COALESCE("IdCaja_Real",0) = 0) THEN 'RECH' ELSE 'OK' END AS "CAJA_NO_EXISTE"
			,CASE WHEN "PERSONA_NO_EXISTE" = 1 AND COALESCE("IdPersona_Real",0) = 0  THEN 'RECH' ELSE 'OK' END AS "PERSONA_NO_EXISTE"
			,CASE WHEN "PERSONA_NO_ACTIVA" = 1 AND "IdEstado_PersonaReal" = 'INA' THEN 'RECH' ELSE 'OK' END AS "PERSONA_NO_ACTIVA"
			,CASE WHEN "PERSONA_RELACIONADA_NO_EXISTE" = 1 AND COALESCE("IdPersonaRelacionada_Real",0) = 0 THEN 'RECH' ELSE 'OK' END AS "PERSONA_RELACIONADA_NO_EXISTE"
			,CASE WHEN "PERSONA_RELACIONADA_NO_ACTIVA" = 1 AND "IdEstado_RelacionadoReal" = 'INA' THEN 'RECH' ELSE 'OK' END AS "PERSONA_RELACIONADA_NO_ACTIVA"
			,CASE WHEN "MONTO_MENOR_IGUAL_A_CERO" = 1 AND "Monto" = 0 THEN 'RECH' ELSE 'OK' END AS "MONTO_MENOR_IGUAL_A_CERO"		    
		    ,CASE WHEN "MONEDA_NO_HABILITADA_PARA_OPERAR_EN_TESORERIA" = 1 AND LENGTH("IdMoneda") > 0 AND (COALESCE("IdMoneda_Tesoreria",'') = '') THEN 'RECH' ELSE 'OK' END AS "MONEDA_NO_HABILITADA_PARA_OPERAR_EN_TESORERIA"
		    ,CASE WHEN "SECUENCIA_AGRUPACION_UTILIZADA_TESORERIA" = 1 AND COALESCE("SecuenciaAgrupacion",'') <> '' AND "CountSec" > 0 THEN 'RECH' ELSE 'OK' END AS "SECUENCIA_AGRUPACION_UTILIZADA_TESORERIA"		    
		    ,CASE WHEN "TIPO_PRODUCTO_NO_EXISTE" = 1 AND (LENGTH("IdTipoProducto") > 0 AND COALESCE("IdTipoProducto_Real",'') = '')  THEN 'RECH' ELSE 'OK' END AS "TIPO_PRODUCTO_NO_EXISTE"
			,CASE WHEN "TIPO_OPERACION_NO_EXISTE" = 1 AND (LENGTH("IdTipoOperacion") > 0 AND COALESCE("IdTipoOperacion_Real",'') = '') THEN 'RECH' ELSE 'OK' END AS "TIPO_OPERACION_NO_EXISTE" 
			,CASE WHEN "CONDICION_DE_LIQUIDACION_NO_EXISTE" = 1 AND (LENGTH("IdCondicionLiquidacion") > 0 AND COALESCE("IdCondicionDeLiquidacion_Real",'') = '') THEN 'RECH' ELSE 'OK' END AS "CONDICION_DE_LIQUIDACION_NO_EXISTE"			
			,CASE WHEN "DIFERENTE_MONEDA_ENTRE_MOVIMIENTO_Y_FORMA_PAGO" = 1 AND "ExisteDiferenciaMoneda" > 0 THEN 'RECH' ELSE 'OK' END AS "DIFERENTE_MONEDA_ENTRE_MOVIMIENTO_Y_FORMA_PAGO"
			,CASE WHEN "MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO" = 1 AND "Monto" <> "MontoTotalFP" THEN 'RECH' ELSE 'OK' END AS "MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO"			
			---,CASE WHEN "FECHA_LIQUIDACION_MENOR_A_FECHA_SISTEMA" = 1 AND (DAYS_BETWEEN (TO_DATE(:v_FechaSistema, 'YYYY-MM-DD'), TO_DATE("FechaLiquidacion", 'YYYY-MM-DD')) < 0) THEN 'RECH' ELSE 'OK' END AS "FECHA_LIQUIDACION_MENOR_A_FECHA_SISTEMA"			
	  FROM :v_MatrizValidaciones_v1;



	-----------------------------------------------------------------  
	-- VALIDACION SOBREGIRO CUENTA INVERSION (para egresos)	
	-----------------------------------------------------------------
	v_PortafolioAConsultar = SELECT DISTINCT "IdPortafolioTitularPago" AS "IdPortafolio","IdMoneda" FROM :v_MatrizValidaciones_v1 WHERE "IdTipoMovimiento" = 'MOVEGR';
	SELECT MIN("FechaLiquidacion") INTO v_FechaDesde FROM :v_MatrizValidaciones_v1 WHERE "IdTipoMovimiento" = 'MOVEGR';
	SELECT MAX("FechaLiquidacion") INTO v_FechaHasta FROM :v_MatrizValidaciones_v1 WHERE "IdTipoMovimiento" = 'MOVEGR';
		
	IF (NOT IS_EMPTY(:v_PortafolioAConsultar)) THEN
		
			CALL "BSCL.Plataforma.BD.Servicios::SaldoFlujoCaja" 
			(
			     p_IdEmpresa				=> p_IdEmpresa				    
				,p_FechaDesde				=> :v_FechaDesde
			    ,p_FechaHasta				=> :v_FechaHasta
			    ,p_uso						=> 'CUENTA_INVERSION'
			    ,p_PortafolioAConsultar		=> :v_PortafolioAConsultar
			    ,p_out_FlujoCaja			=> :v_FlujoCaja
			    ,p_out_FlujoCajaDetallado	=> :v_FlujoCajaDetallado
				,p_out_ErroresContador		=> :v_ErroresContador
				,p_out_ErroresDetalle		=> :v_ErroresDetalle
			);
			
			v_Validaciones_RV2 =
			SELECT "NumeroReferencia"
				   ,CASE WHEN "ERROR_CONSULTA_FLUJO_CAJA" = 1            AND :v_ErroresContador > 0 THEN 'RECH' ELSE 'OK' END AS "ERROR_CONSULTA_FLUJO_CAJA"
			       ,CASE WHEN "SALDO_CAJA_INSUFICIENTE_AUTORIZACION"   = 1            AND :v_ErroresContador = 0 AND "SaldoFinal" < 0 AND "OperaLineaSobregiro" = 'S' THEN 'RAUT' ELSE 'OK' END AS "SALDO_CAJA_INSUFICIENTE_AUTORIZACION"  -- Requiere autorizacion
			       ,CASE WHEN "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO_AUTORIZACION" = 1 AND :v_ErroresContador = 0 AND "SaldoFinal" < 0 AND "OperaLineaSobregiro" = 'N' THEN 'RAUT' ELSE 'OK' END AS "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO_AUTORIZACION"  -- Requiere autorizacion			       
			  FROM (SELECT  POR."NumeroReferencia"
						   ,POR."OperaLineaSobregiro"
						   ,POR."CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO_AUTORIZACION"
						   ,POR."SALDO_CAJA_INSUFICIENTE_AUTORIZACION"
						   ,POR."ERROR_CONSULTA_FLUJO_CAJA"
						   ,(FC."Saldo"+POR."MontoSobregiro") - SUM(POR."Monto") over (partition by POR."IdPortafolioTitularPago",POR."IdMoneda" order by POR."NumeroReferencia" asc 
									 	                     		 rows between unbounded preceding and current row) as "SaldoFinal"
					 FROM :v_FlujoCaja as FC
					 INNER JOIN :v_MatrizValidaciones_v1 as POR
					     	 ON POR."IdPortafolioTitularPago" 		= FC."IdPortafolio" 
					     	AND POR."IdMoneda" 						= FC."IdMoneda"
							AND POR."FechaLiquidacion"  			= FC."FechaSaldo"
				   ) as SF;
	END IF; -- IF (NOT IS_EMPTY(:v_PortafolioAConsultar))

	
	----------------------------------------------------------------
	-- FORMAS DE PAGO
	----------------------------------------------------------------
	v_MatrizValidaciones_v2 =
	 SELECT FP."NumeroReferencia"
			,FP."IdMedioDePago"
			,FP."IdModalidadDePago"
			,FP."IdMoneda"
			,FP."Monto"
			,FP."IdCuentaBancariaCorredora"
			,FP."IdTarjetaBancariaCorredora"
			,FP."IdChequera"
			,FP."NumeroDocumentoCorredora"
			,FP."FechaDocumentoPagoCorredora"
			,FP."IdCuentaBancariaSocio"
			,FP."IdBancoSocio"
			,FP."NombreCuentaBancariaSocio"
			,FP."NumeroCuentaBancariaSocio"
			,FP."IdTipoCuentaBancariaSocio"
			,FP."NombreTipoCuentaBancariaSocio"
			,FP."IdTarjetaBancariaSocio"
			,FP."IdTipoTarjetaBancariaSocio"
			,FP."NombreTipoTarjetaBancariaSocio"
			,FP."NumeroTarjetaBancariaSocio"
			,FP."NumeroSerieSocio"
			,FP."NumeroDocumentoSocio"
			,FP."FechaDocumentoPagoSocio"
			,Mon."IdMoneda" AS "IdMoneda_Real"
			,Mon."IdEstado" AS "IdEstado_MonedaReal"
			,MeP."IdMedioDePago" AS "IdMedioDePago_Real"
			,MoP."IdModalidadDePago" AS "IdModalidadDePago_Real"
			,CBC."IdCuentaBanco" AS "IdCuentaBanco_Real"
			,TBC."IdTarjetaBancaria" AS "IdTarjetaCorredoraBancaria_Real"
			,CBS."IdCuentaBancaria" AS "IdCuentaBancaria_Real"
			,TBS."IdTarjetaBancaria" AS "IdTarjetaSocioBancaria_Real"
			,MeP."TipoMedioDePago"
			,CAN."CountMedioDePago"  -- cantidad de medios de pagos
						
			,1 as "MEDIO_DE_PAGO_INVALIDO"
			,1 as "MODALIDAD_DE_PAGO_INVALIDO"
			,1 as "MONEDA_NO_EXISTE"
			,1 as "MONEDA_NO_ACTIVA"
			,1 as "MONTO_MENOR_IGUAL_A_CERO"
			,1 as "CUENTA_BANCARIA_CORREDORA_INVALIDA"
			,1 as "TARJETA_BANCARIA_CORREDORA_INVALIDA"
			,1 as "CUENTA_BANCARIA_SOCIO_INVALIDA"
			,1 as "TARJETA_BANCARIA_SOCIO_INVALIDA"
			,1 as "TRANSFERENCIA_SOLO_ACEPTA_UN_MEDIO_DE_PAGO"   -- Cuando se paga con transferencia, no se puede mezclar con otros medios de pago    
						
	   FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_FormaDePago" AS FP
	   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MeP ON MeP."IdMedioDePago" = FP."IdMedioDePago"
	   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS MoP ON MoP."IdModalidadDePago" = FP."IdModalidadDePago"
	   LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon ON Mon."IdMoneda" = FP."IdMoneda"
	   LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CBC ON CBC."IdCuentaBanco" = FP."IdCuentaBancariaCorredora"
	   LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS CBS ON CBS."IdCuentaBancaria" = FP."IdCuentaBancariaSocio"
	   LEFT OUTER JOIN :v_PersonaTarjetasBancarias AS TBC ON TBC."IdTarjetaBancaria" = FP."IdTarjetaBancariaCorredora"
	   LEFT OUTER JOIN :v_PersonaTarjetasBancarias AS TBS ON TBS."IdTarjetaBancaria" = FP."IdTarjetaBancariaSocio"
	   LEFT OUTER JOIN (
					      SELECT "NumeroReferencia", COUNT("IdMedioDePago") AS "CountMedioDePago" 
					      FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_FormaDePago"
					      GROUP BY "NumeroReferencia"
		   			   ) AS CAN ON (CAN."NumeroReferencia" = FP."NumeroReferencia");	   
	   

	
	v_Validaciones_FV1 =
	 SELECT  "NumeroReferencia", "IdMedioDePago"
			,CASE WHEN "MEDIO_DE_PAGO_INVALIDO" = 1 AND COALESCE("IdMedioDePago_Real",'') = '' THEN 'RECH' ELSE 'OK' END AS "MEDIO_DE_PAGO_INVALIDO"
			,CASE WHEN "MODALIDAD_DE_PAGO_INVALIDO" = 1 AND (COALESCE("IdMedioDePago",'') <> 'CIN' AND COALESCE("IdModalidadDePago_Real",'') = '') THEN 'RECH' ELSE 'OK' END AS "MODALIDAD_DE_PAGO_INVALIDO"
			,CASE WHEN "MONEDA_NO_EXISTE" = 1 AND COALESCE("IdMoneda_Real",'') = '' THEN 'RECH' ELSE 'OK' END AS "MONEDA_NO_EXISTE"
			,CASE WHEN "MONEDA_NO_ACTIVA" = 1 AND "IdEstado_MonedaReal" = 'INA' THEN 'RECH' ELSE 'OK' END AS "MONEDA_NO_ACTIVA"
			,CASE WHEN "MONTO_MENOR_IGUAL_A_CERO" = 1 AND "Monto" = 0 THEN 'RECH' ELSE 'OK' END AS "MONTO_MENOR_IGUAL_A_CERO"
			,CASE WHEN "CUENTA_BANCARIA_CORREDORA_INVALIDA" = 1 AND "IdCuentaBancariaCorredora" > 0 AND COALESCE("IdCuentaBanco_Real",0) = 0  THEN 'RECH' ELSE 'OK' END AS "CUENTA_BANCARIA_CORREDORA_INVALIDA"
			,CASE WHEN "TARJETA_BANCARIA_CORREDORA_INVALIDA" = 1 AND "IdTarjetaBancariaCorredora" > 0 AND COALESCE("IdTarjetaCorredoraBancaria_Real",0) = 0 THEN 'RECH' ELSE 'OK' END AS "TARJETA_BANCARIA_CORREDORA_INVALIDA"
			,CASE WHEN "CUENTA_BANCARIA_SOCIO_INVALIDA" = 1 AND "IdCuentaBancariaSocio" > 0 AND COALESCE("IdCuentaBancaria_Real",0) = 0 THEN 'RECH' ELSE 'OK' END AS "CUENTA_BANCARIA_SOCIO_INVALIDA"
			,CASE WHEN "TARJETA_BANCARIA_SOCIO_INVALIDA" = 1 AND "IdTarjetaBancariaSocio" > 0 AND COALESCE("IdTarjetaSocioBancaria_Real",0) = 0 THEN 'RECH' ELSE 'OK' END AS "TARJETA_BANCARIA_SOCIO_INVALIDA"
			,CASE WHEN "TRANSFERENCIA_SOLO_ACEPTA_UN_MEDIO_DE_PAGO" = 1 AND COALESCE("CountMedioDePago",0) > 1 AND COALESCE("TipoMedioDePago",'') = 'TRA' THEN 'RECH' ELSE 'OK' END AS "TRANSFERENCIA_SOLO_ACEPTA_UN_MEDIO_DE_PAGO"
	  FROM :v_MatrizValidaciones_v2;
	


	----------------------------------------------------------------
	-- RESULTADO
	----------------------------------------------------------------
	INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Salida_Resultado"
	 SELECT  DISTINCT R."NumeroReferencia" AS "IdInterno",R."Codigo",R."Resultado"	 	
	 		,CASE WHEN "Codigo" = 'SIN_ESPECIFICAR_TIPO_MOVIMIENTO_TESORERIA' THEN 'NumeroReferencia'
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_CONCEPTO_TESORERIA' THEN 'NumeroReferencia'
			      WHEN "Codigo" = 'SIN_ESPECIFICAR_MONEDA_TESORERIA' THEN 'NumeroReferencia'
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_MONTO_TESORERIA' THEN 'NumeroReferencia'
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_FECHA_LIQUIDACION_TESORERIA' THEN 'NumeroReferencia'
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_PERSONA_TESORERIA' THEN 'NumeroReferencia'
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_PORTAFOLIO_PERSONA_TITULAR_PAGO_TESORERIA' THEN 'NumeroReferencia'
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_INDICADOR_BILATERAL_TESORERIA' THEN 'NumeroReferencia'
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_INDICADOR_GENERA_COMPROBANTE_TESORERIA' THEN 'NumeroReferencia'
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_INDICADOR_AGRUPA_EN_COMPROBANTE_TESORERIA' THEN 'NumeroReferencia'
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_INDICADOR_TITULAR_PAGO_TESORERIA' THEN 'NumeroReferencia'				  
				  WHEN "Codigo" = 'SIN_DEFINIR_TIPO_MOVIMIENTO_TESORERIA' THEN 'IdTipoMovimiento'
				  WHEN "Codigo" = 'SIN_DEFINIR_CONCEPTO_TESORERIA' THEN 'IdConcepto'
				  WHEN "Codigo" = 'MONEDA_NO_EXISTE' THEN 'IdMoneda'
				  WHEN "Codigo" = 'MONEDA_NO_ACTIVA' THEN 'IdMoneda'
				  WHEN "Codigo" = 'CAJA_NO_EXISTE' THEN 'IdCaja'
				  WHEN "Codigo" = 'PERSONA_NO_EXISTE' THEN 'IdPersona'
				  WHEN "Codigo" = 'PERSONA_NO_ACTIVA' THEN 'NombrePersona'
				  WHEN "Codigo" = 'PERSONA_RELACIONADA_NO_EXISTE' THEN 'IdPersona'
				  WHEN "Codigo" = 'PERSONA_RELACIONADA_NO_ACTIVA' THEN 'NombrePersona'				  
				  WHEN "Codigo" = 'MONEDA_NO_HABILITADA_PARA_OPERAR_EN_TESORERIA' THEN 'IdMoneda'
				  WHEN "Codigo" = 'SECUENCIA_AGRUPACION_UTILIZADA_TESORERIA' THEN 'IdSecuencia'				  
				  WHEN "Codigo" = 'TIPO_PRODUCTO_NO_EXISTE' THEN 'IdTipoProducto'
				  WHEN "Codigo" = 'TIPO_OPERACION_NO_EXISTE' THEN 'IdTipoOperacion'
				  WHEN "Codigo" = 'CONDICION_DE_LIQUIDACION_NO_EXISTE' THEN 'IdCondicionDeLiquidacion'				  
				  WHEN "Codigo" = 'DIFERENTE_MONEDA_ENTRE_MOVIMIENTO_Y_FORMA_PAGO' THEN 'IdMoneda'
				  WHEN "Codigo" = 'MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO' THEN 'Monto'				  
				  ----WHEN "Codigo" = 'FECHA_LIQUIDACION_MENOR_A_FECHA_SISTEMA' THEN 'FechaLiquidacion'
	 			  ELSE ''
	 		  END "CodigoDato1"
	 		,CASE WHEN "Codigo" = 'SIN_ESPECIFICAR_TIPO_MOVIMIENTO_TESORERIA' THEN CAST(R."NumeroReferencia" as NVARCHAR(520))
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_CONCEPTO_TESORERIA' THEN CAST(R."NumeroReferencia" as NVARCHAR(520))
			      WHEN "Codigo" = 'SIN_ESPECIFICAR_MONEDA_TESORERIA' THEN CAST(R."NumeroReferencia" as NVARCHAR(520))
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_MONTO_TESORERIA' THEN CAST(R."NumeroReferencia" as NVARCHAR(520))
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_FECHA_LIQUIDACION_TESORERIA' THEN CAST(R."NumeroReferencia" as NVARCHAR(520))
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_PERSONA_TESORERIA' THEN CAST(R."NumeroReferencia" as NVARCHAR(520))
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_PORTAFOLIO_PERSONA_TITULAR_PAGO_TESORERIA' THEN CAST(R."NumeroReferencia" as NVARCHAR(520))
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_INDICADOR_BILATERAL_TESORERIA' THEN CAST(R."NumeroReferencia" as NVARCHAR(520))
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_INDICADOR_GENERA_COMPROBANTE_TESORERIA' THEN CAST(R."NumeroReferencia" as NVARCHAR(520))
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_INDICADOR_AGRUPA_EN_COMPROBANTE_TESORERIA' THEN CAST(R."NumeroReferencia" as NVARCHAR(520))
				  WHEN "Codigo" = 'SIN_ESPECIFICAR_INDICADOR_TITULAR_PAGO_TESORERIA' THEN CAST(R."NumeroReferencia" as NVARCHAR(520))				  
				  WHEN "Codigo" = 'SIN_DEFINIR_TIPO_MOVIMIENTO_TESORERIA' THEN "IdTipoMovimiento"
				  WHEN "Codigo" = 'SIN_DEFINIR_CONCEPTO_TESORERIA' THEN "IdConcepto"
				  WHEN "Codigo" = 'MONEDA_NO_EXISTE' THEN "IdMoneda_Real"
				  WHEN "Codigo" = 'MONEDA_NO_ACTIVA' THEN "IdEstado_MonedaReal"
				  WHEN "Codigo" = 'CAJA_NO_EXISTE' THEN CAST("IdCaja" as NVARCHAR(520))
				  WHEN "Codigo" = 'PERSONA_NO_EXISTE' THEN CAST("IdPersona" as NVARCHAR(520))
				  WHEN "Codigo" = 'PERSONA_NO_ACTIVA' THEN "NombrePersona"
				  WHEN "Codigo" = 'PERSONA_RELACIONADA_NO_EXISTE' THEN CAST("IdPersonaTitularPago" as NVARCHAR(520))
				  WHEN "Codigo" = 'PERSONA_RELACIONADA_NO_ACTIVA' THEN "NombrePersonaRelacionada"
				  WHEN "Codigo" = 'MONEDA_NO_HABILITADA_PARA_OPERAR_EN_TESORERIA' THEN "IdMoneda_Tesoreria"
				  WHEN "Codigo" = 'SECUENCIA_AGRUPACION_UTILIZADA_TESORERIA' THEN "SecuenciaAgrupacion"
				  WHEN "Codigo" = 'TIPO_PRODUCTO_NO_EXISTE' THEN CAST("IdTipoProducto" as NVARCHAR(520))
				  WHEN "Codigo" = 'TIPO_OPERACION_NO_EXISTE' THEN CAST("IdTipoOperacion" as NVARCHAR(520))
				  WHEN "Codigo" = 'CONDICION_DE_LIQUIDACION_NO_EXISTE' THEN "IdCondicionLiquidacion"
				  WHEN "Codigo" = 'DIFERENTE_MONEDA_ENTRE_MOVIMIENTO_Y_FORMA_PAGO' THEN "IdMoneda"
				  WHEN "Codigo" = 'MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO' THEN CAST("Monto" as NVARCHAR(520))				  
---				  WHEN "Codigo" = 'FECHA_LIQUIDACION_MENOR_A_FECHA_SISTEMA' THEN CAST("FechaLiquidacion" as NVARCHAR(520))
	 			  ELSE ''
	 		  END "ValorDato1"
	 		,CASE WHEN "Codigo" = 'MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO' THEN 'MontoFP'
	 			  ELSE ''
	 		  END as "CodigoDato2"
	 		,CASE WHEN "Codigo" = 'MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO' THEN CAST("MontoTotalFP" as NVARCHAR(520))
	 			  ELSE ''
	 		  END as "ValorDato2"
	   FROM (SELECT "NumeroReferencia",
			 		COALESCE(			 			
			 			 "1"  ||'SIN_ESPECIFICAR_TIPO_MOVIMIENTO_TESORERIA'
					    ,"2"  ||'SIN_ESPECIFICAR_CONCEPTO_TESORERIA'
					    ,"3"  ||'SIN_ESPECIFICAR_MONEDA_TESORERIA'
					    ,"4"  ||'SIN_ESPECIFICAR_MONTO_TESORERIA'
					    ,"5"  ||'SIN_ESPECIFICAR_FECHA_LIQUIDACION_TESORERIA'
					    ,"6"  ||'SIN_ESPECIFICAR_PERSONA_TESORERIA'
					    ,"7"  ||'SIN_ESPECIFICAR_PORTAFOLIO_PERSONA_TITULAR_PAGO_TESORERIA'
					    ,"8"  ||'SIN_ESPECIFICAR_INDICADOR_BILATERAL_TESORERIA'
					    ,"9"  ||'SIN_ESPECIFICAR_INDICADOR_GENERA_COMPROBANTE_TESORERIA'
					    ,"10" ||'SIN_ESPECIFICAR_INDICADOR_AGRUPA_EN_COMPROBANTE_TESORERIA'
					    ,"11" ||'SIN_ESPECIFICAR_INDICADOR_TITULAR_PAGO_TESORERIA'		    
					    ,"12" ||'SIN_DEFINIR_TIPO_MOVIMIENTO_TESORERIA'
						,"13" ||'SIN_DEFINIR_CONCEPTO_TESORERIA'
						,"14" ||'MONEDA_NO_EXISTE'
						,"15" ||'MONEDA_NO_ACTIVA'
						,"16" ||'CAJA_NO_EXISTE'
						,"17" ||'PERSONA_NO_EXISTE'
						,"18" ||'PERSONA_NO_ACTIVA'
						,"19" ||'PERSONA_RELACIONADA_NO_EXISTE'
						,"20" ||'PERSONA_RELACIONADA_NO_ACTIVA'
						,"21" ||'MONTO_MENOR_IGUAL_A_CERO'			    
					    ,"22" ||'MONEDA_NO_HABILITADA_PARA_OPERAR_EN_TESORERIA'
					    ,"23" ||'SECUENCIA_AGRUPACION_UTILIZADA_TESORERIA'
					    ,"24" ||'TIPO_PRODUCTO_NO_EXISTE'
						,"25" ||'TIPO_OPERACION_NO_EXISTE'
						,"26" ||'CONDICION_DE_LIQUIDACION_NO_EXISTE'
						,"27" ||'DIFERENTE_MONEDA_ENTRE_MOVIMIENTO_Y_FORMA_PAGO'
						,"28" ||'MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO'
---						,"29" ||'FECHA_LIQUIDACION_MENOR_A_FECHA_SISTEMA'
			 		) AS "Codigo",
			 		COALESCE(			 			
			 			 "1"  ||"SIN_ESPECIFICAR_TIPO_MOVIMIENTO_TESORERIA"
					    ,"2"  ||"SIN_ESPECIFICAR_CONCEPTO_TESORERIA"
					    ,"3"  ||"SIN_ESPECIFICAR_MONEDA_TESORERIA"
					    ,"4"  ||"SIN_ESPECIFICAR_MONTO_TESORERIA"
					    ,"5"  ||"SIN_ESPECIFICAR_FECHA_LIQUIDACION_TESORERIA"
					    ,"6"  ||"SIN_ESPECIFICAR_PERSONA_TESORERIA"
					    ,"7"  ||"SIN_ESPECIFICAR_PORTAFOLIO_PERSONA_TITULAR_PAGO_TESORERIA"
					    ,"8"  ||"SIN_ESPECIFICAR_INDICADOR_BILATERAL_TESORERIA"
					    ,"9"  ||"SIN_ESPECIFICAR_INDICADOR_GENERA_COMPROBANTE_TESORERIA"
					    ,"10" ||"SIN_ESPECIFICAR_INDICADOR_AGRUPA_EN_COMPROBANTE_TESORERIA"
					    ,"11" ||"SIN_ESPECIFICAR_INDICADOR_TITULAR_PAGO_TESORERIA"		    
					    ,"12" ||"SIN_DEFINIR_TIPO_MOVIMIENTO_TESORERIA"
						,"13" ||"SIN_DEFINIR_CONCEPTO_TESORERIA"
						,"14" ||"MONEDA_NO_EXISTE"
						,"15" ||"MONEDA_NO_ACTIVA"
						,"16" ||"CAJA_NO_EXISTE"
						,"17" ||"PERSONA_NO_EXISTE"
						,"18" ||"PERSONA_NO_ACTIVA"
						,"19" ||"PERSONA_RELACIONADA_NO_EXISTE"
						,"20" ||"PERSONA_RELACIONADA_NO_ACTIVA"
						,"21" ||"MONTO_MENOR_IGUAL_A_CERO"			    
					    ,"22" ||"MONEDA_NO_HABILITADA_PARA_OPERAR_EN_TESORERIA"
					    ,"23" ||"SECUENCIA_AGRUPACION_UTILIZADA_TESORERIA"
					    ,"24" ||"TIPO_PRODUCTO_NO_EXISTE"
						,"25" ||"TIPO_OPERACION_NO_EXISTE"
						,"26" ||"CONDICION_DE_LIQUIDACION_NO_EXISTE"
						,"27" ||"DIFERENTE_MONEDA_ENTRE_MOVIMIENTO_Y_FORMA_PAGO"
						,"28" ||"MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO"						
---						,"29" ||"FECHA_LIQUIDACION_MENOR_A_FECHA_SISTEMA"

			 		) AS "Resultado"
			   FROM :v_Validaciones_RV1,"BSCL.Plataforma.BD::PLA.Identidad") as R
	  INNER JOIN :v_MatrizValidaciones_v1 as M ON M."NumeroReferencia" = R."NumeroReferencia"
	  WHERE R."Resultado" IS NOT NULL
	  
	  UNION ALL
	  
	  SELECT "IdInterno","Codigo","Resultado",
	 	CASE
	 		WHEN "Codigo" = 'CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO_AUTORIZACION' THEN 'Cliente'
	 		WHEN "Codigo" = 'SALDO_CAJA_INSUFICIENTE_AUTORIZACION' THEN 'Cliente'
	 		ELSE ''
	 	END	"CodigoDato1",
	 	CASE
	 		WHEN "Codigo" = 'CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO_AUTORIZACION' THEN CAST(M."NombrePersona" as NVARCHAR(520))
	 		WHEN "Codigo" = 'SALDO_CAJA_INSUFICIENTE_AUTORIZACION'  THEN CAST(M."NombrePersona" as NVARCHAR(520))
	 		ELSE ''
	 	END	"ValorDato1",
	 	'' as "CodigoDato2",'' as "ValorDato2"
	  FROM (
			 SELECT "NumeroReferencia" AS "IdInterno",
			 		COALESCE(			 			
			 			 "1" ||'CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO_AUTORIZACION'
						,"2" ||'SALDO_CAJA_INSUFICIENTE_AUTORIZACION'
						,"3" ||'ERROR_CONSULTA_FLUJO_CAJA'
			 		) AS "Codigo",
			 		COALESCE(			 			
			 			 "1" ||"CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO_AUTORIZACION"
						,"2" ||"SALDO_CAJA_INSUFICIENTE_AUTORIZACION"
						,"3" ||"ERROR_CONSULTA_FLUJO_CAJA"
			 		) AS "Resultado"
			   FROM :v_Validaciones_RV2,"BSCL.Plataforma.BD::PLA.Identidad") AS R
      INNER JOIN :v_MatrizValidaciones_v1 as M ON M."NumeroReferencia" = R."IdInterno"			   
	  WHERE "Resultado" IS NOT NULL;
 
	  
	 
	INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Salida_Resultado"
	 SELECT  DISTINCT R."NumeroReferencia" AS "IdInterno",R."Codigo",R."Resultado"
	 		,CASE WHEN "Codigo" = 'MEDIO_DE_PAGO_INVALIDO' THEN 'IdMedioDePago'
				  WHEN "Codigo" = 'MODALIDAD_DE_PAGO_INVALIDO' THEN 'IdModalidadDePago'
				  WHEN "Codigo" = 'MONEDA_NO_EXISTE' THEN 'IdMoneda'
				  WHEN "Codigo" = 'MONEDA_NO_ACTIVA' THEN 'IdMoneda'
				  WHEN "Codigo" = 'CUENTA_BANCARIA_CORREDORA_INVALIDA' THEN 'IdCuentaBanco'
				  WHEN "Codigo" = 'TARJETA_BANCARIA_CORREDORA_INVALIDA' THEN 'IdTarjetaBancaria'
				  WHEN "Codigo" = 'CUENTA_BANCARIA_SOCIO_INVALIDA' THEN 'IdCuentaBancaria'
				  WHEN "Codigo" = 'TARJETA_BANCARIA_SOCIO_INVALIDA' THEN 'IdTarjetaBancaria'				  
				  WHEN "Codigo" = 'TRANSFERENCIA_SOLO_ACEPTA_UN_MEDIO_DE_PAGO' THEN 'IdMedioDePago'
			  END "CodigoDato1"
			,CASE WHEN "Codigo" = 'MEDIO_DE_PAGO_INVALIDO' THEN M."IdMedioDePago"
				  WHEN "Codigo" = 'MODALIDAD_DE_PAGO_INVALIDO' THEN M."IdModalidadDePago"
				  WHEN "Codigo" = 'MONEDA_NO_EXISTE' THEN M."IdMoneda_Real"
				  WHEN "Codigo" = 'MONEDA_NO_ACTIVA' THEN M."IdEstado_MonedaReal"
				  WHEN "Codigo" = 'CUENTA_BANCARIA_CORREDORA_INVALIDA' THEN CAST(M."IdCuentaBancariaCorredora" as NVARCHAR(520))
				  WHEN "Codigo" = 'TARJETA_BANCARIA_CORREDORA_INVALIDA' THEN CAST(M."IdTarjetaBancariaCorredora"  as NVARCHAR(520))
				  WHEN "Codigo" = 'CUENTA_BANCARIA_SOCIO_INVALIDA' THEN CAST(M."IdCuentaBancariaSocio" as NVARCHAR(520))
				  WHEN "Codigo" = 'TARJETA_BANCARIA_SOCIO_INVALIDA' THEN CAST(M."IdTarjetaBancariaSocio" as NVARCHAR(520))
				  WHEN "Codigo" = 'TRANSFERENCIA_SOLO_ACEPTA_UN_MEDIO_DE_PAGO' THEN M."IdMedioDePago"
			  END "ValorDato1"
	 		,'' as "CodigoDato2", '' as "ValorDato2"
	   FROM (SELECT FV1."NumeroReferencia", FV1."IdMedioDePago",
			 		COALESCE(			 			
			 			 "1"  ||'MEDIO_DE_PAGO_INVALIDO'
						,"2"  ||'MODALIDAD_DE_PAGO_INVALIDO'
						,"3"  ||'MONEDA_NO_EXISTE'
						,"4"  ||'MONEDA_NO_ACTIVA'
						,"5"  ||'MONTO_MENOR_IGUAL_A_CERO'
						,"6"  ||'CUENTA_BANCARIA_CORREDORA_INVALIDA'
						,"7"  ||'TARJETA_BANCARIA_CORREDORA_INVALIDA'
						,"8"  ||'CUENTA_BANCARIA_SOCIO_INVALIDA'
						,"9"  ||'TARJETA_BANCARIA_SOCIO_INVALIDA'
						,"10" ||'TRANSFERENCIA_SOLO_ACEPTA_UN_MEDIO_DE_PAGO'
			 		) AS "Codigo",
			 		COALESCE(			 			
			 			 "1"  ||"MEDIO_DE_PAGO_INVALIDO"
						,"2"  ||"MODALIDAD_DE_PAGO_INVALIDO"
						,"3"  ||"MONEDA_NO_EXISTE"
						,"4"  ||"MONEDA_NO_ACTIVA"
						,"5"  ||"MONTO_MENOR_IGUAL_A_CERO"
						,"6"  ||"CUENTA_BANCARIA_CORREDORA_INVALIDA"
						,"7"  ||"TARJETA_BANCARIA_CORREDORA_INVALIDA"
						,"8"  ||"CUENTA_BANCARIA_SOCIO_INVALIDA"
						,"9"  ||"TARJETA_BANCARIA_SOCIO_INVALIDA"
						,"10" ||"TRANSFERENCIA_SOLO_ACEPTA_UN_MEDIO_DE_PAGO"
			 		) AS "Resultado"
			   FROM :v_Validaciones_FV1 AS FV1,"BSCL.Plataforma.BD::PLA.Identidad") as R
	  INNER JOIN :v_MatrizValidaciones_v2 as M ON (M."NumeroReferencia" = R."NumeroReferencia" AND M."IdMedioDePago" = R."IdMedioDePago")
	  WHERE R."Resultado" IS NOT NULL;
	  
END;
