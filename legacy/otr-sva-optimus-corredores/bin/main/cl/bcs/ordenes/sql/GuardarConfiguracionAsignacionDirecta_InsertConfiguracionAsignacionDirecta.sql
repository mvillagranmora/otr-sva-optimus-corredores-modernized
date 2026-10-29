INSERT INTO "BSCL.Orden.BD::ORD.ConfiguracionAsignacionDirecta" (
    "IdConfiguracionAsignacionDirecta",
    "AsignacionAuto",
    "MovimientoFacturarAuto",
    "ComprobanteAuto",
    "IdEstado",
    "IdTipoInstrumento",
    "IdEmpresa",
    "IdUsuarioModifica"
)
VALUES (
    "BSCL.Orden.BD.Secuencias::IdConfiguracionAsignacionDirecta".NEXTVAL,
    :AsignacionAuto,
    :MovimientoFacturarAuto,
    :ComprobanteAuto,
    'ACT',
    :IdTipoInstrumento,
    :p_IdEmpresa,
    :p_IdUsuario
);

