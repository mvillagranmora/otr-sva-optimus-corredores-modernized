SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato"  
AS
SELECT 
   ARC."IdArchivo",
   ARC."IdEmpresa",
   ARC."CodigoArchivo",
   ARC."TieneSeparador",
   ARC."SeparadorColumnas",
   ARC."TieneCabecera",
   FRM."IdFormato",
   FRM."CodigoCampo",
   COALESCE(DIC."Descripcion",FRM."CodigoCampo") AS "CodigoCampoTra",
   FRM."TipoDato",
   FRM."Posicion",
   FRM."LargoCampo",
   FRM."EsObligatorio",
   FRM."IncluyeDecimales",
   FRM."CantidadDecimales",
   FRM."Validacion",
   FRM."MensajeValidacion",
   FRM."Ejemplo",
   ARCFRM."IdArchivoFormato",
   FRM."ForzarMayuscula",
   ARC."IdModulo",
   ARC."CodigoArchivoDetalle",
   ARC."CodigoArchivoPie",
   COALESCE(DIC."Abreviado",FRM."CodigoCampo") AS "Abreviado"
FROM "BSCL.Plataforma.BD::PLA.Archivo" ARC
INNER JOIN "BSCL.Plataforma.BD::PLA.ArchivoFormato" ARCFRM ON ARC."IdArchivo" = ARCFRM."IdArchivo"
INNER JOIN "BSCL.Plataforma.BD::PLA.Formato" FRM ON FRM."IdFormato" = ARCFRM."IdFormato"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Diccionario" DIC ON DIC."CodDiccionario" = FRM."CodigoCampo"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CFG ON DIC."CodLenguaje"=CFG."Valor" AND CFG."CodigoParametro"='PLA.IdiomaSistema' AND CFG."IdEmpresa"=ARC."IdEmpresa"
ORDER BY FRM."Posicion";
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato" TO  BCS_DESARROLLADOR WITH GRANT OPTION;