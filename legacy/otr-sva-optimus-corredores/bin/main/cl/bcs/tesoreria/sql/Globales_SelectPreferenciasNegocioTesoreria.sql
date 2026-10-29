/**
 * Proposito: Consulta las preferencias de negocio de tesorería para portafolios
 * Autor: JVILLAGRAN - BCS
 * Fecha Creacion: 18/09/2017
 */ 

SELECT
	PRE."IdPortafolio",
	PRE."IdCliente",
	PRE."IdTipoCliente",
	PRE."TES_NIE"     -- indicador: Netea Ingreso Egreso
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasTesoreria" AS PRE
WHERE "IdPortafolio" IN (:idsPortafolio);
