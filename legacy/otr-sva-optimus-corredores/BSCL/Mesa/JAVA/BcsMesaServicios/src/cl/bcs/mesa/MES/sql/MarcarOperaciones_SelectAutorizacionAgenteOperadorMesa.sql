SELECT AOM."ModificaOperacionOtroUsuario"
FROM "BSCL.Mesa.BD.Vistas::ConsultarAgenteOperadorMesa" AOM
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU
    ON USU."IdUsuario" = :IdUsuario AND USU."TipoEntidad" = 'EMP'
WHERE AOM."IdAgenteOperador" = USU."IdEntidad"
  AND AOM."IdEmpresa" = :IdEmpresa
  AND AOM."IdEstado" = 'ACT'
  AND AOM."IdMesa" IN (:IdMesaOrigen, 0)
ORDER BY CASE WHEN AOM."IdMesa" = :IdMesaOrigen THEN 0 ELSE 1 END
LIMIT 1;