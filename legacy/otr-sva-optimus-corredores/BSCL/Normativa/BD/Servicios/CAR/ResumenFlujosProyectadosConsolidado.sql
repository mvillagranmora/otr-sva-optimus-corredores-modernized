SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ResumenFlujosProyectadosConsolidado";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ResumenFlujosProyectadosConsolidado" (
    IN p_IdModulo NVARCHAR(40),
    IN p_IdUsuario BIGINT,
    IN p_IdEmpresa BIGINT,
    IN p_FechaDesde DATE,
    IN p_FechaHasta DATE,
    IN p_Datos TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT)
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
	v_Datos = SELECT DISTINCT "IdCliente", NULL AS "IdPortafolio" FROM :p_Datos;
	
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
	--SELECT 'CUPONES' as "TiposFlujosProyectados" FROM DUMMY UNION ALL			--Falta incorporar negocio a la cartola
	--SELECT 'DIVIDENDOS' as "TiposFlujosProyectados" FROM DUMMY UNION ALL		--Falta incorporar negocio a la cartola
	--SELECT 'DEPOSITOS' as "TiposFlujosProyectados" FROM DUMMY UNION ALL			--Falta incorporar negocio a la cartola
	SELECT 'FORWARDS' as "TiposFlujosProyectados" FROM DUMMY UNION ALL
	SELECT 'FUTUROS' as "TiposFlujosProyectados" FROM DUMMY UNION ALL
	SELECT 'PACTOS' as "TiposFlujosProyectados" FROM DUMMY UNION ALL	
	SELECT 'SIMULTANEAS' as "TiposFlujosProyectados" FROM DUMMY;
	
	
	
	
	--Nota: Los pactos estan mirados desde el punto de vista de la corredora por ello afectan de forma contraria al cliente
	v_MovimientosCartera =
	SELECT 	CAR."IdCliente", 
			COALESCE(dat."IdPortafolio",0) AS "IdPortafolio",
			CAR."FechaLiquidacion",
			CASE WHEN CAR."IdTipoOperacion" IN ('COMSIM','VENSIM') THEN 'SIMULTANEAS' 
				 WHEN CAR."IdTipoOperacion" IN ('COMPAC','VENPAC') THEN 'PACTOS'
				 WHEN CAR."IdClasificacionTipoInstrumento" IN ('DV') THEN TI."Descripcion"
			 END as "Item",
			CAR."IdMonedaSistema",
			CASE WHEN CAR."IdTipoOperacion" IN ('VENSIM','COMPAC') THEN -1*car."INGMontoATasaCompraActual"
				 ELSE CAR."INGMontoATasaCompraActual"
			 END as "Monto"
	FROM :v_Datos as dat
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (CAR."IdCliente" = dat."IdCliente" AND car."IdPortafolio" = COALESCE(dat."IdPortafolio",CAR."IdPortafolio") )
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TI ON (TI."IdTipoInstrumento" = car."IdTipoInstrumento")
	WHERE CAR."IdEmpresa" = :p_IdEmpresa
	  AND CAR."FechaLiquidacion" <=:v_FechaProyectada90
	  AND CAR."IdEstado" = 'VIG'
	  AND (CAR."IdTipoOperacion" IN ('COMSIM','VENSIM','COMPAC','VENPAC')
	  		OR CAR."IdClasificacionTipoInstrumento" IN ('DV')
	  	  );	
	
	
	v_DistribucionProyeccion = 
	WITH 
	DatosPorTipoFlujos as (
		SELECT "IdCliente","IdPortafolio","TiposFlujosProyectados"
		FROM :v_Datos as dat, :v_TiposFlujosProyectados as TFP
	),
	SaldoProyectado30 as (
		SELECT "IdCliente","IdPortafolio","Item",SUM("Monto") as "Monto"
		FROM :v_MovimientosCartera as FT
		WHERE "FechaLiquidacion" <=:v_FechaProyectada30
		GROUP BY "IdCliente","IdPortafolio","Item"
	),
	SaldoProyectado90 as (
		SELECT "IdCliente","IdPortafolio","Item",SUM("Monto") as "Monto"
		FROM :v_MovimientosCartera as FT
		WHERE "FechaLiquidacion" > :v_FechaProyectada30 
		  AND "FechaLiquidacion" <= :v_FechaProyectada90
		GROUP BY "IdCliente","IdPortafolio","Item"
	)
	SELECT dat."IdCliente", dat."IdPortafolio", dat."TiposFlujosProyectados", sp30."Monto" as "P30", sp90."Monto" as "P30_90"
	FROM DatosPorTipoFlujos as dat
	LEFT JOIN SaldoProyectado30 as sp30 ON sp30."IdCliente" = dat."IdCliente" AND sp30."IdPortafolio" = COALESCE(dat."IdPortafolio",0) AND dat."TiposFlujosProyectados" = sp30."Item"
	LEFT JOIN SaldoProyectado90 as sp90 ON sp90."IdCliente" = dat."IdCliente" AND sp90."IdPortafolio" = COALESCE(dat."IdPortafolio",0) AND dat."TiposFlujosProyectados" = sp90."Item";
	
	
	SELECT  "IdCliente",
			CAST(0 AS bigint) AS "IdPortafolio",
			"BSCL.Plataforma.BD.Utilitarios::CamelCase"("TiposFlujosProyectados") as "item",
			'' as "tipoDiv", 
			COALESCE("P30",0) as "P30", 
			COALESCE("P30_90",0) as "P30_90"
	FROM :v_DistribucionProyeccion;
	
END;
GO
