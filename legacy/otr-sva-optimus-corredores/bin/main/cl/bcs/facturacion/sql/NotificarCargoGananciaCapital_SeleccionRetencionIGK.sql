SELECT "IdEmpresa", "CodigoParametro", "Descripcion", "Valor", 
"EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles",
"ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro",
"FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones"
FROM "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" 
WHERE "CodigoParametro" = :p_CodigoParametro
and "IdEmpresa" = :p_IdEmpresa;