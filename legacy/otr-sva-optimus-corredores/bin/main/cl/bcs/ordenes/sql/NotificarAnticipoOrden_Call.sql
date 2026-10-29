/**************************************************************
Proposito	: CALL notificacion anticipo orden
autor		: CH
fecha		: 14-05-2018
***********************************************************/
CALL "BSCL.Orden.BD.Servicios::NotificarAnticipoOrden"(
    :p_IdEmpresa, 
    :p_IdUsuario, 
    :p_IdModulo, 
    :p_anticipos
);