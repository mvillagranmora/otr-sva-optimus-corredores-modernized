INSERT INTO "BSCL.Normativa.BD::CIR.ArchivoNCG365"(
	"IdArchivoNCG365",
    "IdCliente", 
    "NombreArchivo",
    "UbicacionZip",
    "FechaCertificacion",
    "Archivo"
)VALUES(
    "BSCL.Normativa.BD.Secuencias::IdArchivoNCG365".nextval,
    :IdCliente,
    :NombreArchivo,
    :UbicacionZip,
    :FechaCertificacion,
    :Archivo
);