SELECT 	OPE."IdOperacion"
		,OPE."IdEstado", TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion"
		,OPE."IdCliente"
		,OPE."IdPortafolio"
		,OPE."IdTipoOperacion"
		,OPE."PlazoEnDias"
		,OPE."IdUsuarioCreacion"
		,OPE."MontoOperacionInicial" 
FROM 	"BSCL.Mesa.BD::MES.Operacion" OPE
INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" TMP  ON (TMP."IdOperacion" = OPE."IdOperacion");

