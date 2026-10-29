SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::SaldoFlujoCajaFFMMV1";

CREATE PROCEDURE
--  
--  Proposito: Consultar el flujo de caja de n portafolios. 
--             Servicio sin validacion ya que estas se encuentran en el interior de los servicios que lo llaman. NO SE LLAMA DESDE UI
--  Autor: NRM - BCS 
--  Fecha Creacion: 09/09/2015  
-------------------------------------------
--  Proposito: Se incorpora al calculo de flujo caja:
--             - Parametria para el calculo
--             - Ordenes en transito(compra/venta)
--             - Asignaciones(compra/venta)
--             - Eventos de capital(corte cupon, sorteo de letras, dividendos)
--             - Movimientos a facturar(compra/venta)
--             - Ingresos/Egresos tesoreria sin certificar
--             - Cargos Cta. inversion sin certificar
--  Autor: JVILLAGRAN - BCS
--  Fecha Modificacion: 11/07/2016
-------------------------------------------
--  Autor: GAT-BLAC 
--  Descripcion: Se agregan Aportes y Rescateokass de FFMM en transito.
--  Fecha Modificacion: 12/08/2016
-------------------------------------------
--  Autor: NRM - BCS
--  Descripcion: Optimizacion de servicio e incorporación de salida detallada del flujo caja.
--  Fecha Modificacion: 24/08/2016
-------------------------------------------
"BSCL.Plataforma.BD.Servicios::SaldoFlujoCajaFFMMV1" (
	-- Parametros entrada
    IN  p_IdEmpresa                     BIGINT,
	IN  p_FechaDesde  					NVARCHAR(10),
    IN  p_FechaHasta  					NVARCHAR(10),
    IN  p_Uso                      		NVARCHAR(40) 	DEFAULT null,	-- Uso del flujo de caja.
	IN  p_DesplegarEnUI					NVARCHAR(1) 	DEFAULT 'N', -- N: Muestra todos los items del flujo; S: Muestra solo los items para UI
	IN	p_AgruparPorOrigen				NVARCHAR(1) 	DEFAULT 'N', -- S: agrupa por origen items; N: agrupa por items
    IN  p_PortafolioAConsultar			TABLE("IdPortafolio"				BIGINT,
    										  "IdMoneda"					NVARCHAR(3)) 	DEFAULT EMPTY,
	
    -- Parametros salida
    OUT p_out_FlujoCaja                 TABLE("NombreUso"					NVARCHAR(520),
    										  "FechaSaldo"					NVARCHAR(10), --Fecha liquidacion del saldo
    										  "IdPortafolio"				BIGINT,                                              
                                              "IdMoneda"					NVARCHAR(3), 
                                              "Saldo"						DECIMAL(26,8)) 	DEFAULT EMPTY,
                                              
	OUT p_out_FlujoCajaDetallado		TABLE("FechaSaldo"					NVARCHAR(10), --Fecha liquidacion del saldo
    										  "IdPortafolio"				BIGINT,
                                              "IdMoneda"					NVARCHAR(3),
                                              "IdUsoFlujoCaja"				BIGINT,
                                              "NombreUso"					NVARCHAR(520),
                                              "IdConfiguracionFlujoCaja"	BIGINT,
                                              "ItemCalculo"					NVARCHAR(100),
                                              "FuncionOperador"				NVARCHAR(1),
                                              "Monto"						DECIMAL(26,8))	DEFAULT EMPTY,
	
	-- Salida errores
	OUT p_out_ErroresContador           INT,
	OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))	DEFAULT EMPTY
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA
AS
BEGIN
	--------------------
	--Manejo de Errores 
	--------------------
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_errorExt	 				NVARCHAR(40);

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA
	BEGIN
		v_hash= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_hash,:v_erroroutmsg);		
		p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_erroroutmsg;		
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	p_out_ErroresContador		:= 0;
	
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
		DECLARE v_Contador  BIGINT;
		DECLARE v TABLE("FechaLiquidacion" TIMESTAMP,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"Monto" DECIMAL(26,8),"MontoCompra" DECIMAL(26,8),"MontoVenta" DECIMAL(26,8),"ItemCalculo" NVARCHAR(40));
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN			
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
		---------------------------------------------
		-- INFORMACION DE CONFIGURACION FLUJO CAJA
		---------------------------------------------
		v_ConfiguracionFlujoCaja = 
		 SELECT  "IdUsoFlujoCaja"
		 		,"NombreUso"
		 		,"ItemCalculo"
		 		,"ItemOrigen"
		 		,"FuncionOperador"
		 		,"DesplegarEnUI"
		   FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionFlujoCaja"
		  WHERE "IdEstadoConfiguracion" = 'ACT'
		    AND "IdEmpresa" = :p_IdEmpresa
		    AND :p_Uso IS NULL OR "NombreUso" = :p_Uso;
	
	
		v_ItemCalculo = 
		 SELECT DISTINCT "ItemCalculo" FROM :v_ConfiguracionFlujoCaja;
	
		---------------------------------------------
		-- INFORMACION BASE
		---------------------------------------------
		v_TablaBase =
		 SELECT  CLI."IdPersona"
		 		,POR."IdPortafolio"
		 		,MON."IdMoneda"
		   FROM :p_PortafolioAConsultar as PC
		  INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR 
		  		  ON POR."IdPortafolio" = PC."IdPortafolio"
		  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
		  		  ON CLI."IdCliente" = POR."IdCliente"
		  INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" as MON
		  		  ON (PC."IdMoneda" IS NULL OR MON."IdMoneda" = PC."IdMoneda")
		  GROUP BY CLI."IdPersona",POR."IdPortafolio",MON."IdMoneda";

		
		---------------------------------------------
		-- RANGO DE FECHAS
		---------------------------------------------
		v_RangoFecha = 
		 SELECT  TB."IdPersona"
		 		,TB."IdPortafolio"
		 		,TB."IdMoneda"
		 		,CFC."IdUsoFlujoCaja"
		 		,CFC."NombreUso"
		 		,CFC."ItemCalculo"
		 		,CFC."ItemOrigen"
		 		,CASE WHEN CFC."FuncionOperador" = '-' THEN -1 ELSE 1 END AS "FuncionOperador"
		 		,CFC."FuncionOperador" as "FuncionOperadorString"
		 		,CFC."DesplegarEnUI"
		 		,COALESCE(:p_Uso, CFC."NombreUso") as "UsoSolicitado"
				,GENERATED_PERIOD_START AS "Fecha"
		   FROM :v_TablaBase AS TB,:v_ConfiguracionFlujoCaja as CFC,
		   		SERIES_GENERATE_DATE('INTERVAL 1 DAY', :p_FechaDesde, ADD_DAYS(TO_DATE(:p_FechaHasta,'YYYY-MM-DD'), 1) ) as B
		  WHERE CFC."DesplegarEnUI" = CASE WHEN :p_DesplegarEnUI = 'S' THEN :p_DesplegarEnUI ELSE CFC."DesplegarEnUI" END;
		
		
		---------------------------------------------
		-- SALDO CUENTA INVERSION
		---------------------------------------------
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_IN_CuentaInversionAConsultar" 
		SELECT "IdPortafolio", "IdMoneda" FROM :v_TablaBase GROUP BY "IdPortafolio","IdMoneda";	
				 
		CALL "BSCL.CuentaInversion.BD.Servicios::SaldoCuentaInversion" (
			 p_IdEmpresa					=> :p_IdEmpresa
			,p_FechaDesde					=> :p_FechaDesde
		    ,p_FechaHasta					=> :p_FechaHasta
		    ,p_SoloSaldoNegativo			=> 'N'
		    ,p_IncluyeCargosPendientes		=> 'S'
		    ,p_IncluyeAbonosPendientes		=> 'N'
		);
		--SELECT 'v_SaldoPorFechaEfectiva',* FROM "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_OUT_SaldoPorFechaEfectiva"; --BORRAR

		---------------------------------------------------
		-- SALDO TESORERIA (Sin Liquidar)
		---------------------------------------------------		
		v_SociosAConsultar = 
		 SELECT  "IdPersona"
  				,"IdPortafolio"
  				,"IdMoneda"
  		   FROM :v_TablaBase
  		  GROUP BY "IdPersona","IdPortafolio","IdMoneda";
			  		
		CALL "BSCL.Tesoreria.BD.Servicios::SaldoPendienteDeLiquidar" ( 
			 p_IdEmpresa 					=> p_IdEmpresa
			,p_SociosAConsultar 			=> :v_SociosAConsultar
			,p_FechaDesde					=> :p_FechaDesde
		    ,p_FechaHasta					=> :p_FechaHasta
		    ,p_out_SaldoPorFechaLiquidacion	=> :v_SaldoPorFechaLiquidacion
			,p_out_ErroresContador			=> v_Contador
			,p_out_ErroresDetalle			=> v_ErroresDetalle
		);
		--SELECT ':v_SaldoPorFechaLiquidacion', * FROM :v_SaldoPorFechaLiquidacion;	--BORRAR

		---------------------------------------------------
		-- SALDO ORDENES EN TRANSITO
		---------------------------------------------------
		v = 
		WITH ORD as (
				SELECT CASE WHEN ORD."FechaLiquidacion" >= :p_FechaHasta THEN ORD."FechaLiquidacion" ELSE :p_FechaDesde END AS "FechaLiquidacion"
					  ,ORD."IdPortafolio"
					  ,ORD."IdMonedaLiquidacion" as "IdMoneda"
					  ,ORD."Accion"
					  ,0 as "Monto"					  
					  ,CASE WHEN ORD."Accion" = 'COMPRA' THEN ORD."MontoTotalOrdenMonedaLiquidacion" ELSE 0 END as "MontoCompra"
					  ,CASE WHEN ORD."Accion" = 'VENTA' THEN ORD."MontoTotalOrdenMonedaLiquidacion" ELSE 0 END as "MontoVenta"
				  FROM :v_TablaBase AS TB
				 INNER JOIN "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenIngresadaFlujoCaja" ORD 
				 		 ON (TB."IdPortafolio" = ORD."IdPortafolio" AND TB."IdMoneda" = ORD."IdMonedaLiquidacion"),
				 	   :v_ItemCalculo as IC
				 WHERE ORD."FechaLiquidacion" <= :p_FechaHasta
				   AND ORD."IdEmpresa" = :p_IdEmpresa
				   AND IC."ItemCalculo" = 'ORDENES_EN_TRANSITO_COMPRAS'
				UNION ALL
				SELECT (CASE WHEN ORD."FechaLiquidacion" >= :p_FechaHasta THEN ORD."FechaLiquidacion" ELSE :p_FechaDesde END) AS "FechaLiquidacion"
					  ,ORD."IdPortafolio"
					  ,ORD."IdMonedaLiquidacion" as "IdMoneda"
					  ,ORD."Accion"
					  ,0 as "Monto"
					  ,CASE WHEN ORD."Accion" = 'COMPRA' THEN ORD."MontoTotalOrdenMonedaLiquidacion" ELSE 0 END as "MontoCompra"
					  ,CASE WHEN ORD."Accion" = 'VENTA' THEN ORD."MontoTotalOrdenMonedaLiquidacion" ELSE 0 END as "MontoVenta"
				  FROM :v_TablaBase AS TB
				 INNER JOIN "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenPendienteAutorizacionFlujoCaja" ORD 
				 		 ON (TB."IdPortafolio" = ORD."IdPortafolio" AND TB."IdMoneda" = ORD."IdMonedaLiquidacion"),
				 		:v_ItemCalculo as IC
				 WHERE ORD."FechaLiquidacion" <= :p_FechaHasta
				   AND ORD."IdEmpresa" = :p_IdEmpresa
				   AND IC."ItemCalculo" = 'ORDENES_EN_TRANSITO_VENTAS'
					)
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta",
				CASE WHEN "Accion" = 'COMPRA' THEN 'ORDENES_EN_TRANSITO_COMPRAS'
					 WHEN "Accion" = 'VENTA' THEN 'ORDENES_EN_TRANSITO_VENTAS'
				 END "ItemCalculo"
		FROM ORD
		GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","Accion";

		INSERT INTO "BSCL.Plataforma.BD::PLA.SaldoFlujoCaja_DatosFlujoCaja"
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda","Monto","MontoCompra","MontoVenta","ItemCalculo" FROM :V;

		---------------------------------------------------
		-- SALDO ASIGNACIONES EN TRANSITO
		---------------------------------------------------
		v = 
		WITH ASIG as (
				SELECT (CASE WHEN ASIG."FechaLiquidacion" >= :p_FechaHasta THEN ASIG."FechaLiquidacion" ELSE :p_FechaDesde END) AS "FechaLiquidacion"
					  ,ASIG."IdPortafolio"
					  ,ASIG."IdMonedaLiquidacion" as "IdMoneda"
					  ,0 as "Monto"
					  ,CASE WHEN ASIG."Accion" = 'COMPRA' THEN ASIG."MontoTotalAsignadoMonedaLiquidacion" ELSE 0 END "MontoCompra"
					  ,CASE WHEN ASIG."Accion" = 'VENTA' THEN ASIG."MontoTotalAsignadoMonedaLiquidacion" ELSE 0 END "MontoVenta"
					  ,IC."ItemCalculo"	
				  FROM :v_TablaBase AS TB 
				 INNER JOIN "BSCL.Orden.BD.Vistas::Flujos.ConsultarAsignacionNoEnviadaFacturarFlujoCaja" ASIG 
				 		 ON (TB."IdPortafolio" = ASIG."IdPortafolio" AND TB."IdMoneda" = ASIG."IdMonedaLiquidacion"),
				 		:v_ItemCalculo as IC
				 WHERE ASIG."IdEmpresa" = :p_IdEmpresa
				   AND ASIG."FechaLiquidacion" <= :p_FechaHasta
				   AND IC."ItemCalculo" = CASE WHEN ASIG."Accion" = 'COMPRA' THEN 'ASIGNACIONES_COMPRAS'
										  	   WHEN ASIG."Accion" = 'VENTA' THEN 'ASIGNACIONES_VENTAS'
										   END	
				   )
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta","ItemCalculo"
		FROM ASIG GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.SaldoFlujoCaja_DatosFlujoCaja"
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda","Monto","MontoCompra","MontoVenta","ItemCalculo" FROM :V;

		---------------------------------------------------
		-- SALDO MOVIMIENTOS FACTURACION
		---------------------------------------------------	
		v = 
		WITH FAC as (
				 SELECT  (CASE WHEN MC."FechaLiquidacion" >= :p_FechaHasta THEN MC."FechaLiquidacion" ELSE :p_FechaDesde END) AS "FechaLiquidacion"
						,MC."IdPortafolio"
				   		,MC."IdMonedaLiquidacion" as "IdMoneda"
				   		,0 as "Monto"
						,CASE WHEN MC."AccionTipoOperacion" = 'COMPRA' THEN MC."MontoFinal" ELSE 0 END "MontoCompra"
						,CASE WHEN MC."AccionTipoOperacion" = 'VENTA' THEN MC."MontoFinal" ELSE 0 END "MontoVenta"
						,IC."ItemCalculo"	  
				   FROM :v_TablaBase AS TB
				  INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCaja" AS MC 
				  		  ON (TB."IdPortafolio" = MC."IdPortafolio" AND TB."IdMoneda" = MC."IdMonedaLiquidacion"),
				  		 :v_ItemCalculo as IC
				  WHERE MC."IdEstadoFacturacion" = 'ING'
				    AND MC."IdEmpresa" = :p_IdEmpresa
				    AND MC."FechaLiquidacion" <= :p_FechaHasta
				    AND IC."ItemCalculo" = CASE WHEN MC."AccionTipoOperacion" = 'COMPRA' THEN 'MOVIMIENTOS_FACTURAR_COMPRAS'
							  					WHEN MC."AccionTipoOperacion" = 'VENTA' THEN 'MOVIMIENTOS_FACTURAR_VENTAS'
										    END			  
		  	   )
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta","ItemCalculo"
		FROM FAC GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";
		 
		INSERT INTO "BSCL.Plataforma.BD::PLA.SaldoFlujoCaja_DatosFlujoCaja"
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda","Monto","MontoCompra","MontoVenta","ItemCalculo" FROM :V;

		---------------------------------------------------
		-- SALDO FONDOS MUTUOS
		---------------------------------------------------
		v = 
		WITH FFMM as (
		  		SELECT  (CASE WHEN SOL."FechaLiquidacion" >= :p_FechaHasta THEN SOL."FechaLiquidacion" ELSE :p_FechaDesde END) AS "FechaLiquidacion"
		  		        ,SOL."IdPortafolio"
						,SOL."IdMoneda"
						,0 as "Monto"
						,SOL."CostoMonedaLocal" AS "MontoAporte"
						,0 AS "MontoRescate"
						,IC."ItemCalculo"		
				  FROM :v_TablaBase AS TB
				  INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCaja" AS SOL 
				  		  ON (TB."IdPortafolio" = SOL."IdPortafolio" AND TB."IdMoneda" = SOL."IdMoneda")
				  LEFT OUTER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarPorComprobanteOrigenLiquidado" INGRESO_EGRESO 
				  		  ON (SOL."IdSolicitudAporteRescate" = INGRESO_EGRESO."IdComprobanteOrigen"),
				  		  :v_ItemCalculo as IC			
				  WHERE SOL."EstadoSolicitud" = 'ING'
				    AND SOL."TipoSolicitud" = 'APO'
				    AND SOL."IdTipoMovimientoFondoMutuo" IN ('APONORM','APOOVER','APORESPRO')
				    AND SOL."IdMedioDePago" = 'CIN'
				    AND COALESCE(INGRESO_EGRESO."IdTipoComprobanteOrigen",'FMM') = 'FMM'
				    AND COALESCE(INGRESO_EGRESO."IdEstado",'') != 'LIQ'
				    AND SOL."IdEmpresa" = :p_IdEmpresa
				    AND SOL."FechaLiquidacion" <= :p_FechaHasta
				    AND IC."ItemCalculo" = 'APORTES_EN_TRANSITO'
				UNION ALL
				SELECT  (CASE WHEN SOL."FechaLiquidacion" >= :p_FechaHasta THEN SOL."FechaLiquidacion" ELSE :p_FechaDesde END) AS "FechaLiquidacion"
		  		        ,SOL."IdPortafolio"
						,SOL."IdMoneda"
						,0 as "Monto"
						,SOL."CostoMonedaLocal" AS "MontoAporte"
						,0 AS "MontoRescate"
						,IC."ItemCalculo"		
				  FROM :v_TablaBase AS TB
				  INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCaja" AS SOL 
				  		  ON (TB."IdPortafolio" = SOL."IdPortafolio" AND TB."IdMoneda" = SOL."IdMoneda")
				  INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarPorComprobanteOrigenLiquidado" INGRESO_EGRESO 
				  		  ON (SOL."IdSolicitudAporteRescate" = INGRESO_EGRESO."IdComprobanteOrigen"),
				  		  :v_ItemCalculo as IC
				  WHERE	SOL."EstadoSolicitud" = 'ING'
				    AND SOL."TipoSolicitud" = 'APO'
				    AND SOL."IdTipoMovimientoFondoMutuo" IN ('APONORM','APOOVER','APORESPRO')
				    AND SOL."IdMedioDePago" != 'CIN'
				    AND INGRESO_EGRESO."IdTipoComprobanteOrigen" = 'FMM'
				    AND INGRESO_EGRESO."IdEstado" = 'LIQ'
				    AND SOL."IdEmpresa" = :p_IdEmpresa
				    AND SOL."FechaLiquidacion" <= :p_FechaHasta
				    AND IC."ItemCalculo" = 'APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA'
				UNION ALL
				SELECT  (CASE WHEN SOL."FechaLiquidacion" >= :p_FechaHasta THEN SOL."FechaLiquidacion" ELSE :p_FechaDesde END) AS "FechaLiquidacion"
		  		        ,SOL."IdPortafolio"
						,SOL."IdMoneda"
						,0 as "Monto"
						,0 AS "MontoAporte"
						,SOL."CostoMonedaLocal" AS "MontoRescate"
						,IC."ItemCalculo"		
				  FROM :v_TablaBase AS TB
				  INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCaja" AS SOL 
				  		  ON (TB."IdPortafolio" = SOL."IdPortafolio" AND TB."IdMoneda" = SOL."IdMoneda"),
				  		  :v_ItemCalculo as IC			
				  WHERE SOL."EstadoSolicitud" = 'CRT'
				    AND SOL."TipoSolicitud" = 'RES'
				    AND SOL."IdTipoMovimientoFondoMutuo" = 'RESNORM'
				    AND SOL."IdMedioDePago" = 'CIN'
				    AND SOL."AdministracionLiquidez" = 'N'
				    AND SOL."IdEmpresa" = :p_IdEmpresa
				    AND SOL."FechaLiquidacion" > :p_FechaHasta
				    AND IC."ItemCalculo" = 'RESCATES_EN_TRANSITO'
				UNION ALL 
				SELECT  (CASE WHEN SOL."FechaLiquidacion" >= :p_FechaHasta THEN SOL."FechaLiquidacion" ELSE :p_FechaDesde END) AS "FechaLiquidacion"
		  		        ,SOL."IdPortafolio"
						,SOL."IdMoneda"
						,0 as "Monto"
						,0 AS "MontoAporte"
						,SOL."CostoMonedaLocal" AS "MontoRescate"
						,IC."ItemCalculo"		
				  FROM :v_TablaBase AS TB
				  INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCaja" AS SOL 
				  		  ON (TB."IdPortafolio" = SOL."IdPortafolio" AND TB."IdMoneda" = SOL."IdMoneda"),
				  		  :v_ItemCalculo as IC			
				  WHERE SOL."EstadoSolicitud" = 'CRT'
				    AND SOL."TipoSolicitud" = 'RES'
				    AND SOL."IdTipoMovimientoFondoMutuo" = 'RESNORM'
				    AND SOL."IdMedioDePago" = 'CIN'
				    AND SOL."AdministracionLiquidez" = 'S'
				    AND SOL."IdEmpresa" = :p_IdEmpresa
				    AND SOL."FechaLiquidacion" > :p_FechaHasta
				    AND IC."ItemCalculo" = 'INVERSION_EN_LIQUIDEZ'
		  	   )
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoAporte") as "MontoCompra",SUM("MontoRescate") as "MontoVenta","ItemCalculo"
		FROM FFMM GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.SaldoFlujoCaja_DatosFlujoCaja"
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda","Monto","MontoCompra","MontoVenta","ItemCalculo" FROM :V;
		
		---------------------------------------------------
		-- SALDO DIVIDENDOS / CORTE CUPON / SORTEO LETRAS
		---------------------------------------------------
		v = 
		WITH EVC as (
		  		SELECT   (CASE WHEN EVC."FechaPago" >= :p_FechaHasta THEN EVC."FechaPago" ELSE :p_FechaDesde END) AS "FechaLiquidacion"
						,EVC."IdPortafolio"
						,EVC."IdMoneda"
						,EVC."Total" as "Monto"
						,0 as "MontoCompra"
						,0 as "MontoVenta"
						,IC."ItemCalculo"
				  FROM :v_TablaBase AS TB
				 INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCaja" AS EVC
					     ON (TB."IdPortafolio" = EVC."IdPortafolio" AND TB."IdMoneda" = EVC."IdMoneda"),
				  		:v_ItemCalculo as IC
				 WHERE EVC."IdTipoEventoCapital" IN ('DV','CC','SL')
				   AND EVC."IdEstado" IN ('ING','VIG','NOT')  -- Considera todos los estados intermedios(Cerrado es estado final)
				   AND EVC."IdEmpresa" = :p_IdEmpresa
				   AND EVC."FechaPago" <= :p_FechaHasta
				   AND IC."ItemCalculo" =  CASE WHEN EVC."IdTipoEventoCapital" = 'DV' THEN 'DIVIDENDOS_EN_DINERO'
							  					WHEN EVC."IdTipoEventoCapital" = 'CC' THEN 'CORTE_CUPON'
							 	 				WHEN EVC."IdTipoEventoCapital" = 'SL' THEN 'SORTEO_DE_LETRAS'
										    END	
		       )
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta","ItemCalculo"
		FROM EVC GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.SaldoFlujoCaja_DatosFlujoCaja"
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda","Monto","MontoCompra","MontoVenta","ItemCalculo" FROM :V;


		---------------------------------------------------
		-- SALDO SIMULTANEAS / EMPRESTITOS / PRESTAMOS
		---------------------------------------------------
		v = 
		WITH SMEP as (
				 SELECT	 (CASE WHEN CAR."FechaLiquidacion" >= :p_FechaHasta THEN CAR."FechaLiquidacion" ELSE :p_FechaDesde END) AS "FechaLiquidacion"
						,CAR."IdPortafolio"
						,CAR."IdMonedaSistema" AS "IdMoneda"
						,CASE WHEN CAR."IdTipoInstrumento" = 'EMP' OR CAR."IdTipoInstrumento" = 'PRT' THEN CAR."INGMontoATasaCompraActual" ELSE 0 END AS "Monto"
						,CASE WHEN CAR."IdTipoOperacion" = 'COMSIM' THEN CAR."INGMontoATasaCompraActual" ELSE 0 END "MontoCompra"				
						,CASE WHEN CAR."IdTipoOperacion" = 'VENSIM' THEN CAR."INGMontoATasaCompraActual" ELSE 0 END "MontoVenta"					
						,IC."ItemCalculo"
				   FROM :v_TablaBase AS TB
				  INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraFlujoCaja" AS CAR 
				  		  ON (CAR."IdPortafolio" = TB."IdPortafolio" AND CAR."IdMonedaSistema" = TB."IdMoneda"),
				  		:v_ItemCalculo as IC
				  WHERE CAR."IdTipoInstrumento" IN ('SIM','EMP','PRT')
				    AND CAR."IdEmpresa" = :p_IdEmpresa
				    AND CAR."IdEstado"  = 'VIG'   -- Solo Vigentes
				    AND CAR."FechaLiquidacion" <= :p_FechaHasta
				    AND IC."ItemCalculo" = 	 CASE WHEN CAR."IdTipoOperacion" = 'COMSIM' THEN 'SIMULTANEA_COMPRAS'
												  WHEN CAR."IdTipoOperacion" = 'VENSIM' THEN 'SIMULTANEA_VENTAS'
												  WHEN CAR."IdTipoInstrumento" = 'EMP' THEN 'EMPRESTITOS'
												  WHEN CAR."IdTipoInstrumento" = 'PRT' THEN 'PRESTAMOS'
											  END
		       )
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta","ItemCalculo"
		FROM SMEP GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.SaldoFlujoCaja_DatosFlujoCaja"
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda","Monto","MontoCompra","MontoVenta","ItemCalculo" FROM :V;
		  
		  
		---------------------------------------------------
		-- MESAS
		---------------------------------------------------
		v = 
		WITH SMES as (
				 SELECT	 (CASE WHEN MES."FechaLiquidacion" >= :p_FechaHasta THEN MES."FechaLiquidacion" ELSE :p_FechaDesde END) AS "FechaLiquidacion"
						,MES."IdPortafolio"
						,MES."IdMonedaLiquidacion" AS "IdMoneda"
						,0 AS "Monto"
						,CASE WHEN instr(MES."IdTipoOperacion",'COM') = 1 THEN MES."MontoOperacion" ELSE 0 END "MontoCompra"
						,CASE WHEN instr(MES."IdTipoOperacion",'VEN') = 1 THEN MES."MontoOperacion" ELSE 0 END "MontoVenta"					
						,IC."ItemCalculo"
				   FROM :v_TablaBase AS TB
				  INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperaciones" AS MES 
				  		  ON (MES."IdPortafolio" = TB."IdPortafolio" AND MES."IdMonedaLiquidacion" = TB."IdMoneda"),
				  		:v_ItemCalculo as IC
				  WHERE MES."IdEmpresa" = :p_IdEmpresa
				    AND MES."IdEstado"  = 'PEN'
				    AND MES."FechaLiquidacion" <= :p_FechaHasta
				    AND IC."ItemCalculo" = 	 CASE WHEN MES."IdTipoOperacion" = 'COMPAC' THEN 'COMPRA_PACTO'
												  WHEN MES."IdTipoOperacion" = 'COMTER' THEN 'COMPRA_TERMINO'
												  WHEN MES."IdTipoOperacion" = 'COMFWD' THEN 'COMPRA_FORWARD'												  
												  WHEN MES."IdTipoOperacion" = 'VENPAC' THEN 'VENTA_PACTO'
												  WHEN MES."IdTipoOperacion" = 'VENTER' THEN 'VENTA_TERMINO'
												  WHEN MES."IdTipoOperacion" = 'VENFWD' THEN 'VENTA_FORWARD'
											  END
		       )
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta","ItemCalculo"
		FROM SMES GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.SaldoFlujoCaja_DatosFlujoCaja"
		SELECT "FechaLiquidacion","IdPortafolio","IdMoneda","Monto","MontoCompra","MontoVenta","ItemCalculo" FROM :V;		  
		  
		
		--SELECT 'SaldoFlujoCaja_DatosFlujoCaja',* FROM "BSCL.Plataforma.BD::PLA.SaldoFlujoCaja_DatosFlujoCaja"; --BORRAR
		---------------------------------------------------
		-- OBTENER FLUJO CAJA
		---------------------------------------------------	
		p_out_FlujoCajaDetallado =
		WITH
		RangoFecha as (
				SELECT   RF."Fecha"
				 		,RF."IdPortafolio"
						,RF."IdMoneda"
						,RF."IdUsoFlujoCaja"
						,RF."NombreUso"
						,CASE WHEN :p_AgruparPorOrigen = 'N' THEN RF."ItemCalculo" ELSE RF."ItemOrigen" END as "Item"
						,RF."ItemCalculo"
						,RF."ItemOrigen"
						,RF."FuncionOperadorString" as "FuncionOperador"
				  FROM :v_RangoFecha as RF
		),
		SaldoCuentaInversion as (
				 SELECT  RF."IdPortafolio"
						,RF."IdMoneda"
						,RF."IdUsoFlujoCaja"
						,RF."ItemCalculo"
						,RF."FuncionOperadorString" as "FuncionOperador"
						,CAST(SCTA."FechaSaldo" as date) as "FechaSaldo"
						,CASE WHEN RF."ItemCalculo" = 'SALDO_CUENTA_INVERSION' THEN RF."FuncionOperador" * SCTA."SaldoDisponible"
							  WHEN RF."ItemCalculo" = 'CARGOS_CUENTA_INVERSION' THEN RF."FuncionOperador" * SCTA."SaldoCargosPendientes"
							  ELSE null END "Monto"
				   FROM :v_RangoFecha as RF
				  INNER JOIN "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_OUT_SaldoPorFechaEfectiva" as SCTA
				 		  ON SCTA."IdPortafolio" 	= RF."IdPortafolio"
				 		 AND SCTA."IdMoneda" 		= RF."IdMoneda"
				 		 AND SCTA."FechaSaldo"		= RF."Fecha"
				  WHERE RF."NombreUso" = RF."UsoSolicitado"
				    AND RF."ItemCalculo" IN ('SALDO_CUENTA_INVERSION','CARGOS_CUENTA_INVERSION')
		),
		SaldoTesoreria as (		
				 SELECT  RF."IdPortafolio"
						,RF."IdMoneda"
						,RF."IdUsoFlujoCaja"
						,RF."ItemCalculo"
						,RF."FuncionOperadorString" as "FuncionOperador"
						,CAST(STES."FechaSaldo" as date) as "FechaSaldo"
						,CASE WHEN RF."ItemCalculo" = 'INGRESOS_TESORERIA' THEN RF."FuncionOperador" * STES."SaldoMovIng"
							  WHEN RF."ItemCalculo" = 'EGRESOS_TESORERIA' THEN RF."FuncionOperador" * STES."SaldoMovEgr"  
							  ELSE null END "Monto"
				   FROM :v_RangoFecha as RF		   	
				  INNER JOIN :v_SaldoPorFechaLiquidacion as STES
				 		  ON STES."IdPersona" 		= RF."IdPersona"
				 		 AND STES."IdPortafolio"	= RF."IdPortafolio"
				 		 AND STES."IdMoneda" 		= RF."IdMoneda"
				  WHERE RF."NombreUso" = RF."UsoSolicitado"
				    AND RF."ItemCalculo" IN ('INGRESOS_TESORERIA','EGRESOS_TESORERIA')
		),
		SaldosOperacionesEnTransito as (
				 SELECT  RF."IdPortafolio"
						,RF."IdMoneda"
						,RF."IdUsoFlujoCaja"
						,RF."ItemCalculo"
						,RF."FuncionOperadorString" as "FuncionOperador"
						,RF."Fecha" as "FechaSaldo"
						,CASE
							--------ORDEN
							  WHEN RF."ItemCalculo" = 'ORDENES_EN_TRANSITO_COMPRAS' THEN
					    		   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoCompra") 
					    		   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'ORDENES_EN_TRANSITO_VENTAS' THEN
							  	   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoVenta") 
							  	   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
		
							--------ASIGNACION
							  WHEN RF."ItemCalculo" = 'ASIGNACIONES_COMPRAS' THEN
					    		   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoCompra") 
					    		   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
																	ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'ASIGNACIONES_VENTAS' THEN
							  	   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoVenta") 
							  	   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
																	ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)					  	   
							  	   
							--------FACTURACION
							  WHEN RF."ItemCalculo" = 'MOVIMIENTOS_FACTURAR_COMPRAS' THEN
					    		   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoCompra") 
					    		   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'MOVIMIENTOS_FACTURAR_VENTAS' THEN
							  	   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoVenta") 
							  	   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							
							--------FONDOS MUTUOS
							  WHEN RF."ItemCalculo" = 'APORTES_EN_TRANSITO' THEN
					    		   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoCompra") 
					    		   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'RESCATES_EN_TRANSITO' THEN
							  	   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoVenta") 
							  	   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  	   
							--------SALDO DIVIDENDOS / CORTE CUPON / SORTEO LETRAS
							  WHEN RF."ItemCalculo" = 'DIVIDENDOS_EN_DINERO' THEN
					    		   RF."FuncionOperador" * COALESCE(SUM(DFC."Monto") 
					    		   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'CORTE_CUPON' THEN
							  	   RF."FuncionOperador" * COALESCE(SUM(DFC."Monto") 
							  	   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'SORTEO_DE_LETRAS' THEN
							  	   RF."FuncionOperador" * COALESCE(SUM(DFC."Monto") 
							  	   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
								
							--------SALDO SIMULTANEAS / EMPRESTITOS / PRESTAMOS						
							  WHEN RF."ItemCalculo" = 'SIMULTANEA_COMPRAS' THEN
					    		   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoCompra") 
					    		   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'SIMULTANEA_VENTAS' THEN
							  	   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoVenta") 
							  	   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'EMPRESTITOS' THEN
							  	   RF."FuncionOperador" * COALESCE(SUM(DFC."Monto") 
							  	   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'EMPRESTITOS' THEN
							  	   RF."FuncionOperador" * COALESCE(SUM(DFC."Monto") 
							  	   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							
							--------MESAS
							  WHEN RF."ItemCalculo" = 'COMPRA_PACTO' THEN
					    		   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoCompra") 
					    		   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'COMPRA_TERMINO' THEN
					    		   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoCompra") 
					    		   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'COMPRA_FORWARD' THEN
					    		   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoCompra") 
					    		   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'VENTA_PACTO' THEN
					    		   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoCompra") 
					    		   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'VENTA_TERMINO' THEN
					    		   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoCompra") 
					    		   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
							  WHEN RF."ItemCalculo" = 'VENTA_FORWARD' THEN
					    		   RF."FuncionOperador" * COALESCE(SUM(DFC."MontoCompra") 
					    		   								Over (PARTITION BY RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."ItemCalculo"
															         ORDER BY RF."Fecha" ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0)
															         		 																									         		 
							--------SIN COINCIDENCIA
							  ELSE null 
						  END "Monto"
			      FROM :v_RangoFecha as RF
			      LEFT JOIN "BSCL.Plataforma.BD::PLA.SaldoFlujoCaja_DatosFlujoCaja" as DFC
			      		 ON RF."IdPortafolio" = DFC."IdPortafolio" 
						AND RF."IdMoneda"     = DFC."IdMoneda"
						AND RF."Fecha"        = DFC."FechaLiquidacion"
						AND RF."ItemCalculo"  = DFC."ItemCalculo"
				 WHERE RF."NombreUso" = RF."UsoSolicitado"
				 ORDER BY RF."IdUsoFlujoCaja"
		)
		 SELECT  RF."Fecha" as "FechaSaldo"
		 		,RF."IdPortafolio"
				,RF."IdMoneda"
				,RF."IdUsoFlujoCaja"
				,RF."NombreUso"
				,0 as "IdConfiguracionFlujoCaja"
				,RF."Item" as "ItemCalculo"
				,RF."FuncionOperador"
				,CAST(SUM(COALESCE(CTA."Monto",TES."Monto",SOT."Monto",0)) as DECIMAL(26,8)) as "Monto"
		  FROM RangoFecha as RF
		  LEFT JOIN SaldoCuentaInversion as CTA		  
		  		  ON CTA."FechaSaldo"				= RF."Fecha"
		 		 AND CTA."IdPortafolio"				= RF."IdPortafolio"
				 AND CTA."IdMoneda"					= RF."IdMoneda"
				 AND CTA."IdUsoFlujoCaja"			= RF."IdUsoFlujoCaja"
				 AND CTA."ItemCalculo"				= RF."ItemCalculo"
		  LEFT JOIN SaldoTesoreria as TES
		  		  ON TES."FechaSaldo"				= RF."Fecha"
		 		 AND TES."IdPortafolio"				= RF."IdPortafolio"
				 AND TES."IdMoneda"					= RF."IdMoneda"
				 AND TES."IdUsoFlujoCaja"			= RF."IdUsoFlujoCaja"
				 AND TES."ItemCalculo"				= RF."ItemCalculo"
		  LEFT JOIN SaldosOperacionesEnTransito as SOT
		  		  ON SOT."FechaSaldo"				= RF."Fecha"
		 		 AND SOT."IdPortafolio"				= RF."IdPortafolio"
				 AND SOT."IdMoneda"					= RF."IdMoneda"
				 AND SOT."IdUsoFlujoCaja"			= RF."IdUsoFlujoCaja"
				 AND SOT."ItemCalculo"				= RF."ItemCalculo"		  
		  GROUP BY RF."Fecha",RF."IdPortafolio",RF."IdMoneda",RF."IdUsoFlujoCaja",RF."NombreUso",RF."Item",RF."FuncionOperador"
		  ORDER BY RF."IdUsoFlujoCaja";
		  	

		p_out_FlujoCaja =
		 SELECT  "NombreUso"
		 		,"FechaSaldo"
		 		,"IdPortafolio"
		 		,"IdMoneda"
		 		,CAST(SUM("Monto") as DECIMAL(26,8)) as "Saldo"
		   FROM :p_out_FlujoCajaDetallado
		  GROUP BY "NombreUso","FechaSaldo","IdPortafolio","IdMoneda";
		  
	END;
END;
