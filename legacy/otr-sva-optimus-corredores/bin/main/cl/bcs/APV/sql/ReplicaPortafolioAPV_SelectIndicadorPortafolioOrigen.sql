SELECT
IND."IdTipoEntidad"
,IND."IdTipoIndicador"
,IND."ModeloCliente"
,IND."IdEmpresa"
,IND."ValorIndicador"
,IND."FechaIngreso"
,IND."FechaCreacion"
,IND."FechaModificacion"
,IND."IdEstadoAutorizacion"	
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL
LEFT JOIN  "BSCL.Plataforma.BD::IND.Indicador" IND ON CAST(SOL."IdEntidadOrigen" as INT) = IND."IdIndicador"
LEFT JOIN  "BSCL.Plataforma.BD::IND.TipoIndicador" TIP_IND ON TIP_IND."IdTipoIndicador" = IND."IdTipoIndicador"
WHERE SOL."IdSolicitudCambio" = :IdSolicitudCambio AND SOL."IdEntidadOrigen" = 'INDICADOR' AND TIP_IND."IdTipoEntidad" = 'PORTAFOLIO' AND IND."IdEntidad" = :IdPortafolio;