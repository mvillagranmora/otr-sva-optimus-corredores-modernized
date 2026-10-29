SELECT 
	"Codigo" AS "Codigo",
	"Descripcion" AS "Descripcion" 
		FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarArea"	
			WHERE "Codigo" IN (:Codigo)