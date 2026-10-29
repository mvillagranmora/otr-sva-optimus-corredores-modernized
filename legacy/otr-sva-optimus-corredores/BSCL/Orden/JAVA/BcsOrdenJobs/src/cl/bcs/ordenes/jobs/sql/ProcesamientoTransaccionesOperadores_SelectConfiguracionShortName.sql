SELECT 
    CSN."ShortName", 
    CSN."AsignacionAuto",
	CSN."IdTipoInstrumento",
	CSN."MovimientoFacturarAuto",
	CSN."ComprobanteAuto",
	SN."IdPortafolio",
	SN."DocumentoIdentidad"
FROM "BSCL.Orden.BD.Vistas.ConsultarConfiguracionShortName" as CSN 
LEFT JOIN "BSCL.Orden.BD::ORD.ShortNames" AS SN ON CSN."ShortName" = SN."ShortName"
WHERE SN."Habilitado" = 'S';