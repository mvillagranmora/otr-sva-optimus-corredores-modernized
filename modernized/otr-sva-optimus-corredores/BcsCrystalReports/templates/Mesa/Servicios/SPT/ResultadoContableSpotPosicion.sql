SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.CrystalReports.Servicios::SPT.ResultadoContableSpotPosicion";
--
--  Proposito:
--  Fecha Creacion: 01/07/2016
--
CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::SPT.ResultadoContableSpotPosicion"(
	--Entrada
	IN p_IdEmpresa	BIGINT,
	IN p_Fecha		DATE,
	IN p_Nemotecnico NVARCHAR(6)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_Count							BIGINT;
	DECLARE varFechaHoraSistema				NVARCHAR(40);
	DECLARE varFechaHoraAnterior			NVARCHAR(40);
	DECLARE varFechaAnterior				NVARCHAR(10);
	DECLARE v_ConfiguracionUtilidadPerdida	NVARCHAR(40);
	DECLARE v_DolarObservado				DECIMAL(26,8);
	DECLARE v_PosicionInicial				DECIMAL(26,8);
	DECLARE v_PrecioPosicionInicial			DECIMAL(26,8);
	DECLARE v_PosicionInicialCLP			DECIMAL(26,8);
	DECLARE v_UtilidadPerdidaPosicionFinal  DECIMAL(26,8);
	DECLARE v_UtilidadPerdidaPromedio		DECIMAL(26,8);
	DECLARE v_PrecioPromedioVenta           DECIMAL(26,8);
	DECLARE v_PrecioPromedioCompra          DECIMAL(26,8);
	DECLARE v_PrecioPosicionFinal           DECIMAL(26,8);
	DECLARE v_PosicionFinal                 DECIMAL(26,8);
	DECLARE v_PesosEquivalentePosicion      DECIMAL(26,8);
	DECLARE v_USDCompradosHoy               DECIMAL(26,8);
	DECLARE v_USDVendidosHoy                DECIMAL(26,8);
	DECLARE v_ComprasMXenUSD               	DECIMAL(26,8);
	DECLARE v_VentasMXenUSD                	DECIMAL(26,8);
	DECLARE v_AbonosCtaCteUSD               DECIMAL(26,8);
	DECLARE v_CargosCtaCteUSD               DECIMAL(26,8);
	DECLARE v_TotalBoletasUSDCompras		DECIMAL(26,8); --**A
	DECLARE v_TotalBoletasUSDVentas			DECIMAL(26,8); --**C
	DECLARE v_ComprasBoletasMXenUSD         DECIMAL(26,8); --**A
	DECLARE v_VentasBoletasMXenUSD          DECIMAL(26,8); --**C
	DECLARE v_TotalBoletasCLPCompras        DECIMAL(26,8); --**B
	DECLARE v_TotalBoletasCLPVentas			DECIMAL(26,8); --**D
	DECLARE v_TotalFacturasUSDCompras       DECIMAL(26,8); --**FALTA --**A
	DECLARE v_TotalFacturasUSDVentas        DECIMAL(26,8); --**FALTA --**C
	DECLARE v_ComprasFacturasMXenUSD		DECIMAL(26,8); --**FALTA --**A
	DECLARE v_VentasFacturasMXenUSD			DECIMAL(26,8); --**FALTA --**C
	DECLARE v_TotalFacturasCLPCompras		DECIMAL(26,8); --**FALTA --**B
	DECLARE v_TotalFacturasCLPVentas		DECIMAL(26,8); --**FALTA --**D
	DECLARE v_MargenArbitrajeCompras		DECIMAL(26,8);
	DECLARE v_MargenArbitrajeVentas			DECIMAL(26,8);
	-- Dia anterior:::
	-- [X] Posición Final US$ del 28-06-2013        v_PosicionInicial
	-- [Y] Precio Pos. Final $ del 28-06-2013       v_PrecioPosicionInicial
	-- [Z] Posición Final $ (X * Y) del 28-06-2013  v_PosicionInicialCLP
	DECLARE v_X_PosicionInicialAyer			DECIMAL(26,8); --**FALTA
	DECLARE v_Y_PrecioPosicionInicialAyer	DECIMAL(26,8); --**FALTA
	DECLARE v_Z_PosicionInicialCLPAyer		DECIMAL(26,8); --**FALTA
	DECLARE v_A_TotalComprasEnUSPeso		DECIMAL(26,8); --**FALTA --** a+f
	DECLARE v_C_TotalVentasEnUSPeso			DECIMAL(26,8); --**FALTA --** c+g
	DECLARE v_B_TotalComprasEnPeso			DECIMAL(26,8); --**FALTA --** b+I
	DECLARE v_D_TotalVentasEnPeso			DECIMAL(26,8); --**FALTA --** d+j
	DECLARE v_E_PrecioPosicionFinal			DECIMAL(26,8); --**FALTA --** B/A
	DECLARE v_F_SaldoEnPesos				DECIMAL(26,8); --**FALTA --** Z+B-D
	DECLARE v_G_PosValorizada				DECIMAL(26,8); --**FALTA --** (X+A-C)*E
	DECLARE v_I_TotalOrdenesIngresoCLP		DECIMAL(26,8); --**FALTA
	DECLARE v_J_TotalOrdenesPagoCLP			DECIMAL(26,8); --**FALTA
	DECLARE v_Hant_UtilPerdAcumDiaAnt		DECIMAL(26,8); --**FALTA
	DECLARE v_H_UtilPerDelDia				DECIMAL(26,8); --**FALTA --** G-F
	DECLARE v_TotalConceptoOIUS$			DECIMAL(26,8); --**FALTA --** f
	DECLARE v_TotalConceptoOPUS$			DECIMAL(26,8); --**FALTA --** g
	DECLARE v_NuevaPosicionFinalUSPeso		DECIMAL(26,8); --**FALTA --** X+A-C
	DECLARE v_UtilidadPerdidaDolarObservado	DECIMAL(26,8); --**FALTA
	DECLARE v_UtilidadPerdidaDistribucion	DECIMAL(26,8);
	DECLARE v_UtilidadPerdidaPuntas			DECIMAL(26,8);

	--RESULTADO
	DECLARE v_MovDiarioCompras	DECIMAL(26,8);
	DECLARE v_MovDiarioVentas	DECIMAL(26,8);
	DECLARE v_ResultadoCompras	DECIMAL(26,8);
	DECLARE v_ResultadoVentas	DECIMAL(26,8);
	
	DECLARE v_CantidadDecimales BIGINT;
	DECLARE v_ResultadoPrecioPosicionInicial    DECIMAL(26,8);
    DECLARE v_SumaValorRazonable                DECIMAL(26,8);
    DECLARE v_SumaValorRazonableAnterior        DECIMAL(26,8);
    DECLARE v_SumaRecibimos                     DECIMAL(26,8);
    DECLARE v_SumaEntregamos                    DECIMAL(26,8);
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
		p_IdEmpresa => p_IdEmpresa,
		p_out_FechaHoraSistema => varFechaHoraSistema
	);
	varFechaHoraSistema = TO_CHAR(varFechaHoraSistema,'YYYY-MM-dd')||' 00:00:00';
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaAnterior" (
		p_Fecha		=> :p_Fecha,
		p_NumeroDias=> 1,
		p_EsHabil	=> 'S',
		p_IdPais	=> 1,
		p_out_FechaAnterior => varFechaHoraAnterior
	);
	varFechaAnterior = TO_CHAR(:varFechaHoraAnterior,'DD-MM-YYYY');
	
	SELECT COUNT(1) INTO v_Count FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" WHERE "IdMoneda" = 'DO' AND "Fecha" = :p_Fecha;
	IF v_Count > 0 THEN
		SELECT PARDO."TipoCambioMercado" INTO v_DolarObservado
		FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARDO
		WHERE PARDO."IdMoneda" = 'DO'
		  AND PARDO."Fecha" = :p_Fecha;
	END IF;
	
	v_DolarObservado = CASE WHEN COALESCE(v_DolarObservado,0) = 0 THEN COALESCE(v_PrecioPosicionFinal,0) ELSE COALESCE(v_DolarObservado,0) END;
		
	--Guarda configuracion de sistema en variables
	SELECT "Valor" into v_ConfiguracionUtilidadPerdida
	FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa"
	WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" = 'MOPOSIUTILIDADPERDIDA';
	
	--Calculo de utilidad integrado
	SELECT COALESCE(SUM(CASE
			WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND CAR."EGRIdTipoOperacion" = 'ANTFWD' THEN
				CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN CAR."EGRMontoMonedaPrincipal" ELSE CAR."EGRMontoMonedaSecundaria" END
			WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND CAR."EGRIdTipoOperacion" <> 'ANTFWD' THEN
				CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN CAR."INGMontoMonedaPrincipal" ELSE CAR."INGMontoMonedaSecundaria" END
			WHEN PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdTipoOperacion" = 'COMFWD' AND CAR."EGRMontoLiquidacion" < 0 
				THEN ABS(CAR."EGRMontoLiquidacion")
			WHEN PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdTipoOperacion" = 'VENFWD' AND CAR."EGRMontoLiquidacion" > 0 
				THEN CAR."EGRMontoLiquidacion"
			ELSE 0 END),0),
			COALESCE(SUM(CASE
			WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND CAR."EGRIdTipoOperacion" = 'ANTFWD' THEN
				CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN CAR."EGRMontoMonedaSecundaria" ELSE CAR."EGRMontoMonedaPrincipal" END
			WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND CAR."EGRIdTipoOperacion" <> 'ANTFWD' THEN
				CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN CAR."INGMontoMonedaSecundaria" ELSE CAR."INGMontoMonedaPrincipal" END
			WHEN PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdTipoOperacion" = 'COMFWD' AND CAR."EGRMontoLiquidacion" > 0 
				THEN CAR."EGRMontoLiquidacion"
			WHEN PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdTipoOperacion" = 'VENFWD' AND CAR."EGRMontoLiquidacion" < 0 
				THEN ABS(CAR."EGRMontoLiquidacion")
			ELSE 0 END),0)														
			
			INTO 
			
			v_SumaRecibimos, v_SumaEntregamos
			
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CAR
	INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PARAM
				ON (PARAM."IdParametro" = CAR."IdModalidadCumplimiento" AND
					PARAM."NombreTipoParametro" = 'ModalidadLiquidacion' AND
					PARAM."IdEmpresa" = CAR."IdEmpresa")
	WHERE CAR."IdTipoProducto" = 'MEFW'
	AND CAR."IdEstado" = 'LIQ';
	
	SELECT COALESCE(sum("INGMontoATasaCompraActual"),0) into v_SumaValorRazonableAnterior
	FROM "BSCL.Custodia.BD::HIS.Cartera"
	WHERE "FechaRespaldo" = TO_CHAR(varFechaHoraAnterior,'yyyy-MM-dd')
	AND "IdTipoProducto" = 'MEFW'
	AND "IdEstado" IN ('LIQ','VCD');
	
	SELECT COALESCE(sum("INGMontoATasaCompraActual"),0) INTO v_SumaValorRazonable
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdTipoProducto" = 'MEFW'
	AND "IdEstado" IN ('VIG');
	
	v_ParidadTipoCambioHoy =
		SELECT * FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PTC
		WHERE PTC."Fecha" = :p_Fecha
		  AND PTC."IdEstado" = 'ACT';
		
	v_ParidadTipoCambioAnterior =
		SELECT * FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PTC
		WHERE PTC."Fecha" = TO_CHAR(:varFechaHoraAnterior,'YYYY-MM-DD')
		  AND PTC."IdEstado" = 'ACT';
	
	--POSICION INICIAL
	SELECT
		 CAST(SUM(CASE WHEN POSINI."IdMoneda" = 'USD' THEN COALESCE(POSINI."PosicionInicial",0)
		 			   ELSE (COALESCE(POSINI."PosicionInicial",0) * COALESCE(PTC."ParidadMercado",0)) END)AS DECIMAL(26,8))	AS "PosicionInicial"
		,SUM(CASE WHEN POSINI."IdMoneda" = 'USD' THEN COALESCE("TipoCambioInicial",0) ELSE 0 END)	AS "PrecioPosicionInicial"
		,CAST(SUM(CASE WHEN POSINI."IdMoneda" = 'CLP' THEN COALESCE(POSINI."PosicionInicial",0)
					   ELSE 0 END) AS DECIMAL(26,8))												AS "PosicionInicialCLP"
		,SUM(CASE WHEN POSINI."IdMoneda" = 'USD' THEN COALESCE("CierreIngresado",0) ELSE 0 END)		AS "PrecioPosicionFinal"
		INTO v_PosicionInicial, v_PrecioPosicionInicial , v_PosicionInicialCLP , v_PrecioPosicionFinal
	FROM "BSCL.Mesa.BD.Vistas::ConsultarMonedasPosicionInicial" POSINI
	INNER JOIN :v_ParidadTipoCambioHoy PTC
		ON (POSINI."IdMoneda" = PTC."IdMoneda")
	WHERE POSINI."Estado" = 'ACT'
	  AND POSINI."IdEmpresa" = p_IdEmpresa 
	  AND TO_CHAR(POSINI."Fecha",'YYYY-MM-DD') = :p_Fecha
	;
	
	SELECT COUNT(1) INTO v_Count FROM "BSCL.Mesa.BD::SPT.ResultadoContableSpot" WHERE "Fecha" = ADD_DAYS(:p_fecha,-1) AND "IdEstado" = 'ACT';
	IF v_Count > 0 THEN 
		SELECT
			COALESCE("PrecioPosicionFinal",0),
			COALESCE("PosicionFinal",0)
		INTO v_Y_PrecioPosicionInicialAyer,v_X_PosicionInicialAyer
		FROM
			"BSCL.Mesa.BD::SPT.ResultadoContableSpot"
		WHERE
			"Fecha" = ADD_DAYS(:p_fecha,-1)
		AND "IdEstado" = 'ACT';		
	END IF;
	--VALORES SEGUN OPERACIONES
	SELECT 
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" = 'USD' AND SPOT."IdTipoOperacion" = 'COMSPT' THEN COALESCE(SPOT."MontoMonedaPrincipal",0)
				 WHEN SPOT."IdMonedaPrincipal" != 'USD' AND SPOT."IdMonedaSecundaria" = 'CLP' AND SPOT."IdTipoOperacion" = 'COMSPT' THEN
				 	COALESCE(SPOT."MontoMonedaPrincipalEquivalente",0)
				 ELSE 0 END) AS "USDCompradosHoy",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" = 'USD' AND SPOT."IdTipoOperacion" = 'VENSPT' THEN COALESCE(SPOT."MontoMonedaPrincipal",0)
				 WHEN SPOT."IdMonedaPrincipal" != 'USD' AND SPOT."IdMonedaSecundaria" = 'CLP' AND SPOT."IdTipoOperacion" = 'VENSPT' THEN
				 	COALESCE(SPOT."MontoMonedaPrincipalEquivalente",0)
				 ELSE 0 END) AS "USDVendidosHoy",
		/*SUM(CASE WHEN SPOT."IdMonedaPrincipal" !='USD' AND SPOT."IdMonedaSecundaria" != 'CLP' AND SPOT."IdTipoOperacion" = 'COMSPT' 
					  THEN COALESCE(SPOT."MontoMonedaPrincipalEquivalente",0)
				 ELSE 0 END) AS "ComprasMXenUSD",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" !='USD' AND SPOT."IdMonedaSecundaria" != 'CLP' AND SPOT."IdTipoOperacion" = 'VENSPT' 
					  THEN COALESCE(SPOT."MontoMonedaPrincipalEquivalente",0)
				 ELSE 0 END) AS "VentasMXenUSD",*/
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" = 'USD' AND SPOT."IdTipoOperacion" = 'COMSPT' THEN COALESCE(SPOT."MontoMonedaPrincipal",0)
				 --WHEN SPOT."IdMonedaSecundaria" = 'USD' AND SPOT."IdTipoOperacion" = 'VENSPT' THEN COALESCE(SPOT."MontoMonedaSecundaria",0)
				 ELSE 0 END) AS "AbonosCtaCteUSD",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" = 'USD' AND SPOT."IdTipoOperacion" = 'VENSPT' THEN COALESCE(SPOT."MontoMonedaPrincipal",0)
				 --WHEN SPOT."IdMonedaSecundaria" = 'USD' AND SPOT."IdTipoOperacion" = 'COMSPT' THEN COALESCE(SPOT."MontoMonedaSecundaria",0)
				 ELSE 0 END) AS "CargosCtaCteUSD",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" = 'USD' AND SPOT."IdTipoOperacion" = 'COMSPT' AND 
					  SPOT."TipoComprobanteFacturacion" like 'BO%' THEN COALESCE(SPOT."MontoMonedaPrincipal",0)
				 ELSE 0 END) AS "TotalBoletasUSDCompras",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" = 'USD' AND SPOT."IdTipoOperacion" = 'VENSPT' AND 
					  SPOT."TipoComprobanteFacturacion" like 'BO%' THEN COALESCE(SPOT."MontoMonedaPrincipal",0)
				 ELSE 0 END) AS "TotalBoletasUSDVentas",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" !='USD' AND SPOT."IdMonedaSecundaria" != 'CLP' AND SPOT."IdTipoOperacion" = 'COMSPT' AND
					  SPOT."TipoComprobanteFacturacion" like 'BO%' THEN COALESCE(SPOT."MontoMonedaSecundaria",0)--COALESCE(SPOT."MontoMonedaPrincipal",0)
				 ELSE 0 END) AS "ComprasBoletasMXenUSD",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" !='USD' AND SPOT."IdMonedaSecundaria" != 'CLP' AND SPOT."IdTipoOperacion" = 'VENSPT' AND
					  SPOT."TipoComprobanteFacturacion" like 'BO%' THEN COALESCE(SPOT."MontoMonedaSecundaria",0)--COALESCE(SPOT."MontoMonedaPrincipal",0)
				 ELSE 0 END) AS "VentasBoletasMXenUSD",
		SUM(CASE WHEN SPOT."IdMonedaSecundaria"= 'CLP' AND SPOT."IdTipoOperacion" = 'COMSPT' AND 
					  SPOT."TipoComprobanteFacturacion" like 'BO%' 
					  THEN CASE WHEN TI."Nemotecnico" = 'DISTRI' AND v_ConfiguracionUtilidadPerdida = 'G4'
					  	THEN SPOT."TipoCambioCosto" * SPOT."MontoMonedaPrincipal" 
					  	ELSE COALESCE(SPOT."MontoMonedaSecundaria",0) END
				 ELSE 0 END) AS "TotalBoletasCLPCompras",
		SUM(CASE WHEN SPOT."IdMonedaSecundaria"= 'CLP' AND SPOT."IdTipoOperacion" = 'VENSPT' AND
					  SPOT."TipoComprobanteFacturacion" like 'BO%'
					  THEN CASE WHEN TI."Nemotecnico" = 'DISTRI' AND v_ConfiguracionUtilidadPerdida = 'G4'
					  	THEN SPOT."TipoCambioCosto" * SPOT."MontoMonedaPrincipal" 
					  	ELSE COALESCE(SPOT."MontoMonedaSecundaria",0) END
				 ELSE 0 END) AS "TotalBoletasCLPVentas",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" = 'USD' AND SPOT."IdTipoOperacion" = 'COMSPT' AND
					  SPOT."TipoComprobanteFacturacion" like 'F%' THEN COALESCE(SPOT."MontoMonedaPrincipal",0)
				 ELSE 0 END) AS "TotalFacturasUSDCompras",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" = 'USD' AND SPOT."IdTipoOperacion" = 'VENSPT' AND 
					  SPOT."TipoComprobanteFacturacion" like 'F%' THEN COALESCE(SPOT."MontoMonedaPrincipal",0)
				 ELSE 0 END) AS "TotalFacturasUSDVentas",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" !='USD' AND SPOT."IdMonedaSecundaria" != 'CLP' AND SPOT."IdTipoOperacion" = 'COMSPT' AND 
					  SPOT."TipoComprobanteFacturacion" like 'F%' THEN COALESCE(SPOT."MontoMonedaSecundaria",0)--COALESCE(SPOT."MontoMonedaPrincipal",0)
				 ELSE 0 END) AS "ComprasFacturasMXenUSD",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" !='USD' AND SPOT."IdMonedaSecundaria" != 'CLP' AND SPOT."IdTipoOperacion" = 'VENSPT' AND
					  SPOT."TipoComprobanteFacturacion" like 'F%' THEN COALESCE(SPOT."MontoMonedaSecundaria",0)--COALESCE(SPOT."MontoMonedaPrincipal",0)
				 ELSE 0 END) AS "VentasFacturasMXenUSD",
		SUM(CASE WHEN SPOT."IdMonedaSecundaria"= 'CLP' AND SPOT."IdTipoOperacion" = 'COMSPT' AND 
					  SPOT."TipoComprobanteFacturacion" like 'F%' 
					  THEN CASE WHEN TI."Nemotecnico" = 'DISTRI' AND v_ConfiguracionUtilidadPerdida = 'G4'
					  	THEN SPOT."TipoCambioCosto" * SPOT."MontoMonedaPrincipal" 
					  	ELSE COALESCE(SPOT."MontoMonedaSecundaria",0) END
				 ELSE 0 END) AS "TotalFacturasCLPCompras",
		SUM(CASE WHEN SPOT."IdMonedaSecundaria"= 'CLP' AND SPOT."IdTipoOperacion" = 'VENSPT' AND 
					  SPOT."TipoComprobanteFacturacion" like 'F%' 
					  THEN CASE WHEN TI."Nemotecnico" = 'DISTRI' AND v_ConfiguracionUtilidadPerdida = 'G4'
					  	THEN SPOT."TipoCambioCosto" * SPOT."MontoMonedaPrincipal" 
					  	ELSE COALESCE(SPOT."MontoMonedaSecundaria",0) END
				 ELSE 0 END) AS "TotalFacturasCLPVentas",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" !='USD' AND SPOT."IdMonedaSecundaria" != 'CLP' AND 
					  SPOT."IdTipoOperacion" = 'COMSPT' THEN COALESCE(SPOT."Margen",0)
				 ELSE 0 END) AS "MargenArbitrajeCompras",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" !='USD' AND SPOT."IdMonedaSecundaria" != 'CLP' AND 
					  SPOT."IdTipoOperacion" = 'VENSPT' THEN COALESCE(SPOT."Margen",0)
				 ELSE 0 END) AS "MargenArbitrajeVentas",
				 
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" = 'USD' AND SPOT."IdTipoOperacion" = 'COMSPT' THEN (v_DolarObservado - SPOT."TipoCambioCierre") * COALESCE(SPOT."MontoMonedaPrincipal",0)
				 WHEN SPOT."IdMonedaPrincipal" != 'USD' AND SPOT."IdMonedaSecundaria" = 'CLP' AND SPOT."IdTipoOperacion" = 'COMSPT' THEN
				 	(v_DolarObservado - SPOT."TipoCambioCierre") * COALESCE(SPOT."MontoMonedaPrincipalEquivalente",0)
				 ELSE 0 END) AS "ResultadoCompra",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" = 'USD' AND SPOT."IdTipoOperacion" = 'VENSPT' THEN (SPOT."TipoCambioCierre" - v_DolarObservado) * COALESCE(SPOT."MontoMonedaPrincipal",0)
				 WHEN SPOT."IdMonedaPrincipal" != 'USD' AND SPOT."IdMonedaSecundaria" = 'CLP' AND SPOT."IdTipoOperacion" = 'VENSPT' THEN
				 	(SPOT."TipoCambioCierre" - v_DolarObservado) * COALESCE(SPOT."MontoMonedaPrincipalEquivalente",0)
				 ELSE 0 END) AS "ResultadoVenta",
		SUM(CASE WHEN SPOT."IdMonedaPrincipal" = 'USD' AND SPOT."IdTipoOperacion" IN ('COMSPT', 'VENSPT') AND TI."Nemotecnico" = 'DISTRI' THEN ROUND(SPOT."Margen", 0) ELSE 0 END) AS "UtilidadPerdidaDistribucion"
				 
		INTO v_USDCompradosHoy, v_USDVendidosHoy, --v_ComprasMXenUSD, v_VentasMXenUSD, 
			 v_AbonosCtaCteUSD, v_CargosCtaCteUSD, 
			 v_TotalBoletasUSDCompras,	v_TotalBoletasUSDVentas, v_ComprasBoletasMXenUSD, v_VentasBoletasMXenUSD, 
			 v_TotalBoletasCLPCompras,	v_TotalBoletasCLPVentas,
			 v_TotalFacturasUSDCompras,	v_TotalFacturasUSDVentas, v_ComprasFacturasMXenUSD, v_VentasFacturasMXenUSD, 
			 v_TotalFacturasCLPCompras,	v_TotalFacturasCLPVentas,
			 v_MargenArbitrajeCompras,	v_MargenArbitrajeVentas,
			 v_ResultadoCompras, v_ResultadoVentas, v_UtilidadPerdidaDistribucion
	FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPOT
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" TI
		ON SPOT."IdInstrumento" = TI."IdInstrumento"
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" FAC
		ON (SPOT."IdOperacionSpot"		= FAC."IdTransaccion" AND 
			FAC."IdTipoProducto"		= 'MEME' AND 
			FAC."IdEstadoFacturacion"	= 'FAC' AND
			SPOT."IdMonedaPrincipal"	= FAC."IdMonedaSpot")
	LEFT JOIN (
			SELECT SPOT."IdOperacionSpot"
			FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPOT
			INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" FAC
				ON (SPOT."IdOperacionSpot"		= FAC."IdTransaccion" AND 
					FAC."IdTipoProducto"		= 'MEME' AND 
					FAC."IdEstadoFacturacion"	= 'FAC' AND
					SPOT."IdMonedaPrincipal"	= FAC."IdMonedaSpot")
			INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVNCR
				ON (FAC."IdComprobante"			> 0 AND 
					FAC."IdComprobante"			= MOVNCR."IdComprobanteReferencia" AND 
					MOVNCR."IdTipoComprobante"	IN ('NCE','NCR'))
			WHERE TO_CHAR(SPOT."FechaOperacion") = :p_Fecha
			  AND SPOT."Estado" = 'VIG') MOVNCR
		ON (SPOT."IdOperacionSpot" = MOVNCR."IdOperacionSpot")
	WHERE TO_CHAR(SPOT."FechaOperacion") = :p_Fecha
	  AND SPOT."Estado" = 'VIG'
	  AND (TI."Nemotecnico" = :p_Nemotecnico OR :p_Nemotecnico = '')
	  AND MOVNCR."IdOperacionSpot" IS NULL
	;
	
	v_DolarObservado				= CASE WHEN COALESCE(v_DolarObservado,0) = 0 THEN COALESCE(v_PrecioPosicionFinal,0) ELSE COALESCE(v_DolarObservado,0) END;
	v_PosicionInicial				= COALESCE(v_PosicionInicial,0);
	v_PrecioPosicionInicial			= COALESCE(v_PrecioPosicionInicial,0);
	v_PosicionInicialCLP			= COALESCE(v_PosicionInicialCLP,0);
	v_PrecioPosicionFinal			= COALESCE(v_PrecioPosicionFinal,0);
	v_X_PosicionInicialAyer			= COALESCE(v_X_PosicionInicialAyer,0);
	v_Y_PrecioPosicionInicialAyer	= COALESCE(v_Y_PrecioPosicionInicialAyer,0);
	v_USDCompradosHoy				= COALESCE(v_USDCompradosHoy,0);
	v_USDVendidosHoy				= COALESCE(v_USDVendidosHoy,0);
	v_AbonosCtaCteUSD				= COALESCE(v_AbonosCtaCteUSD,0);
	v_CargosCtaCteUSD				= COALESCE(v_CargosCtaCteUSD,0);
	v_TotalBoletasUSDCompras		= COALESCE(v_TotalBoletasUSDCompras,0);
	v_TotalBoletasUSDVentas			= COALESCE(v_TotalBoletasUSDVentas,0);
	v_ComprasBoletasMXenUSD			= COALESCE(v_ComprasBoletasMXenUSD,0);
	v_VentasBoletasMXenUSD			= COALESCE(v_VentasBoletasMXenUSD,0);
	v_TotalBoletasCLPCompras		= COALESCE(v_TotalBoletasCLPCompras,0);
	v_TotalBoletasCLPVentas			= COALESCE(v_TotalBoletasCLPVentas,0);
	v_TotalFacturasUSDCompras		= COALESCE(v_TotalFacturasUSDCompras,0);
	v_TotalFacturasUSDVentas		= COALESCE(v_TotalFacturasUSDVentas,0);
	v_ComprasFacturasMXenUSD		= COALESCE(v_ComprasFacturasMXenUSD,0);
	v_VentasFacturasMXenUSD			= COALESCE(v_VentasFacturasMXenUSD,0);
	v_TotalFacturasCLPCompras		= COALESCE(v_TotalFacturasCLPCompras,0);
	v_TotalFacturasCLPVentas		= COALESCE(v_TotalFacturasCLPVentas,0);
	v_MargenArbitrajeCompras		= COALESCE(v_MargenArbitrajeCompras,0);
	v_MargenArbitrajeVentas			= COALESCE(v_MargenArbitrajeVentas,0);
	v_ComprasMXenUSD				= v_ComprasBoletasMXenUSD + v_ComprasFacturasMXenUSD;
	v_VentasMXenUSD					= v_VentasBoletasMXenUSD + v_VentasFacturasMXenUSD;
	v_PosicionInicialCLP = v_PosicionInicial * v_PrecioPosicionInicial;
	--Totales
	v_A_TotalComprasEnUSPeso= v_TotalBoletasUSDCompras + v_TotalFacturasUSDCompras; --** a+f
	v_C_TotalVentasEnUSPeso	= v_TotalBoletasUSDVentas + v_TotalFacturasUSDVentas; --** c+g
	v_B_TotalComprasEnPeso	= v_TotalBoletasCLPCompras + v_TotalFacturasCLPCompras; --** b+i
	v_D_TotalVentasEnPeso	= v_TotalBoletasCLPVentas + v_TotalFacturasCLPVentas;   --** d+j
	
	IF v_USDCompradosHoy > 0 THEN
		IF v_PosicionInicial > 0 THEN 
			v_PrecioPromedioCompra = (v_TotalBoletasCLPCompras + v_TotalFacturasCLPCompras + v_PosicionInicialCLP) / (v_USDCompradosHoy + v_PosicionInicial);
		ELSE
			v_PrecioPromedioCompra = (v_TotalBoletasCLPCompras + v_TotalFacturasCLPCompras) / v_USDCompradosHoy;
		END IF;
	ELSE
		v_PrecioPromedioCompra = 0;
	END IF;
	v_PrecioPromedioCompra = ROUND(v_PrecioPromedioCompra,4);
	IF v_USDVendidosHoy > 0 THEN 
		IF v_PosicionInicial < 0  THEN 
			v_PrecioPromedioVenta = (ABS(v_TotalBoletasCLPVentas) + ABS(v_TotalFacturasCLPVentas) + ABS(v_PosicionInicialCLP)) / (ABS(v_USDVendidosHoy) + ABS(v_PosicionInicial));
		ELSE 
			v_PrecioPromedioVenta = (ABS(v_TotalBoletasCLPVentas) + ABS(v_TotalFacturasCLPVentas)) / ABS(v_USDVendidosHoy);
		END IF;
	ELSE
		v_PrecioPromedioVenta = 0;
	END IF;
	v_PrecioPromedioVenta = ROUND(v_PrecioPromedioVenta,4);
	v_UtilidadPerdidaPromedio	= (v_PrecioPromedioVenta - v_PrecioPromedioCompra) * v_USDVendidosHoy;
	v_PosicionFinal = v_PosicionInicial + (v_TotalBoletasUSDCompras - v_TotalBoletasUSDVentas) + (v_TotalFacturasUSDCompras - v_TotalFacturasUSDVentas);
	v_PesosEquivalentePosicion	= v_PosicionFinal * v_PrecioPosicionFinal;
	v_Z_PosicionInicialCLPAyer	= v_X_PosicionInicialAyer * v_Y_PrecioPosicionInicialAyer;

	IF v_A_TotalComprasEnUSPeso > 0 THEN
		v_E_PrecioPosicionFinal = v_B_TotalComprasEnPeso / v_A_TotalComprasEnUSPeso; --** B/A
	ELSE
		v_E_PrecioPosicionFinal = 0;
	END IF;

	v_F_SaldoEnPesos	= v_Z_PosicionInicialCLPAyer + v_B_TotalComprasEnPeso - v_D_TotalVentasEnPeso;  --** Z+B-D
	v_G_PosValorizada	= (v_X_PosicionInicialAyer + v_A_TotalComprasEnUSPeso - v_C_TotalVentasEnUSPeso) * v_E_PrecioPosicionFinal; --** (X+A-C)*E
	v_H_UtilPerDelDia	= v_G_PosValorizada - v_F_SaldoEnPesos; --** G-F
	v_NuevaPosicionFinalUSPeso	= v_X_PosicionInicialAyer + v_A_TotalComprasEnUSPeso - v_C_TotalVentasEnUSPeso;  --** X+A-C

	--RESULTADOS
	v_MovDiarioCompras	= v_USDCompradosHoy;
	--v_MovDiarioVentas	= -v_USDVendidosHoy;
	v_MovDiarioVentas	= v_USDVendidosHoy;
	
	IF v_PosicionInicial >= 0 THEN
		--v_ResultadoCompras	= (v_DolarObservado - v_PrecioPromedioCompra) * (v_USDCompradosHoy + v_PosicionInicial);
		--v_ResultadoVentas	= (v_DolarObservado - v_PrecioPromedioVenta) * v_USDVendidosHoy;
		v_ResultadoCompras = v_ResultadoCompras + v_PosicionInicial;
	ELSE
		--v_ResultadoCompras	= (v_DolarObservado - v_PrecioPromedioCompra) * v_USDCompradosHoy;
		--v_ResultadoVentas	= (v_DolarObservado - v_PrecioPromedioVenta) * (v_USDVendidosHoy + ABS(:v_PosicionInicial));
		v_ResultadoVentas = v_ResultadoVentas + ABS(:v_PosicionInicial);
	END IF;
	--v_UtilidadPerdidaDolarObservado	= v_ResultadoCompras - v_ResultadoVentas;
	
	v_ResultadoPrecioPosicionInicial = v_PesosEquivalentePosicion - v_PosicionInicialCLP;

	--calculo utilidad/perdida G3
   	IF v_PosicionInicial > 0 THEN
   		v_UtilidadPerdidaDolarObservado = ((v_DolarObservado - v_PrecioPromedioCompra) * (v_PosicionInicial + v_A_TotalComprasEnUSPeso)) -
   									      ((v_DolarObservado - v_PrecioPromedioVenta) * v_C_TotalVentasEnUSPeso);
   	ELSEIF v_PosicionInicial < 0 THEN
   		v_UtilidadPerdidaDolarObservado = ((v_DolarObservado - v_PrecioPromedioCompra) * v_A_TotalComprasEnUSPeso) -
   							              ((v_DolarObservado - v_PrecioPromedioVenta) * (abs(v_PosicionInicial)  + v_C_TotalVentasEnUSPeso));
   	ELSE 
   		v_UtilidadPerdidaDolarObservado = ((v_DolarObservado - v_PrecioPromedioCompra) * v_A_TotalComprasEnUSPeso) -
   								          ((v_DolarObservado - v_PrecioPromedioVenta) * v_C_TotalVentasEnUSPeso);
   	END IF;
   	
			
	--v_UtilidadPerdidaDolarObservado    =v_ResultadoCompra + v_ResultadoVentas;
	IF v_ConfiguracionUtilidadPerdida = 'G4' THEN
	
	  	IF (v_PosicionFinal >= 0) THEN 
		
	  		v_UtilidadPerdidaPosicionFinal = (v_PrecioPosicionFinal - v_PrecioPromedioCompra) * v_PosicionFinal;
	
	    ELSE       
	
	        v_UtilidadPerdidaPosicionFinal = (v_PrecioPromedioVenta -v_PrecioPosicionFinal) * -1 * v_PosicionFinal;
	
	   	END IF;
	   
		IF (v_PosicionInicial >= 0) AND (v_PosicionFinal >= 0) THEN
		
		    v_UtilidadPerdidaPuntas = ((v_USDVendidosHoy * (v_PrecioPromedioVenta - v_PrecioPromedioCompra)) + v_UtilidadPerdidaPosicionFinal);
		
		ELSEIF (v_PosicionInicial >= 0) AND (v_PosicionFinal <  0) THEN
		
		    v_UtilidadPerdidaPuntas = (((v_USDVendidosHoy + v_PosicionFinal) * (v_PrecioPromedioVenta - v_PrecioPromedioCompra)) + v_UtilidadPerdidaPosicionFinal);
		
		ELSEIF (v_PosicionInicial <  0) AND (v_PosicionFinal >= 0) THEN
		
		    v_UtilidadPerdidaPuntas = (((v_USDVendidosHoy - v_PosicionInicial) * (v_PrecioPromedioVenta - v_PrecioPromedioCompra)) + v_UtilidadPerdidaPosicionFinal);
		
		ELSEIF (v_PosicionInicial <  0) AND (v_PosicionFinal < 0) THEN
		
		    v_UtilidadPerdidaPuntas = ((v_USDCompradosHoy * (v_PrecioPromedioVenta - v_PrecioPromedioCompra)) + v_UtilidadPerdidaPosicionFinal);
		
		END IF;
		
		v_UtilidadPerdidaDolarObservado = v_UtilidadPerdidaDistribucion + v_UtilidadPerdidaPuntas;
		
    END IF; 
	
	v_out_DetalleResultados =
		SELECT
			 'DolarObservado' as "Codigo"
			,'Dolar Observado del '||TO_CHAR(p_Fecha,'dd-MM-yyyy') as "Descripcion"
			,v_DolarObservado as "Valor"
		FROM dummy
		UNION ALL
		SELECT
			 'PosicionInicial' as "Codigo"
			,'Posicion Inicial' as "Descripcion"
			,v_PosicionInicial as "Valor"
		FROM dummy
		UNION ALL
		SELECT
			 'PrecioPosicionInicial' as "Codigo"
			,'Precio Posicion Inicial' as "Descripcion"
			,v_PrecioPosicionInicial as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'PosicionInicialCLP' as "Codigo"
			,'Posicion Inicial CLP' as "Descripcion"
			,v_PosicionInicialCLP as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'UtilidadPerdidaPromedio' as "Codigo"
			,'Utilidad/Perdida (Promedio)' as "Descripcion"
			,ROUND(v_UtilidadPerdidaPromedio,0) as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'PrecioPromedioVenta' as "Codigo"
			,'Precio Promedio Venta' as "Descripcion"
			,v_PrecioPromedioVenta as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'PrecioPromedioCompra' as "Codigo"
			,'Precio Promedio Compra' as "Descripcion"
			,v_PrecioPromedioCompra as "Valor"
		FROM dummy 
		UNION ALL
		SELECT 'PrecioPosicionFinal' as "Codigo"
			,'Precio Posicion Final' as "Descripcion"
			,v_PrecioPosicionFinal as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'PosicionFinal' as "Codigo"
			,'Posicion Final' as "Descripcion"
			,v_PosicionFinal as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'v_PesosEquivalentePosicion' as "Codigo"
			,'Pesos Equivalente Posicion' as "Descripcion"
			,v_PesosEquivalentePosicion as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'USDCompradosHoy' as "Codigo"
			,'USD Comprados Hoy' as "Descripcion"
			,v_USDCompradosHoy as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'USDVendidossHoy' as "Codigo"
			,'USD Vendidos Hoy' as "Descripcion"
			,v_USDVendidosHoy as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'ComprasMXenUSD' as "Codigo"
			,'Compras Otras Monedas expresadas en USD' as "Descripcion"
			,v_ComprasMXenUSD as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'VentasMXenUSD' as "Codigo"
			,'Ventas Otras Monedas expresadas en USD' as "Descripcion"
			,v_VentasMXenUSD as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'AbonosCtaCteUSD' as "Codigo"
			,'Abonos Cta Cte USD' as "Descripcion"
			,v_AbonosCtaCteUSD as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'CargosCtaCteUSD' as "Codigo"
			,'Cargos Cta Cte USD' as "Descripcion"
			,v_CargosCtaCteUSD as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'TotalBoletasUSDCompras' as "Codigo"
			,'Total X Concepto Boletas USD Compras [a]' as "Descripcion"
			,v_TotalBoletasUSDCompras as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'TotalBoletasUSDVentas' as "Codigo"
			,'Total X Concepto Boletas USD Ventas [c]' as "Descripcion"
			,v_TotalBoletasUSDVentas as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'ComprasBoletasMXenUSD' as "Codigo"
			,'Compras Boletas Otras Monedas expresadas en USD [a]' as "Descripcion"
			,v_ComprasBoletasMXenUSD as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'VentasBoletasMXenUSD' as "Codigo"
			,'Ventas Boletas Otras Monedas expresadas en USD [c]' as "Descripcion"
			,v_VentasBoletasMXenUSD as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'TotalBoletasCLPCompras' as "Codigo"
			,'Total X Concepto Boletas CLP Compras [b]' as "Descripcion"
			,ROUND(v_TotalBoletasCLPCompras) as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'TotalBoletasCLPVentas' as "Codigo"
			,'Total X Concepto Boletas CLP Ventas [d]' as "Descripcion"
			,ROUND(v_TotalBoletasCLPVentas) as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'TotalFacturasUSDCompras' as "Codigo"
			,'Total X Concepto Facturas USD Compras [a]' as "Descripcion"
			,v_TotalFacturasUSDCompras as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'TotalFacturasUSDVentas' as "Codigo"
			,'Total X Concepto Facturas USD Ventas [c]' as "Descripcion"
			,v_TotalFacturasUSDVentas as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'ComprasFacturasMXenUSD' as "Codigo"
			,'Compras facturas otras Monedas expresadas en USD [a]' as "Descripcion"
			,v_ComprasFacturasMXenUSD as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'VentasFacturasMXenUSD' as "Codigo"
			,'Ventas facturas otras Monedas expresadas en USD [c]' as "Descripcion"
			,v_VentasFacturasMXenUSD as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'TotalFacturasCLPCompras' as "Codigo"
			,'Total X Concepto Facturas CLP Compras [b]' as "Descripcion"
			,ROUND(v_TotalFacturasCLPCompras) as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'TotalFacturasCLPVentas' as "Codigo"
			,'Total X Concepto Facturas CLP Ventas [d]' as "Descripcion"
			,ROUND(v_TotalFacturasCLPVentas) as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'X_PosicionInicialAyer' as "Codigo"
			,'[X] Posicion Final US$ del '||:varFechaAnterior as "Descripcion" --** Agregar fecha anterior
			,v_X_PosicionInicialAyer as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'Y_PrecioPosicionInicialAyer' as "Codigo"
			,'[Y] Precio Pos. Final $ del '||:varFechaAnterior as "Descripcion" --** Agregar fecha anterior
			,v_Y_PrecioPosicionInicialAyer as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'Z_PosicionInicialCLPAyer' as "Codigo"
			,'[Z] Posicion Final $ (X * Y) del '||:varFechaAnterior as "Descripcion" --** Agregar fecha anterior
			,v_Z_PosicionInicialCLPAyer as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'A_TotalComprasEnUSPeso' as "Codigo"
			,'[A] Total Compras en US$ (Suma [a]+[f])' as "Descripcion"
			,v_A_TotalComprasEnUSPeso as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'C_TotalVentasEnUSPeso' as "Codigo"
			,'[C] Total Ventas en US$ (Suma [c]+[g])' as "Descripcion"
			,v_C_TotalVentasEnUSPeso as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'B_TotalComprasEnPeso' as "Codigo"
			,'[B] Total Compras en $ (Suma [b]+[I])' as "Descripcion"
			,v_B_TotalComprasEnPeso as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'D_TotalVentasEnPeso' as "Codigo"
			,'[D] Total Ventas en $ (Suma [d]+[J])' as "Descripcion"
			,v_D_TotalVentasEnPeso as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'E_PrecioPosicionFinal' as "Codigo"
			,'[E] Precio Posicion Final ([B]/[A])' as "Descripcion"
			,ROUND(v_E_PrecioPosicionFinal,4) as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'F_SaldoEnPesos' as "Codigo"
			,'[F] Saldo en Pesos ([Z]+[B]-[D])' as "Descripcion"
			,v_F_SaldoEnPesos as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'G_PosValorizada' as "Codigo"
			,'[G] Pos. Valorizada ([X]+[A]-[C])*[E]' as "Descripcion"
			,ROUND(v_G_PosValorizada,0) as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'H_UtilPerDelDia' as "Codigo"
			,'[H] Util/Perd. del día ([G]-[F])' as "Descripcion"
			,ROUND(v_H_UtilPerDelDia,0) as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'NuevaPosicionFinalUS' as "Codigo"
			,'Nueva Posicion Final US$, [X + A - C]' as "Descripcion"
			,v_NuevaPosicionFinalUSPeso as "Valor"
		FROM dummy 
		UNION ALL
		SELECT
			 'UtilidadPerdidaDolarObservado' as "Codigo"
			,'Utilidad/Perdida (Dolar Observado)' as "Descripcion"
			,ROUND(v_UtilidadPerdidaDolarObservado,0) as "Valor"
		FROM dummy
		UNION ALL
		SELECT
			 'MargenArbitrajeCompras' as "Codigo"
			,'Margen Arbitrajes Compras' as "Descripcion"
			,v_MargenArbitrajeCompras as "Valor"
		FROM dummy
		UNION ALL
		SELECT
			 'MargenArbitrajeVentas' as "Codigo"
			,'Margen Arbitrajes Ventas' as "Descripcion"
			,v_MargenArbitrajeVentas as "Valor"
		FROM dummy
		;
	
	
	SELECT COALESCE(MON."MontoDecimales",4) INTO v_CantidadDecimales
	FROM "BSCL.Plataforma.BD::PLA.MonedaFormato" MON
	WHERE MON."IdUsoMonedaFormato" = 'FWD'
	  AND MON."IdMoneda" = 'USD'
	  AND MON."IdTipoOperacion" = '';
	
	UPDATE "BSCL.Mesa.BD::SPT.ResultadoContableSpot"
	SET "IdEstado" = 'INA'
	WHERE "Fecha" = :p_Fecha;
	
	INSERT INTO "BSCL.Mesa.BD::SPT.ResultadoContableSpot"
	("IdResultadoContableSpot", "Fecha", "FechaIngresoRegistro", "DolarObservado", "PosicionInicial", "PrecioPosicionInicial", "PosicionInicialCLP", "UtilidadPerdidaPromedio", "PrecioPromedioVenta"
	, "PrecioPromedioCompra", "PrecioPosicionFinal", "PosicionFinal", "PesosEquivalentePosicion", "USDCompradosHoy", "USDVendidosHoy", "ComprasMXenUSD", "VentasMXenUSD", "AbonosCtaCteUSD"
	, "CargosCtaCteUSD", "TotalBoletasUSDCompras", "TotalBoletasUSDVentas", "ComprasBoletasMXenUSD", "VentasBoletasMXenUSD", "TotalBoletasCLPCompras", "TotalBoletasCLPVentas", "TotalFacturasUSDCompras"
	, "TotalFacturasUSDVentas", "ComprasFacturasMXenUSD", "VentasFacturasMXenUSD", "TotalFacturasCLPCompras", "TotalFacturasCLPVentas", "XPosicionInicialAyer", "YPrecioPosicionInicialAyer", "ZPosicionInicialCLPAyer"
	, "ATotalComprasEnUSPeso", "CTotalVentasEnUSPeso", "BTotalComprasEnPeso", "DTotalVentasEnPeso", "EPrecioPosicionFinal", "FSaldoEnPesos", "GPosValorizada", "HUtilPerDelDia", "NuevaPosicionFinalUS", "UtilidadPerdidaDolarObservado"
	, "MargenArbitrajeCompras", "MargenArbitrajeVentas", "SpreadCompra", "SpreadVenta", "ResultadoCompra", "ResultadoVenta", "UtilidadPerdidaDolObs", "IdEstado")
	VALUES("BSCL.Mesa.BD.Secuencias::SPT.IdResultadoContableSpot".NEXTVAL, :p_Fecha, CURRENT_UTCTIMESTAMP, v_DolarObservado, v_PosicionInicial, v_PrecioPosicionInicial, v_PosicionInicialCLP, v_UtilidadPerdidaPromedio, v_PrecioPromedioVenta
	, v_PrecioPromedioCompra, v_PrecioPosicionFinal, v_PosicionFinal, v_PesosEquivalentePosicion, v_USDCompradosHoy, v_USDVendidosHoy, v_ComprasMXenUSD, v_VentasMXenUSD, v_AbonosCtaCteUSD
	, v_CargosCtaCteUSD, v_TotalBoletasUSDCompras, v_TotalBoletasUSDVentas, v_ComprasBoletasMXenUSD, v_VentasBoletasMXenUSD, ROUND(v_TotalBoletasCLPCompras), ROUND(v_TotalBoletasCLPVentas), v_TotalFacturasUSDCompras
	, v_TotalFacturasUSDVentas, v_ComprasFacturasMXenUSD, v_VentasFacturasMXenUSD, ROUND(v_TotalFacturasCLPCompras), ROUND(v_TotalFacturasCLPVentas), v_X_PosicionInicialAyer, v_Y_PrecioPosicionInicialAyer, v_Z_PosicionInicialCLPAyer
	, v_A_TotalComprasEnUSPeso, v_C_TotalVentasEnUSPeso, v_B_TotalComprasEnPeso, v_D_TotalVentasEnPeso, v_E_PrecioPosicionFinal, v_F_SaldoEnPesos, v_G_PosValorizada, v_H_UtilPerDelDia, v_NuevaPosicionFinalUSPeso, v_UtilidadPerdidaDolarObservado
	, v_MargenArbitrajeCompras, v_MargenArbitrajeVentas, (v_DolarObservado - v_PrecioPromedioCompra) * v_A_TotalComprasEnUSPeso, (v_PrecioPromedioVenta - v_DolarObservado) * v_C_TotalVentasEnUSPeso, v_ResultadoCompras, v_ResultadoVentas, 
	CASE WHEN v_ConfiguracionUtilidadPerdida = 'G1' THEN v_UtilidadPerdidaPromedio
	 	 WHEN v_ConfiguracionUtilidadPerdida = 'G3' THEN v_UtilidadPerdidaDolarObservado
	 	 WHEN v_ConfiguracionUtilidadPerdida = 'G4' THEN v_UtilidadPerdidaDolarObservado END, 'ACT');
	
	INSERT INTO "BSCL.Plataforma.BD::PAU.ControlProceso" 
	("IdEmpresa","IdControlProceso","CodigoModulo","CodigoProceso","FechaSistema","FechaRegistro","CodigoUsuario") 
	VALUES (:p_IdEmpresa,"BSCL.Plataforma.BD.Secuencias::IdControlProceso".NEXTVAL,'MSP','CALCULO_MOPOSI',:p_Fecha,CURRENT_UTCTIMESTAMP,'');
	
	SELECT *
		,v_DolarObservado		as "DolarObservado"
		,v_PrecioPromedioCompra	as "PrecioPromedioCompra"
		,v_PrecioPromedioVenta	as "PrecioPromedioVenta"
		,ROUND((v_DolarObservado - v_PrecioPromedioCompra) * v_A_TotalComprasEnUSPeso,0) as "SpreadCompra"
		,ROUND((v_PrecioPromedioVenta - v_DolarObservado) * v_C_TotalVentasEnUSPeso,0) as "SpreadVenta"
		,v_PosicionInicial		as "PosicionInicial"
		,v_MovDiarioCompras		as "USDCompradosHoy"
		,v_MovDiarioVentas		as "USDVendidosHoy"
		,v_ResultadoCompras		as "ResultadoCompra"
		,v_ResultadoVentas		as "ResultadoVenta"
		,CASE WHEN v_ConfiguracionUtilidadPerdida = 'G1' THEN v_UtilidadPerdidaPromedio
	 		  WHEN v_ConfiguracionUtilidadPerdida = 'G3' THEN ROUND(v_UtilidadPerdidaDolarObservado,0)
	 		  WHEN v_ConfiguracionUtilidadPerdida = 'G4' THEN v_UtilidadPerdidaDolarObservado END as "UtilidadPerdidaDolObs"
	 	,CASE WHEN "Codigo" LIKE '%CLP%' THEN 0 ELSE v_CantidadDecimales END AS "CantidadDecimales"
	FROM :v_out_DetalleResultados;
	
END;
