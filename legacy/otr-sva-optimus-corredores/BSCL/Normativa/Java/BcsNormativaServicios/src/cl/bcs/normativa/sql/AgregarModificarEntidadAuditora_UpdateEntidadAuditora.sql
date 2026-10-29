UPDATE "BSCL.Normativa.BD::CIR.EntidadAuditora"
SET  	"EmpresaAuditora" = :EmpresaAuditora,
		"AuditorEncargado" = :AuditorEncargado,
		"TipoCasilla" = :TipoCasilla,
		"Casilla" = :Casilla,
		"Estado" = :Estado
WHERE "IdEntidadAuditora" = :IdEntidadAuditora; 