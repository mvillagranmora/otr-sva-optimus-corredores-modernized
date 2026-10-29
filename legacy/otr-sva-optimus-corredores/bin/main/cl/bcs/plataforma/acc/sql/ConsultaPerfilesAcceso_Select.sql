SELECT 
CP."IdEmpresa",
CP."IdPerfil",
CP."Nombre" AS "NombrePerfil",
CP."IdEstado",
CP."EstadoPerfil",
CAP."IdAcceso",
CAP."NombreAcceso",
CAP."TipoAcceso",
CAP."Orden" AS "IdMenu",
CAP2."NombreAcceso" AS "NombreMenu"
FROM   "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfiles" AS CP 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfil" AS CAP ON CP."IdPerfil"=CAP."IdPerfil"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfil" AS CAP2 ON CAP."Orden"=CAP2."IdAcceso" AND CAP2."TipoAcceso" = 'MENU1' AND CP."IdPerfil"=CAP2."IdPerfil"
WHERE CP."IdEmpresa"= :IdEmpresa
ORDER BY CP."IdPerfil" DESC  
