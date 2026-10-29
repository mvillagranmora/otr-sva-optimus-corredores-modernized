INSERT INTO "BSCL.Plataforma.BD::DOC.PlantillaContratoDocumento" 
    ( "IdPlantillaContratoDocumento"
    , "PlantillaContrato"
    , "CodigoArchivo" 
    , "Estado"
    , "FechaVigencia"
    , "FechaVencimiento"
    , "IdTipoDocumento" )
VALUES
    ( :IdPlantillaContratoDocumento
    , :PlantillaContrato
    , :CodigoArchivo
    , :Estado
    , :FechaVigencia
    , :FechaVencimiento
    , :IdTipoDocumento )