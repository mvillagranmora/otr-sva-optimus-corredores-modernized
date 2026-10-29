/********************************************************** 
Proposito	: Script para actualizar registro de puntas
autor		: projo
fecha		: 13/03/2020
***********************************************************/
UPSERT "BSCL.Plataforma.BD::INS.BolsaInstrumentoPuntas"
("IdInstrumento","IdBolsa","IdCondicionDeLiquidacion","Fecha","FechaActualizacion","PuntaCompra","CantidadPuntaCompra")
SELECT :IdInstrumento,:IdBolsa,:IdCondicionDeLiquidacion,:Fecha,CURRENT_TIMESTAMP,:PuntaCompra,:CantidadPuntaCompra FROM DUMMY;


