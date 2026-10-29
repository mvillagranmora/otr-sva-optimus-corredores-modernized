SELECT  CLI."IdCliente",
		Contrato."IdContratoSAAPV",PortafolioContrato."OrigenAhorro"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" Contrato ON (Contrato."IdCliente" = CLI."IdCliente")
INNER JOIN "BSCL.APV.BD::APV.Estado" AS Estado ON (Estado."Codigo" = Contrato."IdEstado" AND Estado."Nombre" = 'VIGENTE')
INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" PortafolioContrato ON  (PortafolioContrato."IdContratoSAAPV" = Contrato."IdContratoSAAPV" AND PortafolioContrato."OrigenAhorro" IN ('APV','DC'))
WHERE   CLI."IdCliente" in (:IdCliente);