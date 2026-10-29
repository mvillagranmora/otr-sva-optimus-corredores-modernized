SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteDetalleDeclaracionOrigenFondos";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteDetalleDeclaracionOrigenFondos" AS
SELECT
    DOFBASE."IdDeclaracionOrigenFondos" || ':' || DDOF."IdTipoOrigenFondo" AS TABLE_KEY,
    DOFBASE."IdDeclaracionOrigenFondos" AS "IDDECLARACIONORIGENFONDOS",
    CLI."IdPersona" AS "IDPERSONA",
    DOFBASE."IdCliente" AS "IDCLIENTE",
    DOC."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
    DOFBASE."IdTipoOperacionOrigenFondos" AS "IDTIPOOPERACIONORIGENFONDOS",
    TIPOP."Nombre" AS "NOMBRETIPOOPERACIONORIGENFONDOS",
    DOFBASE."FechaDeclaracion" AS "FECHADECLARACION",
    DOFBASE."Comentario" AS "COMENTARIO",
    DOFBASE."IdUsuarioCreador" AS "IDUSUARIOCREADOR",
    DOFBASE."FechaIngreso" AS "FECHAINGRESO",
    DOFBASE."FechaCreacion" AS "FECHACREACION",
    DOFBASE."FechaModificacion" AS "FECHAMODIFICACION",
    DOFBASE."IdEmpresa" AS "IDEMPRESA",
    DOFBASE."IdEstado" AS "IDESTADO",
    ESTDOF."Nombre" AS "NOMBREESTADO",
    DOFBASE."OperacionInscripcion" AS "OPERACIONINSCRIPCION",
    CASE WHEN IFNULL(DDOF."IdDeclaracionOrigenFondos", 0) = 0 THEN 'N' ELSE 'S' END AS "DETALLEEXISTE",
    DDOF."IdTipoOrigenFondo" AS "IDTIPOORIGENFONDO",
    IFNULL(TOFDET."Nombre", TOFBASE."Nombre") AS "NOMBRETIPOORIGENFONDO",
    IFNULL(DDOF."Valor", 0) AS "VALOR",
    DDOF."IdUsuarioCreador" AS "IDUSUARIOCREADORDETALLE",
    DDOF."FechaIngreso" AS "FECHAINGRESODETALLE",
    DDOF."FechaCreacion" AS "FECHACREACIONDETALLE",
    DDOF."FechaModificacion" AS "FECHAMODIFICACIONDETALLE",
    IFNULL(DDOF."IdEstado", '') AS "IDESTADODETALLE",
    ESTDDOF."Nombre" AS "NOMBREESTADODETALLE",
    IFNULL(DDOF."Otros", '') AS "OTROS"
FROM "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos" AS DOFBASE
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTDOF ON ESTDOF."IdEstado" = DOFBASE."IdEstado"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOP ON TIPOP."IdTipoOperacion" = DOFBASE."IdTipoOperacionOrigenFondos"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo" AS TOFBASE ON TOFBASE."IdEstado" = 'ACT' AND TOFBASE."IdEmpresa" = DOFBASE."IdEmpresa"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.DetalleDeclaracionOrigenFondos" AS DDOF ON DDOF."IdDeclaracionOrigenFondos" = DOFBASE."IdDeclaracionOrigenFondos" AND DDOF."IdTipoOrigenFondo" = TOFBASE."IdTipoOrigenFondo"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo" AS TOFDET ON TOFDET."IdTipoOrigenFondo" = DDOF."IdTipoOrigenFondo" AND TOFDET."IdEmpresa" = DOFBASE."IdEmpresa"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTDDOF ON ESTDDOF."IdEstado" = DDOF."IdEstado"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = DOFBASE."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = CLI."IdPersona" AND DOC."Borrado" = 'N' AND DOC."Predeterminado" = 'S'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteDetalleDeclaracionOrigenFondos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;