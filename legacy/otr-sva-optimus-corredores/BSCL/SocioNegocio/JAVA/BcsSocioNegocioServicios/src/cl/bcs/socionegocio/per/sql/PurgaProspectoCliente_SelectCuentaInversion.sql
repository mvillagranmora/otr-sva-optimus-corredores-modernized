SELECT CLI."IdPersona", CIN."IdCuentaInversion" 
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
LEFT JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CIN  
ON CLI."IdCliente" = CIN."IdCliente" 
WHERE CLI."IdPersona" IN (:IdsPersonas);