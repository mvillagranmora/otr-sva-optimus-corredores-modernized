SELECT MOV."ExcluyeMovimientoEnCartola", CAM."IdCargoAbonoMovimiento"
FROM "BSCL.Normativa.BD::CAR.PortafolioPaso" POR 
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"AS MOV ON MOV."IdPortafolio" = POR."IdPortafolio" 
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM ON CAM."IdMovimientoAsociado" = MOV."IdMovimientoCarteraEnTransito" AND CAM."IdConcepto" = 'REGGTA'
WHERE MOV."IdOrigenGarantia" = 'CTA' AND CAM."IdConcepto" = 'REGGTA'
UNION ALL
SELECT MOV."ExcluyeMovimientoEnCartola", CAM."IdCargoAbonoMovimiento"
FROM "BSCL.Normativa.BD::CAR.PortafolioPaso" POR 
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"AS MOV ON MOV."IdPortafolio" = POR."IdPortafolio"
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM ON CAM."IdMovimientoAsociado" = MOV."IdMovimientoCarteraEnTransito" AND CAM."IdTipoOperacion" = 'RETGTA'
WHERE MOV."IdOrigenGarantia" = 'CTA' AND CAM."IdConcepto" != 'REGGTA'
UNION ALL
SELECT	MOV."ExcluyeMovimientoEnCartola" , CAM."IdCargoAbonoMovimiento"
FROM "BSCL.Normativa.BD::CAR.PortafolioPaso" POR
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"AS MOV ON MOV."IdPortafolio" = POR."IdPortafolio"
LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS GAR ON  GAR."IdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito" 
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM ON CAM."IdComprobante" = GAR."IdGarantia" AND CAM."IdTipoOperacion" = 'INGGTA'
WHERE MOV."IdOrigenGarantia" = 'CTA' AND CAM."IdConcepto" != 'REGGTA';