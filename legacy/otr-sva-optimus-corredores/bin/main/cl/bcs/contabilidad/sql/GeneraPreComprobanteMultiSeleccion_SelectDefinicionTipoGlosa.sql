SELECT 
"IdTipoContabilidad"
,"IdDefinicionAsiento"
,"Origen"
,"Orden"
,"GlosaCampo"
,"NombreCampoMovimiento"
,"IdTipoDatoEvento"
,"Estado"
FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoGlosa"
WHERE "IdTipoContabilidad" IN (:IdTipoContabilidad) AND "Estado" = 'V'
ORDER BY "IdTipoContabilidad", "Orden"