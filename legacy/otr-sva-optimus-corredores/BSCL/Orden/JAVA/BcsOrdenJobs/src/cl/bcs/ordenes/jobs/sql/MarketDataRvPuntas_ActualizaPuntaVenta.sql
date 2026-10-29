/********************************************************** 
Proposito	: Script para actualizar registro de puntas
autor		: RU
fecha		: 01/02/2017
***********************************************************/
UPSERT "BSCL.Plataforma.BD::INS.BolsaInstrumentoPuntas"
("IdInstrumento","IdBolsa","IdCondicionDeLiquidacion","Fecha","FechaActualizacion","PuntaVenta","CantidadPuntaVenta")
VALUES(:IdInstrumento,:IdBolsa,:IdCondicionDeLiquidacion,:Fecha,CURRENT_TIMESTAMP,:PuntaVenta,:CantidadPuntaVenta) WITH PRIMARY KEY;


