INSERT INTO "BSCL.Plataforma.BD::IND.Indicador"(
    "IdIndicador",
    "IdEntidad",
    "IdTipoEntidad",
    "IdTipoIndicador",
    "ModeloCliente",
    "IdEmpresa",
    "ValorIndicador",
    "IdEstadoAutorizacion",
    "FechaVencimiento",
    "IdCarga"
)
VALUES (
    :IdIndicador,
    :IdEntidad,
    :IdTipoEntidad,
    :IdTipoIndicador,
    '',
    :IdEmpresa,
    :ValorIndicador,
    'ING',
    '0001-01-01 00:00:00',
    :IdCarga
) ;
