SELECT TOP 1
    "IdReporte",
    "NombreReporte", 
    "IdServicio",
    "FormatoSalida"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarTemplateFacturacion"
WHERE "CodigoTemplateFacturacion" = 'CMBFAC'
AND "IdEstado" = 'ACT'
AND "IdEmpresa" = :IdEmpresa
AND "IdTipoComprobante" = :IdTipoComprobante
AND "LocalizadorSistema" = :LocalizadorSistema
ORDER BY "IdReporte" DESC