SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarOperacion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarOperacion"  
AS
SELECT DISTINCT TT."IdTipoOperacionTipoInstrumento" 
, TT."IdTipoOperacion"
, TT."IdTipoInstrumento" 
, TT."IdInstrumento" 
, TT."Nemotecnico" 
, TT."IdEstado" 
, TIOP."IdTipoOperacionRelacionada"
, TIOP."Nombre" 
, TIOP."Codigo" 
, TIOP."Accion" 
, TIOP."Accion" AS "CodigoAccion"
, TIOP."AccionOperacion"
, TIOP."AccionOperacion" as "AccionOperacionText" 
, TIOP."IdTipoProducto" 
, TIOP."Comprobante" 
, TIOP."EsOrden" 
, TIOP."EsVentaCorta" 
, TIOP."EsTransaccion"
, TIOP."AccionOrden"
, TIPOINS."IdClasificacionTipoInstrumento"
FROM "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS TT
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  as TIOP ON TT."IdTipoOperacion" = TIOP."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TIPOINS on TIPOINS."IdTipoInstrumento" = TT."IdTipoInstrumento"
Where TIOP."Accion" <> '';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarOperacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
