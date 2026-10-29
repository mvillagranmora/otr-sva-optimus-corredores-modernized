SELECT CCDG."RazonSocial" AS "RazonCorredora"
     , CCDG."Telefono"    AS "TelefonoCorredora"
FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG
WHERE CCDG."IdEmpresa" = :IdEmpresa;