SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::REC.ConsultarFolioRecaudacionConciliacionMasiva";

CREATE VIEW "BSCL.APV.BD.Vistas::REC.ConsultarFolioRecaudacionConciliacionMasiva" AS

SELECT 
	AGR."IdRecaudacion" AS "IdRecaudacion"
	, AGR."IdEmpresa"
	, AGR."IdEstado" AS "IdEstado"
	, AGR."MontoDeposito" AS "MontoDeposito"
	, SUM(AGR."TotalCliente") AS "TotalCliente"
	, SUM(AGR."TotalEmpleador") AS "TotalEmpleador"
	, (AGR."MontoDeposito" - SUM(AGR."TotalCliente")) AS "DiferenciaCliente"
	, (AGR."MontoDeposito" - SUM(AGR."TotalEmpleador")) AS "DiferenciaEmpleador"
FROM (
	SELECT 
		REC."IdRecaudacion"
		, REC."IdEmpresa"
		, REC."IdEstado"
		, REC."MontoDeposito"
		, 0 AS "TotalCliente"
		, COALESCE(SUM(REMP."MontoDepositoConvenido" + REMP."MontoDepositoAPV" + REMP."Reajustes" + REMP."Intereses"), 0) AS "TotalEmpleador"
	FROM "BSCL.APV.BD.Vistas::REC.ConsultarRecaudacion" AS REC
	LEFT JOIN "BSCL.APV.BD::REC.Empleador" AS REMP ON REC."IdRecaudacion" = REMP."IdRecaudacion"
	WHERE REC."Origen" = 'PREVRED' AND REC."IdEstado" = 'ING'
	GROUP BY REC."IdRecaudacion", REC."IdEmpresa", REC."IdEstado", REC."MontoDeposito"
	UNION ALL 
	SELECT 
		REC."IdRecaudacion"
		, REC."IdEmpresa"
		, REC."IdEstado"
		, REC."MontoDeposito"
		, COALESCE(SUM(RCLI."MontoDepositoConvenido" + RCLI."MontoDepositoAPV"), 0) AS "TotalCliente"
		, 0 AS "TotalEmpleador"
	FROM "BSCL.APV.BD.Vistas::REC.ConsultarRecaudacion" AS REC
	LEFT JOIN "BSCL.APV.BD::REC.Cliente" AS RCLI ON REC."IdRecaudacion" = RCLI."IdRecaudacion"
	WHERE REC."Origen" = 'PREVRED' AND REC."IdEstado" = 'ING'
	GROUP BY REC."IdRecaudacion", REC."IdEmpresa", REC."IdEstado", REC."MontoDeposito") AS AGR
GROUP BY AGR."IdRecaudacion", AGR."IdEmpresa", AGR."IdEstado", AGR."MontoDeposito";

GRANT SELECT ON "BSCL.APV.BD.Vistas::REC.ConsultarFolioRecaudacionConciliacionMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;