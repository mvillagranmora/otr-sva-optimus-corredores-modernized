SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PAU.ConsultarControlAperturaCierre"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PAU.ConsultarControlAperturaCierre" 
AS

SELECT A."IdEmpresa", 
	   A."NombreModulo", 
	   A."CodigoProceso",
       ESTADO."Nombre" AS "NombreEstadoProceso",
       A."EstadoProceso",
       A."FechaSistema",
       A."FechaEjecucion",
       A."DescripcionProceso",
       A."CodigoModulo",
       A."CodigoUsuario",
       A."TipoProceso",
       A."Orden"
        FROM (
			SELECT P."IdEmpresa", 
				   MODULOPLA."Nombre" AS "NombreModulo",
				   P."CodigoProceso", 
				   CASE COALESCE(CP."CodigoModulo",'') WHEN '' THEN 'NEJ'
													   ELSE 'EJE' END AS "EstadoProceso",
				   CP."FechaSistema",
				   MAX(COALESCE(CP."FechaRegistro",'9999-12-31')) AS "FechaEjecucion",
				   P."DescripcionProceso",
				   MODULO."CodigoModulo",
				   CP."CodigoUsuario",
				   CASE P."EsApertura" WHEN 'S' THEN 'APERTURA' ELSE 'CIERRE' END AS "TipoProceso",
				   P."Orden"
			FROM "BSCL.Plataforma.BD::PAU.Modulo" MODULO
			JOIN "BSCL.Plataforma.BD::PAU.Proceso" P ON (P."CodigoModulo" = MODULO."CodigoModulo") 
			LEFT JOIN "BSCL.Plataforma.BD::PAU.ControlProceso" CP ON (CP."CodigoModulo" = P."CodigoModulo" AND CP."CodigoProceso" = P."CodigoProceso")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Modulo" MODULOPLA ON (MODULOPLA."IdModulo" = MODULO."CodigoModulo")
			GROUP BY P."IdEmpresa", MODULOPLA."Nombre", P."CodigoProceso", CP."CodigoModulo", MODULO."CodigoModulo"
			, CP."FechaSistema",P."DescripcionProceso",CP."CodigoUsuario",P."EsApertura",P."Orden") A
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTADO ON (ESTADO."IdEstado" = a."EstadoProceso")
ORDER BY A."Orden" ASC;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PAU.ConsultarControlAperturaCierre" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
