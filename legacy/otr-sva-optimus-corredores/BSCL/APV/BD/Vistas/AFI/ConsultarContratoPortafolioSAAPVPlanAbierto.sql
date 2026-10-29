SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarContratoPortafolioSAAPVPlanAbierto";
GO
CREATE VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarContratoPortafolioSAAPVPlanAbierto"  AS 

SELECT P."IdPortafolio"
	,C."IdContratoSAAPV"
	,PC."OrigenAhorro"
	,COALESCE(PC."MontoPesos",0) AS "MontoPesos"
	,COALESCE(PC."MontoUF",0) AS "MontoUF"
	,COALESCE(PC."PorcentajeRegimenImponible",0) AS "PorcentajeRegimenImponible"
	,PC."Pago"
	,PC."PeriodoInicio"
	,PC."PeriodoTermino"
FROM  "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" PC
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON P."IdPortafolio" = PC."IdPortafolio"
INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" C ON C."IdContratoSAAPV" = PC."IdContratoSAAPV"
WHERE C."IdEstado" IN ('PDI','VIG')
AND C."IdPlan" = 1
AND P."IdEstado" <> 'INA';
GO

GRANT SELECT ON "BSCL.APV.BD.Vistas::AFI.ConsultarContratoPortafolioSAAPVPlanAbierto" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO