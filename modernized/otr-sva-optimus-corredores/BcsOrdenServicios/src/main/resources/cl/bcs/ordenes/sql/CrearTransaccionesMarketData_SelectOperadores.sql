SELECT "CodigoOperadorAsociado" || "Fondo" AS "KeyFondo", "CodigoOperador" FROM "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" x WHERE "IdEmpresa" = :IdEmpresa
AND "Fondo" != ''