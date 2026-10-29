SELECT
	Contrato."IdPersona"
   ,Contrato."IdCliente"
   ,Contrato."IdContratoSAAPV"
   ,Contrato."FolioContratoSAAPV"
   ,PF."IdPortafolio"
FROM
	"BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
LEFT JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" PF ON PF."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
	WHERE
	Contrato."IdCliente" = :p_IdCliente
AND PF."IdPortafolio" = :p_IdPortafolio
AND Contrato."IdEstado" = 'VIG';