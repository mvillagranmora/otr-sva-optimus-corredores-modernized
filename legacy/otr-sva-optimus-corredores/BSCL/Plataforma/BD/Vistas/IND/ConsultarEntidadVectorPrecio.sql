SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarEntidadVectorPrecio";
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarEntidadVectorPrecio" AS
    SELECT
        "Fecha",
        "Nemotecnico",
        "Entidad",
        "TipoRegistro",
        "ValorRegistro"
    FROM "BSCL.Plataforma.BD::INS.EntidadVectorPrecio"
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarEntidadVectorPrecio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

