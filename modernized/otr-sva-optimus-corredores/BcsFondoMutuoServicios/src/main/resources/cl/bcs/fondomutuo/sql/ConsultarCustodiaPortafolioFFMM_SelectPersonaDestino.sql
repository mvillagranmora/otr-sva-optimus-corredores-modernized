SELECT distinct PER."IdPersonaDestino"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" AS PER
WHERE PER."IdPersonaOrigen" = :IdPersonaCuentaPropia
AND PER."IdEstado" = 'ACT';	