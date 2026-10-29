SELECT ECC."IdEventoCapitalCliente",
ECC."IdEventoCapital",
ECC."NumeroDividendo", 
ECC."CantidadDerecho",
ECC."CantidadNueva",
ECC."Total",
ECC."MontoCapital",
ECC."MontoInteres",
ECC."TipoSocioNegocio",
ECC."TipoCartera",
ECC."IdBoveda",
ECC."Nemo",
ECC."IdMoneda",
COALESCE(ECC."NombreCliente", '') 	AS "NombreCliente",
COALESCE(P."IdCliente", 0)			AS "IdCliente",
COALESCE(P."IdPortafolio", 0)		AS "IdPortafolio",
ECC."MontoImpuestoExtranjero",
ECC."MontoGastoTransferencia"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" ECC
LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio" P ON P."IdPortafolio" = ECC."IdPortafolio"
WHERE "IdEventoCapitalCliente" IN (:p_IdEventoCapitalCliente)