/********************************************************** 
Proposito	: Script para actualizar registro de precios
autor		: projo
fecha		: 12/03/2020
***********************************************************/
UPSERT "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio"("IdInstrumento","IdBolsa","Fecha","FechaActualizacionMD","PrecioMayor","PrecioMenor","PrecioMedio","PrecioApertura","PrecioCierre","UnidadesTransadas","MontoTransado","Variacion")
SELECT :IdInstrumento,:IdBolsa,:Fecha,CURRENT_UTCTIMESTAMP,:PrecioMayor,:PrecioMenor,:PrecioMedio,:PrecioApertura,:PrecioCierre,:UnidadesTransadas,:MontoTransado,:Variacion FROM DUMMY;


