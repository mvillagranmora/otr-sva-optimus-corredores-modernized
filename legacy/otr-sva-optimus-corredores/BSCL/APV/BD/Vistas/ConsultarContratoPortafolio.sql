SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.APV.BD.Vistas::ConsultarContratoPortafolio"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::ConsultarContratoPortafolio" AS 

SELECT Portafolio."IdPortafolio"
	 , Cliente."IdPersona"
     , MatrizAgrupador."IdPlanTipoInversion"
     , PlanTipoInversion."NombrePlanTipoInversion"
     , MatrizAgrupador."IdProductoAgrupador"
     , ProductoAgrupador."NombreProductoAgrupador"
     , MatrizAgrupador."Codigo"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cliente
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
		ON Cliente."IdCliente" = Portafolio."IdCliente"
INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContratoSAAPV
		ON PortafolioContratoSAAPV."IdPortafolio" = Portafolio."IdPortafolio"
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador
		ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PlanTipoInversion
		ON PlanTipoInversion."IdPlanTipoInversion" = MatrizAgrupador."IdPlanTipoInversion"
INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador
		ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
WHERE Portafolio."IdEstado" <> 'INA';

GRANT SELECT ON "BSCL.APV.BD.Vistas::ConsultarContratoPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
