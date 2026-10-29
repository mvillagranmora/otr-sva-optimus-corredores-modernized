/********************************************************** 
Proposito   : Script para obtener las carteras en reserva
autor       : projo
fecha       : 04/07/2017
***********************************************************/

SELECT "IdOperacion","IdReserva"
FROM "BSCL.Custodia.BD::CUS.Reserva"
WHERE "IdOperacion" IN (:IdOperacion)