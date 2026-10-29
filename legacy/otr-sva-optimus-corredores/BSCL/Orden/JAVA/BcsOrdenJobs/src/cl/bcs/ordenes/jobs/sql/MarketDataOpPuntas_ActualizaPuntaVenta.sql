/********************************************************** 
Proposito	: Script para actualizar registro de puntas
autor		: projo
fecha		: 13/03/2020
***********************************************************/
UPSERT "BSCL.Plataforma.BD::INS.BolsaInstrumentoPuntas"
("IdInstrumento","IdBolsa","IdCondicionDeLiquidacion","Fecha","FechaActualizacion","PuntaVenta","CantidadPuntaVenta")
SELECT :IdInstrumento,:IdBolsa,:IdCondicionDeLiquidacion,:Fecha,CURRENT_TIMESTAMP,:PuntaVenta,:CantidadPuntaVenta FROM DUMMY;


