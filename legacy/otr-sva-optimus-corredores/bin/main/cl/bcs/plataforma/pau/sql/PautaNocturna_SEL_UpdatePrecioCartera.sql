UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET "INGPrecioCompra" = :INGPrecioCompra,
"PrecioCompra" = :PrecioCompra
WHERE "IdCartera" = :IdTablaOrigen;