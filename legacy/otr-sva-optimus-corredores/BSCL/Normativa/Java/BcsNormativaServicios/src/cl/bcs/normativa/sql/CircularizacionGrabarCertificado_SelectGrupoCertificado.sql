SELECT COUNT(1) as "Cert"
FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina"  AS NOM
LEFT JOIN "BSCL.Normativa.BD::CER.CertificadoCliente" c  ON (c."IdTipoDocumento"='CIRCCarta' AND  NOM."IdCliente"=c."IdCliente" AND NOM."IdPortafolio"=c."IdPortafolio" AND NOM."Periodo"=c."Periodo")
WHERE  NOM."Notificar"='SI' AND NOM."Periodo"=REPLACE (LEFT(:p_FechaConsulta,7),'-','') AND c."IdCliente" IS NULL;