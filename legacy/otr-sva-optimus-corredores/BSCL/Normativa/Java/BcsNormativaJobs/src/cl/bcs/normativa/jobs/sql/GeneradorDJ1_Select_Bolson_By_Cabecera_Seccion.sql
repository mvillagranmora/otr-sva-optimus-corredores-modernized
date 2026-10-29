SELECT 
"KS4_XR_NroFlu"
,"S4_XR_TipoFlujo"
,"S4_XR_FacTasa"
,"S4_XR_SpreadTasa"
,"S4_XR_TasaFija"
,"S4_XR_FecFixTasa"
,"S4_XR_FecIni"
,"S4_XR_FecTer"
,"S4_XR_MontoNoc"
,"S4_XR_MontoInt"
,"S4_X_MonMonto"
,"KS5_CorrPago"
,"KS5_IdPago"
,"S5_FecPago"
,"S5_MontoPago"
,"S5_TipoPago"
,"S5_CodMoneda"
,"KS6_IdGar"
,"S6_TipoGar"
,"S6_MonVal"
,"S6_MontoUmb"
,"S6_ValGtia"
,"S7_TipoGar"
,"KS7_IdGar"
,"S7_TipoActGar"
,"S7_PorcGar"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" 
WHERE "KRutCtraparte1" = :KRutCtraparte1
AND "KIdContrato" = :KIdContrato
AND	"KFechaContrato" = :KFechaContrato
AND	"KIdOpeEstruc" = :KIdOpeEstruc
AND	"kProducto" = :kProducto
AND	"kPeriodoComercial" = :kPeriodoComercial
AND "Seccion" = :seccion; 