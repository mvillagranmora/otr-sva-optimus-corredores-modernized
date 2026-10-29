SELECT
	Contrato."IdPersona"
   ,Contrato."IdCliente"
   ,Contrato."IdContratoSAAPV"
   ,Contrato."FolioContratoSAAPV"
   ,Contrato."IdPortafolio"
FROM
	"BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
WHERE
	Contrato."IdCliente" = :p_IdCliente
AND Contrato."IdPortafolio" = :p_IdPortafolio
AND Contrato."IdEstado" = 'VIG';