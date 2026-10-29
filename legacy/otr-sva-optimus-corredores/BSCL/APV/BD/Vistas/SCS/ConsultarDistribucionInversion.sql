SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::SCS.ConsultarDistribucionInversion"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::SCS.ConsultarDistribucionInversion"  AS 

SELECT
	 DIS."IdDistribucionInversion"	
	,DIS."IdContratoSAAPV"
	,DIS."FolioContratoSAAPV"
	,DIS."IdInstrumento"
	,INS."RazonSocial"
	,DIS."Nemotecnico"
	,DIS."MontoPesos"
	,DIS."Porcentaje"
	,DIS."IdEstado"
	,INS."IdMoneda"
FROM "BSCL.APV.BD::SCS.DistribucionInversion" AS DIS
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
ON DIS."IdInstrumento" = INS."IdInstrumento";
	   
GRANT SELECT ON "BSCL.APV.BD.Vistas::SCS.ConsultarDistribucionInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 