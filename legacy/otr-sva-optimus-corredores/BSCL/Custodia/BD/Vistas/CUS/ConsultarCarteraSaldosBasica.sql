SET SCHEMA "BSCL";  
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldosBasica";
  
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldosBasica" AS

SELECT car."IdCartera", car."FolioCartera", car."INGCantidad"
FROM "BSCL.Custodia.BD::CUS.Cartera" car 
WHERE car."EGRCantidad" = 0 AND car."IdEstado" = 'VIG';


GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldosBasica"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

