SELECT
	"Contrato"."IdPersona"
   ,"Contrato"."IdCliente"
   ,"Contrato"."IdContratoSAAPV"
   ,"Contrato"."FolioContratoSAAPV"
   ,"Contrato"."DiaDescuento"
   ,"Contrato"."IdMedioRecaudacionFormaPago"
   ,"Contrato"."IdPortafolio"
   ,PF."IdPortafolio" AS "IdPortafolioContrato"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS "Contrato"
LEFT JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" PF ON PF."IdContratoSAAPV" = "Contrato"."IdContratoSAAPV"
WHERE
	"Contrato"."IdCliente" = :p_IdCliente
AND "Contrato"."IdContratoSAAPV" = :p_IdContratoSAAPV;