UPDATE  ND
SET  
    ND."Despachado" = 'S'
FROM "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND
WHERE ND."IdNotificacion" = :IdEnvio;