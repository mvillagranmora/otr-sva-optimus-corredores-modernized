SELECT "IdMesa","IdCliente","IdPortafolio", "IdEstadoMesa"
FROM "BSCL.Mesa.BD::MES.Mesa"
WHERE "IdTipoProducto" = 'MERF' and
"AplicaFusion" = 'S'