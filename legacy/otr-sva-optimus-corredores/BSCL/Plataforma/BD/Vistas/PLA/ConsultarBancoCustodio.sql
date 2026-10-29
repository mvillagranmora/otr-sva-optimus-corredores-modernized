SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoCustodio"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoCustodio" AS
	SELECT 	
		"Codigo", 
		"IdEstado", 
		"IdPersona", 
		"IdUsuarioCreador",
		"IdUsuarioUltimaModificacion",
		"FechaCreacion",
		"FechaUltimaModificacion", 
		"EsBancoLiquidador",
		"EsBancoLiquidadorUI",
		"EsBancoCustodio",
        "EsBancoCustodioUI",
		"NombreEstado",
		"NombreCompleto",
		"CodigoNombreCompleto",
		"DocumentoIdentidad"
		
	FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoCustodioLiquidador"
	WHERE "EsBancoCustodio"='S';
			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoCustodio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
