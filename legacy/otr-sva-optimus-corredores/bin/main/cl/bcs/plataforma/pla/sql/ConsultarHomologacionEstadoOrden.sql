/* 
Permite consultar el estado equivalente de las ordenes en OpenWorkPlace
*/
SELECT "IdEstadoOWP" FROM "BSCL.Plataforma.BD::PLA.HomologacionEstadoOptimusOWP" 
WHERE "IdEstadoOptimus" = :p_Estado
AND "IdClasificacionTipoInstrumento" = :p_Mercado ;