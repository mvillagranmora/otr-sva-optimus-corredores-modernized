SELECT COR."IdCorredorCorresponsal",pp."IdPerfilCobros",pp."EsPerfilPersonalizado",pp."IdEstado" as "IdEstadoPerfilCobros",
det."IdTipoProducto",det."IdTipoOperacion",det."IdCanalIngreso" as "IdTipoCanalIngreso",det."IdBolsa",det."IdEstado" as "IdEstadoDetallePerfilCobros",
det."IdValorCobro",val."DescripcionValorCobro",val."IdCobro",val."ValorCobro",val."ValorTopeMinimoOrden",val."IdMoneda",val."IdEstado" AS "IdEstadoValorCobro",
cob."FormaCobro",cob."NombreCobro",cob."IdCobroClasificacion",clacob."CodigoCobroClasificacion",cob."PermiteCambiarValor",COR."IdEmpresa"
FROM "BSCL.Orden.BD::ORD.CorredorCorresponsal" COR
INNER JOIN "BSCL.Plataforma.BD::COM.PerfilCobros" pp ON COR."IdPerfilCobros" = pp."IdPerfilCobros"
INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilCobros" det on det."IdPerfilCobros" = pp."IdPerfilCobros"
INNER JOIN "BSCL.Plataforma.BD::COM.ValorCobro" val on val."IdValorCobro" = det."IdValorCobro"
INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" cob on val."IdCobro" = cob."IdCobro"
INNER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" clacob on clacob."IdCobroClasificacion" = cob."IdCobroClasificacion"
WHERE COR."IdCorredorCorresponsal" IN (:idsCorredorCorresponsal) AND COR."IdEmpresa" = :IdEmpresa AND det."Borrado" = 'N'
ORDER BY det."IdBolsa",det."IdTipoProducto",det."IdTipoOperacion",det."IdCanalIngreso";

