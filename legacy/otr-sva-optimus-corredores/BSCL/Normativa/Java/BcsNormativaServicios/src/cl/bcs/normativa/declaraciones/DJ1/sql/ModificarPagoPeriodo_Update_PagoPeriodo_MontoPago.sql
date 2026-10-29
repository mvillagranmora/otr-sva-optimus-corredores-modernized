-- Actualiza el campo S5_MontoPago
UPDATE  PAGPER
SET
	PAGPER."S5_MontoPago" = RC."ValorNuevo",
	PAGPER."FecUltMod" = TO_NVARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'),
	PAGPER."UsrUltMod" =  USU."CodigoUsuario"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" PAGPER
INNER JOIN "BSCL.Normativa.BD::GBL.ModificarPagosPeriodo_RegistroCambio" RC
ON (
RC."RutContraparte" = PAGPER."KRutCtraparte1"
AND RC."IdContrato" = PAGPER."KIdContrato"
AND RC."FechaContrato" = PAGPER."KFechaContrato"
AND RC."IdOperacion" = PAGPER."KIdOpeEstruc"
AND RC."Producto" = PAGPER."kProducto"
AND RC."PeriodoComercial" = PAGPER."kPeriodoComercial"
AND RC."CorrelativoPago" = PAGPER."KS5_CorrPago" 
AND RC."IdPago" = PAGPER."KS5_IdPago"
)
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU ON USU."IdUsuario" = RC."IdUsuario"
WHERE RC."CampoModificado" = :CampoModificado; -- S5_MontoPago
