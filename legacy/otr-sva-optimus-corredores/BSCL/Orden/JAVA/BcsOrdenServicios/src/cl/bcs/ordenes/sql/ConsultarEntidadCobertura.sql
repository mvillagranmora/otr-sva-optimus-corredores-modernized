SELECT ENTIDAD."IdEntidadDestinoGarantia" AS "Valor"
FROM "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" ENTIDAD 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" AS CONTR ON CONTR."IdContraparte" = ENTIDAD."IdContraparte"
WHERE "IdEntidad" = :p_IdEntidad;