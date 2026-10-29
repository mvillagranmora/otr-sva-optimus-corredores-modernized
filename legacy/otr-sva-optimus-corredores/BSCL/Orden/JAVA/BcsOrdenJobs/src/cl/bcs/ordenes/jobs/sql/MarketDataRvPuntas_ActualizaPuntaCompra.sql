/********************************************************** 
Proposito	: Script para actualizar registro de puntas
autor		: RU
fecha		: 01/02/2017
***********************************************************/
UPSERT "BSCL.Plataforma.BD::INS.BolsaInstrumentoPuntas"
("IdInstrumento","IdBolsa","IdCondicionDeLiquidacion","Fecha","FechaActualizacion","PuntaCompra","CantidadPuntaCompra")
VALUES(:IdInstrumento,:IdBolsa,:IdCondicionDeLiquidacion,:Fecha,CURRENT_TIMESTAMP,:PuntaCompra,:CantidadPuntaCompra) WITH PRIMARY KEY;


