
INSERT INTO "BSCL.Plataforma.BD::EVT.EventoArchivoAdjunto"
(
	"IdEventoArchivoAdjunto",
    "IdEvento", 
    "Archivo",
    "NombreArchivo",
    "Contrasenia"	 
)VALUES(
	:IdEventoArchivoAdjunto,
    :IdEvento, 
    :Archivo,
    :NombreArchivo,
    :Contrasenia	
);