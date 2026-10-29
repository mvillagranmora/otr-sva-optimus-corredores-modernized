INSERT INTO
    "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" (
        "IdPreferenciaPagoPortafolio",
        "IdPortafolio",
        "IdPreferenciaPago",
        "IdCuentaBancaria",
        "IdTarjetaBancaria",
        "FechaCreacion",
        "FechaModificacion",
        "IdUsuarioCreador",
        "IdEstado",
        "IdEmpresa",
        "FechaIngreso",
        "IdEstadoAutorizacion",
        "PorDefecto"
    )
SELECT
    "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio".NEXTVAL,
    :IdPortafolio,
    PRE."IdPreferenciaPago",
    :IdCuentaBanco,
    0,
    CURRENT_UTCTIMESTAMP,
    CURRENT_UTCTIMESTAMP,
    :IdUsuario,
    'ACT',
    :IdEmpresa,
    :FechaIngreso,
    'AUT',
    'S'
FROM
    "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" PRE
WHERE
    PRE."IdMedioDePago" = 'CIN'
    AND PRE."IdMoneda" = :MonedaSistema
    AND "IdTipoOperacion" IN ('COMTER', 'VENTER');