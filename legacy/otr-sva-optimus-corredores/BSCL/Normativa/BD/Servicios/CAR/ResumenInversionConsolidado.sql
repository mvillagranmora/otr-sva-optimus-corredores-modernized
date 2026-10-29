SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ResumenInversionConsolidado";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ResumenInversionConsolidado" (
    IN 	p_IdModulo 				NVARCHAR(40),
    IN 	p_IdUsuario 			BIGINT,
    IN 	p_IdEmpresa 			BIGINT,
    IN 	p_FechaDesde 			DATE,
    IN	p_FechaHasta 			DATE,
    IN 	p_Datos 				TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
--READS SQL DATA
AS
BEGIN
	
	V_Datos =   
	SELECT DISTINCT "IdCliente", NULL AS "IdPortafolio" FROM :p_Datos;
	
	CALL "BSCL.Normativa.BD.Servicios::CAR.Resumen_Inversion" (
		p_IdModulo				=> :p_IdModulo,
		p_IdUsuario				=> :p_IdUsuario,
		p_IdEmpresa				=> :p_IdEmpresa,
		p_FechaDesde			=> :p_FechaDesde,
		p_FechaHasta			=> :p_FechaHasta,
		p_IdTrabajo				=> 0,
		p_Datos					=> :V_Datos,
		p_out_ResumenInversion	=> v_ResumenInversion
	);
	
	v_ResultadoResumen = 
	WITH 
	AgrupacionResumenInversion as (
	 SELECT "IdCliente",
			"IdPortafolio",
			"IdMonedaSistema",
			SUM("MontoMonedaSistema") as "MontoMonedaSistema",
			SUM("MontoMonedaAdicional") as "MontoMonedaAdicional"
	   FROM :v_ResumenInversion
	  GROUP BY "IdCliente","IdPortafolio","IdMonedaSistema","IdMonedaAdicional"
	),
	AgrupacionResumenInversionItem as (
	 SELECT "IdCliente",
			"IdPortafolio",
			"Grupo",
			"Item",
			"IdMonedaSistema",
			SUM("MontoMonedaSistema") as "MontoMonedaSistema",
			MAX("TotalMonedaSistema") as "TotalMonedaSistema",
			"IdMonedaAdicional",
			SUM("MontoMonedaAdicional") as "MontoMonedaAdicional",
			MAX("TotalMonedaAdicional") as "TotalMonedaAdicional"
	   FROM :v_ResumenInversion
	  GROUP BY "IdCliente","IdPortafolio","Grupo","Item","IdMonedaSistema","IdMonedaAdicional"
	)
	SELECT  A."IdCliente",
			A."IdPortafolio",
			A."Grupo",
			A."Item",
			A."IdMonedaSistema",
			CASE WHEN C."MontoMonedaSistema" > 0 THEN (A."MontoMonedaSistema"*100) / C."MontoMonedaSistema" ELSE 0 END AS "PorcentajeActivo",
			CASE WHEN C."MontoMonedaSistema" > 0 THEN (A."TotalMonedaSistema"*100) / C."MontoMonedaSistema" ELSE 0 END AS "PorcentajeTotalActivos",			
			
			CASE WHEN A."MontoMonedaSistema" > 0 THEN A."MontoMonedaSistema" ELSE 0 END AS "ActivoMontoMonedaSistema",
			CASE WHEN A."MontoMonedaSistema" < 0 THEN A."MontoMonedaSistema" ELSE 0 END AS "PasivoMontoMonedaSistema",
			CASE WHEN A."TotalMonedaSistema" > 0 THEN A."TotalMonedaSistema" ELSE 0 END AS "ActivoTotalMonedaSistema",
			CASE WHEN A."TotalMonedaSistema" < 0 THEN A."TotalMonedaSistema" ELSE 0 END AS "PasivoTotalMonedaSistema",
			CONCAT( CONCAT('En ',A."IdMonedaSistema"), ', a valor de mercado') AS "TituloResumenInversionSistema",
			A."IdMonedaAdicional",
			CASE WHEN A."MontoMonedaAdicional" > 0 THEN A."MontoMonedaAdicional" ELSE 0 END AS "ActivoMontoMonedaAdicional",
			CASE WHEN A."MontoMonedaAdicional" < 0 THEN A."MontoMonedaAdicional" ELSE 0 END AS "PasivoMontoMonedaAdicional",
			CASE WHEN A."TotalMonedaAdicional" > 0 THEN A."TotalMonedaAdicional" ELSE 0 END AS "ActivoTotalMonedaAdicional",
			CASE WHEN A."TotalMonedaAdicional" < 0 THEN A."TotalMonedaAdicional" ELSE 0 END AS "PasivoTotalMonedaAdicional",
			CONCAT( CONCAT('En ',A."IdMonedaAdicional"),', a valor de mercado') AS "TituloResumenInversionAdicional"
	FROM AgrupacionResumenInversionItem A
	INNER JOIN AgrupacionResumenInversion C ON C."IdCliente" = A."IdCliente" AND C."IdPortafolio" = A."IdPortafolio"
	UNION ALL
	SELECT
			"IdCliente",
			0 AS "IdPortafolio",
			'TOTAL' AS "Grupo",
			'' as "Item",
			'' as "IdMonedaSistema",
			0 AS "PorcentajeActivo",
			0 AS "PorcentajeTotalActivos",
			0 AS "ActivoMontoMonedaSistema",
			0 AS "PasivoMontoMonedaSistema",
			"MontoMonedaSistema" AS "ActivoTotalMonedaSistema",
			0 AS "PasivoTotalMonedaSistema",
			'' AS "TituloResumenInversionSistema",
			'' AS "IdMonedaAdicional",
			0 AS "ActivoMontoMonedaAdicional",
			0 AS "PasivoMontoMonedaAdicional",
			"MontoMonedaAdicional" AS "ActivoTotalMonedaAdicional",
			0 AS "PasivoTotalMonedaAdicional",
			'' AS "TituloResumenInversionAdicional"
	FROM AgrupacionResumenInversion;

	SELECT "IdCliente",
			"IdPortafolio",
			"Grupo",
			"Item",
			"IdMonedaSistema",
			CAST("PorcentajeActivo" AS DECIMAL(26,8)) AS "PorcentajeActivo",
			CAST("PorcentajeTotalActivos" AS DECIMAL(26,8)) AS "PorcentajeTotalActivos",			
			"ActivoMontoMonedaSistema",
			"PasivoMontoMonedaSistema",
			"ActivoTotalMonedaSistema",
			"PasivoTotalMonedaSistema",
			"TituloResumenInversionSistema",
			"IdMonedaAdicional",
			"ActivoMontoMonedaAdicional",
			"PasivoMontoMonedaAdicional",
			"ActivoTotalMonedaAdicional",
			"PasivoTotalMonedaAdicional",
			"TituloResumenInversionAdicional"
	FROM :v_ResultadoResumen;
	
END;
GO