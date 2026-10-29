UPDATE 
      "BSCL.Plataforma.BD::DOC.PlantillaContratoDocumento"
SET 
      "Estado" = :Estado 
    , "FechaVencimiento" = :FechaVencimiento

WHERE
      "IdTipoDocumento" = :IdTipoDocumento
AND   "CodigoArchivo" = :CodigoArchivo;