set schema "BSCL";

--eliminar indicador clase de orden y reemplazar por modalidad de orden
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad","IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (1050, 'PORTAFOLIO', 1, '', 'MULTI', 'Modalidad de Ordenes','ORDMODALIDAD', 
'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 99, 'N' );

INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
VALUES (1,1050,'ESCRITAS','Escritas');
INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
VALUES (2,1050,'FAXCONFIRMACIONPOSTERIOR','Fax con Confirmacion Posterior');
INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
VALUES (3,1050,'TELEFONICAS','Telefonicas');
INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
VALUES (4,1050,'APLICACIONESWEB','Aplicaciones Web');
INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
VALUES (5,1050,'MERCADOINTEGRADO','Mercado Integrado');
INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
VALUES (6,1050,'DMA','Acceso Directo a Mercado (DMA)');



INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('FICHACLIENTECOMPLETA', 'FICHA DEL CLIENTE COMPLETA', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(100, 'CORREDORA', 'FICHACLIENTECOMPLETA', 'INGRESOORDEN', 'RECHAZO', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(101, 'CORREDORA', 'FICHACLIENTECOMPLETA', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');



INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('DECLARACIONORIGENFONDOS', 'DECLARACION DE ORIGEN DE FONDOS', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(102, 'CORREDORA', 'DECLARACIONORIGENFONDOS', 'INGRESOORDEN', 'RECHAZO', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(103, 'CORREDORA', 'DECLARACIONORIGENFONDOS', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');



INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('CONTRATOMEDIOSELECTRONICOS', 'CONTRATO DE MEDIOS ELECTRONICOS', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(104, 'CORREDORA', 'CONTRATOMEDIOSELECTRONICOS', 'INGRESOORDEN', 'RECHAZO', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(105, 'CORREDORA', 'CONTRATOMEDIOSELECTRONICOS', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');



INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('FORMULARIOFATCA', 'FORMULARIO FATCA', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(107, 'CORREDORA', 'FORMULARIOFATCA', 'INGRESOORDEN', 'RECHAZO', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(108, 'CORREDORA', 'FORMULARIOFATCA', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');



INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('CONTRATOVALORESBOLSASEXTRANJERAS', 'CONTRATO DE NEGOCIACION DE VALORES EN BOLSAS DEL EXTRANJERO', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(109, 'CORREDORA', 'CONTRATOVALORESBOLSASEXTRANJERAS', 'INGRESOORDEN', 'RECHAZO', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(110, 'CORREDORA', 'CONTRATOVALORESBOLSASEXTRANJERAS', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');




INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('CONTRATOVALORESMILA', 'CONTRATO DE NEGOCIACION DE VALORES EN BOLSAS MILA', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(111, 'CORREDORA', 'CONTRATOVALORESMILA', 'INGRESOORDEN', 'RECHAZO', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(112, 'CORREDORA', 'CONTRATOVALORESMILA', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');





INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('CONTRATOOPCIONESBOLSASEXTRANJERAS', 'CONTRATO DE NEGOCIACION DE OPCIONES EN BOLSAS DEL EXTRANJERO', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(113, 'CORREDORA', 'CONTRATOOPCIONESBOLSASEXTRANJERAS', 'INGRESOORDEN', 'RECHAZO', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(114, 'CORREDORA', 'CONTRATOOPCIONESBOLSASEXTRANJERAS', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');





INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('CONTRATOVALORESEXTRANJEROSAGENTEPROMOTOR', 'CONTRATO DE NEGOCIACIÓN DE VALORES EXTRANJEROS LISTADOS POR AGENTE PROMOTOR', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(115, 'CORREDORA', 'CONTRATOVALORESEXTRANJEROSAGENTEPROMOTOR', 'INGRESOORDEN', 'RECHAZO', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(116, 'CORREDORA', 'CONTRATOVALORESEXTRANJEROSAGENTEPROMOTOR', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');





INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('CONTRATOMERCADOALTERNATIVOVALORES', 'CONTRATO DE NEGOCIACIÓN DE VALORES EN EL MERCADO ALTERNATIVO DE VALORES (MAV)', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(117, 'CORREDORA', 'CONTRATOMERCADOALTERNATIVOVALORES', 'INGRESOORDEN', 'RECHAZO', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(118, 'CORREDORA', 'CONTRATOMERCADOALTERNATIVOVALORES', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');





INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.CondicionOperativa"
("IdCondicionOperativa", "Nombre", "RequiereAtributo1", "RequiereAtributo2", "IdTipoComportamientoDefecto", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES('CONTRATOMINERASJUNIOR', 'CONTRATO DE NEGOCIACIÓN DE VALORES (MINERAS JUNIOR)', 'N', 'N', 'RECHAZO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(119, 'CORREDORA', 'CONTRATOMINERASJUNIOR', 'INGRESOORDEN', 'RECHAZO', 'AGE', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');

INSERT INTO BSCL."BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo", "IdModeloRequisitoOperativo", "IdCondicionOperativa", "IdTipoEtapa", "IdTipoComportamiento", "IdTipoCanalIngreso", "IdMoneda", "ValorAtributo1", "ValorAtributo2", "IdUsuarioCreador", "FechaCreacion", "FechaModificacion", "IdEmpresa", "IdEstado")
VALUES(120, 'CORREDORA', 'CONTRATOMINERASJUNIOR', 'INGRESOORDEN', 'ADVERTENCIA', 'TRA', 0, '', '', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'ACT');






