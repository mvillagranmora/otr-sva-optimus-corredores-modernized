SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.FlujoProyectado";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.FlujoProyectado" (
    IN p_IdModulo 					NVARCHAR(40),
    IN p_IdUsuario 					BIGINT,
    IN p_IdEmpresa 					BIGINT,
    IN p_FechaDesde 				DATE,
    IN p_FechaHasta 				DATE,
    IN p_FiltroSoloMonedaSistema	NVARCHAR(1) DEFAULT 'S',
    IN p_Datos 						TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_IdPais, v_keyFlujoCustodia, v_out_ErroresContador BIGINT;
	DECLARE v_FechaProyectada30, v_FechaProyectada90 NVARCHAR(10);
	DECLARE v_DocumentoIdentidadCorredora NVARCHAR(40);
	
	SELECT "IdPais" INTO v_IdPais FROM "BSCL.Plataforma.BD::PLA.Empresa" WHERE "IdEmpresa" = :p_IdEmpresa;
	
	v_ConfiguracionDelSistema =
	SELECT  "IdEmpresa",
			"PLA.LocalizacionSistema",
			"PLA.MonedaSistema",
			CASE WHEN "PLA.MonedaAdicional" IS NULL OR "PLA.MonedaAdicional" = '' THEN 'USD' ELSE "PLA.MonedaAdicional" END "PLA.MonedaAdicional" 
	  FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot"
	 WHERE "IdEmpresa" = :p_IdEmpresa;
	
	 
	v_FechaBusqueda = SELECT :p_FechaHasta AS "FechaInicial", ELEMENT_NUMBER AS "NumeroDias" FROM SERIES_GENERATE_BIGINT(1,0,30);
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguienteMasivo" (
		p_IdPais		=> :v_IdPais,
		p_DiaHabil		=> 'S',
		p_FechasIN		=> :v_FechaBusqueda,
		p_FechasOUTf	=> v_FechasOUTf
	);
	
	SELECT MAX("FechaCalculada") INTO v_FechaProyectada30 FROM :v_FechasOUTf;
	v_FechaBusqueda = SELECT :v_FechaProyectada30 AS "FechaInicial", ELEMENT_NUMBER AS "NumeroDias" FROM SERIES_GENERATE_BIGINT(1,0,60);
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguienteMasivo" (
		p_IdPais		=> :v_IdPais,
		p_DiaHabil		=> 'S',
		p_FechasIN		=> :v_FechaBusqueda,
		p_FechasOUTf	=> v_FechasOUTf
	);
	SELECT MAX("FechaCalculada") INTO v_FechaProyectada90 FROM :v_FechasOUTf;
	
	
	v_TiposFlujosProyectados =	
	SELECT 'CUPONES' as "TiposFlujosProyectados" FROM :v_ConfiguracionDelSistema WHERE "PLA.LocalizacionSistema" IN ('CL','PE') UNION ALL
	SELECT 'DIVIDENDOS' as "TiposFlujosProyectados" FROM :v_ConfiguracionDelSistema WHERE "PLA.LocalizacionSistema" IN ('CL','PE') UNION ALL
	SELECT 'DEPOSITOS' as "TiposFlujosProyectados" FROM :v_ConfiguracionDelSistema WHERE "PLA.LocalizacionSistema" = 'CL' UNION ALL
	SELECT 'FORWARDS' as "TiposFlujosProyectados" FROM :v_ConfiguracionDelSistema WHERE "PLA.LocalizacionSistema" = 'CL' UNION ALL
	SELECT 'FUTUROS' as "TiposFlujosProyectados" FROM :v_ConfiguracionDelSistema WHERE "PLA.LocalizacionSistema" = 'CL' UNION ALL
	SELECT 'PACTOS' as "TiposFlujosProyectados" FROM :v_ConfiguracionDelSistema WHERE "PLA.LocalizacionSistema" = 'CL' UNION ALL	
	SELECT 'SIMULTANEAS' as "TiposFlujosProyectados" FROM :v_ConfiguracionDelSistema WHERE "PLA.LocalizacionSistema" IN ('CL','PE');
	
	
	
	
	--Nota: Los pactos estan mirados desde el punto de vista de la corredora por ello afectan de forma contraria al cliente
	v_MovimientosCartera =
	SELECT 	dat."IdCliente", 
			dat."IdPortafolio",
			car."FechaLiquidacion",
			CASE WHEN car."IdTipoOperacion" IN ('COMSIM','VENSIM') THEN 'SIMULTANEAS' 
				 WHEN car."IdTipoOperacion" IN ('COMPAC','VENPAC') THEN 'PACTOS'
				 WHEN car."IdClasificacionTipoInstrumento" IN ('DV') THEN TI."Descripcion"
			 END as "Item",
			car."IdMonedaInstrumento",
			car."IdMonedaSistema",
			CASE WHEN car."IdTipoOperacion" IN ('VENSIM','COMPAC') THEN -1*car."INGMontoATasaCompraActual"
				 ELSE car."INGMontoATasaCompraActual"
			 END as "MontoMonedaSistema",
			car."IdMonedaAdicional",
			CASE WHEN car."IdTipoOperacion" IN ('VENSIM','COMPAC') THEN -1*car."INGMontoCompraActualMonedaAdicional"
				 ELSE car."INGMontoCompraActualMonedaAdicional"
			 END as "MontoMonedaAdicional" 
	FROM :p_Datos as dat
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS car ON (dat."IdCliente" = car."IdCliente" AND dat."IdPortafolio" = car."IdPortafolio")
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TI ON (TI."IdTipoInstrumento" = car."IdTipoInstrumento")
	WHERE car."IdEmpresa" = :p_IdEmpresa
	  AND car."FechaLiquidacion" <=:v_FechaProyectada90
	  AND car."IdEstado" = 'VIG'
	  AND (car."IdTipoOperacion" IN ('COMSIM','VENSIM','COMPAC','VENPAC')
	  OR car."IdClasificacionTipoInstrumento" IN ('DV'));	
	
	
	v_DistribucionProyeccion = 
	WITH 
	DatosPorTipoFlujos as (
		SELECT "IdCliente","IdPortafolio","TiposFlujosProyectados"
		FROM :p_Datos as dat, :v_TiposFlujosProyectados as TFP
	),
	SaldoProyectado30 as (	
		SELECT "IdCliente","IdPortafolio","Item",
				"IdMonedaSistema",
				CASE WHEN "IdMonedaInstrumento" = "IdMonedaSistema" THEN "MontoMonedaSistema"
					 WHEN "IdMonedaInstrumento" != "IdMonedaSistema" AND :p_filtroSoloMonedaSistema = 'S' THEN "MontoMonedaSistema"
					 ELSE 0
				END "MontoMonedaSistema",
				"IdMonedaAdicional",
				CASE WHEN "IdMonedaInstrumento" != "IdMonedaSistema" AND :p_filtroSoloMonedaSistema = 'N' THEN "MontoMonedaAdicional"
					 ELSE 0
				END "MontoMonedaAdicional"
		FROM :v_MovimientosCartera as FT
		WHERE "FechaLiquidacion" <=:v_FechaProyectada30
	),
	SaldoProyectado90 as (
		SELECT "IdCliente","IdPortafolio","Item",
				"IdMonedaSistema",
				CASE WHEN "IdMonedaInstrumento" = "IdMonedaSistema" THEN "MontoMonedaSistema"
					 WHEN "IdMonedaInstrumento" != "IdMonedaSistema" AND :p_filtroSoloMonedaSistema = 'S' THEN "MontoMonedaSistema"
					 ELSE 0
				END "MontoMonedaSistema",
				"IdMonedaAdicional",
				CASE WHEN "IdMonedaInstrumento" != "IdMonedaSistema" AND :p_filtroSoloMonedaSistema = 'N' THEN "MontoMonedaAdicional"
					 ELSE 0
				END "MontoMonedaAdicional"
		FROM :v_MovimientosCartera as FT
		WHERE "FechaLiquidacion" > :v_FechaProyectada30 
		  AND "FechaLiquidacion" <= :v_FechaProyectada90
	)
	SELECT dat."IdCliente", dat."IdPortafolio", 
		   "BSCL.Plataforma.BD.Utilitarios::CamelCase"(dat."TiposFlujosProyectados") AS "Item", 	
		   MAX(sp30."IdMonedaSistema") AS "IdMonedaSistema",SUM(sp30."MontoMonedaSistema") as "MonedaSistemaP30", 
		   MAX(sp30."IdMonedaAdicional") AS "IdMonedaAdicional",SUM(sp30."MontoMonedaAdicional") as "MonedaAdicionalP30",  
		   MAX(sp90."MontoMonedaSistema") as "MonedaSistemaP30_90", 
		   MAX(sp90."MontoMonedaAdicional") as "MonedaAdicionalP30_90"
	FROM DatosPorTipoFlujos as dat
	LEFT JOIN SaldoProyectado30 as sp30 ON sp30."IdCliente" = dat."IdCliente" AND sp30."IdPortafolio" = dat."IdPortafolio" AND dat."TiposFlujosProyectados" = sp30."Item"
	LEFT JOIN SaldoProyectado90 as sp90 ON sp90."IdCliente" = dat."IdCliente" AND sp90."IdPortafolio" = dat."IdPortafolio" AND dat."TiposFlujosProyectados" = sp90."Item"
	GROUP BY dat."IdCliente", dat."IdPortafolio", dat."TiposFlujosProyectados";
	
	
	SELECT "IdCliente","IdPortafolio","Item",'' as "TipoDivision",CS."PLA.MonedaSistema" AS "Moneda",COALESCE("MonedaSistemaP30",0) AS "P30",COALESCE("MonedaSistemaP30_90",0) as "P30_90"
	FROM :v_DistribucionProyeccion AS DP, :v_ConfiguracionDelSistema AS CS 
	WHERE CS."PLA.MonedaSistema" = COALESCE(DP."IdMonedaSistema",CS."PLA.MonedaSistema")
	UNION ALL
	SELECT "IdCliente","IdPortafolio","Item",'' as "TipoDivision",CS."PLA.MonedaAdicional" AS "Moneda",COALESCE("MonedaAdicionalP30",0) AS "P30",COALESCE("MonedaAdicionalP30_90",0) as "P30_90"
	FROM :v_DistribucionProyeccion AS DP,:v_ConfiguracionDelSistema AS CS 
	WHERE CS."PLA.MonedaAdicional" = COALESCE(DP."IdMonedaAdicional",CS."PLA.MonedaAdicional")
	AND :p_filtroSoloMonedaSistema = 'N';

END;
GO
