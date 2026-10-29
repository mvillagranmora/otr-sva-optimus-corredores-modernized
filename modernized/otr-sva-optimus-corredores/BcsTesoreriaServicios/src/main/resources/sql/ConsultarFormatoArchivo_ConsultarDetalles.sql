SELECT "IdFormatoArchivo",
"IdFormatoArchivoDetalle",
"CodigoFormatoArchivo",
"TipoRegistro", 
"Campo", 
"TipoDato", 
"Largo", 
"Decimales", 
"ValorDefecto", 
"Orden", 
"DescripcionFormatoDetalle",
"Formato" 
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivoDetalle" 
WHERE "IdFormatoArchivo" = :IdFormatoArchivo