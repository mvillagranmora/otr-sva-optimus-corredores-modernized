SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarMonedasNoHabilitadasEnTesoreria";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarMonedasNoHabilitadasEnTesoreria" AS

SELECT	 MON."IdMoneda"
		,MON."Codigo"
		,MON."Nombre"
		,MON."NombreLargo" 
		,MON."Simbolo"		
		,MON."MultiplicaODivide"
		,MON."RangoSuperiorParidad"
		,MON."RangoInferiorParidad"
		,MON."PorcentajeSuperiorParidad"
		,MON."PorcentajeInferiorParidad"
		,MON."RangoSuperiorTipoCambio"
		,MON."RangoInferiorTipoCambio"
		,MON."PorcentajeSuperiorTipoCambio"
		,MON."PorcentajeInferiorTipoCambio"
		,MON."Orden"
		,MON."IdPais"
		,MON."IdEstado"	 
		,MON."NombreEstado"
  FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" AS MON
 INNER JOIN (SELECT "IdMoneda"
 			   FROM "BSCL.Plataforma.BD::PLA.Moneda"
 			 EXCEPT
 			 SELECT "IdMoneda"
 			   FROM "BSCL.Tesoreria.BD::TES.MonedaTesoreria"
 			  WHERE "IdEstado" = 'ACT'
 			) AS MHB ON MHB."IdMoneda" = MON."IdMoneda"

WITH READ ONLY;
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarMonedasNoHabilitadasEnTesoreria" TO  BCS_DESARROLLADOR WITH GRANT OPTION;