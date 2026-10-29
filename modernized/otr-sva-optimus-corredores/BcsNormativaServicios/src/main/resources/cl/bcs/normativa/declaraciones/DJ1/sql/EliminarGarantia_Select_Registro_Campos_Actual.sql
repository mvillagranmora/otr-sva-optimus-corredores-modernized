SELECT
	DDJ."KIdContrato",
	DDJ."S6_TipoGar",
	DDJ."S6_MonVal",
	DDJ."KS6_IdGar",
	DDJ."S6_MontoUmb",
	DDJ."S6_ValGtia"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" DDJ
LEFT JOIN "BSCL.Normativa.BD::GBL.EliminarGarantia_RegistroElimina" RC
ON (
	RC."KRutCtraparte1" = DDJ."KRutCtraparte1"
	AND RC."KIdContrato" = DDJ."KIdContrato"
	AND RC."KFechaContrato" = DDJ."KFechaContrato"
	AND RC."KIdOpeEstruc" = DDJ."KIdOpeEstruc"
	AND RC."kProducto" = DDJ."kProducto"
	AND RC."KS6_IdGar" = DDJ."KS6_IdGar"
)
WHERE DDJ."KIdContrato" = :KIdContrato
AND DDJ."Seccion" = 6;