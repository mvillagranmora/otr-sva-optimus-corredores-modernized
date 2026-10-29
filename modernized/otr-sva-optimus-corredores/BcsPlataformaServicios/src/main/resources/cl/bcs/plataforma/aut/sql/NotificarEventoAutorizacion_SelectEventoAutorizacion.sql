SELECT 
"Autorizacion",
"IdEvento", 
"Glosa", 
TO_CHAR(ADD_SECONDS(TO_SECONDDATE(TO_CHAR(:p_Fecha,'DD-MM-YYYY')||' '||TO_TIME(:p_Hora,'HH24:MI:SS'),'DD-MM-YYYY HH:MI:SS'),TE."HorasVigencia"*3600),'DD-MM-YYYY HH24:MI:SS') AS "HoraVigencia", 
"Usuario", 
TO_CHAR(:p_Fecha,'DD-MM-YYYY')||' a las '||TO_TIME(:p_Hora,'HH24:MI:SS')||' hrs.' as "FechaHora",
CE."IdEstado" AS "Estado",
CE."IdTipoEvento" AS "TipoEvento",
"IdEtapaEvento" AS "IdEtapa", 
"IdPersona",
"Telefono" AS "AtencionCliente",
"RazonSocial" AS "Corredora"
FROM
"BSCL.Plataforma.BD.Vistas::AUT.ConsultarEvento" CE
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" U ON U."IdUsuario" =CE."IdUsuario" AND U."IdUsuario"=:p_IdUsuario
INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" E ON E."IdEmpleado" = U."IdEntidad" 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :p_IdEmpresa
LEFT JOIN "BSCL.Plataforma.BD::AUT.TipoEvento" TE ON TE."IdTipoEvento" = CE."IdTipoEvento" AND TE."IdEstado"= 'VIG' AND CCDG ."IdEmpresa"=TE."IdEmpresa" 
WHERE CE."IdEvento" IN (:p_IdEventoAutorizacion)