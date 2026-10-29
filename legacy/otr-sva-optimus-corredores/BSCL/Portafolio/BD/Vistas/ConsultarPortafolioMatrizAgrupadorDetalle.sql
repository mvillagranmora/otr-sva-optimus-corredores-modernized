SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupadorDetalle";
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupadorDetalle" AS 

SELECT 
   	   SOC."IdSocioDeNegocio"								AS "IdSocioDeNegocio"
	 , SOC."IdPersona"										AS "IdPersona"
	 , SOC."IdCliente"										AS "IdCliente"
	 , POR."IdPortafolio" 									AS "IdPortafolio"
	 , POR."IdMatrizAgrupadorAPV"							AS "IdMatrizAgrupadorAPV"
	 , APV."IdPlanTipoInversion"							AS "IdPlanTipoInversion"
	 , APV."IdProductoAgrupador"							AS "IdProductoAgrupador"
	 , APV."Producto"										AS "Producto"
	 , APV."Codigo"											AS "RegimenTributario"
	 , POR."CodigoPortafolio"								AS "CodigoPortafolio"
	 , EST."Nombre"											AS "EstadoPortafolio"
	 , TO_VARCHAR(POR."FechaIngreso", 'DD/MM/YYYY')			AS "FechaIngreso"
	 , ESTAUT."IdEstado" as "IdEstadoPortafolioAutorizacion"
	 , ESTAUT."Nombre" as "EstadoPortafolioAutorizacion"
FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC 
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR on (SOC."IdCliente" = POR."IdCliente")
INNER JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS APV ON (APV."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON EST."IdEstado" = (CASE WHEN POR."IdEstado" IN ('ACT','VIG') THEN POR."IdEstado" ELSE 'BLO' END)
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTAUT ON ESTAUT."IdEstado" = (CASE WHEN POR."IdEstadoAutorizacion" = 'PEN' THEN 'PDA' ELSE POR."IdEstado" END);
		 
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupadorDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;