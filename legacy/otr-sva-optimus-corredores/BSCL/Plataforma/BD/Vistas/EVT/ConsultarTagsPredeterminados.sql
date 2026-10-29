SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTagsPredeterminados"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTagsPredeterminados"  
AS
SELECT 0 as "IdTipoEventoDetalle","Llave","ValorDefault",'TagGenerales' AS "CodigoTipoEvento",0 as "IdPlantilla",'TagGenerales' as "property"
FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTagsGenerales"
UNION ALL
select 
"IdTipoEventoDetalle",
"Llave",
"ValorDefault",
tipoevtdet."CodigoTipoEvento",
1 as "IdPlantilla",
tipoevtdet."CodigoTipoEvento"as "property"
 from "BSCL.Plataforma.BD::EVT.TipoEventoDetalle" tipoevtdet;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTagsPredeterminados" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 
 