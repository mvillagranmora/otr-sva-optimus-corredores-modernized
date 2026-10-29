Select "IdMesa","PorDefecto"
from "BSCL.Mesa.BD.Vistas::ConsultarMesas" mesa
where   mesa."IdEmpresa" = :idEmpresa
	and mesa."IdTipoProducto" = 'MERF'
	AND mesa."IdEstadoRegistro" = 'ACT'
	ORDER BY "IdMesa" ASC;