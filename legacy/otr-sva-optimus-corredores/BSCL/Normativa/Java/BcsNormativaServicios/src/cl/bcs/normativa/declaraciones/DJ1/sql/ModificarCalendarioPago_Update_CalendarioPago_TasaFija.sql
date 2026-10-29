-- Actualiza el campo S4_XR_TasaFija
UPDATE  CALPAG
SET
	CALPAG."S4_XR_TasaFija" = RC."ValorNuevo",
	CALPAG."FecUltMod" = TO_NVARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'),
	CALPAG."UsrUltMod" =  USU."CodigoUsuario"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" CALPAG
INNER JOIN "BSCL.Normativa.BD::GBL.ModificarCalendarioPago_RegistroCambio" RC
ON (
RC."RutContraparte" = CALPAG."KRutCtraparte1"
AND RC."IdContrato" = CALPAG."KIdContrato"
AND RC."FechaContrato" = CALPAG."KFechaContrato"
AND RC."IdOperacion" = CALPAG."KIdOpeEstruc"
AND RC."Producto" = CALPAG."kProducto"
AND RC."PeriodoComercial" = CALPAG."kPeriodoComercial"
AND RC."NumeroFlujo" = CALPAG."KS4_XR_NroFlu"
)
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU ON USU."IdUsuario" = RC."IdUsuario"
WHERE RC."CampoModificado" = :CampoModificado -- S4_XR_TasaFija
AND CALPAG."Seccion" = 4;