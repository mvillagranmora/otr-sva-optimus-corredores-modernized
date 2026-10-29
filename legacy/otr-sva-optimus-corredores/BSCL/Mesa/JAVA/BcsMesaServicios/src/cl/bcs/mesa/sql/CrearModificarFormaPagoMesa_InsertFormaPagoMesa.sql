/********************************************************** 
Proposito   : Script insert en tabla "BSCL.Mesa.BD::MES.FormaDePagoMesa"
autor       : projo
fecha       : 12/09/2017
***********************************************************/

INSERT INTO "BSCL.Mesa.BD::MES.FormaDePagoMesa" ("IdFormaDePago","IdPreferenciaPago","Nombre","Dias","FechaCreacion","FechaModificacion","PorDefecto","IdUsuarioCreador","IdEstado","IdEmpresa")
VALUES (:IdFormaDePago,:IdPreferenciaPago,:Nombre,:Dias,:FechaCreacion,:FechaModificacion,:PorDefecto,:IdUsuarioCreador,:IdEstado,:IdEmpresa);