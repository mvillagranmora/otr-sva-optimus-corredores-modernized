SELECT 
ATR."IdIndicador"
,TO_VARCHAR(D."IdDetalleIndicador") AS "ValorIndicador"
,:IdEstadoAutorizacion AS "IdEstadoAutorizacion"
FROM "BSCL.Plataforma.BD::IND.Indicador" ATR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresNormativosCliente" CIC ON ATR."IdIndicador" = CIC."IdIndicador"
LEFT JOIN  "BSCL.Plataforma.BD::IND.Indicador" VAN ON CIC."IdPersona" = VAN."IdEntidad" AND CIC."IdTipoIndicadorPersona" = VAN."IdTipoIndicador"
LEFT JOIN "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL ON CAST(SOL."IdEntidadOrigen" as INT) = VAN."IdIndicador"
LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" D ON D."IdTipoIndicador" = CIC."IdTipoIndicadorCliente"
LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" DIN ON TO_VARCHAR(DIN."IdDetalleIndicador") = TO_VARCHAR(VAN."ValorIndicador") AND D."Codigo" = DIN."Codigo"
WHERE 
SOL."IdSolicitudCambio" = :IdSolicitudCambio
AND ATR."IdTipoEntidad" = 'CLIENTE'
AND ATR."IdIndicador" = CIC."IdIndicador"
AND CIC."CodigoTipoIndicadorCliente" = 'NCG380'
AND DIN."IdDetalleIndicador" IS NOT NULL;