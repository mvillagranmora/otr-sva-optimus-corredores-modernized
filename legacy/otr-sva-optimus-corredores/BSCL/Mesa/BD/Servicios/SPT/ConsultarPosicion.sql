SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarPosicion";
GO
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarPosicion"(
	-- Tabla Entrada
	IN p_IdMoneda NVARCHAR(3),
	IN p_IdEmpresa BIGINT,
	IN p_IdUsuario BIGINT,
	IN p_FechaOperacion DATE,			   
	
	--Salida
    OUT p_out_hora NVARCHAR(8),
   	OUT p_out_tipoDeCambio	table (	"PosicionInicial"			DECIMAL(26,8),
   									"TipoCambioInicial"			DECIMAL(26,8),
   									"TipoCambioCierreIngresado"	DECIMAL(26,8),
									"ResultadoPosicionInicial"	DECIMAL(26,8)),
	
	OUT p_out_interbancario	table (	"ComprasPuntas"				DECIMAL(26,8),
									"VentasPuntas"				DECIMAL(26,8),
									"Posicion"					DECIMAL(26,8),
									"PromedioDeCompra"			DECIMAL(26,8),
									"PromedioDeVenta"			DECIMAL(26,8),
									"ResultadoInterbancario"	DECIMAL(26,8)),
	
	OUT p_out_arbitrajeInterbancario	table (	"PosicionPorArbitrajes"	DECIMAL(26,8),
												"ResultadoArbitrajes"	DECIMAL(26,8)),
	
	OUT p_out_distribucion	table (	"ComprasDistribucion"	DECIMAL(26,8),
									"VentasDistribucion"	DECIMAL(26,8),
									"Posicion"				DECIMAL(26,8),
									"PromedioDeCompra"		DECIMAL(26,8),
									"PromedioDeVenta"		DECIMAL(26,8),
									"ResultadoDistribucion"	DECIMAL(26,8)),
	
	OUT p_out_arbitrajeDistribucion	table (	"PosicionPorArbitrajes"			DECIMAL(26,8),
											"ResultadoArbitrajes"			DECIMAL(26,8)),
	
   	OUT p_out_Fwd	table (	"ComprasFwd"		DECIMAL(26,8),
							"VentasFwd"			DECIMAL(26,8),
							"Descalce"			DECIMAL(26,8),
							"VFisicoCompras"	DECIMAL(26,8),
							"VFisicoVentas"		DECIMAL(26,8),
							"VFisicoNeto"		DECIMAL(26,8),
							"VFutCompras"		DECIMAL(26,8),
							"VFutVentas"		DECIMAL(26,8),
							"VFutNeto"			DECIMAL(26,8)),
	
	OUT p_out_consolidadoSpot	table (	"Compra"					DECIMAL(26,8),
										"Venta"						DECIMAL(26,8),
										"Posicion"					DECIMAL(26,8),
										"UtilidadPerdidaPosicion"	DECIMAL(26,8),
										"PrecioPromedioCompra"		DECIMAL(26,8),
										"PrecioPromedioVenta"		DECIMAL(26,8),
										"ResultadoOperaciones"		DECIMAL(26,8)),
	
	OUT p_out_resumenOperacionesSpot	table (	"NumeroAnuladas"	BIGINT,
												"NumeroCompras"		BIGINT,
												"NumeroVentas"		BIGINT,
												"TotalOperaciones"	BIGINT),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE (	"IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),
													"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
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
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE v_out_AdvertenciasDetalle  	TABLE (	"IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),
												"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
											DEFAULT SELECT  0  AS "IdInterno"
														   ,'' AS "Codigo"
														   ,'' AS "RequiereAutorizacion"
														   ,'' AS "ValorParametro"
														   ,'' AS "NombreParametro"
													FROM DUMMY
													WHERE 1=2;
	DECLARE v_out_ErroresDetalle  	   	TABLE (	"IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),
												"NombreParametro" NVARCHAR(40)) 
											DEFAULT SELECT 0  AS "IdInterno"
														   ,'' AS "Codigo"
														   ,'' AS "ValorParametro"
														   ,'' AS "NombreParametro"
													FROM DUMMY
													WHERE 1=2;
	
	DECLARE v_count_operaciones				BIGINT;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
								"NombreParametro" as "CodigoDato",
								"ValorParametro" as "ValorDato" 
								FROM :v_out_ErroresDetalle
								UNION ALL
								SELECT "Codigo" AS "CodigoMensaje",
								"NombreParametro" as "CodigoDato",
								"ValorParametro" as "ValorDato" 
								FROM :v_out_AdvertenciasDetalle;
		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									errores."Codigo",
									msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									adv."Codigo",
									adv."RequiereAutorizacion",
									msj."Descripcion"
								FROM :v_out_AdvertenciasDetalle adv
								INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
				 						 	
	END;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",
										'' as "NombreParametro" from dummy where 1=2;
	v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	
	BEGIN
	
		DECLARE v_resultadoRegla 					NVARCHAR(40);
  		DECLARE p_Resultado  						BIGINT;
  		DECLARE p_tipoCambioInicial		  			DECIMAL(26,8);
  		DECLARE p_posicionInicial  					DECIMAL(26,8);
  		DECLARE p_posicionConsolidado				DECIMAL(26,8);
   		DECLARE p_cierreIngresado		  			DECIMAL(26,8);
   		DECLARE p_precioPromedioVenta	  			DECIMAL(26,8);
   		DECLARE p_precioPromedioCompra	  			DECIMAL(26,8);
   		DECLARE p_resultadoInterbancario			DECIMAL(26,8);
   		DECLARE p_resultadoArbitrajeInterbancario  	DECIMAL(26,8);
   		DECLARE p_resultadoDistribucion  		    DECIMAL(26,8);  		
   		DECLARE p_resultadoArbitrajeDistribucion  	DECIMAL(26,8);
   		DECLARE v_monedasPosicionInicial		  	DECIMAL(26,8);
   		DECLARE v_monedasPosicionInicialValorizada 	DECIMAL(26,8);
   		DECLARE v_monedasResultadoPosicionInicial 	DECIMAL(26,8);
   		DECLARE v_comprasFwd						DECIMAL(26,8);
   		DECLARE v_ventasFwd							DECIMAL(26,8);
   		DECLARE v_vFisCompras						DECIMAL(26,8);
   		DECLARE v_vFisVentas						DECIMAL(26,8);
   		DECLARE v_vFutCompras						DECIMAL(26,8);
   		DECLARE v_vFutVentas						DECIMAL(26,8);
   		DECLARE v_countDatosMesa		 			BIGINT;
   		DECLARE v_countPosicionInicial		 		BIGINT;   		
   		DECLARE v_countInterbancario	 			BIGINT;
   		DECLARE v_countArbitrajeInterbancario		BIGINT;
   		DECLARE v_countDistribucion		 			BIGINT;
   		DECLARE v_countArbitrajeDistribucion		BIGINT;
   		DECLARE v_countMovimientos		 			BIGINT;
   		DECLARE v_countMonedasPosicionInicial	 	BIGINT;
   		DECLARE v_Fecha	 							NVARCHAR(10);
   		DECLARE v_FechaSistema						NVARCHAR(10);
   		DECLARE v_EsHabil	 						NVARCHAR(1);
   		
   		DECLARE v_ParidadCalcularPosicion			NVARCHAR(10);
   		
   		DECLARE v_AdvertenciasContador	INT;
		DECLARE v_AdvertenciasDetalle	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
		DECLARE v_ErroresContador		INT;
		DECLARE v_ErroresDetalle		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
   		DECLARE v_tablaPosicionInicial	table (	 "IdPosicionInicial"	BIGINT
											 	, "IdEmpresa"			BIGINT
											 	, "Fecha" 				SECONDDATE
											 	, "IdMoneda"			NVARCHAR(3)	
											 	, "TipoCambioInicial"	DECIMAL(26,8)
											 	, "CierreIngresado"		DECIMAL(26,8)
											 	, "PosicionInicial"		DECIMAL(26,8)
											 	, "TipoInstrumento"		NVARCHAR(50)
											 	, "IdUsuario"			NVARCHAR(50)
											 	, "Estado"				NVARCHAR(3)
											 	, "ResultadoPosicionInicial"  	DECIMAL(26,8)
											  );
   		DECLARE v_countNoParidad					BIGINT; -- Contare si existe paridad en 0
   		--****************************
		--*VALIDACION REGLAS BASICAS *
		--****************************
    
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(
			:p_IdEmpresa,
			:p_IdUsuario,
			'MSP',
			:v_out_ErroresDetalle_Basicos,
			:v_resultadoRegla
		);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
  		
		--*HORA
  		SELECT CURRENT_TIME INTO p_out_hora FROM DUMMY;
  		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
    	
		--*VALOR OPCIÓN RESULTADO
 		CALL "BSCL.Mesa.BD.Servicios::ConsultarConfiguracionMesa"(1,'MEME', 'CODRESULTADO', p_out_datosMesa);
		
		SELECT COUNT(*) INTO v_countDatosMesa  FROM :p_out_datosMesa;
		IF (v_countDatosMesa > 0) THEN
			SELECT "Valor" into p_Resultado from :p_out_datosMesa;
		ELSE
			SELECT 0 into p_Resultado FROM DUMMY;
		END IF;
		
		--*Valores del Mantenedor de Paridad y Tipo Cambio
		v_ParidadTipoCambio =
			SELECT ptc.*
			FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS ptc
			WHERE ptc."Fecha" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399) 
			  AND ptc."IdEstado" = 'ACT';
		
		--*IMPCREDOPT-2812
		SELECT "Valor" INTO v_ParidadCalcularPosicion
		FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa"
		WHERE "CodigoParametro" = 'PARIDADCALCULARPOSICION'
		  AND "IdTipoProducto" = 'MEME'
		  AND "IdEmpresa" = :p_IdEmpresa;
		
		SELECT COALESCE(v_ParidadCalcularPosicion, 'MERCADO') INTO v_ParidadCalcularPosicion FROM DUMMY;
  		
		--*POSICION INICIAL
		v_tablaPosicionInicial =
			SELECT *
			FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarPosicionInicial"
			WHERE "IdEmpresa" = :p_IdEmpresa
			  AND "Estado" = 'ACT'
			  AND "Fecha" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399);
		
		--*MONEDAS POSICION INICIAL
  		v_ConsultarMonedasPosicionInicial =
  			SELECT * 
			FROM "BSCL.Mesa.BD.Vistas::ConsultarMonedasPosicionInicial"
			WHERE "Estado" = 'ACT'
			  AND "IdEmpresa" = :p_IdEmpresa
			  AND "Fecha" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399);
		
		SELECT COUNT(*) INTO v_countMonedasPosicionInicial  FROM :v_ConsultarMonedasPosicionInicial;
		
		IF (v_countMonedasPosicionInicial > 0) THEN
			v_ParidadAUtilizarMonedasPosicionInicial =
				SELECT
					 mpi."IdMoneda"
					,mpi."PosicionInicial"
					,CASE WHEN mpi."PosicionInicial" >= 0 THEN
							CASE WHEN COALESCE(ptc."ParidadBID",0) <> 0 AND :v_ParidadCalcularPosicion = 'BIDASK' THEN COALESCE(ptc."ParidadBID",0)
								 WHEN COALESCE(ptc."ParidadBID",0) <> 0 AND :v_ParidadCalcularPosicion = 'MERCADO' THEN COALESCE(ptc."ParidadMercado",0)
								 ELSE COALESCE(ptc."ParidadMercado",0) END
						  WHEN mpi."PosicionInicial" < 0 THEN
						  	CASE WHEN COALESCE(ptc."ParidadASK",0) <> 0 AND :v_ParidadCalcularPosicion = 'BIDASK' THEN COALESCE(ptc."ParidadASK",0)
								 WHEN COALESCE(ptc."ParidadASK",0) <> 0 AND :v_ParidadCalcularPosicion = 'MERCADO' THEN COALESCE(ptc."ParidadMercado",0)
								 ELSE COALESCE(ptc."ParidadMercado",0) END
						  ELSE 0 END AS "ParidadAUtilizar"
				FROM :v_ConsultarMonedasPosicionInicial mpi
				LEFT JOIN :v_ParidadTipoCambio ptc
					ON mpi."IdMoneda" = ptc."IdMoneda";
				
			SELECT CAST(SUM(CASE WHEN PARMONPOS."IdMoneda" = 'USD' THEN PARMONPOS."PosicionInicial"
								 ELSE
								 	(CASE WHEN TIPMON."MultiplicaODivide" = 'D' THEN
								 			CASE WHEN PARMONPOS."ParidadAUtilizar" <> 0 THEN PARMONPOS."PosicionInicial" / PARMONPOS."ParidadAUtilizar" ELSE 0 END
								 		  WHEN TIPMON."MultiplicaODivide" = 'M' THEN PARMONPOS."PosicionInicial" * PARMONPOS."ParidadAUtilizar"
								 		  ELSE 0 END)
								 END) AS DECIMAL(26,8))
				INTO v_monedasPosicionInicial
			FROM :v_ParidadAUtilizarMonedasPosicionInicial PARMONPOS
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
				ON (PARMONPOS."IdMoneda" = MON."IdMoneda")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON
				ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda");
			
			
			SELECT SUM((CASE WHEN mpi."IdMoneda" = 'USD' THEN mpi."PosicionInicial" 
						ELSE (mpi."PosicionInicial" * COALESCE(ptc."ParidadMercado",0)) END)* mpi."TipoCambioInicial") 
				into v_monedasPosicionInicialValorizada 
			FROM :v_ConsultarMonedasPosicionInicial mpi
			LEFT JOIN :v_ParidadTipoCambio ptc
				ON mpi."IdMoneda" = ptc."IdMoneda";
			
			
			/*SELECT SUM("ResultadoPosicionInicial") into v_monedasResultadoPosicionInicial 
			FROM :v_ConsultarMonedasPosicionInicial mpi
			INNER JOIN :v_ParidadTipoCambio ptc
				ON mpi."IdMoneda" = ptc."IdMoneda";*/
			
			SELECT SUM(("CierreIngresado" - "TipoCambioInicial") * 
					   (CASE WHEN mpi."IdMoneda" = 'USD' THEN mpi."PosicionInicial" 
							 ELSE (mpi."PosicionInicial" * COALESCE(ptc."ParidadMercado",0)) END)) into v_monedasResultadoPosicionInicial 
			FROM :v_ConsultarMonedasPosicionInicial mpi
			LEFT JOIN :v_ParidadTipoCambio ptc
				ON mpi."IdMoneda" = ptc."IdMoneda";
			
		ELSE
			SELECT 0 into v_monedasPosicionInicial FROM DUMMY;
			SELECT 0 into v_monedasResultadoPosicionInicial FROM DUMMY;
		END IF;
		
		
		--*CONSULTAR OPERACIONES
		v_Movimientos_Spot = 
			SELECT * 
			FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
			WHERE ("Estado" = 'VIG' OR "Estado" = 'PEL' OR "Estado" = 'PDA')
			  AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
			  --AND "IdOperacion" IN (78428)
			;
		
		select 'v_Movimientos_Spot', * from :v_Movimientos_Spot;

		v_Movimientos_FWD = 
			SELECT * 
			FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD"
			WHERE ("Estado" = 'VIG' OR "Estado" = 'PEL' OR "Estado" = 'PDA') 
			  --AND "FechaOperacion" <= ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
			  AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
			UNION
			SELECT * 
			FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD"
			WHERE "Estado" = 'VCD' 
			  --AND "FechaOperacion" <= ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
			  AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
			  AND v_FechaSistema != p_FechaOperacion;
		
		v_Movimientos_FWD_Vencidos = 
			SELECT *--"FechaVencimiento","IdEstado"
			FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD"
			--WHERE "FechaVencimiento" BETWEEN TO_SECONDDATE('2017-06-16') AND ADD_SECONDS(TO_SECONDDATE('2017-06-16'), 86399);
			WHERE "FechaVencimiento" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399);
							 		
	    CALL "BSCL.Plataforma.BD.Servicios::ConsultarDiaSiguiente" (
	    	 :p_IdEmpresa
	    	,:p_IdUsuario
	    	,'SPT'
	    	,'N'
	    	,:p_FechaOperacion,
	    	1,
	    	'S'
	    	,44
		    ,:v_Fecha
		    ,:v_EsHabil
		    ,:v_AdvertenciasContador
		    ,:v_AdvertenciasDetalle
		    ,:v_ErroresContador
		    ,:v_ErroresDetalle
		);
	
		v_Movimientos_FWD_Futuros =
			SELECT *
			FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD"
			WHERE "FechaVencimiento" BETWEEN TO_SECONDDATE(:v_Fecha) AND ADD_SECONDS(TO_SECONDDATE(:v_Fecha), 86399);
		
		
		--*TIPO DE CAMBIO
		SELECT COUNT(*) INTO v_countPosicionInicial  FROM :v_tablaPosicionInicial;
		
		IF (v_countPosicionInicial > 0) THEN
			v_ParidadAUtilizarPosicionInicial =
				SELECT
					 POSINI."IdMoneda"
					,POSINI."TipoCambioInicial"
					,POSINI."CierreIngresado"
					,POSINI."PosicionInicial"
					,CASE WHEN POSINI."PosicionInicial" >= 0 THEN
							CASE WHEN COALESCE(ptc."ParidadBID",0) <> 0 AND :v_ParidadCalcularPosicion = 'BIDASK' THEN COALESCE(ptc."ParidadBID",0)
								 WHEN COALESCE(ptc."ParidadBID",0) <> 0 AND :v_ParidadCalcularPosicion = 'MERCADO' THEN COALESCE(ptc."ParidadMercado",0)
								 ELSE COALESCE(ptc."ParidadMercado",0) END
						  WHEN POSINI."PosicionInicial" < 0 THEN
						  	CASE WHEN COALESCE(ptc."ParidadASK",0) <> 0 AND :v_ParidadCalcularPosicion = 'BIDASK' THEN COALESCE(ptc."ParidadASK",0)
								 WHEN COALESCE(ptc."ParidadASK",0) <> 0 AND :v_ParidadCalcularPosicion = 'MERCADO' THEN COALESCE(ptc."ParidadMercado",0)
								 ELSE COALESCE(ptc."ParidadMercado",0) END
						  ELSE 0 END AS "ParidadAUtilizar"
				FROM :v_tablaPosicionInicial POSINI
				LEFT JOIN :v_ParidadTipoCambio ptc
					ON POSINI."IdMoneda" = ptc."IdMoneda";
			
			SELECT 'v_ParidadAUtilizarPosicionInicial', * FROM :v_ParidadAUtilizarPosicionInicial;
			
			
			-- Control error no paridad configurada
			select count(*) into v_countNoParidad from :v_ParidadAUtilizarPosicionInicial Where "ParidadAUtilizar" = 0;
			if(v_countNoParidad > 0 )THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_HAY_PARIDAD_CONFIGURADA' AS "Codigo", "IdMoneda" as "ValorParametro",'IdMoneda' as "NombreParametro" FROM :v_ParidadAUtilizarPosicionInicial
				Where "ParidadAUtilizar"=0;
			SIGNAL SALIDA_CONTROLADA; 		
			END IF;
			
			
			
			v_CalculoValoresEquivalentes =
				SELECT
					 PARPOS."IdMoneda"
					,TIPMON."MultiplicaODivide"
					,PARPOS."TipoCambioInicial"
					,PARPOS."CierreIngresado"
					,PARPOS."PosicionInicial"
					,CASE WHEN TIPMON."MultiplicaODivide" = 'D' THEN PARPOS."TipoCambioInicial" * PARPOS."ParidadAUtilizar"
						  /*WHEN TIPMON."MultiplicaODivide" = 'M' THEN
				 			CASE WHEN PARPOS."ParidadAUtilizar" <> 0 THEN PARPOS."TipoCambioInicial" / PARPOS."ParidadAUtilizar" ELSE 0 END
				 		  ELSE 0 END AS "TipoCambioInicialEquivalente"*/
				 		  ELSE
				 			CASE WHEN PARPOS."ParidadAUtilizar" <> 0 THEN PARPOS."TipoCambioInicial" / PARPOS."ParidadAUtilizar" ELSE 0 END
				 		  END AS "TipoCambioInicialEquivalente"
				 	,CASE WHEN TIPMON."MultiplicaODivide" = 'D' THEN PARPOS."CierreIngresado" * PARPOS."ParidadAUtilizar"
						  /*WHEN TIPMON."MultiplicaODivide" = 'M' THEN
				 			CASE WHEN PARPOS."ParidadAUtilizar" <> 0 THEN PARPOS."CierreIngresado" / PARPOS."ParidadAUtilizar" ELSE 0 END
				 		  ELSE 0 END AS "CierreIngresadoEquivalente"*/
				 		  ELSE
				 			CASE WHEN PARPOS."ParidadAUtilizar" <> 0 THEN PARPOS."CierreIngresado" / PARPOS."ParidadAUtilizar" ELSE 0 END
				 		  END AS "CierreIngresadoEquivalente"
				 	,CASE WHEN TIPMON."MultiplicaODivide" = 'D' THEN
				 			CASE WHEN PARPOS."ParidadAUtilizar" <> 0 THEN PARPOS."PosicionInicial" / PARPOS."ParidadAUtilizar" ELSE 0 END
						  /*WHEN TIPMON."MultiplicaODivide" = 'M' THEN PARPOS."PosicionInicial" * PARPOS."ParidadAUtilizar"
				 		  ELSE 0 END AS "USDEquivalente"*/
				 		  ELSE PARPOS."PosicionInicial" * PARPOS."ParidadAUtilizar" END AS "USDEquivalente"
				FROM :v_ParidadAUtilizarPosicionInicial PARPOS
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
					ON (PARPOS."IdMoneda" = MON."IdMoneda")
				LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON
					ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda");
			
			SELECT 'v_CalculoValoresEquivalentes', * FROM :v_CalculoValoresEquivalentes;
			
			p_out_tipoDeCambio =
				SELECT
					COALESCE(:v_monedasPosicionInicial, 0) AS "PosicionInicial",
					CASE WHEN :v_monedasPosicionInicial = 0 THEN 0 ELSE
					SUM("TipoCambioInicialEquivalente" * "USDEquivalente") / v_monedasPosicionInicial END AS "TipoCambioInicial",
					CASE WHEN :v_monedasPosicionInicial = 0 THEN 0 ELSE
					SUM("CierreIngresadoEquivalente" * "USDEquivalente") / v_monedasPosicionInicial END AS "TipoCambioCierreIngresado",
					COALESCE(:v_monedasResultadoPosicionInicial, 0) AS "ResultadoPosicionInicial"
				--FROM :v_tablaPosicionInicial
				FROM :v_CalculoValoresEquivalentes;
							
			SELECT COALESCE(:v_monedasPosicionInicial, 0) INTO p_posicionInicial FROM DUMMY;
			SELECT "TipoCambioInicial", "TipoCambioCierreIngresado" INTO p_tipoCambioInicial, p_cierreIngresado
			FROM :p_out_tipoDeCambio;

		ELSE
			p_out_tipoDeCambio =
				SELECT
					COALESCE(:v_monedasPosicionInicial, 0) AS "PosicionInicial",
					0 AS "TipoCambioInicial",
					0 AS "TipoCambioCierreIngresado",
					COALESCE(:v_monedasResultadoPosicionInicial, 0) AS "ResultadoPosicionInicial"
				FROM DUMMY;
						
			SELECT 0 INTO p_posicionInicial FROM DUMMY;
			SELECT 0 INTO p_tipoCambioInicial FROM DUMMY;
			SELECT 0 INTO p_cierreIngresado FROM DUMMY;
		
		END IF;
		
		SELECT COUNT(*) INTO v_countMovimientos  FROM :v_Movimientos_Spot;
		
		IF (v_countMovimientos > 0) THEN
			--**********************
			--*POSICIÓN CONSOLIDADO*
			--**********************
			/*SELECT
			(CASE WHEN v_countPosicionInicial > 0 THEN p_posicionInicial ELSE 0 END) + COALESCE(SUM(CASE WHEN ("IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" = 'INTERB') OR ("IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" = 'DISTRI') THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END),0) - COALESCE(SUM(CASE WHEN ("IdTipoOperacion" = 'VENSPT' AND "Nemotecnico" = 'INTERB') OR ("IdTipoOperacion" = 'VENSPT' AND"Nemotecnico" = 'DISTRI') THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END),0) INTO p_PosicionConsolidado 
			FROM :v_Movimientos_Spot;*/
		
			select 'v_Movimientos_Spot_NO_FWD',
				"IdTipoOperacion", "Nemotecnico", "IdMonedaPrincipal", "IdMonedaEquivalente", "IdMonedaSecundaria", "IdMonedaLocal", "ResultadoMonedaLocalConsolidado", "MontoMonedaSecundaria"
				,:p_posicionInicial as "posicionInicial", :v_monedasPosicionInicialValorizada as "monedasPosicionInicialValorizada"
				,"MontoMonedaPrincipalEquivalente", "MontoMonedaPrincipal"
			from :v_Movimientos_Spot WHERE "IdTipoCanalIngreso" != 'FWD';
			
			--**********************
			--*PRECIO PROMEDIO COMPRA*
			--**********************
			SELECT
				COALESCE(NULLIF((COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" = 'INTERB' THEN 
												(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "ResultadoMonedaLocalConsolidado"
													  ELSE "MontoMonedaSecundaria" END) END), 0)
								+COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" = 'DISTRI' THEN
												(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "ResultadoMonedaLocalConsolidado"
													  ELSE "MontoMonedaSecundaria" END) END), 0)
								+(CASE WHEN p_posicionInicial > 0 THEN v_monedasPosicionInicialValorizada ELSE 0 END)), 0)
						/NULLIF((COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" = 'INTERB' THEN 
												(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" 
													  ELSE "MontoMonedaPrincipal" END) END), 0)
								+COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" = 'DISTRI' THEN 
												(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" 
													  ELSE "MontoMonedaPrincipal" END ) END), 0)
								+(CASE WHEN p_posicionInicial > 0 THEN p_posicionInicial ELSE 0 END)), 0), 0) INTO p_PrecioPromedioCompra
			FROM :v_Movimientos_Spot
			WHERE "IdTipoCanalIngreso" != 'FWD';
			
			select p_PrecioPromedioCompra as "p_PrecioPromedioCompra" from dummy;

						
			--**********************
			--*PRECIO PROMEDIO VENTA*
			--**********************
			SELECT
				COALESCE(NULLIF((COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' AND "Nemotecnico" = 'INTERB' THEN 
												(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "ResultadoMonedaLocalConsolidado" 
													  ELSE "MontoMonedaSecundaria" END) END), 0)
								+COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' AND "Nemotecnico" = 'DISTRI' THEN
												(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "ResultadoMonedaLocalConsolidado" 
													  ELSE "MontoMonedaSecundaria" END) END), 0)
								+(CASE WHEN p_posicionInicial < 0 THEN v_monedasPosicionInicialValorizada * -1 ELSE 0 END)), 0)
						/NULLIF((COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' AND "Nemotecnico" = 'INTERB' THEN 
												(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" 
													  ELSE "MontoMonedaPrincipal" END) END), 0)
								+COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' AND "Nemotecnico" = 'DISTRI' THEN 
												(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" 
													  ELSE "MontoMonedaPrincipal" END ) END), 0)
								+(CASE WHEN p_posicionInicial < 0 THEN (p_posicionInicial * -1) ELSE 0 END)),0), 0) INTO p_PrecioPromedioVenta
			FROM :v_Movimientos_Spot
			WHERE "IdTipoCanalIngreso" != 'FWD';
			
			select p_PrecioPromedioVenta as "p_PrecioPromedioVenta" from dummy;
		
		ELSE
			--**********************
			--*POSICIÓN CONSOLIDADO*
			--**********************
			/*SELECT
				CASE WHEN v_countPosicionInicial > 0 THEN p_posicionInicial ELSE COALESCE(:v_monedasPosicionInicial, 0) END INTO p_PosicionConsolidado 
			FROM DUMMY;*/
				
			--**********************
			--*PRECIO PROMEDIO COMPRA*
			--**********************
			SELECT
				CASE WHEN :p_posicionInicial > 0 THEN :p_tipoCambioInicial ELSE 0 END INTO p_PrecioPromedioCompra
			FROM DUMMY;
						
			--**********************
			--*PRECIO PROMEDIO VENTA*
			--**********************
			SELECT
				CASE WHEN :p_posicionInicial < 0 THEN :p_tipoCambioInicial ELSE 0 END INTO p_PrecioPromedioVenta
			FROM DUMMY;
		
		END IF;
		
		
		--*************************
		--*RESULTADO INTERBANCARIO*
		--*************************
		SELECT COUNT(*) INTO v_countInterbancario
		FROM :v_Movimientos_Spot
		WHERE "Nemotecnico" = 'INTERB';
		
		IF (v_countInterbancario > 0) THEN
			SELECT
				CASE WHEN :p_Resultado = 1 THEN SUM("ResultadoMonedaLocalPuntas") 
					 WHEN :p_Resultado = 2 THEN SUM("ResultadoMonedaLocalBidAsk")
					 WHEN :p_Resultado = 3 THEN SUM("ResultadoMonedaLocalDO")
				     WHEN :p_Resultado = 4 THEN SUM("ResultadoMonedaLocalTCSpot")
					 WHEN :p_Resultado = 5 THEN SUM("ResultadoMonedaLocalTCCierreMesa")
				ELSE 0 END 
				INTO p_resultadoInterbancario
			FROM :v_Movimientos_Spot
			WHERE "Nemotecnico" = 'INTERB' AND "IdTipoCanalIngreso" <> 'FWD';
		
		ELSE
			SELECT 0 INTO p_resultadoInterbancario FROM DUMMY;
		END IF;
		
		
		--***********************************
		--*RESULTADO ARBITRAJE INTERBANCARIO*
		--***********************************
		SELECT COUNT(*) INTO v_countArbitrajeInterbancario
		FROM :v_Movimientos_Spot
		WHERE "Nemotecnico" = 'ARBINT';
		
		IF (v_countArbitrajeInterbancario > 0) THEN
			SELECT
				CASE WHEN :p_Resultado = 1 THEN SUM("ResultadoMonedaLocalPuntas") 
					 WHEN :p_Resultado = 2 THEN SUM("ResultadoMonedaLocalBidAsk")
					 WHEN :p_Resultado = 3 THEN SUM("ResultadoMonedaLocalDO")
				     WHEN :p_Resultado = 4 THEN SUM("ResultadoMonedaLocalTCSpot")
					 WHEN :p_Resultado = 5 THEN SUM("ResultadoMonedaLocalTCCierreMesa")
				ELSE 0 END 
				INTO p_resultadoArbitrajeInterbancario
			FROM :v_Movimientos_Spot
			WHERE "Nemotecnico" = 'ARBINT';
		
		ELSE
			SELECT 0 INTO p_resultadoArbitrajeInterbancario FROM DUMMY;
		END IF;
		
		
		--************************
		--*RESULTADO DISTRIBUCION*
		--************************
		SELECT COUNT(*) INTO v_countDistribucion
		FROM :v_Movimientos_Spot
		WHERE "Nemotecnico" = 'DISTRI';
		
		IF (v_countDistribucion > 0) THEN
			SELECT SUM("Margen") INTO p_resultadoDistribucion
			FROM :v_Movimientos_Spot
			WHERE "Nemotecnico" = 'DISTRI';
		
		ELSE
			SELECT 0 INTO p_resultadoDistribucion FROM DUMMY;
		END IF;
		
		
		--**********************************
		--*RESULTADO ARBITRAJE DISTRIBUCION*
		--**********************************
		SELECT COUNT(*) INTO v_countArbitrajeDistribucion
		FROM :v_Movimientos_Spot
		WHERE "Nemotecnico" = 'ARBDIS';
		
		IF (v_countArbitrajeDistribucion > 0) THEN
			SELECT
				CASE WHEN :p_Resultado = 1 THEN SUM("ResultadoMonedaLocalPuntas") 
					 WHEN :p_Resultado = 2 THEN SUM("ResultadoMonedaLocalBidAsk")
					 WHEN :p_Resultado = 3 THEN SUM("ResultadoMonedaLocalDO")
			  		 WHEN :p_Resultado = 4 THEN SUM("ResultadoMonedaLocalTCSpot")
					 WHEN :p_Resultado = 5 THEN SUM("ResultadoMonedaLocalTCCierreMesa")
				ELSE 0 END 
				INTO p_resultadoArbitrajeDistribucion
			FROM :v_Movimientos_Spot
			WHERE "Nemotecnico" = 'ARBDIS';
		
		ELSE
			SELECT 0 INTO p_resultadoArbitrajeDistribucion FROM DUMMY;
		END IF;
		
		
		--****************
		--*INTERBANCARIO *
		--****************						
		p_out_interbancario = 
			SELECT 
				COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END), 0) AS "ComprasPuntas", 
				COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END), 0) AS "VentasPuntas",
				COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END) - SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END), 0) AS "Posicion", 
				COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "ResultadoMonedaLocalConsolidado" ELSE "MontoMonedaSecundaria" END) END) / SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END), 0) AS "PromedioDeCompra", 
				COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "ResultadoMonedaLocalConsolidado" ELSE "MontoMonedaSecundaria" END) END) / SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END), 0) AS "PromedioDeVenta",
				:p_resultadoInterbancario AS "ResultadoInterbancario"
			FROM :v_Movimientos_Spot
			WHERE "Nemotecnico" = 'INTERB' AND "IdTipoCanalIngreso" <> 'FWD';
		
		
		--**************************
		--*ARBITRAJE INTERBANCARIO *
		--**************************
		p_out_arbitrajeInterbancario = 
			SELECT 
				COALESCE(SUM("Margen"),0) AS "PosicionPorArbitrajes",
				:p_resultadoArbitrajeInterbancario AS "ResultadoArbitrajes"
			FROM :v_Movimientos_Spot
			WHERE "Nemotecnico" = 'ARBINT';
		
		
		--****************
		--*DISTRIBUCIÓN *
		--****************
		p_out_distribucion = 
			SELECT 
				COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END), 0) AS "ComprasDistribucion", 
				COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END), 0) AS "VentasDistribucion",
				COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END) - SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END), 0) AS "Posicion", 
				COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' THEN "MontoMonedaSecundaria" END) / SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END), 0) AS "PromedioDeCompra", 
				COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN "MontoMonedaSecundaria" END) / SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END), 0) AS "PromedioDeVenta",
				:p_resultadoDistribucion AS "ResultadoDistribucion"
			FROM :v_Movimientos_Spot
			WHERE "Nemotecnico" = 'DISTRI';
		
		
		--**************************
		--*ARBITRAJE DISTRIBUCIÓN *
		--**************************
		p_out_arbitrajeDistribucion = 
			SELECT 
				COALESCE(SUM("Margen"),0) AS "PosicionPorArbitrajes",
				:p_resultadoArbitrajeDistribucion AS "ResultadoArbitrajes"
			FROM :v_Movimientos_Spot
			WHERE "Nemotecnico" = 'ARBDIS';
		
		
   		--**********
		--*FORWARD *
		--**********
		SELECT 
			 COALESCE(SUM(CASE WHEN FWD."IdTipoOperacion" = 'COMFWD' AND FWD."IdMonedaPrincipal" = 'USD' THEN FWD."MontoMonedaPrincipal"
							   WHEN FWD."IdTipoOperacion" = 'COMFWD' AND FWD."IdMonedaSecundaria" = 'USD' THEN FWD."MontoMonedaSecundaria"
   						  END),0)
   			,COALESCE(SUM(CASE WHEN FWD."IdTipoOperacion" = 'VENFWD' AND FWD."IdMonedaPrincipal" = 'USD' THEN FWD."MontoMonedaPrincipal" 
   							   WHEN FWD."IdTipoOperacion" = 'VENFWD' AND FWD."IdMonedaSecundaria" = 'USD' THEN FWD."MontoMonedaSecundaria"
   						  END),0)
			INTO v_comprasFwd, v_ventasFwd
		FROM :v_Movimientos_FWD AS FWD
		INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PAR
    		ON PAR."IdParametro" = FWD."Modalidad"
		WHERE FWD."Nemotecnico" = 'ARBITR'
		  OR (FWD."Nemotecnico" = 'SECANO' AND (FWD."IdMonedaPrincipal" = 'USD' OR FWD."IdMonedaSecundaria" = 'USD'));
		
		
		SELECT 
			 COALESCE(SUM(CASE WHEN FWD."IdTipoOperacion" = 'COMFWD' AND PAR."ValorParametro" = 'ENTREGA_FISICA' AND 
								   (FWD."Estado" = 'VIG' OR FWD."Estado" = 'VCD') AND v_FechaSistema != p_FechaOperacion THEN
								CASE WHEN "IdMonedaPrincipal" = 'USD' THEN FWD."MontoMonedaPrincipal" ELSE FWD."MontoMonedaSecundaria" END
							   WHEN FWD."IdTipoOperacion" = 'COMFWD' AND PAR."ValorParametro" = 'ENTREGA_FISICA' AND 
									FWD."Estado" = 'VIG' AND v_FechaSistema = p_FechaOperacion THEN
								CASE WHEN "IdMonedaPrincipal" = 'USD' THEN FWD."MontoMonedaPrincipal" ELSE FWD."MontoMonedaSecundaria" END
						  END),0)
			,COALESCE(SUM(CASE WHEN FWD."IdTipoOperacion" = 'VENFWD' AND PAR."ValorParametro" = 'ENTREGA_FISICA' AND 
									(FWD."Estado" = 'VIG' OR FWD."Estado" = 'VCD') AND v_FechaSistema != p_FechaOperacion THEN
								CASE WHEN "IdMonedaPrincipal" = 'USD' THEN FWD."MontoMonedaPrincipal" ELSE FWD."MontoMonedaSecundaria" END
							   WHEN FWD."IdTipoOperacion" = 'VENFWD' AND PAR."ValorParametro" = 'ENTREGA_FISICA' AND 
									FWD."Estado" = 'VIG' AND v_FechaSistema = p_FechaOperacion THEN
								CASE WHEN "IdMonedaPrincipal" = 'USD' THEN FWD."MontoMonedaPrincipal" ELSE FWD."MontoMonedaSecundaria" END
						  END),0)
			INTO v_vFisCompras, v_vFisVentas
		FROM :v_Movimientos_FWD_Vencidos AS FWD
		INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PAR
    		ON PAR."IdParametro" = FWD."Modalidad"
		WHERE FWD."Nemotecnico" = 'ARBITR'
		  OR (FWD."Nemotecnico" = 'SECANO' AND (FWD."IdMonedaPrincipal" = 'USD' OR FWD."IdMonedaSecundaria" = 'USD'));
		
		
		SELECT 
			COALESCE(SUM(CASE WHEN FWD."IdTipoOperacion" = 'COMFWD' AND PAR."ValorParametro" = 'ENTREGA_FISICA' AND FWD."Estado" = 'VIG' THEN
								CASE WHEN "IdMonedaPrincipal" = 'USD' THEN FWD."MontoMonedaPrincipal" ELSE FWD."MontoMonedaSecundaria" END
						 END),0),
			COALESCE(SUM(CASE WHEN FWD."IdTipoOperacion" = 'VENFWD' AND PAR."ValorParametro" = 'ENTREGA_FISICA' AND FWD."Estado" = 'VIG' THEN
								CASE WHEN "IdMonedaPrincipal" = 'USD' THEN FWD."MontoMonedaPrincipal" ELSE FWD."MontoMonedaSecundaria" END
						 END),0)
			INTO v_vFutCompras, v_vFutVentas
		FROM :v_Movimientos_FWD_Futuros AS FWD
		INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PAR
    		ON PAR."IdParametro" = FWD."Modalidad"
		WHERE FWD."Nemotecnico" = 'ARBITR'
		  OR (FWD."Nemotecnico" = 'SECANO' AND (FWD."IdMonedaPrincipal" = 'USD' OR FWD."IdMonedaSecundaria" = 'USD'));
		
		
		p_out_Fwd =
			SELECT 
				:v_comprasFwd AS "ComprasFwd",
				:v_ventasFwd AS "VentasFwd",
				(:v_comprasFwd - :v_ventasFwd) AS "Descalce", 
				:v_vFisCompras AS "VFisicoCompras",
				:v_vFisVentas AS "VFisicoVentas",
				(:v_vFisCompras - :v_vFisVentas) AS"VFisicoNeto",
				:v_vFutCompras AS "VFutCompras",
				:v_vFutVentas AS "VFutVentas",
				(:v_vFutCompras - :v_vFutVentas) AS "VFutNeto"
			FROM DUMMY;


		--**********************
		--*POSICIÓN CONSOLIDADO*
		--**********************
		SELECT COALESCE(("ComprasPuntas" + "ComprasDistribucion" + "PosicionInicial")-("VentasPuntas"+"VentasDistribucion"),0)
		INTO p_PosicionConsolidado
		FROM :p_out_interbancario,:p_out_distribucion,:p_out_tipoDeCambio;
		
	
		--*******************
		--*CONSOLIDADO SPOT *
		--*******************
		SELECT
			 'Revision Valores'
			,p_PosicionConsolidado as "PosicionConsolidado"
			,p_PrecioPromedioVenta as "PrecioPromedioVenta"
			,p_posicionInicial as "posicionInicial"
			,p_tipoCambioInicial as "tipoCambioInicial"
			,p_cierreIngresado as "cierreIngresado"
			,p_PosicionConsolidado as "PosicionConsolidado"
			,p_PrecioPromedioCompra as "PrecioPromedioCompra"
		FROM DUMMY;
		
		p_out_consolidadoSpot = 
			SELECT 
				 COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" IN ('INTERB','DISTRI') THEN
									CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN 
										"MontoMonedaPrincipalEquivalente" 
									ELSE "MontoMonedaPrincipal" END
							  ELSE 0 END),0)	AS "Compra"
				,COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' AND "Nemotecnico" IN ('INTERB','DISTRI') THEN
									CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN
										"MontoMonedaPrincipalEquivalente"
									ELSE "MontoMonedaPrincipal" END
							  ELSE 0 END),0)	AS "Venta"
				,:p_PosicionConsolidado			AS "Posicion"
				,CASE WHEN p_PosicionConsolidado < 0 THEN
					CASE WHEN p_PrecioPromedioVenta = 0 THEN
						CASE WHEN p_posicionInicial < 0 THEN (p_tipoCambioInicial - p_cierreIngresado) * p_PosicionConsolidado
						ELSE (-p_cierreIngresado) * p_PosicionConsolidado END
					ELSE (p_PrecioPromedioVenta - p_cierreIngresado) * p_PosicionConsolidado END
				 ELSE
				 	CASE WHEN p_PrecioPromedioCompra = 0 THEN
				 		CASE WHEN p_posicionInicial > 0 THEN (p_tipoCambioInicial - p_cierreIngresado) * p_PosicionConsolidado
				 		ELSE (-p_cierreIngresado) * p_PosicionConsolidado END
				 	ELSE (p_PrecioPromedioCompra - p_cierreIngresado) * p_PosicionConsolidado END
				 END							AS "UtilidadPerdidaPosicion"
				,:p_PrecioPromedioCompra		AS "PrecioPromedioCompra"
				,:p_PrecioPromedioVenta			AS "PrecioPromedioVenta"
				,COALESCE(:v_monedasResultadoPosicionInicial,0) + COALESCE(:p_resultadoInterbancario,0) + 
				 COALESCE(:p_resultadoArbitrajeInterbancario,0) + COALESCE(:p_resultadoDistribucion,0) + 
				 COALESCE(:p_resultadoArbitrajeDistribucion,0) AS "ResultadoOperaciones"
			FROM :v_Movimientos_Spot
			WHERE "IdTipoCanalIngreso" != 'FWD';
		
		
		--***************************
		--*RESUMEN OPERACIONES SPOT *
		--***************************
		p_out_resumenOperacionesSpot = 
			SELECT 
				 COALESCE(COUNT(CASE WHEN "Estado" = 'ANU' THEN "IdOperacion" END),0) AS "NumeroAnuladas"
				,COALESCE(COUNT(CASE WHEN "IdTipoOperacion" = 'COMSPT' AND "Estado" = 'VIG' THEN "IdOperacion" END),0) AS "NumeroCompras"
				,COALESCE(COUNT(CASE WHEN "IdTipoOperacion" = 'VENSPT' AND "Estado" = 'VIG' THEN "IdOperacion" END),0) AS "NumeroVentas"
				,COALESCE(COUNT(CASE WHEN "IdTipoOperacion" IN ('COMSPT','VENSPT') AND 
										  "Estado" IN ('VIG','PEL','PDA','ANU') THEN "IdOperacion" END),0) AS "TotalOperaciones"
			FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
			WHERE "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
				/*AND ("Estado" = 'VIG'
				OR "Estado" = 'PEL'
				OR "Estado" = 'PDA')*/
			--AND "IdOperacion" IN (78428)
			;
		
  	END;
  	
END;