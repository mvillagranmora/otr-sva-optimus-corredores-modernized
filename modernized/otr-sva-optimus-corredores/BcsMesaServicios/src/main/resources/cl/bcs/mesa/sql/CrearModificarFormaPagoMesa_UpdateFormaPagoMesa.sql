/********************************************************** 
Proposito   : Script update en tabla "BSCL.Mesa.BD::MES.FormaDePagoMesa"
autor       : projo
fecha       : 12/09/2017
***********************************************************/

UPDATE "BSCL.Mesa.BD::MES.FormaDePagoMesa" 
SET "Nombre"=:Nombre
    ,"Dias"=:Dias
    ,"FechaModificacion" =:FechaModificacion
    ,"PorDefecto" =:PorDefecto
    ,"IdUsuarioCreador" =:IdUsuarioCreador
    ,"IdEstado" =:IdEstado
WHERE "IdFormaDePago" = :IdFormaDePago AND "IdEmpresa" = :IdEmpresa;