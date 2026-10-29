SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ResumenMovimientos";
GO

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ResumenMovimientos" (
    IN p_IdModulo				NVARCHAR(40),
    IN p_IdUsuario				BIGINT,
    IN p_IdEmpresa				BIGINT,
    IN p_FechaDesde				DATE,
    IN p_FechaHasta				DATE,
    IN p_Datos					TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT),
    IN p_IdProductoAgrupador	BIGINT DEFAULT NULL
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"

AS
BEGIN
	
	DECLARE v_IdCliente				BIGINT;
	DECLARE v_IdPortafolio			BIGINT;
	DECLARE v_s_InformacionContador, v_s_AdvertenciasContador,v_s_ErroresContador  BIGINT := 0;
	DECLARE v_DiaAnterior 			DATE;
	
	CREATE LOCAL TEMPORARY TABLE #tbl_Salida(
		"TipoMovimiento"    NVARCHAR(40)
		,"IdCliente" 		BIGINT
	   	,"IdPortafolio" 	BIGINT
	   	,"Moneda" 			NVARCHAR(3)
	   	,"Cargo" 			DECIMAL(26,8)
	   	,"Abono" 			DECIMAL(26,8)
	   	,"SaldoInicial" 	DECIMAL(26,8)
	   	,"SaldoFinal" 		DECIMAL(26,8)
	);
	
	
	SELECT top 1 "IdCliente", "IdPortafolio"
	INTO v_IdCliente, v_IdPortafolio
	FROM :p_Datos;
	
	v_DiaAnterior = ADD_DAYS(:p_FechaDesde,-1);
	v_tbl_ConsultaSaldo = SELECT NULL AS "IdEjecutivo", "IdPortafolio", NULL AS "IdMoneda" FROM :p_Datos;
	
	v_ConfiguracionDelSistema =
		SELECT
			"IdEmpresa"
		   ,"PLA.LocalizacionSistema"
		   ,"PLA.MonedaSistema"
		   ,CASE WHEN "PLA.MonedaAdicional" IS NULL OR "PLA.MonedaAdicional" = '' THEN 'USD' ELSE "PLA.MonedaAdicional" END "PLA.MonedaAdicional"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot"
		WHERE "IdEmpresa" = :p_IdEmpresa;
	
	
	-- Consulta saldo inicial
	CALL "BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversion"(
		p_IdEmpresa 	=> :p_IdEmpresa,
		p_IdUsuario 	=> :p_IdUsuario,
		p_IdModulo 		=> :p_IdModulo,
		p_UsaReglasNegocio => 'N',
		p_CuentaInversionAConsultar => :v_tbl_ConsultaSaldo,
		p_FechaDesde 	=> :v_DiaAnterior,
		p_FechaHasta 	=> :v_DiaAnterior,
		p_IncluyeOtrosSaldos => 'S',
		
		p_out_SaldoInicialFinal => tbl_s_SaldoInicialFinal,
		p_out_SaldoPorFechaEfectiva => tbl_s_SaldoPorFechaEfectiva,
		p_out_SaldoPorFechaMovimiento => tbl_s_SaldoPorFechaMovimiento,
		
		p_out_InformacionContador => v_s_InformacionContador,
		p_out_InformacionDetalle => tbl_s_InformacionDetalle,
		p_out_AdvertenciasContador => v_s_AdvertenciasContador,
		p_out_AdvertenciasDetalle => tbl_s_AdvertenciasDetalle,
		p_out_ErroresContador => v_s_ErroresContador,
		p_out_ErroresDetalle => tbl_s_ErroresDetalle
	);
	

	v_TipoMovimiento =
	SELECT 'Operaciones' as "TipoMovimiento", "IdMoneda" FROM :tbl_s_SaldoPorFechaEfectiva
	UNION ALL
	SELECT 'Dividendos / Cupones' as "TipoMovimiento", "IdMoneda" FROM :tbl_s_SaldoPorFechaEfectiva
	UNION ALL
	SELECT 'Movimiento Tesorería' as "TipoMovimiento", "IdMoneda" FROM :tbl_s_SaldoPorFechaEfectiva
	UNION ALL
	SELECT 'Movimiento Cuenta Inversión' as "TipoMovimiento", "IdMoneda" FROM :tbl_s_SaldoPorFechaEfectiva
	UNION ALL
	SELECT 'Admin. Liquidez' as "TipoMovimiento", "IdMoneda" FROM :tbl_s_SaldoPorFechaEfectiva;
	
	
	v_MovimientosCuentaInversion =
	SELECT
		CA."IdCliente"
	   ,CA."IdPortafolio"
	   ,CA."IdCargoAbonoMovimiento"
	   ,CA."IdConcepto"
	   ,C."NombreConcepto"
	   ,C."Origen"
	   ,CA."IdModulo"
	   ,CA."IdTipoOperacion"
	   ,T."Nombre" AS "NombreTipoOperacion"
	   ,CA."IdMoneda"
	   ,CASE WHEN CA."IdTipoMovimiento" = 'MOVCAR' THEN CA."Monto" ELSE 0 END "Cargo"
	   ,CASE WHEN CA."IdTipoMovimiento" = 'MOVABN' THEN CA."Monto" ELSE 0 END "Abono"
	   ,"SaldoPorFechaMovimiento"
	   ,CA."IdEmpresa"
	FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarTablaCargoAbonoMovimiento" AS CA
	INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" AS C ON (C."IdConcepto" = CA."IdConcepto")
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS T ON (T."IdTipoOperacion" = CA."IdTipoOperacion")
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CA."IdPortafolio" AND Portafolio."IdCliente" = CA."IdCliente"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	WHERE
		--CA."FechaSistema" BETWEEN :p_FechaDesde AND :p_FechaHasta
		CA."FechaEfectivaMovimiento" BETWEEN :p_FechaDesde AND :p_FechaHasta
	AND CA."IdEstado" = 'CRT'
	AND (:v_IdCliente IS NULL OR CA."IdCliente" = :v_IdCliente)
	AND (:v_IdPortafolio IS NULL OR CA."IdPortafolio" = :v_IdPortafolio)
	AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador);
	
	
	v_Movimientos =
	WITH
	Operaciones AS
	(
		SELECT
			"IdCliente"
		   ,"IdPortafolio"
		   ,'Operaciones' AS "TipoMovimiento"
		   ,CASE
		   		WHEN M."IdMoneda" = C."PLA.MonedaSistema" THEN "PLA.MonedaSistema"
		   		WHEN M."IdMoneda" != C."PLA.MonedaSistema" AND C."PLA.LocalizacionSistema" = 'PE' THEN "PLA.MonedaAdicional"
		   		WHEN M."IdMoneda" != C."PLA.MonedaSistema" THEN M."IdMoneda"
		   	END "Moneda"
		   ,"Cargo"
		   ,"Abono"
		FROM :v_MovimientosCuentaInversion AS M
		INNER JOIN :v_ConfiguracionDelSistema AS C ON C."IdEmpresa" = M."IdEmpresa"
		WHERE
			"IdModulo" NOT IN ('CTA','CUS','TES')
	),
	CuentaInversion AS
	(
		SELECT
			"IdCliente"
		   ,"IdPortafolio"
		   ,'Movimiento Cuenta Inversión' AS "TipoMovimiento"
		   ,CASE
		   		WHEN M."IdMoneda" = C."PLA.MonedaSistema" THEN "PLA.MonedaSistema"
		   		WHEN M."IdMoneda" != C."PLA.MonedaSistema" AND C."PLA.LocalizacionSistema" = 'PE' THEN "PLA.MonedaAdicional"
		   		WHEN M."IdMoneda" != C."PLA.MonedaSistema" THEN M."IdMoneda"
			END "Moneda"
		   ,"Cargo"
		   ,"Abono"
		FROM :v_MovimientosCuentaInversion AS M
		INNER JOIN :v_ConfiguracionDelSistema AS C ON C."IdEmpresa" = M."IdEmpresa"
		WHERE
			"IdModulo" IN ('CTA')
	),
	Tesoreria AS
	(
		SELECT
			"IdCliente"
		   ,"IdPortafolio"
		   ,'Movimiento Tesorería' AS "TipoMovimiento"
		   ,CASE
		   		WHEN M."IdMoneda" = C."PLA.MonedaSistema" THEN "PLA.MonedaSistema"
		   		WHEN M."IdMoneda" != C."PLA.MonedaSistema" AND C."PLA.LocalizacionSistema" = 'PE' THEN "PLA.MonedaAdicional"
		   		WHEN M."IdMoneda" != C."PLA.MonedaSistema" THEN M."IdMoneda"
			END "Moneda"
		   ,"Cargo"
		   ,"Abono"
		FROM :v_MovimientosCuentaInversion AS M
		INNER JOIN :v_ConfiguracionDelSistema AS C ON C."IdEmpresa" = M."IdEmpresa"
		WHERE
			"IdModulo" IN ('TES')
	),
	DividendosCupones AS
	(
		SELECT
			"IdCliente"
		   ,"IdPortafolio"
		   ,'Dividendos / Cupones' AS "TipoMovimiento"
		   ,CASE
		   		WHEN M."IdMoneda" = C."PLA.MonedaSistema" THEN "PLA.MonedaSistema"
		   		WHEN M."IdMoneda" != C."PLA.MonedaSistema" AND C."PLA.LocalizacionSistema" = 'PE' THEN "PLA.MonedaAdicional"
		   		WHEN M."IdMoneda" != C."PLA.MonedaSistema" THEN M."IdMoneda"
		   	END "Moneda"
		   ,"Cargo"
		   ,"Abono"
		FROM :v_MovimientosCuentaInversion AS M
		INNER JOIN :v_ConfiguracionDelSistema AS C ON C."IdEmpresa" = M."IdEmpresa"
		WHERE
			"IdModulo" IN ('CUS')
		AND (M."NombreTipoOperacion" LIKE 'DIVIDENDO%'  OR  M."NombreTipoOperacion" LIKE '%CUPON%')
	)
	SELECT "IdCliente","IdPortafolio","TipoMovimiento","Moneda","Cargo","Abono" FROM Operaciones AS O
	UNION ALL
	SELECT "IdCliente","IdPortafolio","TipoMovimiento","Moneda","Cargo","Abono" FROM CuentaInversion AS T
	UNION ALL
	SELECT "IdCliente","IdPortafolio","TipoMovimiento","Moneda","Cargo","Abono" FROM Tesoreria AS T
	UNION ALL
	SELECT "IdCliente","IdPortafolio","TipoMovimiento","Moneda","Cargo","Abono" FROM DividendosCupones AS DC;
	
/*	
	SELECT
		TFP."TipoMovimiento"
	   ,M."IdCliente"
	   ,M."IdPortafolio"
	   ,M."Moneda"
	   ,SUM(M."Cargo") AS "Cargo"
	   ,SUM(M."Abono") AS "Abono"
	   ,CAST(0 AS DECIMAL(26,8)) AS "SaldoInicial"
	   ,SUM(M."Abono") - SUM(M."Cargo")  AS "SaldoFinal"
	FROM :v_TipoMovimiento AS TFP
	INNER JOIN :v_Movimientos AS M ON M."TipoMovimiento" = TFP."TipoMovimiento"
	GROUP BY
		TFP."TipoMovimiento"
	   ,M."IdCliente"
	   ,M."IdPortafolio"
	   ,M."Moneda"
	ORDER BY
		M."Moneda"
	   ,TFP."TipoMovimiento";
*/

	
	
	INSERT INTO #tbl_Salida 
	SELECT
		TFP."TipoMovimiento"
	   ,cta."IdCliente"
	   ,cta."IdPortafolio"
	   ,cta."IdMoneda" AS "Moneda"
	   ,CAST(IFNULL(SUM(M."Cargo"),0) AS DECIMAL(26,8)) AS "Cargo"
	   ,CAST(IFNULL(SUM(M."Abono"),0) AS DECIMAL(26,8)) AS "Abono"
	   ,MAX(CAST(IFNULL(cta."SaldoDisponible",0) AS DECIMAL(26,8))) AS "SaldoInicial"
	   ,CAST( (MAX(IFNULL(cta."SaldoDisponible",0)) +   (IFNULL(SUM(M."Abono"),0) - IFNULL(SUM(M."Cargo"),0))) AS DECIMAL(26,8))  AS "SaldoFinal"
	FROM :tbl_s_SaldoPorFechaEfectiva cta
	LEFT JOIN :v_TipoMovimiento AS TFP ON TFP."IdMoneda" = cta."IdMoneda"
	LEFT JOIN :v_Movimientos AS M ON M."TipoMovimiento" = TFP."TipoMovimiento" AND M."Moneda" = cta."IdMoneda"
	GROUP BY
		TFP."TipoMovimiento"
	   ,cta."IdCliente"
	   ,cta."IdPortafolio"
	   ,cta."IdMoneda"
	ORDER BY
		cta."IdMoneda"
	   ,TFP."TipoMovimiento";
	   
	   
	UPDATE #tbl_salida ini SET "SaldoFinal" = "SaldoInicial" + (SELECT IFNULL(SUM("Abono"),0) - IFNULL(SUM("Cargo"),0) FROM #tbl_salida misal WHERE misal."Moneda" = ini."Moneda" GROUP BY "Moneda" );
	
    SELECT "TipoMovimiento", "IdCliente", "IdPortafolio", "Moneda", "Cargo", "Abono", "SaldoInicial", "SaldoFinal" 
    FROM #tbl_Salida
    WHERE 
    	("SaldoInicial" <> 0 OR "SaldoFinal" <> 0)  
    	AND IFNULL("TipoMovimiento",'') <> ''
    ;
    
    DROP TABLE #tbl_Salida;
	
END;
GO