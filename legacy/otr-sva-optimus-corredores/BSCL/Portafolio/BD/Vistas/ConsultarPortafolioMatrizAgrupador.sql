SET SCHEMA "BSCL";

DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupador";
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupador" AS 
SELECT SOC."IdSocioDeNegocio"									AS "IdSocioDeNegocio"
	 , SOC."IdPersona"											AS "IdPersona"
	 , POR."IdCliente"											AS "IdCliente"
	 , POR."IdPortafolio"  									    AS "IdPortafolio"
	 , PER."DocumentoIdentidad"									AS "DocumentoIdentidad"
	 , APV."NombreAgrupador"									AS "NombreAgrupador"
	 , APV."CodigoAgrupador"									AS "CodigoAgrupador"
	 , APV."NombrePlan"											AS "NombrePlanAhorro"
	 , APV."Producto"											AS "Producto"
	 , APV."IdEstado"											AS "IdEstadoAgrupador"
	 --, VISTAPOR."CodigoPortafolio" 								AS "CuentaCodigoPortafolio"
	 , APV."Producto"											AS "CodigoPortafolio"
	 , VISTAPOR."Proposito"										AS "PropositoPortafolio"
	 , MIN(VISTAPOR."IdEstado")									AS "IdEstado"
	 , MIN(VISTAPOR."Estado")									AS "EstadoPortafolio"
	 , MIN(VISTAPOR."FechaIngreso")								AS "FechaIngreso"
	 , MIN(VISTAPOR."FechaCreacion")							AS "FechaCreacion"
	 , MIN(VISTAPOR."FechaModificacion")						AS "FechaModificacion"
	 --, VISTAPOR."IdCarga"										AS "IdCarga"
	 --, APV."Codigo"											AS "RegimenTributario"
	 --, POR."IdMatrizAgrupadorAPV"								AS "IdMatrizAgrupadorAPV"
	 , MIN(VISTAPOR."IdEstadoPortafolioAutorizacion")			AS "IdEstadoPortafolioAutorizacion"
	 , MIN(VISTAPOR."EstadoPortafolioAutorizacion")				AS "EstadoPortafolioAutorizacion"
FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC 
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR on (SOC."IdCliente" = POR."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER on (PER."IdPersona" = SOC."IdPersona")
INNER JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS APV ON (APV."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV")
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS VISTAPOR ON (VISTAPOR."IdPortafolio" = POR."IdPortafolio")
GROUP BY SOC."IdSocioDeNegocio"									
	 , SOC."IdPersona"											
	 , POR."IdCliente"	
	 , POR."IdPortafolio" 
	 , PER."DocumentoIdentidad"									
	 , APV."NombreAgrupador"									
	 , APV."CodigoAgrupador"									
	 , APV."NombrePlan"											
	 , APV."Producto"										
	 , APV."IdEstado"											
	 --, VISTAPOR."CodigoPortafolio" 															
	 , VISTAPOR."Proposito"
	 --, VISTAPOR."IdEstado"
	 --, VISTAPOR."Estado"
	 ,VISTAPOR."IdEstadoPortafolioAutorizacion"
	 ,VISTAPOR."EstadoPortafolioAutorizacion";
		 
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupador" TO  BCS_DESARROLLADOR WITH GRANT OPTION;