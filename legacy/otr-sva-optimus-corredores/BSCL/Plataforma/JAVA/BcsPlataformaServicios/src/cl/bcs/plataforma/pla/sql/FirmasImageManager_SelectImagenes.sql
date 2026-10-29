SELECT 
	"IdEmpresa",	
	"IdImagen",    
	"NombreImagen",
	"IdDocumentoIdentidad",
	"IdPersona",
    "Tipo",
    "Extension",
    "Version",
    "Binario"
From "BSCL.SocioNegocio.BD::PER.ImagenesFirmas" 
WHERE "IdPersona" = :IdPersona 
AND "IdDocumentoIdentidad"=:IdDocumentoIdentidad