SELECT 
"IdEmpresa" ,
"IdTipoDocumento",
"NombreDocumento" ,	      
"CodigoArchivo" AS "CodPlantilla", 
"NumeroFirmantesEmpresa",
"NumeroFirmantesCliente",
"RequiereFirmaCliente",
"FirmaAutomaticaDocumento",
"CodigoTipoIndicador"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" 
WHERE "IdEmpresa"= :IdEmpresa
AND "FirmaAutomaticaDocumento" = 'S' AND "IdTipoPersona" IN (:IdTipoPersona,'A');

