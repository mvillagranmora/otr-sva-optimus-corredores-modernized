SELECT corr."IdCorredorCorresponsal",corr."IdCobro",corr."ValorCobro",
cob."FormaCobro",cob."ValorTopeMinimoOrden",cob."IdMoneda",cob."NombreCobro",cob."IdCobroClasificacion",
cob."CodigoCobroClasificacion",cob."PermiteCambiarValor",cob."IdEmpresa"

FROM "BSCL.Orden.BD::ORD.CorredorCorresponsal" corr
INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarCobro" cob on cob."IdCobro" = corr."IdCobro"
WHERE corr."IdCorredorCorresponsal" in (:idsCorredorCorresponsal);