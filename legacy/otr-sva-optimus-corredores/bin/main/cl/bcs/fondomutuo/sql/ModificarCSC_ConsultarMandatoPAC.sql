SELECT top 1 "IdMandatoPAC",
    "IdCliente",
    "IdPortafolio",
    "FolioCSC",
    "FolioPAC",
    "CuentaCorriente",
    "FolioSAAPV",
    "Receptor",
    "Fecha",
    "NumeroMandato",
    "Firma",
    "IdentServicio",
    "Estado",
    "EstadoCargo",
    "IdEmpresa",
    "Canal",
    "EstadoRendicion",
    "EstadoCargoBanco",
    "IdCarga",
    "IdCuentaBancaria",
    "EstadoPorAutorizar",
    "EstadoContratoPorAutorizar",
    "DiaDescuentoPorAutorizar",
    "IdCliente" || '_' || "IdPortafolio" as "Compuesto"
FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC"
WHERE "IdCliente" IN (:IdsCliente)
AND "IdPortafolio" IN (:IdsPortafolio)
AND "FolioCSC" IN (:FolioCSC)
AND "Estado" = 'ING';