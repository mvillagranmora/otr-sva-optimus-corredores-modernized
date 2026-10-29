UPDATE "BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero"
SET "Periodo"			= COALESCE(:p_Periodo,"Periodo"), 
	"Nombre"			= COALESCE(:p_Nombre, "Nombre"),
	"Descripcion"		= COALESCE(:p_Descripcion,"Descripcion"), 
	"IdMonedaParametro" = COALESCE(:p_IdMonedaParametro,"IdMonedaParametro"),
	"IdMonedaValor"		= COALESCE(:p_IdMonedaValor,"IdMonedaValor")
WHERE "IdIndiceFinanciero"=:p_IdIndiceFinanciero;
