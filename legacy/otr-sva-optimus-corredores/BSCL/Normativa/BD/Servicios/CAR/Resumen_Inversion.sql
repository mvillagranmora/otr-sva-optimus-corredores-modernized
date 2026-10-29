
SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.Resumen_Inversion";
GO

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.Resumen_Inversion" (
    IN 	p_IdModulo 				NVARCHAR(40),
    IN 	p_IdUsuario 			BIGINT,
    IN 	p_IdEmpresa 			BIGINT,
    IN 	p_FechaDesde 			DATE,
    IN	p_FechaHasta 			DATE,
    IN	p_IdTrabajo				BIGINT,
    IN 	p_Datos 				TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT),
    IN  p_IdProductoAgrupador	BIGINT DEFAULT NULL,
    OUT p_out_ResumenInversion	TABLE ( "IdCliente" 			BIGINT,
								        "IdPortafolio"			BIGINT,
								        "Grupo" 				NVARCHAR(1024),
		       		 					"Item" 					NVARCHAR(1024),
		       		 					"IdMonedaOperacion"		NVARCHAR(3),
		       		 					"MontoMonedaOperacion"	DECIMAL(26,8),
		       		 					"IdMonedaSistema"		NVARCHAR(3),
		       		 					"MontoMonedaSistema" 	DECIMAL(26,8),
										"TotalMonedaSistema" 	DECIMAL(26,8),
										"IdMonedaAdicional"		NVARCHAR(3),
									    "MontoMonedaAdicional" 	DECIMAL(28,6),	--En Moneda adicional del sistema
										"TotalMonedaAdicional" 	DECIMAL(28,6) ) DEFAULT EMPTY
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"

AS
BEGIN
	
	DECLARE v_IdCliente				BIGINT;
	DECLARE v_IdPortafolio			BIGINT;
	
	DECLARE v_LocalizacionSistema 	NVARCHAR(3);
	DECLARE v_MonedaSistema 		NVARCHAR(3);
	DECLARE v_MonedaAdicional 		NVARCHAR(3);
	
	SELECT "PLA.LocalizacionSistema", "PLA.MonedaSistema", CASE WHEN "PLA.MonedaAdicional" IS NULL OR "PLA.MonedaAdicional" = '' THEN 'USD' ELSE "PLA.MonedaAdicional" END "PLA.MonedaAdicional"
	INTO v_LocalizacionSistema, v_MonedaSistema, v_MonedaAdicional
	FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot"
	WHERE "IdEmpresa" = :p_IdEmpresa;
	
	
	--Cuenta Inversion
	v_CuentaInversion = 
	WITH 
	MovimientosCuentaInversion AS
	(
		SELECT
			B."IdCuentaInversion"
		   ,B."IdCliente"
		   ,COALESCE(B."IdPortafolio",0) AS "IdPortafolio"
		   ,B."IdMoneda"
		   ,B."SaldoPorFechaMovimiento"
		   ,B."FechaSistema"
		   ,RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."FechaCertificacion",B."IdCargoAbonoMovimiento") as "RankFechaMovimiento"
		FROM :p_Datos AS P
		INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" AS B ON B."IdCliente" = P."IdCliente" AND B."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = B."IdPortafolio" AND Portafolio."IdCliente" = B."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		WHERE
			B."IdEstado" = 'CRT'
		AND B."IdMoneda" IN (v_MonedaSistema, 'USD', 'EUR')
		--AND B."FechaSistema" <= p_FechaHasta
 	    AND B."FechaEfectivaMovimiento" <= p_FechaHasta
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
	)
	SELECT
		MOV."IdCliente"
	   ,MOV."IdPortafolio"
	   ,MOV."IdMoneda"
	   ,MOV."SaldoPorFechaMovimiento"
	FROM MovimientosCuentaInversion AS MOV,
	(
		SELECT
			"IdCuentaInversion"
		   ,MAX("RankFechaMovimiento") AS "RankMovimientoAnterior"
		   ,"IdMoneda"
		FROM MovimientosCuentaInversion
		GROUP BY
			"IdCuentaInversion"
		   ,"IdMoneda"
	) FMAX
	WHERE
		FMAX."IdCuentaInversion" = MOV."IdCuentaInversion"
	AND FMAX."RankMovimientoAnterior" = MOV."RankFechaMovimiento"
	AND FMAX."IdMoneda" = MOV."IdMoneda";
	
	
	INSERT INTO "BSCL.Normativa.BD::CAR.Cartera_ResumenInversion" ("IdTrabajo","IdInterno","IdCliente","IdPortafolio","Grupo","Item","IdMonedaOperacion","MontoMonedaOperacion")
	SELECT
		:p_IdTrabajo
	   ,"BSCL.Normativa.BD.Secuencias::Cartola".NEXTVAL
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,'CAJA' as "grupo"
	   ,CASE WHEN "SaldoPorFechaMovimiento" > 0 THEN CONCAT('Caja Disponible ', "IdMoneda") ELSE CONCAT('Deuda ', "IdMoneda") END as "item"
	   ,"IdMoneda"
	   ,TO_DECIMAL("SaldoPorFechaMovimiento",28,6) as "MontoMonedaOperacion"
	FROM :v_CuentaInversion;
	
	
	--Garantia en dinero
	INSERT INTO "BSCL.Normativa.BD::CAR.Cartera_ResumenInversion" ("IdTrabajo","IdInterno","IdCliente","IdPortafolio","Grupo","Item","IdMonedaOperacion","MontoMonedaOperacion")
	SELECT
		:p_IdTrabajo
	   ,"BSCL.Normativa.BD.Secuencias::Cartola".NEXTVAL as "IdInterno"
	   ,GTA."IdCliente"
	   ,COALESCE(GTA."IdPortafolio",0) AS "IdPortafolio"
	   ,'CAJA' as "grupo"
	   ,'Garantías en efectivo' as "item"
	   ,"IdMonedaInstrumento"
	   ,TO_DECIMAL(SUM("INGMontoGarantiaActualMonedaInstrumento"),28,6) as "MontoMonedaOperacion"
	FROM :p_Datos AS P
	INNER JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia" GTA ON GTA."IdCliente" = P."IdCliente" AND GTA."IdPortafolio" = P."IdPortafolio"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = GTA."IdPortafolio" AND Portafolio."IdCliente" = GTA."IdCliente"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	WHERE
		GTA."IdOrigenGarantia" = 'CTA'
	AND GTA."IdEstado" = 'VIG'
	--AND GTA."INGFechaOperacion" <= p_FechaHasta
	AND GTA."INGFechaLiquidacion" <= p_FechaHasta
	AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
	GROUP BY
		GTA."IdCliente"
	   ,GTA."IdPortafolio"
	   ,GTA."IdMonedaInstrumento";
	
	
	--OperacionesLiquidar
	v_MovimientosTesoreria = 
	WITH 
	MovimientosTesoreria AS
	(
		SELECT
			IE."IdClienteTitularPago" AS "IdCliente"
		   ,COALESCE(IE."IdPortafolioTitularPago",0) AS "IdPortafolio"
		   ,IE."IdMoneda"
		   ,CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' THEN -1*IE."Monto" ELSE IE."Monto" END "MontoMonedaOperacion"
		FROM :p_Datos AS P
		INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoCartola" AS IE ON IE."IdClienteTitularPago" = P."IdCliente" AND IE."IdPortafolioTitularPago" = P."IdPortafolio"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = IE."IdPortafolioTitularPago" AND Portafolio."IdCliente" = IE."IdClienteTitularPago"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		WHERE
			IE."IdEstado" = 'ING'
		AND (IE."FechaLiquidacion" <= p_FechaHasta OR IE."FechaLiquidacion" >= p_FechaHasta)
		AND IE."IdEmpresa" = :p_IdEmpresa
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
	),
	ComprobanteTesoreria AS
	(
		SELECT
			COM."IdClienteTitularPago" AS "IdCliente"
		   ,COALESCE(COM."IdPortafolioTitularPago",0) AS "IdPortafolio"
		   ,COM."IdMoneda"
		   ,CASE WHEN COM."IdTipoMovimiento" = 'MOVEGR' THEN -1*COM."Monto" ELSE COM."Monto" END "MontoMonedaOperacion"
		FROM :p_Datos AS P
		INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteCartola" AS COM ON COM."IdClienteTitularPago" = P."IdCliente" AND COM."IdPortafolioTitularPago" = P."IdPortafolio"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = COM."IdPortafolioTitularPago" AND Portafolio."IdCliente" = COM."IdClienteTitularPago"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		WHERE
			COM."IdEstado" NOT IN ('ANU','MOD','LIQ')
		AND (COM."FechaLiquidacion" <= p_FechaHasta OR COM."FechaLiquidacion" >= p_FechaHasta)
		AND COM."IdEmpresa" = :p_IdEmpresa
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
	)
	SELECT
		"BSCL.Normativa.BD.Secuencias::Cartola".NEXTVAL as "IdInterno"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,'OPL' AS "grupo"
	   ,CONCAT('Saldo Retenido ', "IdMoneda") AS "item"
	   ,"IdMoneda"
	   ,SUM("MontoMonedaOperacion") AS "MontoMonedaOperacion"
	FROM
	(
		SELECT
			"IdCliente"
		   ,"IdPortafolio"
		   ,"IdMoneda"
		   ,"MontoMonedaOperacion"
		FROM MovimientosTesoreria
		UNION ALL
		SELECT
			"IdCliente"
		   ,"IdPortafolio"
		   ,"IdMoneda"
		   ,"MontoMonedaOperacion"
		FROM ComprobanteTesoreria
	)
	GROUP BY
		"IdCliente"
	   ,"IdPortafolio"
	   ,"IdMoneda";
	
	
	INSERT INTO "BSCL.Normativa.BD::CAR.Cartera_ResumenInversion" ("IdTrabajo","IdInterno","IdCliente","IdPortafolio","Grupo","Item","IdMonedaOperacion","MontoMonedaOperacion")
	SELECT
		:p_IdTrabajo
	   ,"IdInterno"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"grupo"
	   ,"item"
	   ,"IdMoneda"
	   ,"MontoMonedaOperacion"
	FROM :v_MovimientosTesoreria;
	
	
	--Renta Variable  / Renta Fija / Intermediacion Financiera / Fondos Mutuos / Derivados
	v_Custodia =
	WITH
	Cartera AS
	(
		SELECT
			'Cartera' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,CAR."IdClasificacionTipoInstrumento"
		   ,CAR."IdTipoInstrumento"
		   ,CAR."IdInstrumento"
		   --------------------------------------------------------------------------------------------
		   ,CAR."IdMonedaInstrumento" AS "IdMonedaOperacion"
		   ,CAR."INGMontoCompraActualMonedaInstrumento" AS "MontoCompraOperacion"
		   ,FLOOR(CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'DV' AND CAR."IdTipoInstrumento" = 'FWD' THEN CAR."INGMontoValorRazonablePasivo" - CAR."INGMontoValorRazonableActivo" ELSE  CAR."INGMontoATasaMercadoActual" END) AS "MontoMercadoOperacion"
		FROM :p_Datos AS P
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON CAR."IdCliente" = P."IdCliente" AND CAR."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		WHERE
			CAR."IdClasificacionTipoInstrumento" IN ('RV','RF','IF','FM','DV')
		AND CAR."IdEstado" = 'VIG'
		AND CAR."IdEmpresa" = :p_IdEmpresa
		--AND CAR."INGFechaOperacion" <= :p_FechaHasta
		AND CAR."INGFechaOperacion" <= (CASE WHEN CAR."IdTipoInstrumento" = 'FWD' THEN CAR."INGFechaLiquidacion" ELSE :p_FechaHasta END)
		--AND CAR."INGFechaLiquidacion" <= (CASE WHEN CAR."IdTipoInstrumento" = 'FWD' THEN CAR."INGFechaLiquidacion" ELSE :p_FechaHasta END)
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
	),
	Garantia AS
	(
		SELECT
			'Garantia' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,GAR."IdClasificacionTipoInstrumento"
		   ,GAR."IdTipoInstrumento"
		   ,GAR."IdInstrumento"
		   --------------------------------------------------------------------------------------------
		   ,GAR."IdMonedaInstrumento" AS "IdMonedaOperacion"
		   ,GAR."INGMontoGarantiaActualMonedaInstrumento" AS "MontoCompraOperacion"
		   ,GAR."INGMontoMercadoActualMonedaInstrumento" AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia" GAR
		INNER JOIN
		(
			SELECT
				RANK() over (partition by CAR."FolioCartera" order by CAR."IdCartera" desc) as RANK
			   ,CAR."FolioCartera"
			   ,CAR."IdCliente"
			   ,CAR."IdPortafolio"
			FROM :p_Datos AS P
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON CAR."IdCliente" = P."IdCliente" AND CAR."IdPortafolio" = P."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			WHERE
				CAR."IdEmpresa" = :p_IdEmpresa
			AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
		) AS CAR ON CAR."FolioCartera" = GAR."FolioCarteraOrigen"
		WHERE
			GAR."IdClasificacionTipoInstrumento" IN ('RV','RF','IF','FM','DV')
		AND GAR."IdEstado" = 'VIG'
		AND GAR."INGFechaOperacion" <= p_FechaHasta
		--AND GAR."INGFechaLiquidacion" <= p_FechaHasta
		AND CAR.RANK = 1
	),
	Subyacente AS
	(
		SELECT
			'Subyacente' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,SUB."IdClasificacionTipoInstrumento"
		   ,CAR."IdTipoInstrumento"
		   ,SUB."IdInstrumento"
		   ,CAR."NombreTipoInstrumento" AS "NombreTipoInstrumento"
		   --------------------------------------------------------------------------------------------
		   ,'CLP'  AS "IdMonedaOperacion"
		   -- se deja la moneda del sistema x mientras, ya q no esta guardando valores en mda instrumento
		   --,CASE WHEN SUB."INGMontoSubyacenteActualMonedaInstrumento" > 0 THEN SUB."INGMontoSubyacenteActualMonedaInstrumento" ELSE SUB."INGMontoMercadoActualMonedaInstrumento" END AS "MontoCompraOperacion"
		   --,CASE WHEN SUB."INGMontoMercadoActualMonedaInstrumento" > 0 THEN SUB."INGMontoMercadoActualMonedaInstrumento" ELSE SUB."INGMontoSubyacenteActualMonedaInstrumento" END AS "MontoMercadoOperacion"
		   ,CASE WHEN SUB."INGMontoATasaSubyacenteActual" > 0 THEN SUB."INGMontoATasaSubyacenteActual" ELSE SUB."INGMontoATasaMercadoActual" END AS "MontoCompraOperacion"
		   ,CASE WHEN SUB."INGMontoATasaMercadoActual" > 0 THEN SUB."INGMontoATasaMercadoActual" ELSE SUB."INGMontoATasaSubyacenteActual" END AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" SUB
		INNER JOIN
		(
			SELECT
				RANK() over (partition by CAR."FolioCartera" order by CAR."IdCartera" desc) as RANK
			   ,CAR."FolioCartera"
			   ,CAR."IdCliente"
			   ,CAR."IdPortafolio"
			   ,CAR."IdTipoInstrumento"
			   ,INS."NombreTipoInstrumento"
			   ,CAR."INGFechaOperacion"
			FROM :p_Datos AS P
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON CAR."IdCliente" = P."IdCliente" AND CAR."IdPortafolio" = P."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
			WHERE
				CAR."IdEmpresa" = :p_IdEmpresa
			AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
		) AS CAR ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada"
		WHERE
			SUB."IdClasificacionTipoInstrumento" IN ('RV','RF','IF','FM','DV')
		AND SUB."IdEstado" = 'VIG'
		--AND CAR."IdTipoOperacion" NOT IN ('COMSIM','COMRRV','COMRRF')
		--AND SUB."INGFechaOperacion" <= p_FechaHasta
		--AND SUB."EGRFechaLiquidacion" <= p_FechaHasta
		--AND CAR."INGFechaLiquidacion" <= p_FechaHasta
		AND CAR."INGFechaOperacion" <= p_FechaHasta
		AND CAR.RANK = 1
	)
	SELECT
		"TipoCus"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
	   ,'' AS "NombreTipoInstrumento"
	   ,"IdInstrumento"
	   ,"IdMonedaOperacion"
	   ,"MontoCompraOperacion"
	   ,"MontoMercadoOperacion"
	FROM Cartera
	UNION ALL
	SELECT
		"TipoCus"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
	   ,'' AS "NombreTipoInstrumento"
	   ,"IdInstrumento"
	   ,"IdMonedaOperacion"
	   ,"MontoCompraOperacion"
	   ,"MontoMercadoOperacion"
	FROM Garantia
	UNION ALL
	SELECT
		"TipoCus"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
	   ,"NombreTipoInstrumento"
	   ,"IdInstrumento"
	   ,"IdMonedaOperacion"
	   ,"MontoCompraOperacion"
	   ,"MontoMercadoOperacion"
	FROM Subyacente;
	
	INSERT INTO "BSCL.Normativa.BD::CAR.Cartera_ResumenInversion" ("IdTrabajo","IdInterno","IdCliente","IdPortafolio","Grupo","Item","IdMonedaOperacion","MontoMonedaOperacion")
	SELECT
		:p_IdTrabajo
	   ,"BSCL.Normativa.BD.Secuencias::Cartola".NEXTVAL as "IdInterno"
	   ,CUS."IdCliente"
	   ,CUS."IdPortafolio"
	   ,CASE
	   		WHEN INS."IdClasificacionTipoInstrumento" = 'RV' AND INS."IdTipoInstrumento" = 'SIM' AND :v_LocalizacionSistema = 'PE' THEN INS."IdTipoInstrumento"
	   		WHEN INS."IdClasificacionTipoInstrumento" = 'RV' THEN INS."IdClasificacionTipoInstrumento"
			WHEN INS."IdClasificacionTipoInstrumento" = 'RF' AND INS."IdTipoInstrumento" IN ('PRT','EMP') THEN INS."IdTipoInstrumento"
			WHEN INS."IdClasificacionTipoInstrumento" = 'RF' AND INS."IdTipoInstrumento" NOT IN ('PRT','EMP') THEN INS."IdClasificacionTipoInstrumento"
	   		WHEN INS."IdClasificacionTipoInstrumento" = 'IF' AND :v_LocalizacionSistema = 'CL' THEN INS."IdClasificacionTipoInstrumento"
	   		WHEN INS."IdClasificacionTipoInstrumento" = 'FM' AND :v_LocalizacionSistema = 'CL' THEN INS."IdClasificacionTipoInstrumento"
	   		WHEN INS."IdClasificacionTipoInstrumento" = 'DV' AND INS."IdTipoInstrumento" = 'FWD' THEN INS."IdTipoInstrumento"
	   		WHEN INS."IdClasificacionTipoInstrumento" = 'DV' THEN INS."IdClasificacionTipoInstrumento"
		END AS "grupo"
	   ,CASE WHEN CUS."NombreTipoInstrumento" = '' THEN INS."NombreTipoInstrumento" ELSE CUS."NombreTipoInstrumento" END as "item"
	   ,CUS."IdMonedaOperacion"
	   ,SUM("MontoMercadoOperacion") AS "MontoOperacion"
	FROM :v_Custodia AS CUS
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = CUS."IdInstrumento"
	GROUP BY
		CUS."IdCliente"
	   ,CUS."IdPortafolio"
	   ,INS."IdClasificacionTipoInstrumento"
	   ,INS."IdTipoInstrumento"
	   ,CASE WHEN CUS."NombreTipoInstrumento" = '' THEN INS."NombreTipoInstrumento" ELSE CUS."NombreTipoInstrumento" END
	   ,INS."NombreTipoInstrumento"
	   ,CUS."IdMonedaOperacion";
	
	
	------------------------------------------------------------------------------------------------
	INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
	SELECT
		0
	   ,"IdInterno"
	   ,:p_FechaHasta
	   ,"IdMonedaOperacion"
	   ,:v_MonedaSistema
	   ,0
	   ,"MontoMonedaOperacion"
	   ,'CARTOLA'
	FROM "BSCL.Normativa.BD::CAR.Cartera_ResumenInversion"
	UNION ALL
	SELECT
		0
	   ,"IdInterno"*-1
	   ,:p_FechaHasta
	   ,"IdMonedaOperacion"
	   ,:v_MonedaAdicional
	   ,0
	   ,"MontoMonedaOperacion"
	   ,'CARTOLA'
	FROM "BSCL.Normativa.BD::CAR.Cartera_ResumenInversion";
	
	--Select * FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada";
	CALL "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
		p_IdEmpresa => :p_IdEmpresa,
		p_IdTrabajo => 0
	);
	--Select * FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida";
	
	
	---Actualizacion monto segun paridad o tipo cambio
	UPDATE TD SET
		TD."IdMonedaSistema" = PTCMS."IdMonedaDestino"
	   ,TD."MontoMonedaSistema" = PTCMS."MontoDestino"
	   ,TD."IdMonedaAdicional" = PTCMA."IdMonedaDestino"
	   ,TD."MontoMonedaAdicional" = PTCMA."MontoDestino"
	FROM "BSCL.Normativa.BD::CAR.Cartera_ResumenInversion" AS TD
	INNER JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" AS PTCMS ON PTCMS."IdInterno" = TD."IdInterno"
	INNER JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" AS PTCMA ON PTCMA."IdInterno" = (TD."IdInterno"*-1)
	WHERE TD."IdTrabajo" = :p_IdTrabajo;
	
	
	--Actualizacion del total por grupo
	UPDATE T SET
		T."TotalMonedaSistema" = CAST(G."montoSistema" as DECIMAL(26,8))
	   ,T."TotalMonedaAdicional" = CAST(G."montoAdicional" as DECIMAL(26,8))
	FROM "BSCL.Normativa.BD::CAR.Cartera_ResumenInversion" T
	INNER JOIN
	(
		SELECT
			T2."IdCliente"
		   ,T2."IdPortafolio"
		   ,T2."Grupo"
		   ,SUM(T2."MontoMonedaSistema") AS "montoSistema"
		   ,SUM(T2."MontoMonedaAdicional") AS "montoAdicional"
		FROM "BSCL.Normativa.BD::CAR.Cartera_ResumenInversion" T2
		WHERE
			T2."IdTrabajo" = :p_IdTrabajo
		GROUP BY
			T2."IdCliente"
		   ,T2."IdPortafolio"
		   ,T2."Grupo"
	) AS G ON G."IdPortafolio" = T."IdPortafolio" AND T."Grupo" = G."Grupo"
	WHERE
		T."IdTrabajo" = :p_IdTrabajo;
	
	
	p_out_ResumenInversion =
	SELECT
		"IdCliente"
	   ,"IdPortafolio"
	   ,"Grupo"
	   ,CASE
	   		WHEN "Grupo" = 'CAJA' THEN "Item"
	   		ELSE "BSCL.Plataforma.BD.Utilitarios::CamelCase"("Item")
	   	END AS "Item"
	   ,"IdMonedaOperacion"
	   ,"MontoMonedaOperacion"
	   ,"IdMonedaSistema"
	   ,"MontoMonedaSistema"
	   ,"TotalMonedaSistema"
	   ,"IdMonedaAdicional"
	   ,"MontoMonedaAdicional"
	   ,"TotalMonedaAdicional"
	FROM "BSCL.Normativa.BD::CAR.Cartera_ResumenInversion"
	WHERE
		"IdTrabajo" = :p_IdTrabajo
	AND "MontoMonedaSistema" IS NOT NULL;
	
END;
GO