SET SCHEMA "BSCL";

﻿INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES (12001,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarCentralizaciones',0,'P','Entrega una consulta con las centralizaciones para un rango de fecha');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12002,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarCentralizacionesTipo',0,'P','Entrega una consulta con los tipos de centralizacion asociados a una empresa');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12003,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarMovimientos',0,'P','Entrega una consulta con los Movimientos para una serie de condiciones.');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES (12004,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarMovimientosDatos',0,'P','Entrega la relacion de MovimientoDato por IdMovimiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12005,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarFiltrosTipoDatos',0,'P','Dependiendo del tipo de consulta solicitada, se retornan los TipoDatoEvento relacionados');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12006,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarFiltrosValor',0,'P','Entrega los valores de los filtros adicionales aplicados a los Eventos');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12007,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventos',0,'P','Retorna los Eventos asociados a una Empresa, incluyendo una serie de filtros adicionales');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12008,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarPreComprobante',0,'P','Retorna los PreComprobantes solicitados.');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12009,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarContabilidadTipo',0,'P','Retorna los Tipos de Contabilidad Existentes.');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12010,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarPreComprobanteAsiento',0,'P','Entrega los Precomprobantes dependiendo de la llamada');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12011,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventosAsiento',0,'P','Entrega los Eventos asociados a la definicion de los Asientos');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12012,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventoPreComprobanteAsiento',0,'P','Retorna el contenido de la tabla EventoPreComprobanteAsiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES (12013,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarContabilidadDefinicionAsiento',0,'P','Retorna los datos existentes en la tabla ContabilidadDefinicionAsiento.');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12014,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarDetallePreComprobanteAsiento',0,'P','Retorna los datos asociados al Detalle de los PreComprobanteAsiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES (12015,'BSCL.Contabilidad.BD.Servicios::ITZ.CentralizaGenerico',0,'P','Proceso de Centralizacion Generico');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12016,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventoTipo',0,'P','Entrega la relacion de MovimientoDato por IdMovimiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12017,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarComprobanteTipo',0,'P','Retorna el Pre-Comprobante de acuerdo a su Tipo');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES (12018,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarCondiciones',0,'P','Entrega las condiciones asociadas al Tipo Asiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12019,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventoAsientoAsociado',0,'P','Retorna los datos asociados a un Precomprobante Asiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES (12020,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarMovimientoTipo',0,'P','Retorna los datos asociados al Tipo de Movimiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12021,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarTipoCentralizacionTipoContabilidad',0,'P','Entrega una centralizacion en funcion de un tipo de contabilidad');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12022,'BSCL.Contabilidad.BD.Servicios::ITZ.ModificarEstadoTipoContabilidad',0,'P','Modifica el Estado de la Contabilidad por Tipo');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12023,'BSCL.Contabilidad.BD.Servicios::ITZ.CrearTipoContabilidad',0,'P','Agrega un nuevo registro a la tabla BSCL.Contabilidad.BD::ITZ.ContabilidadTipo');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12024,'BSCL.Contabilidad.BD.Servicios::ITZ.ModificarTipoContabilidad',0,'P','Modifica un registro existente en la tabla BSCL.Contabilidad.BD::ITZ.ContabilidadTipo');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12026,'BSCL.Contabilidad.BD.Servicios::ITZ.ReplicarDefinicionAsiento',0,'P','Replica todos los asientos dado el tipo de contabilidad antiguo y los asocia a uno nuevo');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12027,'BSCL.Contabilidad.BD.Servicios::ITZ.ReplicarTipoContabilidad',0,'P','Reeplica todos los asientos dado el Tipo de Contabilidad antiguo y los asocia al nuevo');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12029,'BSCL.Contabilidad.BD.Servicios::ITZ.CrearTipoCentralizacionesAsociadas',0,'P','Crea un nuevo tipo de centralización asociada');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12030,'BSCL.Contabilidad.BD.Servicios::ITZ.EliminarTipoCentralizacionesAsociadas',0,'P','Elimina ContabilidadTipoCentralizacionTipo de acuerdo a Tipo de Contabilidad y Centralizacion');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12031,'BSCL.Contabilidad.BD.Servicios::ITZ.CrearDefinicionAsiento',0,'P','Agrega un nuevo registro a la tabla BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12032,'BSCL.Contabilidad.BD.Servicios::ITZ.ModificarDefinicionAsiento',0,'P','Agrega un nuevo registro a la tabla BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12033,'BSCL.Contabilidad.BD.Servicios::ITZ.ModificarEstadoDefinicionAsiento',0,'P','Agrega un nuevo registro a la tabla BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12034,'BSCL.Contabilidad.BD.Servicios::ITZ.CrearCondicionTipoAsiento',0,'P','Agrega un nuevo registro a la tabla BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12035,'BSCL.Contabilidad.BD.Servicios::ITZ.CrearTipoEventosAsociados',0,'P','Agrega un nuevo registro a la tabla BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12037,'BSCL.Contabilidad.BD.Servicios::ITZ.EliminarGlosa',0,'P','Elimina Contabilidad Tipo Glosa por Tipo Contabilidad y/o Definicion Asiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12038,'BSCL.Contabilidad.BD.Servicios::ITZ.InsertarGlosa',0,'P','Inserta Glosa Tipo Contabilidad/Asiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12039,'BSCL.Contabilidad.BD.Servicios::ITZ.EliminaTipoEventosAsociados',0,'P','Elimina un tipo de evento asociado');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12040,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarValoresGlosaDisponibles',0,'P','Consulta las condiciones creadas a una asiento o un tipo de contabilidad');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12041,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarValoresGlosaAsociados',0,'P','Consulta las condiciones creadas a una asiento o un tipo de contabilidad');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12042,'BSCL.Contabilidad.BD.Servicios::ITZ.CrearCentralizacion',0,'P','Inserta un registro de Centralización');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES (12043,'BSCL.Contabilidad.BD.Servicios::ITZ.AnularCentralizacion',0,'P','Anular la Centralizacion, Evento, Movimiento y MovimientoDato');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES (12044,'BSCL.Contabilidad.BD.Servicios::ITZ.AnularMovimiento',0,'P','Anular la Centralizacion, Evento, Movimiento y MovimientoDato');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES (12045,'BSCL.Contabilidad.BD.Servicios::ITZ.AnularEvento',0,'P','Anular el Evento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12046,'BSCL.Contabilidad.BD.Servicios::ITZ.EliminarCondiciones',0,'P','Elimina el contanido de la tabla ContabilidadTipoAsientoCondicion y cambia el estado de la tabla ContabilidadDefinicionAsiento, para una Definicion de Asiento o Tipo de Contabilidad');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12047,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarCuentasContables',0,'P','Consulta las Cuentas Contables disponibles en la tabla BSCL.Contabilidad.BD::ITZ.HomologaCuenta');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12048,'BSCL.Contabilidad.BD.Servicios::ITZ.InsertarEvento',0,'P','Ingresa Evento Contable ');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12049,'BSCL.Contabilidad.BD.Servicios::ITZ.InsertarMovimiento',0,'P','Guarda el Movimiento Generado y su detalle');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12050,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEmpresa',0,'P','Consultar Empresa');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12051,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarCentralizacionesEvento',0,'P','Consultar Centralizaciones de Eventos');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12052,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarCentralizacionesRequeridos',0,'P','Consultar Centralizaciones Requeridos');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12053,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventoAsiento',0,'P','Consultar Evento Asiento');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12054,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventosDisponibles',0,'P','Consultar Eventos Disponibles');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12055,'BSCL.Contabilidad.BD.Servicios::ITZ.EliminarCondicionEspecifica',0,'P','Elimina un registro en particular de la tabla ContabilidadTipoAsientoCondicion y cambia el estado de la tabla ContabilidadDefinicionAsiento, para una Definicion de Asiento o Tipo de Contabilidad');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12056,'BSCL.Contabilidad.BD.Servicios::ITZ.EliminarPreComprobante',0,'P','Elimina un PreComprobante');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12057,'BSCL.Contabilidad.BD.Servicios::ITZ.EnvioContabilidad',0,'P','Envio de Informacion a Sistema Contable');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12061,'BSCL.Contabilidad.BD.Servicios::ITZ.GeneraPreComprobante',0,'P','Genera Precomprobantes ');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12062,'BSCL.Contabilidad.BD.Servicios::ITZ.ReversaPreComprobante',0,'P','Reversa Precomprobantes');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (12063,'BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventoAsientoAgrupados',0,'P','Consulta los eventos de los asientos');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (30000,'BSCL.Contabilidad.BD.Servicios::ITZ.CentralizaCarterasRFTerminoYMayorMenorValor',0,'P','CentralizaCarterasRFTerminoYMayorMenorValor');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (30001,'BSCL.Contabilidad.BD.Servicios::ITZ.CentralizaCarterasRFVencimientos',0,'P','CentralizaCarterasRFVencimientos');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion") 
VALUES (30002,'BSCL.Contabilidad.BD.Servicios::ITZ.CentralizaCarterasRFVentaTerminoYSpread',0,'P','CentralizaCarterasRFVentaTerminoYSpread');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (12025,'BSCL.Contabilidad.BD.Servicios::CuadraturaContabilidad',1,'P','');

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstadoContabilidad"
VALUES ('ANU',  'A');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstadoContabilidad"
VALUES ('CNT', 'C');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstadoContabilidad"
VALUES ('VIG',  'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstadoContabilidad"
VALUES ('DSC','D');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstadoContabilidad"
VALUES ('ING','I');

COMMIT;