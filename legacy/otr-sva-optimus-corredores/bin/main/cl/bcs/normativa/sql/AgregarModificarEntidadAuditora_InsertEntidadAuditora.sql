INSERT INTO "BSCL.Normativa.BD::CIR.EntidadAuditora"
("IdEntidadAuditora", "EmpresaAuditora","AuditorEncargado","TipoCasilla", "Casilla", "Estado")
VALUES((SELECT COALESCE(MAX("IdEntidadAuditora"),0) + 1 FROM "BSCL.Normativa.BD::CIR.EntidadAuditora"), :EmpresaAuditora,:AuditorEncargado,:TipoCasilla, :Casilla, :Estado);