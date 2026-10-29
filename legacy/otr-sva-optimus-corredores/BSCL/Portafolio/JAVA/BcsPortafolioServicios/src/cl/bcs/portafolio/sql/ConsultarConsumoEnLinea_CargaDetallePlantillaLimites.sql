SELECT 
PL."IdPortafolio",
PL."CanalIngreso",
LIM."EsLeverage",
PL."IdClasificacionTipoInstrumento",
PL."IdEstado",
PL."IdEmpresa",
PL."IdLimite",
PL."IdMoneda",
PL."IdPlantillaLimite",
PL."IdTipoLimite",
PL."Moneda",
PL."MontoTotal",
PL."Nombre",
PL."NombreClasificacionTipoInstrumento",
PL."NombreEstadoLimite",
PL."Porcentaje",
PL."TipoInstrumento",
PL."TipoOperacion"
FROM "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarLimitePortafolio" PL
LEFT JOIN "BSCL.Plataforma.BD::LIM.Limite" AS LIM ON (LIM."IdLimite" = PL."IdLimite")
WHERE PL."IdPortafolio" = :IdPortafolio
AND PL."IdEmpresa" = :IdEmpresa