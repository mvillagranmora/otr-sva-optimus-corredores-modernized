SELECT  "IdComprobante", 
        "IdTipoComprobante", 
        "IdCargoAbonoMovimiento",
        "NombreTipoMovimiento",
        "IdEstado","IdModulo"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" ConsCarAbo
WHERE 
ConsCarAbo."IdComprobante" IN (:IdOperacion) 
AND ConsCarAbo."IdEstado" <> 'ANU' 
AND ConsCarAbo."IdModulo" = 'MRF';
