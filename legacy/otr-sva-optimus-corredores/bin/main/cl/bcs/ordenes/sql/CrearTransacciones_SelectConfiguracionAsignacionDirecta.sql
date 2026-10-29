SELECT 
    CAD."IdTipoInstrumento", 
    CAD."AsignacionAuto",
	CAD."MovimientoFacturarAuto",
	CAD."ComprobanteAuto"
FROM "BSCL.Orden.BD::ORD.ConfiguracionAsignacionDirecta" as CAD 
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS 
ON (TINS."IdTipoInstrumento" = CAD."IdTipoInstrumento")
WHERE CAD."IdEmpresa" = :IdEmpresa;