INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion" (
		"IdCondicion",
		"IdDefinicionAsiento",
		"IdTipoContabilidad",
		"IdTipoDatoEvento",
		"ValorVarchar",
		"ValorDecimal",
		"Condicion",
		"Conector",
		"TipoCondicion",
		"Estado")
 VALUES (
	"BSCL.Contabilidad.BD.Secuencias::IdTipoAsientoCondicion".NEXTVAL,
	:IdDefinicionAsiento,
	:IdTipoContabilidad,
	:IdTipoDatoEvento,
	:ValorVarchar,
	:ValorDecimal,
	:Condicion,
	:Conector,
	:TipoCondicion,
	:Estado);