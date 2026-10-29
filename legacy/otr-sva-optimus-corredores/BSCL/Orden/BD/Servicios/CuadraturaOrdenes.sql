SET SCHEMA "BSCL";  
go
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CuadraturaOrdenes";
go
--  
--  Proposito: Servicio para la cuadratura de ordenes.
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 22/03/2016
--  
CREATE PROCEDURE "BSCL.Orden.BD.Servicios::CuadraturaOrdenes" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada para la creacion del usuario
	IN  p_FechaInicio				NVARCHAR(10),  		

	-- Parametros salida del servicio publico
	OUT p_out_resumen		TABLE (
								"Titulo" 		NVARCHAR(100),
								"Resultado" 	NVARCHAR(10), 
								"Total" 		BIGINT, 
								"Diferencias" 	BIGINT, 
								"Tipo" 			NVARCHAR(50), 
								"Orden" 		BIGINT
	), 
	OUT p_out_transacciones TABLE (
									"IdClasificacionTipoInstrumento" 	NVARCHAR(10), 
									"FechaOperacionTransaccion" 		SECONDDATE,
									"FechaVencimiento" 					DATE, 
									"FolioTransaccion"					NVARCHAR(40),     
									"IdBolsa" 							NVARCHAR(3),     
									"AccionOrden" 						NVARCHAR(50),
									"Nemotecnico" 						NVARCHAR(40),
									"CodigoOperadorCorredor" 			NVARCHAR(40),
									"CantidadTransaccion" 				BIGINT,
									"CantidadAsignadaTransaccion" 		BIGINT,
									"MontoTransaccion" 					BIGINT,
									"MontoAsignadoTransaccion" 			BIGINT,
									"IdMonedaTransaccion" 				NVARCHAR(3),
									"EsFungible" 						NVARCHAR(1),
									"Descripcion" 						NVARCHAR(3),
									"IdTipoInstrumento" 				NVARCHAR(3),
									"ValorRescateTransaccion" 			BIGINT,
									"ValorRescateAsignadoTransaccion" 	BIGINT),
	OUT p_out_ordenes TABLE(
									"IdClasificacionTipoInstrumento"	NVARCHAR(2), 
									"IdTipoInstrumento"					NVARCHAR(3), 
									"IdOrden" 							BIGINT, 
									"FechaOperacionOrden" 				SECONDDATE, 
									"FechaVigencia" 					DATE, 
									"FechaVencimiento" 					DATE, 
									"IdEstadoOrden" 					NVARCHAR(3), 
									"CantidadOrden" 					BIGINT, 
									"MontoOrden" 						BIGINT, 
									"IdMonedaOrden" 					NVARCHAR(3), 
									"Tasa" 								BIGINT, 
									"IdCliente" 						BIGINT, 
									"DocumentoIdentidad" 				NVARCHAR(50), 
									"NombreCompleto" 					NVARCHAR(300), 
									"InstrumentoUI" 					NVARCHAR(50), 
									"CantidadAsignadaOrden" 			BIGINT, 
									"MontoAsignadoOrden" 				BIGINT, 
									"Descripcion" 						NVARCHAR(100), 
									"OrdenVigente" 						NVARCHAR(3)),
	OUT p_out_facturacion TABLE (
									"IdMovimientoComprobante" 			BIGINT, 
									"EstadoFacturacion" 				NVARCHAR(3), 
									"NombreEstadoFacturacion" 			NVARCHAR(50), 
									"IdAsignacion" 						BIGINT, 
									"FolioTransaccion" 					NVARCHAR(40), 
									"FechaAsignacion" 					SECONDDATE, 
									"IdEstadoAsignacion" 				NVARCHAR(3), 
									"EstadoAsignacion" 					NVARCHAR(50), 
									"AccionOrden"  						NVARCHAR(100), 
									"NombreTipoOperacion" 				NVARCHAR(100), 
									"InstrumentoUI" 					NVARCHAR(40), 
									"IdBolsa" 							NVARCHAR(3), 
									"NombreBolsa" 						NVARCHAR(150), 
									"IdTipoInstrumento" 				NVARCHAR(20), 
									"NombreTipoInstrumento" 			NVARCHAR(100), 
									"IdClasificacionTipoInstrumento" 	NVARCHAR(5), 
									"IdCliente" 						BIGINT, 
									"DocumentoIdentidad" 				NVARCHAR(50), 
									"NombreCompleto" 					NVARCHAR(300), 
									"Descripcion" 						NVARCHAR(100)),
	OUT p_out_cuadratura_cavali TABLE (
									"NumeroOperacion"		 			BIGINT, 
									"FechaOperacion" 				    DATE, 
									"FolioTransaccion" 			        BIGINT,
									"CantidadOperacion"                 DECIMAL(26,8),
									"MontoEfectivo"						DECIMAL(26,8),
									"DescripcionNegocio"				NVARCHAR(100),
									"PrecioOperacion"					DECIMAL(26,8)),
	OUT p_out_ordenes_vencer TABLE ("Titulo" nvarchar(100), "Total" bigint), 
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN


	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;



	BEGIN
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_total_transacciones bigint;
		DECLARE v_total_transacciones_descuadradas bigint;
		DECLARE v_diferencia_asignacion_permitida bigint;
		DECLARE v_fechaSistema NVARCHAR(10);
		DECLARE v_CodigoUsuario NVARCHAR(40);
		declare v_dif_encontradas bigint;
		DECLARE v_textoResultado NVARCHAR(40);
		
		DECLARE v_transacciones_rue	TABLE ("IdClasificacionTipoInstrumento" NVARCHAR(10), "FechaOperacionTransaccion" SECONDDATE, "FolioRuebas" bigint, "FolioTransaccion" nvarchar(40),     "IdBolsa" NVARCHAR(3),     "AccionOrden" NVARCHAR(50),"Nemotecnico" NVARCHAR(40),"CodigoOperadorCorredor" NVARCHAR(40),"CantidadTransaccion" bigint,"CantidadAsignadaTransaccion" bigint,"MontoTransaccion" bigint,"MontoAsignadoTransaccion" bigint,"IdMonedaInstrumento" NVARCHAR(3),"EsFungible" NVARCHAR(1),"Descripcion" NVARCHAR(3),"Existe" nvarchar(1), "IdTipoInstrumento" NVARCHAR(3),"ValorRescateTransaccion" bigint,"ValorRescateAsignadoTransaccion" bigint);
		
		DECLARE p_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			declare v_resultadoRegla nvarchar(40);
			
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;						  
			
		END;
		----------------------------------------------------------------------
		--CARGAR TODAS LAS TRANSACCIONES RV,RF,IF
		--A cada monto/cantidad de una transaccion, se le resta la suma de monto/cantidad de cada una de sus asignaciones.
		----------------------------------------------------------------------
		--Cargar el monto maximo de tolerancia entre el monto nominal vs monto asignado.
		SELECT TO_BIGINT(COALESCE("DiferenciaMontoAsignacion",0)) INTO v_diferencia_asignacion_permitida
		FROM "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenesPivot"
		WHERE "IdEmpresa" = p_IdEmpresa;
		
		p_out_transacciones = SELECT
			"TRAN"."IdClasificacionTipoInstrumento",
			"TRAN"."FechaOperacionTransaccion",
			"TRAN"."FechaVencimiento",
			"TRAN"."FolioTransaccion",
			"TRAN"."IdBolsa",
			"TRAN"."AccionOrden",
			"TRAN"."InstrumentoUI" AS "Nemotecnico",
			"TRAN"."CodigoOperadorCorredor",
			"TRAN"."CantidadTransaccion"  ,
			"TRAN"."CantidadAsignadaTransaccion" ,
			"TRAN"."MontoTransaccion",
			"TRAN"."MontoAsignadoTransaccion",
			"TRAN"."IdMonedaTransaccion",
			"TRAN"."EsFungible",
			CASE
				WHEN "EsFungible" = 'S' THEN 
					CASE
						
						WHEN "ValorRescateAsignadoTransaccion" = 0 THEN 'UI_SIN_MONTO_RESCATE_ASIGNACION'
						WHEN "ValorRescateTransaccion" > "ValorRescateAsignadoTransaccion" THEN 'UI_FALTA_MONTO_RESCATE_ASIGNACION'
						WHEN "ValorRescateTransaccion" < "ValorRescateAsignadoTransaccion" THEN 'UI_SOBRA_ASIGNACION'
						WHEN "MontoAsignadoTransaccion" = 0 THEN 'UI_SIN_MONTO_ASIGNADO'
						WHEN "MontoTransaccion" > "MontoAsignadoTransaccion" THEN 'UI_FALTA_MONTO_ASIGNACION'
						WHEN "MontoTransaccion" < "MontoAsignadoTransaccion" THEN 'UI_SOBRA_MONTO_ASIGNACION'
						ELSE ''
					END
				ELSE
					CASE
						WHEN "CantidadAsignadaTransaccion" = 0 THEN 'UI_SIN_ASIGNACION'
						WHEN "CantidadTransaccion" > "CantidadAsignadaTransaccion" THEN 'UI_FALTA_ASIGNACION'
						WHEN "CantidadTransaccion" < "CantidadAsignadaTransaccion" THEN 'UI_SOBRA_ASIGNACION'
						WHEN "MontoAsignadoTransaccion" = 0 THEN 'UI_SIN_MONTO_ASIGNADO'
						WHEN "MontoTransaccion" > "MontoAsignadoTransaccion" THEN 'UI_FALTA_MONTO_ASIGNACION'
						WHEN "MontoTransaccion" < "MontoAsignadoTransaccion" THEN 'UI_SOBRA_MONTO_ASIGNACION'
						ELSE ''
					END
			END  AS "Descripcion",
			"TRAN"."IdTipoInstrumento",
			"TRAN"."ValorRescateTransaccion",
			"TRAN"."ValorRescateAsignadoTransaccion"
		FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS "TRAN"
		WHERE "TRAN"."FechaSistemaCreacion" = p_FechaInicio
		AND "TRAN"."IdEstadoTransaccion" != 'ANU'
		AND ("TRAN"."IdClasificacionTipoInstrumento" = 'RV' OR "TRAN"."IdClasificacionTipoInstrumento" = 'RF' OR "TRAN"."IdClasificacionTipoInstrumento" = 'IF' OR "TRAN"."IdClasificacionTipoInstrumento" = 'DV');
		
		----------------------------------------------------------------------
		--La siguiente consulta, muestra las transacciones cuadradas y las que no.
						
		--------------------------------------------------------------
		--OPERACIONES RV
		--------------------------------------------------------------
		--Almacenar el total de las transacciones para el resumen.
		select count("FolioTransaccion") into v_total_transacciones from :p_out_transacciones
		WHERE "IdClasificacionTipoInstrumento" = 'RV' AND "IdTipoInstrumento" <> 'SIM';

		--Almacenar el total de transacciones descuadradas para el resumen.
		select count("FolioTransaccion") into v_total_transacciones_descuadradas from :p_out_transacciones
		WHERE "IdClasificacionTipoInstrumento" = 'RV' AND "IdTipoInstrumento" <> 'SIM' 
		AND 
		( v_diferencia_asignacion_permitida <
		(CASE 
			WHEN "Descripcion" = 'UI_FALTA_MONTO_ASIGNACION' THEN ("MontoTransaccion" - "MontoAsignadoTransaccion")
			WHEN "Descripcion" = 'UI_SOBRA_MONTO_ASIGNACION' THEN ( "MontoAsignadoTransaccion" - "MontoTransaccion" )
			ELSE 0
		END)
		OR "CantidadTransaccion" <> "CantidadAsignadaTransaccion");

		--Almacenar los resultados de la cuadratura_rv en el resumen.
		p_out_resumen = SELECT 'UI_CUADRATURA_ASIGNACION_RV' as "Titulo", 
								CASE
									WHEN COALESCE(v_total_transacciones,0) = 0 THEN 'UI_SIN_OPERACIONES'
									WHEN COALESCE(v_total_transacciones_descuadradas ,0) = 0 THEN 'UI_CUADRADO'
									ELSE 'UI_DESCUADRADO' 
								END AS "Resultado" ,
								v_total_transacciones as "Total",
								v_total_transacciones_descuadradas as "Diferencias",
								'TRX_ASIG' as "Tipo",
								1 as "Orden"
						FROM dummy;

		--------------------------------------------------------------		
		--OPERACIONES RF
		--------------------------------------------------------------
		--Almacenar el total de las transacciones para el resumen.
		select count("FolioTransaccion") into v_total_transacciones from :p_out_transacciones
		WHERE "IdClasificacionTipoInstrumento" = 'RF';

		--Almacenar el total de transacciones descuadradas para el resumen.
		select count("FolioTransaccion") into v_total_transacciones_descuadradas from :p_out_transacciones
		WHERE "IdClasificacionTipoInstrumento" = 'RF'
		AND 
		( v_diferencia_asignacion_permitida <
		(CASE 
			WHEN "Descripcion" = 'UI_FALTA_MONTO_ASIGNACION' THEN ("MontoTransaccion" - "MontoAsignadoTransaccion")
			WHEN "Descripcion" = 'UI_SOBRA_MONTO_ASIGNACION' THEN ( "MontoAsignadoTransaccion" - "MontoTransaccion" )
			ELSE 0
		END)
		OR "CantidadTransaccion" <> "CantidadAsignadaTransaccion");

		--Almacenar los resultados de la cuadratura_rv en el resumen.
		p_out_resumen = select
		'UI_CUADRATURA_ASIGNACION_RF' as "Titulo",
		CASE
			WHEN COALESCE(v_total_transacciones,0) = 0 THEN 'UI_SIN_OPERACIONES'
			WHEN COALESCE(v_total_transacciones_descuadradas ,0) = 0 THEN 'UI_CUADRADO'
			ELSE 'UI_DESCUADRADO' 
		END AS "Resultado" ,
		v_total_transacciones as "Total",
		v_total_transacciones_descuadradas as "Diferencias",
		'TRX_ASIG' as "Tipo",
		2 as "Orden"
		FROM dummy UNION ALL SELECT * FROM :p_out_resumen;

		--------------------------------------------------------------		
		--OPERACIONES IF
		--------------------------------------------------------------
		--Almacenar el total de las transacciones para el resumen.
		select count("FolioTransaccion") into v_total_transacciones from :p_out_transacciones
		WHERE "IdClasificacionTipoInstrumento" = 'IF' AND "EsFungible" = 'N';

		--Almacenar el total de transacciones descuadradas para el resumen.
		select count("FolioTransaccion") into v_total_transacciones_descuadradas from :p_out_transacciones
		WHERE "IdClasificacionTipoInstrumento" = 'IF' AND "EsFungible" = 'N'
		AND 
		( v_diferencia_asignacion_permitida <
		(CASE 
			WHEN "Descripcion" = 'UI_FALTA_MONTO_ASIGNACION' THEN ("MontoTransaccion" - "MontoAsignadoTransaccion")
			WHEN "Descripcion" = 'UI_SOBRA_MONTO_ASIGNACION' THEN ( "MontoAsignadoTransaccion" - "MontoTransaccion" )
			ELSE 0
		END)
		OR "CantidadTransaccion" <> "CantidadAsignadaTransaccion");

		--Almacenar los resultados de la cuadratura_rv en el resumen.
		p_out_resumen = select
		'UI_CUADRATURA_ASIGNACION_IF' as "Titulo",
		CASE
			WHEN COALESCE(v_total_transacciones,0) = 0 THEN 'UI_SIN_OPERACIONES'
			WHEN COALESCE(v_total_transacciones_descuadradas ,0) = 0 THEN 'UI_CUADRADO'
			ELSE 'UI_DESCUADRADO' 
		END AS "Resultado" ,
		v_total_transacciones as "Total",
		v_total_transacciones_descuadradas as "Diferencias",
		'TRX_ASIG' as "Tipo",
		3 as "Orden"
		FROM dummy UNION ALL SELECT * FROM :p_out_resumen;

		--------------------------------------------------------------		
		--OPERACIONES IF FUNGIBLE
		--------------------------------------------------------------
		--Almacenar el total de las transacciones para el resumen.
		select count("FolioTransaccion") into v_total_transacciones from :p_out_transacciones
		WHERE "IdClasificacionTipoInstrumento" = 'IF' AND "EsFungible" = 'S';

		--Almacenar el total de transacciones descuadradas para el resumen.
		select count("FolioTransaccion") into v_total_transacciones_descuadradas from :p_out_transacciones
		WHERE "IdClasificacionTipoInstrumento" = 'IF' AND "EsFungible" = 'S'
		AND ("MontoTransaccion" <> "MontoAsignadoTransaccion" OR "ValorRescateTransaccion" <> "ValorRescateAsignadoTransaccion");

		--Almacenar los resultados de la cuadratura_rv en el resumen.
		p_out_resumen = select
						'UI_CUADRATURA_ASIGNACION_IF_FUNGIBLE' as "Titulo",
						CASE
							WHEN COALESCE(v_total_transacciones,0) = 0 THEN 'UI_SIN_OPERACIONES'
							WHEN COALESCE(v_total_transacciones_descuadradas ,0) = 0 THEN 'UI_CUADRADO'
							ELSE 'UI_DESCUADRADO' 
						END AS "Resultado" ,
						v_total_transacciones as "Total",
						v_total_transacciones_descuadradas as "Diferencias",
						'TRX_ASIG' as "Tipo",
						4 as "Orden"
						FROM dummy UNION ALL SELECT * FROM :p_out_resumen;

		--------------------------------------------------------------		
		--OPERACIONES RV SIMULTANEA
		--------------------------------------------------------------
		--Almacenar el total de las transacciones para el resumen.
		select count("FolioTransaccion") into v_total_transacciones from :p_out_transacciones
		WHERE "IdClasificacionTipoInstrumento" = 'RV' AND "IdTipoInstrumento" = 'SIM';

		--Almacenar el total de transacciones descuadradas para el resumen.
		select count("FolioTransaccion") into v_total_transacciones_descuadradas from :p_out_transacciones
		WHERE "IdClasificacionTipoInstrumento" = 'RV' AND "IdTipoInstrumento" = 'SIM'
		AND 
		( v_diferencia_asignacion_permitida <
		(CASE 
			WHEN "Descripcion" = 'UI_FALTA_MONTO_ASIGNACION' THEN ("MontoTransaccion" - "MontoAsignadoTransaccion")
			WHEN "Descripcion" = 'UI_SOBRA_MONTO_ASIGNACION' THEN ( "MontoAsignadoTransaccion" - "MontoTransaccion" )
			ELSE 0
		END)
		OR "CantidadTransaccion" <> "CantidadAsignadaTransaccion");

		--Almacenar los resultados de la cuadratura_rv en el resumen.
		p_out_resumen = select
						'UI_CUADRATURA_ASIGNACION_SIM' as "Titulo",
						CASE
							WHEN COALESCE(v_total_transacciones,0) = 0 THEN 'UI_SIN_OPERACIONES'
							WHEN COALESCE(v_total_transacciones_descuadradas ,0) = 0 THEN 'UI_CUADRADO'
							ELSE 'UI_DESCUADRADO' 
						END AS "Resultado" ,
						v_total_transacciones as "Total",
						v_total_transacciones_descuadradas as "Diferencias",
						'TRX_ASIG' as "Tipo",
						5 as "Orden"
						FROM dummy UNION ALL SELECT * FROM :p_out_resumen;

		--------------------------------------------------------------		
		--OPERACIONES DV
		--------------------------------------------------------------
		--Almacenar el total de las transacciones para el resumen.
		select count("FolioTransaccion") into v_total_transacciones from :p_out_transacciones
		WHERE "IdClasificacionTipoInstrumento" = 'DV';

		--Almacenar el total de transacciones descuadradas para el resumen.
		select count("FolioTransaccion") into v_total_transacciones_descuadradas from :p_out_transacciones
		WHERE "IdClasificacionTipoInstrumento" = 'DV'
		AND 
		( v_diferencia_asignacion_permitida <
		(CASE 
			WHEN "Descripcion" = 'UI_FALTA_MONTO_ASIGNACION' THEN ("MontoTransaccion" - "MontoAsignadoTransaccion")
			WHEN "Descripcion" = 'UI_SOBRA_MONTO_ASIGNACION' THEN ( "MontoAsignadoTransaccion" - "MontoTransaccion" )
			ELSE 0
		END)
		OR "CantidadTransaccion" <> "CantidadAsignadaTransaccion");

		--Almacenar los resultados de la cuadratura_rv en el resumen.
		p_out_resumen = select
			'UI_CUADRATURA_ASIGNACION_DV' as "Titulo",
			CASE
				WHEN COALESCE(v_total_transacciones,0) = 0 THEN 'UI_SIN_OPERACIONES'
				WHEN COALESCE(v_total_transacciones_descuadradas ,0) = 0 THEN 'UI_CUADRADO'
				ELSE 'UI_DESCUADRADO' 
			END AS "Resultado" ,
			v_total_transacciones as "Total",
			v_total_transacciones_descuadradas as "Diferencias",
			'TRX_ASIG' as "Tipo",
			6 as "Orden"
			FROM dummy UNION ALL SELECT * FROM :p_out_resumen;
						
		--------------------------------------------------------------		
		--Ahora obtener de la tabla todas las operaciones descuadradas.					
		--------------------------------------------------------------
		p_out_transacciones = Select * from :p_out_transacciones 
								where v_diferencia_asignacion_permitida <
								(CASE 
									WHEN "Descripcion" = 'UI_FALTA_MONTO_ASIGNACION' THEN ("MontoTransaccion" - "MontoAsignadoTransaccion")
									WHEN "Descripcion" = 'UI_SOBRA_MONTO_ASIGNACION' THEN ( "MontoAsignadoTransaccion" - "MontoTransaccion" )
									ELSE 0
								END)
								OR "CantidadTransaccion" <> "CantidadAsignadaTransaccion" 
								OR "ValorRescateTransaccion" <> "ValorRescateAsignadoTransaccion";

		---------------------------------------------------------------------
		--VALIDACION TRANSACCIONES OPTIMUS VS TRANSACCIONES RUEBAS		
		---------------------------------------------------------------------

		
----------------------------------------------------------------------		
		--Buscar Ordenes que no esten asignadas
----------------------------------------------------------------------
	 	p_out_ordenes = SELECT
						"IdClasificacionTipoInstrumento",
						"IdTipoInstrumento",
						"IdOrden",
						"FechaOperacionOrden",
						"FechaVigencia",
						"FechaVencimiento",
						"IdEstadoOrden",
						"CantidadOrden",
						"MontoOrden",
						"IdMonedaOrden",
						"Tasa",
						"IdCliente",
						"DocumentoIdentidad",
						"NombreCompleto",
						"InstrumentoUI",
						"CantidadAsignadaOrden",
						"MontoAsignadoOrden",
						CASE
						
							WHEN "CantidadOrden" < "CantidadAsignadaOrden" THEN 'UI_SOBRE_ASIGNACION'
							WHEN "MontoOrden" < "MontoAsignadoOrden" THEN 'UI_SOBRA_MONTO'
							WHEN "IdEstadoOrden" = 'ING' THEN 'UI_ORDEN_NO_OFERTADA'
							WHEN "IdEstadoOrden" = 'PDA' THEN 'UI_ORDEN_NO_OFERTADA'
							ELSE 'UI_ASIGNACION_COMPLETA'
						END AS "Descripcion",
						"OrdenVigente"
						FROM "BSCL.Orden.BD.Vistas::ConsultarOrden"
						WHERE "FechaVigencia" = TO_DATE(p_FechaInicio)
						AND "IdTipoCanalIngreso" <> 'TRA' AND "IdEstadoOrden" != 'ANU' AND "IdEstadoOrden" != 'RZD';
		
		--Construir el resumen
		select count("IdOrden") into v_total_transacciones FROM :p_out_ordenes;
		
		p_out_ordenes = SELECT top 10000 * FROM :p_out_ordenes 
						WHERE "OrdenVigente" = 'S'
						AND ("IdEstadoOrden" = 'ING' OR "IdEstadoOrden" = 'PDA'
						OR ("CantidadOrden" < "CantidadAsignadaOrden" OR "MontoOrden" < "MontoAsignadoOrden"));
		
		
		select count("IdOrden") into v_total_transacciones_descuadradas from :p_out_ordenes;
		
		p_out_resumen = select
						'UI_ORDENES_ASIGNACIONES' as "Titulo",
						 CASE
							WHEN COALESCE(v_total_transacciones,0) = 0 THEN 'UI_SIN_OPERACIONES'
							WHEN COALESCE(v_total_transacciones_descuadradas ,0) = 0 THEN 'UI_CUADRADO'
							ELSE 'UI_DESCUADRADO' 
						END AS "Resultado" ,
						v_total_transacciones as "Total",
						v_total_transacciones_descuadradas as "Diferencias",
						'ORDEN' as "Tipo",
						12 as "Orden"
						FROM dummy
						UNION ALL SELECT * FROM :p_out_resumen;
		
		----------------------------------------------------------------------		
		--Construir resumen de ordenes a vencer.
		----------------------------------------------------------------------
		p_out_ordenes_vencer = SELECT
								CASE
									WHEN "IdEstadoOrden" = 'ING' OR "IdEstadoOrden" = 'PDA' THEN 'UI_ORDENES_INGRESADAS_VENCER'
									WHEN "IdEstadoOrden" = 'OFE' THEN 'UI_ORDENES_OFERTADAS_VENCER'
								END "Titulo",
								COUNT("IdEstadoOrden") "Total"
								FROM "BSCL.Orden.BD.Vistas::ConsultarOrden"
								WHERE "FechaVigencia" <= TO_DATE(p_FechaInicio)
								AND "OrdenVigente" = 'S'
								AND "IdEstadoOrden" <> 'RZD' AND "IdEstadoOrden" <> 'ANU'
								GROUP BY "IdEstadoOrden";
		
		SELECT COUNT("Titulo") INTO v_total_transacciones FROM :p_out_ordenes_vencer;
		
		IF COALESCE(v_total_transacciones,0)  = 0 THEN
			p_out_ordenes_vencer = SELECT 
			'UI_ORDENES_INGRESADAS_VENCER' AS "Titulo",
			0 AS "Total"
			FROM dummy;
			
			p_out_ordenes_vencer = SELECT 'UI_ORDENES_OFERTADAS_VENCER' AS "Titulo", 0 AS "Total" FROM dummy 
									UNION ALL 
									SELECT * FROM :p_out_ordenes_vencer;
		END IF;

----------------------------------------------------------------------		
		--Buscar Asignaciones no enviadas a facturar.
----------------------------------------------------------------------
		p_out_facturacion = SELECT
			asig."IdMovimientoComprobante" ,
			mov."EstadoFacturacion",
			mov."NombreEstadoFacturacion",
			asig."IdAsignacion",
			asig."FolioTransaccion",
			asig."FechaAsignacion",
			asig."IdEstadoAsignacion",
			asig."EstadoAsignacion",
			asig."AccionOrden",
			asig."NombreTipoOperacion",
			asig."InstrumentoUI",
			asig."IdBolsa",
			asig."NombreBolsa",
			asig."IdTipoInstrumento",
			asig."NombreTipoInstrumento",
			asig."IdClasificacionTipoInstrumento",
			asig."IdCliente",
			asig."DocumentoIdentidad",
			asig."NombreCompleto" AS "NombreCompleto",
			CASE
				WHEN asig."IdEstadoAsignacion" = 'ANU' THEN 'UI_ANULADA'
				WHEN asig."IdEstadoAsignacion" <> 'EAF' THEN 'UI_FALTA_ENVIAR_FACTURAR'
				WHEN mov."IdMovimientoComprobante" IS NOT NULL AND mov."EstadoFacturacion" <> 'FAC' THEN 'UI_FALTA_GENERAR_COMPROBANTE'
				ELSE ''
			END AS "Descripcion"
		FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion" asig
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" mov on asig."IdMovimientoComprobante" = mov."IdMovimientoComprobante"
		WHERE TO_DATE(asig."FechaAsignacion") = p_FechaInicio AND "IdEstadoAsignacion" != 'ANU';
		
		select count("IdAsignacion") into v_total_transacciones from :p_out_facturacion;
		
		p_out_facturacion = SELECT top 10000 * 
							FROM :p_out_facturacion 
							WHERE "Descripcion" != '';
		 
		select count("IdAsignacion") into v_total_transacciones_descuadradas from :p_out_facturacion;
		
		p_out_resumen = select
							'UI_ASIGNACIONES_SIN_COMPROBANTE' as "Titulo",
							 CASE
								WHEN COALESCE(v_total_transacciones,0) = 0 THEN 'UI_SIN_OPERACIONES'
								WHEN COALESCE(v_total_transacciones_descuadradas ,0) = 0 THEN 'UI_CUADRADO'
								ELSE 'UI_DESCUADRADO' 
							END AS "Resultado" ,
							v_total_transacciones as "Total",
							v_total_transacciones_descuadradas as "Diferencias",
							'FACTURA' as "Tipo",
							13 as "Orden"
							FROM dummy
							UNION ALL SELECT * FROM :p_out_resumen;
		
		----------------------------------------------------------------------------
		--Buscar Asignaciones Enviadas A facturar, que no esten en estado Facturado.
		----------------------------------------------------------------------------
			
		----------------------------------------------------------------------------
		--Buscar Palos informados por CAVALI, no existentes en OPTIMUS
		--Se valida que Folio, Monto y Cantidad estén cuadrados
		----------------------------------------------------------------------------
		p_out_cuadratura_cavali = select CCV."NumeroOperacion",
		                                 CCV."FechaOperacion",
									     COALESCE(CT."FolioTransaccion",0) AS "FolioTransaccion",
									     "CantidadOperacion",
									     "MontoEfectivo",
									     "DescripcionNegocio",
									     "PrecioOperacion"
								  from "BSCL.Orden.BD::ORD.CargaCuadraturaCavali" CCV
								  left join "BSCL.Orden.BD.Vistas::ConsultarTransaccion" CT on (CT."FolioTransaccion" = CCV."NumeroOperacion" and
									                                                            CT."MontoTransaccion" = CCV."MontoEfectivo" and
									                                                            CT."CantidadTransaccion" = CCV."CantidadOperacion");
		
		SELECT count("NumeroOperacion") INTO v_total_transacciones FROM :p_out_cuadratura_cavali;
								
		SELECT count("NumeroOperacion") INTO v_total_transacciones_descuadradas FROM :p_out_cuadratura_cavali WHERE "FolioTransaccion" = 0;
											 
		p_out_resumen = select
							'UI_TRANSACCIONES_CAVALI_DESCUADRADAS' as "Titulo",
							 CASE
								WHEN COALESCE(v_total_transacciones,0) = 0 THEN 'UI_SIN_TRANSACCIONES'
								WHEN COALESCE(v_total_transacciones_descuadradas ,0) = 0 THEN 'UI_CUADRADO'
								ELSE 'UI_DESCUADRADO' 
							END AS "Resultado" ,
							v_total_transacciones as "Total",
							v_total_transacciones_descuadradas as "Diferencias",
							'CAVALI' as "Tipo",
							14 as "Orden"
							FROM dummy
							UNION ALL SELECT * FROM :p_out_resumen;
		---------------------------------------------------
		--Ordenar el resumen
		---------------------------------------------------
		p_out_resumen = select * from :p_out_resumen order by "Orden";
		
		---------------------------------------------------		
		--Ejecutar el programa de logeo.
		---------------------------------------------------
		p_DetalleLog =  select 'CUADRATURA' as "SubRegistro", 'EVENTO' as "CampoRegistro", '' as "ValorAntiguo", 'CIEMOD' as "ValorNuevo" FROM dummy;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog				=> 'INFORMATIVO',
			p_IdModulo				=> 'ORD',
			p_IdUsuario				=> p_IdUsuario,
			p_IdEmpresa				=> p_IdEmpresa,
			p_FechaSistemaLog		=> p_FechaInicio,
			p_IdUsuarioAtendido		=> p_IdUsuario,
			p_IdTipoRegistro		=> 'ORDEN',
			p_IdProceso				=> 4,
			p_NombreProceso			=> 'CUADRATURA_ORDEN',
			p_DetalleLog			=> :p_DetalleLog
		);
		
		--LOG PARA EL CIERRE DE MODULO
		SELECT "CodigoUsuario" INTO v_CodigoUsuario FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" 
		WHERE "IdUsuario" = p_IdUsuario AND "IdEmpresa" = p_IdEmpresa;

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(
			p_IdEmpresa => p_IdEmpresa,
			p_out_FechaSistema => v_fechaSistema
		);
		
		select sum("Diferencias") 
		  into v_dif_encontradas
		  FROM :p_out_resumen;
		
		IF v_dif_encontradas > 0 THEN
			v_textoResultado = 'Proceso con errores';
		ELSE
			v_textoResultado = 'OK';
		END IF;
		
		INSERT INTO "BSCL.Plataforma.BD::PAU.ControlProceso"
		("IdEmpresa", "IdControlProceso", "CodigoModulo", "CodigoProceso", "FechaSistema", "FechaRegistro", "CodigoUsuario", "ResultadoProceso")
		VALUES(p_IdEmpresa, "BSCL.Plataforma.BD.Secuencias::IdControlProceso".NEXTVAL, 'ORD', 'CUADRATURA_ORDEN', v_fechaSistema, CURRENT_UTCTIMESTAMP, v_CodigoUsuario, v_textoResultado);

		
	END;
END;
