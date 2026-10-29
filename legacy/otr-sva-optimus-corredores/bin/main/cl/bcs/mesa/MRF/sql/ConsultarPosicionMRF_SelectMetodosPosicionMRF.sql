SELECT 
	"IdMetodo", 
	"NombreMetodo", 
	"Glosa", 
	"IdTipoReajuste", 
	"IdMesa", 
	"IdEstado", 
	"IdTipoPosicion"
FROM "BSCL.Mesa.BD::MES.MetodoPosicion"
WHERE "IdEstado" = 'VIG' AND "IdMesa" = :p_IdMesa