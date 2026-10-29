UPDATE "BSCL.Custodia.BD::CUS.CuadraturaCircular1518"
SET "CantidadDCVCuentaRelacionado" = 0,
    "CantidadDCVCuentaTercero"     = 0,
    "CantidadDCVCuentaPropia"      = 0,
    "CantidadEmisores"             = 0,
    "CantidadSorteoLetras"         = 0,
    "CantidadBCS"                  = 0,
    "CantidadBEC"                  = 0,
    "CantidadBOV"                  = 0,
    "TotalCustodia"                = "CantidadCustodia" + "CantidadGarantia" + "CantidadVentaPlazo" + "CantidadCompraPlazo",
    "TotalCustodioEmisores"        = "CompraEnTransito" - "VentaEnTransito",
    "Diferencia"                   = ("CompraEnTransito" - "VentaEnTransito") - ("CantidadCustodia" + "CantidadGarantia" + "CantidadVentaPlazo" + "CantidadCompraPlazo"),
    "Valorizacion"                 = ("CantidadCustodia" + "CantidadGarantia" + "CantidadVentaPlazo" + "CantidadCompraPlazo") * "Precio",
    "DiferenciaValorizada"         = (("CompraEnTransito" - "VentaEnTransito") - ("CantidadCustodia" + "CantidadGarantia" + "CantidadVentaPlazo" + "CantidadCompraPlazo")) * "Precio"
WHERE "FechaCuadratura" = :FechaCuadratura;