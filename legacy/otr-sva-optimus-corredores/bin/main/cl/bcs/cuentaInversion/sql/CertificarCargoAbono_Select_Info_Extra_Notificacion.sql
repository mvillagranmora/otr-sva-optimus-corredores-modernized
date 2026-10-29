SELECT CE."IdPersona" 
       ,CE."IdDireccionElectronica"
       ,CORR."RazonSocial" AS "RazonCorredora"
       ,CORR."Email" AS "DireccionCorredora"
       ,CORR."RUT" AS "RutCorredora"
       ,CORR."Telefono" AS "TelefonoCorredora"
FROM "BSCL.Plataforma.BD::ACC.Usuario" U
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  CE ON CE."IdEmpleado" = U."IdEntidad" AND "IdUsuario" = :IdUsuario
CROSS JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CORR WHERE CORR."IdEmpresa" = :IdEmpresa;
