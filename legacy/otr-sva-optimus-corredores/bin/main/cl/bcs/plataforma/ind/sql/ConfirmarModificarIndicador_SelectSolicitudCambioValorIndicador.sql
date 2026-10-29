SELECT 
SOL."IdSolicitudCambio"
, SOL."IdEntidadOrigen"
, SOL."ValorNuevo"
, SOL."ValorAntiguo"
, SOL."CampoModificado"
, COALESCE(TIP_IND."CodigoTipoIndicador",'') AS "NombreTipoIndicador"
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL
LEFT JOIN  "BSCL.Plataforma.BD::IND.Indicador" IND ON CAST(SOL."IdEntidadOrigen" as INT) = IND."IdIndicador"
LEFT JOIN  "BSCL.Plataforma.BD::IND.TipoIndicador" TIP_IND ON TIP_IND."IdTipoIndicador" = IND."IdTipoIndicador"
WHERE SOL."IdSolicitudCambio" = :IdSolicitudCambio AND SOL."CampoModificado" = 'ValorIndicador';