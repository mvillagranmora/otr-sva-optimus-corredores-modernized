SELECT  "IdCliente","DocumentoIdentidad" ,"NombreCompleto", "IdSocioDeNegocio",COALESCE("IdSucursalEjecutivo",0) AS "IdSucursalEjecutivo","CodigoEmpleadoEjecutivo","NombreEmpleadoEjecutivo"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
WHERE "IdCliente" IN (:p_IdCliente);