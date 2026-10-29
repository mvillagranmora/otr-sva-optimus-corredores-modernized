SELECT
	VCL."HoraAuditoria",
	VCL."Secuencia",
	VCL."IdMoneda",
	VCL."Nemotecnico",
	VCL."Factor"
FROM "BSCL.Custodia.BD.Vistas::EVC.ConsultarFactorPorMoneda" VCL
WHERE
	VCL."Secuencia" = :Secuencia AND
	VCL."Nemotecnico" = :Nemotecnico AND
	VCL."IdMoneda" IN (:IdMoneda)