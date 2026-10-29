SELECT cop."CodigoOperador",
	cop."UtilizaPortafolio",
	cop."IdPortafolio",
	derp."IdTipoInstrumento",
	derp."AsignacionAuto",
	cop."ReservaTransaccion" 
FROM "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" as cop
LEFT JOIN "BSCL.Plataforma.BD::TRA.DetalleConfiguracionOperador" derp ON cop."CodigoOperador" = derp."CodigoOperador" AND derp."IdEstado" = 'ACT' 
WHERE cop."IdEmpresa" = :IdEmpresa AND cop."IdEstado" = 'ACT'
AND cop."CodigoOperador" IN (:codigosOperador)
;