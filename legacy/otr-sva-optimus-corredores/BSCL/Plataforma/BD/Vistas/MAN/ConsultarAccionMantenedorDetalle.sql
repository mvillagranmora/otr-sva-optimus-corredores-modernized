SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::MAN.ConsultarAccionMantenedorDetalle"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::MAN.ConsultarAccionMantenedorDetalle"
AS  
Select "IdAccionMantenedor",
       "IdMantenedor",
       "NombreDiccionario",
       "Visible",
       "Orden",
       "Tipo",
       "ServicioBD"
From "BSCL.Plataforma.BD::MAN.AccionMantenedor" ACCIONMANT
Order by "IdAccionMantenedor";  

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MAN.ConsultarAccionMantenedorDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
