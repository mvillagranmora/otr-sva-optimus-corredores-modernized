UPDATE VC
SET VC."IdMoneda" = :ValorNuevo
FROM "BSCL.Plataforma.BD::COM.ValorCobro" VC
JOIN "BSCL.Plataforma.BD::COM.DetallePerfilCobros" DOC ON VC."IdValorCobro" = DOC."IdValorCobro"
WHERE DOC."IdDetallePerfilCobros" = :IdDetallePerfilCobros
