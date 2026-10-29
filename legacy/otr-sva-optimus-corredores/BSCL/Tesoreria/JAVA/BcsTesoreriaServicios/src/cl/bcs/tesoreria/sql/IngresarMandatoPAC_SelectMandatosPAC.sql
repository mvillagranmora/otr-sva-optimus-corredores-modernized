SELECT PAC."IdMandatoPAC",PAC."IdCliente",PAC."IdPortafolio",PAC."FolioSAAPV",PAC."FolioCSC",PAC."IdentServicio",PAC."IdCuentaBancaria",
COALESCE(CSC."DiaDescuento", CSAAPV."DiaDescuento",0) AS "DiaDescuentoPorAutorizar"
FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC" AS PAC 
LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" AS CSC ON (PAC."FolioCSC" = CSC."FolioCSC" AND CSC."TipoPago" = 'PAC' AND PAC."IdCliente" = CSC."IdCliente")
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS CSAAPV ON (PAC."FolioSAAPV" = CSAAPV."FolioContratoSAAPV" AND CSAAPV."IdCliente" = PAC."IdCliente" AND PAC."FolioCSC" = '0')
WHERE  PAC."IdentServicio" IN (:IdentServicio) AND (PAC."Estado"= 'ING' OR (PAC."Estado"= 'PAU' AND "EstadoPorAutorizar" = 'ING')) AND (PAC."FolioSAAPV" <> '0' OR PAC."FolioCSC" <> '0');