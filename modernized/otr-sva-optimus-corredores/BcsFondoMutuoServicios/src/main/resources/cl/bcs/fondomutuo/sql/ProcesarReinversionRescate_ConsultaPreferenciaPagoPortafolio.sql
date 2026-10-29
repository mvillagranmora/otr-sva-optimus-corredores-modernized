SELECT 
PPP."IdPortafolio"
,PPP."IdPreferenciaPagoPortafolio"
FROM "BSCL.Plataforma.BD::PAG.PreferenciaPago" PP
LEFT JOIN "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" PPP ON PP."IdPreferenciaPago" = PPP."IdPreferenciaPago" AND PP."IdEmpresa" = PPP."IdEmpresa"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON PPP."IdPortafolio" = POR."IdPortafolio" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente" AND PPP."IdEmpresa" = CLI."IdEmpresa"
WHERE PP."IdTipoOperacion" = :p_IdTipoOperacion 
AND PP."IdMedioDePago" = :p_IdMedioDePago 
AND PP."IdMoneda" = :p_IdMoneda
AND POR."IdPortafolio" IN (:p_IdPortafolio)
AND CLI."IdCliente" = :p_IdCliente
AND PP."IdEmpresa" = :p_IdEmpresa
AND PPP."IdCuentaBancaria" = :p_IdCuentaBancaria
AND PPP."IdEstado" = 'ACT'