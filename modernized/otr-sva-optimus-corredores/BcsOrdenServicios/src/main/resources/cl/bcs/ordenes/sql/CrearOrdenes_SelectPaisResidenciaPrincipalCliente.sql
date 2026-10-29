SELECT PER."IdPersona", PAIS."EsPaisUs", PAIS."EsOcde" FROM "BSCL.Plataforma.BD::REG.Pais" PAIS
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPaisResidencia" = PAIS."IdPais"
WHERE PER."IdPersona" IN (:IdPersonas) 
AND "IdPais" <> :IdPaisSistema;