SELECT 
"IdEmpresa" ,
"IdTipoDocumento",
"NombreDocumento" ,          
"CodigoArchivo" AS "CodPlantilla", 
"NumeroFirmantesEmpresa",
"NumeroFirmantesCliente",
"RequiereFirmaCliente"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" 
WHERE "IdEmpresa"= :IdEmpresa
AND "IdTipoDocumento" IN (:TipoDocumento);
