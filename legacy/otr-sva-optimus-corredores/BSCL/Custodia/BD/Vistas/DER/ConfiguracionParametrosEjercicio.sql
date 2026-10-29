SET SCHEMA "BSCL"; 

GO 
DROP VIEW "BSCL.Custodia.BD.Vistas::DER.ConfiguracionParametrosEjercicio"; 

GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::DER.ConfiguracionParametrosEjercicio" AS
SELECT * FROM "BSCL.Custodia.BD::DER.Configuracion";

GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::DER.ConfiguracionParametrosEjercicio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;