UPSERT "BSCL.Normativa.BD::INT.FilialesP40"
("IdSocioDeNegocio","Rut","NombreFilial","IdEmpresa","CodigoTenedor","NombreZip","NombreArchivo") 
VALUES(:IdSocioDeNegocio,:Rut,:NombreFilial,:IdEmpresa,:CodigoTenedor,:NombreZip,:NombreArchivo) 
WHERE "IdSocioDeNegocio" = :IdSocioDeNegocio AND "IdEmpresa" = :IdEmpresa;