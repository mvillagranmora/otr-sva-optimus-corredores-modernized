SET SCHEMA "BSCL";


call "BSCL.Plataforma.BD.Utilitarios::POCNuevaEmpresa" (
	p_IdPais						=> 166,
	p_IdEmpresa						=> 1313,
	p_CodigoEmpresa	   	 			=> 'EMPRESAPE',
	p_NombreEmpresa					=> 'EMPRESA PERÚ',
	p_RutEmpleadoMaster 			=> '16929326-0',
	p_IdEmpresaCopiarConfiguracion 	=> 1,
	p_SobreescribirEmpresa			=> 'S',
	p_out_Usuario 					=> ?,
	p_out_Clave 					=> ?,
	p_out_IdSocioNegocio			=> ?,
	p_out_IdPersona 				=> ?,
	p_out_IdEmpleado 				=> ?,
	p_out_Mensaje					=> ?
);
--commit;

--update  "BSCL"."BSCL.Plataforma.BD::PLA.Empresa"  
--set "Codigo" = 'CREDIPE',"Nombre" = 'CREDICORP CAPITAL PERU - DESAROLLO',"IdPais" = 166 where "IdEmpresa" = 1;

set schema bscl;

--Persona Mancomunada, agregar el idPais? para filtrar en combo?:
delete from "BSCL.SocioNegocio.BD::PER.TipoPersona" where "IdTipoPersona" = 'M';

INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoPersona" ("IdTipoPersona","Descripcion")
VALUES ('M','PERSONA MANCOMUNADA');

commit;

--habilitar division pais para Perú, esto habilita ver campo: Distrito en modal direccion fisica.
update "BSCL.Plataforma.BD::REG.Pais" set "UsaDivisionPais" = 'S' where "IdPais" = 166;	



--clasificacion fatca: VERIFICAR SI SON UNICOS PARA PERU, SE DEBERIA AGREGAR ID_PAIS o ID_EMPRESA
--select * from "BSCL.SocioNegocio.BD::PER.ClasificacionFatca";
insert into "BSCL.SocioNegocio.BD::PER.ClasificacionFatca" ("IdClasificacionFatca","Nombre")
select 'US_PERSON','US_PERSON' from dummy union all
select 'RESIDEEUU','RESIDENTES EEUU' from dummy;

-- Tipo de bloqueo temporal --validar si requiere "IdPais"
insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado")
select 1, 'BTEMPO','CLIENTE','BLOQUEO TEMPORAL','BLO','ACT' from dummy;

-- Tipo de bloqueo temporal --validar si requiere "IdPais"
insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado")
select 1, 'BTEMPO','CLIENTE','BLOQUEO TEMPORAL','BLO','ACT' from dummy;


delete from "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" where "IdTipoEntidad" = 'CLIENTE'
and "IdTipoBloqueo" = 'DESBTEMPORAL';
insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado")
select 1, 'DESBTEMPO','CLIENTE','DESBLOQUEO TEMPORAL','ACT','ACT' from dummy;
insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado")
select 1313, 'DESBTEMPO','CLIENTE','DESBLOQUEO TEMPORAL','ACT','ACT' from dummy;

select * from "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" where "IdTipoEntidad" = 'CLIENTE'

commit;

/*************
LOCALIZACION
*************/
--select * from "BSCL.Plataforma.BD::PLA.Configuracion" where "IdEmpresa" = 1313 and "CodigoParametro" in ('PLA.IdiomaSistema','PLA.LocalizacionSistema');

update "BSCL.Plataforma.BD::PLA.Configuracion" set "Valor" = 'PE' 
where "IdEmpresa" = 1313 and "CodigoParametro" in ('PLA.IdiomaSistema','PLA.LocalizacionSistema');


/*************
INDICADORES
*************/
SET SCHEMA BSCL;

--SELECT *  FROM "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" in (1011,1012,1013);
--SELECT *  FROM "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" in (1002,1003,1004,1005,1006,1007,1008,1009,1010);
--SELECT *  FROM "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" in (1014,1015,1016,1017,1018,1019,1020);
--delete FROM "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" in (1002,1003,1004,1005,1006,1007,1008,1009,1010);

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10002, 'CLIENTE', 1, 'N', 'CHECKBOX', 'FICHA DEL CLIENTE COMPLETA','DOC_FCCOMP', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10003, 'CLIENTE', 1, 'N', 'CHECKBOX', 'FORMULARIO DECLARACIÓN ORIGEN DE FONDOS','DOC_FDEDOF', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10004, 'CLIENTE', 1, 'N', 'CHECKBOX', 'FORMULARIO FATCA (SI ES US-PERSON)','DOC_FFATCA', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10005, 'CLIENTE', 1, 'N', 'CHECKBOX', 'FORMULARIO CONTRATO DE MEDIOS (INTERNET)','DOC_FCNMED', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10006, 'CLIENTE', 1, 'N', 'CHECKBOX', 'FORMULARIO NEGOCIACION MVE','DOC_FNEMVE', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10007, 'CLIENTE', 1, 'N', 'CHECKBOX', 'CONTRATO NEGOCIACION MVE','DOC_CNNMVE', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10008, 'CLIENTE', 1, 'N', 'CHECKBOX', 'CONTRATO NEGOCIACION MILA','DOC_CNMILA', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10009, 'CLIENTE', 1, 'N', 'CHECKBOX', 'CONTRATO NEGOCIACION DE VALORES EN EL SEGMENTO RIESGO','DOC_CNVSRI', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10010, 'CLIENTE', 1, 'N', 'CHECKBOX', 'CONTRATO NEGOCIACION DE MVE AGENTE PROMOTOR','DOC_CNMVEA', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10011, 'CLIENTE', 1, 'N', 'CHECKBOX', 'CONTRATO NEGOCIACION MERCADO ALTERNATIVO NAV','DOC_CNMANA', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10012, 'CLIENTE', 1, 'N', 'CHECKBOX', 'NEGOCIACIÓN DE BONOS ACREDITADOS','DOC_NEGOBA', 'DOCUMENTACION', 'REQ', 'ACT', 0, 0, 0, 'N' );

--ORDENES
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10013, 'PORTAFOLIO', 1, 'N', 'NVARCHAR', 'MODALIDAD DE ÓRDENES','ORDMODORDN', 'GRUPOORDENES', 'PRE', 'ACT', 0, 0, 0, 'N' );

-- INDICADORES DE PERSONA; VALIDAR CONDICIONALIDAD QUE MENCIONAN EN RQ
set schema bscl;
--delete from "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" in (1014,1015,1016,1017,1018,1019,1020);

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
VALUES (10014, 'PERSONA', 1, '', 'COMBO', 'Nivel de Experiencia en MV','GENNIVEXPE', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','A' );
	--DETALLE!
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,10014,'NINGUNO','Ninguno');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,10014,'POCO','Poco');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,10014,'REGULAR','Regular');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,10014,'SICONOCE','Sí conoce');
	
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
VALUES (10015, 'PERSONA', 1, '', 'COMBO', 'Primera Relación con CCB','GENPRIMREL', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','A' );
	--DETALLE!
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,10015,'VINCULADO','Vinculado o relacionado al AI');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,10015,'NOVINCULADO','No vinculado al AI');

select * FROM "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" = 10016;

DELETE FROM "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoIndicador" = 10016;

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
VALUES (10016, 'PERSONA', 1, '', 'COMBO', 'Tipo de Primera Relación con CCB','GENTIPPRIR', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','NM' );
	--DETALLE!
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,10016,'01','Accionista, Director o Gerente del AI o de empresas de su Grupo Económico');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,10016,'02','Representante del AI');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,10016,'03','Funcionario de Control Interno, funcionario o personal del AI o de alguna empresa de su Grupo Económico');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,10016,'04','Pariente de Accionista, Director, Gerente, Representante, Funcionario de Control Interno o de algún funcionario o personal del AI');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (5,10016,'05','Otro tipo de vinculación o relación con el AI o con algún Accionista, Director, Gerente, Representante, Funcionario de Control Interno o Personal del AI');
	/*INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (6,10016,'06','');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (7,10016,'07','');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (8,10016,'08','');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (9,10016,'09','');*/




insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
VALUES (10026, 'PERSONA', 1, '', 'COMBO', 'Tipo de Primera Relación con CCB','GENTIPPRIRJUR', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','J' );
INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
VALUES (10,10026,'10','Conformante del mismo grupo económico');
INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
VALUES (11,10026,'11','Otro tipo de vinculación');
INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
VALUES (12,10026,'12','Cuenta propia');


insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
VALUES (10017, 'PERSONA', 1, '', 'COMBO', 'Segunda Relación con CCB','GENSEGUREL', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','A' );
	--DETALLE!
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,10017,'CONCONTRATOADMIN','Con Contrato de Administración');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,10017,'SINCONTRATOADMIN','Sin Contrato de Administración');

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
VALUES (10018, 'PERSONA', 1, '', 'COMBO', 'Identificador de protección de datos','GENPROTDAT', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','A' );
	--DETALLE!
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,10018,'SI','Si');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,10018,'NO','No');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,10018,'CAN','CAN');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,10018,'VAC','VAC');

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
VALUES (10019, 'PERSONA', 1, '', 'NVARCHAR', 'Cargo Público','GENCARGPUB', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion","IdTipoPersona" ) 
VALUES (10020, 'PERSONA', 1, '', 'NVARCHAR', 'Tipo Entidad SMV','GENTIENSMV', 'GENERALES', 'IND', 'ACT', 0, 0, 0, 'N','J' );  -- agregar tipo JM (juridica mancomunada) y combinaciones



--PEP FALTANTE:
set schema bscl;
insert into "BSCL.Plataforma.BD::IND.TipoIndicador"("IdTipoIndicador","IdTipoEntidad","IdEmpresa","ValorIndicadorDefecto","ClaseDato","NombreTipoIndicador","GrupoIndicador","CantidadDecimales","LongitudMinima","LongitudMaxima","IdUso","IdEstado","RequiereAutorizacion","CodigoTipoIndicador","IdTipoPersona","RequiereFechaVencimiento","IdCarga","IdIndicadorMigracion")
VALUES(10027,'PERSONA','1','N','CHECKBOX','FUNCION PÚBLICA','PEP','0','0','0','IND','ACT','N','REL_PEP','NJ','N','0','0');
commit;
--select * from "BSCL.Plataforma.BD::IND.TipoIndicador" where "CodigoTipoIndicador"='PEP';
--select * from "BSCL.Plataforma.BD::IND.TipoIndicador" where "GrupoIndicador"='PEP';
------------------------------------
--Modelo bloqueo automático cliente 
------------------------------------
set schema bscl;
drop table "BSCL.SocioNegocio.BD::CLI.BloqueoAutomaticoClientes";
drop table "BSCL.SocioNegocio.BD::CLI.DetalleBloqueoAutomaticoClientes";

create table "BSCL.SocioNegocio.BD::CLI.BloqueoAutomaticoClientes"(
	--"BloqueoAutomaticoClientes" 				bigint not null,
	"IdEmpresa"									bigint not null,
	"IdEstado" 				 					nvarchar(3) default 'ACT' not null,
	"BloqueoCumplirMayoriaEdad"					bigint not null default 0, --Años(edad)
	"BloqueoFichaDesactualizada"				bigint not null default 0, --Meses
	"BloqueoPortafolioSinOperaciones"			bigint not null default 0, --Meses
    "FechaCreacion"     						seconddate default current_utctimestamp not null,
   	"FechaModificacion"  						seconddate default '0001-01-01 00:00:00' not null,
   	--primary key ("IdConfiguracionBloqueoAutomaticoClientes")
   	primary key ("IdEmpresa")
);

--alter table "BSCL.SocioNegocio.BD::CLI.BloqueoAutomaticoClientes"
--alter("FechaCreacion" seconddate default current_utctimestamp not null);

create table "BSCL.SocioNegocio.BD::CLI.DetalleBloqueoAutomaticoClientes"(
	"IdEmpresa" 								bigint not null,
	"IdTipoDocumento" 							nvarchar(40) not null,
	"IdEstado" 				 					nvarchar(3) default 'INA' not null,
   primary key ("IdEmpresa","IdTipoDocumento")
);

--select * from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes"

set schema bscl;
--select * from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"  where "RutaServicio" = 'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes';
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") 
values (3500,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes' , 1,  'Q', '','','');

--select * from  "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio"=20023;
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20023,'PLUGIN_SOC.jar@cl.bcs.socionegocio.cli.ModificarConfiguracionBloqueoAutomaticoClientes',0,'J','','','');

commit;

set schema bscl;
call "BSCL.Plataforma.BD.Servicios::CrearPojo"(
	p_nombreTabla => 'BSCL.SocioNegocio.BD::CLI.BloqueoAutomaticoClientes',
	p_nombrePojo => 'BloqueoAutomaticoClientes', 
	p_tipo => 'T', 
	p_out_pojo => ?
);
call "BSCL.Plataforma.BD.Servicios::CrearPojo"(
	p_nombreTabla => 'BSCL.SocioNegocio.BD::CLI.DetalleBloqueoAutomaticoClientes',
	p_nombrePojo => 'DetalleBloqueoAutomaticoClientes', 
	p_tipo => 'T', 
	p_out_pojo => ?
);

------
-- BLOQUEO AUTOMATICO

set schema bscl;
-- Tipo bloqueo automatico
insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado")
select 1, 'BAUTOM','CLIENTE','BLOQUEO AUTOMÁTICO','BLO','ACT' from dummy;
-- Tipo desbloqueo automatico
insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado")
select 1, 'AAUTOM','CLIENTE','ACTIVACIÓN AUTOMÁTICA','ACT','ACT' from dummy;
--
commit;


set schema bscl;
select * from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = 'BLOQUEO_AUTOMATICO_CLIENTES';

--BLOQUEO AUTOMATICO DE CLIENTES
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
insert into #param values('IdUsuario','37');
insert into #param values('SimularOperacion','N');

call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'BLOQUEO_AUTOMATICO_CLIENTES',
	p_Descripcion			=> 'Pauta de bloqueo automatico clientes',
	p_NombreClasePrograma	=> 'cl.bcs.socionegocio.jobs.PautaBloqueoAutomaticoClientes',
	p_Nodo					=> '',
	p_IdModulo				=> 'SOC',
	p_Hora					=> '21',
	p_Minuto				=> '00',
	p_DependenciaAnterior	=> '',
	p_DependenciaProximo	=> '',
	p_Parametros			=> #param);
	commit;

--ACTIVACION AUTOMATICA DE CLIENTES
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
insert into #param values('IdUsuario','37');
insert into #param values('SimularOperacion','N');

call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'ACTIVACION_AUTOMATICA_CLIENTES',
	p_Descripcion			=> 'Pauta de activacion automatica clientes',
	p_NombreClasePrograma	=> 'cl.bcs.socionegocio.jobs.PautaActivacionAutomaticaClientes',
	p_Nodo					=> '',
	p_IdModulo				=> 'SOC',
	p_Hora					=> '21',
	p_Minuto				=> '05',
	p_DependenciaAnterior	=> '',
	p_DependenciaProximo	=> '',
	p_Parametros			=> #param);
	commit;	
	

set schema bscl;

select * from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes";



select 
	cli."IdCliente",
	cli."IdEstado" as "IdEstadoCliente",
	conf."IdTipoDocumento",
	doc."IdDocumento",
	doc."IdUltimoAnexo",
	anx."IdEstado" as "IdEstadoAnexo",
	ANX."FechaVigencia"
from "BSCL.SocioNegocio.BD::CLI.Cliente" cli 
join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes" conf
	on cli."IdEmpresa" = conf."IdEmpresa"
left join "BSCL.Plataforma.BD::DOC.Documento" doc 
	on doc."IdTipoDocumento" = conf."IdTipoDocumento" and doc."IdCliente" = cli."IdCliente"
left join "BSCL"."BSCL.Plataforma.BD::DOC.Anexo" anx 
	on anx."IdDocumento" = DOC."IdDocumento" and anx."IdAnexo" = doc."IdUltimoAnexo"
where cli."IdEmpresa" = 1 and conf."IdEstadoTipoDocumento"='ACT'
order by cli."IdCliente",conf."IdTipoDocumento"
	
	
update "BSCL.SocioNegocio.BD::CLI.Cliente" 
set "IdEstado"='BLO',"IdTipoBloqueo"=:IdTipoBloqueo,
	"ComentarioBloqueo"=:ComentarioBloqueo,
	"FechaBloqueo"=CURRENT_UTCTIMESTAMP,"FechaModificacion"=CURRENT_UTCTIMESTAMP
where "IdCliente" in (:ListaIdCliente)


update "BSCL.SocioNegocio.BD::CLI.Cliente" 
set "IdEstado"='ACT' where "IdCliente" in (524891);
commit;

rollback;	
select * from "BSCL.SocioNegocio.BD::CLI.Cliente" where "IdCliente" = 524891



--BLOQUEOS/DESBLOQUEO TEMPORAL
set schema bscl;

alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
add("FechaBloqueoTemporal" SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL);

alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
add("FechaDesbloqueoTemporal" SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL);

commit;
--


update "BSCL.SocioNegocio.BD::CLI.Cliente"  
set "IdEstado" = 'ACT',"IdTipoDesbloqueo"='ATEMPO' 
where "IdCliente" = 524891;
commit;

--BLOQUEAR PROBAR
set schema bscl;
update "BSCL.SocioNegocio.BD::CLI.Cliente"  
set "IdEstado" = 'BLO',"IdTipoBloqueo"='BTEMPO',
"FechaBloqueo"='2017-04-01 00:00:00',"FechaBloqueoTemporal"='2017-05-01 00:00:00' 
where "IdCliente" = 524891;
commit;
-- BLOQUEO CONSULTAR
select "IdCliente","FechaBloqueo","FechaBloqueoTemporal" 
from "BSCL.SocioNegocio.BD::CLI.Cliente" 
where "IdEstado" = 'BLO' and "IdTipoBloqueo"='BTEMPO' 
and "IdEmpresa" = 1 and "FechaBloqueoTemporal"<='2017-05-02'


--DESBLOQUEAR PROBAR
set schema bscl;
update "BSCL.SocioNegocio.BD::CLI.Cliente"  
set "IdEstado" = 'ACT',"IdTipoDesbloqueo"='ATEMPO',
"FechaDesbloqueo"='2017-04-01 00:00:00',
"FechaDesbloqueoTemporal"='2017-05-01 00:00:00' 
where "IdCliente" = 524891;
commit;
-- DESBLOQUEO CONSULTAR
select "IdCliente","FechaBloqueo","FechaBloqueoTemporal" 
from "BSCL.SocioNegocio.BD::CLI.Cliente" 
where "IdEstado" = 'ACT' and "IdTipoDesbloqueo"='ATEMPO' 
and "IdEmpresa" = 1 and "FechaDesbloqueoTemporal"<='2017-05-02'

/*
select top 10 * from "BSCL.Plataforma.BD::PLA.SolicitudCambio" order by "IdSolicitudCambio" desc;
call "BSCL.SocioNegocio.BD.Servicios::CLI.ModificarEstadoCliente" (
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 1,
	p_IdModulo						=> 'SOC',
	p_UsaReglasNegocio	 			=> 'S',
	p_IdCliente						=> 524891,
	p_IdEstado						=> 'BLO',
	p_IdTipo 						=> 'BTEMPO',
	p_Fecha 						=> '2017-05-02 20:00:00',
	p_FechaHasta					=> '2017-05-07 20:00:00',
	p_Comentario 					=> 'ASD',
	p_out_RefId 					=> ?,
	p_out_AdvertenciasContador 		=> ?,
	p_out_AdvertenciasDetalle 		=> ?,
	p_out_ErroresContador 			=> ?,
	p_out_ErroresDetalle 			=> ?
);*/


-- PAUTA BLOQUEAR 
set schema bscl;
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
VALUES('BLOQUEO_AUTOMATICO_CLIENTES', 'Bloqueo automático de clientes', 1, 'PLATAFORMA', 'cl.bcs.socionegocio.jobs.PautaBloqueoAutomaticoClientes', 'JAVA', '','S');
DROP table #parametros;
create local temporary table #parametros("NombreParametro" nvarchar(100), "Valor" nvarchar(100));
INSERT INTO #parametros Select 'IdEmpresa' as "NombreParametro", '1' as "Valor" from dummy;
INSERT INTO #parametros Select 'IdUsuario' as "NombreParametro", '37' as "Valor" from dummy;
INSERT INTO #parametros Select 'SimularOperacion' as "NombreParametro", 'N' as "Valor" from dummy;
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa  =>  1,
	p_IdUsuario  =>  37,
	p_IdModulo   =>  'PLA',
	p_IdTipoTarea  =>  'BLOQUEO_AUTOMATICO_CLIENTES',
	p_EsDemonio    =>  'N'  ,
	p_DiaDeMes    =>  '**',
	p_DiaDeSemana  =>  '*',
	p_Mes      =>  '**',
	p_Hora      =>  '20',
	p_Minuto    =>  '15',
	p_SoloDiasHabiales  => 'S',
	p_SoloPrincipioMes  => 'N',
	p_SoloFinDeMes    => 'N',
	p_habilitado    =>  'S', 
	p_TablaParametros  =>  #parametros,              
	p_IdAgenda    => ?,
	p_IdPrograma    => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?              
);	 
commit;

-- PAUTA DESBLOQUEAR 
set schema bscl;
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
VALUES('DESBLOQUEO_AUTOMATICO_CLIENTES', 'Desbloqueo automático de clientes', 1, 'PLATAFORMA', 'cl.bcs.socionegocio.jobs.PautaDesbloqueoAutomaticoClientes', 'JAVA', '','S');
DROP table #parametros;
create local temporary table #parametros("NombreParametro" nvarchar(100), "Valor" nvarchar(100));
INSERT INTO #parametros Select 'IdEmpresa' as "NombreParametro", '1' as "Valor" from dummy;
INSERT INTO #parametros Select 'IdUsuario' as "NombreParametro", '37' as "Valor" from dummy;
INSERT INTO #parametros Select 'SimularOperacion' as "NombreParametro", 'N' as "Valor" from dummy;
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa  =>  1,
	p_IdUsuario  =>  37,
	p_IdModulo   =>  'PLA',
	p_IdTipoTarea  =>  'DESBLOQUEO_AUTOMATICO_CLIENTES',
	p_EsDemonio    =>  'N'  ,
	p_DiaDeMes    =>  '**',
	p_DiaDeSemana  =>  '*',
	p_Mes      =>  '**',
	p_Hora      =>  '20',
	p_Minuto    =>  '20',
	p_SoloDiasHabiales  => 'S',
	p_SoloPrincipioMes  => 'N',
	p_SoloFinDeMes    => 'N',
	p_habilitado    =>  'S', 
	p_TablaParametros  =>  #parametros,              
	p_IdAgenda    => ?,
	p_IdPrograma    => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?              
);	 
commit;

/*******************
	DICCIONARIO
*******************/	
set schema bscl;

delete from "BSCL.Plataforma.BD::PLA.Diccionario" where  "CodLenguaje" = 'PE';

-- BORRAR:
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ACTIVACION_MASIVA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_AUTOMATICO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_AUTOMATICO_CLIENTES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_AUTOMATICO_VENCIMIENTO_DOCUMENTOS' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_AÑOS' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ANOS' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_MESES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_CUMPLIR_MAYORIA_EDAD' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_FICHA_DESACTUALIZADA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_PORTAFOLIO_SIN_OPERACIONES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_APERTURA_MODULO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_GESTION_FIX' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ENVIO_FACTURACION' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_CONSULTA_AVANZADA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_CONSULTA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ACTIVACION_MASIVA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_AUTOMATICO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_AUTOMATICO_CLIENTES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_JUNTA_ACCIONISTAS' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_SUSPENDER_TEMPORAL' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_FECHA_INICIO_SUSPENSION' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_FECHA_TERMINO_SUSPENSION' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_SUCURSAL_PREFERENCIA_CLIENTE' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_PARTICIPACION_MAYOR' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_REFERENCIA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_TIPO_BANCO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_IDENTIFICADOR_BANCO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_NACIONALIDAD_SECUNDARIA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_GRADO_INSTRUCCION' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_GIRO_CIIU' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_RESIDENCIA_SECUNDARIA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_DIRECCION_SECUNDARIA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ANOS_SERVICIO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_INGRESO_PROMEDIO_MENSUAL' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ACTIVACION_MASIVA_CLIENTES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_INFORMACION_INTERMEDIARIO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_RANGO_PATRIMONIO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_GRADO_VINCULACION' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_CONDICIONES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_CLASIFICACION_EMPRESA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_NUMERO_ADICIONAL' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_NUMERO_IDENTIDAD' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_MODALIDAD_DE_ORDENES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ENVIO_FACTURA_POR_FISICO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_NUMERO_REGISTRO_PUBLICO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ASIENTO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_FICHA_O_PARTIDA' and "CodLenguaje"='CL';

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ACTIVACION_MASIVA','CL','Activación masiva','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_AUTOMATICO','CL','Bloqueo automático','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_AUTOMATICO_CLIENTES','CL','Bloqueo automático de clientes','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_AUTOMATICO_VENCIMIENTO_DOCUMENTOS','CL','Bloqueo automático por vencimiento de documentos','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_AÑOS','CL','años','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ANOS','CL','años','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MESES','CL','meses','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_CUMPLIR_MAYORIA_EDAD','CL','Bloqueo tras cumplir mayoría de edad','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_FICHA_DESACTUALIZADA','CL','Bloqueo ficha desactualizada','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_PORTAFOLIO_SIN_OPERACIONES','CL','Bloqueo portafolio sin operaciones','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_APERTURA_MODULO','CL','Apertura de módulo','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_GESTION_FIX','CL','Gestión FIX','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ENVIO_FACTURACION','CL','Envío Facturación','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CONSULTA_AVANZADA','CL','Consulta avanzada','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CONSULTA','CL','Consulta','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ACTIVACION_MASIVA','CL','Activación masiva','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_AUTOMATICO','CL','Bloqueo automático','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_AUTOMATICO_CLIENTES','CL','Bloqueo automático de clientes','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_JUNTA_ACCIONISTAS','CL','Junta de accionistas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_SUSPENDER_TEMPORAL','CL','Suspender temporalmente','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_FECHA_INICIO_SUSPENSION','CL','Inicio suspensión','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_FECHA_TERMINO_SUSPENSION','CL','Término suspensión','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_SUCURSAL_PREFERENCIA_CLIENTE','CL','Sucursal preferencia cliente','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PARTICIPACION_MAYOR','CL','Participación mayor','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_REFERENCIA','CL','Referencia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_TIPO_BANCO','CL','Tipo de banco','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_IDENTIFICADOR_BANCO','CL','Identificador de banco','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_NACIONALIDAD_SECUNDARIA','CL','Nacionalidad secundaria','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_GRADO_INSTRUCCION','CL','Grado de instrucción','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_GIRO_CIIU','CL','Giro CIIU','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_RESIDENCIA_SECUNDARIA','CL','Residencia secundaria','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIRECCION_SECUNDARIA','CL','Dirección secundaria','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ANOS_SERVICIO','CL','Años de servicio','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_INGRESO_PROMEDIO_MENSUAL','CL','Ingreso promedio mensual','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ACTIVACION_MASIVA_CLIENTES','CL','Activación masiva de clientes','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_INFORMACION_INTERMEDIARIO','CL','Información intermediario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_RANGO_PATRIMONIO','CL','Rango patrimonio','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_GRADO_VINCULACION','CL','Grado de vinculación','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CONDICIONES','CL','Condiciones','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CLASIFICACION_EMPRESA','CL','Clasificación empresa','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_NUMERO_ADICIONAL','CL','Número adicional','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_NUMERO_IDENTIDAD','CL','Número de entidad','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MODALIDAD_DE_ORDENES','CL','Modalidad de órdenes','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ENVIO_FACTURA_POR_FISICO','CL','Envío factura por físico','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_NUMERO_REGISTRO_PUBLICO','CL','Número de registro público','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ASIENTO','CL','Asiento','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_FICHA_O_PARTIDA','CL','Ficha o partida','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MOTIVO_ACTIVACION_CLIENTE','CL','Motivo activación cliente','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MOTIVO_ACTIVACION_PORTAFOLIO','CL','Motivo activación portafolio','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ACTIVAR','CL','Activar','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_NUMERO_SECUNDARIO','CL','Número Secundario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_NUMERO_IDENTIDAD','CL','Número Identidad','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_UBIGEO','CL','Ubigeo','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CODIGO_COMPUESTO_DIRECCION_FISICA','CL','Ubigeo','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CODIGO_ADICIONAL_HOLDING','CL','Código adicional Holding','');

insert into "BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario","CodLenguaje","Descripcion","Abreviado")
select "CodDiccionario",'PE',"Descripcion","Abreviado"  from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodLenguaje" = 'CL';

update "BSCL.Plataforma.BD::PLA.Diccionario" set "Descripcion" = replace("Descripcion",'Tipo Cliente','Clase Cliente') 
where "CodLenguaje" = 'PE' and  "Descripcion" like '%Tipo Cliente%';

update "BSCL.Plataforma.BD::PLA.Diccionario" set "Descripcion" = replace("Descripcion",'Agente','Asesor Comercial') 
where "CodLenguaje" = 'PE' and  "Descripcion" like '%Agente%';

commit;
-- actualizar diccionario: UI_NACIONALIDAD_PRINCIPAL = 'NACIONALIDAD PRINCIPAL'


drop table "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO";
CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO" (
 "IdTipoCliente" 			NVARCHAR(40)	NOT NULL ,
 "Nombre"					NVARCHAR(100)	NOT NULL DEFAULT '',
 "IdUsuarioCreador"			BIGINT			NOT NULL DEFAULT 0,
 "FechaCreacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaModificacion" 		SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
 "IdEmpresa" 				BIGINT  		NOT NULL,
 "IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL);
 insert into "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO" select * from "BSCL.SocioNegocio.BD::CLI.TipoCliente";

select * from "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO";
commit;

 drop table "BSCL.SocioNegocio.BD::CLI.TipoCliente";
 CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::CLI.TipoCliente" (
 "IdTipoCliente" 			NVARCHAR(40)	NOT NULL ,
 "Nombre"					NVARCHAR(100)	NOT NULL DEFAULT '',
 "IdUsuarioCreador"			BIGINT			NOT NULL DEFAULT 0,
 "FechaCreacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
 "FechaModificacion" 		SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
 "IdEmpresa" 				BIGINT  		NOT NULL,
 "IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
 primary key("IdTipoCliente","IdEmpresa"));
 insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente" select * from "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO";

select * from "BSCL.SocioNegocio.BD::CLI.TipoCliente";
commit;

drop table "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO"; 
select * from "BSCL.SocioNegocio.BD::CLI.TipoClienteRESPALDO";
 
 

delete from "BSCL.SocioNegocio.BD::CLI.TipoCliente" where "IdEmpresa" = 1313;
insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente" ("IdTipoCliente","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
select 'INSTITUCIONAL','INSTITUCIONAL',1,'2017-01-26 00:00:00','2017-01-26 00:00:00',1313,'ACT' from dummy union all
select 'INVERSIONISTA','INVERSIONISTA ACREDITADO',1,'2017-01-26 00:00:00','2017-01-26 00:00:00',1313,'ACT' from dummy union all
select 'BROKERAGE','BROKERAGE',1,'2017-01-26 00:00:00','2017-01-26 00:00:00',1313,'ACT' from dummy union all
select 'CUSTODIO','CUSTODIO',1,'2017-01-26 00:00:00','2017-01-26 00:00:00',1313,'ACT' from dummy union all
select 'PATRIMONIOAUTONOMO','PATRIMONIO AUTÓNOMO',1,'2017-01-26 00:00:00','2017-01-26 00:00:00',1313,'ACT' from dummy;

update "BSCL.Plataforma.BD::REG.Pais" set "NombreDivisionPais" = 'Distrito' where "IdPais" = 166;

set schema bscl;
alter table "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" alter ("IdTipoDocumentoIdentidad" nvarchar(3));
/*
01: Cliente es Institucional
02: Cliente es Inversionista Acreditado
03: Cliente es Broker Extranjero
04: Cliente es Custodio
05: Cliente es Patrimonio Autónomo
*/
--update "BSCL.Plataforma.BD::PLA.Configuracion" set "Valor" = 'PE' where "IdEmpresa" = 1 and "CodigoParametro" in ('PLA.IdiomaSistema','PLA.LocalizacionSistema');

insert into "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad"
("IdTipoDocumentoIdentidad","NombreTipoDocumentoIdentidad","NombreAbrTipoDocumentoIdentidad","IdPais","PorDefecto","IdEstado")
select 'DNI','DOCUMENTO NACIONAL DE IDENTIDAD','DNI',166,'N','ACT' from dummy union all
select 'RUC','REGISTRO ÚNICO CONTRIBUYENTE','RUC',166,'N','ACT' from dummy union all
select 'CE','CARNÉ DE EXTRANJERÍA','CE',166,'N','ACT' from dummy union all
select 'PAS','PASAPORTE','PAS',166,'N','ACT' from dummy union all
select 'CDI','CÉDULA DIPLOMÁTICA DE IDENTIDAD','CDI',166,'N','ACT' from dummy union all
--select 'CIN','PASAPORTE','PAS',166,'N','ACT' from dummy union all
select 'IUP','IDENTIFICADOR UNICO PRINCIPAL','IUP',166,'S','ACT' from dummy union all
select 'RUT','REGISTRO ÚNICO DEL TITULAR (CAVALI)','RUT',166,'N','ACT' from dummy;

update "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" set "PorDefecto" = 'N' where "IdTipoDocumentoIdentidad" = 'RUT';


/*
delete from "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" where "IdPais" = 166;
insert into "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad"
("IdTipoDocumentoIdentidad","NombreTipoDocumentoIdentidad","NombreAbrTipoDocumentoIdentidad","IdPais","PorDefecto","IdEstado")
select 'DNI','DNI','DNI',166,'N','ACT' from dummy union all
select 'RUC','RUC','RUC',166,'N','ACT' from dummy union all
select 'CE','CARNÉ EXTRANJERÍA','CE',166,'N','ACT' from dummy union all
select 'PAS','PASAPORTE','PAS',166,'N','ACT' from dummy union all
select 'CDI','CDI','CDI',166,'N','ACT' from dummy union all
--select 'CIN','PASAPORTE','PAS',166,'N','ACT' from dummy union all
select 'IUP','IUP','IUP',166,'S','ACT' from dummy union all
select 'RUT','RUT CAVALI','RUT',166,'N','ACT' from dummy;
commit;
*/

---------------- 
-- 13/02/2017 --

alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
alter ( "IdTipoDocumentoIdentidad" NVARCHAR(3) not null);

alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
add ( "IdTipoDocumentoIdentidadAdicional" NVARCHAR(3) not null default '');

alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
add ( "DocumentoIdentidadAdicional" NVARCHAR(40) not null default '');

alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
add ( "CLI_CodigoAdicionalHolding" NVARCHAR(40) not null default '');

alter table "BSCL.SocioNegocio.BD::CLI.Cliente" 
add ("CodigoAdicionalHolding"	NVARCHAR(40) NOT NULL DEFAULT '');

alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento" 
add ("CLI_IdEmpleadoEjecutivo"	BIGINT NOT NULL DEFAULT 0);


drop type "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento";
CREATE TYPE "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento" AS TABLE (
"IdEnrolamiento"			BIGINT,
"IdEmpresa" 				BIGINT,
"IdUsuario" 				BIGINT,
"IdTipoEntidad" 			NVARCHAR(100),
"IdEntidadEnrolada" 		BIGINT,
"IdTipoDocumentoIdentidad"  NVARCHAR(3),
"DocumentoIdentidad" 		NVARCHAR(40),
"IdTipoDocumentoIdentidadAdicional" NVARCHAR(3) null default '',
"DocumentoIdentidadAdicional" NVARCHAR(40) null default '',
"Tipo" 						NVARCHAR(2),
"Nombres" 					NVARCHAR(100),
"ApellidoPaterno" 			NVARCHAR(100),
"ApellidoMaterno" 			NVARCHAR(100),
"RazonSocial" 				NVARCHAR(100),
"IdPaisNacimiento" 			BIGINT,
"FechaNacimiento" 			DATE,
"Sexo" 						NVARCHAR(2),
"EstadoCivil" 				NVARCHAR(2),
"IdCiudad" 					BIGINT,
"IdDivisionPais" 			BIGINT,
"IdTipoTelefono" 			NVARCHAR(40),
"Telefono" 					NVARCHAR(100),
"Email" 					NVARCHAR(100),
"DireccionCalle" 			NVARCHAR(100),
"DireccionNumero" 			NVARCHAR(40),
"DireccionDepartamento" 	NVARCHAR(40),
"FechaCreacion" 			SECONDDATE,
"FechaModificacion" 		SECONDDATE,
"IdPersonaConyuge"          BIGINT CS_FIXED  NULL DEFAULT 0,
"RegimenConyugal"           NVARCHAR(2) NULL DEFAULT '',
"CLI_IdCategoriaInversionista" 	NVARCHAR(40),
"CLI_IdTipoCliente"				NVARCHAR(40),
"CLI_IdEmpleadoEjecutivo"   	BIGINT,
"CLI_CodigoAdicionalHolding"	NVARCHAR(40) null default '',
"POR_IdPerfilComercial" 		NVARCHAR(40), 
"EMP_CodigoEmpleado"			NVARCHAR(40),
"EMP_CodigoTipoCargoEmpleado"	NVARCHAR(6),
"EMP_IdEmpleadoSupervisor"		BIGINT,
"EMP_IdSucursal"				BIGINT,
"EMP_FechaInicioCargo"			DATE,
"EMP_FechaTerminoCargo" 		DATE NULL DEFAULT '',
"CNT_CodigoInscripcion" 		NVARCHAR(40),
"CNT_Emisor" 					NVARCHAR(1),
"CNT_Depositos"					NVARCHAR(1),
"CNT_Bolsa" 					NVARCHAR(1),
"CNT_Corredora" 				NVARCHAR(1),
"CNT_CamaraDeCompensacion" 		NVARCHAR(1),
"CNT_Banco" 					NVARCHAR(1),
"CNT_Boveda" 					NVARCHAR(1),
"PRO_IdAreaProveedor"			BIGINT,
"EMP_CargoIndefinido"			NVARCHAR(1) NULL DEFAULT ''
--,"CLI_IdTipoEnrolamiento"		NVARCHAR(40)
 ); 

alter table "BSCL.SocioNegocio.BD::PER.Persona" add("IdGradoInstruccion" bigint default 0);
alter table "BSCL.SocioNegocio.BD::PER.Persona" add("AgnosServicio" int default 0);
alter table "BSCL.SocioNegocio.BD::PER.Persona" add("IngresoPromedioMensual" decimal(26,8) default 0);
alter table "BSCL.SocioNegocio.BD::PER.Persona" add("IdDocumentoIdentidadAdicional"	BIGINT);

--**********************************************

-- va?
--alter table "BSCL.SocioNegocio.BD::PER.Persona" add("IdGiroCIIU" bigint default 0);

CREATE COLUMN TABLE "BSCL.SocioNegocio.BD::PER.GradoInstruccion" (
	 "IdGradoInstruccion" 	BIGINT,
	 "Codigo" 	 			NVARCHAR(40) NOT NULL DEFAULT '' ,
	 "Nombre" 	 			NVARCHAR(100) NOT NULL DEFAULT '' ,
	 "IdPais"				BIGINT NOT NULL DEFAULT 0,
	 PRIMARY KEY ("IdGradoInstruccion")
);

--la vista
SET SCHEMA "BSCL";
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarGradoInstruccion"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarGradoInstruccion" AS
	select "IdGradoInstruccion","Codigo","Nombre","IdPais"	
	from "BSCL.SocioNegocio.BD::PER.GradoInstruccion";
--WITH READ ONLY; 
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarGradoInstruccion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

--select * from "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio"=3501;
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") 
values (3501,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarGradoInstruccion' , 1,  'Q', '','','');

commit;

-- en requiere autorizacion se agrega idEmpresa y se utiliza como llave

drop table "BSCL.Plataforma.BD::PLA.RequiereAutorizacionPASO";

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.RequiereAutorizacionPASO" (
   "IdConcepto"				NVARCHAR(6)     not null,
   "RequiereAutorizacion"	NVARCHAR(1)		not null,
   "IdEmpresa"				BIGINT 			not null default 0,
   PRIMARY KEY("IdConcepto","IdEmpresa")
);

insert into "BSCL.Plataforma.BD::PLA.RequiereAutorizacionPASO" 
select "IdConcepto","RequiereAutorizacion",1 from "BSCL.Plataforma.BD::PLA.RequiereAutorizacion"; 

drop table "BSCL.Plataforma.BD::PLA.RequiereAutorizacion";
CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" (
   "IdConcepto"				NVARCHAR(6)     not null,
   "RequiereAutorizacion"	NVARCHAR(1)		not null,
   "IdEmpresa"				BIGINT 			not null default 0,
   PRIMARY KEY("IdConcepto","IdEmpresa")
);
insert into "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" 
select "IdConcepto","RequiereAutorizacion",1 from "BSCL.Plataforma.BD::PLA.RequiereAutorizacionPASO";

insert into "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" 
select "IdConcepto","RequiereAutorizacion",1313 from "BSCL.Plataforma.BD::PLA.RequiereAutorizacionPASO";

drop table "BSCL.Plataforma.BD::PLA.RequiereAutorizacionPASO";

select * from "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" 

--select * from "BSCL.Plataforma.BD::REG.Ciudad" where "IdPais" = 166; --1821
--select * from "BSCL.Plataforma.BD::REG.DivisionPais" where "IdPais" = 166 and "IdCiudad" = 0;
update "BSCL.Plataforma.BD::REG.DivisionPais" set "IdCiudad" = 1821 where "IdPais" = 166 and "IdCiudad" = 0;

alter table "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
alter( "IdTipoDocumentoIdentidad"		NVARCHAR(3) DEFAULT '0' NOT NULL);

/*select 'alter table "' || tc.table_name || '" alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) '|| 
case tc.is_nullable when 'FALSE' then 'NOT NULL DEFAULT ''''' else 'NULL' end || ');' as "--LE_ALTER" 
--, *
from table_columns tc join sys.tables t on t.table_name = tc.table_name and t.schema_name = tc.schema_name
where tc.schema_name = 'BSCL' and tc.column_name = 'IdTipoDocumentoIdentidad'
and tc.data_type_name = 'NVARCHAR' and length = 1 and t.is_user_defined_type = 'FALSE';*/

/*call "BSCL.Plataforma.BD.Utilitarios::GeneraAlterTable" ( 	
    p_SchemaName	=> 'BSCL',
    p_Action		=> 'ALTER', -- ALTER, ADD, DROP
	p_TableName		=> '',
    p_ColumnName	=> 'IdTipoDocumentoIdentidad',
    p_DataTypeName	=> 'NVARCHAR',-- NVARCHAR, BIGINT, INT, DATE, DECIMAL, etc
    p_LengthNew		=> 3,
	p_out_Salida	=> ?
);*/ 

--LE_ALTER
alter table "BSCL.Homologacion.BD::HOM.ComprobanteTesoreria" alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NULL);
alter table "BSCL.Homologacion.BD::HOM.IngresoEgreso" alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NULL);
alter table "BSCL.Tesoreria.BD::TES.Comprobante" alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NOT NULL DEFAULT '');
alter table "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_IngresoEgresoGenerar" alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NULL);
alter table "BSCL.Custodia.BD::CUS.EventoCapitalCliente" alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NOT NULL DEFAULT '');
alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso" alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NOT NULL DEFAULT '');
alter table "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante" alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NULL);
alter table "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteGenerar" alter( "IdTipoDocumentoIdentidad" NVARCHAR(3) NULL);


commit;

--select * from sys.procedures where schema_name = 'BSCL' and is_valid = 'FALSE'

/*
CREATE COLUMN TABLE "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionGrilla"(
	"IdConfiguracionGrilla"	BIGINT NOT NULL,
	"IdEmpresa"					BIGINT NOT NULL,
	"Llave"						NVARCHAR(5000) NOT NULL,
	"Valor"						NVARCHAR(5000) NOT NULL,
	primary key ("IdConfiguracionGrilla")
);*/


/*
set schema bscl;
select * from  "BSCL.Plataforma.BD::PLA.Empresa"; 
update "BSCL.Plataforma.BD::PLA.Empresa" set "IdEstado" = 'INA' where "IdEmpresa" !=1;
update "BSCL.Plataforma.BD::PLA.Empresa" set "Nombre" = 'CREDICORP CAPITAL PERU - QA' where "IdEmpresa" =1;
commit;
*/



--################################  22/03/2017 ################################

set schema bscl;
select "IdGradoInstruccion","Codigo","Nombre","IdPais"from "BSCL.SocioNegocio.BD::PER.GradoInstruccion";
insert into "BSCL.SocioNegocio.BD::PER.GradoInstruccion"("IdGradoInstruccion","Codigo","Nombre","IdPais") values(1,'PRIMARIA','PRIMARIA',166);
insert into "BSCL.SocioNegocio.BD::PER.GradoInstruccion"("IdGradoInstruccion","Codigo","Nombre","IdPais") values(2,'SECUNDARIA','SECUNDARIA',166);
insert into "BSCL.SocioNegocio.BD::PER.GradoInstruccion"("IdGradoInstruccion","Codigo","Nombre","IdPais") values(3,'SUPERIOR','SUPERIOR',166);
insert into "BSCL.SocioNegocio.BD::PER.GradoInstruccion"("IdGradoInstruccion","Codigo","Nombre","IdPais") values(4,'TÉCNICO','TÉCNICO',166);
commit;
select "IdGradoInstruccion","Codigo","Nombre","IdPais"from "BSCL.SocioNegocio.BD::PER.GradoInstruccion";


--#################### 24/03/2017 ##################
set schema bscl; 

alter table "BSCL.SocioNegocio.BD::PER.Persona"
add("IdPaisResidenciaSecundaria"		BIGINT CS_FIXED); 

alter table "BSCL.SocioNegocio.BD::PER.Persona"
add("IdPaisNacionalidadSecundaria"		BIGINT CS_FIXED);

alter table "BSCL.SocioNegocio.BD::PER.Persona"
add("IdGiroCIIU"		BIGINT CS_FIXED);

alter table "BSCL.Plataforma.BD::REG.Giro"
alter("Nombre" NVARCHAR(500) not null default '');

--SUCURSALES:
set schema bscl;
delete from "BSCL.Plataforma.BD::PLA.Sucursal";
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166001,1,'MESASOFISTICADOS','Mesa sofisticados ','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166002,1,'MESAESTRUCTURALES','Mesa estructurales ','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166003,1,'CRONOS','Cronos ','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166004,1,'SANISIDRO','San Isidro ','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166005,1,'MIRAFLORES','Miraflores','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166006,1,'PIURA','Piura ','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166007,1,'TRUJILLO','Trujillo ','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166008,1,'AREQUIPA','Arequipa ','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166009,1,'ILO','Ilo','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166010,1,'OTROS','Otros ','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  VALUES(166011,1,'CANALWEB','Canal web','VIG');
commit;

-- GIROS:
-- Script independiente.

--RANGO PATRIMONIO
set schema bscl;
--select * from "BSCL.SocioNegocio.BD::PER.RangoPatrimonio";
delete from "BSCL.SocioNegocio.BD::PER.RangoPatrimonio";
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio"("IdRangoPatrimonio","DescripcionRango","Minimo","Maximo") VALUES (166001,'Mayor a USD 500,001',500001,-1);
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio"("IdRangoPatrimonio","DescripcionRango","Minimo","Maximo") VALUES (166002,'Entre USD 100,001 y USD 500,000',100001,500000);
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio"("IdRangoPatrimonio","DescripcionRango","Minimo","Maximo") VALUES (166003,'Entre USD 50,001 y USD 100.000',50001,100000);
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio"("IdRangoPatrimonio","DescripcionRango","Minimo","Maximo") VALUES (166004,'Entre USD 15,001 y USD 50,000',15001,50000);
INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.RangoPatrimonio"("IdRangoPatrimonio","DescripcionRango","Minimo","Maximo") VALUES (166005,'Menor a USD 15,000',-1,15000);
commit;

-- CATEGORIA INVERSIONISTA (Falta Filtro IdEmpresa)
set schema bscl;
select * from "BSCL.Plataforma.BD::COM.CategoriaInversionista";
--update "BSCL.SocioNegocio.BD::CLI.Cliente" set "IdCategoriaInversionista"='CONSERVADOR';
delete from "BSCL.Plataforma.BD::COM.CategoriaInversionista" where "IdEmpresa"=44 and "IdCategoriaInversionista" not in ('CONSERVADOR');
--insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('ARRIESGADO','ARRIESGADO',1,4);
--insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('CONSERVADOR','CONSERVADOR',1,4);
insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('CONSERVADOR','CONSERVADOR',1,4);
insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('CONSERVADOR MODERADO','CONSERVADOR MODERADO',1,4);
insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('MODERADO','MODERADO',1,4);
insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('MODERADO AGRESIVO','MODERADO AGRESIVO',1,4);
insert into "BSCL.Plataforma.BD::COM.CategoriaInversionista"("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa","Jerarquia")values('AGRESIVO','AGRESIVO',1,4);
update "BSCL.Plataforma.BD::COM.CategoriaInversionista" set "IdEmpresa"=1 where "IdCategoriaInversionista"='CONSERVADOR';
commit;

--CORRECCION BANCOS (FALTA ANALIZAR EL MODELO)
--select * from "BSCL.Plataforma.BD::BAN.Banco";
update "BSCL.Plataforma.BD::BAN.Banco" set "IdTipoBanco"='EXT' where "IdPaisOperacion"!=166;
commit;

--PROFESIONES
-- archivo con script.









set schema bscl;

alter table "BSCL.Plataforma.BD::REG.Giro"
alter("Nombre" NVARCHAR(500) not null default '');

--CLASE CLIENTE
set schema bscl;
create column table "BSCL.SocioNegocio.BD::CLI.ClaseCliente"(
"IdClaseCliente" 			NVARCHAR(40)	NOT NULL ,
"Nombre"					NVARCHAR(100)	NOT NULL DEFAULT '',
"IdUsuarioCreador"			BIGINT			NOT NULL DEFAULT 0,
"FechaCreacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
"FechaModificacion" 		SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
"IdEmpresa" 				BIGINT  		NOT NULL,
"IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
primary key("IdClaseCliente","IdEmpresa"));

--insert clase cliente:
set schema bscl;
--select * from "BSCL.SocioNegocio.BD::CLI.ClaseCliente";
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('RED','RED',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('EXTRANJEROS','EXTRANJEROS',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('AFP','AFP',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('CIASEGUROS','CIA. DE SEGUROS',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('INSTITUCIONALES','INSTITUCIONALES',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('BANCOS','BANCOS',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('FONDOSMUTUOS','FONDOS MUTUOS',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('ADMPATRIMONIOS','ADM. PATRIMONIOS',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('BANCAPRIVADA','BANCA PRIVADA',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('CREUZA','CREUZA',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('NIKLAS','NIKLAS',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('FONDOSMUTUOSCHILE','FONDOS MUTUOS CHILE',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('CHILE','CHILE',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('COLOMBIA','COLOMBIA',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.ClaseCliente"("IdClaseCliente","Nombre","IdEmpresa","IdEstado") values('CREDICORPBOLSA','CREDICORP BOLSA',1,'ACT');
commit;
 set schema bscl;
--agregar campo a cliente/enrolamiento 
alter table "BSCL.SocioNegocio.BD::CLI.Cliente" 
add("IdClaseCliente" NVARCHAR(40) NOT NULL DEFAULT '');
 
alter table "BSCL.SocioNegocio.BD::SOC.Enrolamiento" 
add("CLI_IdClaseCliente" NVARCHAR(40) DEFAULT '' NOT NULL); 

--cambio en entidad enrolamiento:
drop type "BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento";
CREATE TYPE "BSCL"."BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento" AS TABLE ( "IdEnrolamiento" BIGINT CS_FIXED,
	 "IdEmpresa" BIGINT CS_FIXED,
	 "IdUsuario" BIGINT CS_FIXED,
	 "IdTipoEntidad" NVARCHAR(100) CS_STRING,
	 "IdEntidadEnrolada" BIGINT CS_FIXED,
	 "IdTipoDocumentoIdentidad" NVARCHAR(3) CS_STRING,
	 "DocumentoIdentidad" NVARCHAR(40) CS_STRING,
	 "IdTipoDocumentoIdentidadAdicional" NVARCHAR(3) CS_STRING DEFAULT '',
	 "DocumentoIdentidadAdicional" NVARCHAR(40) CS_STRING DEFAULT '',
	 "Tipo" NVARCHAR(2) CS_STRING,
	 "Nombres" NVARCHAR(100) CS_STRING,
	 "ApellidoPaterno" NVARCHAR(100) CS_STRING,
	 "ApellidoMaterno" NVARCHAR(100) CS_STRING,
	 "RazonSocial" NVARCHAR(100) CS_STRING,
	 "IdPaisNacimiento" BIGINT CS_FIXED,
	 "FechaNacimiento" DAYDATE CS_DAYDATE,
	 "Sexo" NVARCHAR(2) CS_STRING,
	 "EstadoCivil" NVARCHAR(2) CS_STRING,
	 "IdCiudad" BIGINT CS_FIXED,
	 "IdDivisionPais" BIGINT CS_FIXED,
	 "IdTipoTelefono" NVARCHAR(40) CS_STRING,
	 "Telefono" NVARCHAR(100) CS_STRING,
	 "Email" NVARCHAR(100) CS_STRING,
	 "DireccionCalle" NVARCHAR(100) CS_STRING,
	 "DireccionNumero" NVARCHAR(40) CS_STRING,
	 "DireccionDepartamento" NVARCHAR(40) CS_STRING,
	 "FechaCreacion" SECONDDATE CS_SECONDDATE,
	 "FechaModificacion" SECONDDATE CS_SECONDDATE,
	 "IdPersonaConyuge" BIGINT CS_FIXED DEFAULT 0,
	 "RegimenConyugal" NVARCHAR(2) CS_STRING DEFAULT '',
	 "CLI_IdCategoriaInversionista" NVARCHAR(40) CS_STRING,
	 "CLI_IdTipoCliente" NVARCHAR(40) CS_STRING,
	 "CLI_IdEmpleadoEjecutivo" BIGINT CS_FIXED,
	 "CLI_CodigoAdicionalHolding" NVARCHAR(40) CS_STRING DEFAULT '',
	 "CLI_IdClaseCliente" NVARCHAR(40) CS_STRING,
	 "POR_IdPerfilComercial" NVARCHAR(40) CS_STRING,
	 "EMP_CodigoEmpleado" NVARCHAR(40) CS_STRING,
	 "EMP_CodigoTipoCargoEmpleado" NVARCHAR(6) CS_STRING,
	 "EMP_IdEmpleadoSupervisor" BIGINT CS_FIXED,
	 "EMP_IdSucursal" BIGINT CS_FIXED,
	 "EMP_FechaInicioCargo" DAYDATE CS_DAYDATE,
	 "EMP_FechaTerminoCargo" DAYDATE CS_DAYDATE DEFAULT '',
	 "CNT_CodigoInscripcion" NVARCHAR(40) CS_STRING,
	 "CNT_Emisor" NVARCHAR CS_STRING,
	 "CNT_Depositos" NVARCHAR CS_STRING,
	 "CNT_Bolsa" NVARCHAR CS_STRING,
	 "CNT_Corredora" NVARCHAR CS_STRING,
	 "CNT_CamaraDeCompensacion" NVARCHAR CS_STRING,
	 "CNT_Banco" NVARCHAR CS_STRING,
	 "CNT_Boveda" NVARCHAR CS_STRING,
	 "PRO_IdAreaProveedor" BIGINT CS_FIXED,
	 "EMP_CargoIndefinido" NVARCHAR CS_STRING DEFAULT '' );
commit;

--TIPO CLIENTE
set schema bscl;
--select * from "BSCL.SocioNegocio.BD::CLI.TipoCliente";
insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('INSTITUCIONAL','INSTITUCIONAL',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('INVERSIONISTA','INVERSIONISTA ACREDITADO',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('BROKEREXT','BROKER EXTRANJERO',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('CUSTODIO','CUSTODIO',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('PATRIMONIOAUTONOMO','PATRIMONIO AUTÓNOMO',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('RETAIL','RETAIL',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('SAFP','SAFP- SOCIEDAD ADMINISTRADORA DE FONDO DE PENSIONES',1,'ACT');
insert into "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente","Nombre","IdEmpresa","IdEstado") values('SAFM','SAFM-SOCIEDAD ADMINISTRADORA DE FFMM',1,'ACT');
--QUITAR BROKERAGE?
commit;

--para combo
set schema bscl;
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3562,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClaseCliente',0,'Q','','','');
commit;

--TIPO BLOQUEO:
set schema bscl;
--select * from "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" where "IdEmpresa"=1;
delete from "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" where "IdEmpresa"=1 and "IdTipoEntidad"='CLIENTE' and "IdAccion"='BLO';
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ1','CLIENTE','INFORMACIÓN INCOMPLETA','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ2','CLIENTE','INFORMACIÓN VENCIDA','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ3','CLIENTE','CLIENTE CON RESTRICCIÓN POR VINCULACIÓN GRUPO CREDICORP','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ4','CLIENTE','RUT BLOQUEADO CAVALI','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ5','CLIENTE','INCUMPLIMIENTO DE PAGO','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ6','CLIENTE','SIST. PARTICIPACIÓN CIUDADANA','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ7','CLIENTE','PROBLEMA CON CREDIBOLSA','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1, 'CLIBLOQ8','CLIENTE','CONGELAMIENTO DE FONDOS','BLO','ACT');
commit;


insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ1','PORTAFOLIO','INFORMACIÓN INCOMPLETA','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ2','PORTAFOLIO','INFORMACIÓN VENCIDA','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ3','PORTAFOLIO','CLIENTE CON RESTRICCIÓN POR VINCULACIÓN GRUPO CREDICORP','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ4','PORTAFOLIO','RUT BLOQUEADO CAVALI','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ5','PORTAFOLIO','INCUMPLIMIENTO DE PAGO','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ6','PORTAFOLIO','SIST. PARTICIPACIÓN CIUDADANA','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ7','PORTAFOLIO','PROBLEMA CON CREDIBOLSA','BLO','ACT');
insert into "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"("IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") values(1313, 'CLIBLOQ8','PORTAFOLIO','CONGELAMIENTO DE FONDOS','BLO','ACT');
commit;

--OTROS DICCIONARIO
set schema bscl;
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CLASE_CLIENTE','PE','Clase Cliente','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CREAR_CONYUGE','PE','Crear cónyuge','');
commit;

--CUADRE DIVISION PAIS (ELIMINAR LOS QUE NO TIENEN CUIDAD, BASURA)
set schema bscl;
update dp
set dp."IdCiudad"=ciu."IdCiudad", 
dp."IdClasificacion1"=cdp2."IdClasificacionDivisionPais",
dp."IdClasificacion2"=cdp."IdClasificacionDivisionPais"
from "BSCL.Plataforma.BD::REG.DivisionPais" dp
join "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" cdp 
on (substring(dp."Codigo",0,2) = cdp."Codigo" and dp."IdPais"=cdp."IdPais" and cdp."IdPais"=166)
join "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" cdp2 
on (substring(dp."Codigo",0,4) = cdp2."Codigo" and dp."IdPais"=cdp2."IdPais" and cdp2."IdPais"=166)
join "BSCL.Plataforma.BD::REG.Ciudad" ciu
on (ciu."NombreCiudad"=cdp."NombreClasificacionDivisionPais" and cdp."IdPais"=ciu."IdPais" and ciu."IdPais"=166)
where cdp."TipoClasificacionDivisionPais"='DEPARTAMENTO'
and cdp2."TipoClasificacionDivisionPais"='PROVINCIA';
commit;
delete from "BSCL.Plataforma.BD::REG.DivisionPais" where "IdClasificacion1"=0;
commit;

-- TIPOS RELACION. ANALIZAR SI ESTOS HAY QUE EFECTIVAMENTE BORRARLOS!
set schema bscl;
select * from "BSCL.SocioNegocio.BD::REL.TipoRelacion" where
"IdTipoEntidadOrigen" in('PERSONA','') and "IdTipoRelacion" not in ('CY','PEMDOR','REP','PARIEN','COTITU','FIDUCI');

update "BSCL.SocioNegocio.BD::REL.TipoRelacion"
set "IdEstado"='INA' where "IdTipoEntidadOrigen" in('PERSONA','') and "IdTipoRelacion" not in ('CY','PEMDOR','REP','PARIEN','COTITU','FIDUCI');
commit;
--FALTA VER SI BORRAR LAS OTRAS RELACIONES.
select * from "BSCL.SocioNegocio.BD::REL.RelacionPersona" where "IdTipoRelacion" not in ('CY','PEMDOR','REP','PARIEN','COTITU','FIDUCI');

--ASIGNAR MENU FULL
update "BSCL.Plataforma.BD::ACC.Usuario"
set "CodigoMenu" = 'BCS_FULLMENU_PERU' where "CodigoUsuario" != 'MAMASTER';
COMMIT;

--BARRIDO DEL DICCIONARIO PARA CAMBIAR AGENTE POR ASESOR COMERCIAL
update "BSCL.Plataforma.BD::PLA.Diccionario" set "Descripcion" = replace("Descripcion",'Agente','Asesor Comercial') 
where "CodLenguaje" = 'PE' and  "Descripcion" like '%Agente%';
commit;


--AGREGAR A UAT y MB:
set schema bscl;
insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (1020, 'PORTAFOLIO', 1, 'N', 'CHECKBOX', 'Envío de Cartola a Dirección Física','CAR_ENVCDF', 'GRUPOCARTOLA', 'PRE', 'ACT', 0, 0, 0, 'N' );
commit;


--RECARTERIZACION CLIENTES
set schema bscl;
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20016,'PLUGIN_SOC.jar@cl.bcs.socionegocio.eje.RecarterizacionClientes',0,'J','','','');
commit;

set schema bscl;
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3565,'BSCL.Portafolio.BD.Vistas::POR.ConsultarLogCambioEjecutivoPortafolio',0,'Q','','','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3566,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogCambioEjecutivoCliente',0,'Q','','','');
commit;


set schema bscl;
alter table "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais"
add("Codigo" nvarchar(10) not null default '');

alter table "BSCL.Plataforma.BD::REG.DivisionPais"
add("Codigo" nvarchar(10) not null default '');

set schema bscl;
create column table "BSCL.SocioNegocio.BD::CLI.ClaseCliente"(
"IdClaseCliente" 			NVARCHAR(40)	NOT NULL ,
"Nombre"					NVARCHAR(100)	NOT NULL DEFAULT '',
"IdUsuarioCreador"			BIGINT			NOT NULL DEFAULT 0,
"FechaCreacion" 			SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
"FechaModificacion" 		SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',  
"IdEmpresa" 				BIGINT  		NOT NULL,
"IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
primary key("IdClaseCliente","IdEmpresa"));

--###########################################
-- RECARTERIZACION MASIVA CLIENTES - ARCHIVO
--###########################################
set schema bscl;
select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato" where "CodigoArchivo"='RECARTERIZACION_CLIENTES';
/*
DELETE FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" where "IdArchivo"=23;
delete from "BSCL.Plataforma.BD::PLA.Formato" where "IdFormato">=214 and "IdFormato"<=224; 
delete from "BSCL.Plataforma.BD::PLA.Archivo" where "IdArchivo"=23;
*/

insert into "BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo","IdEmpresa","CodigoArchivo","TieneSeparador","SeparadorColumnas","TieneCabecera")
values(23,1,'RECARTERIZACION_CLIENTES','S',';','S');
-- FORMATO
--Tipo persona: N/J/M
insert into "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
select 219,'TIPOPERSONA','STRING',0,2,'S','','','N',0,'S' from dummy;
--Documento identidad
insert into "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
select 220,'DOCUMENTOIDENTIDAD','STRING',1,40,'S','','','N',0,'S' from dummy;
--Codigo de sucursal del cliente
insert into "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
select 221,'SUCURSALCLIENTE','STRING',2,40,'N','','','N',0,'S' from dummy;
--Código ejecutivo del cliente
insert into "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
select 222,'EJECUTIVOCLIENTE','STRING',3,40,'N','','','N',0,'S' from dummy;
--Codigo del portafolio
insert into "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
select 223,'CODIGOPORTAFOLIO','STRING',4,100,'N','','','N',0,'S' from dummy;
--Código ejecutivo del portafolio
insert into "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales","ForzarMayuscula")
select 224,'EJECUTIVOPORTAFOLIO','STRING',5,40,'N','','','N',0,'S' from dummy;
--ArchivoFormato (mapeo)
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato")
values(229,23,219);
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato")
values(230,23,220);
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato")
values(231,23,221);
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato")
values(232,23,222);
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato")
values(233,23,223);
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato")
values(234,23,224);
--rollback;
commit;

set schema bscl;

call "BSCL.Custodia.BD.Servicios::ConsultarCustodiaPortafolioPASO" (
	p_IdEmpresa                		 => 1,
	p_IdUsuario                		 => 37,
	p_IdModulo                 		 => 'CUS',
	p_IdCliente 					 => 116323,
	p_IdPortafolio 					 => 20021,
	p_IdClasificacionTipoInstrumento => '',
	p_IdTipoInstrumento 			 => '',
	p_IdInstrumento 				 => 1368,
	p_IdBoveda 						 => '',
	p_AgrupaPorBoveda 				 => 'S',
	p_AgrupaPorRelacion 			 => 'S',
	p_FechaConsulta 				 => '2017-04-05',
	p_TipoFechaConsulta				 => 'LIQ',
	p_out_tbl						 => ?
);

select "INGPrecioPromedioCompra","IdInstrumento","INGCantidad",* 
from "BSCL.Custodia.BD::CUS.Cartera" where "Nemotecnico"='LAN' 
and "IdCliente"=116323 and "IdPortafolio"=20021 and "IdEstado"='VIG';


select top 1 "suby_INGPrecioPromedioCompra",* 

select *
from "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea"
where "IdCliente"=116323 and "IdPortafolio"=20021 and "IdEstado"='VIG';


SELECT * 
FROM "BSCL.Custodia.BD::CUS.Subyacente"
where "Nemotecnico"='LAN' 
and "IdClienteOperacionRelacionada"=116323 
and "IdPortafolioOperacionRelacionada"=20021 and "IdEstado"='VIG';

select "INGPrecioPromedioCompra",* from "BSCL.Custodia.BD::CUS.Cartera" where "Nemotecnico"='LAN'
and "INGPrecioPromedioCompra">0 and "IdEstado"='VIG';







INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_GARANTIAS','CL','PPP Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_PPP_GARANTIAS','CL','Monto PPP Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_GARANTIAS','CL','Precio Mercado Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_MERCADO_GARANTIAS','CL','Monto Mercado Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_GARANTIAS','CL','Diferencia Mercado Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_SIMULTANEA_COMPRAS','CL','PPP Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_PPP_SIMULTANEA_COMPRAS','CL','Monto PPP Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_SIMULTANEA_COMPRAS','CL','Precio Mercado Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_MERCADO_SIMULTANEA_COMPRAS','CL','Monto Mercado Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_SIMULTANEA_COMPRAS','CL','Diferencia Mercado Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_SIMULTANEA_VENTAS','CL','PPP Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_PPP_SIMULTANEA_VENTAS','CL','Monto PPP Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_SIMULTANEA_VENTAS','CL','Precio Mercado Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_MERCADO_SIMULTANEA_VENTAS','CL','Monto Mercado Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_SIMULTANEA_VENTAS','CL','Diferencia Mercado Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_PRESTATARIO','CL','PPP Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_PPP_PRESTATARIO','CL','Monto PPP Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_PRESTATARIO','CL','Precio Mercado Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_MERCADO_PRESTATARIO','CL','Monto Mercado Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_PRESTATARIO','CL','Diferencia Mercado Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_EMPRESTITO','CL','PPP Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_PPP_EMPRESTITO','CL','Monto PPP Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_EMPRESTITO','CL','Precio Mercado Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_MERCADO_EMPRESTITO','CL','Monto Mercado Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_EMPRESTITO','CL','Diferencia Mercado Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_CUSTODIA','CL','PPP Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_PPP_CUSTODIA','CL','Monto PPP Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_CUSTODIA','CL','Precio Mercado Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_MERCADO_CUSTODIA','CL','Monto Mercado Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_CUSTODIA','CL','Diferencia Mercado Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_GARANTIAS','CL','PPP Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_PPP_GARANTIAS','CL','Monto PPP Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_GARANTIAS','CL','Precio Mercado Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_MERCADO_GARANTIAS','CL','Monto Mercado Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_GARANTIAS','CL','Diferencia Mercado Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_SIMULTANEA_COMPRAS','CL','PPP Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_PPP_SIMULTANEA_COMPRAS','CL','Monto PPP Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_SIMULTANEA_COMPRAS','CL','Precio Mercado Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_MERCADO_SIMULTANEA_COMPRAS','CL','Monto Mercado Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_SIMULTANEA_COMPRAS','CL','Diferencia Mercado Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_SIMULTANEA_VENTAS','CL','PPP Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_PPP_SIMULTANEA_VENTAS','CL','Monto PPP Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_SIMULTANEA_VENTAS','CL','Precio Mercado Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_MERCADO_SIMULTANEA_VENTAS','CL','Monto Mercado Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_SIMULTANEA_VENTAS','CL','Diferencia Mercado Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_PRESTATARIO','CL','PPP Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_PPP_PRESTATARIO','CL','Monto PPP Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_PRESTATARIO','CL','Precio Mercado Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_MERCADO_PRESTATARIO','CL','Monto Mercado Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_PRESTATARIO','CL','Diferencia Mercado Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_EMPRESTITO','CL','PPP Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_PPP_EMPRESTITO','CL','Monto PPP Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_EMPRESTITO','CL','Precio Mercado Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_MERCADO_EMPRESTITO','CL','Monto Mercado Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_EMPRESTITO','CL','Diferencia Mercado Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_CUSTODIA','CL','PPP Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_PPP_CUSTODIA','CL','Monto PPP Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_CUSTODIA','CL','Precio Mercado Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_MERCADO_CUSTODIA','CL','Monto Mercado Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_CUSTODIA','CL','Diferencia Mercado Custodia','');

commit;
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" like 'UI_VALOR_MERCADO_%' or "CodDiccionario"  like 'UI_VALOR_PPP_%';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" like 'UI_MONTO_MERCADO_%' or "CodDiccionario"  like 'UI_MONTO_PPP_%';
commit;

select * from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" like 'UI_VALOR_MERCADO_%' or "CodDiccionario"  like 'UI_VALOR_PPP_%';

--###########################################
-- BLOQUEO MASIVO CLIENTES - ARCHIVO
--###########################################
set schema bscl;

insert into "BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo","IdEmpresa","CodigoArchivo","TieneSeparador","SeparadorColumnas","TieneCabecera")
values(24,1,'ARCHIVO_BLOQUEO_CLIENTES_MASIVO_PE','S',';','S');

update "BSCL.Plataforma.BD::PLA.Archivo"
set "IdEmpresa"=1313 where "IdArchivo"=24;
commit;

---------- FORMATO ---------
-- 219 : TIPOPERSONA
--   9 : TIPODOCUMENTO
-- 220 : DOCUMENTOIDENTIDAD
-- 223 : CODIGOPORTAFOLIO
--  98 : MOTIVOBLOCLI (OPCIONAL)
--  99 : MOTIVOBLOPOR (OPCIONAL)

--ArchivoFormato (mapeo)
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(235,24,219); 
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(236,24,9);
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(237,24,220);
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(238,24,223);

set schema bscl;
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(239,24,98);
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(240,24,99);
commit;

----archivo no recibirá el motivo de bloqueo (se inyectará al servicio a partir de la UI:
--set schema bscl;
--delete from "BSCL.Plataforma.BD::PLA.ArchivoFormato" where "IdArchivoFormato" in (239,240);
--commit;


select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato" where "CodigoArchivo"='ARCHIVO_BLOQUEO_CLIENTES_MASIVO_PE';

--rollback;
commit;

--###########################################
-- DESBLOQUEO MASIVO CLIENTES - ARCHIVO
--###########################################
set schema bscl;

insert into "BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo","IdEmpresa","CodigoArchivo","TieneSeparador","SeparadorColumnas","TieneCabecera")
values(25,1,'ARCHIVO_DESBLOQUEO_CLIENTES_MASIVO_PE','S',';','S');

set schema bscl;
update "BSCL.Plataforma.BD::PLA.Archivo"
set "IdEmpresa"=1313 where "IdArchivo"=25;
commit;

---------- FORMATO ---------
-- 219 : TIPOPERSONA
--   9 : TIPODOCUMENTO
-- 220 : DOCUMENTOIDENTIDAD
-- 223 : CODIGOPORTAFOLIO
--  98 : MOTIVOBLOCLI (OPCIONAL)
--  99 : MOTIVOBLOPOR (OPCIONAL)

--ArchivoFormato (mapeo)
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(241,25,219); 
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(242,25,9);
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(243,25,220);
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(244,25,223);

set schema bscl;
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(245,25,98);
insert into "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") values(246,25,99);
commit;

----archivo no recibirá el motivo de bloqueo (se inyectará al servicio a partir de la UI:
--set schema bscl;
--delete from "BSCL.Plataforma.BD::PLA.ArchivoFormato" where "IdArchivoFormato" in (245,246);
--commit;


select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato" where "CodigoArchivo"='ARCHIVO_DESBLOQUEO_CLIENTES_MASIVO_PE';

set schema bscl;
select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato" where "CodigoArchivo"='ARCHIVO_BLOQUEO_CLIENTES_MASIVO';


--rollback;
commit;

select 
	per."IdPersona",per."Tipo" as "TipoPersona",di."IdTipoDocumentoIdentidad",di."DocumentoIdentidad", 
	cli."IdCliente",cli."IdEstado" as "IdEstadoCliente",
	por."IdPortafolio",por."CodigoPortafolio",por."IdEstado" as "IdEstadoPortafolio",
	cli."IdTipoBloqueo" as "IdTipoBloqueoCliente", cli."IdTipoDesbloqueo" as "IdTipoDesbloqueoCliente",
	cli."ComentarioBloqueo" as "ComentarioBloqueoCliente",cli."ComentarioDesbloqueo" as "ComentarioDesbloqueoCliente",
	por."IdTipoBloqueo" as "IdTipoBloqueoPortafolio", por."IdTipoDesbloqueo" as "IdTipoDesbloqueoPortafolio",
	por."ComentarioBloqueo" as "ComentarioBloqueoPortafolio",por."ComentarioDesbloqueo" as "ComentarioDesbloqueoPortafolio"
from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" di
join "BSCL.SocioNegocio.BD::PER.Persona" per on per."IdPersona"=di."IdPersona"
join "BSCL.SocioNegocio.BD::CLI.Cliente" cli on cli."IdPersona"=per."IdPersona"
join "BSCL.Portafolio.BD::POR.Portafolio" por on por."IdCliente"=cli."IdCliente"
where cli."IdEmpresa"=IdEmpresa
and "DocumentoIdentidad" in (:ListaDocumentoIdentidad);


set schema bscl;
select 
 per."IdPersona",per."Tipo" as "TipoPersona",di."IdTipoDocumentoIdentidad",di."DocumentoIdentidad", 
 cli."IdCliente",cli."IdEstado" as "IdEstadoCliente", 
 por."IdPortafolio",por."CodigoPortafolio",por."IdEstado" as "IdEstadoPortafolio", 
 cli."IdTipoBloqueo" as "IdTipoBloqueoCliente", cli."IdTipoDesbloqueo" as "IdTipoDesbloqueoCliente", 
 cli."ComentarioBloqueo" as "ComentarioBloqueoCliente",cli."ComentarioDesbloqueo" as "ComentarioDesbloqueoCliente", 
 por."IdTipoBloqueo" as "IdTipoBloqueoPortafolio", por."IdTipoDesbloqueo" as "IdTipoDesbloqueoPortafolio", 
 por."ComentarioBloqueo" as "ComentarioBloqueoPortafolio",por."ComentarioDesbloqueo" as "ComentarioDesbloqueoPortafolio" 
 from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" di 
 join "BSCL.SocioNegocio.BD::PER.Persona" per 
 join "BSCL.SocioNegocio.BD::CLI.Cliente" cli on cli."IdPersona"=per."IdPersona" 
 join "BSCL.Portafolio.BD::POR.Portafolio" por on por."IdCliente"=cli."IdCliente" ;


select * from "BSCL.SocioNegocio.BD::PER.Persona"
select * from "BSCL.SocioNegocio.BD::CLI.Cliente"
select * from "BSCL.Portafolio.BD::POR.Portafolio";
select * from "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio";


set schema bscl;
select * from "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" = 20020;

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20020,'PLUGIN_SOC.jar@cl.bcs.socionegocio.blo.CambioEstadoMasivoClientesPortafolios',0,'J','','','');
commit;


TIPOPERSONA;TIPODOCUMENTOIDENTIDAD;DOCUMENTOIDENTIDAD;CODIGOPORTAFOLIO;TIPOCAMBIOESTADOCLIENTE;TIPOCAMBIOESTADOPORTAFOLIO
N;16929326-0;1;BNOUSO;BNOUSO
N;16929326-0;2;BNOUSO;BFRAUD
J;11301384-2;1;BMUERT;BMUERT
N;22126550-5;0;;
N;22126550-5;0;;
N;40488049-7;;BFRAUD;
N;6654073-1;1;;BMUERT





-- MONITOR 
set schema bscl;

INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
VALUES('MONITOR_PLATAFORMA', 'Monitor de plataforma', 1, 'PLATAFORMA', 'cl.bcs.plataforma.jobs.MonitorPlataformaJob', 'JAVA', '','N');

DROP table #parametros;
create local temporary table #parametros("NombreParametro" nvarchar(100), "Valor" nvarchar(100));
INSERT INTO #parametros Select 'IdEmpresa' as "NombreParametro", '1' as "Valor" from dummy;
INSERT INTO #parametros Select 'NotificarAdministradoresTI' as "NombreParametro", 'F' as "Valor" from dummy;
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa  =>  1,
	p_IdUsuario  =>  37,
	p_IdModulo   =>  'PLA',
	p_IdTipoTarea  =>  'MONITOR_PLATAFORMA',
	p_EsDemonio    =>  'N'  ,
	p_DiaDeMes    =>  '**',
	p_DiaDeSemana  =>  '*',
	p_Mes      =>  '**',
	p_Hora      =>  '**',
	p_Minuto    =>  '15',
	p_SoloDiasHabiales  => 'S',
	p_SoloPrincipioMes  => 'N',
	p_SoloFinDeMes    => 'N',
	p_habilitado    =>  'S', 
	p_TablaParametros  =>  #parametros,              
	p_IdAgenda    => ?,
	p_IdPrograma    => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?              
);	 

commit;



set schema bscl;
-- CLIENTE
alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
add("IdSucursalPreferencia"	BIGINT 	null);

alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
add("IdPersonaBrokerExtranjero"	BIGINT 	null);

alter table "BSCL.SocioNegocio.BD::CLI.Cliente"
add("IdClaseCliente" NVARCHAR(40) NOT NULL DEFAULT '');



/************** DICCIONARIO ***********/
/*
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_TIR_MEDIA','CL','TIR Media','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_TIR_EMISION','CL','TIR Emisión','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CLASIF_RIESGO','CL','Clasif.Riesgo','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DURACION','CL','Duración','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CONVEXIDAD','CL','Convexidad','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MECANISMO_DE_CAPTACION','CL','Mecanismo de captación','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_NOMINAL','CL','Valor Nominal','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_INTERESES_CORRIDOS','CL','Intereses corridos','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_REAJUSTES_VAC_CORRIDO','CL','Reajustes/VAC corrido','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_TOTAL_SIN_COMISIONES','CL','Monto total sin comisiones','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_TIR','CL','TIR','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_INGRESAR_ORDEN_RF','CL','Ingresar Orden Renta Fija','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_TIENE_CALL','CL','Valor tiene call','');
*/

-----
-- FORMULARIO INGRESO DE ORDENES RF
set schema bscl;


commit;



-- CONSULTA DE CUSTODIA A VISTAS:
set schema bscl;

call "BSCL.Custodia.BD.Servicios::ConsultarCustodiaPortafolioANTESCAMBIO" (
	p_IdEmpresa                			=>1,
	p_IdUsuario                			=>1,
	p_IdModulo                 			=>'CUS', 
	p_IdClasificacionTipoInstrumento 	=>'RV', 
	p_IdTipoInstrumento 				=>'ACN',
	p_IdCliente 						=>524896,
	p_IdPortafolio   					=>512357,
	p_TipoFechaConsulta					=>'ADQ', 
	p_out_tbl 							=> ?
);

set schema bscl;
call "BSCL.Custodia.BD.Servicios::ConsultarCustodiaPortafolioANTESCAMBIO" (
	p_IdEmpresa                			=>1,
	p_IdUsuario                			=>1,
	p_IdModulo                 			=>'CUS', 
	p_IdClasificacionTipoInstrumento 	=>'', 
	p_IdTipoInstrumento 				=>'',
	p_IdCliente 						=>524896,
	p_IdPortafolio   					=>512357,
	p_TipoFechaConsulta					=>'ADQ', 
	p_FechaConsulta						=>'2017-05-08',
	p_out_tbl 							=> ?
);

set schema bscl;
select * from
"BSCL.Portafolio.BD::POR.Portafolio"

set schema 
select cast(definition as varchar) from sys.views where view_name like '%UtilConsultarCustodia%'
"BSCL.Portafolio.BD::POR.Portafolio"


select * from "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaCarteraFechaAdquisicion"
where "IdCliente"=524896;

select * from "BSCL.Plataforma.BD::PLA.Configuracion" where "PLA.FechaSistema"

select * 
from "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaCartera" as ccc
	join "BSCL.Plataforma.BD::PLA.Configuracion" conf
		on (conf."IdEmpresa" = ccc."IdEmpresa" and conf."CodigoParametro"='PLA.FechaSistema')
where conf."Valor" >= ccc."EGRFechaOperacion" and conf."Valor" <= ccc."EGRFechaOperacion"	
	
select * from "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaCarteraFechaAdquisicion"

select * from (
	select "IdEmpresa","IdPersonaOrigen","IdPersonaDestino"
	from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona"
	where "IdEstado" = 'ACT' group by "IdEmpresa","IdPersonaOrigen","IdPersonaDestino") as rel 
where 



/*******************
VALORIZACION PERU
********************/
set schema bscl;
--ENTRADAS SP VALORIZAR
call "BSCL.Plataforma.BD.Servicios::CrearPojo"(
	p_nombreTabla => 'BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosPQ',
	p_nombrePojo => 'InstrumentosPQ', 
	p_tipo => 'T', 
	p_out_pojo => ?
);
call "BSCL.Plataforma.BD.Servicios::CrearPojo"(
	p_nombreTabla => 'BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosWS',
	p_nombrePojo => 'InstrumentosWS', 
	p_tipo => 'T', 
	p_out_pojo => ?
);
--SALIDAS SP VALORIZAR
call "BSCL.Plataforma.BD.Servicios::CrearPojo"(
	p_nombreTabla => 'BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosPQ',
	p_nombrePojo => 'InstrumentosPQ', 
	p_tipo => 'T', 
	p_out_pojo => ?
);
call "BSCL.Plataforma.BD.Servicios::CrearPojo"(
	p_nombreTabla => 'BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosWS',
	p_nombrePojo => 'InstrumentosWS', 
	p_tipo => 'T', 
	p_out_pojo => ?
);



set schema bscl;
select * from "BSCL.Plataforma.BD::BAT.TipoPrograma";

set schema bscl;
select * from "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea";


select * from sys.procedures where is_valid = 'FALSE';

select * from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" where "DocumentoIdentidad" in(
'456456464-3',
'456456465-1',
'456456466-K',
'456456467-8');







SELECT *
FROM "_SYS_BIC"."BSCL.Custodia.BD.Analytics/CustodiaFechaOperacion"('PLACEHOLDER' = ('$$FechaConsulta$$','0001-01-01'))




set schema bscl;
SELECT * FROM  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" WHERE          
"IdPersona"=214068


select * from sys.tables where upper(table_name) 
like '%RELACIO%%';


/*******************
CONSULTA CUSTODIA
********************/
set schema bscl;
create column table "BSCL.Custodia.BD::CUS.TipoCuenta"(
	"IdEmpresa"		bigint,
	"IdTipoCuenta" 	nvarchar(10),
	"Nombre"		nvarchar(40) not null default '',
	primary key ("IdEmpresa","IdTipoCuenta")
);
insert into "BSCL.Custodia.BD::CUS.TipoCuenta"("IdEmpresa","IdTipoCuenta","Nombre")
select 1,'CPRO','Cuenta propia' 	from dummy union all
select 1,'REL', 'Relacionado' 		from dummy union all
select 1,'TER', 'Cuenta terceros' 	from dummy;
commit;





--PENDIENTE: SUBIR JOBMAN EN QA PERU!

select *








/*************************
 CREACION CLIENTES NOMINA
**************************/
set schema bscl;
select * from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio"=3592;-- order by "IdServicio";
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") 
values (3592,'BSCL.SocioNegocio.BD.Servicios::CLI.CrearClienteNominaPE' , 1,  'P', '','','');
commit;
--rollback;

set schema "BSCL";

delete from "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"
where "CodigoArchivo" = 'ARCHIVO_CLIENTE';

/* DELETE FROM "BSCL"."BSCL.Plataforma.BD::PLA.Formato"
WHERE "IdFormato" between 5 and 33;
DELETE FROM "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
WHERE "IdFormato" between 5 and 33;*/

set schema bscl; 
select * from "BSCL"."BSCL.Plataforma.BD::PLA.Archivo" order by "IdArchivo" desc;
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo","IdEmpresa","CodigoArchivo","TieneSeparador","SeparadorColumnas","TieneCabecera")
values(26,1,'ARCHIVO_CLIENTE_PE','S',';','S');
commit;


update "BSCL"."BSCL.Plataforma.BD::PLA.Archivo" set "IdEmpresa" = 1313 where "IdArchivo"=26;
commit;

--select * from "BSCL"."BSCL.Plataforma.BD::PLA.Formato" where "IdFormato" between 9 and 37 order by "IdFormato";
select * from "BSCL.Plataforma.BD::PLA.Formato" where "IdFormato" between 9 and 37 or "IdFormato" in(225,226,227) order by "IdFormato"

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion")--,"CantidadDecimales","IncluyeDecimales","ForzarMayuscula") 
select 225,'CODIGORUT','string',0,0,'S','','' from dummy union all
select 226,'BROKER','string',0,0,'S','','' from dummy union all
select 227,'DISTRITO','string',0,0,'S','','' from dummy;
commit;

select * from "BSCL.Plataforma.BD::PLA.Formato" where "IdFormato" in(225,226,227) order by "IdFormato"

/*
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(9,'TIPODOCUMENTO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(10,'DOCUMENTOIDENTIDAD','string',0,0,'S','','');
//225,'CODIGORUT'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(11,'TIPOPERSONA','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(12,'NOMBRES','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(13,'APELLIDOPATERNO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(14,'APELLIDOMATERNO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(15,'PAISNACIMIENTO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(16,'FECHANACIMIENTO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(17,'EDOCIVIL','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(18,'GENERO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(19,'CALLE','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(20,'PAISRESIDENCIA','string',0,0,'S','','');
227,'DISTRITO'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(21,'NUMERO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(22,'TELEFONO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(23,'EMAIL','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(24,'CATINV','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(25,'PERCOMER','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(32,'RAZONSOCIAL','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(33,'TIPOCLIENTE','STRING',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(34,'COMPLEMENTO','STRING',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(35,'TIPOTELEFONO','STRING',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(37,'CIUDAD','STRING',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(100,'EJECUTIVO','STRING',0,0,'S','','');
*/

select * from "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" where "IdArchivoFormato" between 300 and 324;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(300 , 26 , 9  );--  9,'TIPODOCUMENTO',
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(301 , 26 , 10 );-- 10,'DOCUMENTOIDENTIDAD'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(302 , 26 , 225);--225,'CODIGORUT'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(303 , 26 , 11 );-- 11,'TIPOPERSONA'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(304 , 26 , 32 );-- 32,'RAZONSOCIAL'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(305 , 26 , 12 );-- 12,'NOMBRES',
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(306 , 26 , 13 );-- 13,'APELLIDOPATERNO'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(307 , 26 , 14 );-- 14,'APELLIDOMATERNO'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(308 , 26 , 15 );-- 15,'PAISNACIMIENTO'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(309 , 26 , 16 );-- 16,'FECHANACIMIENTO'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(310 , 26 , 17 );-- 17,'EDOCIVIL'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(311 , 26 , 18 );-- 18,'GENERO'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(312 , 26 , 20 );-- 20,'PAISRESIDENCIA'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(313 , 26 , 227);--227,'DISTRITO'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(314 , 26 , 19 );-- 19,'CALLE',
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(315 , 26 , 21 );-- 21,'NUMERO'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(316 , 26 , 34 );-- 34,'COMPLEMENTO'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(317 , 26 , 35 );-- 35,'TIPOTELEFONO'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(318 , 26 , 22 );-- 22,'TELEFONO'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(319 , 26 , 23 );-- 23,'EMAIL'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(320 , 26 , 24 );-- 24,'CATINV'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(321 , 26 , 25 );-- 25,'PERCOMER',
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(322 , 26 , 33 );-- 33,'TIPOCLIENTE',
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(323 , 26 , 100);--100,'EJECUTIVO'
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(324 , 26 , 226);--226,'BROKER'

commit;

set schema bscl;
select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato"
where "CodigoArchivo" = 'ARCHIVO_CLIENTE_PE';


select * from "BSCL.Plataforma.BD::REG.DivisionPais"


-------------------------
-- VALORIZAR CARTERA
-------------------------
set schema bscl;
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_VALORIZADO','CL','Valor Valorizado','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_ACTUALIZADO','CL','Valor Actualizado','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_SUCIO','CL','Precio Sucio','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_TIR_NETA','CL','TIR Neta','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CLASE_DE_ORDEN','CL','Clase de Orden','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ES_ORDEN_ESPECIAL','CL','Es Orden Especial','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_VALORIZADO','PE','Valor Valorizado','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_ACTUALIZADO','PE','Valor Actualizado','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_SUCIO','PE','Precio Sucio','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_TIR_NETA','PE','TIR Neta','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CLASE_DE_ORDEN','PE','Clase de Orden','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ES_ORDEN_ESPECIAL','PE','Es Orden Especial','');

commit;


set schema bscl;
SELECT"Nombre", sum("Id") AS "Id" FROM "_SYS_BIC"."BSCL.Custodia.BD.Analytics/demo4" GROUP BY "Nombre";

select "IdEmpresa","TipoFechaConsulta" from "BSCL.Custodia.BD::CUS.PasoCustodiaPortafolio"

insert into "BSCL.Custodia.BD::CUS.PasoCustodiaPortafolio" ("IdEmpresa","TipoFechaConsulta") select 1,'HOA' from dummy;
select * from "BSCL.Custodia.BD::CUS.PasoCustodiaPortafolio";
commit;

--EXECUTE IMMEDIATE 'insert into "BSCL.Custodia.BD::CUS.PasoCustodiaPortafolio" ("IdEmpresa","TipoFechaConsulta") select 1,''HOA'' from dummy;';

set schema bscl;
select * from "BSCL.Plataforma.BD::INS.TipoInstrumento" where "IdClasificacionTipoInstrumento"='RF';

select distinct "IdPaisEmisor" from "BSCL.Plataforma.BD::INS.Instrumento" 
where "IdClasificacionTipoInstrumento"='RF';

select * from "BSCL.Plataforma.BD::INS.Instrumento" where "IdPaisEmisor" = 166

SELECT * FROM "BSCL.Plataforma.BD::VAL.Config";


set schema bscl;
--select * from "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio"=20033;
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20033,'PLUGIN_PLA.jar@cl.bcs.plataforma.val.Valorizar',0,'J','','','');
commit;


/*********************

*********************/

set schema bscl;

select * from (
select distinct TTO."AccionOrden",TTO."IdTipoOperacion",TI."IdTipoInstrumento",TTO."IdTipoProducto",TTO."EsVentaCorta" 
from "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" TI 
join "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" tto ON TI."IdTipoOperacion" = TTO."IdTipoOperacion"  
WHERE TTO."AccionOrden" IS NOT NULL) t
where "IdTipoInstrumento" in ('BEM');

insert into "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento"
("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
select max("IdTipoOperacionTipoInstrumento")+1,'COMRF','BON','ACT',0,'' from "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" union
select max("IdTipoOperacionTipoInstrumento")+2,'VENRF','BON','ACT',0,'' from "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento";
commit;



set schema bscl;
call "BSCL.Orden.BD.Servicios::ConsultarRequisitosBasicosCrearOrdenes" (
	p_IdEmpresa						=>1313,
	p_IdUsuario						=>1,
	p_IdModulo						=>'ORD', 
	p_IdEmpleado					=>11439,
	p_IdTipoOperacion				=>'VENRF',
	P_IdPortafolio					=>1450115,
	p_out_InformacionContador		=>?,
	p_out_InformacionDetalle 		=>?,
	p_out_AdvertenciasContador		=>?,
	p_out_AdvertenciasDetalle		=>?,
	p_out_ErroresContador 			=>?,
	p_out_ErroresDetalle 			=>?);
	
	
SELECT 
	CASE 
		WHEN det1."IdTipoOperacion" IS NOT NULL THEN 'S' 
		WHEN det2."IdTipoOperacion" IS NOT NULL THEN 'S' ELSE 'N' END as "PEC"
	,*
	FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" emp
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on tipoope."IdTipoOperacion" = 'VENRF'
	LEFT JOIN "BSCL.Plataforma.BD::COM.PlantillaEmpleado" pnt on emp."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado"
	LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" det1 on det1."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado" and det1."IdEstado" = 'ACT' and det1."IdTipoProducto" = tipoope."IdTipoProducto" and det1."IdTipoOperacion" = 'VENRF'
	LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" det2 on det2."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado" and det2."IdEstado" = 'ACT' and det2."IdTipoProducto" = tipoope."IdTipoProducto" and det2."IdTipoOperacion" = '0'
	WHERE emp."IdEmpleado" = 11439;	

select * 
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"
WHERE "IdEmpleado" = 11439;

	
select * 
FROM "BSCL.SocioNegocio.BD::EMP.Empleado" 
WHERE "IdEmpleado" = 11439;
set schema bscl;
update "BSCL.SocioNegocio.BD::EMP.Empleado" 
set "UtilizaPlantillaEmpleado" = 'S'
where "IdEmpleado" = 11439;
commit;
	
	
select * from "BSCL.Plataforma.BD::PLA.SolicitudCambio" where "IdTipoEntidad"='EMPLEADO' and "IdEntidadOrigen" = 11439;
delete from "BSCL.Plataforma.BD::PLA.SolicitudCambio" where "IdTipoEntidad"='EMPLEADO' and "IdEntidadOrigen" = 11439;
commit;

select * from "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 1580;


select * from "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion"where "IdTipoInstrumento" = 'BON'

-- AGRREGAR TIPOS DE MONEDA LIQUIDACION.
insert into "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion"
("IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion","EsPorDefecto")
select max("IdTipoInstrumentoMonedaLiquidacion")+1,'BON','PEN','PEN','N' from "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoMonedaLiquidacion";
commit;
insert into "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion"
("IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion","EsPorDefecto")
select max("IdTipoInstrumentoMonedaLiquidacion")+1,'BON','PEN','CLP','N' from "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoMonedaLiquidacion";
commit;


1;BBC;UF;CLP;PESO CHILENO

SELECT 
	"MonedaLiquidacion"."IdTipoInstrumentoMonedaLiquidacion"
	,"MonedaLiquidacion"."IdTipoInstrumento"
	,"MonedaLiquidacion"."IdMoneda"
	,"MonedaLiquidacion"."IdMonedaLiquidacion"
	,"Moneda"."Nombre" as "NombreMonedaLiquidacion"
FROM  "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" AS "MonedaLiquidacion"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" as "Moneda" on "MonedaLiquidacion"."IdMonedaLiquidacion" = "Moneda"."IdMoneda"

select * from  "BSCL.Plataforma.BD::PLA.Moneda"
select * from  "BSCL.Plataforma.BD::INS.Instrumento" where "Nemotecnico" = 'COLOM0628'
select * from  "BSCL.Plataforma.BD::INS.Instrumento" where "IdMoneda" = 'PEN'


SELECT * FROM "BSCL.Plataforma.BD.Vistas::VAL.ConsultarBonos" 
where "IdMoneda" = 'PEN' and "FechaVencimiento" >'2017-05-15';

select * from "BSCL.Plataforma.BD::CRE.ModuloEmpresa" where "IdModulo" = 'SIS';

update "BSCL.Plataforma.BD::CRE.ModuloEmpresa" set "TipoEstado" = 'A' where "IdModulo" = 'SIS';
commit;



select * from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = 'TIPIFICAR_INSTRUMENTOS' ORDER BY "IdPrograma" desc;

select * from "BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta"

select  top 1 p."FechaEjecucionPrograma",a."UltimaFechaDeEjecucion",
			p."IdTipoPrograma",p."Descripcion",p."MaximoIntentos",p."Modulo",p."NombreClasePrograma", 
			p."TipoClasePrograma",p."EsPauta",p."Ejecucion",p."RequiereTipoPrograma",p."SiguienteTipoPrograma",p."IdPrograma", 
			p."IdGrupoParametro",p."IdAgenda",p."Intento",p."IdUsuario",p."IdEstadoPrograma",p."NombreEstadoPrograma",p."ResultadoPrograma", 
			p."FechaCreacionPrograma",p."FechaEjecucionPrograma",p."FechaFinPrograma",p."FechaSistemaPrograma",p."IdProgramaReprocesado",
			p."IdControlPauta",p."FechaSistema",p."FechaCreacion",p."IdEstado",p."NombreEstado",p."Mensaje"
			from "BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta" p
			join "BSCL.Plataforma.BD::BAT.Agenda" a on p."IdTipoPrograma" = a."IdTipoPrograma"
			where p."IdTipoPrograma"= 'TIPIFICAR_INSTRUMENTOS' and CURRENT_UTCTIMESTAMP > coalesce(a."UltimaFechaDeEjecucion",'1900-01-01') 
			and p."FechaEjecucionPrograma"<=a."UltimaFechaDeEjecucion"
			and p."FechaEjecucionPrograma"!='0001-01-01 00:00:00.000' order by p."FechaEjecucionPrograma" desc; 
			
	
set schema bscl;
select * from "BSCL.Plataforma.BD::BAT.TipoPrograma" where "IdTipoPrograma" = 'VALORIZADOR';

select count(1) from "BSCL.Plataforma.BD::BAT.Programa" 
			
			
/***************************
 DETALLE COBRO PERSONALIZADO
 ***************************/
set schema bscl;
select * from "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" in (272,273);

set schema bscl;
alter table  "BSCL.Plataforma.BD::COM.ValorCobro"
add("EsPersonalizado" NVARCHAR(1) not null default 'N');

set schema bscl;
select * from "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" in (1337);

/***************************
 DETALLE COBRO PERSONALIZADO
 ***************************/
set schema bscl;

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CALCULAR','CL','Calcular','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALORIZAR_TIR','CL','Valorizar TIR','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALORIZAR_PRECIO','CL','Valorizar Precio','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CALCULAR','PE','Calcular','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALORIZAR_TIR','PE','Valorizar TIR','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALORIZAR_PRECIO','PE','Valorizar Precio','');

commit;

set schema bscl;
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (382,'PEN','GEN','BON','COMRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (383,'PEN','GEN','BON','VENRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');


set schema bscl;
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (382,'PEN','ORD','BON','COMRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (383,'PEN','ORD','BON','VENRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (384,'PEN','ORD','BON','COMRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (385,'PEN','ORD','BON','VENRF',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (386,'PEN','ORD','BON','',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (387,'PEN','GEN','BON','',4,'T',2,'T',2,'T',0,'T',4,'T',4,'T',4,'T','ACT');


select * from "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" where "IdTipoInstrumento" = 'BON'



commit;

select * from "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" where "IdTipoInstrumento" = 'BON'


select "PrecioOrden",* from "BSCL"."BSCL.Orden.BD::ORD.Orden" where "IdOrden" = 312437



set schema bscl;
select * from "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento"
where "IdTipoOperacion" like '%RF%' and "IdTipoInstrumento" = 'BON';

select * from "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" where "IdTipoInstrumento" = 'BON';

select max("IdBolsaTipoInstrumento") from "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 


INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdEstado" )  
VALUES(58,'BON','BCS','','N','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdEstado" )  
VALUES(59,'BON','BVL','','N','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdEstado" )  
VALUES(60,'BON','BEC','','N','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdEstado" )  
VALUES(61,'BON','BCV','','N','ACT');
commit;

insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa",
"GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
values (1,'MAN','AGE','BON','BCS','N','N',0);

insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa",
"GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
values (1313,'MAN','AGE','BON','BCS','N','N',0);

insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa",
"GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
values (1,'MAN','AGE','BON','BVL','N','N',0);

insert into "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
("IdEmpresa","IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa",
"GeneraMovimientoFacturacionAutomatico","GeneraComprobanteFacturacionAutomatico","MontoMaximoDistribucionOrden")
values (1313,'MAN','AGE','BON','BVL','N','N',0);

commit;

select * from "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion" where "IdTipoInstrumento" = 'BON';
-- select * from "BSCL.Orden.BD::ORD.TipoCanalDistribucion";


select * from "BSCL.Plataforma.BD::PLA.Moneda" order by "Orden";


--INCIDENCIAS EXPRESS:
--http://jira.bcs.cl/browse/OPTIMUSPE-141
--ORDEN CL:
	set schema bscl;
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 0 where "IdMoneda" = 'CLP';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 1 where "IdMoneda" = 'UF';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 2 where "IdMoneda" = 'USD';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 3 where "IdMoneda" = 'EUR';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 4 where "IdMoneda" = 'PEN';
	commit;
--ORDEN PE:
	set schema bscl;
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 0 where "IdMoneda" = 'PEN';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 1 where "IdMoneda" = 'USD';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 2 where "IdMoneda" = 'EUR';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 3 where "IdMoneda" = 'CLP';
	update "BSCL.Plataforma.BD::PLA.Moneda" set "Orden" = 4 where "IdMoneda" = 'UF';
	commit;

-- http://jira.bcs.cl/browse/OPTIMUSPE-178
-- PERSONA: Rango Patrimonio no obligatorio.
-- CLIENTE: Segmento cliente no obligatorio.
-- PORTAFOLIO: Propósito no obligatorio.

-- http://jira.bcs.cl/browse/OPTIMUSPE-196
-- ANALISIS:
	set schema bscl;
	select * from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad";
	select * from "BSCL.SocioNegocio.BD::PER.Persona" where  "IdPaisNacimiento" = 166; --"RazonSocial" like '%PROFUTURO%'
	select * from "BSCL.SocioNegocio.BD::PER.Persona" where "IdCarga">0 and "RazonSocial" like '%PROFUTURO%'
	select * from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 
	where "IdPersona" in (
		select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona" where  "IdPaisNacimiento" = 166)
	and "IdTipoDocumentoIdentidad" = 'R';	
--SOLUCION:
	update "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 
	set "IdTipoDocumentoIdentidad" = 'RUT'
	where "IdPersona" in (
		select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona" where  "IdPaisNacimiento" = 166)
	and "IdTipoDocumentoIdentidad" = 'R';
	commit;



-- ORDEN RF
set schema bscl;
select * from "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" in (1571);
select * from "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento";

--PARA FORMA DE NEGOCIACION:
set schema bscl;
select * from "BSCL.Orden.BD::ORD.SistemaBursatil";

CREATE COLUMN TABLE "BSCL"."BSCL.Orden.BD::ORD.PASOSistemaBursatil"(
"IdSistemaBursatil" nvarchar(40),"NombreSistemaBursatil" nvarchar(100));

insert into "BSCL"."BSCL.Orden.BD::ORD.PASOSistemaBursatil"("IdSistemaBursatil","NombreSistemaBursatil")
select "IdSistemaBursatil","NombreSistemaBursatil" from "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil";
commit;

select * from "BSCL"."BSCL.Orden.BD::ORD.PASOSistemaBursatil";
drop table "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil";

CREATE COLUMN TABLE "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil" (
	"IdSistemaBursatil"					nvarchar(40),
	"NombreSistemaBursatil"				nvarchar(100),
	"IdClasificacionTipoInstrumento" 	nvarchar(3),
	"IdEmpresa" 						bigint,
	PRIMARY KEY ("IdSistemaBursatil","IdEmpresa")
);

insert into "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil"(
"IdSistemaBursatil","NombreSistemaBursatil","IdClasificacionTipoInstrumento","IdEmpresa")
select "IdSistemaBursatil","NombreSistemaBursatil",null,1 
from "BSCL"."BSCL.Orden.BD::ORD.PASOSistemaBursatil";
commit;

select * from "BSCL.Orden.BD::ORD.SistemaBursatil";

insert into "BSCL"."BSCL.Orden.BD::ORD.SistemaBursatil"(
"IdSistemaBursatil","NombreSistemaBursatil","IdClasificacionTipoInstrumento","IdEmpresa")
select 'NEGOCIACIONCONTINUA','NEGOCIACIÓN CONTINUA','RF',1313 from dummy union all
select 'MERCADODELDINERO','MERCADO DEL DINERO','RF',1313 from dummy;
commit;

SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil";
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil" AS 
	select
		"IdSistemaBursatil",
		"NombreSistemaBursatil",
		"IdClasificacionTipoInstrumento",
		"IdEmpresa"
	from "BSCL.Orden.BD::ORD.SistemaBursatil";
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

select * from "BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil";

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") 
values (3330,'BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil',1,'Q','','','');
commit;
select * from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio"=3330;


-- BUG  
-- http://jira.bcs.cl/browse/IMPCREDOPT-395
set schema bscl;
select * from "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" where "NombreCorto" = 'BTU';

DESA
IdEmisor;NombreCorto;CodigoEmisor;CodigoRegulador;Regulador;EmiteIIF;EmiteIRF;EmiteFungible;ValorClasificacionRiesgoIIF;IdEntidadClasificadoraRiesgo;IndicadorAGF;IdPersona;IdEstado;FechaActualizacion;DocumentoIdentidad;TipoDocumentoIdentidad;NombreCompleto;TipoEntidad
BTU;BTU;KX;BTU;SVS;N;S;N;;;N;1.766.287;ACT;01-06-2017 19:35:52.0;60805000-0;R;BONO TESORERIA GRAL.REP. CHILE EN PESOS;1

MARCHA BLANCA
IdEmisor;NombreCorto;CodigoEmisor;CodigoRegulador;Regulador;EmiteIIF;EmiteIRF;EmiteFungible;ValorClasificacionRiesgoIIF;IdEntidadClasificadoraRiesgo;IndicadorAGF;IdPersona;IdEstado;FechaActualizacion;DocumentoIdentidad;TipoDocumentoIdentidad;NombreCompleto;TipoEntidad
BTU;BTU;KX;BTU;SVS;N;S;N;;;N;219.409;ACT;02-06-2017 8:46:42.0;60805000-0;R;TESORERIA GENERAL DE LA REPUBLICA;1


select "IdTipoReajuste",* from "BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'BON';

select * from "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" 
where "IdTipoInstrumento" = 'BON' and "Nemotecnico" = 'CONTI5BC2A';

set schema bscl;
select * FROM "BSCL.Plataforma.BD::INS.Instrumento" inst 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste" as ITI on ITI."IdInstrumento" = inst."IdInstrumento"
JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TR on (TR."IdTipoReajuste" = case when tipoins."IdClasificacionTipoInstrumento" = 'IF' then ITI."IdTipoReajuste" else inst."IdTipoReajuste" end)
where inst."Nemotecnico" = 'CONTI5BC2A';

select "IdTipoReajuste","IdMoneda",* FROM "BSCL.Plataforma.BD::INS.Instrumento" where "Nemotecnico" = 'CONTI5BC2A';

select "IdTipoReajuste",* from "BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'BON';

update "BSCL.Plataforma.BD::INS.Instrumento" 
set "IdTipoReajuste" = "IdMoneda"
where "IdTipoInstrumento" = 'BON';
commit;

select * from "BSCL.Plataforma.BD::INS.Instrumento" 
where "IdTipoInstrumento" = 'BON' and "Nemotecnico" = '';

select * from "BSCL.Plataforma.BD::PLA.TipoReajuste";
insert into "BSCL.Plataforma.BD::PLA.TipoReajuste"("IdTipoReajuste","Codigo","Nombre","Reajustable","IdEstado")
values('PEN','PEN','NUEVO SOL PERUANO','N','ACT');
commit;
select * from "BSCL.Plataforma.BD::PLA.TipoReajuste";


-- AGREGAR FILTRO MANUAL EN VENTANA DE ORDENES POR NEGOCIAR.
select * from "BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
where "Controlador" = 'OrdenesPorNegociarCtrl' and "NombreObjeto" = 'combobox-clasificacion-tipo-instrumento';
/*IdConfiguracionFiltroInterfaz;Controlador;TipoObjeto;NombreObjeto;IdEstado;Filtros
16;OrdenesPorNegociarCtrl;ComboBox;combobox-clasificacion-tipo-instrumento;ACT;{"logic": "or",
	    			"filters": [
	    			            {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
	    			            {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "DV"}
	    		    ]} 
*/

delete from "BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
where "Controlador" = 'OrdenesPorNegociarCtrl' and "NombreObjeto" = 'combobox-clasificacion-tipo-instrumento';
commit;

set schema bscl;
drop table #t;
create local temporary table #t("IdInterno" BIGINT,"IdOrden" BIGINT,"IdBolsa" NVARCHAR(3));
insert into #t
select 312609,312609,'' from dummy; 
commit;
--{"p_IdEmpresa":1313,"p_IdUsuario":89,"p_IdModulo":"ORD","p_Ordenes":[{"IdInterno":312609,"IdOrden":312609,"IdBolsa":""}]}

call "BSCL.Orden.BD.Servicios::CantarOrdenes" ( 
    p_IdEmpresa => 1313,
    p_IdUsuario => 89,
    p_IdModulo =>'ORD',
	p_Ordenes => #t,
	p_out_InformacionContador =>?,
	p_out_InformacionDetalle =>?,
	p_out_AdvertenciasContador =>?,
	p_out_AdvertenciasDetalle =>?,
	p_out_ErroresContador =>?,
	p_out_ErroresDetalle =>?
);

CALL "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
	p_Ordenes				=> #t,	
	p_NuevoEstado			=> 'OFE',
	p_out_SalidaDetalle		=> ?,
	p_out_SalidaResumen		=> ?
);

"BSCL.Orden.BD.Servicios::NotificarEstadoOrden" ( 
    IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3),
	
	IN p_Ordenes						  table ("IdOrden" BIGINT)
	)



--PIRUETAS VALORIZADOR	
set schema bscl;

select * from "BSCL.Plataforma.BD::PLA.Configuracion" 
select * from "BSCL.Plataforma.BD::MER.ParidadYTipoCambio";
select * from "BSCL.Plataforma.BD::MER.ParametroFinanciero"; 
select * from "BSCL.Plataforma.BD::VAL.ConfigValorizador";
SELECT * FROM "BSCL.Plataforma.BD::VAL.Config";

ALTER SYSTEM CANCEL SESSION '316715';

drop table #t;
create local temporary table #t(
"IdInterno"				BIGINT,
"IdPortafolio" 			BIGINT, 
"IdTipoProducto" 		NVARCHAR(4), 
"IdTipoOperacion" 		NVARCHAR(6), 
"IdCanalIngreso" 		NVARCHAR(4),
"IdBolsa" 					NVARCHAR(3),
"IdCorredorCorresponsal" 	BIGINT
);
insert into #t select 4095 as "IdInterno",1450115 as "IdPortafolio",'INRF','COMRF','AGE','BCS',null as "IdCorredorCorresponsal" from dummy;
commit;
CALL "BSCL.Orden.BD.Servicios::ConsultarCobrosPortafolios"(
	p_IdEmpresa				=> 1,
	p_IdUsuario				=> 87,
	p_IdModulo				=> 'PLA',
	p_Portafolios			=> #t,
	p_out_MatrizOperacional	=> ?,
	p_out_AdvertenciasContador	=> ?,
	p_out_AdvertenciasDetalle	=> ?,
	p_out_ErroresContador		=> ?,
	p_out_ErroresDetalle		=> ?);
	
	

-- INCIDENCIA: http://jira.bcs.cl/browse/SAP-5774	
-- Revisar que no exista una autorización de nivel inferior pendiente.
set schema bscl;	
select * from "BSCL.Plataforma.BD::AUT.TipoEvento" where "IdTipoEvento" in ('ENROL','ENRPER','NUEPOR') and "IdEmpresa" = 1;
update "BSCL.Plataforma.BD::AUT.TipoEvento" set "HorasVigencia" = 24
where "IdTipoEvento" in ('ENROL','ENRPER','NUEPOR'); --and "IdEmpresa" = 1;
commit;
select "SPConfirma",* from "BSCL.Plataforma.BD::AUT.TipoEvento" where "IdTipoEvento" in ('ENROL','ENRPER','NUEPOR') and "IdEmpresa" = 1;

update "BSCL.Plataforma.BD::AUT.TipoEvento" set "SPFormato" = 8 where "IdTipoEvento" in('ENROL');-- and "IdEmpresa" = 1;
commit;

--orden:
--BSCL.Portafolio.BD.Servicios::ConfirmarCrearPortafolioMasivo
--BSCL.SocioNegocio.BD.Servicios::CLI.ConfirmaAutorizacionEnrolamientoMasivo
--BSCL.SocioNegocio.BD.Servicios::PER.ConfirmaAutorizacionEnrolamientoPersonaMasivo

select "EsEnrolado","IdEstado",* from "BSCL.SocioNegocio.BD::PER.Persona" where "IdPersona" in (
  select "IdPersona" from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" where "DocumentoIdentidad" = '59704977-3'
);
  
select "EsEnrolado","IdEstado",* from "BSCL.SocioNegocio.BD::CLI.Cliente" where "IdPersona" in (
  select "IdPersona" from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" where "DocumentoIdentidad" = '59704977-3'
);

select "IdEstadoAutorizacion",* from "BSCL.Portafolio.BD::POR.Portafolio" where "IdCliente" in (
  select "IdCliente" from "BSCL.SocioNegocio.BD::CLI.Cliente" where "IdPersona" in (
    select "IdPersona" from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" where "DocumentoIdentidad" = '59704977-3')
);

drop table #tpor;
create local temporary table #tpor("IdPortafolio"	BIGINT);
insert into #tpor values(1450130);
commit;
call "BSCL"."BSCL.Portafolio.BD.Servicios::ConfirmarCrearPortafolioMasivo"(
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 1,
	p_IdModulo						=> 'SOC',
	p_UsaReglasNegocio	 			=> 'S',
	p_Portafolio					=> #tpor,
	p_IdEstadoAutorizacion			=> 'AUT',	
	p_out_AdvertenciasContador 		=> ?,
	p_out_AdvertenciasDetalle 		=> ?,
	p_out_ErroresContador 			=> ?,
	p_out_ErroresDetalle 			=> ?
);
rollback;


SELECT cli.*
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI on POR."IdCliente" = CLI."IdCliente"
JOIN #vm P on P."IdPortafolio" = POR."IdPortafolio"
WHERE CLI."EsEnrolado" != 'S';


SELECT * FROM "BSCL.Plataforma.BD::BAT.TipoPrograma";

set schema bscl;

SELECT * FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" tp
join "BSCL.Plataforma.BD::BAT.Agenda" a on tp."IdTipoPrograma" = a."IdTipoPrograma"
where tp."EsPauta" = 'S'
and tp."IdTipoPrograma" = 'ENTREGAR_SISTEMA';


SELECT * FROM "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = 'ENTREGAR_SISTEMA';

update "BSCL.Plataforma.BD::BAT.Programa"
set "fechaEjecucion" = CURRENT_UTCTIMESTAMP
where "IdTipoPrograma" = 'ENTREGAR_SISTEMA' and "fechaEjecucion">CURRENT_UTCTIMESTAMP
and "idEstado" = 'PEN';
commit;