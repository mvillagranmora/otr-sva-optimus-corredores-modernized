SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::MAN.ConsultarAtributoMantenedor"; 
  
CREATE VIEW "BSCL.Plataforma.BD.Vistas::MAN.ConsultarAtributoMantenedor" AS
SELECT 
   ATR."IdAtributoMantenedor",
   ATR."IdMantenedor",
   ATR."NombreDiccionario",
   ATR."VisibleGrilla",
   ATR."VisibleAgregar",
   ATR."VisibleModificar",
   ATR."Orden",
   ATR."ComponenteUI",
   ATR."Formato",
   ATR."NombreCampo",
   ATR."TipoDatoCampo",
   ATR."LargoDatoCampo",
   ATR."ValorDefectoCampo",
   ATR."RequeridoCampo",
   ATR."TipoCampo",   
   ATR."AnchoColumna",
   MAN."TituloMantenedor"
FROM "BSCL.Plataforma.BD::MAN.AtributoMantenedor" AS ATR
INNER JOIN "BSCL.Plataforma.BD::MAN.Mantenedor" AS MAN ON (MAN."IdMantenedor" = ATR."IdMantenedor")
ORDER BY "Orden";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MAN.ConsultarAtributoMantenedor" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
