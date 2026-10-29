SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/


DELETE FROM "BSCL.SocioNegocio.BD::PER.TipoGenero";
DELETE FROM "BSCL.SocioNegocio.BD::PER.TipoPersona";
DELETE FROM "BSCL.SocioNegocio.BD::PER.TipoMedioContacto";
DELETE FROM "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto";
delete from "BSCL.SocioNegocio.BD::PER.TipoCargoPersona";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/


INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoGenero" ("IdTipoGenero","Descripcion")
VALUES ('F','Femenino');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoGenero" ("IdTipoGenero","Descripcion")
VALUES ('M','Masculino');




INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoPersona" ("IdTipoPersona","Descripcion")
VALUES ('N','Persona Natural');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoPersona" ("IdTipoPersona","Descripcion")
VALUES ('J','Persona Juridica');
/*INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoPersona" ("IdTipoPersona","Descripcion")
VALUES ('A','Ambas');
*/


INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DFISICA','Dirección','DIRFIS');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DCASILLA','Casilla','DIRFIS');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('TFIJO','Telefono Fijo','TELE');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('TMOVIL','Telefono Celular','TELE');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DEMAIL','Email','DIRELEC');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DWEB','Página Web','DIRELEC');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DTWIT','Twitter','DIRELEC');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DFACE','FaceBook','DIRELEC');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" ("IdTipoMedioContacto","Descripcion","SubTipo")
VALUES ('DGOOGLE','Google +','DIRELEC');



INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" ("IdPropositoMedioContacto","Descripcion")
VALUES ('PART','Particular');
INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" ("IdPropositoMedioContacto","Descripcion")
VALUES ('COME','Comercial');
INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" ("IdPropositoMedioContacto","Descripcion")
VALUES ('RECA','Recados');
INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" ("IdPropositoMedioContacto","Descripcion")
VALUES ('FACT','Facturación');
INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" ("IdPropositoMedioContacto","Descripcion")
VALUES ('CORR','Correspondencia');


-- FATCA
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"("IdClasificacionFatca","Nombre","PorDefecto")
VALUES('PENDIENTE','Pendiente de Clasificar','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"("IdClasificacionFatca","Nombre","PorDefecto")
VALUES('USENTITY','US Entity','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"("IdClasificacionFatca","Nombre","PorDefecto")
VALUES('NOUSENTITY','Non US Entity','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"("IdClasificacionFatca","Nombre","PorDefecto")
VALUES('RECALCITR','Recalcitrante','N');


INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('FFI','FFI','N','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('FFIEXCEPT','FFI Excepted','N','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('NPFFI','NPFFI','N','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('NFFEEXCEPT','NFFE Excepted','N','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('NFFEPASSIV','NFFE Passive','N','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('NFFERECALC','NFFE Recalcitrante','N','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"("IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN")
VALUES('PENDIENTE','Pendiente de clasificar','S','S');


INSERT INTO "BSCL.SocioNegocio.BD::PER.Fatca" ( 
	"IdPersona", "IdEstado", "IdClasificacionFatca", "IdClasificacionEmpresaFatca", "NumeroGIIN", 
	 "FechaIngreso","FechaCreacion","FechaModificacion","IdUsuarioCreador","IdUsuarioModificador")
SELECT 
	"IdPersona",'ACT','NOUSENTITY','','',CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,1
FROM "BSCL.SocioNegocio.BD::PER.Persona"



INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio" 
VALUES (1,'Mayor a UF 100.001',100001,-1);
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio" 
VALUES (2,'Entre UF 50.001 y UF 100.000',50001,100000);
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio" 
VALUES (3,'Entre UF 10.000 y UF 50.000',10000,50000);
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio" 
VALUES (4,'Menor a UF 10.000',0,10000);

delete from "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio" 
where "IdRangoPatrimonio" = 2



INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )  
VALUES(1,1,'JEFCUS','JEFE DE CUSTODIA','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )    
VALUES(1,2,'PERADM','PERSONAL ADMINISTRATIVO','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )    
VALUES(1,3,'TRA','TRADER','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )    
VALUES(1,4,'TES','TESORERO','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )  
VALUES(1,5,'AGE','AGENTE','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )  
VALUES(1,6,'SUP','SUPERVISOR','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )   
VALUES(1,7,'OPEOTC','OPERADOR','N');--Operador otra corredora
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )    
VALUES(1,8,'ASEFIN','ASESOR FINANCIERO','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )    
VALUES(1,9,'DIR','DIRECTOR','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )    
VALUES(1,10,'GERENT','GERENTE','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )   
VALUES(1,11,'GERGEN','GERENTE GENERAL','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )    
VALUES(1,12,'GERSUB','GERENTE DE AREA O SUBGERENTE','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )    
VALUES(1,13,'ADMIN','ADMINISTRADOR','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )    
VALUES(1,14,'EMPLEA','EMPLEADO','S');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )    
VALUES(1,15,'JEFMES','JEFE MESA DE DINERO','N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" ( "IdEmpresa","IdTipoCargoPersona","CodigoTipoCargoPersona","NombreTipoCargoPersona","UtilizaDetalle" )    
VALUES(1,16,'OPEMES','OPERADOR MESA DE DINERO','N');
  

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
