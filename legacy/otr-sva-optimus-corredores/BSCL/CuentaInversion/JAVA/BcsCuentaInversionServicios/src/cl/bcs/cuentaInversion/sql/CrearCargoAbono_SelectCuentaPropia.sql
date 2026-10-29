SELECT c."IdCuentaInversion" 
FROM "BSCL.Plataforma.BD::PLA.Empresa" e 
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" s ON s."IdSocioDeNegocio"  = e."IdSocioDeNegocio"  
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" c ON c."IdCliente" = s."IdCliente"  
WHERE e."IdEmpresa" = :p_IdEmpresa