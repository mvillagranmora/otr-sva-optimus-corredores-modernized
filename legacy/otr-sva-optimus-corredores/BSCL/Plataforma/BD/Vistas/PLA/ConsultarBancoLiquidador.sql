SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoLiquidador"; 
             
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoLiquidador" AS
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
	WHERE "EsBancoLiquidador" = 'S';
			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoLiquidador" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
