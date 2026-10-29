/********************************************************** 
Proposito   : Script para obtener las Forma de pago Opreaciones mesa
autor       : projo
fecha       : 04/07/2017
***********************************************************/

SELECT "IdFormaDePago","IdPreferenciaPago","Nombre","Dias"/*,"FechaCreacion","FechaModificacion"*/,"PorDefecto","IdUsuarioCreador","IdEstado","IdEmpresa"
FROM "BSCL.Mesa.BD::MES.FormaDePagoMesa"
WHERE "IdEmpresa" = :IdEmpresa AND "IdFormaDePago" IN (:FormasDePago) 