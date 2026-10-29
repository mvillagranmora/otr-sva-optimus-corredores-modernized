SELECT
	DDJ."KIdContrato" AS "IdContrato",
	DDJ."S4_XR_TipoFlujo",
	DDJ."S4_XR_FacTasa",
	DDJ."S4_XR_SpreadTasa",
	DDJ."S4_XR_TasaFija",
	TO_VARCHAR(DDJ."S4_XR_FecFixTasa",'YYYY-MM-DD') AS "S4_XR_FecFixTasa",
	TO_VARCHAR(DDJ."S4_XR_FecIni",'YYYY-MM-DD') AS "S4_XR_FecIni",
	TO_VARCHAR(DDJ."S4_XR_FecTer",'YYYY-MM-DD') AS "S4_XR_FecTer",
	DDJ."S4_XR_MontoNoc",
	DDJ."S4_XR_MontoInt",
	DDJ."S4_X_MonMonto"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" DDJ
LEFT JOIN "BSCL.Normativa.BD::GBL.ModificarCalendarioPago_RegistroCambio" RC 
ON (
	RC."RutContraparte" = DDJ."KRutCtraparte1"
	AND RC."IdContrato" = DDJ."KIdContrato"
	AND RC."FechaContrato" = DDJ."KFechaContrato"
	AND RC."IdOperacion" = DDJ."KIdOpeEstruc"
	AND RC."Producto" = DDJ."kProducto"
	AND RC."NumeroFlujo" = DDJ."KS4_XR_NroFlu"
)
WHERE DDJ."KIdContrato" IN (:KIdContrato)
AND DDJ."Seccion"=4;