SELECT DISTINCT
	TES."IdIngresoEgreso",
	TES."IdEstado",
    TES."IdComprobanteOrigen",
    TES."Monto",
    TES."IdCliente",
    TES."NombrePersona",
    TES."IdPortafolio",
    TES."IdTipoOperacion",
    TES."IdMoneda",
    TES."Nemotecnico",
    CD."IdEstado" AS "EstadoComprobanteDetalle"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" TES
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" CD ON CD."IdComprobanteOrigen" = TES."IdComprobanteOrigen"
WHERE TES."IdModulo" = 'FAC'
AND TES."IdComprobanteOrigen" IN (:idsComprobanteOrigen)
AND NOT EXISTS (
	SELECT 1 FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" CD2
	WHERE CD2."IdComprobanteOrigen" = TES."IdComprobanteOrigen" AND CD2."IdEstado" = 'LIQ');



        
        
      