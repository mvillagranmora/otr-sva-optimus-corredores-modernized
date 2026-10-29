SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarNotificacionesPorCliente"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarNotificacionesPorCliente" 
AS

SELECT
    CE."IdConfiguracionEvento"||'_'||CC."IdCliente"||'_CLI' AS "IdModificacion",
	CE."IdConfiguracionEvento",
    CC."IdEmpresa",
    CC."DocumentoIdentidad",
    CC."IdCliente",
    CC."NombreCompleto" AS "NombreCompleto",
    'CLIENTE' AS "Entidad",
    'CLI' AS "IdTipoEntidad",
    'DATOS GENERALES' AS "DescripcionEntidad",
    CE."IdConfiguracionEvento" AS "IdNotificacion",
    CE."Descripcion" AS "CodNotificacion",
    CTEAC."DescripcionTipoEvento" AS "Notificacion",
    COALESCE(PE."IdEstado",'ACT') AS "IdEstado",
    0 AS "IdPortafolio",
    CASE COALESCE(PE."IdEstado",'ACT') WHEN 'ACT' THEN 'ACTIVO' ELSE E."Nombre" END AS "EstadoNotificacion",
    COALESCE(PE."FechaCreacion", '0001-01-01') AS "FechaActivacion",
    CASE COALESCE(PE."IdEstado",'ACT') WHEN 'ACT' THEN TRUE ELSE FALSE END AS "Activo",
    0 AS "Orden",
    CASE CE."PermiteDesactivarOptCliente" WHEN 'S' THEN TRUE ELSE FALSE END AS "EsEditable"
FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE
INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventosAsociadosAConfiguracion" CTEAC ON CE."IdConfiguracionEvento" = CTEAC."IdConfiguracionEvento"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" IN (SELECT "IdCliente" FROM "BSCL.SocioNegocio.BD::CLI.Cliente")
LEFT JOIN "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" PE ON PE."IdConfiguracionEvento" = CE."IdConfiguracionEvento" AND PE."TipoEntidad" = 'CLI' AND PE."IdEntidad" = CC."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" E ON E."IdEstado" = PE."IdEstado"
WHERE CTEAC."DirEvt" = 'EMPCLI'
UNION ALL
SELECT
	CE."IdConfiguracionEvento"||'_'||CP."IdPortafolio"||'_POR' AS "IdModificacion",
	CE."IdConfiguracionEvento",
    CC."IdEmpresa",
    CC."DocumentoIdentidad",
    CC."IdCliente",
    CC."NombreCompleto" AS "NombreCompleto",
    'PORTAFOLIO' AS "Entidad",
    'POR' AS "IdTipoEntidad",
    COALESCE(CP."CodigoPropositoPortafolio",'Portafolio sin Descripcion') AS "DescripcionEntidad",
    CE."IdConfiguracionEvento" AS "IdNotificacion",
    CE."Descripcion" AS "CodNotificacion",
    CTEAC."DescripcionTipoEvento" ||' ('|| CP."CodigoPropositoPortafolio"||')' AS "Notificacion",
    COALESCE(PE."IdEstado",'ACT') AS "IdEstado",
    CP."IdPortafolio" AS "IdPortafolio",
    CASE COALESCE(PE."IdEstado",'ACT') WHEN 'ACT' THEN 'ACTIVO' ELSE E."Nombre" END AS "EstadoNotificacion",
    COALESCE(PE."FechaCreacion", '0001-01-01') AS "FechaActivacion",
    CASE COALESCE(PE."IdEstado",'ACT') WHEN 'ACT' THEN TRUE ELSE FALSE END AS "Activo",
   	1 AS "Orden",
   	CASE CE."PermiteDesactivarOptCliente" WHEN 'S' THEN TRUE ELSE FALSE END AS "EsEditable"
FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE
INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventosAsociadosAConfiguracion" CTEAC ON CE."IdConfiguracionEvento" = CTEAC."IdConfiguracionEvento"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" IN (SELECT "IdCliente" FROM "BSCL.SocioNegocio.BD::CLI.Cliente")
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP ON CP."IdCliente" = CC."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" PE ON PE."IdConfiguracionEvento" = CE."IdConfiguracionEvento" AND PE."TipoEntidad" = 'POR' AND PE."IdEntidad" = CP."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" E ON E."IdEstado" = PE."IdEstado"
WHERE CTEAC."DirEvt" = 'EMPPOR'
ORDER BY "Orden","IdConfiguracionEvento";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarNotificacionesPorCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
