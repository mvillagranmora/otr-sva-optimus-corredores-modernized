SELECT CC."IdContratoComision", 
DCC."IdDetalleContratoComision",
SDCC."IdSubdetalleContratoComision",
CC."IdTipoContrato",
TC."Nombre" AS "TipoContrato",
CC."IdFormatoCobro",
FC."Nombre" AS "FormatoCobro",
CC."IdClasificacionTipoInstrumento",
CTI."Nombre" AS "NombreTipoInstrumento",
Moneda."Nombre" AS "NombreMoneda",
CC."IdSegmentoCliente",
TS."DescripcionSegmento",
CC."IdPortafolio",
PORTAFOLIO."CodigoPortafolio",
DOCID."DocumentoIdentidad" AS "RutCliente",
PORTAFOLIO."Alias" AS "NombreCliente",
DCC."IdCobroClasificacion",
CLASIFICACION."CodigoCobroClasificacion",
COBRO."NombreCobro",
CONCEPTO."Nombre" AS "NombreConcepto",
TO_DECIMAL(SDCC."Monto", 26, 8) AS "Monto",
TO_DECIMAL(SDCC."Porcentaje", 26, 8) AS "Porcentaje",
TO_DECIMAL(SDCC."MontoMaximo", 26, 8) AS "MontoMaximo"
FROM "BSCL.Plataforma.BD::COM.ContratoComision" AS CC
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Area" AS AREA ON AREA."Codigo" = CC."CodigoArea"
LEFT JOIN "BSCL.Plataforma.BD::COM.TipoContrato" AS TC ON TC."IdTipoContrato" = CC."IdTipoContrato"
LEFT JOIN "BSCL.Plataforma.BD::COM.FormatoCobro" AS FC ON FC."IdFormatoCobro" = CC."IdFormatoCobro"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTI ON CTI."IdClasificacionTipoInstrumento" = CC."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS Moneda ON Moneda."IdMoneda" = CC."IdMoneda"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoSegmento" AS TS ON TS."IdSegmentoCliente" = CC."IdSegmentoCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = CC."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE ON CLIENTE."IdCliente" = PORTAFOLIO."IdCliente" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA ON PERSONA."IdPersona" = CLIENTE."IdPersona" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCID ON DOCID."IdDocumentoIdentidad" = PERSONA."IdDocumentoIdentidad" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS PERSONANOMBRECOMPLETO ON PERSONANOMBRECOMPLETO."IdPersona" = CLIENTE."IdPersona" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTADO ON ESTADO."IdEstado" = CC."IdEstado"
LEFT JOIN "BSCL.Plataforma.BD::COM.DetalleContratoComision" AS DCC ON DCC."IdContratoComision" = CC."IdContratoComision"
LEFT JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS CLASIFICACION ON CLASIFICACION."IdCobroClasificacion" = DCC."IdCobroClasificacion"
LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" AS COBRO ON COBRO."IdCobro" = DCC."IdCobro"
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS CONCEPTOFACTURACION ON CONCEPTOFACTURACION."IdConcepto" = DCC."IdConcepto"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONCEPTO ON CONCEPTO."IdConcepto" = CONCEPTOFACTURACION."IdConcepto"
LEFT JOIN "BSCL.Plataforma.BD::COM.SubdetalleContratoComision" AS SDCC ON SDCC."IdDetalleContratoComision" = DCC."IdDetalleContratoComision"
WHERE CC."IdCliente" = CASE WHEN :IdCliente = 0 THEN CC."IdCliente" ELSE :IdCliente END 
AND CC."IdPortafolio" = CASE WHEN :IdPortafolio = 0 THEN CC."IdPortafolio" ELSE :IdPortafolio END 
AND CC."IdClasificacionTipoInstrumento" = CASE WHEN :IdClasificacionTipoInstrumento = '' THEN CC."IdClasificacionTipoInstrumento" ELSE :IdClasificacionTipoInstrumento END
AND ((:IdSegmentoCliente = 0 AND (CC."IdSegmentoCliente" IS NULL OR CC."IdSegmentoCliente" IS NOT NULL))
		OR :IdSegmentoCliente != 0 AND CC."IdSegmentoCliente" = :IdSegmentoCliente)
AND CC."IdEstado" = 'ACT'
ORDER BY CC."FechaIngresoRegistro";