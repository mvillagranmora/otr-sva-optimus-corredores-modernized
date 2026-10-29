delete 
from "BSCL.Orden.BD::ORD.ControlMercado" c
where c."FechaIngresoRegistro" < ADD_MONTHS(CURRENT_UTCTIMESTAMP,-1) and "TipoMensaje" = 'MARKET_DATA_PALO_RV_MME'
