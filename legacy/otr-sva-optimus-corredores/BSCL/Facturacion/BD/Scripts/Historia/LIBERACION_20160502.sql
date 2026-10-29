-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

SET SCHEMA BSCL;
--
--
-- LIBERACION SEMANA: 08-04-2016
--



--################################################
--       <INICIALES DESARROLLADOR>.<MOTIVO>
--################################################

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
alter ("FolioTransaccion" NVARCHAR(40) DEFAULT '');

--Forma de Pago Movimiento
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
alter ("FechaDocumentoPago" DATE DEFAULT '0001-01-01');

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
alter ("FechaVctoTarjetaBancaria" DATE DEFAULT '0001-01-01');


--Forma de Pago Comprobante
alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
 drop ("CodigoMedioDePago");
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
 drop ("NumeroCheque"); 

alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
 drop ("Sucursal"); 

alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
 drop ("FechaVctoDocumento"); 
 
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
alter ("IdTipoTarjetaBancaria" NVARCHAR(40) DEFAULT '');

alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
alter ("NombreTipoTarjetaBancaria" NVARCHAR(100) DEFAULT '');

alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
alter ("NumeroCuentaBancaria" NVARCHAR(100) DEFAULT '');

alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
alter ("NumeroTarjetaBancaria" NVARCHAR(100) DEFAULT '');



alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
 add ("NumeroSerie" NVARCHAR(40) DEFAULT '');

alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
 add ("NumeroDocumento" BIGINT DEFAULT 0);
 
alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
 add ("FechaDocumentoPago" DATE DEFAULT '0001-01-01'); 

alter table "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
 add ("FechaVctoTarjetaBancaria" DATE DEFAULT '0001-01-01');
 

alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("ActualizarCuentaInversion" NVARCHAR(1) DEFAULT 'S');
 
alter table "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
 add ("ActualizarTesoreria" NVARCHAR(1) DEFAULT 'S');  

--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------
