SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/

DELETE FROM  "BSCL.SocioNegocio.BD::REL.TipoRelacion"; 
DELETE FROM  "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino";
DELETE FROM  "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion";
DELETE FROM  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion";
DELETE FROM  "BSCL.SocioNegocio.BD::REL.GradoVinculacionParental";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/


-----	TipoRelacion

--MANTENEDOR RELACIONADOS
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('APO',1,'S','CLIENTE','APODERADO','ATAPO','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('ATAPO',1,'S','PERSONA','AUTORIZA APODERADO','APO','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('VINPRO',1,'S','CLIENTE','VINCULADO POR PROPIEDAD AL INTERMEDIARIO','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('VINLAB',1,'S','EMPLEADO','​VINCULADO LABORALMENTE AL INTERMEDIARIO','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('VINADM',1,'S','CLIENTE','​VINCULADO POR ADMINISTRACION AL INTERMEDIARIO','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('ENTGEM',1,'S','PERSONA','ENTIDAD DEL GRUPO EMPRESARIAL','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('MIADMS',1,'S','PERSONA','MIEMBRO DE LA ADMINISTRACION CONTROLE >=10% SOCIEDAD','','N','ACT','RELACIONADOS');

INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('SOC',1,'S','PERSONA','SOCIO','','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('REPAD',1,'S','PERSONA','REPRESENTANTE LEGAL ADICIONAL','','N','ACT','RELACIONADOS');

--FICHA PERSONA
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('CY',1,'S','PERSONA','CONYUGE','','S','ACT','PERSONA');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('REP',1,'S','PERSONA','REPRESENTANTE LEGAL','','S','ACT','PERSONA');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('CEMDOR',1,'N','CORREDOR','EMPLEADOR','','S','ACT','EMPLEADO'); 	--CORREDOR/EMPLEADOR
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('PEMDOR',1,'N','PERSONA','EMPLEADOR','','S','ACT','EMPLEADO');	--PERSONA/EMPLEADOR
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('AUTZDO',1,'S','PERSONA','AUTORIZADO A DAR ORDENES','AUTDOR','S','ACT','AUTORIZADO');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('AUTDOR',1,'S','PERSONA','AUTORIZA A DAR ORDENES','','S','ACT','AUTORIZADO');

INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('OTRARE',1,'S','PERSONA','OTRA RELACION (SISTEMA)','','S','ACT','SISTEMA');

INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('OTRARE',1,'S','PERSONA','OTRA RELACION (SISTEMA)','','S','ACT','SISTEMA');

---


INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('PARSOC',1,'S','CLIENTE','Participación en Sociedades','','S','ACT','RELACIONADOS');

INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('BENEMP',1,'S','CLIENTE','Beneficiario de la Empresa','','S','ACT','RELACIONADOS');


--Para generacion de contratos Persona JURIDICA
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::REL.TipoRelacion"( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('APOFIR',1,'S','CLIENTE','APODERADO FIRMANTE','ATAPOF','N','ACT','RELACIONADOS');
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('ATAPOF',1,'S','PERSONA','AUTORIZA APODERADO FIRMANTE','APOFIR','N','ACT','RELACIONADOS');

INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('RELFAM',1,'S','PERSONA','Relacion Familiar','RELFAM','N','ACT','RELACIONADOS');

INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('PREVI',1,'S','PERSONA','PREVISION','RELFAM','N','ACT','RELACIONADOS');
	
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('PODER',1,'S','PERSONA','Poder a Terceros','','N','ACT','RELACIONADOS');
			


/*
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('CLIOPE',1,'S','CLIENTE','Operador','EM','N','ACT','RELACIONADOS');

INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")
VALUES('EMPDO',1,'S','PERSONA','Empleado','EMPDOR','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('EMPDOR',1,'S','PERSONA','Empleador','EMPDO','N','ACT','RELACIONADOS');
*/

/*
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('GG',1,'S','PERSONA','Gerente General','EMPDOR','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")
VALUES('GA',1,'S','PERSONA','Gerente de Area o Subgerente','EMPDOR','N','ACT','RELACIONADOS');
*/
/*
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('AS',1,'S','Persona','Asesor financiero','EMP','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")
VALUES('JM',1,'S','Persona','Jefe de Mesa de Dinero','EMP','N','ACT','UI_OPERADORES');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('OM',1,'S','Persona','Operador de Mesa de Dinero','EMP','N','ACT','UI_OPERADORES');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('CY',1,'S','Persona','Conyuge','CY','S','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('REP',1,'S','Persona','Representante Legal','EMP','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")
VALUES('DIR',1,'S','Persona','Director','EMP','N','ACT','UI_EMPLEADO');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('GE',1,'S','Persona','Gerente','EMP','N','ACT','UI_EMPLEADO');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion") 
VALUES('AD',1,'S','Persona','Administrador','EMP','N','ACT','UI_EMPLEADO');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")
VALUES('OP',1,'S','Persona','Operadores','EMP','N','ACT','RELACIONADOS');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion" ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion")  
VALUES('OTP',1,'S','Persona','Otras personas','OTP','N','ACT','RELACIONADOS');

*/

INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('APO01','APO','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('AUT01','AUT','Persona','ACT');

----TipoRelacionDestino
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('GG01','GG','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('GG02','GG','Corredora','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('EMP01','EMP','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('EM01','EM','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('GA01','GA','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('GA02','GA','Corredora','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('AS01','AS','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('AS02','AS','Corredora','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('JM01','JM','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('JM02','JM','Corredora','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('OM01','OM','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('OM02','OM','Corredora','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('CY01','CY','Persona','ACT');--natural
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('REP01','REP','Corredora','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('REP02','REP','Persona','ACT');--Juridico
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('DIR01','DIR','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('DIR02','DIR','Corredora','ACT');--Juridico
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('GE01','GE','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('GE02','GE','Corredora','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('AD01','AD','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('AD02','AD','Corredora','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('OP01','OP','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('OP02','OP','Corredora','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('OTP01','OTP','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('AP01','APO','Persona','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" ( "IdTipoRelacionDestino","IdTipoRelacion","IdTipoEntidad","IdEstado" )  
VALUES('AU01','AUT','Persona','ACT');

























----TipoClasificacionRelacion (CLASIFICACION)
---  NCG380
INSERT INTO  "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion" ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado")  
VALUES('SOC','Socio','NCG380','ACT');
INSERT INTO  "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion" ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado")  
VALUES('DIR','Director','NCG380','ACT');
INSERT INTO  "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion" ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado")  
VALUES('TR','Trabajador','NCG380','ACT');
INSERT INTO  "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion" ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado")  
VALUES('MG','Miembro Grupo Empresarial','NCG380','ACT');
INSERT INTO  "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion" ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado")  
VALUES('OTR','Otra Relación','NCG380','ACT');
INSERT INTO  "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion" ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado")  
VALUES('SR','Sin Relación','NCG380','ACT');
---CIR1064
INSERT INTO  "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion" ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado")  
VALUES('NV','No vinculado al intermediario','CIR1064','ACT');
INSERT INTO  "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion" ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado")  
VALUES('VP','Vinculado por propiedad al intermediario','CIR1064','ACT');
INSERT INTO  "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion" ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado")  
VALUES('VL','Vinculado laboralmente al intermediario','CIR1064','ACT');


 ----- NormativaRelacionClasificacion (CLASIFICACION) -->Intermedia
---CIR1064
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(1,'NV','NR');     
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(2,'VP','C-PRO');     
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(3,'VL','GA');     
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(4,'VL','OP');  
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(5,'VL','APO');    
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(6,'VL','CY');    
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(7,'VL','APO');    

---  NCG380    
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(8,'SOC','C-SOC');  
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(9,'DIR','C-DIR');    
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(10,'TR','C-EM'); 
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(11,'TR','C-GG'); 
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(12,'TR','C-GA'); 
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(13,'TR','C-JM');      
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(14,'TR','C-OM'); 
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(15,'OTR','OTR');      
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion" ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion")  
VALUES(16,'SR','NR');      
  




----------	NormativaRelacion  (DIRECTA)
---CIR1237  
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacion" ( "IdNormativaRelacion","IdNormativa","IdTipoRelacion","IdEstado","DescripcionRelacion")  
VALUES(1,'CIR1237','GG','ACT','Gerente General');
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacion" ( "IdNormativaRelacion","IdNormativa","IdTipoRelacion","IdEstado","DescripcionRelacion")  
VALUES(2,'CIR1237','GA','ACT','Gerente de Area o Subgerente');
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacion" ( "IdNormativaRelacion","IdNormativa","IdTipoRelacion","IdEstado","DescripcionRelacion")  
VALUES(3,'CIR1237','JM','ACT','Jefe de Mesa de Dinero');
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacion" ( "IdNormativaRelacion","IdNormativa","IdTipoRelacion","IdEstado","DescripcionRelacion")  
VALUES(4,'CIR1237','OM','ACT','Operador de Mesa de Dinero');
INSERT INTO  "BSCL.SocioNegocio.BD::REL.NormativaRelacion" ( "IdNormativaRelacion","IdNormativa","IdTipoRelacion","IdEstado","DescripcionRelacion")  
VALUES(5,'CIR1237','OTR','ACT','Otra Relacion');




INSERT INTO "BSCL.SocioNegocio.BD::REL.GradoVinculacionParental"("IdGradoVinculacionParental","Nombre")
SELECT 1,'Abuel@s' from dummy union all
SELECT 2,'Padre' from dummy union all
SELECT 3,'Madre' from dummy union all
SELECT 4,'Herman@s' from dummy union all
SELECT 5,'Hij@s' from dummy union all
SELECT 6,'Espos@ o conyugue' from dummy union all
SELECT 7,'Tí@s carnales' from dummy union all
SELECT 8,'Tí@s abuel@s' from dummy union all
SELECT 9,'Prim@s carnales' from dummy union all
SELECT 10,'Tí@s segundos' from dummy union all
SELECT 11,'Prim@s carnales' from dummy union all
SELECT 12,'Prim@s segundos' from dummy union all
SELECT 13,'Sobrin@s carnales' from dummy union all
SELECT 14,'Sobrin@s segundos' from dummy union all
SELECT 15,'Suegr@s, Cuñad@s' from dummy union all
SELECT 16,'Yernos y Nueras' from dummy union all
SELECT 17,'Tí@s políticos' from dummy union all
SELECT 18,'Prim@s Políticos' from dummy union all
SELECT 19,'Nietos' from dummy union all
SELECT 20,'Sobrin@ Nietos' from dummy union all
SELECT 21,'Sobrin@os Nietos segundos' from dummy;


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
