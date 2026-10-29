SELECT
"IdMesa"
,"NombreMesa"
FROM "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa"
WHERE 
"IdEmpresa" = :IdEmpresa
AND "IdTipoProductoMesaModulo" = 'FWD'
AND "IdMesa" IN (:IdMesa)