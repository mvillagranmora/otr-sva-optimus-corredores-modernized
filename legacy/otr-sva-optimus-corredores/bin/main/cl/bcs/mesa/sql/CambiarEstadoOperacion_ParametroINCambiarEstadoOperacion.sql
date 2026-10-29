/********************************************************** 
Proposito	: Script para la insercion en Parametro Entrada Servicio Cambiar estado Operacion
autor		: projo
fecha		: 03/07/2017
***********************************************************/

INSERT INTO "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN"(
     "IdOperacion"
    ,"IdTipoProducto"
    ,"IdNuevoEstado"
    ,"IdReserva"
    ,"FolioCarteraCustodia"
    ,"MontoOperacionAnticipo"
    ,"MontoOperacionInicialAnticipo"
    ,"MontoOperacionFinalAnticipo"
    ,"TasaAnticipo"
    ,"MontoOperacionMonedaAnticipo"
    ,"MontoOperacionInicialMonedaAnticipo"
    ,"MontoOperacionFinalMonedaAnticipo"
    ,"IdMonedaLiquidacionAnticipo"
    ,"IdCondicionLiquidacionAnticipo"
    ,"FechaCondicionLiquidacionAnticipo"
    ,"Glosa"
)
values (
	 :IdOperacion
    ,:IdTipoProducto
    ,:IdNuevoEstado
    ,:IdReserva
    ,:FolioCarteraCustodia
    ,:MontoOperacionAnticipo
    ,:MontoOperacionInicialAnticipo
    ,:MontoOperacionFinalAnticipo
    ,:TasaAnticipo
    ,:MontoOperacionMonedaAnticipo
    ,:MontoOperacionInicialMonedaAnticipo
    ,:MontoOperacionFinalMonedaAnticipo
    ,:IdMonedaLiquidacionAnticipo
    ,:IdCondicionLiquidacionAnticipo
    ,:FechaCondicionLiquidacionAnticipo
    ,COALESCE(:Glosa, '') 
);