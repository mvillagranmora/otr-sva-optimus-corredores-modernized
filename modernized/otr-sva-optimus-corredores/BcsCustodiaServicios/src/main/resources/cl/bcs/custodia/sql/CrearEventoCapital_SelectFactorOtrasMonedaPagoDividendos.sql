SELECT
	VCL."HoraAuditoria",
	VCL."Secuencia",
	VCL."IdMoneda",
	VCL."Nemotecnico",
	VCL."Factor"
FROM "BSCL.Custodia.BD.Vistas::EVC.ConsultarFactorPorMoneda" VCL
WHERE
	VCL."Secuencia" IN (:Secuencia) AND
	VCL."IdMoneda" = 'USD'