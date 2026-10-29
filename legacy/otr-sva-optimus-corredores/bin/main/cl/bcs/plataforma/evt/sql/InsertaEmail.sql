INSERT INTO "BSCL.Plataforma.BD::EVT.Mail"
(
    "IdEmail",
    "FechaCreacion",
    "Remitente",
    "Asunto",
    "MensajeCuerpo",
    "IdEnvio"
)
VALUES (
    :IdEmail,   
    CURRENT_UTCTIMESTAMP,
    :Remitente,
    :Asunto,
    :MensajeCuerpo,
    :IdEnvio
);
