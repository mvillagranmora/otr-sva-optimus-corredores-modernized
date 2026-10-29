SELECT 
"IdValorCobro"
,"DescripcionValorCobro"
,"IdTipoProducto"
,"IdCobro"
,"ValorCobro"
,"ValorTopeMinimoOrden"
,"IdMoneda"
,"IdEstado"
,"IdEmpresa"
FROM "BSCL.Plataforma.BD::COM.ValorCobro"
WHERE "IdValorCobro" = :IdValorCobro;