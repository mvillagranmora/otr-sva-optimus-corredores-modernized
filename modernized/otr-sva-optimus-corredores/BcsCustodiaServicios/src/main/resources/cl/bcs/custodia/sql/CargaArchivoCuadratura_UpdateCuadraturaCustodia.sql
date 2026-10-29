UPDATE "BSCL.Custodia.BD::CUS.CuadraturaCustodia"
SET
	"PosicionBoveda" = :PosicionBoveda,
	"PosicionBloqueada"	= :PosicionBloqueada,
	"PosicionTotal" = :PosicionTotal,
	"PosicionReventa" = :PosicionReventa,
	"PosicionRecompra" = :PosicionRecompra,
	"PosicionSimultanea" = :PosicionSimultanea,
	"Diferencia" = :Diferencia
WHERE "IdCuadraturaCustodia" = :IdCuadraturaCustodia