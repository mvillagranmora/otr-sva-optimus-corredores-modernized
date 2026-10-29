SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
delete from "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria";
delete from "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
delete from "BSCL.Tesoreria.BD::TES.EstadoTesoreria"; 
delete from "BSCL.Tesoreria.BD::TES.CuentaBanco";
delete from "BSCL.Tesoreria.BD::TES.MonedaTesoreria";
delete from "BSCL.Tesoreria.BD::TES.FormatoArchivo";
delete from "BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle";
delete from "BSCL.Tesoreria.BD::TES.HomologacionArchivo";
delete from "BSCL.Tesoreria.BD::TES.TemplateTesoreria";
delete from "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago";
delete from "BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor";
delete from "BSCL.Tesoreria.BD::TES.MedioDePagoMoneda";

/*==============================================================*/
/* Insert Table:  BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria */
/*==============================================================*/
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'SEPARAPORTIPOMOVIMIENTO','SEPARA MOVIMIENTOS POR TIPO MOVIMIENTO','N');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'SEPARAPORCONCEPTO','SEPARA POR MOVIMIENTOS POR CONCEPTO','N');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'SEPARAPORVERIFICASALDO','SEPARA MOVIMIENTOS POR VERIFICACION DE SALDO','N');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'SEPARAPORAGENTE','SEPARA MOVIMIENTOS POR AGENTE','N');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'SEPARAPORSUCURSAL','SEPARA MOVIMIENTOS POR SUCURSAL','N');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'SEPARAPORCONTRAPARTE','SEPARA MOVIMIENTOS POR CONTRAPARTE','N');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'SEPARAPORCORREDORACONTRAPARTE','SEPARA MOVIMIENTOS POR CORREDORA CONTRAPARTE','N');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'SEPARAPORTIPOCOMPROBANTEFACTURACION','SEPARA MOVIMIENTOS POR TIPO COMPROBANTES DE FACTURACION','N');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'SEPARAPORTIPOPRODUCTO','SEPARA MOVIMIENTOS POR TIPO PRODUCTO','N');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'SEPARAPORTIPOOPERACION','SEPARA MOVIMIENTOS POR TIPO OPERACION','N');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'SEPARAPORBOLSA','SEPARA MOVIMIENTOS POR BOLSA','N');
INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" ("IdEmpresa","CodigoParametro","Descripcion","Valor")
VALUES (1,'LIQUIDARPORCOMPROBANTE','LIQUIDAR POR COMPROBANTE O POR FORMA DE PAGO','N');
INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" ("IdEmpresa","CodigoParametro","Descripcion","Valor")
VALUES (1,'LIQUIDARVERIFICARSALDOCAJA','AL LIQUIDAR VERIFICAR SALDO CAJA CTA. INVERSION','N');
INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" ("IdEmpresa","CodigoParametro","Descripcion","Valor")
VALUES (1,'UNIDADMEDIDAIMPRESIONCHEQUES','Unidad de medida para configuración de cheques para impresión','C');

-- Parámetros Clearing Financiero
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'ClearingIncluyeRFPM','Clearing Financiero incluye: RF PM','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'ClearingIncluyeIFPM','Clearing Financiero incluye: IF PM','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'ClearingIncluyeRFPH','Clearing Financiero incluye: RF PH','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'ClearingIncluyeIFPH','Clearing Financiero incluye: IF PH','S');

INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES(1,'RUTAARCHIVOTRANSFERENCIAS','Ruta para almacenar archivos de tesoreria. Se concatena con RutaBaseArchivos de configuracion Plataforma','tes/');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion","Valor")
VALUES(1,'LIQUIDACIONBILATEALMUEVECTAINVERSION','Indica si la liquidación bilateral afecta la cuenta inversion de la contraparte(S/N)','S');

INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion","Valor")
VALUES(1, 'PERMITECERRARPROCESODIARIODESCUADRADO', 'INDICA SI SE PUEDE CERRAR UN PROCESO DEL CIERRE DIARIO QUE ESTE DESCUADRADO', 'S');


/*==============================================================*/
/* Insert Table:   BSCL.Tesoreria.BD::TES.ConceptoTesoreria     */
/*==============================================================*/
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" 
("IdConcepto","NombreEntidad","MultipleConcepto","Tipo","DesplegarEnComboBox","DisponibleIngresoManual","VisiblePorEjecutivo","Origen")
SELECT 'MULCPT','TES.Comprobante',  'S',''  ,'S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'PARING','TES.Comprobante',  'N',''  ,'S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'PAREGR','TES.Comprobante',  'N',''  ,'S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'ANCOTE','TES.Comprobante',  'N',''  ,'S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'APORCI','TES.IngresoEgreso','N','IN','S','S','S','CTA' FROM DUMMY UNION ALL 
SELECT 'LQSDCI','TES.Comprobante',  'N',''  ,'N','S','N','CTA' FROM DUMMY UNION ALL 
SELECT 'ANTSIC','TES.IngresoEgreso','N','EG','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'ANTSIV','TES.IngresoEgreso','N','IN','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'VCTSIC','TES.IngresoEgreso','N','EG','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'VCTSIV','TES.IngresoEgreso','N','IN','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'ANTEMP','TES.IngresoEgreso','N','EG','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'VENEMP','TES.IngresoEgreso','N','EG','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'ANTPRT','TES.IngresoEgreso','N','IN','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'VENPRT','TES.IngresoEgreso','N','IN','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'CIECDV','TES.IngresoEgreso','N',''  ,'S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'CIECSL','TES.IngresoEgreso','N',''  ,'S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'CIECCC','TES.IngresoEgreso','N',''  ,'S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'CMACEX','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMACMI','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMACNA','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMCFEX','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMCFIN','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMCFMN','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMDVFU','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMETFN','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMETFX','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMINFI','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMINRF','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMMONE','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMOSAC','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'CMSIMU','TES.IngresoEgreso','N','IN','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNACEX','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNACMI','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNACNA','TES.IngresoEgreso','N','EG','S','S','S','ORD' FROM DUMMY UNION ALL 
SELECT 'VNCFEX','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNCFIN','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNCFMN','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNDVFU','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNETFN','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNETFX','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNINFI','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNINRF','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNMONE','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNOSAC','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'VNSIMU','TES.IngresoEgreso','N','EG','S','S','N','ORD' FROM DUMMY UNION ALL 
SELECT 'INGPRT','TES.IngresoEgreso','N','IN','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'COBPRI','TES.IngresoEgreso','N','IN','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'APORFM','TES.IngresoEgreso','N','EG','S','S','N','FFM' FROM DUMMY UNION ALL 
SELECT 'RESCFM','TES.IngresoEgreso','N','IN','S','S','N','FFM' FROM DUMMY UNION ALL 
SELECT 'SUECOS','TES.IngresoEgreso','N',''  ,'S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'CMMESP','TES.IngresoEgreso','N','IN','S','S','N','MSP' FROM DUMMY UNION ALL 
SELECT 'VNMESP','TES.IngresoEgreso','N','EG','S','S','N','MSP' FROM DUMMY UNION ALL 
SELECT 'LIQBIL','TES.IngresoEgreso','N',''  ,'S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'VENFWD','TES.IngresoEgreso','N','EG','S','S','N','MFW' FROM DUMMY UNION ALL 
SELECT 'COMFWD','TES.IngresoEgreso','N','IN','S','S','N','MFW' FROM DUMMY UNION ALL 
SELECT 'REPRSE','TES.CargaMasiva'  ,'N','IN','S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'ANUCDH','TES.Deposito'     ,'','ANU','S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'ANUONP','TES.Deposito'     ,'','ANU','S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'ANUMEX','TES.Deposito'     ,'','ANU','S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'ANUDTO','TES.Deposito'     ,'','ANU','S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'ANUFDF','TES.Deposito'     ,'','ANU','S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'ANUCCC','TES.Deposito'     ,'','ANU','S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'ANUFDS','TES.Deposito'     ,'','ANU','S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'SAINCT','TES.IngresoEgreso','N','IN','N','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'CAAEFM','TES.IngresoEgreso','N','EG','S','S','N','FFM' FROM DUMMY UNION ALL 
SELECT 'ABREFM','TES.IngresoEgreso','N','IN','S','S','N','FFM' FROM DUMMY UNION ALL 
SELECT 'CLEFIN','TES.IngresoEgreso','N',''  ,'S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'SINMDP',''				   ,'N',''  ,'N','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'MZCLMP',''				   ,'N',''  ,'N','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'AJCTCO','TES.IngresoEgreso','N',''  ,'S','S','S','TES' FROM DUMMY UNION ALL 
SELECT 'DEURPC','TES.IngresoEgreso','N','IN','S','S','N','TES' FROM DUMMY UNION ALL 
SELECT 'VNOTCN','TES.IngresoEgreso','N','EG','S','S','N','FAC' FROM DUMMY UNION ALL 
SELECT 'VCDTIT','TES.IngresoEgreso','N','IN','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'CPTRME','TES.IngresoEgreso','N','EG','S','S','N','MES' FROM DUMMY UNION ALL 
SELECT 'VNTRME','TES.IngresoEgreso','N','IN','S','S','N','MES' FROM DUMMY UNION ALL 
SELECT 'CPPCME','TES.IngresoEgreso','N','EG','S','S','N','MES' FROM DUMMY UNION ALL 
SELECT 'VNPCME','TES.IngresoEgreso','N','IN','S','S','N','MES' FROM DUMMY UNION ALL
SELECT 'INGGGO','TES.IngresoEgreso','N','IN','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'INGGCC','TES.IngresoEgreso','N','IN','S','S','N','CUS' FROM DUMMY UNION ALL 
SELECT 'RETGGO','TES.IngresoEgreso','N','IN','S','S','N','CUS' FROM DUMMY UNION ALL
SELECT 'RETGCC','TES.IngresoEgreso','N','IN','S','S','N','CUS' FROM DUMMY;

/*==============================================================*/
/* Insert Table:     BSCL.Tesoreria.BD::TES.EstadoTesoreria     */
/*==============================================================*/
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(1,'ING','TES.MovimientoIngresoEgreso');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(3,'BLO','TES.MovimientoIngresoEgreso');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(4,'ANU','TES.MovimientoIngresoEgreso');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(5,'EMI','TES.MovimientoIngresoEgreso');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(6,'ING','TES.Comprobante');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(7,'BLO','TES.Comprobante');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(8,'LIQ','TES.Comprobante');

INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(9,'ING','TES.ComprobanteFormaDePago');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(10,'CRT','TES.ComprobanteFormaDePago');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(13,'ANU','TES.ComprobanteFormaDePago'); --INTERNO
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(14,'LIQ','TES.ComprobanteFormaDePago');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(15,'ACT','TES.Caja');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(16,'INA','TES.Caja');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(17,'BLO','TES.Caja');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(18,'ACT','TES.CajaMoneda');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(19,'INA','TES.CajaMoneda');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(20,'BLO','TES.CajaMoneda');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(21,'ACT','TES.CajaUsuario');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(22,'INA','TES.CajaUsuario');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(23,'BLO','TES.CajaUsuario');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(24,'ACT','TES.CajaProducto');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(25,'INA','TES.CajaProducto');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(26,'BLO','TES.CajaProducto');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(27,'ACT','TES.CuentaBancaria');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(28,'INA','TES.CuentaBancaria');

INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(29,'GIR','TES.Comprobante');  -- Comprobante girado Totalmente
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(30,'GIP','TES.Comprobante');  -- Comprobante girado Parcialmente

INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(31,'ING','TES.NominaTransferencia');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(32,'ANU','TES.NominaTransferencia');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(33,'RCP','TES.NominaTransferencia');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(34,'ING','TES.NominaTransferenciaDetalle');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(35,'ANU','TES.NominaTransferenciaDetalle');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(36,'RCP','TES.NominaTransferenciaDetalle');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(37,'GIR','TES.ComprobanteFormaDePago');


INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(38,'ING','TES.Deposito');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(39,'ANU','TES.Deposito');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(40,'DEP','TES.Deposito');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(41,'ING','TES.DepositoDetalle');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(42,'ANU','TES.DepositoDetalle');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(43,'EMI','TES.DepositoDetalle');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(44,'DEP','TES.ComprobanteFormaDePago');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(45,'LIP','TES.Comprobante');
INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(46,'ANU','TES.Comprobante');

INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(48,'EMI','TES.ComprobanteFormaDePago');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(49,'CRT','TES.ComprobanteFormaDePago');

INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(50,'MOD','TES.Comprobante');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(51,'NDP','TES.ComprobanteFormaDePago');

INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria") + 1, 'GEN','TES.NominaTransferencia' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria"; 
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria") + 1, 'SOL','TES.NominaTransferencia' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria") + 1, 'EMI','TES.ComprobanteFormaDePago' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria") + 1, 'INV', 'TES.CargaMasiva' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria") + 1, 'ING', 'TES.CargaMasiva' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria") + 1, 'PRO', 'TES.CargaMasiva' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria") + 1, 'ABT', 'TES.CierreDiario' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria") + 1, 'CER', 'TES.CierreDiario' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria")+1, 'DEV','TES.EnvioDocumento' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria")+1, 'REV','TES.EnvioDocumento' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria")+1, 'ING','TES.EnvioDocumento' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria")+1, 'ACP','TES.EnvioDocumento' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria")+1, 'DPA','TES.EnvioDocumento' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria")+1, 'DEV','TES.EnvioDocumentoDetalle' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria")+1, 'ACP','TES.EnvioDocumentoDetalle' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT MAX("IdEstadoTesoreria")+1, 'ING','TES.EnvioDocumentoDetalle' FROM BSCL."BSCL.Tesoreria.BD::TES.EstadoTesoreria";




/*==============================================================*/
/* Insert Table:     BSCL.Tesoreria.BD::TES.CuentaBanco         */
/*==============================================================*/
INSERT INTO "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco","IdEmpresa","IdBanco","NombreCuentaBancaria","IdTipoCuentaBancaria","IdMoneda","NumeroCuentaBancaria","Sucursal","Dirección","Contacto","MedioContacto","IdEstado")
VALUES(1,1,10,'CTA BBV CLP N°1','CUCO','CLP','12345678','Las Condes','Av. Apoquindo 3038','Juan Perez','Teléfono 2300695','ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco","IdEmpresa","IdBanco","NombreCuentaBancaria","IdTipoCuentaBancaria","IdMoneda","NumeroCuentaBancaria","Sucursal","Dirección","Contacto","MedioContacto","IdEstado")
VALUES(2,1,10,'CTA BBV CLP N°2','CUCO','CLP','22223333','Santiago','Moneda 1025','Peco Machuca','Teléfono 5623569','ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco","IdEmpresa","IdBanco","NombreCuentaBancaria","IdTipoCuentaBancaria","IdMoneda","NumeroCuentaBancaria","Sucursal","Dirección","Contacto","MedioContacto","IdEstado")
VALUES(3,1,15,'CTA  CON CLP','CUVI','CLP','5555544444','Norte','La camelias 325','','','ACT');


/*==============================================================*/
/* Insert Table:     "BSCL.Tesoreria.BD::TES.MonedaTesoreria"   */
/*==============================================================*/
INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.MonedaTesoreria"("IdMonedaTesoreria", "IdMoneda", "IdEstado")
VALUES(1,'CLP','ACT');
INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.MonedaTesoreria"("IdMonedaTesoreria", "IdMoneda", "IdEstado")
VALUES(2,'USD','ACT');
INSERT INTO "BSCL"."BSCL.Tesoreria.BD::TES.MonedaTesoreria"("IdMonedaTesoreria", "IdMoneda", "IdEstado")
VALUES(3,'EUR','ACT');


/*===============================================================================*/
/* Insert Table:     "BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago"   */
/*===============================================================================*/
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','CHE','LIQ','ING;ANU;CRT;EBV;NDP;LIQ','S','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','VVI','LIQ','ING;ANU;CRT;EBV;NDP;LIQ','S','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','BDE','CRT;LIQ','ING;ANU;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','EFE','LIQ','ING;ANU;CRT;EBV;NDP;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TLR','CRT;LIQ','ING;ANU;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TSW','CRT;LIQ','ING;ANU;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TDP','CRT;LIQ','ING;ANU;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','EBA','LIQ','ING;ANU;EBV;LIQ','S','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TRA','CRT;LIQ','ING;ANU;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','VVE','LIQ','ING;ANU;LIQ','N','S');

INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','CHE','EMI;GIR;LIQ','ING;ANU;EMI;GIR;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','VVI','EMI;GIR;LIQ','ING;ANU;EMI;GIR;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','BDE',NULL,NULL,NULL,'N');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','EFE','ING;GIR;LIQ','ING;ANU;GIR;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TLR','ING;GIR;LIQ','ING;ANU;GIR;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TSW','ING;GIR;LIQ','ING;ANU;GIR;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TDP','ING;GIR;LIQ','ING;ANU;GIR;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','EBA',NULL,NULL,NULL,'N');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TRA','ING;GIR;LIQ','ING;ANU;GIR;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','VVE','ING;GIR;LIQ','ING;ANU;GIR;LIQ','N','S');


INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','AJU','CRT;LIQ','ING;ANU;CRT;LIQ','N','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","EstadosPosibleTipoMovimientoMedioDePago","AfectoADiasRetencion","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','AJU','ING;GIR;LIQ','ING;ANU;GIR;LIQ','N','S');



/*==============================================================*/
/* Insert Table:  BSCL.Tesoreria.BD::TES.FormatoArchivo         */
/*==============================================================*/
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivo"("IdFormatoArchivo","IdBanco","IdMoneda","IdMedioDePago","FormatoSalida","Descripcion","IdEmpresa","TipoGlosa","Servicio","IdEstado")
VALUES (1,17,'CLP','TRA','TXT','Formato de archivo para: Bco Chile/TRANS/CLP',1,2,'BSCL.Tesoreria.BD.Servicios.NOM::ConsultarTransferenciasBcoChile','ACT');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivo"("IdFormatoArchivo","IdBanco","IdMoneda","IdMedioDePago","FormatoSalida","Descripcion","IdEmpresa","TipoGlosa","Servicio","IdEstado")
VALUES (2,17,'EUR','TRA','TXT','Formato de archivo para: Bco Chile/TRANS/EUR',1,1,'BSCL.Tesoreria.BD.Servicios.NOM::ConsultarTransferenciasBcoChile','ACT');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivo"("IdFormatoArchivo","IdBanco","IdMoneda","IdMedioDePago","FormatoSalida","Descripcion","IdEmpresa","TipoGlosa","Servicio","IdEstado")
VALUES (3,17,'CLP','VVE','TXT','Formato de archivo para¨: Bco Chile/VAL VIS/CLP',1,0,'BSCL.Tesoreria.BD.Servicios.NOM::ConsultarTransferenciasBcoChile','ACT');


/*==============================================================*/
/* Insert Table:  BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle  */
/*==============================================================*/
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (1,1,1,'TipoOperacion','CHAR',3,0,'',1,'Tipo Operacion');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (2,1,1,'RutCliente','CHAR',11,0,'',2,'Rut Cliente Beneficiario');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (3,1,1,'CuentaCargo','CHAR',12,0,'',3,'Secuencia de Cliente');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (4,1,1,'RutBeneficiario','CHAR',11,0,'',4,'Rut Cliente Beneficiario');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (5,1,1,'NombreBeneficiario','CHAR',50,0,'',5,'Nombre Beneficiario');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (6,1,1,'CuentaBeneficiario','CHAR',20,0,'',6,'CuentaBeneficiario');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (7,1,1,'RutBancoBeneficiario','CHAR',11,0,'',7,'Banco Beneficiario');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (8,1,1,'MontoTransferencia','NUM',9,0,'',8,'Monto Transferencia');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (9,1,1,'AbonoInmediato','CHAR',1,0,' ',9,'AbonoInmediato - Valor fijo: “ “');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (10,1,1,'MotivoTransferencia','CHAR',30,0,'',10,'MotivoTransferencia');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (11,1,1,'NotificacionViaEmail','NUM',1,0,'',11,'NotificacionViaEmail');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (12,1,1,'AsuntoEmail','CHAR',23,0,'Aviso de Transferencias',12,'Valor Fijo: “Aviso de Transferencias”');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (13,1,1,'DireccionEMail','CHAR',50,0,'',13,'DireccionEMail');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" ("IdFormatoArchivoDetalle","IdFormatoArchivo","TipoRegistro","Campo","TipoDato","Largo","Decimales","ValorDefecto","Orden","Descripcion")
VALUES (14,1,1,'TipoCuenta','CHAR',3,0,'',14,'TipoCuenta');


/*==============================================================*/
/* Insert Table:  BSCL.Tesoreria.BD::TES.HomologacionArchivo    */
/*==============================================================*/
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.HomologacionArchivo" ("IdHomologacionArchivo","IdFormatoArchivo","Campo","ValorOptimus","ValorExterno","Descripcion")
VALUES (1,1,'TipoCuenta','CUCO','CTD','Cuenta corriente');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.HomologacionArchivo" ("IdHomologacionArchivo","IdFormatoArchivo","Campo","ValorOptimus","ValorExterno","Descripcion")
VALUES (2,1,'TipoCuenta','CUAH','AHB','Cuenta ahorro');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.HomologacionArchivo" ("IdHomologacionArchivo","IdFormatoArchivo","Campo","ValorOptimus","ValorExterno","Descripcion")
VALUES (3,1,'TipoCuenta','CUVI','JUV','Cuenta Vista');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.HomologacionArchivo" ("IdHomologacionArchivo","IdFormatoArchivo","Campo","ValorOptimus","ValorExterno","Descripcion")
VALUES (4,1,'TipoOperacion','1','TEC','Transferencias a Terceros Banco de Chile');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.HomologacionArchivo" ("IdHomologacionArchivo","IdFormatoArchivo","Campo","ValorOptimus","ValorExterno","Descripcion")
VALUES (5,1,'TipoOperacion','2','TOB','Transferencias a Otros Bancos');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.HomologacionArchivo" ("IdHomologacionArchivo","IdFormatoArchivo","Campo","ValorOptimus","ValorExterno","Descripcion")
VALUES (6,1,'TipoOperacion','3','TAM','Transferencias Altos Montos (SPAV)');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.HomologacionArchivo" ("IdHomologacionArchivo","IdFormatoArchivo","Campo","ValorOptimus","ValorExterno","Descripcion")
VALUES (7,1,'TipoOperacion','4','TMC','Transferencias Entre Cuentas Propias');
INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.HomologacionArchivo" ("IdHomologacionArchivo","IdFormatoArchivo","Campo","ValorOptimus","ValorExterno","Descripcion")
VALUES (8,1,'TipoOperacion','5','TVV','Transferencias a Vales Vistas');


/*==============================================================*/
/* Insert Table:  BSCL.Tesoreria.BD::TES.HomologacionArchivo    */
/*==============================================================*/

INSERT INTO bscl."BSCL.Tesoreria.BD::TES.TemplateTesoreria" 
SELECT 'RELIBI','LiquidacionBitaleralDetallado','Rpt liquidacion bilateral detallado',1732,'pdf',1,'ACT' FROM DUMMY UNION ALL 
SELECT 'RECATR','CartaTransferenciasBancarias','Rpt Carta de transferencias bancarias',1733,'pdf',1,'ACT' FROM DUMMY UNION ALL 
SELECT 'DECOTE','DetalleComprobanteTesoreria','Detalle de Comprobante de Tesorería',1731,'pdf',1,'ACT' FROM DUMMY;


/*==================================================================*/
/* Insert Table:  BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor    */
/*==================================================================*/

INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor"("IdPreferenciasPagoCorredor","IdTipoMovimiento","IdConcepto","IdMedioDePago","IdCuentaBanco","UnicaOpcionDePago","IdEmpresa","IdEstado")
VALUES (1, 'MOVING', 'AJCTCO', 'AJU', 1, 'S', 1,'ACT');

INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor"("IdPreferenciasPagoCorredor","IdTipoMovimiento","IdConcepto","IdMedioDePago","IdCuentaBanco","UnicaOpcionDePago","IdEmpresa","IdEstado")
VALUES (2,'MOVEGR','AJCTCO','AJU',1,'S', 1,'ACT');

INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor"("IdPreferenciasPagoCorredor","IdTipoMovimiento","IdConcepto","IdMedioDePago","IdCuentaBanco","UnicaOpcionDePago","IdEmpresa","IdEstado")
VALUES (3,'MOVEGR','FEVTSM','CHE',1,'N', 1,'ACT');

INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor"("IdPreferenciasPagoCorredor","IdTipoMovimiento","IdConcepto","IdMedioDePago","IdCuentaBanco","UnicaOpcionDePago","IdEmpresa","IdEstado")
VALUES (4,'MOVEGR','ANTSIV','TSW',2,'N', 1,'ACT');

INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor"("IdPreferenciasPagoCorredor","IdTipoMovimiento","IdConcepto","IdMedioDePago","IdCuentaBanco","UnicaOpcionDePago","IdEmpresa","IdEstado")
VALUES (5,'MOVEGR','ANTSIC','CHE',1,'N', 1,'ACT');

INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor"("IdPreferenciasPagoCorredor","IdTipoMovimiento","IdConcepto","IdMedioDePago","IdCuentaBanco","UnicaOpcionDePago","IdEmpresa","IdEstado")
VALUES (6,'MOVEGR','VCTSIC','TSW',2,'N', 1,'ACT');

INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor"("IdPreferenciasPagoCorredor","IdTipoMovimiento","IdConcepto","IdMedioDePago","IdCuentaBanco","UnicaOpcionDePago","IdEmpresa","IdEstado")
VALUES (7,'MOVEGR','VCTSIC','CHE',3,'N', 1,'ACT');

INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor"("IdPreferenciasPagoCorredor","IdTipoMovimiento","IdConcepto","IdMedioDePago","IdCuentaBanco","UnicaOpcionDePago","IdEmpresa","IdEstado")
VALUES (8,'MOVEGR','VCTSIV','EFE',29,'N', 1,'ACT');

INSERT INTO BSCL."BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor" ("IdPreferenciasPagoCorredor","IdTipoMovimiento","IdConcepto","IdMedioDePago","IdCuentaBanco","UnicaOpcionDePago","IdEmpresa","IdEstado","IdModalidadDePago")
SELECT MAX("IdPreferenciasPagoCorredor") + 1, 'MOVING', 'SAINCT', 'TRA', 1, 'N', 1, 'ACT', 'MODDEP' FROM BSCL."BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor";


/*==================================================================*/
/* Insert Table:  BSCL.Tesoreria.BD::TES.MedioDePagoMoneda          */
/*==================================================================*/
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('CHE','CLP',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('VVI','CLP',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('BDE','CLP',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('EFE','CLP',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('TLR','CLP',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('TSW','CLP',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('TDP','CLP',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('EBA','CLP',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('TRA','CLP',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('VVE','CLP',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('CHE','USD',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('EFE','USD',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('TLR','USD',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('TSW','USD',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('TDP','USD',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('EBA','USD',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('TRA','USD',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('CHE','EUR',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('EFE','EUR',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('TLR','EUR',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('TSW','EUR',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('TDP','EUR',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('EBA','EUR',1,'ACT');
INSERT INTO "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria"("IdMedioDePago","IdMoneda","IdEmpresa","IdEstado")
VALUES ('TRA','EUR',1,'ACT');


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
