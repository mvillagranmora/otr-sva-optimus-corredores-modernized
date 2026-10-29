SELECT DISTINCT 
CR."IdEmpresa",
CR."Nombre" AS "NombreRol",
CR."IdRol",
CR."EstadoRol",
CRP."NombrePerfil",
CRP."IdPerfil",
CAP."NombreAcceso",
CAP."TipoAcceso",
--CAP."IdElementoHtml" AS "Modulo",
ACP."NombreAcceso" AS "Modulo",
COALESCE(CAP2."NombreAcceso",'') AS "PrimerNivel",
COALESCE(CAP3."NombreAcceso",'') AS "SegundoNivel",
COALESCE(CAP4."NombreAcceso",'') AS "TercerNivel",
CRP."IdRol" AS "IdRol1",
CAP."Padre" AS "Padre1",
CAP2."Padre" AS "Padre2",
CAP3."Padre" AS "Padre3",
CAP4."Padre" AS "Padre4"
FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRol" AS CR
LEFT JOIN  "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfiles" AS CRP ON CR."IdRol" = CRP."IdRol"
LEFT JOIN  "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfil" AS CAP ON CAP."IdPerfil"=CRP."IdPerfil" AND CAP."Existe" = 'S'
LEFT JOIN  "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfil" AS CAP2 ON CAP2."Padre"=CAP."IdAcceso" AND CAP2."TipoAcceso" = 'BTN' AND CAP2."Existe" = 'S'
LEFT JOIN  "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfil" AS CAP3 ON CAP3."Padre"=CAP2."IdAcceso" AND CAP3."TipoAcceso" = 'BTN' AND CAP3."Existe" = 'S'
LEFT JOIN  "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfil" AS CAP4 ON CAP4."Padre"=CAP3."IdAcceso" AND CAP3."TipoAcceso" = 'BTN' AND CAP4."Existe" = 'S'
LEFT JOIN  "BSCL.Plataforma.BD::ACC.Acceso" AS ACP ON CAP."Padre" = ACP."IdAcceso"
WHERE  CAP."IdEmpresaPerfil" = 1 AND CAP."IdEmpresaAcceso"= :IdEmpresa AND CRP."IdEstadoPerfil"= 'ACT' AND CAP."TipoAcceso" = 'MENU2'
ORDER BY CRP."IdRol", CRP."IdPerfil", CAP."Padre",CAP2."Padre",CAP3."Padre",CAP4."Padre" DESC