SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" 
AS
SELECT 
TIPOOPE."IdTipoOperacion" , 
TIPOOPE."Codigo" , 
TIPOOPE."Nombre" , 
TIPOOPE."Accion" , 
TIPOOPE."IdTipoProducto",
COALESCE(PRODUCTO."Nombre",'-') AS "NombreTipoProducto", 
TIPOOPE."RecibePagaPortafolio", 
TIPOOPE."EsSimultanea" , 
TIPOOPE."IdTipoOperacionRelacionada" , 
TIPOOPE."Comprobante" , 
TIPOOPE."EsAnticipo",
TIPOOPE."EsAnulacion",
TIPOOPE."EsOtrosConceptos",
"TipoOperacionTipoInstrumento_$1"."IdTipoInstrumento" , 
"TipoInstrumento_$2"."Nombre" as "NombreTipoInstrumento" ,
"TipoOperacionTipoInstrumento_$1"."IdInstrumento" ,
"TipoOperacionTipoInstrumento_$1"."IdEstado" AS "EstadoTipoOperacionTipoInstrumento" , 
"ClasificacionTipoInstrumento_$3"."IdClasificacionTipoInstrumento" , 
"AccionCustodia",
"AccionOrden",
"EsVentaCorta",
"EsTransaccion",
"EsOrden",
"EsMovimientoComprobante",
"GeneraOperacionPlazo",
TIPOOPE."IdTipoOperacionAnulacion",
TIPOOPE."PermiteCobros",
TIPNEG."OrdenSecuencia" ,
TIPNEG."Estado" AS "EstadoTipoNegocios"
FROM "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPE
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" PRODUCTO ON PRODUCTO."IdTipoProducto" = TIPOOPE."IdTipoProducto"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS "TipoOperacionTipoInstrumento_$1" ON  "TipoOperacionTipoInstrumento_$1"."IdTipoOperacion" = TIPOOPE."IdTipoOperacion" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_$2" ON "TipoInstrumento_$2"."IdTipoInstrumento" = "TipoOperacionTipoInstrumento_$1"."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_$3" ON "ClasificacionTipoInstrumento_$3"."IdClasificacionTipoInstrumento" = "TipoInstrumento_$2"."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProductoNegocios" AS TIPNEG ON TIPNEG."IdTipoInstrumento" = "TipoOperacionTipoInstrumento_$1"."IdTipoInstrumento";
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

GO