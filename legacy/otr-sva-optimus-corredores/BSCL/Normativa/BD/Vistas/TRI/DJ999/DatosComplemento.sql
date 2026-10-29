SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ999.DatosComplemento";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ999.DatosComplemento" (
	"Periodo"
	,"RutInstitucion"
	,"FechaPosicion"
	,"Concepto"
	,"Moneda"
	,"Paridad"
	,"MontoMonedaPrincipal"
	,"MontoDolar"
) AS

SELECT 
	"Periodo"
	,"RutInstitucion"
	,"FechaPosicion"
	,"Concepto"
	,"Moneda"
	,"Paridad"
	,"MontoMonedaPrincipal"
	,"MontoDolar"
FROM "BSCL.Normativa.BD::TRI.SaldoDJ999"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ999.DatosComplemento" TO bcs_desarrollador WITH GRANT OPTION;