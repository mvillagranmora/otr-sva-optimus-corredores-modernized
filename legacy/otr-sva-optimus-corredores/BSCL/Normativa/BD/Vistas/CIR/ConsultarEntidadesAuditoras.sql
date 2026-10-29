SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarEntidadesAuditoras";
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarEntidadesAuditoras" AS
SELECT  "IdEntidadAuditora",
		"EmpresaAuditora",
        "AuditorEncargado",
		"TipoCasilla",
		"Casilla",
		"Estado"
FROM 
"BSCL.Normativa.BD::CIR.EntidadAuditora";
GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CIR.ConsultarEntidadesAuditoras" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 