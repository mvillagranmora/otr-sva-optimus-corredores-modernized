SELECT ECC."IdEventoCapital" 		AS "p_IdEventoCapital",
ECC."IdEventoCapitalCliente" 		AS "p_IdEventoCapitalCliente",
ECC."Nemo"							AS "p_Nemo",
ECC."IdMoneda"						AS "p_IdMoneda",
ECC."IdTipoEventoCapital"			AS "p_IdTipoEventoCapital",
ECC."Secuencia"						AS "p_Secuencia",
COALESCE(ECC."NombreCliente", '') 	AS "p_NombreCliente",
COALESCE(P."IdCliente", 0)			AS "p_IdCliente"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" ECC
LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio" P ON P."IdPortafolio" = ECC."IdPortafolio"
WHERE "IdEventoCapitalCliente" IN (:p_IdEventoCapitalCliente)