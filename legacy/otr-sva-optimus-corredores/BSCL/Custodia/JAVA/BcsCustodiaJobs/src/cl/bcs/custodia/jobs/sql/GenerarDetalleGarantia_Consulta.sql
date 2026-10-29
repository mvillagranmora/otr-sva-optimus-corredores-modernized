SELECT COALESCE(DOC."DocumentoIdentidadAsociado",'') 	AS "DocumentoIdentidad"
     , COALESCE(GTIA."Nemotecnico",'') 					AS "Nemotecnico"
     , COALESCE(GTIA."NombreCliente",'')				AS "RazonSocial"
     , COALESCE(GTIA."Cantidad",0)						AS "Monto"
     , COALESCE(MON."MonedaSMV",0)					 	AS "IdMoneda"
     , TO_NVARCHAR(COALESCE(GTIA."FechaLiquidacion",'0001-01-01'),'DDMMYYYY')		AS "FechaUltimoMovimiento"
FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" AS GTIA
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI 
		ON CLI."IdCliente" = GTIA."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER 
		ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN (
				SELECT "IdDocumentoIdentidad", "IdTipoDocumentoIdentidadAsociado", "DocumentoIdentidadAsociado"
				FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
			) AS DOC 
		ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
		ON MON."IdMoneda" = GTIA."IdMonedaInstrumento"
WHERE (:IdCliente IS NULL OR GTIA."IdCliente" = :IdCliente)
AND (:IdPortafolio IS NULL OR GTIA."IdPortafolio" = :IdPortafolio)
AND (:IdTipoInstrumentoGarantizado IS NULL OR GTIA."IdTipoInstrumentoGarantizado" = :IdTipoInstrumentoGarantizado)
AND (:IdContraparteGarantia IS NULL OR GTIA."IdContraparteGarantia" = :IdContraparteGarantia)
AND (:IdOrigenGarantia IS NULL OR GTIA."IdOrigenGarantia" = :IdOrigenGarantia)
AND (:IdEstado IS NULL OR GTIA."IdEstado" = :IdEstado)
AND (:IdMonedaInstrumento IS NULL OR GTIA."IdMonedaInstrumento" = :IdMonedaInstrumento)
AND (:IdInstrumento IS NULL OR GTIA."IdInstrumento" = :IdInstrumento)
AND (:FolioCarteraGarantizada IS NULL OR GTIA."FolioCarteraGarantizada" = :FolioCarteraGarantizada)
AND (:FechaLiquidacion IS NULL OR GTIA."INGFechaLiquidacion" <= :FechaLiquidacion)