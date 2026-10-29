INSERT INTO "BSCL.Plataforma.BD::EVT.ArchivoAdjunto"(
    "IdArchivoAdjunto",
    "IdEmail",
    "Nombre",
    "Archivo"
)VALUES(
    "BSCL.Plataforma.BD.Secuencias::EVT.ArchivoAdjunto".nextval,
    :IdEnvio,
    :Nombre,
    :Archivo
);
