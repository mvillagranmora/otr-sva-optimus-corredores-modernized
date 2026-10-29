SELECT por."IdPortafolio",pp."IdPerfilCobros",pp."EsPerfilPersonalizado",pp."IdEstado" as "IdEstadoPerfilCobros",
det."IdTipoProducto",det."IdTipoOperacion",det."IdCanalIngreso" as "IdTipoCanalIngreso",det."IdBolsa",det."IdEstado" as "IdEstadoDetallePerfilCobros",
det."IdValorCobro",val."DescripcionValorCobro",val."IdCobro",val."ValorCobro",val."ValorTopeMinimoOrden",val."IdMoneda",val."IdEstado" AS "IdEstadoValorCobro",
cob."FormaCobro",cob."NombreCobro",cob."IdCobroClasificacion",clacob."CodigoCobroClasificacion",cob."PermiteCambiarValor",cli."IdEmpresa"
FROM "BSCL.Portafolio.BD::POR.Portafolio" por
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli on por."IdCliente" = cli."IdCliente"
INNER JOIN "BSCL.Plataforma.BD::COM.PerfilCobros" pp ON POR."IdPerfilCobros" = pp."IdPerfilCobros"
INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilCobros" det on det."IdPerfilCobros" = pp."IdPerfilCobros"
INNER JOIN "BSCL.Plataforma.BD::COM.ValorCobro" val on val."IdValorCobro" = det."IdValorCobro"
INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" cob on val."IdCobro" = cob."IdCobro"
INNER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" clacob on clacob."IdCobroClasificacion" = cob."IdCobroClasificacion"
WHERE por."IdPortafolio" IN (:idsPortafolios) AND cli."IdEmpresa" = :IdEmpresa AND det."Borrado" = 'N'
ORDER BY por."IdPortafolio",det."IdBolsa",det."IdTipoProducto",det."IdTipoOperacion",det."IdCanalIngreso";
