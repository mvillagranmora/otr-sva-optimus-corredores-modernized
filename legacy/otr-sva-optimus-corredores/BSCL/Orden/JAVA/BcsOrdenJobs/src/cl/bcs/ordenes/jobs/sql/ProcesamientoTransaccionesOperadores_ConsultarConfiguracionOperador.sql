SELECT COP."CodigoOperador","UtilizaPortafolio","IdPortafolio","ReservaTransaccion","IdTipoInstrumento","AsignacionAuto","MovimientoFacturarAuto","ComprobanteAuto"
FROM "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" as COP
INNER JOIN "BSCL.Plataforma.BD::TRA.DetalleConfiguracionOperador" DERP ON COP."CodigoOperador" = DERP."CodigoOperador" AND DERP."IdEstado" = 'ACT'
WHERE COP."IdEmpresa" = :IdEmpresa AND COP."CodigoOperador" in (:codigosOperador) AND COP."IdEstado" = 'ACT';