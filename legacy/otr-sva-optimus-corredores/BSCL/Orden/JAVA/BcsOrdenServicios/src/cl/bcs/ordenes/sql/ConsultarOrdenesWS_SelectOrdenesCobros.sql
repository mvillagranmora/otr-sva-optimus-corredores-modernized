SELECT 
"IdOrden",
"IdCobro",
"NombreCobro",
"CodigoCobroClasificacion",
"ValorCobroPerfil",
"ValorCobroAplicado",
"MontoCobroCalculado",
"MontoCobroCalculadoLocal",
"MontoCobroCalculadoLiquidacion",
"FormaCobro",
"MontoMinimo",
"IdMoneda"
FROM "BSCL.Orden.BD.Vistas::ConsultarCobrosOrden"
WHERE "IdOrden" IN (:p_IdOrden);