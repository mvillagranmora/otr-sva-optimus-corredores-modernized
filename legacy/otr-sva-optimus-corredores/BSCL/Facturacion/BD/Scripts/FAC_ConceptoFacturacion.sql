SET SCHEMA "BSCL"; 
DELETE FROM "BSCL.Facturacion.BD::FAC.ConceptoFacturacion";

insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")
values(1,'FACCOM','FAC.MovimientoComprobante','','','','');                                                                                                                                      
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")
values(2,'FACVEN','FAC.MovimientoComprobante','','','','');                                                                                                                                      
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")
values(3,'FAECOM','FAC.MovimientoComprobante','','','','');                                                                                                                                      
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")
values(4,'FAEVEN','FAC.MovimientoComprobante','','','','');                                                                                                                                      
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(5,'NCECOM','FAC.MovimientoComprobante','','','','');                                                                                                                                      
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(6,'NCEVEN','FAC.MovimientoComprobante','','','','');                                                                                                                                      
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(7,'NCRCOM','FAC.MovimientoComprobante','','','','');                                                                                                                                      
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(8,'NCRVEN','FAC.MovimientoComprobante','','','','');                                                                                                                                      
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(9,'FAOFAO','FAC.MovimientoComprobante','','','','');                                                                                                                                      
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(10,'FAOPRI','FAC.MovimientoComprobante','','','','');                                                                                                                                     
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(11,'FAOPRE','FAC.MovimientoComprobante','','','',''); 

--Asociado al tipo producto y tipo operacion
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(12,'FACMSM','','FAC','INSI','COMSIM','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(13,'FAVTSM','','FAC','INSI','VENSIM','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(14,'FACMAC','','FAC','INAC','COMACN','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(15,'FAPRES','','FAC','','PRESTA','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(16,'FAEMPR','','FAC','','EMPRES','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(17,'FAVNAC','','FAC','INAC','VENACN','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(18,'FECMSM','','FAE','INSI','COMSIM','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(19,'FEVTSM','','FAE','INSI','VENSIM','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(20,'FECMAC','','FAE','INAC','COMACN','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(23,'FEVNAC','','FAE','INAC','VENACN','TOTAL');

insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(24,'FEPRES','','FAE','','PRESTA','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(25,'FOPRES','','FAO','','PRESTA','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(26,'FECMAX','','FAE','INAX','COMACX','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(27,'FEVNAX','','FAE','INAX','VENACX','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(28,'FECMMI','','FAE','INMI','COMMIL','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(29,'FEVNMI','','FAE','INMI','VENMIL','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(30,'FECMRF','','FAE','INRF','COMRF','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(31,'FEVNRF','','FAE','INRF','VENRF','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(32,'FECMIF','','FAE','INIF','COMIIF','TOTAL');
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(33,'FEVNIF','','FAE','INIF','VENIIF','TOTAL');


insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
values(34,'FOPREM','','FAO','INAC','EMPRES','TOTAL')
insert into "BSCL.Plataforma.BD::PLA.Concepto" 
values ('NCEACS','','FAC', 'NOTA CREDITO ELECTRONICA ANTICIPO COMPRA SIMULTANEA','VIG')
insert into "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
values(35,'NCEACS','','NCE','INSI','COMSIM','TOTAL')

insert into "BSCL.Plataforma.BD::PLA.Concepto" 
values ('NCRACS','','FAC', 'NOTA CREDITO ANTICIPO COMPRA SIMULTANEA','VIG')
insert into "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
values(36,'NCRACS','','NCR','INSI','ANSIMC','TOTAL')

insert into "BSCL.Plataforma.BD::PLA.Concepto" 
values ('NCEAVS','','FAC', 'NOTA CREDITO ELECTRONICA ANTICIPO VENTA SIMULTANEA','VIG')
insert into "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
values(37,'NCEAVS','','NCR','INSI','ANSIMV','TOTAL')

insert into "BSCL.Plataforma.BD::PLA.Concepto" 
values ('NCRAVS','','FAC', 'NOTA CREDITO ANTICIPO VENTA SIMULTANEA','VIG')
insert into "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
values(38,'NCRAVS','','NCR','INSI','VENSIM','TOTAL')

-- FALTAN DEL 39 AL 46 ESTAN INSERTADOS, PERO NO DEFINIDOS EN ESTE SCRIPT.

insert into "BSCL"."BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
values(47,'FECOSA','','FAE','INAC','SUSOSA','TOTAL')
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(48,'FECMCX','','FAE','INCX','COMCFX','TOTAL'); 
insert into "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "NombreEntidad","IdTipoComprobante","IdTipoProducto","IdTipoOperacion","CodigoPreferencia")                    
values(49,'FEVNCX','','FAE','INCX','VENCFX','TOTAL'); 

INSERT INTO BSCL."BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(46, 'DIEVAC', 'DIEVAC', 'CUS', 'VIG');

----------------------------------------------------------------------------------------------------------------------------------
-- ANULACIONES
----------------------------------------------------------------------------------------------------------------------------------

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(62, 'ANVETN', 'ANVETN', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(63, 'ANCETX', 'ANCETX', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(81, 'ANVACN', 'ANVACN', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(64, 'ANVFUT', 'ANVFUT', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(65, 'ANCSIM', 'ANCSIM', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(66, 'ANVETX', 'ANVETX', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(67, 'ANVIIF', 'ANVIIF', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(68, 'ANCETN', 'ANCETN', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(69, 'ANCFUT', 'ANCFUT', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(70, 'ANVACX', 'ANVACX', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(71, 'ANCCFX', 'ANCCFX', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(72, 'ANVCFI', 'ANVCFI', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(73, 'ANCACN', 'ANCACN', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(74, 'ANCIIF', 'ANCIIF', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(75, 'ANCCFI', 'ANCCFI', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(76, 'ANVMIL', 'ANVMIL', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(77, 'ANVCFX', 'ANVCFX', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(78, 'ANVSIM', 'ANVSIM', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(79, 'ANCMIL', 'ANCMIL', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(80, 'ANCACX', 'ANCACX', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(82, 'ANCMON', 'ANCMON', 'FAC', 'VIG');

INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado")
VALUES(83, 'ANVMON', 'ANVMON', 'FAC', 'VIG');






Commit;
