SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ResumenInversion";
GO

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ResumenInversion" (
    IN 	p_IdModulo 				NVARCHAR(40),
    IN 	p_IdUsuario 			BIGINT,
    IN 	p_IdEmpresa 			BIGINT,
    IN 	p_FechaDesde 			DATE,
    IN	p_FechaHasta 			DATE,
    IN 	p_Datos 				TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT),
    IN  p_IdProductoAgrupador	BIGINT DEFAULT NULL
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"

AS
BEGIN
	
	CALL "BSCL.Normativa.BD.Servicios::CAR.Resumen_Inversion" (
		p_IdModulo				=> :p_IdModulo,
		p_IdUsuario				=> :p_IdUsuario,
		p_IdEmpresa				=> :p_IdEmpresa,
		p_FechaDesde			=> :p_FechaDesde,
		p_FechaHasta			=> :p_FechaHasta,
		p_IdTrabajo				=> 1,
		p_Datos					=> :p_Datos,
		p_IdProductoAgrupador	=> :p_IdProductoAgrupador,
		p_out_ResumenInversion	=> v_ResumenInversion
	);
	
	SELECT
		"IdCliente"
	   ,"IdPortafolio"
	   ,"Grupo"
	   ,"Item"
	   ,"IdMonedaSistema"
	   ,CAST(IFNULL(SUM("MontoMonedaSistema"),0.0) AS DECIMAL(26,8))	AS "MontoMonedaSistema"
	   ,SUM("TotalGrupoMonedaSistema")		AS "TotalGrupoMonedaSistema"
	   ,SUM("TotalMonedaSistema")			AS "TotalMonedaSistema"
	   ,"IdMonedaAdicional"
	   ,SUM("MontoMonedaAdicional")			AS "MontoMonedaAdicional"
	   ,SUM("TotalGrupoMonedaAdicional")	AS "TotalGrupoMonedaAdicional"
	   ,SUM("TotalMonedaAdicional")			AS "TotalMonedaAdicional"
	FROM
	(
		SELECT
			"IdCliente"
		   ,"IdPortafolio"
		   ,"Grupo"
		   ,"Item"
		   ,"IdMonedaSistema"
		   ,"MontoMonedaSistema"
		   ,SUM("MontoMonedaSistema") OVER (PARTITION BY "IdCliente","IdPortafolio","Grupo","IdMonedaSistema" ORDER BY "IdPortafolio") AS "TotalGrupoMonedaSistema"
		   ,SUM("MontoMonedaSistema") OVER (PARTITION BY "IdCliente","IdPortafolio","IdMonedaSistema" ORDER BY "IdPortafolio") AS "TotalMonedaSistema"
		   ,"IdMonedaAdicional"
		   ,"MontoMonedaAdicional"
		   ,SUM("MontoMonedaAdicional") OVER (PARTITION BY "IdCliente","IdPortafolio","Grupo","IdMonedaAdicional" ORDER BY "IdPortafolio") AS "TotalGrupoMonedaAdicional"
		   ,SUM("MontoMonedaAdicional") OVER (PARTITION BY "IdCliente","IdPortafolio","IdMonedaAdicional" ORDER BY "IdPortafolio") AS "TotalMonedaAdicional"
		FROM :v_ResumenInversion
	)
	GROUP BY
		"IdCliente"
	   ,"IdPortafolio"
	   ,"Grupo"
	   ,"Item"
	   ,"IdMonedaSistema"
	   ,"IdMonedaAdicional";
	
END;
GO