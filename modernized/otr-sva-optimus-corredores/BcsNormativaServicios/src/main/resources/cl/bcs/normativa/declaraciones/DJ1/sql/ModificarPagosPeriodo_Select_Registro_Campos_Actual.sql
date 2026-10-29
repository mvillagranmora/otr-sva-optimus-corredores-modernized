SELECT
	DDJ."KIdContrato" AS "IdContrato",
	DDJ."KS5_CorrPago",
	DDJ."KS5_IdPago",
	TO_VARCHAR(DDJ."S5_FecPago",'YYYY-MM-DD') AS "S5_FecPago",
	DDJ."S5_MontoPago",
	DDJ."S5_TipoPago",
	DDJ."S5_CodMoneda"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" AS DDJ
LEFT JOIN "BSCL.Normativa.BD::GBL.ModificarPagosPeriodo_RegistroCambio" RC 
ON (
	RC."RutContraparte" = DDJ."KRutCtraparte1"
	AND RC."IdContrato" = DDJ."KIdContrato"
	AND RC."FechaContrato" = DDJ."KFechaContrato"
	AND RC."IdOperacion" = DDJ."KIdOpeEstruc"
	AND RC."Producto" = DDJ."kProducto"
	AND RC."CorrelativoPago" = DDJ."KS5_CorrPago" 
	AND RC."IdPago" = DDJ."KS5_IdPago"
)
WHERE DDJ."KIdContrato" IN (:KIdContrato)
AND DDJ."Seccion"=5;