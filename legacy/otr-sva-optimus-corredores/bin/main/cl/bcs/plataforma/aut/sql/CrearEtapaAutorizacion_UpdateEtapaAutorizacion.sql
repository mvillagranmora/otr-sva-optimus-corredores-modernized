UPDATE "BSCL.Plataforma.BD::AUT.Etapa"
SET "Codigo" = TRIM(:Codigo),                                                                   
	"Nombre" = TRIM(:Nombre),
	"Orden" = :Orden,                                                                   
	"TipoAutorizacion" = :TipoAutorizacion , 
	"MinimoAutorizan" = :MinimoAutorizan, 
	"IdEstado" = :IdEstado
WHERE "IdEtapa" = :IdEtapa
AND "IdEmpresa" = :IdEmpresa;