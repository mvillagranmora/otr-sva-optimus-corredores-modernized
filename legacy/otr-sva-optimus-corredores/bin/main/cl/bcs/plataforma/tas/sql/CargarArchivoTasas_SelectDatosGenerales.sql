SELECT
        CAST(TAS."IdPrecioTasa" AS INTEGER) AS "IdPrecioTasa",
        TAS."IdInstrumento",
        TAS."Nemotecnico",
        TAS."IdClasificacionTipoInstrumento",
        TAS."IdTipoInstrumento",
        TAS."Tasa",
        DGA."Tasa" AS "TasaMod",
        TAS."TasaEstimada",
        DGA."TasaEstimada" AS "TasaEstimadaMod",
        TAS."Plazo",
        DGA."Plazo" AS "PlazoMod",
        TAS."IdEstado",
        TAS."IdMoneda"
    FROM "BSCL.Plataforma.BD::GBL.CargarArchivoTasas_Entrada_CargarArchivoTasas" DGA
    INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" TAS
        ON (DGA."Nemo" = TAS."Nemotecnico" AND 
            (TAS."FechaSolicitud" BETWEEN :FechaDesde AND :FechaHasta) AND
            SUBSTR_BEFORE(TAS."NombreArchivo", '.') = SUBSTR_BEFORE(:p_Archivo, '.') AND
            TAS."IdEmpresa" = :p_IdEmpresa)
    INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
        ON (TAS."IdFuente" = FU."IdFuente" AND 
            FU."Descripcion" = :p_Fuente AND 
            FU."IdEmpresa" = :p_IdEmpresa)
    WHERE TAS."Borrado" = 'N';