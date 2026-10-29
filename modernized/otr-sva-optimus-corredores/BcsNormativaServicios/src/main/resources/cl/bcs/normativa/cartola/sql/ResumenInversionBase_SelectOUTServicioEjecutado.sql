SELECT "IdCliente",
"IdPortafolio",
"Grupo",
"Item",
"IdMonedaOperacion",
"MontoMonedaOperacion",
"IdMonedaSistema",
"MontoMonedaSistema",
"TotalMonedaSistema",
"IdMonedaAdicional",
"MontoMonedaAdicional",
"TotalMonedaAdicional",
"MonedaResumenInversion",
"IdTrabajo"
FROM "BSCL.Normativa.BD::CAR.OUTResumenInversionBasePaso"
WHERE "IdNomina" = :IdNomina;