/********************************************************** 
Proposito   : Script para obtener las Forma de pago Opreaciones mesa
autor       : projo
fecha       : 04/07/2017
***********************************************************/

SELECT "IdComprobantePagoOperacion","IdOperacion", "IdIngresoEgreso", "IdAbono", "IdCargo", "IdFormaDePago", "RecibePaga", "IdModalidadPago", "IdMonedaComprobantePago", "IdMonedaLiquidacion",
    "Monto", "MontoLiquidacion", "FechaIngresoComprobantePagoOperacion", "FechaLiquidacionComprobantePagoOperacion", "IdEstadoComprobantePago", "IdFormaDePago", "NombreFormaDePago", "Dias",
    "RecibePagaPortafolioDesc", "IdPreferenciaPago", "IdTipoOperacion", "IdMedioDePago", "IdMoneda", "UsaPersonaRelacionada", "FechaCreacion", "FechaModificacion", "IdUsuarioCreador", 
    "IdEstado", "IdEmpresa", "NombreTipoOperacion", "IdTipoProducto", "NombreTipoProducto", "RecibePagaPortafolio", "NombreMedioDePago", "NombreMoneda", "UsaCuentaBancaria", "UsaTarjetaBancaria",
    "NombreEstado"
FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" 
WHERE "IdEmpresa" = :IdEmpresa AND "IdOperacion" IN (:Operaciones) 