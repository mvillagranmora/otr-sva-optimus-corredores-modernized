INSERT INTO "BSCL.Plataforma.BD::EVT.Destinatario"(
    "IdDestinatario",
    "IdEmail",
    "TipoDestinatario",
    "Destinatario"
)
VALUES(
    "BSCL.Plataforma.BD.Secuencias::EVT.Destinatarios".nextval,
    :IdEnvio,
    :TipoDestinatario,
    :Destinatario
);
