SELECT "IdFormatoArchivo", 
"CodigoFormatoArchivo", 
"SeparadorCampos", 
"SeparadorDecimales",
"RellenarCampo",
"CodigoFormatoArchivo",
"IdBanco",
"NombreBanco",
"IdMoneda",
"IdMedioDePago",
"FormatoSalida",
"Servicio","Descripcion",
"RutaTesoreria",
"RutaCompleta",
"NombreArchivoSalida",
"CantidadDeTiposDeRegistro" 
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo" 
WHERE "IdFormatoArchivo" = :IdFormatoArchivo;