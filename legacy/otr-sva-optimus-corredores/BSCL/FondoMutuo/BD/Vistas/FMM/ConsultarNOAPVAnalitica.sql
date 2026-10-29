SET SCHEMA "BSCL";

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarNOAPVAnalitica";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarNOAPVAnalitica" AS
SELECT  "IdMatrizAgrupadorNOAPV"
		,"CodigoPortafolio"
  		,"IdEstado"
  		,"EsADC"					
FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV";
				
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarNOAPVAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION;
