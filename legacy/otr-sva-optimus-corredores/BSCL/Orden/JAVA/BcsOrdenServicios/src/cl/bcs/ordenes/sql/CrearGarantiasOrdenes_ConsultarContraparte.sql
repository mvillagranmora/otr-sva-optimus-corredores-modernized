SELECT ENTIDAD."IdContraparte" AS "IdContraparteGarantia",CONTR."NombreCompleto" AS "RazonSocialContraparteGarantia"
FROM "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" ENTIDAD 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" AS CONTR ON CONTR."IdContraparte" = ENTIDAD."IdContraparte"
WHERE "IdEntidad" = 'BCS';--Error hay mas Y TOMA LA PRIMERA EN EL JAVA  .-- se comenta por que debe haber solo una entidad destino garantia


