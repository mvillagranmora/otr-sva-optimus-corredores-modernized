SELECT
	DDJ."KIdContrato",
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
LEFT JOIN "BSCL.Normativa.BD::GBL.EliminarCalendarioPago_RegistroElimina" RC
ON (
	RC."KRutCtraparte1" = DDJ."KRutCtraparte1"
	AND RC."KIdContrato" = DDJ."KIdContrato"
	AND RC."KFechaContrato" = DDJ."KFechaContrato"
	AND RC."KIdOpeEstruc" = DDJ."KIdOpeEstruc"
	AND RC."kProducto" = DDJ."kProducto"
	AND RC."KS4_XR_NroFlu" = DDJ."KS4_XR_NroFlu"
)
WHERE DDJ."KIdContrato" = :KIdContrato
AND DDJ."Seccion" = 4;