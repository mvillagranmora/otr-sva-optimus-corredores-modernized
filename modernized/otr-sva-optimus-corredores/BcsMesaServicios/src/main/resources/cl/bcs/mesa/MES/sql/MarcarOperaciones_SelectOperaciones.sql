SELECT
     CO."IdOperacion"
    ,CO."FolioOperacion"
    ,CAST(CO."FechaOperacion" AS DATE) AS "FechaOperacion"
    ,CO."IdTipoProducto"
    ,CO."IdTipoOperacion"
    ,CO."IdMesaOrigen"
    ,CO."IdEstado"
    ,CO."IdUsuarioCreacion"
    ,CO."IdUsuarioEdicion"
    ,CO."FechaEdicion"
    ,CO."BloqueoPorEdicion"
    ,CO."CodigoUsuario"
    ,CO."CodigoUsuarioBloqueoEdicion"
    ,CO."FechaHoraInicioEdicion"
FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" CO
WHERE CO."IdOperacion" IN (:IdOperacion);
