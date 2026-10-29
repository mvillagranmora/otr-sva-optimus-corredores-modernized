SELECT 
"IdMesa"
, "IdEmpresa"
, "Codigo"
, "NombreMesa"
, "IdTipoProducto"
, "NombreTipoProducto"
, "IdEstadoMesa"
, "EstadoMesa"
, "IdEstadoRegistro"
, "IdTipoProductoMesaModulo"
, "IdTipoProductoMesa"
, "FechaActualSistema" 
FROM "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" 
WHERE "IdEmpresa" = (:IdEmpresa)
AND "IdEstadoRegistro" = 'ACT'
AND "IdTipoProducto" = 'MEME'
AND "NombreTipoProducto" = 'SPOT';