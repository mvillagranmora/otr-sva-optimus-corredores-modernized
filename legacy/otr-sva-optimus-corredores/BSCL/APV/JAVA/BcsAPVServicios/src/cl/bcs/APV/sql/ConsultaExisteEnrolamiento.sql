SELECT count("DocumentoIdentidad")
FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarEnrolamiento"
WHERE "DocumentoIdentidad"= :ExistePersona;