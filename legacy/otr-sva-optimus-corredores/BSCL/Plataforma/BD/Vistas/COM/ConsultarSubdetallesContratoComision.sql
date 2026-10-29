SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarSubdetallesContratoComision"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarSubdetallesContratoComision"  
AS
SELECT 
SDCC."IdSubdetalleContratoComision",
SDCC."IdDetalleContratoComision",
SDCC."Monto",
SDCC."Porcentaje",
SDCC."MontoMaximo"
FROM "BSCL.Plataforma.BD::COM.SubdetalleContratoComision" AS SDCC;
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarSubdetallesContratoComision" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
