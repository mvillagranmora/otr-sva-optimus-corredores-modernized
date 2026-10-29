SELECT Portafolio."IdPortafolio"
FROM "BSCL.Plataforma.BD::IND.Indicador" IND
LEFT JOIN "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL ON CAST(SOL."IdEntidadOrigen" as INT) = IND."IdIndicador"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" Portafolio ON Portafolio."IdPortafolio" = IND."IdEntidad"
WHERE SOL."IdSolicitudCambio" = :IdSolicitudCambio AND IND."IdTipoEntidad" = 'PORTAFOLIO' AND Portafolio."Proposito" = 'PROPOSITOAPV'