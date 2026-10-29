SELECT 
ATR."IdIndicador"
,VAN."ValorIndicador"
,:IdEstadoAutorizacion AS "IdEstadoAutorizacion"
FROM "BSCL.Plataforma.BD::IND.Indicador" ATR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresNormativosCliente" CIC ON ATR."IdIndicador" = CIC."IdIndicador"
LEFT JOIN  "BSCL.Plataforma.BD::IND.Indicador" VAN ON CIC."IdPersona" = VAN."IdEntidad" AND CIC."IdTipoIndicadorPersona" = VAN."IdTipoIndicador"
LEFT JOIN "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL ON CAST(SOL."IdEntidadOrigen" as INT) = VAN."IdIndicador"
WHERE
SOL."IdSolicitudCambio" = :IdSolicitudCambio
AND ATR."IdTipoEntidad" = 'CLIENTE'
AND ATR."IdIndicador" = CIC."IdIndicador"
AND CIC."CodigoTipoIndicadorCliente" = 'RELCORR';