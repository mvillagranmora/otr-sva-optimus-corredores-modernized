INSERT INTO
    "BSCL.Mesa.BD::SPT.Puntas" (
        "IdPuntas",
        "IdEmpresa",
        "Fecha",
        "IdUsuario",
        "AperturaPrecio",
        "PuntaCompra",
        "PuntaVenta",
        "IdMonedaPunta",
        "Estado",
        "CheckPrecioPorTramo",
        "Origen"
    )
VALUES
(
        "BSCL.Mesa.BD.Secuencias::SPT.IdPuntas".NEXTVAL,
        :IdEmpresa,
        :Fecha,
        :IdUsuario,
        :AperturaPrecio,
        :PuntaCompra,
        :PuntaVenta,
        :IdMonedaPunta,
        :Estado,
        :CheckPrecioPorTramo,
        :Origen
    );