/********************************************************** 
Proposito	: Script para actualizar registro de precios
autor		: RU
fecha		: 01/02/2017
***********************************************************/
UPSERT "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio"("IdInstrumento","IdBolsa","Fecha","FechaActualizacionMD","PrecioMayor","PrecioMenor","PrecioMedio","PrecioApertura","PrecioCierre","UnidadesTransadas","MontoTransado","Variacion")
VALUES(:IdInstrumento,:IdBolsa,:Fecha,CURRENT_UTCTIMESTAMP,:PrecioMayor,:PrecioMenor,:PrecioMedio,:PrecioApertura,:PrecioCierre,:UnidadesTransadas,:MontoTransado,:Variacion) WITH PRIMARY KEY;


