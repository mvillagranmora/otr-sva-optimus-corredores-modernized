SELECT
"IdTipoContabilidad"
,"IdTipoCentralizacion"
FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo"
WHERE "IdTipoContabilidad" IN (:IdTipoContabilidad) AND "Estado" = 'V'