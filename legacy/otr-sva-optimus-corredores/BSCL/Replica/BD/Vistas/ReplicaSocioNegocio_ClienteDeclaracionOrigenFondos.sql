SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteDeclaracionOrigenFondos";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteDeclaracionOrigenFondos" AS
SELECT
    DOF."IdDeclaracionOrigenFondos" AS TABLE_KEY,
    DOF."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
    DOF."IdPersona" AS "IDPERSONA",
    DOF."IdCliente" AS "IDCLIENTE",
    DOF."IdDeclaracionOrigenFondos" AS "IDDECLARACIONORIGENFONDOS",
    DOF."IdTipoOperacionOrigenFondos" AS "IDTIPOOPERACIONORIGENFONDOS",
    DOF."NombreTipoOperacionOrigenFondos" AS "NOMBRETIPOOPERACIONORIGENFONDOS",
    DOF."FechaDeclaracion" AS "FECHADECLARACION",
    DOF."Comentario" AS "COMENTARIO",
    DOF."IdUsuarioCreador" AS "IDUSUARIOCREADOR",
    DOF."FechaIngreso" AS "FECHAINGRESO",
    DOF."FechaCreacion" AS "FECHACREACION",
    DOF."FechaModificacion" AS "FECHAMODIFICACION",
    DOF."IdEmpresa" AS "IDEMPRESA",
    DOF."IdEstado" AS "IDESTADO",
    DOF."NombreEstado" AS "NOMBREESTADO",
    DOF."IdEstadoAutorizacion" AS "IDESTADOAUTORIZACION",
    DOF."NombreEstadoAutorizacion" AS "NOMBREESTADOAUTORIZACION",
    DOF."IdEstadoCalculado" AS "IDESTADOCALCULADO",
    DOF."OperacionInscripcion" AS "OPERACIONINSCRIPCION",
    ESTCALC."Nombre" AS "NOMBREESTADOCALCULADO"
FROM (
    SELECT
    	DOC."DocumentoIdentidad",
        ORIGEN."IdDeclaracionOrigenFondos",
        ORIGEN."IdCliente",
        CLI."IdPersona",
        ORIGEN."IdTipoOperacionOrigenFondos",
        TIPOP."Nombre" AS "NombreTipoOperacionOrigenFondos",
        ORIGEN."FechaDeclaracion",
        ORIGEN."Comentario",
        ORIGEN."IdUsuarioCreador",
        ORIGEN."FechaIngreso",
        ORIGEN."FechaCreacion",
        ORIGEN."FechaModificacion",
        ORIGEN."IdEmpresa",
        ORIGEN."IdEstado",
        ESTDOF."Nombre" AS "NombreEstado",
        ORIGEN."IdEstadoAutorizacion",
        ESTAUTDOF."Nombre" AS "NombreEstadoAutorizacion",
        CASE
            WHEN ORIGEN."IdEstado" = 'ACT' AND ORIGEN."IdEstadoAutorizacion" = 'AUT' THEN 'ACT'
            WHEN ORIGEN."IdEstado" = 'ACT' AND ORIGEN."IdEstadoAutorizacion" = 'ING' THEN 'ACT'
            WHEN ORIGEN."IdEstado" = 'ACT' AND ORIGEN."IdEstadoAutorizacion" = '' THEN 'PEN'
            WHEN ORIGEN."IdEstado" = 'ACT' THEN IFNULL(ORIGEN."IdEstadoAutorizacion", 'PEN')
            ELSE ORIGEN."IdEstado"
        END AS "IdEstadoCalculado",
        ORIGEN."OperacionInscripcion"
    FROM "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos" AS ORIGEN
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTDOF ON ESTDOF."IdEstado" = ORIGEN."IdEstado"
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTAUTDOF ON ESTAUTDOF."IdEstado" = ORIGEN."IdEstadoAutorizacion"
    LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOP ON TIPOP."IdTipoOperacion" = ORIGEN."IdTipoOperacionOrigenFondos"
    LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = ORIGEN."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = CLI."IdPersona" AND DOC."Borrado" = 'N' AND DOC."Predeterminado" = 'S'
) AS DOF
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTCALC ON ESTCALC."IdEstado" = DOF."IdEstadoCalculado"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_ClienteDeclaracionOrigenFondos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;