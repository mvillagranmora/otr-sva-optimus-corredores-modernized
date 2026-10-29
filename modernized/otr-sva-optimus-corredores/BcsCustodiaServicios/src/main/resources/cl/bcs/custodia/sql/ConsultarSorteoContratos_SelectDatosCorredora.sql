SELECT 
	"Emp"."CodigoCorredora",
	"Emp"."IdEmpresa",
	"Emp"."Codigo",
	"Emp"."Nombre",
	"Emp"."IdPais",
	"Emp"."IdSocioDeNegocio",
	"Emp"."IdCorredora",
	"Emp"."IdPersona"
FROM  "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" "Emp"
WHERE "Emp"."IdBolsa" = :IdBolsa;