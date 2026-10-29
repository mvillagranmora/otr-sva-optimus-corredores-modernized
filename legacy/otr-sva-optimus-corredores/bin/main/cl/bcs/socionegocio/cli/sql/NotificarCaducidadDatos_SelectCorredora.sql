SELECT U."IdUsuario"      AS "IdUsuario"
     , CCDG."RazonSocial" AS "RazonCorredora"
     , CCDG."Telefono"    AS "TelefonoCorredora"
FROM "BSCL.Plataforma.BD::ACC.Usuario" U
     CROSS JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG
WHERE CCDG."IdEmpresa" = :IdEmpresa
  AND U."CodigoUsuario" = :CodigoUsuario;