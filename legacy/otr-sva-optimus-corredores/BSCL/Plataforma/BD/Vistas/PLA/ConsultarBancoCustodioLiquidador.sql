SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoCustodioLiquidador"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoCustodioLiquidador" AS
    SELECT  
        BANCO."Codigo", 
        BANCO."IdEstado", 
        BANCO."IdPersona", 
        BANCO."IdUsuarioCreador",
        BANCO."IdUsuarioUltimaModificacion",
        BANCO."FechaCreacion",
        BANCO."FechaUltimaModificacion", 
        BANCO."EsBancoLiquidador",
        "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(BANCO."EsBancoLiquidador") AS "EsBancoLiquidadorUI",
        BANCO."EsBancoCustodio",
        "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(BANCO."EsBancoCustodio") AS "EsBancoCustodioUI",
        ESTADO."Nombre" AS "NombreEstado",
        PER."NombreCompleto" as "NombreCompleto",
        BANCO."Codigo" || ' - ' || PER."NombreCompleto" as "CodigoNombreCompleto",
        PER."DocumentoIdentidad" as "DocumentoIdentidad"
        
    FROM "BSCL.Plataforma.BD::PLA.BancoCustodio" AS BANCO
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = BANCO."IdPersona"
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTADO ON ESTADO."IdEstado" = BANCO."IdEstado"
    WHERE BANCO."Borrado"=  'N';
			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBancoCustodioLiquidador" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
