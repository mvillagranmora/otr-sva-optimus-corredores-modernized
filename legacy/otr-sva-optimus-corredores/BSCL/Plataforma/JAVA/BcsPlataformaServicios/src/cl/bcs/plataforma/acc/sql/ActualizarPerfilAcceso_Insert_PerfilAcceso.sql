INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.PerfilAcceso"
    ("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado", "AsignoUsuario", "FechaAsignacion")
VALUES 
    (:IdAccesoPerfil, :IdEmpresa, :IdPerfil, :IdAcceso, :IdEstado, :AsignoUsuario, CURRENT_UTCTIMESTAMP)