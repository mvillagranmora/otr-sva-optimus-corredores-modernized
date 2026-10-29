SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM  "BSCL.Plataforma.BD::PAG.MedioDePago";
DELETE FROM  "BSCL.Plataforma.BD::PAG.ModalidadDePago";
DELETE FROM  "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","Codigo","Nombre","IdEstado" )  
VALUES('CHE','CHE','Cheque','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","Codigo","Nombre","IdEstado" )  
VALUES('VVI','VVI','Vale Vista','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","Codigo","Nombre","IdEstado" )  
VALUES('EFE','EFE','Efectivo','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","Codigo","Nombre","IdEstado" )  
VALUES('CIN','CIN','Cuenta Inversion','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('TDP','TRA','Transferencia DVP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('TLR','TRA','Transferencia LBTR','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('TSW','TRA','Transferencia Swift','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('BDE','BDE','Boleta de Depósito','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('EBA','CHE','Extra Banco','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('TRA','TRA','Transferencia','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePago" ( "IdMedioDePago","TipoMedioDePago","Nombre","IdEstado" ) 
VALUES('VVE','VVI','Vale Vista Electronico','ACT');


INSERT INTO "BSCL.Plataforma.BD::PAG.ModalidadDePago" ( "IdModalidadDePago","Nombre","IdEstado" )
VALUES('MODCAJ','Por Caja','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.ModalidadDePago" ( "IdModalidadDePago","Nombre","IdEstado" )
VALUES('MODDEP','Depósito','ACT');


INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(1,'CHE','MODCAJ','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(2,'VVI','MODCAJ','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(3,'BDE','MODCAJ','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(4,'EFE','MODCAJ','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(5,'TLR','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(6,'TSW','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(7,'TDP','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(8,'TRA','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(9,'VVE','MODDEP','ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ( "IdMedioDePagoYModalidadDePago","IdMedioDePago","IdModalidadDePago","IdEstado" )
VALUES(10,'EBA','MODDEP','ACT');



-- JG- Ej. INSERT PARA PREFERENCIAS DE PAGO DEFINIDAS POR CORREDORA.
	INSERT INTO "BSCL.Plataforma.BD::PAG.PreferenciaPago" (
		"IdPreferenciaPago","IdTipoOperacion","IdMedioDePago","IdMoneda","UsaPersonaRelacionada","UsaCuentaBancaria","IdEmpresa")
	VALUES('COMACN_CHE_CLP','COMACN','CHE','CLP','N','N',1);
	
	INSERT INTO "BSCL.Plataforma.BD::PAG.PreferenciaPago" (
		"IdPreferenciaPago","IdTipoOperacion","IdMedioDePago","IdMoneda","UsaPersonaRelacionada","UsaCuentaBancaria","IdEmpresa")
	VALUES('COMACN_EFE_CLP','COMACN','EFE','CLP','N','N',1);
	
	INSERT INTO "BSCL.Plataforma.BD::PAG.PreferenciaPago" (
		"IdPreferenciaPago","IdTipoOperacion","IdMedioDePago","IdMoneda","UsaPersonaRelacionada","UsaCuentaBancaria","IdEmpresa")
	VALUES('VENACN_CHE_CLP','VENACN','CHE','CLP','N','N',1);
	
	INSERT INTO "BSCL.Plataforma.BD::PAG.PreferenciaPago" (
		"IdPreferenciaPago","IdTipoOperacion","IdMedioDePago","IdMoneda","UsaPersonaRelacionada","UsaCuentaBancaria","IdEmpresa")
	VALUES('VENACN_EFE_CLP','VENACN','EFE','CLP','N','N',1);

	INSERT INTO "BSCL.Plataforma.BD::PAG.PreferenciaPago" (
		"IdPreferenciaPago","IdTipoOperacion","IdMedioDePago","IdMoneda","UsaPersonaRelacionada","UsaCuentaBancaria","IdEmpresa")
	VALUES('VENACN_TRA_CLP','VENACN','TRA','CLP','N','S',1);

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
