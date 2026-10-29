SELECT
	DDJ."KIdContrato",
	DDJ."KS5_CorrPago",
	DDJ."KS5_IdPago",
	TO_VARCHAR(DDJ."S5_FecPago",'YYYY-MM-DD') AS "S5_FecPago",
	DDJ."S5_MontoPago",
	DDJ."S5_TipoPago"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" DDJ
LEFT JOIN "BSCL.Normativa.BD::GBL.EliminarPagoPeriodo_RegistroElimina" RC
ON (
	RC."KRutCtraparte1" = DDJ."KRutCtraparte1"
	AND RC."KIdContrato" = DDJ."KIdContrato"
	AND RC."KFechaContrato" = DDJ."KFechaContrato"
	AND RC."KIdOpeEstruc" = DDJ."KIdOpeEstruc"
	AND RC."kProducto" = DDJ."kProducto"
	AND RC."KS5_CorrPago" = DDJ."KS5_CorrPago"
	AND RC."KS5_IdPago" = DDJ."KS5_IdPago"
)
WHERE DDJ."KIdContrato" = :KIdContrato
AND DDJ."Seccion" = 5;