SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::INT.ConsultarFilialesP40";

CREATE VIEW "BSCL.Normativa.BD.Vistas::INT.ConsultarFilialesP40" (
 "IdSocioDeNegocio"
,"Rut"
,"NombreFilial"
,"IdEmpresa"
,"CodigoTenedor"
,"NombreZip"
,"NombreArchivo"
)
AS 

SELECT 
 "IdSocioDeNegocio"
,"Rut"
,"NombreFilial"
,"IdEmpresa"
,"CodigoTenedor"
,"NombreZip"
,"NombreArchivo"
FROM "BSCL.Normativa.BD::INT.FilialesP40";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::INT.ConsultarFilialesP40" TO BCS_DESARROLLADOR WITH GRANT OPTION;
