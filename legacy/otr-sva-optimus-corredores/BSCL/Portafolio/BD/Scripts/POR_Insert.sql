SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/

DELETE FROM  "BSCL.Portafolio.BD::POR.PortafolioClienteUsoMedioContacto"; 
DELETE FROM  "BSCL.Portafolio.BD::POR.Portafolio"; 
DELETE FROM  "BSCL.Portafolio.BD::POR.EstadoPortafolio"; 
DELETE FROM  "BSCL.Portafolio.BD::POR.TipoBloqueoPortafolio"; 
DELETE FROM  "BSCL.Portafolio.BD::POR.PreferenciaFacturacion"; 
DELETE FROM  "BSCL.Portafolio.BD::POR.PropositoPortafolio";
DELETE FROM  "BSCL.Portafolio.BD::POR.TipoAutorizado";
/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('INTERMEDIACION','Intermediacion','ACT');
INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('57BIS','57BIS','ACT');
INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('ADMINCARTERA','Administración de cartera','ACT');
INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('APV','APV','ACT');
INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('FFMM','Fondos Mutuos','ACT');


INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio" ( "IdEstadoPortafolio","IdEstado","NombreEntidad" )  VALUES(1,'ACT','PORTAFOLIO');
INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio" ( "IdEstadoPortafolio","IdEstado","NombreEntidad" )  VALUES(2,'BLO','PORTAFOLIO');
INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio" ( "IdEstadoPortafolio","IdEstado","NombreEntidad" )  VALUES(3,'ACT','ORDENAUTORIZADO');
INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio" ( "IdEstadoPortafolio","IdEstado","NombreEntidad" )  VALUES(4,'BLO','ORDENAUTORIZADO');
INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio" ( "IdEstadoPortafolio","IdEstado","NombreEntidad" )  VALUES(5,'PEN','PORTAFOLIO');



insert into "BSCL.Portafolio.BD::POR.TipoAutorizado" ("IdEmpresa", "IdTipoAutorizado", "NombreTipoAutorizado","IdTipoRelacion")
values(1,'ORDEN-CREAR','Autorizado a dar órdenes','AUTZDO');

insert into "BSCL.Portafolio.BD::POR.TipoAutorizado" ("IdEmpresa", "IdTipoAutorizado", "NombreTipoAutorizado","IdTipoRelacion")
values(1,'ORDEN-CONSULTAR','Sólo consulta','AUTZDO');

insert into "BSCL.Portafolio.BD::POR.TipoAutorizado" ("IdEmpresa", "IdTipoAutorizado", "NombreTipoAutorizado","IdTipoRelacion")
values(1,'ORDEN-FIRMAR','Firmante de contratos','AUTZDO');



--PORTAFOLIO
--INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio","CodigoPortafolio","Proposito","IdCliente","IdEstado","FechaBloqueo","ComentarioBloqueo","IdTipoBloqueo","FechaIngreso","FechaCreacion","FechaModificacion","IdCarga","IdPerfilComercialBaseOrigen","IdPerfilProductos","IdPerfilCobros","IdPerfilLimites","IdPerfilCondicionesOperativas","IdPerfilFacturacion","IdPerfilFormasPago")

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
