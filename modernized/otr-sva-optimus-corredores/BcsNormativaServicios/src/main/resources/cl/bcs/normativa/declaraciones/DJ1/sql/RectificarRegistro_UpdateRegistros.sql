UPDATE DJ
SET DJ."Rectifica"=:ValorNuevo
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" DJ
WHERE DJ."KIdContrato"=:KIdContrato;