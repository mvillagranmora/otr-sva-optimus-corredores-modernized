-- Verificar si existe evento NAUTOR para el autorizado creado
SELECT COUNT(*) AS "CantidadEventos"
FROM "BSCL.Plataforma.BD::AUT.Evento"
WHERE "IdTipoEvento" = 'NAUTOR'
  AND "NumeroOperacionConcepto" = :IdAutorizado
  AND "IdEmpresa" = :IdEmpresa
